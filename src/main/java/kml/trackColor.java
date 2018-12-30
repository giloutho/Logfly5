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
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Locale;
import org.xnap.commons.i18n.I18n;
import org.xnap.commons.i18n.I18nFactory;
import settings.configProg;
import trackgps.traceGPS;

/**
 *
 * @author Gil with great help of Emmanuel Chabani aka Man's 
 * Translated in xLogfly from Man's php script 
 * Manage generation of track color
 * 
 */
public class trackColor {
    private boolean colorOK;
    private String kmlString;
    private static final String RC = "\n";
    private int localReduc;
    int totalPoints;
    private traceGPS KmlTrace;
    private makingKml currMakingKml;
    DecimalFormat fmtsigne = new DecimalFormat("+#0.00;-#");
    
    // Localization
    private I18n i18n; 
    
    public String getKmlString() {
        return kmlString;
    } 
    
    public trackColor(traceGPS pTrace, makingKml pMakingKml, Locale currLocale) {    
        colorOK = false;
        KmlTrace = pTrace;
        currMakingKml = pMakingKml;
        i18n = I18nFactory.getI18n("","lang/Messages",trackColor.class.getClass().getClassLoader(),currLocale,0);
        if (currMakingKml.isUseReduc())
            localReduc = currMakingKml.getKmlReduc();
        else
            localReduc = 1;
        totalPoints = KmlTrace.Tb_Good_Points.size()-1;
    }

    /**
     * in xLogfly -> kml_D_Folder_Rad_Deb method
     * Begin of folder code 
     */
    public String genDebFolder() {
        StringBuilder res = new StringBuilder();
        
        res.append("     <Folder>").append(RC); 
        res.append("          <styleUrl>#Liste_Radio</styleUrl>").append(RC); 
        res.append("          <open>1</open>").append(RC); 
        res.append("          <name>").append(i18n.tr("Colored tracks")).append("</name>").append(RC); 
        
        return res.toString();
    }
    
    /**
     * in xLogfly -> kml_H_Folder_Rad_Fin method
     * End of code folder
     * @return 
     */
    public String genFinFolder() {
        StringBuilder res = new StringBuilder();
        
        res.append("     </Folder>").append(RC);         
        return res.toString();
    }
    
    /**
     * Changes colour under vario values
     * @return 
     */
    public boolean genColorVario() {
        StringBuilder res = new StringBuilder();
        int aMini;
        int aMaxi;
        boolean resGeneration = false;
        
        DecimalFormatSymbols decimalFormatSymbols = new DecimalFormatSymbols();
        // Mandatory : separator must be a point
        decimalFormatSymbols.setDecimalSeparator('.');        
        DecimalFormat decimalFormat = new DecimalFormat("###.00000", decimalFormatSymbols);
        
        try {
            pointIGC ptVarioMini = KmlTrace.getVario_Mini();
            pointIGC ptVarioMax = KmlTrace.getVario_Max();  
            if (currMakingKml.isGraphAltiBaro()) {            
                aMini = ptVarioMini.AltiBaro;
                aMaxi = ptVarioMax.AltiBaro;            
            } else {
                aMini = ptVarioMini.AltiGPS;
                aMaxi = ptVarioMax.AltiGPS;    
            }

            res.append("          <Folder>").append(RC);  
            res.append("               <styleUrl>#Liste_Coche</styleUrl>").append(RC);  
            res.append("               <name>").append(i18n.tr("By climb")).append("</name>").append(RC);  
            res.append("               <visibility>0</visibility>").append(RC);  
            res.append("               <description><![CDATA[<b><span style=\" color:#0000FF\">").append(fmtsigne.format(ptVarioMax.Vario));
            res.append("m/s   </span><span style=\"color:#FF0000\">").append(fmtsigne.format(ptVarioMini.Vario)).append("m/s</span></b>]]></description>").append(RC);  
            res.append(genTraceDegradee(2));

            // Mini vario value
            res.append("                         <Placemark>").append(RC);  
            res.append("                              <Point>").append(RC);  
            res.append("                                   <altitudeMode>absolute</altitudeMode>").append(RC);  
            res.append("                                   <coordinates>").append(decimalFormat.format(ptVarioMini.Longitude)).append(",").append(decimalFormat.format(ptVarioMini.Latitude)).append(",").append(String.valueOf(aMini)).append("</coordinates>").append(RC);  
            res.append("                              </Point>").append(RC);  
            res.append("                              <Snippet/>").append(RC);  
            res.append("                              <styleUrl>#Point_Bleu</styleUrl>").append(RC);  
            res.append("                              <name>Min :").append(fmtsigne.format(ptVarioMini.Vario)).append("m/s</name>").append(RC);  
            res.append("                         </Placemark>").append(RC);  
            // Max vario value
            res.append("                         <Placemark>").append(RC);  
            res.append("                              <Point>").append(RC);  
            res.append("                                   <altitudeMode>absolute</altitudeMode>").append(RC);  
            res.append("                                   <coordinates>").append(decimalFormat.format(ptVarioMax.Longitude)).append(",").append(decimalFormat.format(ptVarioMax.Latitude)).append(",").append(String.valueOf(aMaxi)).append("</coordinates>").append(RC);          
            res.append("                              </Point>").append(RC);  
            res.append("                              <Snippet/>").append(RC);  
            res.append("                              <styleUrl>#Point_Rouge</styleUrl>").append(RC);  
            res.append("                              <name>Max : ").append(fmtsigne.format(ptVarioMax.Vario)).append("m/s</name>").append(RC);  
            res.append("                         </Placemark>").append(RC);  

            res.append("               <ScreenOverlay>").append(RC);  
            res.append("                        <Icon>").append(RC);  
            res.append("                                <href><![CDATA[").append(genLegendeDouble(ptVarioMini.Vario, ptVarioMax.Vario)).append("]]></href>").append(RC);  
            res.append("                        </Icon>").append(RC);  
            res.append("                        <overlayXY y=\"1\" x=\"0\" xunits=\"fraction\" yunits=\"fraction\"/>").append(RC);  
            res.append("                        <screenXY y=\"1\" x=\"0\" xunits=\"fraction\" yunits=\"fraction\"/>").append(RC);  
            res.append("                        <size y=\"0\" x=\"0\" xunits=\"fraction\" yunits=\"fraction\"/>").append(RC);  
            res.append("               </ScreenOverlay>").append(RC);  
            res.append("          </Folder>").append(RC);
            resGeneration = true;
            kmlString = res.toString();            
        } catch (Exception e) {
            resGeneration = false;
        }   

        return resGeneration;
    }
    
    /**
     * in xLogfly -> kml_F_Track_Alti method
     * Changes colour under altitude values
     * @return 
     */
    public boolean genColorAlti() {
        StringBuilder res = new StringBuilder();
        int aMini;
        int aMaxi;
        pointIGC pMini;
        pointIGC pMaxi;
        boolean resGeneration = false;
        
        DecimalFormatSymbols decimalFormatSymbols = new DecimalFormatSymbols();
        // Mandatory : separator must be a point
        decimalFormatSymbols.setDecimalSeparator('.');        
        DecimalFormat decimalFormat = new DecimalFormat("###.00000", decimalFormatSymbols);
        
        try {            
            if (currMakingKml.isGraphAltiBaro()) {            
                pMini = KmlTrace.getAlt_Mini_Baro();
                pMaxi = KmlTrace.getAlt_Maxi_Baro();
                aMini = pMini.AltiBaro;
                aMaxi = pMaxi.AltiBaro;                
            } else {
                 pMini = KmlTrace.getAlt_Mini_GPS();
                pMaxi = KmlTrace.getAlt_Maxi_GPS();
                aMini = pMini.AltiGPS;
                aMaxi = pMaxi.AltiGPS;      
            }
            
            res.append("          <Folder>").append(RC); 
            res.append("               <styleUrl>#Liste_Coche</styleUrl>").append(RC); 
            res.append("               <name>").append(i18n.tr("By altitude")).append("</name>").append(RC); 
            res.append("               <visibility>0</visibility>").append(RC); 
            res.append("               <description><![CDATA[<b><span style=\" color:#0000FF\">").append(String.valueOf(aMaxi));
            res.append("m    </span><span style=\"color:#FF0000\">").append(String.valueOf(aMini)).append("m</span></b>]]></description>").append(RC);             
            res.append(genTraceDegradee(1));
            // Mini altitude value
            res.append("                         <Placemark>").append(RC); 
            res.append("                              <Point>").append(RC); 
            res.append("                                   <altitudeMode>absolute</altitudeMode>").append(RC); 
            res.append("                                   <coordinates>").append(decimalFormat.format(pMini.Longitude)).append(",").append(decimalFormat.format(pMini.Latitude)).append(",").append(String.valueOf(aMini)).append("</coordinates>").append(RC);              
            res.append("                              </Point>").append(RC); 
            res.append("                              <Snippet/>").append(RC); 
            res.append("                              <styleUrl>#Point_Bleu</styleUrl>").append(RC); 
            res.append("                              <name>Min : ").append(String.valueOf(aMini)).append("m</name>").append(RC); 
            res.append("                         </Placemark>").append(RC); 
            // Max altitude value
            res.append("                         <Placemark>").append(RC); 
            res.append("                              <Point>").append(RC); 
            res.append("                                   <altitudeMode>absolute</altitudeMode>").append(RC); 
            res.append("                                   <coordinates>").append(decimalFormat.format(pMaxi.Longitude)).append(",").append(decimalFormat.format(pMaxi.Latitude)).append(",").append(String.valueOf(aMaxi)).append("</coordinates>").append(RC);                      
            res.append("                              </Point>").append(RC); 
            res.append("                              <Snippet/>").append(RC); 
            res.append("                              <styleUrl>#Point_Rouge</styleUrl>").append(RC); 
            res.append("                              <name>Max : ").append(String.valueOf(aMaxi)).append("m</name>").append(RC); 
            res.append("                         </Placemark>").append(RC); 
  
            res.append("               <ScreenOverlay>").append(RC); 
            res.append("                        <Icon>").append(RC); 
            res.append("                                <href><![CDATA[").append(genLegendeInt(aMini,aMaxi,100)).append("]]></href>").append(RC); 
            res.append("                        </Icon>").append(RC); 
            res.append("                        <overlayXY y=\"1\" x=\"0\" xunits=\"fraction\" yunits=\"fraction\"/>").append(RC); 
            res.append("                        <screenXY y=\"1\" x=\"0\" xunits=\"fraction\" yunits=\"fraction\"/>").append(RC); 
            res.append("                        <size y=\"0\" x=\"0\" xunits=\"fraction\" yunits=\"fraction\"/>").append(RC); 
            res.append("               </ScreenOverlay>").append(RC); 
            res.append("          </Folder>").append(RC); 
            
            resGeneration = true;
            kmlString = res.toString();            
        } catch (Exception e) {
            resGeneration = false;
        }   

        return resGeneration;
    }
          
    /**
     * in xLogfly -> kml_G_Track_Speed method
     * Changes colour under speed values
     * @return 
     */
    public boolean genColorSpeed() {
        StringBuilder res = new StringBuilder();
        int aMini;
        int aMaxi;
        pointIGC pMini;
        pointIGC pMaxi;
        boolean resGeneration = false;
        
        DecimalFormatSymbols decimalFormatSymbols = new DecimalFormatSymbols();
        // Mandatory : separator must be a point
        decimalFormatSymbols.setDecimalSeparator('.');        
        DecimalFormat decimalFormat = new DecimalFormat("###.00000", decimalFormatSymbols);
        
        try {  
            pMini = KmlTrace.getVit_Mini();
            pMaxi = KmlTrace.getVit_Max();
            if (currMakingKml.isGraphAltiBaro()) {                           
                aMini = pMini.AltiBaro;
                aMaxi = pMaxi.AltiBaro;                
            } else {                 
                aMini = pMini.AltiGPS;
                aMaxi = pMaxi.AltiGPS;      
            }
                         
            res.append("          <Folder>").append(RC); 
            res.append("               <styleUrl>#Liste_Coche</styleUrl>").append(RC); 
            res.append("               <name>").append(i18n.tr("By speed")).append("</name>").append(RC); 
            res.append("               <visibility>0</visibility>").append(RC); 
            res.append("               <description><![CDATA[<b><span style=\" color:#0000FF\">").append(String.format("%2.2f",pMaxi.Vitesse));
            res.append("km/h    </span><span style=\"color:#FF0000\">").append(String.format("%2.2f",pMini.Vitesse)).append("km/h</span></b>]]></description>").append(RC);             
            res.append(genTraceDegradee(3));
            // Mini speed value
            res.append("                         <Placemark>").append(RC); 
            res.append("                              <Point>").append(RC); 
            res.append("                                   <altitudeMode>absolute</altitudeMode>").append(RC); 
            res.append("                                   <coordinates>").append(decimalFormat.format(pMini.Longitude)).append(",").append(decimalFormat.format(pMini.Latitude)).append(",").append(String.valueOf(aMini)).append("</coordinates>").append(RC);              
            res.append("                              </Point>").append(RC); 
            res.append("                              <Snippet/>").append(RC); 
            res.append("                              <styleUrl>#Point_Bleu</styleUrl>").append(RC); 
            res.append("                              <name>Min : ").append(String.format("%2.2f",pMini.Vitesse)).append("Km/h</name>").append(RC); 
            res.append("                         </Placemark>").append(RC); 
            // Max speed value
            res.append("                         <Placemark>").append(RC); 
            res.append("                              <Point>").append(RC); 
            res.append("                                   <altitudeMode>absolute</altitudeMode>").append(RC); 
            res.append("                                   <coordinates>").append(decimalFormat.format(pMaxi.Longitude)).append(",").append(decimalFormat.format(pMaxi.Latitude)).append(",").append(String.valueOf(aMaxi)).append("</coordinates>").append(RC);                      
            res.append("                              </Point>").append(RC); 
            res.append("                              <Snippet/>").append(RC); 
            res.append("                              <styleUrl>#Point_Rouge</styleUrl>").append(RC); 
            res.append("                              <name>Max : ").append(String.format("%2.2f",pMaxi.Vitesse)).append("Km/h</name>").append(RC); 
            res.append("                         </Placemark>").append(RC); 
  
            res.append("               <ScreenOverlay>").append(RC); 
            res.append("                        <Icon>").append(RC); 
            res.append("                                <href><![CDATA[").append(genLegendeInt((int)pMini.Vitesse,(int)pMaxi.Vitesse,10)).append("]]></href>").append(RC); 
            res.append("                        </Icon>").append(RC); 
            res.append("                        <overlayXY y=\"1\" x=\"0\" xunits=\"fraction\" yunits=\"fraction\"/>").append(RC); 
            res.append("                        <screenXY y=\"1\" x=\"0\" xunits=\"fraction\" yunits=\"fraction\"/>").append(RC); 
            res.append("                        <size y=\"0\" x=\"0\" xunits=\"fraction\" yunits=\"fraction\"/>").append(RC); 
            res.append("               </ScreenOverlay>").append(RC); 
            res.append("          </Folder>").append(RC); 
            
            resGeneration = true;
            kmlString = res.toString();            
        } catch (Exception e) {
            resGeneration = false;
        }   

        return resGeneration;
    }
    
  
    /**
     *  in xLogfly -> mGen_Trace_Degradee(2,0) method
     * Colour gradient for the track
     * second parameter : altitude type was unused. It was deleted. We used only "absolute"     
     * @param typeTrace
     * @return 
     */
    public String genTraceDegradee(int typeTrace) {
        StringBuilder res = new StringBuilder();
        String sRefVal = null;
        String sAlti = null;
        String sCurrVal = null;
        
        DateTimeFormatter dtfHHmm = DateTimeFormatter.ofPattern("HH:mm");
        DecimalFormatSymbols decimalFormatSymbols = new DecimalFormatSymbols();
        // Mandatory : separator must be a point
        decimalFormatSymbols.setDecimalSeparator('.');        
        DecimalFormat decimalFormat = new DecimalFormat("###.00000", decimalFormatSymbols);
        
        pointIGC firstPoint = KmlTrace.Tb_Good_Points.get(0);
        switch (typeTrace) {
            case 1:
                if (currMakingKml.isGraphAltiBaro()) 
                    sRefVal = String.valueOf(firstPoint.AltiBaro);
                else
                    sRefVal = String.valueOf(firstPoint.AltiGPS);                
                break;
            case 2:
                sRefVal = String.format("%2.2f",firstPoint.Vario);
                break;
            case 3:
                sRefVal = String.format("%2.2f",firstPoint.Vitesse);   
                break;
        }
        if (currMakingKml.isGraphAltiBaro()) 
            sAlti = String.valueOf(firstPoint.AltiBaro);
        else
            sAlti = String.valueOf(firstPoint.AltiGPS);
        
        // kml initialization
        res.append("                         <Placemark>").append(RC);  
        res.append("                              <LineString>").append(RC);  
        switch (typeTrace) {
            case 1:
                res.append("                              <name>").append(firstPoint.dHeure.format(dtfHHmm)).append(" ").append(sAlti).append(" m</name>").append(RC);  
                break;
            case 2:              
                res.append("                              <name>").append(firstPoint.dHeure.format(dtfHHmm)).append(" ").append(String.format("%2.2f",firstPoint.Vario)).append(" m/s</name>").append(RC); 
                break;
            case 3:
                res.append("                              <name>").append(firstPoint.dHeure.format(dtfHHmm)).append(" ").append(String.format("%3.2f",firstPoint.Vitesse)).append(" km/h</name>").append(RC);
                break;
        }
        res.append("                                   <altitudeMode>absolute</altitudeMode>").append(RC);  
        res.append("                                   <coordinates>").append(decimalFormat.format(firstPoint.Longitude)).append(",");
        res.append(decimalFormat.format(firstPoint.Latitude)).append(",").append(sAlti);
        
        pointIGC currPoint;        
        for (int i = 1; i < totalPoints; i += localReduc) { 
           currPoint = KmlTrace.Tb_Good_Points.get(i);
            switch (typeTrace) {
                case 1:
                    if (currMakingKml.isGraphAltiBaro()) 
                        sCurrVal = String.valueOf(currPoint.AltiBaro);
                    else
                        sCurrVal = String.valueOf(currPoint.AltiGPS); 
                     break;
                case 2:
                    sCurrVal = String.format("%2.2f",currPoint.Vario);
                    break;
               case 3:
                    sCurrVal = String.format("%3.2f",currPoint.Vitesse);
                    break;
            }
            if (!sCurrVal.equals(sRefVal)) {                 
                sRefVal = sCurrVal;                
                // Value change : last value with same RefVal
                res.append(" ").append(decimalFormat.format(currPoint.Longitude)).append(",").append(decimalFormat.format(currPoint.Latitude)).append(",").append(sAlti).append("</coordinates>").append(RC);
                res.append("                              </LineString>").append(RC);  
                res.append("                              <Style>").append(RC);  
                res.append("                                   <LineStyle>").append(RC);  
                switch (typeTrace) {
                    case 1:
                        if (currMakingKml.isGraphAltiBaro()) 
                            res.append("                                        <color>FF").append(couleurPoint(currPoint.AltiBaro,KmlTrace.getAlt_Mini_Baro().AltiBaro,KmlTrace.getAlt_Maxi_Baro().AltiBaro)).append("</color>").append(RC);  
                        else
                            res.append("                                        <color>FF").append(couleurPoint(currPoint.AltiGPS,KmlTrace.getAlt_Mini_GPS().AltiGPS,KmlTrace.getAlt_Maxi_GPS().AltiGPS)).append("</color>").append(RC);  
                        break;
                    case 2:
                        res.append("                                        <color>FF").append(couleurPoint(currPoint.Vario,KmlTrace.getVario_Mini().Vario,KmlTrace.getVario_Max().Vario)).append("</color>").append(RC);  
                        break;
                    case 3:        
                        res.append("                                        <color>FF").append(couleurPoint(Math.floor(currPoint.Vitesse),Math.floor(KmlTrace.getVit_Mini().Vitesse),Math.floor(KmlTrace.getVit_Max().Vitesse))).append("</color>").append(RC);  
                        break;
                }
                res.append("                                        <width>3</width>").append(RC);  
                res.append("                                   </LineStyle>").append(RC);  
                res.append("                              </Style>").append(RC);  
                res.append("                         </Placemark>").append(RC);  
                // New set with first value with same RefVal
                if (currMakingKml.isGraphAltiBaro()) 
                    sAlti = String.valueOf(currPoint.AltiBaro);
                else
                    sAlti = String.valueOf(currPoint.AltiGPS);      
                res.append("                         <Placemark>").append(RC);  
                switch (typeTrace) {
                    case 1:
                        res.append("                              <name>").append(currPoint.dHeure.format(dtfHHmm)).append(" ").append(sAlti).append(" m</name>").append(RC);  
                        break;
                    case 2:                  
                        res.append("                              <name>").append(currPoint.dHeure.format(dtfHHmm)).append(" ").append(fmtsigne.format(currPoint.Vario)).append(" m/s</name>").append(RC);  
                        break;
                    case 3:
                        res.append("                              <name>").append(currPoint.dHeure.format(dtfHHmm)).append(" ").append(String.format("%3.2f",currPoint.Vitesse)).append(" km/h</name>").append(RC);  
                        break;
                }
                res.append("                              <LineString>").append(RC);  
                res.append("                                   <altitudeMode>absolute</altitudeMode>").append(RC);  
                res.append("                                   <coordinates>").append(decimalFormat.format(currPoint.Longitude)).append(",").append(decimalFormat.format(currPoint.Latitude)).append(",").append(sAlti);
            }  else {
                // same values
                if (currMakingKml.isGraphAltiBaro()) 
                    res.append(" ").append(decimalFormat.format(currPoint.Longitude)).append(",").append(decimalFormat.format(currPoint.Latitude)).append(",").append(String.valueOf(currPoint.AltiBaro));
                else
                    res.append(" ").append(decimalFormat.format(currPoint.Longitude)).append(",").append(decimalFormat.format(currPoint.Latitude)).append(",").append(String.valueOf(currPoint.AltiGPS));
            }
        }

        // It is necessary to complete the last set of values
        // Value change : last value with same RefVal
        currPoint = KmlTrace.Tb_Good_Points.get(totalPoints);
        if (currMakingKml.isGraphAltiBaro()) 
            res.append(" ").append(decimalFormat.format(currPoint.Longitude)).append(",").append(decimalFormat.format(currPoint.Latitude)).append(",").append(String.valueOf(currPoint.AltiBaro)).append("</coordinates>").append(RC); 
        else
            res.append(" ").append(decimalFormat.format(currPoint.Longitude)).append(",").append(decimalFormat.format(currPoint.Latitude)).append(",").append(String.valueOf(currPoint.AltiGPS)).append("</coordinates>").append(RC);             
        res.append("                              </LineString>").append(RC); 
        res.append("                              <Style>").append(RC); 
        res.append("                                   <LineStyle>").append(RC); 
        switch (typeTrace) {
            case 1:
                if (currMakingKml.isGraphAltiBaro()) 
                    res.append("                                        <color>FF").append(couleurPoint(currPoint.AltiBaro,KmlTrace.getAlt_Mini_Baro().AltiBaro,KmlTrace.getAlt_Maxi_Baro().AltiBaro)).append("</color>").append(RC);                     
                else
                    res.append("                                        <color>FF").append(couleurPoint(currPoint.AltiGPS,KmlTrace.getAlt_Mini_GPS().AltiGPS,KmlTrace.getAlt_Maxi_GPS().AltiGPS)).append("</color>").append(RC);                                            
                break;
            case 2:
                res.append("                                        <color>FF").append(couleurPoint(currPoint.Vario,KmlTrace.getVario_Mini().Vario,KmlTrace.getVario_Max().Vario)).append("</color>").append(RC);  
                break;
            case 3:
                res.append("                                        <color>FF").append(couleurPoint(Math.floor(currPoint.Vitesse),Math.floor(KmlTrace.getVit_Mini().Vitesse),Math.floor(KmlTrace.getVit_Max().Vitesse))).append("</color>").append(RC);  
                break;
        }
        res.append("                                        <width>3</width>").append(RC); 
        res.append("                                   </LineStyle>").append(RC); 
        res.append("                              </Style>").append(RC); 
        res.append("                         </Placemark>").append(RC);         
        
        return res.toString();        
    }
    
    /**
     * This strange name come from PHP script and was kept in xLogfly
     * We kept variable names
     * Compute point color
     * @param x
     * @param Mini
     * @param maxi
     * @return 
     */
    private String couleurPoint(double x, double Mini, double Maxi)  {
        String res;
        
        double Mm,xm, Multi;  
  
        Mm = Maxi - Mini;
        xm = x - Mini;
        
        if (Mini * Maxi < 0) {
            if (Mini == 0) Mini = 1;
            if (Maxi == 0) Maxi = 1;
            x = x * 1.25;
            if (x > Maxi) x=Maxi;
            if (x < Mini) x = Mini;
            if (x >= 0) {
                Multi = 255 * (1 - x / Maxi);
                res = String.format("%02X", (0xFF & (int)Multi))+"00FF";               
            } else {
                Multi = 255 * (1 - x / Mini);      
                res = "FF00" + String.format("%02X", (0xFF & (int)Multi));
            }
        } else {
            if (Mm==0) Mm=1;
            if (xm >= 2*Mm/3) {
                Multi = 255 * 3 * (1 - xm / Mm);               
                res = "00" + String.format("%02X", (0xFF & (int)Multi)) + "FF";
            } else if (xm >= Mm/2) {
                Multi = 255 * (6 * xm / Mm - 3);      
                res = "00FF" + String.format("%02X", (0xFF & (int)Multi));
            } else if (xm >= Mm/3) {
                Multi = 255 * (3 - 6 * xm / Mm);      
                res = String.format("%02X", (0xFF & (int)Multi)) + "FF00";
            } else {
                Multi = 255 * 3*xm/Mm;      
                res = "FF"+String.format("%02X", (0xFF & (int)Multi))+"00";
            }
        }
        
        return res;
    }
    
    /**
     * in xLogfly -> mGen_Legende_Double method
     * Chart legend generation for overlay dosplay in Google Earth
     * use double values
     * @param Mini
     * @param Maxi
     * @return 
     */
    private String genLegendeDouble(double Mini, double Maxi)  {
        StringBuilder gcURL = new StringBuilder();
        
        gcURL.append("http://chart.googleapis.com/chart?chf=");
  
        String gcFontSize = "9";
        String gcFontColor = "FFFFFF";  // White
        // Dim gcFontColor As String = "000000"  ' Black
        String gcAlignment = "0";
        String gcTicksMarkColor = "000000";
        String gcWidth, gcHeight, gcTitre;
        ArrayList<String> gcColor = new ArrayList<>();
        ArrayList<Double> gcValue = new ArrayList<>();              
        // arbitrarily fixed to 6 colors in chart legend
  
        // Direct values for maxi and mini.  Compute 4 intermediate values
        double Tranche,Range;
  
        // No title
        gcTitre = null;
        
        gcValue.add(Mini);   // range 0
        Range = Maxi - Mini;
        Tranche = Range / 5;
        for (int i = 1; i <= 4; i++) {
            gcValue.add(gcValue.get(0)+(Tranche*i));
        }
        gcValue.add(Maxi);     // range 5
        for (int i = 0; i <= 5; i++) {
            gcColor.add(couleurPoint(gcValue.get(i), gcValue.get(0), gcValue.get(4)));
        }
        
        // background is transparent
        gcURL.append("bg,s,00000000|");
        // Fills in Image Chart Editor
        gcURL.append("c,lg,90,");    // c -> chart lg -> linear gradient   Le dernier chiffre représente l'angle 0 horizontal, 90 vertical
        // For instance offsets are skipped. They are hard coded
        // No explanations, I inversed code and it's running  -:)
        // gcURL = gcURL + gcColor(0)+",0,"+ gcColor(1)+",0.1,"+ gcColor(2)+",0.2,"+ gcColor(3)+",0.567,"+ gcColor(4)+",0.75,"+ gcColor(5)+",1"
        gcURL.append(gcColor.get(5)).append(",0,").append(gcColor.get(4)).append(",0.1,").append(gcColor.get(3)).append(",0.2,").append(gcColor.get(2)).append(",0.567,").append(gcColor.get(1)).append(",0.75,").append(gcColor.get(0)).append(",1");
  
        // Axes in Image Chart Editor
        // In &chxl=0 figure 0 means a first set but but several are possible Cf Average electrical example
        // reversal problem mentioned earlier
        gcURL.append("&chxl=0:|").append(fmtsigne.format(gcValue.get(0))).append("|").append(fmtsigne.format(gcValue.get(1))).append("|").append(fmtsigne.format(gcValue.get(2)));
        gcURL.append("|").append(fmtsigne.format(gcValue.get(3))).append("|").append(fmtsigne.format(gcValue.get(4))).append("|").append(fmtsigne.format(gcValue.get(5)));
        // Position in pixel
        gcURL.append("&chxp=20,80,140,200,260,320");
        //gcURL = gcURL + "&chxp=0,8,30,50,70,90"
        // Label writing start, default settings are OK
        gcURL.append("&chxs=0,").append(gcFontColor).append(",").append(gcFontSize).append(",").append(gcAlignment).append(",t,").append(gcTicksMarkColor);
        // Chart legend placed at the bottom : bottom -> x  Left -> y   Right -> r  top -> t
        gcURL.append("&chxt=r");
  
        // Size in Image Chart Editor
        gcWidth = "40";
        gcHeight = "200";
        gcURL.append("&chs=").append(gcWidth).append("x").append(gcHeight);
  
        // Pattern type
        gcURL.append("&cht=lc");   // lc comme linechart  [ je suppose ! ]
  
        // For this, I don't know [ Une expression 0000000 par série de labels, ds l'ex d'origine, il y en a 2 ]
        gcURL.append("&chco=00000000");
  
        // Data in Image Chart Editor
        // space 100%         t like Encoding = Text
        gcURL.append("&chd=t:100");
  
        // DataStyle in Image Chart Editor
        // 1 for width = 1
        gcURL.append("&chls=1");
  
        // Margin in Image Chart Editor
        // In Average electrical example, there is margins, not usable for us
  
        // Title in Image Chart Editor
        // For us it seems too big
        // We don't put it but if we want...
        if (gcTitre != null)
            gcURL.append("&chtt=").append(gcTitre);
        
        return gcURL.toString();
    }
 
    /**
     * In xLogfly -> mGen_Legende_Int method
     * Chart legend generation for overlay dosplay in Google Earth
     * use integer values     
     * For explanations see comments in genLegendeDouble just below
     * @param Mini
     * @param Maxi
     * @param NbMod
     * @return 
     */
    private String genLegendeInt(int Mini, int Maxi, int NbMod )  {
        StringBuilder gcURL = new StringBuilder();
        
        gcURL.append("http://chart.googleapis.com/chart?chf=");
  
        String gcFontSize = "9";
        String gcFontColor = "FFFFFF";  // White
        // Dim gcFontColor As String = "000000"  ' Black
        String gcAlignment = "0";
        String gcTicksMarkColor = "000000";
        String gcWidth, gcHeight, gcTitre;
        ArrayList<String> gcColor = new ArrayList<>();
        ArrayList<Integer> gcValue = new ArrayList<>();              
        // arbitrarily fixed to 6 colors in chart legend
  
        // Direct values for maxi and mini.  Compute 4 intermediate values
        int Tranche,Range;
  
        // Pas de titre pour l'instant
        gcTitre = null;
        // On arrondit Mini et Maxi à NbMod près (100m pr l'alti, 10kmh pr la vitesse)
        gcValue.add(specArrondi(Mini, 0, NbMod));   // rang 0
        Range = Maxi - Mini;                
        Tranche = Range / 5;
        for (int i = 1; i <= 4; i++) {
            gcValue.add(specArrondi(gcValue.get(0)+(Tranche*i),2,NbMod));            
        }
        gcValue.add(specArrondi(Maxi,1,NbMod));     // Rang 5
        for (int i = 0; i <= 5; i++) {
            gcColor.add(couleurPoint(gcValue.get(i), gcValue.get(0), gcValue.get(4)));
        }
        
        // On précise que le background doit être transparent, voir le configurateur en ligne pour les différentes combinaisons
        gcURL.append("bg,s,00000000|");
        // Fills dans Image Chart Editor
        gcURL.append("c,lg,90,");    // c -> chart lg -> linear gradient   Le dernier chiffre représente l'angle 0 horizontal, 90 vertical
        // Je fais l'impasse pr l'instant sur les offsets, ils sont mis en dur
        // PLUS GRAVE... Avec le code d'origine ci dessous j'obtenais un graphe complètement inversé, la couleur du max attribuée au mini et vv
        // Cherché mais pas trouvé d'explis, j'ai tout simplement inversé mais sans explication rationnelle
        // gcURL = gcURL + gcColor(0)+",0,"+ gcColor(1)+",0.1,"+ gcColor(2)+",0.2,"+ gcColor(3)+",0.567,"+ gcColor(4)+",0.75,"+ gcColor(5)+",1"
        gcURL.append(gcColor.get(5)).append(",0,").append(gcColor.get(4)).append(",0.1,").append(gcColor.get(3)).append(",0.2,").append(gcColor.get(2)).append(",0.567,").append(gcColor.get(1)).append(",0.75,").append(gcColor.get(0)).append(",1");
  
        // Axes dans Image Chart Editor
        // Dans &chxl=0 le chiffre 0 indique une première série mais il pt y en avoir plusiuers cf exemple Average electrical
        // Problème de l'inversion
        gcURL.append("&chxl=0:|").append(String.valueOf(gcValue.get(0))).append("|").append(String.valueOf(gcValue.get(1))).append("|").append(String.valueOf(gcValue.get(2)));
        gcURL.append("|").append(String.valueOf(gcValue.get(3))).append("|").append(String.valueOf(gcValue.get(4))).append("|").append(String.valueOf(gcValue.get(5)));
        // On indique mtnt leur position en pixel
        gcURL.append("&chxp=20,80,140,200,260,320");
        //gcURL = gcURL + "&chxp=0,8,30,50,70,90"
        // Concerne le démarrage de l'écriture des labels, si l'axe est visble les couleurs, etc... Réglages par défaut me convenaient !
        gcURL.append("&chxs=0,").append(gcFontColor).append(",").append(gcFontSize).append(",").append(gcAlignment).append(",t,").append(gcTicksMarkColor);
        // La légende est placée en bas : bottom -> x  Left -> y   Right -> r  top -> t
        gcURL.append("&chxt=r");
  
        // Size dans Image Chart Editor
        gcWidth = "40";
        gcHeight = "200";
        gcURL.append("&chs=").append(gcWidth).append("x").append(gcHeight);
  
        // Type de schéma
        gcURL.append("&cht=lc");   // lc comme linechart  [ je suppose ! ]
  
        // Pour cette mention je ne sais pas  [ Une expression 0000000 par série de labels, ds l'ex d'origine, il y en a 2 ]
        gcURL.append("&chco=00000000");
  
        // Data dans Image Chart Editor
        // Occupe 100% de l'espace         t comme Encoding = Text
        gcURL.append("&chd=t:100");
  
        // DataStyle dans Image Chart Editor
        // 1 pour width = 1
        gcURL.append("&chls=1");
  
        // Margin dans Image Chart Editor
        // Dans l'exemple Average electrical il y des marges, je ne les ai pas trouvé utiles dans notre cas
  
        // Titre dans Image Chart Editor
        // dans notre cas le titre me parait surcharger un peu
        // Donc je ne le met pas mais on pourrait...
        if (gcTitre != null)
            gcURL.append("&chtt=").append(gcTitre);
        
        return gcURL.toString();
    }
    /**
     * Manage round number
     * @param Nb
     * @param Sens
     * @param NbMod
     * @return 
     */
    private int specArrondi(int Nb, int Sens,int NbMod)  {
        // NbMod rounded to a whole number
        // Sens 0 -> rounded lower value
        // Sens 1 -> rounded upper value
        // Sens 3 -> rounded to the nearest whole value
        int Reste;
        int Res = 0;
  
        Reste = Nb % NbMod;  // like mod function
        switch (Sens) {
            case 0:
                Res = Nb - Reste;
                break;
            case 1:
                Res = Nb + (NbMod - Reste);
                break;
            case 2:
                if (Reste < 50) 
                    Res = Nb - Reste;
                else
                    Res = Nb + (NbMod - Reste);
                break;                
        }
    
        return Res;
    }
}
