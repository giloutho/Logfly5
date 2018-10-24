/*
 * Copyright Gil THOMAS
 * This file forms an integral part of Logfly project
 * See the LICENSE file distributed with source code
 * for details of Logfly licence project
 */
package org.logfly.waypio;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

/**
 *
 * @author gil
 */
public class pointRecord {
    
    private SimpleStringProperty fBalise;
    private SimpleStringProperty fAlt;
    private SimpleStringProperty fDesc;
    private SimpleStringProperty fLat;
    private SimpleStringProperty fLong;
    private SimpleIntegerProperty fIndex;               

    public pointRecord(String pBalise, String pAlt, String pDesc){
        this.fBalise = new SimpleStringProperty(pBalise);
        this.fAlt = new SimpleStringProperty(pAlt);
        this.fDesc = new SimpleStringProperty(pDesc); 
        this.fLat = new SimpleStringProperty();
        this.fLong = new SimpleStringProperty();
        this.fIndex = new SimpleIntegerProperty();
    }

    public String getFBalise() {
        return fBalise.get();
    }      

    public void setFBalise(String pBalise) {
        fBalise.set(pBalise);
    }          

    public String getFAlt() {
        return fAlt.get();
    }

    public void setFAlt(String pAlt) {
        fAlt.set(pAlt);
    }          

    public String getFDesc() {
        return fDesc.get();
    }            

    public void setFDesc(String pDesc) {
        fDesc.set(pDesc);
    }          

    public String getFLat() {
        return fLat.get();
    }

    public void setFLat(String pLat) {
        fLat.set(pLat);
    }        

    public String getFLong() {
        return fLong.get();
    }

    public void setFLong(String pLong) {
        fLong.set(pLong);
    }

    public int getFIndex() {
        return fIndex.get();
    }

    public void setFIndex(int pIndex) {    
        fIndex.set(pIndex);
    }            
}
