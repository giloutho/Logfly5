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
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import settings.privateData;

/**
 *
 * @author gil
 */
public class googlegeo {
    
    
    private String geoVille = "";
    private String geoCP = "";
    private String geoPays = "";    
    private String geoLat = "";
    private String geoLong = ""; 
    private String geoAlt = "";
    private String geoStatus = "";

    public String getGeoVille() {
        return geoVille;
    }

    public String getGeoCP() {
        return geoCP;
    }

    public String getGeoPays() {
        return geoPays;
    }

    public String getGeoLat() {
        return geoLat;
    }

    public String getGeoLong() {
        return geoLong;
    }

    public String getGeoAlt() {
        return geoAlt;
    }
        
    public String getGeoStatus() {
        return geoStatus;
    }
                
    private String askReverseGeo(String sCoord) {
        String res = null;
        
        try {
            //https://maps.googleapis.com/maps/api/geocode/json?latlng=40.714224,-73.961452&key=YOUR_API_KEY            
            URL url = new URL("https://maps.googleapis.com/maps/api/geocode/json?latlng="+sCoord+"&key="+privateData.geocodeKey.toString());            
            // making connection
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/json");
            if (conn.getResponseCode() != 200) {
                    throw new RuntimeException("Failed : HTTP error code : "
                                    + conn.getResponseCode());
            }

            // Reading data's from url
            BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));

            String output;
            String out="";            
            while ((output = br.readLine()) != null) {
                out+=output;
            }
            conn.disconnect();
            res = out; 
        } catch (Exception e) {
            geoStatus = "No answer from Google service";
        }
        
        return res;
    }        
    
    /**
     *  Pour affiner le décodage il faudra aller voir sur cette page
     *  https://developers.google.com/maps/documentation/geocoding/intro?hl=fr#GeocodingResponses 
     * @param sCoord
     * @return 
     */
    public int googleReverseGeo(String sCoord) {
        int res = -1;
        String googVille = null;
        String googCP = null;
        String googPays = null;
        
        String resJson = askReverseGeo(sCoord);
        if (resJson != null)  {
            try {
                JSONParser jsonParser = new JSONParser();           
                JSONObject jsonObject = (JSONObject) jsonParser.parse(resJson);
                String status = (String) jsonObject.get("status");
                geoStatus = status; 
                if (status.equals("OK")) {
                    JSONObject localityObject = new JSONObject();
                    JSONArray resultat = (JSONArray) jsonObject.get("results");   
                    parcoursJson:
                    for (int i = 0; i < resultat.size(); i++) {
                        JSONObject jsonRes = (JSONObject) resultat.get(i);
                        JSONArray adressComp = (JSONArray) jsonRes.get("address_components");  
                        for (int j = 0; j < adressComp.size(); j++) {
                            JSONObject adressRes = (JSONObject) adressComp.get(j);
                            JSONArray adressTypes = (JSONArray) adressRes.get("types");
                            for (int k = 0; k < adressTypes.size(); k++) {                           
                                if(adressTypes.get(k).toString().equals("locality")) {
                                    googVille = adressRes.get("short_name").toString();
                                }
                                if(adressTypes.get(k).toString().equals("country")) {
                                   googPays = adressRes.get("short_name").toString();
                                }                           
                                if(adressTypes.get(k).toString().equals("postal_code")) {
                                   googCP = adressRes.get("short_name").toString();
                                }                                      
                                if (googCP != null && googVille != null && googPays != null) {
                                    break parcoursJson;    
                                }        
                            }
                        }
                    }
                    if (googCP != null) geoCP = googCP;
                    if (googVille != null) geoVille = googVille;
                    if (googPays != null) geoPays = googPays;
                    res = 0;
                } 
            } catch (ParseException ex) {
                geoStatus = ex.getMessage();                
            } catch (NullPointerException ex) {
                geoStatus = ex.getMessage(); 
            }
        } else {
            geoStatus = "No answer from Google service";
        }  
        
        return res;
    }
    
    private String askGeocode(String sName) {
        String res = null;
        
        try {
            URL url = new URL("https://maps.googleapis.com/maps/api/geocode/json?address="+sName+"&key="+privateData.geocodeKey.toString());            
            // making connection
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/json");
            if (conn.getResponseCode() != 200) {
                    throw new RuntimeException("Failed : HTTP error code : "
                                    + conn.getResponseCode());
            }

            // Reading data's from url
            BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));

            String output;
            String out="";            
            while ((output = br.readLine()) != null) {
                out+=output;
            }
            conn.disconnect();
            res = out;
        } catch (Exception e) {
            geoStatus = "No answer from Google service";  
        }
        
        return res;
    }    
    
    /**
     * from https://stackoverflow.com/questions/11202934/get-latitude-longitude-from-given-address-name-not-geocoder
     * @param pName 
     */
    public int googleLatLong( String pName) {
        
        int res = -1;
        String resJson = askGeocode(pName);
        if (resJson != null)  {
            try {
                JSONParser jsonParser = new JSONParser();
                JSONObject jsonObject = (JSONObject) jsonParser.parse(resJson);
                String status = (String) jsonObject.get("status");
                geoStatus = status; 
                if (status.equals("OK")) {          
                
                    JSONArray resultat = (JSONArray) jsonObject.get("results");

                    JSONObject geoMetryObject = new JSONObject();
                    JSONObject locations = new JSONObject();

                    int i;
                    for (i = 0; i < resultat.size(); i++) {
                        JSONObject jsonRes = (JSONObject) resultat.get(i);
                        geoMetryObject = (JSONObject) jsonRes.get("geometry");

                        locations = (JSONObject) geoMetryObject.get("location");
                        geoLat = locations.get("lat").toString();
                        geoLong = locations.get("lng").toString();
                        res = 0;
                    } 
                } 
            } catch (Exception e) {
                geoStatus = e.getMessage();
            }
        } else {
            geoStatus = "No answer from Google service";
        }  
        
        return res;
        
    }    
    
    private String askGoogleElevation(String sCoord) {
        String res = null;
        try {
            String ELEVATION_API_URL =  "https://maps.googleapis.com/maps/api/elevation/json";
            String USER_AGENT = "Mozilla/5.0";
            String urlParameters = "locations="+sCoord+"&sensor=true&key="+privateData.elevationKey.toString();

            URL url = new URL(ELEVATION_API_URL + "?" + urlParameters);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("User-Agent", USER_AGENT);
            conn.setDoOutput(true);
            if (conn.getResponseCode() != 200) {
                    throw new RuntimeException("Failed : HTTP error code : "
                                    + conn.getResponseCode());
            }

            // Reading data's from url
            BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));

            String output;
            String out="";            
            while ((output = br.readLine()) != null) {
                out+=output;
            }
            conn.disconnect();
            res = out;
        } catch (Exception e) {
            geoStatus = "No answer from Google service";
        }        
        
        return res;
    }    

    public int googleElevation(String sCoord) {

        int res = -1;
        String resJson = askGoogleElevation(sCoord);  
        if (resJson != null)  {
            try {
                JSONParser jsonParser = new JSONParser();
                JSONObject elevationObject = new JSONObject();
                JSONObject jsonObject = (JSONObject) jsonParser.parse(resJson);
                String status = (String) jsonObject.get("status");
                if (status.equals("OK")) {
                    JSONObject localityObject = new JSONObject();
                    JSONArray resultat = (JSONArray) jsonObject.get("results");   
                    for (int i = 0; i < resultat.size(); i++) {
                        JSONObject jsonRes = (JSONObject) resultat.get(i);
                        Double elevation = (Double) jsonRes.get("elevation"); 
                        geoAlt = String.format("%4.0f",elevation);
                        res = 0;
                    }
                } else {
                    geoStatus = status;
                }
            } catch (Exception e) {                
                geoStatus = e.getMessage();
            }
        } else {
            geoStatus = "No answer from Google service";
        }    
        
        return res;
    }    
    
}
