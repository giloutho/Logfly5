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
 */
public class listStartwin {
    
    // Localization
    private I18n i18n; 
    
    public listStartwin(Locale currLocale)  {
        i18n = I18nFactory.getI18n("","lang/Messages",listStartwin.class.getClass().getClassLoader(),currLocale,0);
    }
        
    public static  ObservableList <String>  fill(I18n i18n)  {        
        
        ObservableList <String> Startwin_List;
        
        Startwin_List = FXCollections.observableArrayList();       
                 
        Startwin_List.add(i18n.tr("Logbook"));
        Startwin_List.add(i18n.tr("Overview"));        
        
        return Startwin_List;
    }        
    
}
