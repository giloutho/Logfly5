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
import java.util.ArrayList;
import org.xnap.commons.i18n.I18n;

/**
 *
 * @author gil
 * HTML generation of a leaflet map with markers
 */
public class map_X_markers {
    
    private String map_HTML;
    private static final String RC = "\n";
    private I18n i18n; 
    private static String jsLayer;
    private static int idxMap;
    DecimalFormat decimalFormat;
    private static StringBuilder jsTabPoints;
    private static StringBuilder jsInfo;    
    private ArrayList<pointIGC> pointsList;
    
    public map_X_markers(I18n pI18n, int numMap)  {
        map_HTML = null;
        this.i18n = pI18n;
        idxMap = numMap;
        DecimalFormatSymbols decimalFormatSymbols = new DecimalFormatSymbols();
        decimalFormatSymbols.setDecimalSeparator('.');        
        decimalFormat = new DecimalFormat("###.00000", decimalFormatSymbols);     
        jsTabPoints = new StringBuilder();
        jsInfo = new StringBuilder();
    }
        
    public String getMap_HTML() {
        return map_HTML;
    }

        
    public void setPointsList(ArrayList<pointIGC> pListe) {
        this.pointsList = pListe;
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
    
    private void genData() {
        String commentOk;
        
        for(pointIGC onePoint : pointsList) {   
            jsTabPoints.append("    tabPoints.push(new L.LatLng(");
            jsTabPoints.append(decimalFormat.format(onePoint.Latitude)).append(",").append(decimalFormat.format(onePoint.Longitude)).append(")); ").append(RC);
            if (onePoint.Comment != null && !onePoint.Comment.equals("")) {
                commentOk = onePoint.Comment.replace("'", "\\'"); 
                jsInfo.append("    infoPoints.push('").append(commentOk).append("'); ").append(RC);
            } 
        }
    }
    
    
    /**
     * Generation of HTML code of the map
     * @return 
     */
    public int genMap() {
        int res = -1;
        StringBuilder sbHTML = new StringBuilder();
        
        try {
            try  {
                BufferedReader br = new BufferedReader(new InputStreamReader(map_visu.class.getResourceAsStream("/skl/skl_x_markers.txt")));
                String line = null;            
                while ((line = br.readLine()) != null) {
                    sbHTML.append(line).append(RC);                    
                }
                br.close();
            } catch (IOException e) {
                res = 8;    // Unabel to load the resource
            }
            if (sbHTML.length() > 500)  {
                genData();
                String beginHTML = sbHTML.toString();
                String pointsHTML = beginHTML.replace("%tabPoints%", jsTabPoints.toString());
                String infosHTML = pointsHTML.replace("%infoPoints%", jsInfo.toString());
                genDefaultLayer();
                String layerHTML = infosHTML.replace("%layer%", jsLayer);                    
                map_HTML = layerHTML; 
                res = 0;    
            }
        } catch (Exception e) {
            res = -1;
        } 
        
        return res;
    }
    
}
