/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package littlewins;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.xnap.commons.i18n.I18n;

/**
 *
 * @author gil
 */
public class winAirSearch {
    
    private I18n i18n; 
    private boolean runSearch;
    private String searchName;    
    
    public winAirSearch(I18n pI18n) {
        this.runSearch = false;  
        i18n = pI18n;
        showWin();
    }    

    public boolean isRunSearch() {
        return runSearch;
    }

    public String getSearchName() {
        return searchName;
    }
                
    private void showWin() {
        Stage subStage = new Stage();
        
        Label lbName = new Label(i18n.tr("Search")+" ");
        lbName.setMinWidth(80);        
        final TextField txName = new TextField ();
        txName.setMinWidth(190);        
        HBox hBox1 = new HBox();
        
        hBox1.setSpacing(10);
        hBox1.setMaxHeight(25);
        hBox1.setMinWidth(290);
        hBox1.setAlignment(Pos.CENTER_LEFT);
        hBox1.getChildren().addAll(lbName, txName);
                
        final VBox vbox = new VBox();
        vbox.setPadding(new Insets(10));
        vbox.setSpacing(5);                
        
        HBox buttonBar = new HBox();
        buttonBar.setPadding(new Insets(6));
        buttonBar.setSpacing(5);
        buttonBar.setAlignment(Pos.CENTER_RIGHT);
        Button btValid = new Button(i18n.tr("OK"));
         btValid.setOnAction((event) -> {
            searchName = txName.getText().trim();
            if (searchName != null && !searchName.equals("")) {
                runSearch = true;
            } else {
                runSearch = false;                
            }
             System.out.println(searchName+"  "+runSearch);
            subStage.close();
        });
        Button btCancel = new Button(i18n.tr("Cancel"));
        btCancel.setOnAction((event) -> {
            runSearch = false;
            subStage.close();
        });
        buttonBar.getChildren().addAll(btCancel, btValid);
        
        vbox.getChildren().addAll(hBox1, buttonBar);
        
        StackPane subRoot = new StackPane();
        subRoot.getChildren().add(vbox);    
        
        Scene secondScene = new Scene(subRoot, 300, 90);
        // modal mode
        subStage.initModality(Modality.APPLICATION_MODAL);        
        subStage.setScene(secondScene);     
        subStage.showAndWait();
    }    
}
