/* 
 * Copyright Gil THOMAS
 * This file forms an integral part of Logfly project
 * See the LICENSE file distributed with source code
 * for details of Logfly licence project
 */
package settings;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 *
 * @author gil
 * League contest list
 */
public class listLeague {
    
    public static  ObservableList <String>  fill()  {        
        ObservableList <String> League_List;
        
        League_List = FXCollections.observableArrayList();       
        
        League_List.add("FR");
        League_List.add("CH");
        League_List.add("XC");
        League_List.add("AU");
        League_List.add("CZ");
        League_List.add("CZX");
        League_List.add("ES");
        League_List.add("FI");
        League_List.add("HU");
        League_List.add("MX");
        League_List.add("NE");
        League_List.add("NEX");
        League_List.add("SK");
        League_List.add("SKX");
        //League_List.add("XC");  // ? This is a dublicate. See above ln. 26
        League_List.add("Free 3pt");
        League_List.add("Flat Trg");
        League_List.add("FAI Trg");
        League_List.add("Leonardo");
        
        return League_List;
        
    }
    
}
