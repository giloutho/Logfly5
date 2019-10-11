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
public class dashGliders {

    public SimpleStringProperty glider = new SimpleStringProperty();
    public SimpleIntegerProperty flights = new SimpleIntegerProperty();
    public ObjectProperty<LocalTime> duree = new SimpleObjectProperty();
    
    public dashGliders(String pGlider, int pFlights, String pDuree) {
        this.glider = new SimpleStringProperty(pGlider);
        this.flights = new SimpleIntegerProperty(pFlights);  
        setDuree(pDuree);
    }    

    public String getMonth() {
        return glider.get();
    }

    public void setMonth(String monthStr) {
        glider.set(monthStr);
    }
    
    public int getFlights() {
        return flights.get();
    }

    public void setFlights(int pIdx) {
        flights.set(pIdx);
    }        
 
    public LocalTime getDuree() {
        return duree.get();
    }
    
    public void setDuree(String dureeStr) {
        int seconds = Integer.parseInt(dureeStr);
        duree.set(LocalTime.ofSecondOfDay(seconds));     
    }    
        
    
}
