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
import java.time.format.DateTimeFormatter;
import java.util.regex.Pattern;
import org.xnap.commons.i18n.I18n;
import trackgps.traceGPS;

/**
 *
 * @author gil
 */
public class map_photo_one {
    
    private boolean map_OK;
    private int errorCode;
    private String map_HTML;   
    private final String RC = "\n";
    private boolean graphAltiBaro = false;    
    private StringBuilder jsTabPoints;
    private StringBuilder jsaltiLg ;
    private StringBuilder jsaltiVal;
    private StringBuilder jsHeure;
    private StringBuilder jsMarker;
    private StringBuilder jsInfoMsg;
    private String jsLayer;
    private String photoPath;
    private int idxMap;
    private int idxPoint;
    private I18n i18n;    
    private DateTimeFormatter dtfHHmm = DateTimeFormatter.ofPattern("HH:mm");
    private DecimalFormat fmtsigne = new DecimalFormat("+#0.00;-#");
    DecimalFormat decimalFormat;
    
    public boolean isMap_OK() {
        return map_OK;
    }

    public String getMap_HTML() {
        return map_HTML;
    }

    public int getErrorCode() {
        return errorCode;
    }  

    public map_photo_one(traceGPS pTrace, int pIdx, String phPath, int numMap, I18n currLang)
    {
        map_HTML = null;
        map_OK = false;
        idxMap = numMap;
        idxPoint = pIdx;
        i18n = currLang;
        photoPath = phPath;
        jsTabPoints = new StringBuilder();
        jsaltiLg = new StringBuilder();
        jsaltiVal = new StringBuilder();
        jsHeure = new StringBuilder();  
        jsMarker = new StringBuilder();
        jsInfoMsg = new StringBuilder();
        DecimalFormatSymbols decimalFormatSymbols = new DecimalFormatSymbols();
        decimalFormatSymbols.setDecimalSeparator('.');        
        decimalFormat = new DecimalFormat("###.00000", decimalFormatSymbols);         
        genMap(pTrace);       
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
    
    /**
     * HTML generation of track data
     * @param tracePM
     * @return 
     */    
    private boolean genData(traceGPS tracePh)  {
        
        Boolean res = false;
        String sNb;      
        pointIGC pointMarker;        
        
        int step;
        int totPoints = tracePh.Tb_Good_Points.size();
        
//        // Be careful with big tracklogs, we reduced total number of points 
//        if (totPoints > 1500)  {
//            step = totPoints / 1500;
//        }  else  {
//            step = 1;
//        }               
        step= 1;
        // Checking of double cast to string
        Pattern DOUBLE = Pattern.compile("\\d");    
        if (idxPoint > 0 ) {
            pointMarker = tracePh.Tb_Good_Points.get(idxPoint);           
        } else {
            pointMarker = new pointIGC();
            double dLat = tracePh.getLatMini() + ((tracePh.getLatMaxi() - tracePh.getLatMini())/2);
            double dLong = tracePh.getLongMini() + ((tracePh.getLongMaxi() - tracePh.getLongMini())/2);
            pointMarker.setLatitude(dLat);
            pointMarker.setLongitude(dLong);            
        }                
                        
        jsMarker.append("var markerPhoto = L.marker([").append(RC);
        jsMarker.append(decimalFormat.format(pointMarker.Latitude)).append(",").append(decimalFormat.format(pointMarker.Longitude));
        jsMarker.append("],{icon: iconPhoto}).addTo(map).on('click', onClickMarker);").append(RC).append(RC);
        jsMarker.append("function onClickMarker(e) {").append(RC);
        jsMarker.append("    var popup = e.target.getPopup();").append(RC);       
        jsMarker.append("    var content = {'class' : 'fancybox','href': 'file://localhost/").append(photoPath).append("'};").append(RC);
        jsMarker.append("    $.fancybox.open([content], {padding : 0 });").append(RC);
        jsMarker.append("    map.closePopup();").append(RC);
        jsMarker.append("}").append(RC);       
        
        jsInfoMsg.append("var infoMsg = '").append(i18n.tr("Click on timeline")).append("<br />");
        jsInfoMsg.append(i18n.tr("to place the photo marker")).append("';");
        
        for(int i = 1; i<=totPoints; i = i+step)
        {
            pointIGC currPoint = tracePh.Tb_Good_Points.get(i-1);
            jsTabPoints.append("      tabPoints.push(new L.LatLng(");
            jsTabPoints.append(decimalFormat.format(currPoint.Latitude)).append(",").append(decimalFormat.format(currPoint.Longitude)).append(")); ").append(RC);
            jsaltiLg.append("'").append(currPoint.dHeure.format(dtfHHmm)).append("',");
            jsaltiVal.append("0,");                                 
            jsHeure.append("'").append(currPoint.dHeure.format(DateTimeFormatter.ISO_TIME)).append("',");                    
        }
        
        // last comma is removed
        if (jsaltiLg.length() > 0 ) {
            jsaltiLg.setLength(jsaltiLg.length() - 1);
            res = true;
        }
        if ( res && jsaltiVal.length() > 0 ) {
            jsaltiVal.setLength(jsaltiVal.length() - 1);
        }  else res = false;
        if ( res && jsHeure.length() > 0 ) {
            jsHeure.setLength(jsHeure.length() - 1);
        }  else res = false;
        
        return res;

    }    

    /**
     * HTML generation for a little map with a simplified track
     * @param tracePM 
     */
    public void genMap(traceGPS pTrace)  {
        StringBuilder sbHTML = new StringBuilder();
        
        try {
            try  {
                    BufferedReader br = new BufferedReader(new InputStreamReader(map_visu.class.getResourceAsStream("/skl/skl_photo_one.txt")));                     
                    String line = null;            
                    while ((line = br.readLine()) != null) {
                        sbHTML.append(line).append(RC);                    
                    }
                    br.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (sbHTML.length() > 1000 && genData(pTrace))  {  
                String beginHTML = sbHTML.toString();
                String pointsHTML = beginHTML.replace("%tabPoints%", jsTabPoints.toString());   
                String altiLgHTML = pointsHTML.replace("%altiLg%", jsaltiLg.toString());
                String altiValHTML = altiLgHTML.replace("%altiVal%", jsaltiVal.toString());
                String heureHTML = altiValHTML.replace("%Heure%", jsHeure.toString());
                genDefaultLayer();
                String layerHTML = heureHTML.replace("%layer%", jsLayer);  
                String markerHTML = layerHTML.replace("%marker%", jsMarker.toString());
                map_HTML = markerHTML.replace("%infoMsg%", jsInfoMsg.toString());            
                map_OK = true;            
            }            
        } catch (Exception e) {
            map_OK = false;
            errorCode = -1;    // Undefined error
        }            
    }
    
}
