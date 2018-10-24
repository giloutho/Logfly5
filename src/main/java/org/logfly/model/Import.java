/* 
 * Copyright Gil THOMAS
 * This file forms an integral part of Logfly project
 * See the LICENSE file distributed with source code
 * for details of Logfly licence project
 */
package org.logfly.model;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;

/**
 *
 * @author gil
 * 
 * Model for ImportViewController
 */
public class Import {
    
    public SimpleBooleanProperty checkedFile = new SimpleBooleanProperty();
    public SimpleStringProperty date = new SimpleStringProperty();
    public SimpleStringProperty heure = new SimpleStringProperty();
    public SimpleStringProperty fileName = new SimpleStringProperty();
    public SimpleStringProperty pilotName = new SimpleStringProperty();
    public SimpleStringProperty filePath = new SimpleStringProperty();
    public SimpleStringProperty colSort = new SimpleStringProperty();
    
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
        // in database, date is YYYY-MM-DD HH:MM:SS
        date.set(dateStr.substring(0,10));
    }
    
    public String getHeure() {
        return heure.get();
    }
    
    public void setHeure(String dateStr) {
        // in database, date is YYYY-MM-DD HH:MM:SS
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

    public String getColSort() {
        return colSort.get();
    }

    public void setColSort(String colSortStr) {
        colSort.set(colSortStr);
    }        
}
