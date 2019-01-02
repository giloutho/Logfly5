/*
 * Copyright Gil THOMAS
 * This file forms an integral part of Logfly project
 * See the LICENSE file distributed with source code
 * for details of Logfly licence project
 */
package littlewins;

import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.ChoiceBox;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 *
 * @author gil
 */
public class winLanguage {
    
    private Stage subStage;
    private int idxLang;
    
    public winLanguage() {
        idxLang = 0;
        showWin();
    }

    public int getIdxLang() {
        return idxLang;
    }

    public void setIdxLang(int idxLang) {
        this.idxLang = idxLang;
    }
        
    
    private void showWin() {
        
        subStage = new Stage(); 
        subStage.setTitle("Choose your language");
        String imgPath ="/images/Lang_GM.png";
        
        final HBox hBox1 = new HBox();
        hBox1.setPadding(new Insets(30));
        hBox1.setSpacing(5);   
        
        hBox1.setSpacing(10);
        
        Image img = new Image(imgPath);
        ImageView imgView = new ImageView(img);
        
        ChoiceBox chbLang = new ChoiceBox(
            FXCollections.observableArrayList("Select...","Deutsche","English","Français", "Italiano")
        );
        chbLang.getSelectionModel().selectedItemProperty()
            .addListener((ObservableValue observable, 
                    Object oldValue, Object newValue) -> {
                setIdxLang(chbLang.getSelectionModel().getSelectedIndex()-1);
                subStage.close();
        });        
        
        chbLang.getSelectionModel().selectFirst();
        chbLang.setMinWidth(120);
        
        final VBox vbox = new VBox();
        vbox.setPadding(new Insets(25));
        vbox.getChildren().add(chbLang);
        
        hBox1.getChildren().addAll(imgView, vbox);
        
        StackPane subRoot = new StackPane();
        subRoot.getChildren().add(hBox1);
        subStage.setScene(new Scene(subRoot, 300, 140));
        subStage.showAndWait();           
    }
    
}
