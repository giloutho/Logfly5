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

/**
 *
 * @author gil
 */

public class map_carto {

    private boolean map_OK;
    private int errorCode;
    private String map_HTML;
    private static I18n i18n;
    private final String RC = "\n";
    private StringBuilder sbError;    
    private String jsLayer;   
    private int idxMap;
    
    public map_carto(I18n currLang, int numMap, String pCoord) {
        map_HTML = null;
        map_OK = false;
        this.i18n = currLang;
        this.idxMap = numMap;
        genMap(pCoord);        
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
        } else if (idxMap ==4) {
            jsLayer = "     map.addLayer(googleLayer);";
        } else {
            jsLayer = "    OpenTopoMap.addTo(map);";
        }        
    }    
    
    private void genMap(String startCoord) {

        StringBuilder sbHTML = new StringBuilder();        
        try {
            try  {
                BufferedReader br = new BufferedReader(new InputStreamReader(map_carto.class.getResourceAsStream("/skl/skl_carto.txt")));
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
                map_HTML = layerHTML.replace("%CoordIni%", startCoord);     
                map_OK = true;
            }
        } catch (Exception e) {
            map_OK = false;
        }         
    }          
}

