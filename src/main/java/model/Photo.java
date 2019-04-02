/*
 * Copyright Gil THOMAS
 * This file forms an integral part of Logfly project
 * See the LICENSE file distributed with source code
 * for details of Logfly licence project
 */
package model;

import java.time.LocalDateTime;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;

/**
 *
 * @author gil
 */
public class Photo {
    
    public SimpleBooleanProperty checkedFile = new SimpleBooleanProperty();
    public SimpleStringProperty fileName = new SimpleStringProperty();
    public SimpleStringProperty filePath = new SimpleStringProperty();
    public SimpleStringProperty date = new SimpleStringProperty();
    public SimpleStringProperty heure = new SimpleStringProperty();
    public SimpleStringProperty originalDateTime = new SimpleStringProperty();
    public ObjectProperty<LocalDateTime> ldtOriginal = new SimpleObjectProperty();
    public SimpleStringProperty modifiedDateTime = new SimpleStringProperty();
    public SimpleStringProperty gpsTag = new SimpleStringProperty();  
    public SimpleDoubleProperty latitude = new SimpleDoubleProperty();
    public SimpleDoubleProperty longitude = new SimpleDoubleProperty();
    public SimpleBooleanProperty inRange = new SimpleBooleanProperty(); 
    public SimpleIntegerProperty idx = new SimpleIntegerProperty();    
    
    public SimpleBooleanProperty checkedProperty() {
        return this.checkedFile;
    }
    
    public java.lang.Boolean getChecked() {
        return this.checkedProperty().get();
    }
    
    public void setChecked(final java.lang.Boolean checked) {
        this.checkedProperty().set(checked);
    }     

    public String getFileName() {
        return fileName.get();
    }
    
    public void setFileName(String fileNameStr) {
        fileName.set(fileNameStr);
    }
    
    public String getFilePath() {
        return filePath.get();
    }
    
    public void setFilePath(String filePathStr) {
        filePath.set(filePathStr);
    }    

    public String getDate() {
        return date.get();
    }
    
    public void setDate(String dateStr) {    
        date.set(dateStr);
    }
    
    public String getHeure() {
        return heure.get();
    }
    
    public void setHeure(String hourStr) {
        heure.set(hourStr);
    }    

    public LocalDateTime getLdtOriginal() {
        return ldtOriginal.get();
    }
    
    public void setLdtOriginal(LocalDateTime ldt) {
        ldtOriginal.set(ldt);
    }
    
    public String getOriginalDateTime() {
        return originalDateTime.get();
    }

    public void setOriginalDateTime(String strDateTimeStr) {
        originalDateTime.set(strDateTimeStr);
    }

    public String getModifiedDateTime() {
        return modifiedDateTime.get();
    }

    public void setModifiedDateTime(String strDateTimeStr) {
        modifiedDateTime.set(strDateTimeStr);
    }
    
    public String getGpsTag() {
        return gpsTag.get();
    }
    
    public void setGpsTag(String gpsTagStr) {
        gpsTag.set(gpsTagStr);
    }
    
    public double getLatitude() {
        return latitude.get();
    }

    public void setLatitude(double pLatitude) {
        latitude.set(pLatitude);
    }  
    
    public double getLongitude() {
        return longitude.get();
    }

    public void setLongitude(double pLongitude) {
        longitude.set(pLongitude);
    }      
    
    public SimpleBooleanProperty inRangeProperty() {
        return this.inRange;
    }
    
    public java.lang.Boolean getInrange() {
        return this.inRangeProperty().get();
    }    
    
    public void setInRange(final java.lang.Boolean inrange) {
        this.inRangeProperty().set(inrange);
    }     

    public int getIdx() {
        return idx.get();
    }

    public void setIdx(int pIdx) {
        idx.set(pIdx);
    }    
    
}
