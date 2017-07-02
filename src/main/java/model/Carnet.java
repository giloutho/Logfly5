/*
 * Copyright Gil THOMAS
 * Ce fichier fait partie intégrante du projet Logfly
 * Pour tous les détails sur la licence du projet Logfly
 * Consulter le fichier LICENSE distribué avec le code source
 */
package model;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.image.Image;

/**
 *
 * @author Gil Thomas logfly.org
 */

public class Carnet {
    public SimpleStringProperty idVol = new SimpleStringProperty();
    public SimpleStringProperty date = new SimpleStringProperty();
    public SimpleStringProperty heure = new SimpleStringProperty();
    public SimpleStringProperty duree = new SimpleStringProperty();
    public SimpleStringProperty site = new SimpleStringProperty();
    public SimpleStringProperty engin = new SimpleStringProperty();
    public SimpleStringProperty altiDeco = new SimpleStringProperty();
    public SimpleStringProperty latDeco = new SimpleStringProperty();
    public SimpleStringProperty longDeco = new SimpleStringProperty();
    public SimpleBooleanProperty Comment = new SimpleBooleanProperty();
    public SimpleStringProperty comTexte = new SimpleStringProperty();
    public SimpleBooleanProperty Photo = new SimpleBooleanProperty();  
    public SimpleStringProperty camera = new SimpleStringProperty();    
    
    public String getIdVol() {
        return idVol.get();
    }
    
    public void setIdVol(String idVolStr) {    
        idVol.set(idVolStr);
    }

    public String getDate() {
        return date.get();
    }
    
    public void setDate(String dateStr) {    
        // Ds la db la date est de la forme YYYY-MM-DD HH:MM:SS
        date.set(dateStr.substring(0,10));
    }

    public String getHeure() {
        return heure.get();
    }
    
    public void setHeure(String dateStr) {
        // Ds la db la date est de la forme YYYY-MM-DD HH:MM:SS
        heure.set(dateStr.substring(11,16));
    }
    
    public String getDuree() {
        return duree.get();
    }
    
    public void setDuree(String dureeStr) {
        duree.set(dureeStr);
    }
    
    public String getSite() {
        return site.get();
    }
    
    public void setSite(String siteStr) {
        site.set(siteStr);
    }
    
    public String getEngin() {
        return engin.get();
    }
    
    public void setEngin(String enginStr) {
        engin.set(enginStr);
    }
    
    public String getAltiDeco() {
        return altiDeco.get();
    }
    
    public void setAltiDeco(String altiDecoStr) {
        altiDeco.set(altiDecoStr);
    }
    
    public String getLatDeco() {
        return latDeco.get();
    }
    
    public void setLatDeco(String latDecoStr) {
        latDeco.set(latDecoStr);
    }
    
    public String getLongDeco() {
        return longDeco.get();
    }
    
    public void setLongDeco(String longDecoStr) {
        longDeco.set(longDecoStr);
    }

    public boolean getComment() {
        return Comment.get();
    }

    public void setComment(boolean bComment) {
        Comment.set(bComment);
    }

    public String getComTexte() {
        return comTexte.get();
    }

    public void setComTexte(String comTexteStr) {
        comTexte.set(comTexteStr);
    }
        
    public boolean getPhoto() {
        return Photo.get();
    }

    public void setPhoto(boolean bPhoto) {
        Photo.set(bPhoto);
    }

    
    public String getCamera() {
        return camera.get();
    }

    public void setCamera(String cameraStr) {
        camera.set(cameraStr); 
    }                    
}
