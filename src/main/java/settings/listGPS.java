/* 
 * Copyright Gil THOMAS
 * This file forms an integral part of Logfly project
 * See the LICENSE file distributed with source code
 * for details of Logfly licence project
 */
package settings;

import java.util.Comparator;
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
    
    public static class idGPS {
        private String name;
        private int idModel;
        
        public idGPS(String name, int idModel) {
            this.name = name;
            this.idModel = idModel;
        }

        public String getName() {
            return name;
        }       
        
        public int getIdModel() {
            return idModel;
        }       
        
        @Override
        public String toString() {
            return name;
        }        
    }    
     
    private static I18n i18n;
    
    public listGPS(Locale currLocale)  {
        i18n = I18nFactory.getI18n("","lang/Messages",listGPS.class.getClass().getClassLoader(),currLocale,0);
    }
    
    /**
     * First implementation - GPS order is directed by Logfly V4 compatibility
     * @return 
     */
    public static  ObservableList <String>  fill()  {        
        ObservableList <String> GPS_List;
        
        GPS_List = FXCollections.observableArrayList();
        
        GPS_List.add(i18n.tr("Sélection GPS"));
        GPS_List.add("6020/6030");
        GPS_List.add("6015");
        GPS_List.add("Flynet");
        GPS_List.add("Flymaster (old)");
        GPS_List.add("Reversale");
        GPS_List.add("Skytraax 2");
        GPS_List.add("Oudie");
        GPS_List.add("Element");
        GPS_List.add("Sensbox");
        GPS_List.add("Syride");
        GPS_List.add("Flymaster");
        GPS_List.add("Connect");
        GPS_List.add("Skytraxx 3");
        GPS_List.add("C-Pilot Evo");
        GPS_List.add("XC Tracer II");        
        
        return GPS_List;
        
    }

    /**
     * Implementation with alphabetic sort 
     * Old Index value from Logfly V4 is kept as a simple key
     * @return 
     */
    public final ObservableList<idGPS> newFill() {
        
        ObservableList <idGPS> GPS_List;
        
        GPS_List = FXCollections.observableArrayList(                
            new idGPS(" "+i18n.tr("Sélection GPS"),0),  // space necessary to keep this option as first element
            new idGPS("6020/6030",1),
            new idGPS("6015",2),
            new idGPS("Flynet",3),
            new idGPS("Flymaster (old)",4),
            new idGPS("Reversale",5),
            new idGPS("Skytraax 2",6),
            new idGPS("Oudie",7),
            new idGPS("Element",8),
            new idGPS("Sensbox",9),
            new idGPS("Syride",10),
            new idGPS("Flymaster",11),
            new idGPS("Connect",12),
            new idGPS("Skytraxx 3",13),
            new idGPS("C-Pilot Evo",14),
            new idGPS("XC Tracer II",15)    
        ); 
        
        Comparator<? super idGPS> comparatorAlpha = new Comparator<idGPS>() {
            @Override
            public int compare(idGPS o1, idGPS o2) {
                // order asc -> o1.getCol7().compareTo(o2.getCol7());
                return o1.getName().compareTo(o2.getName());
            }
        };  
        
        FXCollections.sort(GPS_List, comparatorAlpha); 
       
        return GPS_List;        
    }    
    
}
