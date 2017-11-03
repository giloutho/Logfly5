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
import static geoutils.convigc.Lat_Dd_IGC;
import static geoutils.convigc.Long_Dd_IGC;
import static gps.gpsutils.*;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Level;
import javafx.collections.ObservableList;
import model.Gpsmodel;
import systemio.mylogging;

/**
 *
 * Communication with Flymaster SD series
 * @author Gil and Rishi Gupta https://github.com/RishiGupta12/SerialPundit
 * without Rishi help, this class doesn't exist
 * 
 * Init           : call GPS id and raw flight list. True if GPS answers
 *                  serial port must be closed
 * IniForFlights  : requires to open again the serial port (without flight list request)
 * getListPFMLST  : raw flight list stored in a ArrayList
 * getListFlights : called by GPSViewController for extracted flight list 
 * getIGC         : called by GPSViewController for extracted flight track in IGC format
 *                  call getFlightPOS : download a raw flight track form GPS memory [POSitions list]
 *                  call makeIGC : production and certification of an IGC file for the raw dowloaded track
 */
public class flymasterold {
    private SerialComManager scm;
    private int osType;
    private long handle;
    private String serialPortName;
    private String deviceType;
    private String deviceSerial;
    private String deviceFirm;
    private String lstVols;
    private ArrayList<byte[]> packetList;
    private StringBuilder recIGC;
    private StringBuilder sbError = null;
                
    public flymasterold() throws Exception {
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
            lstVols = null;
            // open and configure serial port
            serialPortName = namePort;
            scm = new SerialComManager();
            handle = scm.openComPort(serialPortName, true, true, true);
            scm.configureComPortData(handle, SerialComManager.DATABITS.DB_8, SerialComManager.STOPBITS.SB_1, SerialComManager.PARITY.P_NONE, SerialComManager.BAUDRATE.B57600, 0);
            scm.configureComPortControl(handle, SerialComManager.FLOWCONTROL.NONE, 'x', 'x', false, false);

            if(osType != SerialComPlatform.OS_WINDOWS) {
                // Prepare serial port for burst style data read of 500 milli-seconds timeout
                // This line is a problem with Windows
                scm.fineTuneReadBehaviour(handle, 0, 5, 100, 5, 200);
            }
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
            handle = scm.openComPort(serialPortName, true, true, true);            
            scm.configureComPortData(handle, SerialComManager.DATABITS.DB_8, SerialComManager.STOPBITS.SB_1, SerialComManager.PARITY.P_NONE, SerialComManager.BAUDRATE.B57600, 0);
            scm.configureComPortControl(handle, SerialComManager.FLOWCONTROL.NONE, 'x', 'x', false, false);

            if(osType != SerialComPlatform.OS_WINDOWS) {
                // Prepare serial port for burst style data read of 500 milli-seconds timeout
                // This line is a problem with Windows
                scm.fineTuneReadBehaviour(handle, 0, 5, 100, 5, 200);
            }
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
        String req;

        // On envoie une demande d'identification
        req = ajouteChecksum("$PFMSNP,*")+"\r\n";
        scm.writeString(handle, req, 0);      

        // give some time to GPS to send data to computer. We do not depend upon 100 because we also used 
        Thread.sleep(100);

        res = false;
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
        if (lstVols != null)  {
            String[] tbVols = lstVols.split("\r\n");
            if (tbVols.length > 0) {
                for(int x=0; x<tbVols.length; x++) {    
                    //  tbVols[x] -> $PFMLST,003,001,13.01.16,12:24:05,00:08:43*35
                    String vol[] = tbVols[x].split(",");
                    if (vol.length > 4) {                                                                    
                        Gpsmodel oneFlight = new Gpsmodel();                                             
                        oneFlight.setChecked(false);
                        oneFlight.setDate(vol[3]);      
                        // Building specific download instruction of this flight 
                        // reqPacket = ajouteChecksum("$PFMDNL,170611154445,*")+"\r\n";    
                        StringBuilder sbVol = new StringBuilder();
                        sbVol.append("$PFMDNL,");
                        String[] tbDate = vol[3].split("\\.");
                        if (tbDate.length == 3) {
                            sbVol.append(tbDate[2]).append(tbDate[1]).append(tbDate[0]);                            
                            oneFlight.setHeure(vol[4]);
                            sbVol.append(vol[4].replaceAll(":", ""));
                            sbVol.append(",*");    
                            String[] tbDuration = vol[5].split("\\*");
                            // Value of duration output is 00:28:13*32 [ended with checksum]
                            if (tbDuration.length == 2)  {
                                oneFlight.setCol4(tbDuration[0]);    // duration
                                oneFlight.setCol5(sbVol.toString());                
                                listFlights.add(oneFlight);  
                            }
                        }                        
                    }
                }                
            }
        }                                
    }     
    
    /**
     * raw flight list request.
     * output format : $PFMLST,025,025,28.06.16,12:33:05,01:15:10*35
     * @throws Exception 
     */
    private void getListPFMLST() throws Exception {
        boolean exit = false;
        String req;
        String data;

        req = ajouteChecksum("$PFMDNL,LST,*")+"\r\n";
        scm.writeString(handle, req, 0);
        while(exit == false) {
            data  = scm.readString(handle);
            if(data != null) {                                                                 
                lstVols += data;  
            }
            else {
                exit = true;
                break;
            }
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
    public boolean getIGC(String gpsCommand, String strPilote, String strVoile)  {
        boolean res = false;
                
        try {
            getFlightPOS(gpsCommand);
            if (packetList.size() > 0) {
                res = makeIGC(strPilote, strVoile);                
            }
        } catch (Exception e) {
            sbError = new StringBuilder(this.getClass().getName()+"."+Thread.currentThread().getStackTrace()[1].getMethodName());
            sbError.append("\r\n").append(e.toString());
            mylogging.log(Level.SEVERE, sbError.toString());
            res = false;
        }       
        
        return res;
    }
    
    public String getFinalIGC() {        
        return recIGC.toString();
    }
    
    private void getFlightPOS(String gpsCommand) throws Exception  {
        String reqPacket;
        String begPacket;
        int lenPacket;
        packetList = new ArrayList<byte[]>();                                      
        byte[] packet = null;                                                                    
        boolean exit = false;
    
        reqPacket = ajouteChecksum(gpsCommand)+"\r\n";       
        while(exit == false) {
            scm.writeString(handle, reqPacket , 0);  
            Thread.sleep(100);
            packet = scm.readBytes(handle);
            if(packet != null && packet.length > 2) {   
                // Each data block starts with packet identification (2 bytes)
                begPacket = Integer.toHexString(0xFF & packet[0])+Integer.toHexString(0xFF & packet[1]);
                // GPS send 0xa3a3 indicating no more data.
                if (begPacket.equals("a3a3")) {
                    exit = true;
                    break;
                } else {
                    // Warning... in java a byte is always signed...                                 
                    lenPacket = 0xFF & packet[2];
                    if (packet.length == 4+lenPacket) {                        
                        if (checkCrc(packet)) {                       
                            packetList.add(packet);
                            // ask GPS to send next block
                            reqPacket = String.valueOf(Character.toChars(177));                            
                        } else {
                            // ask GPS to resend data block
                            reqPacket = String.valueOf(Character.toChars(178));                                                    
                        }             
                    }
                }
            } else {
                // ask GPS to resend data block
                reqPacket = String.valueOf(Character.toChars(178));    
            }                                                        
        }          
    }
    
    private boolean makeIGC(String appPilote, String appVoile)  {
        int nbPoints = 0;
        String flyPacketId;
        String sPilote = "";
        String sGliderBrand = "";
        String sGliderModel = "";
        String sCompetId = "";        
        double latRef = 0; 
        double longRef = 0;
        int iLat, iLong;
        String igc_Lat, igc_Long;
        double igc_Press;
        int altRef = 0;
        int pressRef = 0;
        int deltaSec;
        int iFix;
        String sFix;
        boolean debIGC = true;        
        // Time will be the number of seconds elapsed since the first second of the year 2000.
        LocalDateTime baseDate = LocalDateTime.of(2000, 1, 1, 0, 0, 0);  
        // refDate will be computed with baseDate but must be initialized 
        LocalDateTime refDate = LocalDateTime.of(2000, 1, 1, 0, 0, 0); 
        DateTimeFormatter dayDtf = DateTimeFormatter.ofPattern("ddMMYY");
        DateTimeFormatter timeDtf = DateTimeFormatter.ofPattern("HHmmss");
        recIGC = new StringBuilder();
        
        for (byte[] tbpacket: packetList) {                
            flyPacketId = Integer.toHexString(0xFF & tbpacket[0])+Integer.toHexString(0xFF & tbpacket[1]);
            switch (flyPacketId) {
                case "a0a0" :
                    // Flight information record
                    if (tbpacket[19] > 0) sPilote = new String(tbpacket, 19, 15);         
                    if (tbpacket[34] > 0) sGliderBrand = new String(tbpacket, 34, 15);         
                    if (tbpacket[49] > 0) sGliderModel = new String(tbpacket, 49, 15); 
                    if (tbpacket[11] > 0) sCompetId = new String(tbpacket, 11, 8);
                    // we put pilot name and paraglider
                    // at the beginning, it was inly when logbook update asked
                    // finally for a the G record, we must put it now         
                    if (sPilote == null) sPilote = appPilote;
                    if (sGliderBrand == null) sGliderBrand = appVoile;                 
                    break;
                case "a1a1" :
                    //Key track position record
                    nbPoints++;
                    iLat = fourBytesToInt(Arrays.copyOfRange(tbpacket, 4, 8)); 
                    iLong = fourBytesToInt(Arrays.copyOfRange(tbpacket, 8, 12));         
                    latRef = (double)iLat/60000;
                    igc_Lat = Lat_Dd_IGC(latRef); // Viendra de GeoUtils
                    longRef = (double)iLong/60000;
                    // multiplied by  -1 is necessary because Flymaster give a negative value for east longitude
                    igc_Long = Long_Dd_IGC(longRef * -1);
                    altRef = twoBytesToInt(Arrays.copyOfRange(tbpacket, 12, 14));
                    pressRef = twoBytesToInt(Arrays.copyOfRange(tbpacket, 14, 16));
                    // Formula came from Flymaster documentation : to convert barometric pressure to altitude, use the following formula:
                    igc_Press = (1.0 - Math.pow(Math.abs((pressRef / 10.0) / 1013.25), 0.190284)) * 44307.69;
                    deltaSec = Math.abs(fourBytesToInt(Arrays.copyOfRange(tbpacket, 16, 20)));
                    refDate = baseDate.plusSeconds(deltaSec);  
                    // if it's the first point, we must put header file
                    if (debIGC) {
                        if (deviceType != null) {
                            recIGC.append("AXLF ").append(deviceType).append(" S/N ").append(deviceSerial).append(" ").append(deviceFirm).append("\r\n");
                        } else {
                            recIGC.append("AXLF FLYMASTER").append("\r\n");
                        }
                        recIGC.append("AXLF FLYMASTER S/N ").append("\r\n");   // to do getDeviceInfo
                        recIGC.append("HFDTE").append(dayDtf.format(refDate)).append("\r\n");
                        recIGC.append("HFPLTPILOT: ").append(sPilote).append("\r\n");
                        recIGC.append("HPGTYGLIDERTYPE: ").append(sGliderBrand).append(" ").append(sGliderModel).append("\r\n");
                        recIGC.append("HPCIDCOMPETITIONID: ").append(sCompetId).append("\r\n");                       
                        debIGC = false;                        
                    }
                    recIGC.append("B").append(timeDtf.format(refDate));
                    recIGC.append(igc_Lat).append(igc_Long).append("A");
                    recIGC.append(String.format("%05.0f" ,igc_Press)).append(String.format("%05d" ,altRef)).append("\r\n");                    
                    break;
                case "a2a2" :
                    // each delta record = 6 bytes
                    int nbDelta = (0xFF & tbpacket[2])/6;
                    for (int i = 0; i < nbDelta; i++) {
                        nbPoints++;
                        iFix = tbpacket[3+(6*i)];
                        // The 8th bit, of the Fix flag is set for an ‘A’ type fix and unset for a ‘V’ type fix.
                        if  ((iFix & 1 << 8) != 0)                    
                            sFix = "A";
                        else
                            sFix = "V";                    
                        iLat = tbpacket[4+(6*i)];
                        iLong = tbpacket[5+(6*i)];
                        latRef = latRef+((double)iLat/60000);
                        igc_Lat = Lat_Dd_IGC(latRef);
                        longRef = longRef+((double)iLong/60000);
                        // multiplied by  -1 is necessary because Flymaster give a negative value for east longitude
                        igc_Long = Long_Dd_IGC(longRef * -1);
                        altRef = altRef+tbpacket[6+(6*i)];
                        pressRef = pressRef+tbpacket[7+(6*i)];
                        // Formula came from Flymaster documentation : to convert barometric pressure to altitude, use the following formula:
                        igc_Press = (1.0 - Math.pow(Math.abs((pressRef / 10.0) / 1013.25), 0.190284)) * 44307.69;
                        deltaSec = tbpacket[8+(6*i)];                        
                        refDate = refDate.plusSeconds(deltaSec);                                  
                        recIGC.append("B").append(timeDtf.format(refDate));
                        recIGC.append(igc_Lat).append(igc_Long).append("A");
                        recIGC.append(String.format("%05.0f" ,igc_Press)).append(String.format("%05d" ,altRef)).append("\r\n");        
                    }
                    break;
            }                             
        }
        if (nbPoints > 0) {
            // Génération du G Record avant les enregistrement de type L  qui seront évités à la vérification
            try {
                MessageDigest md = MessageDigest.getInstance("SHA1");  
                md.update(recIGC.toString().getBytes()); 
                byte[] output = md.digest();
                // L record addition
                recIGC.append("LXLF Logfly 5").append("\r\n");     // To do Current version number                                 
                recIGC.append("LXLF Downloaded ").append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))).append("\r\n");
                // G Record addition
                recIGC.append("G").append(bytesToHex(output)).append("\r\n");
            } catch (Exception e) {
                nbPoints = 0;
                sbError = new StringBuilder(this.getClass().getName()+"."+Thread.currentThread().getStackTrace()[1].getMethodName());
                sbError.append("\r\n").append(e.toString());
                mylogging.log(Level.SEVERE, sbError.toString());                
            }
            
        }
        if (nbPoints > 0)
            return true;
        else
            return false;        
    }
}
