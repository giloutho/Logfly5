/*
 * Copyright Gil THOMAS
 * Ce fichier fait partie intégrante du projet Logfly
 * Pour tous les détails sur la licence du projet Logfly
 * Consulter le fichier LICENSE distribué avec le code source
 */
package settings;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.*;


public class configProg {
    
    private static String pathConfig;       // Chemin du fichier de configuration
    private static boolean readConfig;      // Le fichier de configuration a été lu
    private static boolean validConfig;     // La config a été validée (chemin db)
    private static boolean configDefault;
    private static int currOS;              // Type OS    : 1/Windows  2/Mac  3/Linux    
    private static String osSeparator;
                                            // App fait référence aux paramètres utilisés dans xLogfly
    private static String pathW;            // App.Wpath
    private static String dbName;           // App.db_Name  
    private static String pathDb;           // App.WpathDb
    private static String fullPathDb;       // pathDb + dbName
    private static String finderLat;        // App.FinderLat       
    private static String finderLong;       // App.FinderLong      
    private static String pathImport;       // WImport
    private static int  idxLang;            // Lang
    private static String winLangue;        // Nom de la ressource correspondant à la langue utilisée
    private static Locale locale;
    private static String defaultPilote;    // Def_Pilote
    private static String defaultVoile;     // App.Def_Voile
    private static int decGMT;              // App.GMT_Plus    
    private static boolean gmtCEST;         // App.GMT_CEST  Heure été 
    private static int mainWidth;           // Largeur fenêtre principale App.Main_Width 
    private static int mainHeight;          // Hauteur fenêtre principale App.Main_Height = 681                       
    private static String urlLogflyIGC;     // App.ServerURL
    private static String urlVisu;          // App.VisuURL
    private static String urlLogfly;        // App.urlSite  url du site de base de Logfly
    private static String urlIcones;        // App.urlIcones Recup url des icônes utilisées dans les cartes Google
    private static String mailPass;         // App.MailPass     
    private static String lastNotif;        // App.LastNotif
    private static int idxLeague;           // App.NumLeague 
    private static int idxGPS;              // App.MonGPS    
    private static int seuilAberrants;      // App.Seuil_Aberrants 
    private static String pathOpenAir;      // App.WOpenAir
    private static String urlContest;       // App.Contest_URL =  "http://parapente.ffvl.fr/user/746/cfd/declaration"
    private static String pathContest;      // App.Contest_Path  Chemin dossier d'export de trace pour un contest
    private static int integration;         // App.Integration 
    private static boolean visuGPSinNav;    // App.VisuGPSNav  VisuGPS affiché dans le navigateur par défaut
    private static boolean photoAuto;       // Apparition automatique de la photo
    private static String piloteMail;       // App.MailPilote
    private static String lastSerialCom;    // App.SerialCom  
    private static int idxMap;              // App.Default_Map Indice carte par défaut
    private static String piloteID;         // App.Id_Pilote     Id Pilote contest
    private static String pilotePass;       // App.Id_Pass
    private static String lastTrace;        // App.LastTrace 
    private static String lastOpenAir;      // App.LastOpenAir    
    private static final int distDeco = 300;// distance de référence pour trouver un décollage dans le fichier des sites
    
    private static Connection dbConn;
    
    public void whichOS()  {
        String OS = System.getProperty("os.name").toLowerCase();
        if (OS.indexOf("win") >= 0) {
            currOS = 1;      
            osSeparator = "\\";
        } else if (OS.indexOf("mac")>= 0) {
            currOS = 2;
            osSeparator = "/";
        } else if (OS.indexOf("nix") >= 0 || OS.indexOf("nux") >= 0 || OS.indexOf("aix") > 0 ) {
            currOS = 3;       
            osSeparator = "/";
        } else {
            currOS = 0;
            osSeparator = null;
        } 
        
    }        

    public static boolean isValidConfig() {
        return validConfig;
    }
    
    public static String getPathConfig() {
        return pathConfig;
    }

    public static boolean isReadConfig() {
        return readConfig;
    }

    public static boolean isConfigDefault() {
        return configDefault;
    }    

    public static String getPathW() {
        return pathW;
    }

    public static void setPathW(String pathW) {
        configProg.pathW = pathW;
    }
    
    
    public static String getPathDb() {
        return pathDb;
    }

    public static void setPathDb(String PathDb) {
        configProg.pathDb = PathDb;
    }

    public static String getOsSeparator() {
        return osSeparator;
    }
    

    public static String getFullPathDb() {
        return fullPathDb;
    }   
    
    public static void setFullPathDb(String fullPathDb) {
        configProg.fullPathDb = fullPathDb;
    }
    
    public static String getDbName() {
        return dbName;
    }

    public static void setDbName(String dbName) {
        configProg.dbName = dbName;
    }

    public static Connection getDbConn() {
        return dbConn;
    }

    public static void setDbConn(Connection dbConn) {
        // Procédure à utiliser UNIQUEMENT pour les tests
        configProg.dbConn = dbConn;
    }
    
    
    
    public static String getDefaultPilote() {
        return defaultPilote;
    }

    public static void setDefaultPilote(String defaultPilote) {
        configProg.defaultPilote = defaultPilote;
    }

    public static String getDefaultVoile() {
        return defaultVoile;
    }

    public static void setDefaultVoile(String defaultVoile) {
        configProg.defaultVoile = defaultVoile;
    }

    public static int getIdxGPS() {
        return idxGPS;
    }

    public static void setIdxGPS(int idxGPS) {
        configProg.idxGPS = idxGPS;
    }

    public static int getIdxLeague() {
        return idxLeague;
    }

    public static void setIdxLeague(int idxLeague) {
        configProg.idxLeague = idxLeague;
    }

    public static boolean isVisuGPSinNav() {
        return visuGPSinNav;
    }

    public static void setVisuGPSinNav(boolean visuGPSinNav) {
        configProg.visuGPSinNav = visuGPSinNav;
    }

    public static int getIdxMap() {
        return idxMap;
    }

    public static void setIdxMap(int idxMap) {
        configProg.idxMap = idxMap;
    }

    public static String getFinderLat() {
        return finderLat;
    }

    public static void setFinderLat(String finderLat) {
        configProg.finderLat = finderLat;
    }

    public static String getFinderLong() {
        return finderLong;
    }

    public static void setFinderLong(String finderLong) {
        configProg.finderLong = finderLong;
    }

    public static int getSeuilAberrants() {
        return seuilAberrants;
    }

    public static void setSeuilAberrants(int seuilAberrants) {
        configProg.seuilAberrants = seuilAberrants;
    }

    public static String getLastSerialCom() {
        return lastSerialCom;
    }

    public static void setLastSerialCom(String lastSerialCom) {
        configProg.lastSerialCom = lastSerialCom;
    }

    public static int getIdxLang() {
        return idxLang;
    }

    public static void setIdxLang(int idxLang) {
        configProg.idxLang = idxLang;
    }

    public static String getWinLangue() {
        return winLangue;
    }

    public static void setWinLangue(int idxLang) {
        switch (idxLang) {
            case 0 :
                winLangue = "i18n/win_DE";                
                break;
            case 1 :
                winLangue = "i18n/win_EN";                
                break;
            case 2 :
                winLangue = "i18n/win_FR";                
                break;
            default:
                winLangue = "i18n/win_FR";
        }
    }    

    public static Locale getLocale() {
        return locale;
    }

    public static void setLocale(int idxLang) {
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
            default:
                locale = new Locale("fr"); 
        }
       
    }
    

    public static String getPathImport() {
        return pathImport;
    }

    public static void setPathImport(String pathImport) {
        configProg.pathImport = pathImport;
    }

    public static String getUrlLogflyIGC() {
        return urlLogflyIGC;
    }

    public static void setUrlLogflyIGC(String urlLogflyIGC) {
        configProg.urlLogflyIGC = urlLogflyIGC;
    }

    public static String getUrlVisu() {
        return urlVisu;
    }

    public static void setUrlVisu(String urlVisu) {
        configProg.urlVisu = urlVisu;
    }

    public static String getUrlLogfly() {
        return urlLogfly;
    }

    public static void setUrlLogfly(String urlLogfly) {
        configProg.urlLogfly = urlLogfly;
    }

    public static String getUrlIcones() {
        return urlIcones;
    }

    public static void setUrlIcones(String urlIcones) {
        configProg.urlIcones = urlIcones;
    }

    public static String getUrlContest() {
        return urlContest;
    }

    public static String getMailPass() {
        return mailPass;
    }

    public static void setMailPass(String mailPass) {
        configProg.mailPass = mailPass;
    }
    
    public static void setUrlContest(String urlContest) {
        configProg.urlContest = urlContest;
    }

    public static String getPathContest() {
        return pathContest;
    }

    public static void setPathContest(String pathContest) {
        configProg.pathContest = pathContest;
    }
    
    
    
    public static int getIntegration() {
        return integration;
    }

    public static void setIntegration(int integration) {
        configProg.integration = integration;
    }

    public static String getPiloteMail() {
        return piloteMail;
    }

    public static void setPiloteMail(String piloteMail) {
        configProg.piloteMail = piloteMail;
    }

    public static String getPiloteID() {
        return piloteID;
    }

    public static void setPiloteID(String piloteID) {
        configProg.piloteID = piloteID;
    }

    public static String getPilotePass() {
        return pilotePass;
    }

    public static void setPilotePass(String pilotePass) {
        configProg.pilotePass = pilotePass;
    }

    public static int getDistDeco() {
        return distDeco;
    }

    public static boolean isPhotoAuto() {
        return photoAuto;
    }

    public static void setPhotoAuto(boolean photoAuto) {
        configProg.photoAuto = photoAuto;
    }
        
    
    
    
    public boolean dbVerif(String dbCheckName) {
        Connection con;
        
        try {
            Class.forName("org.sqlite.JDBC");
            con = DriverManager.getConnection("jdbc:sqlite:"+dbCheckName);
            Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT name FROM sqlite_master WHERE type='table' AND name='Vol';");    
            // Essai de différentes méthodes Cf http://stackoverflow.com/questions/7886462/how-to-get-row-count-using-resultset-in-java
            // Problème : SQLite method gives Error like `TYPE_FORWARD_ONLY' 
            // Donc finalement on bourrine...             
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
    
    /* On change de db en cours de fonctionnement
    *  donc tous les paramètres ont été initialisés
    *  il faut juste vérifier que la db est opérationnelle avant de changer le paramètre
    */
    public boolean dbSwitch(String dbNewName) {        
        boolean res;        
        String newFullPathDb = fullPathDb.replaceAll(dbName, dbNewName);
             
        res = dbVerif(newFullPathDb);
        if (res) {
            fullPathDb = newFullPathDb;
            dbName = dbNewName;
        }
        
        return res;
        
    }
    
    /* On créé une nouvelle db en cours de fonctionnement
    *  donc tous les paramètres ont été initialisés
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
    
    private boolean dbCreation(String dbNewName) {
        boolean res = false;
        Connection con;     
        
        // On vérifie que la db n'est pas déjà existante
        File f = new File(dbNewName);
        if(f.exists() && f.isFile()) {            
            // On vérifie que la db est OK
            // Si la db est OK dbConn est initialisé
            res = dbVerif(dbNewName);
        }  else  {                         
            try {
                Class.forName("org.sqlite.JDBC");
                con = DriverManager.getConnection("jdbc:sqlite:"+dbNewName);
                if (con != null) {
                    StringBuilder req1 = new StringBuilder();
                    req1.append("CREATE TABLE Vol (V_ID integer NOT NULL PRIMARY KEY, V_Date TimeStamp, V_Duree integer, ");
                    req1.append("V_sDuree varchar(20), V_LatDeco double, V_LongDeco double, V_AltDeco integer, ");
                    req1.append("V_Site varchar(100), V_Pays varchar(50), V_Commentaire Long Text, V_IGC Long Text, V_Photos Long Text,UTC integer, V_CFD integer,V_Engin Varchar(10))");
                    Statement stmt = con.createStatement();
                    stmt.execute(req1.toString());
                    // Discussions à l'infini sur l'avantage de réutiliser ou créer un nouveau StringBuilder
                    // http://stackoverflow.com/questions/242438/is-it-better-to-reuse-a-stringbuilder-in-a-loop
                    StringBuilder req2 = new StringBuilder();
                    req2.append("CREATE TABLE Site(S_ID integer NOT NULL primary key,S_Nom varchar(50),S_Localite varchar(50),");
                    req2.append("S_CP varchar(8),S_Pays varchar(50),S_Type varchar(1),S_Orientation varchar(20),S_Alti varchar(12),");
                    req2.append("S_Latitude double,S_Longitude double,S_Commentaire Long Text,S_Maj varchar(10))");
                    stmt.execute(req2.toString());
                    // Tout est OK, on positionne dbConn
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
    
    private boolean litOldPrf(File prfFile) {
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
                        // Pour Mac, le path a été stocké ainsi : Macintosh HD:Users:gil:Documents:Logfly:
                        // On veut /Users/gil/Dropbox/Logfly_data
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
                        //  dossier d'import s'il existe  [pathImport]
                        // Pour Mac, le path a été stocké ainsi : Macintosh HD:Users:gil:Documents:Logfly:
                        // On veut /Users/gil/Dropbox/Logfly_data
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
                        idxLang = Integer.parseInt(line);   // Numéro de la langue en cours    
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
                        // Taille fenêtre principale App.Main_Width et App.Main_Height = 681
                        break;
                    // case 13 : App.Sky_Exclu  Deprec depuis la xLogfly V3 
                    case 14 :
                        // App.WpathDb
                        // Pour Mac, le path a été stocké ainsi : Macintosh HD:Users:gil:Dropbox:Logfly_data:
                        // On veut /Users/gil/Dropbox/Logfly_data/
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
                        urlVisu = line;    // App.VisuURL
                        break;
                    case 17 :
                        urlLogfly = line;    // App.urlSite  url du site de base de Logfly
                        break;
                    case 18 :
                        urlIcones = line;   // App.urlIcones Recup url des icônes utilisées dans les cartes Google
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
                        // Pour Mac, le path a été stocké ainsi : Macintosh HD:Users:gil:Documents:Logfly:
                        // On veut /Users/gil/Dropbox/Logfly_data
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
                        // Pour Mac, le path a été stocké ainsi : Macintosh HD:Users:gil:Documents:Logfly:
                        // On veut /Users/gil/Dropbox/Logfly_data
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
                        int inNav = Integer.parseInt(line);     // App.VisuGPSNav  VisuGPS dans le navigateur Boolean
                        visuGPSinNav = (inNav != 0);                        
                        break;
                    case 29 :
                        piloteMail = line;    // App.MailPilote
                        break;
                    case 30 :
                        lastSerialCom = line;   // App.SerialCom   String
                        break;
                    case 31 :
                        idxMap =  Integer.parseInt(line);    // App.Default_Map Indice carte par défaut
                        break;
                    case 32 :
                        piloteID = line;    // App.Id_Pilote     Id Pilote FFVL
                        break;
                    case 33 :
                        pilotePass = line;   // App.Id_Pass
                        break;
                    case 34 :
                        // App.LastTrace 
                        // Pour Mac, le path a été stocké ainsi : Macintosh HD:Users:gil:Documents:Logfly:
                        // On veut /Users/gil/Dropbox/Logfly_data
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
                        // Pour Mac, le path a été stocké ainsi : Macintosh HD:Users:gil:Documents:Logfly:
                        // On veut /Users/gil/Dropbox/Logfly_data
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
            // la db enregistrée est elle opérationnelle ? 
            String oldDbPath;
            switch (currOS) {
                case 1 :
                    oldDbPath = pathDb+dbName; 
                    break;
                case 2 :
                    oldDbPath = pathDb+dbName;                     
                    break;
                default :
                    oldDbPath = "";
            }   
            if (dbVerif(oldDbPath)) {
                // Si la db est OK dbConn est initialisé
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
    
    // Existe il un fichier de configuration concernant xLogfly ?
    public boolean setOldConfig()  {        
        boolean res = false;        
        
        switch (currOS) {
            case 1 :
                File fWin = new File(System.getProperty("user.home")+"\\AppData\\Roaming\\logfly.prf");
                if (fWin.exists() && fWin.isFile()) {
                    res = litOldPrf(fWin);
                }  
            case 2 :
                File fMac = new File(System.getProperty("user.home")+"/Library/Preferences/logfly.prf");
                if(fMac.exists() && fMac.isFile()) {
                res = litOldPrf(fMac);
            }   
                                                    
        }
                        
        return res;        
    }
       
    //************* Configuration par défaut *************  
    public void setDefault() {
        
        String targetPath;
        String importPath;
        boolean folderOK;
        
        if (!setOldConfig())  {
            switch (currOS) {
                case 1 :
                    targetPath = System.getProperty("user.home")+"\\Documents\\Logfly";  
                    fullPathDb = System.getProperty("user.home")+"\\Documents\\Logfly\\Logfly.db"; 
                    importPath = System.getProperty("user.home")+"\\Documents\\Logfly\\Import";
                    break;
                case 2 :
                    targetPath = System.getProperty("user.home")+"/Documents/Logfly";   
                    fullPathDb = System.getProperty("user.home")+"/Documents/Logfly/Logfly.db";  
                    importPath = System.getProperty("user.home")+"/Documents/Logfly/Import";
                    break;
                case 3 :
                    targetPath = System.getProperty("user.home")+"/Documents/Logfly";
                    fullPathDb = System.getProperty("user.home")+"/Documents/Logfly/Logfly.db";  
                    importPath = System.getProperty("user.home")+"/Documents/Logfly/Import";
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
                pathW = targetPath;
                dbName = "Logfly.db";
                pathDb = targetPath;  
                // fullPathDb a été défini plus haut
                finderLat = "45.863";   //  lac d'Annecy
                finderLong = "6.1725";   
                pathImport = importPath;
                idxLang = 2;    // français par défaut
                setWinLangue(idxLang);
                setLocale(idxLang);
                defaultPilote = "";
                defaultVoile = "";
                decGMT = 0;
                gmtCEST = false;
                mainWidth = 900;  // Anciennes valeurs à préciser
                mainHeight = 681;   // Anciennes valeurs à préciser            
                urlLogflyIGC = "http://www.logfly.org/Visu/";     
                urlVisu = "http://www.victorb.fr/visugps/visugps.html?track=";          
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
                configDefault = dbCreation(fullPathDb);            
            } else {
                configDefault = false;
            }    
        } else {
            // la config xLogfly a été lue et validée
            configDefault = true; 
        }
    }
    
    /*
    *  Vérification de l'existence du fichier de paramètrage
    *   - Windows :  - > \Users\UserName\AppData\Roaming\
    *   - Mac :  -> /Users/UserName/Library/Preferences
    *   - Linux : Home Folder 
    */
    private boolean existConfFile()  {
        boolean res = false;
        String searchPath;
        
        switch (currOS) {
            case 1 :
                // Ce chemin est valide à partir de Windows 7, avant c'était C:\Documents and Settings\<username>\Application Data
                searchPath = System.getProperty("user.home")+"\\AppData\\Roaming\\logfly.properties"; 
                break;
            case 2 :
                searchPath = System.getProperty("user.home")+"/Library/Preferences/logfly.properties";
            break;
            case 3 :
                searchPath = System.getProperty("user.home")+"/logfly.properties";
                break;
            default: 
                searchPath = null;
        }
                
        if (searchPath != null)  { 
            // From http://stackoverflow.com/questions/1816673/how-do-i-check-if-a-file-exists-in-java
            File f = new File(searchPath);
            if(f.exists() && f.isFile()) {
               pathConfig = searchPath;
               res = true;
            }  else  {
                pathConfig = null;
            }                                        
        }
        
        return res;
    }
    
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
                    validConfig = dbVerif(fullPathDb);
                } else {
                    validConfig = false;
                }
            }
        }  else  {            
            setDefault();            
        }
        
    }
       
    private static void setAllProperties(Properties prop)  {                
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
        //prop.setProperty("mainWidth = 900;  mainHeight = 681;   // Anciennes valeurs à préciser            
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
    }
    
    private static void getAllProperties(Properties prop)  {                
              
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
        setWinLangue(idxLang);
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
        //prop.setProperty("mainWidth = 900;  mainHeight = 681;   // Anciennes valeurs à préciser            
        urlLogflyIGC = prop.getProperty("urllogflyigc");
        urlVisu = prop.getProperty("urlvisu");
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
    }
    
    
    public boolean writeProperties()  {
        
        Properties prop = new Properties();
	FileOutputStream output = null;
        boolean res = false;

	try {
            switch (currOS) {
                case 1 :
                    // Ce chemin est valide à partir de Windows 7, avant c'était C:\Documents and Settings\<username>\Application Data
                    output = new FileOutputStream(System.getProperty("user.home")+"\\AppData\\Roaming\\logfly.properties"); 
                    break;
                case 2 :
                    output = new FileOutputStream(System.getProperty("user.home")+"/Library/Preferences/logfly.properties");
                break;
                case 3 :
                    output = new FileOutputStream(System.getProperty("user.home")+"/logfly.properties");
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
    
}
