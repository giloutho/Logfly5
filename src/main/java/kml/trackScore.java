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
import java.util.Locale;
import org.xnap.commons.i18n.I18n;
import org.xnap.commons.i18n.I18nFactory;
import settings.configProg;
import trackgps.traceGPS;

/**
 *
 * @author gil
 * Manage score display
 */
public class trackScore {
    private boolean scoreOK;
    private String kmlString;
    private static final String RC = "\n";   
    int totalPoints;
    private traceGPS KmlTrace;
    private makingKml currMakingKml;
    DecimalFormat fmtsigne = new DecimalFormat("+#0.00;-#");
    DateTimeFormatter dtfHHmm = DateTimeFormatter.ofPattern("HH:mm");
    DecimalFormat decimalFormat;
       
    private static StringBuilder kmlScoreDetails;
    private static StringBuilder kmlScoreTrace;
    private static StringBuilder sCoordTrace;
    
    // Localization
    private I18n i18n; 
    
    public trackScore(traceGPS pTrace, makingKml pMakingKml, Locale currLocale) {    
        scoreOK = false;
        KmlTrace = pTrace;
        currMakingKml = pMakingKml;
        i18n = I18nFactory.getI18n("","lang/Messages",trackScore.class.getClass().getClassLoader(),currLocale,0);
        totalPoints = KmlTrace.Tb_Good_Points.size()-1;
        DecimalFormatSymbols decimalFormatSymbols = new DecimalFormatSymbols();
        decimalFormatSymbols.setDecimalSeparator('.');        
        decimalFormat = new DecimalFormat("###.00000", decimalFormatSymbols);       
        
        kmlScoreDetails = new StringBuilder();
        kmlScoreTrace = new StringBuilder();
        sCoordTrace = new StringBuilder();  
    }

    public String getKmlString() {
        return kmlString;
    }
    /**
     * Generation of scoring display
     * Track is scored by Scoring class before generation
     * @return 
     */
    public boolean genScore() {
        
        StringBuilder res = new StringBuilder();
        boolean resGeneration = false;
        
        try {
            res.append("      <Folder>").append(RC); 
            res.append("               <name>").append(i18n.tr("Score")).append("</name>").append(RC);      
            res.append( "                         <description>").append(RC); 
            res.append( "                         <![CDATA[").append(RC); 
            res.append( "                                   <TABLE><TR><TD><B><I>").append(tradLeague(KmlTrace.getScore_League()));
            res.append("</I></B></TD></TR>").append(RC);    
            res.append( "                                                  <TR><TD>").append(tradShape(KmlTrace.getScore_Shape())).append("</TD>").append(RC);    
            res.append( "                                                  <TR><TD>");
            res.append(String.format("%4.2f",KmlTrace.getScore_Route_Km())).append(" km</TD>").append(RC);    
            res.append( "                                                  <TR><TD>");
            res.append(String.format("%4.2f",KmlTrace.getScore_Route_Pts())).append(" pts</TD>").append(RC);    
            res.append( "                                                  <TR><TD>").append(i18n.tr("Moyenne")).append(" : ");
            res.append(String.format("%3.2f",KmlTrace.getScore_Moyenne())).append(" km/h</TR></TABLE>").append(RC); 

            if (genScoreDetails()) {
                res.append(kmlScoreDetails);
                res.append( "                         ]]></description>").append(RC); 

                if (genScoreTrace()) {
                    res.append(kmlScoreTrace);
                    res.append("     </Folder>").append(RC); 
                    resGeneration = true;
                    kmlString = res.toString();
                }
            }
        } catch (Exception e) {
            resGeneration = false;
        }   

        return resGeneration;        
    }
    
    /**
     * League -> specific online contest rules
     * @param codeLeague
     * @return 
     */
    private String tradLeague(String codeLeague)  {
        String res;
        switch (codeLeague) {
            case "FR" :
                res = i18n.tr("CFD");
                break;
            case "CH" :
                res = i18n.tr("Challenge suisse");
                break;
            case "XC":
                res = i18n.tr("World XContest");
                break;
            default:
                res = codeLeague;    
        }
        return res;
    }
    
    /**
     * Shape evaluation of a track according selected contest rules
     * @param codeShape
     * @return 
     */
    private String tradShape(String codeShape)  {
        String res;
        switch (codeShape) {
            case "FAI Triangle" :
                res = i18n.tr("Triangle FAI");
                break;
            case "Free flight 2 wpt" :
                res = i18n.tr("Distance 2 points");
                break;
            case "Flat Triangle":
                res = i18n.tr("Triangle plat");
                break;
             case "Free flight 1 wpt" :
                res = i18n.tr("Distance 1 point"); 
                break;
            case "Free flight 3 wpt":
                res = i18n.tr("Distance 2 points");
                break;
            default:
                res = codeShape;
        }
        return res;
    }
    
    /**
     * Details of the track (turnpoints)
     * in xLogfly -> kml_I_CFD_Detail
     * @return 
     */
    private boolean genScoreDetails() {
        boolean res = false;  
        int LgTb = 0;
        double dDist = 0;
        double PartDist = 0;
        String sDist; 
        pointIGC currPoint;
        
        try {            
            LgTb = KmlTrace.Score_Tb_Balises.size()-1;
            kmlScoreDetails.append("                                   <TABLE><TR><TD></TD></TR>").append(RC);   
            // Start point
            int IdxBD = KmlTrace.Score_Tb_Balises.get(0);
            currPoint = KmlTrace.Tb_Calcul.get(IdxBD);
            kmlScoreDetails.append("                                                  <TR><TD nowrap>");
            kmlScoreDetails.append(i18n.tr("BD")).append("</TD>");    // BD ou SP en anglais
            kmlScoreDetails.append("<TD>").append(currPoint.dHeure.format(dtfHHmm)).append("</TD>");
            kmlScoreDetails.append("<TD  align=\"right\">").append(String.format("%03d",IdxBD)).append("</TD>");
            kmlScoreDetails.append("<TD  align=\"right\">").append(decimalFormat.format(currPoint.Latitude)).append("</TD>");
            kmlScoreDetails.append("<TD  align=\"right\">").append(decimalFormat.format(currPoint.Longitude)).append("</TD>");
            if (currMakingKml.isGraphAltiBaro()) {
                kmlScoreDetails.append("<TD>").append(String.valueOf(currPoint.AltiBaro)).append("m</TD></TR>").append(RC); 
                if (!KmlTrace.getScore_Triangle()) {
                    sCoordTrace.append(decimalFormat.format(currPoint.Longitude)).append(",");
                    sCoordTrace.append(decimalFormat.format(currPoint.Latitude)).append(",");
                    sCoordTrace.append(String.valueOf(currPoint.AltiBaro)).append(" ");
                }
            } else {
                kmlScoreDetails.append("<TD>").append(String.valueOf(currPoint.AltiGPS)).append("m</TD></TR>").append(RC);
                if (!KmlTrace.getScore_Triangle()) {
                    sCoordTrace.append(decimalFormat.format(currPoint.Longitude)).append(",");
                    sCoordTrace.append(decimalFormat.format(currPoint.Latitude)).append(",");
                    sCoordTrace.append(String.valueOf(currPoint.AltiGPS)).append(" ");
                }
            }
            // Turnpoints
            for (int i = 1; i < LgTb; i++) {
                int IdxBal = KmlTrace.Score_Tb_Balises.get(i);
                currPoint = KmlTrace.Tb_Calcul.get(IdxBal);
                kmlScoreDetails.append("                                                  <TR><TD>").append(i18n.tr("B")).append(String.valueOf(i)).append("</TD>");
                kmlScoreDetails.append("<TD>").append(currPoint.dHeure.format(dtfHHmm)).append("</TD>");
                kmlScoreDetails.append("<TD  align=\"right\">").append(String.format("%03d",IdxBal)).append("</TD>");
                kmlScoreDetails.append("<TD  align=\"right\">").append(decimalFormat.format(currPoint.Latitude)).append("</TD>");
                kmlScoreDetails.append("<TD  align=\"right\">").append(decimalFormat.format(currPoint.Longitude)).append("</TD>");
                if (currMakingKml.isGraphAltiBaro()) {
                    kmlScoreDetails.append("<TD>").append(String.valueOf(currPoint.AltiBaro)).append("m</TD>");
                    sCoordTrace.append(decimalFormat.format(currPoint.Longitude)).append(",");
                    sCoordTrace.append(decimalFormat.format(currPoint.Latitude)).append(",");
                    sCoordTrace.append(String.valueOf(currPoint.AltiBaro)).append(" ");
                } else {
                    kmlScoreDetails.append("<TD>").append(String.valueOf(currPoint.AltiGPS)).append("m</TD>");
                    sCoordTrace.append(decimalFormat.format(currPoint.Longitude)).append(",");
                    sCoordTrace.append(decimalFormat.format(currPoint.Latitude)).append(",");
                    sCoordTrace.append(String.valueOf(currPoint.AltiGPS)).append(" ");
                }                
                pointIGC pointB = KmlTrace.Tb_Calcul.get(KmlTrace.Score_Tb_Balises.get(i-1));
                if (i == 1) {                    
                    if ( !KmlTrace.getScore_Triangle()) {                        
                        dDist = (geoutils.trigo.CoordDistance(currPoint.Latitude,currPoint.Longitude,pointB.Latitude,pointB.Longitude))/1000;     
                        sDist = String.format(Locale.ROOT,"%5.2f",dDist);
                        kmlScoreDetails.append("<TD nowrap>").append(sDist).append(" km</TD>");
                        PartDist = (dDist / KmlTrace.getScore_Route_Km()) * 100;
                        kmlScoreDetails.append("<TD  align=\"right\">").append(String.format(Locale.ROOT,"%3.2f",PartDist)).append("%</TD></TR>").append(RC); 
                    } else {
                        kmlScoreDetails.append("<TD></TD><TD></TD></TR>").append(RC); 
                    }
                } else {
                    dDist = (geoutils.trigo.CoordDistance(currPoint.Latitude,currPoint.Longitude,pointB.Latitude,pointB.Longitude))/1000;     
                    sDist = String.format(Locale.ROOT,"%5.2f",dDist);
                    kmlScoreDetails.append("<TD nowrap>").append(sDist).append(" km</TD>");
                    PartDist = (dDist / KmlTrace.getScore_Route_Km()) * 100;
                    kmlScoreDetails.append("<TD  align=\"right\">").append(String.format(Locale.ROOT,"%3.2f",PartDist)).append("%</TD></TR>").append(RC); 
                }
            }
            
            // End point
            int IdxBA = KmlTrace.Score_Tb_Balises.get(LgTb);
            currPoint = KmlTrace.Tb_Calcul.get(IdxBA);
            kmlScoreDetails.append("                                                  <TR><TD>").append(i18n.tr("BA")).append("</TD>");
            kmlScoreDetails.append("<TD>").append(currPoint.dHeure.format(dtfHHmm)).append("</TD>");
            kmlScoreDetails.append("<TD  align=\"right\">").append(String.format("%03d",IdxBA)).append("</TD>");
            kmlScoreDetails.append("<TD  align=\"right\">").append(decimalFormat.format(currPoint.Latitude)).append("</TD>");
            kmlScoreDetails.append("<TD  align=\"right\">").append(decimalFormat.format(currPoint.Longitude)).append("</TD>");
            if (currMakingKml.isGraphAltiBaro()) {
                kmlScoreDetails.append("<TD>").append(String.valueOf(currPoint.AltiBaro)).append("m</TD></TR>").append(RC); 
                if (!KmlTrace.getScore_Triangle()) {
                    sCoordTrace.append(decimalFormat.format(currPoint.Longitude)).append(",");
                    sCoordTrace.append(decimalFormat.format(currPoint.Latitude)).append(",");
                    sCoordTrace.append(String.valueOf(currPoint.AltiBaro)).append(" ");
                }
            } else {
                kmlScoreDetails.append("<TD>").append(String.valueOf(currPoint.AltiGPS)).append("m</TD></TR>").append(RC);
                if (!KmlTrace.getScore_Triangle()) {
                    sCoordTrace.append(decimalFormat.format(currPoint.Longitude)).append(",");
                    sCoordTrace.append(decimalFormat.format(currPoint.Latitude)).append(",");
                    sCoordTrace.append(String.valueOf(currPoint.AltiGPS)).append(" ");
                }
            }                         
            if (!KmlTrace.getScore_Triangle()) {
                pointIGC pointB = KmlTrace.Tb_Calcul.get(KmlTrace.Score_Tb_Balises.get(LgTb-1));
                dDist = (geoutils.trigo.CoordDistance(currPoint.Latitude,currPoint.Longitude,pointB.Latitude,pointB.Longitude))/1000;     
                sDist = String.format(Locale.ROOT,"%5.2f",dDist);                
                kmlScoreDetails.append("<TD nowrap>").append(sDist).append(" km</TD>");
                PartDist = (dDist / KmlTrace.getScore_Route_Km()) * 100;
                kmlScoreDetails.append("<TD  align=\"right\">").append(String.format(Locale.ROOT,"%3.2f",PartDist)).append("%</TD></TR></Table>").append(RC); 
            } else {
                kmlScoreDetails.append("<TD></TD><TD></TD></TR>").append(RC); 
            }
            
            if (KmlTrace.getScore_Triangle()) {
                // B3 -> B1
                kmlScoreDetails.append("                                                  <TR><TD>").append(i18n.tr("B")).append("3 -> ").append(i18n.tr("B")).append("1</TD>");
                kmlScoreDetails.append("<TD></TD>");
                kmlScoreDetails.append("<TD></TD>");
                kmlScoreDetails.append("<TD></TD>");
                kmlScoreDetails.append("<TD></TD>");
                kmlScoreDetails.append("<TD></TD>");
                pointIGC pointA = KmlTrace.Tb_Calcul.get(KmlTrace.Score_Tb_Balises.get(1));
                pointIGC pointB = KmlTrace.Tb_Calcul.get(KmlTrace.Score_Tb_Balises.get(LgTb-1));
                dDist = (geoutils.trigo.CoordDistance(pointB.Latitude,pointB.Longitude,pointA.Latitude,pointA.Longitude))/1000;  
                sDist = String.format(Locale.ROOT,"%5.2f",dDist);       
                kmlScoreDetails.append("<TD nowrap>").append(sDist).append(" km</TD>");
                PartDist = (dDist / KmlTrace.getScore_Route_Km()) * 100;
                kmlScoreDetails.append("<TD  align=\"right\">").append(String.format(Locale.ROOT,"%3.2f",PartDist)).append("%</TD></TR>").append(RC); 
                if (currMakingKml.isGraphAltiBaro()) {
                    sCoordTrace.append(decimalFormat.format(pointA.Longitude)).append(",");
                    sCoordTrace.append(decimalFormat.format(pointA.Latitude)).append(",");
                    sCoordTrace.append(String.valueOf(pointA.AltiBaro)).append(" ");
                } else {
                    sCoordTrace.append(decimalFormat.format(pointA.Longitude)).append(",");
                    sCoordTrace.append(decimalFormat.format(pointA.Latitude)).append(",");
                    sCoordTrace.append(String.valueOf(pointA.AltiGPS)).append(" ");
                }      
                // BD -> BA
                kmlScoreDetails.append("                                                  <TR><TD>").append(i18n.tr("BD")).append(" -> ").append(i18n.tr("BA")).append("</TD>");
                kmlScoreDetails.append("<TD></TD>");
                kmlScoreDetails.append("<TD></TD>");
                kmlScoreDetails.append("<TD></TD>");
                kmlScoreDetails.append("<TD></TD>");
                kmlScoreDetails.append("<TD></TD>");
                pointA = KmlTrace.Tb_Calcul.get(IdxBA);
                pointB = KmlTrace.Tb_Calcul.get(IdxBD);
                dDist = (geoutils.trigo.CoordDistance(pointA.Latitude,pointA.Longitude,pointB.Latitude,pointB.Longitude))/1000;  
                sDist = String.format(Locale.ROOT,"%5.2f",dDist);                    
                kmlScoreDetails.append("<TD nowrap>").append(sDist).append(" km</TD>");
                kmlScoreDetails.append("<TD></TD></TR></TABLE>").append(RC); 
            }
            
            res = true;
        } catch (Exception e) {
            res = false;
        }     
        
        return res;
    }
    
     /**
     * Draw lines between turnpoints
     * in xLogfly -> kml_I_CFD_Trace
     * @return 
     */
    private boolean genScoreTrace() {
        
        boolean res = false;
        int LgTb;
        double d;
        
        try {
            LgTb = KmlTrace.Score_Tb_Balises.size()-1;    
            kmlScoreTrace.append("                                   <Placemark>").append(RC); 
            kmlScoreTrace.append("                                            <name>").append(KmlTrace.getScore_Shape()).append("</name>").append(RC); 
            kmlScoreTrace.append("                                            <description></description>").append(RC); 
            kmlScoreTrace.append("                                            <visibility>0</visibility>").append(RC); 
            kmlScoreTrace.append("                                            <Style>").append(RC); 
            kmlScoreTrace.append("                                                   <LineStyle>").append(RC); 
            kmlScoreTrace.append("                                                          <color>FF00FFFF</color>").append(RC); 
            kmlScoreTrace.append("                                                          <width>2</width>").append(RC); 
            kmlScoreTrace.append("                                                   </LineStyle>").append(RC); 
            kmlScoreTrace.append("                                            </Style>").append(RC); 
            kmlScoreTrace.append("                                            <LineString>").append(RC); 
            kmlScoreTrace.append("                                                   <tessellate>1</tessellate>").append(RC); 
            kmlScoreTrace.append("                                                   <altitudeMode>absolute</altitudeMode>").append(RC); 
            kmlScoreTrace.append("                                                   <coordinates>").append(RC); 
            kmlScoreTrace.append("                                                          ").append(sCoordTrace.toString()).append(RC); 
            kmlScoreTrace.append("                                                   </coordinates>").append(RC); 
            kmlScoreTrace.append("                                            </LineString>").append(RC); 
            kmlScoreTrace.append("                                   </Placemark>").append(RC); 
            // Turnpoints
            for (int i = 0; i <= LgTb; i++) {
                if (i == 0) {                    
                    kmlScoreTrace.append(codeBalise(KmlTrace.Score_Tb_Balises.get(i),i18n.tr("BD"),0,"",KmlTrace.Score_Tb_Balises.get(i+1),i18n.tr("B")+String.valueOf(i+1)));
                } else if (i == LgTb) {
                    kmlScoreTrace.append(codeBalise(KmlTrace.Score_Tb_Balises.get(i),i18n.tr("BA"),KmlTrace.Score_Tb_Balises.get(i-1),i18n.tr("B")+String.valueOf(i-1),0,""));
                } else {
                    kmlScoreTrace.append(codeBalise(KmlTrace.Score_Tb_Balises.get(i),i18n.tr("B")+String.valueOf(i),KmlTrace.Score_Tb_Balises.get(i-1),i18n.tr("P")+String.valueOf(i-1),KmlTrace.Score_Tb_Balises.get(i+1),"P"+String.valueOf(i+1)));
                }
                    
            }

            res = true;
        } catch (Exception e) {
            res = false;
        }     
        
        return res;
    }
    
    /*
    * Details of a turnpoint
    */
    private String codeBalise(int IdxCurr, String nCurr,int IdxPcdt, String nPcdt, int IdxSvt, String nSvt) {
        StringBuilder res = new StringBuilder();
        String sAlti, sDist;
        double dDist;        
        pointIGC currPoint;
        
        currPoint = KmlTrace.Tb_Calcul.get(IdxCurr);
        if (currMakingKml.isGraphAltiBaro()) 
            sAlti = String.valueOf(currPoint.AltiBaro);
        else
            sAlti = String.valueOf(currPoint.AltiGPS);
        res.append("                                   <Placemark>").append(RC); 
        res.append("                                            <name>").append(nCurr).append("</name>").append(RC); 
        res.append("                                            <description>").append(currPoint.dHeure.format(dtfHHmm)).append(" - ");
        res.append(sAlti).append("m").append(RC); 
        if (IdxPcdt > 0) {
            pointIGC pointPcdt = KmlTrace.Tb_Calcul.get(IdxPcdt);
            dDist = (geoutils.trigo.CoordDistance(currPoint.Latitude,currPoint.Longitude,pointPcdt.Latitude,pointPcdt.Longitude))/1000;  
            sDist = String.format(Locale.ROOT,"%5.2f",dDist);       
            res.append("                                                       ").append(i18n.tr("Distance")).append(" ").append(nPcdt).append(" ").append(sDist).append(" km").append(RC);   
        }
        if (IdxSvt > 0) {
            pointIGC pointSvt = KmlTrace.Tb_Calcul.get(IdxSvt);
            dDist = (geoutils.trigo.CoordDistance(pointSvt.Latitude,pointSvt.Longitude,currPoint.Latitude,currPoint.Longitude))/1000; 
            sDist = String.format(Locale.ROOT,"%5.2f",dDist); 
            res.append("                                                       ").append(i18n.tr("Distance")).append(" ").append(nSvt).append(" ").append(sDist).append(" km").append(RC); 
        }
        res.append("                                            </description>").append(RC); 
        res.append("                                            <visibility>0</visibility>").append(RC); 
        res.append("                                            <styleUrl>root://styles#default+icon=0x307</styleUrl>").append(RC); 
        res.append("                                            <Style>").append(RC); 
        res.append("                                                   <IconStyle>").append(RC); 
        res.append("                                                          <Icon>").append(RC); 
        res.append("                                                                 <href>root://icons/palette-4.png</href>").append(RC); 
        res.append("                                                                  <x>64</x><y>128</y>").append(RC); 
        res.append("                                                                 <w>32</w>").append(RC); 
        res.append("                                                                 <h>32</h>").append(RC); 
        res.append("                                                          </Icon>").append(RC); 
        res.append("                                                   </IconStyle>").append(RC); 
        res.append("                                                   <LabelStyle>").append(RC); 
        res.append("                                                          <scale>1</scale>").append(RC); 
        res.append("                                                   </LabelStyle>").append(RC); 
        res.append("                                            </Style>").append(RC); 
        res.append("                                            <Point>").append(RC); 
        res.append("                                                   <altitudeMode>absolute</altitudeMode>").append(RC); 
        res.append("                                                   <extrude>1</extrude>").append(RC); 
        res.append("                                                   <coordinates>").append(decimalFormat.format(currPoint.Longitude)).append(",");
        res.append(decimalFormat.format(currPoint.Latitude)).append(",").append(sAlti).append("</coordinates>").append(RC); 
        res.append("                                            </Point>").append(RC); 
        res.append("                                   </Placemark>").append(RC); 
        
        return res.toString();
    }
            
}
