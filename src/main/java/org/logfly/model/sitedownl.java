/*
 * Copyright Gil THOMAS
 * This file forms an integral part of Logfly project
 * See the LICENSE file distributed with source code
 * for details of Logfly licence project
 */
package org.logfly.model;

import javafx.beans.property.SimpleStringProperty;

/**
 *
 * @author gil
 */
public class sitedownl {
    
    public SimpleStringProperty pays = new SimpleStringProperty();
    public SimpleStringProperty region = new SimpleStringProperty();
    public SimpleStringProperty origine = new SimpleStringProperty();
    public SimpleStringProperty fichier = new SimpleStringProperty();
    
    public sitedownl(String pPays, String pRegion, String pOrigine, String pFichier) {
        this.pays = new SimpleStringProperty(pPays);
        this.region = new SimpleStringProperty(pRegion);
        this.origine = new SimpleStringProperty(pOrigine);
        this.fichier = new SimpleStringProperty(pFichier);                
    }

    public String getPays() {
        return pays.get();
    }

    public String getRegion() {
        return region.get();
    }

    public String getOrigine() {
        return origine.get();
    }

    public String getFichier() {
        return fichier.get();
    }        
    
}
