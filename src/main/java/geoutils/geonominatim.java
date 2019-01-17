/*
 * Copyright Gil THOMAS
 * This file forms an integral part of Logfly project
 * See the LICENSE file distributed with source code
 * for details of Logfly licence project
 */
package geoutils;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.logging.Level;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import model.Sitemodel;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import systemio.mylogging;

/**
 *
 * @author gil
 */
public class geonominatim {
    
    private String geoVille = "";
    private String geoCP = "";
    private String geoCodepays = "";
    private String geoPays = "";    
    private String geoLat = "";
    private String geoLong = ""; 
    private String geoStatus = "";
    private int geoError = 0;
    private ObservableList <Sitemodel> osmTowns; 
    
    private StringBuilder sbError;    
    
    public geonominatim() {
        osmTowns = FXCollections.observableArrayList(); 
    }

    public String getGeoVille() {
        return geoVille;
    }

    public String getGeoCP() {
        return geoCP;
    }

    public String getGeoPays() {
        return geoPays;
    }

    public String getGeoCodepays() {
        return geoCodepays;
    }
    
    public String getGeoLat() {
        return geoLat;
    }

    public String getGeoLong() {
        return geoLong;
    }
        
    public String getGeoStatus() {
        return geoStatus;
    }    

    public int getGeoError() {
        return geoError;
    }

    public ObservableList<Sitemodel> getOsmTowns() {
        return osmTowns;
    }
        
        
    public void askReverseGeo(String sLat, String sLong) {
        
        String response = null;
        URL url = null;
        StringBuilder sbUrl = new StringBuilder();
        sbUrl.append("https://nominatim.openstreetmap.org/reverse?format=json&email=contactATlogfly.org");      
        sbUrl.append("&lat=").append(sLat).append("&lon=").append(sLong).append("&addressdetails=1");
        try {       
            url = getURL(sbUrl.toString());
            if (url != null) {
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                response = inputStreamToString(httpURLConnection.getInputStream());
                decodeReverseRep(response);
            }
        } catch (Throwable th) {
            sbError = new StringBuilder(this.getClass().getName()+"."+Thread.currentThread().getStackTrace()[1].getMethodName());
            sbError.append("\r\n").append("Can't get response from web location ").append(url).append(" ").append(th);
            geoError = 310;    // Can't get response from web service
            mylogging.log(Level.SEVERE, sbError.toString());               
        }
        
    }            
    
    private void decodeReverseRep(String totalAnswer) {
        if (totalAnswer.contains("place_id")) {
            try {
                JSONParser jsonParser = new JSONParser();
                JSONObject jsonObject = (JSONObject) jsonParser.parse(totalAnswer);
                JSONObject adress = (JSONObject) jsonObject.get("address");
                if (adress.get("city") != null) {
                    geoVille = adress.get("city").toString();  
                } else if (adress.get("village") != null) {
                    geoVille = adress.get("village").toString(); 
                } else {
                    geoVille = "";
                }
                geoCodepays = adress.get("country_code").toString().toUpperCase();
                geoPays = adress.get("country").toString();
                geoCP = adress.get("postcode").toString();
                geoStatus = "OK";
            } catch (ParseException ex) {
                sbError = new StringBuilder(this.getClass().getName()+"."+Thread.currentThread().getStackTrace()[1].getMethodName());
                sbError.append("\r\n").append("Can't parse ").append(totalAnswer);
                mylogging.log(Level.SEVERE, sbError.toString());                                    
            } catch (NullPointerException ex) {
                sbError = new StringBuilder(this.getClass().getName()+"."+Thread.currentThread().getStackTrace()[1].getMethodName());
                sbError.append("\r\n").append("Can't parse ").append(totalAnswer);
                geoError = 4;   // Parsing error
                mylogging.log(Level.SEVERE, sbError.toString());  
            }                    
        } else {
            geoStatus = totalAnswer;
        }
    }    
    
    private URL getURL(String urlString) {
        try {
            return new URL(urlString);
        } catch (Throwable th) {
            sbError = new StringBuilder(this.getClass().getName()+"."+Thread.currentThread().getStackTrace()[1].getMethodName());
            sbError.append("\r\n").append("Can't build url").append(th);
            mylogging.log(Level.SEVERE, sbError.toString());              
            return null;
        }
    }    
    
    private String inputStreamToString(InputStream inputStream) {
        try {
            if (inputStream == null) {
                return null;
            }
            StringBuilder stringBuilder = new StringBuilder();
            String line;
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            while ((line = bufferedReader.readLine()) != null) {
                stringBuilder.append(line);
            }
            return stringBuilder.toString();
        } catch (Throwable th) {
            sbError = new StringBuilder(this.getClass().getName()+"."+Thread.currentThread().getStackTrace()[1].getMethodName());
            sbError.append("\r\n").append("Something happened while reading from input stream").append(th);
            mylogging.log(Level.SEVERE, sbError.toString());                
            return null;
        } finally {
            try {
                inputStream.close();
            } catch (Throwable th) {
                sbError = new StringBuilder(this.getClass().getName()+"."+Thread.currentThread().getStackTrace()[1].getMethodName());
                sbError.append("\r\n").append("Can't close input stream").append(th);
                mylogging.log(Level.SEVERE, sbError.toString());                  
            }
        }
    }    

    public void askGeo( String pName) {
        
        String response = null;
        URL url = null;
        StringBuilder sbUrl = new StringBuilder();
        sbUrl.append("https://nominatim.openstreetmap.org/search?q=,+");
        sbUrl.append(pName);
        sbUrl.append("&format=json&polygon=0&addressdetails=1&email=contactATlogfly.org");
        try {       
            url = getURL(sbUrl.toString());
            if (url != null) {
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                response = inputStreamToString(httpURLConnection.getInputStream());
                decodeGeoRep(response);
            }
        } catch (Throwable th) {
            sbError = new StringBuilder(this.getClass().getName()+"."+Thread.currentThread().getStackTrace()[1].getMethodName());
            sbError.append("\r\n").append("Can't get response from web location ").append(url).append(" ").append(th);
            geoError = 310;  // No response from web service
            mylogging.log(Level.SEVERE, sbError.toString());               
        }                
    }    
    
    private void decodeGeoRep(String resJson) {
       
        if (resJson != null)  {
            try {       
                JSONParser jsonParser = new JSONParser();                
                JSONArray resultat = (JSONArray) jsonParser.parse(resJson);
                for (int i = 0; i < resultat.size(); i++) {
                    JSONObject oneAd = (JSONObject) resultat.get(i);                    
                    JSONObject adress = (JSONObject) oneAd.get("address");  
                    Sitemodel osmCity = new Sitemodel();
                    if (adress.get("postcode") != null) {
                        geoCP = adress.get("postcode").toString();
                        if (geoCP != null) {
                            osmCity.setCp(geoCP);
                        }
                    }
                    geoPays = null;
                    if (adress.get("country") != null) {
                        geoPays = adress.get("country").toString();                    
                        if (geoPays != null) {
                            osmCity.setPays(geoPays);
                        }
                    }
                    geoVille = null;
                    if (adress.get("city") != null) {
                        geoVille = adress.get("city").toString();                       
                    } else if (adress.get("village") != null) {
                        geoVille = adress.get("village").toString();                        
                    } 
                    if (geoVille != null && geoPays != null) {
                        osmCity.setVille(geoVille);
                        Double dLat = 0.00;
                        Double dLong = 0.00;
                        if (oneAd.get("lat") != null) {
                            String sLat = oneAd.get("lat").toString();
                            dLat = Double.valueOf(sLat);
                            if (oneAd.get("lon") != null) {
                                dLong = Double.valueOf(oneAd.get("lon").toString());                            
                            }
                            osmCity.setLatitude(dLat);
                            osmCity.setLongitude(dLong);
                            osmTowns.add(osmCity);
                        }                                                  
                    }                                      
                }
                if (resultat.size() < 1) {
                    geoError = 320;  // Unusable response from web service
                }
            } catch (Exception ex) {
                sbError = new StringBuilder(this.getClass().getName()+"."+Thread.currentThread().getStackTrace()[1].getMethodName());
                sbError.append("\r\n").append("Can't parse ").append(resJson);
                geoError = 4; // Parsing error
                mylogging.log(Level.SEVERE, sbError.toString());  
            } 
        } else {
            sbError = new StringBuilder(this.getClass().getName()+"."+Thread.currentThread().getStackTrace()[1].getMethodName());
            sbError.append("\r\n").append("Json is null");
            geoError = 330;   // Json is null
            mylogging.log(Level.SEVERE, sbError.toString());  
        }                
    }
}
