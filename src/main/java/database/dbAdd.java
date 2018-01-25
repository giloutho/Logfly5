/* 
 * Copyright Gil THOMAS
 * This file forms an integral part of Logfly project
 * See the LICENSE file distributed with source code
 * for details of Logfly licence project
 */
package database;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.logging.Level;
import javafx.scene.control.Alert;
import org.xnap.commons.i18n.I18n;
import org.xnap.commons.i18n.I18nFactory;
import settings.configProg;
import systemio.mylogging;
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
    private configProg myConfig;
    
    private StringBuilder sbError;
    
    public dbAdd(configProg pConfig)  {
        myConfig = pConfig;
        i18n = I18nFactory.getI18n("","lang/Messages",dbAdd.class.getClass().getClassLoader(),myConfig.getLocale(),0);
    }
    
    
    /**
     * Add a flight in the logbook
     * @param pTrace
     * @return 
     */
    public int addVolCarnet(traceGPS pTrace) {
        int res = -1;
        String sReq;
        String sQuote ="'";
        String sQuoteVirg ="',";
        String siteNom = null;
        String sitePays = null;
        Statement stmt = null;
        ResultSet rs = null;
        
        // last checking for flight existence in the logbook
        sReq = "SELECT * FROM Vol WHERE V_Date = '"+pTrace.getDate_Vol_SQL()+"'";
        try {
            stmt = myConfig.getDbConn().createStatement();
            rs = stmt.executeQuery(sReq);           
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
                        if (siteComplet.length > 1) {
                            sitePays = siteComplet[1];
                        } else {
                           sitePays = "..."; 
                        }
                    }
                    
                    StringBuilder insertTableSQL = new StringBuilder();
                    insertTableSQL.append("INSERT INTO Vol (V_Date,V_Duree,V_sDuree,V_LatDeco,V_LongDeco,V_AltDeco,V_Site,V_Pays,V_IGC,UTC,V_Engin) VALUES");
                    insertTableSQL.append("(?,?,?,?,?,?,?,?,?,?,?)");
                    PreparedStatement preparedStatement = myConfig.getDbConn().prepareStatement(insertTableSQL.toString());
                    preparedStatement.setString(1, pTrace.getDate_Vol_SQL());
                    preparedStatement.setLong(2, pTrace.getDuree_Vol());
                    preparedStatement.setString(3,pTrace.getsDuree_Vol());
                    preparedStatement.setDouble(4,pTrace.getLatDeco());
                    preparedStatement.setDouble(5,pTrace.getLongDeco());
                    preparedStatement.setInt(6,pTrace.getAlt_Deco_GPS());
                    preparedStatement.setString(7,siteNom);
                    preparedStatement.setString(8,sitePays);
                    preparedStatement.setString(9,pTrace.getFicIGC());
                    // GMTOffset usage is only for compatibility with xLogfly, it was in minutes
                    // Track offset is stored in a double
                    // Cast in long type is for eliminate decimals
                    preparedStatement.setString(10,String.format("%d",(long)pTrace.getUtcOffset()*60));
                    preparedStatement.setString(11,pTrace.getsVoile());
                    try {
			preparedStatement.executeUpdate();  
                        System.out.println("Insertion OK");  
                        res = 0;
                    } catch ( Exception e ) {
                        res = 1104;   // Insertion error in flights file                                           
                        sbError = new StringBuilder(this.getClass().getName()+"."+Thread.currentThread().getStackTrace()[1].getMethodName());
                        sbError.append("\r\n").append(e.getMessage());
                        sbError.append("\r\n").append("Date vol : ").append(pTrace.getDate_Vol_SQL());
                        mylogging.log(Level.SEVERE, sbError.toString());
                    } finally {
                        try{                            
                            if (preparedStatement != null) {
				preparedStatement.close();
                            }
                        } catch(Exception e) { } 
                    }    
                }
            }            
        } catch ( Exception e ) {
            res = 1102;    // I/O access error in flights file                     
        } finally {
            try{
                rs.close(); 
                stmt.close();
            } catch(Exception e) { } 
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
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String sQuote ="'";
        Statement stmt = null;
        ResultSet rs = null;
        
        sReq.append("Select Count(S_ID) from Site where S_Nom LIKE 'Site No%'");
        try {
            stmt = myConfig.getDbConn().createStatement();
            rs = stmt.executeQuery(sReq.toString());
            if (rs != null)  {
                // Mandatory ->  Initially, the cursor is positioned before the first row.
                rs.next();
                int totSiteNo = rs.getInt(1);  
                rs.close();
                String sNom = "Site No "+String.valueOf(totSiteNo + 1)+"  ("+i18n.tr("A renommer")+")";
                StringBuilder insertTableSQL = new StringBuilder();
                insertTableSQL.append("INSERT INTO Site (S_Nom,S_CP,S_Type,S_Alti,S_Latitude,S_Longitude,S_Maj) VALUES");
                insertTableSQL.append("(?,?,?,?,?,?,?)");
                PreparedStatement preparedStatement = myConfig.getDbConn().prepareStatement(insertTableSQL.toString());
                preparedStatement.setString(1, sNom);
                preparedStatement.setString(2,"***");
                preparedStatement.setString(3,"D");
                preparedStatement.setInt(4,pAlt);
                preparedStatement.setDouble(5, pLat);
                preparedStatement.setDouble(6,pLong);
                preparedStatement.setString(7,ldtNow.format(formatter));
                try {
                    preparedStatement.executeUpdate();                
                    res = sNom+"*...";  // Depuis la xLogfly 3, on colle le pays
                } catch ( Exception e ) {
                    sbError = new StringBuilder(this.getClass().getName()+"."+Thread.currentThread().getStackTrace()[1].getMethodName());
                    sbError.append("\r\n").append(e.getMessage());
                    sbError.append("\r\n").append("requête : ").append(sReq);
                    mylogging.log(Level.SEVERE, sbError.toString());                                   
                } finally {
                    try {
                        if (preparedStatement != null) {
                            preparedStatement.close();
                         }
                    } catch(Exception e) { } 
                }                                 
            }            
        } catch ( Exception e ) {
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
}
