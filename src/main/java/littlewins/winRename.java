/*
 * Copyright Gil THOMAS
 * This file forms an integral part of Logfly project
 * See the LICENSE file distributed with source code
 * for details of Logfly licence project
 */
package littlewins;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.logging.Level;
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
import org.xnap.commons.i18n.I18nFactory;
import settings.configProg;
import systemio.mylogging;

/**
 *
 * @author gil
 */
public class winRename {
    
    private configProg myConfig;
    private I18n i18n; 
    private String sInitial;
    private StringBuilder sbError;
    private TextField txSiteName;
    private int nbUpdates = 0;
    
    public winRename(String initialName, configProg currConfig)  {
        myConfig = currConfig;
        i18n = myConfig.getI18n(); 
        this.sInitial = initialName;
        showWin();
    }

    public int getNbUpdates() {
        return nbUpdates;
    }        
    
    
    private void showWin() {
        Stage subStage = new Stage();
        
        Label lbInitial = new Label(i18n.tr("Old name")+" ");
        lbInitial.setMinWidth(100);       
        lbInitial.setPrefWidth(100);
        final TextField txInitial = new TextField ();
        txInitial.setMinWidth(200);
        txInitial.setPrefWidth(200);
        txInitial.setText(sInitial);
        txInitial.setDisable(true);
        
        HBox hBox1 = new HBox();        
        hBox1.setSpacing(10);
        hBox1.setMaxHeight(25);
        hBox1.setMinWidth(300);
        hBox1.setAlignment(Pos.CENTER_LEFT);
        hBox1.getChildren().addAll(lbInitial, txInitial);
        
        Label lbSite = new Label(i18n.tr("New name")+" ");
        lbSite.setMinWidth(100);  
        lbSite.setPrefWidth(100);
        txSiteName = new TextField ()
            // Uppercase wanted...
            // Good example on https://stackoverflow.com/questions/20214962/replace-text-with-number-in-textfield/36436191#36436191   
        {
            @Override            
            public void replaceText(int start, int end, String text)
            {            
                super.replaceText(start, end, text.toUpperCase());            
            }       
        };              
        txSiteName.setMinWidth(200);
        txSiteName.setPrefWidth(200);  
        
        HBox hBox2 = new HBox();        
        hBox2.setSpacing(10);
        hBox2.setMaxHeight(25);
        hBox2.setMinWidth(300);
        hBox2.setAlignment(Pos.CENTER_LEFT);
        hBox2.getChildren().addAll(lbSite, txSiteName);
        
        final VBox vbox = new VBox();
        vbox.setPadding(new Insets(10));
        vbox.setSpacing(5);                
        
        HBox buttonBar = new HBox();
        buttonBar.setPadding(new Insets(6));
        buttonBar.setSpacing(5);
        buttonBar.setAlignment(Pos.CENTER_RIGHT);
        Button btValid = new Button(i18n.tr("OK"));
         btValid.setOnAction((event) -> {
            updateCarnet();
            subStage.close();
        });
        Button btCancel = new Button(i18n.tr("Cancel"));
        btCancel.setOnAction((event) -> {
            subStage.close();
        });
        buttonBar.getChildren().addAll(btCancel, btValid);
        
        vbox.getChildren().addAll(hBox1, hBox2, buttonBar);
        
        StackPane subRoot = new StackPane();
        subRoot.getChildren().add(vbox);    
        
        Scene secondScene = new Scene(subRoot, 350, 120);
        // modal mode
        subStage.initModality(Modality.APPLICATION_MODAL);        
        subStage.setScene(secondScene);     
        subStage.showAndWait();        
    }
    
    private void updateCarnet() {
  
        PreparedStatement pstmt = null;
        ResultSet rs = null;       
        String sReq = "SELECT V_ID,V_Site from vol where V_Site = ?";
        try {
            pstmt = myConfig.getDbConn().prepareStatement(sReq);                      
            pstmt.setString(1, sInitial); 
            rs = pstmt.executeQuery();
            if (rs != null)  {  
                PreparedStatement pstmtCarnet = null;                    
                String sReqCarnet = "UPDATE Vol SET V_Site=? WHERE V_ID =?";
                while (rs.next()) {
                    nbUpdates++;
                    pstmtCarnet = myConfig.getDbConn().prepareStatement(sReqCarnet);                      
                    pstmtCarnet.setString(1, txSiteName.getText());
                    pstmtCarnet.setString(2, rs.getString("V_ID"));
                    pstmtCarnet.executeUpdate();
                }
                PreparedStatement pstmtSite = null;                    
                String sReqSite = "UPDATE Site SET S_Nom=? WHERE S_Nom = ?";
                pstmtSite = myConfig.getDbConn().prepareStatement(sReqSite); 
                pstmtSite.setString(1, txSiteName.getText());
                pstmtSite.setString(2, sInitial);
                pstmtSite.executeUpdate();   
            }
        } catch (Exception e) {  
            sbError = new StringBuilder(this.getClass().getName()+"."+Thread.currentThread().getStackTrace()[1].getMethodName());
            sbError.append("\r\n").append(e.toString());
            mylogging.log(Level.SEVERE, sbError.toString());  
            nbUpdates = -1;
        } finally {
            try{
                rs.close(); 
                pstmt.close();
            } catch(Exception e) { 
                sbError = new StringBuilder(this.getClass().getName()+"."+Thread.currentThread().getStackTrace()[1].getMethodName());
                sbError.append("\r\n").append(e.toString());
                mylogging.log(Level.SEVERE, sbError.toString()); 
                nbUpdates = -1;
            } 
        }       
    }
    
}
