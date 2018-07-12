/*
 * Copyright Gil THOMAS
 * This file forms an integral part of Logfly project
 * See the LICENSE file distributed with source code
 * for details of Logfly licence project
 */
package model;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;

/**
 *
 * @author gil
 */
public class Balisemodel {
    
    public SimpleStringProperty nomLong = new SimpleStringProperty();    
    public SimpleStringProperty nomCourt = new SimpleStringProperty();
    public SimpleStringProperty latitude = new SimpleStringProperty();
    public SimpleStringProperty longitude = new SimpleStringProperty(); 
    public SimpleBooleanProperty withCoord = new SimpleBooleanProperty();
    
    public String getNomLong() {
        return nomLong.get();
    }

    public void setNomLong(String orientStr) {
        nomLong.set(orientStr);
    }         
    
    public String getNomCourt() {
        return nomCourt.get();
    }

    public void setNomCourt(String typeStr) {
        nomCourt.set(typeStr);
    }    

    public String getLatitude() {
        return latitude.get();
    }

    public void setLatitude(String latitude) {
        this.latitude.set(latitude);
    }

    public String getLongitude() {
        return longitude.get();
    }

    public void setLongitude(String longitude) {
        this.longitude.set(longitude);
    }    
    
    public SimpleBooleanProperty withCoordProperty() {
        return this.withCoord;
    }
    
    public java.lang.Boolean getWithCoord() {
        return this.withCoordProperty().get();
    }
    
    public void setWithCoord(final java.lang.Boolean withcoord) {
        this.withCoordProperty().set(withcoord);
    }          
    
}
