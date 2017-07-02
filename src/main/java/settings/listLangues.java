/*
 * Copyright Gil THOMAS
 * Ce fichier fait partie intégrante du projet Logfly
 * Pour tous les détails sur la licence du projet Logfly
 * Consulter le fichier LICENSE distribué avec le code source
 */
package settings;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.xnap.commons.i18n.I18n;

/**
 *
 * @author gil
 */
public class listLangues {
        
    public static  ObservableList <String>  fill(I18n i18n)  {        
        
        ObservableList <String> Lang_List;
        
        Lang_List = FXCollections.observableArrayList();       
                 
        Lang_List.add(i18n.tr("Allemand"));
        Lang_List.add(i18n.tr("Anglais"));
        Lang_List.add(i18n.tr("Français"));        
        
        return Lang_List;
    }
}
