/*
 * Copyright Gil THOMAS
 * This file forms an integral part of Logfly project
 * See the LICENSE file distributed with source code
 * for details of Logfly licence project
 */
package littlewins;

import javafx.scene.Scene;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.StackPane;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Modality;
import javafx.stage.Stage;
import leaflet.map_photo_one;
import org.xnap.commons.i18n.I18n;
import org.xnap.commons.i18n.I18nFactory;
import settings.configProg;
import trackgps.traceGPS;

/**
 *
 * @author gil
 */
public class winMapPhotoPoint {
    
    private configProg myConfig;
    private I18n i18n; 
    private traceGPS currTrace;
    private int idxPoint;
    private map_photo_one visuMap;
    private int backPoint;

    public winMapPhotoPoint(configProg currConfig, traceGPS pTrace, int pIdx, String phPath) {
        // it will be useful if we choose url according to current language
        myConfig = currConfig;
        i18n = I18nFactory.getI18n("","lang/Messages",winLog.class.getClass().getClassLoader(),myConfig.getLocale(),0);    
        currTrace = pTrace;
        idxPoint = pIdx;
        backPoint = -1;
        visuMap = new map_photo_one(currTrace, idxPoint, phPath, myConfig.getIdxMap(), i18n);
        showBrowser();
    }       

    public int getBackPoint() {
        return backPoint;
    }
            
    private void showBrowser() {
        
        Stage subStage = new Stage();
        WebView wv = new WebView();
        WebEngine weng = wv.getEngine();
        weng.setJavaScriptEnabled(true);
        
        weng.titleProperty().addListener( (observable, oldValue, newValue) -> {
            if(newValue != null && !newValue.isEmpty() && !newValue.equals("Leaflet"))
                decodeCoord(newValue);
        });     
        StackPane root = new StackPane();
        root.getChildren().add(wv);
         if (visuMap.isMap_OK()) {
            String sDebug = visuMap.getMap_HTML();
            final Clipboard clipboard = Clipboard.getSystemClipboard();
            final ClipboardContent content = new ClipboardContent();
            content.putString(visuMap.getMap_HTML());            
            clipboard.setContent(content);               
            wv.getEngine().loadContent(visuMap.getMap_HTML());             
         } else
            wv.getEngine().load("http://logfly.org/download/erreur/erreur2.html"); // it's really fucked
        Scene scene = new Scene(root, 1000, 650);
       
        subStage.setTitle(i18n.tr("Click on timeline to place the photo marker"));
        subStage.initModality(Modality.APPLICATION_MODAL);       
        subStage.setScene(scene);
        subStage.showAndWait();
        
    }    
    
    private void decodeCoord(String sValue) {
        try {
            backPoint = Integer.parseInt(sValue.trim());
        } catch (Exception e) {
            backPoint = 0;
        }        
    }        
    
}
