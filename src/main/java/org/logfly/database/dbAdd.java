/* 
 * Copyright Gil THOMAS
 * This file forms an integral part of Logfly project
 * See the LICENSE file distributed with source code
 * for details of Logfly licence project
 */
package org.logfly.database;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javafx.scene.control.Alert;

import org.logfly.settings.configProg;
import org.logfly.systemio.mylogging;
import org.logfly.trackgps.traceGPS;
import org.xnap.commons.i18n.I18n;
import org.xnap.commons.i18n.I18nFactory;

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
    
    public dbAdd(configProg pConfig, I18n pI18n)  {
        myConfig = pConfig;
        this.i18n = pI18n;
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
                    String sVoile = pTrace.getsVoile();
                    if (sVoile == null || sVoile.trim().equals("")) {
                        sVoile = myConfig.getDefaultVoile();
                    }
                    preparedStatement.setString(11,sVoile);
                    try {
			preparedStatement.executeUpdate();   
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
    
    public boolean importSite(String[] partImport)  {
        boolean res = false;
        StringBuilder sReq = new StringBuilder();
        LocalDateTime ldtNow = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String sQuote ="'";
        Statement stmt = null;
        ResultSet rs = null;
        PreparedStatement preparedStatement = null;
        String p_Nom = null;
        String p_Localite = null;
        String p_CP = null;
        String p_Pays = null;
        String p_Type = null;
        String p_Orientation = null;
        int p_Alt = 0;
        double p_Latitude = 0.00;
        double p_Longitude = 0.00;
        String p_Commentaire = null;
        if (partImport.length > 1) p_Nom = partImport[1].toUpperCase();
        if (partImport.length > 11) p_Localite = partImport[11].toUpperCase();
        if (partImport.length > 10) p_CP = partImport[10];
        if (partImport.length > 12) p_Pays = partImport[12].toUpperCase();        
        if (partImport.length > 5) {
            switch (partImport[5]) {
                case "Décollage":
                    p_Type = "D";
                    break;
                case "Atterrissage":
                    p_Type = "A";
                    break;    
            }            
        }
        if (partImport.length > 7) p_Orientation = partImport[7]; // dans le 74 on avait Coche Cabane : N,SO,OSO,O,ONO,NO,NNO soit 21 caractères !!!        
        if (partImport.length > 4) {
            // Je voulais l'altitude en pur numérique pour pouvoir éventuellement rechercher tous les décos supérieur à 1000
            // L'altitude peut être saisie avec ou sans espaces et avec la lettre m (1870m)
            String numberNoWhiteSpace = partImport[4].replaceAll("\\s","");
            Pattern patternI = Pattern.compile("([\\+-]?\\d+)([eE][\\+-]?\\d+)?");
            Matcher matcherI = patternI.matcher(numberNoWhiteSpace);
            if (matcherI.find()) {
                p_Alt = Integer.parseInt(matcherI.group(0));                
            } 
        }
        if (partImport.length > 2) p_Latitude = Double.parseDouble(partImport[2]); 
        if (partImport.length > 3) p_Longitude = Double.parseDouble(partImport[3]); 
        if (partImport.length > 15) p_Commentaire = partImport[16];        
            
        try {            
            StringBuilder insertTableSQL = new StringBuilder();
            insertTableSQL.append("INSERT INTO Site (S_Nom,S_Localite,S_CP,S_Pays,S_Type,S_Orientation,S_Alti,S_Latitude,S_Longitude,S_Commentaire,S_Maj) VALUES");
            insertTableSQL.append("(?,?,?,?,?,?,?,?,?,?,?)");
            preparedStatement = myConfig.getDbConn().prepareStatement(insertTableSQL.toString());
            preparedStatement.setString(1, p_Nom);
            preparedStatement.setString(2,p_Localite);            
            preparedStatement.setString(3,p_CP);
            preparedStatement.setString(4,p_Pays);
            preparedStatement.setString(5,p_Type);                    
            preparedStatement.setString(6,p_Orientation);   
            preparedStatement.setInt(7,p_Alt);;            
            preparedStatement.setDouble(8, p_Latitude);
            preparedStatement.setDouble(9,p_Longitude);
            preparedStatement.setString(10,p_Commentaire);
            preparedStatement.setString(11,ldtNow.format(formatter));
            preparedStatement.executeUpdate();                
            res = true;
        } catch ( Exception e ) {
            sbError = new StringBuilder(this.getClass().getName()+"."+Thread.currentThread().getStackTrace()[1].getMethodName());
            sbError.append("\r\n").append(e.getMessage());
            sbError.append("\r\n").append("requête : ").append(sReq);
            sbError.append("\r\n").append(partImport[1]).append(";").append(partImport[2]).append(";").append(partImport[3]);
            mylogging.log(Level.SEVERE, sbError.toString());                                   
        } finally {
            try {
                if (preparedStatement != null) {
                    preparedStatement.close();
                 }
            } catch(Exception e) { } 
        }                
        
        return res;        
    }
}
