/*
 * Copyright Gil THOMAS
 * This file forms an integral part of Logfly project
 * See the LICENSE file distributed with source code
 * for details of Logfly licence project
 */
package littlewins;

import gps.skytraax;
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
public class winUsbWWayp {
    
    private I18n i18n; 
    private String fileName = null;
    private String fileExt = null;
    private winGPS.gpsType currGPS;    
    private String wTitle;      
    
    public winUsbWWayp(I18n pI18n, winGPS.gpsType pGPS, String pTitle) {
        i18n = pI18n;        
        currGPS = pGPS;
        wTitle = pTitle;
        showWin();
    }    

    public String getFilename() {
        return fileName;
    }

    public String getFileExt() {
        return fileExt;
    }            
    
    private void showWin() {
        
        int wWidth = 400;
        String btWptLabel = "";
        String btGoogLabel = "";

        Stage subStage = new Stage();
        Label lbFile = new Label(i18n.tr("Nom du fichier"));
        lbFile.setMinWidth(100);               
        lbFile.setPadding(new Insets(5, 0, 0, 0));
        TextField txFile = new TextField ();        
        txFile.setMinWidth(190);
        HBox hBox1 = new HBox();
        hBox1.setPadding(new Insets(6));
        hBox1.getChildren().addAll(lbFile, txFile);
        
        
        final VBox vbox = new VBox();
        vbox.setPadding(new Insets(10));
        vbox.setSpacing(5);        
        
        HBox buttonBar = new HBox();
        buttonBar.setPadding(new Insets(6));
        buttonBar.setSpacing(5);
        buttonBar.setAlignment(Pos.CENTER_RIGHT);
        Button btCancel = new Button(i18n.tr("Annuler"));
        btCancel.setOnAction((event) -> {
            subStage.close();
        });    
        Button btSendGoog = new Button(btGoogLabel);
         btSendGoog.setOnAction((event) -> {
            fileExt = ".kml";
            if (txFile.getText().trim() != null && !txFile.getText().trim().equals("")) fileName = txFile.getText()+fileExt;
            subStage.close();
        });        
        Button btSendWpt = new Button(btWptLabel);
        btSendWpt.setOnAction((event) -> {
            if (txFile.getText().trim() != null && !txFile.getText().trim().equals("")) fileName = txFile.getText()+fileExt;
            subStage.close();});           
        
        switch (currGPS) {
             case Rever :  
                 wWidth = 400;
                 btGoogLabel = i18n.tr("Envoi format Google");                 
                 btWptLabel = i18n.tr("Envoi format wpt");
                 fileExt = ".wpt";
                 buttonBar.getChildren().addAll(btCancel, btSendGoog, btSendWpt);
                 break;
             case Sky :
                 wWidth = 320;
                 btWptLabel = i18n.tr("Envoi format wpt");
                 fileExt = ".wpt";
                 buttonBar.getChildren().addAll(btCancel,btSendWpt);                 
                 break;
             case Sky3 :
                 wWidth = 320;
                 btWptLabel = i18n.tr("Envoi format wpt");
                 fileExt = ".wpt";
                 buttonBar.getChildren().addAll(btCancel,btSendWpt);                   
                 break;                              
             case Oudie :
                 wWidth = 320;
                 btWptLabel = i18n.tr("Envoi format cup");
                 fileExt = ".cup";
                 buttonBar.getChildren().addAll(btCancel,btSendWpt);                                                        
                 break;  
             case Syride :
                 break;                            
             case Connect :
                 wWidth = 320;
                 // No specifications in Flytec site, I have a backup with CompeGPS format
                 btWptLabel = i18n.tr("Envoi format CompeGPS");     
                 fileExt = ".wpt";
                 buttonBar.getChildren().addAll(btCancel,btSendWpt);                    
                 break;   
             case Element :
                 wWidth = 320;
                 btWptLabel = i18n.tr("Envoi format OZI");
                 fileExt = ".wpt";
                 buttonBar.getChildren().addAll(btCancel,btSendWpt);                  
                 break;                         
             case CPilot :
                 wWidth = 320;
                 btWptLabel = i18n.tr("Envoi format CompeGPS");
                 fileExt = ".wpt";
                 buttonBar.getChildren().addAll(btCancel,btSendWpt);                  
                 break;                 
        }        
        btSendGoog.setText(btGoogLabel);
        btSendWpt.setText(btWptLabel);
        vbox.getChildren().addAll(hBox1, buttonBar);
        
        StackPane subRoot = new StackPane();
        subRoot.getChildren().add(vbox);    
        
        Scene secondScene = new Scene(subRoot, wWidth, 100);
        // modal mode
        subStage.initModality(Modality.APPLICATION_MODAL);        
        subStage.setScene(secondScene);     
        subStage.setTitle(wTitle);
        subStage.showAndWait();         
         
    }
    
}
