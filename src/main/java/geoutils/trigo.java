/* 
 * Copyright Gil THOMAS
 * This file forms an integral part of Logfly project
 * See the LICENSE file distributed with source code
 * for details of Logfly licence project
 */
package geoutils;

/**
 *
 * @author Gil
 */
public class trigo {
                    
    public static double Deg2Rad(double pDeg)
    {
        return pDeg * Math.PI / 180.0;
    }
    
    public static double Rad2Deg(double pRad) {
        return (pRad / Math.PI * 180.0);
    }
    
    /**
    * Inspired by this very useful page :  http://www.movable-type.co.uk/scripts/latlong.html
    * Erich Lerch (Flytec) computed awith an earth radius of 6360 whilst most of the other formulas use 6371
    * In his document, he referred to  http://www.movable-type.co.uk/scripts/latlong-vincenty.html with a more accurate formula    
    * @param pLat1
    * @param pLong1
    * @param pLat2
    * @param pLong2
    * @return Distance in meters
    */
    public static double CoordDistance(double pLat1, double pLong1, double pLat2, double pLong2)   
    {
        double R = 6371000;   // Rayon de la terre
        double lat1 = Deg2Rad(pLat1);
        double lat2 = Deg2Rad(pLat2);
        double long1 = Deg2Rad(pLong1);
        double long2 = Deg2Rad(pLong2);
        double dLat = lat2 - lat1;
        double dLon = long2 - long1;
        double a = Math.sin(dLat/2) * Math.sin(dLat/2) + Math.cos(lat1) * Math.cos(lat2) * Math.sin(dLon/2) * Math.sin(dLon/2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        
        return R * c;
    }
    
    /**
     *   Adapted from Man's code in gps2ge.php
     *   azimuth from point1 to point 2
     * @param pLat1
     * @param pLong1
     * @param pLat2
     * @param pLong2
     * @return 
     */
    public static double CoordBearing(double pLat1, double pLong1, double pLat2, double pLong2)   {
        double res;
  
        double coords1lat, coords1lon, coords2lat, coords2lon;
        double dlat, dlon;
  
        coords1lat = Deg2Rad(pLat1);
        coords1lon = Deg2Rad(pLong1);
        coords2lat = Deg2Rad(pLat2);
        coords2lon = Deg2Rad(pLong2);
        dlat = coords2lat - coords1lat;      // delta
        dlon = coords2lon - coords1lon;      // delta
  
        // Source Man's $bearing = atan2((sin($dlon) * cos($coords2lat)) , (cos($coords1lat) * sin($coords2lat) - sin($coords1lat) * cos($coords2lat) * cos($dlon)));
        res = Math.atan2((Math.sin(dlon) * Math.cos(coords2lat)),(Math.cos(coords1lat) * Math.sin(coords2lat) - Math.sin(coords1lat) * Math.cos(coords2lat) * Math.cos(dlon)));
   
        res = Rad2Deg(res);  
        if (res < 0) res = res + 360;
        
        return res;        
    }
    
}
