/*
 * Copyright Gil THOMAS
 * Ce fichier fait partie intégrante du projet Logfly
 * Pour tous les détails sur la licence du projet Logfly
 * Consulter le fichier LICENSE distribué avec le code source
 */
package kml;

import igc.pointIGC;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;
import javax.management.StringValueExp;
import org.xnap.commons.i18n.I18n;
import org.xnap.commons.i18n.I18nFactory;
import settings.configProg;
import trackgps.thermique;
import trackgps.traceGPS;

/**
 *
 * @author Man's costumes Gil
 * Les procédures de cette classe peuvent paraitre étranges...
 * elles ont été traduites d' xLogfly
 * où elles avaient été traduites du PHP (moulinette Man's)
 * 
 */
public class trackThermals {
    private boolean thermalOK;
    private String kmlString;
    private static final String RC = "\n";   
    int totalPoints;
    private traceGPS KmlTrace;
    private makingKml currMakingKml;
    DecimalFormat fmtsigne = new DecimalFormat("+#0.00;-#");
    DecimalFormat decimalFormat;
    
    // Localization
    private I18n i18n; 
    
    // Paramètres de configuration
    configProg myConfig;
    
    public String getKmlString() {
        return kmlString;
    } 
    
    public trackThermals(traceGPS pTrace, makingKml pMakingKml) {    
        thermalOK = false;
        KmlTrace = pTrace;
        currMakingKml = pMakingKml;
        // **** i18n = I18nFactory.getI18n(logfly.Logfly.class.getClass(),myConfig.getLocale());
        // pour debug
        i18n = I18nFactory.getI18n(getClass());       
        totalPoints = KmlTrace.Tb_Good_Points.size()-1;
        DecimalFormatSymbols decimalFormatSymbols = new DecimalFormatSymbols();
        // Imperatif -> forcer le point comme séparateur
        decimalFormatSymbols.setDecimalSeparator('.');        
        decimalFormat = new DecimalFormat("###.00000", decimalFormatSymbols);
    }
    
    public boolean genThermals() {
        StringBuilder res = new StringBuilder();
        boolean resGeneration = false;
        
        try {
            res.append("     <Folder>").append(RC); 
            res.append("          <open>1</open>").append(RC); 
            res.append("          <name>").append(i18n.tr("Analyse")).append("</name>").append(RC); 
            res.append(anaThermals());
            res.append(anaBestGain());
            res.append(anaTransitions());
            res.append(anaBestTrans());
            res.append("      </Folder>").append(RC); 
            resGeneration = true;
            kmlString = res.toString();                  
        } catch (Exception e) {
            resGeneration = false;
        }   

        return resGeneration;
    }
    
    /**
     * in xLogfly -> kml_J_Ana_Therm
     * @return 
     */
    private String anaThermals()  {
        StringBuilder res = new StringBuilder();
        String sAlti;
        int TotalPoints = KmlTrace.Tb_Thermique.size() - 1;
        int idxPtB;
        int minGain;
        thermique currTh;               
              
        if (TotalPoints > 0) {
            minGain = (int) (KmlTrace.getBestGain() * 0.10);     // On fixe un gain mini à 10% du meilleur gain
            res.append("              <Folder>").append(RC); 
            res.append("                     <styleUrl>#Liste_Coche</styleUrl>").append(RC); 
            res.append("                     <name>").append(i18n.tr("Thermiques")).append("</name>").append(RC); 
            res.append("                     <visibility>0</visibility>").append(RC); 
            for (int i = 0; i <= TotalPoints; i++) {
                // Pour l'instant on ne marque le positif
                currTh = KmlTrace.Tb_Thermique.get(i);
                if (currTh.DeltaAlt > 0 && currTh.DeltaAlt > minGain) {
                    idxPtB = currTh.NumPoint;
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
                    res.append("                           <name>").append(String.valueOf(currTh.DeltaAlt)).append(" m  ").append(fmtsigne.format(currTh.MeanVarioValue)).append(" m/s</name>").append(RC); 
                    res.append("                     </Placemark>").append(RC); 
                }
            }      
            res.append("      </Folder>").append(RC); 
        }        
        return res.toString();
    }
    
    /**
     * in xLogfly -> kml_J_Ana_B_Gain
     * @return 
     */
    private String anaBestGain() {
        StringBuilder res = new StringBuilder();
        String sAltiA,sAltiB;  
  
        res.append("              <Folder>").append(RC); 
        res.append("                     <styleUrl>#Liste_Coche</styleUrl>").append(RC); 
        res.append("                     <name>").append(i18n.tr("Meilleur gain")).append("</name>").append(RC); 
        res.append("                     <visibility>0</visibility>").append(RC); 
                
        pointIGC bestPointA = KmlTrace.Tb_Good_Points.get(KmlTrace.getBestGainIndice1());
        pointIGC bestPointB = KmlTrace.Tb_Good_Points.get(KmlTrace.getBestGainIndice2());
        
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
         
        return res.toString();        
    }
    
    /**
     * in xLogfly -> kml_J_Ana_Trans
     * @return 
     */
    private String anaTransitions() {        
        StringBuilder res = new StringBuilder();
        String sAltiA,sAltiB;     
        double dDist,MinDist;
        String sDist;
        double Finesse;
        int IdxPtB,IdxPtA;
        thermique currTh;
  
        int TotalPoints = KmlTrace.Tb_Thermique.size() - 1;        
        MinDist = (KmlTrace.getBestTransDist()/1000) * 0.10;     // On fixe une distance mini de transition à 10% de la meilleure transition (en km)
        if (TotalPoints > 0) {
            res.append("              <Folder>").append(RC); 
            res.append("                     <styleUrl>#Liste_Coche</styleUrl>").append(RC); 
            res.append("                     <name>").append(i18n.tr("Transitions")).append("</name>").append(RC); 
            res.append("                     <visibility>0</visibility>").append(RC); 
            for (int i = 0; i <= TotalPoints; i++) {
                currTh = KmlTrace.Tb_Thermique.get(i);
                if (currTh.DeltaAlt < 0) {
                    IdxPtB = currTh.NumPoint;        
                    if (i < 1) 
                        IdxPtA = 0;
                    else
                        IdxPtA = KmlTrace.Tb_Thermique.get(i-1).NumPoint;    
                    pointIGC pointA = KmlTrace.Tb_Good_Points.get(IdxPtA);
                    pointIGC pointB = KmlTrace.Tb_Good_Points.get(IdxPtB);
                    dDist = (geoutils.trigo.CoordDistance(pointB.Latitude,pointB.Longitude,pointA.Latitude,pointA.Longitude))/1000;                    
                    if (dDist > MinDist) {
                        if (currMakingKml.isGraphAltiBaro()) { 
                            sAltiB = String.valueOf(pointB.AltiBaro);
                            sAltiA = String.valueOf(pointA.AltiBaro);
                        } else {
                            sAltiB = String.valueOf(pointB.AltiGPS);
                            sAltiA = String.valueOf(pointA.AltiGPS);    
                        }
                        // locale.ROOT -> impose le point comme séparateur décimal sinon en config FR, on aura la virgule
                        sDist = String.format(Locale.ROOT,"%5.2f",dDist);
                        if (currTh.DeltaAlt != 0)
                            Finesse = -dDist / currTh.DeltaAlt;
                        else
                            Finesse = -dDist / 1;
                        res.append("                     <Placemark>").append(RC); 
                        res.append("                           <Point>").append(RC); 
                        res.append("                                  <altitudeMode>absolute</altitudeMode>").append(RC); 
                        res.append("                                  <coordinates>").append(decimalFormat.format(pointB.Longitude)).append(",");
                        res.append(decimalFormat.format(pointB.Latitude)).append(",").append(sAltiB).append("</coordinates>").append(RC); 
                        res.append("                           </Point>").append(RC); 
                        res.append("                           <Snippet/>").append(RC); 
                        res.append("                           <styleUrl>#Point_Rouge</styleUrl>").append(RC); 
                        res.append("                           <name>").append(sDist).append( "km ").append(String.format(Locale.ROOT,"%2.1f",currTh.GlideRatioValue)).append("</name>").append(RC); 
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
                    }
                }
            }
            res.append("      </Folder>").append(RC); 
        }
        
        return res.toString();
    }
    
    
    /**
     * in xLogfly -> kml_J_Ana_B_Trans
     * @return 
     */
    private String anaBestTrans() {        
        StringBuilder res = new StringBuilder();
        
        String sAltiA,sAltiB;        
        String sDist;
       
        res.append("              <Folder>").append(RC); 
        res.append("                     <styleUrl>#Liste_Coche</styleUrl>").append(RC); 
        res.append("                     <name>").append(i18n.tr("Meilleure transition")).append("</name>").append(RC); 
        res.append("                     <visibility>0</visibility>").append(RC); 
  
        pointIGC bestPointA = KmlTrace.Tb_Good_Points.get(KmlTrace.getBestTransIndice1());
        pointIGC bestPointB = KmlTrace.Tb_Good_Points.get(KmlTrace.getBestTransIndice2());
        if (currMakingKml.isGraphAltiBaro()) {
            sAltiB = String.valueOf(bestPointB.AltiBaro);
            sAltiA = String.valueOf(bestPointA.AltiBaro);
        } else {
            sAltiB = String.valueOf(bestPointB.AltiGPS);
            sAltiA = String.valueOf(bestPointA.AltiGPS);     
        }
        // locale.ROOT -> impose le point comme séparateur décimal sinon en config FR, on aura la virgule
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
        
        return res.toString(); 
    }
}
