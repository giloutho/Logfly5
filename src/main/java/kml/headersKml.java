/*
 * Copyright Gil THOMAS
 * Ce fichier fait partie intégrante du projet Logfly
 * Pour tous les détails sur la licence du projet Logfly
 * Consulter le fichier LICENSE distribué avec le code source
 */
package kml;

import igc.pointIGC;
import java.time.format.DateTimeFormatter;
import javax.management.StringValueExp;
import org.xnap.commons.i18n.I18n;
import org.xnap.commons.i18n.I18nFactory;
import settings.configProg;
import trackgps.traceGPS;

/**
 *
 * @author gil
 * 
 * Nom des variables héritées d'xLogfly pour faciliter la récupération du code
 * 
 */
public class headersKml {
        
    private boolean headerOK;
    private String kmlString;
    private static final String RC = "\n";   
    
    // Localization
    private I18n i18n; 
    
    // Paramètres de configuration
    configProg myConfig;
    
    public boolean isHeaderOK() {
        return headerOK;
    }  

    public String getKmlString() {
        return kmlString;
    }        
    
    public headersKml(traceGPS pTrace, makingKml pMakingKml) {    
        headerOK = false;
        // **** i18n = I18nFactory.getI18n(logfly.Logfly.class.getClass(),myConfig.getLocale());
        // pour debug
        i18n = I18nFactory.getI18n(getClass());
        genHeader(pTrace,pMakingKml);
    }    
          
    
    private void genHeader(traceGPS KmlTrace, makingKml currMakingKml)  {
        StringBuilder res = new StringBuilder();
        
        try {
            // Ecriture de l'en tête XML
            res.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>").append(RC);
            res.append("<kml xmlns=\"http://www.opengis.net/kml/2.2\"").append(RC);
            // This namespace URI must be added to the <kml> element in any KML file using gx-prefixed elements:
            res.append(" xmlns:gx=\"http://www.google.com/kml/ext/2.2\">").append(RC);  
            res.append("<Document>").append(RC);
            res.append("     <open>1</open>").append(RC);
            // On génère le nom global du document
            if (KmlTrace.getsPilote() != "") 
                res.append("     <name>").append(KmlTrace.getsPilote()).append("</name>").append(RC);
            else
                res.append("     <name>Pilot X</name>").append(RC);

            // Génération du panneau descriptif
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

            // Pour les valeurs qui suivent, On élimine les vérifications faites dans xLogfly
            // avec des chaines comme -NAN(000).00, -INF.00 ou -1.#J qui faisaient planter l'affichage du kml
            // en principe, nous ne devrions pas avoir le problème avec String.format
            // Gain max         
            res.append("        <tr bgcolor=\"#ffffff\"><th align=\"right\">").append(i18n.tr("Gain max")).append("</th><td>").append(String.valueOf(KmlTrace.getBestGain())).append("m</td></tr>").append(RC);
            // Vario Max
            pointIGC ptVarioMax = KmlTrace.getVario_Max();                    
            res.append("        <tr bgcolor=\"#dddddd\"><th align=\"right\">").append(i18n.tr("Vario max")).append("</th><td>").append(String.format("%2.2f",ptVarioMax.Vario)).append(" m/s</td>").append(RC);
            // Vario Mini
            pointIGC ptVarioMini = KmlTrace.getVario_Mini();
            res.append("        <tr bgcolor=\"#ffffff\"><th align=\"right\">").append(i18n.tr("Vario mini")).append("</th><td>").append(String.format("%2.2f",ptVarioMini.Vario)).append(" m/s</td></tr>").append(RC);
            // Vitesse Max
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
