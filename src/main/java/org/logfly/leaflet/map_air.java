/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.logfly.leaflet;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.logging.Level;

/**
 *
 * @author gil
 */
public class map_air {
    
    private String map_HTML;
    private boolean map_OK;
    private StringBuilder sbGeoJson;
    private final String RC = "\n";
    private StringBuilder sbError;
    //    private I18n i18n; 
    
    public map_air(StringBuilder pSb) {
        map_HTML = null;
        map_OK = false;
        this.sbGeoJson = pSb;
        //this.i18n = pI18n;
        genMap();
        
    }
    
    public String getMap_HTML() {
        return map_HTML;
    }    

    public boolean isMap_OK() {
        return map_OK;
    }
        
    
    /**
    * Generation of HTML code of the map
    * @return 
    */
    private void genMap() {

        StringBuilder sbHTML = new StringBuilder();
        StringBuilder sbComment = new StringBuilder();
        String commentOk;
        
        try {
            try  {
                BufferedReader br = new BufferedReader(new InputStreamReader(map_air.class.getResourceAsStream("/skl/skl_airspaces.txt")));
                String line = null;            
                while ((line = br.readLine()) != null) {
                    sbHTML.append(line).append(RC);                    
                }
                br.close();
            } catch (IOException e) {
                sbError = new StringBuilder(this.getClass().getName()+"."+Thread.currentThread().getStackTrace()[1].getMethodName());
                sbError.append("\r\n").append(e.getMessage());
                System.out.println(sbError.toString());
               // mylogging.log(Level.SEVERE, sbError.toString()); 
            }
            if (sbHTML.length() > 500)  {
                String layerHTML = sbHTML.toString();           
                map_HTML = layerHTML.replace("%ZAgeojson%", sbGeoJson.toString());     
                map_OK = true;
            }
        } catch (Exception e) {
            map_OK = false;
        }         
    }        
    
}
