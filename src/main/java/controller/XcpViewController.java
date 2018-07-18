/*
 * Copyright Gil THOMAS
 * This file forms an integral part of Logfly project
 * See the LICENSE file distributed with source code
 * for details of Logfly licence project
 */
package controller;

import Logfly.Main;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import org.xnap.commons.i18n.I18n;
import org.xnap.commons.i18n.I18nFactory;
import settings.configProg;

/**
 *
 * @author gil
 */
public class XcpViewController {
    
    @FXML
    private Label lbSecteur; 
    @FXML
    private Button btGo;
    @FXML
    private Button btRead;
    @FXML
    private Button btSave;
    @FXML
    private Button btClose;
    @FXML
    private TextField txLocality;
    @FXML
    private WebView xcView;    
    private WebEngine eng;
    
        // Reference to the main application.
    private Main mainApp;
    
    // Localization
    private I18n i18n; 
    
    // Settings
    configProg myConfig;
    StringBuilder sbError;
    String RC = "\n";
    
    private String urlBaseXC;
    private String urlCircuit;
    private String urlXC;    

    @FXML
    private void initialize() {
        
    }   
    
    @FXML
    private void handleGo(ActionEvent event) {
        String locality = txLocality.getText().trim();
        if (locality != null && !locality.equals("")) {       
            xcView.setVisible(true);
            StringBuilder sbUrl = new StringBuilder();
            sbUrl.append(urlBaseXC);
            sbUrl.append("?location=").append(locality);
            // Dépendra de la choice box
            urlCircuit = "cfd3c";
            sbUrl.append("&flightType=").append(urlCircuit);                       
            urlXC = sbUrl.toString();
            eng.load(urlXC);   
        }
    }    
    
    @FXML
    private void handleClose() {
        // get a handle to the stage
        Stage stage = (Stage) btClose.getScene().getWindow();
        stage.close();          
    }
    
    @FXML
    private void handleSave() {
        
    }    
    
    /**
     * Is called by the main application to give a reference back to itself.
     * 
     * @param mainApp
     */
    public void setMainApp(Main mainApp) {
        this.mainApp = mainApp; 
        myConfig = mainApp.myConfig;
        i18n = I18nFactory.getI18n("","lang/Messages",SitesViewController.class.getClass().getClassLoader(),myConfig.getLocale(),0);
        winTraduction();
        this.mainApp.rootLayoutController.updateMsgBar("", false, 50); 
    }  
    
        /**
    * Translate labels of the window
    */
    private void winTraduction() {  
        lbSecteur.setText(i18n.tr("Secteur"));
        btRead.setText(i18n.tr("Lire"));
        btSave.setText(i18n.tr("Enregistrer"));
        btClose.setText(i18n.tr("Fermer"));        
    }
}
