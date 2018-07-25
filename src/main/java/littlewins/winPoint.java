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
public class winPoint {

    private boolean Modif;
    private String wAlt;
    private String wBalise;
    private String wDesc;
    // Localization
    private I18n i18n;    
    
    public boolean isModif() {
        return Modif;
    }

    public void setModif(boolean Modif) {
        this.Modif = Modif;
    }    
    
    public String getAlt() {
        return wAlt;
    }    
    
    public String getBalise() {
        return wBalise;
    }        
    
    public String getDesc() {
        return wDesc;
    }        
    
    public winPoint(I18n pI18n, String pAlt, String pDesc, String pBalise) {
        setModif(false);          
        i18n = pI18n;
        wAlt = pAlt;
        wDesc = pDesc;
        wBalise = pBalise;
        showWin();
    }    
    
    public void showWin() {
        Stage subStage = new Stage();
        
        Label lbAlt = new Label(i18n.tr("Altitude "));
        lbAlt.setMinWidth(100);        
        TextField txAlt = new TextField ();
        txAlt.setMinWidth(60);
        txAlt.setMaxWidth(60);
        txAlt.setText(wAlt);
        HBox hBox1 = new HBox();
        
        hBox1.setSpacing(10);
        hBox1.setMaxHeight(25);
        hBox1.setMinWidth(290);
        hBox1.setAlignment(Pos.CENTER_LEFT);
        hBox1.getChildren().addAll(lbAlt, txAlt);
        
        Label lbDesc = new Label(i18n.tr("Description "));
        lbDesc.setMinWidth(100);
        TextField txDesc = new TextField ();
        txDesc.setMinWidth(150);
        txDesc.setText(wDesc);                
        HBox hBox2 = new HBox();
        hBox2.getChildren().addAll(lbDesc, txDesc);
        hBox2.setSpacing(10);
        hBox2.setMaxHeight(25);
        hBox2.setMinWidth(290);
        hBox2.setAlignment(Pos.CENTER_LEFT);

        Label lbBal = new Label(i18n.tr("Nom court "));
        lbBal.setMinWidth(100);
        TextField txBalise = new TextField ();
        txBalise.setMinWidth(80);
        txBalise.setMaxWidth(80);
        txBalise.setText(wBalise);                
        HBox hBox3 = new HBox();
        hBox3.getChildren().addAll(lbBal, txBalise);
        hBox3.setSpacing(10);
        hBox3.setMaxHeight(25);
        hBox3.setMinWidth(290);
        hBox3.setAlignment(Pos.CENTER_LEFT);
        
        
        final VBox vbox = new VBox();
        vbox.setPadding(new Insets(10));
        vbox.setSpacing(5);                
        
        HBox buttonBar = new HBox();
        buttonBar.setPadding(new Insets(6));
        buttonBar.setSpacing(5);
        buttonBar.setAlignment(Pos.CENTER_RIGHT);
        Button btValid = new Button(i18n.tr("Valider"));
         btValid.setOnAction((event) -> {
            wBalise = txBalise.getText();            
            wDesc = txDesc.getText();
            wAlt = txAlt.getText();
            setModif(true);
            subStage.close();
        });
        Button btCancel = new Button(i18n.tr("Annuler"));
        btCancel.setOnAction((event) -> {
            setModif(false);
            subStage.close();
        });
        buttonBar.getChildren().addAll(btCancel, btValid);
        
        vbox.getChildren().addAll(hBox1, hBox2, hBox3, buttonBar);
        
        if (wAlt == null || wAlt.equals("")) hBox1.setVisible(false);
        if (wBalise == null || wBalise.equals("")) hBox3.setVisible(false);
        
        StackPane subRoot = new StackPane();
        subRoot.getChildren().add(vbox);    
        
        Scene secondScene = new Scene(subRoot, 300, 150);
        // modal mode
        subStage.initModality(Modality.APPLICATION_MODAL);        
        subStage.setScene(secondScene);     
        subStage.showAndWait();
    }    
    
}
