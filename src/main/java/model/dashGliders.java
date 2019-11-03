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

public class dashGliders {

    public SimpleStringProperty glider = new SimpleStringProperty();
    public SimpleStringProperty flights = new SimpleStringProperty();
    public SimpleStringProperty duration = new SimpleStringProperty();
    
    public dashGliders(String pGlider, int iFlights, int iDuration) {
        this.glider = new SimpleStringProperty(pGlider);
        this.flights = new SimpleStringProperty(String.valueOf(iFlights));  
        setDuration(iDuration);
    }       

    public String getGlider() {
        return glider.get();
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
