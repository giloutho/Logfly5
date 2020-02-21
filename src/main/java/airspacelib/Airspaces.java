/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package airspacelib;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.logging.Level;
import org.locationtech.jts.geom.Coordinate;
import systemio.mylogging;

/**
 *
 * Created by Rob Verhoef on 20-10-2015 (https://github.com/mobileaviation/AirspacesImportApp)
 * 
 * Rob removes airspace with less 4 coordinates
 * We have openAir files with only three coordinates (triangle) or even
 * Accordingly code is modified
 * 
 */
public class Airspaces extends ArrayList<Airspace> {
    
    private StringBuilder sbError;
    
    public Airspaces()
    {

    }    
    
    public void Add(Airspace airspace)
    {
        this.add(airspace);
    }
    
    
    private String readTxt8859(String pathFichier){
        String res = null;
        
        try {
            File fichier = new File(pathFichier);
            res = new String(Files.readAllBytes(Paths.get(fichier.getAbsolutePath())),Charset.forName("ISO-8859-1"));     
        } catch (IOException e) {
            sbError = new StringBuilder(this.getClass().getName()+"."+Thread.currentThread().getStackTrace()[1].getMethodName());
            sbError.append("\r\n").append(e.toString());
            mylogging.log(Level.SEVERE, sbError.toString());
        }    
                                
        return res;
    }        

    public void OpenOpenAirTextFile(String filename)
    {
        String _filename = filename;
        // Original code
        //String txt = Helpers.readFromFile(_filename);
        // We had bad characters, we use waypoint function readTxt8859
        String txt = readTxt8859(_filename);
        readOpenAirText(txt);
    }
    
    public String lines[];
    
    /**
     * Altitude is now computed in meters
     * @param text
     */
    private void readOpenAirText(String text)
    {
        try {
            //text = text.replace("\n", "\r\n");
            //lines = text.split("\r\n");
            String textNoComments = text.replaceAll("(?m)^\\*.*", "");
            lines = textNoComments.split("(?=(?m)^.)");
            //lines = text.split("(?=\\*)|(?=\\bAC)|(?=\\bAN)|(?=\\bAH)|(?=\\bAL)|(?=\\bAF)|(?=\\bAG)|(?=\\bDP)|(?=\\bDB)|(?=\\bDC\\s[0-9]+(\\.\\d{1,2})?)|(?=\\bV\\sX=[0-9])|(?=\\bV\\sD)|(?=\\bDA)");
            Airspace airspace = null;
            LatLng location = null;
            LatLng center = null;
            Boolean circle  = false;
            Boolean cw = true;
            //Boolean newAirspace = false;
            for (String l : lines)
            {

                try {
                    if (!l.startsWith("*")) {
                        // Check is first char = * then discard this split[*]
                        // Read the first line for the Airspace Category (AC)
                        // Read the line with starts with AN, Following string is Name
                        // -- AH, unit (non if FT), top level limit, folowed by reference (MSL)
                        // -- AL, unit (non if FT), bottom level limit, folowed by reference (MSL)

                        //

                        if (l.startsWith("AC ")) {
                            // This is a new airspace description in the file
                            // 3 replaced by 1
                            if ((airspace != null) && (airspace.coordinates.size() > 1) && !circle
                                    && !airspace.coordinates.get(0).equals(airspace.coordinates.get(airspace.coordinates.size() - 1)))
                                airspace.coordinates.add(airspace.coordinates.get(0));
                            if ((airspace != null) && (airspace.Name == null))
                                this.remove(airspace);
                            // 4 replaced by 2
                            if ((airspace != null) && (airspace.coordinates.size() < 2))
                                this.remove(airspace);
                            if ((airspace != null) && airspace.coordinates.size() > 1) {
                                airspace.computeBoundingBox();
                            }
                            airspace = new Airspace();
                            airspace.originalText.append(l);                            
                            cw = true;
                            //newAirspace = false;
                            this.add(airspace);
                            airspace.Version = "0";
                            airspace.ID = 0;
                            String c = l.replace("AC ", "").trim();
                            airspace.Category = AirspaceCategory.valueOf(Helpers.findRegex("[A-Za-z]+\\w|[A-Za-z]", c));
                            switch (airspace.Category) {
                                case  A :
                                    airspace.typeColor = "1";
                                    break;
                                case  AWY :                                    
                                    airspace.typeColor = "2";                                    
                                    break;
                                case  B :                                    
                                    airspace.typeColor = "3";                                    
                                    break;
                                case  C :                                    
                                    airspace.typeColor = "4";                                    
                                    break;
                                case  CTR :                                    
                                    airspace.typeColor = "5";                                    
                                    break;
                                case  CTA :                                    
                                    airspace.typeColor = "6";                                    
                                    break;
                                case  D :                                    
                                    airspace.typeColor = "7";                                    
                                    break;
                                case  DANGER :                                    
                                    airspace.typeColor = "20";                                    
                                    break;
                                case  Q :                                    
                                    airspace.typeColor = "8";                                    
                                    break;
                                case  E :                                    
                                    airspace.typeColor = "9";                                    
                                    break;
                                case  F :                                    
                                    airspace.typeColor = "10";                                    
                                    break;
                                case  G :                                    
                                    airspace.typeColor = "11";                                    
                                    break;
                                case  GP :                                    
                                    airspace.typeColor = "12";                                    
                                    break;
                                case  GLIDING :                                    
                                    airspace.typeColor = "13";                                    
                                    break;
                                case  GSEC :                                    
                                    airspace.typeColor = "14";                                    
                                    break;
                                case  OTH :                                    
                                    airspace.typeColor = "14";                                    
                                    break;
                                case  RESTRICTED :                                    
                                    airspace.typeColor = "15";                                    
                                    break;
                                case  R :                                    
                                    airspace.typeColor = "15";                                    
                                    break;
                                case  TMA :                                    
                                    airspace.typeColor = "6";                                    
                                    break;
                                case  TMZ :                                    
                                    airspace.typeColor = "16";                                    
                                    break;
                                case  TSA :                                    
                                    airspace.typeColor = "17";                                    
                                    break;
                                case  WAVE :                                    
                                    airspace.typeColor = "18";                                    
                                    break;
                                case  W :                                    
                                    airspace.typeColor = "19";                                    
                                    break;
                                case  PROHIBITED :                                    
                                    airspace.typeColor = "20";                                    
                                    break;
                                case  P :                                    
                                    airspace.typeColor = "20";                                    
                                    break;
                                case  FIR :                                   
                                    airspace.typeColor = "21";                                    
                                    break;
                                case  UIR :                                    
                                    airspace.typeColor = "21";                                    
                                    break;
                                case  RMZ :                                    
                                    airspace.typeColor = "22";                                    
                                    break;
                                case  Z :                                    
                                    airspace.typeColor = "23";                                    
                                    break;
                                case  ZP :                                    
                                    airspace.typeColor = "23";                                    
                                    break;
                                case  UKN:
                                    airspace.typeColor = "14";                                    
                                    break;
                             }
                        }
                        if (l.startsWith("AN ")) {
                            if (airspace != null) {
                                airspace.originalText.append(l);  
                                String sName = l.replace("AN ", "");
                                sName = sName.replace("\r","");
                                sName = sName.replace("\n","");
                                airspace.Name = sName;
                                //newAirspace = true;
                            }

                        }
                        if (l.startsWith("AH")) {
                            if (airspace != null) {
                                airspace.originalText.append(l);  
                                int iAlt = Integer.parseInt("0" + Helpers.findRegex("\\d+", l));
                                String m = Helpers.findRegex("(\\bMSL)|(\\bFL)|(\\bFT)|(\\bSFC)|(\\bUNLIM)|(\\bAGL)", l);
                                if (m.equals("UNLIM")) airspace.AltLimit_Top = 100000;
                                airspace.AltLimit_Top_Ref = Helpers.parseReference(m);
                                airspace.AltLimit_Top_Unit = Helpers.parseUnit(m);
                                if (airspace.AltLimit_Top_Ref == AltitudeReference.AGL) {
                                    airspace.AltLimit_Top_AGL = 1;
                                    String altMeter = Helpers.findRegex("\\d+[Mm]*", l);
                                    if (altMeter.contains("M") || altMeter.contains("m")) {
                                        // unit altitude is meters. Not current but possible
                                        airspace.AltLimit_Top = iAlt;
                                    } else {
                                        // unit altitude is feets.
                                        airspace.AltLimit_Top = (int) (iAlt * 0.3048);
                                    }
                                    
                                } else {
                                    switch (airspace.AltLimit_Top_Unit) {
                                    case F :
                                        airspace.AltLimit_Top = (int) (iAlt * 0.3048);
                                        break;
                                    case FL :
                                        airspace.AltLimit_Top = (int) (iAlt *100 * 0.3048);
                                        break;
                                    }
                                }
                            }
                        }
                        if (l.startsWith("AL ")) {
                            if (airspace != null) {
                                airspace.originalText.append(l);  
                                int iAlt = Integer.parseInt("0" + Helpers.findRegex("\\d+", l));
                                String m = Helpers.findRegex("(\\bMSL)|(\\bFL)|(\\bFT)|(\\bSFC)|(\\bUNLIM)|(\\bAGL)|(\\bGND)", l);
                                if (m.equals("UNLIM")) airspace.AltLimit_Top = 99999;
                                airspace.AltLimit_Bottom_Ref = Helpers.parseReference(m);
                                airspace.AltLimit_Bottom_Unit = Helpers.parseUnit(m);
                                if (airspace.AltLimit_Bottom_Ref == AltitudeReference.AGL) {
                                    airspace.AltLimit_Bottom_AGL = 1;
                                    String altMeter = Helpers.findRegex("\\d+[Mm]*", l);
                                    if (altMeter.contains("M") || altMeter.contains("m")) {
                                        // unit altitude is meters. Not current but possible
                                        airspace.AltLimit_Bottom = iAlt;
                                    } else {
                                        // unit altitude is feets.
                                        airspace.AltLimit_Bottom = (int) (iAlt * 0.3048);
                                    }
                                    
                                } else {
                                    switch (airspace.AltLimit_Bottom_Unit) {
                                        case F :
                                            airspace.AltLimit_Bottom = (int) (iAlt * 0.3048);
                                            break;
                                        case FL :
                                            airspace.AltLimit_Bottom = (int) (iAlt *100 * 0.3048);
                                            break;
                                    }
                                }
                            }
                        }
                        if (l.startsWith("V D")) {
                            airspace.originalText.append(l);  
                            cw = (Helpers.findRegex("\\+", l).equals("+"));
                            int i = 0;
                        }
                        if (l.startsWith("V X")) {
                            airspace.originalText.append(l);  
                            center = Helpers.parseOpenAirLocation(l);
                        }
                        if (l.startsWith("DA ")) {
                            airspace.originalText.append(l);  
                            String[] be = l.split(",");
                            Double begin = Double.valueOf(Helpers.findRegex("([0-9.]+\\w)|([0-9])", be[1]));
                            Double end = Double.valueOf(Helpers.findRegex("([0-9.]+\\w)|([0-9])", be[2]));
                            Double distance = Double.valueOf(Helpers.findRegex("([0-9.]+\\w)|([0-9])", be[0]));
                            airspace.coordinates.addAll(GeometricHelpers.drawArc(begin, end, distance, center, cw));
                            circle = false;
                        }
                        if (l.startsWith("DB ")) {
                            airspace.originalText.append(l);  
                            String[] be = l.split(",");
                            LatLng begin = Helpers.parseOpenAirLocation(be[0]);
                            LatLng end = Helpers.parseOpenAirLocation(be[1]);
                            airspace.coordinates.addAll(GeometricHelpers.drawArc(begin, end, center, cw));
                            circle = false;
                        }
                        if (l.startsWith("DP ")) {
                            airspace.originalText.append(l);  
                            location = Helpers.parseOpenAirLocation(l);
                            airspace.coordinates.add(new Coordinate(location.longitude, location.latitude));
                            circle = false;
                        }
                        if (l.startsWith("DC ")) {                            
                            if (airspace != null) {
                                airspace.originalText.append(l);  
                                String m = Helpers.findRegex("([0-9.]+\\w)|([0-9])", l);
                                airspace.coordinates.addAll(GeometricHelpers.drawCircle(center, Double.valueOf(m)));
                                circle = true;
                            }
                        }
                        //                if (l.startsWith("SP"))
                        //                {
                        //                    // What if SP becomes before AN ??????????????
                        //
                        //                    // We need to check if the SP is just a pen setting which means this iy does not belong to a specific airspace
                        //                    // If it does not belong to an airspace than delete this airspace
                        //                    if (!newAirspace) {
                        //                        if (airspace != null) {
                        //                            this.remove(airspace);
                        //                            airspace = null;
                        //                        }
                        //                    }
                        //                }

                    }
                }
                catch (Exception e)
                {
                    if (airspace != null)
                        this.remove(airspace);
                    e.printStackTrace();
                }
            }
            if ((airspace != null) && (airspace.coordinates.size()>0)) {
                airspace.coordinates.add(airspace.coordinates.get(0));
                airspace.computeBoundingBox();
            }
        } catch (Exception e) {
            e.printStackTrace();
            int i=0;
        }
    }

    private int getAltitude(String s) {
        int res = 0;
        
        return res;
    }
}
