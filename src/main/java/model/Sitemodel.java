/*
 * Copyright Gil THOMAS
 * This file forms an integral part of Logfly project
 * See the LICENSE file distributed with source code
 * for details of Logfly licence project
 */
package model;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.SimpleDoubleProperty;
/**
 *
 * @author gil
 */
public class Sitemodel {
    
    public SimpleStringProperty idSite = new SimpleStringProperty();
    public SimpleStringProperty nom = new SimpleStringProperty();
    public SimpleStringProperty ville = new SimpleStringProperty();
    public SimpleStringProperty cp = new SimpleStringProperty();    
    public SimpleStringProperty pays = new SimpleStringProperty();    
    public SimpleStringProperty alt = new SimpleStringProperty();
    public SimpleStringProperty orient = new SimpleStringProperty();    
    public SimpleStringProperty type = new SimpleStringProperty();
    public SimpleDoubleProperty latitude = new SimpleDoubleProperty();
    public SimpleDoubleProperty longitude = new SimpleDoubleProperty();
     
    public String getIdSite() {
        return idSite.get();
    }
       
    public void setIdSite(String idVolStr) {    
        idSite.set(idVolStr);
    }

    public String getNom() {
        return nom.get();
    }

    public void setNom(String nomStr) {
        nom.set(nomStr);
    }
    
    public String getVille() {
        return ville.get();
    }

    public void setVille(String villeStr) {
        ville.set(villeStr);
    }    
    
    public String getCp() {
        return cp.get();
    }

    public void setCp(String cpStr) {
        cp.set(cpStr);
    }  
    
    public String getPays() {
        return pays.get();
    }

    public void setPays(String paysStr) {
        pays.set(paysStr);
    }      
    
    public String getAlt() {
        return alt.get();
    }

    public void setAlt(String altStr) {
        alt.set(altStr);
    }  
    
    public String getOrient() {
        return orient.get();
    }

    public void setOrient(String orientStr) {
        orient.set(orientStr);
    }         
    
    public String getType() {
        return type.get();
    }

    public void setType(String typeStr) {
        type.set(typeStr);
    }    

    public Double getLatitude() {
        return latitude.get();
    }

    public void setLatitude(Double latitude) {
        this.latitude.set(latitude);
    }

    public Double getLongitude() {
        return longitude.get();
    }

    public void setLongitude(Double longitude) {
        this.longitude.set(longitude);
    }
    
   
    
}
