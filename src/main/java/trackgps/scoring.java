/*
 * Copyright Gil THOMAS
 * Ce fichier fait partie intégrante du projet Logfly
 * Pour tous les détails sur la licence du projet Logfly
 * Consulter le fichier LICENSE distribué avec le code source
 */
package trackgps;

import dialogues.ProgressForm;
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
import controller.TraceViewController;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import systemio.textio;

/**
 *
 * @author gil
 */
public class scoring {
    
    private static int errorScore;
    private static int errorPoints;
    private String jsonScore;
    private int codeRetour;
    
    private CarnetViewController carnetController;
    private TraceViewController extController;
    
    public scoring (int myParam)  {
        // myParm ne sert à rien juste à faire un nouveau constructeur
    }
    
    public scoring (CarnetViewController callCarnet, int pRetour)  {
        this.carnetController = callCarnet;
        // Code 0 uniquement pour test ...
        // Code 1 au retour, on affiche la carte plein écran (fullMap) dans le CarnetViewController 
        // Code 2 au retour, on génère le fichier kml dans le CarnetViewController 
        codeRetour = pRetour;
    }
    
    public scoring (TraceViewController callExterne, int pRetour, int myParam)  {
        this.extController = callExterne;
        // myParm ne sert à rien juste à faire un nouveau constructeur
        
        // Code 3 au retour, on affiche la carte plein écran (fullMap) dans le TraceViewController
        // Code 4 au retour, on génère le fichier kml dans le TraceViewController        
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
    
    public int runScoring(traceGPS evalTrace, String scoreType) {
        int res = -1; 
        String pathModPoints;

        try{           
            fileIGC tempf= new fileIGC();            
            int creaFile = tempf.creaIgcForCalcul(evalTrace, "tpoints.igc");
            if (creaFile == 0)  {
                File optFile = systemio.tempacess.getAppFile("Logfly", "tpoints.opt");
                if (optFile.exists())  optFile.delete();

                String executionPath = System.getProperty("user.dir");
                String os = System.getProperty("os.name");
                if (os.indexOf("Windows") != -1) {
                    pathModPoints = executionPath+"\\logfly_lib\\points.exe";            
                } else  {
                    // ce que l'on faisait au départ
                    //pathModPoints = executionPath+"/logfly_lib/points";
                    pathModPoints = executionPath+"/points";
                }
                // http://labs.excilys.com/2012/06/26/runtime-exec-pour-les-nuls-et-processbuilder/
                // l'auteur émet des réserves : ce schéma ne fonctionne bien que si le programme s'éxecute ou se plante                                     
                Process p = Runtime.getRuntime().exec(new String[]{pathModPoints,tempf.getFileAbsPath(), optFile.getAbsolutePath(), scoreType});
                //Process p = Runtime.getRuntime().exec(pathModPoints);           
                BufferedReader error = getError(p);
                String ligne = "";     
                StringBuilder sbError = new StringBuilder();
                while ((ligne = error.readLine()) != null) {
                    sbError.append(ligne);
                }
                p.waitFor();
                res = p.exitValue();  // Code 0 si tout est OK... 
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
                
        ProgressForm pForm = new ProgressForm();
           
        Task<Void> task = new Task<Void>() {
            @Override
            public Void call() throws InterruptedException { 
                int res = runScoring(evalTrace, scoreType);
                return null ;
                
            }
        
        };
        // binds progress of progress bars to progress of task:
        pForm.activateProgressBar(task);

        // in real life this method would get the result of the task
        // and update the UI based on its value:
        task.setOnSucceeded(event -> {
            pForm.getDialogStage().close();
            scoreClosing();
        });

        pForm.getDialogStage().show();

        Thread thread = new Thread(task);
        thread.start();        
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
            }
        else {
            alertbox aError = new alertbox();
            aError.alertNumError(errorScore);
        }
    }
        
    /**
     * Méthode utilisée en interne après analyse par le module points
     * mais aussi utilisée en externe pour décoder le json de scoring stockée dans la db
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
                        // Chaque élément est un ensemble de trois éléments : latitude, longitude, et index du point dans la trace
                        // seul l'index nous intéresse
                        // TabPoints[i] =  [44.63208333333333,5.189766666666667,839]
                        JSONArray coord = (JSONArray) tabPoints.get(i);                
                        // Le cast (int) coord.get(2) envoyait une exception 
                        evalTrace.Score_Tb_Balises.add(Integer.parseInt(coord.get(2).toString()));
                        if (i == 0) IdxBD = Integer.parseInt(coord.get(2).toString());
                        if (i == tabPoints.size()-1) IdxBA = Integer.parseInt(coord.get(2).toString());                    
                    }
                }
                // pas d'exception ... Decodage OK, on peut calculer la moyenne
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
