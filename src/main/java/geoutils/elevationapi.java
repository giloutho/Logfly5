/*
 * Copyright Gil THOMAS
 * This file forms an integral part of Logfly project
 * See the LICENSE file distributed with source code
 * for details of Logfly licence project
 */
package geoutils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 *
 * @author gil
 */
public class elevationapi {
    
    /**
     * Called raceElevation because we used free api from racemap.com 
     * https://github.com/racemap/elevation-service
     * @param sLat
     * @param sLong
     * @return 
     */
    public static int raceElevation(String sLat, String sLong) {
        int res = -100; 
        
        try {
            String ELEVATION_API_URL =  "https://elevation.racemap.com/api/";
            String USER_AGENT = "Mozilla/5.0";
            StringBuilder urlParameters = new StringBuilder();
            urlParameters.append("lat=").append(sLat).append("&lng=").append(sLong);
            URL url = new URL(ELEVATION_API_URL + "?" + urlParameters.toString());
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("User-Agent", USER_AGENT);
            conn.setDoOutput(true);
            System.out.println("reponse : "+conn.getResponseCode());
            if (conn.getResponseCode() != 200) {
                    throw new RuntimeException("Failed : HTTP error code : "+ conn.getResponseCode());
            } else {
                // Reading data's from url
                BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));
                String output;
                String out="";            
                while ((output = br.readLine()) != null) {
                    out+=output;
                }
                conn.disconnect();
                String[] tbOut = out.split("\\.");
                if (tbOut.length > 1) {
                    res = Integer.parseInt(tbOut[0]);
                } else {
                    res = -200;
                }
            }        
        } catch (Exception e) {
            res = -1000;
        }
        
        return res;
        
    }
}
