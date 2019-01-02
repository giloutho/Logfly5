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
 * Supported languages list
 */
public class listLangues {
    
    // Localization
    private I18n i18n; 
    
    public listLangues(Locale currLocale)  {
        i18n = I18nFactory.getI18n("","lang/Messages",listLangues.class.getClass().getClassLoader(),currLocale,0);
    }
        
    public static  ObservableList <String>  fill(I18n i18n)  {        
        
        ObservableList <String> Lang_List;
        
        Lang_List = FXCollections.observableArrayList();       
                 
        Lang_List.add(i18n.tr("German"));
        Lang_List.add(i18n.tr("English"));
        Lang_List.add(i18n.tr("French"));     
        Lang_List.add(i18n.tr("Italian"));
        
        return Lang_List;
    }
}
