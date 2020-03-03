/*
 * Copyright Gil THOMAS
 * This file forms an integral part of Logfly project
 * See the LICENSE file distributed with source code
 * for details of Logfly licence project
 */
package trackgps;

import airspacelib.Airspace;
import airspacelib.AirspaceCategory;
import airspacelib.Helpers;
import airspacelib.dbAirspace;
import igc.pointIGC;
import java.io.File;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.xnap.commons.i18n.I18n;
import settings.configProg;
import systemio.mylogging;

/**
 *
 * @author gil
 * 
 * Checks whether a gps track violates one or more airspaces
 * 
 */
public class checkAirspace {
    
    private configProg myConfig;    
    private dbAirspace currDbAir = null;
    private int airTotal = 0;
    private String airNameFile = null;    
    private boolean airDbLoad = false;
    private int airToCheck;
    private int airPolygons;
    private int nbBadpoints;
    private traceGPS evalTrace;
    private StringBuilder sbError;  
    private List<Airspace> lsAirsp = new ArrayList<Airspace>();
    private StringBuilder sbViGeoJson;
    private StringBuilder sbPtGeoJson;
    private StringBuilder sbBadPoints;
    private StringBuilder sbCheckedGeoJson;
    private StringBuilder sbTested;
    private final String RC = "\n";
    private DecimalFormatSymbols decimalFormatSymbols = new DecimalFormatSymbols();    
    private I18n i18n;    
    private String strDash = "-------------------------------------------------------------------------------";
    private StringBuilder sbInfo;
    private boolean withoutE;
    
    public checkAirspace (configProg currConfig, File airFile, boolean pWithoutE)  {
        myConfig = currConfig;
        i18n = myConfig.getI18n();
        withoutE = pWithoutE;
        sbInfo = new StringBuilder();
        sbInfo.append("  ").append("The content of this report is placed in the clipboard").append(RC).append(RC);
        sbInfo.append("  ").append(i18n.tr("File")).append(" : ").append(airFile.getName()).append(RC);
        currDbAir = new dbAirspace(airFile, null);
        if (currDbAir.isDbOK()) {
            airNameFile = airFile.getName();
            airTotal = currDbAir.getNbAirspaces();   
            airDbLoad = true;
        }
        decimalFormatSymbols.setDecimalSeparator('.');
        sbTested = new StringBuilder();
    }   

    public dbAirspace getCurrDbAir() {
        return currDbAir;
    }

    public int getAirTotal() {
        return airTotal;
    }

    public int getAirToCheck() {
        return airToCheck;
    }
        
    public boolean isAirDbLoad() {
        return airDbLoad;
    }

    public int getAirPolygons() {
        return airPolygons;
    }

    public String getViGeoJson() {
        return sbViGeoJson.toString();
    }

    public String getPtGeoJson() {
        return sbPtGeoJson.toString();
    }

    public int getNbBadpoints() {
        return nbBadpoints;
    }   

    public String getCheckedGeoJson() {
        return sbCheckedGeoJson.toString();
    }
     
    /**
     * Determines the airspaces to be checked in the selected file
     * @param pTrace
     * @return 
     */
    public int prepareCheck(traceGPS pTrace) {
        
        int res = -1;
        DecimalFormat latFormat = new DecimalFormat("##.###", decimalFormatSymbols);
        DecimalFormat longFormat = new DecimalFormat("###.###", decimalFormatSymbols);
        evalTrace = pTrace;
        StringBuilder sbReq = new StringBuilder();
        String sLatMini = latFormat.format(evalTrace.getLatMini());
        String sLatMaxi = latFormat.format(evalTrace.getLatMaxi());
        String sLongMini = longFormat.format(evalTrace.getLongMini());
        String sLongMaxi =longFormat.format(evalTrace.getLongMaxi());
        String sAltMax = String.valueOf(evalTrace.getAlt_Maxi_GPS().AltiGPS+1);
        try {
            Connection conn = currDbAir.getDbConn();                                 
            // With PreparedStatement we had an error 
            // [SQLITE_ERROR] SQL error or missing database (parameters are not allowed in views)
            sbReq.append("CREATE VIEW SELECT_VIEW AS SELECT * FROM Zones ");
            sbReq.append("WHERE Visu = '1' AND ((((LatMini >= ").append(sLatMini).append(" AND LatMini <= ").append(sLatMaxi).append(") ");
            sbReq.append("OR (LatMaxi >= ").append(sLatMini).append(" AND LatMaxi <= ").append(sLatMaxi).append(")) ");
            sbReq.append("AND ((LongMini >= ").append(sLongMini).append(" AND LongMini <= ").append(sLongMaxi).append(") ");
            sbReq.append("OR (LongMaxi >= ").append(sLongMini).append(" AND LongMaxi <= ").append(sLongMaxi).append(")))"); 
            sbReq.append("OR (").append(sLatMini).append(" > LatMini AND ").append(sLatMaxi);
            sbReq.append(" < LatMaxi AND ").append(sLongMini).append(" > LongMini AND ").append(sLongMaxi);
            sbReq.append(" < LongMaxi)) AND (Floor < ").append(sAltMax).append(")");
            Statement st2 = conn.createStatement();
            st2.execute(sbReq.toString());
            st2.close();       
            // How many airspaces selected
            Statement st3 = conn.createStatement();
            ResultSet rs = null;                
            String reqCount = "SELECT Count(Z_ID) FROM SELECT_VIEW  WHERE Visu = '1'";                
            rs = st3.executeQuery(reqCount);
            if (rs != null && rs.next())  {   
                airToCheck = rs.getInt(1);
            } 
            if (airToCheck > 0) {
                buildPolygons();
                res = 1;   
            } else {
                res = 0;
            }               
        } catch (Exception ex) {
            sbError = new StringBuilder(this.getClass().getName()+"."+Thread.currentThread().getStackTrace()[1].getMethodName());
            sbError.append("\r\n").append(ex.toString());
            sbError.append("\r\n").append(sbReq.toString());
            mylogging.log(Level.SEVERE, sbError.toString());  
            res = 220; //  Impossible de créer la sélection d'espaces aériens
        } 
        
        return res;
    }
    
    /**
     * Build geojson from selected airspaces
     * @return 
     */
    private int buildPolygons() {
        int res = - 1;
        int cpt = 0;
        String sReq = "SELECT Name, Geojson, Classe, Ceiling, Ceiling_AGL, Floor, Floor_AGL FROM SELECT_VIEW ";                                   
        try {
            Statement stmt = currDbAir.getDbConn().createStatement();     
            ResultSet rs = null;            
            rs = stmt.executeQuery(sReq);
            if (rs != null)  {                  
                sbTested.append(strDash).append(RC);
                sbTested.append("  ").append(i18n.tr("Checked airspaces")).append(RC);
                sbTested.append(strDash).append(RC);
                while (rs.next()) {  
                    Airspace airspace = new Airspace();
                    airspace.Category = AirspaceCategory.valueOf(Helpers.findRegex("[A-Za-z]+\\w|[A-Za-z]", rs.getString(3)));
                    if (withoutE && airspace.Category == AirspaceCategory.E) {
                        continue;
                    } else {
                        airspace.AltLimit_Top = rs.getInt(4);
                        airspace.AltLimit_Top_AGL = rs.getInt(5);
                        airspace.AltLimit_Bottom = rs.getInt(6);
                        airspace.AltLimit_Bottom_AGL = rs.getInt(7);
                        airspace.Name = rs.getString(1);
                        airspace.setDbGeoJson(rs.getString(2));
                        ArrayList<Coordinate> lsCoord = geojsonToCoord(rs.getString(2));
                        if (lsCoord.size() > 0) {
                            airspace.coordinates = lsCoord;
                        }  
                        lsAirsp.add(airspace);
                        sbTested.append("  ").append(airspace.Name).append(RC);
                        sbTested.append("     ").append(i18n.tr("Floor")).append(" ").append(String.valueOf(airspace.AltLimit_Bottom)).append("m  ");
                        sbTested.append(i18n.tr("Ceiling")).append(" ").append(String.valueOf(airspace.AltLimit_Top)).append("m").append(RC);
                        cpt++;
                    }
                }
                airPolygons = cpt;
                StringBuilder sb = new StringBuilder();
                sb.append(strDash).append(RC);
                sb.append("  ").append(String.valueOf(cpt)).append(" ").append(i18n.tr("airspaces checked")).append(RC);
                sb.append(strDash).append(RC);
                
                sbTested.append(RC);
            }      
        } catch (Exception ex ) {
            sbError = new StringBuilder(this.getClass().getName()+"."+Thread.currentThread().getStackTrace()[1].getMethodName());
            sbError.append("\r\n").append(ex.toString());
            mylogging.log(Level.SEVERE, sbError.toString());  
        }       
        
        return res;
    }
    
    /**
     * Build geometric polygons from selected airspaces
     * @param strGeoJson
     * @return 
     */
    private ArrayList<Coordinate> geojsonToCoord(String strGeoJson) {
        
        ArrayList<Coordinate> lsCoord = new ArrayList<Coordinate>();
        
        JSONParser jsonParser = new JSONParser();
        try {
            JSONObject jsonObject = (JSONObject) jsonParser.parse(strGeoJson);
            JSONObject jsGeometry = (JSONObject) jsonObject.get("geometry");
            JSONArray totalCoord = (JSONArray) jsGeometry.get("coordinates");
            if (totalCoord != null)  {
                for(int i=0; i< totalCoord.size(); i++){                
                    JSONArray coord = (JSONArray) totalCoord.get(i);
                    for (int j = 0; j < coord.size(); j++) {
                        JSONArray latLong = (JSONArray) coord.get(j);
                        double dLat = Double.valueOf(latLong.get(1).toString());
                        double dLong = Double.valueOf(latLong.get(0).toString());
                        lsCoord.add(new Coordinate(dLong, dLat));                                                
                    }
                }            
            } else {
                sbError = new StringBuilder(this.getClass().getName()+"."+Thread.currentThread().getStackTrace()[1].getMethodName());
                sbError.append("\r\n").append("JSON Array null "+totalCoord.toJSONString());
                mylogging.log(Level.SEVERE, sbError.toString());                  
                System.out.println("JSONArray nul");
            }
        } catch (Exception e) {
            System.out.println("Erreur "+e.getMessage());
        }
        
        return lsCoord;
    }   
    
    /**
     * Check every point of the track
     * @return 
     */
    public int checkPoints() {
        int res = 0;
        boolean onceViolation = false;
        GeometryFactory gf = new GeometryFactory();       
        int totPoints = evalTrace.Tb_Good_Points.size();
        int iMult = evalTrace.getAlt_Maxi_GPS().AltiGPS/500;         
        int maxGraphValue;
        if (iMult*500 < evalTrace.getAlt_Maxi_GPS().AltiGPS)
            maxGraphValue = (iMult*500)+500;
        else
            maxGraphValue = iMult*500;
        try {
            for(int i = 0; i<=totPoints-1; i++)
            {
                pointIGC currPoint = evalTrace.Tb_Good_Points.get(i);
                Coordinate coord = new Coordinate(currPoint.getLongitude(), currPoint.getLatitude());
                Point point = gf.createPoint(coord);
                for (int j = 0; j <  lsAirsp.size(); j++) {
                    if (point.within(lsAirsp.get(j).getGeometry())) {
                        int localLimitBottom;
                        int localLimitTop;
                        if (lsAirsp.get(j).AltLimit_Bottom_AGL == 1) {
                            localLimitBottom = currPoint.elevation + lsAirsp.get(j).AltLimit_Bottom;
                        } else {
                            localLimitBottom = lsAirsp.get(j).AltLimit_Bottom;
                        }    
                        if (lsAirsp.get(j).AltLimit_Top_AGL == 1) {
                            localLimitTop = currPoint.elevation + lsAirsp.get(j).AltLimit_Top;
                        } else {
                            localLimitTop = lsAirsp.get(j).AltLimit_Top;
                        }             
                        if (currPoint.AltiGPS > localLimitBottom && currPoint.AltiGPS < localLimitTop) {
                            lsAirsp.get(j).setViolations(lsAirsp.get(j).getViolations()+1);                            
                            currPoint.violation = true;
                            currPoint.violationName = lsAirsp.get(j).Name;  
                            currPoint.violationLimit = localLimitTop;
                            onceViolation = true;
                        } 
                        if (localLimitTop < maxGraphValue)
                            currPoint.airspaceTop = localLimitTop;
                        if (localLimitBottom > 0) {
                            if (localLimitBottom < maxGraphValue)
                                currPoint.airspaceBottom = localLimitBottom;
                            else
                                currPoint.airspaceBottom = maxGraphValue;
                        }                                        
                    }
                }
            }         
            if (onceViolation) {
                genBadPointsGeoJson();
                genViolatedGeojson();
            } else {
                genCheckedGeojson();
            }
        } catch (Exception e) {
            sbError = new StringBuilder(this.getClass().getName()+"."+Thread.currentThread().getStackTrace()[1].getMethodName());
            sbError.append("\r\n").append("Error in and track ansd airspace loops");
            mylogging.log(Level.SEVERE, sbError.toString());                  
            nbBadpoints = -1;
        } finally {
            res = nbBadpoints;
        }
        
        return res;
    }
    
    
    /**
     * generate geojson of violated airspaces
     * will be used in fullmap
     */
    private void genViolatedGeojson() {
        
        sbViGeoJson = new StringBuilder();        
        sbViGeoJson.append("    var zoneReg = {").append("    \"type\": \"FeatureCollection\",").append(RC);
        sbViGeoJson.append("    \"crs\": { \"type\": \"name\", \"properties\": { \"name\": \"urn:ogc:def:crs:OGC:1.3:CRS84\" } },").append(RC);
        sbViGeoJson.append("    \"features\": [").append(RC);        
        for (int i = 0; i < lsAirsp.size(); i++) {
            if (lsAirsp.get(i).getViolations() > 0) {
                sbViGeoJson.append(lsAirsp.get(i).getDbGeoJson()).append(",");
            }
        } 
        // totalGeoJson is completed, last comma deleted
        if (sbViGeoJson.length() > 0) {
            sbViGeoJson.setLength(sbViGeoJson.length() - 1);
        }                       
        sbViGeoJson.append("]}");     
    }
    
    /**
     * generate geojson of checked airspaces
     * will be used in fullmap
     */
    private void genCheckedGeojson() {
        
        sbCheckedGeoJson = new StringBuilder();        
        sbCheckedGeoJson.append("    var zoneReg = {").append("    \"type\": \"FeatureCollection\",").append(RC);
        sbCheckedGeoJson.append("    \"crs\": { \"type\": \"name\", \"properties\": { \"name\": \"urn:ogc:def:crs:OGC:1.3:CRS84\" } },").append(RC);
        sbCheckedGeoJson.append("    \"features\": [").append(RC);        
        for (int i = 0; i < lsAirsp.size(); i++) {            
            sbCheckedGeoJson.append(lsAirsp.get(i).getDbGeoJson()).append(",");            
        } 
        // totalGeoJson is completed, last comma deleted
        if (sbCheckedGeoJson.length() > 0) {
            sbCheckedGeoJson.setLength(sbCheckedGeoJson.length() - 1);
        }                       
        sbCheckedGeoJson.append("]}");     
    }    
    
    /**
     * generate geojson of bad points
     * will be used in fullmap
     */
    private void genBadPointsGeoJson() {
        DecimalFormat decimalFormat = new DecimalFormat("###.00000", decimalFormatSymbols);  
        int totPoints = evalTrace.Tb_Good_Points.size();
        sbBadPoints = new StringBuilder();
        sbPtGeoJson = new StringBuilder();        
        nbBadpoints = 0;
        sbPtGeoJson.append("       var badPoints = { \"type\": \"Feature\", \"geometry\": { \"type\": \"MultiPoint\", \"coordinates\": [");
        for(int i = 0; i<=totPoints-1; i++) {
            pointIGC currPoint = evalTrace.Tb_Good_Points.get(i); 
            if (currPoint.violation) {
                sbBadPoints.append("[").append(decimalFormat.format(currPoint.Longitude)).append(",").append(decimalFormat.format(currPoint.Latitude)).append("],");
                nbBadpoints++;
            }
        }
        // last comma deleted
        if (sbBadPoints.length() > 0) {
            sbBadPoints.setLength(sbBadPoints.length() - 1);
        }     
        sbPtGeoJson.append(sbBadPoints.toString());
        sbPtGeoJson.append("]} ,\"style\": {'color': \"#FFFF00\",'weight': 2,'opacity': 1}};  ");       
    }
    
    /**
     * Format a string with complete reporting : 
     * checked airspaces and violation enter/exit time
     * @return 
     */    
    public String reportPoints() {
        String res = "";   
        DateTimeFormatter dtfHHmm = DateTimeFormatter.ofPattern("HH:mm");
        StringBuilder sbRep = new StringBuilder();
        Boolean currViolation = false;
        int totPoints = evalTrace.Tb_Good_Points.size();
        int nbViolPoints = 0;
        try {
            for(int i = 0; i<=totPoints-1; i++)
            {
                pointIGC currPoint = evalTrace.Tb_Good_Points.get(i);                
                if (currPoint.violation) {
                    nbViolPoints++;
                    if (!currViolation) {
                        System.out.println("NbViolPoints "+nbViolPoints);
                        sbRep.append(currPoint.violationName).append(RC);
                        sbRep.append("     ").append(i18n.tr("enter")).append(" : ").append(currPoint.getTimeHHMMSS()).append(" ").append(i18n.tr("GPS alt"));
                        sbRep.append(" ").append(String.valueOf(currPoint.getElevation())).append("m").append(RC); 
                        currViolation = true;
                    }
                }
                if (!currPoint.violation && currViolation) {
                    sbRep.append("     ").append(i18n.tr("exit")).append(" : ").append(currPoint.getTimeHHMMSS()).append(" ").append(i18n.tr("GPS alt"));
                    sbRep.append(" ").append(String.valueOf(currPoint.getElevation())).append("m").append(RC); 
                    sbRep.append("     ").append(String.valueOf(nbViolPoints)).append(" ").append("point(s)").append(RC); 
                    currViolation = false;
                    nbViolPoints = 0;
                }                                
            }         
        } catch (Exception e) {
            sbError = new StringBuilder(this.getClass().getName()+"."+Thread.currentThread().getStackTrace()[1].getMethodName());
            sbError.append("\r\n").append("Error in and track ansd airspace loops");
            mylogging.log(Level.SEVERE, sbError.toString());                  
            nbBadpoints = -1;
        } finally {         
            sbInfo.append("  ").append(String.valueOf(nbBadpoints)).append(" ").append("point(s) within the airspace(s)").append(RC).append(RC);
            sbTested.insert(0, sbInfo.toString());
            sbTested.append(strDash).append(RC);
            if (nbBadpoints > 0) {
                sbTested.append("  ").append(i18n.tr("violation(s)")).append(RC);
                sbTested.append("  ").append("(").append(i18n.tr("if a point is in more than one airspace, the last tested is displayed")).append(")").append(RC);
                sbTested.append(strDash).append(RC);
                sbTested.append(sbRep.toString());
            }
            res = sbTested.toString();            
        }
        
        return res;
    }    
    
}
