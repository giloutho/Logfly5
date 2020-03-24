/* 
 * Copyright Gil THOMAS
 * This file forms an integral part of Logfly project
 * See the LICENSE file distributed with source code
 * for details of Logfly licence project
 */
package controller;

import dialogues.alertbox;
import java.awt.Desktop;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Tooltip;
import javafx.scene.image.ImageView;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.web.WebView;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import kml.makingKml;
import leaflet.map_pm;
import leaflet.map_visu;
import littlewins.winPoints;
import littlewins.winTrackFile;
import Logfly.Main;
import javafx.geometry.Rectangle2D;
import javafx.stage.Screen;
import java.util.logging.Level;
import littlewins.winFileChoose;
import littlewins.winFileSave;
import org.xnap.commons.i18n.I18n;
import settings.configProg;
import settings.fileType;
import settings.osType;
import systemio.mylogging;
import systemio.textio;
import static systemio.textio.getFileExtension;
import systemio.webio;
import trackgps.scoring;
import trackgps.traceGPS;

/**
 *
 * @author gil
 * Display an external GPS track
 */
public class TraceViewController {
    // Localization
    private I18n i18n; 
    
    // Settings
    configProg myConfig;
     
    private Stage dialogStage;
     
    traceGPS extTrace; 
    
    @FXML
    private HBox buttonBar;
    @FXML
    private AnchorPane webAnchor;
    @FXML
    private WebView mapViewer;
    @FXML
    private Button btnSelect;
    @FXML
    private ImageView top_Menu;
    @FXML
    private Button btnMap;
    @FXML
    private Button btnVisuGPS;  
    @FXML
    private Button btnScore;      
    @FXML
    private Button btnGEarth;    
    
    // Reference à l'application principale
    private Main mainApp;
    private boolean isGPX;
    private StringBuilder sbError;
    private String lastDirUsed = null;
    
    @FXML
    private void initialize() {
        // empty but necessary
    }
    
    /**
     * Select the track in a folder
     * @throws Exception 
     */
    @FXML
    private void selectTrackFolder() throws Exception {
        
        alertbox aError = new alertbox(myConfig.getLocale());
        winFileChoose wf = new winFileChoose(myConfig, i18n, fileType.IgcGpx, lastDirUsed);  
        File selectedFile = wf.getSelectedFile(); 
        try {            
            if(selectedFile != null && selectedFile.exists()){
                lastDirUsed = selectedFile.getParent();
                String extension = getFileExtension(selectedFile);
                if (extension.equals("GPX") || extension.equals("gpx")) 
                    isGPX = true;
                else
                    isGPX = false;
                extTrace = new traceGPS(selectedFile,true, myConfig);
                if (extTrace.isDecodage()) {                     
                    map_pm visuMap = new map_pm(extTrace, true, myConfig.getIdxMap(),i18n); 
                    StringBuilder sbInfo = new StringBuilder();
                    sbInfo.append(selectedFile.getAbsolutePath()).append("    ");
                    sbInfo.append(String.valueOf(extTrace.getNbPoints())).append(" ").append(i18n.tr("points"));
                    this.mainApp.rootLayoutController.updateMsgBar(sbInfo.toString(), true, 50);              
                    if (visuMap.isMap_OK()) {
                        mapViewer.getEngine().loadContent(visuMap.getMap_HTML());
                    }
                    webAnchor.setVisible(true);
                    buttonBar.setVisible(true);    
                    top_Menu.addEventHandler(MouseEvent.MOUSE_CLICKED,
                    new EventHandler<MouseEvent>() {
                        @Override public void handle(MouseEvent e) {                        
                            clicTop_Menu().show(top_Menu, e.getScreenX(), e.getScreenY());
                        }
                    });                         
                } else {
                    String errMsg;
                    StringBuilder sbMsg = new StringBuilder();
                    if (extTrace.Tb_Tot_Points.size() > 0)  {                            
                        sbMsg.append(i18n.tr("Invalid Track")).append(" - ").append(i18n.tr("Rough Points")).append(" : ");
                        sbMsg.append(extTrace.Tb_Tot_Points.size()).append(" ").append(i18n.tr("valid points")).append(" : ").append(                           extTrace.Tb_Good_Points.size());                              if (isGPX) {
                            sbMsg.append(" -- "+i18n.tr("Try Cartography option"));
                        }  
                        errMsg = sbMsg.toString();
                    } else {      
                        sbMsg.append(i18n.tr("No valid points in this track file"));
                        sbMsg.append(" -- "+i18n.tr("Try Cartography option"));
                        errMsg = sbMsg.toString();
                    }
                    aError.alertError(errMsg);
                    if (isGPX) {
                        winTrackFile myTrace = new winTrackFile(extTrace.getFicGPX());
                    } else {
                        winTrackFile myTrace = new winTrackFile(extTrace.getFicIGC());
                    }           
                }   
            }
        } catch (Exception ex) {
            sbError = new StringBuilder(this.getClass().getName()+"."+Thread.currentThread().getStackTrace()[1].getMethodName());
            sbError.append("\r\n").append(ex.toString());
            mylogging.log(Level.SEVERE, sbError.toString());  
            aError = new alertbox(myConfig.getLocale());
            aError.alertError(ex.getClass().getName() + ": " + ex.getMessage());                
        }             
    }
       
    
    /**
     * Show a fullscreen map of the track in a new window with flght parameters
     */
    @FXML
    private void showFullMapOld() {
        if (extTrace.isDecodage()) {        
            map_visu visuFullMap = new map_visu(extTrace, myConfig);
            if (visuFullMap.isMap_OK()) {
                AnchorPane anchorPane = new AnchorPane();                
                WebView viewMap = new WebView();   
                AnchorPane.setTopAnchor(viewMap, 10.0);
                AnchorPane.setLeftAnchor(viewMap, 10.0);
                AnchorPane.setRightAnchor(viewMap, 10.0);
                AnchorPane.setBottomAnchor(viewMap, 10.0);
                anchorPane.getChildren().add(viewMap);  
                
                String sHTML = visuFullMap.getMap_HTML();
                /** ----- Begin Debug --------*/                 
                final Clipboard clipboard = Clipboard.getSystemClipboard();
                final ClipboardContent content = new ClipboardContent();
                content.putString(sHTML);            
                clipboard.setContent(content);
                
                viewMap.getEngine().loadContent(sHTML,"text/html");
                StackPane subRoot = new StackPane();
                subRoot.getChildren().add(anchorPane);
                Scene secondScene = new Scene(subRoot, 500, 400);
                Stage subStage = new Stage();
                // window will be modal
                subStage.initModality(Modality.APPLICATION_MODAL);
                subStage.setScene(secondScene); 
                subStage.setMaximized(true);
                subStage.show();
            }  else {
                alertbox aErrMap = new alertbox(myConfig.getLocale()); 
                aErrMap.alertError(i18n.tr("An error occurred during the map generation"));                
            }            
        } else {
            alertbox aError = new alertbox(myConfig.getLocale());
            String errMsg;
            if (extTrace.Tb_Tot_Points.size() > 0)  { 
                StringBuilder sbMsg = new StringBuilder();
                sbMsg.append(i18n.tr("Invalid Track")).append(" - ").append(i18n.tr("Rough Points")).append(" : ");
                sbMsg.append(extTrace.Tb_Tot_Points.size()).append(" ").append(i18n.tr("valid points")).append(" : ").append(                           extTrace.Tb_Good_Points.size());                      
                errMsg = sbMsg.toString();
            } else {                            
                errMsg = i18n.tr("No valid points in this track file");
            }
            aError.alertError(errMsg);
        }
    }
    
    @FXML
    private void showFullMap() {
        
        if (extTrace.isDecodage()) {        
            map_visu visuFullMap = new map_visu(extTrace, myConfig);
            if (visuFullMap.isMap_OK()) {            
                try {
                    String sHTML = visuFullMap.getMap_HTML();         
                    /** ----- Begin Debug --------*/                 
                    final Clipboard clipboard = Clipboard.getSystemClipboard();
                    final ClipboardContent content = new ClipboardContent();
                    content.putString(sHTML);            
                    clipboard.setContent(content);                                
                    /**------ End Debug --------- */                       
                    FXMLLoader loader = new FXMLLoader();
                    loader.setLocation(Main.class.getResource("/fullmap.fxml"));                    

                    AnchorPane page = (AnchorPane) loader.load();
                    Stage fullMap = new Stage();            
                    fullMap.initModality(Modality.WINDOW_MODAL);       
                    fullMap.initOwner(mainApp.getPrimaryStage());
                    Scene scene = null;
                    if (myConfig.getOS() == osType.LINUX) {
                        // With this code for Linux, this is not OK with Win and Mac 
                        // This code found on http://java-buddy.blogspot.fr/2012/02/javafx-20-full-screen-scene.html
                        Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
                        scene = new Scene(page, screenBounds.getWidth(), screenBounds.getHeight());
                    } else {
                        // With this code, subStage.setMaximized(true) don't run under Linux
                        scene = new Scene(page, 500, 400);
                    }                                    
                    fullMap.setScene(scene);
                   
                    // Initialization of a communication bridge between CarnetView and KmlView
                    FullMapController controller = loader.getController();
                    controller.setTraceBridge(this);
                    controller.setMapStage(fullMap);  
                    controller.setParams(myConfig, sHTML, -1);
                    controller.setWinMax();
                    fullMap.showAndWait();
                } catch (IOException e) {
                    sbError = new StringBuilder(this.getClass().getName()+"."+Thread.currentThread().getStackTrace()[1].getMethodName());
                    sbError.append("\r\n").append(e.toString());
                    mylogging.log(Level.SEVERE, sbError.toString());
                }
            }
        }
    }        
    
    /**
     * VisuGPS need a track with http url
     * runVisuGPS upload the track with a special php script in a server
     * This script upload the track and delete old tracks      
     */
    @FXML
    private void runVisuGPS() {
        if (extTrace.isDecodage()) { 
            webio myUpload = new webio();
            try {
                String uploadUrl = myConfig.getUrlLogflyIGC()+"jtransfert.php";
                if (myUpload.testURL(uploadUrl) == 200)  {
                    byte[] igcBytes = extTrace.exportBytes();
                    if (igcBytes.length > 100)  {
                        String webFicIGC = myUpload.httpUploadIgc(igcBytes, uploadUrl);
                        if (webFicIGC != null) {
                            showVisuGPS(webFicIGC);
                        } else {
                            myUpload.getDlError();
                            alertbox aError = new alertbox(myConfig.getLocale());     
                            aError.alertNumError(myUpload.getDlError());                                                          
                        }
                    }                                        
                } else {
                    Alert alert = new Alert(Alert.AlertType.ERROR);           
                    alert.setContentText(i18n.tr("Bad download URL"));
                    alert.showAndWait();  
                }                
            } catch (Exception e) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle(i18n.tr("Could not download track"));            
                String s = e.getClass().getName() + ": " + e.getMessage();
                alert.setContentText(s);
                alert.showAndWait();     
            }       
        }
    }
    
    /**
     * track uploaded in a server with a name like
     * YYYYMMDDHHMMSS_Random  [Random = number between 1 and 1000]
     * @param webFicIGC 
     */    
    private void showVisuGPS(String webFicIGC)  {
        StringBuilder visuUrl = new StringBuilder();
        visuUrl.append(myConfig.getUrlVisu()).append(myConfig.getUrlLogflyIGC());
        visuUrl.append(webFicIGC);
        System.out.println(visuUrl.toString());
        AnchorPane anchorPane = new AnchorPane();                
        WebView viewMap = new WebView();   
        AnchorPane.setTopAnchor(viewMap, 10.0);
        AnchorPane.setLeftAnchor(viewMap, 10.0);
        AnchorPane.setRightAnchor(viewMap, 10.0);
        AnchorPane.setBottomAnchor(viewMap, 10.0);
        anchorPane.getChildren().add(viewMap);                         
        viewMap.getEngine().load(visuUrl.toString());
        StackPane subRoot = new StackPane();
        subRoot.getChildren().add(anchorPane);
        Scene secondScene = new Scene(subRoot, 500, 400);
        Stage subStage = new Stage();
        // On veut que cette fenêtre soit modale
        subStage.initModality(Modality.APPLICATION_MODAL);
        subStage.setScene(secondScene); 
        subStage.setMaximized(true);
        subStage.show();       
    }
    
    /**
     * if needed, call the scoring class
     */
    @FXML
    private void showScore() {
        if (extTrace.isDecodage()) {
            if (extTrace.isScored())  {
                showFullMap();                
            } else {
                // On lance le calcul avec le module externe points (classe scoring) dont on DOIT attendre la fin d'execution
                // C'est la classe scoring qui reviendra vers le controller en appellant scoreReturn()
                // D'où la nécessité de mettre un pont avec ce controller
                scoring currScore = new scoring(this,3,0, myConfig);  
                currScore.start(extTrace, myConfig.getIdxLeague());                            
            }
        }
    }
       
    /**
     * Answer of scoring class
     * @param pRetour 
     */
    public void scoreReturn(int pRetour) {
        // If scoring failed, error message was sent by Scoring class
        if (extTrace.isScored())  {            
            switch (pRetour) {
                case 3:
                    showFullMap();
                    break;   
                case 4:
                    showWinGE();
                    break; 
            }
        }         
    }
    
    /**
     * Manage Google Earth kml file generation
     */
    @FXML
    private void askWinGE() {
        // Track will be scored before generation
        if (extTrace.isDecodage()) {
            if (extTrace.isScored())  {
               showWinGE();                
            } else {
                // Launch computation with an external program "points" (Scoring class). We must wait the end of the process
                // Scoring claas come back to controller with scoreReturn()
                // Therefore a communication bridge is necessary
                scoring currScore = new scoring(this,4,0, myConfig);  
                currScore.start(extTrace, myConfig.getIdxLeague());                            
            }
        }        
    }
    
    /**
     * Display window with parameters kml generation
     * @return 
     */
    private boolean showWinGE() {
        try {                                  
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(Main.class.getResource("/KmlView.fxml")); 
            
            AnchorPane page = (AnchorPane) loader.load();
            Stage dialogStage = new Stage();
            dialogStage.setTitle(i18n.tr("Generating KML file"));
            dialogStage.initModality(Modality.WINDOW_MODAL);       
            dialogStage.initOwner(mainApp.getPrimaryStage());
            Scene scene = new Scene(page);
            dialogStage.setScene(scene);
            
            // Initialization of a communication bridge 
            KmlViewController controller = loader.getController();
            controller.setTraceBridge(this);
            controller.setAppel(2, myConfig);
            controller.setDialogStage(dialogStage);
            // controller.setPerson(person);           
            // Show the dialog and wait until the user closes it
            dialogStage.showAndWait();
            
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Answer of KmlViewController when kml generation is finished 
     * @param currKml
     * @throws IOException 
     */
    public void configKml(makingKml currKml) throws IOException {  
        boolean kmlDisk = false;
        File ficKml = null;

        currKml.genKml(extTrace);
        if (currKml.isKmlOK())  {
            if (currKml.getErrorCode() == 0) {
                if (currKml.isExport() && currKml.getExportPath() != null)  {
                    // Génération du fichier demandé
                    ficKml = new File(currKml.getExportPath());
                    if (textio.writeTxtFile(ficKml, currKml.getKmlString())) {                       
                        kmlDisk = true;
                    }
                }
                if (currKml.isRunGE()) {
                    if (!kmlDisk) {
                        ficKml = systemio.tempacess.getAppFile("Logfly", "temp.kml");
                        System.out.println("fichier kml : "+ficKml.getAbsolutePath());
                        if (textio.writeTxtFile(ficKml, currKml.getKmlString())) kmlDisk = true;
                    }
                    if (kmlDisk) {
                        try {                        
                            Desktop dt = Desktop.getDesktop();     
                            dt.open(ficKml);            
                        } catch (Exception e) {
                            alertbox aError = new alertbox(myConfig.getLocale());
                            aError.alertNumError(1030); 
                        }       
                    }
                } else {
                    alertbox aInfo = new alertbox(myConfig.getLocale());
                    aInfo.alertInfo(i18n.tr("File generation completed")); 
                }
            } else {
                alertbox aError = new alertbox(myConfig.getLocale());
                aError.alertNumError(currKml.getErrorCode()); 
            }
        }
    }

    /**
     * Track export on disk
     */
    private void exportTrace() {
        int res = -1;
        winFileSave wfs = new winFileSave(myConfig, i18n, fileType.IgcGpx, null, null);  
        File selectedFile = wfs.getSelectedFile();      
        if(selectedFile != null){
            String selExt = wfs.getExtFormat();                
            switch (selExt) {
                case ".igc":
                    try {
                        FileWriter fileWriter = null;
                        fileWriter = new FileWriter(selectedFile);
                        fileWriter.write(extTrace.getFicIGC());
                        fileWriter.close();
                        res = 0;
                    } catch (IOException ex) {
                        res = 2;
                    }
                    break;
                case ".gpx":
                   String exportGPX = null;
                    if (extTrace.getOrigine().equals("IGC")) {
                        res = extTrace.encodeGPX();
                        if (res == 0) exportGPX = extTrace.getFicGPX();
                    } else {
                        exportGPX = extTrace.getFicGPX();
                    }
                    if (exportGPX != null && !exportGPX.equals("")) {
                        try {
                            FileWriter fileWriter = null;
                            fileWriter = new FileWriter(selectedFile);                        
                            fileWriter.write(extTrace.getFicGPX());
                            fileWriter.close();
                            res = 0;
                        } catch (IOException ex) {
                            res = 2;
                        }   
                    }         
                    break;
            }
            alertbox finOp = new alertbox(myConfig.getLocale());
            finOp.alertNumError(res);
        }        
    }        
        
    // Add a context menu based on Adding Context Menus from
    // http://docs.oracle.com/javafx/2/ui_controls/menu_controls.htm    
    private ContextMenu clicTop_Menu()   {
        final ContextMenu cm = new ContextMenu();
        
        MenuItem cmItem0 = new MenuItem(i18n.tr("Summary"));        
        cmItem0.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent e) {
                showSummary();
            }
        });
        cm.getItems().add(cmItem0);        
        
        MenuItem cmItem1 = new MenuItem(i18n.tr("Track file"));        
        cmItem1.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent e) {
                winTrackFile myTrace = new winTrackFile(extTrace.getFicIGC());                     
            }
        });
        cm.getItems().add(cmItem1);
        
        MenuItem cmItem2 = new MenuItem(i18n.tr("Points list"));
        cmItem2.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent e) {
                winPoints myGrid = new winPoints(myConfig.getLocale());    
                myGrid.showTablePoints(extTrace);
            }
        });
        cm.getItems().add(cmItem2);
        
        MenuItem cmItem3 = new MenuItem(i18n.tr("Export"));
        cmItem3.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent e) {
                exportTrace();
            }
        });
        cm.getItems().add(cmItem3);
        
        MenuItem cmItemMa = new MenuItem(i18n.tr("Mail"));
        cmItemMa.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent e) {
                alertbox aInfo = new alertbox(myConfig.getLocale());
                aInfo.alertInfo(i18n.tr("Send via e-mail"));   
            }
        });
        cm.getItems().add(cmItemMa);
        
        return cm;
    }    

    private void showSummary() {
        try {                     
            // Load the fxml file and create a new stage for the popup dialog.
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(Main.class.getResource("/summary.fxml")); 
            AnchorPane page = (AnchorPane) loader.load();

            Stage dialogStage = new Stage();
            dialogStage.initModality(Modality.WINDOW_MODAL);       
            dialogStage.initOwner(mainApp.getPrimaryStage());
            Scene scene = new Scene(page);
            dialogStage.setScene(scene);

            // Communication bridge between SummaryController and TraceViewController
            SummaryController controller = loader.getController();
            controller.setTraceBridge(this);
            controller.setForm(myConfig);
            controller.iniData(extTrace);
            controller.setDialogStage(dialogStage); 
            dialogStage.showAndWait();
                       
        } catch (IOException e) {
            sbError = new StringBuilder(this.getClass().getName()+"."+Thread.currentThread().getStackTrace()[1].getMethodName());
            sbError.append("\r\n").append(e.toString());
            mylogging.log(Level.SEVERE, sbError.toString()); 
            
        }
    }    
    
    
    /**
     * Is called by the main application to give a reference back to itself.
     * 
     * @param mainApp
     */
    public void setMainApp(Main mainApp) {
        this.mainApp = mainApp;    
        myConfig = mainApp.myConfig;
        i18n = myConfig.getI18n();
        // StatusBar is cleaned
        mainApp.rootLayoutController.updateMsgBar("", false, 60);
        winTraduction();
    }

    /**
    * Translate labels of the window
    */
    private void winTraduction() {
        btnSelect.setText(i18n.tr("Select a track"));
        
        btnMap.setStyle("-fx-background-color: transparent;");
        Tooltip mapToolTip = new Tooltip();
        mapToolTip.setStyle(myConfig.getDecoToolTip());
        mapToolTip.setText(i18n.tr("Full screen map"));
        btnMap.setTooltip(mapToolTip);
                
        btnVisuGPS.setStyle("-fx-background-color: transparent;"); 
        Tooltip visuToolTip = new Tooltip();
        visuToolTip.setStyle(myConfig.getDecoToolTip());
        visuToolTip.setText(i18n.tr("VisuGPS display"));
        btnVisuGPS.setTooltip(visuToolTip);
        
        btnScore.setStyle("-fx-background-color: transparent;");      
        Tooltip scoreToolTip = new Tooltip();
        scoreToolTip.setStyle(myConfig.getDecoToolTip());
        scoreToolTip.setText(i18n.tr("Score evaluation"));
        btnScore.setTooltip(scoreToolTip);
        
        btnGEarth.setStyle("-fx-background-color: transparent;");      
        Tooltip geToolTip = new Tooltip();
        geToolTip.setStyle(myConfig.getDecoToolTip());
        geToolTip.setText(i18n.tr("Google Earth file generation"));
        btnGEarth.setTooltip(geToolTip);
    }
    
}
