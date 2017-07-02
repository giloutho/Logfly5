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
public class listCarte {
    
    public static  ObservableList <String>  fill()  {        
        
        ObservableList <String> Carte_List;
        
        Carte_List = FXCollections.observableArrayList();       
                 
        Carte_List.add("OSM");
        Carte_List.add("OpenTopo");
        Carte_List.add("MTK");
        Carte_List.add("4UMaps");
        Carte_List.add("Google");       
        
        return Carte_List;
    }
    
}
