/*
 * Copyright Gil THOMAS
 * This file forms an integral part of Logfly project
 * See the LICENSE file distributed with source code
 * for details of Logfly licence project
 */
package gps;

/**
 *
 * Communication with Flymaster SD series
 * @author Gil and Rishi Gupta https://github.com/RishiGupta12/SerialPundit
 * without Rishi help, this class doesn't exist
 * 
 * Init           : call GPS id and raw flight list. True if GPS answers
 *                  serial port is closed
 * IniForFlights  : requires to open again the serial port (without flight list request)
 * getListPBRTL   : raw flight list stored in a ArrayList
 * getListFlights : called by GPSViewController for extracted flight list 
 * getIGC         : called by GPSViewController for extracted flight track in IGC format
 */

import com.serialpundit.serial.SerialComManager;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.logging.Level;
import javafx.collections.ObservableList;
import model.Gpsmodel;
import systemio.mylogging;

public class flytec15 {
    
    private SerialComManager scm;
    private long handle;   
    private String serialPortName;    
    private String lstVols;
    private StringBuilder sbError;
    
    public flytec15() throws Exception {
        // Create and initialize serialpundit
        scm = null;        
    }
    
    /**
     * Get information id about device. Keep reading until timeout happens.
     * @throws Exception 
     */
    private boolean getDeviceInfo(boolean listPFM) throws Exception {
        boolean res = false;
        String rep;

        String req = req =  "ACT_BD_00"+"\r\n";
        res = false;
        scm.writeString(handle, req, 0);

        // give some time to GPS to send data to computer. We do not depend upon 100 because we also used 
        Thread.sleep(100);        

        // GPS send Flytec 6015 or IQ-Basic GPS
        String data = scm.readString(handle);
        if (data != null && !data.isEmpty()) {
             String[] tbdata = data.split(" ");
            if (tbdata.length > 0) {
                if (tbdata[0].equals("Flytec") || tbdata[0].equals("IQ-Basic")) {                
                    res = true;
                }
            } else {
                res = false;
            }
        }    
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
            lstVols = null;
            // open and configure serial port
            serialPortName = namePort;
            scm = new SerialComManager();   
            handle = scm.openComPort(serialPortName, true, true, false);
            scm.configureComPortData(handle, SerialComManager.DATABITS.DB_8, SerialComManager.STOPBITS.SB_1, SerialComManager.PARITY.P_NONE, SerialComManager.BAUDRATE.B57600, 0);
            
            scm.configureComPortControl(handle, SerialComManager.FLOWCONTROL.XON_XOFF, (char) 0x11, (char)0x13, false, false);                      

            // Prepare serial port for burst style data read of 500 milli-seconds timeout
            // scm.fineTuneReadBehaviour(handle, 0, 5, 100, 5, 200);
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
            sbError = new StringBuilder(this.getClass().getName()+"."+Thread.currentThread().getStackTrace()[1].getMethodName());
            sbError.append("\r\n").append(e.toString());
            mylogging.log(Level.SEVERE, sbError.toString());
        }
        return res;        
    }
    
    /**
     * raw flight list request.
     * output format 0; 17.07.06; 16:37:44;        2; 00:17:27;       95;     2005;     1055;        0.89;       -3.82;       14.00;Pilot Name        ;Glider name         ;not-set 
     * @throws Exception 
     */
    private void getListPBRTL() throws Exception {
        boolean exit = false;
        String data;
        lstVols = null;
        
        String req = ("ACT_20_00")+"\r\n";
        scm.writeString(handle, req, 0);
        Thread.sleep(100);

        scm.writeString(handle, req, 0);
        while(exit == false) {
            data  = scm.readString(handle);
            if(data != null) {                                                                 
                lstVols += data;  
                if (lstVols.contains("Done"))
                    exit = true;
            }
            else {
                exit = true;
                break;
            }
        }    
    }
    
    /**
     * raw flight list decoding  [$PFMLST,025,025,28.06.16,12:33:05,01:15:10*35]
     * for an ObservableList used by controller's tableview
     * @param listFlights 
     */
    public void getListFlights(ObservableList <Gpsmodel> listFlights) {        
        DateTimeFormatter df = DateTimeFormatter.ofPattern("dd.MM.YY");
        
        if (lstVols != null)  {            
            String[] tbVols = lstVols.split("[\\r\\n]+");
            if (tbVols.length > 0) {
                for(int x=0; x<tbVols.length; x++) {    
                    //  tbVols[x] -> 0; 17.07.06; 16:37:44;        2; 00:17:27;       95;     2005;     1055;        0.89;       -3.82;       14.00;Pilot Name        ;Glider name         ;not-set 
                    // spaces must be removed -> st.replaceAll("\\s+","") 
                    //  tbVols[x] -> $PFMLST,003,001,13.01.16,12:24:05,00:08:43*35
                    String vol[] = tbVols[x].split(";");
                    if (vol.length > 4) {      
                        try {                                                    
                            Gpsmodel oneFlight = new Gpsmodel();                                             
                            oneFlight.setChecked(false);
                            // date formatting  Output is : YY.MM.DD
                            String sDate = vol[1].replaceAll("\\s+","");
                            StringBuilder sbDate = new StringBuilder();
                            sbDate.append("20").append(sDate.replace(".","-"));                            
                            LocalDate lDate = LocalDate.parse(sbDate.toString());
                            //oneFlight.setDate(vol[1].replaceAll("\\s+",""));  
                            oneFlight.setDate(lDate.format(df));  
                            oneFlight.setHeure(vol[2].replaceAll("\\s+",""));
                            oneFlight.setCol4(vol[4].replaceAll("\\s+",""));    // duration
                            // Building specific download instruction of this flight 
                            // s = "ACT_21_"+Tb_Vol_Load(0).sNumVol+EndOfLine.Windows  
                            StringBuilder sbVol = new StringBuilder();
                            sbVol.append("ACT_21_");
                            // a null beginning is possible
                            String numFlight = vol[0].replace("null", "");
                            int iNumFlight = Integer.valueOf(numFlight.replaceAll("\\s+",""));                        
                            sbVol.append(String.format("%02X", iNumFlight));
                            sbVol.append("\r\n");
                            oneFlight.setCol5(sbVol.toString());  
                            listFlights.add(oneFlight);                               
                        } catch (Exception e) { 
                            sbError = new StringBuilder(this.getClass().getName()+"."+Thread.currentThread().getStackTrace()[1].getMethodName());
                            sbError.append("\r\n").append(e.toString());
                            mylogging.log(Level.SEVERE, sbError.toString());
                        }
                    }
                }                
            }
        }                                
    } 
    
    public String getIGC(String req)  {
        String res = null;
        boolean exit = false;
        String data;
        String sIGC = "";
        
        try {            
            scm.writeString(handle, req, 0);
             // give some time to GPS to send data to computer. We do not depend upon 100 because we also used 
            Thread.sleep(100);
            while(exit == false) {
            data  = scm.readString(handle);
            if(data != null) {   
                sIGC += data;                  
            }
            else {
                exit = true;
                break;
            }
        }                         
        } catch (Exception e) {
            res = null;
            sbError = new StringBuilder(this.getClass().getName()+"."+Thread.currentThread().getStackTrace()[1].getMethodName());
            sbError.append("\r\n").append(e.toString());
            mylogging.log(Level.SEVERE, sbError.toString());            
        }        
                
        res = sIGC;
        
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
    
    
}
