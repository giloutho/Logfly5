/*
 * Copyright Gil THOMAS
 * This file forms an integral part of Logfly project
 * See the LICENSE file distributed with source code
 * for details of Logfly licence project
 */
package littlewins;

import igc.pointIGC;
import javafx.scene.Scene;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.StackPane;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Modality;
import javafx.stage.Stage;
import leaflet.map_markers_coord;
import org.xnap.commons.i18n.I18n;
import org.xnap.commons.i18n.I18nFactory;
import settings.configProg;
import trackgps.traceGPS;

/**
 *
 * @author gil
 */
public class winCoord {
    
    private configProg myConfig;
    private I18n i18n; 
    private WebEngine webEngine;    
    private String sHTML = null;
    private String mapLat;
    private String mapLong;
    private String mapAlt;
    private traceGPS currTrace;

    public winCoord(configProg currConfig, String pLat, String pLong, traceGPS pTrack) {
        myConfig = currConfig;
        i18n = I18nFactory.getI18n("","lang/Messages",winLog.class.getClass().getClassLoader(),myConfig.getLocale(),0); 
        mapLat = null;
        mapLong = null;
        mapAlt = null;
        currTrace = pTrack;
        iniMap(pLat,pLong);
        showBrowser();
    }      

    public String getMapLat() {
        return mapLat;
    }

    public String getMapLong() {
        return mapLong;
    }

    public String getMapAlt() {
        return mapAlt;
    }
                    
    private void iniMap(String sLat, String sLong) {
        
        if (sLat != null && sLong != null) {
            try {
                double dLatitude = Double.parseDouble(sLat);     
                double dLongitude = Double.parseDouble(sLong); 
                pointIGC pPoint1 = new pointIGC();             
                if (dLatitude > 90 || dLatitude < -90) dLatitude = 0;
                pPoint1.setLatitude(dLatitude);        
                if (dLongitude > 180 || dLongitude < -180) dLongitude = 0;
                pPoint1.setLongitude(dLongitude);    
                map_markers_coord myMap = new map_markers_coord(i18n, myConfig.getIdxMap(), pPoint1, currTrace); 
                if (myMap.isMap_OK()) {              
                    String sDebug = myMap.getMap_HTML();
                    final Clipboard clipboard = Clipboard.getSystemClipboard();
                    final ClipboardContent content = new ClipboardContent();
                    content.putString(myMap.getMap_HTML());            
                    clipboard.setContent(content);            
                    sHTML = myMap.getMap_HTML(); 
                }                
            } catch (Exception e) {
                sHTML = null;    
            }                         
        }        
    }    
    
    private void showBrowser() {
        
        Stage subStage = new Stage();
        WebView wv = new WebView();
        webEngine = wv.getEngine();
        webEngine.setJavaScriptEnabled(true);
        
        webEngine.titleProperty().addListener( (observable, oldValue, newValue) -> {
            if(newValue != null && !newValue.isEmpty() && !newValue.equals("Leaflet"))
                decodeCoord(newValue);
        });                     
        StackPane root = new StackPane();
        root.getChildren().add(wv);
        if (sHTML != null) webEngine.loadContent(sHTML); 
        Scene scene = new Scene(root, 600, 600);
        subStage.initModality(Modality.APPLICATION_MODAL);  
        subStage.setScene(scene);
        subStage.showAndWait();
                   
    }
    
    private void decodeCoord(String sCoord) {
        String[] tbCoord = sCoord.split(",");
        if (tbCoord.length == 3) {       
            mapLat = tbCoord[0].replaceAll("\\s+","");
            mapLong = tbCoord[1].replaceAll("\\s+","");
            mapAlt = tbCoord[2].replaceAll("\\s+","");  
        }
    }     
    
}
