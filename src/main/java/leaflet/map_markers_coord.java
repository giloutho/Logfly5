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
import trackgps.traceGPS;

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
    private StringBuilder jsTabPoints;
    private traceGPS currTrace;
    
    public boolean isMap_OK() {
        return map_OK;
    }

    public String getMap_HTML() {
        return map_HTML;
    }    
    
    public map_markers_coord(I18n pI18n, int numMap, pointIGC pPoint, traceGPS pTrack) {
        map_HTML = null;
        this.i18n = pI18n;
        this.pointMarker = pPoint;
        this.idxMap = numMap;
        this.currTrace = pTrack;
        DecimalFormatSymbols decimalFormatSymbols = new DecimalFormatSymbols();
        decimalFormatSymbols.setDecimalSeparator('.');        
        decimalFormat = new DecimalFormat("###.00000", decimalFormatSymbols);          
        genMap();
    }
    
    /**
     * HTML generation of track data
     * @param tracePM
     * @return 
     */    
    private boolean genData()  {
        
        Boolean res = false;
        DecimalFormatSymbols decimalFormatSymbols = new DecimalFormatSymbols();
        decimalFormatSymbols.setDecimalSeparator('.');        
        DecimalFormat decimalFormat = new DecimalFormat("###.00000", decimalFormatSymbols);
        
        int step;
        int totPoints = currTrace.Tb_Good_Points.size();

        if (totPoints > 200)  {
            step = totPoints / 200;
        }  else  {
            step = 1;
        }
        
        for(int i = 1; i<=totPoints; i = i+step)
        {
            pointIGC currPoint = currTrace.Tb_Good_Points.get(i-1);
            jsTabPoints.append("      tabPoints.push(new L.LatLng(");
            jsTabPoints.append(decimalFormat.format(currPoint.Latitude));
            jsTabPoints.append(",");
            jsTabPoints.append(decimalFormat.format(currPoint.Longitude));
            jsTabPoints.append(")); ");
            jsTabPoints.append(RC); 
        }      
        
        if (jsTabPoints.length() > 100) res = true;

        return res;
    }    
    
    private String genTrackDisplay() {
        StringBuilder jsTrackLayer = new StringBuilder();
        
        jsTrackLayer.append("    var trackOptions = {").append(RC); 
        jsTrackLayer.append("        color: 'red',").append(RC); 
        jsTrackLayer.append("        weight: 2,").append(RC); 
        jsTrackLayer.append("        opacity: 0.85").append(RC); 
        jsTrackLayer.append("    };").append(RC).append(RC);  
        jsTrackLayer.append("    var track = new L.Polyline(tabPoints, trackOptions);").append(RC).append(RC); 
        jsTrackLayer.append("    map.addLayer(track);").append(RC).append(RC);  
        jsTrackLayer.append("    map.fitBounds(track.getBounds());").append(RC);  
        
        return jsTrackLayer.toString();
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
    
    private String genIniMap() {
        
        StringBuilder sb = new StringBuilder();
        
        if (currTrace != null) {
            sb.append("    var map = new L.Map('map');").append(RC);  
            sb.append("    // Zoom stuff").append(RC);  
            sb.append("    $(\"#zoom\").html(map.getZoom());").append(RC);  
            sb.append("    map.on('zoomend', function(e) {").append(RC);  
            sb.append("        $(\"#zoom\").html(map.getZoom());").append(RC);  
            sb.append("    });").append(RC);              
        } else {
            sb.append("    var map = L.map('map').setView([%coord%], 12);").append(RC);  
        }
        
        return sb.toString();
        
    }
    
    /**
     * Generation of HTML code of the map
     * @return 
     */
    public void genMap() {

        jsTabPoints = new StringBuilder();
        String iniMap = genIniMap();
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
                if (currTrace != null) {
                    if (genData()) {
                        String markerCoord = layerHTML.replace("%coord%", sbCoord.toString()); 
                        String pointsTab = markerCoord.replace("//%tabPoints%", jsTabPoints.toString());
                        String sIniMap = pointsTab.replace("%inimap%", iniMap); 
                        map_HTML = sIniMap.replace("//%trackDisplay%",genTrackDisplay());
                        map_OK = true;
                    }
                } else {
                    String iniCoord = iniMap.replace("%coord%", sbCoord.toString()); 
                    String sIniMap = layerHTML.replace("%inimap%", iniCoord); 
                    map_HTML = sIniMap.replace("%coord%", sbCoord.toString()); 
                    map_OK = true;
                }
            }
        } catch (Exception e) {
            map_OK = false;
        }         
    }    
    
}
