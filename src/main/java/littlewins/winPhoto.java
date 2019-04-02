/* 
 * Copyright Gil THOMAS
 * This file forms an integral part of Logfly project
 * See the LICENSE file distributed with source code
 * for details of Logfly licence project
 */
package littlewins;

import javafx.animation.FadeTransition;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.stage.Modality;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.util.Duration;

/**
 *
 * @author gil
 * Special simple window to show the photo of the flight
 */
public class winPhoto {
            
    public void displayPhoto(Image srcImage) {
        StackPane subRoot = new StackPane();
        int winWidth = (int) srcImage.getWidth();
        int winHeight = (int) srcImage.getHeight();
        // Get screen size
        Rectangle2D visualBounds = Screen.getPrimary().getVisualBounds();        
        double scrWidth = visualBounds.getWidth();
        double scrHeight = visualBounds.getHeight();
        double imgWidth = srcImage.getWidth();
        double imgHeight = srcImage.getHeight();
        
        if (imgWidth > scrWidth) {
            double scrRatio = scrWidth/imgWidth;
            // keep a margin of 5%
            scrRatio = scrRatio * 0.95;
            winWidth = (int) (imgWidth * scrRatio);
            winHeight = (int) (imgHeight * scrRatio);
            if (winHeight > scrHeight) {
                scrRatio = scrHeight/imgHeight;
                // keep a margin of 5%
                scrRatio = scrRatio * 0.95;
                winWidth = (int) (imgWidth * scrRatio);
                winHeight = (int) (imgHeight * scrRatio);
            }
        } 
        ImageView imgView = new ImageView(srcImage);               
        imgView.setFitWidth(winWidth);
        imgView.setFitHeight(winHeight);
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
