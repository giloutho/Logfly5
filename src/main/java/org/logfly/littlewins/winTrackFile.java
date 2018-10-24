/* 
 * Copyright Gil THOMAS
 * This file forms an integral part of Logfly project
 * See the LICENSE file distributed with source code
 * for details of Logfly licence project
 */
package org.logfly.littlewins;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.TextArea;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 *
 * @author gil
 * 
 * Show the plaint text of the track file
 */
public class winTrackFile {
    private String traceTxt;
    
    public winTrackFile(String strTrace) {
        this.traceTxt = strTrace;        
        showTrace();
    }

    public void setTraceTxt(String traceTxt) {
        this.traceTxt = traceTxt;
    }

    private void showTrace() {
        Stage subStage = new Stage();
               
         // Using an anchorpane because we add a text area in the top left corner 
        AnchorPane anchorPane = new AnchorPane();
        TextArea txtTrace = new TextArea();
        txtTrace.setWrapText(true);          
        // anchor in four corners
        AnchorPane.setTopAnchor(txtTrace, 10.0);
        AnchorPane.setLeftAnchor(txtTrace, 10.0);
        AnchorPane.setRightAnchor(txtTrace, 10.0);
        AnchorPane.setBottomAnchor(txtTrace, 10.0);
        anchorPane.getChildren().add(txtTrace);
        txtTrace.setText(traceTxt);                
        
        StackPane subRoot = new StackPane();       
        subRoot.getChildren().add(anchorPane);
        
        Scene secondScene = new Scene(subRoot, 500, 600);
        subStage.initModality(Modality.APPLICATION_MODAL);        
        subStage.setScene(secondScene);     
        subStage.showAndWait();
    }
        
}
