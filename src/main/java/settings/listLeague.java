/*
 * Copyright Gil THOMAS
 * Ce fichier fait partie intégrante du projet Logfly
 * Pour tous les détails sur la licence du projet Logfly
 * Consulter le fichier LICENSE distribué avec le code source
 */
package settings;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 *
 * @author gil
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
        League_List.add("XC");
        
        return League_List;
        
    }
    
}
