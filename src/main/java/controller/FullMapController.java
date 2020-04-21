/*
 * Copyright Gil THOMAS
 * This file forms an integral part of Logfly project
 * See the LICENSE file distributed with source code
 * for details of Logfly licence project
 */
package controller;

import Logfly.Main;
import dialogues.alertbox;
import dialogues.dialogbox;
import geoutils.geonominatim;
import igc.pointIGC;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.format.DateTimeFormatter;
import java.util.logging.Level;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.image.WritableImage;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.Tooltip;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javax.imageio.ImageIO;
import leaflet.map_visu;
import littlewins.winComment;
import littlewins.winFileChoose;
import littlewins.winMail;
import littlewins.winSaveXcp;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.xnap.commons.i18n.I18n;
import settings.configProg;
import settings.fileType;
import settings.listLeague;
import systemio.mylogging;
import trackgps.checkAirspace;
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
    private Button btSummary;    
    
    @FXML
    private Button btChrono;    
    
    @FXML
    private ChoiceBox chbLeague; 
    
    @FXML
    private Button btScoring;    
    
    @FXML
    private Button btXcplanner;
    
    @FXML
    private Button btCheck;    
    
    @FXML
    private Button btCut;    

    @FXML
    private Button btMail;    
    
    @FXML
    private Button btClose;
    
    @FXML
    private WebView viewMap; 
    
    private CarnetViewController carnetController;  
    private TraceViewController extController; 
    private DashViewController dashController;
    
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
    private WebEngine webEngine;
    // Flag for track changes
    private boolean changedSuccess = false;
    
    @FXML
    private void initialize() {
        
        webEngine = viewMap.getEngine();
        webEngine.setJavaScriptEnabled(true);
        webEngine.titleProperty().addListener( (observable, oldValue, newValue) -> {
            if(newValue != null && !newValue.isEmpty() && !newValue.equals("Leaflet"))
                cutAction(newValue);
        });       
                   
    }    
    
    public void setParams(configProg mainConfig, String pHTML, int idxCarnet) {
        this.myConfig = mainConfig;
        this.carnetHTML = pHTML;
        this.idxDb = idxCarnet;
        webEngine.loadContent(carnetHTML,"text/html");
        i18n = myConfig.getI18n();
        winTraduction();
        iniChbLeague();
    }
    
    @FXML
    private void toggleInfos(ActionEvent event) {
        if (dispLegend) {
            webEngine.executeScript("hideLegend()"); 
            dispLegend = false;
        } else {
            webEngine.executeScript("fillLegend()"); 
            dispLegend = true;            
        }
    }    
    
    @FXML
    private void showMeasure(ActionEvent event) {
        webEngine.executeScript("startMeasure()");
    }    
    
    @FXML
    private void showChrono(ActionEvent event) {
        webEngine.executeScript("openNav()()");
    }    
    
    @FXML 
    private void showSummary(ActionEvent event) {
        try {                     
            // Load the fxml file and create a new stage for the popup dialog.
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(Main.class.getResource("/summary.fxml")); 
            AnchorPane page = (AnchorPane) loader.load();

            Stage dialogStage = new Stage();
            dialogStage.initModality(Modality.WINDOW_MODAL);       
            dialogStage.initOwner(btSummary.getScene().getWindow());
            Scene scene = new Scene(page);
            dialogStage.setScene(scene);

            // Communication bridge between SummaryController and TraceViewController
            SummaryController controller = loader.getController();
            controller.setFullMapBridge(this);
            controller.setForm(myConfig);
            controller.iniData(mapTrace);
            controller.setDialogStage(dialogStage); 
            dialogStage.showAndWait();
                       
        } catch (IOException e) {
            sbError = new StringBuilder(this.getClass().getName()+"."+Thread.currentThread().getStackTrace()[1].getMethodName());
            sbError.append("\r\n").append(e.toString());
            mylogging.log(Level.SEVERE, sbError.toString()); 
            
        }        
    }
    
    // BtScoring clic
    @FXML
    private void showLeague(ActionEvent event) {
        askScoring();
    }   
    
    // XC planner clic
    @FXML
    private void handleXcp(ActionEvent event) {
        xcpTranslation();
    }
    
    @FXML
    private void handleCheck(ActionEvent event) {
        airChecking();
    }
    
    private void askScoring() {
        scoring currScore = new scoring(this,myConfig);  
        currScore.start(mapTrace,chbLeague.getSelectionModel().getSelectedIndex());
    }
    
    private void updateDbLeague()  {
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
        
    private void cutAction(String sPoints) {
        DateTimeFormatter dtfHHmmss = DateTimeFormatter.ofPattern("HH:mm:ss");
        String[] tbPoints = sPoints.split(";");
        if (tbPoints.length > 2) {
            try {
                int iStart = Integer.parseInt(tbPoints[1]);
                int iEnd = Integer.parseInt(tbPoints[2]);
                pointIGC pStart = mapTrace.Tb_Good_Points.get(iStart);
                pointIGC pEnd = mapTrace.Tb_Good_Points.get(iEnd);
                StringBuilder sbMsg = new StringBuilder();
                sbMsg.append(i18n.tr("The track will start at")).append("\n");
                sbMsg.append(i18n.tr("   Point")).append(" ").append(tbPoints[1]).append("  ").append(pStart.dHeure.format(dtfHHmmss));
                sbMsg.append("  ").append(String.format("%3.2f",pStart.Vitesse)).append("km/h").append("\n");
                sbMsg.append(i18n.tr("and will end at")).append("\n");
                sbMsg.append(i18n.tr("   Point")).append(" ").append(tbPoints[2]).append("  ").append(pEnd.dHeure.format(dtfHHmmss));
                sbMsg.append("  ").append(String.format("%3.2f",pEnd.Vitesse)).append("km/h").append("\n");
                dialogbox dlb = new dialogbox(i18n);
                if (dlb.YesNo("", sbMsg.toString())) {
                    boolean isDb = false;
                    if (idxDb > 0) {
                        // idxDb > O the track came from logbook
                        // a backup will be created on work folder
                        isDb = true;
                    }
                    mapTrace.cutPoints(iStart, iEnd, isDb);
                    mapTrace.newDecodage();
                    if (mapTrace.isDecodage()) {
                        map_visu newFullMap = new map_visu(mapTrace, myConfig);
                        if (newFullMap.isMap_OK()) {   
                            carnetHTML = newFullMap.getMap_HTML();
                            webEngine.loadContent(carnetHTML,"text/html");
                        }
                        changedSuccess = true;
                    }
                }                
            } catch (Exception e) {
                sbError = new StringBuilder(this.getClass().getName()+"."+Thread.currentThread().getStackTrace()[1].getMethodName());
                sbError.append("\r\n").append("sPoints = ").append(sPoints);
                sbError.append("\r\n").append(e.toString());
                mylogging.log(Level.SEVERE, sbError.toString());                                                
            }
        }
    }  
    
    public void scoreReturn() {
        // If scoring failed, error message was sent by Scoring class
        if (mapTrace.isScored())  {     
            try {
                map_visu visuFullMap = new map_visu(mapTrace, myConfig);
                if (visuFullMap.isMap_OK()) {
                    btXcplanner.setVisible(true);
                    carnetHTML = visuFullMap.getMap_HTML();
                    webEngine.loadContent(carnetHTML,"text/html");
                }
                
            } catch (Exception e) {
                alertbox aError = new alertbox(myConfig.getLocale());
                aError.alertError(e.getMessage());                 
            }
        }         
    }    
    
    @FXML
    private void cutRequest(ActionEvent event) {
        
        StringBuilder sbMsg = new StringBuilder();
//        Pour éliminer les points inutiles, 
//        sélectionner par un clic sur la courbe d'altitude, 
//        le point de départ et le point de fin de la zone à conserver.
//        Une copie de l'original est généré dans le dossier de travail.        
        sbMsg.append(i18n.tr("To remove the useless points, select by a click on the altitude line "));
        sbMsg.append(i18n.tr("the starting point and the end point of the zone to be kept")).append("\n");
        sbMsg.append(i18n.tr("A copy of the original track will be generated in the working folder")).append("\n");
        alertbox aInfo = new alertbox(myConfig.getLocale());
        aInfo.alertInfo(sbMsg.toString());      
        webEngine.executeScript("enableClick()");
        
    }    
    
    private void xcpTranslation() {
        String sflightType="";
        StringBuilder sbUrl = new StringBuilder();
        StringBuilder sbStart = new StringBuilder();
        StringBuilder totUrl = new StringBuilder();
        DecimalFormatSymbols decimalFormatSymbols = new DecimalFormatSymbols();    
        decimalFormatSymbols.setDecimalSeparator('.'); 
        DecimalFormat df2 = new DecimalFormat("#0.00000", decimalFormatSymbols);
        DecimalFormat df3 = new DecimalFormat("##0.00000", decimalFormatSymbols);
        String sLat;
        String googLat;
        double dLat;
        String sLong;
        String googLong;
        double dLong;
        boolean isTriangle = false;
        String sLocation=null;
        
        try {
            JSONParser jsonParser = new JSONParser();
            JSONObject jsonObject = (JSONObject) jsonParser.parse(mapTrace.getScore_JSON());
            JSONObject score = (JSONObject) jsonObject.get("drawScore");
            System.out.println("Score league " + score.get("scoreLeague")); 
            String sLeague =  score.get("scoreLeague").toString();
            String sShape = score.get("scoreShape").toString().trim();
            String scoreBrut = score.get("scorePoints").toString().trim();
            String distBrute = score.get("scoreDistance").toString().trim();
            double dDist = Double.valueOf(distBrute)*1000;   
            String distMetre = df3.format(dDist) ;
            switch (sShape) {
                case "Free flight" :
                    switch (sLeague) {
                        case "FR":
                            sflightType = "cfd2";
                            break;
                        case "XC" :
                            sflightType = "xc2";
                            break;                            
                    }
                    break;
                case "Free flight 1 wpt" :
                    switch (sLeague) {
                        case "FR":
                            sflightType = "cfd3";
                            break;
                        case "XC" :
                            sflightType = "xc3";
                            break;                            
                    }                    
                    break;
                case "Free flight 2 wpt" :
                    switch (sLeague) {
                        case "FR":
                            sflightType = "cfd4";
                            break;
                        case "XC" :
                            sflightType = "xc4";
                            break;                            
                    }                    
                    break;
                case "Free flight 3 wpt" :
                    switch (sLeague) {
                        case "FR":
                            sflightType = "cfd5";   // à priori ne sortira pas avec points
                            break;
                        case "XC" :
                            sflightType = "xc5";
                            break;                            
                    }                    
                    break;
                case "outReturn" :
                    switch (sLeague) {
                        case "FR":
                            sflightType = "cfd2c";
                            break;
                        case "XC" :
                            sflightType = "xc2c";  // à priori ne sortira pas avec points
                            break;                            
                    }                    
                    break;
                case "FAI Triangle" :
                    isTriangle = true;
                    switch (sLeague) {
                        case "FR":
                            sflightType = "cfd3c";
                            break;
                        case "XC" :
                            sflightType = "xc3c";
                            break;                            
                    }                          
                    break;
                case "Flat Triangle" :
                    isTriangle = true;
                    switch (sLeague) {
                        case "FR":
                            sflightType = "cfd3c";
                            break;
                        case "XC" :
                            sflightType = "xc3c";
                            break;                            
                    }                          
                    break;                    
            }
            sbUrl.append("&flightType=").append(sflightType);
            JSONArray tabPoints = (JSONArray) jsonObject.get("drawPoints");
            if (tabPoints != null)  {
                sbUrl.append("&turnpoints=%5B%5B");                
                for(int i=0; i<tabPoints.size(); i++){ 
                    String[] arCoord = tabPoints.get(i).toString().split(",");
                    if (i > 1) sbUrl.append(",%5B");
                    if (arCoord.length > 1) {
                        sLat = arCoord[0].replace("[", "");
                        // To avoid errors with Google maps API,
                        // we must limit number of decimals (max 6)
                        // With points we have currently 9 decimals
                        dLat = Double.parseDouble(sLat);   
                        googLat = df2.format(dLat);
                        sLong = arCoord[1].replace("[", "");
                        dLong = Double.parseDouble(sLong);   
                        googLong = df3.format(dLong);
                        if ( i == tabPoints.size() - 1) {
                            sbUrl.append(googLat).append(",").append(googLong).append("%5D");  
                        } else {
                            if (i==0 ) {
                                sLocation = askLocationName(dLat,dLong);
                                if (isTriangle)
                                    sbStart.append("%5D&start=%5B").append(googLat).append(",").append(googLong).append("%5D");
                                else
                                    sbUrl.append(googLat).append(",").append(googLong).append("%5D,%5B");
                            } else {
                                sbUrl.append(googLat).append(",").append(googLong).append("%5D");
                            }
                        }
                    }
                }
                if (isTriangle) 
                    sbUrl.append(sbStart.toString());
                else
                    sbUrl.append("%5D");
            } 
            // unused here only required by XcpViewController
            totUrl.append("Timestamp=1111111111");
            totUrl.append("&Distance=").append(distMetre);
            totUrl.append("&Score=").append(scoreBrut);
            totUrl.append("&location=").append(sLocation);
            totUrl.append(sbUrl.toString());
            System.out.println(totUrl.toString());
            if (totUrl.toString().contains("turnpoints")) {
                winSaveXcp saveWin = new winSaveXcp(i18n, totUrl.toString(), "WP");        
            }
        } catch (Exception e) {
            
        }
    }
    
    private void airChecking() {
        int res = -1;
        
        winFileChoose wf = new winFileChoose(myConfig, i18n, fileType.OACheck, myConfig.getPathOpenAir());  
        File selectedFile = wf.getSelectedFile();
        if (selectedFile != null && selectedFile.exists()) {  
            dialogbox dg = new dialogbox(i18n);
            boolean withoutE = dg.YesNo(i18n.tr("Do you want to exclude airspaces of type E"), "");
            checkAirspace trackCheck = new checkAirspace(myConfig, selectedFile, withoutE);
            if (trackCheck.isAirDbLoad()) {               
                if (mapTrace.isDecodage()) {  
                    res = trackCheck.prepareCheck(mapTrace);
                    if (res == 1) {
                        int badPoints = trackCheck.checkPoints();
                        String s = trackCheck.reportPoints();
                        final Clipboard clipboard = Clipboard.getSystemClipboard();
                        final ClipboardContent content = new ClipboardContent();
                        content.putString(s);            
                        clipboard.setContent(content);                            
                        winComment myComment = new winComment(s,i18n); 
                        if (badPoints > 0) {
                            mapTrace.setAirPoints(badPoints);
                            mapTrace.setGeoJsonAirsp(trackCheck.getViGeoJson());
                            mapTrace.setGeoJsonBadPts(trackCheck.getPtGeoJson());                        
                        } else {
                             mapTrace.setGeoJsonAirsp(trackCheck.getCheckedGeoJson());      
                        }
                        try {
                            map_visu visuFullMap = new map_visu(mapTrace, myConfig);
                            if (visuFullMap.isMap_OK()) {                                                                  
                                carnetHTML = visuFullMap.getMap_HTML();                                             
                                webEngine.loadContent(carnetHTML,"text/html");
                            }

                        } catch (Exception e) {      
                            alertbox aError = new alertbox(myConfig.getLocale());
                            aError.alertError(e.getMessage());                 
                        }                                                                                
                    } else {
                        if (res == 0) {
                            alertbox aInfo = new alertbox(myConfig.getLocale());
                            aInfo.alertInfo(i18n.tr("No airspaces to check"));
                        } else {
                            alertbox aError = new alertbox(myConfig.getLocale());
                            aError.alertNumError(res);
                        }
                    }
                }
            } else {
                alertbox aError = new alertbox(myConfig.getLocale());
                aError.alertNumError(res);
            }                          
        }        
    }
    
    private String askLocationName(double dLat, double dLong) {
        String res = i18n.tr("Unknown");
        
        DecimalFormatSymbols decimalFormatSymbols = new DecimalFormatSymbols();
        decimalFormatSymbols.setDecimalSeparator('.');        
        DecimalFormat decimalFormat = new DecimalFormat("###.0000", decimalFormatSymbols);
        geonominatim nom = new geonominatim();
        nom.askReverseGeo(decimalFormat.format(dLat), decimalFormat.format(dLong));
        if (nom.getGeoStatus().equals("OK")) {
            res = nom.getGeoVille();             
        } 
        
        return res;
    }
    
    @FXML
    private void sendMail(ActionEvent event) {

        if (snapshot()) {            
            winMail showMail = new winMail(myConfig,snapFile.getAbsolutePath(), false);            
        } else {
            winMail showMail = new winMail(myConfig,null, false); 
        }
        
    }    
    
    private void closeMap()  {
        if (idxDb != -1) {
            if (firstLeagueIdx != newLeagueIdx && firstLeagueIdx == -1) {
                updateDbLeague();
            } else if (firstLeagueIdx != newLeagueIdx)  {
                String stFirstLeague = allLeagues.get(firstLeagueIdx);
                String stNewLeague = allLeagues.get(newLeagueIdx);
                dialogbox dConfirm = new dialogbox(i18n);
                StringBuilder sbMsg = new StringBuilder(); 
                sbMsg.append(i18n.tr("Update Score")).append(" : ").append(stFirstLeague).append(" -> ").append(stNewLeague);
                if (dConfirm.YesNo(i18n.tr("Scoring"), sbMsg.toString())) { 
                    updateDbLeague();
                }
            }
            if (changedSuccess) {
                carnetController.trackChanged(mapTrace);
            }
        } else {
            // external track
            if (changedSuccess) {
                // track was modified              
                extController.trackChanged(mapTrace.getCutFilePath());
            }
        }
    }
    
    @FXML
    private void handleClose(ActionEvent event) {
        closeMap();
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
        this.mapStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
          public void handle(WindowEvent we) {
              closeMap();
          }
        });          
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
     * Set a communication bridge with DashViewController 
     * @param callExterne 
     */
    public void setDashBridge(DashViewController callExterne, traceGPS pTrace)  {
        this.dashController = callExterne;  
        mapTrace = pTrace;      
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
            btXcplanner.setVisible(true);
        } else {
            chbLeague.getSelectionModel().select(myConfig.getIdxLeague()); 
            firstLeagueIdx = -1;
            btXcplanner.setVisible(false);
        }
    }    
    
    private boolean snapshot() {
        
        boolean res = false;
        
        // code from http://java-buddy.blogspot.fr/2012/12/take-snapshot-of-node-with-javafx.html
        WritableImage snapImage = viewMap.snapshot(new SnapshotParameters(), null);

        // code from http://java-buddy.blogspot.fr/2012/12/save-writableimage-to-file.html
        try {
            String fileName = mapTrace.suggestShortName()+".png";
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
        btMesure.setText(i18n.tr("Measure"));
        Tooltip msToolTip = new Tooltip();
        msToolTip.setStyle(myConfig.getDecoToolTip());
        msToolTip.setText(i18n.tr("Measure on map"));
        btMesure.setTooltip(msToolTip);  
        btSummary.setText(i18n.tr("Summary"));
        Tooltip sumToolTip = new Tooltip();
        sumToolTip.setStyle(myConfig.getDecoToolTip());
        sumToolTip.setText(i18n.tr("Show a summary of the flight"));        
        btChrono.setText(i18n.tr("Pathway"));
        Tooltip chToolTip = new Tooltip();
        chToolTip.setStyle(myConfig.getDecoToolTip());
        chToolTip.setText(i18n.tr("Show pathway"));
        btMesure.setTooltip(msToolTip);                  
        btScoring.setText(i18n.tr("Scoring"));
        Tooltip scToolTip = new Tooltip();
        scToolTip.setStyle(myConfig.getDecoToolTip());
        scToolTip.setText(i18n.tr("Score evaluation"));        
        btScoring.setTooltip(scToolTip);          
        btCut.setText(i18n.tr("Modify"));
        Tooltip cutToolTip = new Tooltip();
        cutToolTip.setStyle(myConfig.getDecoToolTip());
        cutToolTip.setText(i18n.tr("Adjust track structure"));        
        btMail.setText(i18n.tr("Mail"));
        btCheck.setText(i18n.tr("Checking"));
        Tooltip checkToolTip = new Tooltip();
        checkToolTip.setStyle(myConfig.getDecoToolTip());
        checkToolTip.setText(i18n.tr("Airspaces checking"));
        btCheck.setTooltip(checkToolTip);        
        btClose.setText(i18n.tr("Close"));                
    }    
}
