/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package waypio;

import geoutils.position;
import io.jenetics.jpx.GPX;
import io.jenetics.jpx.Link;
import io.jenetics.jpx.Metadata;
import io.jenetics.jpx.WayPoint;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.OutputStreamWriter;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 *
 * @author gil
 */
public class wpwritefile {    
    
    private DecimalFormatSymbols decimalFormatSymbols = new DecimalFormatSymbols();    
    private DecimalFormat df2; 
    private DecimalFormat df3;     
    private String CF =  "\r\n"; 
    
    /**
     * Waypoint data
     * One line per waypoint
     * each field separated by a comma
     * comma's not allowed in text fields, character 209 can be used instead and a comma will be substituted.
     * non essential fields need not be entered but comma separators must still be used (example ,,)
     * defaults will be used for empty fields
     * Any number of the last fields in a data line need not be included at all not even the commas.
     * Field 1 : Number - this is the location in the array (max 1000), must be unique, usually start at 1 and increment. Can be set to -1 (minus 1) and the number will be auto generated.
     * Field 2 : Name - the waypoint name, use the correct length name to suit the GPS type.
     * Field 3 : Latitude - decimal degrees.
     * Field 4 : Longitude - decimal degrees.
     * Field 5 : Date - see Date Format below, if blank a preset date will be used
     * Field 6 : Symbol - 0 to number of symbols in GPS
     * Field 7 : Status - always set to 1
     * Field 8 : Map Display Format
     * Field 9 : Foreground Color (RGB value)
     * Field 10 : Background Color (RGB value)
     * Field 11 : Description (max 40), no commas
     * Field 12 : Pointer Direction
     * Field 13 : Garmin Display Format
     * Field 14 : Proximity Distance - 0 is off any other number is valid
     * Field 15 : Altitude - in feet (-777 if not valid)
     * Field 16 : Font Size - in points
     * Field 17 : Font Style - 0 is normal, 1 is bold.
     * Field 18 : Symbol Size - 17 is normal size
     * Field 19 : Proximity Symbol Position
     * Field 20 : Proximity Time
     * Field 21 : Proximity or Route or Both
     * Field 22 : File Attachment Name
     * Field 23 : Proximity File Attachment Name
     * Field 24 : Proximity Symbol Name   
     * @param wpList
     * @param pFile
     * @return 
     */
    public boolean writeOzi(List<pointRecord> wpList, File pFile) {
        boolean res = false;
        String sPart;
        decimalFormatSymbols.setDecimalSeparator('.');       
        df2 = new DecimalFormat("####.000000", decimalFormatSymbols); 
        try
        {
            FileWriter filewrite = new FileWriter(pFile);

            if(!pFile.exists())
            {
                pFile.createNewFile();
            }
            // Encodage ASCII
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(pFile), "Cp1252"));
            // header spécifications -> http://www.rus-roads.ru/gps/help_ozi/fileformats.html
            // Line 1 : File type and version information
            writer.write("OziExplorer Waypoint File Version 1.0"+CF);
            // Line 2 : Geodetic Datum used for the Lat/Lon positions for each waypoint
            writer.write("WGS 84"+CF);
            // Line 3 : Reserved for future use
            writer.write("Reserved 2"+CF);
            // Line 4 : GPS Symbol set - not used yet
            writer.write("Reserved 3"+CF);      
            for (int i = 0; i < wpList.size(); i++) {
                StringBuilder sb = new StringBuilder();
                // limit to six characters but not absolutely required -> use the correct length name to suit the GPS type.
                sPart = wpList.get(i).getFBalise();
                sPart = wpList.get(i).getFBalise().length() > 6 ? wpList.get(i).getFBalise().substring(0, 7) : wpList.get(i).getFBalise();
                sb.append(String.format("%4s", i+1)).append(",").append(sPart).append(",");
                double dLat = Double.parseDouble(wpList.get(i).getFLat());
                sb.append(df2.format(dLat)).append(",");
                double dLong = Double.parseDouble(wpList.get(i).getFLong());
                sb.append(df2.format(dLong)).append(",,0,1,3,0,65535,");
                sPart = wpList.get(i).getFDesc().length() > 40 ? wpList.get(i).getFDesc().substring(0, 41) : wpList.get(i).getFDesc();
                sb.append(sPart).append(",0,0,0,");
                int iAlt = (int) (Math.round(Integer.parseInt(wpList.get(i).getFAlt()) * 3.280839895));
                sb.append(String.valueOf(iAlt)).append(",6,0,17").append(CF);
                writer.write(sb.toString());
            }
            writer.close();
            res = true;
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
        
        return res;
    }
    
    /**  
     * Format syntax http://www.keepitsoaring.com/LKSC/Downloads/cup_format.pdf   (Logfly Docs -> Format Cup)
     * @param wpList
     * @param pFile
     * @return 
     */    
    public boolean writeCup(List<pointRecord> wpList, File pFile) {
        boolean res = false;   
        try
        {
            FileWriter filewrite = new FileWriter(pFile);

            if(!pFile.exists())
            {
                pFile.createNewFile();
            }        
            // Encodage ASCII
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(pFile), "Cp1252"));
            writer.write("name,code,country,lat,lon,elev,style,rwdir,rwlen,freq,desc"+CF);       
            for (int i = 0; i < wpList.size(); i++) {
                StringBuilder sb = new StringBuilder();
                // "DECO CRET DU LOUP",D02183,,4553.445N,00627.076E,1830.0m,2,,,,
                int iAlt = (int) (Integer.parseInt(wpList.get(i).getFAlt()));
                String sLat = encodeCupLat(wpList.get(i).getFLat());
                String sLong = encodeCupLong(wpList.get(i).getFLong());
                sb.append("\"").append(wpList.get(i).getFDesc()).append("\",");
                sb.append(wpList.get(i).getFBalise()).append(",,");   // No country we put double comma
                sb.append(sLat).append(",").append(sLong).append(",");
                sb.append(String.valueOf(iAlt)).append("m,1,,,,").append(CF);
                writer.write(sb.toString());
            }                
            
            writer.close();
            res = true;
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }        
        return res;
    } 
    
    public boolean writePCX(List<pointRecord> wpList, File pFile) {                
        
        boolean res = false; 
        // date time required
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter parser = new DateTimeFormatterBuilder().parseCaseInsensitive().appendPattern("dd-MMM-YY HH:mm:ss").toFormatter(Locale.ENGLISH);
        String sDate = now.format(parser).toUpperCase();      
        
        try
        {
            FileWriter filewrite = new FileWriter(pFile);

            if(!pFile.exists())
            {
                pFile.createNewFile();
            }        
            // Encodage ASCII
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(pFile), "Cp1252"));
            // header                   
            StringBuilder sbHeader = new StringBuilder();    
            sbHeader.append("").append(CF);
            sbHeader.append("H  SOFTWARE NAME & VERSION").append(CF);
            sbHeader.append("I  PCX5 2.09").append(CF);
            sbHeader.append("").append(CF);
            sbHeader.append("H  R DATUM                IDX DA             DF             DX             DY             DZ").append(CF);
            sbHeader.append("M  G WGS 84               121 +0.000000e+000 +0.000000e+000 +0.000000e+000 +0.000000e+000 +0.000000e+000").append(CF);
            sbHeader.append("").append(CF);
            sbHeader.append("H  COORDINATE SYSTEM").append(CF);
            sbHeader.append("U  LAT LON DEG").append(CF);
            sbHeader.append("").append(CF);
            sbHeader.append("H  IDNT   LATITUDE    LONGITUDE    DATE      TIME     ALT   DESCRIPTION                              PROXIMITY     SYMBOL ;waypts").append(CF);            
            writer.write(sbHeader.toString());
            for (int i = 0; i < wpList.size(); i++) {
                StringBuilder sb = new StringBuilder();
                String sLat = encodeSpecLat(wpList.get(i).getFLat(),2);
                String sLong = encodeSpecLong(wpList.get(i).getFLong(),2);  
                String sAlt = String.format("%05d", Integer.parseInt(wpList.get(i).getFAlt())); 
                sb.append("W  ").append(String.format("%1$-6s",wpList.get(i).getFBalise()));
                sb.append(" ").append(sLat).append(" ").append(sLong).append(" ");
                sb.append(sDate).append(" ").append(sAlt).append(" ");
                sb.append(String.format("%1$-40s",wpList.get(i).getFDesc())).append(" 0.00000e+000  00018").append(CF);                                           
                writer.write(sb.toString());                
            }            
            writer.close();
            res = true;            
        } catch(Exception e) {
            e.printStackTrace();
        }                
        
        return res;
    }
    
    public boolean writeKml(List<pointRecord> wpList, File pFile) {                
        boolean res = false;   
        
        try
            {
                FileWriter filewrite = new FileWriter(pFile);

                if(!pFile.exists())
                {
                    pFile.createNewFile();
                }        
                // Encodage ASCII
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(pFile), "Cp1252"));
                // header                   
                StringBuilder sbHeader = new StringBuilder();
                sbHeader.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>").append(CF);
                sbHeader.append("<kml xmlns=\"http://www.opengis.net/kml/2.2\"").append(CF);
                sbHeader.append("         xmlns:gx=\"http://www.google.com/kml/ext/2.2\">").append(CF);
                sbHeader.append((char) 9).append("<Document>").append(CF);
                sbHeader.append((char) 9).append((char) 9).append("<name>").append(pFile.getName()).append("</name>").append(CF);
                sbHeader.append((char) 9).append((char) 9).append("<snippet>Generated by Logfly </snippet>").append(CF);
                sbHeader.append((char) 9).append((char) 9).append("<!-- Normal waypoint style -->").append(CF);
                sbHeader.append((char) 9).append((char) 9).append("<Style id=\"waypoint_n\">").append(CF);
                sbHeader.append((char) 9).append((char) 9).append((char) 9).append("<IconStyle>").append(CF);
                sbHeader.append((char) 9).append((char) 9).append((char) 9).append((char) 9).append("<Icon>").append(CF);
                sbHeader.append((char) 9).append((char) 9).append((char) 9).append((char) 9).append((char) 9).append("<href>http://maps.google.com/mapfiles/kml/pal4/icon61.png</href>").append(CF);
                sbHeader.append((char) 9).append((char) 9).append((char) 9).append((char) 9).append("</Icon>").append(CF);
                sbHeader.append((char) 9).append((char) 9).append((char) 9).append("</IconStyle>").append(CF);
                sbHeader.append((char) 9).append((char) 9).append("</Style>").append(CF);
                sbHeader.append((char) 9).append((char) 9).append("<!-- Highlighted waypoint style -->").append(CF);
                sbHeader.append((char) 9).append((char) 9).append("<Style id=\"waypoint_h\">").append(CF);
                sbHeader.append((char) 9).append((char) 9).append((char) 9).append("<IconStyle>").append(CF);
                sbHeader.append((char) 9).append((char) 9).append((char) 9).append((char) 9).append("<scale>1.2</scale>").append(CF);
                sbHeader.append((char) 9).append((char) 9).append((char) 9).append((char) 9).append("<Icon>").append(CF);
                sbHeader.append((char) 9).append((char) 9).append((char) 9).append((char) 9).append((char) 9).append("<href>http://maps.google.com/mapfiles/kml/pal4/icon61.png</href>").append(CF);
                sbHeader.append((char) 9).append((char) 9).append((char) 9).append((char) 9).append("</Icon>").append(CF);
                sbHeader.append((char) 9).append((char) 9).append((char) 9).append("</IconStyle>").append(CF);
                sbHeader.append((char) 9).append((char) 9).append("</Style>").append(CF);
                sbHeader.append((char) 9).append((char) 9).append("<StyleMap id=\"waypoint\">").append(CF);
                sbHeader.append((char) 9).append((char) 9).append((char) 9).append("<Pair>").append(CF);
                sbHeader.append((char) 9).append((char) 9).append((char) 9).append((char) 9).append("<key>normal</key>").append(CF);
                sbHeader.append((char) 9).append((char) 9).append((char) 9).append((char) 9).append("<styleUrl>#waypoint_n</styleUrl>").append(CF);
                sbHeader.append((char) 9).append((char) 9).append((char) 9).append("</Pair>").append(CF);
                sbHeader.append((char) 9).append((char) 9).append((char) 9).append("<Pair>").append(CF);
                sbHeader.append((char) 9).append((char) 9).append((char) 9).append((char) 9).append("<key>highlight</key>").append(CF);
                sbHeader.append((char) 9).append((char) 9).append((char) 9).append((char) 9).append("<styleUrl>#waypoint_h</styleUrl>").append(CF);
                sbHeader.append((char) 9).append((char) 9).append((char) 9).append("</Pair>").append(CF);
                sbHeader.append((char) 9).append((char) 9).append("</StyleMap>").append(CF);
                sbHeader.append((char) 9).append((char) 9).append("<Folder>").append(CF);
                sbHeader.append((char) 9).append((char) 9).append((char) 9).append("<name>Waypoints</name>").append(CF);     
                writer.write(sbHeader.toString());
                for (int i = 0; i < wpList.size(); i++) {
                    StringBuilder sb = new StringBuilder();
                    sb.append((char) 9).append((char) 9).append((char) 9).append("<Placemark>").append(CF);   
                    sb.append((char) 9).append((char) 9).append((char) 9).append((char) 9).append("<name>").append(wpList.get(i).getFBalise()).append("</name>").append(CF);   
                    sb.append((char) 9).append((char) 9).append((char) 9).append((char) 9).append("<description>").append(wpList.get(i).getFDesc()).append("</description>").append(CF);   
                    sb.append((char) 9).append((char) 9).append((char) 9).append((char) 9).append("<styleUrl>#waypoint</styleUrl>").append(CF);   
                    sb.append((char) 9).append((char) 9).append((char) 9).append((char) 9).append("<Point>").append(CF);   
                    sb.append((char) 9).append((char) 9).append((char) 9).append((char) 9).append((char)(9));
                    sb.append("<coordinates>").append(wpList.get(i).getFLong()).append(",").append(wpList.get(i).getFLat());
                    sb.append(",").append(wpList.get(i).getFAlt()).append("</coordinates>").append(CF);   
                    sb.append((char) 9).append((char) 9).append((char) 9).append((char) 9).append("</Point>").append(CF);   
                    sb.append((char) 9).append((char) 9).append((char) 9).append("</Placemark>").append(CF);                       
                    writer.write(sb.toString());
                }
                // End of file
                StringBuilder sbEnd = new StringBuilder();
                sbEnd.append((char) 9).append((char) 9).append("</Folder>").append(CF); 
                sbEnd.append((char) 9).append("</Document>").append(CF); 
                sbEnd.append("</kml>");
                writer.write(sbEnd.toString());
                writer.close();
                res = true;
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }                
        return res;
    }
    
    
    public boolean writeComp(List<pointRecord> wpList, File pFile) {
        boolean res = false;   
        // date time required
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter parser = new DateTimeFormatterBuilder().parseCaseInsensitive().appendPattern("dd-MMM-yyyy HH:mm:ss").toFormatter(Locale.ENGLISH);
        String sDate = now.format(parser).toUpperCase();         
        try
        {
            FileWriter filewrite = new FileWriter(pFile);

            if(!pFile.exists())
            {
                pFile.createNewFile();
            }        
            // Encodage ASCII
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(pFile), "Cp1252"));
            // header
            writer.write("G  WGS 84"+CF);     // Line G: It identifies the datum of the map
            writer.write("U  1"+CF);          //  Line U: It identifies the system of coordinate [U 1] indicates coordinates in Lat/Lot.                  
            for (int i = 0; i < wpList.size(); i++) {
                StringBuilder sb = new StringBuilder();
                double dAlt = Double.parseDouble(wpList.get(i).getFAlt());
                String sAlt = encodeCompAlt(wpList.get(i).getFAlt());
                String sLat = encodeSpecLat(wpList.get(i).getFLat(),1);
                String sLong = encodeSpecLong(wpList.get(i).getFLong(),1);                
                sb.append("W  ").append(wpList.get(i).getFBalise());         // -> ' W  A01046
                sb.append(" A ").append(sLat).append(" ").append(sLong);     // -> A 47.9013900∫N 6.9843200∫E
                sb.append(" ").append(sDate).append(" ").append(sAlt);       // -> 8-MAR-2013 22:48:09 680.000000            
                sb.append(" ").append(wpList.get(i).getFDesc()).append(CF);  // ->  LA BRESSE   no space completion
                sb.append("w Waypoint,,,,,,,,,").append(CF);                 //  always added              
                writer.write(sb.toString());
            }                            
            writer.close();
            res = true;
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }        
        return res;
    }     
    
    
    
    
    /**
     * Library jpx-1.3.0 used from https://github.com/jenetics/jpx
     * Javadoc http://www.javadoc.io/doc/io.jenetics/jpx
     * @param wpList
     * @param pFile
     * @return 
     */
    public boolean writeGpx(List<pointRecord> wpList, File pFile) {
        
        boolean res = false;
        Metadata metaGpx = Metadata.builder().author("Logfly 5").addLink(Link.of("http://logfly.org")).build();  
        
        try {
            
            Date dateFile = new Date();
            List<WayPoint> gpxWpList = new ArrayList<WayPoint>();
            for (int i = 0; i < wpList.size(); i++) {                
                double dLat = Double.parseDouble(wpList.get(i).getFLat());
                double dLong = Double.parseDouble(wpList.get(i).getFLong());
                double dAlt = Double.parseDouble(wpList.get(i).getFAlt());
                WayPoint point = WayPoint.builder().lat(dLat).lon(dLong).ele(dAlt).desc(wpList.get(i).getFDesc()).name(wpList.get(i).getFBalise()).build();
                gpxWpList.add(point);
            }
            GPX myGpx = GPX.of("", metaGpx, gpxWpList, null, null);
            GPX.write(myGpx, pFile.getAbsolutePath());
            res = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return res;    
    }   
    
    private String encodeCompAlt(String sAlt) {
        String res = "";
        decimalFormatSymbols.setDecimalSeparator('.');       
        df2 = new DecimalFormat("#####.000000", decimalFormatSymbols); 
        double dAlt = Double.parseDouble(sAlt);
        res = df2.format(dAlt);       
        
        return res;
    }

    private String encodeSpecLat(String sLat, int idFormat) {
        StringBuilder sbRes = new StringBuilder();
        String res = "";
        decimalFormatSymbols.setDecimalSeparator('.');         
        // V4 -> sLat = Format(dLat,"##.0000000")+Encodings.ASCII.Chr(186)+"N"       ' 47.9013900∫N        
        try {
            double dLat = Double.parseDouble(sLat);
            String sHem = dLat > 0 ? "N" : "S";
            switch (idFormat) {
                case 1:
                    // CompeGPS
                    df2 = new DecimalFormat("##.0000000", decimalFormatSymbols);                     
                    sbRes.append(df2.format(dLat));                        
                    sbRes.append((char) 186).append(sHem);                    
                    break;
                case 2 :
                    // PCX
                    df2 = new DecimalFormat("00.0000000", decimalFormatSymbols);                     
                    sbRes.append(sHem).append(df2.format(dLat));                                        
                    break;
            }
            res = sbRes.toString();
        } catch (Exception e) {
            res = "error";
        }
        
        return res;
    }

    private String encodeSpecLong(String sLong, int idFormat) {
        StringBuilder sbRes = new StringBuilder();
        String res = "";
        decimalFormatSymbols.setDecimalSeparator('.');         
        // V4 ->  sLong = Format(dLong,"###.0000000")+Encodings.ASCII.Chr(186)+"E"        ' 6.9843200∫E       
        try {
            double dLong = Double.parseDouble(sLong);    
            String sMer = dLong > 0 ? "E" : "W";
            switch (idFormat) {
                case 1:
                    // CompeGPS
                    df3 = new DecimalFormat("###.0000000", decimalFormatSymbols);                     
                    sbRes.append(df3.format(dLong));            
                    sbRes.append((char) 186).append(sMer);                    
                    break;
                case 2:
                    // PCX
                    df3 = new DecimalFormat("000.0000000", decimalFormatSymbols);                     
                    sbRes.append(sMer).append(df3.format(dLong));                             
                    break;
            }            
            res = sbRes.toString();
        } catch (Exception e) {
            res = "error";
        }
        
        return res;
    }
    
    private String encodeCupLat(String sLat) {
        StringBuilder sbRes = new StringBuilder();
        String res = "";
        decimalFormatSymbols.setDecimalSeparator('.');       
        df2 = new DecimalFormat("00.000", decimalFormatSymbols);           
        try {
            position myPos = new position(); 
            double dLat = Double.parseDouble(sLat);
            myPos.setLatitudeDd(dLat);
            // 4553.445N  
            sbRes.append(String.format("%02d",myPos.getLatDegres()));
            sbRes.append(df2.format(myPos.getLatMin_mm()));
            sbRes.append(myPos.getHemisphere());
            res = sbRes.toString();
        } catch (Exception e) {
            res = "error";
        }
        
        return res;
    }
    
    private String encodeCupLong(String sLong) {
        StringBuilder sbRes = new StringBuilder();
        String res = "";
        decimalFormatSymbols.setDecimalSeparator('.'); 
        df2 = new DecimalFormat("00.000", decimalFormatSymbols);             
        try {
            position myPos = new position(); 
            double dLong = Double.parseDouble(sLong);
            myPos.setLongitudeDd(dLong);
            // 00627.076E
            sbRes.append(String.format("%03d",myPos.getLongDegres()));
            sbRes.append(df2.format(myPos.getLongMin_mm()));
            sbRes.append(myPos.getMeridien());
            res = sbRes.toString();
        } catch (Exception e) {
            res = "error";
        }
        
        return res;
    }    
}
