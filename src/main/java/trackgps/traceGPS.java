/* 
 * Copyright Gil THOMAS
 * This file forms an integral part of Logfly project
 * See the LICENSE file distributed with source code
 * for details of Logfly licence project
 */
package trackgps;

/**
 *
 * @author Gil Thomas logfly.org
 * Decoding track IGC format or GPX format
 */

import gapchenko.llttz.Converter;
import gapchenko.llttz.IConverter;
import gapchenko.llttz.stores.TimeZoneListStore;
import static geoutils.convigc.Lat_Dd_IGC;
import static geoutils.convigc.Long_Dd_IGC;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.Duration;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import igc.pointIGC;
import geoutils.trigo;
import io.jenetics.jpx.GPX;
import io.jenetics.jpx.Track;
import io.jenetics.jpx.TrackSegment;
import io.jenetics.jpx.WayPoint;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.time.ZoneOffset;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.stream.Collectors;
import settings.configProg;
import systemio.mylogging;
import systemio.textio;

public class traceGPS {
    // variables names from xLogfly
    private int APP_INTEGRATION;    
    private String pathFichier;
    private String FicIGC;
    private String FicGPX;
    private String Origine;
    private String sSite;
    private String sVoile;  
    private String sPilote;
    private String Signature;
    private String sDate_Vol;
    private String Date_Vol_SQL;
    private LocalDateTime Date_Vol;
    private double utcOffset;          
    private boolean dstOffset;       // Daylight saving time (DST) heure été
    private TimeZone tzVol;
    private String sFirmware;
    private double TrackLen;
    private Boolean Decodage;
    private int numErrDecodage;
    private Boolean avecPoints;
    private LocalDateTime DT_Deco;   
    private LocalDateTime DT_Attero;
    private double LatDeco;
    private double LongDeco;
    private int Alt_Deco_Baro;
    private int Alt_Deco_GPS;
    private pointIGC Alt_Maxi_Baro;
    private pointIGC Alt_Mini_Baro;
    private pointIGC Alt_Maxi_GPS;
    private pointIGC Alt_Mini_GPS;
    private pointIGC Vit_Max;
    private pointIGC Vit_Mini;
    private pointIGC Vario_Max;
    private pointIGC Vario_Mini;
    private int Alt_Attero_Baro;
    private int Alt_Attero_GPS;
    private long Duree_Vol;
    private String sDuree_Vol;
    private String colDureeVol;   // Duration with colon HH:MM:SS
    private int NbPoints;
    private int NbPointsAberr;
    private boolean Decodage_En_Tete;
    private String Score_JSON;
    private int Score_Idx_League;
    private String Score_League;
    private String Score_Shape;
    private Boolean Score_Triangle;
    private double Score_Route_Km;
    private double Score_Route_Pts;
    private double Score_Moyenne;
    private boolean Scored;
    private int Th_DeltaAltMax;
    private int Th_DeltaAltMini;
    private double bestTransDist;
    private int bestTransIndice1;
    private int bestTransIndice2;
    private int bestGain;
    private int bestGainIndice1;
    private int bestGainIndice2;
    private String Comment;
    private String photo;
    private int nbGpxPoint;
    private int totGpxPoint;
    
    public List<pointIGC> Tb_Tot_Points = new ArrayList<pointIGC>();
    public List<pointIGC> Tb_Good_Points = new ArrayList<pointIGC>();
    public List<pointIGC> Tb_Calcul = new ArrayList<pointIGC>();
    public List<thermique> Tb_Thermique = new ArrayList<thermique>();
    public ArrayList<Integer> Score_Tb_Balises = new ArrayList<Integer>();
    
    private final String CrLf = "\r\n";
    private StringBuilder sbError;
    
    // Needed for personal integration value and probably others parameters in the future
    configProg myConfig;
    
    /**
     * Track is a file
     * @param pFile 
     * @param pType
     * @param pPath
     * @param totalPoints 
     */
    public traceGPS(File pFile, Boolean totalPoints, configProg pConfig)
    {
        Decodage = false;
        Scored = false;        
        sSite = "";
        sVoile = "";
        sPilote = "";
        
        myConfig = pConfig;
        if (myConfig.getIntegration() > 0) {
            APP_INTEGRATION = myConfig.getIntegration();
        } else {
            APP_INTEGRATION = 15;
        }

        avecPoints = totalPoints;                       
        textio fread = new textio();                                    
        String pFichier = fread.readTxt(pFile);
        if (pFichier != null && !pFichier.isEmpty())  {
            pathFichier = pFile.getAbsolutePath();
            String fileExt = textio.getFileExtension(pFile).toUpperCase();     
            if (fileExt.equals("IGC"))
            {
                FicIGC = pFichier;
                Origine = "IGC";
                Decode_IGC();
            }
            else if (fileExt.equals("GPX"))
            {
                FicGPX = pFichier;
                Origine = "GPX";
                InputStream stream = new ByteArrayInputStream(pFichier.getBytes(StandardCharsets.UTF_8));
                DecodeGPX(stream);
            } else {
                numErrDecodage = 1060;    // unknown file extension 
            }                 
        }
    } 
    
    /**
     *  Track is a string
     * @param pFichier
     * @param pType
     * @param pPath
     * @param totalPoints 
     */
    public traceGPS(String pFichier, String pPath, Boolean totalPoints, configProg pConfig)
    {
        Decodage = false;
        Scored = false;
        myConfig = pConfig;
        sSite = "";
        sVoile = "";
        sPilote = "";
        if (myConfig.getIntegration() > 0) {
            APP_INTEGRATION = myConfig.getIntegration();
        } else {
            APP_INTEGRATION = 15;
        }
        avecPoints = totalPoints;
        pathFichier = pPath;
        // For IGC, it is expected that extension file is checked before calling traceGPS
        // IGC format is too imprecise... no specific mandatory record. Any one could be missing in the file.   
        if (pFichier == null) {
            Origine = "NIL"; 
        } else {
            if (pFichier.indexOf("topografix.com") > 0 ) {
                FicGPX = pFichier;
                Origine = "GPX";
                InputStream stream = new ByteArrayInputStream(pFichier.getBytes(StandardCharsets.UTF_8));
                DecodeGPX(stream);
            } else {
                FicIGC = pFichier;
                Origine = "IGC";
                Decode_IGC();
            }
        }        
    }         
    
    public int getAlt_Deco_Baro() {
        return Alt_Deco_Baro;
    }

    public void setAlt_Deco_Baro(int Alt_Deco_Baro) {
        this.Alt_Deco_Baro = Alt_Deco_Baro;
    }

    public void setAlt_Deco_GPS(int Alt_Deco_GPS) {
        this.Alt_Deco_GPS = Alt_Deco_GPS;
    }
    
    public int getAlt_Deco_GPS() {
        return Alt_Deco_GPS;
    }

    public int getAlt_Attero_Baro() {
        return Alt_Attero_Baro;
    }

    public int getAlt_Attero_GPS() {
        return Alt_Attero_GPS;
    }
        
    public pointIGC getAlt_Maxi_Baro() {
        return Alt_Maxi_Baro;
    }    

    public pointIGC getAlt_Mini_Baro() {
        return Alt_Mini_Baro;
    }

    public pointIGC getAlt_Maxi_GPS() {
        return Alt_Maxi_GPS;
    }

    public pointIGC getAlt_Mini_GPS() {
        return Alt_Mini_GPS;
    }
    
    public void setPathFichier(String pathFichier) {
        this.pathFichier = pathFichier;
    }

    public String getPathFichier() {
        return pathFichier;
    }

    public long getDuree_Vol() {
        return Duree_Vol;
    }

    public void setDuree_Vol(long Duree_Vol) {
        this.Duree_Vol = Duree_Vol;
    }
        

    public String getsDuree_Vol() {
        return sDuree_Vol;
    }

    public void setsDuree_Vol(String sDuree_Vol) {
        this.sDuree_Vol = sDuree_Vol;
    }

    public String getColDureeVol() {
        return colDureeVol;
    }
                       
    public int getBestGain() {
        return bestGain;
    }

    public int getBestGainIndice1() {
        return bestGainIndice1;
    }
    
    public int getBestGainIndice2() {
        return bestGainIndice2;
    }

    public int getBestTransIndice1() {
        return bestTransIndice1;
    }

    public int getBestTransIndice2() {
        return bestTransIndice2;
    }

    public double getLatDeco() {
        return LatDeco;
    }

    public void setLatDeco(double LatDeco) {
        this.LatDeco = LatDeco;
    }

    public void setLongDeco(double LongDeco) {
        this.LongDeco = LongDeco;
    }
    
    public double getLongDeco() {
        return LongDeco;
    }

    public int getNbPoints() {
        return NbPoints;
    }

    public int getNbPointsAberr() {
        return NbPointsAberr;
    }
    
    public LocalDateTime getDT_Deco() {
        return DT_Deco;
    }

    public void setDT_Deco(LocalDateTime DT_Deco) {
        this.DT_Deco = DT_Deco;
    }
    
    public LocalDateTime getDT_Attero() {
        return DT_Attero;
    }

    public void setDT_Attero(LocalDateTime DT_Attero) {
        this.DT_Attero = DT_Attero;
    }
          
    public String getsPilote() {
        return sPilote;
    }

    public LocalDateTime getDate_Vol() {
        return Date_Vol;
    }
    
    public void setDate_Vol(LocalDateTime Date_Vol) {
        this.Date_Vol = Date_Vol;
    }
    
    public String getsDate_Vol() {
        return sDate_Vol;
    }
    
    public String getDate_Vol_SQL() {
        return Date_Vol_SQL;
    }

    public TimeZone getTzVol() {
        return tzVol;
    }

    public double getUtcOffset() {
        return utcOffset;
    }

    public boolean isDstOffset() {
        return dstOffset;
    }        
    
    public String getFicIGC() {
        return FicIGC;
    }

    public String getFicGPX() {
        return FicGPX;
    }    
    
    public String getsVoile() {
        return sVoile;
    }

    public void setsVoile(String sVoile) {
        this.sVoile = sVoile;
    }

    public pointIGC getVit_Max() {
        return Vit_Max;
    }

    public pointIGC getVit_Mini() {
        return Vit_Mini;
    }
        
    public pointIGC getVario_Max() {
        return Vario_Max;
    }

    public pointIGC getVario_Mini() {
        return Vario_Mini;
    }

    public double getTrackLen() {
        return TrackLen;
    }

    public String getSignature() {
        return Signature;
    }

    public double getBestTransDist() {
        return bestTransDist;
    }        
                    
    
    public String getOrigine()
    {
      return Origine;
      
    }
    
    public Boolean isDecodage() {
        return Decodage;
    }

    public int getNumErrDecodage() {
        return numErrDecodage;
    }    
    
    public Boolean isScored() {
        return Scored;
    }
   
    public String getScore_Shape() {
        return Score_Shape;
    }

    public void setScore_Shape(String Score_Shape) {
        this.Score_Shape = Score_Shape;
    }

    public double getScore_Route_Km() {
        return Score_Route_Km;
    }

    public void setScore_Route_Km(double Score_Route_Km) {
        this.Score_Route_Km = Score_Route_Km;
    }

    public double getScore_Route_Pts() {
        return Score_Route_Pts;
    }

    public void setScore_Route_Pts(double Score_Route_Pts) {
        this.Score_Route_Pts = Score_Route_Pts;
    }

    public double getScore_Moyenne() {
        return Score_Moyenne;
    }

    public void setScore_Moyenne(double Score_Moyenne) {
        this.Score_Moyenne = Score_Moyenne;
    }

    public void setScored(boolean Scoring) {
        this.Scored = Scoring;
    }

    public String getScore_League() {
        return Score_League;
    }

    public void setScore_League(String Score_League) {
        this.Score_League = Score_League;
    }
        
    

    public int getScore_Idx_League() {
        return Score_Idx_League;
    }

    public void setScore_Idx_League(int Score_Idx_League) {
        this.Score_Idx_League = Score_Idx_League;
    }    
    
    public String getScore_JSON() {
        return Score_JSON;
    }

    public void setScore_JSON(String Score_JSON) {
        this.Score_JSON = Score_JSON;
    }

    public Boolean getScore_Triangle() {
        return Score_Triangle;
    }

    public void setScore_Triangle(Boolean Score_Triangle) {
        this.Score_Triangle = Score_Triangle;
    }

    public String getComment() {
        return Comment;
    }

    public void setComment(String Comment) {
        this.Comment = Comment;
    }

    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }

    public String getsSite() {
        return sSite;
    }

    public void setsSite(String sSite) {
        this.sSite = sSite;
    }
                   
    
    /**
     * if there is a non numeric character, Integer.parseInt triggers an exception
     * exception is managed with a default value
     * see http://stackoverflow.com/questions/1486077/java-good-way-to-encapsulate-integer-parseint
     * @param number
     * @param defaultVal
     * @return 
     */
    private static int checkParseInt(String number, int defaultVal) {
        // spaces are removed with a regex
        number = number.replaceAll("\\s+","");
        try {
            return Integer.parseInt(number);
        } catch (NumberFormatException e) {
            return defaultVal;
        }
    }
    
    /** Double checking
     * @param number
     * @param defaultVal
     * @return 
     */
    private static double checkParseDouble(String number, double defaultVal) {
        // spaces are removed with a regex
        number = number.replaceAll("\\s+","");
        try {
            return Double.parseDouble(number);
        } catch (NumberFormatException e) {
            return defaultVal;
        }
    }
                   
    /**
     * decoding all track points or only header and firstpoint (boolean avecPoints)
     * This choice of the first point is not relevant : GPS fix can be bad when first point is recorded
     */
    
    private void Decode_IGC()
    {
        Boolean Valid_Trace;
        String DebChar;
        String DebMot;
        Boolean flag_PEV;
        int LignesNonB;
        Boolean Decodage_HFDTE;
        String GoodName;
        int NbLgIgnorees;
        int Alti_Baro;
        int Alti_GPS;
        int BadAlti;
        int Deg;
        double Min,Sec;
        int TotPoint;
        
        Valid_Trace = false;
        Decodage_HFDTE = false;
        NbLgIgnorees = 0;
        BadAlti = 9000;
        TotPoint = 0;
        NbPointsAberr = 0;
        
        // For PEV details see FAI documentation p44
        // In a nutshell
        // The form of the E-Record is record identifer, time, TLC, textstring. Some examples follow, with extra spaces for clarity:
        // E 104533 PEV CR LF
        // B104533 4945333N 01132444EA 01357 01501CRLF
        // This indicates a pilot initiated event (PEV) at 10:45:33 UTC, and the associated B record shows the location 49:45.333 N 11:32.444 E, etc...
        // Some events require more than just the TLC for interpretation (with extra spaces for clarity):
        // E 104544 ATS 102312 CR LF
        flag_PEV = false;
        // we remove DC3 character sent by XON. This was a problem for IGC validation
        // This invisible character is detected by FFVL server. IGC file is rejected
        String BadCar = Character.toString((char)19);
        FicIGC = FicIGC.replace(BadCar,"");       
        // likewise DC1 character sent by XOFF is removed
        BadCar = Character.toString((char)17);
        FicIGC = FicIGC.replace(BadCar,"");    
        
        // File is converted in an array
        // At beginning split charracter was in xLogfly EndOfLine.Windows -> chr(13)+chr(10)
        // sLine = ficIGC.Split(EndOfLine.Windows)  
        // We received some tracklogs where there is only chr(10)
        BadCar = Character.toString((char)10);
        String[] sLine = FicIGC.split(BadCar);
        int Lg_sLine =  sLine.length;
        
        // later we received a track file with only chr(13) -:)
        if (Lg_sLine == 1) {
            BadCar = Character.toString((char)13);
            sLine = FicIGC.split(BadCar);
            Lg_sLine = sLine.length;
        }
        
        // On Flytec 6020, there is three G records + one empty line
        // with GPSDump, only one G records + one empty line
        // there is an empty line -> test on second-to-last row
        // Is is a valid track ?
        if (Lg_sLine > 3) {          
            // Use the string.equals(Object other) function to compare strings, not the == operator
            if (sLine[Lg_sLine - 1].substring(0,1).equals("G"))
                Valid_Trace = true;  
            else
                if (sLine[Lg_sLine - 2].substring(0).equals("G")) 
                    Valid_Trace = true;   
                else
                    if (sLine[Lg_sLine-3].substring(0).equals("G")) 
                    Valid_Trace = true;   
        }           
        if ( !Valid_Trace){
            // crash or no G record
            for (int k = 0; k < Lg_sLine; k++) {
                if (sLine[k].length() > 1) {
                    DebChar = sLine[k].substring(0,1);
                    if (DebChar.equals("B") && sLine[k].length() > 23 ) {
                        Valid_Trace = true;
                        break;
                    }
                }
            }
      
        }
        
        // for substring, the begin index is inclusive but the end index isexclusive.
        if (Valid_Trace) {
            LignesNonB = 0;
            long topDebut = System.currentTimeMillis();
            for (int i = 0; i < Lg_sLine; i++) {
                if (!sLine[i].trim().equals("")) {
                    DebChar = sLine[i].substring(0,1);
                    // signature decoding
                    if (DebChar.equals("A") && sLine[i].length() > 3) {
                        Signature = sLine[i].substring(1,4);
                    }          
                    if (!DebChar.equals("B")) {
                        if (DebChar.equals("E") && sLine[i].indexOf("PEV")> -1)
                            flag_PEV = true;
                        // count of lines except line beginning by B or by E
                        LignesNonB++;
                        // if ligne not begin by B, this is not a standard position line
                        // sometimes, we have a line with a bad carriage return (CR+LF) 
                        // B1541104553226N00627642EA0221002345000<CR> and 5562 <LF> GPS record problem  ?
                        // To avoid exceptions, we keep the line if length > 11 
                        if (sLine[i].length() > 10) {
                            DebMot = sLine[i].substring(0,5);
                            switch (DebMot)
                            {
                              case "HFDTE": 
                                // sometime IGC is truncated, in this case date can be wrong
                                // 2016 new  -> from HFDTE060910 we will have HFDTEDATE:070816,01
                                // after comma, number is the day flight number
                                // better to use regex, semi colon is not constant
                                Pattern pHDte = Pattern.compile("\\D+(\\d{2})(\\d{2})(\\d{2})");       
                                Matcher SearchDate = pHDte.matcher(sLine[i]);
                                if(SearchDate.find() && SearchDate.groupCount() == 3) {
                                    int Annee = Integer.parseInt("20"+SearchDate.group(3));
                                    int Mois = Integer.parseInt(SearchDate.group(2));
                                    int Jour = Integer.parseInt(SearchDate.group(1));
                                    // Ces paramètres calculés sur L'UTC seront modifiés plus tard après calcul du décalage UTC
                                    // On eu des vols réalisés en Australie le 1er janvier à 10h où l'UTC est au 31 décembre à 23h... 
                                    Date_Vol = LocalDateTime.of(Annee, Mois, Jour,0,0,0);
                                    sDate_Vol = Date_Vol.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                                    Decodage_HFDTE = true;                                                                          
                                }                                                                 
                                break;   
                              case "HFPLT": 
                              case "HPPLT": 
                              case "HOPLT" :   // in GPSDump we have HOPLTPILOT: G. Legras instead of HFPLTPILOT:G. Legras
                                  // pilot name decoding, 17 characters or less
                                  String[] LineHFPLT = sLine[i].split(":");
                                  if (LineHFPLT.length > 1) sPilote = LineHFPLT[1];
                                  GoodName = "";
                                  // bad display of accent (Daphné ->Daphn@), we remove
                                  // we can find a carriage return
                                  for (int kk = 0 ; kk < sPilote.length() ; kk++) {
                                        char aChar = sPilote.charAt(kk);                                      
                                        if ((int)aChar > 31 && (int)aChar < 123) {
                                            if ((int)aChar == 39) GoodName += "\\";     // Escape single quote                                                                                                           
                                            GoodName = GoodName.concat(sPilote.substring(kk,kk+1));
                                        }
                                  }
                                  sPilote = GoodName;
                                break;                                
                                case "HFGTY":  // glider name decoding
                                case "HPGTY":                                    
                                case "HOGTY":                                    
                                  // glider name decoding
                                  String[] LineHFGTY = sLine[i].split(":");
                                  if (LineHFGTY.length > 1) sVoile = LineHFGTY[1];  
                                  GoodName = "";
                                  // always a possible CR in the string
                                  for (int kk = 0 ; kk < sVoile.length() ; kk++) {
                                        char aChar = sVoile.charAt(kk);                                      
                                        if ((int)aChar > 31 && (int)aChar < 123) {
                                            if ((int)aChar == 39) GoodName += "\\";     // Escape single quote  
                                            GoodName = GoodName.concat(sVoile.substring(kk,kk+1));
                                        }
                                  }
                                  sVoile = GoodName;
                                break;                               
                                case "HFRFW":
                                  // Other case for glider name
                                  String[] LineHFRFW = sLine[i].split(":");
                                  if (LineHFRFW.length > 1) sFirmware = LineHFRFW[1];    
                                  GoodName = "";
                                  // always a possible CR in the string
                                  for (int kk = 0 ; kk < sVoile.length() ; kk++) {
                                        char aChar = sVoile.charAt(kk);                                      
                                        if ((int)aChar > 31 && (int)aChar < 123) GoodName = GoodName.concat(sVoile.substring(kk,kk+1));
                                  }
                                  sVoile = GoodName;
                                break;
                               
                            }
                        }
                    }
                    else
                    {
                        // B line decoding
                        // TAS > 9 therefore speed... last 3 bytes of B record in IGC file
                        //  Byte must be "A" for a valid altitude 
                        //  we had a track where TAS is 0 we take care only of byte "A"
                        //  checking of all characters mini is 35 for correct parsing                        
                        if (sLine[i].length() > 34) {
                            if (sLine[i].length() > 24)
                                DebMot = sLine[i].substring(24,25);
                            else
                                DebMot = "";
                            Alti_Baro = 0;
                            Alti_GPS = 0;
                            if (DebMot.equals("V"))  {
                                if (sLine[i].length() > 30)                                    
                                    Alti_Baro = checkParseInt(sLine[i].substring(25,30),0);
                                else
                                    Alti_Baro = 0;              
                            }
                            if (DebMot.equals("A"))  {
                                if (sLine[i].length() > 30)                                    
                                    Alti_Baro = checkParseInt(sLine[i].substring(25,30),0);
                                else
                                    Alti_Baro = 0;    
                                if (sLine[i].length() > 34)                                    
                                    Alti_GPS = checkParseInt(sLine[i].substring(30,35),0);
                                else
                                    Alti_GPS = 0;  
                            }
                            pointIGC Point1 = new pointIGC();
                            Point1.setComment("");
                            Point1.setAltiBaro(Alti_Baro);
                            Point1.setAltiGPS(Alti_GPS);
                            // With Flytec problems, remove points with null GPS alt is not a good solution
                            // the next line is temporarily escaped
                            //if (Point1.AltiGPS <= 0) Point1.setComment("ZERO");
                            // make a comparison of GPS lat and baro alt
                            if (Point1.AltiBaro > 0 && Point1.AltiGPS > 0 && Math.abs(Point1.AltiGPS -  Point1.AltiBaro) > 500) Point1.setComment("BAD");
                            // Cf. track 02/08/11 where GPS alt = 46500...
                            if (Point1.AltiGPS > BadAlti) Point1.setComment("BAD");
                            // air speed
                            if (sLine[i].length() > 37) Point1.setTAS(checkParseInt(sLine[i].substring(35,38),0));                                                                                       
                            // latitude decoding
                            Deg = checkParseInt(sLine[i].substring(7,9),0);
                            Min = checkParseDouble(sLine[i].substring(9,11),0);
                            Sec = (checkParseDouble(sLine[i].substring(11,14),0)/1000) * 60;                           
                            Point1.setLatitudeDMS(Deg, Min, Sec, sLine[i].substring(14,15));
                            // recorded in seconds for scoring
                            Min = checkParseDouble(sLine[i].substring(9,14),0);
                            Point1.setLatitudeSec(Deg,(int)Min,sLine[i].substring(14,15)); 
                            
                            // longitude decoding
                            Deg = checkParseInt(sLine[i].substring(15,18),0);
                            Min = checkParseDouble(sLine[i].substring(18,20),0);
                            Sec = (checkParseDouble(sLine[i].substring(20,23),0)/1000) * 60;
                            Point1.setLongitudeDMS(Deg, Min, Sec, sLine[i].substring(23,24));
                            // recorded in seconds for scoring
                            Min = checkParseDouble(sLine[i].substring(18,23),0);
                            Point1.setLongitudeSec(Deg,(int)Min,sLine[i].substring(23,24));
                            // period computing (seconds)
                            Point1.setPeriode(checkParseInt(sLine[i].substring(1,3),0)*3600+checkParseInt(sLine[i].substring(3,5),0)*60+checkParseInt(sLine[i].substring(5,7),0));                            
                            // date coding
                            if (Decodage_HFDTE) {
                                Point1.setdHeure(Date_Vol,checkParseInt(sLine[i].substring(1,3),0), checkParseInt(sLine[i].substring(3,5),0), checkParseInt(sLine[i].substring(5,7),0));    
                            }
                            
                            if (i-LignesNonB == 0) {   
                                // first point
                                if (!Decodage_HFDTE)  {
                                    // We can find a track without date header (SeeYou export).
                                    // To avoid exception we initialize 01/01/2000
                                    Date_Vol = LocalDateTime.of(2000, 1, 1, 0, 0, 0); 
                                    sDate_Vol = Date_Vol.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                                    // For SQLIte
                                    Date_Vol_SQL = "2000-01-01 ";
                                    Decodage_HFDTE = true;
                                    // In this case Point1.dHeure was not initialized
                                    Point1.setdHeure(Date_Vol,checkParseInt(sLine[i].substring(1,3),0), checkParseInt(sLine[i].substring(3,5),0), checkParseInt(sLine[i].substring(5,7),0));                            
                                }
                                // Decoding without points, we want only header with global informations :date, pilot name, etc...
                                if (!avecPoints) {
                                    LocalDateTime iniDate_Vol = Date_Vol;
                                    // timezone computing
                                    tzVol = tzCalcul(Point1);
                                    if (tzVol.getID() != null)  {
                                        ZonedDateTime utcZDT = Point1.dHeure.atZone(ZoneId.of("Etc/UTC"));
                                        DT_Deco = LocalDateTime.ofInstant(utcZDT.toInstant(), ZoneId.of(tzVol.getID()));                                         
                                    } else  {
                                        // no timezone, we keep UTC time
                                        DT_Deco = Point1.dHeure;
                                    }
                                    // Update UTC offset
                                    majParamUTC(DT_Deco);
                                    DateTimeFormatter formatterSQL = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");                                        
                                    Date_Vol_SQL = DT_Deco.format(formatterSQL); 
                                    // update of day date if necessary (Australia, NZ)
                                    Date_Vol = DT_Deco;
                                    sDate_Vol = Date_Vol.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                                        
                                    // take off  paremeters update
                                    LatDeco = Point1.Latitude;
                                    LongDeco = Point1.Longitude;
                                    Alt_Deco_Baro = Point1.AltiBaro;
                                    Alt_Deco_GPS = Point1.AltiGPS;
                                    // compute duration with end point
                                    int ideb = i;
                                    int j = Lg_sLine -1;   // for (int i = 0; i < Lg_sLine; i++) {  

                                    DebChar = sLine[j].substring(0,1);    
                                    // We must test j Sometimes, the GPS screwed
                                    // we find tracks with only one B record
                                    if (j > ideb)  {
                                        while (!DebChar.equals("B") || j == ideb ) {
                                            j--;
                                            DebChar = sLine[j].substring(0,1);
                                        }
                                    }
                                    calcLastPoint(sLine[j],iniDate_Vol);
                                    Decodage = true;
                                    return;
                                }                                                                        
                            }
                            else  {                                        
                                pointIGC PtPcdt = Tb_Tot_Points.get(TotPoint - 1);                                 
                                // parameters computing with previous point
                                // Totpoint = 1 more than last array index
                                Point1.setDistPtPcdt(trigo.CoordDistance(Point1.Latitude,Point1.Longitude,PtPcdt.Latitude,PtPcdt.Longitude));
                                long DiffSec = ChronoUnit.SECONDS.between(PtPcdt.dHeure, Point1.dHeure);
                                Point1.setPeriodePtPcdt((int)DiffSec);
                                // to avoid a division by zero
                                if (Point1.PeriodePtPcdt > 0) {
                                    Point1.setVitesse(Point1.DistPtPcdt / Point1.PeriodePtPcdt * 3.6);
                                    if (flag_PEV)  {
                                        // previous line indicate a PEV position, we record it
                                        Point1.setComment("PEV");
                                        flag_PEV = false;
                                     }
                                }
                                else  {
                                    // Reversale GPS record several points by second
                                    // this duplicates points give false results in average and max computing
                                    // we put a flag to skip this point in flight analysis
                                    Point1.setComment("DOUBLON");
                                }   
                                // in Australia or NZ, a flight around midnight UTC is possible. We must manage a date change
                                // we had a track with outliers like
                                // Point X 18:38.38     Point X+1 18:38.39         Point X+2 18:38.38
                                // we must check with first point
                                if (Point1.dHeure.isBefore(PtPcdt.dHeure))  {
                                    PtPcdt = Tb_Tot_Points.get(0);
                                    if (Point1.dHeure.isBefore(PtPcdt.dHeure)) {
                                        // day date must be incremented
                                        Date_Vol.plusDays(1);                       
                                    }
                                }
                                
                                
              
              
                            } 
                           if (!"DOUBLON".equals(Point1.Comment))   {
                                // outliers management            
                                // A Zero point [non valid GPS alt] is not necessarily an outlier 
                                //if (Point1.Comment == "ZERO" || Point1.Comment == "DIST" || Point1.Comment == "BAD" || Point1.Comment == "PEV")
                                if (Point1.Comment == "DIST" || Point1.Comment == "BAD" || Point1.Comment == "PEV")
                                    NbPointsAberr++;
                                Tb_Tot_Points.add(Point1);              
                                TotPoint++;
                            }           
                        }
                        else  
                            NbLgIgnorees++;                                                             
                    }
                }
            
            }
            long topFin = System.currentTimeMillis();
            float seconds = (topFin - topDebut) / 1000F;
            System.out.println("Décodage IGC : "+ Float.toString(seconds) + " secondes.");
            if (TotPoint > 5 && Decodage_HFDTE)  {
                // time shift to laocal time for all points
                utcToLocalDecalage();

                // filling Tb_Good_Points                
                topDebut = System.currentTimeMillis();
                // first False is a parameter imposed by GPX decoding
                // second is for inclusion of outliers
                Verif_Tb_Tot_Points(false, false);
                topFin = System.currentTimeMillis();
                seconds = (topFin - topDebut) / 1000F;
                System.out.println("Verif_Tb_Tot_Points : "+ Float.toString(seconds) + " secondes.");
      
                // we had a case with zero good points (bad IGC file) 
                // Dt_Deco is updated in Verif_Tb_Tot_Points
                // therefore we crashed in flight duration
                if (Tb_Good_Points.size() > 1)  {
                    pointIGC LastPoint = Tb_Good_Points.get(Tb_Good_Points.size() - 1);                                        
                    DT_Attero = LastPoint.dHeure;
                    // altitude landing recorded
                    Alt_Attero_Baro = LastPoint.AltiBaro;
                    Alt_Attero_GPS = LastPoint.AltiGPS;
                    // it's possible to compute flight duration
                    Duree_Vol = Duration.between(DT_Deco,DT_Attero).getSeconds();
                    // compute average period between two points
                    int AvgPeriode = (int) (Duree_Vol / Tb_Good_Points.size());
                    if (AvgPeriode < 1) AvgPeriode = 1;
                    LocalTime TotSecondes = LocalTime.ofSecondOfDay(Duree_Vol);
                    sDuree_Vol = TotSecondes.getHour()+"h"+TotSecondes.getMinute()+"mn";
                    colDureeVol = String.format("%02d", TotSecondes.getHour())+":"+String.format("%02d", TotSecondes.getMinute())+":"+String.format("%02d", TotSecondes.getSecond());
                    NbPoints = TotPoint;
                    // in xLogfly we recalculated an average speed
                    // it seems unnecessary an average is computed in Verif_Tb_Tot_Points with an integration parameter
                    
                    // Reduce number of points for scoring
                    fillTb_Calcul();
                    
                    // compoute thermals points
                    calc_Thermiques();
                                        
                    Decodage = true;
                }
            }
        }
        
        // System.out.println(String.valueOf(Lg_sLine)+" lignes");
        // System.out.println("Valid_Trace : "+Valid_Trace);
        // System.out.println("Date vol : "+sDate_Vol);
        // System.out.println("Pilote : "+sPilote);
        // System.out.println("Date vol SQL : "+Date_Vol_SQL);
        // System.out.println("Signature : "+Signature);
        // System.out.println(TotPoint+" points ajoutés");                              
    }
    
    
    /**
     * When track is not completely decoded
     * this function is called to compute flight duration with last point
     * @param lastB
     * @param iniDateVol 
     */
    private void calcLastPoint(String lastB, LocalDateTime iniDateVol ) {
        String DebMot;
        int Deg;
        double Min,Sec;
        int Alti_Baro;
        int Alti_GPS;
        
        if (lastB.length() > 34) {
            if (lastB.length() > 24)
                DebMot = lastB.substring(24,25);
            else
                DebMot = "";
            Alti_Baro = 0;
            Alti_GPS = 0;
            if (DebMot.equals("V"))  {
                if (lastB.length() > 30)                                    
                    Alti_Baro = checkParseInt(lastB.substring(25,30),0);
                else
                    Alti_Baro = 0;              
            }
            if (DebMot.equals("A"))  {
                if (lastB.length() > 30)                                    
                    Alti_Baro = checkParseInt(lastB.substring(25,30),0);
                else
                    Alti_Baro = 0;    
                if (lastB.length() > 34)                                    
                    Alti_GPS = checkParseInt(lastB.substring(30,35),0);
                else
                    Alti_GPS = 0;  
            }
            pointIGC Point1 = new pointIGC();
            Point1.setComment("");
            Point1.setAltiBaro(Alti_Baro);
            Point1.setAltiGPS(Alti_GPS);            
            // latitude decoding
            Deg = checkParseInt(lastB.substring(7,9),0);
            Min = checkParseDouble(lastB.substring(9,11),0);
            Sec = (checkParseDouble(lastB.substring(11,14),0)/1000) * 60;                           
            Point1.setLatitudeDMS(Deg, Min, Sec, lastB.substring(14,15));
            // recorded in seconds for scoring
            Min = checkParseDouble(lastB.substring(9,14),0);
            Point1.setLatitudeSec(Deg,(int)Min,lastB.substring(14,15)); 

            // longitude decoding
            Deg = checkParseInt(lastB.substring(15,18),0);
            Min = checkParseDouble(lastB.substring(18,20),0);
            Sec = (checkParseDouble(lastB.substring(20,23),0)/1000) * 60;
            Point1.setLongitudeDMS(Deg, Min, Sec, lastB.substring(23,24));
            // recorded in seconds for scoring
            Min = checkParseDouble(lastB.substring(18,23),0);
            Point1.setLongitudeSec(Deg,(int)Min,lastB.substring(23,24));                       
            // date coding Decodage_HFDTE is necessarily OK
            Point1.setdHeure(iniDateVol,checkParseInt(lastB.substring(1,3),0), checkParseInt(lastB.substring(3,5),0), checkParseInt(lastB.substring(5,7),0));                
            // Update final flight parameters
            long decUTC = (long) (utcOffset*3600);
            DT_Attero = Point1.dHeure.plusSeconds(decUTC);
            // altitude landing recorded            
            // it's possible to compute flight duration
            Duree_Vol = Duration.between(DT_Deco,DT_Attero).getSeconds();
            if (Duree_Vol > 0) {
                // compute average period between two points            
                LocalTime TotSecondes = LocalTime.ofSecondOfDay(Duree_Vol);
                sDuree_Vol = TotSecondes.getHour()+"h"+TotSecondes.getMinute()+"mn";
                colDureeVol = String.format("%02d", TotSecondes.getHour())+":"+String.format("%02d", TotSecondes.getMinute())+":"+String.format("%02d", TotSecondes.getSecond());
            } else {
                sDuree_Vol = "not def";
                colDureeVol = "not def";
            }
                        
            Alt_Attero_Baro = Point1.AltiBaro;
            Alt_Attero_GPS = Point1.AltiGPS;
            
        }                
    }
    
    /**
     * Compute TimeZone
     */
    private void utcToLocalDecalage()  {
                
        tzVol = tzCalcul(Tb_Tot_Points.get(0));
        if (tzVol.getID() != null)  {
            int i = 0;
            int TotPoints = Tb_Tot_Points.size();
            while (i < TotPoints)  {   
                pointIGC myPoint = Tb_Tot_Points.get(i);  
                ZonedDateTime utcZDT = myPoint.dHeure.atZone(ZoneId.of("Etc/UTC"));               
                myPoint.dHeure = LocalDateTime.ofInstant(utcZDT.toInstant(), ZoneId.of(tzVol.getID()));                 
                Tb_Tot_Points.set(i, myPoint);
                i++; 
            }
        }         
    }
    
    /**
     * For TimeZone computing, llttz of Artem Gapchenko (https://github.com/agap/llttz) is called
     * @param p1
     * @return 
     */
    private TimeZone tzCalcul(pointIGC p1)  {        
        IConverter iconv = Converter.getInstance(TimeZoneListStore.class);
        TimeZone tzCalc = iconv.getTimeZone(p1.Latitude,p1.Longitude); 
        
        return tzCalc;
    }
    
    /**
     * offset UTC is updated
     * @param ldt 
     */
    private void majParamUTC(LocalDateTime ldt)  {
        ZoneId idz = ZoneId.of(tzVol.getID()); 
        ZonedDateTime zdt = ZonedDateTime.of(ldt, idz);  
        double decalage = tzVol.getRawOffset()/3600000;
        if (idz.getRules().isDaylightSavings(zdt.toInstant()))  {            
            decalage += tzVol.getDSTSavings()/3600000;           
            dstOffset = true;            
        } else  {
            dstOffset = false;
        }   
        utcOffset = decalage;
    }
    
    /**
     * scoring program is fast but can take time with big tracks (1 point/seconde)
     * if necessary we reduce to 1 point / 5 secondes
     * IGC temp file for scoring comuting is created with this array
     */
    private void fillTb_Calcul() {
        
        int iReduction;
        pointIGC currPoint;
        int iTotPoints = Tb_Good_Points.size();
        int iIntervalle = (int) (Duree_Vol/ iTotPoints);
        
        if (iIntervalle < 2) { iReduction = 5; } else
        if (iIntervalle < 3) { iReduction = 4; } else
        if (iIntervalle < 4) { iReduction = 3; } else
        if (iIntervalle < 5) { iReduction = 2; } else
            iReduction = 1;
        
        for (int i = 0; i < iTotPoints; i+=iReduction) {
            currPoint = Tb_Good_Points.get(i);
            Tb_Calcul.add(currPoint);           
        }
    }
           
    private void Verif_Tb_Tot_Points(boolean MissTime, boolean WithAberrant)
    {
        /* this specific methos was introduced after a nightmare track file (barbare.igc)
        * first point is not good, therefore at point 2 we had a big speed
        * and point 2 was tagged like an outlier
        * this is why point 2 is now the first point of Tb_Good_Points
        * Problem : speed must be zero. If not, point is tagged OK and this big speed becomes the reference for max and average computing
        */
        int i, ii, TotPoints, TotGoodPoints;
        int BadVitesse;
        int BadAlti;
        double DistVerif;        
        long DeltaTime;
        double DeltaDist;
        double DistVerifDiv;
        pointIGC AltMaxGps = new pointIGC();
        pointIGC AltMaxBaro = new pointIGC();
        pointIGC AltMiniGps = new pointIGC();
        pointIGC AltMiniBaro = new pointIGC();
        pointIGC VitMax = new pointIGC();
        pointIGC VitMini = new pointIGC();
        pointIGC VarioMax = new pointIGC();
        pointIGC VarioMini = new pointIGC();  
        int DebugVit;
        double Vz;
        
        
        BadVitesse = 99;
        BadAlti = 9000;
        TotPoints = Tb_Tot_Points.size();
        TotGoodPoints = 0;
        // begin with second point
        i = 1;
        TrackLen = 0;
        pointIGC Pcdt2Point = null;
        pointIGC iiPoint = null;
        
        try   {
            while (i < (TotPoints-1))  {            
                pointIGC CurrPoint = Tb_Tot_Points.get(i);
                pointIGC PcdtPoint = Tb_Tot_Points.get(i-1);
                pointIGC NextPoint = Tb_Tot_Points.get(i+1);
                // for scoring, we keep all points even tagged "DOUBLON" in Tb_Tot_Points
                if (!"DOUBLON".equals(CurrPoint.Comment))   {
                    if (CurrPoint.PeriodePtPcdt > 0 || MissTime)  {
                        if (CurrPoint.Vitesse > BadVitesse)  {
                            // how is the next point ?
                            if (NextPoint.Vitesse < BadVitesse)  {
                                // next point is good, we can exam previous point against next
                                DistVerif = trigo.CoordDistance(NextPoint.Latitude,NextPoint.Longitude,PcdtPoint.Latitude,PcdtPoint.Longitude);
                                if (NextPoint.DistPtPcdt == 0)  {
                                    DistVerifDiv = 1;
                                }
                                else    {
                                    DistVerifDiv = NextPoint.DistPtPcdt;
                                }
                                if (DistVerif/DistVerifDiv > 3) {
                                    // Point i-1 is an outlier, it is tagged DIST
                                    PcdtPoint.setComment("DIST");
                                    Tb_Tot_Points.set(i-1, PcdtPoint);
                                    // but the speed at point i is wrong with no tag
                                    // we put zero not to distort average computing
                                    CurrPoint.Vitesse = 0;
                                    CurrPoint.DistPtPcdt = 0;
                                    Tb_Tot_Points.set(i, CurrPoint);              
                                }
                                else   {
                                    // point i is really an outlier, it is tagged
                                    CurrPoint.setComment("DIST");
                                    CurrPoint.Vitesse = 0;
                                    CurrPoint.DistPtPcdt = 0;
                                    Tb_Tot_Points.set(i, CurrPoint);  

                                }
                            }
                        }
                        if (PcdtPoint.Comment.trim() == "" || WithAberrant)   {
                            TotGoodPoints++;
                            // if point 1, take off coordinates are recorded
                            if (TotGoodPoints == 1)   {                                                                                                                                                  
                                // take off time is recorded
                                DT_Deco = PcdtPoint.dHeure;
                                // UTC offset is updated
                                majParamUTC(DT_Deco);
                                DateTimeFormatter formatterSQL = DateTimeFormatter.ofPattern("yyyy-MM-dd ");                                        
                                Date_Vol_SQL = DT_Deco.format(formatterSQL);          
                                // datetime is formatted for SQLIte
                                Date_Vol_SQL = Date_Vol_SQL +PcdtPoint.dHeure.format(DateTimeFormatter.ofPattern("HH:mm:ss"));                                    
                                // Date_Vol completed
                                Date_Vol = Date_Vol.plusHours(PcdtPoint.dHeure.getHour());
                                Date_Vol = Date_Vol.plusMinutes(PcdtPoint.dHeure.getMinute());
                                Date_Vol = Date_Vol.plusSeconds(PcdtPoint.dHeure.getSecond());            
                                // take off coordinates
                                LatDeco = PcdtPoint.Latitude;
                                LongDeco = PcdtPoint.Longitude;
                                // take off altitude
                                Alt_Deco_Baro = PcdtPoint.AltiBaro;
                                Alt_Deco_GPS = PcdtPoint.AltiGPS;
                                AltMaxBaro = PcdtPoint;
                                AltMaxGps = PcdtPoint;
                                AltMiniBaro = PcdtPoint;
                                AltMiniGps = PcdtPoint;
                                VitMax = PcdtPoint;
                                VitMini = PcdtPoint;
                            } else   {

                                // ------------ begin average speed computing -------------------
                                // average speed with min period 20 seconds
                                PcdtPoint = Tb_Tot_Points.get(i-1);
                                DeltaTime = PcdtPoint.PeriodePtPcdt;
                                ii = 2;
                                DeltaDist = PcdtPoint.DistPtPcdt;
                                // direct length track                            
                                TrackLen += DeltaDist;
                                if (DeltaTime < APP_INTEGRATION - 1)   {
                                    if ((i-1) - ii > -1) {
                                        do   {   
                                                CurrPoint = Tb_Tot_Points.get((i-1)-ii);
                                                if (CurrPoint.Comment.equals(""))  {                                                
                                                    DeltaTime = Duration.between(CurrPoint.dHeure, PcdtPoint.dHeure).getSeconds();
                                                    // we go back two points (ii) for elapsed time
                                                    // but distance must be computed with ii -1
                                                    Pcdt2Point = Tb_Tot_Points.get((i-1)-(ii-1));
                                                    DeltaDist += Pcdt2Point.DistPtPcdt;                    
                                                }
                                                ii++;
                                        } while (DeltaTime <= APP_INTEGRATION - 1 && ((i-1) - ii) > 0);
                                    }
                                }
                                if (DeltaTime > 0)  { 
                                    DebugVit = (int) (DeltaDist / DeltaTime * 3.6);
                                } else {
                                    DebugVit = 0;
                                }
                                PcdtPoint.setVitesse(DebugVit);
                                if (PcdtPoint.Vitesse > VitMax.Vitesse && PcdtPoint.Vitesse <= BadVitesse)
                                    VitMax = PcdtPoint;
                                if (PcdtPoint.Vitesse < VitMini.Vitesse && PcdtPoint.Vitesse > 0)
                                    VitMini = PcdtPoint;
                                // ------------ end average speed computing -------------------


                                // max and mini altitude computing
                                if (PcdtPoint.AltiBaro > AltMaxBaro.AltiBaro && PcdtPoint.AltiBaro < BadAlti)
                                    AltMaxBaro = PcdtPoint;
                                if (PcdtPoint.AltiGPS > AltMaxGps.AltiGPS && PcdtPoint.AltiGPS < BadAlti)
                                    AltMaxGps = PcdtPoint;
                                if (PcdtPoint.AltiBaro < AltMiniBaro.AltiBaro && PcdtPoint.AltiBaro > 0)
                                    AltMiniBaro = PcdtPoint;
                                if (PcdtPoint.AltiGPS < AltMiniGps.AltiGPS && PcdtPoint.AltiGPS > 0)
                                    AltMiniGps = PcdtPoint;

                                // average vario with min period 20 seconds                                
                                DeltaTime = PcdtPoint.PeriodePtPcdt;
                                ii = 2;
                                //----------------------------------
                                if (DeltaTime < APP_INTEGRATION - 1) {
                                    if (TotGoodPoints - ii > -1) {
                                        do  {
                                            iiPoint = Tb_Tot_Points.get(TotGoodPoints-ii);
                                            DeltaTime = Duration.between(iiPoint.dHeure, PcdtPoint.dHeure).getSeconds();                                        
                                            ii++;
                                        } while (DeltaTime <= APP_INTEGRATION - 1 && TotGoodPoints - ii > -1);
                                    }
                                } else {
                                    // necessary if time between two points is  superior to integration setting
                                    // with a priod of 15 secons in the track and an integration setting of 10 seconds, Vz is zero
                                    // with 3, ii = ii - 1 give 2, initial value
                                    ii = 3;
                                }
                                //-----------------------------------
                                ii = ii - 1;
                                iiPoint = Tb_Tot_Points.get(TotGoodPoints-ii);                                // for Vz computing we prefer baro altitudes
                                if (PcdtPoint.AltiBaro > 0) {
                                    
                                    Vz = (double)(PcdtPoint.AltiBaro - iiPoint.AltiBaro)/DeltaTime;
                                    // we had a track with a difference of 500 meters between two points
                                    // we must test a free failing value ( near 50m/s)                                    
                                    if (Vz > 20 || Vz < -55) {
                                        // bad Vz , we take GPS altitude
                                        PcdtPoint.setVario((double) (PcdtPoint.AltiGPS - iiPoint.AltiGPS)/DeltaTime);                                    
                                    } else {
                                        PcdtPoint.setVario(Vz);
                                    }
                                } else {
                                    PcdtPoint.setVario((double) (PcdtPoint.AltiGPS - iiPoint.AltiGPS)/DeltaTime);
                                }               
                                if (PcdtPoint.Vario > VarioMax.Vario) 
                                    VarioMax = PcdtPoint;
                                if (PcdtPoint.Vario < VarioMini.Vario)
                                    VarioMini = PcdtPoint;                           
                            }
                            Tb_Good_Points.add(PcdtPoint); 
                        }
                    }                
                }
                i++;              
            }            
        }catch(Exception ex){
            sbError = new StringBuilder(this.getClass().getName()+"."+Thread.currentThread().getStackTrace()[1].getMethodName());
            sbError.append("\r\n").append(ex.toString());
            sbError.append("\r\n").append("Index loop :").append(String.valueOf(i));
            mylogging.log(Level.SEVERE, sbError.toString());
        }
        /*  
            We stopped two points before the end of the track, we must include them            
            
            Ou bien prendre le parti de laisser tomber, les deux derniers points sont au sol...
  
            Cependant si c'est un tracé GPX avec points aberrants et sans base temps,
            on prends le parti d'ajouter les deux points manquants sans les vérifier
        
            En faisant la conversion java, je m'aperçois que l'on introduit une erreur
            les deux derniers points sont absents même s'ils sont bon
        */
        if (MissTime && WithAberrant) {
            if (TotPoints-1 < Tb_Tot_Points.size()-1) Tb_Good_Points.add(Tb_Tot_Points.get(TotPoints-1));
            if (TotPoints < Tb_Tot_Points.size()) Tb_Good_Points.add(Tb_Tot_Points.get(TotPoints));         
        }
        // mini max updated  
        Alt_Maxi_Baro = AltMaxBaro;
        Alt_Maxi_GPS = AltMaxGps;
        Alt_Mini_Baro = AltMiniBaro;
        Alt_Mini_GPS = AltMiniGps;
        Vit_Max = VitMax;
        Vit_Mini = VitMini;
        Vario_Max = VarioMax;
        Vario_Mini = VarioMini;
        TrackLen = TrackLen / 1000;   // meters converted to km
    }
         
    private void DecodeGPX(InputStream in) {
        
        int nbPoint = 0;   
        int nbWp;
        int TotPoint = 0;
        int totWp = 0;
        LocalDateTime ldt;
        WayPoint lastWp = null;
        boolean MissTime = false;
        SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        DateTimeFormatter dtfDate = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        
        // we must check if creator is empty
        if (FicGPX.indexOf("creator=\"\"") > 0 ) {
            FicGPX = FicGPX.replace("creator=\"\"", "creator=\"Logfly\"");
        }
        InputStream stream = new ByteArrayInputStream(FicGPX.getBytes(StandardCharsets.UTF_8));
        try {           
            GPX mygpx = GPX.read(stream);
            List<Track> lTrack = mygpx.getTracks();
            if (lTrack.size() > 0) {                
                List<WayPoint> lWayp = mygpx.tracks().flatMap(Track::segments).flatMap(TrackSegment::points).collect(Collectors.toList());                       
                nbWp = 0;
                for (io.jenetics.jpx.WayPoint eachWp : lWayp) {                            
                    pointIGC Point1 = new pointIGC();
                    Point1.setComment("");
                    Point1.setLatitude(eachWp.getLatitude().doubleValue());
                    Point1.setLongitude(eachWp.getLongitude().doubleValue());
                    Point1.setAltiGPS(eachWp.getElevation().get().intValue());   
                    // GPX -> Time is UTC. We must convert to LocalDateTime
                    // with Instant we convert in UTC +0
                    if (eachWp.getTime() != null) {
                        ldt = eachWp.getTime().get().toLocalDateTime();
                        Point1.setPeriode((ldt.getHour()*3600)+(ldt.getMinute()*60)+ldt.getSecond());
                    } else {
                        ldt = null;
                        Point1.setPeriode(0);
                    }
                    Point1.setLdtHeure(ldt);         
                    // First point of GPX track
                    if (nbPoint == 0) {
                        // take off time
                        if (eachWp.getTime() != null) {
                            DT_Deco = ldt;
                        } else {
                            // Time field can be null (BaseCamp GPX generation)
                            DT_Deco = LocalDateTime.of(2000, 1, 1, 0, 0, 0);                                   
                            // For SQLIte
                            Date_Vol_SQL = "2000-01-01 ";
                        }
                        // flight date                      
                        Date_Vol = DT_Deco;
                        sDate_Vol = Date_Vol.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                        if (!avecPoints) {
                            LocalDateTime iniDate_Vol = Date_Vol;
                            // timezone computing
                            tzVol = tzCalcul(Point1);
                            if (tzVol.getID() != null)  {
                                ZonedDateTime utcZDT = Point1.dHeure.atZone(ZoneId.of("Etc/UTC"));
                                DT_Deco = LocalDateTime.ofInstant(utcZDT.toInstant(), ZoneId.of(tzVol.getID()));                                         
                            } else  {
                                // no timezone, we keep UTC time
                                DT_Deco = Point1.dHeure;
                            }
                            // Update UTC offset
                            majParamUTC(DT_Deco);
                            DateTimeFormatter formatterSQL = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");                                        
                            Date_Vol_SQL = DT_Deco.format(formatterSQL); 
                            // update of day date if necessary (Australia, NZ)
                            Date_Vol = DT_Deco;
                            sDate_Vol = Date_Vol.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));

                            // take off  paremeters update
                            LatDeco = Point1.Latitude;
                            LongDeco = Point1.Longitude;
                            Alt_Deco_Baro = Point1.AltiBaro;
                            Alt_Deco_GPS = Point1.AltiGPS;
                            Decodage = true;                                    
                        }
                    } else {
                        pointIGC PtPcdt = Tb_Tot_Points.get(TotPoint - 1);                                 
                        // parameters computing with previous point
                        // Totpoint = 1 more than last array index
                        Point1.setDistPtPcdt(trigo.CoordDistance(Point1.Latitude,Point1.Longitude,PtPcdt.Latitude,PtPcdt.Longitude));
                        if (Point1.dHeure == null) {
                            // no timestamp  -> Basecamp generation
                            MissTime = true;
                            if (Tb_Tot_Points.get(0).dHeure != null) {
                                Point1.dHeure = Tb_Tot_Points.get(0).dHeure;
                            } else {
                                Point1.dHeure = LocalDateTime.of(2000, 1, 1, 0, 0, 0);
                            }
                        }
                        long DiffSec = ChronoUnit.SECONDS.between(PtPcdt.dHeure, Point1.dHeure);
                        Point1.setPeriodePtPcdt((int)DiffSec);
                        // to avoid a division by zero
                        if (Point1.PeriodePtPcdt > 0) {
                            Point1.setVitesse(Point1.DistPtPcdt / Point1.PeriodePtPcdt * 3.6);                                    
                        } else  {
                            Point1.setVitesse(0);
                        }   
                    }                                                                                        
                    Tb_Tot_Points.add(Point1);              
                    TotPoint++;
                    nbPoint++;
                    if (!avecPoints && nbWp == 0) {
                      lastWp = lWayp.get(lWayp.size() -1);
                      break;
                    }
                    nbWp++;
                }
                // compute duration with end point  
            }            
            if (!avecPoints)  {                
                pointIGC Point1 = new pointIGC();
                Point1.setComment("");
                Point1.setLatitude(lastWp.getLatitude().doubleValue());
                Point1.setLongitude(lastWp.getLongitude().doubleValue());
                Point1.setAltiGPS(lastWp.getElevation().get().intValue());
                if (lastWp.getTime() != null) {    
                     ldt = lastWp.getTime().get().toLocalDateTime();    
                } else {
                    ldt = null;
                }
                Point1.setLdtHeure(ldt); 
                long decUTC = (long) (utcOffset*3600);
                DT_Attero = Point1.dHeure.plusSeconds(decUTC);
                // altitude landing recorded            
                // it's possible to compute flight duration
                Duree_Vol = Duration.between(DT_Deco,DT_Attero).getSeconds();
                // compute average period between two points            
                LocalTime TotSecondes = LocalTime.ofSecondOfDay(Duree_Vol);
                sDuree_Vol = TotSecondes.getHour()+"h"+TotSecondes.getMinute()+"mn";
                colDureeVol = String.format("%02d", TotSecondes.getHour())+":"+String.format("%02d", TotSecondes.getMinute())+":"+String.format("%02d", TotSecondes.getSecond());

                Alt_Attero_Baro = Point1.AltiBaro;
                Alt_Attero_GPS = Point1.AltiGPS;   
                NbPoints = totWp;                
            } else {
                if (TotPoint > 5)  {
                    // time shift to local time for all points
                    utcToLocalDecalage();
                    if (MissTime == true) {
                        Verif_Tb_Tot_Points(MissTime, true);     //  outliers taken into account
                    } else {
                        Verif_Tb_Tot_Points(MissTime, false);     // outliers are not taken into account
                    } 
                    if (Tb_Good_Points.size() > 1)  {
                        pointIGC LastPoint = Tb_Good_Points.get(Tb_Good_Points.size() - 1);                                        
                        DT_Attero = LastPoint.dHeure;
                        // altitude landing recorded
                        Alt_Attero_Baro = LastPoint.AltiBaro;
                        Alt_Attero_GPS = LastPoint.AltiGPS;
                        // it's possible to compute flight duration
                        Duree_Vol = Duration.between(DT_Deco,DT_Attero).getSeconds();
                        // compute average period between two points
                        int AvgPeriode = (int) (Duree_Vol / Tb_Good_Points.size());
                        if (AvgPeriode < 1) AvgPeriode = 1;
                        LocalTime TotSecondes = LocalTime.ofSecondOfDay(Duree_Vol);
                        sDuree_Vol = TotSecondes.getHour()+"h"+TotSecondes.getMinute()+"mn";
                        colDureeVol = String.format("%02d", TotSecondes.getHour())+":"+String.format("%02d", TotSecondes.getMinute())+":"+String.format("%02d", TotSecondes.getSecond());
                        NbPoints = TotPoint;
                        Signature = "";
                        // in xLogfly we recalculated an average speed
                        // it seems unnecessary an average is computed in Verif_Tb_Tot_Points with an integration parameter

                        // Reduce number of points for scoring
                        fillTb_Calcul();

                        // compute thermals points
                        calc_Thermiques();
                        
                        encodeIGC();

                        Decodage = true;
                    }                
                }             
            }  // end of if size > 0
        } catch (Exception e) {
            sbError = new StringBuilder(this.getClass().getName()+"."+Thread.currentThread().getStackTrace()[1].getMethodName());
            sbError.append("\r\n").append(e.getMessage());
            sbError.append("\r\n").append("Path : ").append(pathFichier);
            mylogging.log(Level.SEVERE, sbError.toString());        
        }     
    }
    
               
    /**
    * Thermal points computing
    * adapted from a php script [Emmanuel Chabani [Man's] and P.O. Gueneguo (Parawing.net)]
    */
    private void calc_Thermiques()   {
        
        double MeanVarioMinValue = 0;
        double MeanVarioMaxValue = 0;
        int localAltMin = 0;
        int localAltMax = 0;
        int LastChange = 0;
        String direction = null;
        int switchlevel = 50;  // This value is the height value deciding if we're in or out of a thermal
        int DeltaAltMin;
        int DeltaAltMax;
        int deltaAlt=0;
        double TotalDistWithoutThermal = 0;
        double MeanVarioMin = 0;
        double MeanVarioMax = 0;
        double bestGlideRatioValue = 0;
        int bestGlideRatio1;
        int bestGlideRatio2;
        double bestTransitionValue = 0;
        int bestTransition1=0;
        int bestTransition2=0;
        int bestGainValue=0;
        int bestGain1=0;
        int bestGain2=0;
        int minAlt=0;
        int high;
        int low;
        double deltaDist = 0;
        int deltaTime;
        double MeanvarioValue = 0;
        double meanvario = 0;
        double GlideRatioValue = 0;
        int fin = Tb_Good_Points.size();

        Th_DeltaAltMax = 0;
        Th_DeltaAltMini = 0;
  
        for (int i = 0; i < fin; i++) {
            // for thermal computing, we take baro altitude 
            pointIGC currPoint = Tb_Good_Points.get(i);
            pointIGC pointMax = Tb_Good_Points.get(localAltMax);
            pointIGC pointMin = Tb_Good_Points.get(localAltMin);
            if (currPoint.AltiBaro > 0) {                
                if (currPoint.AltiBaro > pointMax.AltiBaro) localAltMax = i;
                if (currPoint.AltiBaro < pointMin.AltiBaro) localAltMin = i;
                DeltaAltMin = currPoint.AltiBaro - pointMin.AltiBaro;
                DeltaAltMax = pointMax.AltiBaro - currPoint.AltiBaro;
            }  else  {
                if (currPoint.AltiGPS > pointMax.AltiGPS) localAltMax = i;
                if (currPoint.AltiGPS < pointMin.AltiGPS) localAltMin = i;
                DeltaAltMin = currPoint.AltiGPS - pointMin.AltiGPS;
                DeltaAltMax = pointMax.AltiGPS - currPoint.AltiGPS;
            }
            if (((direction != "up" && DeltaAltMin > switchlevel) || (direction != "down" && DeltaAltMax > switchlevel))) {
                high = i;
                low = LastChange;
                pointIGC highPoint = Tb_Good_Points.get(high);
                pointIGC lowPoint = Tb_Good_Points.get(low);
                if (currPoint.AltiBaro > 0)  {
                    deltaAlt = highPoint.AltiBaro - lowPoint.AltiBaro;
                } else  {
                    deltaAlt = highPoint.AltiGPS - lowPoint.AltiGPS;
                }
                if (deltaAlt == 0) deltaAlt = 1;
                deltaDist = geoutils.trigo.CoordDistance(highPoint.Latitude,highPoint.Longitude,lowPoint.Latitude,lowPoint.Longitude);
                TotalDistWithoutThermal = TotalDistWithoutThermal + deltaDist / 1000;
                deltaTime = highPoint.Periode - lowPoint.Periode;
                if (deltaTime == 0) {
                    deltaTime = 1;
                }
                // special Java -> division result of two integers is an integer. We must cast in double                
                MeanvarioValue = (double) deltaAlt / deltaTime;
                if (MeanvarioValue < MeanVarioMinValue) {
                    MeanVarioMinValue = MeanvarioValue;
                    MeanVarioMin = i;
                }
                if (meanvario > MeanVarioMaxValue) {     // WARNING Change original code -> if ($meanvario!!!!>$meanvariomaxValue){
                    MeanVarioMaxValue = MeanvarioValue;
                    MeanVarioMax = i;
                }
                GlideRatioValue = -deltaDist / deltaAlt;
                if (deltaAlt < 0) {
                    direction = "down";
                }  else  {
                    direction = "up";
                }
                if (GlideRatioValue > bestGlideRatioValue && GlideRatioValue < 50) {    // GR>50 is quite unrealistic !
                    bestGlideRatioValue = GlideRatioValue;
                    bestGlideRatio1 = low;
                    bestGlideRatio2 = high;
                }
                if (deltaDist > bestTransitionValue)  {
                    bestTransitionValue = deltaDist;
                    bestTransition1 = low;
                    bestTransition2 = high;
                }
                if (deltaAlt > bestGainValue) {
                    bestGainValue = deltaAlt;
                    bestGain1 = low;
                    bestGain2 = high;
                }
                pointMin = Tb_Good_Points.get(localAltMin);
                pointIGC pointMinAlt = Tb_Good_Points.get(minAlt);
                if (pointMin.AltiBaro > 0) {
                    if (pointMin.AltiBaro < pointMinAlt.AltiBaro) minAlt = localAltMin;
                } else  {
                    if (pointMin.AltiGPS < pointMinAlt.AltiGPS) minAlt = localAltMin;
                }
                /* -----------------------------------------------------------------
                *    ATTENTION   J'ai sauté ces lignes dont je ne comprends pas le sens
                *    mais elles ne me paraissent pas avoir une importance mathematique
                *    if ($deltaAlt>0) $deltaAlt="+".$deltaAlt;
                *    if ($meanvarioValue>0) $meanvarioValue="+".$meanvarioValue;
                * ----------------------------------------------------------------- */
                localAltMax = i;
                localAltMin = i;
                LastChange = i;
                // Ajouté en version Xojo
                if (deltaAlt < Th_DeltaAltMini) Th_DeltaAltMini = deltaAlt;
                if (deltaAlt > Th_DeltaAltMax) Th_DeltaAltMax = deltaAlt;
                // this point is considered like a "thermal point"
                thermique Thermal1 = new thermique();
                Thermal1.NumPoint = i;
                Thermal1.GlideRatioValue = GlideRatioValue;
                Thermal1.MeanVarioValue = MeanvarioValue;
                Thermal1.d_DeltaDist = deltaDist / 1000;
                Thermal1.DeltaAlt = deltaAlt;
                Tb_Thermique.add(Thermal1);                
            }
            bestTransDist = bestTransitionValue;
            bestTransIndice1 = bestTransition1;
            bestTransIndice2 = bestTransition2;
            bestGain = bestGainValue;
            bestGainIndice1 = bestGain1;
            bestGainIndice2 = bestGain2;                       
        }
        
    }
    
    /**
     * Library jpx-1.3.0 used from https://github.com/jenetics/jpx
     * Javadoc http://www.javadoc.io/doc/io.jenetics/jpx
     * @return 
     */
    public int encodeGPX() {
        int res = 2;
        majParamUTC(Tb_Good_Points.get(0).dHeure);
        long decUTC = (long) (utcOffset*3600);   
        
        try {
            List<WayPoint> gpxWpList = new ArrayList<WayPoint>();
            for (int i = 0; i < Tb_Good_Points.size(); i++) {
                double dLat = Tb_Good_Points.get(i).Latitude;
                double dLong =Tb_Good_Points.get(i).Longitude;
                double dAlt = (double) (Tb_Good_Points.get(i).AltiGPS);              
                LocalDateTime ldt = Tb_Good_Points.get(i).dHeure.minusSeconds(decUTC);  // UTC offset is removed
                ZonedDateTime zdt = ldt.atZone(ZoneOffset.UTC);
                long millis = zdt.toInstant().toEpochMilli();
                WayPoint point = WayPoint.builder().lat(dLat).lon(dLong).ele(dAlt).time(millis).build();
                gpxWpList.add(point);          
            } 

            TrackSegment tseg = TrackSegment.of(gpxWpList);
            final Track track = Track.builder().name(sPilote).desc(sVoile).addSegment(tseg).build();                
            final GPX outGpx = GPX.builder().addTrack(track).build();

            FicGPX = GPX.writer().toString(outGpx);
         //   io.jenetics.jpx.GPX.write(outGpx, pPath);
            res = 0;
        } catch (Exception e) {
            sbError = new StringBuilder(this.getClass().getName()+"."+Thread.currentThread().getStackTrace()[1].getMethodName());
            sbError.append("\r\n").append(e.getMessage());
            mylogging.log(Level.SEVERE, sbError.toString());              
        }   
        
        return res;
    }
    
    public void encodeIGC() {
        StringBuilder sbIGC = new StringBuilder();
        String igc_Lat;
        String igc_Long;
        String igc_Time;

        // Header
        sbIGC.append("AXLF").append(CrLf);
        DateTimeFormatter fHDTE = DateTimeFormatter.ofPattern("YYMMdd");
        sbIGC.append("HFDTE").append(Date_Vol.format(fHDTE)).append(CrLf);  
        // By default, we put the pilot name and glider name stored in settings
        // for external tracks we overhide it...
        sbIGC.append("HFPLTPILOT:").append(sPilote).append(CrLf);        
        sbIGC.append("HFGTYGLIDERTYPE:").append(sVoile).append(CrLf); 
        sbIGC.append("HFGIDGLIDERID:").append(CrLf); 
        sbIGC.append("HODTM100GPSDATUM: WGS-84").append(CrLf); 
        sbIGC.append("HOCIDCOMPETITIONID:").append(CrLf); 
        sbIGC.append("HOCCLCOMPETITION CLASS:").append(CrLf); 
        sbIGC.append("HOSITSite:").append(CrLf); 
        
        
        majParamUTC(Tb_Good_Points.get(0).dHeure);
        long decUTC = (long) (utcOffset*3600);
        DateTimeFormatter dtfTime = DateTimeFormatter.ofPattern("HHmmss");
                
        for (int i = 0; i < Tb_Good_Points.size(); i++) {                      
            igc_Lat = Lat_Dd_IGC(Tb_Good_Points.get(i).Latitude);
            igc_Long = Long_Dd_IGC(Tb_Good_Points.get(i).Longitude);
            igc_Time = Tb_Good_Points.get(i).dHeure.minusSeconds(decUTC).format(dtfTime);   // UTC offset is removed
            sbIGC.append("B").append(igc_Time).append(igc_Lat).append(igc_Long);
            sbIGC.append("A00000").append(String.format("%05d",Tb_Good_Points.get(i).AltiGPS)).append(CrLf);            
        }
        
        // footer
        sbIGC.append("LXLF Logfly 5").append(CrLf);
        Date genDate = new Date();  
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-YY HH:mm:ss");
        sbIGC.append("LXLF generated ").append(sdf.format(genDate)).append(CrLf);
        FicIGC = sbIGC.toString();        
    }
    
    /**
     * la trace est exportée avec un nom de la forme YYYYMMDDHHMMSS_Aleatoire
     * Cet export est nécessaire pour l'upload de la trace à destination de VisuGPS
     * @return 
     */
    public String exportVisu()  {
        String res = null;
        
        LocalDateTime ldt = LocalDateTime.now();
        String sLdt = ldt.format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss_"));    
        // On veut un nombre aleatoire entre 1 et 1000
        // Min + (int)(Math.random() * ((Max - Min) + 1))
        // glané sur http://stackoverflow.com/questions/363681/generating-random-integers-in-a-specific-range
        int aleaNumber = 1 + (int)(Math.random() * ((1000 - 1) + 1));
        StringBuilder suggName = new StringBuilder();
        suggName.append(sLdt).append(String.format("%d",aleaNumber));
        
        try{

    	    //create a temp file
    	    File temp = File.createTempFile("tempfile", ".igc");

	    //write it
    	    BufferedWriter bw = new BufferedWriter(new FileWriter(temp));
    	    bw.write(FicIGC);
    	    bw.close();  

            res = temp.getAbsolutePath();
            System.out.println(res);

    	}catch(IOException e){
            sbError = new StringBuilder(this.getClass().getName()+"."+Thread.currentThread().getStackTrace()[1].getMethodName());
            sbError.append("\r\n").append(e.getMessage());
            mylogging.log(Level.SEVERE, sbError.toString());    	    
    	}
        
        return res;
    }
    
    /**
     * IGC file is converted in an array of bytes
     * @return 
     */
    public byte[] exportBytes()  {
        
        byte[] txtData = FicIGC.getBytes(StandardCharsets.UTF_8);
        
        return txtData;    
    }
    
    public String suggestName() {        
         
        String suggName1 = Date_Vol_SQL.replaceAll("-","_");
        String suggName2 = suggName1.replaceAll(":","_");
        String suggName = suggName2.replaceAll(" ","_");
        
        String suggPilote;
        String finalName = null;
        if (sPilote != null && !sPilote.equals(""))  {
            // Il y a un problème s'il y a un point dans l'expression comme Gégé avait fait -> sPilote = G. LEGRAS...        
            String suggPilote1 = sPilote.replaceAll("\\.","_");
            suggPilote = suggPilote1.replaceAll(" ","_");  
            if (suggPilote.equals("-")) 
                finalName = suggName;
            else
                finalName = suggPilote+"_"+suggName; 
        } else {
            suggPilote = "";
            finalName = suggName; 
        }            
  
        return finalName;        
    }

}
