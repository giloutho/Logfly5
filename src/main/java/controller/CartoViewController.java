/*
 * Copyright Gil THOMAS
 * This file forms an integral part of Logfly project
 * See the LICENSE file distributed with source code
 * for details of Logfly licence project
 */
package controller;

import Logfly.Main;
import dialogues.alertbox;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.Button;
import javafx.scene.image.WritableImage;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import javax.imageio.ImageIO;
import leaflet.map_carto;
import leaflet.map_carto_gpx;
import leaflet.map_carto_igc;
import littlewins.winFileChoose;
import littlewins.winMail;
import littlewins.winTrackFile;
import org.xnap.commons.i18n.I18n;
import settings.configProg;
import settings.fileType;
import systemio.mylogging;
import trackgps.simpleGPX;
import trackgps.traceGPS;

/**
 *
 * @author gil
 */
public class CartoViewController {
    
    @FXML
    private Button btMesure;  
    @FXML
    private Button btTrace;    
    @FXML
    private Button btReset;     
    @FXML
    private Button btHtml;    
    @FXML
    private Button btMail;        
    @FXML
    private Button btClose;    
    @FXML
    private WebView viewMap;     
    
    private boolean dispLegend = true;           
    // Localization
    private I18n i18n; 
    private Stage cartoStage;    
    private RootLayoutController rootController; 
    
    // Reference to the main application.
    private Main mainApp;    
    // Settings
    configProg myConfig;
    StringBuilder sbError;
    String RC = "\n";    
    traceGPS igcTrack;
    simpleGPX gpxTrack;
    String trackFileName;
    int typeTrack;     // 1 IGC  2 GPX
    private File snapFile;  
    private String screenHtml;
    private String lastDirUsed;
    
        
    @FXML
    public void initialize() {
   
    }             
    
    @FXML
    private void showMeasure(ActionEvent event) {
        viewMap.getEngine().executeScript("startMeasure()");
    }        
    
    @FXML
    private void handleTrack(ActionEvent event) {
        
        alertbox aError = new alertbox(myConfig.getLocale());
        winFileChoose wf = new winFileChoose(myConfig, i18n, fileType.GpxIgc, lastDirUsed);  
        File selectedFile = wf.getSelectedFile();
        if (selectedFile != null && selectedFile.exists()) {
            lastDirUsed = selectedFile.getParent();
            try {
                String fileExt = wf.getExtFormat().toUpperCase();
                switch (fileExt) {
                    case "IGC":
                        igcTrack = new traceGPS(selectedFile,true, myConfig);   
                        if (igcTrack.isDecodage()) {  
                            showIGC();
                        }                     
                        break;
                    case "GPX":
                        gpxTrack = new simpleGPX(selectedFile,myConfig);   
                        if (gpxTrack.isDecodage()) {  
                            showGPX();
                        }                     
                        break;
                } 
            } catch (Exception ex) {
                sbError = new StringBuilder(this.getClass().getName()+"."+Thread.currentThread().getStackTrace()[1].getMethodName());
                sbError.append("\r\n").append(ex.toString());
                mylogging.log(Level.SEVERE, sbError.toString());  
                aError = new alertbox(myConfig.getLocale());
                aError.alertError(ex.getClass().getName() + ": " + ex.getMessage());                
            }                       
        }                   
   }          
    
    @FXML
    private void handleReset(ActionEvent event) {
        iniMap();
    }
    
    @FXML
    private void recHTML(ActionEvent event) {
        winTrackFile wHtml = new winTrackFile(screenHtml); 
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
        // get a handle to the stage
        Stage stage = (Stage) btClose.getScene().getWindow();
        stage.close();  
    }    
    
    private void iniMap() {
        
        String sCoord = null;       
        int idxMap = 0;
        trackFileName = null;
        
        if (myConfig.getFinderLat() != null && myConfig.getFinderLong() != null) {
            try {
                double testLat = Double.parseDouble(myConfig.getFinderLat());     
                double testLong = Double.parseDouble(myConfig.getFinderLong()); 
                sCoord = myConfig.getFinderLat()+","+myConfig.getFinderLong();
                idxMap = myConfig.getIdxMap();
            } catch (Exception e) {
                // settings not valid, we put Annecy Lake
                sCoord = "45.863,6.1725"; 
                idxMap = 0;
            }                         
        }
                 
        map_carto mCarto = new map_carto(i18n,idxMap, sCoord); 
                                                  
        if (mCarto.isMap_OK()) {
            //viewMap.getEngine().setUserAgent(" Mozilla/5.0 (Windows NT x.y; Win64; x64; rv:10.0) Gecko/20100101 Firefox/10.0");  
            screenHtml = mCarto.getMap_HTML();
            viewMap.getEngine().loadContent(screenHtml,"text/html");   
        } else {            
            alertbox aError = new alertbox(myConfig.getLocale());
            aError.alertNumError(mCarto.getErrorCode());            
        }       
    }
    
    private void showIGC() {
        System.out.println(igcTrack.getLatMini());
        System.out.println(igcTrack.getLongMini());
        System.out.println(igcTrack.getLatMaxi());
        System.out.println(igcTrack.getLongMaxi());
        map_carto_igc  mapTrack = new map_carto_igc(igcTrack, myConfig.getIdxMap(), i18n); 
        if (mapTrack.isMap_OK()) {       
            screenHtml = mapTrack.getMap_HTML();
            viewMap.getEngine().loadContent(screenHtml);         
        } else {            
            alertbox aError = new alertbox(myConfig.getLocale());
            aError.alertNumError(mapTrack.getErrorCode());            
        }
    }
    
    private void showGPX() {
        
        map_carto_gpx  mapTrack = new map_carto_gpx(gpxTrack, myConfig.getIdxMap(), i18n); 
        if (mapTrack.isMap_OK()) {       
            screenHtml = mapTrack.getMap_HTML();
            viewMap.getEngine().loadContent(screenHtml);         
        } else {            
            alertbox aError = new alertbox(myConfig.getLocale());
            aError.alertNumError(mapTrack.getErrorCode());            
        }        
        
    }
    
    private boolean snapshot() {
        
        boolean res = false;
        
        // code from http://java-buddy.blogspot.fr/2012/12/take-snapshot-of-node-with-javafx.html
        WritableImage snapImage = viewMap.snapshot(new SnapshotParameters(), null);

        // code from http://java-buddy.blogspot.fr/2012/12/save-writableimage-to-file.html
        try {     
            String fileName = "logfly.png";;
            if (trackFileName != null) fileName = trackFileName+".png";                    
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
     * Sets the stage of this Viewer.
     *
     * @param pCartoStage
     */
    public void setCartoStage(Stage pCartoStage) {
        this.cartoStage = pCartoStage;
    }      
    
    /**
     * Set a communication bridge with RootViewController 
     * @param callExterne 
     */
    public void setRootBridge(RootLayoutController callRoot)  {
        this.rootController = callRoot;     
    }   
    
    public void setWinMax()  {           
        cartoStage.setMaximized(true);
    }   
    
    public void setMainApp(Main mainApp) {
        this.mainApp = mainApp; 
        myConfig = mainApp.myConfig;
        i18n = myConfig.getI18n();
        winTraduction();
        iniMap();
        cartoStage.setOnHiding( event -> {
            rootController.switchMenu(1);
            rootController.mainApp.showCarnetOverview();
        });          
    }     
    
    private void winTraduction() {
        btMesure.setText(i18n.tr("Measure"));    
        btTrace.setText(i18n.tr("Track"));    
        btReset.setText(i18n.tr("Reset"));    
        btHtml.setText("HTML");
        btMail.setText(i18n.tr("Mail"));
        btClose.setText(i18n.tr("Close"));
    }      
}    
   
