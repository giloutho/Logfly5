/*
 * Copyright Gil THOMAS
 * This file forms an integral part of Logfly project
 * See the LICENSE file distributed with source code
 * for details of Logfly licence project
 */
package littlewins;

import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
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
 *
 * For simple windows, we don't use SceneBuilder
 * 
 */

public class winGlider {
    
    private I18n i18n; 
    private boolean Modif;
    private String wPilot;
    private String wGlider;
    ObservableList<String> lsGliders;
    
    public winGlider(ObservableList<String> pGliders,String appPilot, I18n pI18n) {
        setModif(false);  
        setwPilot(appPilot);
        i18n = pI18n;
        lsGliders = pGliders;
        showWin();
    }
    
    public boolean isModif() {
        return Modif;
    }

    public void setModif(boolean Modif) {
        this.Modif = Modif;
    }

    public String getwPilot() {
        return wPilot;
    }

    public void setwPilot(String wPilot) {
        this.wPilot = wPilot;
    }

    public String getwGlider() {
        return wGlider;
    }

    public void setwGlider(String wGlider) {
        this.wGlider = wGlider;
    }
    
    

    private void showWin() {
        Stage subStage = new Stage();
        
        Label lbPilote = new Label(i18n.tr("Pilot")+" ");
        lbPilote.setMinWidth(80);        
        final TextField txPilot = new TextField ();
//          if Uppercase wanted...
        // Good example on https://stackoverflow.com/questions/20214962/replace-text-with-number-in-textfield/36436191#36436191
        // code with regex for number input etc...                
//        {
//            @Override            
//            public void replaceText(int start, int end, String text)
//            {            
//                super.replaceText(start, end, text.toUpperCase());            
//            }       
//        };
        txPilot.setMinWidth(190);
        txPilot.setText(wPilot);
        HBox hBox1 = new HBox();
        
        hBox1.setSpacing(10);
        hBox1.setMaxHeight(25);
        hBox1.setMinWidth(290);
        hBox1.setAlignment(Pos.CENTER_LEFT);
        hBox1.getChildren().addAll(lbPilote, txPilot);
        
        Label lbVoile = new Label(i18n.tr("Glider")+" ");
        lbVoile.setMinWidth(80);
        
        ComboBox cbGliders = new ComboBox();
        cbGliders.setItems(lsGliders);        
        cbGliders.setEditable(true);
        cbGliders.setMinWidth(190);
        if (lsGliders.size() > 0) {
            cbGliders.getSelectionModel().select(0); 
        }
        
        HBox hBox2 = new HBox();
        hBox2.getChildren().addAll(lbVoile, cbGliders);
        hBox2.setSpacing(10);
        hBox2.setMaxHeight(25);
        hBox2.setMinWidth(290);
        hBox2.setAlignment(Pos.CENTER_LEFT);
        
        
        final VBox vbox = new VBox();
        vbox.setPadding(new Insets(10));
        vbox.setSpacing(5);                
        
        HBox buttonBar = new HBox();
        buttonBar.setPadding(new Insets(6));
        buttonBar.setSpacing(5);
        buttonBar.setAlignment(Pos.CENTER_RIGHT);
        Button btValid = new Button(i18n.tr("OK"));
         btValid.setOnAction((event) -> {
            setwPilot(txPilot.getText());
            setwGlider((String) cbGliders.getValue());
            setModif(true);
            subStage.close();
        });
        Button btCancel = new Button(i18n.tr("Cancel"));
        btCancel.setOnAction((event) -> {
            setModif(false);
            subStage.close();
        });
        buttonBar.getChildren().addAll(btCancel, btValid);
        
        vbox.getChildren().addAll(hBox1, hBox2, buttonBar);
        
        StackPane subRoot = new StackPane();
        subRoot.getChildren().add(vbox);    
        
        Scene secondScene = new Scene(subRoot, 300, 120);
        // modal mode
        subStage.initModality(Modality.APPLICATION_MODAL);        
        subStage.setScene(secondScene);     
        subStage.showAndWait();
    }
    
}
