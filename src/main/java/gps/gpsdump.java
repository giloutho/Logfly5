/*
 * Copyright Gil THOMAS
 * This file forms an integral part of Logfly project
 * See the LICENSE file distributed with source code
 * for details of Logfly licence project
 */
package gps;

import controller.CarnetViewController;
import controller.FullMapController;
import controller.TraceViewController;
import controller.GPSViewController;
import controller.WaypViewController;
import dialogues.ProgressForm;
import dialogues.alertbox;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import model.Gpsmodel;
import settings.configProg;
import systemio.mylogging;
import systemio.textio;

/**
 *
 * @author gil
 */
public class gpsdump {
  
    private CarnetViewController carnetController;
    private GPSViewController gpsController;
    private TraceViewController extController;
    private FullMapController mapController;

    // Settings
    configProg myConfig;

    private StringBuilder sbError;
    private int errorGpsDump;    
    private String pathGpsDump;    
    private int codeRetour;      
    private File igcFile;    
    private String CF =  "\r\n"; 
    private String strLog;
    private String portNumber; 
    private String linuxPort;
    private ArrayList<String> listPFM;
    private ObservableList <Gpsmodel> listFlights;   
    

    public gpsdump(String pNamePort, configProg currConfig)  {
        myConfig = currConfig;
        switch (myConfig.getOS()) {
            case WINDOWS :
                this.portNumber = pNamePort.replace("COM","");   // For Windows we need only the port number 
                break;
            case LINUX :
                String subPort = "ca0";
                if (pNamePort.length() > 8) subPort = pNamePort.substring(0,9);
                switch (subPort) {
                    case "/dev/ttyA":
                        linuxPort = pNamePort.replace("/dev/ttyACM","-ca");  
                        break;
                     case "/dev/ttyS":
                        linuxPort = pNamePort.replace("/dev/ttyS","-c");  
                        break;       
                     case "/dev/ttyU":
                        linuxPort = pNamePort.replace("/dev/ttyUSB","-cu");  
                        break;                            
                }                
        }
    }
    
    public gpsdump(GPSViewController callGPSView, int pRetour, String pNamePort, configProg currConfig)  {
        myConfig = currConfig;
        this.gpsController = callGPSView;
        listFlights = FXCollections.observableArrayList();  
         switch (myConfig.getOS()) {
            case WINDOWS :
                this.portNumber = pNamePort.replace("COM","");   // For Windows we need only the port number 
                break;
            case LINUX :
                String subPort = "ca0";
                if (pNamePort.length() > 8) subPort = pNamePort.substring(0,9);
                switch (subPort) {
                    case "/dev/ttyA":
                        linuxPort = pNamePort.replace("/dev/ttyACM","-ca");  
                        break;
                     case "/dev/ttyS":
                        linuxPort = pNamePort.replace("/dev/ttyS","-c");  
                        break;       
                     case "/dev/ttyU":
                        linuxPort = pNamePort.replace("/dev/ttyUSB","-cu");  
                        break;                            
                }                
         }
        codeRetour = pRetour;
    }    
    
    public int getError() {
        return errorGpsDump;
    }    

    public String getStrLog() {
        return strLog;
    }

    public ObservableList<Gpsmodel> getListFlights() {
        return listFlights;
    }            
        
    public boolean testGpsDump() {
        boolean gpsdumpOK = false;
        
        String executionPath = System.getProperty("user.dir");
        
            switch (myConfig.getOS()) {
                    case MACOS :
                    case WINDOWS :
                        pathGpsDump = executionPath+File.separator+"GpsDump";
                        break;
                    case LINUX :
                        pathGpsDump = executionPath+File.separator+"gpsdump";
                        System.out.println(pathGpsDump);
                        break;
            }
            File fGpsDump = new File(pathGpsDump);
            if(fGpsDump.exists()) gpsdumpOK = true;         
                
        return gpsdumpOK;
    }
    
    private static BufferedReader getOutput(Process p) {
        return new BufferedReader(new InputStreamReader(p.getInputStream()));
    }

    private static BufferedReader getError(Process p) {
        return new BufferedReader(new InputStreamReader(p.getErrorStream()));
    }    

    /**
     * Windows call need more parameters
     *    -  /com="Port number"
     *    -  /igc_log="File name or folder name"
     *    -  /win="Window option"  Select how GpsDump shall be shown (0=Hide, 1=Minimized, 2=Show)
     *    - /exit   Used to make GpsDump exit after doing the job.
     * @param idGPS
     * @param idFlight
     * @return 
     */
    private int getFlight(int idGPS, int idFlight)  {
        int res = -1; 
        String[] arrayParam = null;
        boolean gpsDumpOK = false;
        String wNoWin = "/win=0";  
        String wComPort = "/com="+portNumber;
        String wExit = "/exit";        
        String sTypeGps = "";       
        StringBuilder sbLog = new StringBuilder();
        switch (idGPS) {
            case 1:
                switch (myConfig.getOS()) {
                    case MACOS :
                    case WINDOWS :
                        sTypeGps = "/gps=flymaster";
                        break;
                    case LINUX : 
                        sTypeGps = "-gyn"; 
                        break;
                }
                break;
            case 2:
                 switch (myConfig.getOS()) {
                    case MACOS :
                        sTypeGps = "/gps=flymasterold";
                        break;
                    case WINDOWS :                     
                        sTypeGps = "/gps=flymasterold";
                        break;
                    case LINUX : 
                        sTypeGps = "-gy";    // A vérifier
                        break;
                }
                break;               
            case 3:
                switch (myConfig.getOS()) {
                    case WINDOWS :
                        sTypeGps = "/gps=iqcompeo";	// Compeo/Compeo+/Galileo/Competino/Flytec 5020,5030,6030
                        break;
                    case MACOS :
                        sTypeGps = "/gps=flytec";
                        break;
                    case LINUX :
                        sTypeGps = "-gc";
                        break;                        
                }
                break;
            case 4:
                switch (myConfig.getOS()) {
                    case MACOS :
                    case WINDOWS : 
                        sTypeGps = "/gps=ascent";
                        break;
                }
                break; 
            case 5:
                switch (myConfig.getOS()) {
                    case MACOS :
                    case WINDOWS : 
                        sTypeGps = "/gps=syride";
                        break;
                    case LINUX :
                        sTypeGps = "-gsy";
                        break;                        
                }
                break;
            case 6:
                switch (myConfig.getOS()) {
                    case MACOS :
                    case WINDOWS :                 
                        sTypeGps = "/gps=leonardo";
                        break;
                }
                break; 
            case 7:
                switch (myConfig.getOS()) {
                    case MACOS :
                    case WINDOWS : 
                        sTypeGps = "/gps=digiflyair";
                        break;
                }
                break;   
            case 8:
                switch (myConfig.getOS()) {
                    case WINDOWS :
                        sTypeGps = "/gps=iqbasic";	// IQ-Basic / Flytec 6015
                        break;
                    case MACOS :
                        sTypeGps = "/gps=iqbasic";       // with Mac, same as Compeo/Compeo+/Galileo/Competino/Flytec 5020,5030,6030
                        break;
                    case LINUX :
                        sTypeGps = "-giq";
                        break;
                }
                break;                
        }
        igcFile = systemio.tempacess.getAppFile("Logfly", "temp.igc");
        if (igcFile.exists())  igcFile.delete();              
        String numberIGC = null;
        // Index track is different in Mac 
        switch (myConfig.getOS()) {
            case MACOS :
                numberIGC = "/track="+String.valueOf(idFlight+1);
                break;
            case WINDOWS :
                switch (idGPS) {
                        case 1 :
                            // Flymaster
                            numberIGC = "/track="+String.valueOf(idFlight+1);     
                            break;
                        case 2 :
                            // Flymaster Old
                            numberIGC = "/track="+String.valueOf(idFlight+1);    
                            break;    
                        case 3 :
                            // 6020 6030
                            numberIGC = "/track="+String.valueOf(idFlight+1);   
                            break;
                        case 8: 
                            // 6015 
                            numberIGC = "/track="+String.valueOf(idFlight);
                            break;
                    }                      
                break;
            case LINUX :
                    switch (idGPS) {
                        case 1 :
                            // Flymaster
                            numberIGC = "-f"+String.valueOf(idFlight+1);       
                            break;
                        case 2 :
                            // Flymaster Old
                            numberIGC = "-f"+String.valueOf(idFlight+1);       
                            break;    
                        case 3 :
                            // 6020 6030
                            numberIGC = "-f"+String.valueOf(idFlight+1);       
                            break;
                        case 8: 
                            // 6015 
                            // Non résolu pour le premier vol qui est numéroté 0 
                            // donc on obtient la liste et une attente clavier
                            numberIGC = "-f"+String.valueOf(idFlight+1); 
                            break;
                    }              
                break;
        }

        try {
            String executionPath = System.getProperty("user.dir");
            switch (myConfig.getOS()) {
                case WINDOWS :
                    // to do windows path testing
                    pathGpsDump = executionPath+File.separator+"GpsDump.exe";    // Windows
                    File fwGpsDump = new File(pathGpsDump);
                    if(fwGpsDump.exists()) gpsDumpOK = true;         
                    break;                
                case MACOS :
                    pathGpsDump = executionPath+File.separator+"GpsDump";
                    File fmGpsDump = new File(pathGpsDump);
                    if(fmGpsDump.exists()) gpsDumpOK = true;  
                    break;
                case LINUX :
                    pathGpsDump = executionPath+File.separator+"gpsdump";
                    System.out.println(pathGpsDump);
                    File flGpsDump = new File(pathGpsDump);
                    if(flGpsDump.exists()) gpsDumpOK = true;                        
                    break;                    
            }    
            if (gpsDumpOK) {
                // http://labs.excilys.com/2012/06/26/runtime-exec-pour-les-nuls-et-processbuilder/
                // the author has serious doubts : ok only if program run correctly or crashes
                switch (myConfig.getOS()) {
                    case WINDOWS :
                        String logIGC = "/igc_log="+igcFile.getAbsolutePath();  
                        arrayParam = new String[]{pathGpsDump,wNoWin,wComPort,sTypeGps, logIGC, numberIGC, wExit};
                        break;
                    case MACOS : 
                        String nameIGC = "/name="+igcFile.getAbsolutePath();   
                        arrayParam =new String[]{pathGpsDump,sTypeGps, nameIGC, numberIGC};
                        break;
                    case LINUX : 
                        String tempIGC = "-l"+igcFile.getAbsolutePath();   
                        arrayParam =new String[]{pathGpsDump,sTypeGps, linuxPort, tempIGC, numberIGC};
                        break;                        
                }
                sbLog.append("Call : ").append(java.util.Arrays.toString(arrayParam));
                System.out.println(java.util.Arrays.toString(arrayParam));
                Process p = Runtime.getRuntime().exec(arrayParam);   
                p.waitFor();
                res = p.exitValue();  // 0 if all is OK  
                // Sometimes, a flight is missing
                // Log report temp.igc missing
                // We try to wait one second 
                
                String ligne = ""; 
                if (res == 0) {
                    BufferedReader output = getOutput(p);                    
                    while ((ligne = output.readLine()) != null) {
                        sbLog.append(ligne).append(CF);
                    }
                } else {
                    BufferedReader error = getError(p);
                    while ((ligne = error.readLine()) != null) {
                        sbLog.append(ligne).append(CF);
                    }
                }
                strLog = sbLog.toString();
            } else {
                res = 1201;
                errorGpsDump = 1201;                
            }    
        } catch (FileNotFoundException ex) {
            res = 1;
            errorGpsDump = 1;
        } catch (IOException ex) {
            res = 2;
            errorGpsDump = 2;       
        } catch (InterruptedException ex) {
            res = 6;
            errorGpsDump = 6;   
        } catch (NullPointerException ex) {
            res = 5;
            errorGpsDump = 5;
        } 
        
        return res;                          
    }    
    
    public String directFlight(int idGPS, int idFlight)  { 
        String res = null;
        try {
            int resDown = getFlight(idGPS,idFlight);
            if (resDown == 0 && igcFile.exists()) {
                // We want to check GPSDump communication
                mylogging.log(Level.INFO, strLog.toString());
                textio fread = new textio();                                    
                res = fread.readTxt(igcFile);
            } else {
                sbError = new StringBuilder("===== GPSDump Error =====\r\n");
                sbError.append(strLog);
                mylogging.log(Level.SEVERE, sbError.toString());
            }
        } catch (Exception e) {
            sbError = new StringBuilder("===== GPSDump Error =====\r\n");
            sbError = sbError.append(this.getClass().getName()+"."+Thread.currentThread().getStackTrace()[1].getMethodName());
            mylogging.log(Level.SEVERE, sbError.toString());
        }
                
        return res;
    }
    
    public void askFlightsList(int idGPS) {
        
        int res = getRawList(idGPS);
        
        if (res ==0) {            
            switch (myConfig.getOS()) {
                    case WINDOWS :
                        winListFormatting();
                        break;
                    case MACOS :
                        macListFormatting();
                        break;
                    case LINUX : 
                        linuxListFormatting();
                        break;
                }
        } // if error strLog will be read;
    }     
    
    /**
     * A line is : 2019.08.10,17:33:03,0:01:36
     */
    private void winListFormatting() {
        int nbFlights = 0;
        
        for (int i = 0; i < listPFM.size(); i++) {
            String ligPFM = listPFM.get(i);    
            // Sample : 2019.08.14,13:13:32,0:27:25
            Pattern pDate = Pattern.compile("\\d{2}.\\d{2}.\\d{2}");
            Matcher mDate = pDate.matcher(ligPFM);
            if (mDate.find()) {   
                // date is reverses 
                String sDate = mDate.group(0).substring(6)+ mDate.group(0).substring(2,6)+ mDate.group(0).substring(0,2);
                nbFlights++;
                String sTime = null;
                String sDur = null;
                // durée vol supérieure à 9 heures 59 -> 2019.08.03,17:39:54,11:12:15
                Pattern pTime2 = Pattern.compile("\\d{2}:\\d{2}:\\d{2},\\d{2}:\\d{2}:\\d{2}");
                Matcher mTime2 = pTime2.matcher(ligPFM);                    
                if (mTime2.find()) {
                    sTime = mTime2.group(0).substring(0,8); 
                    sDur = mTime2.group(0).substring(9);                         
                } else {
                    // durée vol inférieure à 9 heures 59 -> 2019.08.10,17:33:03,0:01:36
                    Pattern pTime1 = Pattern.compile("\\d{2}:\\d{2}:\\d{2},\\d{1}:\\d{2}:\\d{2}");
                    Matcher mTime1 = pTime1.matcher(ligPFM);                    
                    if (mTime1.find()) {
                        sTime = mTime1.group(0).substring(0,8); 
                        sDur = mTime1.group(0).substring(9);  
                    }
                }
                // System.out.println(sDate+" "+sTime+" "+sDur);
                Gpsmodel oneFlight = new Gpsmodel();                                             
                oneFlight.setChecked(false);
                oneFlight.setDate(sDate);
                oneFlight.setHeure(sTime);
                oneFlight.setCol4(sDur);
                oneFlight.setCol5(null);                
                listFlights.add(oneFlight);                
            }                
        }
        if (nbFlights == 0) {
            sbError = new StringBuilder();
            for (int i = 0; i < listPFM.size(); i++) {                
                sbError.append(listPFM.get(i)).append(CF);                
            }              
            System.out.println("Sb error "+sbError.toString());
            strLog = sbError.toString();
        }        
    }
    
    private void linuxListFormatting() {
        int nbFlights = 0;
        
        for (int i = 0; i < listPFM.size(); i++) {
            String ligPFM = listPFM.get(i);    
            // Sample :  1   14.08.19   13:13:32   00:27:25
            Pattern pDate = Pattern.compile("\\d{2}.\\d{2}.\\d{2}");
            Matcher mDate = pDate.matcher(ligPFM);
            if (mDate.find()) {   
                // date is reverses 
                String sDate = mDate.group(0);
                nbFlights++;
                String sTime = null;
                String sDur = null;
                Pattern pTime = Pattern.compile("\\d{2}:\\d{2}:\\d{2}\\s\\s\\s\\d{2}:\\d{2}:\\d{2}");
                Matcher mTime = pTime.matcher(ligPFM);                    
                if (mTime.find()) {
                    sTime = mTime.group(0).substring(0,8); 
                    sDur = mTime.group(0).substring(11);                         
                } 
                // System.out.println(sDate+" "+sTime+" "+sDur);
                Gpsmodel oneFlight = new Gpsmodel();                                             
                oneFlight.setChecked(false);
                oneFlight.setDate(sDate);
                oneFlight.setHeure(sTime);
                oneFlight.setCol4(sDur);
                oneFlight.setCol5(null);                
                listFlights.add(oneFlight);                
            }                
        }
        if (nbFlights == 0) {
            sbError = new StringBuilder();
            for (int i = 0; i < listPFM.size(); i++) {                
                sbError.append(listPFM.get(i)).append(CF);                
            }              
            System.out.println("Sb error "+sbError.toString());
            strLog = sbError.toString();
        }         
    }
    
    private void macListFormatting() {
        int nbFlights = 0;
        
        for (int i = 0; i < listPFM.size(); i++) {
            String ligPFM = listPFM.get(i);            
            Pattern pDate = Pattern.compile("Flight date\\s\\d{2}.\\d{2}.\\d{2}");
            Matcher mDate = pDate.matcher(ligPFM);
            if (mDate.find()) {   
                String sDate = mDate.group(0).substring(12);
                nbFlights++;
                String sTime = null;
                String sDur = null;
                Pattern pTime = Pattern.compile("time\\s\\d{2}:\\d{2}:\\d{2}");
                Matcher mTime = pTime.matcher(ligPFM);
                if (mTime.find()) {
                    sTime = mTime.group(0).substring(5);
                    Pattern pDur = Pattern.compile("duration\\s\\d{2}:\\d{2}:\\d{2}");

                    Matcher mDur = pDur.matcher(ligPFM);
                    if (mDur.find()) {
                        sDur = mDur.group(0).substring(9);
                    }
                }
                Gpsmodel oneFlight = new Gpsmodel();                                             
                oneFlight.setChecked(false);
                oneFlight.setDate(sDate);
                oneFlight.setHeure(sTime);
                oneFlight.setCol4(sDur);
                oneFlight.setCol5(null);                
                listFlights.add(oneFlight);                
            }             
        }
        if (nbFlights == 0) {
            sbError = new StringBuilder();
            for (int i = 0; i < listPFM.size(); i++) {                
                sbError.append(listPFM.get(i)).append(CF);                
            }              
            System.out.println("Sb error "+sbError.toString());
            strLog = sbError.toString();
        }                
    }
    
    private int getRawList(int idGPS)  {   
        
        int res = -1; 
        String[] arrayParam = null;
        boolean gpsDumpOK = false;
        String numberIGC = "";
        String wNoWin = "/win=0";  
        String wExit = "/exit";        
        File listFile = systemio.tempacess.getAppFile("Logfly", "temp.txt");
        if (listFile.exists())  listFile.delete();          
        String sNotify = "/notify="+listFile.getAbsolutePath();
        String wComPort = "/com="+portNumber;
        String sOverw = "/overwrite";
        String sTypeGps = "";       
        String sAction = "";
        StringBuilder sbLog = new StringBuilder();
        switch (idGPS) {
            case 1:
                switch (myConfig.getOS()) {
                    case MACOS :
                    case WINDOWS :
                        sTypeGps = "/gps=flymaster";
                        break;
                    case LINUX : 
                        sTypeGps = "-gyn"; 
                        break;
                }
                break;
            case 2:
                 switch (myConfig.getOS()) {
                    case MACOS :
                        sTypeGps = "/gps=flymasterold";
                        break;
                    case WINDOWS :                     
                        sTypeGps = "/gps=flymasterold";
                        break;
                    case LINUX : 
                        sTypeGps = "-gy";    // A vérifier
                        break;
                }
                break;               
            case 3:
                switch (myConfig.getOS()) {
                    case WINDOWS :
                        sTypeGps = "/gps=iqcompeo";	// Compeo/Compeo+/Galileo/Competino/Flytec 5020,5030,6030
                        break;
                    case MACOS :
                        sTypeGps = "/gps=flytec";
                        break;
                    case LINUX :
                        sTypeGps = "-gc";
                        break;                        
                }
                break;
            case 4:
                switch (myConfig.getOS()) {
                    case MACOS :
                    case WINDOWS : 
                        sTypeGps = "/gps=ascent";
                        break;
                }
                break; 
            case 5:
                switch (myConfig.getOS()) {
                    case MACOS :
                    case WINDOWS : 
                        sTypeGps = "/gps=syride";
                        break;
                    case LINUX :
                        sTypeGps = "-gsy";
                        break;                        
                }
                break;
            case 6:
                switch (myConfig.getOS()) {
                    case MACOS :
                    case WINDOWS :                 
                        sTypeGps = "/gps=leonardo";
                        break;
                }
                break; 
            case 7:
                switch (myConfig.getOS()) {
                    case MACOS :
                    case WINDOWS : 
                        sTypeGps = "/gps=digiflyair";
                        break;
                }
                break;   
            case 8:
                switch (myConfig.getOS()) {
                    case WINDOWS :
                        sTypeGps = "/gps=iqbasic";	// IQ-Basic / Flytec 6015
                        break;
                    case MACOS :
                        sTypeGps = "/gps=iqbasic";       // with Mac, same as Compeo/Compeo+/Galileo/Competino/Flytec 5020,5030,6030
                        break;
                    case LINUX :
                        sTypeGps = "-giq";
                        break;
                }
                break;                
        }        
        switch (myConfig.getOS()) {
            case MACOS :
                sAction = "/flightlist";
                break;
            case WINDOWS :
                sAction = "/flightlist";
                break;
            case LINUX :
                // -f“N” Select a specific flight (Brauniger/Flytec/Flymaster). If N=0 a flightlist is displayed.
                numberIGC = "-f0";
                break;
        }        
        try {
            String executionPath = System.getProperty("user.dir");
            switch (myConfig.getOS()) {
                case WINDOWS :
                    // to do windows path testing
                    pathGpsDump = executionPath+File.separator+"GpsDump.exe";    // Windows
                    File fwGpsDump = new File(pathGpsDump);
                    if(fwGpsDump.exists()) gpsDumpOK = true;         
                    break;                
                case MACOS :
                    pathGpsDump = executionPath+File.separator+"GpsDump";
                    File fmGpsDump = new File(pathGpsDump);
                    if(fmGpsDump.exists()) gpsDumpOK = true;  
                    break;
                case LINUX :
                    pathGpsDump = executionPath+File.separator+"gpsdump";
                    System.out.println(pathGpsDump);
                    File flGpsDump = new File(pathGpsDump);
                    if(flGpsDump.exists()) gpsDumpOK = true;                        
                    break;                    
            }    
            if (gpsDumpOK) {
                listPFM =new ArrayList<String>();
                // http://labs.excilys.com/2012/06/26/runtime-exec-pour-les-nuls-et-processbuilder/
                // the author has serious doubts : ok only if program run correctly or crashes
                switch (myConfig.getOS()) {
                    case WINDOWS :
                        arrayParam = new String[]{pathGpsDump,wNoWin, wComPort, sTypeGps, sAction, sNotify, sOverw,wExit};
                        break;
                    case MACOS : 
                        arrayParam =new String[]{pathGpsDump,sTypeGps, sAction};                        
                        break;
                    case LINUX :   
                        // result is displayed on the screen but a file path is required
                        String tempList = "-l"+listFile.getAbsolutePath();   
                        arrayParam =new String[]{pathGpsDump,sTypeGps, linuxPort, tempList, numberIGC};
                        break;                        
                }
                sbLog.append("Call : ").append(java.util.Arrays.toString(arrayParam)).append(CF);
                Process p = Runtime.getRuntime().exec(arrayParam);   
                p.waitFor();
                res = p.exitValue();  // 0 if all is OK  
                switch (myConfig.getOS()) {
                    case WINDOWS :
                        if (res == 0) {
                            System.out.println("res = 0");
                            if (listFile.exists()) {
                                try {
                                    InputStream flux=new FileInputStream(listFile); 
                                    InputStreamReader lecture=new InputStreamReader(flux);
                                    BufferedReader buff=new BufferedReader(lecture);
                                    String ligne;
                                    while ((ligne=buff.readLine())!=null){
                                        listPFM.add(ligne);
                                    }
                                    buff.close(); 		
                                } catch (Exception e) {
                                    res = 1;
                                    sbError = new StringBuilder(this.getClass().getName()+"."+Thread.currentThread().getStackTrace()[1].getMethodName());                                    
                                    sbError.append("\r\n").append(e.toString());
                                    sbError.append("\r\n").append("Problem to read flightlist file");
                                }
                            } else {
                                sbError = new StringBuilder("===== GPSDump return Error : No flight list =====\r\n");
                                sbError = sbError.append(sbLog.toString());
                                mylogging.log(Level.INFO, sbError.toString());
                                sbLog.setLength(0);
                                sbLog.append("GPSDump error").append(" ").append("No flight list returned");                               
                            }                                 
                        } else {
                            sbError = new StringBuilder("===== GPSDump return Error : No response from GPS =====\r\n");
                            sbError = sbError.append(sbLog.toString());
                            mylogging.log(Level.INFO, sbError.toString());
                            sbLog.setLength(0);
                            sbLog.append(sTypeGps).append(" ").append("No response from GPS");
                        }    
                        break;
                    case MACOS : 
                        String ligne = ""; 
                        if (res == 0) {
                            BufferedReader output = getOutput(p);                    
                            while ((ligne = output.readLine()) != null) {
                                listPFM.add(ligne);
                            }
                        } else {
                            BufferedReader error = getError(p);
                            while ((ligne = error.readLine()) != null) {
                                listPFM.add(ligne);
                            }
                        }                                               
                        break;
                    case LINUX :                         
                        ligne = ""; 
                        if (res == 255) {
                            // GPSDump returned a flightlist with an error code : Flight number out of range 
                            res = 0;
                            BufferedReader output = getOutput(p);                    
                            while ((ligne = output.readLine()) != null) {
                                listPFM.add(ligne);
                            }
                        } else {
                            BufferedReader error = getError(p);
                            while ((ligne = error.readLine()) != null) {
                                listPFM.add(ligne);
                            }
                        }                                               
                        break;   
                }                
            } else {
                sbLog.append("Error 1201 ").append(CF);
                res = 1201;
                errorGpsDump = 1201;                
            }    
        } catch (Exception ex) {
            sbLog.append("Error 1 ").append(CF);
            res = 1;
            errorGpsDump = 1;
        }  
        
        strLog = sbLog.toString();
        return res;            
        
    } 
    
     public int getOziWpt(int idGPS, File wptFile)  {   
        
        int res = -1; 
        if (wptFile.exists())  wptFile.delete();  
        String[] arrayParam = null;
        boolean gpsDumpOK = false;
        String numberIGC = "";
        String wNoWin = "/win=0";  
        String wExit = "/exit";                
        String wComPort = "/com="+portNumber;
        String sOverw = "/overwrite";
        String sTypeGps = "";       
        String sAction = "";
        StringBuilder sbLog = new StringBuilder();
        switch (idGPS) {
            case 1:
                switch (myConfig.getOS()) {
                    case MACOS :
                    case WINDOWS :
                        sTypeGps = "/gps=flymaster";
                        break;
                    case LINUX : 
                        sTypeGps = "-gyn"; 
                        break;
                }
                break;
            case 2:
                 switch (myConfig.getOS()) {
                    case MACOS :
                        sTypeGps = "/gps=flymasterold";
                        break;
                    case WINDOWS :                     
                        sTypeGps = "/gps=flymasterold";
                        break;
                    case LINUX : 
                        sTypeGps = "-gy";    // A vérifier
                        break;
                }
                break;               
            case 3:
                switch (myConfig.getOS()) {
                    case WINDOWS :
                        sTypeGps = "/gps=iqcompeo";	// Compeo/Compeo+/Galileo/Competino/Flytec 5020,5030,6030
                        break;
                    case MACOS :
                        sTypeGps = "/gps=flytec";
                        break;
                    case LINUX :
                        sTypeGps = "-gc";
                        break;                        
                }
                break;
            case 4:
                switch (myConfig.getOS()) {
                    case MACOS :
                    case WINDOWS : 
                        sTypeGps = "/gps=ascent";
                        break;
                }
                break; 
            case 5:
                switch (myConfig.getOS()) {
                    case MACOS :
                    case WINDOWS : 
                        sTypeGps = "/gps=syride";
                        break;
                    case LINUX :
                        sTypeGps = "-gsy";
                        break;                        
                }
                break;
            case 6:
                switch (myConfig.getOS()) {
                    case MACOS :
                    case WINDOWS :                 
                        sTypeGps = "/gps=leonardo";
                        break;
                }
                break; 
            case 7:
                switch (myConfig.getOS()) {
                    case MACOS :
                    case WINDOWS : 
                        sTypeGps = "/gps=digiflyair";
                        break;
                }
                break;   
            case 8:
                switch (myConfig.getOS()) {
                    case WINDOWS :
                        sTypeGps = "/gps=iqbasic";	// IQ-Basic / Flytec 6015
                        break;
                    case MACOS :
                        sTypeGps = "/gps=iqbasic";       // with Mac, same as Compeo/Compeo+/Galileo/Competino/Flytec 5020,5030,6030
                        break;
                    case LINUX :
                        sTypeGps = "-giq";
                        break;
                }
                break;                
        }        
        switch (myConfig.getOS()) {
            case MACOS :
                sAction = "/flightlist";
                break;
            case WINDOWS :
                sAction = "/rd_wpt="+wptFile.getAbsolutePath();
                break;
            case LINUX :
                // -f“N” Select a specific flight (Brauniger/Flytec/Flymaster). If N=0 a flightlist is displayed.
                numberIGC = "-f0";
                break;
        }        
        try {
            String executionPath = System.getProperty("user.dir");
            switch (myConfig.getOS()) {
                case WINDOWS :
                    // to do windows path testing
                    pathGpsDump = executionPath+File.separator+"GpsDump.exe";    // Windows
                    File fwGpsDump = new File(pathGpsDump);
                    if(fwGpsDump.exists()) gpsDumpOK = true;         
                    break;                
                case MACOS :
                    pathGpsDump = executionPath+File.separator+"GpsDump";
                    File fmGpsDump = new File(pathGpsDump);
                    if(fmGpsDump.exists()) gpsDumpOK = true;  
                    break;
                case LINUX :
                    pathGpsDump = executionPath+File.separator+"gpsdump";
                    System.out.println(pathGpsDump);
                    File flGpsDump = new File(pathGpsDump);
                    if(flGpsDump.exists()) gpsDumpOK = true;                        
                    break;                    
            }    
            if (gpsDumpOK) {
                listPFM =new ArrayList<String>();
                // http://labs.excilys.com/2012/06/26/runtime-exec-pour-les-nuls-et-processbuilder/
                // the author has serious doubts : ok only if program run correctly or crashes
                switch (myConfig.getOS()) {
                    case WINDOWS :
                        arrayParam = new String[]{pathGpsDump,wNoWin, wComPort, sTypeGps, sAction, sOverw,wExit};
                        break;
                    case MACOS : 
                        arrayParam =new String[]{pathGpsDump,sTypeGps, sAction};                        
                        break;
                    case LINUX :   
                        // result is displayed on the screen but a file path is required  
                     //   arrayParam =new String[]{pathGpsDump,sTypeGps, linuxPort, tempList, numberIGC};
                        break;                        
                }
                sbLog.append("Call : ").append(java.util.Arrays.toString(arrayParam)).append(CF);
                System.out.println("Call : "+(java.util.Arrays.toString(arrayParam)));
                Process p = Runtime.getRuntime().exec(arrayParam);   
                p.waitFor();
                res = p.exitValue();  // 0 if all is OK  
                System.out.println("res = "+res);
                String ligne = ""; 
                if (res == 0) {
                    BufferedReader output = getOutput(p);                    
                    while ((ligne = output.readLine()) != null) {
                        sbLog.append(ligne).append(CF);
                    }
                } else {
                    BufferedReader error = getError(p);
                    while ((ligne = error.readLine()) != null) {
                        sbLog.append(ligne).append(CF);
                    }
                }
                strLog = sbLog.toString();               
            } else {
                sbLog.append("Error 1201 ").append(CF);
                res = 1201;
                errorGpsDump = 1201;                
            }    
        } catch (Exception ex) {
            sbLog.append("Error 1 ").append(CF);
            res = 1;
            errorGpsDump = 1;
        }  
        
        strLog = sbLog.toString();
        return res;            
        
    }    

     public int setOziWpt(int idGPS, String pPath, int gpsTypeName)  {   
        
        int res = -1;   
        String[] arrayParam = null;
        File wptFile = new File(pPath);
        boolean gpsDumpOK = false;
        String numberIGC = "";
        String wNoWin = "/win=0";  
        String wExit = "/exit";                
        String wComPort = "/com="+portNumber;
        String sTypeGps = "";       
        String sAction = "";
        StringBuilder sbLog = new StringBuilder();
        switch (idGPS) {
            case 1:
                switch (myConfig.getOS()) {
                    case MACOS :
                    case WINDOWS :
                        sTypeGps = "/gps=flymaster";
                        break;
                    case LINUX : 
                        sTypeGps = "-gyn"; 
                        break;
                }
                break;
            case 2:
                 switch (myConfig.getOS()) {
                    case MACOS :
                        sTypeGps = "/gps=flymasterold";
                        break;
                    case WINDOWS :                     
                        sTypeGps = "/gps=flymasterold";
                        break;
                    case LINUX : 
                        sTypeGps = "-gy";    // A vérifier
                        break;
                }
                break;               
            case 3:
                switch (myConfig.getOS()) {
                    case WINDOWS :
                        sTypeGps = "/gps=iqcompeo";	// Compeo/Compeo+/Galileo/Competino/Flytec 5020,5030,6030
                        break;
                    case MACOS :
                        sTypeGps = "/gps=flytec";
                        break;
                    case LINUX :
                        sTypeGps = "-gc";
                        break;                        
                }
                break;
            case 4:
                switch (myConfig.getOS()) {
                    case MACOS :
                    case WINDOWS : 
                        sTypeGps = "/gps=ascent";
                        break;
                }
                break; 
            case 5:
                switch (myConfig.getOS()) {
                    case MACOS :
                    case WINDOWS : 
                        sTypeGps = "/gps=syride";
                        break;
                    case LINUX :
                        sTypeGps = "-gsy";
                        break;                        
                }
                break;
            case 6:
                switch (myConfig.getOS()) {
                    case MACOS :
                    case WINDOWS :                 
                        sTypeGps = "/gps=leonardo";
                        break;
                }
                break; 
            case 7:
                switch (myConfig.getOS()) {
                    case MACOS :
                    case WINDOWS : 
                        sTypeGps = "/gps=digiflyair";
                        break;
                }
                break;   
            case 8:
                switch (myConfig.getOS()) {
                    case WINDOWS :
                        sTypeGps = "/gps=iqbasic";	// IQ-Basic / Flytec 6015
                        break;
                    case MACOS :
                        sTypeGps = "/gps=iqbasic";       // with Mac, same as Compeo/Compeo+/Galileo/Competino/Flytec 5020,5030,6030
                        break;
                    case LINUX :
                        sTypeGps = "-giq";
                        break;
                }
                break;                
        }        
        switch (myConfig.getOS()) {
            case MACOS :
                sAction = "/flightlist";
                break;
            case WINDOWS :
                switch (gpsTypeName) {
                    case 0:    // long name  
                    case 1 :   // short name
                        sAction = "/wr_wpt="+wptFile.getAbsolutePath();
                        break;
                    case 2 :      // mixed name
                        sAction = "/wr_wpt2="+wptFile.getAbsolutePath();
                        break;
                }                    
                break;
            case LINUX :
                // -f“N” Select a specific flight (Brauniger/Flytec/Flymaster). If N=0 a flightlist is displayed.
                numberIGC = "-f0";
                break;
        }        
        try {
            String executionPath = System.getProperty("user.dir");
            switch (myConfig.getOS()) {
                case WINDOWS :
                    // to do windows path testing
                    pathGpsDump = executionPath+File.separator+"GpsDump.exe";    // Windows
                    File fwGpsDump = new File(pathGpsDump);
                    if(fwGpsDump.exists()) gpsDumpOK = true;         
                    break;                
                case MACOS :
                    pathGpsDump = executionPath+File.separator+"GpsDump";
                    File fmGpsDump = new File(pathGpsDump);
                    if(fmGpsDump.exists()) gpsDumpOK = true;  
                    break;
                case LINUX :
                    pathGpsDump = executionPath+File.separator+"gpsdump";
                    System.out.println(pathGpsDump);
                    File flGpsDump = new File(pathGpsDump);
                    if(flGpsDump.exists()) gpsDumpOK = true;                        
                    break;                    
            }    
            if (gpsDumpOK) {
                listPFM =new ArrayList<String>();
                // http://labs.excilys.com/2012/06/26/runtime-exec-pour-les-nuls-et-processbuilder/
                // the author has serious doubts : ok only if program run correctly or crashes
                switch (myConfig.getOS()) {
                    case WINDOWS :
                        arrayParam = new String[]{pathGpsDump,wNoWin, wComPort, sTypeGps, sAction, wExit};
                        break;
                    case MACOS : 
                        arrayParam =new String[]{pathGpsDump,sTypeGps, sAction};                        
                        break;
                    case LINUX :   
                        // result is displayed on the screen but a file path is required  
                     //   arrayParam =new String[]{pathGpsDump,sTypeGps, linuxPort, tempList, numberIGC};
                        break;                        
                }
                sbLog.append("Call : ").append(java.util.Arrays.toString(arrayParam)).append(CF);
                System.out.println("SetOzi : "+(java.util.Arrays.toString(arrayParam)));
                Process p = Runtime.getRuntime().exec(arrayParam);   
                p.waitFor();
                res = p.exitValue();  // 0 if all is OK  
                System.out.println("res = "+res);
                String ligne = ""; 
                if (res == 0) {
                    BufferedReader output = getOutput(p);                    
                    while ((ligne = output.readLine()) != null) {
                        sbLog.append(ligne).append(CF);
                    }
                } else {
                    BufferedReader error = getError(p);
                    while ((ligne = error.readLine()) != null) {
                        sbLog.append(ligne).append(CF);
                    }
                }
                strLog = sbLog.toString();               
            } else {
                sbLog.append("Error 1201 ").append(CF);
                res = 1201;
                errorGpsDump = 1201;                
            }    
        } catch (Exception ex) {
            sbLog.append("Error 1 ").append(CF);
            res = 1;
            errorGpsDump = 1;
        }  
        
        strLog = sbLog.toString();
        return res;            
        
    }         
     
    public void start(int idGPS, int idFlight)  {                        
        ProgressForm pForm = new ProgressForm();
           
        Task<Void> task = new Task<Void>() {
            @Override
            public Void call() throws InterruptedException { 
                int res = getFlight(idGPS,idFlight);
                return null ;                
            }
        
        };
        // binds progress of progress bars to progress of task:
        pForm.activateProgressBar(task);

        // task is finished 
        task.setOnSucceeded(event -> {
            pForm.getDialogStage().close();
            gpsdumpClose();
        });

        pForm.getDialogStage().show();

        Thread thread = new Thread(task);
        thread.start();        
    }    
    
    private void gpsdumpClose() {
        if (errorGpsDump ==0 ) 
            switch (codeRetour) {
                case 0:
                    // test case
                    System.out.println(strLog);
                    System.out.println("OK...");
                    break;
                case 1:
                   // carnetController.returnXXX
                    break;
                case 2:
                   // carnetController.returnXXX
                    break;
                case 3:
                   // extController.returnXXX
                    break;
                case 4:
                   // extController.returnXXX
                    break;    
                case 5:
                   // mapController.returnXXX
                    break;  
                case 6:
                    // GPSViewController ask for one track with progress bar
                    System.out.println(strLog);
                    String strIGC = null;
                    try {
                        textio fread = new textio();                                    
                        strIGC = fread.readTxt(igcFile);
                        gpsController.returnGpsDump(strIGC);
                    } catch (Exception e) {
                        
                    }
                    break;                        
            }
        else {
            alertbox aError = new alertbox(myConfig.getLocale());
            aError.alertNumError(errorGpsDump);
        }
    }    
    
}
