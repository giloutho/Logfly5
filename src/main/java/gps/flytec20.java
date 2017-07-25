/*
 * Copyright Gil THOMAS
 * This file forms an integral part of Logfly project
 * See the LICENSE file distributed with source code
 * for details of Logfly licence project
 */
package gps;

import com.serialpundit.serial.SerialComManager;
import java.util.ArrayList;
import java.util.Arrays;
import javafx.collections.ObservableList;
import model.Gpsmodel;

/**
 *
 * @author Rishi Gupta https://github.com/RishiGupta12/SerialPundit
 * without Rishi help, this class doesn't exist
 * 
 */
public class flytec20 {
    
    private SerialComManager scm;
    private long handle;   
    private String serialPortName;
    private String deviceType;
    private String deviceSerial;
    private String deviceFirm;
    private ArrayList<String> listPBR;

    public String getDeviceType() {
        return deviceType;
    }

    public String getDeviceSerial() {
        return deviceSerial;
    }

    public String getDeviceFirm() {
        return deviceFirm;
    }
    
    
    
    public flytec20() throws Exception {
        // Create and initialize serialpundit. Note 'private final' word before variable name.
        scm = null;
    }
    
    /**
     * Get information id about device. Keep reading until timeout happens.
     * @throws Exception 
     */
    private boolean getDeviceInfo(boolean listPFM) throws Exception {
        boolean res = false;
        String rep;

        String req = ajouteChecksum("$PBRSNP,*")+"\r\n";
        res = false;
        scm.writeString(handle, req, 0);

        // give some time to GPS to send data to computer. We do not depend upon 100 because we also used 
        Thread.sleep(100);        

        rep = flAnswer(scm, handle);
        if (rep != null && !rep.isEmpty()) {
            String[] tbdata = rep.split(",");
            if (tbdata.length > 1 && tbdata[0].contains("$PBRSNP")) {  
                deviceType = tbdata[1];
                deviceSerial = tbdata[3];
                deviceFirm = tbdata[4];   
                res = true;
            } else {
                res = false;
            }
        }    
        System.out.println("GetdeviceInfo : "+deviceType+" "+deviceSerial+"\r\n");
        if (res && listPFM) getListPBRTL();                           
        
        return res;
    }
    
    /**
     * Initialize serial port and request GPS id [getDeviceInfo]
     * @param namePort
     * @return 
     */
    public boolean init(String namePort) {
        boolean res = false;
        try {
            listPBR = new ArrayList<String>();
            // open and configure serial port
            serialPortName = namePort;
            scm = new SerialComManager();   
            handle = scm.openComPort(serialPortName, true, true, false);
            scm.configureComPortData(handle, SerialComManager.DATABITS.DB_8, SerialComManager.STOPBITS.SB_1, SerialComManager.PARITY.P_NONE, SerialComManager.BAUDRATE.B57600, 0);
            
            scm.configureComPortControl(handle, SerialComManager.FLOWCONTROL.XON_XOFF, (char) 0x11, (char)0x13, false, false);                      

            // Prepare serial port for burst style data read of 500 milli-seconds timeout
            //   scm.fineTuneReadBehaviour(handle, 0, 5, 100, 5, 200);
            // ID GPS request + raw flight list (true)
            if (getDeviceInfo(true)) {
                res = true;
            }   
            // Closing port mandatory
            scm.closeComPort(handle);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return res;        
    }
    
    /**
     * Initialize the serial port for flight downloading operation
     * @param namePort
     * @return 
     */
    public boolean iniForFlights(String namePort) {
        boolean res = false;
        try {
            // open and configure serial port
            serialPortName = namePort;
            scm = new SerialComManager();   
            handle = scm.openComPort(serialPortName, true, true, false);            
            scm.configureComPortData(handle, SerialComManager.DATABITS.DB_8, SerialComManager.STOPBITS.SB_1, SerialComManager.PARITY.P_NONE, SerialComManager.BAUDRATE.B57600, 0);            
            scm.configureComPortControl(handle, SerialComManager.FLOWCONTROL.XON_XOFF, (char) 0x11, (char)0x13, false, false);            
            if (getDeviceInfo(false)) {
                res = true;
            }   
        } catch (Exception e) {
            e.printStackTrace();
        }
        return res;        
    }
    
    /**
     * raw flight list request.
     * output format Flymaster : $PFMLST,025,025,28.06.16,12:33:05,01:15:10*35
     * output format Flytec : $PBRTL,07,01,27.04.17,15:29:56,00:04:55*7D
     * @throws Exception 
     */
    private void getListPBRTL() throws Exception {

        String req = ajouteChecksum("$PBRTL,*")+"\r\n";
        scm.writeString(handle, req, 0);
        Thread.sleep(100);

        String lstVols = flAnswer(scm, handle);
        if (lstVols != null) {
            listPBR = new ArrayList<String>(Arrays.asList(lstVols.split("\r\n")));
        }
    }
    
    /**
     * raw flight list decoding  [$PFMLST,025,025,28.06.16,12:33:05,01:15:10*35]
     * raw flight list decoding  [$PBRTL,07,01,27.04.17,15:29:56,00:04:55*7D]
     *                                   07 total number of stored tracks
     *                                      01 actual number of track
     * for an ObservableList used by controller's tableview
     * @param listFlights 
     */
     public void getListFlights(ObservableList <Gpsmodel> listFlights) {
        for (int i = 0; i < listPBR.size(); i++) {
            String ligPBR = listPBR.get(i);
            String[] cleanVol = ligPBR.split("\\*");
            String[] idVol = cleanVol[0].split(",");
            if (idVol.length > 0 ) {
                Gpsmodel oneFlight = new Gpsmodel();                                             
                oneFlight.setChecked(false);
                oneFlight.setDate(idVol[3]);                
                oneFlight.setHeure(idVol[4]);                
                oneFlight.setCol4(idVol[5]);
                // Specific download instruction of this flight is builded
                String req = ajouteChecksum("$PBRTR,"+idVol[2]+"*")+"\r\n";
                oneFlight.setCol5(req);                
                listFlights.add(oneFlight);
            }     
        }        
    }        
    
    public String getIGC(String req)  {
        String res = null;
        try {            
            System.out.println("Requête : "+req);
            scm.writeString(handle, req, 0);
             // give some time to GPS to send data to computer. We do not depend upon 100 because we also used 
            Thread.sleep(100);
            String repGPS = flAnswer(scm, handle);             
            if (repGPS.length() > 24)  {   
            // First character (XOFF) must be removed 
                if (repGPS.charAt(0) == (char)19)  {            
                    repGPS = repGPS.substring(1);                
                }            
                res = repGPS;
                System.out.println(res.substring(0, 23));                
            }                        
        } catch (Exception e) {
            res = null;
        }        
        
        return res;        
    }
    
    static String flAnswer(SerialComManager scm, long handle)  {
        // We put a counter
        boolean exit = false;
        byte[] data = null;
        int index = 0;
        String sData = "";
        
        try {
            while(exit == false) {
                data = scm.readBytes(handle);                
                if(data != null) {
                    for(int x=0; x < data.length; x++) {                       
                        
                        if (data[x] != 17) {
                            sData += (char)data[x];
                            index++;
                        }else {
                            exit = true;
                            break;
                        }
                    }                        
                }
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        return sData;
    }
    
    /**
     * calculates checksum in the parameter string and adds this to the output 
     * @param s
     * @return 
     */
    private static String ajouteChecksum(String s) {
        String res = null;
        int chksum = 0;
        char[] charArray = s.toCharArray();
        for (int i=0; i<charArray.length; i++) {
            switch (charArray[i]) {
                case '$': break;
                case '*': break;
                default:  chksum = chksum ^ (int)charArray[i];
            }
            
        }
        res = s+Integer.toHexString(chksum);
               
        return res.toUpperCase();     
    }
    
    public void closePort() {
        try {
            scm.closeComPort(handle);
        } catch (Exception e) {
            e.printStackTrace();
        }        
    }
    
}
