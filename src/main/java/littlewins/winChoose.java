/*
 * Copyright Gil THOMAS
 * This file forms an integral part of Logfly project
 * See the LICENSE file distributed with source code
 * for details of Logfly licence project
 */
package littlewins;

import database.dbImport;
import java.io.File;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.xnap.commons.i18n.I18n;
import settings.configProg;

/**
 *
 * @author gil
 * 
 * Usage of threads is tricky with task usage in webdow
 * We use this simple scheme (modal window) to waitb the end of import process
 * Necessary for a refresh of the tableview after csv import
 * 
 */
public class winChoose {
    
    private I18n i18n; 
    private configProg myConfig; 
    private Label lbChoose;
    
    public winChoose(configProg pConfig, I18n pI18n) {        
        myConfig = pConfig;        
        this.i18n = pI18n;        
        showWin();
    }

    private void showWin() {
        
        Stage subStage = new Stage();
         
        Group newRoot = new Group();
        Scene secondScene = new Scene(newRoot, 300, 130); 

        VBox vBox = new VBox(20);
       // vBox.setSpacing(10); 
        vBox.setPadding(new Insets(20, 10, 10, 10));
       

        lbChoose = new Label();
        lbChoose.setText("");      
        lbChoose.setAlignment(Pos.CENTER);
        lbChoose.setMinWidth(secondScene.getWidth() - 20);

        HBox hBox = new HBox();
        hBox.setSpacing(10); 
        hBox.setPadding(new Insets(10, 10, 10, 10));
        
        Button btnOpen = new Button("Sélectionner un fichier");
        btnOpen.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent t) {
                selectFile();
            }
        });

        Button btnClose = new Button("Fermer");
        btnClose.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent t) {
                subStage.close();
            }
        });
                
        hBox.getChildren().addAll(btnOpen, btnClose);
        
        vBox.getChildren().addAll(lbChoose, hBox);
        newRoot.getChildren().add(vBox);  
        
        subStage.initModality(Modality.APPLICATION_MODAL);        
        subStage.setScene(secondScene);     
        subStage.showAndWait();        
    }     
    
    private void selectFile() {
        FileChooser fileChooser = new FileChooser();
        FileChooser.ExtensionFilter csvFilter = new FileChooser.ExtensionFilter(i18n.tr("fichiers traces (*.csv)"), "*.csv");        
        fileChooser.getExtensionFilters().add(csvFilter);
        File selectedFile = fileChooser.showOpenDialog(null);  
        if(selectedFile != null){   
            lbChoose.setText(selectedFile.getName());
            dbImport myImport = new dbImport(myConfig, i18n);
            myImport.importCsv(selectedFile);
        }
    }    
    
}
