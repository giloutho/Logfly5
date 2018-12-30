/* 
 * Copyright Gil THOMAS
 * This file forms an integral part of Logfly project
 * See the LICENSE file distributed with source code
 * for details of Logfly licence project
 */
package kml;

import igc.pointIGC;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import org.xnap.commons.i18n.I18n;
import org.xnap.commons.i18n.I18nFactory;
import settings.configProg;
import trackgps.traceGPS;

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
            res.append("        <tr bgcolor=\"#dddddd\\\"><th align=\"right\">").append(i18n.tr("Pilot name")).append("</th><td>").append(KmlTrace.getsPilote()).append("</td></tr>").append(RC);
            res.append("        <tr bgcolor=\"#ffffff\"><th align=\"right\">").append("Voile").append("</th><td>").append(KmlTrace.getsVoile()).append("</td></tr>").append(RC);
            String hDeco = KmlTrace.getDT_Deco().format(DateTimeFormatter.ofPattern("HH:mm")); 
            res.append("        <tr bgcolor=\"#ffffff\"><th align=\"right\">").append(i18n.tr("Take-off time")).append("</th><td>").append(hDeco).append("</td></tr>").append(RC);
            String hAtterro = KmlTrace.getDT_Attero().format(DateTimeFormatter.ofPattern("HH:mm")); 
            res.append("        <tr bgcolor=\"#dddddd\"><th align=\"right\">").append(i18n.tr("Landing time")).append("</th><td>").append(hAtterro).append("</td></tr>").append(RC);
            res.append("        <tr bgcolor=\"#ffffff\"><th align=\"right\">").append("Durée").append("</th><td>").append(KmlTrace.getsDuree_Vol()).append("</td></tr>").append(RC);

            if (currMakingKml.isGraphAltiBaro()) {
                res.append("        <tr bgcolor=\"#dddddd\"><th align=\"right\">").append(i18n.tr("Take-off Baro Alt")).append("</th><td>").append(String.valueOf(KmlTrace.getAlt_Deco_Baro())).append(" m</td></tr>").append(RC);
                pointIGC ptAltMax = KmlTrace.getAlt_Maxi_Baro();
                res.append("        <tr bgcolor=\"#ffffff\"><th align=\"right\">").append(i18n.tr("Max Baro Alt")).append("</th><td>").append(String.valueOf(ptAltMax.AltiBaro)).append(" m</td></tr>").append(RC);
                pointIGC ptAltMini = KmlTrace.getAlt_Mini_Baro();
                res.append("        <tr bgcolor=\"#ffffff\"><th align=\"right\">").append(i18n.tr("Min baro alt")).append("</th><td>").append(String.valueOf(ptAltMini.AltiBaro)).append(" m</td></tr>").append(RC);
                res.append("        <tr bgcolor=\"#ffffff\"><th align=\"right\">").append(i18n.tr("Landing Baro Alt")).append("</th><td>").append(String.valueOf(KmlTrace.getAlt_Attero_Baro())).append(" m</td></tr>").append(RC);
            }  else {
                res.append("        <tr bgcolor=\"#dddddd\"><th align=\"right\">").append(i18n.tr("Take-off GPS Alt")).append("</th><td>").append(String.valueOf(KmlTrace.getAlt_Deco_GPS())).append(" m</td></tr>").append(RC);
                pointIGC ptAltMax = KmlTrace.getAlt_Maxi_GPS();
                res.append("        <tr bgcolor=\"#ffffff\"><th align=\"right\">").append(i18n.tr("Max GPS Alt")).append("</th><td>").append(String.valueOf(ptAltMax.AltiGPS)).append(" m</td></tr>").append(RC);
                pointIGC ptAltMini = KmlTrace.getAlt_Mini_GPS();
                res.append("        <tr bgcolor=\"#ffffff\"><th align=\"right\">").append(i18n.tr("Min GPS Alt")).append("</th><td>").append(String.valueOf(ptAltMini.AltiGPS)).append(" m</td></tr>").append(RC);
                res.append("        <tr bgcolor=\"#ffffff\"><th align=\"right\">").append(i18n.tr("Landing GPS Alt")).append("</th><td>").append(String.valueOf(KmlTrace.getAlt_Attero_GPS())).append(" m</td></tr>").append(RC);
            }

            // in xLogfly, we must check values to avoid some strings like -NAN(000).00, -INF.00 ou -1.#J 
            // They crashed kml display
            // Normally with String.format, we don't need to check
            // Max gain          
            res.append("        <tr bgcolor=\"#ffffff\"><th align=\"right\">").append(i18n.tr("Max gain")).append("</th><td>").append(String.valueOf(KmlTrace.getBestGain())).append("m</td></tr>").append(RC);
            // Max vario 
            pointIGC ptVarioMax = KmlTrace.getVario_Max();                    
            res.append("        <tr bgcolor=\"#dddddd\"><th align=\"right\">").append(i18n.tr("Max climb")).append("</th><td>").append(String.format("%2.2f",ptVarioMax.Vario)).append(" m/s</td>").append(RC);
            // Mini vario
            pointIGC ptVarioMini = KmlTrace.getVario_Mini();
            res.append("        <tr bgcolor=\"#ffffff\"><th align=\"right\">").append(i18n.tr("Max sink")).append("</th><td>").append(String.format("%2.2f",ptVarioMini.Vario)).append(" m/s</td></tr>").append(RC);
            // Max speed
            pointIGC ptVitMax = KmlTrace.getVit_Max();        
            res.append("        <tr bgcolor=\"#dddddd\"><th align=\"right\">").append(i18n.tr("Max speed")).append("</th><td>").append(String.format("%3.2f",ptVitMax.Vitesse)).append(" km/h</td></tr></table>").append(RC);

            res.append("        <br><br><a href='http://www.logfly.org/index.php?id=google-earth-utilisation&lang=fr' target='_blank'>").append(i18n.tr("Help for exploitation of this kml file")).append("</a>]]>").append(RC);
            res.append("     </description>").append(RC);
            res.append("     <Snippet maxLines=\"1\"> ").append(KmlTrace.getsDate_Vol()).append("  ").append(hDeco).append(" ").append(hAtterro).append("</Snippet>").append(RC);                
            kmlString = res.toString();
            headerOK = true;
        } catch (Exception e) {
            headerOK = false;
        }     
    }
}
