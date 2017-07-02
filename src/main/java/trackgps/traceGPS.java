/*
 * Copyright Gil THOMAS
 * Ce fichier fait partie intégrante du projet Logfly
 * Pour tous les détails sur la licence du projet Logfly
 * Consulter le fichier LICENSE distribué avec le code source
 */
package trackgps;


import gapchenko.llttz.Converter;
import gapchenko.llttz.IConverter;
import gapchenko.llttz.stores.TimeZoneListStore;
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
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.TimeZone;
import systemio.textio;

public class traceGPS {
    // La plupart des variables ont gardés leur nom xLogfly, seules les introductions respectent les conventions Java
    private static final int APP_INTEGRATION = 15;
    
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
    
    public List<pointIGC> Tb_Tot_Points = new ArrayList<pointIGC>();
    public List<pointIGC> Tb_Good_Points = new ArrayList<pointIGC>();
    public List<pointIGC> Tb_Calcul = new ArrayList<pointIGC>();
    public List<thermique> Tb_Thermique = new ArrayList<thermique>();
    public ArrayList<Integer> Score_Tb_Balises = new ArrayList<Integer>();
    
    public traceGPS(File pFile, String pType, Boolean totalPoints)
    {
        Decodage = false;
        Scored = false;
        avecPoints = totalPoints;
        textio fread = new textio();                                    
        String pFichier = fread.readTxt(pFile);
        if (pFichier != null && !pFichier.isEmpty())  {
            if(pType == "IGC")
            {
                FicIGC = pFichier;
                Origine = "IGC";
                Decode_IGC();
            }
            else
            {
                FicGPX = pFichier;
                Origine = "GPX";
            }     
            pathFichier = pFile.getAbsolutePath();
        }
    } 
    
    /**
     * Le contructeur peut prendre deux formes :
     *   - on passe la string contenant la trace
     *   - on passe le chemin du fichier pour lecture avant décodage
     * @param pFichier
     * @param pType
     * @param pPath
     * @param totalPoints 
     */
    public traceGPS(String pFichier, String pType, String pPath, Boolean totalPoints)
    {
        Decodage = false;
        Scored = false;
        avecPoints = totalPoints;
        switch (pType) {
            case "IGC":
                FicIGC = pFichier;
                Origine = "IGC";
                Decode_IGC();
                break;
            case "GPX":
                FicGPX = pFichier;
                Origine = "GPX";
                break;
            case "NIL":
                Origine = "NIL";    
                break;                    
        }       
        pathFichier = pPath; 
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
     * S'il y a un seul caractère non numérique, Integer.parseInt renvoie une exception
     * On gère l'exception en renvoyant une valeur par défaut
     * Issu d'une longue discussion sur http://stackoverflow.com/questions/1486077/java-good-way-to-encapsulate-integer-parseint
     * @param number
     * @param defaultVal
     * @return 
     */
    private static int checkParseInt(String number, int defaultVal) {
        // On élimine les espaces éventuels avec une regex
        number = number.replaceAll("\\s+","");
        try {
            return Integer.parseInt(number);
        } catch (NumberFormatException e) {
            return defaultVal;
        }
    }
    
    /**
     * S'il y a un seul caractère non numérique, Integer.parseInt renvoie une exception
     * On gère l'exception en renvoyant une valeur par défaut
     * Issu d'une longue discussion sur http://stackoverflow.com/questions/1486077/java-good-way-to-encapsulate-integer-parseint
     * @param number
     * @param defaultVal
     * @return 
     */
    private static double checkParseDouble(String number, double defaultVal) {
        // On élimine les espaces éventuels avec une regex
        number = number.replaceAll("\\s+","");
        try {
            return Double.parseDouble(number);
        } catch (NumberFormatException e) {
            return defaultVal;
        }
    }
                   
    /**
     * Decodage partiel ou total du fichier IGC
     * Si avecPoints est à faux, on s'arrête au premier point de la trace
     * Ce premier point n'est pas forcément le plus pertinent, car le GPS peut ne pas être calé
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
        sSite = "";
        sVoile = "";
        sPilote = "";
        
        // Pour les détails sur les PEV c'est page 44 de la doc FAI
        // En résumé
        // The form of the E-Record is record identifer, time, TLC, textstring. Some examples follow, with extra spaces for clarity:
        // E 104533 PEV CR LF
        // B104533 4945333N 01132444EA 01357 01501CRLF
        // This indicates a pilot initiated event (PEV) at 10:45:33 UTC, and the associated B record shows the location 49:45.333 N 11:32.444 E, etc...
        // Some events require more than just the TLC for interpretation (with extra spaces for clarity):
        // E 104544 ATS 102312 CR LF
        flag_PEV = false;
        // On élimine le fameux caractère DC3 Device control 3 envoyé par le XON qui posait problème sur la validation du fichier IGC
        // En effet ce caractère non valable et surtout non visible faisiat échouer la validation sur le site de la CFD
        String BadCar = Character.toString((char)19);
        FicIGC = FicIGC.replace(BadCar,"");       
        // De la même façon on élimine le DC1 situé à la fin du fichier (le XOFF)
        BadCar = Character.toString((char)17);
        FicIGC = FicIGC.replace(BadCar,"");    
        
        // On transforme la variable en un tableau
        // Au départ je splitait sur EndOfLine.Windows soir chr(13)+chr(10)
        // sLine = ficIGC.Split(EndOfLine.Windows)  ' IMPORTANT de spécifier Retour Chariot Windows
        // En examinant la trace envoyée par un internaute, il n'y avait que du chr(10) donc on adopte un split sur ce caractère
        BadCar = Character.toString((char)10);
        String[] sLine = FicIGC.split(BadCar);
        int Lg_sLine =  sLine.length;
        
        // mais depuis j'ai eu un nouveau problème il n'y avait que du chr(13), donc on refait une tentative avec celui-ci
        if (Lg_sLine == 1) {
            BadCar = Character.toString((char)13);
            sLine = FicIGC.split(BadCar);
            Lg_sLine = sLine.length;
        }
        
        // Sur le 6020 il y a trois rec G + une ligne vide
        // Sur GPSDump, il y en a un seul + 1 ligne vide
        // il y au moins trois enreg G et parfois une ligne vide en dernier, on prend donc l'avant avant dernière ligne
        // Est-ce qu'on a une trace valide ?
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
            // Plantage lecture ou pas de record G à se mettre sous la dent
            for (int k = 0; k < Lg_sLine; k++) {
                DebChar = sLine[k].substring(0,1);
                if (DebChar.equals("B") && sLine[k].length() > 23 ) {
                    Valid_Trace = true;
                    break;
                }
            }
      
        }
        
        // Pour les substring, the begin index is inclusive but the end index isexclusive.
        if (Valid_Trace) {
            LignesNonB = 0;
            long topDebut = System.currentTimeMillis();
            for (int i = 0; i < Lg_sLine; i++) {
                if (!sLine[i].trim().equals("")) {
                    DebChar = sLine[i].substring(0,1);
                    // Décodage de la signature
                    if (DebChar.equals("A") && sLine[i].length() > 3) {
                        Signature = sLine[i].substring(1,4);
                    }          
                    if (!DebChar.equals("B")) {
                        if (DebChar.equals("E") && sLine[i].indexOf("PEV")> -1)
                            flag_PEV = true;
                        // On compte les lignes ne commençant pas par B ni par E
                        LignesNonB++;
                        // la ligne ne commence pas par B ce n'est donc pas une ligne de position standard
                        // Apparemment je reçois parfois une trame avec un cr lf mal placé... ( trace Gérard)
                        // B1541104553226N00627642EA0221002345000<CR> et 5562 <LF> incroyable non ?
                        // Donc on n'exploitera que si la ligne fait plus de 11 caractères car le découpage
                        // d'une chaine qui arriverait plus courte (mauvaise récup) provoque une exception
                        if (sLine[i].length() > 10) {
                            DebMot = sLine[i].substring(0,5);
                            switch (DebMot)
                            {
                              case "HFDTE": 
                                // il peut arriver que l'IGC soit tronquée (mauvaise récup) dans ce cas la date ne sera pas intialisée correctement
                                // Petit nouveauté 2016 -> de HFDTE060910 on aura HFDTEDATE:070816,01
                                // où le nombre après la virgule représente le numéro du vol du jour...
                                // du coup on passe au regex car la présence des deux points ne sera pas constante
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
                                  // Decodage du nom du pilote, il peut y avoir moins de 17 caractères
                                  String[] LineHFPLT = sLine[i].split(":");
                                  if (LineHFPLT.length > 1) sPilote = LineHFPLT[1];
                                  GoodName = "";
                                  // Un accent apparaissant comme un signe cabalistique dans un nom (Daphné ->Daphn@)
                                  // Cela plantait l'affichage des détails dans la listbox d'où l'élimination
                                  // j'ai la flemme de faire une procédure complète de remplacement par le bon signe
                                  // Pallie également la présence d'un caractère de retour chariot laissé par le split
                                  for (int kk = 0 ; kk < sPilote.length() ; kk++) {
                                        char aChar = sPilote.charAt(kk);                                      
                                        if ((int)aChar > 31 && (int)aChar < 122) GoodName = GoodName.concat(sPilote.substring(kk,kk+1));
                                  }
                                  sPilote = GoodName;
                                break;
                                case "HOPLT":
                                  // Ds GPSDump on a HOPLTPILOT: G. Legras au lieu de HFPLTPILOT:G. Legras
                                  String[] LineHOPLT = sLine[i].split(":");
                                  if (LineHOPLT .length > 1) sPilote = LineHOPLT[1];
                                  GoodName = "";
                                  for (int kk = 0 ; kk < sPilote.length() ; kk++) {
                                        char aChar = sPilote.charAt(kk);                                      
                                        if ((int)aChar > 31 && (int)aChar < 122) GoodName = GoodName.concat(sPilote.substring(kk,kk+1));
                                  }
                                  sPilote = GoodName;
                                break;
                                case "HFGTY":
                                  // Decodage du nom de la voile ou de l'appareil
                                  String[] LineHFGTY = sLine[i].split(":");
                                  if (LineHFGTY.length > 1) sVoile = LineHFGTY[1];  
                                  GoodName = "";
                                  // Toujours le problème d'un retour chariot possible
                                  for (int kk = 0 ; kk < sVoile.length() ; kk++) {
                                        char aChar = sVoile.charAt(kk);                                      
                                        if ((int)aChar > 31 && (int)aChar < 122) GoodName = GoodName.concat(sVoile.substring(kk,kk+1));
                                  }
                                  sVoile = GoodName;
                                break;
                                case "HOGTY":
                                  // ' Ds GPSDump on a HOGTYGLIDERTYPE: None
                                  // Decodage du nom de la voile ou de l'appareil
                                  String[] LineHOGTY = sLine[i].split(":");
                                  if (LineHOGTY.length > 1) sVoile = LineHOGTY[1];
                                  GoodName = "";
                                  // Toujours le problème d'un retour chariot possible
                                  for (int kk = 0 ; kk < sVoile.length() ; kk++) {
                                        char aChar = sVoile.charAt(kk);                                      
                                        if ((int)aChar > 31 && (int)aChar < 122) GoodName = GoodName.concat(sVoile.substring(kk,kk+1));
                                  }
                                  sVoile = GoodName;                                  
                                break;
                                case "HFRFW":
                                  // ' Ds GPSDump on a HOGTYGLIDERTYPE: None
                                  // Decodage du nom de la voile ou de l'appareil
                                  String[] LineHFRFW = sLine[i].split(":");
                                  if (LineHFRFW.length > 1) sFirmware = LineHFRFW[1];    
                                  GoodName = "";
                                  // Toujours le problème d'un retour chariot possible
                                  for (int kk = 0 ; kk < sVoile.length() ; kk++) {
                                        char aChar = sVoile.charAt(kk);                                      
                                        if ((int)aChar > 31 && (int)aChar < 122) GoodName = GoodName.concat(sVoile.substring(kk,kk+1));
                                  }
                                  sVoile = GoodName;
                                break;
                               
                            }
                        }
                    }
                    else
                    {
                        // Décodage des lignes de points donc commençant par B
                        // TAS > 9 donc vitesse... 3 derniers octets du record B ds le fichier IGC
                        //  L'octet doit être à A pour signifier acquisition en 3D dc alti valide
                        //  On a eu une trace où TAS reste à 0 dc on ne s'appuie plus que sur l'octet à A 
                        //   Verifie qu'il y a tous les caractères pour sortir la position et les altis
                        //   Il faut qu'on ait au minimum 35 caractères pour parser la ligne sans encombres
                        if (sLine[i].length() > 34) {
                            if (sLine[i].length() > 24)
                                DebMot = sLine[i].substring(24,25);
                            else
                                DebMot = "";
                            Alti_Baro = 0;
                            Alti_GPS = 0;
                            if (DebMot.equals("V"))  {
                                if (sLine[i].length() > 30)
                                    // Acquisition en 2D seule l'alti baro est valide
                                    Alti_Baro = checkParseInt(sLine[i].substring(25,30),0);
                                else
                                    Alti_Baro = 0;              
                            }
                            if (DebMot.equals("A"))  {
                                if (sLine[i].length() > 30)
                                    // Acquisition en 2D seule l'alti baro est valide
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
                            if (Point1.AltiGPS <= 0) Point1.setComment("ZERO");
                            // Comparaison des altis baro et GPS
                            if (Point1.AltiBaro > 0 && Point1.AltiGPS > 0 && Math.abs(Point1.AltiGPS -  Point1.AltiBaro) > 500) Point1.setComment("BAD");
                            // Cf trace 02/08/11 où alti GPS = 46500...
                            if (Point1.AltiGPS > BadAlti) Point1.setComment("BAD");
                            //Vitesse air
                            if (sLine[i].length() > 37) Point1.setTAS(checkParseInt(sLine[i].substring(35,38),0));                                                                                       
                            // Traitement de la latitude
                            Deg = checkParseInt(sLine[i].substring(7,9),0);
                            Min = checkParseDouble(sLine[i].substring(9,11),0);
                            Sec = (checkParseDouble(sLine[i].substring(11,14),0)/1000) * 60;                           
                            Point1.setLatitudeDMS(Deg, Min, Sec, sLine[i].substring(14,15));
                            // On enregistre en secondes pour évaluation Score
                            Min = checkParseDouble(sLine[i].substring(9,14),0);
                            Point1.setLatitudeSec(Deg,(int)Min,sLine[i].substring(14,15)); 
                            
                            // Traitement de la longitude
                            Deg = checkParseInt(sLine[i].substring(15,18),0);
                            Min = checkParseDouble(sLine[i].substring(18,20),0);
                            Sec = (checkParseDouble(sLine[i].substring(20,23),0)/1000) * 60;
                            Point1.setLongitudeDMS(Deg, Min, Sec, sLine[i].substring(23,24));
                            // On enregistre en secondes pour évaluation Score
                            Min = checkParseDouble(sLine[i].substring(18,23),0);
                            Point1.setLongitudeSec(Deg,(int)Min,sLine[i].substring(23,24));
                            // Calcul de la période en secondes (CFD)                            
                            Point1.setPeriode(checkParseInt(sLine[i].substring(1,3),0)*3600+checkParseInt(sLine[i].substring(3,5),0)*60+checkParseInt(sLine[i].substring(5,7),0));                            
                            // Calcul de l'heure en format date uniquement si Date_Vol a bien été initialisée
                            if (Decodage_HFDTE) {
                                Point1.setdHeure(Date_Vol,checkParseInt(sLine[i].substring(1,3),0), checkParseInt(sLine[i].substring(3,5),0), checkParseInt(sLine[i].substring(5,7),0));            
                            }
                            
                            if (i-LignesNonB == 0) {   
                                // Premier point de la trace IGC
                                if (!Decodage_HFDTE)  {
                                    // Histoire de la trace SeeYou qui ne met pas de date, pas d'enregsitrement HFDTE en conversion GPX -> IGC...
                                    // Enorme... du coup on plantait Pour éviter cela on initialise au 01 01 2000
                                    Date_Vol = LocalDateTime.of(2000, 1, 1, 0, 0, 0); 
                                    sDate_Vol = Date_Vol.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                                    // Encodage de la date pour SQLite
                                    Date_Vol_SQL = "2000-01-01 ";
                                    Decodage_HFDTE = true;
                                    // Dans ce cas Point1.dHeure n'avait pas été initialisé...
                                    Point1.setdHeure(Date_Vol,checkParseInt(sLine[i].substring(1,3),0), checkParseInt(sLine[i].substring(3,5),0), checkParseInt(sLine[i].substring(5,7),0));                            
                                }
                                // Decodage sans les points, on ne veut que l'en tête 
                                if (!avecPoints) {
                                    // Détermination de la TimeZone du vol
                                    tzVol = tzCalcul(Point1);
                                    if (tzVol.getID() != null)  {
                                        ZonedDateTime utcZDT = Point1.dHeure.atZone(ZoneId.of("Etc/UTC"));
                                        DT_Deco = LocalDateTime.ofInstant(utcZDT.toInstant(), ZoneId.of(tzVol.getID()));                                         
                                    } else  {
                                        // Pas de zone UTC epxloitable, on reste en heure UTC                                        
                                        DT_Deco = Point1.dHeure;
                                    }
                                    // Mise à jour du décalage UTC
                                    majParamUTC(DT_Deco);
                                    DateTimeFormatter formatterSQL = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");                                        
                                    Date_Vol_SQL = DT_Deco.format(formatterSQL); 
                                    // On actualise pour les vols réalisés près des changements de date (Australie, NZ)
                                    Date_Vol = DT_Deco;
                                    sDate_Vol = Date_Vol.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                                        
                                    // Mise à jour des coordonnées
                                    LatDeco = Point1.Latitude;
                                    LongDeco = Point1.Longitude;
                                    Alt_Deco_Baro = Point1.AltiBaro;
                                    Alt_Deco_GPS = Point1.AltiGPS;
                                    Decodage = true;
                                    // On sort du traitement
                                    return;
                                }                                                                        
                            }
                            else  {                                        
                                pointIGC PtPcdt = Tb_Tot_Points.get(TotPoint - 1);                                 
                                // On calcule les paramètres avec le point précédent
                                // Totpoint vaut 1 unité de plus que le dernier indice du tableau                                
                                Point1.setDistPtPcdt(trigo.CoordDistance(Point1.Latitude,Point1.Longitude,PtPcdt.Latitude,PtPcdt.Longitude));
                                long DiffSec = ChronoUnit.SECONDS.between(PtPcdt.dHeure, Point1.dHeure);
                                Point1.setPeriodePtPcdt((int)DiffSec);
                                // Division par zero pas glop              
                                if (Point1.PeriodePtPcdt > 0) {
                                    Point1.setVitesse(Point1.DistPtPcdt / Point1.PeriodePtPcdt * 3.6);
                                    if (flag_PEV)  {
                                        // La ligne précédente indiquait qu'il s'agissait d'une indication de position PEV, on l'enregistre
                                        Point1.setComment("PEV");
                                        flag_PEV = false;
                                     }
                                }
                                else  {
                                    // Le Reversale peut enregistrer plusieurs points à la même seconde...
                                    // Problème : ces doublons introduisaient des erreurs dans les calculs de Max et de moyenne
                                    // puisqu'on divise par exemple la distance avec le nombre de points
                                    Point1.setComment("DOUBLON");
                                }             
                                // Il peut arriver que l'on vole après minuit UTC (?)... problème donc... cghmt de jour pour les calculs Cf trace envoyée depuis les US [Vol_minuit]
                                // On a eu un retour sur une trace Reversale débile ou un point on avait 
                                // Point X 18:38.38     Point X+1 18:38.39         Point X+2 18:38.38
                                // Du coup on vérifie s'il y a bien eu changement d'heure en confimant avec le premier point ce qui merd... si le point aberrant est le deuxième...                                
                                if (Point1.dHeure.isBefore(PtPcdt.dHeure))  {
                                    PtPcdt = Tb_Tot_Points.get(0);
                                    if (Point1.dHeure.isBefore(PtPcdt.dHeure)) {
                                        // il faut incrémenter la date 
                                        Date_Vol.plusDays(1);                       
                                    }
                                }
                                
                                
              
              
                            } 
                           if (!"DOUBLON".equals(Point1.Comment))   {
                                // Gestion des points aberrants              
                                if (Point1.Comment == "ZERO" || Point1.Comment == "DIST" || Point1.Comment == "BAD" || Point1.Comment == "PEV")
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
                // Decalage de tous les points à l'heure réelle du vol
                utcToLocalDecalage();

                // Remplissage de Tb_Good_Points                
                topDebut = System.currentTimeMillis();
                // Le premier False est un paramètre imposé par le décodage des GPX    
                // 2ème paramètre est pour la prise en compte des aberrants
                Verif_Tb_Tot_Points(false, false);
                topFin = System.currentTimeMillis();
                seconds = (topFin - topDebut) / 1000F;
                System.out.println("Verif_Tb_Tot_Points : "+ Float.toString(seconds) + " secondes.");
      
                // On a eu un cas où il y avait zéro good points (mauvais fichier IGC) 
                // Dt_Deco est mis au point dans Verif_Tb_Tot_Points
                // donc on plantait direct sur la durée de vol                
                if (Tb_Good_Points.size() > 1)  {
                    pointIGC LastPoint = Tb_Good_Points.get(Tb_Good_Points.size() - 1);                                        
                    DT_Attero = LastPoint.dHeure;
                    // On enregistre l'altitude de l'attero
                    Alt_Attero_Baro = LastPoint.AltiBaro;
                    Alt_Attero_GPS = LastPoint.AltiGPS;
                    // On peut donc calculer la durée du vol
                    Duree_Vol = Duration.between(DT_Deco,DT_Attero).getSeconds();
                    // Calcul de la période moyenne entre deux points
                    int AvgPeriode = (int) (Duree_Vol / Tb_Good_Points.size());
                    if (AvgPeriode < 1) AvgPeriode = 1;
                    LocalTime TotSecondes = LocalTime.ofSecondOfDay(Duree_Vol);
                    sDuree_Vol = TotSecondes.getHour()+"h"+TotSecondes.getMinute()+"mn";
                    NbPoints = TotPoint;
                    // Dans Xojo on recalculait une vitesse moyenne
                    // A la relecture cela me semble inutile vu que l'on fait déjà une moyenne
                    // dans Verif_Tb_Tot_Points avec un  temps d'intégration paramètrable    
                    
                    // Réduction des points pour le scoring
                    fillTb_Calcul();
                    
                    calc_Thermiques();
                    
                    // On a tout bon ?
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
    
    private void utcToLocalDecalage()  {
        
         // Détermination de la TimeZone du vol
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
    
    private TimeZone tzCalcul(pointIGC p1)  {
        
        // Appel de la librairie llttz d'Artem Gapchenko   https://github.com/agap/llttz
        IConverter iconv = Converter.getInstance(TimeZoneListStore.class);
        TimeZone tzCalc = iconv.getTimeZone(p1.Latitude,p1.Longitude); 
        
        return tzCalc;
    }
    
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
     * Bien que performant, le calcul du score peut prendre un temps très important
     * surtout avec des grosses traces à un point par seconde
     * On génére si besoin une liste de points avec un point tous les 5 secondes
     * cette liste servira à générer un fichier igc temporaire 
     * qui sera analysé par le module externe points
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
        /* Ce calcul spécifique est intervenu après être tombé sur un cas barbare (barbare.igc)
        * le premier point était aberrant, donc le point 2 avait une vitesse énorme donc 
        * c'était le point 2 qui était tagué aberrant
        * Après refonte du calcul dans cette procédure ce point 2 devient le premier de Tb_Good_Points
        * Problème Si on ne remet pas a vitesse à zéro, comme il est tagué OK, cette vitesse énorme devient la Max de référence
        * et entre dans les calculs de moyenne
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
        // On attaque avec le deuxième point
        i = 1;
        TrackLen = 0;
        pointIGC Pcdt2Point = null;
        pointIGC iiPoint = null;
        
        try   {
            while (i < (TotPoints-1))  {            
                pointIGC CurrPoint = Tb_Tot_Points.get(i);
                pointIGC PcdtPoint = Tb_Tot_Points.get(i-1);
                pointIGC NextPoint = Tb_Tot_Points.get(i+1);
                // Avant l'utilisation de HSPoints  je n'avais pas les points "DOUBLON" dans Tb_Tot_Points, depuis, je veux garder tous les points
                if (!"DOUBLON".equals(CurrPoint.Comment))   {
                    if (CurrPoint.PeriodePtPcdt > 0 || MissTime)  {
                        if (CurrPoint.Vitesse > BadVitesse)  {
                            // Comment est le suivant ?
                            if (NextPoint.Vitesse < BadVitesse)  {
                                // Le suivant est correct il faut donc examiner le précédent par rapport au suivant
                                DistVerif = trigo.CoordDistance(NextPoint.Latitude,NextPoint.Longitude,PcdtPoint.Latitude,PcdtPoint.Longitude);
                                if (NextPoint.DistPtPcdt == 0)  {
                                    DistVerifDiv = 1;
                                }
                                else    {
                                    DistVerifDiv = NextPoint.DistPtPcdt;
                                }
                                if (DistVerif/DistVerifDiv > 3) {
                                    // C'est le point i-1 qui est aberrant, on le tague DIST
                                    PcdtPoint.setComment("DIST");
                                    Tb_Tot_Points.set(i-1, PcdtPoint);
                                    // mais du coup la vitesse du point i est fausse sans que le point soit tagué
                                    // il faut donc mettre ces paramètres à zéro pour ne pas fausser les calculs de moyenne
                                    CurrPoint.Vitesse = 0;
                                    CurrPoint.DistPtPcdt = 0;
                                    Tb_Tot_Points.set(i, CurrPoint);              
                                }
                                else   {
                                    // C'est bien le point i qui est aberrant, on le tague BAD
                                    CurrPoint.setComment("DIST");
                                    CurrPoint.Vitesse = 0;
                                    CurrPoint.DistPtPcdt = 0;
                                    Tb_Tot_Points.set(i, CurrPoint);  

                                }
                            }
                        }
                        if (PcdtPoint.Comment.trim() == "" || WithAberrant)   {
                            TotGoodPoints++;
                            // Si c'est le premier point, on enregistre les coord du décollage
                            if (TotGoodPoints == 1)   {                                                                                                                                                  
                                // on enregistre l'heure de décollage
                                DT_Deco = PcdtPoint.dHeure;
                                // Mise à jour du décalage UTC
                                majParamUTC(DT_Deco);
                                DateTimeFormatter formatterSQL = DateTimeFormatter.ofPattern("yyyy-MM-dd ");                                        
                                Date_Vol_SQL = DT_Deco.format(formatterSQL);          
                                // On complète l'Heure de déco pour SQLIte
                                Date_Vol_SQL = Date_Vol_SQL +PcdtPoint.dHeure.format(DateTimeFormatter.ofPattern("HH:mm:ss"));                                    
                                // On complète Date_Vol 
                                Date_Vol = Date_Vol.plusHours(PcdtPoint.dHeure.getHour());
                                Date_Vol = Date_Vol.plusMinutes(PcdtPoint.dHeure.getMinute());
                                Date_Vol = Date_Vol.plusSeconds(PcdtPoint.dHeure.getSecond());            
                                // les coordonnées
                                LatDeco = PcdtPoint.Latitude;
                                LongDeco = PcdtPoint.Longitude;
                                // Altitude
                                Alt_Deco_Baro = PcdtPoint.AltiBaro;
                                Alt_Deco_GPS = PcdtPoint.AltiGPS;
                                AltMaxBaro = PcdtPoint;
                                AltMaxGps = PcdtPoint;
                                AltMiniBaro = PcdtPoint;
                                AltMiniGps = PcdtPoint;
                                VitMax = PcdtPoint;
                                VitMini = PcdtPoint;
                            } else   {

                                // ------------ Debut Calcul Vitesse moyennée -------------------
                                // Calcul d'une vitesse moyennée sur au moins 20 secondes
                                // Si on a un intervalle sup à 20 secondes, on se contente de prendre entre deux points successifs
                                PcdtPoint = Tb_Tot_Points.get(i-1);
                                DeltaTime = PcdtPoint.PeriodePtPcdt;
                                ii = 2;
                                DeltaDist = PcdtPoint.DistPtPcdt;
                                // Calcul longueur brute de la trace                            
                                TrackLen += DeltaDist;
                                if (DeltaTime < APP_INTEGRATION - 1)   {
                                    if ((i-1) - ii > -1) {
                                        do   {   
                                                CurrPoint = Tb_Tot_Points.get((i-1)-ii);
                                                if (CurrPoint.Comment.equals(""))  {                                                
                                                    DeltaTime = Duration.between(CurrPoint.dHeure, PcdtPoint.dHeure).getSeconds();
                                                    // on remonte de deux indices (ii) pour le temps écoulé, 
                                                    // mais la distance doit être prise sur ii -1
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
                                // ------------ Fin Calcul Vitesse moyennée -------------------


                                // Calcul des alti maxi mini
                                if (PcdtPoint.AltiBaro > AltMaxBaro.AltiBaro && PcdtPoint.AltiBaro < BadAlti)
                                    AltMaxBaro = PcdtPoint;
                                if (PcdtPoint.AltiGPS > AltMaxGps.AltiGPS && PcdtPoint.AltiGPS < BadAlti)
                                    AltMaxGps = PcdtPoint;
                                if (PcdtPoint.AltiBaro < AltMiniBaro.AltiBaro && PcdtPoint.AltiBaro > 0)
                                    AltMiniBaro = PcdtPoint;
                                if (PcdtPoint.AltiGPS < AltMiniGps.AltiGPS && PcdtPoint.AltiGPS > 0)
                                    AltMiniGps = PcdtPoint;

                                // Calcul du vario que l'on va moyenner sur 20 secondes
                                // On a un intervalle sup à 20 secondes, on se contente
                                // de prendre entre deux points successifs
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
                                    // Nécessaire... Cas du petit tour du lac avec un 1 point toutes les 15 s
                                    // supérieur au paramètre d'intégration. On obtenait des Vz nulles
                                    // en le mettant à 3, le ii = ii - 1 le fait repasser à 2, la valeur initiale
                                    ii = 3;
                                }
                                //-----------------------------------
                                ii = ii - 1;
                                // De préference on prend l'alti baro, à priori plus sûr pour calculer les VZ
                                if (PcdtPoint.AltiBaro > 0) {
                                    iiPoint = Tb_Tot_Points.get(TotGoodPoints-ii);
                                    Vz = (double)(PcdtPoint.AltiBaro - iiPoint.AltiBaro)/DeltaTime;
                                    // On m'a envoyé une trace avec 500 m d'écart entre les 2 premiers points baro 
                                    // donc il faut tester la valeur de chute libre 
                                    // trouvé sur internet tournait autour de 50 m/s
                                    if (Vz > 20 || Vz < -55) {
                                        // Vz aberrrante, on prend l'alti GPS
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
            System.out.println("ERROR -> Index : "+i);
        }
        /*  
            On s'est arrêté à deux points de la fin, il faut les inclure....
            Ou bien prendre le parti de laisser tomber, les deux derniers points sont au sol...
  
            Cependant si c'est un tracé GPX avec points aberrants et sans base temps,
            on prends le parti d'ajouter les deux points manquants sans les vérifier
        
            En faisant la conversion java, je m'aperçois que 'lon introduit une erreur
            les deux derniers points sont absents même s'ils sont bon
        */
        if (MissTime && WithAberrant) {
            Tb_Good_Points.add(Tb_Tot_Points.get(TotPoints-1));
            Tb_Good_Points.add(Tb_Tot_Points.get(TotPoints));         
        }
        // On met à jour les mini maxi  
        Alt_Maxi_Baro = AltMaxBaro;
        Alt_Maxi_GPS = AltMaxGps;
        Alt_Mini_Baro = AltMiniBaro;
        Alt_Mini_GPS = AltMiniGps;
        Vit_Max = VitMax;
        Vit_Mini = VitMini;
        Vario_Max = VarioMax;
        Vario_Mini = VarioMini;
        TrackLen = TrackLen / 1000;   // Calcul en mètres est converti en km
    }
    
    /* Procedure de calcul adaptée du source php
    * d'Emmanuel Chabani [Man's] et P.O. Gueneguo (Parawing.net)
    * aimablement envoyée par Emmanuel pour xLogfly
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
            // On décide que pour le thermique le calcul ne se fera que sur la variation d'altitude barométrique
            // si elle est naturellement renseignée ...
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
                // Particularité Java -> le résulat est la division de deux entiers donc c'est un entier
                // Pour avoir un double il faut caster !!!
                MeanvarioValue = (double) deltaAlt / deltaTime;
                if (MeanvarioValue < MeanVarioMinValue) {
                    MeanVarioMinValue = MeanvarioValue;
                    MeanVarioMin = i;
                }
                if (meanvario > MeanVarioMaxValue) {     // ATTENTION Changement if ($meanvario!!!!>$meanvariomaxValue){
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
                // Le point est mémorisé comme "Point de Thermique"
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

    	    e.printStackTrace();

    	}
        
        return res;
    }
    
    public byte[] exportBytes()  {
        
        byte[] txtData = FicIGC.getBytes(StandardCharsets.UTF_8);
        
        return txtData;    
    }

}
