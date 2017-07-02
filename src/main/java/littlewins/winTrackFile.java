/*
 * Copyright Gil THOMAS
 * Ce fichier fait partie intégrante du projet Logfly
 * Pour tous les détails sur la licence du projet Logfly
 * Consulter le fichier LICENSE distribué avec le code source
 */
package littlewins;

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
               
         // On ajoute le textArea ancré en haut à gauche donc on utilise AnchorPane
        AnchorPane anchorPane = new AnchorPane();
        TextArea txtTrace = new TextArea();
        txtTrace.setWrapText(true);          
        // On l'ancre aux 4 coins
        AnchorPane.setTopAnchor(txtTrace, 10.0);
        AnchorPane.setLeftAnchor(txtTrace, 10.0);
        AnchorPane.setRightAnchor(txtTrace, 10.0);
        AnchorPane.setBottomAnchor(txtTrace, 10.0);
        anchorPane.getChildren().add(txtTrace);
        txtTrace.setText(traceTxt);                
        
        StackPane subRoot = new StackPane();       
        subRoot.getChildren().add(anchorPane);
        
        Scene secondScene = new Scene(subRoot, 500, 600);
        // On veut que cette fenêtre soit modale
        subStage.initModality(Modality.APPLICATION_MODAL);        
        subStage.setScene(secondScene);     
        subStage.showAndWait();
    }
        
}
