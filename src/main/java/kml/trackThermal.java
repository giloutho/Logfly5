/* 
 * Copyright Gil THOMAS
 * This file forms an integral part of Logfly project
 * See the LICENSE file distributed with source code
 * for details of Logfly licence project
 */
package kml;

import igc.pointIGC;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;
import org.xnap.commons.i18n.I18n;
import org.xnap.commons.i18n.I18nFactory;
import trackgps.analyse;
import trackgps.remarkable;
import trackgps.traceGPS;

/**
 *
 * @author Gil with great help of Emmanuel Chabani aka Man's
 * Translated in xLogfly from Man's php script 
 * Draw thermals of the flight
 * 
 */
public class trackThermal {
    private boolean thermalOK;
    private String kmlString;
    private static final String RC = "\n";   
    int totalPoints;
    private traceGPS KmlTrace;
    private makingKml currMakingKml;
    private DecimalFormat fmtsigne = new DecimalFormat("+#0.00;-#");
    private DecimalFormat decimalFormat;
    private analyse trackAnalyze;
    private String kmlBestGain;
    private String kmlBestTransition;
    
    // Localization
    private I18n i18n; 
        
    public String getKmlString() {
        return kmlString;
    } 
    
    public trackThermal(traceGPS pTrace, makingKml pMakingKml, Locale currLocale) {    
        thermalOK = false;
        KmlTrace = pTrace;
        currMakingKml = pMakingKml;
        i18n = I18nFactory.getI18n("","lang/Messages",trackThermal.class.getClass().getClassLoader(),currLocale,0);
        totalPoints = KmlTrace.Tb_Good_Points.size()-1;
        DecimalFormatSymbols decimalFormatSymbols = new DecimalFormatSymbols();
        decimalFormatSymbols.setDecimalSeparator('.');        
        decimalFormat = new DecimalFormat("###.00000", decimalFormatSymbols);
        
        trackAnalyze = new analyse(pTrace);
    }
    
    /**
     * Manage generation
     * @return 
     */
    public boolean genThermals() {
        StringBuilder res = new StringBuilder();
        boolean resGeneration = false;
        
        try {
            res.append("     <Folder>").append(RC); 
            res.append("          <open>1</open>").append(RC); 
            res.append("          <name>").append(i18n.tr("Analyze")).append("</name>").append(RC); 
            res.append(anaThermals());            
            res.append(kmlBestGain);
            res.append(anaTransitions());
            res.append(kmlBestTransition);
            res.append("      </Folder>").append(RC); 
            resGeneration = true;
            kmlString = res.toString();                  
        } catch (Exception e) {
            resGeneration = false;
        }   

        return resGeneration;
    }
    
    /**
     * Place thermals along the track
     * in xLogfly -> kml_J_Ana_Therm
     * @return 
     */
    private String anaThermals()  {
        StringBuilder res = new StringBuilder();
        String sAlti;
        int TotalPoints = trackAnalyze.finalThermals.size();
        int idxPtB;            
              
        if (TotalPoints > 0) {
            res.append("              <Folder>").append(RC); 
            res.append("                     <styleUrl>#Liste_Coche</styleUrl>").append(RC); 
            res.append("                     <name>").append(i18n.tr("Thermals")).append("</name>").append(RC); 
            res.append("                     <visibility>0</visibility>").append(RC); 
            for (int i = 0; i < TotalPoints; i++) {
                // For instance only positive values
                remarkable currRmk = trackAnalyze.finalThermals.get(i);  
                idxPtB = currRmk.getIdxEnd();    
                pointIGC currPoint = KmlTrace.Tb_Good_Points.get(idxPtB);
                if (currMakingKml.isGraphAltiBaro()) 
                    sAlti = String.valueOf(currPoint.AltiBaro);
                else
                    sAlti = String.valueOf(currPoint.AltiGPS);                              
                res.append("                     <Placemark>").append(RC); 
                res.append("                           <Point>").append(RC); 
                res.append("                                  <altitudeMode>absolute</altitudeMode>").append(RC); 
                res.append("                                  <coordinates>").append(decimalFormat.format(currPoint.Longitude)).append(",");
                res.append(decimalFormat.format(currPoint.Latitude)).append(",").append(sAlti).append("</coordinates>").append(RC); 
                res.append("                           </Point>").append(RC); 
                res.append("                           <Snippet/>").append(RC); 
                res.append("                           <styleUrl>#Point_Rouge</styleUrl>").append(RC); 
                res.append("                           <name>").append(String.valueOf(currRmk.getDeltaAlt())).append(" m  ").append(fmtsigne.format(currRmk.getMeanVario())).append(" m/s</name>").append(RC); 
                res.append("                     </Placemark>").append(RC); 
                 if (currRmk.getIdxEnd() == trackAnalyze.getBestGainEnd()) {
                     anaBestGain(currRmk);
                 }
                
            }      
            res.append("      </Folder>").append(RC); 
        }        
        return res.toString();
    }
    
    /**
     * Special for the best gain of the flight
     * in xLogfly -> kml_J_Ana_B_Gain
     * @return 
     */
    private  void anaBestGain(remarkable bestTh) {
        StringBuilder res = new StringBuilder();
        String sAltiA,sAltiB;  
  
        res.append("              <Folder>").append(RC); 
        res.append("                     <styleUrl>#Liste_Coche</styleUrl>").append(RC); 
        res.append("                     <name>").append(i18n.tr("Best gain")).append("</name>").append(RC); 
        res.append("                     <visibility>0</visibility>").append(RC); 
                
        pointIGC bestPointA = KmlTrace.Tb_Good_Points.get(bestTh.getIdxStart());              
        pointIGC bestPointB  = KmlTrace.Tb_Good_Points.get(bestTh.getIdxEnd());         
        
        if (currMakingKml.isGraphAltiBaro()) {
            sAltiB = String.valueOf(bestPointB.AltiBaro);
            sAltiA = String.valueOf(bestPointA.AltiBaro);
        } else {
            sAltiB = String.valueOf(bestPointB.AltiGPS);
            sAltiA = String.valueOf(bestPointA.AltiGPS);     
        }
        res.append("                     <Placemark>").append(RC); 
        res.append("                           <Point>").append(RC); 
        res.append("                                  <altitudeMode>absolute</altitudeMode>").append(RC); 
        res.append("                                  <coordinates>").append(decimalFormat.format(bestPointA.Longitude)).append(",");
        res.append(decimalFormat.format(bestPointA.Latitude)).append(",").append(sAltiA).append("</coordinates>").append(RC); 
        res.append("                           </Point>").append(RC); 
        res.append("                           <Snippet/>").append(RC); 
        res.append("                           <styleUrl>#Pushpin_P</styleUrl>").append(RC); 
        res.append("                           <name>").append(sAltiA).append(" m</name>").append(RC); 
        res.append("                     </Placemark>").append(RC); 
        res.append("                     <Placemark>").append(RC); 
        res.append("                           <Point>").append(RC); 
        res.append("                                  <altitudeMode>absolute</altitudeMode>").append(RC); 
        res.append("                                  <coordinates>").append(decimalFormat.format(bestPointB.Longitude)).append(",");
        res.append(decimalFormat.format(bestPointB.Latitude)).append(",").append(sAltiB).append("</coordinates>").append(RC); 
        res.append("                           </Point>").append(RC); 
        res.append("                           <Snippet/>").append(RC); 
        res.append("                           <styleUrl>#Pushpin_P</styleUrl>").append(RC); 
        res.append("                           <name>").append(sAltiB).append(" m</name>").append(RC); ;
        res.append("                     </Placemark>").append(RC); 
        res.append("                     <Placemark>").append(RC); 
        res.append("                           <name>").append(String.valueOf(KmlTrace.getBestGain())).append(" m</name>").append(RC); 
        res.append("                           <styleUrl>#Line_Gain</styleUrl>").append(RC); 
        res.append("                           <LineString>").append(RC); 
        res.append("                                  <altitudeMode>absolute</altitudeMode>").append(RC); 
        res.append("                                  <coordinates>").append(decimalFormat.format(bestPointA.Longitude)).append(",");
        res.append(decimalFormat.format(bestPointA.Latitude)).append(",").append(sAltiA).append(" ").append(decimalFormat.format(bestPointB.Longitude)).append(",");
        res.append(decimalFormat.format(bestPointB.Latitude)).append(",").append(sAltiB).append("</coordinates>").append(RC); 
        res.append("                           </LineString>").append(RC); 
        res.append("                     </Placemark>").append(RC); 
        res.append("      </Folder>").append(RC);  
         
        kmlBestGain = res.toString();        
    }
    
    /**
     * Place transitions along the track
     * in xLogfly -> kml_J_Ana_Trans
     * @return 
     */
    private String anaTransitions() {        
        StringBuilder res = new StringBuilder();
        String sAltiA,sAltiB;     
  
        int TotalPoints = trackAnalyze.finalGlides.size();           
        if (TotalPoints > 0) {
            res.append("              <Folder>").append(RC); 
            res.append("                     <styleUrl>#Liste_Coche</styleUrl>").append(RC); 
            res.append("                     <name>").append(i18n.tr("Transitions")).append("</name>").append(RC); 
            res.append("                     <visibility>0</visibility>").append(RC); 
            for (int i = 0; i < TotalPoints; i++) {
                remarkable currRmk = trackAnalyze.finalGlides.get(i);   
                pointIGC pointA = KmlTrace.Tb_Good_Points.get(currRmk.getIdxStart());              
                pointIGC pointB = KmlTrace.Tb_Good_Points.get(currRmk.getIdxEnd());             
                if (currMakingKml.isGraphAltiBaro()) { 
                    sAltiB = String.valueOf(pointB.AltiBaro);
                    sAltiA = String.valueOf(pointA.AltiBaro);
                } else {
                    sAltiB = String.valueOf(pointB.AltiGPS);
                    sAltiA = String.valueOf(pointA.AltiGPS);    
                }
                res.append("                     <Placemark>").append(RC); 
                res.append("                           <Point>").append(RC); 
                res.append("                                  <altitudeMode>absolute</altitudeMode>").append(RC); 
                res.append("                                  <coordinates>").append(decimalFormat.format(pointB.Longitude)).append(",");
                res.append(decimalFormat.format(pointB.Latitude)).append(",").append(sAltiB).append("</coordinates>").append(RC); 
                res.append("                           </Point>").append(RC); 
                res.append("                           <Snippet/>").append(RC); 
                res.append("                           <styleUrl>#Point_Rouge</styleUrl>").append(RC); 
                res.append("                           <name>").append(String.format(Locale.ROOT,"%2.1f",currRmk.getDistance())).append( "km ");
                res.append(String.format(Locale.ROOT,"%2.1f",currRmk.getAverage_ld())).append("</name>").append(RC); 
                res.append("                     </Placemark>").append(RC); 
                res.append("                     <Placemark>").append(RC); 
                res.append("                           <Point>").append(RC); 
                res.append("                                  <altitudeMode>absolute</altitudeMode>").append(RC); 
                res.append("                                  <coordinates>").append(decimalFormat.format(pointA.Longitude)).append(",");
                res.append(decimalFormat.format(pointA.Latitude)).append(",").append(sAltiA).append("</coordinates>").append(RC); 
                res.append("                           </Point>").append(RC); 
                res.append("                           <Snippet/>").append(RC); 
                res.append("                           <styleUrl>#Point_Rouge</styleUrl>").append(RC); 
                res.append("                     </Placemark>").append(RC); 
                res.append("                     <Placemark>").append(RC); 
                res.append("                           <styleUrl>#Line_Trans</styleUrl>").append(RC); 
                res.append("                           <LineString>").append(RC); 
                res.append("                                  <altitudeMode>absolute</altitudeMode>").append(RC); 
                res.append("                                  <coordinates>").append(decimalFormat.format(pointA.Longitude)).append(",");
                res.append(decimalFormat.format(pointA.Latitude)).append(",").append(sAltiA).append(" ").append(decimalFormat.format(pointB.Longitude));
                res.append(",").append(decimalFormat.format(pointB.Latitude)).append(",").append(sAltiB).append("</coordinates>").append(RC); 
                res.append("                           </LineString>").append(RC); 
                res.append("                     </Placemark>").append(RC); 
                if (currRmk.getIdxEnd() == trackAnalyze.getBestGlideEnd()) {
                    anaBestTrans(currRmk);
                }    
            }
            res.append("      </Folder>").append(RC); 
        }
        
        return res.toString();
    }
    
    
    /**
     * Special for the best transition of the flight
     * in xLogfly -> kml_J_Ana_B_Trans
     * @return 
     */
    private void anaBestTrans(remarkable bestTrans) {        
        StringBuilder res = new StringBuilder();
        
        String sAltiA,sAltiB;        
        String sDist;
       
        res.append("              <Folder>").append(RC); 
        res.append("                     <styleUrl>#Liste_Coche</styleUrl>").append(RC); 
        res.append("                     <name>").append(i18n.tr("Best transition")).append("</name>").append(RC); 
        res.append("                     <visibility>0</visibility>").append(RC); 
  
        pointIGC bestPointA = KmlTrace.Tb_Good_Points.get(bestTrans.getIdxStart());              
        pointIGC bestPointB  = KmlTrace.Tb_Good_Points.get(bestTrans.getIdxEnd());    
        if (currMakingKml.isGraphAltiBaro()) {
            sAltiB = String.valueOf(bestPointB.AltiBaro);
            sAltiA = String.valueOf(bestPointA.AltiBaro);
        } else {
            sAltiB = String.valueOf(bestPointB.AltiGPS);
            sAltiA = String.valueOf(bestPointA.AltiGPS);     
        }
        // Mandatory : point as decimal separator
        sDist = String.format(Locale.ROOT,"%5.2f",(KmlTrace.getBestTransDist()/1000));
        res.append("                     <Placemark>").append(RC); 
        res.append("                           <Point>").append(RC); 
        res.append("                                  <altitudeMode>absolute</altitudeMode>").append(RC); 
        res.append("                                  <coordinates>").append(decimalFormat.format(bestPointA.Longitude)).append(",");
        res.append(decimalFormat.format(bestPointA.Latitude)).append(",").append(sAltiA).append("</coordinates>").append(RC); 
        res.append("                           </Point>").append(RC); 
        res.append("                           <Snippet/>").append(RC); 
        res.append("                           <styleUrl>#Pushpin_G</styleUrl>").append(RC); 
        res.append("                           <name>").append(sAltiA).append(" m</name>").append(RC); 
        res.append("                     </Placemark>").append(RC); 
        res.append("                     <Placemark>").append(RC); 
        res.append("                           <Point>").append(RC); 
        res.append("                                  <altitudeMode>absolute</altitudeMode>").append(RC); 
        res.append("                                  <coordinates>").append(decimalFormat.format(bestPointB.Longitude)).append(",");
        res.append(decimalFormat.format(bestPointB.Latitude)).append(",").append(sAltiB).append("</coordinates>").append(RC); 
        res.append("                           </Point>").append(RC); 
        res.append("                           <Snippet/>").append(RC); 
        res.append("                           <styleUrl>#Pushpin_G</styleUrl>").append(RC); 
        res.append("                           <name>").append(sAltiB).append(" m</name>").append(RC); 
        res.append("                     </Placemark>").append(RC); 
        res.append("                     <Placemark>").append(RC); 
        res.append("                           <name>").append(sDist).append("km</name>").append(RC); 
        res.append("                           <styleUrl>#Line_Trans</styleUrl>").append(RC); 
        res.append("                           <LineString>").append(RC); 
        res.append("                                  <altitudeMode>absolute</altitudeMode>").append(RC); 
        res.append("                                  <coordinates>").append(decimalFormat.format(bestPointA.Longitude)).append(",");
        res.append(decimalFormat.format(bestPointA.Latitude)).append(",").append(sAltiA).append(" ").append(decimalFormat.format(bestPointB.Longitude));
        res.append(",").append(decimalFormat.format(bestPointB.Latitude)).append(",").append(sAltiB).append("</coordinates>").append(RC); 
        res.append("                           </LineString>").append(RC); 
        res.append("                     </Placemark>").append(RC); 
        res.append("      </Folder>").append(RC); 
        
        kmlBestTransition = res.toString(); 
    }
}
