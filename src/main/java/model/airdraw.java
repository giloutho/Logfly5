/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package model;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

/**
 *
 * @author gil
 */
public class airdraw {

    public SimpleStringProperty category = new SimpleStringProperty();
    public SimpleStringProperty name = new SimpleStringProperty();
    public SimpleIntegerProperty floor = new SimpleIntegerProperty();
    public SimpleIntegerProperty ceiling = new SimpleIntegerProperty();    
    public SimpleStringProperty oatext = new SimpleStringProperty();      
    
    public airdraw(String pName, String pOatext) {
        this.name = new SimpleStringProperty(pName);
        this.oatext = new SimpleStringProperty(pOatext);             
    }    
    
    public String getCategory() {
        return category.get();
    }
    
    public void setCategory(String categoryStr) {
        category.set(categoryStr);
    }   

    public String getName() {
        return name.get();
    }
    
    public void setName(String nameStr) {
        name.set(nameStr);
    }       
    
    public String getOatext() {
        return oatext.get();
    }   
    
    public void setFloor(int floorInt) {
        floor.set(floorInt);
    }
    
    public Integer getFloor() {        
        return floor.get();
    }
    
    public void setCeiling(int ceilingInt) {
        ceiling.set(ceilingInt);
    }
    
    public Integer getCeiling() {        
        return ceiling.get();
    }    
}
