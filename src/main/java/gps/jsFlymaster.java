/*
 * Copyright Gil THOMAS
 * This file forms an integral part of Logfly project
 * See the LICENSE file distributed with source code
 * for details of Logfly licence project
 */
package gps;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.logging.Level;
import jssc.SerialPort;
import jssc.SerialPortException;
import jssc.SerialPortTimeoutException;
import systemio.mylogging;
import waypio.pointRecord;

/**
 *
 * @author gil
 */
public class jsFlymaster {
    
    private String serialPortName;    
    private SerialPort serialPort;
    private StringBuilder sbRead;  
    private StringBuilder sbError;  
    private ArrayList<pointRecord> wpreadList;  
    private ArrayList<String> listPFMWP;
    private DecimalFormatSymbols decimalFormatSymbols = new DecimalFormatSymbols();
    private final int iBufLen = 6144;
    
    public jsFlymaster(String namePort) {
        serialPortName = namePort; 
    }  
    
    public ArrayList<pointRecord> getWpreadList() {
        return wpreadList;
    } 

    public void setListPFMWP(ArrayList<String> listPFMWP) {        
        this.listPFMWP = listPFMWP;
    }
    public String getError() {
        if (sbError != null)
            return sbError.toString();
        else
            return "No Error message";
    }     
    
    /**
     * String read from GPS are decoded to populate ArrayList<pointRecord> wpreadList
     * GPS string -> $PFMWPL, 45.92732,N,  6.35088,E,1993,LACHAT THONES   ,0*01
     * @return 
     */
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
            getListPFMWPL();
            if (!listPFMWP.isEmpty()) {
                for (int i = 0; i < listPFMWP.size(); i++) {
                    String ligPFM = listPFMWP.get(i);
                    String[] partWp = ligPFM.split(",");
                    if (partWp.length > 6) {
                        sPref = partWp[2].equals("S") ? "-" : ""; 
                        sLat = sPref+partWp[1].trim();
                        sPref = partWp[4].equals("W") ? "-" : ""; 
                        sLong = sPref+partWp[3].trim();
                        sAlt = partWp[5];
                        iAlt = Integer.parseInt(sAlt);
                        balAlt = String.format("%03d",(int) iAlt/10);
                        sDesc = partWp[6];
                        // short name build
                        if (sDesc.length() > 3)
                            sBalise = sDesc.substring(0, 3);
                        else
                            sBalise = sDesc;
                        sBalise = sBalise+balAlt;
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
    
    private void getListPFMWPL() throws Exception {
        
        try {
            listPFMWP = new ArrayList<String>();
            serialPort = new SerialPort(serialPortName);
            serialPort.openPort();
            serialPort.setParams(SerialPort.BAUDRATE_57600, 
                                 SerialPort.DATABITS_8,
                                 SerialPort.STOPBITS_1,
                                 SerialPort.PARITY_NONE);              
            serialPort.writeString("$PFMWPL,\n");
            while(read_line()>0)
            {                
                listPFMWP.add(sbRead.toString());
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
            for (int i = 0; i < listPFMWP.size(); i++) {
                write_line(listPFMWP.get(i));
                // answer is read but not verification
                read_line();
            } 
            serialPort.closePort();
        } catch (Exception e) {
            
        }        
    }
    
    private void write_line(String reqDevice) {
        String req;
        
        try {
            req = reqDevice + "\n";
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
}
