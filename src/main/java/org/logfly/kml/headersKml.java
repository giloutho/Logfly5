/* 
 * Copyright Gil THOMAS
 * This file forms an integral part of Logfly project
 * See the LICENSE file distributed with source code
 * for details of Logfly licence project
 */
package org.logfly.kml;

import java.time.format.DateTimeFormatter;
import java.util.Locale;

import org.logfly.igc.pointIGC;
import org.logfly.settings.configProg;
import org.logfly.trackgps.traceGPS;
import org.xnap.commons.i18n.I18n;
import org.xnap.commons.i18n.I18nFactory;

/**
 *
 * @author gil
 * 
 * Header generation of kml file
 * Variables names herited from xLogfly for easy translation of the code
 * 
 */
public class headersKml {
        
    private boolean headerOK;
    private String kmlString;
    private static final String RC = "\n";   
    
    // Localization
    private I18n i18n; 
    
    public boolean isHeaderOK() {
        return headerOK;
    }  

    public String getKmlString() {
        return kmlString;
    }        
    
    public headersKml(traceGPS pTrace, makingKml pMakingKml,Locale currLocale) {    
        headerOK = false;
        i18n = I18nFactory.getI18n("","lang/Messages",headersKml.class.getClass().getClassLoader(),currLocale,0);
        genHeader(pTrace,pMakingKml);
    }    
          
    
    private void genHeader(traceGPS KmlTrace, makingKml currMakingKml)  {
        StringBuilder res = new StringBuilder();
        
        try {
            // XML header
            res.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>").append(RC);
            res.append("<kml xmlns=\"http://www.opengis.net/kml/2.2\"").append(RC);
            // This namespace URI must be added to the <kml> element in any KML file using gx-prefixed elements:
            res.append(" xmlns:gx=\"http://www.google.com/kml/ext/2.2\">").append(RC);  
            res.append("<Document>").append(RC);
            res.append("     <open>1</open>").append(RC);
            // global name
            if (KmlTrace.getsPilote() != "") 
                res.append("     <name>").append(KmlTrace.getsPilote()).append("</name>").append(RC);
            else
                res.append("     <name>Pilot X</name>").append(RC);

            // Descriptive Panel
            res.append("     <description><![CDATA[<table cellpadding=\"1\" cellspacing=\"1\">").append(RC);
            res.append("        <tr bgcolor=\"#dddddd\\\"><th align=\"right\">").append(i18n.tr("Nom Pilote")).append("</th><td>").append(KmlTrace.getsPilote()).append("</td></tr>").append(RC);
            res.append("        <tr bgcolor=\"#ffffff\"><th align=\"right\">").append("Voile").append("</th><td>").append(KmlTrace.getsVoile()).append("</td></tr>").append(RC);
            String hDeco = KmlTrace.getDT_Deco().format(DateTimeFormatter.ofPattern("HH:mm")); 
            res.append("        <tr bgcolor=\"#ffffff\"><th align=\"right\">").append(i18n.tr("Heure Déco")).append("</th><td>").append(hDeco).append("</td></tr>").append(RC);
            String hAtterro = KmlTrace.getDT_Attero().format(DateTimeFormatter.ofPattern("HH:mm")); 
            res.append("        <tr bgcolor=\"#dddddd\"><th align=\"right\">").append(i18n.tr("Heure Attero")).append("</th><td>").append(hAtterro).append("</td></tr>").append(RC);
            res.append("        <tr bgcolor=\"#ffffff\"><th align=\"right\">").append("Durée").append("</th><td>").append(KmlTrace.getsDuree_Vol()).append("</td></tr>").append(RC);

            if (currMakingKml.isGraphAltiBaro()) {
                res.append("        <tr bgcolor=\"#dddddd\"><th align=\"right\">").append(i18n.tr("Alt Deco Baro")).append("</th><td>").append(String.valueOf(KmlTrace.getAlt_Deco_Baro())).append(" m</td></tr>").append(RC);
                pointIGC ptAltMax = KmlTrace.getAlt_Maxi_Baro();
                res.append("        <tr bgcolor=\"#ffffff\"><th align=\"right\">").append(i18n.tr("Alti Max Baro")).append("</th><td>").append(String.valueOf(ptAltMax.AltiBaro)).append(" m</td></tr>").append(RC);
                pointIGC ptAltMini = KmlTrace.getAlt_Mini_Baro();
                res.append("        <tr bgcolor=\"#ffffff\"><th align=\"right\">").append(i18n.tr("Alti Mini Baro")).append("</th><td>").append(String.valueOf(ptAltMini.AltiBaro)).append(" m</td></tr>").append(RC);
                res.append("        <tr bgcolor=\"#ffffff\"><th align=\"right\">").append(i18n.tr("Alt Attero Baro")).append("</th><td>").append(String.valueOf(KmlTrace.getAlt_Attero_Baro())).append(" m</td></tr>").append(RC);
            }  else {
                res.append("        <tr bgcolor=\"#dddddd\"><th align=\"right\">").append(i18n.tr("Alti Déco GPS")).append("</th><td>").append(String.valueOf(KmlTrace.getAlt_Deco_GPS())).append(" m</td></tr>").append(RC);
                pointIGC ptAltMax = KmlTrace.getAlt_Maxi_GPS();
                res.append("        <tr bgcolor=\"#ffffff\"><th align=\"right\">").append(i18n.tr("Alti Max GPS")).append("</th><td>").append(String.valueOf(ptAltMax.AltiGPS)).append(" m</td></tr>").append(RC);
                pointIGC ptAltMini = KmlTrace.getAlt_Mini_GPS();
                res.append("        <tr bgcolor=\"#ffffff\"><th align=\"right\">").append(i18n.tr("Alti Min GPS")).append("</th><td>").append(String.valueOf(ptAltMini.AltiGPS)).append(" m</td></tr>").append(RC);
                res.append("        <tr bgcolor=\"#ffffff\"><th align=\"right\">").append(i18n.tr("Alt Attero GPS")).append("</th><td>").append(String.valueOf(KmlTrace.getAlt_Attero_GPS())).append(" m</td></tr>").append(RC);
            }

            // in xLogfly, we must check values to avoid some strings like -NAN(000).00, -INF.00 ou -1.#J 
            // They crashed kml display
            // Normally with String.format, we don't need to check
            // Max gain          
            res.append("        <tr bgcolor=\"#ffffff\"><th align=\"right\">").append(i18n.tr("Gain max")).append("</th><td>").append(String.valueOf(KmlTrace.getBestGain())).append("m</td></tr>").append(RC);
            // Max vario 
            pointIGC ptVarioMax = KmlTrace.getVario_Max();                    
            res.append("        <tr bgcolor=\"#dddddd\"><th align=\"right\">").append(i18n.tr("Vario max")).append("</th><td>").append(String.format("%2.2f",ptVarioMax.Vario)).append(" m/s</td>").append(RC);
            // Mini vario
            pointIGC ptVarioMini = KmlTrace.getVario_Mini();
            res.append("        <tr bgcolor=\"#ffffff\"><th align=\"right\">").append(i18n.tr("Vario mini")).append("</th><td>").append(String.format("%2.2f",ptVarioMini.Vario)).append(" m/s</td></tr>").append(RC);
            // Max speed
            pointIGC ptVitMax = KmlTrace.getVit_Max();        
            res.append("        <tr bgcolor=\"#dddddd\"><th align=\"right\">").append(i18n.tr("Vitesse max")).append("</th><td>").append(String.format("%3.2f",ptVitMax.Vitesse)).append(" km/h</td></tr></table>").append(RC);

            res.append("        <br><br><a href='http://www.logfly.org/index.php?id=google-earth-utilisation&lang=fr' target='_blank'>").append(i18n.tr("Aide sur l'exploitation de ce fichier kml")).append("</a>]]>").append(RC);
            res.append("     </description>").append(RC);
            res.append("     <Snippet maxLines=\"1\"> ").append(KmlTrace.getsDate_Vol()).append("  ").append(hDeco).append(" ").append(hAtterro).append("</Snippet>").append(RC);                
            kmlString = res.toString();
            headerOK = true;
        } catch (Exception e) {
            headerOK = false;
        }     
    }
}
