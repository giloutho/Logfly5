/*
 * Copyright Gil THOMAS
 * Ce fichier fait partie intégrante du projet Logfly
 * Pour tous les détails sur la licence du projet Logfly
 * Consulter le fichier LICENSE distribué avec le code source
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
    * Code inspiré de cette page où il y a un paquet de trucs... http://www.movable-type.co.uk/scripts/latlong.html
    * Erich Lerch (Flytec) calculait avec un rayon de la terre à 6360 alors que la plupart des formules sont à 6371
    * Dans son document sur les calculs de distance, il renvoie à une page du même site http://www.movable-type.co.uk/scripts/latlong-vincenty.html
    * qui présente une formule de calcul beaucoup plus précise
    * @param pLat1
    * @param pLong1
    * @param pLat2
    * @param pLong2
    * @return Distance en mètres
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
     *   Adaptation de la fonction trouvée dans le source de Man's gps2ge.php
     *   Cela donne l'azimuth du point1 vers le point 2
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
