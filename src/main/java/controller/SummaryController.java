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
    
    
    
    // Reference to TraceViewController
    private TraceViewController traceController;    
    
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
    
    public void setForm(configProg mainConfig) {
        this.myConfig = mainConfig;
        i18n = I18nFactory.getI18n("","lang/Messages",TimeTableController.class.getClass().getClassLoader(),myConfig.getLocale(),0);
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
