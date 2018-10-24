/* 
 * Copyright Gil THOMAS
 * This file forms an integral part of Logfly project
 * See the LICENSE file distributed with source code
 * for details of Logfly licence project
 */
package org.logfly.littlewins;

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
 * Special simple window to show the photo of the flight
 */
public class winPhoto {
            
    public void showDbPhoto(Image dbImage) {
        StackPane subRoot = new StackPane();
        int winWidth = (int)dbImage.getWidth();
        int winHeight = (int)dbImage.getHeight();
        ImageView imgView = new ImageView(dbImage);
        subRoot.getChildren().add(imgView);       
        // Add a fading effect
        FadeTransition ft = new FadeTransition(Duration.millis(500), subRoot);
        ft.setFromValue(0.0);
        ft.setToValue(1.0);
        ft.play();
        Scene secondScene = new Scene(subRoot, winWidth, winHeight);
        Stage subStage = new Stage();
        subStage.initModality(Modality.APPLICATION_MODAL);
        subStage.setScene(secondScene);     
        subStage.show();                          
    }
    
}
