/*
 * Copyright Gil THOMAS
 * Ce fichier fait partie intégrante du projet Logfly
 * Pour tous les détails sur la licence du projet Logfly
 * Consulter le fichier LICENSE distribué avec le code source
 */
package model;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;

/**
 *
 * @author gil
 */
public class Import {
    
    public SimpleBooleanProperty checkedFile = new SimpleBooleanProperty();
    public SimpleStringProperty date = new SimpleStringProperty();
    public SimpleStringProperty heure = new SimpleStringProperty();
    public SimpleStringProperty fileName = new SimpleStringProperty();
    public SimpleStringProperty pilotName = new SimpleStringProperty();
    public SimpleStringProperty filePath = new SimpleStringProperty();
    
    public SimpleBooleanProperty checkedProperty() {
        return this.checkedFile;
    }
    
    public java.lang.Boolean getChecked() {
        return this.checkedProperty().get();
    }
    
    public void setChecked(final java.lang.Boolean checked) {
        this.checkedProperty().set(checked);
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
    
    public String getFileName() {
        return fileName.get();
    }
    
    public void setFileName(String fileNameStr) {
        fileName.set(fileNameStr);
    }
    
    public String getPilotName() {
        return pilotName.get();
    }
    
    public void setPilotName(String pilotNameStr) {
        pilotName.set(pilotNameStr);
    }
  
    public String getFilePath() {
        return filePath.get();
    }
    
    public void setFilePath(String filePathStr) {
        filePath.set(filePathStr);
    }
    
}
