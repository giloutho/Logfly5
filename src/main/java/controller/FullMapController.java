/*
 * Copyright Gil THOMAS
 * This file forms an integral part of Logfly project
 * See the LICENSE file distributed with source code
 * for details of Logfly licence project
 */
package controller;

import dialogues.alertbox;
import dialogues.dialogbox;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.util.logging.Level;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.image.WritableImage;
import javafx.scene.SnapshotParameters;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import javax.imageio.ImageIO;
import leaflet.map_visu;
import littlewins.winMail;
import littlewins.winTrackFile;
import org.xnap.commons.i18n.I18n;
import org.xnap.commons.i18n.I18nFactory;
import settings.configProg;
import settings.listLeague;
import systemio.mylogging;
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
    private TraceViewController extController;    
    
    private Stage mapStage;    
    
    configProg myConfig;
    
    private static I18n i18n;
    
    private traceGPS mapTrace;    
    private String carnetHTML; 
    private boolean dispLegend = true;  
    private int idxDb;
    private int firstLeagueIdx;
    private int newLeagueIdx; 
    private ObservableList <String> allLeagues;
    private StringBuilder sbError;
    private File snapFile;

    @FXML
    private void initialize() {
        
      //  WebEngine engine = viewMap.getEngine();
       // engine.load("http://www.example.org");   
                   
    }    
    
    public void setParams(configProg mainConfig, String pHTML, int idxCarnet) {
        this.myConfig = mainConfig;
        this.carnetHTML = pHTML;
        this.idxDb = idxCarnet;
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
    
    // BtScoring clic
    @FXML
    private void showLeague(ActionEvent event) {
        askScoring();
    }   
    
    private void askScoring() {
        scoring currScore = new scoring(this,myConfig);  
        currScore.start(mapTrace,chbLeague.getSelectionModel().getSelectedIndex());
    }
    
    private void updateDb()  {
        StringBuilder sReq = new StringBuilder();
        sReq.append("UPDATE Vol SET V_League='").append(String.valueOf(mapTrace.getScore_Idx_League())).append("'");
        sReq.append(",V_Score='").append(mapTrace.getScore_JSON()).append("'");
        sReq.append(" WHERE V_ID = ?");            
        try {
            PreparedStatement pstmt = myConfig.getDbConn().prepareStatement(sReq.toString());
            pstmt.setInt(1, idxDb);
            pstmt.executeUpdate(); 
            pstmt.close();                        
        } catch (Exception e) {
            alertbox aError = new alertbox(myConfig.getLocale());
            aError.alertError(e.getMessage()); 
        } 
    }
    
    public void scoreReturn() {
        // If scoring failed, error message was sent by Scoring class
        if (mapTrace.isScored())  {     
            try {
                map_visu visuFullMap = new map_visu(mapTrace, myConfig);
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
        
        winTrackFile myTrace = new winTrackFile(carnetHTML);        
        
    }    
    
    @FXML
    private void sendMail(ActionEvent event) {
        if (snapshot()) {            
            winMail showMail = new winMail(myConfig,snapFile.getAbsolutePath(), false);            
        } else {
            winMail showMail = new winMail(myConfig,null, false); 
        }            
    }    
    
    @FXML
    private void closeMap(ActionEvent event) {
        if (idxDb != -1) {
            if (firstLeagueIdx != newLeagueIdx && firstLeagueIdx == -1) {
                updateDb();
            } else if (firstLeagueIdx != newLeagueIdx)  {
                String stFirstLeague = allLeagues.get(firstLeagueIdx);
                String stNewLeague = allLeagues.get(newLeagueIdx);
                dialogbox dConfirm = new dialogbox();
                StringBuilder sbMsg = new StringBuilder(); 
                sbMsg.append(i18n.tr("Mettre à jour le score : ")).append(stFirstLeague).append(" -> ").append(stNewLeague);
                if (dConfirm.YesNo(i18n.tr("Scoring"), sbMsg.toString())) { 
                    updateDb();
                }
            }
        }
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
        mapTrace = carnetController.currTrace;     
    }   
    
    /**
     * Set a communication bridge with TraceViewController 
     * @param callExterne 
     */
    public void setTraceBridge(TraceViewController callExterne)  {
        this.extController = callExterne;  
        mapTrace = extController.extTrace;      
    }    
    
    /**
     * Choicebox is fille with online contest supported by scoring module     
     */
    private void iniChbLeague()  { 
        
        listLeague suppLeagues = new listLeague();
        allLeagues = suppLeagues.fill();        
        
        chbLeague.getItems().clear();
        chbLeague.setItems(allLeagues);  
        chbLeague.getSelectionModel().selectedItemProperty()
            .addListener((observable, oldValue, newValue) -> {
                System.out.println((String)newValue+" "+chbLeague.getSelectionModel().getSelectedIndex());
                newLeagueIdx = chbLeague.getSelectionModel().getSelectedIndex();
        });        
        // debugging
        if (mapTrace.isScored())  {
            String currLeague = mapTrace.getScore_League();
            int size = allLeagues.size();
            for(int index=0; index<size; index++){
                if (currLeague.equals(allLeagues.get(index))){
                    firstLeagueIdx = index;
                    chbLeague.getSelectionModel().select(index);    
                    break;
                }
            }
        } else {
            chbLeague.getSelectionModel().select(myConfig.getIdxLeague()); 
            firstLeagueIdx = -1;
        }
    }    
    
    private boolean snapshot() {
        
        boolean res = false;
        
        // code from http://java-buddy.blogspot.fr/2012/12/take-snapshot-of-node-with-javafx.html
        WritableImage snapImage = viewMap.snapshot(new SnapshotParameters(), null);

        // code from http://java-buddy.blogspot.fr/2012/12/save-writableimage-to-file.html
        try {
            String fileName = mapTrace.suggestName()+".png";
            snapFile = systemio.tempacess.getAppFile("Logfly", fileName);
            RenderedImage renderedImage = SwingFXUtils.fromFXImage(snapImage, null);
            ImageIO.write(renderedImage, "png",snapFile);
            res = true;
        } catch (IOException ex) {
            sbError = new StringBuilder(this.getClass().getName()+"."+Thread.currentThread().getStackTrace()[1].getMethodName());
            sbError.append("\r\n").append(ex.toString());
            mylogging.log(Level.SEVERE, sbError.toString());
        }
        
        return res;
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
