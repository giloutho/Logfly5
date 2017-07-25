/* 
 * Copyright Gil THOMAS
 * This file forms an integral part of Logfly project
 * See the LICENSE file distributed with source code
 * for details of Logfly licence project
 */
package database;

import dialogues.alertbox;
import geoutils.trigo;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.text.NumberFormat;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import javafx.scene.control.Alert;
import org.xnap.commons.i18n.I18n;
import org.xnap.commons.i18n.I18nFactory;
import settings.configProg;

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
        
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("YYYY-MM-dd");        
        StringBuilder sReq = new StringBuilder();
        sReq.append("SELECT V_date,V_Duree,V_LatDeco,V_LongDeco FROM Vol WHERE V_Date >= '");
        sReq.append(pDate.format(dateFormatter)+" 00:00:00");
        sReq.append("' and V_Date <= '");
        sReq.append(pDate.format(dateFormatter)+" 23:59:59'");
        try {
            ResultSet rs = myConfig.getDbConn().createStatement().executeQuery(sReq.toString());
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
            alertbox aError = new alertbox(myConfig.getLocale());
            aError.alertError(e.getClass().getName() + ": " + e.getMessage());                         
        }
        
        return res;
    }    
    
    /**
     * From xLogfly V4 and new offset computation, it's not possible to compare takeoff hours
     * on GPS flight list (Flymaster) and flights stored in db
     * now based only on flight duration... not very safe !
     * @param strDate must be formated : YYYY-MM-DD
     * @param totSec
     * @return 
     */
    public boolean Rech_Vol_by_Duree(String strDate, String strDepMin, int totSec) {
        boolean res = false;
        StringBuilder sReq = new StringBuilder();
        int iDepMin = Integer.parseInt((strDepMin));
        boolean diffMin;
        
        sReq.append("SELECT V_Date,V_Duree FROM Vol WHERE V_Date >= '").append(strDate);
        sReq.append(" 00:00:00' and V_Date <= '").append(strDate).append(" 23:59:59'");
        
        try {
            ResultSet rs = myConfig.getDbConn().createStatement().executeQuery(sReq.toString());
            if (rs != null)  { 
                while (rs.next()) {
                    String sqlDate = rs.getString("V_Date");  // SQLDatetime ->"2011-09-03 08:13:32"
                    String noTiret = sqlDate.replaceAll("-",":");
                    String sDate = noTiret.replaceAll(" ",":");
                    String[] tbDate = sDate.split(":");
                    if (tbDate.length > 4) {
                        int iMin = Integer.parseInt(tbDate[4]);
                        int iDiffMin = Math.abs(iMin - iDepMin);
                        // Cas où on a un changement d'heure -> on compare 01:59 à 02:01
                        // A cause du 6015, où contrairement au 6020 les références de date et d'heure ne correspondent pas
                        // à la première ligne de l'IGC, on prend 5 mn de marge
                        if (iDiffMin > 5) {
                            iDiffMin = 60 - iDiffMin;
                            if (iDiffMin < 6)
                                diffMin = true;
                            else
                                diffMin = false;
                        } else {
                            diffMin = true;
                        }
                        int dbDuree = rs.getInt("V_Duree");        
                        if (Math.abs(totSec - dbDuree) < 60 && diffMin) {
                            errSearch = null;
                            res = true;
                            break;
                        }
                    }
                }                
            }
        } catch (Exception e) {
            errSearch = e.getClass().getName() + ": " + e.getMessage();          
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
            ResultSet rs = myConfig.getDbConn().createStatement().executeQuery(sReq.toString());
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
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(i18n.tr("Problème de lecture sur le carnet de vol"));            
            String s = e.getClass().getName() + ": " + e.getMessage();
            alert.setContentText(s);
            alert.showAndWait();                              
        }
        
        return res;
        
    }
}
