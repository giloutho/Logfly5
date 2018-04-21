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

import com.serialpundit.core.SerialComPlatform;
import com.serialpundit.core.SerialComSystemProperty;
import com.serialpundit.serial.SerialComManager;
import geoutils.position;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Level;
import javafx.collections.ObservableList;
import model.Gpsmodel;
import systemio.mylogging;
import waypio.pointRecord;

public class flytec15 {
    
    private SerialComManager scm;
    private int osType;
    private long handle;   
    private String serialPortName;    
    private String lstVols;
    private StringBuilder sbError = null;
    private String deviceId;
    private ArrayList<String> listPBRWP;
    private ArrayList<pointRecord> wpreadList;       
    private DecimalFormatSymbols decimalFormatSymbols = new DecimalFormatSymbols();      
    
    public flytec15() throws Exception {
        // Create and initialize serialpundit
        scm = null;    
        SerialComPlatform scp = new SerialComPlatform(new SerialComSystemProperty());
        osType = scp.getOSType();
    }

    public String getDeviceId() {
        return deviceId;
    }
    
    public ArrayList<pointRecord> getWpreadList() {
        return wpreadList;
    }    

    public void setListPBRWP(ArrayList<String> listPBRWP) {
        this.listPBRWP = listPBRWP;
    }    
    
    public String getError() {
        if (sbError != null)
            return sbError.toString();
        else
            return "No Error message";
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
                    deviceId = data.replaceAll("\r\n", "");
                    res = true;
                } else {
                    sbError = new StringBuilder("GPS answer not splited : "+data);
                }
            } else {
                sbError = new StringBuilder("No GPS answer (GetDeviceInfo)");
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
            if(osType == SerialComPlatform.OS_WINDOWS) {
                // For Windows the exclusiveOwnerShip must be true as it does not allow sharing COM ports
                handle = scm.openComPort(serialPortName, true, true, true);
            } else {
                handle = scm.openComPort(serialPortName, true, true, false);
            }
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
    public boolean isPresent(String namePort) {
        boolean res = false;
        try {
            // open and configure serial port
            serialPortName = namePort;
            scm = new SerialComManager();   
            if(osType == SerialComPlatform.OS_WINDOWS) {
                // For Windows the exclusiveOwnerShip must be true as it does not allow sharing COM ports
                handle = scm.openComPort(serialPortName, true, true, true);
            } else {
                handle = scm.openComPort(serialPortName, true, true, false);
            }          
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
    
    private void getListPBRWP() throws Exception {        
        boolean exit = false;
        String data = null;
        String repGps = "";
        String req = ("ACT_31_00")+"\r\n";     
        scm.writeString(handle, req, 0);
        Thread.sleep(300);               
        while(exit == false) {
            data  = scm.readString(handle);
            if(data != null) {                                                                 
                repGps += data;  
                System.out.println(data);             
            }
            else {
                exit = true;
                break;
            }
        }               
        if (repGps != null) {
            listPBRWP = new ArrayList<String>(Arrays.asList(repGps.split("\r\n")));
        }
    }    
    
   public int getListWaypoints() {
        int res = 0;
        wpreadList = new ArrayList<>();
        String sLat;
        String sLong;  
        String sPref;
        String sAlt;
        String sBalise;
        String sDesc;
        String balAlt;
        int iAlt;
        
        try {
            getListPBRWP();
            if (!listPBRWP.isEmpty()) {
                for (int i = 0; i < listPBRWP.size(); i++) {                      
                    String ligPFM = listPBRWP.get(i);
                    // ligPFM is somrthing like S03             ;N   4'25.130;W  76'07.610;   950;   400
                    String[] partWp = ligPFM.split(";");
                    if (partWp.length > 4) {
                        sAlt = partWp[3].trim();
                        iAlt = Integer.parseInt(sAlt);
                        balAlt = String.format("%03d",(int) iAlt/10);                        
                        sDesc = partWp[0].trim();
                        // short name build
                        if (sDesc.length() > 3)
                            sBalise = sDesc.substring(0, 3).toUpperCase();
                        else
                            sBalise = sDesc;
                        sBalise = sBalise+balAlt;
                        sLat = decodeLat(partWp[1]);
                        sLong = decodeLong(partWp[2]);
                        pointRecord myPoint = new pointRecord(sBalise, sAlt, sDesc);
                        myPoint.setFLat(sLat);
                        myPoint.setFLong(sLong);
                        myPoint.setFIndex(i);
                        wpreadList.add(myPoint);                                                     
                    }
                }
                res = wpreadList.size();                
            }
        } catch (Exception e) {
            sbError = new StringBuilder(this.getClass().getName()+"."+Thread.currentThread().getStackTrace()[1].getMethodName());
            sbError.append("\r\n").append(e.toString());
            mylogging.log(Level.SEVERE, sbError.toString());            
        }
        
        return res;
    }        
    
    public void sendWaypoint() throws Exception {
        String ligPBRWP; 
        boolean exit;
        String repGPS;   
        String data;
        for (int i = 0; i < listPBRWP.size(); i++) {            
            ligPBRWP = listPBRWP.get(i);   
            exit = false;
            repGPS = null;            
            String req = ("ACT_32_00")+"\r\n";
            scm.writeString(handle, req, 0);            
            System.out.println("Envoi "+ligPBRWP);
            scm.writeString(handle, ligPBRWP, 0);
            Thread.sleep(500);             // necessary with shorter delay, we loss data  
            while(exit == false) {
                data  = scm.readString(handle);
                if(data != null) {                                                                 
                    repGPS += data;  
                    if (repGPS.contains("Done"))
                        exit = true;
                    else if (repGPS.contains("full list"))
                        exit = true;
                    else if (repGPS.contains("Syntax Error"))
                        exit = true;
                    else if (repGPS.contains("already exist"))
                        exit = true;                    
                }
                else {
                    exit = false;
                    break;
                }
            }    
            System.out.println("repGPS : "+repGPS);            
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
    
    /**
     * sLat is a string like N   4'25.130
     * @param sLat
     * @return 
     */
    private String decodeLat(String sLat) {
        String res = "";
        String sDeg;
        String sMn;
        String sHem;
        String sCoord;
        DecimalFormat df2;       
                       
        decimalFormatSymbols.setDecimalSeparator('.');       
        df2 = new DecimalFormat("#0.00000", decimalFormatSymbols);        
        
        try {
            sHem = sLat.substring(0,1);
            if (sLat.length() > 10 && (sHem.equals("N") || sHem.equals("S"))) {
                sCoord = sLat.substring(1, sLat.length()).trim();
                String[] sLatApo = sCoord.split("'");
                if (sLatApo.length > 1) {
                    sDeg = sLatApo[0];
                    sMn = sLatApo[1];
                    if (systemio.checking.parseDouble(sMn) && systemio.checking.checkInt(sDeg)) {   
                        position myPos = new position();
                        myPos.setLatDegres(Integer.parseInt(sDeg));
                        myPos.setHemisphere(sHem);
                        myPos.setLatMin_mm(Double.parseDouble(sMn));               
                        res = df2.format(myPos.getLatitude());
                    } else {
                        res = "00.00000";
                    }
                }else {
                        res = "00.00000";
                }
            } else {
                        res = "00.00000";
            }            
        } catch (Exception e) {
            res = "00.00000";
        }            
        
        return res;
    }    
    
    /**
     * sLong is a string like W  76'07.610
     * @param sLong
     * @return 
     */
    private String decodeLong(String sLong) {
        String res = "";
        String sCoord;        
        String sDeg;
        String sMn;
        String sMer;
        DecimalFormat df3;       
                       
        decimalFormatSymbols.setDecimalSeparator('.');       
        df3 = new DecimalFormat("##0.00000", decimalFormatSymbols);        
        
        try {
            sMer = sLong.substring(0,1);
            if (sLong.length() > 10 && (sMer.equals("W") || sMer.equals("E"))) {
                sCoord = sLong.substring(1, sLong.length()).trim();
                String[] sLongApo = sCoord.split("'");
                if (sLongApo.length > 1) {
                    sDeg = sLongApo[0];
                    sMn = sLongApo[1];
                    if (systemio.checking.parseDouble(sMn) && systemio.checking.checkInt(sDeg)) {   
                        position myPos = new position();
                        myPos.setLongDegres(Integer.parseInt(sDeg));
                        myPos.setMeridien(sMer);
                        myPos.setLongMin_mm(Double.parseDouble(sMn));               
                        res = df3.format(myPos.getLongitude());
                    } else {
                        res = "000.00000";
                    }
                }else {
                        res = "000.00000";
                }
            } else {
                        res = "000.00000";
            }            
        } catch (Exception e) {
            res = "000.00000";
        }            
        
        return res;
    }        
}
