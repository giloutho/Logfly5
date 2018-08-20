/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package airspacelib;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import org.locationtech.jts.geom.Coordinate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author gil
 */

public class Helpers {
    
public static LatLng getDutchFormatPosition(String text)
    {
        // Dutch notams
        // PSN 521837N0045613E
        // PSN 5403N00627E
        // PSN 53.18.4N 003.56.8E
        LatLng latLng = null;

        try {
            if (text.contains("PSN")) {
                String[] l = text.substring(text.indexOf("PSN") + 4, text.length()).split("[EW]");
                String loc = text.substring(text.indexOf("PSN") + 4, text.indexOf("PSN") + 4 + l[0].length()+1);
                loc = loc.replace(".", "");
                loc = loc.replace(" ", "");
                String[] ll = loc.split("[NS]");
                if (ll.length>1)
                {
                    String latstr = ll[0].replaceAll("[\\D]", "");
                    String lonstr = ll[1].replaceAll("[\\D]", "");

                    if (latstr.length()==4) latstr = latstr + "00";
                    if (latstr.length()==5) latstr = latstr.substring(0, 4) + "00";

                    if (lonstr.length()==5) lonstr = lonstr + "00";
                    if (lonstr.length()==5) lonstr = lonstr.substring(0, 5) + "00";

                    Double lat = Double.valueOf(latstr.substring(0,2)) +
                            (Double.valueOf(latstr.substring(2,4)) / 60) +
                            (Double.valueOf(latstr.substring(4,6)) / 3600)
                                    * ((loc.contains("S")) ? -1 : 1);
                    Double lon = Double.valueOf(lonstr.substring(0,3)) +
                            (Double.valueOf(lonstr.substring(3,5)) / 60) +
                            (Double.valueOf(lonstr.substring(5,7)) / 3600)
                                    * ((loc.contains("W")) ? -1 : 1);
                    latLng = new LatLng(lat,lon);

                }
            }
        } catch (NumberFormatException e) {
            //e.printStackTrace();
            return null;
        }

        return latLng;
    }    
    
    
  public static LatLng parseOpenAirLocation(String location)
    {
        //Remove all the text after the * sign

        // replace DP, DB, V X=
        String l = location.replace("DP", "");
        l = l.replace("DB", "");
        l = l.replace("V X=", "");
        l = l.trim();
        l = l.replace(":.", ":");
        l = l.replace(".", ":");
        if (l.indexOf("*")>-1) l = l.substring(0, l.indexOf("*"));

        // 53:40:00 N 006:30:00 E
        String[] loc = l.split("[NSns]");

        LatLng latLng = null;
        String lat[] = loc[0].split(":");
        Double _lat = (Double.valueOf(lat[0]) +
                (Double.valueOf(lat[1]) / 60) +
                (Double.valueOf(lat[2]) / 3600))
                        * ((l.contains("S")) ? -1 : 1);
        String lon[] = loc[1].split(":");
        lon[2] = findRegex("[0-9]+", lon[2]);

        Double _lon = (Double.valueOf(lon[0]) +
                (Double.valueOf(lon[1]) / 60) +
                (Double.valueOf(lon[2]) / 3600))
                        * ((l.contains("W")) ? -1 : 1);
        latLng = new LatLng(_lat,_lon);

        return latLng;
    }       
  
    public static Coordinate getCoordinate(LatLng latLng)
    {
        return new Coordinate(latLng.longitude, latLng.latitude);
    }    
    
    public static String findRegex(String pattern, String input)
    {
        try {
            Pattern p = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE);
            Matcher matcher = p.matcher(input);
            matcher.find();
            return matcher.group();
        }
        catch (Exception e)
        {
            return "";
        }
    }    

    public static AltitudeUnit parseUnit(String value)
    {
        value = value.toUpperCase();
        if (value.equals("MSL")) return AltitudeUnit.F;
        if (value.equals("AGL")) return AltitudeUnit.F;
        if (value.equals("FT")) return AltitudeUnit.F;
        if (value.equals("FL")) return AltitudeUnit.FL;
        if (value.equals("GND") || value.equals("SFC")) return AltitudeUnit.F;

        return AltitudeUnit.F;
    }  
    
    public static AltitudeReference parseReference(String value)
    {
        value = value.toUpperCase();
        if (value.equals("MSL")) return AltitudeReference.MSL;
        if (value.equals("AGL")) return AltitudeReference.AGL;
        if (value.equals("FT")) return AltitudeReference.MSL;
        if (value.equals("FL")) return AltitudeReference.STD;
        if (value.equals("GND")|| value.equals("SFC")) return AltitudeReference.GND;

        return AltitudeReference.MSL;
    }    
    
    public static String readFromFile(String fileName) {

        String ret = "";

        try {
            FileInputStream inputStream = new FileInputStream (new File(fileName));

            if ( inputStream != null ) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);

                int size = inputStream.available();
                char[] buffer = new char[size];

                inputStreamReader.read(buffer);

                inputStream.close();
                ret = new String(buffer);
            }
        }catch (Exception e) {
            e.printStackTrace();
        }

        return ret;
    }    
    
}
