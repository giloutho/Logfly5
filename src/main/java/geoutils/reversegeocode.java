/* 
 * Copyright Gil THOMAS
 * This file forms an integral part of Logfly project
 * See the LICENSE file distributed with source code
 * for details of Logfly licence project
 */
package geoutils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 *
 * Inspiré de http://www.smarttutorials.net/google-maps-reverse-geocoding-in-java/
 */
public class reversegeocode {
    
    private String connectGoogle(String sCoord) {
        String res = null;
        
        try {
            URL url = new URL("http://maps.googleapis.com/maps/api/geocode/json?latlng="+sCoord+"&sensor=true");
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
               // System.out.println(output);
                out+=output;
            }
            conn.disconnect();
            res = out;
        } catch (MalformedURLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        return res;
    }
    
    /**
     * Obtention de l'adresse correspondant à un point avec latitude et longitude
     * grâce à l'API Reverse Geocoding de Googlegoogle map API 
     * @param sCoord  les coordonénes doivent être préformatées sur une string : latitude + "," + longitude
     * @param retourCP  vrai ->  le code postal est intégré au résultat : 06460 Saint-Vallier-de-Thiey FR
     * @return string contenant éventuellement le code postal+espace et ville+espace+abréviation pays 
     *         -> 06460 Saint-Vallier-de-Thiey FR
     */
    public String googleGeocode(String sCoord, boolean retourCP)  {
        String res = null;        
        
        String resJson = connectGoogle(sCoord);
        if (resJson != null)  {
            try {
                JSONParser jsonParser = new JSONParser();
                JSONObject jsonObject = (JSONObject) jsonParser.parse(resJson);
                JSONArray resultat = (JSONArray) jsonObject.get("results");
                // Compliqué car il n'y a pas toujours le même nombre d'éléments dans le JSON
                // Finalement il semblerait que le deuxième élément soit toujours 
                // adresse formattée selon le schéma
                // Code Postal espace Localité virgule Pays
                // 20190 Pamukkale/Denizli, Turkey
                // 06460 Saint-Vallier-de-Thiey, France
                JSONObject jsonRes = (JSONObject) resultat.get(1);                
                res = (String) jsonRes.get("formatted_address");                                       
            } catch (ParseException ex) {
                ex.printStackTrace();
            } catch (NullPointerException ex) {
                ex.printStackTrace();
            }
        }
        
        return res;        
    }  
    
}

/**
 * Code de départ qui s'est avéré insuffisant
 * car il n'y a pas toujours le même nombre d'éléments dans le JSON
    // Sort le premier "address_components"
    JSONObject jsonRes = (JSONObject) resultat.get(0);
    // Sort le premier "address_components"
    JSONArray firstRes = (JSONArray) jsonRes.get("address_components");
    // take the elements of the json array            
    for(int i=0; i<firstRes.size(); i++){                
        JSONObject jsonAdress = (JSONObject) firstRes.get(i);
        JSONArray typesArray = (JSONArray) jsonAdress.get("types");
        String typeName = (String) typesArray.get(0);
        if(typeName.equals("locality")){
            ville = (String) jsonAdress.get("short_name");                    
        }
        if(typeName.equals("postal_code")){
            codeP = (String) jsonAdress.get("short_name");                    
        }
        if(typeName.equals("country")){
            pays = (String) jsonAdress.get("short_name");                    
        }
 */