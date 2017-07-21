/* 
 * Copyright Gil THOMAS
 * This file forms an integral part of Logfly project
 * See the LICENSE file distributed with source code
 * for details of Logfly licence project
 */
package database;

import java.sql.ResultSet;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import javafx.scene.control.Alert;
import org.xnap.commons.i18n.I18n;
import org.xnap.commons.i18n.I18nFactory;
import settings.configProg;
import trackgps.traceGPS;

/**
 *
 * @author gil
 * database addition operations
 */
public class dbAdd {
    
    // Localization
    private I18n i18n;

    // Settings
    configProg myConfig;
    
    public dbAdd(configProg pConfig)  {
        myConfig = pConfig;
        i18n = I18nFactory.getI18n("","lang/Messages",dbAdd.class.getClass().getClassLoader(),myConfig.getLocale(),0);
    }
    
    
    /**
     * Add a flight in the logbook
     * @param pTrace
     * @return 
     */
    public boolean addVolCarnet(traceGPS pTrace) {
        boolean res = false;
        String sReq;
        String sQuote ="'";
        String sQuoteVirg ="',";
        String siteNom = null;
        String sitePays = null;
        
        // last checking for flight existence in the logbook
        sReq = "SELECT * FROM Vol WHERE V_Date = '"+pTrace.getDate_Vol_SQL()+"'";
        try {
            ResultSet rs = myConfig.getDbConn().createStatement().executeQuery(sReq);           
            if (!rs.next())  { 
                dbSearch myRech = new dbSearch(myConfig);
                String siteDeco = myRech.rechSiteCorrect(pTrace.getLatDeco(),pTrace.getLongDeco(),true);   
                if (siteDeco == null)  {
                    // No lauching site found, a new site must be created with takeoff coordinates
                    siteDeco = addBlankSite(pTrace.getLatDeco(),pTrace.getLongDeco(),pTrace.getAlt_Deco_GPS());
                }
                if (siteDeco != null && !siteDeco.isEmpty())  {
                    // Warning java split founded on regular expressions
                    // * is part of 12 characters must be escaped
                    String[] siteComplet = siteDeco.split("\\*");
                    if (siteComplet.length > 0) {
                        siteNom = siteComplet[0];
                        sitePays = siteComplet[1];
                    }
                    StringBuilder sReqInsert = new StringBuilder();
                    sReqInsert.append("INSERT INTO Vol (V_Date,V_Duree,V_sDuree,V_LatDeco,V_LongDeco,V_AltDeco,V_Site,V_Pays,V_IGC,UTC,V_Engin) VALUES (");
                    sReqInsert.append(sQuote).append(pTrace.getDate_Vol_SQL()).append(sQuoteVirg);
                    sReqInsert.append(sQuote).append(String.valueOf(pTrace.getDuree_Vol())).append(sQuoteVirg);
                    sReqInsert.append(sQuote).append(pTrace.getsDuree_Vol()).append(sQuoteVirg);
                    sReqInsert.append(sQuote).append(String.valueOf(pTrace.getLatDeco())).append(sQuoteVirg);
                    sReqInsert.append(sQuote).append(String.valueOf(pTrace.getLongDeco())).append(sQuoteVirg);
                    sReqInsert.append(sQuote).append(String.valueOf(pTrace.getAlt_Deco_GPS())).append(sQuoteVirg);
                    sReqInsert.append(sQuote).append(siteNom).append(sQuoteVirg);
                    sReqInsert.append(sQuote).append(sitePays).append(sQuoteVirg);
                    sReqInsert.append(sQuote).append(pTrace.getFicIGC()).append(sQuoteVirg);
                    // GMTOffset usage is only for compatibility with xLogfly, it was in minutes
                    // Track offset is stored in a double
                    // Cast in long type is for eliminate decimals
                    sReqInsert.append(sQuote).append(String.format("%d",(long)pTrace.getUtcOffset()*60)).append(sQuoteVirg);
                    sReqInsert.append(sQuote).append(pTrace.getsVoile()).append(sQuote).append(")");   
                    try {
                        myConfig.getDbConn().createStatement().executeUpdate(sReqInsert.toString());                
                        res = true;
                    } catch ( Exception e ) {
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setTitle(i18n.tr("Problème d'insertion dans la base de données"));            
                        String s = e.getClass().getName() + ": " + e.getMessage();
                        alert.setContentText(s);
                        alert.showAndWait();                              
                    }    
                }
            }            
        } catch ( Exception e ) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(i18n.tr("Problème de lecture sur le fichier des vols"));            
            String s = e.getClass().getName() + ": " + e.getMessage();
            alert.setContentText(s);
            alert.showAndWait();                              
        }
        return res;
    }
    
    /**
     * Add a new site in db with a number and a mention "To rename"
     * This method didn't exist in xLogfly
     * It was embedded in a else in Rech_Site_Correct method
     * @return 
     */   
    public String addBlankSite(double pLat,double pLong,int pAlt)  {
        String res = null;
        StringBuilder sReq = new StringBuilder();
        LocalDateTime ldtNow = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String sQuote ="'";
        
        sReq.append("Select Count(S_ID) from Site where S_Nom LIKE 'Site No%'");
        try {
            ResultSet rs = myConfig.getDbConn().createStatement().executeQuery(sReq.toString());
            if (rs != null)  {
                // Mandatory ->  Initially, the cursor is positioned before the first row.
                rs.next();
                int totSiteNo = rs.getInt(1);                
                String sNom = "Site No "+String.valueOf(totSiteNo + 1)+"  ("+i18n.tr("A renommer")+")";
                // Big post about StringBuilder
                // http://stackoverflow.com/questions/8725739/correct-way-to-use-stringbuilder
                sReq.setLength(0);   // Stringbuilder is cleared            
                sReq.append("INSERT INTO Site (S_Nom,S_CP,S_Type,S_Alti,S_Latitude,S_Longitude,S_Maj) VALUES(");
                sReq.append(sQuote).append(sNom).append(sQuote).append(", ").append(sQuote).append("***").append(sQuote);
                sReq.append(", ").append(sQuote).append("D").append(sQuote).append(",").append(sQuote).append(String.valueOf(pAlt)).append(sQuote).append(",");
                sReq.append(sQuote).append(String.valueOf(pLat)).append(sQuote).append(",").append(sQuote).append(String.valueOf(pLong)).append(sQuote).append(",");
                sReq.append(sQuote).append(ldtNow.format(formatter)).append(sQuote).append(")");
                try {
                    myConfig.getDbConn().createStatement().executeUpdate(sReq.toString());                
                    res = sNom+"* ";  // Depuis la xLogfly 3, on colle le pays
                } catch ( Exception e ) {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle(i18n.tr("Problème d'insertion le fichier des sites"));            
                    String s = e.getClass().getName() + ": " + e.getMessage();
                    alert.setContentText(s);
                    alert.showAndWait();                              
                }                                  
            }            
        } catch ( Exception e ) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(i18n.tr("Problème de lecture sur le fichier des sites"));            
            String s = e.getClass().getName() + ": " + e.getMessage();
            alert.setContentText(s);
            alert.showAndWait();                              
        }      
        
        return res;
    }
}
