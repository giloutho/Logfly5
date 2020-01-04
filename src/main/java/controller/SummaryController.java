/*
 * Copyright Gil THOMAS
 * This file forms an integral part of Logfly project
 * See the LICENSE file distributed with source code
 * for details of Logfly licence project
 */
package controller;

import igc.pointIGC;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.xnap.commons.i18n.I18n;
import org.xnap.commons.i18n.I18nFactory;
import settings.configProg;
import trackgps.analyse;
import trackgps.traceGPS;

/**
 *
 * @author gil
 */
public class SummaryController {
    
    @FXML
    private PieChart pieChart1;
    @FXML
    private Label lbAltiMax;
    @FXML
    private Label vAltiMax;   
    @FXML
    private Label lbAltiMini;
    @FXML
    private Label vAltiMini;
    @FXML
    private Label lbExtraction;
    @FXML
    private Label vExtraction;
    @FXML
    private Label lbVitMoyenne;
    @FXML
    private Label vVitMoyenne;  
    @FXML
    private Label lbVarioMax;
    @FXML
    private Label vVarioMax;   
    @FXML
    private Label lbVarioMini;
    @FXML
    private Label vVarioMini;
    @FXML
    private Label lbMonteeMoyen;
    @FXML
    private Label vMonteeMoyen;
    @FXML
    private Label lbEfficacite;
    @FXML
    private Label vEfficacite;    
    @FXML
    private Label lbShape;
    @FXML
    private Label lbLeague;
    @FXML
    private Label vDistance;
    @FXML
    private Label vScore;
    
    
    
    // Reference to TraceViewController
    private TraceViewController traceController; 
    
    // Reference to FullMapController
    private FullMapController fullMapController;     
    
    private Stage dialogStage;     
    
    // Localization
    private I18n i18n; 

    // Settings
    private configProg myConfig;
    private StringBuilder sbError;    
    private double totPeriod;
    private double totThermals;
    private double totGlides;
    private double totDives;
    private int extractTime;
    
    @FXML
    private void initialize() {       
        
    }
    
    /**
     * Sets the stage of this dialog.
     *
     * @param dialogStage
     */
    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }   
    
    /**
     * Initialize communication brdige with TraceViewController 
     * @param pTraceViewController 
     */
    public void setTraceBridge(TraceViewController pTraceViewController) {
        this.traceController = pTraceViewController;        
    }   
    
    /**
     * Initialize communication brdige with FullMapController 
     * @param pFullMapController 
     */
    public void setFullMapBridge(FullMapController pFullMapController) {
        this.fullMapController = pFullMapController;        
    }      
    
    public void setForm(configProg mainConfig) {
        this.myConfig = mainConfig;
        i18n = I18nFactory.getI18n("","lang/Messages",SummaryController.class.getClass().getClassLoader(),myConfig.getLocale(),0);
        winTraduction();
    }    

    public void iniData(traceGPS currTrack) {  
        
        analyse trackAnalyze = new analyse(currTrack,i18n);
        double pThermal = trackAnalyze.getPercThermals();        
        StringBuilder sbTh = new StringBuilder();
        sbTh.append(i18n.tr("Thermal")).append(" ").append(String.format("%2.0f" , pThermal*100)).append("%"); 
        System.out.println(sbTh.toString());
        double pGlide = trackAnalyze.getPercGlides();
        StringBuilder sbGl = new StringBuilder();
        sbGl.append(i18n.tr("Glide")).append(" ").append(String.format("%2.0f" , pGlide*100)).append("%");  
        System.out.println(sbGl.toString());
        double pDive = trackAnalyze.getPercDives();
        StringBuilder sbDi = new StringBuilder();
        sbDi.append(i18n.tr("Dive")).append(" ").append(String.format("%2.0f" , pDive*100)).append("%");    
        double pVarious = 1 - (pThermal+pGlide+pDive);
        StringBuilder sbVa = new StringBuilder();
        sbVa.append(i18n.tr("Various")).append(" ").append(String.format("%2.0f" , pVarious*100)).append("%");    
        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList(
            new PieChart.Data(sbTh.toString(), pThermal),
            new PieChart.Data(sbGl.toString(), pGlide),
            new PieChart.Data(sbDi.toString(), pDive),
            new PieChart.Data(sbVa.toString(), pVarious));         
        pieChart1.setData(pieChartData);     
        pieChart1.setLegendVisible(false);
        pieChart1.setLabelsVisible(true);
        
        pointIGC ptAltMax = currTrack.getAlt_Maxi_GPS();
        vAltiMax.setText(String.valueOf(ptAltMax.AltiGPS)+" m");
        pointIGC ptAltMini = currTrack.getAlt_Mini_GPS();          
        vAltiMini.setText((ptAltMini.AltiGPS)+" m");
        vExtraction.setText(trackAnalyze.getExtractTime());
        vVitMoyenne.setText(String.format("%3.0f" ,trackAnalyze.getAvgTransSpeed())+" km/h");
        pointIGC ptVarioMax = currTrack.getVario_Max();
        vVarioMax.setText(String.format("%2.2f",ptVarioMax.Vario)+" m/s");
        pointIGC ptVarioMini = currTrack.getVario_Mini();
        vVarioMini.setText(String.format("%2.2f",ptVarioMini.Vario)+" m/s");
        vMonteeMoyen.setText(String.format("%2.2f" ,trackAnalyze.getAvgThermalClimb())+" m/s");
        vEfficacite.setText(String.format("%3.0f" ,trackAnalyze.getAvgThermalEffi())+"%");   
        lbShape.setText("");
        lbLeague.setText("");
        vDistance.setText("");
        vScore.setText("");
        if (currTrack.isScored()) genScoreLabels(currTrack);
            
    }   
    
    private void genScoreLabels(traceGPS currTrack) {
        
        boolean res = false;
        
        try {
            
            JSONParser jsonParser = new JSONParser();
            JSONObject jsonObject = (JSONObject) jsonParser.parse(currTrack.getScore_JSON());
  
            // is it a triangle or not ?
            JSONObject score = (JSONObject) jsonObject.get("drawScore");            
            String legLeague = score.get("scoreLeague").toString();           
            String legShape = score.get("scoreShape").toString();
            String legDistance = score.get("scoreDistance").toString();
            String legPoints = score.get("scorePoints").toString();        
            switch (legLeague) {
                case "FR" :
                    lbLeague.setText(i18n.tr("French contest"));
                    break;
                case "CH" :
                    lbLeague.setText(i18n.tr("Swiss contest"));
                    break;
                case "XC":
                    lbLeague.setText(i18n.tr("World XContest"));
                    break;
                default:
                    lbLeague.setText(legLeague);    
            }
            switch (legShape) {
                case "FAI Triangle" :
                    lbShape.setText(i18n.tr("FAI triangle"));
                    break;
                case "Free flight 2 wpt" :
                    lbShape.setText(i18n.tr("Flight 2 points")); 
                    break;
                case "Flat Triangle":
                    lbShape.setText(i18n.tr("Flat triangle"));
                    break;
                case "Free flight 1 wpt" :
                    lbShape.setText(i18n.tr("Flight 1 point"));
                    break;
                case "Free flight 3 wpt":
                    lbShape.setText(i18n.tr("Flight 3 points"));
                    break;
                default:
                    lbShape.setText(legShape);   
            }
            // Formatting distance is of the form 21.89160109032659
            int iLength = legPoints.length();
            String legFormate = null;
            if (iLength > legDistance.indexOf(".")+3) {
                legFormate = legDistance.substring(0,legDistance.indexOf(".")+3);
            } else {
                legFormate = legDistance;
            }
            vDistance.setText(legFormate+" km");
            // Formatting score is of the form 9.89160109032659
            iLength = legPoints.length();        
            if (iLength > legPoints.indexOf(".")+3) {
                legFormate = legPoints.substring(0,legPoints.indexOf(".")+3);
            } else {
                legFormate = legPoints;
            }
            vScore.setText(legFormate+" pts");            
        } catch (Exception e) {
            
        }        
    }
    
    private void winTraduction() {
        
        lbAltiMax.setText(i18n.tr("Max GPS alt"));
        lbAltiMini.setText(i18n.tr("Min GPS alt"));
        lbExtraction.setText(i18n.tr("Extraction time"));
        lbVitMoyenne.setText(i18n.tr("Avg transition speed"));
        lbVarioMax.setText(i18n.tr("Max climb"));
        lbVarioMini.setText(i18n.tr("Max sink"));
        lbMonteeMoyen.setText(i18n.tr("Avg thermal climb"));
        lbEfficacite.setText(i18n.tr("Avg th efficiency"));
}
    
}
