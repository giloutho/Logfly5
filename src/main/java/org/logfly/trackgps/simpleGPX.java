/*
 * Copyright Gil THOMAS
 * This file forms an integral part of Logfly project
 * See the LICENSE file distributed with source code
 * for details of Logfly licence project
 */
package org.logfly.trackgps;

import io.jenetics.jpx.GPX;
import io.jenetics.jpx.Track;
import io.jenetics.jpx.TrackSegment;
import io.jenetics.jpx.WayPoint;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.logfly.geoutils.position;
import org.logfly.igc.pointIGC;
import org.logfly.settings.configProg;
import org.logfly.systemio.mylogging;
import org.logfly.systemio.textio;

/**
 *
 * @author gil
 * Special for GPx withiut timestamp.
 * Only used in map utilities
 */
public class simpleGPX {

    private boolean Decodage;
    private String pathFichier;
    private int numErrDecodage;    
    private int nbPoints;
    private int nbTracks;
    List<String> lsTrackName = new ArrayList<String>();    
    private String[] trackName;
    private double latMini;
    private double latMaxi;
    private double longMini;
    private double longMaxi;    
         
    private StringBuilder sbError;
    private final String CrLf = "\r\n";    
    
    public List<position> Tb_Tot_Points = new ArrayList<position>(); 
    private List<position> Tb_Track = new ArrayList<position>(); 
    public List<JSONObject> Tb_GeoJson = new ArrayList<JSONObject>();
    
    configProg myConfig;    
    
    public simpleGPX(File pFile, configProg pConfig)
    {    
        Decodage = false;                
        textio fread = new textio();                                    
        String txtFichier = fread.readTxt(pFile);
        if (txtFichier != null && !txtFichier.isEmpty())  {
            pathFichier = pFile.getAbsolutePath();
            String fileExt = textio.getFileExtension(pFile).toUpperCase();     
            if (fileExt.equals("GPX"))
            {
                decodeGPX(txtFichier);
            } else {
                numErrDecodage = 1060;    // unknown file extension 
            }                 
        }        
    }

    public boolean isDecodage() {
        return Decodage;
    }

    public int getNbTracks() {
        return nbTracks;
    }
    
    
            
    private void decodeGPX(String strGPX) {
        int nbWp = 0;   
        latMini = 90;
        latMaxi = -90;
        longMini = 180;
        longMaxi = -180;        
        
        try {
            // we must check if creator is empty
            if (strGPX.indexOf("creator=\"\"") > 0 ) {
                strGPX = strGPX.replace("creator=\"\"", "creator=\"Logfly\"");
            }
            InputStream stream = new ByteArrayInputStream(strGPX.getBytes(StandardCharsets.UTF_8));    
            boolean lenient = true;
            GPX mygpx = GPX.read(stream,lenient);          
            List<Track> lTrack = mygpx.getTracks();
            nbTracks = lTrack.size();
            for (int i = 0; i < lTrack.size(); i++) {
                String trackName = lTrack.get(i).getName().get().toString();
                if (trackName == null || trackName.equals("")) {
                    trackName = "Track "+String.valueOf(i);
                }
                lsTrackName.add(trackName);
                Tb_Track.clear();
                List<TrackSegment> lSegments = lTrack.get(i).getSegments();                
                for (int j = 0; j < lSegments.size(); j++) {
                    List<WayPoint> lWayp = lSegments.get(j).getPoints();
                    for (int k = 0; k < lWayp.size(); k++) {
                        position currPoint = new position();
                        double currLat = lWayp.get(k).getLatitude().doubleValue();
                        double currLong = lWayp.get(k).getLongitude().doubleValue();
                        currPoint.setLatitudeDd(currLat);
                        currPoint.setLongitudeDd(currLong);
                        currPoint.setAltitude(lWayp.get(k).getElevation().get().intValue());
                        Tb_Track.add(currPoint);
                        nbWp++; 
                        if (currLat> latMaxi) latMaxi = currLat;
                        if (currLong > longMaxi) longMaxi = currLong;
                        if (currLat < latMini) latMini = currLat;
                        if (currLong < longMini) longMini = currLong;                         
                    }                    
                }
                if (Tb_Track.size() > 0) {
                    Tb_GeoJson.add(styledPointsToGeoJson());
                }
            }
            if (nbWp > 0) {
                Decodage = true;
                nbPoints = nbWp;
            }
            
        } catch (Exception ex) {
            sbError = new StringBuilder(this.getClass().getName()+"."+Thread.currentThread().getStackTrace()[1].getMethodName());
            sbError.append("\r\n").append(ex.toString());
            mylogging.log(Level.SEVERE, sbError.toString());
        }                
    }
    
    public String getGeoJson () {
        String res = null;      
        
        JSONObject trackJsonObjectbuilder = new JSONObject();
        trackJsonObjectbuilder.put("type", "FeatureCollection");   
        JSONArray trackArray = new JSONArray();
        for (int i = 0; i < Tb_GeoJson.size(); i++) {
            trackArray.add(Tb_GeoJson.get(i));
        }
        trackJsonObjectbuilder.put("features", trackArray);
        res = trackJsonObjectbuilder.toJSONString();        
        return res;
    }

    private JSONObject pointsToGeoJson() {
                
        JSONObject feature = new JSONObject();
        feature.put("type", "Feature");
        
        // Actually we don't put properties
        // If we want we can add 
        // JSONObject properties = new JSONObject();
        // properties.put("stroke", "#FF0000");
        // etc.. etc...
        // Properties even null is required
        feature.put("properties", null);
        
        JSONObject geometry = new JSONObject();
        geometry.put("type", "LineString");
        int fin = Tb_Track.size();  
        JSONArray coordinates = new JSONArray();        
        for (int i = 0; i < fin; i++) {
            JSONArray coordinate = new JSONArray();
            position currPoint = Tb_Track.get(i);
            coordinate.add(0, currPoint.getLongitude());
            coordinate.add(1, currPoint.getLatitude());
            coordinate.add(2, currPoint.getAltitude());
            coordinates.add(coordinate);                        
        }

        geometry.put("coordinates", coordinates);
        feature.put("geometry",geometry);
                            
        return feature;
    }    
    
    private JSONObject styledPointsToGeoJson() {
                
        JSONObject feature = new JSONObject();
        feature.put("type", "Feature");
        //"style": {'color': "#5B900A",'weight': 2,'opacity': 0.6}};        
        JSONObject jsStyle = new JSONObject();
        jsStyle.put("color", "#FF0000");
        jsStyle.put("weight", 2);
        jsStyle.put("opacity", 0.6);
        feature.put("style", jsStyle);
        
        JSONObject geometry = new JSONObject();
        geometry.put("type", "LineString");
        int fin = Tb_Track.size();  
        JSONArray coordinates = new JSONArray();        
        for (int i = 0; i < fin; i++) {
            JSONArray coordinate = new JSONArray();
            position currPoint = Tb_Track.get(i);
            coordinate.add(0, currPoint.getLongitude());
            coordinate.add(1, currPoint.getLatitude());
            coordinate.add(2, currPoint.getAltitude());
            coordinates.add(coordinate);                        
        }

        geometry.put("coordinates", coordinates);
        feature.put("geometry",geometry);
                            
        return feature;
    }        
}
