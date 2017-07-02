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
import java.time.format.DateTimeFormatter;
import org.xnap.commons.i18n.I18n;
import org.xnap.commons.i18n.I18nFactory;
import settings.configProg;
import trackgps.traceGPS;

/**
 *
 * @author gil
 */
public class trackSimple {
    private boolean trackOK;
    private String kmlString;
    private static final String RC = "\n";  
    private int localReduc;
    
    // Localization
    private I18n i18n; 
    
    // Paramètres de configuration
    configProg myConfig;
    
    public boolean isTrackOK() {
        return trackOK;
    }  

    public String getKmlString() {
        return kmlString;
    }        
    
    public trackSimple(traceGPS pTrace, makingKml pMakingKml) {    
        trackOK = false;
        // **** i18n = I18nFactory.getI18n(logfly.Logfly.class.getClass(),myConfig.getLocale());
        // pour debug
        i18n = I18nFactory.getI18n(getClass());
        genTrack(pTrace,pMakingKml);
    }

    private void genTrack(traceGPS KmlTrace, makingKml currMakingKml) {
        if (currMakingKml.isUseReduc())
            localReduc = currMakingKml.getKmlReduc();
        else
            localReduc = 1;
        
        StringBuilder res = new StringBuilder();
        
        try {
            res.append("          <Folder>").append(RC);  
            res.append("               <name>").append(i18n.tr("Trace avec profil")).append("</name>").append(RC);  
            res.append("               <description><![CDATA[").append(i18n.tr("Clic droit pour profil")).append("]]></description>").append(RC);  
            res.append("               <Placemark>").append(RC);  
            // Si on le met on a le nom du pilote qui se balade dans l'animation
            // res.append("                    <name>"+KmlTrace.sPilote.trim+"</name>").append(RC);  
            res.append("                    <styleUrl>#msn_track-0</styleUrl>").append(RC);  
            res.append("                    <gx:balloonVisibility>1</gx:balloonVisibility>").append(RC);  
            res.append("                    <gx:Track>").append(RC);  
            res.append("                         <altitudeMode>absolute</altitudeMode>").append(RC);  
  
            // Construction des valeurs temps
            int totalPoints = KmlTrace.Tb_Good_Points.size()-1;
            String sDate;            
            DateTimeFormatter dtfHHmm = DateTimeFormatter.ISO_LOCAL_DATE_TIME;            
            DecimalFormatSymbols decimalFormatSymbols = new DecimalFormatSymbols();
            // Imperatif -> forcer le point comme séparateur
            decimalFormatSymbols.setDecimalSeparator('.');        
            DecimalFormat decimalFormat = new DecimalFormat("###.00000", decimalFormatSymbols);  
            for (int i = 1; i <= totalPoints; i += localReduc) { 
                pointIGC currPoint = KmlTrace.Tb_Good_Points.get(i-1);
                sDate = currPoint.dHeure.format(dtfHHmm);   // sortie -> "2011-12-03T10:15:30Z"
                res.append("                         <when>").append(sDate).append("Z</when>").append(RC); 
            }
            for (int i = 1; i <= totalPoints; i += localReduc) { 
                pointIGC currPoint = KmlTrace.Tb_Good_Points.get(i-1);
                if(currMakingKml.isGraphAltiBaro())  {
                    res.append("                         <gx:coord>").append(decimalFormat.format(currPoint.Longitude)).append(" ");
                    res.append(decimalFormat.format(currPoint.Latitude)).append(" ").append(String.valueOf(currPoint.AltiBaro)).append("</gx:coord>").append(RC);  
                } else {
                    res.append("                         <gx:coord>").append(decimalFormat.format(currPoint.Longitude)).append(" ");
                    res.append(decimalFormat.format(currPoint.Latitude)).append(" ").append(String.valueOf(currPoint.AltiGPS)).append("</gx:coord>").append(RC);  
                }
            }           
            
            // Addition d'un model pour le replay avec dessin sketchup  A COMPLETER
            //if (currMakingKml.isWithModel()) -> then s = s + kml_C_Model
            
            // Addition des valeurs de vario sur le profil d'élévation  in xLogfly -> kml_C_Ext_Data 
            res.append( "                         <ExtendedData>").append(RC); 
            res.append( "                            <SchemaData schemaUrl=\"#schema\">").append(RC); 
            res.append( "                               <gx:SimpleArrayData name=\"").append(i18n.tr("Vario")).append("\">").append(RC); 
            for (int i = 1; i <= totalPoints; i += localReduc) { 
                pointIGC currPoint = KmlTrace.Tb_Good_Points.get(i);
                // On fait l'impasse sur la vérification de la valeur de vario in xLogfly -> mValeur_Verifiee
                 res.append( "                                  <gx:value>").append(String.format("%2.2f",currPoint.Vario)).append("</gx:value>").append(RC); 
            }
            res.append( "                               </gx:SimpleArrayData>").append(RC); 
            res.append( "                            </SchemaData>").append(RC); 
            res.append( "                         </ExtendedData>").append(RC); 
            
            res.append("                    </gx:Track>").append(RC);  
            res.append("               </Placemark>").append(RC);  
            res.append("          </Folder>").append(RC);  
            kmlString = res.toString();
            trackOK = true;
        } catch (Exception e) {
            trackOK = false;
        }     
    }
    
}
