/* 
 * Copyright Gil THOMAS
 * This file forms an integral part of Logfly project
 * See the LICENSE file distributed with source code
 * for details of Logfly licence project
 */
package trackgps;

import dialogues.alertbox;
import igc.fileIGC;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import javafx.concurrent.Task;
import controller.CarnetViewController;
import controller.FullMapController;
import controller.TraceViewController;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;
import org.controlsfx.dialog.ProgressDialog;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.xnap.commons.i18n.I18n;
import org.xnap.commons.i18n.I18nFactory;
import settings.configProg;
import systemio.textio;

/**
 *
 * @author gil
 * Scoring a track with an external program
 */
public class scoring {
    
    private static int errorScore;
    private static int errorPoints;
    private String jsonScore;
    private int codeRetour;
    
    private CarnetViewController carnetController;
    private TraceViewController extController;
    private FullMapController mapController;
    
    // Settings
    configProg myConfig;
    // Localization
    private I18n i18n;
    
    public scoring (int myParam, configProg currConfig)  {
        // myParam unused... this is just a new constructor
        myConfig = currConfig;
        i18n = myConfig.getI18n();
    }
    
    public scoring (CarnetViewController callCarnet, int pRetour, configProg currConfig)  {
        myConfig = currConfig;
        i18n = myConfig.getI18n();
        this.carnetController = callCarnet;
        // 0 only for test ...
        // 1 when process finished, display a fullmao in CarnetViewController 
        // 2 when process finished, kml file genration instruction will be called in CarnetViewController 
        codeRetour = pRetour;
    }
    
    public scoring (FullMapController callFullMap, configProg currConfig)  {
        myConfig = currConfig;
        i18n = myConfig.getI18n();
        this.mapController = callFullMap;
        codeRetour = 5;
    }    
    
    public scoring (TraceViewController callExterne, int pRetour, int myParam, configProg currConfig)  {
        myConfig = currConfig;
        i18n = myConfig.getI18n();
        this.extController = callExterne;
        // myParam unused... this is just a new constructor
        
        // 3 when process finished, display a fullmao in TraceViewController
        // 4 when process finished, kml file genration instruction will be called in TraceViewController        
        codeRetour = pRetour;
    }
 

    public static int getErrorScore() {
        return errorScore;
    }

    public String getJsonScore() {
        return jsonScore;
    }
            
    private static BufferedReader getOutput(Process p) {
        return new BufferedReader(new InputStreamReader(p.getInputStream()));
    }

    private static BufferedReader getError(Process p) {
        return new BufferedReader(new InputStreamReader(p.getErrorStream()));
    }
    
    private int runScoring(traceGPS evalTrace, String scoreType) {
        int res = -1; 
        String pathModPoints = "";
        boolean pointsOK = false;

        try{           
            fileIGC tempf= new fileIGC(myConfig.getLocale());            
            int creaFile = tempf.creaIgcForCalcul(evalTrace, "tpoints.igc");
            if (creaFile == 0)  {
                File optFile = systemio.tempacess.getAppFile("Logfly", "tpoints.opt");
                if (optFile.exists())  optFile.delete();                               

                String executionPath = System.getProperty("user.dir");
                /* First code
                String os = System.getProperty("os.name");
                if (os.indexOf("Windows") != -1) {
                    pathModPoints = executionPath+"\\logfly_lib\\points.exe";            
                } else  {
                    pathModPoints = executionPath+"/points";
                }
                */
                switch (myConfig.getOS()) {
                    case WINDOWS :
                        // to do windows path testing
                        pathModPoints = executionPath+File.separator+"points.exe";    // Windows
                        File fwPoints = new File(pathModPoints);
                        if(fwPoints.exists()) pointsOK = true;          
                        break;
                    case MACOS :
                        pathModPoints = executionPath+File.separator+"points";
                        File fPoints = new File(pathModPoints);
                        if(fPoints.exists()) pointsOK = true;                        
                        break;
                    case LINUX :
                        pathModPoints = executionPath+File.separator+"points";
                        File flPoints = new File(pathModPoints);
                        if(flPoints.exists()) pointsOK = true;                        
                        break;
                }
                if (pointsOK)  {
                    // http://labs.excilys.com/2012/06/26/runtime-exec-pour-les-nuls-et-processbuilder/
                    // the author has serious doubts : ok only if program run correctly or crashes
                    Process p;
                    if (scoreType.equals("FR")) {
                        // Special case of french contest
                        p = Runtime.getRuntime().exec(new String[]{pathModPoints,tempf.getFileAbsPath(), optFile.getAbsolutePath(), "USER", "FR", "flat", "pct", "0.05", "penalize", "1.2", "fai", "pct", "0.05", "penalize", "1.4", "freeFlight", "3", "1"});
                    } else {
                        p = Runtime.getRuntime().exec(new String[]{pathModPoints,tempf.getFileAbsPath(), optFile.getAbsolutePath(), scoreType});
                    }
                    //Process p = Runtime.getRuntime().exec(pathModPoints);           
                    BufferedReader error = getError(p);
                    String ligne = "";     
                    StringBuilder sbError = new StringBuilder();
                    while ((ligne = error.readLine()) != null) {
                        sbError.append(ligne);
                    }
                    p.waitFor();
                    res = p.exitValue();  // 0 if all is OK
                    if (res == 0) {
                        FileReader reader = new FileReader(optFile.getAbsolutePath());            

                        JSONParser jsonParser = new JSONParser();
                        JSONObject jsonObject = (JSONObject) jsonParser.parse(reader);
                        evalTrace.setScore_JSON(jsonObject.toString());
                        res = decodeStrJson(evalTrace);
                        if (res == 0)  {
                            evalTrace.setScored(true);
                        } else {
                            evalTrace.setScored(false);
                            errorScore = res;  
                        }

                    }
                } else {
                    res = 1001;
                    errorScore = 1001;
                }
            }
        } catch (FileNotFoundException ex) {
            res = 1;
            errorScore = 1;
        } catch (IOException ex) {
            res = 2;
            errorScore = 2;       
        } catch (ParseException ex) {
            res = 4;
            errorScore = 4;    
        } catch (InterruptedException ex) {
            res = 6;
            errorScore = 6;   
        } catch (NullPointerException ex) {
            res = 5;
            errorScore = 5;
        } 
        
        return res;
    }
        
    public void start(traceGPS evalTrace, int idxScoreType)  {
        
        String scoreType = stringListLeague(idxScoreType);
                
        Task<Object> worker = new Task<Object>() {
            @Override
            protected Object call() throws Exception {
                int res = runScoring(evalTrace, scoreType);
                return null ;
                
            }
        
        };
        worker.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
            @Override
            public void handle(WorkerStateEvent t) {
                scoreClosing();
            }
        });

        ProgressDialog dlg = new ProgressDialog(worker);
        dlg.setHeaderText(i18n.tr("Score evaluation"));
        dlg.setTitle("");
        Thread th = new Thread(worker);
        th.setDaemon(true);
        th.start();   
    }
    
    private void scoreClosing() {
        if (errorScore ==0 ) 
            switch (codeRetour) {
                case 1:
                    carnetController.scoreReturn(codeRetour);
                    break;
                case 2:
                    carnetController.scoreReturn(codeRetour);
                    break;
                case 3:
                    extController.scoreReturn(codeRetour);
                    break;
                case 4:
                    extController.scoreReturn(codeRetour);
                    break;    
                case 5:
                    mapController.scoreReturn();
                    break;                       
            }
        else {
            alertbox aError = new alertbox(myConfig.getLocale());
            aError.alertNumError(errorScore);
        }
    }
        
    /**
     * Decode scoring json result 
     * used directly in this class
     * after scoring, json is stored in db. This method will be used to decode 
     * @return 
     */
    public int decodeStrJson(traceGPS evalTrace)  {
        
        double dMoy = 0;
        double distance = 0;
        int IdxBD = -1;
        int IdxBA = -1;
        long DureeBDBA;
        int res = -1;
        
        if (evalTrace.getScore_JSON().contains("drawScore")) {
            try {                        

                JSONParser jsonParser = new JSONParser();
                JSONObject jsonObject = (JSONObject) jsonParser.parse(evalTrace.getScore_JSON());

                // handle a structure into the json object
                JSONObject score = (JSONObject) jsonObject.get("drawScore");
                evalTrace.setScore_League(score.get("scoreLeague").toString());
                evalTrace.setScore_Idx_League(numListLeague(score.get("scoreLeague").toString()));
                evalTrace.setScore_Shape(score.get("scoreShape").toString());
                if (score.get("scoreShape").toString().contains("Triangle")) evalTrace.setScore_Triangle(true);
                // Beware, it can throw a ClassCastException if your object isn't a double and 
                // a NullPointerException if your object is null.
                if (score.get("scoreDistance") != null) {
                    distance = (double) score.get("scoreDistance");
                    evalTrace.setScore_Route_Km(distance);
                }
                if (score.get("scorePoints") != null) evalTrace.setScore_Route_Pts((double) score.get("scorePoints"));                                   
                JSONArray tabPoints = (JSONArray) jsonObject.get("drawPoints");
                if (tabPoints != null)  {
                    for(int i=0; i<tabPoints.size(); i++){
                        // each element  : latitude, longitude, and track point index                        
                        // TabPoints[i] =  [44.63208333333333,5.189766666666667,839]
                        // we need only index
                        JSONArray coord = (JSONArray) tabPoints.get(i);                
                        // cast (int) coord.get(2) triggered an exception
                        evalTrace.Score_Tb_Balises.add(Integer.parseInt(coord.get(2).toString()));
                        if (i == 0) IdxBD = Integer.parseInt(coord.get(2).toString());
                        if (i == tabPoints.size()-1) IdxBA = Integer.parseInt(coord.get(2).toString());                    
                    }
                }
                // all is OK, average speed is computed
                if (IdxBD > -1 && IdxBD <  evalTrace.Tb_Calcul.size()) {
                    if (IdxBA > -1 && IdxBA < evalTrace.Tb_Calcul.size()) {
                        DureeBDBA = java.time.Duration.between(evalTrace.Tb_Calcul.get(IdxBD).dHeure, evalTrace.Tb_Calcul.get(IdxBA).dHeure).toMillis();            
                        // Conversion en seconde
                        DureeBDBA = DureeBDBA / 1000;    
                        if (DureeBDBA > 0) {
                            dMoy = (distance / DureeBDBA)*3600;
                            evalTrace.setScore_Moyenne(dMoy);
                        }
                    }
                    res = 0;                                                
                }        
            } catch (ParseException | NullPointerException ex) {
                res = 4;
            }    
        } else {
            res = 1002;
        }
        
        return res;
    }
    
    private int numListLeague(String sLeague) {
        int res;
        switch (sLeague) {
            case "FR":
                res = 0;
                break;                    
            case "CH":
                res = 1;
                break;  
            case "XC":
                res = 2;
                break;  
            case "AU":
                res = 3;
                break;  
            case "CZ":
                res = 4;
                break;  
            case "CZX":
                res = 5;
                break;  
            case "ES":
                res = 6;
                break;  
            case "FI":
                res = 7;
                break;  
            case "HU":
                res = 8;
                break;  
            case "MX":
                res = 9;
                break;  
            case "NE":
                res = 10;
                break;  
            case "NEX":
                res = 11;
                break;  
            case "SK":
                res = 12;
                break;  
            case "SKX": 
                res = 13;
                break;  
            default:
                res = 0;
        }                
        return res;
    }
    
    private String stringListLeague(int idxLeague) {
        String res;
        switch (idxLeague) {
            case 0:
                // Il semlble que la CFD française adopte les règles XC
                res = "FR";
                break;                    
            case 1:
                res = "CH";                
                break;  
            case 2:
                res = "XC";
                break;  
            case 3:
                res = "AU";                
                break;  
            case 4:
                res = "CZ";                
                break;  
            case 5:
                res = "CZX";                
                break;  
            case 6:
                res = "ES";                
                break;  
            case 7:
                res = "FI";                
                break;  
            case 8:
                res = "HU";                
                break;  
            case 9:
                res = "MX";                
                break;  
            case 10:
                res = "NE";                
                break;  
            case 11:
                res = "NEX";                
                break;  
            case 12:
                res = "SK";                
                break;  
            case 13:
                res = "SKX";                 
                break;  
            default:
                res = "FR";
        }                
        return res;
    }
    
}
