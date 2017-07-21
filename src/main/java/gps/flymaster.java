/* 
 * Copyright Gil THOMAS
 * This file forms an integral part of Logfly project
 * See the LICENSE file distributed with source code
 * for details of Logfly licence project
 */
package gps;

import com.serialpundit.core.util.SerialComUtil;
import com.serialpundit.serial.SerialComManager;
import static geoutils.convigc.Lat_Dd_IGC;
import static geoutils.convigc.Long_Dd_IGC;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import javafx.collections.ObservableList;
import model.Gpsmodel;

/**
 *
 * @author Rishi Gupta https://github.com/RishiGupta12/SerialPundit
 * without Rishi help, this class doesn't exist
 * 
 * Init           : call GPS id and raw flight list. True if GPS answers
 *                  serial port must be closed
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
    
    private final SerialComManager scm;
    private long handle;
    private String serialPortName;
    private String deviceType;
    private String deviceSerial;
    private String deviceFirm;
    private ArrayList<String> listPFM;
    private ArrayList<String> listPOS;
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
    
    public flymaster() throws Exception {
        // Create and initialize serialpundit. Note 'private final' word before variable name.
        scm = new SerialComManager();       
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
            handle = scm.openComPort(serialPortName, true, true, true);
            scm.configureComPortData(handle, SerialComManager.DATABITS.DB_8, SerialComManager.STOPBITS.SB_1, SerialComManager.PARITY.P_NONE, SerialComManager.BAUDRATE.B57600, 0);
            scm.configureComPortControl(handle, SerialComManager.FLOWCONTROL.NONE, 'x', 'x', false, false);

            // Prepare serial port for burst style data read of 500 milli-seconds timeout
            scm.fineTuneReadBehaviour(handle, 0, 5, 100, 5, 200);
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
            handle = scm.openComPort(serialPortName, true, true, true);            
            scm.configureComPortData(handle, SerialComManager.DATABITS.DB_8, SerialComManager.STOPBITS.SB_1, SerialComManager.PARITY.P_NONE, SerialComManager.BAUDRATE.B57600, 0);
            scm.configureComPortControl(handle, SerialComManager.FLOWCONTROL.NONE, 'x', 'x', false, false);

            // Prepare serial port for burst style data read of 500 milli-seconds timeout
            scm.fineTuneReadBehaviour(handle, 0, 5, 100, 5, 200);
            if (getDeviceInfo(false)) {
                res = true;
            }   
        } catch (Exception e) {
            e.printStackTrace();
        }
        return res;        
    }
    
    public void closePort() {
        try {
            scm.closeComPort(handle);
        } catch (Exception e) {
            e.printStackTrace();
        }        
    }
    
    /**
     * Get information id about device. Keep reading until timeout happens.
     * @throws Exception 
     */
    private boolean getDeviceInfo(boolean listPFM) throws Exception {
        boolean res = false;

        byte[] buffer = new byte[4096];
        int offset = 0;
        int totalNumOfBytesReadTillNow = 0;
        int numOfBytesRead = 0;
        int numOfBytesRequested = 0;

        scm.writeString(handle, "$PFMSNP,\n", 0);

        // give some time to GPS to send data to computer. We do not depend upon 100 because we also used 
        Thread.sleep(100);

        // Answer must be something like : $PFMSNP,GpsSD,,02988,1.06j, 872.20,*3C
        String data = scm.readString(handle);
        if (data != null && !data.isEmpty()) {
            String[] tbdata = data.split(",");
            if (tbdata.length > 0 && tbdata[0].equals("$PFMSNP")) {  
                deviceType = tbdata[1];
                deviceSerial = tbdata[3];
                deviceFirm = tbdata[4];   
                res = true;
            } else {
                res = false;
            }
        }    
        
        if (res && listPFM) getListPFMLST();                           
        
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
                // Specific download instruction of this flight is builded
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

        int a = 0;
        int b = 0;
        int offset = 0;
        int totalNumOfBytesReadTillNow = 0;
        int numOfBytesRead = 0;
        int numOfBytesRequested = 4096;
        byte[] buffer = new byte[4096];

        scm.writeString(handle, "$PFMDNL,LST\n", 0);
        Thread.sleep(100);

        numOfBytesRequested = 2048;
        for(int i=0; i < 1500; i++) {
                numOfBytesRead = 0;
                numOfBytesRead = scm.readBytes(handle, buffer, offset, numOfBytesRequested, -1, null);
                if(numOfBytesRead > 0) {
                        offset = offset + numOfBytesRead;
                        totalNumOfBytesReadTillNow = totalNumOfBytesReadTillNow + numOfBytesRead;
                        numOfBytesRequested = 2048 - totalNumOfBytesReadTillNow;
                }else {
                        if(totalNumOfBytesReadTillNow <= 0) {
                                continue;
                        }
                        break;
                }
        }

        if(totalNumOfBytesReadTillNow > 0) {
                while(b < totalNumOfBytesReadTillNow) {
                        if((buffer[b] == (byte)'\r') && (buffer[b + 1] == (byte)'\n')) {
                            listPFM.add(new String(buffer, a, (b - a)));
                            a = b + 2;
                        }
                        b++;
                }
        }else {
                System.out.println("No flights available in device memory !");
                return;
        }
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
                
        try {
            getFlightPOS(gpsCommand);
            if (listPOS.size() > 0) {
                makeIGC(strDate,strPilote, strVoile);
                res = genIGC;
            }
        } catch (Exception e) {
            
        }                
        return res;
    }
    
    /**
     * called by getIGC  for raw flight track download from GPS memory 
     * result is an Arraylist of POSition
     * @param gpsCommand
     * @throws Exception 
     */
    public void getFlightPOS(String gpsCommand) throws Exception {

        final int IS_GEO   = 0x01;
        final int IS_HEART = 0x02;
        final int IS_TAS   = 0x03;
        final int SIZE_GPSRMC_LONG      = 19;
        final int SIZE_GPSRECORD_LONG   = 8;
        final int SIZE_HEARTDATA_STORED = 36;
        final int SIZE_TAS_STORED       = 34;
        final int SIZE_TP_STORED        = 30;
        final int SIZE_DECLARE_STORED   = 7;
        final int REQUESTING_POSITION = 0x0001;
        final int REQUESTING_PULSE    = 0x0002;
        final int REQUESTING_GFORCE   = 0x0004;
        final int REQUESTING_TAS      = 0x0008;
        int dataRequested = REQUESTING_POSITION;

        int offset = 0;
        int totalNumOfBytesReadTillNow = 0;
        int numOfBytesRead = 0;
        int numOfBytesRequested = 4096;
        byte[] raw = null;
        ArrayList<byte[]> rawData = new ArrayList<byte[]>();
        listPOS = new ArrayList<String>();
       
        scm.writeString(handle, gpsCommand, 0);  // // gpsCommand like -> $PFMDNL,160709110637,2\n

        // Give some time to GPS firmware to prepare and start sending blocks.
        Thread.sleep(100);

        boolean allDatumReceived = false;

        while(allDatumReceived == false) {

            offset = 0;
            raw = new byte[4096];

            for(int x=0; x < 2; x++) {

                totalNumOfBytesReadTillNow = 0;
                numOfBytesRequested = 2048;

                while(numOfBytesRequested > 0) {
                    numOfBytesRead = 0;
                    numOfBytesRead = scm.readBytes(handle, raw, offset, numOfBytesRequested, -1, null);
                    if(numOfBytesRead > 0) {
                            offset = offset + numOfBytesRead;
                            totalNumOfBytesReadTillNow = totalNumOfBytesReadTillNow + numOfBytesRead;
                            numOfBytesRequested = 2048 - totalNumOfBytesReadTillNow;
                    }else {
                            if(totalNumOfBytesReadTillNow <= 0) {
                                    continue;
                            }
                            // Reaching here means that there is no data at serial port and read operation timed-out.
                            // All data has been received so proceed to extraction stage.
                            numOfBytesRequested = -1;
                            x = 5;
                            allDatumReceived = true;
                            break;
                    }
                }
            }
            rawData.add(raw);
        }

        // Checksum
        byte[] verify = new byte[8];
        for(int x = 0; x < (rawData.size() - 1); x++) {
                raw = rawData.get(x);
                for(int n = 0; n < 4096; n++) {
                        verify[n & 7] ^= raw[n];
                }
        }
        raw = rawData.get(rawData.size() - 1);
        for(int x=0; x < 8; x++) {
                if(verify[x] != raw[x]) {
                        System.out.println("Checksum failed !");
                        System.out.println("Calculated checksum : " + SerialComUtil.byteArrayToHexString(verify, ":"));
                        return;
                }
        }

        // Extract flight info from each block processing one 4k block at a time.
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

        for (int x = 0; x < (rawData.size() - 1); x++) {

                raw = rawData.get(x);
                index = 12;
                if(x == 0) {
                        index = index + (raw[12] & 0x1F);	
                }

                // 0x1f means the raw[index] is neither carrying control info nor gps data (0x1f is 5 bits uDataSize in gpshdr)
                // data spanning more than 1 byte has to be converted from little to big endian
                while ((index < 4096) && ((raw[index] & 0x1f) != 0x1f) && (raw[index] != 0x00)) {

                        switch (raw[index] & 0x7f) {
                        case SIZE_GPSRMC_LONG:
                                speed = raw[index + 1] & 0xFF;
                                latitude = (raw[index + 5] << 24 | (raw[index + 4] & 0xFF) << 16 | (raw[index + 3] & 0xFF) << 8 | (raw[index + 2] & 0xFF));
                                longitude = (raw[index + 9] << 24 | (raw[index + 8] & 0xFF) << 16 | (raw[index + 7] & 0xFF) << 8 | (raw[index + 6] & 0xFF));
                                altitude = (((raw[index + 11] & 0xFF) << 8) | (raw[index + 10] & 0xFF)) & 0xFFFF;
                                pressure = (((raw[index + 13] & 0xFF) << 8) | (raw[index + 12] & 0xFF)) & 0xFFFF;
                                time = (raw[index + 17] << 24 | (raw[index + 16] & 0xFF) << 16 | (raw[index + 15] & 0xFF) << 8 | (raw[index + 14] & 0xFF));
                                heading = raw[index + 18] & 0xFF;
                                dataType = IS_GEO;
                                break;

                        case SIZE_GPSRECORD_LONG:
                                latitude += raw[index + 1];
                                longitude += raw[index + 2];
                                altitude += raw[index + 3];
                                pressure += raw[index + 4];
                                time += raw[index + 5];
                                speed = raw[index + 6] & 0xFF;    // speed,heading is unsigned char therefore use MSB in arithmetic calculation instead of using it for sign
                                heading = raw[index + 7] & 0xFF;
                                dataType = IS_GEO;
                                break;

                        case SIZE_HEARTDATA_STORED:
                                dataType = IS_HEART;
                                break;

                        case SIZE_TAS_STORED:
                                dataType = IS_TAS;
                                break;

                        case SIZE_TP_STORED:
                                break;

                        case SIZE_DECLARE_STORED:
                                System.out.println("TSK," + (raw[index + 1] & 0xFF));
                                break;

                        default:
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
                                                        "," + altitude + "," + (pressure / 10.0) + "," + (speed & 0x7f) + "," + heading * 2);
                                            break;
                                        default:
                                            listPOS.add("POS," + (year + 2000) + "/" + month + "/" + day + "," + hour + ":" + minute + ":" + second + "," + (la1 / 60000) + "," 
                                                                + (la1 % 60000) + "," + chNS + ","+ (lo2 / 60000) + "," + (lo2 % 60000) + "," + chEW + "," + altitude + "," + (pressure / 10.0) + "," + 
                                                                (speed & 0x7f) + "," + heading * 2);
                                            break;
                                        }
                                }
                                break;

                        case IS_HEART:
                                if((dataRequested & REQUESTING_PULSE) == REQUESTING_PULSE) {
                                }
                                break;
                        default:
                                System.out.println("default !");
                        }

                        index = index + (raw[index] & 0x1F);
                }
        }
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
     * digital signature generation
     * @param b
     * @return 
     */
    private static String bytesToHex(byte[] b) {
        // Initialy letters were in upperercase
        // for xLogfly compatibility, they are converted in lowercase
        char hexDigit[] = {'0', '1', '2', '3', '4', '5', '6', '7',
                           '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};
        StringBuffer buf = new StringBuffer();
        for (int j=0; j<b.length; j++) {
           buf.append(hexDigit[(b[j] >> 4) & 0x0f]);
           buf.append(hexDigit[b[j] & 0x0f]);
        }
        return buf.toString();
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
                txtIGC.append("AXLF ").append(deviceType).append(" S/N ").append(deviceSerial).append(RC);
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
            txtIGC.append("LXLF Logfly version 5.0").append(RC);          
            txtIGC.append("LXLF Downloaded ").append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))).append(RC);
            // Insertion du G Record
            txtIGC.append("G").append(bytesToHex(output)).append(RC);
            genIGC = true;
            finalIGC = txtIGC.toString();            
        } catch (Exception e) {
            
        }                
    }        
    
    /**
     * Debugging
     * @param listFlights 
     */
    public void debugListFlights(ObservableList <Gpsmodel> listFlights) {
        File fFlight = new File("flylist.txt");
	listFlights.clear();
	BufferedReader br = null;            
	try {
            InputStream ips=new FileInputStream(fFlight); 
            InputStreamReader ipsr=new InputStreamReader(ips);
            br=new BufferedReader(ipsr);
            String ligne;
            while ((ligne=br.readLine())!=null){
                listPFM.add(ligne);                
            }
            br.close();  
            for (int i = 0; i < listPFM.size(); i++) {
                String ligPOS = listPFM.get(i);
                String[] cleanVol = ligPOS.split("\\*");
                String[] idVol = cleanVol[0].split(",");
                if (idVol.length > 0 ) {
                    Gpsmodel oneFlight = new Gpsmodel();                                             
                    oneFlight.setChecked(true);
                    oneFlight.setDate(idVol[3]);
                    // On compose l'instruction qui permet de charger le vol
                    StringBuilder sbVol = new StringBuilder();
                    sbVol.append("$PFMDNL,");
                    sbVol.append(idVol[3].replaceAll("\\.", ""));
                    oneFlight.setHeure(idVol[4]);
                    sbVol.append(idVol[4].replaceAll("\\.", ""));
                    sbVol.append(",2\n");
                    oneFlight.setCol4(idVol[5]);
                    oneFlight.setCol5(sbVol.toString());
                    listFlights.add(oneFlight);
                    System.out.println(idVol[3]+" "+idVol[4]+" "+idVol[5]+" "+sbVol.toString());
                }     
                
            }            
            System.out.println("listFlights size : "+listFlights.size());
	} catch (FileNotFoundException ex) {
		System.out.println(ex.getMessage());
	} catch (IOException ex) {
		System.out.println(ex.getMessage());
	}   
        
    }    
}
