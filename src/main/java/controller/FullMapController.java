/*
 * Copyright Gil THOMAS
 * This file forms an integral part of Logfly project
 * See the LICENSE file distributed with source code
 * for details of Logfly licence project
 */
package controller;

import dialogues.alertbox;
import java.sql.PreparedStatement;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ToolBar;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import leaflet.map_visu;
import model.Carnet;
import org.xnap.commons.i18n.I18n;
import org.xnap.commons.i18n.I18nFactory;
import settings.configProg;
import settings.listLeague;
import trackgps.scoring;
import trackgps.traceGPS;

/**
 *
 * @author gil
 */
public class FullMapController {
    
    
    @FXML
    private Button btInfo;
    
    @FXML
    private Button btMesure;
    
    @FXML
    private ChoiceBox chbLeague; 
    
    @FXML
    private Button btScoring;    
    
    @FXML
    private Button btHtml;    

    @FXML
    private Button btMail;    
    
    @FXML
    private Button btClose;
    
    @FXML
    private WebView viewMap; 
    
    private CarnetViewController carnetController;    
    
    private Stage mapStage;    
    
    configProg myConfig;
    
    private static I18n i18n;
    
    private traceGPS carnetTrace;    
    private String carnetHTML; 
    private boolean dispLegend = true;  

    @FXML
    private void initialize() {
        
      //  WebEngine engine = viewMap.getEngine();
       // engine.load("http://www.example.org");   
               
    }    
    
    public void setParams(configProg mainConfig, String pHTML) {
        this.myConfig = mainConfig;
        this.carnetHTML = pHTML;
        viewMap.getEngine().loadContent(carnetHTML,"text/html");
        i18n = I18nFactory.getI18n("","lang/Messages",FullMapController.class.getClass().getClassLoader(),myConfig.getLocale(),0);
        winTraduction();
        iniChbLeague();
    }
    
    @FXML
    private void toggleInfos(ActionEvent event) {
        if (dispLegend) {
            viewMap.getEngine().executeScript("hideLegend()"); 
            dispLegend = false;
        } else {
            viewMap.getEngine().executeScript("fillLegend()"); 
            dispLegend = true;            
        }
    }    
    
    @FXML
    private void showMeasure(ActionEvent event) {
       viewMap.getEngine().executeScript("startMeasure()");
    }    
    
    @FXML
    private void showLeague(ActionEvent event) {
        scoring currScore = new scoring(this,myConfig);  
        currScore.start(carnetTrace, myConfig.getIdxLeague());  
    }    
    
    public void scoreReturn() {
        // If scoring failed, error message was sent by Scoring class
        if (carnetTrace.isScored())  {
            // Mise à jour de la db
//            Carnet selectedVol = tableVols.getSelectionModel().getSelectedItem();
//            StringBuilder sReq = new StringBuilder();
//            sReq.append("UPDATE Vol SET V_League='").append(String.valueOf(currTrace.getScore_Idx_League())).append("'");
//            sReq.append(",V_Score='").append(currTrace.getScore_JSON()).append("'");
//            sReq.append(" WHERE V_ID = ?");            
//            try {
//                PreparedStatement pstmt = myConfig.getDbConn().prepareStatement(sReq.toString());
//                pstmt.setInt(1, Integer.valueOf(selectedVol.getIdVol()));
//                pstmt.executeUpdate(); 
//                pstmt.close();
//                switch (pRetour) {
//                    case 1:
//                        showFullMap();
//                        break;   
//                    case 2:
//                        showWinGE();
//                        break;  
//                }                
//            } catch (Exception e) {
//                alertbox aError = new alertbox(myConfig.getLocale());
//                aError.alertError(e.getMessage()); 
//            }      
            try {
                map_visu visuFullMap = new map_visu(carnetTrace, myConfig);
                if (visuFullMap.isMap_OK()) {
                    carnetHTML = visuFullMap.getMap_HTML();
                    viewMap.getEngine().loadContent(carnetHTML,"text/html");
                }
                
            } catch (Exception e) {
                alertbox aError = new alertbox(myConfig.getLocale());
                aError.alertError(e.getMessage());                 
            }
        }         
    }    
    
    @FXML
    private void recHTML(ActionEvent event) {
        // label.setText("Clic HTML");
    }    
    
    @FXML
    private void sendMail(ActionEvent event) {
        // label.setText("Clic mail...");
    }    
    
    @FXML
    private void closeMap(ActionEvent event) {
        // get a handle to the stage
        Stage stage = (Stage) btClose.getScene().getWindow();
        stage.close();  
    }      
    
    public void setWinMax()  {           
        mapStage.setMaximized(true);
    }
    
    /**
     * Sets the stage of this mapViewer.
     *
     * @param pMapStage
     */
    public void setMapStage(Stage pMapStage) {
        this.mapStage = pMapStage;
    }    
    
    /**
     * Set a communication bridge with CarnetViewController 
     * @param callExterne 
     */
    public void setCarnetBridge(CarnetViewController callCarnet)  {
        this.carnetController = callCarnet;   
        carnetTrace = carnetController.currTrace;     
    }   
    
    /**
     * Choicebox is fille with online contest supported by scoring module     
     */
    private void iniChbLeague()  { 
        
        listLeague suppLeagues = new listLeague();
        ObservableList <String> allLeagues = suppLeagues.fill();        
        
        chbLeague.getItems().clear();
        chbLeague.setItems(allLeagues);
        chbLeague.getSelectionModel().select(myConfig.getIdxLeague());                       
        // debugging
        if (carnetTrace.isScored())  {
            System.out.println("League : "+carnetTrace.getScore_League());
            String currLeague = carnetTrace.getScore_League();
            int size = allLeagues.size();
            for(int index=0; index<size; index++){
                if (currLeague.equals(allLeagues.get(index))){
                    System.out.println("Index dans la liste : "+index);
                    break;
                }
            }
        }
    }    
    
    /**
     * Translate labels of the window
     */
    private void winTraduction() {
        btInfo.setText(i18n.tr("Infos"));
        btMesure.setText(i18n.tr("Mesurer"));
        btScoring.setText(i18n.tr("Scoring"));
        btHtml.setText(i18n.tr("HTML"));
        btMail.setText(i18n.tr("Mail"));
        btClose.setText(i18n.tr("Fermer"));                
    }    
}
