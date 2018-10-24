/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.logfly.model;

/**
 *
 * @author gil
 */
public class airspacetree {
    
    private String name;  
    private boolean selected;    
    
    public airspacetree(String pName, boolean pSelected) {
        this.name = pName;
        this.selected = pSelected;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }        

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }
    
    @Override
    public String toString() {
        return this.name;
    }
    
}
