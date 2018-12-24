/* 
 * Copyright Gil THOMAS
 * This file forms an integral part of Logfly project
 * See the LICENSE file distributed with source code
 * for details of Logfly licence project
 */
package controller;

import java.io.IOException;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import Logfly.Main;
import database.dbParawing;
import dialogues.alertbox;
import dialogues.dialogbox;
import geoutils.geonominatim;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.logging.Level;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.HBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import littlewins.winLog;
import littlewins.winMail;
import littlewins.winOsmCities;
import littlewins.winWeb;
import model.Sitemodel;
import org.xnap.commons.i18n.I18n;
import org.xnap.commons.i18n.I18nFactory;
import settings.configProg;
import systemio.mylogging;

/**
 *
 * @author Gil Thomas logfly.org
 */
public class RootLayoutController {
    private I18n i18n;
         
    @FXML
    private Label mnCarnet;    
    @FXML
    private Label mnGPS;     
    @FXML
    private Label mnImport;    
    @FXML
    private Label mnManuel;     
    @FXML
    private Label mnTrace;    
    @FXML
    private Label mnSynthese;    
    @FXML
    private Label mnStat;    
    @FXML
    private Label mnSites;    
    @FXML
    private Label mnBalises;    
    //@FXML
    //private Label mnXcplanner;
    @FXML
    private Label mnEspaces;    
    @FXML
    private Label mnPhotos;    
    @FXML
    private Label mnCarto;     
    @FXML
    private Button btnConfig;
    @FXML
    private Button btnTools;
    @FXML
    private Button btnSupport;
    @FXML
    private Button btnInfo;    
    @FXML
    private Label LbMsg; 
    @FXML
    private HBox hbMenu;
    @FXML
    private HBox hbInput;
    @FXML
    private HBox hbAction;
    @FXML
    private SplitPane mapPane;
    // Refer to the main application.
    public Main mainApp;
    
    private configProg myConfig;
    private StringBuilder sbError;
        
    @FXML
    private void initialize() {           
        // At startup, default display is logbook table, logbook option is highlighted
        mnCarnet.setStyle("-fx-text-fill:black; -fx-background-color:  #CAC3C2;");
        //Initialization of click areas with lambda expressions
        mnCarnet.setOnMouseClicked((MouseEvent event) -> {
            switchMenu(1);
            mainApp.showCarnetOverview();
        });
        mnGPS.setOnMouseClicked((MouseEvent event) -> {
            switchMenu(2);
            mainApp.showGPSview();
        });
        mnImport.setOnMouseClicked((MouseEvent event) -> {
            switchMenu(3);
            mainApp.showImportview();
        });
        mnManuel.setOnMouseClicked((MouseEvent event) -> {
            switchMenu(4);
            mainApp.showManualview(0, null);
        });        
        mnTrace.setOnMouseClicked((MouseEvent event) -> {
            switchMenu(5);
            mainApp.showTraceview();
        });  
        mnSynthese.setOnMouseClicked((MouseEvent event) -> {
            switchMenu(6);
            mainApp.showDashView();
        });        
        mnStat.setOnMouseClicked((MouseEvent event) -> {
            switchMenu(7);
            mainApp.showStatView();
        });           
        mnSites.setOnMouseClicked((MouseEvent event) -> {
            switchMenu(8);
            mainApp.showSitesOverview();
        });            
        mnBalises.setOnMouseClicked((MouseEvent event) -> {
            switchMenu(9);
            mainApp.showWaypOverview();
        });  
//        mnXcplanner.setOnMouseClicked((MouseEvent event) -> {
//            switchMenu(13);
//            mainApp.showXcplannerview();
//        });           
        mnEspaces.setOnMouseClicked((MouseEvent event) -> {
            switchMenu(10);
            mainApp.showAirspaceView();
        });       
        mnPhotos.setOnMouseClicked((MouseEvent event) -> {
            switchMenu(11);
            comingSoon();
        });    
        mnCarto.setOnMouseClicked((MouseEvent event) -> {
            switchMenu(12);
            mainApp.showCartoOverview();
        });            
        btnSupport.addEventHandler(MouseEvent.MOUSE_CLICKED,
                new EventHandler<MouseEvent>() {
                    @Override public void handle(MouseEvent e) {                        
                            clicTop_Menu().show(btnSupport, e.getScreenX(), e.getScreenY());
                    }
            });  
        btnInfo.addEventHandler(MouseEvent.MOUSE_CLICKED,
                new EventHandler<MouseEvent>() {
                    @Override public void handle(MouseEvent e) {                        
                            clicInfo_Menu().show(btnSupport, e.getScreenX(), e.getScreenY());
                    }
            });          
        btnTools.addEventHandler(MouseEvent.MOUSE_CLICKED,
                new EventHandler<MouseEvent>() {
                    @Override public void handle(MouseEvent e) {                        
                            clicTool_Menu().show(btnTools, e.getScreenX(), e.getScreenY());
                    }
            });          
    }
           
    /**
     * Is called by the main application to give a reference back to itself.
     * 
     * @param mainApp
     */
    public void setMainApp(Main mainApp) {
        this.mainApp = mainApp;  
        myConfig = mainApp.myConfig;       
        mainApp.getPrimaryStage().setWidth(myConfig.getMainWidth());
        mainApp.getPrimaryStage().setHeight(myConfig.getMainHeight());
        i18n = I18nFactory.getI18n("","lang/Messages",RootLayoutController.class.getClass().getClassLoader(),myConfig.getLocale(),0);
        winTraduction();
    }
    
    /**
     * Show Configuration window
     * @return 
     */
    @FXML
    public boolean showConfigDialog() {
        try {                     
            // Load the fxml file and create a new stage for the popup dialog.
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(Main.class.getResource("/ConfigView.fxml")); 
            AnchorPane page = (AnchorPane) loader.load();

            Stage dialogStage = new Stage();
            dialogStage.setTitle(i18n.tr("Configuration"));
            dialogStage.initModality(Modality.WINDOW_MODAL);       
            dialogStage.initOwner(mainApp.getPrimaryStage());
            Scene scene = new Scene(page);
            dialogStage.setScene(scene);

            // Communication bridge between Configview and RootLayout controllers
            ConfigViewController controller = loader.getController();
            controller.setRootBridge(this);
            controller.setDialogStage(dialogStage); 
            controller.setMyConfig(myConfig);
            // This window will be modal
            dialogStage.showAndWait();
            
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    private void originalSize() {
        dialogbox dConfirm = new dialogbox(i18n);
        StringBuilder sb = new StringBuilder();
        sb.append("Largeur : ").append(String.valueOf(mainApp.getPrimaryStage().getWidth()));
        sb.append(" - > 1100").append(System.getProperty("line.separator"));
        sb.append(" Hauteur : ").append(String.valueOf(mainApp.getPrimaryStage().getHeight()));
        sb.append(" - > 600");
        if (dConfirm.YesNo(i18n.tr("Retour à la taille originale"), sb.toString()))   {         
            mainApp.getPrimaryStage().setWidth(1102);
            mainApp.getPrimaryStage().setHeight(650);
        }      
    }
    
    public void updateMsgBar(String sMessage, boolean visuBar, int iLeftPadding) { 
        // default left padding is 50
        LbMsg.setPadding(new Insets(4, 0, 0, iLeftPadding));
        LbMsg.setText(sMessage);
        LbMsg.setVisible(visuBar);
    }
    
    private void comingSoon() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);                       
        alert.setContentText(i18n.tr("C'est pour bientôt..."));
        alert.showAndWait();   
    }
    
    /**
     * Selected option is highlighted (IdxMenu)
     * Others are in normal mode (black background, white letters)
     * @param idxMenu 
     */
    public void switchMenu(int idxMenu)  {
        
        mnCarnet.setStyle("-fx-text-fill:white; -fx-background-color:  #000000;");
        mnGPS.setStyle("-fx-text-fill:white; -fx-background-color:  #000000;"); 
        mnImport.setStyle("-fx-text-fill:white; -fx-background-color:  #000000;");
        mnManuel.setStyle("-fx-text-fill:white; -fx-background-color:  #000000;");        
        mnTrace.setStyle("-fx-text-fill:white; -fx-background-color:  #000000;");
        mnSynthese.setStyle("-fx-text-fill:white; -fx-background-color:  #000000;");
        mnStat.setStyle("-fx-text-fill:white; -fx-background-color:  #000000;");
        mnSites.setStyle("-fx-text-fill:white; -fx-background-color:  #000000;");
        mnBalises.setStyle("-fx-text-fill:white; -fx-background-color:  #000000;");
        //mnXcplanner.setStyle("-fx-text-fill:white; -fx-background-color:  #000000;"); 
        mnEspaces.setStyle("-fx-text-fill:white; -fx-background-color:  #000000;");
        mnPhotos.setStyle("-fx-text-fill:white; -fx-background-color:  #000000;");
        mnCarto.setStyle("-fx-text-fill:white; -fx-background-color:  #000000;"); 
        switch (idxMenu) {
            case 1:
                mnCarnet.setStyle("-fx-text-fill:black; -fx-background-color:  #CAC3C2;");
                break;
            case 2:
                mnGPS.setStyle("-fx-text-fill:black; -fx-background-color:  #CAC3C2;");                                       
                break;
            case 3:
                mnImport.setStyle("-fx-text-fill:black; -fx-background-color:  #CAC3C2;");
                break;
            case 4:
                mnManuel.setStyle("-fx-text-fill:black; -fx-background-color:  #CAC3C2;");
                break;
            case 5:
                mnTrace.setStyle("-fx-text-fill:black; -fx-background-color:  #CAC3C2;");
                break;
            case 6:
                mnSynthese.setStyle("-fx-text-fill:black; -fx-background-color:  #CAC3C2;");               
                break;
            case 7:
                mnStat.setStyle("-fx-text-fill:black; -fx-background-color:  #CAC3C2;");                
                break;
            case 8:
                mnSites.setStyle("-fx-text-fill:black; -fx-background-color:  #CAC3C2;");                
                break;
            case 9:
                mnBalises.setStyle("-fx-text-fill:black; -fx-background-color:  #CAC3C2;");                
                break;
            case 10:
                mnEspaces.setStyle("-fx-text-fill:black; -fx-background-color:  #CAC3C2;");                
                break;
            case 11:
                mnPhotos.setStyle("-fx-text-fill:black; -fx-background-color:  #CAC3C2;");
                break;
            case 12:    
                mnCarto.setStyle("-fx-text-fill:black; -fx-background-color:  #CAC3C2;");
                break;    
            case 13:    
               // mnXcplanner.setStyle("-fx-text-fill:black; -fx-background-color:  #CAC3C2;");
                break;                    
            default:
                throw new AssertionError();
        }        
    }
    
    /**
     * Adding Context Menus, last paragraph
    *     http://docs.oracle.com/javafx/2/ui_controls/menu_controls.htm    
    */
    private ContextMenu clicTop_Menu()   {
        final ContextMenu cm = new ContextMenu();
        
        MenuItem cmItem0 = new MenuItem(i18n.tr("Envoyer un mail"));        
        cmItem0.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent e) {
                sendMailSupport();
            }            
        });
        cm.getItems().add(cmItem0);
        
        MenuItem cmItem1 = new MenuItem(i18n.tr("Fichier log"));
        cmItem1.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent e) {
               displayLog();
            }
        });
        cm.getItems().add(cmItem1);
        
        MenuItem cmItem11 = new MenuItem(i18n.tr("Rapport système"));
        cmItem11.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent e) {
               displaySystem();
            }
        });
        cm.getItems().add(cmItem11);                       
                                    
        return cm;
    }
    
    private ContextMenu clicInfo_Menu()   {
        final ContextMenu cm = new ContextMenu();                      
        
        MenuItem cmItem1 = new MenuItem(i18n.tr("Notes de publication"));        
        cmItem1.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent e) {
                winWeb myWeb = new winWeb(myConfig,"https://www.logfly.org/doku.php?id=historique:historique");
            }
        });
        cm.getItems().add(cmItem1);          
        
        MenuItem cmItem2 = new MenuItem(i18n.tr("Credits"));
        cmItem2.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent e) {
               winWeb myWeb = new winWeb(myConfig,"https://www.logfly.org/doku.php?id=credits");
            }
        });
        cm.getItems().add(cmItem2);          
                                    
        return cm;
    }    
    
    private ContextMenu clicTool_Menu()   {
        final ContextMenu cm = new ContextMenu();
        
        MenuItem cmItem0 = new MenuItem(i18n.tr("Affichage original"));        
        cmItem0.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent e) {
                originalSize();
            }            
        });
        cm.getItems().add(cmItem0);
        
        MenuItem cmItem1 = new MenuItem(i18n.tr("Copie carnet"));
        cmItem1.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent e) {
               copyDb();
            }
        });
        cm.getItems().add(cmItem1);
        
        MenuItem cmItem2 = new MenuItem(i18n.tr("Import csv"));        
        cmItem2.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent e) {
                importCsv();
            }
        });
        cm.getItems().add(cmItem2);          
        
        MenuItem cmItem3 = new MenuItem(i18n.tr("Export csv"));        
        cmItem3.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent e) {
                comingSoon();
            }
        });
        cm.getItems().add(cmItem3);  
        
        MenuItem cmItem4 = new MenuItem(i18n.tr("Export sql"));        
        cmItem4.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent e) {
                comingSoon();
            }
        });
        cm.getItems().add(cmItem4);    
        
        MenuItem cmItem5 = new MenuItem(i18n.tr("Traduction"));        
        cmItem5.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent e) {
                winWeb myWeb = new winWeb(myConfig,"http://www.logfly.org/doku.php?id=trad:tradinprog");
            }
        });
        cm.getItems().add(cmItem5);         
        
        return cm;
    }
    
    private void displaySystem() {
        winLog myLog = new winLog(myConfig,1); 
    }
    
    private void displayLog() {
        winLog myLog = new winLog(myConfig,0);        
    }
    
    private void copyDb() {
        
        if (myConfig.isValidConfig()) {
            try {                
                DirectoryChooser directoryChooser = new DirectoryChooser();
                File selectedDirectory = directoryChooser.showDialog(null);        
                if(selectedDirectory != null){
                    Path srcPath = Paths.get(myConfig.getFullPathDb());
                    Path dstPath = Paths.get(selectedDirectory.getAbsolutePath()+File.separator+myConfig.getDbName());
                    Files.copy(srcPath, dstPath, StandardCopyOption.REPLACE_EXISTING);                    
                }
            } catch (Exception ex) {
                sbError = new StringBuilder(this.getClass().getName()+"."+Thread.currentThread().getStackTrace()[1].getMethodName());
                sbError.append("\r\n").append(ex.toString());
                mylogging.log(Level.SEVERE, sbError.toString());  
                alertbox aError = new alertbox(myConfig.getLocale());
                aError.alertError(ex.getClass().getName() + ": " + ex.getMessage());                  
            }
        } else {
            alertbox aError = new alertbox(myConfig.getLocale());
            aError.alertNumError(20);   // Invalid configuration            
        }           
        
    }        
    
    private void importCsv() {
        FileChooser fileChooser = new FileChooser();
        FileChooser.ExtensionFilter csvFilter = new FileChooser.ExtensionFilter(i18n.tr("fichiers vols (*.csv)"), "*.csv");        
        fileChooser.getExtensionFilters().add(csvFilter);
        File selectedFile = fileChooser.showOpenDialog(null);  
        if(selectedFile != null){   
            dbParawing impParawing = new dbParawing(myConfig, i18n, this);
            impParawing.importCsv(selectedFile);
        }        
    }
    
    private void sendMailSupport() {
        if (myConfig.isValidConfig()) {
            // Finalement on n'envoie pas le fichier log systématiquement
            winMail showMail = new winMail(myConfig,null, true);            
        } else {
            winMail showMail = new winMail(myConfig, null, true); 
        }
    }
        
    public void changeCarnetView()  {  
        switchMenu(1);
        mainApp.showCarnetOverview();
    }
    
    /**
    * Translate labels of the window
    */
    private void winTraduction() {         
        mnCarnet.setText(i18n.tr("Carnet"));
        mnGPS.setText(i18n.tr("Import GPS")); 
        mnImport.setText(i18n.tr("Import disque"));
        mnManuel.setText(i18n.tr("Import manuel"));
        mnTrace.setText(i18n.tr("Trace externe"));
        LocalDate today = LocalDate.now();
        DateTimeFormatter dtfYear = DateTimeFormatter.ofPattern(" yyyy");
        mnSynthese.setText(i18n.tr("Synthèse")+today.format(dtfYear));
        mnStat.setText(i18n.tr("Statistiques"));
        mnSites.setText(i18n.tr("Sites"));
        mnBalises.setText(i18n.tr("Balises"));
        mnEspaces.setText(i18n.tr("Espaces aériens"));
        mnPhotos.setText(i18n.tr("Photos"));
        mnCarto.setText(i18n.tr("Cartographie"));        
        Tooltip confToolTip = new Tooltip();
        confToolTip.setStyle(myConfig.getDecoToolTip());
        confToolTip.setText(i18n.tr("Configuration"));
        btnConfig.setTooltip(confToolTip);
        Tooltip toolsToolTip = new Tooltip();
        toolsToolTip.setStyle(myConfig.getDecoToolTip());        
        toolsToolTip.setText(i18n.tr("Outils"));
        btnTools.setTooltip(toolsToolTip);
        Tooltip supToolTip = new Tooltip();
        supToolTip.setStyle(myConfig.getDecoToolTip());        
        supToolTip.setText(i18n.tr("Support"));
        btnSupport.setTooltip(supToolTip);
    }    
     
}
