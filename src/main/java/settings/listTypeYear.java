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
public class listTypeYear {
    
    // Localization
    private I18n i18n; 
    
    public listTypeYear(Locale currLocale)  {
        i18n = I18nFactory.getI18n("","lang/Messages",listTypeYear.class.getClass().getClassLoader(),currLocale,0);
    }
        
    public static  ObservableList <String>  fill(I18n i18n)  {        
        
        ObservableList <String> Year_Type_List;
        
        Year_Type_List = FXCollections.observableArrayList();       
                 
        Year_Type_List.add(i18n.tr("Year by year"));
        Year_Type_List.add(i18n.tr("All years"));        
        
        return Year_Type_List;
    }
    
}
