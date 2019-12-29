/*
 * Copyright Gil THOMAS
 * This file forms an integral part of Logfly project
 * See the LICENSE file distributed with source code
 * for details of Logfly licence project
 */
package model;

import java.time.LocalDateTime;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import java.util.Comparator; 

/**
 *
 * @author gil
 */
public class cutting {
    
    public SimpleStringProperty cTime = new SimpleStringProperty();
    public SimpleStringProperty cElapsed = new SimpleStringProperty();
    public SimpleStringProperty cText = new SimpleStringProperty();    
    public SimpleStringProperty cHTML = new SimpleStringProperty();  
    public SimpleStringProperty cCoord = new SimpleStringProperty();  
    public ObjectProperty<LocalDateTime> cLdt = new SimpleObjectProperty();
    
    
    public String getCTime() {
        return cTime.get();
    }
    
    public void setCTime(String cTimeStr) {    
        cTime.set(cTimeStr);
    }    
    
    public String getCElapsed() {
        return cElapsed.get();
    }
    
    public void setCElapsed(String cElapsedStr) {    
        cElapsed.set(cElapsedStr);
    }      
    
    public String getCText() {
        return cText.get();
    }
    
    public void setCText(String cTextStr) {    
        cText.set(cTextStr);
    }      
    
    public String getCHTML() {
        return cHTML.get();
    }
    
    public void setCHTML(String cHtmlStr) {    
        cHTML.set(cHtmlStr);
    }  
    
    public String getCCoord() {
        return cCoord.get();
    }
    
    public void setCCoord(String cCoordStr) {    
        cCoord.set(cCoordStr);
    }        
    
    public LocalDateTime getCLdt() {
        return cLdt.get();
    }
    
    public void setCLdt(LocalDateTime pLdt) {    
        cLdt.set(pLdt);
    }      
    
    public static Comparator<cutting> elapsedComparator = new Comparator<cutting>() {                  
      @Override         
        public int compare(cutting c1, cutting c2) {             
          return (int) (c1.getCTime().compareTo(c2.getCTime()));         
        }   
    };      
    

    @Override     
        public String toString() {         
          return getCText() + " " + getCElapsed();   
        }     
    
    
}
