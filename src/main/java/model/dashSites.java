/*
 * Copyright Gil THOMAS
 * This file forms an integral part of Logfly project
 * See the LICENSE file distributed with source code
 * for details of Logfly licence project
 */
package model;

import javafx.beans.property.SimpleStringProperty;

/**
 *
 * @author gil
 */
public class dashSites {
    
    public SimpleStringProperty site = new SimpleStringProperty();
    public SimpleStringProperty flights = new SimpleStringProperty();
    public SimpleStringProperty duration = new SimpleStringProperty();    
    
    public dashSites(String pSite, int iFlights, int iDuration) {
        this.site = new SimpleStringProperty(pSite);
        this.flights = new SimpleStringProperty(String.valueOf(iFlights));  
        setDuration(iDuration);
    }         
    public String getSite() {
        return site.get();
    }
    
    public String getFlights() {
        return String.format("%4s", flights.get());
    } 
     
    public String getDuration() {
        return duration.get();
    }
        
    public void setDuration(int iDuration) {
        if (iDuration == 0) {
            duration.set("");
        } else {
            int nbHour = iDuration/3600;
            int nbMn = (iDuration - (nbHour*3600))/60;
            StringBuilder sbDur = new StringBuilder();
            sbDur.append(String.format("%3d", nbHour)).append("h");
            sbDur.append(String.format("%02d", nbMn)).append("mn");
            duration.set(sbDur.toString());   
        }
    }        
}
