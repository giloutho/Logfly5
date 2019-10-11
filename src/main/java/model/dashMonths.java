/*
 * Copyright Gil THOMAS
 * This file forms an integral part of Logfly project
 * See the LICENSE file distributed with source code
 * for details of Logfly licence project
 */
package model;

import java.time.LocalTime;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;

/**
 *
 * @author gil
 */
public class dashMonths {
    
    public SimpleStringProperty month = new SimpleStringProperty();
    public SimpleStringProperty flights = new SimpleStringProperty();
    public ObjectProperty<LocalTime> duree = new SimpleObjectProperty();
    
    public dashMonths(String pMonth, String pFlights, String pDuree) {
        this.month = new SimpleStringProperty(pMonth);
        this.flights = new SimpleStringProperty(pFlights);  
        setDuree(pDuree);
    }        

    public String getMonth() {
        return month.get();
    }

    public void setMonth(String monthStr) {
        month.set(monthStr);
    }
    
    public String getFlights() {
        return flights.get();
    }

    public void setFlights(String flightStr) {
        flights.set(flightStr);
    }        
 
    public LocalTime getDuree() {
        return duree.get();
    }
    
    public void setDuree(String dureeStr) {
        int seconds = Integer.parseInt(dureeStr);
        duree.set(LocalTime.ofSecondOfDay(seconds));     
    }    
    
}
