/* 
 * Copyright Gil THOMAS
 * This file forms an integral part of Logfly project
 * See the LICENSE file distributed with source code
 * for details of Logfly licence project
 */
package settings;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.*;
import java.util.logging.Level;
import littlewins.winLanguage;
import org.xnap.commons.i18n.I18n;
import org.xnap.commons.i18n.I18nFactory;
import systemio.mylogging;


public class configProg {
    
    private  String pathConfig;       // Settings path file
    private  boolean readConfig;      // Settings file read
    private  boolean validConfig;     // Configuration validated (db path)
    private  boolean configDefault;
    private  osType currOS;                
    private  String osSeparator;
                                            // App refers to xLogfly parameters
    private  String pathW;            // App.Wpath
    private  String dbName;           // App.db_Name  
    private  String pathDb;           // App.WpathDb
    private  String fullPathDb;       // pathDb + dbName
    private  String finderLat;        // App.FinderLat       
    private  String finderLong;       // App.FinderLong      
    private  String pathImport;       // WImport
    private  int  idxLang;            // Language index
    private  int idxTypeYear;         // Logbook presentation Year by Year or all years
    private  int idxSynthese;         // Overview presentation (Calendar or last twelve months)
    private  int idxStartwin;         // Start windows : logbook, overview...
    private  Locale locale;
    private  String defaultPilote;    // Default pilote name
    private  String defaultVoile;     // App.Def_Voile
    private  int decGMT;              // App.GMT_Plus    
    private  boolean gmtCEST;         // App.GMT_CEST  Heure été 
    private  int mainWidth;           // Main window width App.Main_Width 
    private  int mainHeight;          // Main window height App.Main_Height = 681                       
    private  String urlLogflyIGC;     // App.ServerURL
    private  String urlVisu;          // App.VisuURL
    private String urlFlyXC = "https://flyxc.app/?track=";
    private String  oldUrlVisu = "http://www.victorb.fr/visugps/visugps.html?track=";    // deprecated url
    private  String urlLogfly;        // App.urlSite  url du site de base de Logfly
    private  String urlIcones;        // App.urlIcones Recup url des icônes utilisées dans les cartes Google    
    private  String mailPass;         // App.MailPass     
    private  String lastNotif;        // App.LastNotif
    private  int idxLeague;           // App.NumLeague 
    private  int idxGPS;              // App.MonGPS    
    private  int seuilAberrants;      // App.Seuil_Aberrants 
    private  String pathOpenAir;      // App.WOpenAir
    private  String urlContest;       // App.Contest_URL =  "http://parapente.ffvl.fr/user/746/cfd/declaration"
    private  String pathContest;      // App.Contest_Path  Export path file for a contest
    private  int integration;         // App.Integration 
    private  boolean visuGPSinNav;    // App.VisuGPSNav  VisuGPS will be displayed in default browser
    private  boolean photoAuto;       // Automatic display of the flight photo
    private  String piloteMail;       // App.MailPilote
    private  String lastSerialCom;    // App.SerialCom  
    private  int idxMap;              // App.Default_Map map layer default index
    private  String piloteID;         // App.Id_Pilote  Contest pilot id 
    private  String pilotePass;       // App.Id_Pass    Contest pilot password 
    private  String lastTrace;        // App.LastTrace 
    private  String lastOpenAir;      // App.LastOpenAir    
    private  String pathSyride;    
    private  final int distDeco = 300;// distance for take off research
    private  boolean updateAuto;      // new in V5
    private  boolean debugMode;      // new in V5
    private  int gpsLimit;            // new in V5 date track search depth for usb GPS
    private  String version;
    private  Connection dbConn;
    private StringBuilder sbError;
    private I18n i18n;
    
    public void whichOS()  {
        String OS = System.getProperty("os.name").toLowerCase();
        if (OS.indexOf("win") >= 0) {
            currOS = osType.WINDOWS;      
            osSeparator = "\\";
        } else if (OS.indexOf("mac")>= 0) {
            currOS = osType.MACOS;
            osSeparator = "/";
        } else if (OS.indexOf("nix") >= 0 || OS.indexOf("nux") >= 0 || OS.indexOf("aix") > 0 ) {
            currOS = osType.LINUX;       
            osSeparator = "/";
        } else {
            currOS = osType.UNDEFINED;
            osSeparator = null;
        } 
        
    }        

    public boolean isValidConfig() {
        return validConfig;
    }

    public void setValidConfig(boolean validConfig) {
        this.validConfig = validConfig;
    }    
    
    public  String getPathConfig() {
        return pathConfig;
    }

    public  boolean isReadConfig() {
        return readConfig;
    }

    public  boolean isConfigDefault() {
        return configDefault;
    }    

    public  String getPathW() {
        return pathW;
    }

    public  void setPathW(String pathW) {
        this.pathW = pathW;
    }
    
    
    public  String getPathDb() {
        return pathDb;
    }

    public  void setPathDb(String PathDb) {
        this.pathDb = PathDb;
    }

    public  String getOsSeparator() {
        return osSeparator;
    }

    public  osType getOS() {
        return currOS;
    }
            
    public  String getFullPathDb() {
        return fullPathDb;
    }   
    
    public  void setFullPathDb(String fullPathDb) {
        this.fullPathDb = fullPathDb;
    }
    
    public  String getDbName() {
        return dbName;
    }

    public  void setDbName(String dbName) {
        this.dbName = dbName;
    }

    public  Connection getDbConn() {
        return dbConn;
    }

    public  void setDbConn(Connection dbConn) {
        // Procédure à utiliser UNIQUEMENT pour les tests
        this.dbConn = dbConn;
    }

    public  int getIdxSynthese() {
        return idxSynthese;
    }

    public  void setIdxSynthese(int idxSynthese) {
        this.idxSynthese = idxSynthese;
    }

    public int getIdxStartwin() {
        return idxStartwin;
    }

    public void setIdxStartwin(int idxStartwin) {
        this.idxStartwin = idxStartwin;
    }
        
    /**
     * Default pilot name
     * @return 
     */
    public  String getDefaultPilote() {
        return defaultPilote;
    }

    public  void setDefaultPilote(String defaultPilote) {
        this.defaultPilote = defaultPilote;
    }

    /**
     * Default glider name
     * @return 
     */
    public  String getDefaultVoile() {
        return defaultVoile;
    }

    public  void setDefaultVoile(String defaultVoile) {
        this.defaultVoile = defaultVoile;
    }

    public  int getIdxGPS() {
        return idxGPS;
    }

    public  void setIdxGPS(int idxGPS) {
        this.idxGPS = idxGPS;
    }

    public  int getIdxLeague() {
        return idxLeague;
    }

    public  void setIdxLeague(int idxLeague) {
        this.idxLeague = idxLeague;
    }

    public  boolean isVisuGPSinNav() {
        return visuGPSinNav;
    }

    public  void setVisuGPSinNav(boolean visuGPSinNav) {
        this.visuGPSinNav = visuGPSinNav;
    }

    public  int getIdxMap() {
        return idxMap;
    }

    public  void setIdxMap(int idxMap) {
        this.idxMap = idxMap;
    }

    public  int getIdxTypeYear() {
        return idxTypeYear;
    }

    public  void setIdxTypeYear(int idxTypeYear) {
        this.idxTypeYear = idxTypeYear;
    }
        
    public  String getFinderLat() {
        return finderLat;
    }

    public  void setFinderLat(String finderLat) {
        this.finderLat = finderLat;
    }

    public  String getFinderLong() {
        return finderLong;
    }

    public  void setFinderLong(String finderLong) {
        this.finderLong = finderLong;
    }

    /**
     * threshold outliers in GPS tracks
     * @return 
     */
    public  int getSeuilAberrants() {
        return seuilAberrants;
    }

    public  void setSeuilAberrants(int seuilAberrants) {
        this.seuilAberrants = seuilAberrants;
    }

    public  String getLastSerialCom() {
        return lastSerialCom;
    }

    public  void setLastSerialCom(String lastSerialCom) {
        this.lastSerialCom = lastSerialCom;
    }

    public  int getIdxLang() {
        return idxLang;
    }

    public  void setIdxLang(int idxLang) {
        this.idxLang = idxLang;
    }

    public  Locale getLocale() {
        return locale;
    }

    public I18n getI18n() {
        return i18n;
    }
        
    public  void setLocale(int idxLang) {
        switch (idxLang) {
            case 0 :
               // locale = new Locale("de");  
                locale = java.util.Locale.GERMAN;
                break;
            case 1 :
               // locale = new Locale("en");    
                locale = java.util.Locale.ENGLISH;
                break;
            case 2 :
                //locale = new Locale("fr"); 
                locale = java.util.Locale.FRENCH;
                break;
            case 3 :
                //locale = new Locale("it"); 
                locale = java.util.Locale.ITALIAN;
                break;                
            default:
                locale = new Locale("fr"); 
        }
        i18n = I18nFactory.getI18n("","lang/Messages",this.getClass().getClassLoader(),locale,org.xnap.commons.i18n.I18nFactory.FALLBACK);
    }

    public  int getGpsLimit() {
        return gpsLimit;
    }

    public  void setGpsLimit(int gpsLimit) {
        this.gpsLimit = gpsLimit;
    }
        
    
    public  String getPathImport() {
        return pathImport;
    }

    public  void setPathImport(String pathImport) {
        this.pathImport = pathImport;
    }

    public String getPathSyride() {
        return pathSyride;
    }

    public void setPathSyride(String pathSyride) {
        this.pathSyride = pathSyride;
    }    
    
    public  String getUrlLogflyIGC() {
        return urlLogflyIGC;
    }

    public  void setUrlLogflyIGC(String urlLogflyIGC) {
        this.urlLogflyIGC = urlLogflyIGC;
    }

    public  String getUrlVisu() {
        return urlVisu;
    }

    public  void setUrlVisu(String urlVisu) {
        this.urlVisu = urlVisu;
    }

    public  String getUrlLogfly() {
        return urlLogfly;
    }

    public  void setUrlLogfly(String urlLogfly) {
        this.urlLogfly = urlLogfly;
    }

    /**
     * Specific icons displayed on maps
     * @return 
     */
    public  String getUrlIcones() {
        return urlIcones;
    }

    public  void setUrlIcones(String urlIcones) {
        this.urlIcones = urlIcones;
    }

    public  String getUrlContest() {
        return urlContest;
    }

    public  String getMailPass() {
        return mailPass;
    }

    public  void setMailPass(String mailPass) {
        this.mailPass = mailPass;
    }
    
    public  void setUrlContest(String urlContest) {
        this.urlContest = urlContest;
    }

    public  String getPathContest() {
        return pathContest;
    }

    public  void setPathContest(String pathContest) {
        this.pathContest = pathContest;
    }

    public  int getMainWidth() {
        return mainWidth;
    }

    public  void setMainWidth(int mainWidth) {
        this.mainWidth = mainWidth;
    }

    public  int getMainHeight() {
        return mainHeight;
    }

    public  void setMainHeight(int mainHeight) {
        this.mainHeight = mainHeight;
    }
    
    public  int getIntegration() {
        return integration;
    }

    public  void setIntegration(int integration) {
        this.integration = integration;
    }

    public  String getPiloteMail() {
        return piloteMail;
    }

    public  void setPiloteMail(String piloteMail) {
        this.piloteMail = piloteMail;
    }

    /**
     * ID pilot for web contest
     * @return 
     */
    public  String getPiloteID() {
        return piloteID;
    }

    public  void setPiloteID(String piloteID) {
        this.piloteID = piloteID;
    }

    /**
     * Pilot pass for web contest
     * @return 
     */
    public  String getPilotePass() {
        return pilotePass;
    }

    public  void setPilotePass(String pilotePass) {
        this.pilotePass = pilotePass;
    }

    public  int getDistDeco() {
        return distDeco;
    }

    /**
     * Photo is 
     * @return 
     */
    public  boolean isPhotoAuto() {
        return photoAuto;
    }

    public  void setPhotoAuto(boolean photoAuto) {
        this.photoAuto = photoAuto;
    }
   
    public  boolean isUpdateAuto() {
        return updateAuto;
    }

    public  void setUpdateAuto(boolean UpdateAuto) {
        this.updateAuto = UpdateAuto;
    }

    public boolean isDebugMode() {
        return debugMode;
    }

    public void setDebugMode(boolean debugMode) {
        this.debugMode = debugMode;
    }    

    public  String getVersion() {
        return version;
    }

    public  void setVersion(String version) {
        this.version = version;
    }

    public  String getPathOpenAir() {
        return pathOpenAir;
    }

    public  void setPathOpenAir(String pathOpenAir) {
        this.pathOpenAir = pathOpenAir;
    }        
    
    private boolean dbCheckField(Connection con, String fieldName) {
        boolean res = false;       
        
        try {
            PreparedStatement pstmt = null;
            ResultSet rs = null;
            String sReq = "SELECT * FROM sqlite_master where sql like ?";   
            pstmt = con.prepareStatement(sReq);                      
            pstmt.setString(1, "%"+fieldName+"%"); 
            rs = pstmt.executeQuery();
            if (rs.next()) {  
                res = true;   
            } else {
                res = false;             
            }
        } catch ( Exception e ) {
                sbError = new StringBuilder(this.getClass().getName()+"."+Thread.currentThread().getStackTrace()[1].getMethodName());
                sbError.append("\r\n").append(e.toString());
                sbError.append("\r\n").append(fieldName).append(" ").append(" is not present");
                mylogging.log(Level.SEVERE, sbError.toString());                       
            return false;    
        }         
        
        return res;
    }
    
    private boolean dbAdd_V_Eng(Connection con)  {
        
        try {
            PreparedStatement ps = con.prepareStatement("ALTER TABLE Vol ADD V_Engin Varchar(10)");                                  
            ps.executeUpdate();
            ps.close();  
            return true;
        } catch ( Exception e ) {
            sbError = new StringBuilder(this.getClass().getName()+"."+Thread.currentThread().getStackTrace()[1].getMethodName());
            sbError.append("\r\n").append(e.toString());
            sbError.append("\r\n").append("Unable to alter table Vol with V_Engin");
            mylogging.log(Level.SEVERE, sbError.toString());                           
            return false;    
        }            
    }
    
    private boolean dbAdd_V_Scoring(Connection con)  {
        
        try {
            PreparedStatement ps1 = con.prepareStatement("ALTER TABLE Vol ADD V_League integer");                                  
            ps1.executeUpdate();
            ps1.close();  
            PreparedStatement ps2 = con.prepareStatement("ALTER TABLE Vol ADD V_Score Long Text");                                  
            ps2.executeUpdate();
            ps2.close();              
            return true;
        } catch ( Exception e ) {
            sbError = new StringBuilder(this.getClass().getName()+"."+Thread.currentThread().getStackTrace()[1].getMethodName());
            sbError.append("\r\n").append(e.toString());
            sbError.append("\r\n").append("Unable to alter table Vol with V_League and V_Score");
            mylogging.log(Level.SEVERE, sbError.toString());                             
            return false;    
        }            
    }    
                  
    /**
     * Check db connection
     * @param dbCheckName
     * @return 
     */      
    public boolean dbCheck(String dbCheckName) {
        Connection con;
        boolean res = false;
        
        try {
            Class.forName("org.sqlite.JDBC");
            con = DriverManager.getConnection("jdbc:sqlite:"+dbCheckName);
            Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT name FROM sqlite_master WHERE type='table' AND name='Vol';");    
            // We try several methods http://stackoverflow.com/questions/7886462/how-to-get-row-count-using-resultset-in-java
            // Problem : SQLite method gives Error like `TYPE_FORWARD_ONLY' 
            // finally brute force...       
            int count = 0;
            while (rs.next()) {
                ++count;
            }
            if (count > 0)  {  
                dbConn = con;
                // Version of database is checked
                boolean v_Eng_OK;
                if (!dbCheckField(con, "V_Eng")) {
                    if (!dbAdd_V_Eng(con))
                        v_Eng_OK = false;
                    else
                        v_Eng_OK = true;
                } else {
                    v_Eng_OK = true;
                }
                if (v_Eng_OK) {
                    if (dbCheckField(con, "V_League"))
                        res = true;
                    else {
                        if (dbAdd_V_Scoring(con))
                            res = true;
                        else
                            res = false;
                    } 
                } else {
                    res = false;
                }
            }
            else
                res = false;
        } catch ( Exception e ) {
            sbError = new StringBuilder(this.getClass().getName()+"."+Thread.currentThread().getStackTrace()[1].getMethodName());
            sbError.append("\r\n").append(e.toString());
            sbError.append("\r\n").append("Problem to connect and check database");
            mylogging.log(Level.SEVERE, sbError.toString());                         
            res = false;    
        }      
        
        return res;
    }
    
    /**
    * database change request
    * check before accept change 
    */
    public boolean dbSwitch(String dbNewName) {        
        boolean res;        
        String newFullPathDb = fullPathDb.replaceAll(dbName, dbNewName);
             
        res = dbCheck(newFullPathDb);
        if (res) {
            fullPathDb = newFullPathDb;
            dbName = dbNewName;
        }
        
        return res;
        
    }
    
    /**
    * new database creation request
    */
    
    public boolean dbNewOne(String dbNewName) {        
        boolean res;        
        String newFullPathDb = fullPathDb.replaceAll(dbName, dbNewName);
             
        res = dbCreation(newFullPathDb);
        if (res) {
            fullPathDb = newFullPathDb;
            dbName = dbNewName;
        }
        
        return res;
        
    }
    
    /**
     * new database creation
     * @param dbNewName
     * @return 
     */
    private boolean dbCreation(String dbNewName) {
        boolean res = false;
        Connection con;     
        
        // db existence checking
        File f = new File(dbNewName);
        if(f.exists() && f.isFile()) {            
            // if db is OK, dbConn is initialized
            res = dbCheck(dbNewName);
        }  else  {                         
            try {
                Class.forName("org.sqlite.JDBC");
                con = DriverManager.getConnection("jdbc:sqlite:"+dbNewName);
                if (con != null) {
                    StringBuilder req1 = new StringBuilder();
                    req1.append("CREATE TABLE Vol (V_ID integer NOT NULL PRIMARY KEY, V_Date TimeStamp, V_Duree integer, ");
                    req1.append("V_sDuree varchar(20), V_LatDeco double, V_LongDeco double, V_AltDeco integer, ");
                    req1.append("V_Site varchar(100), V_Pays varchar(50), V_Commentaire Long Text, V_IGC Long Text, V_Photos Long Text,UTC integer, V_CFD integer,V_Engin Varchar(10), ");
                    req1.append("V_League integer, V_Score Long Text)");
                    Statement stmt = con.createStatement();
                    stmt.execute(req1.toString());
                    // a debate about stringbuilder creation 
                    // http://stackoverflow.com/questions/242438/is-it-better-to-reuse-a-stringbuilder-in-a-loop
                    StringBuilder req2 = new StringBuilder();
                    req2.append("CREATE TABLE Site(S_ID integer NOT NULL primary key,S_Nom varchar(50),S_Localite varchar(50),");
                    req2.append("S_CP varchar(8),S_Pays varchar(50),S_Type varchar(1),S_Orientation varchar(20),S_Alti varchar(12),");
                    req2.append("S_Latitude double,S_Longitude double,S_Commentaire Long Text,S_Maj varchar(10))");
                    stmt.execute(req2.toString());
                    // all is OK
                    dbConn = con;
                    res = true;
                }               
            } catch ( Exception e ) {
                System.out.println("Db error : "+e.getMessage());                         
                res = false;         
            }
        }
        
        return res;        
    }
     
    /**
     * Read settings of Logfly V4 and lower
     * @param prfFile
     * @return 
     */
    private boolean readOldPrf(File prfFile) {
        boolean res = false;
        int numLigne = 0;
        String cleanLine;
        String parsePath[];

        try {          
          BufferedReader input =  new BufferedReader(new FileReader(prfFile));
          try {
            String line = null;                       
            while (( line = input.readLine()) != null){
                numLigne++;
                switch (numLigne) {
                    case 1 :
                        // For Mac, pah was stored like Macintosh HD:Users:gil:Documents:Logfly:
                        // we want /Users/gil/Dropbox/Logfly_data
                        if (line.indexOf("Users:") >= 0)  {
                            cleanLine = line.replaceAll(":", "/");
                            parsePath = cleanLine.split("Users/");
                             if (parsePath.length > 0)
                                 pathW = "/Users/"+parsePath[1];
                        } else {
                            pathW = line;
                        }                      
                        break;
                    case 2 :
                        dbName = line;   // App.db_Name
                        break;
                    case 3 :
                        finderLat = line;    // App.FinderLat           
                        break;
                    case 4 :
                        finderLong = line;    // App.FinderLong      
                        break;
                    case 5 :
                        //  import file if exists  [pathImport]
                        // For Mac, pah was stored like Macintosh HD:Users:gil:Documents:Logfly:
                        // we want /Users/gil/Dropbox/Logfly_data
                        if (line.indexOf("Users:") >= 0)  {
                            cleanLine = line.replaceAll(":", "/");
                            parsePath = cleanLine.split("Users/");
                             if (parsePath.length > 0)
                                 pathImport = "/Users/"+parsePath[1];
                        } else {                                
                            pathImport = line;   
                        }
                        break;
                    case 6 :
                        idxLang = Integer.parseInt(line);   // Language index  
                        if (idxLang < 0 || idxLang > 3) idxLang = 1;
                        setLocale(idxLang);
                        break;
                    case 7 :
                        defaultPilote = line;    // App.Def_Pilote
                        break;
                    case 8 :
                        defaultVoile = line;    // App.Def_Voile
                        break;
                    case 9 :
                        decGMT = Integer.parseInt(line);    // App.GMT_Plus    int
                        break;
                    case 10 :
                        int decalage = Integer.parseInt(line);    // App.GMT_CEST 1 ou 0
                        gmtCEST = (decalage != 0);
                        break;
                    // case 11 :  App.Rev_Exclu                         
                    case 12 :
                        // Main window size App.Main_Width et App.Main_Height = 681
                        break;
                    // case 13 : App.Sky_Exclu  Deprecated since xLogfly V3 
                    case 14 :
                        // App.WpathDb
                        // For Mac, pah was stored like Macintosh HD:Users:gil:Documents:Logfly:
                        // we want /Users/gil/Dropbox/Logfly_data
                        if (line.indexOf("Users:") >= 0)  {
                            cleanLine = line.replaceAll(":", "/");
                            parsePath = cleanLine.split("Users/");
                             if (parsePath.length > 0)
                                 pathDb = "/Users/"+parsePath[1];
                        } else {
                            pathDb = line;   
                        }
                        break;
                    case 15 :
                        urlLogflyIGC = line;    // App.ServerURL
                        break;
                    case 16 :
                        // urlVisu = line;    // App.VisuURL deprecated, Logfly 4 used VisuGPS
                        urlVisu = urlFlyXC;
                        break;
                    case 17 :
                        urlLogfly = line;    // App.urlSite  logfly base url 
                        break;
                    case 18 :
                        urlIcones = line;   // App.urlIcones url of map icons
                        break;
                    case 19 :
                        mailPass = line;    // App.MailPass     
                        break;
                    case 20 :
                        lastNotif = line;    // App.LastNotif
                        break;
                    case 21 :
                        idxLeague = Integer.parseInt(line);    //  App.NumLeague 
                        break;
                    case 22 :
                        idxGPS =   Integer.parseInt(line);   // App.MonGPS    int
                        break;
                    case 23 :
                        seuilAberrants = Integer.parseInt(line);    // App.Seuil_Aberrants 
                        break;
                    case 24 :
                        // App.WOpenAir
                        // For Mac, pah was stored like Macintosh HD:Users:gil:Documents:Logfly:
                        // we want /Users/gil/Dropbox/Logfly_data
                        if (line.indexOf("Users:") >= 0)  {
                            cleanLine = line.replaceAll(":", "/");
                            parsePath = cleanLine.split("Users/");
                             if (parsePath.length > 0)
                                 pathOpenAir = "/Users/"+parsePath[1];
                        } else {         
                            pathOpenAir = line;   
                        }
                        break;
                    case 25 :
                        urlContest = line;    // App.Contest_URL =  "http://parapente.ffvl.fr/user/746/cfd/declaration"
                        break;
                    case 26 :
                        // App.Contest_Path
                        // For Mac, pah was stored like Macintosh HD:Users:gil:Documents:Logfly:
                        // we want /Users/gil/Dropbox/Logfly_data
                        if (line.indexOf("Users:") >= 0)  {
                            cleanLine = line.replaceAll(":", "/");
                            parsePath = cleanLine.split("Users/");
                             if (parsePath.length > 0)
                                 pathContest = "/Users/"+parsePath[1];
                        } else {        
                            pathContest = line;   
                        }
                        break;
                    case 27 :
                        integration = Integer.parseInt(line);    // App.Integration int
                        break;
                    case 28 :
                        int inNav = Integer.parseInt(line);     // App.VisuGPSNav  VisuGPS in external browser
                        visuGPSinNav = (inNav != 0);                        
                        break;
                    case 29 :
                        piloteMail = line;    // App.MailPilote
                        break;
                    case 30 :
                        lastSerialCom = line;   // App.SerialCom   String
                        break;
                    case 31 :
                        idxMap =  Integer.parseInt(line);    // App.Default_Map Default map layer index
                        break;
                    case 32 :
                        piloteID = line;    // App.Id_Pilote     
                        break;
                    case 33 :
                        pilotePass = line;   // App.Id_Pass
                        break;
                    case 34 :
                        // App.LastTrace 
                        // For Mac, pah was stored like Macintosh HD:Users:gil:Documents:Logfly:
                        // we want /Users/gil/Dropbox/Logfly_data
                        if (line.indexOf("Users:") >= 0)  {
                            cleanLine = line.replaceAll(":", "/");
                            parsePath = cleanLine.split("Users/");
                             if (parsePath.length > 0)
                                 lastTrace = "/Users/"+parsePath[1];
                        } else {         
                            lastTrace = line;    
                        }
                        break;
                    case 35 :
                        // App.LastOpenAir 
                        // For Mac, pah was stored like Macintosh HD:Users:gil:Documents:Logfly:
                        // we want /Users/gil/Dropbox/Logfly_data
                        if (line.indexOf("Users:") >= 0)  {
                            cleanLine = line.replaceAll(":", "/");
                            parsePath = cleanLine.split("Users/");
                            if (parsePath.length > 0)
                                lastOpenAir = "/Users/"+parsePath[1];
                        } else {
                            lastOpenAir = line;   
                        }                        
                        break;
                }               
            }
            // is recorded database operational ? 
            String oldDbPath;
            switch (currOS) {
                case WINDOWS :
                    oldDbPath = pathDb+dbName; 
                    break;
                case MACOS :
                    oldDbPath = pathDb+dbName;                     
                    break;
                default :
                    oldDbPath = "";
            }   
            if (dbCheck(oldDbPath)) {
                // OK dbConn is initialized
                fullPathDb = oldDbPath;
                res = true;
            } else {
                res = false;
            }             
            
          }
          finally {
            input.close();            
          }
        }
        catch (IOException ex){
          ex.printStackTrace();
          res = false;
        }
        
        return res;
        
    }  
    
    /**
     * Is there a settings file of xLogfly
     * @return 
     */
    public boolean setOldConfig()  {        
        boolean res = false;        
        
        switch (currOS) {
            case WINDOWS :
                File fWin = new File(System.getProperty("user.home")+"\\AppData\\Roaming\\logfly.prf");
                if (fWin.exists() && fWin.isFile()) {
                    res = readOldPrf(fWin);
                }  
            case MACOS :
                File fMac = new File(System.getProperty("user.home")+"/Library/Preferences/logfly.prf");
                if(fMac.exists() && fMac.isFile()) {
                res = readOldPrf(fMac);
            }   
                                                    
        }
                        
        return res;        
    }
     
    /**
     * ************* Default settings *************  
     */    
    public void setDefault() {
        
        String targetPath;
        String importPath;
        boolean folderOK;
        
        if (!setOldConfig())  {
            switch (currOS) {
                case WINDOWS :
                    targetPath = System.getProperty("user.home")+"\\Documents\\Logfly";  
                    fullPathDb = System.getProperty("user.home")+"\\Documents\\Logfly\\Logfly.db"; 
                    importPath = System.getProperty("user.home")+"\\Documents\\Logfly\\Import";
                    pathSyride = System.getProperty("user.home")+File.separatorChar + "Documents"+"\\Syride"; 
                    break;
                case MACOS :
                    targetPath = System.getProperty("user.home")+"/Documents/Logfly";   
                    fullPathDb = System.getProperty("user.home")+"/Documents/Logfly/Logfly.db";  
                    importPath = System.getProperty("user.home")+"/Documents/Logfly/Import";
                    pathSyride = System.getProperty("user.home")+"/syride";
                    break;
                case LINUX :
                    targetPath = System.getProperty("user.home")+"/.logfly";
                    fullPathDb = System.getProperty("user.home")+"/.logfly/Logfly.db";  
                    importPath = System.getProperty("user.home")+"/.logfly/Import";
                    pathSyride = System.getProperty("user.home")+"/syride";
                    break;
                default: 
                    targetPath = null;
                    importPath = null;
            }
            if (targetPath != null )  {
                File f = new File(targetPath);
                if(f.exists() && f.isDirectory()) {                    
                    folderOK = true;
                } else {
                    File dir = new File(targetPath);
                    folderOK = dir.mkdirs();       
                }                  
            } else {
                folderOK = false;
            }
            if (folderOK)  {
                // language choice
                winLanguage showLang = new winLanguage();
                idxLang = showLang.getIdxLang();
                pathW = targetPath;
                dbName = "Logfly.db";
                pathDb = targetPath;  
                // fullPathDb is defined previously
                finderLat = "45.863";   //  Lake of Annecy
                finderLong = "6.1725";   
                pathImport = importPath;                
                setLocale(idxLang);
                defaultPilote = "";
                defaultVoile = "";
                decGMT = 0;
                gmtCEST = false;
                mainWidth = 1102;  
                mainHeight = 650;          
                urlLogflyIGC = "http://www.logfly.org/Visu/";     
                urlVisu = urlFlyXC;       
                urlLogfly = "http://www.logfly.org";       
                urlIcones = "http://www.logfly.org/download/gmap/"; 
                mailPass = "";
                lastNotif = "";    
                idxLeague = 0;  // Fr
                idxGPS = 0;       
                seuilAberrants = 30; 
                pathOpenAir = "";
                urlContest = ""; 
                pathContest = "";
                integration = 15;  
                visuGPSinNav = false;
                photoAuto = true;
                piloteMail = "";       
                lastSerialCom = "";    
                idxMap = 1;                    
                piloteID = "";         
                pilotePass = "";
                lastTrace = ""; 
                lastOpenAir = "";
                updateAuto = false;
                debugMode = false;
                photoAuto = true;
                gpsLimit = 6;
                configDefault = dbCreation(fullPathDb);              
            } else {
                configDefault = false;
            }    
        } else {
            // old settings of xLogfly read and validated
            configDefault = true; 
            // default values for new parameters of V5 
            mainWidth = 1102;  
            mainHeight = 650;  
            updateAuto = true;
            photoAuto = true;
            debugMode = false;
            gpsLimit = 6;
            // PathSyride must be defined
            // It causes a boot bug wrongly attributed to the operating system environment
            switch (currOS) {
                case WINDOWS :
                    pathSyride = System.getProperty("user.home")+File.separatorChar + "Documents"+"\\Syride"; 
                    break;
                case MACOS :
                    pathSyride = System.getProperty("user.home")+"/syride";
                    break;
                case LINUX :
                    pathSyride = System.getProperty("user.home")+"/syride";
                    break;            
            }
        }
    }
    
    /*
    * Settings file checking
    *   - Windows :  - > \Users\UserName\AppData\Roaming\
    *   - Mac :  -> /Users/UserName/Library/Preferences
    *   - Linux : /.logfly 
    */
    private boolean existConfFile()  {
        boolean res = false;
        String searchPath;
        
        switch (currOS) {
            case WINDOWS :
                // It's ok from Windows 7, before it was C:\Documents and Settings\<username>\Application Data
                searchPath = System.getProperty("user.home")+"\\AppData\\Roaming\\logfly.properties"; 
                break;
            case MACOS :
                searchPath = System.getProperty("user.home")+"/Library/Preferences/logfly.properties";
            break;
            case LINUX :
                searchPath = System.getProperty("user.home")+"/.logfly/logfly.properties";
                break;
            default: 
                searchPath = null;
        }
                
        if (searchPath != null)  { 
            // From http://stackoverflow.com/questions/1816673/how-do-i-check-if-a-file-exists-in-java
            File f = new File(searchPath);
            if(f.exists() && f.isFile()) {
               // Is this file empty ?  We had a case where user properties file became empty             
               // initially we put 800 but in Linux the default file lenght is 746 !
               if (f.length() > 500) {
                    pathConfig = searchPath;
                    res = true;
               } else {
                  pathConfig = null; 
               }
            }  else  {
                pathConfig = null;
            }                                        
        }
        
        return res;
    }
    
    /**
     * Read settings in a properties file
     */
    private void readProperties()  {
        
        Properties prop = new Properties();
	FileInputStream input = null;

	try {
            input = new FileInputStream(pathConfig);       

            prop.load(input);           
            getAllProperties(prop);

	} catch (IOException ex) {
		//ex.printStackTrace();
                readConfig = false;
	} finally {
		if (input != null) {
                    try {
                        input.close();
                        readConfig = true;
                    } catch (IOException e) {
                        readConfig = false;
                    }
		}
	}        
        if (readConfig) {            
            if (Objects.equals(null, fullPathDb)) {
                validConfig = false;
            }  else {
                File f = new File(fullPathDb);
                if(f.exists() && f.isFile()) {
                    // Si la db est OK dbConn est initialisé
                    validConfig = dbCheck(fullPathDb);
                } else {
                    validConfig = false;
                }
            }
        }  else  {            
            setDefault();            
        }
        
    }
       
    /**
     * Set properties 
     * @param prop 
     */
    private  void setAllProperties(Properties prop)  {                
        prop.setProperty("pathw",pathW);
        prop.setProperty("dbname",dbName);
        prop.setProperty("pathdb",pathDb);
        prop.setProperty("fullpathdb",fullPathDb);
        prop.setProperty("finderlat",finderLat);
        prop.setProperty("finderlong",finderLong);
        prop.setProperty("pathimport",pathImport);
        prop.setProperty("idxlang",String.valueOf(idxLang));
        prop.setProperty("defaultpilote",defaultPilote);
        prop.setProperty("defaultvoile",defaultVoile);
        prop.setProperty("decgmt",String.valueOf(decGMT));
        prop.setProperty("gmtcest",String.valueOf(gmtCEST));
        prop.setProperty("width", String.valueOf(mainWidth)); 
        prop.setProperty("height",String.valueOf(mainHeight));           
        prop.setProperty("urllogflyigc",urlLogflyIGC);
        prop.setProperty("urlvisu",urlVisu);
        prop.setProperty("urllogfly",urlLogfly);
        prop.setProperty("urlicones",urlIcones);
        prop.setProperty("mailpass",mailPass);
        prop.setProperty("lastnotif",lastNotif);
        prop.setProperty("idxleague",String.valueOf(idxLeague));
        prop.setProperty("idxgps",String.valueOf(idxGPS));
        prop.setProperty("seuilaberrants",String.valueOf(seuilAberrants));
        prop.setProperty("pathopenair",pathOpenAir);
        prop.setProperty("urlcontest",urlContest);
        prop.setProperty("pathcontest",pathContest);
        prop.setProperty("integration",String.valueOf(integration));
        prop.setProperty("visugpsnav",String.valueOf(visuGPSinNav));
        prop.setProperty("photoauto",String.valueOf(photoAuto));
        prop.setProperty("pilotemail",piloteMail);
        prop.setProperty("latsserialcom",lastSerialCom);
        prop.setProperty("idxmap",String.valueOf(idxMap));
        prop.setProperty("piloteid",piloteID);
        prop.setProperty("pilotepass",pilotePass);
        prop.setProperty("lasttrace",lastTrace);
        prop.setProperty("lastopenair",lastOpenAir);
        prop.setProperty("updateauto",String.valueOf(updateAuto));
        prop.setProperty("debugmode",String.valueOf(debugMode));        
        prop.setProperty("gpslimit",String.valueOf(gpsLimit));
        prop.setProperty("idxtypeyear",String.valueOf(idxTypeYear));
        prop.setProperty("idxsynthese",String.valueOf(idxSynthese));
        prop.setProperty("idxstartwin",String.valueOf(idxStartwin));
        prop.setProperty("pathsyride",pathSyride);
    }
    
    /**
     * Get properties
     * @param prop 
     */
    private  void getAllProperties(Properties prop)  {                
              
        pathW = prop.getProperty("pathw");
        dbName = prop.getProperty("dbname");
        pathDb = prop.getProperty("pathdb");
        fullPathDb = prop.getProperty("fullpathdb");
        finderLat = prop.getProperty("finderlat");
        finderLong = prop.getProperty("finderlong");
        pathImport = prop.getProperty("pathimport");
        if (prop.getProperty("idxlang") != null)
            idxLang = Integer.parseInt(prop.getProperty("idxlang"));
        else
            idxLang = 2;
        setLocale(idxLang);
        defaultPilote = prop.getProperty("defaultpilote");
        defaultVoile = prop.getProperty("defaultvoile");
        if (prop.getProperty("decgmt") != null)
            decGMT = Integer.parseInt(prop.getProperty("decgmt"));
        else
            decGMT = 0;
        if (prop.getProperty("gmtcest") != null)
            gmtCEST = Boolean.parseBoolean(prop.getProperty("gmtcest"));
        else
            gmtCEST = false;
        if (prop.getProperty("width") != null)
            mainWidth = Integer.parseInt(prop.getProperty("width"));
        else
            mainWidth = 1102;
        if (prop.getProperty("height") != null)
            mainHeight = Integer.parseInt(prop.getProperty("height"));
        else
            mainHeight = 650;           
        urlLogflyIGC = prop.getProperty("urllogflyigc");
        urlVisu = prop.getProperty("urlvisu");
        // VisuGPS is now deprecated
        if (urlVisu.contains("visugps")) urlVisu = urlFlyXC;
            
        urlLogfly = prop.getProperty("urllogfly");
        urlIcones = prop.getProperty("urlicones");
        mailPass = prop.getProperty("mailpass");
        lastNotif = prop.getProperty("lastnotif");
        if (prop.getProperty("idxleague") != null)
            idxLeague = Integer.parseInt(prop.getProperty("idxleague"));
        else
            idxLeague = 0;
        if (prop.getProperty("idxgps") != null)
            idxGPS = Integer.parseInt(prop.getProperty("idxgps"));
        else
            idxGPS = -1;
        if (prop.getProperty("seuilaberrants") != null)
            seuilAberrants = Integer.parseInt(prop.getProperty("seuilaberrants"));
        else
            seuilAberrants = 30;
        pathOpenAir = prop.getProperty("pathopenair");
        urlContest = prop.getProperty("urlcontest");
        pathContest = prop.getProperty("pathcontest");
        if (prop.getProperty("integration") != null)
            integration = Integer.parseInt(prop.getProperty("integration"));
        else
            integration = 15;
        if (prop.getProperty("visugpsnav") != null)
            visuGPSinNav = Boolean.parseBoolean(prop.getProperty("visugpsnav"));
        else
            visuGPSinNav = false;
        if (prop.getProperty("photoauto") != null)
            photoAuto = Boolean.parseBoolean(prop.getProperty("photoauto"));
        else
            photoAuto = true;
        piloteMail = prop.getProperty("pilotemail");
        lastSerialCom = prop.getProperty("latsserialcom");
        if (prop.getProperty("idxmap") != null)
            idxMap = Integer.parseInt(prop.getProperty("idxmap"));
        else
            idxMap = 1;
        piloteID = prop.getProperty("piloteid");
        pilotePass = prop.getProperty("pilotepass");
        lastTrace = prop.getProperty("lasttrace");
        lastOpenAir = prop.getProperty("lastopenair");        
        if (prop.getProperty("updateauto") != null)
            updateAuto = Boolean.parseBoolean(prop.getProperty("updateauto"));
        else
            updateAuto = false;
        if (prop.getProperty("debugmode") != null)
            debugMode = Boolean.parseBoolean(prop.getProperty("debugmode"));
        else
            debugMode = false;        
        if (prop.getProperty("gpslimit") != null)
            gpsLimit = Integer.parseInt(prop.getProperty("gpslimit"));
        else
            gpsLimit = 6;
        if (prop.getProperty("idxtypeyear") != null)
            idxTypeYear = Integer.parseInt(prop.getProperty("idxtypeyear"));
        else
            idxTypeYear = 0;    
        if (prop.getProperty("idxsynthese") != null)
            idxSynthese = Integer.parseInt(prop.getProperty("idxsynthese"));
        else
            idxSynthese = 0;      
        if (prop.getProperty("idxstartwin") != null)
            idxStartwin = Integer.parseInt(prop.getProperty("idxstartwin"));
        else
            idxStartwin = 0;         
        if (prop.getProperty("pathsyride") != null)
            pathSyride = prop.getProperty("pathsyride");
        else {
            switch (currOS) {                
                case WINDOWS:
                    // https://stackoverflow.com/questions/9677692/getting-my-documents-path-in-java 
                    // System.getProperty("user.home")+File.separatorChar + "Documents"  
                    pathSyride = System.getProperty("user.home")+File.separatorChar + "Documents"+"\\Syride"; 
                    break;
                case MACOS :
                    pathSyride = System.getProperty("user.home")+"/syride";                
                    break;
                case LINUX :
                    pathSyride = System.getProperty("user.home")+"/syride";
                    break;
                }            
        }             
        
    }
    
    /**
     * Write settins in a properties file
     * @return 
     */
    public boolean writeProperties()  {
        
        Properties prop = new Properties();
	FileOutputStream output = null;
        boolean res = false;

	try {
            switch (currOS) {
                case WINDOWS :
                    // Valid path from Windows 7, before it was C:\Documents and Settings\<username>\Application Data
                    output = new FileOutputStream(System.getProperty("user.home")+"\\AppData\\Roaming\\logfly.properties"); 
                    break;
                case MACOS :
                    output = new FileOutputStream(System.getProperty("user.home")+"/Library/Preferences/logfly.properties");
                break;
                case LINUX :
                    output = new FileOutputStream(System.getProperty("user.home")+"/.logfly/logfly.properties");
                    break;                
            }		
            setAllProperties(prop);		
            prop.store(output, null);
	} catch (IOException io) {
            //io.printStackTrace();
            res = false;
	} finally {
            if (output != null) {
                try {
                    output.close();
                    res = true;
                } catch (IOException e) {
                    //e.printStackTrace();
                    res = false;
                }
            }
	}
        
        return res;
    }
       
    /**
     * manage settings reading
     */
    public void readSettings()  {
        whichOS();
        if (existConfFile())  {
            readProperties();
        }  else  {
            readConfig = false;
            setDefault();
            if (configDefault) {
                validConfig = writeProperties();
            }            
        }
    }
    
    public String getDecoToolTip() {
        
        // on line tool for gradient : https://www.w3schools.com/colors/colors_gradient.asp
        String decoToolTip = "-fx-background-color: linear-gradient(#ffff00, #ff9900); -fx-text-fill: black;";
        
        return decoToolTip;
    }    
}
