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
public class listSynthese {
    
    // Localization
    private I18n i18n; 
    
    public listSynthese(Locale currLocale)  {
        i18n = I18nFactory.getI18n("","lang/Messages",listSynthese.class.getClass().getClassLoader(),currLocale,0);
    }
        
    public static  ObservableList <String>  fill(I18n i18n)  {        
        
        ObservableList <String> Synthese_List;
        
        Synthese_List = FXCollections.observableArrayList();       
                 
        Synthese_List.add(i18n.tr("Calendar year"));
        Synthese_List.add(i18n.tr("Last twelve months"));        
        
        return Synthese_List;
    }    
}
