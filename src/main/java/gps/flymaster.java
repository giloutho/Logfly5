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
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import waypio.pointRecord;

/**
 *
 * ===========================================================================================
 * Many problems with serialpundit on Windows 
 * Many users have this error : com.serialpundit.core.SerialComException: 
 *           C:\xx\spcomwinx64.dll: Can''t find dependent libraries
 * Unable to solve this error with Visual C++ Redistributable For Visual Studio 2013.
 * ============================================================================================
 * 
 * @author Alessandro Faillace
 * Decoration Gil
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
    private String flightInfo;
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
    private StringBuilder txtTask;
    private boolean genIGC;
    private boolean gotGforce;
    private boolean gotTAS;
    private boolean gotHeart;
    private String trackFw = "";
    private String pilotName="";
    private boolean gotGPSSPEED_HEADING;
    private String finalIGC;
    private StringBuilder sbError = null;
    private boolean mDebug;
    private String debugPath;
    private boolean portClosed=true;
    
    //To log binary on flle set to true
    boolean fileDebug = false;
    
    
    public flymaster(boolean pDebug, String pDebugPath) throws Exception {
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
    
    //function to debug binary tracks, will be called upon gps selection.. 
    void debugTrack()
    {
        String debugFile ="";
         //insert debug file to debug a flymaster binary, format is file#path
         //if commented, code will exit
        
        //debugFile= "file#c:/jil/PFMDNL_191023065040_2";
       
        if (debugFile.length() ==0)
            return;
        
        getFlightData(debugFile);  //Debug stuff
        makeIGC("2019-01-01","test", "test");
        String result = txtIGC.toString();
    }
        
    byte[] readFileOnDisk( String filepath)
    {
        try
        {
            Path path = Paths.get(filepath);
            byte[] retval =  Files.readAllBytes(path);
            return retval;
        } catch (IOException ex)
        {

        }
        return null;
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
            openPort(namePort);
            // ID GPS request + raw flight list (true)
            if (getDeviceInfo(true)) {
                res = true;
            }   
            // Closing port mandatory
            closePort();
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
 
        debugTrack();
        boolean res = false;
        listPFMWP = new ArrayList<String>();
        try {
            openPort(namePort);
            if (getDeviceInfo(false)) {
                res = true;
            } else {
                closePort();
            } 
        } catch (Exception e) {
            sbError = new StringBuilder(this.getClass().getName()+"."+Thread.currentThread().getStackTrace()[1].getMethodName());
            sbError.append("\r\n").append(e.toString());
            mylogging.log(Level.SEVERE, sbError.toString());
        }
        return res;        
    }
    
    public void openPort(String namePort)
    {
        if (!portClosed)
            return;
        try {
            // open and configure serial port
            serialPortName = namePort;
            scm = new SerialComManager();
            handle = scm.openComPort(serialPortName, true, true, true);       
            portClosed=false;
            scm.configureComPortData(handle, SerialComManager.DATABITS.DB_8, SerialComManager.STOPBITS.SB_1, SerialComManager.PARITY.P_NONE, SerialComManager.BAUDRATE.B57600, 0);
            scm.configureComPortControl(handle, SerialComManager.FLOWCONTROL.NONE, 'x', 'x', false, false);
            // Normally this instruction should not be a problem for Windows, it's special parameters for Windows !!!
           // if(osType != SerialComPlatform.OS_WINDOWS) {
                // Prepare serial port for burst style data read of 500 milli-seconds timeout
                // This line is a problem with Windows
                scm.fineTuneReadBehaviour(handle, 0, 5, 100, 5, 200);
                // scm.fineTuneReadBehaviour(handle, 0, 3000, 0, 0, 0);

        } catch (Exception e) {
            sbError = new StringBuilder(this.getClass().getName()+"."+Thread.currentThread().getStackTrace()[1].getMethodName());
            sbError.append("\r\n").append(e.toString());
            mylogging.log(Level.SEVERE, sbError.toString());
        }
    }
    
    public void closePort() {
        if (portClosed)
            return;
        try {
            scm.closeComPort(handle);
             portClosed=true;
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
  
    private void getFlightData(String gpsCommand) 
    {
        int trycount=0;
        try
        {
            do {
                // initialization necessary
                listPOS = new ArrayList<String>();
                byte[] trackDataRaw=null;
                
                if (gpsCommand.startsWith("file#"))
                {
                    String[] filePart = gpsCommand.split("#");
                    String file = filePart[1];
                    trackDataRaw = readFileOnDisk(file);
                    
                }else 
                {
                    trackDataRaw = serialRead(gpsCommand);
                }
                byte[] trackData = getTrackData(trackDataRaw);
                byte[] uChkSum = getCkSum(trackDataRaw);
                boolean success;
                if (checkSumFlightData(trackDataRaw, uChkSum)) {                    
                    if (mDebug) mylogging.log(Level.INFO, "Checksum OK");
                    System.out.println("Checksum OK");
                    success=decodeFlightData(trackData);  
                    if (success)
                    {
                        if (mDebug) mylogging.log(Level.INFO, "Decode OK");
                        System.out.println("Decode OK!");
                        break;
                    } else {
                        if (mDebug) mylogging.log(Level.INFO, "ERROR !!! : Bad Decode!! Try Again");
                        System.out.println("ERROR !!! : Bad Decode!! Try Again");
                        Thread.sleep(500);  //Wait 100 ms after error
                    }
                } else {
                    if (mDebug) mylogging.log(Level.INFO, "ERROR !!! : Checksum KO!! Try Again");
                    System.out.println("ERROR !!! : Checksum KO!! Try Again");
                    Thread.sleep(500);  //Wait 500 ms after error
                }
              trycount++;
              String dMsg = "ERROR Retry# " + trycount;
              if (mDebug) mylogging.log(Level.INFO, dMsg);
              System.out.println(dMsg);
            } while (trycount<5);       //After 5 tries Bye bye
            if(trycount==5) {
                if (mDebug) mylogging.log(Level.INFO, "ERROR: Givin up sorry!");
                System.out.println("ERROR: Givin up sorry!");
            }

        } catch (Exception e) {
            sbError = new StringBuilder(this.getClass().getName()+"."+Thread.currentThread().getStackTrace()[1].getMethodName());
            sbError.append("\r\n").append(e.toString());
            mylogging.log(Level.SEVERE, sbError.toString());            
        }
    }
    
    private byte[] getCkSum (byte[] trackData)
    {
        byte[] retval= new byte[8];
        if (trackData.length-8 >0)
            System.arraycopy(trackData, trackData.length-8, retval,0 , 8);
        
        return retval;
    }
    private byte[] getTrackData (byte[] trackData)
    {
        if (trackData.length>8)
        {
            byte[] retval= new byte[trackData.length-8];
            System.arraycopy(trackData, 0, retval,0 , trackData.length-8);
            return retval;
        }
        return null;
    }
    
    
    //KISS Binary Serial real (Keep It Simple and Stupid)
    private byte[] serialRead (String gpsCommand)
    {        
        System.out.println("------Start Serial Read------");
        System.out.println("Sending : "+gpsCommand);
        if (mDebug) mylogging.log(Level.INFO, "------Start Serial Read------ with "+gpsCommand);
        byte[] trackdata = null;   
        int lenBuffer = 0;
        byte[] BufferRead = new byte[1048576];
        byte[] retval =null;
        try {
            write_line(gpsCommand);   // gpsCommand like -> $PFMDNL,160709110637,2\n
            Thread.sleep(100);  //Wait 100 ms after sending the cmd
            while(true) 
            {
                // Windows requested   --- sleep not needed and unefficitve in while loop we put 10 ms 
                Thread.sleep(10); 
                trackdata = scm.readBytes(handle,512);
                if(trackdata != null) {
                    System.arraycopy(trackdata, 0, BufferRead,lenBuffer , trackdata.length);
                    lenBuffer += trackdata.length;   
                }
                else 
                {
                    break;
                }
             }
            retval = new byte[lenBuffer];
            System.arraycopy(BufferRead, 0, retval,0 , lenBuffer);
             
            //File output for Debug
            if (mDebug)
            {
                String sDest = debugPath+(gpsCommand.replace(",","_").replace("$","").replace("\n",""));
                FileOutputStream outf = new FileOutputStream(sDest);
                outf.write(retval);
                outf.close();
                mylogging.log(Level.INFO, "Binary data in "+sDest);                
            }                        
            
        } catch (Exception e) {
            sbError = new StringBuilder(this.getClass().getName()+"."+Thread.currentThread().getStackTrace()[1].getMethodName());
            sbError.append("\r\n").append(e.toString());
            mylogging.log(Level.SEVERE, sbError.toString());            
        }        
        System.out.println("------End Serial Read------");
        if (mDebug) mylogging.log(Level.INFO, "------End Serial Read------");
        return retval;
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
    
    private boolean decodeFlightData(byte[] FlyRaw){  
        int nbPoints = 0;
        int nbTpoints=0;
        int tpNum=0;
        if (mDebug) mylogging.log(Level.INFO, "Start of decodeFlightData");
        System.out.println("decodeFlightData");
        trackFw="";
        txtTask = new StringBuilder();
        //Record type data
        final int TP_STORED = 30;  //Stored Waypoint
        final int FLIGHT_INFO = 26; //Flight info (unknown)
        final int GPSRMC_LONG_A_T = 21; //GPSRMC Key Record with alititide and  TAS info masked with 0x40
        final int GPSRMC_LONG_A = 19; //GPSRMC Long Extended altitude masked with 0x40
        final int GPSRMC_LONG_P = 19; //GPSRMC Long Extended pressure not masked
        final int PILOT_IN_CHARGE = 17; //Pilot in charge info masked with 0x20
        final int PILOT_IN_CHARGE_NEW = 31; //Pilot in charge info masked with 0x20
        final int GPSRECORD_LONG_A_T = 10; //GPSRECORD Delta Record with altitude and  TAS info masked with 0x40
        final int GPSRECORD_LONG_A = 8; //GPSRECORD Delta Record with altitude masked with 0x40
        final int GPSRECORD_LONG_P = 8; //GPSRECORD Delta Record with pressure not masked
        final int GPSRECORD_SHORT =6;
        final int GPSLOST = 7; //GPS Lost info Masked with  0x20
        final int DECLARE_STORED = 7; //Task Delclare info not maksed (on old fw)
        final int HEARTDATA_STORED = 4; //Hearth info
        final int TAS_STORED = 3; //Tas info.. someware
        final int FW_STORED = 3; //Firmware info masked with 0x40;
	final int SOCFWDEV = 5; //Firmware info masked with 0x60;
        final int EVENT_STORED = 2; //Event stroed (gforce masked 0x20)
        final int GPSUNK = 1; //no data
        

        int uDecimalCoords = 1;
        int dataRequested = REQUESTING_POSITION;
        int latitude = 0;
        int longitude = 0;
        int altitude = 0;
        int pAltitude = 0;
        int time = 0;
        int heading = 0;
        int speed = 0;
        int dataType = 0;
        int debugRec = 0;
        int TAS_Temp=0;
        int TAS=1;
        int HeartBeat=0;
        int HeartStepsPerMin=1;
        int GForce=10;
        int lostTasFrames =0;
        boolean gForceFirstPkt=true;
        boolean FirstGPSRMC=true;
        int dwScan ;
        String hexLenght;
        int wOffset;
        int wLenght;
        int byteLength;
        int GPSDATA_HDR;
        double dPress=0;
        gotGPSSPEED_HEADING=false;
        gotTAS=false;
        gotGforce=false;
        gotHeart=false;
        
        int FlyRawLimite = FlyRaw.length - (FlyRaw.length % 4096);
        pilotName="";
        for(dwScan = 0; dwScan < FlyRawLimite; dwScan+= 4096)  {
            wOffset = 12;
            if (dwScan == 0)  {
                
                // On est dans le premeir secteur, on récupère les infos générales du vol [Flight_Info]
                hexLenght = Integer.toHexString(0xFF & FlyRaw[wOffset]);
                if (hexLenght.equals("1e"))
                {
                    StringBuilder logbookSb= new  StringBuilder();
                    flightInfo="";
                    wLenght = Integer.parseInt(hexLenght, 16);
                    byte [] FlightInfo = Arrays.copyOfRange(FlyRaw, wOffset, wOffset+wLenght);

                   // byte [] bConvert = Arrays.copyOfRange(FlightInfo, 2, 2); 
                    // Récupère durée du vol en secondes
                    byte taskInfo = (byte)(FlightInfo[1]&0xFF);
                    
                    int Duration = twoBytesToInt(Arrays.copyOfRange(FlightInfo, 2, 4));
                    // récupère décalage UTC en secondes
                    int OffsetUTC = twoBytesToInt(Arrays.copyOfRange(FlightInfo, 4, 6));
                    int maxSpeed=  twoBytesToInt(Arrays.copyOfRange(FlightInfo, 6, 8));
               
                    int dist = fourBytesToInt(Arrays.copyOfRange(FlightInfo, 8, 12));
                    float fDist =  Float.intBitsToFloat(dist);
                    
                    int wSpeedSection = twoBytesToInt(Arrays.copyOfRange(FlightInfo, 12, 14));
                    int dwMinPressure = fourBytesToInt(Arrays.copyOfRange(FlightInfo, 14, 18));
                    int dwToffPressure = fourBytesToInt(Arrays.copyOfRange(FlightInfo, 18, 22));
                    
                    int iMaxRoc = twoBytesToInt(Arrays.copyOfRange(FlightInfo, 22, 24));
                    int iMinRoc = twoBytesToInt(Arrays.copyOfRange(FlightInfo, 24, 26));

                    int qnh = fourBytesToInt(Arrays.copyOfRange(FlightInfo, 26, 30));
                    double fQnh =  Float.intBitsToFloat(qnh);
                    
                    //Negative UTC
                    if ((taskInfo &0x02)==0x02)
                        OffsetUTC = 0-OffsetUTC;
                    
                    int hours = Duration / 3600;
                    int minutes = (Duration - hours * 3600) / 60;
                    int seconds = (Duration - hours * 3600) - minutes * 60;
                  
                    
                    logbookSb.append(String.format ("LINFO: Duration %ds %02d:%02d:%02d, UTC Offset %ds", Duration,hours,minutes,seconds,OffsetUTC));
                    logbookSb.append(RC);
                    
                             
                            
                    double fToffPressure =(double)dwToffPressure/100;
                    double fMinPressure =(double)dwMinPressure/100;
                    
                    if (fQnh ==0)
                        fQnh = 1013.25;
                    
                    int pAltitudeTakeOff = (int) ((1 - Math.pow(Math.abs((fToffPressure)/fQnh), 0.190284)) * 44307.69);
                    int pAltitudeMax = (int) ((1 - Math.pow(Math.abs((fMinPressure)/fQnh), 0.190284)) * 44307.69);
                   
                        
                    logbookSb.append(String.format ("LINFO: Max Alti: %dm, TakeOff Alti: %dm", pAltitudeMax,pAltitudeTakeOff,pAltitudeMax-pAltitudeTakeOff));
                    logbookSb.append(RC);
                    
                    logbookSb.append(String.format ("LINFO: Above TakeOff: %dm, QNH: %.3f", pAltitudeMax-pAltitudeTakeOff,fQnh));
                    logbookSb.append(RC);
               
                    double minsink = (float)((0xFFFF-iMinRoc)+1)/10;
                    double maxclimb = (float)(iMaxRoc)/10;
                    logbookSb.append(String.format ("LINFO: Max Climb: +%.1f, MaxSink: -%.1f",maxclimb ,minsink));
                    logbookSb.append(RC);
               
                    logbookSb.append(String.format ("LINFO: Max Speed: %d, Dist: %.4f", maxSpeed,fDist));
                    logbookSb.append(RC);
               

                    String sIsTask ="Task:False";
                    String sHasStart ="Start:False";
                    String sReachedGoal ="Goal:False";
                    String sReachedEnd ="End:False";
                    boolean bIsTask=false;
                    
                    if ((taskInfo &0x01)==0x01)
                    {
                        bIsTask=true;
                        sIsTask="Task:True";
                    }
                    
                    if ((taskInfo &0x04)==0x04)
                        sHasStart ="Start:True";
                   
                    if ((taskInfo &0x08)==0x08)
                        sReachedGoal ="Goal:True";
   
                      if ((taskInfo &0x10)==0x10)
                        sReachedEnd ="End:True";
                      
                    if (bIsTask)
                    {
                        logbookSb.append("LTASK:");
                        logbookSb.append(sIsTask).append(",");
                        logbookSb.append(sHasStart).append(",");
                        logbookSb.append(sReachedGoal).append(",");
                        logbookSb.append(sReachedEnd);
                        logbookSb.append(RC);
                        if (wSpeedSection>0)
                        {
                            int taskHours = wSpeedSection / 3600;
                            int taskMinutes = (wSpeedSection - taskHours * 3600) / 60;
                            int taskSeconds = (wSpeedSection - taskHours * 3600) - taskMinutes * 60;

                            logbookSb.append(String.format ("LTASK: Speed Section: %d %02d:%02d:%02d", wSpeedSection,taskHours,taskMinutes,taskSeconds));
                            logbookSb.append(RC);
                        }
                    }
                    flightInfo = logbookSb.toString();
                    
                    //wOffset = 49;     
                    wOffset = 0x1e + 0xc;
                }            
            }
   
            while(wOffset <4096)  {
                
                GPSDATA_HDR = FlyRaw[wOffset+dwScan];
                byteLength = GPSDATA_HDR& 0x1F;
                 
                 
                 
                 if (byteLength==0)
                 {
                    if (mDebug) mylogging.log(Level.INFO, "0 len Paket DeathLoop ai ai ai!! EXITING!");
                    System.out.println("0 len Paket DeathLoop ai ai ai!! EXITING!");
                    String dMsg = "pkt offst " + (wOffset +dwScan) + " type " + FlyRaw[wOffset+dwScan]  + " len "+byteLength;
                    System.out.println(dMsg );
                    if (mDebug) mylogging.log(Level.INFO, dMsg);
                    listPOS.clear();
                    return false;
                 }
                 
                if (byteLength==0x1F)
                {
                    // it could be the end of the sector, so i test if the byte WO the mask to see if it's FF
                    if ((FlyRaw[wOffset+dwScan]&0xFF)==0xFF)
                        break;
                }
                
                boolean usePressure=false;
                switch (GPSDATA_HDR &0x7F) //Mask for the first 7 bits
                {
                    case GPSRMC_LONG_P:
                        usePressure=true;
                    case GPSRMC_LONG_A_T | 0x40:
                    case GPSRMC_LONG_A | 0x40:    
                    {
                        nbPoints++;  
                        int altiOrPressure=0;
                        // beg Gil
                        latitude = fourBytesToInt(Arrays.copyOfRange(FlyRaw, wOffset+dwScan+2, wOffset+dwScan+6));
                        longitude = fourBytesToInt(Arrays.copyOfRange(FlyRaw, wOffset+dwScan+6, wOffset+dwScan+10));                                      // end Gil
                        altitude = (short)twoBytesToInt(Arrays.copyOfRange(FlyRaw, wOffset+dwScan+10, wOffset+dwScan+12));  
                        altiOrPressure = (short)twoBytesToInt(Arrays.copyOfRange(FlyRaw, wOffset+dwScan+12, wOffset+dwScan+14)); 
                        altitude = twoBytesToInt(Arrays.copyOfRange(FlyRaw, wOffset+dwScan+10, wOffset+dwScan+12)); 
                        altiOrPressure = twoBytesToInt(Arrays.copyOfRange(FlyRaw, wOffset+dwScan+12, wOffset+dwScan+14));
                        time  = fourBytesToInt(Arrays.copyOfRange(FlyRaw, wOffset+dwScan+14, wOffset+dwScan+18));
                        
                        gotGPSSPEED_HEADING=true;
                        speed = FlyRaw[wOffset+dwScan+1];
                        heading = (FlyRaw[wOffset+dwScan+18]&0xFF);
                        
                        //fixes some shitty first time readings
                        if (FirstGPSRMC)
                        {
                            if (heading > 0xB5)
                            {
                                heading=0;
                                if (speed >80)
                                    speed=0;
                            }
                            FirstGPSRMC=false;
                        }
                        
                        
                        dataType = IS_GEO;
                        debugRec = 1;
                        if (usePressure)
                        {
                            dPress=(double)(altiOrPressure)/10;
                            pAltitude = (int) ((1 - Math.pow(Math.abs((dPress)/1013.25), 0.190284)) * 44307.69);
                        } else {
                            pAltitude=altiOrPressure;
                        }
                        lostTasFrames++; // increment 1 
                        if (lostTasFrames>6)  
                        {
                            TAS =0;    //Set to 0 
                            TAS_Temp =0; //Set to 0
                        }
                        if ((GPSDATA_HDR &0x7F) == (GPSRMC_LONG_A_T | 0x40))
                        {
                            //Tas Stuff here
                           //Tas Stuff here
                          
                            lostTasFrames=0;  //Got Tas Frame .. zero out counter
                            TAS = FlyRaw[wOffset+dwScan+19];  //Tas info
                            TAS_Temp = FlyRaw[wOffset+dwScan+20];  //Tas Temperature info
                            
                            if ((TAS!=0) && (TAS_Temp!=0))
                                gotTAS=true; //set true for Irecord stuff
                            
                        }

                        break;
                     }   
                    case GPSRECORD_LONG_P:
                        usePressure=true;
                    case GPSRECORD_LONG_A_T |0x40:    
                    case GPSRECORD_LONG_A | 0x40: 
                    {
                        nbPoints++;   
                        int altiOrPressure=0;
                        latitude += FlyRaw[wOffset+dwScan+1];
                        longitude += FlyRaw[wOffset+dwScan+2];
                        altitude += FlyRaw[wOffset+dwScan+3];
                        altiOrPressure += FlyRaw[wOffset+dwScan+4];
                        time += oneByteToInt(FlyRaw[wOffset+dwScan+5]);
                        
                        gotGPSSPEED_HEADING=true;
                        speed = FlyRaw[wOffset+dwScan+6];
                        heading = (FlyRaw[wOffset+dwScan+7]&0xFF);
                        
                        dataType = IS_GEO;                                                                                      
                        debugRec = 2;
                        
                        if (usePressure)
                        {
                            dPress += (double)(altiOrPressure)/10;
                            pAltitude = (int) ((1 - Math.pow(Math.abs((dPress)/1013.25), 0.190284)) * 44307.69);
                        } else {
                            pAltitude +=altiOrPressure;
                        }                     
                        
                        lostTasFrames++; // increment 1 
                        if (lostTasFrames>6)  
                        {
                            TAS =0;    //Set to 0 
                            TAS_Temp =0; //Set to 0
                        }                                
                   
                        if ((GPSDATA_HDR &0x7F) == (GPSRECORD_LONG_A_T | 0x40))
                        {
                            //Tas Stuff here
                            
                            lostTasFrames=0;  //Got Tas Frame .. zero out counter
                            TAS = FlyRaw[wOffset+dwScan+8];  //Tas info
                            TAS_Temp = FlyRaw[wOffset+dwScan+9];  //Tas Temperature info
                            if ((TAS!=0) && (TAS_Temp!=0))
                                gotTAS=true; //set true for Irecord stuff
                        }
                        break;
                    }
                    case GPSRECORD_SHORT &0x1F:   //Old gps record 6 bytes.. but just in case you never now
                    {
                        nbPoints++;   
                        int pressure=0;
                        latitude += FlyRaw[wOffset+dwScan+1];
                        longitude += FlyRaw[wOffset+dwScan+2];
                        altitude += FlyRaw[wOffset+dwScan+3];
                        pressure += FlyRaw[wOffset+dwScan+4];
                        time += oneByteToInt(FlyRaw[wOffset+dwScan+5]);
                        dPress += (double)(pressure)/10;
                        pAltitude = (int) ((1 - Math.pow(Math.abs((dPress)/1013.25), 0.190284)) * 44307.69);
                        break;
                    }
                    case GPSLOST |0x20:  //lost gps signal
                    {
                        //UNIT32 timestamp
                        //Signed int 16 pressure altitude
                        dataType=0;
                        break;
                    }
                    case DECLARE_STORED | 0x40:
                    case DECLARE_STORED:  //Task Stuff
                    {
                        dataType=0;
                        int numPoints = FlyRaw[wOffset+dwScan+1];
                        int seq = FlyRaw[wOffset+dwScan+2];
                        int declTime = fourBytesToInt(Arrays.copyOfRange(FlyRaw, wOffset+dwScan+3, wOffset+dwScan+7));
                        long2Time(declTime);
                        txtTask.append("C");
                        nbTpoints=numPoints;
                        txtTask.append(String.format("%02d", day));
                        txtTask.append(String.format("%02d", month));
                        txtTask.append(String.format("%02d", year));
                        txtTask.append(String.format("%02d", hour));
                        txtTask.append(String.format("%02d", minute));
                        txtTask.append(String.format("%02d", second));
                        txtTask.append(String.format("%02d", day));
                        txtTask.append(String.format("%02d", month));
                        txtTask.append(String.format("%02d", year));
                        txtTask.append("0000");
                        
                        if (numPoints>=2)
                            numPoints-=3;
                        else 
                            numPoints=0;
                        
                        txtTask.append(String.format("%02d", numPoints));
                        txtTask.append(RC);
                        txtTask.append("C0000000N00000000WTAKEOFF");
                        txtTask.append(RC);
                        break;
                    }   
                    case TP_STORED:  //waypoint of the task
                    {
                        dataType=0;
                        nbTpoints--;
                        tpNum++;
                        int tpType = FlyRaw[wOffset+dwScan+1];
                        int tpSize = twoBytesToInt(Arrays.copyOfRange(FlyRaw, wOffset+dwScan+2, wOffset+dwScan+4));
                        int tpAlt = twoBytesToInt(Arrays.copyOfRange(FlyRaw, wOffset+dwScan+4, wOffset+dwScan+6));
                        int tpLat = fourBytesToInt(Arrays.copyOfRange(FlyRaw, wOffset+dwScan+6, wOffset+dwScan+10));
                        int tpLon = fourBytesToInt(Arrays.copyOfRange(FlyRaw, wOffset+dwScan+10, wOffset+dwScan+14));
                        byte [] tpNameBa  = Arrays.copyOfRange(FlyRaw, wOffset+dwScan+14, wOffset+dwScan+30);
                        String tpName = byteArrayToString(tpNameBa, 16);
 
                        txtTask.append("C");
                        txtTask.append(makeCoordString (tpLat,tpLon));
                        
                        //This code would be fantastic if i had a tpType data record..
                        /*
                        if (tpNum==1)
                            txtTask.append("START ");
                        else if (nbTpoints==2)
                            txtTask.append("FINISH ");
                        else if (nbTpoints==1)
                            txtTask.append("LANDING ");
                        else 
                            txtTask.append("TURN ");
                        */
                        
                        txtTask.append(tpName);
                        txtTask.append(RC);
                        
                        if (nbTpoints==1)
                        {
                            txtTask.append("C0000000N00000000ELANDING");
                            txtTask.append(RC);
                        }
                        
                        break;
                    }   
                    case PILOT_IN_CHARGE | 0x20: //Pilot name
                    {
                        dataType=0;
                        byte [] pilotNameba  = Arrays.copyOfRange(FlyRaw, wOffset+dwScan+1, wOffset+dwScan+17);
                        pilotName = byteArrayToString(pilotNameba, -1);
                        break;
                    }
					
                    case PILOT_IN_CHARGE_NEW | 0x20: //Pilot name
                    {
                        dataType=0;
                        byte [] pilotNameba  = Arrays.copyOfRange(FlyRaw, wOffset+dwScan+1, wOffset+dwScan+17);
                        pilotName = byteArrayToString(pilotNameba, -1);
                        break;
                    }
  
                    case HEARTDATA_STORED |0x20:
                    {
                        dataType=0;
                        HeartBeat = FlyRaw[wOffset+dwScan+1]&0xFF;  //Heart beat info
                        HeartStepsPerMin = FlyRaw[wOffset+dwScan+2]&0xFF;  //Steps per minute
                        gotHeart=true;
                        gotGforce=true;  //set true for Irecord stuff
                        GForce = FlyRaw[wOffset+dwScan+3];
                         //Only one time
                         if (gForceFirstPkt)
                         {
                             gForceFirstPkt=false;
                             if (GForce==0) // initail pkt , gives always 0 We Set to 1.0
                                GForce=10;
                         }
 
                    }
                    case TAS_STORED |0x20:
                    {
                        dataType=0;
                        lostTasFrames=0;  //Got Tas Frame .. zero out counter
                        TAS = FlyRaw[wOffset+dwScan+1];  //Tas info
                        TAS_Temp = FlyRaw[wOffset+dwScan+2];  //Tas Temperature info
                        if ((TAS!=0) && (TAS_Temp!=0))
                            gotTAS=true; //set true for Irecord stuff
                        
                        break;
                    }
                    case FW_STORED | 0x40: //Firmware of the track
                    {
                        dataType=0;
                        int fwVer = twoBytesToInt(Arrays.copyOfRange(FlyRaw, wOffset+dwScan+1, wOffset+dwScan+3)); 
                        int major =fwVer / 2600;
                        int minor = (fwVer - major * 2600) / 26;
                        int rev = fwVer % 26;
                        char subRev = (char)('a'+ rev);
                        trackFw  =   String.format("%d",major)+"."+String.format("%02d",minor)+ subRev;
                        break;
                    }   
					
                    case SOCFWDEV | 0x60: //Firmware of the track and device ID
                    {
                        dataType=0;
                        int fwVer = twoBytesToInt(Arrays.copyOfRange(FlyRaw, wOffset+dwScan+1, wOffset+dwScan+3)); 
                        int fwdevId = twoBytesToInt(Arrays.copyOfRange(FlyRaw, wOffset+dwScan+3, wOffset+dwScan+5)); 
                        
                        int major =fwVer / 2600;
                        int minor = (fwVer - major * 2600) / 26;
                        int rev = fwVer % 26;
                        char subRev = (char)('a'+ rev);
                        
                        trackFw  =   String.format("Flymaster %s (0x%02X) FW Ver: %d", getFMModelFromDeviceID(fwdevId) ,fwdevId ,major)+"."+String.format("%02d",minor)+ subRev;
                        
                        break;
                    }  
					
                    case EVENT_STORED | 0x20:  //Gforce Data
                    {
                         dataType=0;
                         gotGforce=true;  //set true for Irecord stuff
                         GForce = FlyRaw[wOffset+dwScan+1];
                         //Only one time
                         if (gForceFirstPkt)
                         {
                             gForceFirstPkt=false;
                             if (GForce==0) // initail pkt , gives always 0 We Set to 1.0
                                GForce=10;
                         }
                        break;
                    }
                                                   
                    default :
                         System.out.println("Unknown Data Paket");
                         if (mDebug) mylogging.log(Level.INFO, "Unknown Data Paket");
                         String dMsg = "pkt offst " + (wOffset +dwScan) + " type " + ((int)((FlyRaw[wOffset+dwScan]) >>>5)&0x7)   + " len "+byteLength;
                         System.out.println(dMsg );
                         if (mDebug) mylogging.log(Level.INFO, dMsg);
                         return false;
                 }
                
                wOffset +=byteLength;
                long2Time(time);
                switch (dataType) {
                    case IS_TAS:
                        if((dataRequested & REQUESTING_TAS) == REQUESTING_TAS) {
                            System.out.println("TAS ");
                            if (mDebug) mylogging.log(Level.INFO, "TAS");
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

                            String Heading_GpsSpeed =",0,0";
                            String GForceInfo =",10";
                            String TasInfo =",0,+0";
                            String HeartInfo =",0,0";
                           
                            
                            //String sPressure = (pressure / 10.0);
                                    
                                    //deviceFirm
                            if (gotGPSSPEED_HEADING)
                                Heading_GpsSpeed = ","+speed+","+heading;
                            
                            if (gotGforce)
                                GForceInfo= ","+ GForce;
                            
                            if (gotTAS)
                                TasInfo = ","+TAS+","+TAS_Temp;
                            if (gotHeart)
                                HeartInfo = ","+HeartBeat+","+HeartStepsPerMin;
 
                                                        
                            switch (uDecimalCoords) {
                                case 1:
                                    
                                    
                                    listPOS.add("POS," + (year + 2000) + "/" + month + "/" + day + "," + hour + ":" + minute + ":" + second + "," + fLatitude + "," + fLongitude + 
                                                "," + altitude + "," + pAltitude
                                                +Heading_GpsSpeed+GForceInfo + TasInfo+HeartInfo);
                                    break;
                                default:
                                    listPOS.add("POS," + (year + 2000) + "/" + month + "/" + day + "," + hour + ":" + minute + ":" + second + "," + (la1 / 60000) + "," 
                                                        + (la1 % 60000) + "," + chNS + ","+ (lo2 / 60000) + "," + (lo2 % 60000) + "," + chEW + "," + altitude + "," + pAltitude
                                                        +Heading_GpsSpeed+ GForceInfo + TasInfo+HeartInfo);
                                                        
                                    break;
                                }
                            GForce=10; //set to 10 anyway (10 is decimal (1.0))
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
        if (mDebug) mylogging.log(Level.INFO, "Points : "+nbPoints);
        
        if (tpNum==0)
            txtTask.delete(0, txtTask.length());
        
        return true;
      } 
    
    public String getFMModelFromDeviceID(int devID)
    {
        String strRetVal = "Unknown";
        switch (devID)
        {
            case 0xb301:
                strRetVal = "LiveSD";
                break;
            case 0xb311:
                strRetVal = "LiveSDII";
                break;
            case 0xb302:
                strRetVal = "NavSD";
                break;
            case 0xb312:
                strRetVal = "NavSDII";
                break;
            case 0xb303:
                strRetVal = "GpsSD+";
                break;
            case 0xb313:
                strRetVal = "GpsSD+II";
                break;
            case 0xb304:
                strRetVal = "GpsSD";
                break;
            case 0xb314:
                strRetVal = "GpsSDII";
                break;
            case 0xb305:
                strRetVal = "VarioSD";
                break;
            case 0xb315:
                strRetVal = "VarioSDII";
                break;
            case 0xb306:
                strRetVal = "Watch";
                break;
            case 0xb307:
                strRetVal = "Tracker";
                break;
            case 0xb30a:
                strRetVal = "VarioLS";
                break;
            case 0xb30b:
                strRetVal = "VarioLSII";
                break;
            case 0xb30c:
                strRetVal = "VarioLS_GPS";
                break;
            case 0xb201:
                strRetVal = "Live";
                break;
            case 0xb202:
                strRetVal = "Nav";
                break;
            case 0xb203:
                strRetVal = "Gps";
                break;
            case 0xb204:
                strRetVal = "Vario";
                break;

        }
        return strRetVal;
    }
    
    private String makeCoordString (int lat, int lon)
    {
        StringBuilder retval = new StringBuilder();
        double fLatitude = lat;
        double fLongitude = lon;
        fLatitude /= 60000.0;
        fLongitude /= 60000.0;   
        retval.append(Lat_Dd_IGC(fLatitude)).append(Long_Dd_IGC(fLongitude));
        return retval.toString();
    }
    
    private String byteArrayToString (byte[] inData, int strLen)
    {
        int len=0;
        if (strLen ==-1)
        {
            for (int i =1; i<=inData.length;i++)
            {
                byte tmp = inData[i];
                if (tmp ==0)
                {
                    len=i;
                    break;
                }
            }
        } else {
            len= strLen;
        }
        byte [] retvalba  = Arrays.copyOfRange(inData, 0, len);
        return  new String(retvalba);
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
            int iAltBaro = Integer.parseInt(tbLig[6]); 
            //double dPress = Double.parseDouble(tbLig[6]); 
            
            sbLigIGC.append("B").append(codeHour(sHour)).append(Lat_Dd_IGC(dLat)).append(Long_Dd_IGC(dLong));
            // Calcul d'AltiBaro dans le source de Cristiano
            //  fAltiBaro = (1 - pow(fabs((rmc.pressure/10.0)/1013.25), 0.190284)) * 44307.69;
            // Dans le source de Rishi la pression est déjà divisée par 10, donc on fait pas
            //  int iAltBaro = (1 - Math.pow(Math.abs((dPress/10.0)/1013.25), 0.190284)) * 44307.69;
            // mais ...
            //int iAltBaro = (int) ((1 - Math.pow(Math.abs((dPress)/1013.25), 0.190284)) * 44307.69);
            
            sbLigIGC.
                    append("A")
                    .append(String.format("%05d", iAltBaro))
                    .append(String.format("%05d", iAltGPS));
                   
            //Positions accordind to sectors defined in Irecord
            //GSP & HDM (gps speed & heading)
            // ACZ (gforce)
            // IAS & OAT  (tas speed and outside temp)
            // HRT & SPM  (Heart and Steps per minute)
            
            if (gotGPSSPEED_HEADING)
            {
                int  gpsSpeed = Integer.parseInt(tbLig[7]) &0xFF; 
                int  heading = Integer.parseInt(tbLig[8]) & 0xFF; 
                 sbLigIGC
                         .append(String.format("%03d",gpsSpeed).replace("-", ""))
                         .append(String.format("%03d", heading*2).replace("-", ""));
            }
            if (gotGforce)
            {
                String posNegG = "P";
                int  iGforce = Integer.parseInt(tbLig[9]); 
                if (iGforce<0)
                     posNegG = "N";
                 
                 sbLigIGC
                         .append(posNegG)
                         .append(String.format("%02d", iGforce).replace("-", ""));
            }
            
            if (gotTAS)
            {
                 int  iTas = Integer.parseInt(tbLig[10]); 
                 int  iTasTemp = Integer.parseInt(tbLig[11]); 
                 
                 //set negative limits
                 if (iTas<-99)
                     iTas = -99;
                 
                 if (iTasTemp<-99)
                     iTasTemp = -99;
                 
                 sbLigIGC
                         .append(String.format("%03d", iTas))
                         .append(String.format("%03d", iTasTemp));
            }

            if (gotHeart)
            {
                 int  iHeart = Integer.parseInt(tbLig[11]); 
                 int  iStepsPerMin = Integer.parseInt(tbLig[12]); 
                 sbLigIGC
                         .append(String.format("%03d",iHeart).replace("-", ""))
                         .append(String.format("%03d", iStepsPerMin).replace("-", ""));
            }

            
            txtIGC.append(sbLigIGC.toString()).append(RC);
        }
    }
    
    
    
    private String createIRecord()
    {
        
        //ACZ Linear accelerations in X, Y and Z axes, for aerobatic aircraft equipped with appropriate sensors feeding to the recorder and IGC file. X
        //= longitudinal, Y = lateral, Z = vertical (so-called "G")
        //IAS I, B, J, K Airspeed, three numbers in kilometres per hour
        //OAT Outside air temperature (Celsius). If negative, use negative sign before the numbers.
        //HDM I, B, J, K Heading Magnetic, three numbers based on degrees clockwise from 000 for north
        //GSP gps Speed
        StringBuilder retval = new StringBuilder();
        StringBuilder iRecord = new StringBuilder();
        int offset=36;
        int sectors =0;
       

        if (gotGPSSPEED_HEADING)
        {
            iRecord.append(
            String.format("%02d%02dGSP%02d%02dHDM", offset , offset +2 ,offset +2+1,offset +2+1+2)); 
            offset+=6;
            sectors+=2;
           
        }
        
        if  (gotGforce)
        {
            iRecord.append(String.format("%02d%02dACZ", offset , offset +2 ));
            offset+=3;
            sectors+=1;
        }
        
        if  (gotTAS)
        {
            iRecord.append(String.format("%02d%02dIAS%02d%02dOAT", offset , offset +2 ,offset +2+1,offset +2+1+2)); 
            offset+=6;
            sectors+=2;
        }
        
        if (gotHeart)
        {
            iRecord.append(String.format("%02d%02dHRT%02d%02dSPM", offset , offset +2 ,offset +2+1,offset +2+1+2)); 
            offset+=6;
            sectors+=2;
        }
        
        
        if (iRecord.length()>0)
            retval.append(String.format ("I%02d",sectors)).append(iRecord.toString());


        return retval.toString();
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
            if (pilotName.equals(""))
                txtIGC.append("HFPLTPILOT:").append(sPilote).append(RC);
            else 
                txtIGC.append("HFPLTPILOT:").append(pilotName).append(RC);
            
            txtIGC.append("HFGTYGLIDERTYPE:").append(sVoile).append(RC);
            txtIGC.append("HPCIDCOMPETITIONID: ").append(RC);
            if (deviceFirm != null) {            
                if (trackFw.equals(""))
                {
                    txtIGC.append("HFRFWFIRMWAREVERSION: ").append(deviceFirm).append(RC);
                } else {
                    txtIGC.append("HFRFWFIRMWAREVERSION: ").append(trackFw).append(RC);
                }
            } else {
                txtIGC.append("HFRHWHARDWAREVERSION: unknown").append(RC);
            }
            
            
         
            String iRecord= createIRecord();
            
            if (iRecord.length()>0)
                txtIGC.append (iRecord).append(RC);
            
            //Task Lines
            txtIGC.append(txtTask.toString());
            
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
