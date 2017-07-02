/*
 * Copyright Gil THOMAS
 * Ce fichier fait partie intégrante du projet Logfly
 * Pour tous les détails sur la licence du projet Logfly
 * Consulter le fichier LICENSE distribué avec le code source
 */
package model;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;

/**
 *
 * @author gil
 */
public class Gpsmodel {
    
    public SimpleBooleanProperty checkedFile = new SimpleBooleanProperty();
    public SimpleStringProperty date = new SimpleStringProperty();
    public SimpleStringProperty heure = new SimpleStringProperty();
    public SimpleStringProperty col4 = new SimpleStringProperty();
    public SimpleStringProperty col5 = new SimpleStringProperty();
    public SimpleStringProperty col6 = new SimpleStringProperty();
    public SimpleStringProperty col7 = new SimpleStringProperty();
    
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
        date.set(dateStr);
    }
    
    public String getHeure() {
        return heure.get();
    }
    
    public void setHeure(String heureStr) {
        heure.set(heureStr);
    }
    
    public String getCol4() {
        return col4.get();
    }
    
    public void setCol4(String col4Str) {            
        col4.set(col4Str);
    }
    
    public String getCol5() {
        return col5.get();
    }
    
    public void setCol5(String col5Str) {    
        col5.set(col5Str);
    }
    
    public String getCol6() {
        return col6.get();
    }
    
    public void setCol6(String col6Str) {    
        col6.set(col6Str);
    }
}
