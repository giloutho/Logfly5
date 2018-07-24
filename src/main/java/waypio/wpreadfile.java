/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package waypio;

import geoutils.position;
import io.jenetics.jpx.GPX;
import io.jenetics.jpx.WayPoint;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


/**
 *
 * @author gil
 */
public class wpreadfile {
    
    private ArrayList<pointRecord> wpreadList;
    private DecimalFormatSymbols decimalFormatSymbols = new DecimalFormatSymbols();    
    private DecimalFormat df2;
    private DecimalFormat df3;    
    private final Pattern patternInt = Pattern.compile("([\\+-]?\\d+)([eE][\\+-]?\\d+)?");

    public ArrayList<pointRecord> getWpreadList() {
        return wpreadList;
    }
        
    
    public boolean litOzi(String strFichier) {
        boolean res = false;
        String tbFile[];
        wpreadList = new ArrayList<pointRecord>();
        int idxPoint = 0;
        // We read some files where there is only one character 10
        tbFile = strFichier.split(Character.toString((char)10));
        int lgTb = tbFile.length; 
        if (lgTb < 1) {
            // We read some files where there is only one character 13
            tbFile = strFichier.split(Character.toString((char)13));
            lgTb = tbFile.length; 
        } 
        try {
        // First 4 lines jumped
        // Line 1 : File type and version information
        // Line 2 : Geodetic Datum used for the Lat/Lon positions for each waypoint
        // Line 3 : Reserved for future use
        // Line 4 : GPS Symbol set - not used yet        
            for (int i = 4; i < lgTb; i++) {
                pointRecord myPoint = new pointRecord("","","");
                String[] partPoint = tbFile[i].split(",");
                String piecePoint;
                //System.out.println("0 -"+partPoint[0]+"- 1 -"+partPoint[1]+"- 2 -"+partPoint[2]+"- 3 -"+partPoint[3]+"- 4 -"+partPoint[4]+"- 5 -"+partPoint[5]+"- 6 -"+partPoint[6]+"-7 -"+partPoint[7]+"-");
                // 1,A01037        ,  46.330236,   5.388291,42121.6437153,0, 1, 3, 0, 65535,Mt Myon                                 , 0, 0, 0, 1201
                if (partPoint.length > 14) {
                    // Field 2 : Name - the waypoint name, use the correct length name to suit the GPS type.
                    String sNom = partPoint[1].trim();
                    if (sNom != null) myPoint.setFBalise(sNom);
                    // Field 15 : Altitude - in feet (-777 if not valid)
                    piecePoint = partPoint[14];
                    // We want to keep numbers only. We can find some endline character like char 10 or char 13
                    piecePoint = piecePoint.replaceAll("[^0-9]", "");
                    int altMeter;
                    if (piecePoint != null && !piecePoint.equals("") && !piecePoint.equals("-777")) {
                        //altMeter = (int) (Integer.parseInt(piecePoint)* 0.304757260842);
                        altMeter = (int) (Math.round(Integer.parseInt(piecePoint)/ 3.2808));
                        if (altMeter < 0 ) altMeter = 0;                   
                    } else {
                        altMeter = 0;
                    }
                    if (altMeter == 0 && sNom.length() == 6) {
                        // On est sur du nom court avec 6 caractères selon le vieux format
                        // On essaye d'en déduire les altitudes
                        try {
                            altMeter = Integer.parseInt(sNom.substring(4, 7));
                        } catch (Exception e) {
                            altMeter = 0;
                        }
                    }

                    myPoint.setFAlt(String.valueOf(altMeter));
                    // Field 11 : Description (max 40), no commas 
                    piecePoint = partPoint[10].trim();
                    myPoint.setFDesc(piecePoint);
                    // Field 3 : Latitude - decimal degrees
                    piecePoint = partPoint[2].trim();
                    myPoint.setFLat(piecePoint);
                    // Field 4 : Longitude - decimal degrees
                    piecePoint = partPoint[3].trim();
                    myPoint.setFLong(piecePoint);
                    myPoint.setFIndex(idxPoint);
                    wpreadList.add(myPoint);    
                    idxPoint++;
                }            
            }
            res = true;             
        } catch (Exception e) {
            res = false;
        }     
        
        return res;
    }    
    
    public boolean litPcx(String strFichier) {    
        boolean res = false;
        String tbFile[];
        String piecePoint;
        wpreadList = new ArrayList<pointRecord>();
        int idxPoint = 0;
        // We read some files where there is only one character 10
        tbFile = strFichier.split(Character.toString((char)10));
        int lgTb = tbFile.length; 
        if (lgTb < 2) {
            // We read some files where there is only one character 13
            tbFile = strFichier.split(Character.toString((char)13));
            lgTb = tbFile.length;
        }     
        try {
            for (int i = 0; i < lgTb; i++) {
                if (tbFile[i] != null && !tbFile[i].equals("")) {
                    if (tbFile[i].length() > 100 && tbFile[i].subSequence(0, 1).equals("W") ) {                
                        // H  IDNT   LATITUDE    LONGITUDE    DATE      TIME     ALT   DESCRIPTION                              PROXIMITY     SYMBOL ;waypts
                        // W  B25075 N45.9989596 E006.3994376 15-JUL-05 10:02:42 00750 PORET LE PETIT BORNAND                   0.00000e+000  00018
                        pointRecord myPoint = new pointRecord("","","");
                        myPoint.setFBalise(tbFile[i].substring(3, 9));                
                        piecePoint = tbFile[i].substring(55, 59);                     
                        myPoint.setFAlt(piecePoint);
                        // latitude
                        String sign = tbFile[i].substring(10, 11); 
                        piecePoint = tbFile[i].substring(11, 21);                    
                        if (sign.equals("N"))
                            myPoint.setFLat(piecePoint);
                        else if (sign.equals("S")) 
                            myPoint.setFLat("-"+piecePoint);
                        // longitude
                        sign = tbFile[i].substring(22, 23); 
                        piecePoint = tbFile[i].substring(23, 34);                    
                        if (sign.equals("E"))
                            myPoint.setFLong(piecePoint);
                        else if (sign.equals("W")) 
                            myPoint.setFLong("-"+piecePoint);   
                        piecePoint = tbFile[i].substring(60, 81);
                        if (piecePoint.trim().equals("")) {
                            piecePoint = myPoint.getFBalise();
                        }
                        myPoint.setFDesc(piecePoint);
                        wpreadList.add(myPoint);
                    } 
                }             
            }  
            res = true;             
        } catch (Exception e) {
            res = false;
        }   
        return res;
    }
    
    public boolean litCup(String strFichier) {
        boolean res = false;
        String tbFile[];
        String piecePoint;
        String sLat;
        String sLong;
        
        int iAlt;
        wpreadList = new ArrayList<pointRecord>();
        int idxPoint = 0;
        // We read some files where there is only one character 10
        tbFile = strFichier.split(Character.toString((char)10));
        int lgTb = tbFile.length; 
        if (lgTb < 1) {
            // We read some files where there is only one character 13
            tbFile = strFichier.split(Character.toString((char)13));
            lgTb = tbFile.length; 
        }
        try {
            for (int i = 0; i < lgTb; i++) {        
                if (!tbFile[i].equals("") && tbFile[i].length() > 47) {
                    if (tbFile[i].indexOf("task") > -1) {
                        break;
                    } else {
                        // les waypoints arrivent lorsque la ligne commence par "
                        if (tbFile[i].substring(0,1).equals("\"")) {
                            String[] partPoint = tbFile[i].split(",");
                            if (partPoint.length > 8) {                                
                                sLat = decodeCupLat(partPoint[3]);
                                sLong = decodeCupLong(partPoint[4]);
                                if (!sLat.equals("error") && !sLong.equals("error"))  {                                        
                                    pointRecord myPoint = new pointRecord("","","");
                                    // Description en éliminant les guillemets
                                    piecePoint = partPoint[0].replaceAll("\"", "");
                                    myPoint.setFDesc(piecePoint);
                                    // guillemets possibles
                                    piecePoint = partPoint[1].replaceAll("\"", "");
                                    myPoint.setFBalise(piecePoint);
                                    // Altitude extraction
                                    iAlt = 0;
                                    if (partPoint[5].indexOf("ft") > - 1) {
                                        iAlt = checkAlti(partPoint[5]);
                                        iAlt = (int) (iAlt * 0.3048);
                                    } else if (partPoint[5].indexOf("m") > - 1) {
                                        iAlt = checkAlti(partPoint[5]);
                                    }
                                    myPoint.setFAlt(String.valueOf(iAlt));                            
                                    myPoint.setFLat(sLat);
                                    myPoint.setFLong(sLong);
                                    myPoint.setFIndex(idxPoint);
                                    wpreadList.add(myPoint);
                                    idxPoint++;                                        
                                }                                                      
                            }
                        }                                                 
                    }
                }
            }
            
            displayList();
            
            res = true;             
        } catch (Exception e) {
            res = false;
        }  
        
        return res;
    }
    
    public boolean litComp(String strFichier) {
        boolean res = false;
        String tbFile[];
        String piecePoint;
        String realLine;
        String sAlt;
        String sDesc;
        String sCoord;
        String lastCoord;
        // each line begin with W and two spaces
        final String begLine = "W  ";
        final int begSize = begLine.length();
        int nbPoint = 0;
        int iAlt;
        wpreadList = new ArrayList<pointRecord>();
        // We read some files where there is only one character 10
        tbFile = strFichier.split(Character.toString((char)10));
        int lgTb = tbFile.length; 
        if (lgTb < 1) {
            // We read some files where there is only one character 13
            tbFile = strFichier.split(Character.toString((char)13));
            lgTb = tbFile.length; 
        }
        
        try {
            for (int i = 0; i < lgTb; i++) {      
                // snippet from https://bytefreaks.net/programming-2/java/java-remove-leading-character-or-any-prefix-from-string-only-if-it-matches
                realLine = tbFile[i].startsWith(begLine) ? tbFile[i].substring(begSize) : tbFile[i];
                if (!realLine.substring(0, 1).equals("w")) {
                    String [] partPoint = realLine.split(" ");
                    if (partPoint.length > 6)  {
                      //  System.out.println(realLine);
                     //   System.out.println("0 -"+partPoint[0]+"- 1 -"+partPoint[1]+"- 2 -"+partPoint[2]+"- 3 -"+partPoint[3]+"- 4 -"+partPoint[4]+"- 5 -"+partPoint[5]+"- 6 -"+partPoint[6]+"-7 -"+partPoint[7]+"-");
                        pointRecord myPoint = new pointRecord("","","");
                        myPoint.setFBalise(partPoint[0]);
                        // Description is the last part. Generally line ends with (char)13 (char)10 
                        // We split with (char)10 -> char(13) can remains
                        // a blank field is possible
                        if (partPoint.length > 7)  {
                            // Description is splitted on space character
                            StringBuilder sbDesc = new StringBuilder();
                            for (int j = 7; j < partPoint.length; j++) {
                                sbDesc.append(partPoint[j]);
                                if (j < partPoint.length - 1) sbDesc.append(" ");
                            }
                            sDesc = sbDesc.toString().replaceAll(Character.toString((char)13), "");
                        } else {
                            sDesc = partPoint[0];
                        }                    
                        myPoint.setFDesc(sDesc);
                        int idxPoint = partPoint[6].indexOf(".");
                        if (idxPoint > 0) {
                            sAlt = partPoint[6].substring(0, idxPoint);
                        } else {
                            sAlt = partPoint[6];
                        }
                        myPoint.setFAlt(sAlt);   
                        // Latitude includes a mysterious character between coord and hemisphere : 36.8155390�S
                        // Split is problematic with encoding variations. We extract last character                    
                        lastCoord = partPoint[2].substring(partPoint[2].length() - 1,partPoint[2].length());
                        if (lastCoord.equals("S")) {
                            sCoord = "-"+partPoint[2].substring(0,partPoint[2].length()-2);
                        } else {
                            sCoord = partPoint[2].substring(0,partPoint[2].length()-2);
                        }
                        myPoint.setFLat(sCoord);
                        // Longitude includes a mysterious character between coord and meridian : 146.8527860�E
                        // Split is problematic with encoding variations. We extract last character                      
                        lastCoord = partPoint[3].substring(partPoint[3].length() - 1,partPoint[3].length());
                        if (lastCoord.equals("W")) {
                            sCoord = "-"+partPoint[3].substring(0,partPoint[3].length()-2);
                        } else {
                            sCoord = partPoint[3].substring(0,partPoint[3].length()-2);
                        }
                        myPoint.setFLong(sCoord); 
                        myPoint.setFIndex(nbPoint);
                        wpreadList.add(myPoint);    
                        nbPoint++;
                    }
                }                        
            }
            res = true;             
        } catch (Exception e) {
            res = false;
        }        
        
        return res;
    }
    
    /**
     * Library jpx-1.3.0 used from https://github.com/jenetics/jpx
     * Javadoc http://www.javadoc.io/doc/io.jenetics/jpx     
     * @param strPath 
     */
    public boolean litGpx(String strPath) {  
        boolean res = false;
        String sName = "";
        String sBalise = "";
        wpreadList = new ArrayList<pointRecord>();      
        try {           
            final GPX mygpx = GPX.read(strPath);       
            // we are expecting waypoints file
            List<WayPoint> wpList;
            wpList = mygpx.getWayPoints(); 
            if (wpList.size() > 0) {
                for (int i = 0; i < wpList.size(); i++) {
                    WayPoint wp = wpList.get(i);
                    pointRecord myPoint = new pointRecord("","","");
                    myPoint.setFLat(String.valueOf(wp.getLatitude().doubleValue()));
                    myPoint.setFLong(String.valueOf(wp.getLongitude().doubleValue()));
                    myPoint.setFAlt(String.valueOf(wp.getElevation().get().intValue())); 
                    sName = wp.getName().get();
                    if(sName != null) {                    
                        if (sName.length() > 6) {
                           sBalise = sName.substring(0,7).toUpperCase();
                        } else {
                           sBalise = sName.toUpperCase(); 
                        }
                    } else {
                        sBalise = "WPT"+String.format("%03d", (int) wp.getElevation().get().intValue()/10 );  
                    }                    
                    myPoint.setFBalise(sBalise);
                
                    sName = wp.getDescription().get();
                    if (sName != null && sName.length() > 1 ) {
                        myPoint.setFDesc(sName);
                    } else {
                        sName = wp.getComment().get();
                        if (sName != null && sName.length() > 1 ) {
                            myPoint.setFDesc(sName);
                        } else {
                            myPoint.setFDesc(sBalise);
                        }
                    }        
                    myPoint.setFIndex(i);
                    wpreadList.add(myPoint);                     
                }                  
            }
            res = true;
            displayList();
        } catch (Exception e) {
           res = false;
        }
        
        return res;
    }
    
    public boolean litKml(String strFichier) { 
        boolean res = false;
        try {            
            wpreadList = new ArrayList<pointRecord>();
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();   
            InputStream stream = new ByteArrayInputStream(strFichier.getBytes(StandardCharsets.UTF_8));
            Document doc = dBuilder.parse(stream);
            //optional, but recommended
            //read this - http://stackoverflow.com/questions/13786607/normalization-in-dom-parsing-with-java-how-does-it-work
            doc.getDocumentElement().normalize();
            NodeList placeList;

            //Separate data by the <coordinates> tag.
            placeList = doc.getElementsByTagName("Placemark");
            for (int i = 0; i < placeList.getLength(); i++) {
                pointRecord myPoint = new pointRecord("","","");
		Node nNode = placeList.item(i);				
		if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element eElement = (Element) nNode;
                    myPoint.setFBalise(eElement.getElementsByTagName("name").item(0).getTextContent());	
                    myPoint.setFDesc(eElement.getElementsByTagName("description").item(0).getTextContent());		                    
                    String totalCoordinates = eElement.getElementsByTagName("coordinates").item(0).getChildNodes().item(0).getNodeValue();        
                    String[] coordinates = totalCoordinates.split(",");
                    if (coordinates.length > 2) {
                        myPoint.setFLong(coordinates[0]);
                        myPoint.setFLat(coordinates[1]);
                        //myPoint.setFAlt(coordinates[2]);                        
                        // Strange exceptions I double values are eliminated
                        String[] tbAlt = coordinates[2].split("\\.");
                        if (tbAlt.length > 1) 
                            myPoint.setFAlt(tbAlt[0]); 
                        else
                            myPoint.setFAlt(coordinates[2]); 
                    }
                    myPoint.setFIndex(i);
                    wpreadList.add(myPoint);     
		}
            }
            res = true;           
        } catch (Exception e) {
            res = false;
        }
        return res;
    }
    
    public boolean litXcp(String strFichier) {    
        boolean res = false;
        wpreadList = new ArrayList<pointRecord>();
        int cpt = -1;
        
        try {
            JSONParser parser = new JSONParser();
            Object jsonObj = parser.parse(strFichier);
            JSONObject jsonObject = (JSONObject) jsonObj;
            
            JSONArray waypoints = (JSONArray) jsonObject.get("waypoints");

            @SuppressWarnings("unchecked")
            Iterator<String> it = waypoints.iterator();
            while (it.hasNext()) {
                String[] partWp = it.next().split(",");
                if (partWp.length == 4) {
                    cpt++;
                    pointRecord pr = new pointRecord(partWp[0], partWp[3], partWp[0]);
                    pr.setFLat(partWp[1]);
                    pr.setFLong(partWp[2]);
                    pr.setFIndex(cpt);
                    wpreadList.add(pr);
                }
            }            
            res = true;             
        } catch (Exception e) {
            res = false;
        }
        
        return res;
    }
    
    private String decodeCupLat(String sLat) {
        String res = "";
        String sDeg;
        String sMn;
        String sHem;  
                       
        decimalFormatSymbols.setDecimalSeparator('.');       
        df2 = new DecimalFormat("#0.00000", decimalFormatSymbols);        
        
        try {
            // Latitude is a decimal number (eg 4553.445N )where 1-2 characters are degrees, 3-4 characters are minutes,
            // 5  decimal point, 6-8 characters are decimal minutes. The ellipsoid used is WGS-1984
            sDeg = sLat.substring(0,2);
            sMn = sLat.substring(2,8);
            sHem = sLat.substring(8);
            if (systemio.checking.parseDouble(sMn) && systemio.checking.checkInt(sDeg)) {   
                position myPos = new position();
                myPos.setLatDegres(Integer.parseInt(sDeg));
                myPos.setHemisphere(sHem);
                myPos.setLatMin_mm(Double.parseDouble(sMn));               
                res = df2.format(myPos.getLatitude());
            } else {
                res = "error";
            }
        } catch (Exception e) {
            res = "error";
        }            
        
        return res;
    }

    private String decodeCupLong(String sLong) {
        String res = "";
        String sDeg;
        String sMn;
        String sMer;  
        decimalFormatSymbols.setDecimalSeparator('.'); 
        df3 = new DecimalFormat("##0.00000", decimalFormatSymbols);        
        
        try {
            // Longitude is a decimal number (eg 00627.076E) where 1-3 characters are degrees, 4-5 characters are minutes,
            // 6 decimal point, 7-9 characters are decimal minutes. The ellipsoid used is WGS-1984
            sDeg = sLong.substring(0,3);
            sMn = sLong.substring(3,9);
            sMer = sLong.substring(9);
            if (systemio.checking.parseDouble(sMn) && systemio.checking.checkInt(sDeg) ) {   
                position myPos = new position();
                myPos.setLongDegres(Integer.parseInt(sDeg));
                myPos.setMeridien(sMer);
                myPos.setLongMin_mm(Double.parseDouble(sMn));               
                res = df3.format(myPos.getLongitude());
            } else {
                res = "error";
            }
        } catch (Exception e) {
            res = "error";
        }            
        
        return res;
    }
    
    /**
     * Value of sAlt is "1830.0m"
     * @param sAlt
     * @return 
     */
    private int checkAlti(String sAlt) {
        
        int res = 0;
        String numberNoWhiteSpace = sAlt.replaceAll("\\s","");
     //   Pattern patternI = Pattern.compile("([\\+-]?\\d+)([eE][\\+-]?\\d+)?");
        Matcher matcherI = patternInt.matcher(numberNoWhiteSpace);
        if (matcherI.find()) {
          res = Integer.parseInt(matcherI.group(0));
        } 
        
        return res;        
        
    }    
    
    private void displayList() {
        //System.out.println(myPoint.getFBalise()+" "+myPoint.getFAlt()+" "+myPoint.getFLat()+" "+myPoint.getFLong()+" "+myPoint.getFDesc());        
        if (wpreadList.size() > 0) {
            System.out.println("size "+wpreadList.size());
            for (int i = 0; i < wpreadList.size(); i++) {
                System.out.println(wpreadList.get(i).getFIndex()+" "+wpreadList.get(i).getFBalise()+" "+wpreadList.get(i).getFDesc()+" "+wpreadList.get(i).getFLat()+" "+wpreadList.get(i).getFLong()+" "+wpreadList.get(i).getFAlt());
            }
            System.out.println("Liste terminée");
        }        
    }
    
}
