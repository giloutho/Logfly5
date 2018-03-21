/* 
 * Copyright Gil THOMAS
 * This file forms an integral part of Logfly project
 * See the LICENSE file distributed with source code
 * for details of Logfly licence project
 */
package controller;

import dialogues.alertbox;
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
import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import littlewins.winLog;
import littlewins.winMail;
import littlewins.winWeb;
import org.xnap.commons.i18n.I18n;
import org.xnap.commons.i18n.I18nFactory;
import settings.configProg;

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
    private Label LbMsg; 
    
    // Refer to the main application.
    private Main mainApp;
    
    private configProg myConfig;
        
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
            comingSoon();
        });           
        mnSites.setOnMouseClicked((MouseEvent event) -> {
            switchMenu(8);
            mainApp.showSitesOverview();
        });            
        mnBalises.setOnMouseClicked((MouseEvent event) -> {
            switchMenu(9);
            comingSoon();
        });      
        mnEspaces.setOnMouseClicked((MouseEvent event) -> {
            switchMenu(10);
            comingSoon();
        });       
        mnPhotos.setOnMouseClicked((MouseEvent event) -> {
            switchMenu(11);
            comingSoon();
        });    
        mnCarto.setOnMouseClicked((MouseEvent event) -> {
            switchMenu(12);
            comingSoon();
        });            
        btnSupport.addEventHandler(MouseEvent.MOUSE_CLICKED,
                new EventHandler<MouseEvent>() {
                    @Override public void handle(MouseEvent e) {                        
                            clicTop_Menu().show(btnSupport, e.getScreenX(), e.getScreenY());
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
    
    /**
     * Show support window
     */
//    @FXML 
//    public void showSupport() {
//        winTrackFile myTrace = new winTrackFile("Ceci est une trace ");                                    
//    }
    
    /**
     * Show tools window
     */
    @FXML 
    public void showOutils() {
        alertbox myInfo = new alertbox(myConfig.getLocale()); 
        StringBuilder sb = new StringBuilder();
        sb.append("Largeur : ").append(String.valueOf(mainApp.getPrimaryStage().getWidth()));
        sb.append(" Hauteur : ").append(String.valueOf(mainApp.getPrimaryStage().getHeight()));
        myInfo.alertInfo(sb.toString());       
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
        
        MenuItem cmItem2 = new MenuItem(i18n.tr("Notes de publication"));        
        cmItem2.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent e) {
                winWeb myWeb = new winWeb(myConfig,"http://logfly.org/logfly5/historique");
            }
        });
        cm.getItems().add(cmItem2);          
        
        MenuItem cmItemSup = new MenuItem(i18n.tr("Base données"));        
        cmItemSup.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent e) {
                dbRepair();
            }
        });
        cm.getItems().add(cmItemSup);                     
        
        return cm;
    }
    
    private void displayLog() {
        winLog myLog = new winLog(myConfig);        
    }
    
    private void sendMailSupport() {
        if (myConfig.isValidConfig()) {
            File fileLog = new File(myConfig.getPathW()+"logfly.log");
            if (fileLog.exists()) {   
                winMail showMail = new winMail(myConfig,fileLog.getAbsolutePath(), true);            
            } else {
                winMail showMail = new winMail(myConfig, null, true);   
            }
        } else {
            winMail showMail = new winMail(myConfig, null, true); 
        }
    }
    
    private void dbRepair() {
        alertbox aError = new alertbox(myConfig.getLocale());
        aError.alertInfo(i18n.tr("Non implémenté..."));     
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
