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
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.stream.Collectors;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.logfly.igc.pointIGC;
import org.logfly.settings.configProg;
import org.logfly.systemio.mylogging;
import org.logfly.systemio.textio;

/**
 *
 * @author gil
 */
public class traceSimple {
    
    private boolean Decodage;
    private String pathFichier;
    private int numErrDecodage;
    private LocalDateTime DT_Deco;   
    private LocalDateTime DT_Attero;
    private double LatDeco;
    private double LongDeco; 
    private String sDate_Vol;    
    private LocalDateTime Date_Vol;    
    private int Alt_Deco_GPS;
    private double LatAterro;
    private double LongAterro;
    private int Alt_Attero_GPS;
    private int NbPoints;
    private double latMini;
    private double latMaxi;
    private double longMini;
    private double longMaxi;    
    
    public List<pointIGC> Tb_Tot_Points = new ArrayList<pointIGC>();  
    private StringBuilder sbError;
    
    public traceSimple(File pFile, configProg pConfig)
    {    
        Decodage = false;                
        textio fread = new textio();                                    
        String txtFichier = fread.readTxt(pFile);
        if (txtFichier != null && !txtFichier.isEmpty())  {
            pathFichier = pFile.getAbsolutePath();
            String fileExt = textio.getFileExtension(pFile).toUpperCase();     
            if (fileExt.equals("GPX"))
            {
                DecodeGPX(txtFichier);
            } else {
                numErrDecodage = 1060;    // unknown file extension 
            }                 
        }        
    }
 
    
    private void DecodeGPX(String txtGPX) {
        
        pointIGC Point1 = null;
        int nbPoint = 0;   
        int nbWp;
        int TotPoint = 0;
        int totWp = 0;
        LocalDateTime ldt;
        WayPoint lastWp = null;
        boolean MissTime = false;
        SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        DateTimeFormatter dtfDate = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        
        // we must check if creator is empty
        if (txtGPX.indexOf("creator=\"\"") > 0 ) {
            txtGPX = txtGPX.replace("creator=\"\"", "creator=\"Logfly\"");
        }
        InputStream stream = new ByteArrayInputStream(txtGPX.getBytes(StandardCharsets.UTF_8));
        try {           
            GPX mygpx = GPX.read(stream);
            List<Track> lTrack = mygpx.getTracks();
            if (lTrack.size() > 0) {                
                List<WayPoint> lWayp = mygpx.tracks().flatMap(Track::segments).flatMap(TrackSegment::points).collect(Collectors.toList());                       
                nbWp = 0;
                for (io.jenetics.jpx.WayPoint eachWp : lWayp) {                            
                    Point1 = new pointIGC();
                    Point1.setComment("");
                    Point1.setLatitude(eachWp.getLatitude().doubleValue());
                    Point1.setLongitude(eachWp.getLongitude().doubleValue());
                    Point1.setAltiGPS(eachWp.getElevation().get().intValue());   
                    // GPX -> Time is UTC. We must convert to LocalDateTime
                    // with Instant we convert in UTC +0
                    // No date eachWp.getTime() returns Optional.empty
                    try {
                        ldt = eachWp.getTime().get().toLocalDateTime();
                        Point1.setPeriode((ldt.getHour()*3600)+(ldt.getMinute()*60)+ldt.getSecond());                        
                    } catch (Exception e) {
                        ldt = null;
                        Point1.setPeriode(0);
                    }
                    Point1.setLdtHeure(ldt);         
                    // First point of GPX track
                    if (nbPoint == 0) {
                        // take off time
                        if (ldt != null) {
                            DT_Deco = ldt;
                        } else {
                            // Time field can be null (BaseCamp GPX generation)
                            DT_Deco = LocalDateTime.of(2000, 1, 1, 0, 0, 0);                                   
                        }
                        // flight date                      
                        Date_Vol = DT_Deco;
                        sDate_Vol = Date_Vol.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                        LatDeco = Point1.Latitude;
                        LongDeco = Point1.Longitude;
                        Alt_Deco_GPS = Point1.AltiGPS;                                   
                        
                    }                                                                         
                    Tb_Tot_Points.add(Point1);              
                    TotPoint++;
                    nbPoint++;                    
                    nbWp++;
                }
                // compute duration with end point  
            }
            // last point used
            LatAterro = Point1.Latitude;
            LongAterro = Point1.Longitude;
            Alt_Attero_GPS = Point1.AltiGPS;                                     
            NbPoints = TotPoint;
            Decodage = true;
        } catch (Exception e) {
            sbError = new StringBuilder(this.getClass().getName()+"."+Thread.currentThread().getStackTrace()[1].getMethodName());
            sbError.append("\r\n").append(e.getMessage());
            sbError.append("\r\n").append("Path : ").append(pathFichier);
            mylogging.log(Level.SEVERE, sbError.toString());        
        }     
    } 
    
    public String getGeoJson () {
        String res = null;
        
        JSONObject trackJsonObjectbuilder = new JSONObject();
        trackJsonObjectbuilder.put("type", "FeatureCollection");   
        JSONArray trackArray = new JSONArray();
        trackArray.add(pointsToGeoJson());
        trackJsonObjectbuilder.put("features", trackArray);
        res = trackJsonObjectbuilder.toJSONString();        
        return res;
    }
    
    public String getUnitJson() {
        String res = null;
        
        res = pointsToGeoJson().toJSONString();
        
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
        int fin = Tb_Tot_Points.size();  
        JSONArray coordinates = new JSONArray();        
        for (int i = 0; i < fin; i++) {
            JSONArray coordinate = new JSONArray();
            pointIGC currPoint = Tb_Tot_Points.get(i);
            coordinate.add(0, currPoint.getLongitude());
            coordinate.add(1, currPoint.getLatitude());
            coordinate.add(2, currPoint.AltiGPS);
            coordinates.add(coordinate);                        
        }

        geometry.put("coordinates", coordinates);
        feature.put("geometry",geometry);
                            
        return feature;
    }    
    
}
