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
import org.xnap.commons.i18n.I18nFactory;

/**
 *
 * @author gil
 */
public class listGPS {
     
    private static I18n i18n;
    
    public static  ObservableList <String>  fill()  {        
        ObservableList <String> GPS_List;
        i18n = I18nFactory.getI18n(Logfly.Main.class.getClass());
        
        GPS_List = FXCollections.observableArrayList();
        
        GPS_List.add(i18n.tr("Sélection GPS"));
        GPS_List.add("6020/6030");
        GPS_List.add("6015");
        GPS_List.add("Flynet");
        GPS_List.add("Flymaster (old)");
        GPS_List.add("Reversale");
        GPS_List.add("Skytraax");
        GPS_List.add("Oudie");
        GPS_List.add("Element");
        GPS_List.add("Sensbox");
        GPS_List.add("Syride");
        GPS_List.add("Flymaster");
        GPS_List.add("Connect");
        
        return GPS_List;
        
    }
    
}
