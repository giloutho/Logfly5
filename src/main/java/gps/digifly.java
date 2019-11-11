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
import static gps.gpsutils.bytesToHex;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
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
    private ArrayList<String> listFLL;
    private ArrayList<String> listPOS;
    private ArrayList<String> listPFMWP;
    private StringBuilder sbRead;      
    private final int iBufLen = 6144;
    private String pilotName="";
    private StringBuilder txtIGC;
    private boolean mDebug;
    private StringBuilder sbError = null;
    private static final String RC = "\r\n";
    private boolean genIGC;
    private String finalIGC;
    private String debugPath;
    private boolean is400fw=false;
    
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
    
    public String getFinalIGC() {
        return finalIGC;
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
            scm.configureComPortData(handle, SerialComManager.DATABITS.DB_8, SerialComManager.STOPBITS.SB_1, SerialComManager.PARITY.P_NONE, SerialComManager.BAUDRATE.B115200, 0);
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
        closePort();
        boolean res = false;
        try {
            listFLL = new ArrayList<String>();

            // open and configure serial port
            serialPortName = namePort;
            scm = new SerialComManager();
            handle = scm.openComPort(serialPortName, true, true, true);
            scm.configureComPortData(handle, SerialComManager.DATABITS.DB_8, SerialComManager.STOPBITS.SB_1, SerialComManager.PARITY.P_NONE, SerialComManager.BAUDRATE.B115200, 0);
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
    private boolean getDeviceInfo(boolean callListFLL) throws Exception {
        boolean res = false;
        String data;
        String command ="$PDGFVID";
        
        write_line(craftCommand(command));       

        // give some time to GPS to send data to computer. We do not depend upon 100 because we also used 
        Thread.sleep(200);
    
        // Answer must be something like : $PDGFVID,DeviceName,FirmwareVersion,PilotName,*3C
        //$PDGFVID,DIGIFLY AIR BT,141a-012,31958,DONATO*30[0D][0A]
        int iLenData = read_line();
        if (iLenData > 0) {
            data = sbRead.toString();
        } else {
            data = null;
        }
        if (data != null && !data.isEmpty()) {
            int posPFMSNP = data.indexOf("$PDGFVID");
            String cleanData = data.substring(posPFMSNP,data.length());
            String[] tbdata = cleanData.split(",");
            if (tbdata.length > 0 && tbdata[0].contains("$PDGFVID")) {  
                deviceType = tbdata[1];
                deviceFirm = tbdata[2];
                deviceSerial = tbdata[3];
                pilotName =   tbdata[4].split("\\*")[0];
                
                if (deviceFirm.startsWith("4"))
                    is400fw=true;
               
                
                res = true;
            } else {
                sbError = new StringBuilder("GPS not splited : "+data);
                res = false;
            }
        } else {
            sbError = new StringBuilder("No GPS answer (GetDeviceInfo)");
        }    
        
        if (res && callListFLL) getRawFlightList();   
        
         
        return res;
    } 
    
   /**
     * raw flight list request.
     * output format : $PDGFFLL,totflights, [UTCtakeoff] [startblock] [startbyteaddr] [UTClanding] [lastblock] [lastbyteaddr] *[cksm][CR][LF]
     * @throws Exception 
     */
    private void getRawFlightList() throws Exception {
       // List<String> flts = new ArrayList<>();
        write_line(craftCommand("$PDGFFLL*53"));
        Thread.sleep(200);  //Wait 500 ms after error
        if (read_line()>0)
        {
            String fllreply =sbRead.toString();
            String[] segments = fllreply.split(",");
            if (segments.length>0)
            {
               int flights = Integer.parseInt(segments[1]);
               String fltlistRaw =segments[2];
               int cursor=0;
               for (int i=0;i<flights;i++)
               {
                   listFLL.add(fltlistRaw.substring(cursor, cursor+28));
                   cursor +=28;
               }
                Collections.reverse(listFLL); 
            }
        }
    }
    
    /**
     * raw flight list decoding  [$PFMLST,025,025,28.06.16,12:33:05,01:15:10*35]
     * for an ObservableList used by controller's tableview
     * @param listFlights 
     */
    public void getListFlights(ObservableList <Gpsmodel> listFlights) {
        for (int i = 0; i < listFLL.size(); i++) {
            String ligPFM = listFLL.get(i);
            String[] cleanVol = ligPFM.split("\\*");
             String FightDate = GetFlightInfo(cleanVol[0]);
            String[] idVol = FightDate.split("§");
            if (idVol.length > 0 ) {
                Gpsmodel oneFlight = new Gpsmodel();                                             
                oneFlight.setChecked(false);
                oneFlight.setDate(idVol[3]);
                oneFlight.setHeure(idVol[4]);
                oneFlight.setCol4(idVol[5]);
                
                oneFlight.setCol5(cleanVol[0]);                
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
     * For Digifly, we must add the pilot name and the glider name
     * @return 
     */
    public boolean getIGC(String gpsCommand, String strPilote, String strVoile)  {        
        
        boolean res = false;
        // Flytec delivers directly an IGC string
        // Flymaster delivers raw data. 
        // Digifly delivers raw data.
        // They must be decoded 
        
        // For Flytec getIGC returns directly the string (see Flytec20.java)
        // For Flylmaster getIGC returns a boolean
        // For Digifly getIGC returns a boolean
        // if true, decoding function put final IGC string in finalIGC
        
       // ? finalIGC = null;
        String Fi= GetFlightInfo(gpsCommand);
        String strDate = Fi.split("§")[3].replaceAll("\\.", "");
       
        finalIGC = null;
                
        try {
            
            byte[] track= getBinaryBuffer(gpsCommand);
            parseAirBinary(track);
            
            
            //getFlightData(gpsCommand);
            if (!listPOS.isEmpty()) {
                makeIGC(strDate,strPilote, strVoile);
                res = genIGC;
            }else 
            {
                String dMsg = "Error Reading : "+strDate;
                if (mDebug) mylogging.log(Level.INFO, dMsg);   
                System.out.println(dMsg);
            }
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

    //Needed to have a delay in loops
    private int delayed_read_line(int delay)
    {
        try
        {
        Thread.sleep(delay);
        } catch (InterruptedException ex)
        {
        
        }
        return read_line();
    }    
    
 
    private static String getChecksum(String in) {
        int checksum = 0;
        if (in.startsWith("$")) {
            in = in.substring(1, in.length());
        }

        int end = in.indexOf('*');
        if (end == -1)
            end = in.length();
        for (int i = 0; i < end; i++) {
            checksum = checksum ^ in.charAt(i);
        }
        String hex = Integer.toHexString(checksum);
        if (hex.length() == 1)
            hex = "0" + hex;
        return hex.toUpperCase();
    }
    
    private String craftCommand(String command)
    {
        String retval =command+= "*"+ getChecksum(command +"*")+"\r\n";
        return retval;
    }    
    
    public byte[] getBinaryBuffer(String command)
    {
        String hexDateStart = command.substring(0, 8);
        String hexStartBlock = command.substring(8, 8+2);
        String hexStartAddr = command.substring(10, 10+4);
        String hexDateStop = command.substring(14, 14+8);
        String hexStopBlock = command.substring(22, 22+2);
        String hexStopAddr = command.substring(24, 24+4);
        int startb = Integer.parseInt(hexStartBlock, 16);
        int stopb = Integer.parseInt(hexStopBlock, 16);
        int numBlock = stopb-startb;
        int currentblock= startb;
        ArrayList<String> blocksAscii=new ArrayList<String>();
        String data="";
        while (true)
        {
            int stopaddr =4094;
            
            if (currentblock==stopb)
                stopaddr = Integer.parseInt(hexStopAddr, 16)+1;
            
            String airCommand = String.format("$PDGFRMM,2,%02d,%02d,%04d", currentblock, 0, stopaddr);
            write_line(craftCommand(airCommand));   
            
            int iLenData = delayed_read_line(100);
            
            while (iLenData!=0)
            {
                data += sbRead.toString();
                iLenData = delayed_read_line(10);
            }
            blocksAscii.add(data);
            data="";
            if (currentblock==stopb)
                break;
            
            currentblock++;
        }
        List<Byte> retval1 = new ArrayList<Byte>();
        
        int curs=0;
        for (String block : blocksAscii)
        {
            String hexDataRaw = block.split(",")[1];
            String hexData = hexDataRaw.split("\\*")[0];
            byte[] datas = hexStringToByteArray(hexData);
            for(byte b :datas)
            {
                retval1.add(b);
                curs++;
            }
        }
        byte[]retval= new byte[curs];
        curs=0;
        for (byte rb : retval1)
            retval[curs++]=rb;
        
        closePort();
        return retval;
    }
    
    
    void parseAirBinary(byte[] flightdata)
    {
        ArrayList<byte[]> points= new ArrayList<>();
        int cursor=0;
        while (true)
        {
            if (cursor >= flightdata.length)
                    break;
          
            points.add( Arrays.copyOfRange(flightdata, cursor+0, cursor+23));
            cursor += 23;
        }
       listPOS = new ArrayList<>();
       //txtTask = new StringBuilder();
       
       flightInfo = "";
        for (byte[] point :points)
        {
            cursor=0;
            byte controlbyte = point[cursor++];
            byte[] utcb = Arrays.copyOfRange(point, cursor, cursor+4);cursor+=4;        
            byte[] latitudeb = Arrays.copyOfRange(point, cursor, cursor+4);cursor+=4;        
            byte[] longitudeb = Arrays.copyOfRange(point, cursor, cursor+4);cursor+=4;
            byte[] altQneb = Arrays.copyOfRange(point, cursor, cursor+2);cursor+=2;
            byte[] variob = Arrays.copyOfRange(point, cursor, cursor+2);cursor+=2;
            byte airspeedb = point[cursor++];
            byte gndspeedb = point[cursor++];
            byte[] a1qnhb = Arrays.copyOfRange(point, cursor, cursor+2);cursor+=2;
            byte[] gpsAltib = Arrays.copyOfRange(point, cursor, cursor+2);cursor+=2;
            
            int utcSecs = ByteBuffer.wrap(utcb).order(ByteOrder.LITTLE_ENDIAN).getInt();
            int gpsAlti = ByteBuffer.wrap(gpsAltib).order(ByteOrder.BIG_ENDIAN).getShort();
            int baroAlti = ByteBuffer.wrap(altQneb).order(ByteOrder.BIG_ENDIAN).getShort();
            
            int vario = ByteBuffer.wrap(variob).order(ByteOrder.BIG_ENDIAN).getShort();
            int a1qnh = ByteBuffer.wrap(a1qnhb).order(ByteOrder.BIG_ENDIAN).getShort();
            
            float latf = ByteBuffer.wrap(latitudeb).order(ByteOrder.LITTLE_ENDIAN).getFloat();
            float lonf = ByteBuffer.wrap(longitudeb).order(ByteOrder.LITTLE_ENDIAN).getFloat();
            double latitude = RadianToDegree(latf);
            double longitude = RadianToDegree(lonf);
            LocalDateTime timestamp = getDigifyDateTime(utcSecs);
            
            int sats = (((controlbyte >>1)&0x7)*2)+3;
            int traking = 0;
            if (is400fw)
            {
                 traking = a1qnh ;
                 a1qnh = baroAlti;
            }
            //GSP groundSpeed
            //IAS  000 kmh
            //VAR  00000 vario  decimimet/sec
            
            //QNH  00000
            //SIU Sats in use
            listPOS.add("POS," + timestamp.getYear() + "/" + timestamp.getMonthValue()+ "/" + timestamp.getDayOfMonth()+ "," + timestamp.getHour()+ ":" + timestamp.getMinute() + ":" + timestamp.getSecond() + "," + latitude + "," + longitude + "," + gpsAlti + "," + baroAlti
            +","+gndspeedb+","+airspeedb+","+vario+","+a1qnh+","+sats
            );

        }
    }
    private static double RadianToDegree(double angle)
    {
            return angle * (180.0 / Math.PI);
    }

    private byte[] hexStringToByteArray(String s) 
    {
         int len = s.length();
         byte[] data = new byte[len / 2];
         for (int i = 0; i < len; i += 2) {
             data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                                  + Character.digit(s.charAt(i+1), 16));
         }
         return data;
     }    
    
    private LocalDateTime getDigifyDateTime (int Seconds)
    {
         return OffsetDateTime.of( 1980  , 1 , 6 , 0 , 0 , 0 , 0 , ZoneOffset.UTC ).plusSeconds( Seconds ).toLocalDateTime();
    }
   
    private String GetFlightInfo(String item)
    {
        String hexDateStart = item.substring(0, 8);
        int secondsStart = toLittleEndian(hexDateStart);
        
        String hexDateStop = item.substring(14, 14+8);
        int secondsStop = toLittleEndian(hexDateStop);
        int duration = secondsStop-secondsStart;
        LocalDateTime startDate = getDigifyDateTime(secondsStart);
        String retval ="§_§_§";
        retval += startDate.format(DateTimeFormatter.ofPattern("dd.MM.yy§HH:mm:ss")) ;
        retval +="§";
        SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss");
        int hours = duration / 3600;
        int minutes = (duration % 3600) / 60;
        int seconds = duration % 60;

        String timeString = String.format("%02d:%02d:%02d", hours, minutes, seconds);
        retval += timeString;
        return retval;
    }
    
    public static int toLittleEndian(final String hex) {
        int ret = 0;
        String hexLittleEndian = "";
        if (hex.length() % 2 != 0) return ret;
        for (int i = hex.length() - 2; i >= 0; i -= 2) {
            hexLittleEndian += hex.substring(i, i + 2);
        }
        ret = Integer.parseInt(hexLittleEndian, 16);
        return ret;
    }
    
    /**
     * production and certification of an IGC file for the raw downloaded track 
     * @param sDate
     * @param sPilote
     * @param sVoile 
     */
    private void makeIGC(String sDate, String sPilote, String sVoile)  
    {       
        txtIGC = new StringBuilder();
        try {
            // En tête IGC
            if (deviceType != null) {
                txtIGC.append("AXLF ").append(deviceType).append(" S/N ").append(deviceSerial).append(" ").append(deviceFirm).append(RC);
            } else {
                txtIGC.append("AXLF DIGIFLY AIR").append(RC);
            }
            txtIGC.append("HFDTE").append(sDate).append(RC);
            if (pilotName.equals(""))
                txtIGC.append("HFPLTPILOT:").append(sPilote).append(RC);
            else 
                txtIGC.append("HFPLTPILOT:").append(pilotName).append(RC);
            
            txtIGC.append("HFGTYGLIDERTYPE:").append(sVoile).append(RC);
            txtIGC.append("HPCIDCOMPETITIONID: ").append(RC);
            
            if (deviceFirm != null) {            
                txtIGC.append("HFRFWFIRMWAREVERSION: ").append(deviceFirm).append(RC);
            } else {
                txtIGC.append("HFRHWHARDWAREVERSION: unknown").append(RC);
            }
            
            String iRecord= createIRecord();
            
            if (iRecord.length()>0)
                txtIGC.append (iRecord).append(RC);
            
            //Task Lines
            //txtIGC.append(txtTask.toString());
            
            // Lignes de positions
            decodagePOS();
            // Génération du G Record avant les enregistrement de type L  qui seront évités à la vérification
            MessageDigest md = MessageDigest.getInstance("SHA1");  
            md.update(txtIGC.toString().getBytes()); 
            byte[] output = md.digest();
            // Ajout des records type L (Enregistrements finaux de la trace IGC)    
            // Numéro de version a ajouter plus tard
            txtIGC.append(flightInfo);
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
            int iAltBaro = Integer.parseInt(tbLig[6]); 
            //double dPress = Double.parseDouble(tbLig[6]); 
            
            sbLigIGC.append("B").append(codeHour(sHour)).append(Lat_Dd_IGC(dLat)).append(Long_Dd_IGC(dLong));
            // Calcul d'AltiBaro dans le source de Cristiano
            //  fAltiBaro = (1 - pow(fabs((rmc.pressure/10.0)/1013.25), 0.190284)) * 44307.69;
            // Dans le source de Rishi la pression est déjà divisée par 10, donc on fait pas
            //  int iAltBaro = (1 - Math.pow(Math.abs((dPress/10.0)/1013.25), 0.190284)) * 44307.69;
            // mais ...
            //int iAltBaro = (int) ((1 - Math.pow(Math.abs((dPress)/1013.25), 0.190284)) * 44307.69);
            //+","+gndspeed+","+airspeed+","+vario+","+a1qnh
            sbLigIGC.
                    append("A")
                    .append(String.format("%05d", iAltBaro))
                    .append(String.format("%05d", iAltGPS));
                   
            int  gpsSpeed = Integer.parseInt(tbLig[7]) &0xFF; 
            int  airSpeed = Integer.parseInt(tbLig[8]) & 0xFF; 
            int  vario = Integer.parseInt(tbLig[9]); 
            int  a1qnh = Integer.parseInt(tbLig[10]); 
            int  sats = Integer.parseInt(tbLig[11]); 
            
             sbLigIGC
                       .append(String.format("%03d",gpsSpeed))
                       .append(String.format("%03d", airSpeed));
            
             sbLigIGC
                       .append(String.format("%05d",vario))
                       .append(String.format("%05d", a1qnh));
             
              sbLigIGC
                       .append(String.format("%02d",sats));
                       
            
             
            txtIGC.append(sbLigIGC.toString()).append(RC);
        }
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
    
    private String createIRecord()
    {
        //GSP  000-255 kmh groundSpeed
        //IAS  000-255 kmh airspeed
        //VAR  -32000 +32000 vario  decimimet/sec
        //QNH  -32000 +32000  
        //SIU  00 Satellites in use
        StringBuilder retval = new StringBuilder();
        StringBuilder iRecord = new StringBuilder();
        int offset=36;
        int sectors =0;
       

        
        iRecord.append(
        String.format("%02d%02dGSP%02d%02dIAS", offset , offset +2 ,offset +2+1,offset +2+1+2)); 
        offset+=6;
        sectors+=2;
        

        //vario + qnh
        iRecord.append(String.format("%02d%02dVAR%02d%02dQNH", offset , offset +4 ,offset +4+1,offset +4+1+4)); 
        offset+=10;
        sectors+=2;
        
        //Sats in use
        iRecord.append(String.format("%02d%02dSIU", offset , offset +1 ));
        offset+=2;
        sectors+=1;
        
        if (iRecord.length()>0)
            retval.append(String.format ("I%02d",sectors)).append(iRecord.toString());


        return retval.toString();
    }    
}
