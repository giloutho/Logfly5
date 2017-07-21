/* 
 * Copyright Gil THOMAS
 * This file forms an integral part of Logfly project
 * See the LICENSE file distributed with source code
 * for details of Logfly licence project
 */
package settings;

import java.util.Locale;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.xnap.commons.i18n.I18n;
import org.xnap.commons.i18n.I18nFactory;

/**
 *
 * @author gil
 * Supported GPS list
 */
public class listGPS {
     
    private static I18n i18n;
    
    public listGPS(Locale currLocale)  {
        i18n = I18nFactory.getI18n("","lang/Messages",listGPS.class.getClass().getClassLoader(),currLocale,0);
    }
    
    public static  ObservableList <String>  fill()  {        
        ObservableList <String> GPS_List;
        
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
