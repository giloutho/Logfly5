/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gps;

import geoutils.position;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import jssc.SerialPort;
import jssc.SerialPortException;
import jssc.SerialPortTimeoutException;
import model.Gpsmodel;
import systemio.mylogging;
import waypio.pointRecord;

/**
 *
 * @author thinklinux
 */
public class jsFlytec15 {
    
    private String serialPortName;    
    private SerialPort serialPort;
    private StringBuilder sbRead;  
    private StringBuilder sbError;  
    private int nbFlights;    
    private ObservableList <Gpsmodel> listFlights;   
    private ArrayList<String> listPBRWP;
    private ArrayList<pointRecord> wpreadList;  
    private DecimalFormatSymbols decimalFormatSymbols = new DecimalFormatSymbols();
    
    public jsFlytec15(String namePort) {
        serialPortName = namePort;
        nbFlights = 0;
        listFlights = FXCollections.observableArrayList(); 
    }  

    public ObservableList<Gpsmodel> getListFlights() {
        return listFlights;
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
    
    public boolean reqFlightList() {
        boolean res = false;
        
        try {
            serialPort = new SerialPort(serialPortName);
            serialPort.openPort();
            serialPort.setParams(SerialPort.BAUDRATE_57600, 
                                 SerialPort.DATABITS_8,
                                 SerialPort.STOPBITS_1,
                                 SerialPort.PARITY_NONE);    
            serialPort.setFlowControlMode(SerialPort.FLOWCONTROL_XONXOFF_IN | SerialPort.FLOWCONTROL_XONXOFF_OUT);
            getListPBRTL();
            serialPort.closePort();         
            res = true;
        } catch (Exception e) {
            sbError = new StringBuilder(this.getClass().getName()+"."+Thread.currentThread().getStackTrace()[1].getMethodName());
            sbError.append("\r\n").append(e.toString());
            mylogging.log(Level.SEVERE, sbError.toString());            
        }
        
        return res;
             
    }
    
    public int getListWaypoints() {
        int res = 0;
        wpreadList = new ArrayList<>();
        try {
            serialPort = new SerialPort(serialPortName);
            serialPort.openPort();
            serialPort.setParams(SerialPort.BAUDRATE_57600, 
                                 SerialPort.DATABITS_8,
                                 SerialPort.STOPBITS_1,
                                 SerialPort.PARITY_NONE);    
            serialPort.setFlowControlMode(SerialPort.FLOWCONTROL_XONXOFF_IN | SerialPort.FLOWCONTROL_XONXOFF_OUT);            
            getListPBRWP();
            serialPort.closePort(); 
            res = wpreadList.size(); 
        } catch (Exception e) {
            sbError = new StringBuilder(this.getClass().getName()+"."+Thread.currentThread().getStackTrace()[1].getMethodName());
            sbError.append("\r\n").append(e.toString());
            mylogging.log(Level.SEVERE, sbError.toString());            
        }
        
        return res;
    }
    
    private void getListPBRWP() throws Exception {     
        
        String sLat;
        String sLong;  
        String sPref;
        String sAlt;
        String sBalise;
        String sDesc;
        String balAlt;
        int iAlt;

        String req = ("ACT_31_00")+"\r\n";     
        serialPort.writeString(req);  
        Thread.sleep(300);  
        byte[] gpsBytes = getData();
        if (gpsBytes.length > 0) {
            String s = new String(gpsBytes);
            listPBRWP = new ArrayList<String>(Arrays.asList(s.split("\r\n")));
            for (int i = 0; i < listPBRWP.size(); i++) {                      
                String ligPFM = listPBRWP.get(i);
                // ligPFM is something like S03             ;N   4'25.130;W  76'07.610;   950;   400
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
        }
    }  
    
    
    private void getListPBRTL() {
        int res = 1;
        
        try {
            String req = ("ACT_20_00")+"\r\n";
            serialPort.writeString(req);  
            byte[] gpsBytes = getData();
            if (gpsBytes.length > 0) {
                String s = new String(gpsBytes);
                String[] gpsLines = s.split("\\r");
                int nbLines = 0;
                for (int i = 0; i < gpsLines.length; i++) {                   
                    String ligPFM = gpsLines[i];
                    Pattern pDate = Pattern.compile("\\d{2}.\\d{2}.\\d{2}");
                    Matcher mDate = pDate.matcher(ligPFM);
                    if (mDate.find()) {   
                        String sDate = mDate.group(0).substring(6)+ mDate.group(0).substring(2,6)+ mDate.group(0).substring(0,2);
                        nbFlights++;
                        String sTime = null;
                        String sDur = null;
                        // Sample :  1   14.08.19   13:13:32   00:27:25
                        Pattern pTime = Pattern.compile(";\\s\\d{2}:\\d{2}:\\d{2};");
                        Matcher mTime = pTime.matcher(ligPFM);   
                        int idx = 0;
                        // explanation on "while" in https://stackoverflow.com/questions/23244390/groupcount-in-java-util-regex-matcher-always-returns-0
                        while (mTime.find()) {
                            idx++;
                            if (idx == 1)  sTime = mTime.group(0).substring(2,10);  
                            else if (idx == 2)  sDur = mTime.group(0).substring(2,10);  
                        }
                        System.out.println(sDate+" "+sTime+" "+sDur);
                        Gpsmodel oneFlight = new Gpsmodel();                                             
                        oneFlight.setChecked(false);
                        oneFlight.setDate(sDate);
                        oneFlight.setHeure(sTime);
                        oneFlight.setCol4(sDur);
                        oneFlight.setCol5(null);                
                        listFlights.add(oneFlight);  
                    }
                }
                System.out.println("listFlights size : "+listFlights.size());
            }
        } catch (Exception e) {
            sbError = new StringBuilder(this.getClass().getName()+"."+Thread.currentThread().getStackTrace()[1].getMethodName());
            sbError.append("\r\n").append(e.toString());
            mylogging.log(Level.SEVERE, sbError.toString());    
        }

    }
    
    public void sendWaypoint() throws Exception {
        String ligPBRWP; 
        boolean exit;
        String repGPS;   
        String data;
        try {
            serialPort = new SerialPort(serialPortName);
            serialPort.openPort();
            serialPort.setParams(SerialPort.BAUDRATE_57600, 
                                 SerialPort.DATABITS_8,
                                 SerialPort.STOPBITS_1,
                                 SerialPort.PARITY_NONE);    
            serialPort.setFlowControlMode(SerialPort.FLOWCONTROL_XONXOFF_IN | SerialPort.FLOWCONTROL_XONXOFF_OUT); 
            for (int i = 0; i < listPBRWP.size(); i++) {            
                ligPBRWP = listPBRWP.get(i);   
                exit = false;
                repGPS = null;            
                String req = ("ACT_32_00")+"\r\n";
                serialPort.writeString(req);              
                serialPort.writeString(ligPBRWP);
                Thread.sleep(500);             // necessary with shorter delay, we loss data  
                while(exit == false) {
                    byte[] gpsBytes = getData();
                    if (gpsBytes.length > 0) {
                        data = new String(gpsBytes);
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
                        } else {
                            exit = false;
                            break;
                        }
                    }
                }
               // System.out.println("repGPS : "+repGPS); 
            }
            serialPort.closePort(); 
        } catch (Exception e) {
            sbError = new StringBuilder(this.getClass().getName()+"."+Thread.currentThread().getStackTrace()[1].getMethodName());
            sbError.append("\r\n").append(e.toString());
            mylogging.log(Level.SEVERE, sbError.toString());             
        }
        
    }        
    
    private byte[] getData() throws SerialPortException, IOException {
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] b;
        sbRead = new StringBuilder();
        
        try {
            while ((b = serialPort.readBytes(1, 100)) != null) {
                baos.write(b);
            }
        } catch (SerialPortTimeoutException ex) {
            ;   //don't want to catch it, it just means there is no more data         to read
        }
        
        return baos.toByteArray();    
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
