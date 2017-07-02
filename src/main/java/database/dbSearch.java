/*
 * Copyright Gil THOMAS
 * Ce fichier fait partie intégrante du projet Logfly
 * Pour tous les détails sur la licence du projet Logfly
 * Consulter le fichier LICENSE distribué avec le code source
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
 * Cette classe reprend toutes les procédures de recherches spécifiques dans la base de données
 * 
 */
public class dbSearch {
    
    // Localization
    private I18n i18n;

    // Paramètres de configuration
    configProg myConfig;
    
    public dbSearch(configProg pConfig)  {
        myConfig = pConfig;
        i18n = I18nFactory.getI18n(Logfly.Main.class.getClass(),myConfig.getLocale());
    }
    
    /**
     * Détermine si un vol existe déjà dans la table des vols
     * Pour un jour donné, cherche dans la table des vols s'il existe un vol
     * démarrant dans un rayon de 300 mètres et dans un délai de deux minutes
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
            alertbox aError = new alertbox();
            aError.alertError(e.getClass().getName() + ": " + e.getMessage());                         
        }
        
        return res;
    }    
    
    /**
     * Depuis xLogfly V4 et la mise à l'heure locale exacte on ne peut plus comparer des heures UTC annoncées 
     * sur la liste GPS (Flymaster) et les vols figurant dans le carnet. 
     * On va donc se fonder uniquement sur la durée du vol ... un peu olé olé !
     * @param strDate doit déjà être formaté sur un modèle YYYY-MM-DD
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
                          res = true;
                          break;
                        }
                    }
                }                
            }
        } catch (Exception e) {
            alertbox aError = new alertbox();
            aError.alertError(e.getClass().getName() + ": " + e.getMessage()); 
        }
        
        return res;        
    }
    /**
     * Appellation procédure vient de xLogfly où il y avait pls procédures rechSite.
     * la dernière employée s'appelait ainsi...
     * le paramètre pAlt n'est pas utilisé pour la recherche, il est nécessaire
     * s'il faut créer un nouveau site avec addBlankSite
     * @param pLat
     * @param pLong
     * @param exAtterro   Si vrai on exclut de la recherche les sites marqués "A" commme atterro     
     * @return 
     */
    public String rechSiteCorrect(double pLat,double pLong,boolean exAtterro) {
        String res=null;
        // Pour recherche de sites au plus près...
        String sLat = String.valueOf(pLat);
        String sLong = String.valueOf(pLong);
        double distMini = myConfig.getDistDeco();
                
        /*
        * A NOTER : Sous nos latitudes, en prenant la deuxième décimales cela donne un périmètre de recherche de 1,11km. 
        * Si on passe à la troisième décimale, on passe à 222 mètres ...      
        */
        double arrLat = Math.ceil(pLat*1000)/1000;
        double arrLong = Math.ceil(pLong*1000)/1000;
        double LatMin = arrLat - 0.01;
        double LatMax = arrLat + 0.01;
        double LongMin = arrLong - 0.01;
        double LongMax = arrLong + 0.01;
        
        // Dans les versions précedentes, on se limitait aux sites marqués décos dans le fichier Sites. 
        // Ce marquage peut être absent.. Donc on excluera simplement ce qui est marqué comme atterrissage 
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
