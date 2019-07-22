/*
 * Copyright Gil THOMAS
 * This file forms an integral part of Logfly project
 * See the LICENSE file distributed with source code
 * for details of Logfly licence project
 */
package littlewins;

import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import org.xnap.commons.i18n.I18n;
import org.xnap.commons.i18n.I18nFactory;
import settings.configProg;

/**
 *
 * @author gil
 */
public class winWebFile {
    
    private configProg myConfig;
    private I18n i18n; 
    private String htmlCode;
    private boolean sizeMax;
    
    public winWebFile(configProg currConfig, String pHTML, boolean pSizeMax) {
        // it will be useful if we choose url according to current language
        myConfig = currConfig;
        this.sizeMax = pSizeMax;
        i18n = I18nFactory.getI18n("","lang/Messages",winWebFile.class.getClass().getClassLoader(),myConfig.getLocale(),0);    
        this.htmlCode = pHTML;
        showBrowser();
    }    
    
    private void showBrowser() {
        
        Stage subStage = new Stage();
        WebView wv = new WebView();
        StackPane root = new StackPane();
        root.getChildren().add(wv);

        Scene scene = new Scene(root, 1000, 650);
       
        subStage.setScene(scene);        
        subStage.show();
        if (sizeMax) subStage.setMaximized(true);
        
        wv.getEngine().loadContent(htmlCode); 
    }    
}
