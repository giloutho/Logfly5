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
import settings.configProg;
import trackgps.traceGPS;

/**
 *
 * @author gil
 * 
 * Manage generation of track replay in Google Earth
 */
public class trackReplay {
    
    private boolean replayOK;
    private String kmlString;
    private static final String RC = "\n";
    int totalPoints;
    private traceGPS KmlTrace;
    private makingKml currMakingKml;
    DecimalFormat fmtsigne = new DecimalFormat("+#0.00;-#");
    DecimalFormat decimalFormat;
    private int localReduc;
    
    // Paramètres replay
    private int camDessus;
    private int camIncli;
    private int camRecul;
    private int camStep;
    private int camTimer;
    
    // Localization
    private I18n i18n; 
       
    public String getKmlString() {
        return kmlString;
    } 
    
    public trackReplay(traceGPS pTrace, makingKml pMakingKml,Locale currLocale) {    
        replayOK = false;
        KmlTrace = pTrace;
        currMakingKml = pMakingKml;
        i18n = I18nFactory.getI18n("","lang/Messages",trackReplay.class.getClass().getClassLoader(),currLocale,0);
        
        totalPoints = KmlTrace.Tb_Good_Points.size()-1;
        DecimalFormatSymbols decimalFormatSymbols = new DecimalFormatSymbols();
        // Mandatory : separator must be a point
        decimalFormatSymbols.setDecimalSeparator('.');        
        decimalFormat = new DecimalFormat("###.00000", decimalFormatSymbols);
        
        if (currMakingKml.isUseReduc())
            localReduc = currMakingKml.getKmlReduc();
        else
            localReduc = 1;
        
        camDessus = currMakingKml.getCamDessus();
        camIncli = currMakingKml.getCamIncli();
        camRecul = currMakingKml.getCamRecul();
        camStep = currMakingKml.getCamStep();
        camTimer = currMakingKml.getCamTimer();
    }
    
    public boolean genKmlReplay() {
        StringBuilder res = new StringBuilder();
        int totalPoints = KmlTrace.Tb_Good_Points.size()-1;
        double Bearing;
        // Step computing
        int NbMin;
        pointIGC currPoint;
        pointIGC secondPoint;
        
        boolean resGeneration = false;
        
        try {             
            NbMin = (int) (KmlTrace.getDuree_Vol() / currMakingKml.getCamStep());

            // Basic documentation https://developers.google.com/kml/documentation/touring
  
            res.append("          <gx:Tour>").append(RC); 
            res.append("             <visibility>0</visibility>").append(RC); 
            res.append("             <name>").append(i18n.tr("Refaire le vol")).append("</name>").append(RC); 
            res.append("             <gx:Playlist>").append(RC); 
            res.append("                <gx:FlyTo>").append(RC); 
            res.append("                   <gx:duration>"+String.valueOf(camTimer)+"</gx:duration>").append(RC); 
            res.append("                      <!-- bounce is the default flyToMode -->").append(RC); 
            res.append("                      <LookAt>").append(RC); 
            currPoint = KmlTrace.Tb_Good_Points.get(0);
            res.append("                         <longitude>").append(decimalFormat.format(currPoint.Longitude)).append("</longitude>").append(RC); 
            res.append("                         <latitude>").append(decimalFormat.format(currPoint.Latitude)+"</latitude>").append(RC); 
            if (currMakingKml.isGraphAltiBaro()) {
                res.append("                         <altitude>").append(String.valueOf(currPoint.AltiBaro+camDessus)).append("</altitude>").append(RC); 
            } else {
                res.append("                         <altitude>").append(String.valueOf(currPoint.AltiGPS+camDessus)).append("</altitude>").append(RC); 
            }
            // xLogfly -> Bearing = mCalc_Bearing(0,1)
            secondPoint = KmlTrace.Tb_Good_Points.get(1);                    
            Bearing = geoutils.trigo.CoordBearing(currPoint.Latitude, currPoint.Longitude,secondPoint.Latitude, secondPoint.Longitude);
            
            res.append("                         <heading>").append(String.valueOf((int)Bearing)).append("</heading>").append(RC); 
            res.append("                         <tilt>").append(String.valueOf(camIncli)).append("</tilt>").append(RC); 
            res.append("                         <range>").append(String.valueOf(camRecul)).append("</range>").append(RC); 
            res.append("                         <altitudeMode>absolute</altitudeMode>").append(RC); 
            res.append("                      </LookAt>").append(RC); 
            res.append("                   </gx:FlyTo>").append(RC); 
            for (int i = 1; i < totalPoints; i += localReduc) { 
                
                res.append("                <gx:AnimatedUpdate>").append(RC); 
                res.append("                   <Update>").append(RC); 
                res.append("                         <targetHref/>").append(RC); 
                res.append("                         <Change>").append(RC); 
                res.append("                             <ScreenOverlay targetId=\"Ov").append(String.valueOf(i)).append("\">").append(RC); 
                res.append("                                  <visibility>1</visibility>").append(RC); 
                res.append("                             </ScreenOverlay>").append(RC); 
                res.append("                         </Change>").append(RC); 
                res.append("                   </Update>").append(RC); 
                res.append("                </gx:AnimatedUpdate>").append(RC); 
    
    
                res.append("                <gx:FlyTo>").append(RC); 
                res.append("                   <gx:duration>").append(String.valueOf(camTimer)).append("</gx:duration>").append(RC); 
                res.append("                   <gx:flyToMode>smooth</gx:flyToMode>").append(RC); 
                res.append("                   <LookAt>").append(RC); 
                
                currPoint = KmlTrace.Tb_Good_Points.get(i);
                res.append("                      <longitude>").append(decimalFormat.format(currPoint.Longitude)).append("</longitude>").append(RC); 
                res.append("                      <latitude>").append(decimalFormat.format(currPoint.Latitude)).append("</latitude>").append(RC); 
                if (currMakingKml.isGraphAltiBaro()) {
                  res.append("                      <altitude>").append(String.valueOf(currPoint.AltiBaro+camDessus)).append("</altitude>").append(RC); 
                } else {
                  res.append("                      <altitude>").append(String.valueOf(currPoint.AltiGPS+camDessus)).append("</altitude>").append(RC); 
                }
                if (i + localReduc < totalPoints) {
                    // in xLogfly -> Bearing = mCalc_Bearing(i,i+Reduc)
                    secondPoint = KmlTrace.Tb_Good_Points.get(i+localReduc);
                    Bearing = geoutils.trigo.CoordBearing(currPoint.Latitude, currPoint.Longitude,secondPoint.Latitude, secondPoint.Longitude);                    
                }
                res.append("                      <heading>").append(String.valueOf((int)Bearing)).append("</heading>").append(RC); 
                res.append("                      <tilt>").append(String.valueOf(camIncli)).append("</tilt>").append(RC); 
                res.append("                      <range>").append(String.valueOf(camRecul)).append("</range>").append(RC); 
                res.append("                      <altitudeMode>absolute</altitudeMode>").append(RC); 
                res.append("                   </LookAt>").append(RC); 
                res.append("                </gx:FlyTo>").append(RC); 
            }
            // To be at landing
            currPoint = KmlTrace.Tb_Good_Points.get(totalPoints);
            res.append("                <gx:FlyTo>").append(RC); 
            res.append("                   <gx:duration>").append(String.valueOf(camTimer)).append("</gx:duration>").append(RC); 
            res.append("                      <!-- bounce is the default flyToMode -->").append(RC); 
            res.append("                      <LookAt>").append(RC); 
            res.append("                         <longitude>").append(decimalFormat.format(currPoint.Longitude)).append("</longitude>").append(RC); 
            res.append("                         <latitude>").append(decimalFormat.format(currPoint.Latitude)).append("</latitude>").append(RC); 
            if (currMakingKml.isGraphAltiBaro()) {
                res.append("                         <altitude>").append(String.valueOf(currPoint.AltiBaro)).append("</altitude>").append(RC);  
            } else {
                res.append("                         <altitude>").append(String.valueOf(currPoint.AltiGPS)).append("</altitude>").append(RC); 
           }  
            res.append("                         <heading>").append(String.valueOf((int)Bearing)).append("</heading>").append(RC); 
            res.append("                         <tilt>90</tilt>").append(RC); 
            res.append("                         <range>8</range>").append(RC); 
            res.append("                         <altitudeMode>absolute</altitudeMode>").append(RC); 
            res.append("                      </LookAt>").append(RC); 
            res.append("                   </gx:FlyTo>").append(RC); 
  
            res.append("             </gx:Playlist>").append(RC); 
            res.append("            </gx:Tour>").append(RC); 
  
            // Alt and vario overlay generation
            res.append("            <Folder>").append(RC); 
            res.append("                   <styleUrl>#Liste_Coche</styleUrl>").append(RC); 
            res.append("                   <name>Compteurs</name>").append(RC); 
            for (int i = 1; i < totalPoints; i += localReduc) { 
                res.append("                   <ScreenOverlay id=\"Ov").append(String.valueOf(i)).append("\">").append(RC); 
                res.append("                        <Icon>").append(RC); 
    
                /*
                ' This code was deactivated in xLogfly
                ' Envoi un bandeau simple de foind jaune pâle avec Altitude et Vitesse
                'if GraphAltiBaro then
                'res.append("                             <href><![CDATA[http://chart.apis.google.com/chart?chf=bg,s,FFF4C2&chs=300x20&cht=v&chd=s:&chtt=Alt+%3A+"+_
                'Str(KmlTrace.Tb_Good_Points(i).AltiBaro)+"+m++Vit+%3A+"+Format(KmlTrace.Tb_Good_Points(i).Vitesse,"###")+_
                '"+km%2Fh+++Vario+%2B2%2C5m%2Fs&chts=000000,13.5]]></href>").append(RC); 
                'else
                'res.append("                             <href><![CDATA[http://chart.apis.google.com/chart?chf=bg,s,FFF4C2&chs=300x20&cht=v&chd=s:&chtt=Alt+%3A+"+_
                'Str(KmlTrace.Tb_Good_Points(i).AltiGPS)+"+m++Vit+%3A+"+Format(KmlTrace.Tb_Good_Points(i).Vitesse,"###")+_
                '"+km%2Fh+++Vario+%2B2%2C5m%2Fs&chts=000000,13.5]]></href>").append(RC); 
                'end
                */
                currPoint = KmlTrace.Tb_Good_Points.get(i);
                // Meter with speed and altitude
                // Less detailed but prefered to digital display to avoid "Oh... This speed is bad at point 1252 "
                if (currMakingKml.isGraphAltiBaro()) {
                    res.append("                             <href><![CDATA[http://chart.apis.google.com/chart?chxl=0:%7C0%7C40%7C80&chxt=y&chs=140x90&cht=gm&chds=0,98.333&chd=t:");
                    res.append(String.valueOf(currPoint.Vitesse)).append("&chl=").append(String.valueOf(currPoint.Vitesse)).append("&chtt=Alt+%3A+");
                    res.append(String.valueOf(currPoint.AltiBaro)).append("+m]]></href>").append(RC); 
                } else {
                    res.append("                             <href><![CDATA[http://chart.apis.google.com/chart?chxl=0:%7C0%7C40%7C80&chxt=y&chs=140x90&cht=gm&chds=0,98.333&chd=t:");
                    res.append(String.valueOf(currPoint.Vitesse)).append("&chl=").append(String.valueOf(currPoint.Vitesse)).append("&chtt=Alt+%3A+");
                    res.append(String.valueOf(currPoint.AltiGPS)).append("+m]]></href>").append(RC); 
                }
                res.append("                        </Icon>").append(RC); 
                res.append("                        <overlayXY y=\"1\" x=\"0.5\" xunits=\"fraction\" yunits=\"fraction\"/>").append(RC);  // 1 c'est 100% de l'écran donc 0.5 pile poil au milieu
                res.append("                        <screenXY y=\"1\" x=\"0.5\" xunits=\"fraction\" yunits=\"fraction\"/>").append(RC); 
                res.append("                        <size y=\"0\" x=\"0\" xunits=\"fraction\" yunits=\"fraction\"/>").append(RC); 
                res.append("                        <visibility>0</visibility>").append(RC); 
                res.append("                   </ScreenOverlay>").append(RC); 
            }
            res.append("            </Folder>").append(RC); 
   
            resGeneration = true;
            kmlString = res.toString();                  
        } catch (Exception e) {
            resGeneration = false;
        }   

        return resGeneration;
    }
    
}
