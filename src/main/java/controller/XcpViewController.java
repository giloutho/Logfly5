/*
 * Copyright Gil THOMAS
 * This file forms an integral part of Logfly project
 * See the LICENSE file distributed with source code
 * for details of Logfly licence project
 */
package controller;

import Logfly.Main;
import java.io.File;
import java.io.FileReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.logging.Level;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.paint.Paint;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import littlewins.winSaveXcp;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.xnap.commons.i18n.I18n;
import org.xnap.commons.i18n.I18nFactory;
import settings.configProg;
import settings.privateData;
import systemio.mylogging;
import waypio.pointRecord;

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
    private Stage xcpStage;   
    private RootLayoutController rootController; 
    
    private Paint colorBadValue = Paint.valueOf("FA6C04");
    private Paint colorGoodValue = Paint.valueOf("FFFFFF");
    
    private String urlBaseXC;
    private String urlCircuit;
    private String urlXC;    
    private String usedPrefix = null;
    
    
    @FXML
    private void initialize() {
        urlBaseXC = privateData.xcplannerUrl.toString();
        
        eng = xcView.getEngine();
        eng.setUserAgent(" Mozilla/5.0 (Windows NT x.y; Win64; x64; rv:10.0) Gecko/20100101 Firefox/10.0");              

        eng.titleProperty().addListener( (observable, oldValue, newValue) -> {
            if(newValue != null && !newValue.isEmpty() && !newValue.equals("Leaflet"))
                showRecord(newValue);
        });        
    }   
    
    @FXML
    private void handleGo(ActionEvent event) {
        String locality = txLocality.getText().trim();
        if (locality != null && !locality.equals("")) { 
            txLocality.setStyle("-fx-control-inner-background: #"+colorGoodValue.toString().substring(2));  
            xcView.setVisible(true);
            StringBuilder sbUrl = new StringBuilder();
            sbUrl.append(urlBaseXC);
            sbUrl.append("?location=").append(locality);
            // Au départ on pensait mettre une choicebox mais cela fait double emploi avec le script
            // donc on opte pour un triangle par défaut
            urlCircuit = "cfd3c";
            sbUrl.append("&flightType=").append(urlCircuit);                       
            urlXC = sbUrl.toString();
            eng.load(urlXC);   
            btSave.setVisible(true);
        } else {
            btSave.setVisible(false);
            txLocality.setStyle("-fx-control-inner-background: #"+colorBadValue.toString().toString().substring(2));
            txLocality.requestFocus();  
        }
    }    
    
    @FXML
    private void handleRead() {
        // il faudra ajouter http://alpidev.com/xcplanner/  et le symbole ?
        FileChooser fileChooser = new FileChooser();
        FileChooser.ExtensionFilter xcpFilter = new FileChooser.ExtensionFilter(i18n.tr("Track files (*.xcp)"), "*.xcp");
        fileChooser.getExtensionFilters().addAll(xcpFilter);
        File selectedFile = fileChooser.showOpenDialog(null);        
        if(selectedFile != null){    
            try {
                JSONParser parser = new JSONParser();
                Reader reader = new FileReader(selectedFile.getAbsolutePath());

                Object jsonObj = parser.parse(reader);

                JSONObject jsonObject = (JSONObject) jsonObj;

                String sUrl = (String) jsonObject.get("url");
                String sPrefix = (String) jsonObject.get("prefix");              

                if (sUrl != null && sUrl.contains("turnpoints")) {
                    xcView.setVisible(true);
                    StringBuilder sbUrl = new StringBuilder();
                    sbUrl.append(urlBaseXC).append("?").append(sUrl);
                    eng.load(sbUrl.toString());
                    btSave.setVisible(true);                     
                }
                if (sPrefix != null) usedPrefix = sPrefix;
            } catch (Exception e) {
                btSave.setVisible(false);
                sbError = new StringBuilder(this.getClass().getName()+"."+Thread.currentThread().getStackTrace()[1].getMethodName());
                sbError.append("eng is null\r\n");
                mylogging.log(Level.SEVERE, sbError.toString());                
            }
        }         
    }
        
    @FXML
    private void handleSave() {
        if (eng != null) {   
            // we want to update title at each clic
            // We put a timestamp to modify title at each request
            // even if there has been no change in position
            eng.executeScript("updateTile()");
        } else {
            sbError = new StringBuilder(this.getClass().getName()+"."+Thread.currentThread().getStackTrace()[1].getMethodName());
            sbError.append("eng is null\r\n");
            mylogging.log(Level.SEVERE, sbError.toString());  
        }        
    }    
    
    private void showRecord(String sUrl) {
        if (sUrl.contains("turnpoints")) {
            winSaveXcp saveWin = new winSaveXcp(i18n, sUrl, usedPrefix);        
        }
    }    
    
    
    /**
     * Sets the stage of this Viewer.
     *
     * @param pXcpStage
     */
    public void setXcpStage(Stage pXcpStage) {
        this.xcpStage = pXcpStage;
    }        
    
    public void setWinMax()  {           
        xcpStage.setMaximized(true);
    }       
    
    /**
     * Set a communication bridge with RootViewController 
     * @param callExterne 
     */
    public void setRootBridge(RootLayoutController callRoot)  {
        this.rootController = callRoot;     
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
        lbSecteur.setText(i18n.tr("Sector"));
        btRead.setText(i18n.tr("Read"));
        btSave.setText(i18n.tr("Save"));        
    }
}
