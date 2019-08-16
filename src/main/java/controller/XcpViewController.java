/*
 * Copyright Gil THOMAS
 * This file forms an integral part of Logfly project
 * See the LICENSE file distributed with source code
 * for details of Logfly licence project
 */
package controller;

import Logfly.Main;
import dialogues.alertbox;
import geoutils.geonominatim;
import java.io.File;
import java.io.FileReader;
import java.io.Reader;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.logging.Level;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.paint.Paint;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import littlewins.winFileChoose;
import littlewins.winOsmCities;
import littlewins.winSaveXcp;
import model.Sitemodel;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.xnap.commons.i18n.I18n;
import org.xnap.commons.i18n.I18nFactory;
import settings.configProg;
import settings.fileType;
import settings.privateData;
import systemio.mylogging;

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
    private DecimalFormatSymbols decimalFormatSymbols = new DecimalFormatSymbols();     
    private DecimalFormat df2;
    private DecimalFormat df3;    
    
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
        
        txLocality.setOnKeyPressed(new EventHandler<KeyEvent>() {              
                @Override
                public void handle(KeyEvent t) {
                    if (t.getCode() == KeyCode.ENTER) {
                        String locality = txLocality.getText().trim();
                        if (locality != null && !locality.equals("")) { 
                            txLocality.setStyle("-fx-control-inner-background: #"+colorGoodValue.toString().substring(2));  
                            getCoordinate(locality);
                        } else {
                            btSave.setVisible(false);
                            txLocality.setStyle("-fx-control-inner-background: #"+colorBadValue.toString().toString().substring(2));
                            txLocality.requestFocus();  
                        }
                    } 
                }
            });        
    }   
    
    @FXML
    private void handleGo(ActionEvent event) {
        if (eng != null) {   
            eng.executeScript("XCHere()");
        } else {
            sbError = new StringBuilder(this.getClass().getName()+"."+Thread.currentThread().getStackTrace()[1].getMethodName());
            sbError.append("eng is null\r\n");
            mylogging.log(Level.SEVERE, sbError.toString());  
        }    
    }    
    
    @FXML
    private void handleRead() {
        // il faudra ajouter http://alpidev.com/xcplanner/  et le symbole ?
        winFileChoose wf = new winFileChoose(myConfig, i18n, fileType.xcp, null);  
        File selectedFile = wf.getSelectedFile();
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
                    System.out.println(sbUrl.toString());
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
    
    private void iniMap() {
        
        String sLat;
        String sLong;
        String sIndex;
        String sContest;
        
        if (myConfig.getFinderLat() != null && myConfig.getFinderLong() != null) {
            sLat = myConfig.getFinderLat();
            sLong = myConfig.getFinderLong();
        } else {
            // settings not valid, we put Annecy Lake
            sLat = "45.863";
            sLong = "6.1725";
        }
        xcView.setVisible(true);
        StringBuilder sbUrl = new StringBuilder();
        sbUrl.append(urlBaseXC);
        if (myConfig.getLocale() == java.util.Locale.FRENCH) {
            sIndex = "index_fr.php";
            urlCircuit = "cfd3c";
        } else {
            sIndex = "index.php";
            urlCircuit = "xc3c";
        }
        sbUrl.append(sIndex).append("?&start=%5B").append(myConfig.getFinderLat()).append(",").append(myConfig.getFinderLong()).append("%5D");
        sbUrl.append("&flightType=").append(urlCircuit);                       
        urlXC = sbUrl.toString();
        eng.load(urlXC);   
        btSave.setVisible(true);           
    }
    
    private void getCoordinate(String pLocality) {
        
        geonominatim debGeo = new geonominatim();         
        debGeo.askGeo(pLocality.trim());
        ObservableList<Sitemodel> osmCities = debGeo.getOsmTowns(); 
        int lsSize = osmCities.size();         
        if (lsSize > 0) {
            winOsmCities wCities = new winOsmCities(i18n, osmCities, this);  
        } else {
            alertbox aError = new alertbox(myConfig.getLocale());
            aError.alertNumError(debGeo.getGeoError());
        }        
    }
    
    public void returnFromOsmCities(Sitemodel pSelectedCity) {
        
        decimalFormatSymbols.setDecimalSeparator('.');  
        df2 = new DecimalFormat("#0.0000", decimalFormatSymbols);
        df3 = new DecimalFormat("##0.0000", decimalFormatSymbols);
        
        try {
            if (pSelectedCity.getLatitude() != null && pSelectedCity.getLongitude() != null) {
                String sLat = df2.format(pSelectedCity.getLatitude());
                String sLong = df3.format(pSelectedCity.getLongitude());           
                xcView.setVisible(true);
                StringBuilder sbUrl = new StringBuilder();
                sbUrl.append(urlBaseXC);
                sbUrl.append("?&start=%5B").append(sLat).append(",").append(sLong).append("%5D");
                urlCircuit = "cfd3c";
                sbUrl.append("&flightType=").append(urlCircuit);                       
                urlXC = sbUrl.toString();
                eng.load(urlXC);   
                btSave.setVisible(true);                
            } else {
                alertbox aError = new alertbox(myConfig.getLocale());
                aError.alertNumError(320);  // Unusable response from web service           
            }
        } catch ( Exception e) {
            alertbox aError = new alertbox(myConfig.getLocale());
            aError.alertNumError(320);  // Unusable response from web service              
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
        i18n = I18nFactory.getI18n("","lang/Messages",XcpViewController.class.getClass().getClassLoader(),myConfig.getLocale(),0);
        winTraduction();
        this.mainApp.rootLayoutController.updateMsgBar("", false, 50); 
        // To return first option  
        xcpStage.setOnHiding( event -> {
            rootController.switchMenu(1);
            rootController.mainApp.showCarnetOverview();
        });     
        iniMap();
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
