/*
 * Copyright Gil THOMAS
 * Ce fichier fait partie intégrante du projet Logfly
 * Pour tous les détails sur la licence du projet Logfly
 * Consulter le fichier LICENSE distribué avec le code source
 */
package littlewins;

import javafx.animation.FadeTransition;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;

/**
 *
 * @author gil
 */
public class winPhoto {
            
    public void showDbPhoto(Image dbImage) {
        StackPane subRoot = new StackPane();
        int winWidth = (int)dbImage.getWidth();
        int winHeight = (int)dbImage.getHeight();
        ImageView imgView = new ImageView(dbImage);
        subRoot.getChildren().add(imgView);       
        // Ajout d'un effet de fading
        FadeTransition ft = new FadeTransition(Duration.millis(500), subRoot);
        ft.setFromValue(0.0);
        ft.setToValue(1.0);
        ft.play();
        Scene secondScene = new Scene(subRoot, winWidth, winHeight);
        Stage subStage = new Stage();
        // On veut que cette fenêtre soit modale
        subStage.initModality(Modality.APPLICATION_MODAL);
        subStage.setScene(secondScene);     
        subStage.show();                          
    }
    
}
