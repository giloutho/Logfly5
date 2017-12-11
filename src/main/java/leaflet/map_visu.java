/* 
 * Copyright Gil THOMAS
 * This file forms an integral part of Logfly project
 * See the LICENSE file distributed with source code
 * for details of Logfly licence project
 */
package leaflet;

import database.dbSearch;
import geoutils.reversegeocode;
import igc.pointIGC;
import java.io.BufferedReader;
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

/**
 *
 * @author Gil Thomas logfly.org
 * HTML generation of a full screen leaflet map
 */
public class map_visu {
    
    private I18n i18n;
    
    // Paramètres de configuration
    configProg myConfig;
    
    public static boolean graphAltiBaro = false;
    
    private boolean map_OK;
    private String map_HTML;
    private String map_HTML_NoScore;
    private static final String RC = "\n";  
    private static StringBuilder jsaltiLg ;
    private static StringBuilder jsaltiVal;
    private static StringBuilder jsHeure;
    private static StringBuilder jsSpeed;
    private static StringBuilder jsTabPoints;
    private static StringBuilder jsVario;
    private static StringBuilder jsThermique;
    private static StringBuilder jsLegende;
    private static StringBuilder jsBalises;
    private static StringBuilder jsScore;
    private static String jsLayer;
    private static String legLeague; 
    private static String legShape;
    private static String legDistance;
    private static String legPoints;
    private static double dBestT = 0;
    private static DateTimeFormatter dtfHHmm = DateTimeFormatter.ofPattern("HH:mm");
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
        jsLegende = new StringBuilder();
        jsBalises = new StringBuilder();
        jsScore = new StringBuilder();
               
        i18n = I18nFactory.getI18n("","lang/Messages",map_visu.class.getClass().getClassLoader(),myConfig.getLocale(),0);
        
        DecimalFormatSymbols decimalFormatSymbols = new DecimalFormatSymbols();
        decimalFormatSymbols.setDecimalSeparator('.');        
        decimalFormat = new DecimalFormat("###.00000", decimalFormatSymbols);        
        
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
     * @param traceVisu
     * @return 
     */
    private boolean genThermData(traceGPS traceVisu)   {
        
        boolean res = false;
        int minGain;
        int idxPtB;
        String sIcone = "fa-cloud-upload";
        String sBestIcone = "fa-thumbs-up";
        int totPoints = traceVisu.Tb_Thermique.size();
                      
        DecimalFormatSymbols decimalFormatSymbols = new DecimalFormatSymbols();
        decimalFormatSymbols.setDecimalSeparator('.');        
        DecimalFormat decimalFormat = new DecimalFormat("###.00000", decimalFormatSymbols);  
        
        if (totPoints > 0) {
            // Mini gain is 10% of best gain
            minGain = (int) (traceVisu.getBestGain() * 0.10);     
            for (int i = 0; i < totPoints; i++) {
                thermique currTh = traceVisu.Tb_Thermique.get(i);
                if (currTh.DeltaAlt > 0 && currTh.DeltaAlt > minGain) {
                    idxPtB = currTh.NumPoint;
                    pointIGC currPoint = traceVisu.Tb_Good_Points.get(idxPtB);
                    jsThermique.append("    var TH").append(String.valueOf(i)).append("marker = new L.marker([");
                    jsThermique.append(decimalFormat.format(currPoint.Latitude)).append(",").append(decimalFormat.format(currPoint.Longitude));                            
                    // S'agit il du best gain ?
                    if (currTh.NumPoint == traceVisu.getBestGainIndice2()) {
                        jsThermique.append("],{icon: L.AwesomeMarkers.icon({icon: '").append(sBestIcone);
                        jsThermique.append("', markerColor: 'darkblue', prefix: 'fa', iconColor: 'white'}) })").append(RC);
                    } else {
                        jsThermique.append("],{icon: L.AwesomeMarkers.icon({icon: '").append(sIcone);
                        jsThermique.append("', markerColor: 'blue', prefix: 'fa', iconColor: 'white'}) })").append(RC);                        
                    }     
                    jsThermique.append("        .addTo(THmarkers)").append(RC);
                    jsThermique.append("        .bindPopup(\"");
                    jsThermique .append(" ").append(i18n.tr("Alt."));
                    jsThermique.append(" ").append(String.valueOf(currPoint.AltiGPS)).append("m  \\r\\n");
                    jsThermique.append(i18n.tr("Gain ")).append(" : ").append(String.valueOf(currTh.DeltaAlt)).append(" m  ");
                    jsThermique.append(String.format(Locale.ROOT,"%+5.2f",currTh.MeanVarioValue)).append(" m/s");
                    jsThermique.append("\");").append(RC);
                    
                }
            }
            // best transition
            pointIGC bestPoint2 = traceVisu.Tb_Good_Points.get(traceVisu.getBestTransIndice2());
            pointIGC bestPoint1 = traceVisu.Tb_Good_Points.get(traceVisu.getBestTransIndice1());
            dBestT = (geoutils.trigo.CoordDistance(bestPoint2.Latitude,bestPoint2.Longitude,bestPoint1.Latitude,bestPoint1.Longitude))/1000;
            
            jsThermique.append(RC).append("   var tabBT = [").append(RC);
            jsThermique.append("         new L.LatLng(").append(decimalFormat.format(bestPoint1.Latitude));
            jsThermique.append(",").append(decimalFormat.format(bestPoint1.Longitude)).append("),").append(RC);
            jsThermique.append("         new L.LatLng(").append(decimalFormat.format(bestPoint2.Latitude));
            jsThermique.append(",").append(decimalFormat.format(bestPoint2.Longitude)).append("),").append(RC);            
            jsThermique.append("      ];").append(RC);
            
            jsThermique.append("   var optionsBT = {").append(RC);
            jsThermique.append("      map: map,").append(RC);
            jsThermique.append("      strokeColor : \"#7FFF00\",").append(RC);
            jsThermique.append("      strokeWeight : 3,").append(RC);
            jsThermique.append("      path: tabBT").append(RC);
            jsThermique.append("   };").append(RC);
            jsThermique.append("   var BTPolyline = L.polyline(tabBT, {color: '#848484',weight: 3, dashArray: '10,5', opacity: 1 });").append(RC);
            jsThermique.append("   BTPolyline.addTo(THmarkers);").append(RC);
            jsThermique.append(RC);
            
            /*   removed code in last xLogfly version
            *   **************** with yhis markers, the map seems less readable
                's=s+"     var BT1marker = new google.maps.Marker({").append(RC); 
                's=s+"           position: new google.maps.LatLng("+Str(LeafTrace.Tb_Good_Points(LeafTrace.BestTransIndice1).Latitude)+","+Str(LeafTrace.Tb_Good_Points(LeafTrace.BestTransIndice1).Longitude)+"),").append(RC); 
                's=s+"           map: map,").append(RC); 
                's=s+"           title:"""+App.LangRef(214,App.Lang)+""",").append(RC); 
                's=s+"           icon: ""http://maps.google.com/mapfiles/kml/pal5/icon13.png""});").append(RC); 
            */
            
            jsThermique.append("   var BT2marker = new L.marker([").append(decimalFormat.format(bestPoint2.Latitude)).append(",");
            jsThermique.append(decimalFormat.format(bestPoint2.Longitude));
            jsThermique.append("],{icon: L.AwesomeMarkers.icon({icon: 'fa-external-link', markerColor: 'green', prefix: 'fa', iconColor: 'white'}) })").append(RC);
            jsThermique.append("        .addTo(THmarkers)").append(RC);
            jsThermique.append("        .bindPopup(\"").append(i18n.tr("Meilleure transition")).append(" : ");
            jsThermique.append(String.format(Locale.ROOT,"%6.2f",dBestT)).append(" km \");").append(RC);
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
                            jsBalises.append("                    .bindPopup(\"<b>").append(i18n.tr("BD")).append("</b><br/>");
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
                        jsBalises.append( "                    .bindPopup(\"<b>").append(i18n.tr("BA")).append("</b><br/>");
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
    
    /**
     * HTML generation of detailed info panel
     * @param traceVisu 
     */
    public void genLegende(traceGPS traceVisu)  {
                
        jsLegende.append("legend._div.innerHTML += '").append(traceVisu.getsDate_Vol()).append("<br>';").append(RC);
        jsLegende.append("        legend._div.innerHTML += '").append(traceVisu.getsPilote()).append("<br>';").append(RC);
        jsLegende.append("        legend._div.innerHTML += '").append(traceVisu.getsVoile()).append("<br>';").append(RC);
        jsLegende.append("        legend._div.innerHTML += '").append(i18n.tr("Durée")).append(" : ").append(traceVisu.getsDuree_Vol()).append("<br>';").append(RC);
        jsLegende.append("        legend._div.innerHTML += '").append("<hr />';").append(RC);
        // Launching time
        String hDeco = traceVisu.getDT_Deco().format(DateTimeFormatter.ofPattern("HH:mm"));                
        jsLegende.append("        legend._div.innerHTML += '").append(i18n.tr("Décollage")).append(" : ").append(hDeco).append("<br>';").append(RC);   
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
            reversegeocode rechDeco = new reversegeocode();
            DecimalFormatSymbols decimalFormatSymbols = new DecimalFormatSymbols();
            decimalFormatSymbols.setDecimalSeparator('.');        
            DecimalFormat decimalFormat = new DecimalFormat("###.0000", decimalFormatSymbols);
            String sCoord = decimalFormat.format(traceVisu.getLatDeco())+","+decimalFormat.format(traceVisu.getLongDeco());
            siteDeco = rechDeco.googleGeocode(sCoord, false);
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
            reversegeocode rechSite = new reversegeocode();
            DecimalFormatSymbols decimalFormatSymbols = new DecimalFormatSymbols();
            decimalFormatSymbols.setDecimalSeparator('.');        
            DecimalFormat decimalFormat = new DecimalFormat("###.0000", decimalFormatSymbols);
            String sCoord = decimalFormat.format(lastPoint.Latitude)+","+decimalFormat.format(lastPoint.Longitude);
            siteAtterro = rechSite.googleGeocode(sCoord, false);
            if (siteAtterro.length() > 25) 
                finalSiteAtterro = siteAtterro.substring(0,25);
            else
                finalSiteAtterro = siteAtterro;
        }            
        // To avoid an unsightly \ in place of apostrophe
        String goodSiteAterro = finalSiteAtterro.replace("'", "\\'");    
        String hAttero = traceVisu.getDT_Attero().format(DateTimeFormatter.ofPattern("HH:mm"));                 
        jsLegende.append("        legend._div.innerHTML += '").append(i18n.tr("Atterrissage")).append(" : ").append(hAttero).append("<br>';").append(RC);
        jsLegende.append("        legend._div.innerHTML += '").append(" GPS : ").append(String.valueOf(traceVisu.getAlt_Attero_GPS())).append("m<br>';").append(RC);        
        jsLegende.append("        legend._div.innerHTML += '").append(goodSiteAterro).append("<br>';").append(RC);
        jsLegende.append("        legend._div.innerHTML += '").append("<hr />';").append(RC);
        pointIGC ptAltMax = traceVisu.getAlt_Maxi_GPS();
        jsLegende.append("        legend._div.innerHTML += '").append(i18n.tr("Alt max GPS")).append(" : ").append(String.valueOf(ptAltMax.AltiGPS)).append("m<br>';").append(RC);
        pointIGC ptAltMini = traceVisu.getAlt_Mini_GPS();
        jsLegende.append("        legend._div.innerHTML += '").append(i18n.tr("Alt mini GPS")).append(" : ").append(String.valueOf(ptAltMini.AltiGPS)).append("m<br>';").append(RC);
        pointIGC ptVarioMax = traceVisu.getVario_Max();
        jsLegende.append("        legend._div.innerHTML += '").append(i18n.tr("Vario max")).append(" : ").append(String.format("%2.2f",ptVarioMax.Vario)).append("m/s<br>';").append(RC);
        pointIGC ptVarioMini = traceVisu.getVario_Mini();
        jsLegende.append("        legend._div.innerHTML += '").append(i18n.tr("Vario mini")).append(" : ").append(String.format("%2.2f",ptVarioMini.Vario)).append("m/s<br>';").append(RC);
        jsLegende.append("        legend._div.innerHTML += '").append(i18n.tr("Gain max")).append(" : ").append(String.valueOf(traceVisu.getBestGain())).append("m<br>';").append(RC);
        pointIGC ptVitMax = traceVisu.getVit_Max();
        jsLegende.append("        legend._div.innerHTML += '").append(i18n.tr("Vitesse max")).append(" : ").append(String.format("%3.2f",ptVitMax.Vitesse)).append("km/h<br>';").append(RC);
        jsLegende.append("        legend._div.innerHTML += '").append(i18n.tr("Transition max")).append(" : ").append(String.format("%3.2f",dBestT)).append("km<br>';").append(RC);
        jsLegende.append("        legend._div.innerHTML += '").append(i18n.tr("Longueur")).append(" : ").append(String.format("%3.2f",traceVisu.getTrackLen())).append("km<br>';").append(RC);        
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
                jsLegende.append("        legend._div.innerHTML += '<b>").append(i18n.tr("CFD")).append("</b><br>';").append(RC);    
                break;
            case "CH" :
                jsLegende.append("        legend._div.innerHTML += '<b>").append(i18n.tr("Challenge suisse")).append("</b><br>';").append(RC);    
                break;
            case "XC":
                jsLegende.append("        legend._div.innerHTML += '<b>").append(i18n.tr("World XContest")).append("</b><br>';").append(RC);    
                break;
            default:
                jsLegende.append("        legend._div.innerHTML += '<b>").append(legLeague).append("</b><br>';").append(RC);    
        }
        switch (legShape) {
            case "FAI Triangle" :
                jsLegende.append("        legend._div.innerHTML += '<b>&nbsp;&nbsp;").append(i18n.tr("Triangle FAI")).append("</b><br>';").append(RC);    
                break;
            case "Free flight 2 wpt" :
                jsLegende.append("        legend._div.innerHTML += '<b>&nbsp;&nbsp;").append(i18n.tr("Distance 2 points")).append("</b><br>';").append(RC);    
                break;
            case "Flat Triangle":
                jsLegende.append("        legend._div.innerHTML += '<b>&nbsp;&nbsp;").append(i18n.tr("Triangle plat")).append("</b><br>';").append(RC);    
                break;
             case "Free flight 1 wpt" :
                jsLegende.append("        legend._div.innerHTML += '<b>&nbsp;&nbsp;").append(i18n.tr("Distance 1 point")).append("</b><br>';").append(RC);    
                break;
            case "Free flight 3 wpt":
                jsLegende.append("        legend._div.innerHTML += '<b>&nbsp;&nbsp;").append(i18n.tr("Distance 2 points")).append("</b><br>';").append(RC);    
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
                String heureHTML = speedHTML.replace("%Heure%", jsHeure.toString());   
                genDefaultLayer();
                String layerHTML = heureHTML.replace("%layer%", jsLayer); 
                String thermiqHTML;
                // special method for thermals
                if (genThermData(traceVisu)) {
                    thermiqHTML = layerHTML.replace("//%THmarker%", jsThermique.toString());
                } else  {
                    thermiqHTML = layerHTML;
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
                        scoreOptions.append("\"Balises\" : LayerBal");
                        String scoreOptHTML = scoreLinesHTML.replace("//%ScoreOptions%", scoreOptions.toString());
                        String scoreLayer = "    map.addLayer(ScoreMarkers);";
                        String scoreLayerHTML =scoreOptHTML.replace("//%AddScoreMarkers%", scoreLayer);
                        endHTML = scoreLayerHTML;
                        genScoreLegende();
                    } else {
                        endHTML = balisesHTML;
                    }  
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
