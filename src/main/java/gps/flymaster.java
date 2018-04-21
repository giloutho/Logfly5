/* 
 * Copyright Gil THOMAS
 * This file forms an integral part of Logfly project
 * See the LICENSE file distributed with source code
 * for details of Logfly licence project
 */
package gps;

import com.serialpundit.core.SerialComPlatform;
import com.serialpundit.core.SerialComSystemProperty;
import com.serialpundit.core.util.SerialComUtil;
import com.serialpundit.serial.SerialComManager;
import static geoutils.convigc.Lat_Dd_IGC;
import static geoutils.convigc.Long_Dd_IGC;
import static gps.gpsutils.bytesToHex;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Level;
import javafx.collections.ObservableList;
import model.Gpsmodel;
import systemio.mylogging;
import static gps.gpsutils.fourBytesToInt;
import static gps.gpsutils.oneByteToInt;
import static gps.gpsutils.twoBytesToInt;
import waypio.pointRecord;

/**
 *
 * Communication with Flymaster SD series
 * @author Gil and Rishi Gupta https://github.com/RishiGupta12/SerialPundit
 * without Rishi help, this class doesn't exist
 * 
 * Init           : call GPS id and raw flight list. True if GPS answers
 *                  serial port is closed
 * IniForFlights  : serial port closing in Init requires to open again the serial port (without flight list request)
 * getListPFMLST  : raw flight list stored in a ArrayList
 * getListFlights : called by GPSViewController for extracted flight list 
 * getIGC         : called by GPSViewController for extracted flight track in IGC format
 *                  call getFlightPOS : download a raw flight track form GPS memory [POSitions list]
 *                  call makeIGC : production and certification of an IGC file for the raw dowloaded track
 * getFlightPOS   : download a raw flight track form GPS memory [POSitions list]
 * makeIGC        : production and certification of an IGC file for the raw dowloaded track 
 */
public class flymaster {
    
    private SerialComManager scm;
    private int osType;
    private long handle;
    private String serialPortName;
    private String deviceType;
    private String deviceSerial;
    private String deviceFirm;
    private ArrayList<String> listPFM;
    private ArrayList<String> listPOS;
    private ArrayList<String> listPFMWP;
    private ArrayList<pointRecord> wpreadList;
    private final int IS_GEO   = 0x01;
    private final int IS_HEART = 0x02;
    private final int IS_TAS   = 0x03;
    private final int REQUESTING_POSITION = 0x0001;
    private final int REQUESTING_PULSE    = 0x0002;
    private final int REQUESTING_GFORCE   = 0x0004;
    private final int REQUESTING_TAS      = 0x0008;
    private final int iBufLen = 6144;
    private StringBuilder sbRead;    
    private int year = 0;
    private int month = 0;
    private int day = 0;
    private int hour = 0;
    private int minute = 0;
    private int second = 0;
    private static final String RC = "\r\n";
    private StringBuilder txtIGC;
    private boolean genIGC;
    private String finalIGC;
    private StringBuilder sbError = null;
    
    public flymaster() throws Exception {
        // Create and initialize serialpundit. Note 'private final' word before variable name.
        scm = null;     
        SerialComPlatform scp = new SerialComPlatform(new SerialComSystemProperty());
        osType = scp.getOSType();
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
    
    public boolean isGenIGC() {
        return genIGC;
    }

    public String getFinalIGC() {
        return finalIGC;
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

    /**
     * Initialize the serial port for flight downloading operation
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
        
        write_line("$PFMSNP,");       

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
        
        if (res && callListPFM) getListPFMLST();   
        
         
        return res;
    } 
    
    /**
     * raw flight list decoding  [$PFMLST,025,025,28.06.16,12:33:05,01:15:10*35]
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
     * raw flight list request.
     * output format : $PFMLST,025,025,28.06.16,12:33:05,01:15:10*35
     * @throws Exception 
     */
    private void getListPFMLST() throws Exception {
        
        write_line("$PFMDNL,LST");
        while(read_line()>0)
        {
            listPFM.add(sbRead.toString());
            //System.out.println(sbRead.toString());
        }
    }
    
    private void getListPFMWPL() throws Exception {
        
        write_line("$PFMWPL,");
        while(read_line()>0)
        {
            listPFMWP.add(sbRead.toString());
        }
    }    
    
    public void sendWaypoint() {
        
        for (int i = 0; i < listPFMWP.size(); i++) {
            write_line(listPFMWP.get(i));
            // answer is read but not verification
            read_line();
        }
        
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
        
    /**
     * IGC file request with two steps 
     *   - getFlightPOS
     *   - makeIGC
     * @param gpsCommand
     * @param strDate
     * @param strPilote
     * @param strVoile
     * @return 
     */
    public boolean getIGC(String gpsCommand, String strDate, String strPilote, String strVoile)  {
        boolean res = false;
        finalIGC = null;
                
        try {
            getFlightData(gpsCommand);
            if (!listPOS.isEmpty()) {
                makeIGC(strDate,strPilote, strVoile);
                res = genIGC;
            }
        } catch (Exception e) {
            sbError = new StringBuilder(this.getClass().getName()+"."+Thread.currentThread().getStackTrace()[1].getMethodName());
            sbError.append("\r\n").append(e.toString());
            mylogging.log(Level.SEVERE, sbError.toString());            
        }                
        return res;
    }
    
   private void getFlightData(String gpsCommand) {
       
        // initialization necessary
        listPOS = new ArrayList<String>();
        
        try {
            byte[] BufferRead = new byte[1048576];
            byte[] uChkSum = new byte[8];
            int lenBuffer = 0;
            byte[] trackdata = null;                
            int lenTrackdata = 0;
            boolean exit = false;
            System.out.println("Envoi : "+gpsCommand);
            write_line(gpsCommand);   // gpsCommand like -> $PFMDNL,160709110637,2\n
            while(exit == false) {
                // Windows requested
                Thread.sleep(100);      
                trackdata = scm.readBytes(handle,128);
                if(trackdata != null) {
                    System.arraycopy(trackdata, 0, BufferRead,lenBuffer , trackdata.length);
                    lenBuffer += trackdata.length;   
                    if (trackdata.length == 8 ) {
                        System.arraycopy(trackdata, 0, uChkSum, 0, trackdata.length);
                    }
                }
                else {
                    exit = true;
                    break;
                }
            }
            System.out.println("lenBufer : "+lenBuffer);
            if (lenBuffer > 0) {
             //   if (checkSumFlightData(Arrays.copyOfRange(BufferRead, 0, lenBuffer), uChkSum)) {
                    System.out.println("Checksum OK");
                    decodeFlightData(Arrays.copyOfRange(BufferRead, 0, lenBuffer));  
                //}
            } else {
                sbError = new StringBuilder(gpsCommand+" -> no data [LenBuffer = 0]");
                mylogging.log(Level.SEVERE, sbError.toString());
            }
        } catch (Exception e) {
            sbError = new StringBuilder(this.getClass().getName()+"."+Thread.currentThread().getStackTrace()[1].getMethodName());
            sbError.append("\r\n").append(e.toString());
            mylogging.log(Level.SEVERE, sbError.toString());            
        }
    }    
   
    private boolean checkSumFlightData(byte[] flyRaw, byte[] uChkSum) {
        
        byte[] verify = new byte[8];
        int flyRawLimit = flyRaw.length - (flyRaw.length % 4096);
        for(int dwScan = 0; dwScan < flyRawLimit; dwScan+= 4096)  {
            for(int n = 0; n < 4096; n++) {
                verify[n & 7] ^= flyRaw[dwScan+n];
            }                   
        }
        for(int x=0; x < 8; x++) {
            if(verify[x] != uChkSum[x]) {
                sbError = new StringBuilder("Checksum failed !").append("\r\n");
                sbError.append("uChkSum : " + SerialComUtil.byteArrayToHexString(uChkSum, ":")).append("\r\n");
                sbError.append("Calculated checksum : " + SerialComUtil.byteArrayToHexString(verify, ":"));
                return false;
            }
        }                  
        return true;
    }   
    
    private void decodeFlightData(byte[] FlyRaw){  
        final int GPSRMC_LONG = 19;
        final int GPSRECORD_LONG = 8;
        final int HEARTDATA_STORED = 4;
        final int TAS_STORED = 2;
        final int TP_STORED = 30;
        final int DECLARE_STORED = 7;   
        int dwScan = 0;
        int wOffset;
        int wLenght;
        String hexLenght;
        int byteLength;
        int nbPoints = 0;
        int dataType = 0;
        int index = 0;
        int latitude = 0;
        int longitude = 0;
        int altitude = 0;
        int pressure = 0;
        int time = 0;
        int heading = 0;
        int speed = 0;
        int uDecimalCoords = 1;
        int dataRequested = REQUESTING_POSITION;
        
        int debugRec = 0;
        
        // Combien de secteurs de 4096 octets ? (le fichier fait toujours un peu plus)
        int FlyRawLimite = FlyRaw.length - (FlyRaw.length % 4096);
        
        for(dwScan = 0; dwScan < FlyRawLimite; dwScan+= 4096)  {
            wOffset = 12;
            if (dwScan == 0)  {
                // On est dans le premeir secteur, on récupère les infos générales du vol [Flight_Info]
                hexLenght = Integer.toHexString(0xFF & FlyRaw[wOffset]);
                if (hexLenght.equals("1e"))
                {
                    wLenght = Integer.parseInt(hexLenght, 16);
                    byte [] FlightInfo = Arrays.copyOfRange(FlyRaw, wOffset, wOffset+wLenght);
                    wOffset = wOffset + wLenght;

                   // byte [] bConvert = Arrays.copyOfRange(FlightInfo, 2, 2); 
                    // Récupère durée du vol en secondes
                    int Duration = twoBytesToInt(Arrays.copyOfRange(FlightInfo, 2, 4));
                    // récupère décalage UTC en secondes
                    int OffsetUTC = twoBytesToInt(Arrays.copyOfRange(FlightInfo, 4, 6));
                   
                    wOffset = 49;     
                }            
            }
            while(wOffset <4096)  {
                byteLength = FlyRaw[wOffset+dwScan]& 0x7F;
                switch (byteLength) {
                    case GPSRMC_LONG :   
                        nbPoints++;  
                        // Faire le traitement pour une position complète
                        // Travail arrêté ici... Les valeurs ci dessous ont été vérifiées.
                        latitude = fourBytesToInt(Arrays.copyOfRange(FlyRaw, wOffset+dwScan+2, wOffset+dwScan+6));
                        longitude = fourBytesToInt(Arrays.copyOfRange(FlyRaw, wOffset+dwScan+6, wOffset+dwScan+10));
                        altitude = twoBytesToInt(Arrays.copyOfRange(FlyRaw, wOffset+dwScan+10, wOffset+dwScan+12)); 
                        pressure = twoBytesToInt(Arrays.copyOfRange(FlyRaw, wOffset+dwScan+12, wOffset+dwScan+14));
                        time  = fourBytesToInt(Arrays.copyOfRange(FlyRaw, wOffset+dwScan+14, wOffset+dwScan+18));
                        dataType = IS_GEO;
                        wOffset += GPSRMC_LONG;   
                        debugRec = 1;
                        break;
                    case GPSRECORD_LONG :   
                        nbPoints++;     
                        latitude += FlyRaw[wOffset+dwScan+1];
                        longitude += FlyRaw[wOffset+dwScan+2];
                        altitude += FlyRaw[wOffset+dwScan+3];
                        pressure += FlyRaw[wOffset+dwScan+4];
                        time += oneByteToInt(FlyRaw[wOffset+dwScan+5]);
                        dataType = IS_GEO;                                                                                      
                        wOffset += GPSRECORD_LONG;  
                        debugRec = 2;
                        break;
                    case (0x20 | HEARTDATA_STORED) :   
                        wOffset += HEARTDATA_STORED;
                        dataType = IS_HEART;
                        break;
                    case (0x20 | TAS_STORED) :   
                        wOffset += TAS_STORED;                               
                        dataType = IS_TAS;
                        break;
                    case (0x00 | TP_STORED) :   
                        wOffset += TP_STORED;                               
                        break;
                    case (0x00 | DECLARE_STORED) :  
                        wOffset += DECLARE_STORED;                               
                        break;
                    case 0x7f :   
                        wOffset = 4096;  // FF -> on est à la fin d'un secteur  [FF est décalé à 7F dans la boucle]
                        break;
                    default :
                        wOffset += 3;
                        break;
                }   
                long2Time(time);
                switch (dataType) {
                    case IS_TAS:
                        if((dataRequested & REQUESTING_TAS) == REQUESTING_TAS) {
                            System.out.println("TAS ");
                        }
                        break;

                    case IS_GEO:
                        if((dataRequested & REQUESTING_POSITION) == REQUESTING_POSITION) {
                            int la1 = 0;
                            int lo2 = 0;
                            double fLatitude = 0;
                            double fLongitude = 0;
                            char chNS = 'a';
                            char chEW = 'a';

                            la1 = latitude;
                            lo2 = longitude;
                            fLatitude = latitude;
                            fLongitude = -longitude;
                            fLatitude /= 60000.0;
                            fLongitude /= 60000.0;
                            if (la1 < 0) {
                                    chNS = 'S';
                                    la1 *= -1;
                            } else {
                                    chNS = 'N';
                            }
                            if (lo2 < 0) {
                                    chEW = 'E';
                                    lo2 *= -1;
                            } else {
                                    chEW = 'W';
                            }

                            switch (uDecimalCoords) {
                                case 1:
                                    listPOS.add("POS," + (year + 2000) + "/" + month + "/" + day + "," + hour + ":" + minute + ":" + second + "," + fLatitude + "," + fLongitude + 
                                                "," + altitude + "," + (pressure / 10.0));// + "," + (speed & 0x7f) + "," + heading * 2);
                                    break;
                                default:
                                    listPOS.add("POS," + (year + 2000) + "/" + month + "/" + day + "," + hour + ":" + minute + ":" + second + "," + (la1 / 60000) + "," 
                                                        + (la1 % 60000) + "," + chNS + ","+ (lo2 / 60000) + "," + (lo2 % 60000) + "," + chEW + "," + altitude + "," + (pressure / 10.0));
                                                        //+ "," + (speed & 0x7f) + "," + heading * 2);
                                    break;
                                }
                        }
                        break;
                    case IS_HEART:
                        if((dataRequested & REQUESTING_PULSE) == REQUESTING_PULSE) {
                        }
                        break;
                }                
            }
        }               
        System.out.println("Points : "+nbPoints);
    }    
     
    /**
     * Extract day and hour from a long integer
     * @param itime 
     */
    private void long2Time(int itime) {

        boolean iFoundit = false;
        int imonth = 0;
        int time = itime;
        second = time % 60;
        minute = (time % 3600) / 60;
        hour = (time / 3600) % 24;
        int yr = 0;
        int secondsInFeb = 2419200;
        int daysThisYear = 0;

        for(yr = 0; yr < 255; yr++) {
            daysThisYear = 31536000;
            if ((yr & 3) == 0) {
                    daysThisYear = daysThisYear + (86400 & 0xFFFFFFFF);
            }
            if (time < daysThisYear) {
                    break;
            } else {
                    time = time - daysThisYear;
            }
        }
        if ((yr & 3) == 0) {
            secondsInFeb += 86400;
        }

        for (imonth = 0; imonth < 12 && !iFoundit; imonth++) {
            switch (imonth) {
            case 3:
            case 5:
            case 8:
            case 10:
                    if (time >= (30 * 86400)) {
                            time -= (30 * 86400);
                    } else {
                            iFoundit = true;
                    }
                    break;
            case 1:
                    if (time >= secondsInFeb) {
                            time -= secondsInFeb;
                    } else {
                            iFoundit = true;
                    }
                    break;
            default:
                    if (time >= (31 * 86400)) {
                            time -= (31 * 86400);
                    } else {
                            iFoundit = true;
                    }
            }
        }

        year = yr;
        month = imonth;
        day = (time / 86400) + 1;
    }
    
    /**
     * With a string like 11:8:7, we must have 110807
     * @param sHour
     * @return 
     */
    private String codeHour(String sHour) {
        String res = null;
        String[] tbHr = sHour.split(":");
        if (tbHr.length == 3) {
            StringBuilder sTime = new StringBuilder();
            int iHr = Integer.valueOf(tbHr[0]);
            int iMn = Integer.valueOf(tbHr[1]);
            int iSe = Integer.valueOf(tbHr[2]);
            sTime.append(String.format("%02d", iHr));
            sTime.append(String.format("%02d", iMn));
            sTime.append(String.format("%02d", iSe));
            res = sTime.toString();
        }
        
        
        return res;
    }
            
    /**
     * called by makeIGC POSitions arraylist decoding
     */
    private void decodagePOS() {
        for (int i = 0; i < listPOS.size(); i++) {
            StringBuilder sbLigIGC = new StringBuilder();
            String ligne = listPOS.get(i);
            String[] tbLig = ligne.split(","); 
            String sHour = tbLig[2]; 
            double dLat = Double.parseDouble(tbLig[3]);
            double dLong = Double.parseDouble(tbLig[4]); 
            int iAltGPS = Integer.parseInt(tbLig[5]); 
            double dPress = Double.parseDouble(tbLig[6]); 
            sbLigIGC.append("B").append(codeHour(sHour)).append(Lat_Dd_IGC(dLat)).append(Long_Dd_IGC(dLong));
            // Calcul d'AltiBaro dans le source de Cristiano
            //  fAltiBaro = (1 - pow(fabs((rmc.pressure/10.0)/1013.25), 0.190284)) * 44307.69;
            // Dans le source de Rishi la pression est déjà divisée par 10, donc on fait pas
            //  int iAltBaro = (1 - Math.pow(Math.abs((dPress/10.0)/1013.25), 0.190284)) * 44307.69;
            // mais ...
            int iAltBaro = (int) ((1 - Math.pow(Math.abs((dPress)/1013.25), 0.190284)) * 44307.69);
            sbLigIGC.append("A").append(String.format("%05d", iAltBaro)).append(String.format("%05d", iAltGPS));
            txtIGC.append(sbLigIGC.toString()).append(RC);
        }
    }
    
    /**
     * production and certification of an IGC file for the raw dowloaded track 
     * @param sDate
     * @param sPilote
     * @param sVoile 
     */
    private void makeIGC(String sDate, String sPilote, String sVoile)  {       
        txtIGC = new StringBuilder();
        
        try {
            // En tête IGC
            if (deviceType != null) {
                txtIGC.append("AXLF ").append(deviceType).append(" S/N ").append(deviceSerial).append(" ").append(deviceFirm).append(RC);
            } else {
                txtIGC.append("AXLF FLYMASTER").append(RC);
            }
            txtIGC.append("HFDTE").append(sDate).append(RC);
            txtIGC.append("HFPLTPILOT:").append(sPilote).append(RC);
            txtIGC.append("HFGTYGLIDERTYPE:").append(sVoile).append(RC);
            txtIGC.append("HPCIDCOMPETITIONID: ").append(RC);
            if (deviceFirm != null) {            
                txtIGC.append("HFRFWFIRMWAREVERSION: ").append(deviceFirm).append(RC);
            } else {
                txtIGC.append("HFRHWHARDWAREVERSION: unknown").append(RC);
            }
            // Lignes de positions
            decodagePOS();
            // Génération du G Record avant les enregistrement de type L  qui seront évités à la vérification
            MessageDigest md = MessageDigest.getInstance("SHA1");  
            md.update(txtIGC.toString().getBytes()); 
            byte[] output = md.digest();
            // Ajout des records type L (Enregistrements finaux de la trace IGC)    
            // Numéro de version a ajouter plus tard
            txtIGC.append("LXLF Logfly 5").append(RC);     // To do Current version number     
            txtIGC.append("LXLF Downloaded ").append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))).append(RC);
            // Insertion du G Record
            txtIGC.append("G").append(bytesToHex(output)).append(RC);
            genIGC = true;
            finalIGC = txtIGC.toString(); 
        } catch (Exception e) {
            sbError = new StringBuilder(this.getClass().getName()+"."+Thread.currentThread().getStackTrace()[1].getMethodName());
            sbError.append("\r\n").append(e.toString());
            mylogging.log(Level.SEVERE, sbError.toString());            
        }                
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
