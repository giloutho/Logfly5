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
import settings.osType;

/**
 *
 * @author gil
 */
public class winWeb {
    
    private configProg myConfig;
    private I18n i18n; 
    private String dispUrl;

    public winWeb(configProg currConfig, String pURL) {
        // it will be useful if we choose url according to current language
        myConfig = currConfig;
        i18n = I18nFactory.getI18n("","lang/Messages",winLog.class.getClass().getClassLoader(),myConfig.getLocale(),0);    
        this.dispUrl = pURL;
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
        
        // known problem of bad display
        // WebView fonts appear as Gibberish on Mac
        // First post : https://stackoverflow.com/questions/41602344/strange-chracters-in-google-maps-api-using-java-webview
        // 2nd post : https://stackoverflow.com/questions/41952734/javafx-webview-font-issue-on-mac
        // Finally good solution is bad solution (see post 1)
        if (myConfig.getOS() == osType.MACOS) {
            wv.getEngine().setUserAgent(" Mozilla/5.0 (Windows NT x.y; Win64; x64; rv:10.0) Gecko/20100101 Firefox/10.0");
        }
        wv.getEngine().load(dispUrl);            
    }
    
}
