/*
 * Copyright Gil THOMAS
 * This file forms an integral part of Logfly project
 * See the LICENSE file distributed with source code
 * for details of Logfly licence project
 */
package gps;

import com.serialpundit.core.SerialComPlatform;
import com.serialpundit.core.SerialComSystemProperty;
import com.serialpundit.serial.SerialComManager;
import java.util.ArrayList;
import java.util.logging.Level;
import javafx.collections.ObservableList;
import model.Gpsmodel;
import systemio.mylogging;
import waypio.pointRecord;

/**
 * @author Alessandro Faillace
 * Decoration Gil
 */
 
public class digifly {
    
    private SerialComManager scm;
    private int osType;
    private long handle;
    private String serialPortName;
    private String deviceType;
    private String deviceSerial;
    private String deviceFirm;
    private String flightInfo;
    private ArrayList<String> listPFM;
    private ArrayList<String> listPOS;
    private ArrayList<String> listPFMWP;
    private ArrayList<pointRecord> wpreadList;  
    private StringBuilder sbRead;      
    private final int iBufLen = 6144;

    private boolean mDebug;
    private String debugPath;    
    private StringBuilder sbError = null;
    
    public digifly(boolean pDebug, String pDebugPath) throws Exception {
        // Create and initialize serialpundit. Note 'private final' word before variable name.
        scm = null;     
        SerialComPlatform scp = new SerialComPlatform(new SerialComSystemProperty());
        osType = scp.getOSType();
        mDebug = pDebug;
        debugPath = pDebugPath;
    }    
    
    public String getDeviceType() {
        return deviceType;
    }

    public String getDeviceSerial() {
        return deviceSerial;
    }

    public String getDeviceFirm() {
        return deviceFirm;
    }    
    
    /**
     * Initialize the serial port and ask Gps ID
     * @param namePort
     * @return 
     */
    public boolean isPresent(String namePort) {
        boolean res = false;
        listPFMWP = new ArrayList<String>();
        try {
            // open and configure serial port
            serialPortName = namePort;
            scm = new SerialComManager();
            handle = scm.openComPort(serialPortName, true, true, true);            
            scm.configureComPortData(handle, SerialComManager.DATABITS.DB_8, SerialComManager.STOPBITS.SB_1, SerialComManager.PARITY.P_NONE, SerialComManager.BAUDRATE.B57600, 0);
            scm.configureComPortControl(handle, SerialComManager.FLOWCONTROL.NONE, 'x', 'x', false, false);
            // Normally this instruction should not be a problem for Windows, it's special parameters for Windows !!!
           // if(osType != SerialComPlatform.OS_WINDOWS) {
                // Prepare serial port for burst style data read of 500 milli-seconds timeout
                // This line is a problem with Windows
                scm.fineTuneReadBehaviour(handle, 0, 5, 100, 5, 200);
                // scm.fineTuneReadBehaviour(handle, 0, 3000, 0, 0, 0);
          //  }
            if (getDeviceInfo(false)) {
                res = true;
            } else {
                scm.closeComPort(handle);
            } 
        } catch (Exception e) {
            sbError = new StringBuilder(this.getClass().getName()+"."+Thread.currentThread().getStackTrace()[1].getMethodName());
            sbError.append("\r\n").append(e.toString());
            mylogging.log(Level.SEVERE, sbError.toString());
        }
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
            listPFM = new ArrayList<String>();
            // open and configure serial port
            serialPortName = namePort;
            scm = new SerialComManager();
            handle = scm.openComPort(serialPortName, true, true, true);
            scm.configureComPortData(handle, SerialComManager.DATABITS.DB_8, SerialComManager.STOPBITS.SB_1, SerialComManager.PARITY.P_NONE, SerialComManager.BAUDRATE.B57600, 0);
            scm.configureComPortControl(handle, SerialComManager.FLOWCONTROL.NONE, 'x', 'x', false, false);

            // Normally this instruction should not be a problem for Windows, it's special parameters for Windows !!!
            //if(osType != SerialComPlatform.OS_WINDOWS) {
                // Prepare serial port for burst style data read of 500 milli-seconds timeout
                // This line is a problem with Windows
                scm.fineTuneReadBehaviour(handle, 0, 5, 100, 5, 200);
            //}
            // ID GPS request + raw flight list (true)
            if (getDeviceInfo(true)) {
                res = true;
            }   
            // Closing port mandatory
            scm.closeComPort(handle);
        } catch (Exception e) {
            sbError = new StringBuilder(this.getClass().getName()+"."+Thread.currentThread().getStackTrace()[1].getMethodName());
            sbError.append("\r\n").append(e.toString());
            mylogging.log(Level.SEVERE, sbError.toString());
        }
        
        return res;        
    }    
    
    public void closePort() {
        try {
            scm.closeComPort(handle);
        } catch (Exception e) {
            sbError = new StringBuilder(this.getClass().getName()+"."+Thread.currentThread().getStackTrace()[1].getMethodName());
            sbError.append("\r\n").append(e.toString());
            mylogging.log(Level.SEVERE, sbError.toString());
        }        
    }    
    
   /**
     * Get information id about device. Keep reading until timeout happens.
     * @throws Exception 
     */
    private boolean getDeviceInfo(boolean callListPFM) throws Exception {
        boolean res = false;
        String data;
        
        write_line("$PFMSNP,");   // MUST BE CORRECTED    

        // give some time to GPS to send data to computer. We do not depend upon 100 because we also used 
        Thread.sleep(200);
    
        // Answer must be something like : $PFMSNP,GpsSD,,02988,1.06j, 872.20,*3C

        int iLenData = read_line();
        if (iLenData > 0) {
            data = sbRead.toString();
        } else {
            data = null;
        }
        if (data != null && !data.isEmpty()) {
            int posPFMSNP = data.indexOf("$PFMSNP");
            String cleanData = data.substring(posPFMSNP,data.length());
            String[] tbdata = cleanData.split(",");
            if (tbdata.length > 0 && tbdata[0].contains("$PFMSNP")) {  
                deviceType = tbdata[1];
                deviceSerial = tbdata[3];
                deviceFirm = tbdata[4];   
                res = true;
            } else {
                sbError = new StringBuilder("GPS not splited : "+data);
                res = false;
            }
        } else {
            sbError = new StringBuilder("No GPS answer (GetDeviceInfo)");
        }    
        
        if (res && callListPFM) getRawFlightList();   
        
         
        return res;
    }     
    
    private void getRawFlightList() {
        write_line("$PFMDNL,LST");  // MUST BE CORRECTED
        while(read_line()>0)
        {
            listPFM.add(sbRead.toString());
        }        
    }
    
    /**
     * raw flight list decoding  for flymaster it's [$PFMLST,025,025,28.06.16,12:33:05,01:15:10*35]
     * for Diggifly it's ????
     * for an ObservableList used by controller's tableview
     * @param listFlights 
     */
    public void getListFlights(ObservableList <Gpsmodel> listFlights) {
        for (int i = 0; i < listPFM.size(); i++) {
            String ligPFM = listPFM.get(i);
            String[] cleanVol = ligPFM.split("\\*");
            String[] idVol = cleanVol[0].split(",");
            if (idVol.length > 0 ) {
                Gpsmodel oneFlight = new Gpsmodel();                                             
                oneFlight.setChecked(false);
                oneFlight.setDate(idVol[3]);
                // Building specific download instruction of this flight
                StringBuilder sbVol = new StringBuilder();
                sbVol.append("$PFMDNL,");
                String[] tbDate = idVol[3].split("\\.");
                if (tbDate.length == 3) {
                    sbVol.append(tbDate[2]).append(tbDate[1]).append(tbDate[0]);
                }
                oneFlight.setHeure(idVol[4]);
                sbVol.append(idVol[4].replaceAll(":", ""));
                sbVol.append(",2\n");
                oneFlight.setCol4(idVol[5]);
                oneFlight.setCol5(sbVol.toString());                
                listFlights.add(oneFlight);
            }     
        }        
    }    
    
    /**
     * 
     * @param gpsCommand
     * some additional parameters can be necessary
     * For Flytec, pilot name is the name stored in the GPS
     * For Flymaster, we must add the pilot name and the glider name
     * @return 
     */
    public boolean getIGC(String gpsCommand)  {        
        
        boolean res = false;
        // Flytec delivers directly an IGC string
        // Flymaster delivers raw data. 
        // They must be decoded 
        
        // For Flytec getIGC returns directly the string (see Flytec20.java)
        // For Flylmaster getIGC returns a boolean
        // if true, decoding function put final IGC string in finalIGC
        
       // ? finalIGC = null;
                
        try {
//            getFlightData(gpsCommand);
//            if (!listPOS.isEmpty()) {
//                makeIGC(strDate,strPilote, strVoile);
//                res = genIGC;
//            }else 
//            {
//                String dMsg = "Error Reading : "+strDate;
//                if (mDebug) mylogging.log(Level.INFO, dMsg);   
//                System.out.println(dMsg);
//            }
        } catch (Exception e) {
            sbError = new StringBuilder(this.getClass().getName()+"."+Thread.currentThread().getStackTrace()[1].getMethodName());
            sbError.append("\r\n").append(e.toString());
            mylogging.log(Level.SEVERE, sbError.toString());            
        }                
        return res;     
    }    
    
    private int read_line() {
        int iLen = 0;
        byte[] iRes = null;
        sbRead = new StringBuilder();
        
        try {
            // Windows requested
            Thread.sleep(100);
            iLen = 0;
            while (iLen < iBufLen) {
                iRes = scm.readBytes(handle, 1);   
                if (iRes != null)  {
                    char cData = (char) (iRes[0] & 0xFF);
                        if (iRes[0] > 0 && cData != '\n') {
                            if (cData != '\r') {
                                sbRead.append(cData);
                                iLen++;
                            }
                        } else {           
                            //  timed out
                            //System.out.println("ilen : "+iLen+" "+sbRead.toString());
                            break;
                        }
                } else {
                    break;
                }
            }
        } catch (Exception e) {
            sbError = new StringBuilder(this.getClass().getName()+"."+Thread.currentThread().getStackTrace()[1].getMethodName());
            sbError.append("\r\n").append(e.toString());
            mylogging.log(Level.SEVERE, sbError.toString()); 
        }
        
        return iLen;
    }    
    
    private void write_line(String reqDevice) {
        
        try {
            scm.writeString(handle, reqDevice, 0);  
            scm.writeString(handle, "\n", 0);   
        } catch (Exception e) {
            sbError = new StringBuilder(this.getClass().getName()+"."+Thread.currentThread().getStackTrace()[1].getMethodName());
            sbError.append("\r\n").append(e.toString());
            mylogging.log(Level.SEVERE, sbError.toString());            
        }
    }        
    
}
