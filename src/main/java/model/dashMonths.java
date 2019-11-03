/*
 * Copyright Gil THOMAS
 * This file forms an integral part of Logfly project
 * See the LICENSE file distributed with source code
 * for details of Logfly licence project
 */
package model;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

/**
 *
 * @author gil
 */
public class dashMonths {
    
    public SimpleStringProperty month = new SimpleStringProperty();
    public SimpleStringProperty flights = new SimpleStringProperty();
    public SimpleStringProperty duration = new SimpleStringProperty();
    public SimpleIntegerProperty Seconds = new SimpleIntegerProperty();
    
    public dashMonths(String pMonth, int iFlights, int iDuration) {
        this.month = new SimpleStringProperty(pMonth);
        this.flights = new SimpleStringProperty(String.valueOf(iFlights));  
        this.Seconds = new SimpleIntegerProperty(iDuration);
        setDuration(iDuration);
    }       

    public String getMonth() {
        return String.format("%9s", month.get());
    }

    public void setMonth(String monthStr) {
        month.set(monthStr);
    }
    
    public String getFlights() {
        return String.format("%4s", flights.get());
    }
    
    public int getIntFlights() {
        return Integer.parseInt(flights.get());
    }

    public void setFlights(String flightStr) {
        flights.set(flightStr);
    }        
     
    public String getDuration() {
        return duration.get();
    }
    
    public int getSeconds() {
        return Seconds.get();
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
