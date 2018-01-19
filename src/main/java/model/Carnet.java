/* 
 * Copyright Gil THOMAS
 * This file forms an integral part of Logfly project
 * See the LICENSE file distributed with source code
 * for details of Logfly licence project
 */
package model;

import java.text.ParseException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.SimpleObjectProperty;

/**
 *
 * @author Gil Thomas logfly.org
 * 
 * Model for CarnetviewController
 * 
 * For a correct sorting in tableview date must be LocalDate
 * Solution found in https://stackoverflow.com/questions/43851194/javafx-sort-tableview-column-by-date-dd-mm-yyyy-format
 */

public class Carnet {
    public SimpleStringProperty idVol = new SimpleStringProperty();
    public ObjectProperty<LocalDate> date = new SimpleObjectProperty();
    public SimpleStringProperty heure = new SimpleStringProperty();
    public ObjectProperty<LocalTime> duree = new SimpleObjectProperty();
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

    public LocalDate getDate() {
        return date.get();
    }
    
    public void setDate(String dateStr) throws ParseException {            
        
        // in database, date is in principle YYYY-MM-DD HH:MM:SS      
        // but sometimes we have only YYYY-MM-DD
        DateTimeFormatter formatterSQL = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        Pattern fullDate = Pattern.compile("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}");
        Matcher matchFull = fullDate.matcher(dateStr);
        try {
            if(! matchFull.find()) {
                // Date in ot YYYY-MM-DD HH:MM, check for YYYY-MM-DD            
                Pattern dayDate = Pattern.compile("\\d{4}-\\d{2}-\\d{2}");
                Matcher matchDay = dayDate.matcher(dateStr);
                if(matchDay.find()) {          
                    // Direct parsing is possible because we have default ISO_LOCAL_DATE format
                    LocalDate localDate = LocalDate.parse(dateStr);
                    date.set(localDate);
                } else {
                    LocalDate localDate = LocalDate.parse("2000-01-01");
                    date.set(localDate);
                }
            } else {
                LocalDateTime ldtFromDb = LocalDateTime.parse(dateStr, formatterSQL);
                LocalDate localDate = ldtFromDb.toLocalDate();
                date.set(localDate);
            }
        } catch (Exception e) {

        }   
    }

    public String getHeure() {
        return heure.get();
    }
    
    public void setHeure(String dateStr) {
        // in database, date is YYYY-MM-DD HH:MM:SS
        if (dateStr.length() > 15)  
            heure.set(dateStr.substring(11,16));
        else
            heure.set("12:00");
    }
    
    public LocalTime getDuree() {
        return duree.get();
    }
    
    public void setDuree(String dureeStr) {
        int seconds = Integer.parseInt(dureeStr);
        duree.set(LocalTime.ofSecondOfDay(seconds));     
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
