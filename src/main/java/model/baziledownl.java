/*
 * Copyright Gil THOMAS
 * This file forms an integral part of Logfly project
 * See the LICENSE file distributed with source code
 * for details of Logfly licence project
 */
package model;

import javafx.beans.property.SimpleStringProperty;

/**
 *
 * @author gil
 */
public class baziledownl {
    
    public SimpleStringProperty  filename = new SimpleStringProperty();
    public SimpleStringProperty dateTrans = new SimpleStringProperty();
    public SimpleStringProperty dateOrigin = new SimpleStringProperty();
    public SimpleStringProperty description = new SimpleStringProperty();  

    public baziledownl(String pFilename, String pDateTrans, String pDateOrigin, String pDescription) {
        this.filename = new SimpleStringProperty(pFilename);
        this.dateTrans = new SimpleStringProperty(pDateTrans);
        this.dateOrigin = new SimpleStringProperty(pDateOrigin);
        this.description = new SimpleStringProperty(pDescription);                
    }  

    public String getFilename() {
        return filename.get();
    }        

    public String getDateOrigin() {
        return dateOrigin.get();
    }    

    public String getDateTrans() {
        return dateTrans.get();
    }

    public String getDescription() {
        return description.get();
    }            
}
