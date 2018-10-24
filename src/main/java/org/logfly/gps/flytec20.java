/*
 * Copyright Gil THOMAS
 * This file forms an integral part of Logfly project
 * See the LICENSE file distributed with source code
 * for details of Logfly licence project
 */
package org.logfly.gps;

import com.serialpundit.core.SerialComPlatform;
import com.serialpundit.core.SerialComSystemProperty;
import com.serialpundit.serial.SerialComManager;

import static org.logfly.gps.gpsutils.ajouteChecksum;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Level;

import org.logfly.geoutils.position;
import org.logfly.model.Gpsmodel;
import org.logfly.systemio.mylogging;
import org.logfly.waypio.pointRecord;

import javafx.collections.ObservableList;

/**
 *
 * @author Rishi Gupta https://github.com/RishiGupta12/SerialPundit
 * without Rishi help, this class doesn't exist
 *  
 * Init           : call GPS id and raw flight list. True if GPS answers
 *                  serial port is closed
 * IniForFlights  : requires to open again the serial port (without flight list request)
 * getListPBRTL   : raw flight list stored in a ArrayList
 * getListFlights : called by GPSViewController for extracted flight list 
 * getIGC         : called by GPSViewController for extracted flight track in IGC format
 * 
 */
public class flytec20 {
    
    private SerialComManager scm;
    private int osType;
    private long handle;   
    private String serialPortName;
    private String deviceType;
    private String deviceSerial;
    private String deviceFirm;
    private ArrayList<String> listPBR;
    private ArrayList<String> listPBRWP;
    private ArrayList<pointRecord> wpreadList;    
    private StringBuilder sbError = null;
    private DecimalFormatSymbols decimalFormatSymbols = new DecimalFormatSymbols();       

    public String getDeviceType() {
        return deviceType;
    }

    public String getDeviceSerial() {
        return deviceSerial;
    }

    public String getDeviceFirm() {
        return deviceFirm;
    }
    
    public String getError() {
        if (sbError != null)
            return sbError.toString();
        else
            return "No Error message";
    }
    
    public ArrayList<pointRecord> getWpreadList() {
        return wpreadList;
    }    

    public void setListPBRWP(ArrayList<String> listPBRWP) {
        this.listPBRWP = listPBRWP;
    }
          
    
    public flytec20() throws Exception {
        // Create and initialize serialpundit
        scm = null;
        SerialComPlatform scp = new SerialComPlatform(new SerialComSystemProperty());
        osType = scp.getOSType();        
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
                String[] tbFirm = tbdata[4].split("\\*");
                if (tbFirm.length > 1)
                    deviceFirm = tbFirm[0];
                else
                    deviceFirm = tbdata[4];  
                res = true;
            } else {
                sbError = new StringBuilder("GPS answer not splited : "+rep);
                res = false;
            }
        } else {
            sbError = new StringBuilder("No GPS answer (GetDeviceInfo)");
            res = false;
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
            listPBR = new ArrayList<String>();
            // open and configure serial port
            serialPortName = namePort;
            scm = new SerialComManager();   
            handle = scm.openComPort(serialPortName, true, true, true);
            scm.configureComPortData(handle, SerialComManager.DATABITS.DB_8, SerialComManager.STOPBITS.SB_1, SerialComManager.PARITY.P_NONE, SerialComManager.BAUDRATE.B57600, 0);
            
            scm.configureComPortControl(handle, SerialComManager.FLOWCONTROL.XON_XOFF, (char) 0x11, (char)0x13, false, false);                      

            // Prepare serial port for burst style data read of 500 milli-seconds timeout
            //scm.fineTuneReadBehaviour(handle, 0, 5, 100, 5, 200);
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
            handle = scm.openComPort(serialPortName, true, true, true);            
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
     
    private void getListPBRWP() throws Exception {        
        
        String req = ajouteChecksum("$PBRWPS,*")+"\r\n";
        scm.writeString(handle, req, 0);
        Thread.sleep(100);

        String lstWayp = flAnswer(scm, handle);
        if (lstWayp != null) {
            listPBRWP = new ArrayList<String>(Arrays.asList(lstWayp.split("\r\n")));
        }
    }
     
    public int getListWaypoints() {
        int res = 0;
        wpreadList = new ArrayList<>();
        String sLat;
        String sLong;  
        String sPref;
        String[] sAlt;
        String sBalise;
        String sDesc;
        String balAlt;
        int iAlt;
        
        try {
            getListPBRWP();
            if (!listPBRWP.isEmpty()) {
                for (int i = 0; i < listPBRWP.size(); i++) {                      
                    String ligPFM = listPBRWP.get(i);
                    // ligPFM is somrthing like $PBRWPS,0425.130,N,07607.610,W,S03095,S03              ,0950*21
                    String[] partWp = ligPFM.split(",");
                    if (partWp.length > 7) {
                        sAlt = partWp[7].split("\\*");
                        if (sAlt.length > 1) {
                            iAlt = Integer.parseInt(sAlt[0]);
                            balAlt = String.format("%03d",(int) iAlt/10);
                            // Short name is defined by GPS device loading
                            sBalise = partWp[5];
                            sDesc = partWp[6];
                            sLat = decodeLat(partWp[1], partWp[2]);
                            sLong = decodeLong(partWp[3], partWp[4]);
                            pointRecord myPoint = new pointRecord(sBalise, sAlt[0], sDesc);
                            myPoint.setFLat(sLat);
                            myPoint.setFLong(sLong);
                            myPoint.setFIndex(i);
                            wpreadList.add(myPoint);                                                     
                        }
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
        for (int i = 0; i < listPBRWP.size(); i++) {            
            ligPBRWP = listPBRWP.get(i);
            scm.writeString(handle, ligPBRWP, 0);
            Thread.sleep(300);    // used in Logfly V4
        }        
    }    
    
    public String getIGC(String req)  {
        String res = null;
        try {            
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
            }                        
        } catch (Exception e) {
            sbError = new StringBuilder(this.getClass().getName()+"."+Thread.currentThread().getStackTrace()[1].getMethodName());
            sbError.append("\r\n").append(e.toString());
            mylogging.log(Level.SEVERE, sbError.toString());
            res = null;
        }        
        
        return res;        
    }
    
    private String flAnswer(SerialComManager scm, long handle)  {
        // We put a counter to avoid a dead loop if user isn't on flight list
        // is it a suitable solution ?
        boolean exit = false;
        byte[] data = null;
        int nbTrials=0;
        String sData = "";      
        try {
            while(exit == false) {
                data = scm.readBytes(handle);   
                nbTrials++;
                if(data != null) {   
                    nbTrials = 0;
                    for(int x=0; x < data.length; x++) {                                               
                        if (data[x] != 17) {
                            sData += (char)data[x];
                        }else {
                            exit = true;
                            break;
                        }
                    }                        
                } else {                    
                    if (nbTrials > 10) {
                        exit = true;
                    }
                }
            }
            
        } catch (Exception e) {
            sbError = new StringBuilder(this.getClass().getName()+"."+Thread.currentThread().getStackTrace()[1].getMethodName());
            sbError.append("\r\n").append(e.toString());
            mylogging.log(Level.SEVERE, sbError.toString());
        }
        return sData;
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
     * sLat is a string like 4553.445 and sHem is "N" or "S"
     * @param sLat
     * @param sHem
     * @return 
     */
    private String decodeLat(String sLat, String sHem) {
        String res = "";
        String sDeg;
        String sMn;
        DecimalFormat df2;       
                       
        decimalFormatSymbols.setDecimalSeparator('.');       
        df2 = new DecimalFormat("#0.00000", decimalFormatSymbols);        
        
        try {
            // Latitude is a decimal number (eg 4553.445 )where 1-2 characters are degrees, 3-4 characters are minutes,
            // 5  decimal point, 6-8 characters are decimal minutes. The ellipsoid used is WGS-1984
            sDeg = sLat.substring(0,2);
            sMn = sLat.substring(2,8);
            if (org.logfly.systemio.checking.parseDouble(sMn) && org.logfly.systemio.checking.checkInt(sDeg)) {   
                position myPos = new position();
                myPos.setLatDegres(Integer.parseInt(sDeg));
                myPos.setHemisphere(sHem);
                myPos.setLatMin_mm(Double.parseDouble(sMn));               
                res = df2.format(myPos.getLatitude());
            } else {
                res = "00.00000";
            }
        } catch (Exception e) {
            res = "00.00000";
        }            
        
        return res;
    }    
    
    /**
     * SLong is a a string like 00627.076 and sMer = "W" or "E"
     * @param sLong
     * @return 
     */
    private String decodeLong(String sLong, String sMer) {
        String res = "";
        String sDeg;
        String sMn;
        DecimalFormat df3; 
        decimalFormatSymbols.setDecimalSeparator('.'); 
        df3 = new DecimalFormat("##0.00000", decimalFormatSymbols);        
        
        try {
            // Longitude is a decimal number (eg 00627.076) where 1-3 characters are degrees, 4-5 characters are minutes,
            // 6 decimal point, 7-9 characters are decimal minutes. The ellipsoid used is WGS-1984
            sDeg = sLong.substring(0,3);
            sMn = sLong.substring(3,9);
            if (org.logfly.systemio.checking.parseDouble(sMn) && org.logfly.systemio.checking.checkInt(sDeg) ) {   
                position myPos = new position();
                myPos.setLongDegres(Integer.parseInt(sDeg));
                myPos.setMeridien(sMer);
                myPos.setLongMin_mm(Double.parseDouble(sMn));               
                res = df3.format(myPos.getLongitude());
            } else {
                res = "000.00000";
            }
        } catch (Exception e) {
            res = "000.00000";
        }            
        
        return res;
    }    
    
}
