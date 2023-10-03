/*
 * Copyright Gil THOMAS
 * This file forms an integral part of Logfly project
 * See the LICENSE file distributed with source code
 * for details of Logfly licence project
 */
package leaflet;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import org.xnap.commons.i18n.I18n;
import trackgps.traceGPS;

/**
 *
 * @author gil
 */
public class map_carto_igc {
    
    private boolean map_OK;
    private int errorCode;
    private String map_HTML;
    private String jsLayer;      
    private int idxMap;
    private I18n i18n;    
    private StringBuilder sbError; 
    private final String RC = "\n";
    private String strGeoJson;
    
    public map_carto_igc(traceGPS extTrace, int numMap, I18n currLang)
    {    
        map_HTML = null;
        map_OK = false;        
        idxMap = numMap;
        i18n = currLang;      
        strGeoJson = extTrace.getGeoJson();
        if (strGeoJson != null) 
            genMap();
        else
            errorCode = 1370; // Erreur de génération GeoJson
    }
    
    public boolean isMap_OK() {
        return map_OK;
    }

    public String getMap_HTML() {
        return map_HTML;
    }

    public int getErrorCode() {
        return errorCode;
    }    
    
    /**
     * Default layer of the map
     */
    private void genDefaultLayer() {
         
        // We put a default value to avoid an undefined case    
        if(idxMap == 0) {
            jsLayer = "    osmlayer.addTo(map);";
        } else if (idxMap ==1) { 
            jsLayer = "    OpenTopoMap.addTo(map);";
        } else if (idxMap ==2) {
            jsLayer = "    mtklayer.addTo(map);";
        } else if (idxMap ==3) {
            jsLayer = "    fouryoulayer.addTo(map);";
        } else {
            jsLayer = "    OpenTopoMap.addTo(map);";
        }        
    }        
    
    private void genMap() {

        StringBuilder sbHTML = new StringBuilder();
        
        try {
            try  {
                BufferedReader br = new BufferedReader(new InputStreamReader(map_carto.class.getResourceAsStream("/skl/skl_carto_track.txt")));
                String line = null;            
                while ((line = br.readLine()) != null) {
                    sbHTML.append(line).append(RC);                    
                }
                br.close();
            } catch (IOException e) {
                sbError = new StringBuilder(this.getClass().getName()+"."+Thread.currentThread().getStackTrace()[1].getMethodName());
                sbError.append("\r\n").append(e.getMessage());
                System.out.println(sbError.toString());
               // mylogging.log(Level.SEVERE, sbError.toString()); 
            }
            if (sbHTML.length() > 500)  {
                genDefaultLayer();
                String layerHTML = sbHTML.toString().replace("%layer%", jsLayer);   
                StringBuilder sbJson = new StringBuilder();
                sbJson.append("    var geojson = ").append(strGeoJson).append(";");
                map_HTML = layerHTML.replace("%geojson%", sbJson.toString());     
                map_OK = true;
            }
        } catch (Exception e) {
            map_OK = false;
        }         
    }         
}
