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
 * 
 * map layer list
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
