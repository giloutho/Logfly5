/* 
 * Copyright Gil THOMAS
 * This file forms an integral part of Logfly project
 * See the LICENSE file distributed with source code
 * for details of Logfly licence project
 */
package org.logfly.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.NumberFormat;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.logging.Level;
import javafx.scene.control.Alert;
import javax.management.StringValueExp;

import org.logfly.dialog.alertbox;
import org.logfly.geoutils.trigo;
import org.logfly.settings.configProg;
import org.logfly.systemio.mylogging;
import org.xnap.commons.i18n.I18n;
import org.xnap.commons.i18n.I18nFactory;

/**
 *
 * @author gil
 * 
 * Database search operations
 * 
 */
public class dbSearch {
    
    // Localization
    private I18n i18n;
    private String errSearch;

    // settings
    configProg myConfig;
    
    StringBuilder sbError;
    
    public dbSearch(configProg pConfig)  {
        myConfig = pConfig;
        i18n = I18nFactory.getI18n("","lang/Messages",dbSearch.class.getClass().getClassLoader(),myConfig.getLocale(),0);
    }

    public String getErrSearch() {
        return errSearch;
    }
    
    
    /**
     * Search if a flight exist in current database
     * For a specific day, search a flight started in 300m radius and a delay of two minutes      
     * @param pDate
     * @param pLatDeco
     * @param pLongDeco
     * @return 
     */
    public boolean searchVolByDeco(LocalDateTime pDate, double pLatDeco, double pLongDeco)  {
        double distMaxi = 300;
        long delaiMaxi = 120;
        boolean res = false;
        
        
        Statement stmt = null;
        ResultSet rs = null;
        
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("YYYY-MM-dd");        
        StringBuilder sReq = new StringBuilder();
        sReq.append("SELECT V_date,V_Duree,V_LatDeco,V_LongDeco FROM Vol WHERE V_Date >= '");
        sReq.append(pDate.format(dateFormatter)+" 00:00:00");
        sReq.append("' and V_Date <= '");
        sReq.append(pDate.format(dateFormatter)+" 23:59:59'");
        try {
            stmt = myConfig.getDbConn().createStatement();
            rs = stmt.executeQuery(sReq.toString());
            if (rs != null)  { 
                while (rs.next()) {
                    double carnetLat = rs.getDouble("V_LatDeco");
                    double carnetLong = rs.getDouble("V_LongDeco");
                    double distCarnet = Math.abs(trigo.CoordDistance(pLatDeco,pLongDeco,carnetLat,carnetLong));
                    // On commence par examiner si les décollages sont circonscrits dans un rayon de 500m
                    if (distCarnet < distMaxi)  {
                        // On calcule la différence entre les heures de décollages respectifs
                        Timestamp tsCarnet = Timestamp.valueOf(rs.getString("V_date"));
                        LocalDateTime ldtCarnet = tsCarnet.toLocalDateTime();
                        long diffInSeconds = Math.abs(Duration.between(ldtCarnet, pDate).getSeconds());
                        if (diffInSeconds < delaiMaxi)  {
                            res = true;
                            break;
                        }
                    }
                }
            } 

        } catch ( Exception e ) {
            sbError = new StringBuilder(this.getClass().getName()+"."+Thread.currentThread().getStackTrace()[1].getMethodName());
            sbError.append("\r\n").append(e.getMessage());          
            mylogging.log(Level.SEVERE, sbError.toString());                            
        } finally {
            try{
                rs.close(); 
                stmt.close();
            } catch(Exception e) { } 
        }
        
        return res;
    }    
    
    /**
     * From xLogfly V4 and new offset computation, it's not possible to compare takeoff hours
     * on GPS flight list, it's UTC time for Flymaster and local time in Flytec. Flights are stored in db in local time
     * Seach is now based on minutes of take off and flight duration... a bit far-fetched !
     * @param strDate must be formated : YYYY-MM-DD
     * @param totSec
     * @return 
     */
    public boolean Rech_Vol_by_Duree(String gpsDate, String gpsDepMin, String gpsDepSec, int gpsTotalSec) {
        boolean res = false;
        StringBuilder sReq = new StringBuilder();
        int iGpsDepSec = Integer.parseInt((gpsDepMin))*60+Integer.parseInt((gpsDepSec));
        boolean diffSecOK;
        int totalSec;
        
        Statement stmt = null;
        ResultSet rs = null;
                
        sReq.append("SELECT V_Date,V_Duree FROM Vol WHERE V_Date >= '").append(gpsDate);
        sReq.append(" 00:00:00' and V_Date <= '").append(gpsDate).append(" 23:59:59'");
        
        try {
            stmt = myConfig.getDbConn().createStatement();
            rs = stmt.executeQuery(sReq.toString());
            if (rs != null)  { 
                while (rs.next()) {
                    String sqlDate = rs.getString("V_Date");  // SQLDatetime ->"2011-09-03 08:13:32"
                    String noTiret = sqlDate.replaceAll("-",":");
                    String sDate = noTiret.replaceAll(" ",":");
                    String[] tbDate = sDate.split(":");
                    if (tbDate.length > 4) {
                        int dbSec = Integer.parseInt(tbDate[4])*60+Integer.parseInt(tbDate[5]);
                        int iDiffSec = dbSec - iGpsDepSec;
                        // We can't compare LocalDateTime : in db this is local time, in GPS, this is depending of user settings -> unreliable 
                        // We compute only with minute component. If hour change -> we compare 01:59 to 02:01
                        // In Flytec 6015 and 6030, GPS start time displayed and track start point are not the same values. (few minutes)
                        // We consider an offset of 5 mn (300 s)
                        if (iDiffSec > 300) {
                            iDiffSec = 3600 - iDiffSec;
                            if (iDiffSec < 360)
                                diffSecOK = true;
                            else
                                diffSecOK = false;
                        } else {
                            diffSecOK = true;
                        }
                        int dbDuree = rs.getInt("V_Duree");
                        // if it's a 6015 or a flytec we must correct the gps duration displayed and the logbook duration recorded!
                        // in a 6020 we find differences of 85 sec between real duration (first point - last point)
                        // and duration displayed in the flight list of the GPS
                        totalSec = gpsTotalSec - iDiffSec;
                        if (Math.abs(totalSec - dbDuree) < 120 && diffSecOK) {
                            errSearch = null;
                            res = true;
                            break;
                        }
                    }
                }                
            }
        } catch (Exception e) {
            sbError = new StringBuilder(this.getClass().getName()+"."+Thread.currentThread().getStackTrace()[1].getMethodName());
            sbError.append("\r\n").append(e.getMessage());
            sbError.append("\r\n").append("Problème de lecture sur le fichier des sites");
            mylogging.log(Level.SEVERE, sbError.toString());                           
        } finally {
            try{
                rs.close(); 
                stmt.close();
            } catch(Exception e) { } 
        }
        
        return res;        
    }
    
    /**
     * Method name come from xLogfly where there was several rechSite methods
     * this is the name of the last one
     * pAlt (altitude) is not required for searching operation but it is necessary
     * to create a new site with addBlankSite method
     * @param pLat
     * @param pLong
     * @param exAtterro   if true, sites with "A" like landing (Atterrissage in french)are excluded of searching operation   
     * @return 
     */
    public String rechSiteCorrect(double pLat,double pLong,boolean exAtterro) {
        String res=null;
        
        Statement stmt = null;
        ResultSet rs = null;
        
        // Pour recherche de sites au plus près...
        String sLat = String.valueOf(pLat);
        String sLong = String.valueOf(pLong);
        double distMini = myConfig.getDistDeco();
                
        /*
        * NOTE : under our latitudes, second decimal give a search perimeter of 1,11km. 
        * third decimal, perimeter is 222 meters ...      
        */
        double arrLat = Math.ceil(pLat*1000)/1000;
        double arrLong = Math.ceil(pLong*1000)/1000;
        double LatMin = arrLat - 0.01;
        double LatMax = arrLat + 0.01;
        double LongMin = arrLong - 0.01;
        double LongMax = arrLong + 0.01;
        
        // In old versions, search is limited to launching sites
        // But this information can be absent
        // Only landing sites are excluded
        StringBuilder sReq = new StringBuilder();
        sReq.append("SELECT S_ID,S_Nom,S_Latitude,S_Longitude,S_Alti,S_Localite,S_Pays FROM Site WHERE S_Latitude > ").append(String.valueOf(LatMin)).append(" AND S_Latitude < ").append(String.valueOf(LatMax));
        sReq.append(" AND S_Longitude > ").append(String.valueOf(LongMin)).append(" AND S_Longitude < ").append(String.valueOf(LongMax));
        if (exAtterro)  {
            sReq.append(" AND S_Type <> 'A'");
        }               
        try {
            stmt = myConfig.getDbConn().createStatement();
            rs = stmt.executeQuery(sReq.toString());
            if (rs != null)  { 
                while (rs.next()) {
                    double carnetLat = rs.getDouble("S_Latitude");
                    double carnetLong = rs.getDouble("S_Longitude");
                    double distSite = Math.abs(trigo.CoordDistance(pLat,pLong,carnetLat,carnetLong));
                    if (distSite < distMini)  {
                        distMini = distSite;
                        res = rs.getString("S_Nom")+"*"+rs.getString("S_Pays");  // Depuis la xLogfly 3, on colle le pays
                    }     
                }                
            }        
        } catch ( Exception e ) {
            sbError = new StringBuilder(this.getClass().getName()+"."+Thread.currentThread().getStackTrace()[1].getMethodName());
            sbError.append("\r\n").append(e.getMessage());
            sbError.append("\r\n").append("Problème de lecture sur le carnet de vol");
            mylogging.log(Level.SEVERE, sbError.toString());                                                         
        } finally {
            try{
                rs.close(); 
                stmt.close();
            } catch(Exception e) { } 
        }
        
        return res;
        
    }
    
    /**
     * Check if a site exists in database
     * Code directly translated from xLogfly
     * @param pLat
     * @param pLong
     * @return 
     */
    public String existeSite(double pLat,double pLong) {
        String res = null;
        StringBuilder sbRes = new StringBuilder();
        Statement stmt = null;
        ResultSet rs = null;
        double arrLat, arrLong;
        double latMin, latMax, longMin, longMax;
        StringBuilder sbReq = new StringBuilder();
        
        // On réduit à deux décimales, pour les explis voir l'aide à Ceil
        arrLat = Math.ceil(pLat*1000)/1000;
        arrLong = Math.ceil(pLong*1000)/1000;
        // First we take a greater value : 0.005 
        // we have a problem with landing zone just near take off
        // take off is inserted, it exists so landing is rejected
        latMin = arrLat - 0.001;     // 0,01 1,13 km 0,001  113m
        latMax = arrLat + 0.001;
        longMin = arrLong - 0.001;
        longMax = arrLong + 0.001;
        sbReq.append("SELECT S_ID,S_Nom,S_Latitude,S_Longitude,S_Localite FROM Site WHERE S_Latitude > ");
        sbReq.append(String.valueOf(latMin)).append(" AND S_Latitude < ").append(String.valueOf(latMax));
        sbReq.append(" AND S_Longitude > ").append(String.valueOf(longMin)).append(" AND S_Longitude < ").append(String.valueOf(longMax));
        try {
            stmt = myConfig.getDbConn().createStatement();
            rs = stmt.executeQuery(sbReq.toString());
            // if (rs != null)  is not a good solution
            // the best test -> if rs.next() returns false then there are no rows.
            if (rs.next()) {
                sbRes.append(" - !!! > ").append(rs.getString("S_Nom")).append(" ").append(rs.getString("S_Localite"));
                res = sbRes.toString();
            }            
        } catch ( Exception e ) {
            sbError = new StringBuilder(this.getClass().getName()+"."+Thread.currentThread().getStackTrace()[1].getMethodName());
            sbError.append("\r\n").append(e.getMessage());
            sbError.append("\r\n").append("Problème de lecture sur le carnet de vol");
            mylogging.log(Level.SEVERE, sbError.toString());                                                         
        } finally {
            try{
                rs.close(); 
                stmt.close();
            } catch(Exception e) { } 
        }            
        
        return res;
    }    
}
