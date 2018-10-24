/*
 * Copyright Gil THOMAS
 * This file forms an integral part of Logfly project
 * See the LICENSE file distributed with source code
 * for details of Logfly licence project
 */
package org.logfly.trackgps;

import io.jenetics.jpx.WayPoint;
import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.logfly.airspacelib.Airspace;
import org.logfly.airspacelib.AirspaceCategory;
import org.logfly.airspacelib.Helpers;
import org.logfly.airspacelib.dbAirspace;
import org.logfly.dialog.alertbox;
import org.logfly.igc.pointIGC;
import org.logfly.settings.configProg;
import org.logfly.systemio.mylogging;

/**
 *
 * @author gil
 */
public class checkAirspace {
    
    // Settings
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
    private final String RC = "\n";
    private DecimalFormatSymbols decimalFormatSymbols = new DecimalFormatSymbols();        
    
    public checkAirspace (configProg currConfig, File airFile)  {
        // myParam unused... this is just a new constructor
        myConfig = currConfig;
        currDbAir = new dbAirspace(airFile, null);
        if (currDbAir.isDbOK()) {
            airNameFile = airFile.getName();
            airTotal = currDbAir.getNbAirspaces();   
            airDbLoad = true;
        }
        decimalFormatSymbols.setDecimalSeparator('.');
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
            //sbReq.append("CREATE VIEW SELECT_VIEW AS SELECT Z_ID,Name, Geojson, Openair, Classe,Floor,Ceiling,Visu FROM Zones ");
            sbReq.append("CREATE VIEW SELECT_VIEW AS SELECT * FROM Zones ");
            sbReq.append("WHERE Visu = '1' AND (((LatMini >= ").append(sLatMini).append(" AND LatMini <= ").append(sLatMaxi).append(") ");
            sbReq.append("OR (LatMaxi >= ").append(sLatMini).append(" AND LatMaxi <= ").append(sLatMaxi).append(")) ");
            sbReq.append("AND ((LongMini >= ").append(sLongMini).append(" AND LongMini <= ").append(sLongMaxi).append(") ");
            sbReq.append("OR (LongMaxi >= ").append(sLongMini).append(" AND LongMaxi <= ").append(sLongMaxi).append(")))");  
            sbReq.append(" AND (Floor < ").append(sAltMax).append(")");
            Statement st2 = conn.createStatement();
            st2.execute(sbReq.toString());
            st2.close();       
            System.out.println(sbReq.toString());
            // How many airspaces selected
            Statement st3 = conn.createStatement();
            ResultSet rs = null;                
            String reqCount = "SELECT Count(Z_ID) FROM SELECT_VIEW  WHERE Visu = '1'";                 
            rs = st3.executeQuery(reqCount);
            if (rs != null && rs.next())  {   
                airToCheck = rs.getInt(1);
                buildPolygons();
                res = 1;
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
    
    private int buildPolygons() {
        int res = - 1;
        int cpt = 0;
        String sReq = "SELECT Name, Geojson, Classe, Ceiling, Floor FROM SELECT_VIEW ";                                   
        try {
            Statement stmt = currDbAir.getDbConn().createStatement();     
            ResultSet rs = null;            
            rs = stmt.executeQuery(sReq);
            if (rs != null)  {  
                while (rs.next()) {  
                    Airspace airspace = new Airspace();
                    airspace.Category = AirspaceCategory.valueOf(Helpers.findRegex("[A-Za-z]+\\w|[A-Za-z]", rs.getString(3)));
                    airspace.AltLimit_Top = rs.getInt(4);
                    airspace.AltLimit_Bottom = rs.getInt(5);
                    airspace.Name = rs.getString(1);
                    airspace.setDbGeoJson(rs.getString(2));
                    ArrayList<Coordinate> lsCoord = geojsonToCoord(rs.getString(2));
                    if (lsCoord.size() > 0) {
                        airspace.coordinates = lsCoord;
                    }  
                    lsAirsp.add(airspace);
                    cpt++;
                }
                airPolygons = cpt;
            }      
        } catch (Exception ex ) {
            sbError = new StringBuilder(this.getClass().getName()+"."+Thread.currentThread().getStackTrace()[1].getMethodName());
            sbError.append("\r\n").append(ex.toString());
            mylogging.log(Level.SEVERE, sbError.toString());  
        }       
        
        return res;
    }
    
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
    
    public int checkPoints() {
        int res = 0;
        boolean onceViolation = false;
        GeometryFactory gf = new GeometryFactory();        
        int totPoints = evalTrace.Tb_Good_Points.size();
        try {
            for(int i = 0; i<=totPoints-1; i++)
            {
                pointIGC currPoint = evalTrace.Tb_Good_Points.get(i);
                Coordinate coord = new Coordinate(currPoint.getLongitude(), currPoint.getLatitude());
                Point point = gf.createPoint(coord);
                for (int j = 0; j <  lsAirsp.size(); j++) {
                    if (point.within(lsAirsp.get(j).getGeometry())) {
                        if (currPoint.AltiGPS > lsAirsp.get(j).AltLimit_Bottom || currPoint.AltiGPS > lsAirsp.get(j).AltLimit_Top) {
                            lsAirsp.get(j).setViolations(lsAirsp.get(j).getViolations()+1);                            
                            currPoint.violation = true;
                            onceViolation = true;
                        }
                    }
                }
            }         
            if (onceViolation) {
                genBadPointsGeoJson();
                genViolatedGeojson();
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
    
    private void genViolatedGeojson() {
        
        sbViGeoJson = new StringBuilder();        
        sbViGeoJson.append("    var zoneReg = {").append("    \"type\": \"FeatureCollection\",").append(RC);
        sbViGeoJson.append("    \"crs\": { \"type\": \"name\", \"properties\": { \"name\": \"urn:ogc:def:crs:OGC:1.3:CRS84\" } },").append(RC);
        sbViGeoJson.append("    \"features\": [").append(RC);        
        for (int i = 0; i < lsAirsp.size(); i++) {
            if (lsAirsp.get(i).getViolations() > 0) {
                sbViGeoJson.append(lsAirsp.get(i).getDbGeoJson()).append(",");
           //     System.out.println(lsAirsp.get(i).Name+"  "+lsAirsp.get(i).getViolations()+" violations");
            }
        } 
        // totalGeoJson is completed, last comma deleted
        if (sbViGeoJson.length() > 0) {
            sbViGeoJson.setLength(sbViGeoJson.length() - 1);
        }                       
        sbViGeoJson.append("]}");     
    }
    
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
    
}
