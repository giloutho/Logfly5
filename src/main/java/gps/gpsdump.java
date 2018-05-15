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
import javafx.concurrent.Task;
import org.json.simple.parser.ParseException;
import settings.configProg;
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

    private int errorGpsDump;    
    private String pathGpsDump;    
    private int codeRetour;      
    private File igcFile;    
    private String CF =  "\r\n"; 
    private String strLog;
    

    public gpsdump(configProg currConfig)  {
        myConfig = currConfig;
    }
    
    public gpsdump(GPSViewController callGPSView, int pRetour, configProg currConfig)  {
        myConfig = currConfig;
        this.gpsController = callGPSView;
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
        
        // case MACOS :
            pathGpsDump = executionPath+File.separator+"GpsDump";
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
        String wComPort = "/com=5";
        String wExit = "/exit";        
        String sTypeGps = "";       
        StringBuilder sbLog = new StringBuilder();
        switch (idGPS) {
            case 1:
                sTypeGps = "/gps=flymaster";
                break;
            case 2:
                sTypeGps = "/gps=flymasterold";
                break;               
            case 3:
                sTypeGps = "/gps=flytec";
                break;
            case 4:
                sTypeGps = "/gps=ascent";
                break; 
            case 5:
                sTypeGps = "/gps=syride";
                break;
            case 6:
                sTypeGps = "/gps=leonardo";
                break; 
            case 7:
                sTypeGps = "/gps=digiflyair";
                break;   
        }
        igcFile = systemio.tempacess.getAppFile("Logfly", "temp.igc");
        if (igcFile.exists())  igcFile.delete();              
        String numberIGC = "/track="+String.valueOf(idFlight);

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
                case LINUX :
                    pathGpsDump = executionPath+File.separator+"GpsDump";
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
                }
                sbLog.append("Call : ").append(java.util.Arrays.toString(arrayParam));
                System.out.println(java.util.Arrays.toString(arrayParam));
                Process p = Runtime.getRuntime().exec(arrayParam);   
                p.waitFor();
                res = p.exitValue();  // 0 if all is OK  
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
                    // GPSViewController ask for one track
                    System.out.println(strLog);
                    textio fread = new textio();                                    
                    String strIGC = fread.readTxt(igcFile);
                    gpsController.returnGpsDump(strIGC);
                    break;                      
            }
        else {
            alertbox aError = new alertbox(myConfig.getLocale());
            aError.alertNumError(errorGpsDump);
        }
    }    
    
}
