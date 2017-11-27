/* 
 * Copyright Gil THOMAS
 * This file forms an integral part of Logfly project
 * See the LICENSE file distributed with source code
 * for details of Logfly licence project
 */
package model;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;

/**
 *
 * @author Gil Thomas logfly.org
 * 
 * Model for CarnetviewController
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
    
    private SimpleDateFormat sdfSql = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    
    public String getIdVol() {
        return idVol.get();
    }
    
    public void setIdVol(String idVolStr) {    
        idVol.set(idVolStr);
    }

    public String getDate() {
        return date.get();
    }
    
    public void setDate(String dateStr) throws ParseException {            
        // in database, date is in principle YYYY-MM-DD HH:MM:SS      
        // but sometimes we have only YYYY-MM-DD
        Pattern fullDate = Pattern.compile("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}");
        Matcher matchFull = fullDate.matcher(dateStr);
        try {
            if(! matchFull.find()) {
                // Date in ot YYYY-MM-DD HH:MM, check for YYYY-MM-DD            
                Pattern dayDate = Pattern.compile("\\d{4}-\\d{2}-\\d{2}");
                Matcher matchDay = dayDate.matcher(dateStr);
                if(matchDay.find()) {                    
                    SimpleDateFormat sdfDay = new SimpleDateFormat("yyyy-MM-dd");
                    java.util.Date dateDay = sdfDay.parse(dateStr);
                    Locale osLocale = Locale.getDefault();
                    DateFormat dtloc = DateFormat.getDateInstance(DateFormat.SHORT,osLocale);
                    date.set(dtloc.format(dateDay));
                } else {
                    date.set("01-01-2000");
                }
            } else {
                java.util.Date dDate = sdfSql.parse(dateStr);
                Locale osLocale = Locale.getDefault();
                DateFormat dtloc = DateFormat.getDateInstance(DateFormat.SHORT,osLocale);
                date.set(dtloc.format(dDate));
            }
        } catch (ParseException e) {

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
