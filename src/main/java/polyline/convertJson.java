/*
 * Copyright Gil THOMAS
 * This file forms an integral part of Logfly project
 * See the LICENSE file distributed with source code
 * for details of Logfly licence project
 */
package polyline;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.HashMap;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

/**
 *
 * @author gil
 * Scoring is written in json format
 * json is converted to a Google maps encoded polyline
 */
public class convertJson {
    
    public String scorePoint(String jsonScore, Long lDureeVol) {
        
        String res = null;
        Track trk = new Track();        
        DecimalFormatSymbols decimalFormatSymbols = new DecimalFormatSymbols();    
        decimalFormatSymbols.setDecimalSeparator('.'); 
        DecimalFormat df2 = new DecimalFormat("#0.00000", decimalFormatSymbols);
        DecimalFormat df3 = new DecimalFormat("##0.00000", decimalFormatSymbols);
        String sLat;
        String googLat;
        double dLat;
        String sLong;
        String googLong;
        double dLong;
        boolean isTriangle = false;
        String sLocation=null;
        
        try {
            JSONParser jsonParser = new JSONParser();
            JSONObject jsonObject = (JSONObject) jsonParser.parse(jsonScore);
            JSONObject score = (JSONObject) jsonObject.get("drawScore");
            String scLeague =  score.get("scoreLeague").toString();
            String sShape = score.get("scoreShape").toString().trim();
            String sDistance = score.get("scoreDistance").toString();
            switch (sShape) {                
                case "FAI Triangle" :
                case "Flat Triangle" :
                    isTriangle = true;                       
                    break;                    
            }
            JSONArray tabPoints = (JSONArray) jsonObject.get("drawPoints");
            if (tabPoints != null)  {               
                for(int i=0; i<tabPoints.size(); i++){ 
                    String[] arCoord = tabPoints.get(i).toString().split(",");
                    if (arCoord.length > 1) {
                        sLat = arCoord[0].replace("[", "");
                        // To avoid errors with Google maps API,
                        // we must limit number of decimals (max 6)
                        // With points we have currently 9 decimals
                        dLat = Double.parseDouble(sLat);   
                        googLat = df2.format(dLat);
                        sLong = arCoord[1].replace("[", "");
                        dLong = Double.parseDouble(sLong);   
                        googLong = df3.format(dLong);
                        trk.addTrackpoint(new Trackpoint(new Double(googLat), new Double(googLong)));
                    }
                }
            }
            if (trk.getLength() > 0) {
                // Compute speed
                String sSpeed;
                try {
                    double dDist = Double.valueOf(sDistance);
                    double speed = (dDist / lDureeVol) * 3600;     // lDureeVol -> duration in seconds
                    // we want point as decimal separator  
                    DecimalFormat decimalFormat = new DecimalFormat("###.0", decimalFormatSymbols);                    
                    sSpeed = "&s="+decimalFormat.format(speed);
                } catch (Exception e) {
                    sSpeed = null;            
                }
                // league encoding
                String sLeague;
                switch (scLeague) {
                    case "FR" :
                        sLeague = "&l=fr";
                        break;
                    case "XC":
                         sLeague = "&l=xc";  
                        break;
                    default:
                         sLeague = null; 
                }              
                Encoder enc = new Encoder();
                HashMap hm = enc.createEncodings(trk, 17, 1);
                String rawLine = hm.get("encodedPoints").toString();
                StringBuilder sb = new StringBuilder();
                if (sLeague != null) sb.append(sLeague);
                if (sSpeed != null) sb.append(sSpeed);
                if (rawLine != null) sb.append("&p=").append(rawLine);
                res = sb.toString();
            }
        } catch (Exception e) {
            res = null;
        }        
        
        return res;
    }
    
}
