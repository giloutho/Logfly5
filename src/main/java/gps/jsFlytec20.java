/*
 * Copyright Gil THOMAS
 * This file forms an integral part of Logfly project
 * See the LICENSE file distributed with source code
 * for details of Logfly licence project
 */
package gps;

/**
 *
 * @author gil
 */

import geoutils.position;
import static gps.gpsutils.ajouteChecksum;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Level;
import jssc.SerialPort;
import systemio.mylogging;
import waypio.pointRecord;

public class jsFlytec20 {
    
    private String serialPortName;    
    private SerialPort serialPort;
    private StringBuilder sbRead;  
    private StringBuilder sbError;  
    private ArrayList<pointRecord> wpreadList;  
    private ArrayList<String> listPBRWP;
    private DecimalFormatSymbols decimalFormatSymbols = new DecimalFormatSymbols();
    private final int iBufLen = 6144;
    
    public jsFlytec20(String namePort) {
        serialPortName = namePort; 
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

    private void getListPBRWP() throws Exception {      
        
        try {
            listPBRWP = new ArrayList<String>();
            serialPort = new SerialPort(serialPortName);
            serialPort.openPort();
            serialPort.setParams(SerialPort.BAUDRATE_57600, 
                                 SerialPort.DATABITS_8,
                                 SerialPort.STOPBITS_1,
                                 SerialPort.PARITY_NONE);  
            serialPort.setFlowControlMode(SerialPort.FLOWCONTROL_XONXOFF_IN | SerialPort.FLOWCONTROL_XONXOFF_OUT);            
            String req = ajouteChecksum("$PBRWPS,*")+"\r\n";            
            serialPort.writeString(req);

            while(read_line()>0)
            {                
                listPBRWP.add(sbRead.toString());
            }
            
            serialPort.closePort();                
        } catch (Exception e) {
            sbError = new StringBuilder(this.getClass().getName()+"."+Thread.currentThread().getStackTrace()[1].getMethodName());
            sbError.append("\r\n").append(e.toString());
            mylogging.log(Level.SEVERE, sbError.toString());                
        }        

    } 
    
    public void sendWaypoint() {
        try {
            serialPort = new SerialPort(serialPortName);
            serialPort.openPort();
            serialPort.setParams(SerialPort.BAUDRATE_57600, 
                                     SerialPort.DATABITS_8,
                                     SerialPort.STOPBITS_1,
                                     SerialPort.PARITY_NONE);   
            serialPort.setFlowControlMode(SerialPort.FLOWCONTROL_XONXOFF_IN | SerialPort.FLOWCONTROL_XONXOFF_OUT);  
            for (int i = 0; i < listPBRWP.size(); i++) {
                Thread.sleep(500);    // used in Logfly V4
                write_line(listPBRWP.get(i));
            } 
            serialPort.closePort();
        } catch (Exception e) {
            
        }        
    }    
    
    private void write_line(String reqDevice) {
        
        try {
            serialPort.writeString(reqDevice);
        } catch (Exception e) {
            sbError = new StringBuilder(this.getClass().getName()+"."+Thread.currentThread().getStackTrace()[1].getMethodName());
            sbError.append("\r\n").append(e.toString());
            mylogging.log(Level.SEVERE, sbError.toString());            
        }
    }  
    
    
    private int read_line() {
        int iLen = 0;
        int iBufLen = 6144;
        byte[] iRes = null;
        sbRead = new StringBuilder();
        
        try {
            // Windows requested
            Thread.sleep(100);
            iLen = 0;
            while (iLen < iBufLen) {
                iRes = serialPort.readBytes(1, 100);
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
            if (systemio.checking.parseDouble(sMn) && systemio.checking.checkInt(sDeg)) {   
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
            if (systemio.checking.parseDouble(sMn) && systemio.checking.checkInt(sDeg) ) {   
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
