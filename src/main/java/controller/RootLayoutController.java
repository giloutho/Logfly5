/*
 * Copyright Gil THOMAS
 * Ce fichier fait partie intégrante du projet Logfly
 * Pour tous les détails sur la licence du projet Logfly
 * Consulter le fichier LICENSE distribué avec le code source
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
import littlewins.winTrackFile;
import Logfly.Main;
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
    private Label mnTrace;    
    @FXML
    private Label mnCalendrier;    
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
    
    
    // Reference to the main application.
    private Main mainApp;
    
    private configProg myConfig;
        
    /*
    * Procédure destinée à afficher une AboutBox lors du clic sur le logo
    */
    
    @FXML
    private void initialize() {           
        // Au premier lancement, on affiche le carnet donc on met l'option menu Carnet en surbrillance
        mnCarnet.setStyle("-fx-text-fill:black; -fx-background-color:  #CAC3C2;");
        //Initialisation des zones cliquables avec les expressions Lambda
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
        mnTrace.setOnMouseClicked((MouseEvent event) -> {
            switchMenu(4);
            mainApp.showTraceview();
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
        //i18n = I18nFactory.getI18n(Logfly.Main.class.getClass(), myConfig.getLocale());  
        i18n = I18nFactory.getI18n(Logfly.Main.class.getClass(),myConfig.getLocale());  
        winTraduction();
    }
    
    
    @FXML
    public boolean showConfigDialog() {
        try {                     
            // Load the fxml file and create a new stage for the popup dialog.
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(Main.class.getResource("/ConfigView.fxml")); 
            // Détermination de la langue à utiliser
            configProg myConfig = new configProg();   
            AnchorPane page = (AnchorPane) loader.load();

            // Creation de la scène pour la fenêtre de Configuration
            Stage dialogStage = new Stage();
            dialogStage.setTitle(i18n.tr("Configuration"));
            dialogStage.initModality(Modality.WINDOW_MODAL);       
            dialogStage.initOwner(mainApp.getPrimaryStage());
            Scene scene = new Scene(page);
            dialogStage.setScene(scene);

            // Initialisation d'un pont de communication entre le controller Configview et RootLayout
            ConfigViewController controller = loader.getController();
            controller.setRootBridge(this);
            controller.setDialogStage(dialogStage); 
            controller.setMyConfig(myConfig);
            // Cette fenêtre sera modale
            dialogStage.showAndWait();
            
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    @FXML 
    public void showSupport() {
        winTrackFile myTrace = new winTrackFile("Ceci est une trace ");                                    
    }
    
    @FXML 
    public void showOutils() {
        alertbox myInfo = new alertbox();
        myInfo.alertInfo("Module en construction");
    }
    
    /**
     * L'option sélectionnée (IdxMenu) est mise en surbrillance
     * toutes les autres options sont remises en mode normal (fond noir, lettres blanches)
     * @param idxMenu 
     */
    public void switchMenu(int idxMenu)  {
        
        mnCarnet.setStyle("-fx-text-fill:white; -fx-background-color:  #000000;");
        mnGPS.setStyle("-fx-text-fill:white; -fx-background-color:  #000000;"); 
        mnImport.setStyle("-fx-text-fill:white; -fx-background-color:  #000000;");
        mnTrace.setStyle("-fx-text-fill:white; -fx-background-color:  #000000;");
        mnCalendrier.setStyle("-fx-text-fill:white; -fx-background-color:  #000000;");
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
                mnTrace.setStyle("-fx-text-fill:black; -fx-background-color:  #CAC3C2;");
                break;
            case 5:
                mnCalendrier.setStyle("-fx-text-fill:black; -fx-background-color:  #CAC3C2;");
                break;
            case 6:
                mnStat.setStyle("-fx-text-fill:black; -fx-background-color:  #CAC3C2;");
                break;
            case 7:
                mnSites.setStyle("-fx-text-fill:black; -fx-background-color:  #CAC3C2;");
                break;
            case 8:
                mnBalises.setStyle("-fx-text-fill:black; -fx-background-color:  #CAC3C2;");
                break;
            case 9:
                mnEspaces.setStyle("-fx-text-fill:black; -fx-background-color:  #CAC3C2;");
                break;
            case 10:
                mnPhotos.setStyle("-fx-text-fill:black; -fx-background-color:  #CAC3C2;");
                break;
            case 11:
                mnCarto.setStyle("-fx-text-fill:black; -fx-background-color:  #CAC3C2;");
                break;           
            default:
                throw new AssertionError();
        }        
    }
    
    public void changeCarnetView()  {  
        switchMenu(1);
        mainApp.showCarnetOverview();
    }
    
    private void winTraduction() {         
        mnCarnet.setText(i18n.tr("Carnet"));
        mnGPS.setText(i18n.tr("Import GPS")); 
        mnImport.setText(i18n.tr("Import disque"));
        mnTrace.setText(i18n.tr("Trace externe"));
        mnCalendrier.setText(i18n.tr("Calendrier"));
        mnStat.setText(i18n.tr("Statistiques"));
        mnSites.setText(i18n.tr("Sites"));
        mnBalises.setText(i18n.tr("Balises"));
        mnEspaces.setText(i18n.tr("Espaces aériens"));
        mnPhotos.setText(i18n.tr("Photos"));
        mnCarto.setText(i18n.tr("Cartographie"));        
        Tooltip confToolTip = new Tooltip();
        confToolTip.setStyle("-fx-background-color: linear-gradient(#e2ecfe, #99bcfd);");
        confToolTip.setText(i18n.tr("Configuration"));
        btnConfig.setTooltip(confToolTip);
        Tooltip toolsToolTip = new Tooltip();
        toolsToolTip.setStyle("-fx-background-color: linear-gradient(#e2ecfe, #99bcfd);");        
        toolsToolTip.setText(i18n.tr("Outils"));
        btnTools.setTooltip(toolsToolTip);
        Tooltip supToolTip = new Tooltip();
        supToolTip.setStyle("-fx-background-color: linear-gradient(#e2ecfe, #99bcfd);");        
        supToolTip.setText(i18n.tr("Support"));
        btnSupport.setTooltip(supToolTip);
    }    
     
}
