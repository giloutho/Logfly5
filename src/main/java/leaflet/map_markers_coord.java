/*
 * Copyright Gil THOMAS
 * This file forms an integral part of Logfly project
 * See the LICENSE file distributed with source code
 * for details of Logfly licence project
 */
package leaflet;

import igc.pointIGC;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.logging.Level;
import org.xnap.commons.i18n.I18n;
import systemio.mylogging;

/**
 *
 * @author gil
 */
public class map_markers_coord {
    
    private boolean map_OK;
    private String map_HTML;
    private static final String RC = "\n";
    private I18n i18n; 
    private int idxMap;
    private DecimalFormat decimalFormat;
    private String jsLayer;    
    private pointIGC pointMarker;
    private StringBuilder sbError;
    
    public boolean isMap_OK() {
        return map_OK;
    }

    public String getMap_HTML() {
        return map_HTML;
    }    
    
    public map_markers_coord(I18n pI18n, int numMap, pointIGC pPoint) {
        map_HTML = null;
        this.i18n = pI18n;
        this.pointMarker = pPoint;
        this.idxMap = numMap;
        DecimalFormatSymbols decimalFormatSymbols = new DecimalFormatSymbols();
        decimalFormatSymbols.setDecimalSeparator('.');        
        decimalFormat = new DecimalFormat("###.00000", decimalFormatSymbols);          
        genMap();
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
    
    /**
     * Generation of HTML code of the map
     * @return 
     */
    public void genMap() {

        StringBuilder sbHTML = new StringBuilder();
        StringBuilder sbComment = new StringBuilder();
        String commentOk;
        
        try {
            try  {
                BufferedReader br = new BufferedReader(new InputStreamReader(map_visu.class.getResourceAsStream("/skl/skl_marker_coord.txt")));
                String line = null;            
                while ((line = br.readLine()) != null) {
                    sbHTML.append(line).append(RC);                    
                }
                br.close();
            } catch (IOException e) {
                sbError = new StringBuilder(this.getClass().getName()+"."+Thread.currentThread().getStackTrace()[1].getMethodName());
                sbError.append("\r\n").append(e.getMessage());
                mylogging.log(Level.SEVERE, sbError.toString()); 
            }
            if (sbHTML.length() > 500)  {
                String beginHTML = sbHTML.toString();
                StringBuilder sbCoord = new StringBuilder();
                sbCoord.append(decimalFormat.format(pointMarker.Latitude)).append(",").append(decimalFormat.format(pointMarker.Longitude));
                String pointsHTML = beginHTML.replaceAll("%coord%", sbCoord.toString());
                genDefaultLayer();
                String layerHTML = beginHTML.replace("%layer%", jsLayer);               
                map_HTML = layerHTML.replace("%coord%", sbCoord.toString());     
                map_OK = true;
            }
        } catch (Exception e) {
            map_OK = false;
        }         
    }    
    
}
