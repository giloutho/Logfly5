/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.logfly.airspacelib;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.logging.Level;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.locationtech.jts.geom.Coordinate;

/**
 *
 * @author gil
 */
public class dbAirspace {
    
    private Connection dbConn;
    private String filePath;
    private String fileName;
    private String fullDbName;
    private int nbAirspaces;
    private boolean dbOK;
    private JSONObject airspaceJsonObject;  
    private StringBuilder sbError;    
    
    /**
     * 
     * @param pFile  airspaces file     
     */
    public dbAirspace (File pFile, Connection pDbConn) {
        fileName = pFile.getName().replace(".txt","");
        filePath = pFile.getParent();   
        Airspaces airspacesSet = new Airspaces();
        airspacesSet.OpenOpenAirTextFile(pFile.getAbsolutePath()); 
        if (pDbConn == null) {
            dbOK = false; 
            insertInMemDb(airspacesSet);          
        } else {
            this.dbConn = pDbConn;
            addToMemDb(airspacesSet); 
        }
    }    

    public Connection getDbConn() {
        return dbConn;
    }

    public int getNbAirspaces() {
        return nbAirspaces;
    }

    public boolean isDbOK() {
        return dbOK;
    }        
            
    private void insertInMemDb(Airspaces airspaces) {
        if (createMemDb()) {
            int cpt = 0;
            for(Airspace airspace: airspaces)
            {                
                String airJson = airspaceToJson(airspace);
                if (airJson != null) {
                    cpt = cpt+addAirspaceToDb(airspace,airJson);
                }
            }
            System.out.println(cpt+" airspaces insérés dans la db");
            nbAirspaces = cpt;
            if (nbAirspaces > 0) dbOK = true;
        }
    }  
    
    private void addToMemDb(Airspaces airspaces) {
            
        int cpt = 0;
        for(Airspace airspace: airspaces)
        {                
            String airJson = airspaceToJson(airspace);
            if (airJson != null) {
                cpt = cpt+addAirspaceToDb(airspace,airJson);
            }
        }
        System.out.println(cpt+" airspaces ajoutés dans la db");
        // On fera une requête pour tout recompter
        try {
            Statement stmt = dbConn.createStatement();
            ResultSet rs = null;    
            String sReq = "SELECT Count(Z_ID) FROM Zones  WHERE Visu = '1'";
            rs = stmt.executeQuery(sReq);
            if (rs != null && rs.next())  {   
                nbAirspaces = rs.getInt(1);
                if (nbAirspaces > 0) dbOK = true; 
            }
        } catch (Exception e) {
            System.out.println("db error...");
        }              
    }  
                
    private void insertInDbAirspaces(Airspaces airspaces) {
        if (createDb()) {
            int cpt = 0;
            for(Airspace airspace: airspaces)
            {
                String airJson = airspaceToJson(airspace);
                if (airJson != null) {
                    cpt = cpt+addAirspaceToDb(airspace,airJson);
                }
            }
            System.out.println(cpt+" airspaces insérés dans la db");
            closeDb(); 
        }
    }
    
    private String airspaceToJson(Airspace airspace) {
        
        String res = null;
        
        try {
            JSONObject feature = new JSONObject();
            feature.put("type", "Feature");
            JSONObject properties = new JSONObject();
            properties.put("Cat", (airspace.typeColor == null)? "0" : airspace.typeColor);
            properties.put("Class", (airspace.Category == null)? "UNK" : airspace.Category.toString());            
            properties.put("Name", (airspace.Name == null)? "UNK" : airspace.Name );
            properties.put("Floor",(airspace.AltLimit_Bottom == null)? "UNK" : airspace.AltLimit_Bottom );
            properties.put("Ceiling",(airspace.AltLimit_Top == null)? "UNK" : airspace.AltLimit_Top );
            feature.put("properties", properties);


            JSONArray polygons = new JSONArray();
            JSONArray coordinates = new JSONArray();
            for (Coordinate c: airspace.coordinates)
            {
                JSONArray coordinate = new JSONArray();
                coordinate.add(0, c.x);
                coordinate.add(1, c.y);
                coordinates.add(coordinate);
            }

            polygons.add(coordinates);

            JSONObject geometry = new JSONObject();
            geometry.put("type", "Polygon");
            geometry.put("coordinates", polygons);

            feature.put("geometry",geometry); 
            res = feature.toString();
        } catch (Exception e) {
            
        }

        return res;
    }
    
    private int addAirspaceToDb(Airspace airspace, String airJson) {
        int res = 0;
        try {            
            StringBuilder insertTableSQL = new StringBuilder();        
            insertTableSQL.append("INSERT INTO Zones ( Openair, Geojson, Name, Classe, Floor, Ceiling, LatMini, LatMaxi, LongMini, LongMaxi, Visu ) VALUES");
            insertTableSQL.append("(?,?,?,?,?,?,?,?,?,?,?)");  
            PreparedStatement preparedStatement = dbConn.prepareStatement(insertTableSQL.toString());
            preparedStatement.setString(1, airspace.originalText.toString());
            preparedStatement.setString(2, airJson);
            preparedStatement.setString(3, airspace.Name);
            preparedStatement.setString(4, airspace.Category.toString());
            preparedStatement.setInt(5, airspace.AltLimit_Bottom);
            preparedStatement.setInt(6, airspace.AltLimit_Top);
            preparedStatement.setDouble(7, airspace.getLatMini());
            preparedStatement.setDouble(8, airspace.getLatMaxi());
            preparedStatement.setDouble(9, airspace.getLongMini());
            preparedStatement.setDouble(10, airspace.getLongMaxi());
            preparedStatement.setInt(11, 1);
            preparedStatement.executeUpdate();              
            res = 1;            
        } catch (Exception e) {
//            res = 1104;   // Insertion error in flights file                                           
//            sbError = new StringBuilder(this.getClass().getName()+"."+Thread.currentThread().getStackTrace()[1].getMethodName());
//            sbError.append("\r\n").append(e.getMessage());
//            sbError.append("\r\n").append("Date vol : ").append(pTrace.getDate_Vol_SQL());
//            mylogging.log(Level.SEVERE, sbError.toString());            
        }                    
        
        return res;
    }
    
    public boolean openDbz() {
        if(openFileDb()) {  
            String sReq = "UPDATE Zones SET Visu = ?";                    
            try {
                PreparedStatement pstmt = dbConn.prepareStatement(sReq);
                pstmt.setInt(1,1); 
                pstmt.executeUpdate();  
                pstmt.close();
                return true;
            } catch (Exception e) {
                // Importé de configProg, il faudra corriger AUSSI !!!
                System.out.println("Db error : "+e.getMessage());    
                return false;
            }              
        } else {
            return false;
        }
    }
    
    
    private boolean openFileDb() {
        
        Connection con; 
        
        try {
            Class.forName("org.sqlite.JDBC");
            con = DriverManager.getConnection("jdbc:sqlite:"+fullDbName);
            Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT name FROM sqlite_master WHERE type='table' AND name='Zones';");    
            // We try several methods http://stackoverflow.com/questions/7886462/how-to-get-row-count-using-resultset-in-java
            // Problem : SQLite method gives Error like `TYPE_FORWARD_ONLY' 
            // finally brute force...       
            int count = 0;
            while (rs.next()) {
                ++count;
            }
            if (count > 0)  {  
                dbConn = con;
                return true;
            }
            else
                return false;
        } catch ( Exception e ) {
            System.out.println("Db error : "+e.getMessage());                         
            return false;    
        }               
    }
    
    private boolean createMemDb() {
        
        boolean res = false;
        Connection con;     
        
        try {
            Class.forName("org.sqlite.JDBC");
            con = DriverManager.getConnection("jdbc:sqlite::memory:");
            if (con != null) {
                StringBuilder req = new StringBuilder();
                req.append("CREATE TABLE Zones( Z_ID integer NOT NULL PRIMARY KEY, Openair Long Text, ");
                req.append("Geojson Long Text, Name Text, Classe TEXT, Floor INTEGER, Ceiling INTEGER, ");
                req.append("LatMini REAL, LatMaxi REAL, LongMini REAL, LongMaxi REAL, Visu VARCHAR(1))");
                Statement stmt = con.createStatement();
                stmt.execute(req.toString());
                dbConn = con;
                
                System.out.println("Création mem db OK");
                
                res = true;
                }               
            } catch ( Exception e ) {
                // Importé de configProg, il faudra corriger AUSSI !!!
                System.out.println("Db error : "+e.getMessage());                         
                res = false;         
            } 
        
        return res;
    }    
    
    
    private boolean createDb() {
        
        boolean res = false;
        Connection con;     
        
        try {
            Class.forName("org.sqlite.JDBC");
            con = DriverManager.getConnection("jdbc:sqlite:"+fullDbName);
            if (con != null) {
                StringBuilder req = new StringBuilder();
                req.append("CREATE TABLE Zones( Z_ID integer NOT NULL PRIMARY KEY, Openair Long Text, ");
                req.append("Geojson Long Text, Name Text, Classe TEXT, Floor INTEGER, Ceiling INTEGER, ");
                req.append("LatMini REAL, LatMaxi REAL, LongMini REAL, LongMaxi REAL, Visu VARCHAR(1))");
                Statement stmt = con.createStatement();
                stmt.execute(req.toString());
                dbConn = con;
                
                System.out.println("db "+fullDbName+" OK");
                
                res = true;
                }               
            } catch ( Exception e ) {
                // Importé de configProg, il faudra corriger AUSSI !!!
                System.out.println("Db error : "+e.getMessage());                         
                res = false;         
            } 
        
        return res;
    }
    
    private void closeDb() {
        try {
            dbConn.close();
        } catch (Exception e) {
            System.out.println("Db error : "+e.getMessage());                                     
        }
    }
    
}
