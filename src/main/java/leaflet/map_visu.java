/* 
 * Copyright Gil THOMAS
 * This file forms an integral part of Logfly project
 * See the LICENSE file distributed with source code
 * for details of Logfly licence project
 */
package leaflet;

import database.dbSearch;
import geoutils.geonominatim;
import igc.pointIGC;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.regex.Pattern;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import trackgps.thermique;
import trackgps.traceGPS;
import org.xnap.commons.i18n.I18n;
import org.xnap.commons.i18n.I18nFactory;
import settings.configProg;
import trackgps.analyse;
import trackgps.remarkable;

/**
 *
 * @author Gil Thomas logfly.org
 * HTML generation of a full screen leaflet map
 */
public class map_visu {
    
    private I18n i18n;
    
    // Paramètres de configuration
    configProg myConfig;
    
    public boolean graphAltiBaro = false;
    
    private boolean map_OK;
    private String map_HTML;
    private String map_HTML_NoScore;
    private final String RC = "\n";  
    private StringBuilder jsaltiLg ;
    private StringBuilder jsaltiVal;
    private StringBuilder jsHeure;
    private StringBuilder jsSpeed;
    private StringBuilder jsTabPoints;
    private StringBuilder jsVario;
    private StringBuilder jsThermique;
    private StringBuilder jsGlide;
    private StringBuilder jsLegende;
    private StringBuilder jsBalises;
    private StringBuilder jsScore;
    private StringBuilder jsPhotos;
    private StringBuilder jsGallery;
    private StringBuilder jsPhotosCode;
    private StringBuilder jsGalleryCode;
    
    private analyse trackAnalyze;
    
    private String jsLayer;
    private String legLeague; 
    private String legShape;
    private String legDistance;
    private String legPoints;
    private double dBestT = 0;
    private DateTimeFormatter dtfHHmm = DateTimeFormatter.ofPattern("HH:mm");
    private DecimalFormat fmtsigne = new DecimalFormat("+#0.00;-#");
    DecimalFormat decimalFormat;

    public boolean isMap_OK() {
        return map_OK;
    }

    public String getMap_HTML() {
        return map_HTML;
    }

    public String getMap_HTML_NoScore() {
        return map_HTML_NoScore;
    }
            
    public map_visu(traceGPS traceVisu, configProg currConfig)
    {
        myConfig = currConfig;
        map_OK = false;
        map_HTML = null;
        jsaltiLg = new StringBuilder();
        jsaltiVal = new StringBuilder();
        jsHeure = new StringBuilder();
        jsSpeed = new StringBuilder();
        jsTabPoints = new StringBuilder();
        jsVario = new StringBuilder();
        jsThermique = new StringBuilder();
        jsGlide = new StringBuilder();
        jsLegende = new StringBuilder();
        jsBalises = new StringBuilder();
        jsScore = new StringBuilder();
        jsPhotos = new StringBuilder();
        jsPhotosCode = new StringBuilder();
        jsGallery = new StringBuilder();
        jsGalleryCode = new StringBuilder();
               
        i18n = I18nFactory.getI18n("","lang/Messages",map_visu.class.getClass().getClassLoader(),myConfig.getLocale(),0);
        
        DecimalFormatSymbols decimalFormatSymbols = new DecimalFormatSymbols();
        decimalFormatSymbols.setDecimalSeparator('.');        
        decimalFormat = new DecimalFormat("###.00000", decimalFormatSymbols);       
        
        trackAnalyze = new analyse(traceVisu);
        
//        for (int i = 0; i < trackAnalyze.finalDives.size(); i++) {
//            System.out.println(trackAnalyze.finalDives.get(i).getHTMLDives(i18n));
//        }
        
        
        carteVisu(traceVisu);       
    } 
    
    /**
     * HTML generation of track data
     * @param traceGPS
     * @return 
     */    
    private boolean genData(traceGPS traceVisu)  {
        
        Boolean res = false;
        String sNb;
                        
        
        int step;
        int totPoints = traceVisu.Tb_Good_Points.size();
        
        // Be careful with big tracklogs, we reduced total number of points 
        if (totPoints > 1500)  {
            step = totPoints / 1500;
        }  else  {
            step = 1;
        }               
        
        // Checking of double cast to string
        Pattern DOUBLE = Pattern.compile("\\d");
        
        for(int i = 1; i<=totPoints; i = i+step)
        {
            pointIGC currPoint = traceVisu.Tb_Good_Points.get(i-1);
            jsTabPoints.append("      tabPoints.push(new L.LatLng(");
            jsTabPoints.append(decimalFormat.format(currPoint.Latitude)).append(",").append(decimalFormat.format(currPoint.Longitude)).append(")); ").append(RC);
            jsaltiLg.append("'").append(currPoint.dHeure.format(dtfHHmm)).append("',");
            if (graphAltiBaro) {
                jsaltiVal.append(String.valueOf(currPoint.AltiBaro)).append(",");
            }  else {
                jsaltiVal.append(String.valueOf(currPoint.AltiGPS)).append(",");
            }
            // locale.ROOT -> force point a decimal separator
            sNb = String.format(Locale.ROOT,"%5.2f",currPoint.Vario);
            // in xLogfly sometimes we have expressions like : -NAN(000).00 
            // that is why we check result string
            if (DOUBLE.matcher(sNb).find()) {
                jsVario.append(sNb).append(",");
            }  else {
                jsVario.append("0.00,");
            }
         
            // locale.ROOT -> force point a decimal separator
            sNb = String.format(Locale.ROOT,"%6.2f",currPoint.Vitesse);
            // in xLogfly sometimes we have expressions like : -NAN(000).00 
            // that is why we check result string
            if (DOUBLE.matcher(sNb).find()) {
                jsSpeed.append(sNb).append(",");
            }  else {
                jsSpeed.append("0.00,");
            } 
            
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
        if ( res && jsVario.length() > 0 ) {
            jsVario.setLength(jsVario.length() - 1);
        }  else res = false;
        if ( res && jsSpeed.length() > 0 ) {
            jsSpeed.setLength(jsSpeed.length() - 1);
        }  else res = false;
        if ( res && jsHeure.length() > 0 ) {
            jsHeure.setLength(jsHeure.length() - 1);
        }  else res = false;
          
        return res;
    }
    
    /**
     * HTML generation of thermals markers
     * @param traceGPS
     * @return 
     */
    private boolean genThermalData(traceGPS traceVisu)   {
        
        boolean res = false;
        int idxPtB;
        String sIcone = "fa-cloud-upload";
        String sBestIcone = "fa-thumbs-up";
        int totPoints = trackAnalyze.finalThermals.size();
                      
        DecimalFormatSymbols decimalFormatSymbols = new DecimalFormatSymbols();
        decimalFormatSymbols.setDecimalSeparator('.');        
        DecimalFormat decimalFormat = new DecimalFormat("###.00000", decimalFormatSymbols);  
        
        if (totPoints > 0) {
            for (int i = 0; i < totPoints; i++) {
                remarkable currRmk = trackAnalyze.finalThermals.get(i);         
                int startLine = currRmk.getIdxStart();
                if (startLine > 0)
                    startLine = startLine - 1;
                int endLine = currRmk.getIdxEnd();
                if (endLine < traceVisu.Tb_Good_Points.size()-2)
                    endLine = endLine+1;
                idxPtB = currRmk.getIdxEnd();                    
                pointIGC currPoint = traceVisu.Tb_Good_Points.get(idxPtB);
                jsThermique.append("    var TH").append(String.valueOf(i)).append("marker = new L.marker([");
                jsThermique.append(decimalFormat.format(currPoint.Latitude)).append(",").append(decimalFormat.format(currPoint.Longitude));                            
                // Is it best gain ?
                if (currRmk.getIdxEnd() == trackAnalyze.getBestGainEnd()) {
                    jsThermique.append("],{icon: L.AwesomeMarkers.icon({icon: '").append(sBestIcone);
                    jsThermique.append("', markerColor: 'darkblue', prefix: 'fa', iconColor: 'white'}) })").append(RC);
                } else {
                    jsThermique.append("],{icon: L.AwesomeMarkers.icon({icon: '").append(sIcone);
                    jsThermique.append("', markerColor: 'blue', prefix: 'fa', iconColor: 'white'}) })").append(RC);                        
                }     
                jsThermique.append("        .addTo(THmarkers)").append(RC);
                jsThermique.append("        .bindPopup(\"");
                jsThermique.append(currRmk.getHTMLThermal(i18n));
                jsThermique.append("\");").append(RC);
                jsThermique.append("    var TH").append(String.valueOf(i)).append("Points = tabPoints.slice(").append(String.valueOf(startLine)).append(",").append(String.valueOf(endLine)).append(");").append(RC);
                jsThermique.append("    var TH").append(String.valueOf(i)).append("Line = new L.Polyline(TH").append(String.valueOf(i)).append("Points, thermOptions);").append(RC);
                jsThermique.append("    TH").append(String.valueOf(i)).append("Line.addTo(THmarkers);").append(RC).append(RC);
            }
            
            res = true;
        }
                  
        return res;
        
    }    
    
    
    /**
     * HTML generation of thermals markers
     * @param traceGPS
     * @return 
     */
    private boolean genGlideData(traceGPS traceVisu)   {
        
        boolean res = false;
        String glideIcon;
        String colorIcon;
        int totPoints = trackAnalyze.finalGlides.size();
                      
        DecimalFormatSymbols decimalFormatSymbols = new DecimalFormatSymbols();
        decimalFormatSymbols.setDecimalSeparator('.');        
        DecimalFormat decimalFormat = new DecimalFormat("###.00000", decimalFormatSymbols);  
        
        if (totPoints > 0) {
            for (int i = 0; i < totPoints; i++) {
                remarkable currRmk = trackAnalyze.finalGlides.get(i);          
                pointIGC startPoint = traceVisu.Tb_Good_Points.get(currRmk.getIdxStart());              
                pointIGC endPoint = traceVisu.Tb_Good_Points.get(currRmk.getIdxEnd());                    
                if (startPoint.getLongitude() < endPoint.getLongitude()) 
                    glideIcon = "'fa-angle-right'";
                else
                    glideIcon = "'fa-angle-left'";
                jsGlide.append("    var Line").append(String.valueOf(i)).append(" = L.polyline([new L.LatLng(");
                jsGlide.append(decimalFormat.format(startPoint.Latitude)).append(",").append(decimalFormat.format(startPoint.Longitude)).append("),");       
                jsGlide.append("new L.LatLng(").append(decimalFormat.format(endPoint.Latitude)).append(",").append(decimalFormat.format(endPoint.Longitude)).append(")],").append(RC); 
                jsGlide.append("                      {color: '#848484',weight: 3, dashArray: '10,5', opacity: 1 });").append(RC); 
                jsGlide.append("     Line").append(String.valueOf(i)).append(".addTo(GLmarkers);").append(RC); 
                // Info distances supprimées pour l'instant
                // 1. rendent la carte un peu chargée
                // 2. surtout restent par dessus le popup rendant celui-ci illisible
//                jsGlide.append("labelDist = new L.Label();").append(RC); 
//                jsGlide.append("LabelInfo = '").append(String.format("%2.1f",currRmk.getDistance())).append(" km'").append(RC);
//                jsGlide.append("labelDist.setContent(LabelInfo);").append(RC); 
//                jsGlide.append("labelDist.setLatLng(Line").append(String.valueOf(i)).append(".getBounds().getCenter());").append(RC); 
//                jsGlide.append("GLmarkers.addLayer(labelDist);").append(RC);      
                if (currRmk.getIdxEnd() == trackAnalyze.getBestGlideEnd()) {
                    colorIcon = "red";
                } else {
                    colorIcon = "blue";
                }
                jsGlide.append("    var posMarker = Line").append(String.valueOf(i)).append(".getBounds().getCenter();").append(RC);
                jsGlide.append("    var GL").append(String.valueOf(i));
                jsGlide.append("marker = new L.marker(posMarker,{icon: L.AwesomeMarkers.icon({icon: ");
                jsGlide.append(glideIcon).append(", markerColor: '").append(colorIcon);
                jsGlide.append("', prefix: 'fa', iconColor: 'white'}) })");
                jsGlide.append("         .addTo(GLmarkers)");
                jsGlide.append("         .bindPopup(\"");
                jsGlide.append(currRmk.getHTMLGlides(i18n));
                jsGlide.append("\");").append(RC);
                jsGlide.append("    GL").append(String.valueOf(i)).append("marker.addTo(GLmarkers);").append(RC);                                
                jsGlide.append(RC);                                                             
            }
            res = true;
        }
                  
        return res;
        
    }                
    /**
     * HTML generation of trunpoints markers
     * in xLogfly -> jsVisuBal
     * this code is bad, we keep it for historical reasons
     * JSON is decoded twice : first in the track treatment and second here
     * @param traceVisu
     * @return 
     */
    private boolean genBalises(traceGPS traceVisu)   {
        boolean res = false;
        String sCoord,sBal;
        String [] TbBal = new String [7];
        TbBal[0] = "";
        TbBal[1] = "iconUrl: 'http://maps.google.com/mapfiles/kml/pal3/icon8.png',";
        TbBal[2] = "iconUrl: 'http://maps.google.com/mapfiles/kml/pal3/icon9.png',";
        TbBal[3] = "iconUrl: 'http://maps.google.com/mapfiles/kml/pal3/icon10.png',";
        TbBal[4] = "iconUrl: 'http://maps.google.com/mapfiles/kml/pal3/icon11.png',";
        TbBal[5] = "iconUrl: 'http://maps.google.com/mapfiles/kml/pal3/icon12.png',";
        TbBal[6] = "iconUrl: 'http://maps.google.com/mapfiles/kml/pal3/icon13.png',";
        int Idx;
        
        try {
            JSONParser jsonParser = new JSONParser();
            JSONObject jsonObject = (JSONObject) jsonParser.parse(traceVisu.getScore_JSON());
            
            jsBalises.append("    var SPIcon = L.Icon.Default.extend({").append(RC); 
            jsBalises.append( "        options: {").append(RC); 
            jsBalises.append( "        iconUrl: 'http://maps.google.com/mapfiles/kml/pal4/icon20.png',").append(RC); 
            jsBalises.append( "        iconSize:     [32, 32]").append(RC); 
            jsBalises.append( "        }").append(RC); 
            jsBalises.append( "    });").append(RC); 
            jsBalises.append(RC); 
            jsBalises.append( "    var StartIcon = new SPIcon();").append(RC); 
            
            JSONArray tabPoints = (JSONArray) jsonObject.get("drawPoints");
            if (tabPoints != null)  {
                final int idxBA = tabPoints.size() -1;                
                for(int i=0; i<tabPoints.size(); i++){                    
                    JSONArray coord = (JSONArray) tabPoints.get(i);                                            
                    // In json, corrdinate is like 45.94393,6.4455833,371 -> Lat, long and point number 
                    // if the string can't be casted in integer -> exception
                    Idx = Integer.valueOf(coord.get(2).toString());                    
                    pointIGC currPoint = traceVisu.Tb_Calcul.get(Idx);
                    // No switch possible -> "Constant expression required" for different cases
                    if ( i == 0) {                    
                            // start point BD                            
                            jsBalises.append("    var markerBal = new L.marker(new L.LatLng(").append(coord.get(0)).append(",").append(coord.get(1)).append("), {icon: StartIcon})").append(RC); 
                            jsBalises.append("                    .bindPopup(\"<b>").append(i18n.tr("SP")).append("</b><br/>");
                            jsBalises.append(currPoint.dHeure.format(dtfHHmm)).append("<br/>Alt GPS : ").append(String.valueOf(currPoint.AltiGPS)).append("<br/>");
                            jsBalises.append(String.format("%3.2f",currPoint.Vitesse)).append("km/h<br/>").append(fmtsigne.format(currPoint.Vario)).append("m/s\");").append(RC); 
                            jsBalises.append("    LayerBal.addLayer(markerBal);").append(RC).append(RC); 
                    } else if (i == idxBA) {
                        // end point BA
                        jsBalises.append(RC);
                        jsBalises.append("    var IconEP = L.Icon.Default.extend({").append(RC); 
                        jsBalises.append("        options: {").append(RC); 
                        jsBalises.append("        iconUrl: 'http://maps.google.com/mapfiles/kml/pal4/icon21.png',").append(RC); 
                        jsBalises.append("        iconSize:     [32, 32]").append(RC); 
                        jsBalises.append("        }").append(RC); 
                        jsBalises.append("    });").append(RC); 
                        jsBalises.append("    var EndIcon = new IconEP();").append(RC); 
                        jsBalises.append(RC);
                        jsBalises.append("    var markerBal = new L.marker(new L.LatLng(").append(coord.get(0)).append(",").append(coord.get(1)).append("), {icon: EndIcon})").append(RC); 
                        jsBalises.append( "                    .bindPopup(\"<b>").append(i18n.tr("EP")).append("</b><br/>");
                        jsBalises.append(currPoint.dHeure.format(dtfHHmm)).append("<br/>Alt GPS : ").append(String.valueOf(currPoint.AltiGPS)).append("<br/>");
                        jsBalises.append(String.format("%3.2f",currPoint.Vitesse)).append("km/h<br/>").append(fmtsigne.format(currPoint.Vario)).append("m/s\");").append(RC); 
                        jsBalises.append( "    LayerBal.addLayer(markerBal);").append(RC);                         
                    } else {
                        jsBalises.append( "    var StylIcon"+String.valueOf(i)).append(" = L.Icon.Default.extend({").append(RC); 
                        jsBalises.append( "        options: {").append(RC); 
                        jsBalises.append(TbBal[i]).append(RC); 
                        jsBalises.append( "            iconSize:     [32, 32]").append(RC); 
                        jsBalises.append( "        }").append(RC); 
                        jsBalises.append( "    });").append(RC); 
                        jsBalises.append( "    var Icon").append(String.valueOf(i)).append(" = new StylIcon").append(String.valueOf(i)).append("();").append(RC); 
                        jsBalises.append( "    var markerBal = new L.marker(new L.LatLng(").append(coord.get(0)).append(",").append(coord.get(1)).append("), {icon: Icon"+String.valueOf(i)).append("})").append(RC); 
                        if (Idx > -1 && Idx < traceVisu.Tb_Calcul.size()) {
                            /* Dans la version Google API, on avait noté ceci :
                            * L'élimination des points "doublons" générés par le Reversale impose ces précautions
                            * Dans "drawPoints", il peut sortir un point "Doublon" qui n'a pas été gardé lors du décodage
                            * Sur FAI_118_25_165_55.igc, plus de 240 points "doublons" ont ainsi été éliminés
                            * Le traitement aurait été trop compliqué... On fait l'impasse et l'infowindow du point manquant sera vierge...
                            * Dans la version Leaflet, on ne binde pas de PopUp  */
                            jsBalises.append("                    .bindPopup(\"<b>B").append(String.valueOf(i)).append("</b><br/>");
                            jsBalises.append(currPoint.dHeure.format(dtfHHmm)).append("<br/>Alt GPS : ").append(String.valueOf(currPoint.AltiGPS)).append("<br/>");
                            jsBalises.append(String.format("%3.2f",currPoint.Vitesse)).append("km/h<br/>").append(fmtsigne.format(currPoint.Vario)).append("m/s\");").append(RC); 
                        }
                        jsBalises.append( "   LayerBal.addLayer(markerBal);").append(RC); 
                    }
                }
                res = true;
            }            
        } catch (Exception e) {
            res = false;
        }
 
        return res;
    }        
    
    /**
     * HTML generation of lines between turnpoints
     * in xLogfly -> jsVisuScore     
     * this code is bad, we keep it for historical reasons
     * JSON is decoded twice : first in the track treatment and second here
     * Here this more justified : drawlines values were not decoded in track treatment
     * @param traceVisu
     * @return 
     */
    private boolean genScore(traceGPS traceVisu) {
        boolean res = false;
        String sLine, sCoord1, sCoord2, key;
        boolean IsTriangle = false;
  
        try {
            JSONParser jsonParser = new JSONParser();
            JSONObject jsonObject = (JSONObject) jsonParser.parse(traceVisu.getScore_JSON());
  
            // is it a triangle or not ?
            JSONObject score = (JSONObject) jsonObject.get("drawScore");            
            legLeague = score.get("scoreLeague").toString();           
            legShape = score.get("scoreShape").toString();
            legDistance = score.get("scoreDistance").toString();
            legPoints = score.get("scorePoints").toString();  
            if (score.get("scoreShape").toString().contains("Triangle")) IsTriangle = true;
            JSONArray tabLines = (JSONArray) jsonObject.get("drawLines");
            if (tabLines != null)  {
                for(int i=0; i<tabLines.size(); i++){    
                    JSONArray biCoord = (JSONArray) tabLines.get(i);
                    if (biCoord.size() == 2) {    
                        JSONArray coord1 = (JSONArray) biCoord.get(0);
                        JSONArray coord2 = (JSONArray) biCoord.get(1);
                        if (i < tabLines.size()-1 || IsTriangle == false)
                        {       
                            jsScore.append("var Line").append(String.valueOf(i)).append(" = L.polyline([new L.LatLng(");
                            jsScore.append(coord1.get(0)).append(",").append(coord1.get(1)).append("),").append(RC);         
                            jsScore.append("                   new L.LatLng(").append(coord2.get(0)).append(",").append(coord2.get(1)).append(")],").append(RC); 
                            jsScore.append("                      {color: '#FFFF00',weight: 3,opacity: 1 });").append(RC); 
                            jsScore.append("Line").append(String.valueOf(i)).append(".addTo(ScoreMarkers);").append(RC); 
                            // Info distances
                            jsScore.append("labelDist = new L.Label();").append(RC); 
                            jsScore.append("LabelInfo = (new L.LatLng(").append(coord1.get(0)).append(",").append(coord1.get(1)).append(").distanceTo(new L.LatLng(");
                            jsScore.append(coord2.get(0)).append(",").append(coord2.get(1)).append(")) / 1000).toFixed(2) + ' km';").append(RC); 
                            jsScore.append("labelDist.setContent(LabelInfo);").append(RC); 
                            jsScore.append("labelDist.setLatLng(Line").append(String.valueOf(i)).append(".getBounds().getCenter());").append(RC); 
                            jsScore.append("ScoreMarkers.addLayer(labelDist);").append(RC);         
                            jsScore.append(RC); 
                        } else {        
                            jsScore.append("var Line").append(String.valueOf(i)).append(" = L.polyline([new L.LatLng(").append(coord1.get(0)).append(",").append(coord1.get(1)).append("),").append(RC);         
                            jsScore.append( "                  new L.LatLng(").append(coord2.get(0)).append(",").append(coord2.get(1)).append(")],").append(RC); 
                            jsScore.append("                     {color: '#848484',weight: 3, dashArray: '10,5', opacity: 1 });").append(RC); 
                            jsScore.append( "Line").append(String.valueOf(i)).append(".addTo(ScoreMarkers);").append(RC); 
                            // Info distances
                            jsScore.append( "labelDist = new L.Label();").append(RC); 
                            jsScore.append( "LabelInfo = (new L.LatLng(").append(coord1.get(0)).append(",").append(coord1.get(1)).append(").distanceTo(new L.LatLng(");
                            jsScore.append(coord2.get(0)).append(",").append(coord2.get(1)).append(")) / 1000).toFixed(2) + ' km';").append(RC); 
                            jsScore.append( "labelDist.setContent(LabelInfo);").append(RC); 
                            jsScore.append( "labelDist.setLatLng(Line").append(String.valueOf(i)).append(".getBounds().getCenter());").append(RC); 
                            jsScore.append( "ScoreMarkers.addLayer(labelDist);").append(RC); 
                            jsScore.append(RC);                            
                        }
                    }
                }
                res = true;
            }
        } catch (Exception e) {
            
        }
        
        return res;
    }
        
    private void genPhotosLayer(traceGPS traceVisu) {
                
        String pathPhotoFolder = traceVisu.getPhotosPath();
        if (pathPhotoFolder != null) {
            map_photos genJsPhotos = new map_photos(traceVisu, new File(pathPhotoFolder), myConfig, false);
            if (genJsPhotos.getNbGpsPhotos() > 0) {                        
                jsPhotos.append(genJsPhotos.getJsTabPhotos().toString());
                
                jsPhotosCode.append("    var photoIcon = new L.Icon({").append(RC);
                jsPhotosCode.append("        iconUrl: 'http://maps.google.com/mapfiles/kml/pal4/icon46.png',").append(RC);
                jsPhotosCode.append("        iconSize: [32, 32],").append(RC);
                jsPhotosCode.append("        iconAnchor: [16, 32],").append(RC);
                jsPhotosCode.append("        popupAnchor: [1, -34]").append(RC);
                jsPhotosCode.append("    });").append(RC).append(RC);
                jsPhotosCode.append("    var photo_layer = L.layerGroup();").append(RC);
                jsPhotosCode.append("    $.each(photos, function(k, photo) {").append(RC);
                jsPhotosCode.append("         var marker = L.marker(photo.latLng, { icon: photoIcon}).addTo(photo_layer)").append(RC);
                jsPhotosCode.append("                      .bindPopup(''+k+'').on('click', onClickMarker);").append(RC);
                jsPhotosCode.append("    });").append(RC);
                jsPhotosCode.append("    photo_layer.addTo(map);").append(RC).append(RC);
                jsPhotosCode.append("    function onClickMarker(e) {").append(RC);
                jsPhotosCode.append("        var popup = e.target.getPopup();").append(RC);
                jsPhotosCode.append("        var content = popup.getContent();").append(RC);
                jsPhotosCode.append("        $.fancybox.open([galerie[content]], {padding : 0 });").append(RC);
                jsPhotosCode.append("        map.closePopup();").append(RC);
                jsPhotosCode.append("    }").append(RC);                                   
            }
            
            if (genJsPhotos.getNbSimplePhotos() > 0) {                   
                jsGallery.append(genJsPhotos.getJsTabGallery().toString());
                
                jsGalleryCode.append("L.easyButton('fa-photo', function(btn, map){").append(RC);
                jsGalleryCode.append("         $.fancybox.open(galerie, {").append(RC); 
                jsGalleryCode.append("             \"overlayOpacity\":0.8,").append(RC);
                jsGalleryCode.append("             \"autoScale\" : true,").append(RC);
                jsGalleryCode.append("             \"arrows\" : true,").append(RC);
                jsGalleryCode.append("             \"type\" : \"image\",").append(RC);
                jsGalleryCode.append("             \"width\" : \"100%\",").append(RC);
                jsGalleryCode.append("             \"height\" : \"100%\",").append(RC);
                jsGalleryCode.append("             \"padding\": 0,").append(RC);
                jsGalleryCode.append("             \"margin\": 20").append(RC);
                jsGalleryCode.append("         });").append(RC);
                jsGalleryCode.append("         $(\".fancybox\").fancybox();").append(RC);
                jsGalleryCode.append("    }).addTo(map);").append(RC);                
            }
        }
    }
    
    /**
     * HTML generation of detailed info panel
     * @param traceVisu 
     */
    public void genLegende(traceGPS traceVisu)  {
                
        jsLegende.append("legend._div.innerHTML += '").append(traceVisu.getsDate_Vol()).append("<br>';").append(RC);
        jsLegende.append("        legend._div.innerHTML += '").append(traceVisu.getsPilote()).append("<br>';").append(RC);
        jsLegende.append("        legend._div.innerHTML += '").append(traceVisu.getsVoile()).append("<br>';").append(RC);
        jsLegende.append("        legend._div.innerHTML += '").append(i18n.tr("Duration")).append(" : ").append(traceVisu.getsDuree_Vol()).append("<br>';").append(RC);
        jsLegende.append("        legend._div.innerHTML += '").append("<hr />';").append(RC);
        // Launching time
        String hDeco = traceVisu.getDT_Deco().format(DateTimeFormatter.ofPattern("HH:mm"));                
        jsLegende.append("        legend._div.innerHTML += '").append(i18n.tr("Take off")).append(" : ").append(hDeco).append("<br>';").append(RC);   
        jsLegende.append("        legend._div.innerHTML += '").append("GPS : ").append(String.valueOf(traceVisu.getAlt_Deco_GPS())).append("m<br>';").append(RC);
        String[] siteComplet;       
        // Search for launching site
        String finalSiteDeco = null; 
        dbSearch myRech = new dbSearch(myConfig);
        String siteDeco = myRech.rechSiteCorrect(traceVisu.getLatDeco(),traceVisu.getLongDeco(),true);   
        if (siteDeco != null)  {
            // we found something like PLANFAIT*France
            siteComplet = siteDeco.split("\\*");
            if (siteComplet.length > 0) {    
                siteDeco = siteComplet[0]; 
                if (siteDeco.length() > 25) 
                    finalSiteDeco = siteDeco.substring(0,25);
                else
                    finalSiteDeco = siteDeco;                
            }                
        } else {
            DecimalFormatSymbols decimalFormatSymbols = new DecimalFormatSymbols();
            decimalFormatSymbols.setDecimalSeparator('.');        
            DecimalFormat decimalFormat = new DecimalFormat("###.0000", decimalFormatSymbols);
            
            geonominatim nom = new geonominatim();
            nom.askReverseGeo(decimalFormat.format(traceVisu.getLatDeco()), decimalFormat.format(traceVisu.getLongDeco()));
            if (nom.getGeoStatus().equals("OK")) {
                StringBuilder sb = new StringBuilder();
                sb.append(nom.getGeoCodepays()).append(" ").append(nom.getGeoVille());
                siteDeco = sb.toString();                
            } else {
                siteDeco = "";
            }                   
            if (siteDeco.length() > 25) 
                finalSiteDeco = siteDeco.substring(0,25);
            else
                finalSiteDeco = siteDeco;
        }
        // To avoid an unsightly \ in place of apostrophe
        String goodSiteDeco = finalSiteDeco.replace("'", "\\'");     
        //String goodSiteDeco = "Plan de L\\'AIGOUILLER"; 
        jsLegende.append("        legend._div.innerHTML += '").append(goodSiteDeco).append("<br>';").append(RC);         
        // Search for landing site with last point coordinates
        pointIGC lastPoint = traceVisu.Tb_Good_Points.get(traceVisu.Tb_Good_Points.size()-1);  
        String finalSiteAtterro = null;
        String siteAtterro = myRech.rechSiteCorrect(lastPoint.Latitude,lastPoint.Longitude,false);         
        if (siteAtterro != null)  {
            siteComplet = siteAtterro.split("\\*");
            if (siteComplet.length > 0) {            
                siteAtterro = siteComplet[0]; 
                if (siteAtterro.length() > 25) 
                    finalSiteAtterro = siteAtterro.substring(0,25);
                else
                    finalSiteAtterro = siteAtterro;
            }
        } else {    
            DecimalFormatSymbols decimalFormatSymbols = new DecimalFormatSymbols();
            decimalFormatSymbols.setDecimalSeparator('.');        
            DecimalFormat decimalFormat = new DecimalFormat("###.0000", decimalFormatSymbols);
            
            geonominatim nom = new geonominatim();
            nom.askReverseGeo(decimalFormat.format(lastPoint.Latitude), decimalFormat.format(lastPoint.Longitude));
            if (nom.getGeoStatus().equals("OK")) {
                StringBuilder sb = new StringBuilder();
                sb.append(nom.getGeoCodepays()).append(" ").append(nom.getGeoVille());
                siteAtterro = sb.toString();                
            } else {
                siteAtterro = "";
            }               
            if (siteAtterro.length() > 25) 
                finalSiteAtterro = siteAtterro.substring(0,25);
            else
                finalSiteAtterro = siteAtterro;
        }            
        // To avoid an unsightly \ in place of apostrophe
        String goodSiteAterro = finalSiteAtterro.replace("'", "\\'");    
        String hAttero = traceVisu.getDT_Attero().format(DateTimeFormatter.ofPattern("HH:mm"));                 
        jsLegende.append("        legend._div.innerHTML += '").append(i18n.tr("Landing")).append(" : ").append(hAttero).append("<br>';").append(RC);
        jsLegende.append("        legend._div.innerHTML += '").append(" GPS : ").append(String.valueOf(traceVisu.getAlt_Attero_GPS())).append("m<br>';").append(RC);        
        jsLegende.append("        legend._div.innerHTML += '").append(goodSiteAterro).append("<br>';").append(RC);
        jsLegende.append("        legend._div.innerHTML += '").append("<hr />';").append(RC);
        pointIGC ptAltMax = traceVisu.getAlt_Maxi_GPS();
        jsLegende.append("        legend._div.innerHTML += '").append(i18n.tr("Max GPS alt")).append(" : ").append(String.valueOf(ptAltMax.AltiGPS)).append("m<br>';").append(RC);
        pointIGC ptAltMini = traceVisu.getAlt_Mini_GPS();
        jsLegende.append("        legend._div.innerHTML += '").append(i18n.tr("Min GPS alt")).append(" : ").append(String.valueOf(ptAltMini.AltiGPS)).append("m<br>';").append(RC);
        pointIGC ptVarioMax = traceVisu.getVario_Max();
        jsLegende.append("        legend._div.innerHTML += '").append(i18n.tr("Max climb")).append(" : ").append(String.format("%2.2f",ptVarioMax.Vario)).append("m/s<br>';").append(RC);
        pointIGC ptVarioMini = traceVisu.getVario_Mini();
        jsLegende.append("        legend._div.innerHTML += '").append(i18n.tr("Max sink")).append(" : ").append(String.format("%2.2f",ptVarioMini.Vario)).append("m/s<br>';").append(RC);
        jsLegende.append("        legend._div.innerHTML += '").append(i18n.tr("Max gain")).append(" : ").append(String.valueOf(traceVisu.getBestGain())).append("m<br>';").append(RC);
        pointIGC ptVitMax = traceVisu.getVit_Max();
        jsLegende.append("        legend._div.innerHTML += '").append(i18n.tr("Max speed")).append(" : ").append(String.format("%3.2f",ptVitMax.Vitesse)).append("km/h<br>';").append(RC);
        jsLegende.append("        legend._div.innerHTML += '").append(i18n.tr("Best transition")).append(" : ").append(String.format("%3.2f",dBestT)).append("km<br>';").append(RC);
        jsLegende.append("        legend._div.innerHTML += '").append(i18n.tr("Size")).append(" : ").append(String.format("%3.2f",traceVisu.getTrackLen())).append("km<br>';").append(RC);        
        jsLegende.append("        legend._div.innerHTML += '").append(i18n.tr("Points")).append(" : ").append(String.valueOf(traceVisu.getNbPoints())).append("<br>';").append(RC);
        jsLegende.append("        legend._div.innerHTML += '").append(i18n.tr("Aberrants")).append(" : ").append(String.valueOf(traceVisu.getNbPointsAberr())).append("<br>';").append(RC);
        jsLegende.append("        legend._div.innerHTML += '").append(i18n.tr("Signature")).append(" : ").append(traceVisu.getSignature()).append("<br>';").append(RC);        
    }
    
    /**
     * HTML generation of socre info panel
     */
    private void genScoreLegende() {
        jsLegende.append("        legend._div.innerHTML += '").append("<hr />';").append(RC);
        switch (legLeague) {
            case "FR" :
                jsLegende.append("        legend._div.innerHTML += '<b>").append(i18n.tr("French contest")).append("</b><br>';").append(RC);    
                break;
            case "CH" :
                jsLegende.append("        legend._div.innerHTML += '<b>").append(i18n.tr("Swiss contest")).append("</b><br>';").append(RC);    
                break;
            case "XC":
                jsLegende.append("        legend._div.innerHTML += '<b>").append(i18n.tr("World XContest")).append("</b><br>';").append(RC);    
                break;
            default:
                jsLegende.append("        legend._div.innerHTML += '<b>").append(legLeague).append("</b><br>';").append(RC);    
        }
        switch (legShape) {
            case "FAI Triangle" :
                jsLegende.append("        legend._div.innerHTML += '<b>&nbsp;&nbsp;").append(i18n.tr("FAI triangle")).append("</b><br>';").append(RC);    
                break;
            case "Free flight 2 wpt" :
                jsLegende.append("        legend._div.innerHTML += '<b>&nbsp;&nbsp;").append(i18n.tr("Flight 2 points")).append("</b><br>';").append(RC);    
                break;
            case "Flat Triangle":
                jsLegende.append("        legend._div.innerHTML += '<b>&nbsp;&nbsp;").append(i18n.tr("Flat triangle")).append("</b><br>';").append(RC);    
                break;
             case "Free flight 1 wpt" :
                jsLegende.append("        legend._div.innerHTML += '<b>&nbsp;&nbsp;").append(i18n.tr("Flight 1 point")).append("</b><br>';").append(RC);    
                break;
            case "Free flight 3 wpt":
                jsLegende.append("        legend._div.innerHTML += '<b>&nbsp;&nbsp;").append(i18n.tr("Flight 3 points")).append("</b><br>';").append(RC);    
                break;
            default:
                jsLegende.append("        legend._div.innerHTML += '<b>&nbsp;&nbsp;").append(legShape).append("</b><br>';").append(RC);    
        }
        // Formatting distance is of the form 21.89160109032659
        int iLength = legPoints.length();
        String legFormate = null;
        if (iLength > legDistance.indexOf(".")+3) {
            legFormate = legDistance.substring(0,legDistance.indexOf(".")+3);
        } else {
            legFormate = legDistance;
        }
        jsLegende.append("        legend._div.innerHTML += '&nbsp;&nbsp;&nbsp;&nbsp;").append(legFormate).append(" km").append("<br>';").append(RC);    
        // Formatting score is of the form 9.89160109032659
        iLength = legPoints.length();        
        if (iLength > legPoints.indexOf(".")+3) {
            legFormate = legPoints.substring(0,legPoints.indexOf(".")+3);
        } else {
            legFormate = legPoints;
        }
        jsLegende.append("        legend._div.innerHTML += '&nbsp;&nbsp;&nbsp;&nbsp;").append(legFormate).append(" pts").append("<br>';").append(RC);    
    }
    
    /**
     * Default layer of the map
     * A the beginning, we tested the first point
     * if UBound(Tb_Coord) > -1 Then
     *     if Tb_Coord(0).Latitude < 71 and Tb_Coord(0).Latitude > 35 and Tb_Coord(0).Longitude > -10 and Tb_Coord(0).Longitude < 50 Then
     *        OpenTopo = True
     *     end
     *     else
     *       if Val(App.FinderLat) < 71 and Val(App.FinderLat) > 35 and Val(App.FinderLong) > -10 and Val(App.FinderLong) < 50 Then
     *         OpenTopo = True
     *       end
     *     end
     * According to coordinates we force to OSM layer or Google
     * Only these layers cover whole world
     */
    private void genDefaultLayer() {
         
        // We put a default value to avoid an undefined case   
        int idxMap = myConfig.getIdxMap();
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
     * HTML generation of the map
     * @param traceVisu 
     */
    public void carteVisu(traceGPS traceVisu)  {
        StringBuilder sbHTML = new StringBuilder();
        
        try {
            try  {                
                BufferedReader br = new BufferedReader(new InputStreamReader(map_visu.class.getResourceAsStream("/skl/skl_full_visu.txt")));                
                String line = null;            
                while ((line = br.readLine()) != null) {
                    sbHTML.append(line).append(RC);                    
                }
                br.close();
            } catch (IOException e) {
                System.out.println("Erreur skl "+e.getMessage());                               
            }
            if (sbHTML.length() > 1000 && genData(traceVisu))  {
                String beginHTML = sbHTML.toString();
                String pointsHTML = beginHTML.replace("%tabPoints%", jsTabPoints.toString());
                String altiLgHTML = pointsHTML.replace("%altiLg%", jsaltiLg.toString());
                String altiValHTML = altiLgHTML.replace("%altiVal%", jsaltiVal.toString());
                String varioHTML =  altiValHTML.replace("%Vario%", jsVario.toString());
                String speedHTML = varioHTML.replace("%Speed%", jsSpeed.toString());
                String heureHTML = "";
                if (traceVisu.getPhotosPath() != null) {
                    genPhotosLayer(traceVisu);
                    String photosHTML;
                    if (jsPhotos.length() > 10) 
                        photosHTML = speedHTML.replace("//%Photos%", jsPhotos.toString());
                    else
                        photosHTML = speedHTML;
                    String galleryHTML;
                    if (jsGallery.length() > 30)
                        galleryHTML = photosHTML.replace("//%Gallery%", jsGallery.toString());
                    else
                        galleryHTML = photosHTML;
                    heureHTML = photosHTML.replace("%Heure%", jsHeure.toString());
                } else {
                    heureHTML = speedHTML.replace("%Heure%", jsHeure.toString());   
                }                
                genDefaultLayer();
                String layerHTML = heureHTML.replace("%layer%", jsLayer); 
                String glideHTML;
                 if (genGlideData(traceVisu)) {
                    glideHTML = layerHTML.replace("//%GLmarker%", jsGlide.toString());
                } else  {
                    glideHTML = layerHTML;
                }                  
                String thermiqHTML;
                // special method for thermals
                if (genThermalData(traceVisu)) {
                    thermiqHTML = glideHTML.replace("//%THmarker%", jsThermique.toString());
                } else  {
                    thermiqHTML = glideHTML;
                }   
                genLegende(traceVisu);
                map_HTML_NoScore = thermiqHTML;
                String endHTML = thermiqHTML;
                if (traceVisu.isScored())  {
                    String balisesHTML;
                    if (genBalises(traceVisu)) {
                        balisesHTML = thermiqHTML.replace("//%ScoreBalises%", jsBalises.toString());
                    } else {
                        balisesHTML = thermiqHTML;
                    }           
                    if (genScore(traceVisu)) {
                        String scoreLinesHTML = balisesHTML.replace("//%ScoreLines%", jsScore.toString());
                        StringBuilder scoreOptions = new StringBuilder();
                        scoreOptions.append("\"Score\" : ScoreMarkers,").append(RC); 
                        scoreOptions.append("\"Balises\" : LayerBal,");                    
                        String scoreOptHTML = scoreLinesHTML.replace("//%ScoreOptions%", scoreOptions.toString());
                        String scoreLayer = "    map.addLayer(ScoreMarkers);";
                        String scoreLayerHTML =scoreOptHTML.replace("//%AddScoreMarkers%", scoreLayer);
                        endHTML = scoreLayerHTML;
                        genScoreLegende();
                    } else {
                        endHTML = balisesHTML;
                    }  
                }
                if (traceVisu.getAirPoints() > 0 && traceVisu.getGeoJsonAirsp() != null) {
                    String beginAirHTML = endHTML;
                    String zoneRegHTML = beginAirHTML.replace("//%zoneReg%", traceVisu.getGeoJsonAirsp());
                    String badPtHTML = zoneRegHTML.replace("//%badPoints%", traceVisu.getGeoJsonBadPts());
                    StringBuilder sbAff = new StringBuilder();
                    sbAff.append("var Aff_Zone = new L.geoJson.css(zoneReg, { onEachFeature: popup});").append(RC);
                    sbAff.append("    map.addLayer(Aff_Zone);").append(RC);
                    String affZoneHTML = badPtHTML.replace("//%Aff_Zones%", sbAff.toString());
                    sbAff.setLength(0);
                    sbAff.append("var Aff_BadPoints = new L.geoJson.css(badPoints, {pointToLayer: function(f, latlng) {return L.circleMarker(latlng,geojsonMarkerOptions);}});");
                    sbAff.append(RC).append("    map.addLayer(Aff_BadPoints);").append(RC);
                    String affBadHTML = affZoneHTML.replace("//%Aff_BadPoints%", sbAff.toString());
                    StringBuilder checkOptions = new StringBuilder();
                    checkOptions.append("\"Litige : zones\" : Aff_Zone,").append(RC); 
                    checkOptions.append("\"Litige : points\" : Aff_BadPoints,").append(RC); 
                    String checkOpHTML = affBadHTML.replace("//%CheckOption%",checkOptions.toString());
                    endHTML = checkOpHTML;
                }
                if (traceVisu.getPhotosPath() != null) {
                    String beforePhotosHTML = endHTML;
                    String beforeGalleryHTML;
                    if (jsPhotosCode.length() > 30) {
                        String photosHTML = beforePhotosHTML.replace("//%Aff_Photos%", jsPhotosCode); 
                        beforeGalleryHTML = photosHTML.replace("//%PhotosOption%", "Photos : photo_layer"); 
                    } else {
                        beforeGalleryHTML = endHTML;
                    }
                    if (jsGalleryCode.length() > 30) {
                        String galleryHTML = beforeGalleryHTML.replace("//%Gallery%", jsGallery); 
                        endHTML = galleryHTML.replace("//%btnGallery%", jsGalleryCode); 
                    } else
                        endHTML = beforeGalleryHTML;
                } 
                String Code_HTML = endHTML.replace("%legende%", jsLegende.toString());
                map_HTML = Code_HTML;      
                map_OK = true;
            }
        } catch (Exception e) {
            System.out.println("Erreur carte "+e.getMessage());  
            map_OK = false;
        }                               
    } 
    
}
