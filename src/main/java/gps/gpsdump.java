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
import dialogues.ProgressForm;
import dialogues.alertbox;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.logging.Level;
import javafx.concurrent.Task;
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
    

    public gpsdump(configProg currConfig)  {
        myConfig = currConfig;
    }
    
    public gpsdump(GPSViewController callGPSView, int pRetour, String pNamePort, configProg currConfig)  {
        myConfig = currConfig;
        this.gpsController = callGPSView;
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
                        sTypeGps = "-gy"; 
                        break;
                }
                break;
            case 2:
                 switch (myConfig.getOS()) {
                    case MACOS :
                    case WINDOWS :                     
                        sTypeGps = "/gps=flymaster";
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
                        sTypeGps = "/gps=flytec";       // with Mac, same as Compeo/Compeo+/Galileo/Competino/Flytec 5020,5030,6030
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
        switch (myConfig.getOS()) {
            case MACOS :
            case WINDOWS :
                numberIGC = "/track="+String.valueOf(idFlight);
                break;
            case LINUX :
                numberIGC = "-f"+String.valueOf(idFlight);
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
