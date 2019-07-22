/*
 * Copyright Gil THOMAS
 * This file forms an integral part of Logfly project
 * See the LICENSE file distributed with source code
 * for details of Logfly licence project
 */
package littlewins;

import dialogues.ProgressForm;
import dialogues.alertbox;
import java.io.File;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Paint;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.xnap.commons.i18n.I18n;
import org.xnap.commons.i18n.I18nFactory;
import settings.configProg;
import settings.privateData;
import systemio.MultipartUtility;
import systemio.mylogging;
import systemio.tempacess;
import systemio.webio;

/**
 *
 * @author gil
 */
public class winSendDb {
    
    private configProg myConfig;
    private I18n i18n;     
    private Stage subStage;
    private TextField txtAdress; 
    private TextArea txFile;
    private File uploadFile;
    private String namedb = null;
    private boolean mailSended = false; 
    private boolean dbSended = false;
    private StringBuilder sbError;
        
    public winSendDb(configProg currConfig) {
        myConfig = currConfig;
        i18n = I18nFactory.getI18n("","lang/Messages",winSendDb.class.getClass().getClassLoader(),myConfig.getLocale(),0);   
        showWin();
    }    
    
    private final Pattern VALID_EMAIL_ADDRESS_REGEX = 
        Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE);

    private boolean mailValid(String emailStr) {
        Matcher matcher = VALID_EMAIL_ADDRESS_REGEX .matcher(emailStr);
        return matcher.find();
    }   

    private boolean checkFields() {
        
        boolean res = false;
        String stChecking;
        Paint value0 = Paint.valueOf("FA6C04");
           
        stChecking = txtAdress.getText();
        if (stChecking == null || stChecking.equals("")) {
            res = false;
        } else {
            if (!mailValid(stChecking)) {
                res = false;
            } else {
                res = true;
                String[] arrMail = stChecking.split("@");
                if (arrMail.length > 0) {
                    namedb = arrMail[0]+".db";
                } else {
                    namedb = "unknown.db";
                }
            }
        }        
        if (res) {
            stChecking = txFile.getText();
            if (stChecking == null || stChecking.equals("")) {
                res = false;
            } else {
                res = true;
            }
            if (res) {
                return res;
            } else {
                txFile.setStyle("-fx-control-inner-background: #"+value0.toString().substring(2));
                txFile.requestFocus();                          
            }                      
        } else {
            txtAdress.setStyle("-fx-control-inner-background: #"+value0.toString().substring(2));
            txtAdress.requestFocus();            
        }
        
        return res;
    }    
    
    private void checkAndSend() {
       if (checkFields()) startSending();
    }        
    
    private void showWin() {
        int winHeight;
        
        HBox hbAdresse = new HBox();
        hbAdresse.setPadding(new Insets(5, 12, 5, 12));
        hbAdresse.setSpacing(10);
        
        Label lbMail = new Label();
        lbMail.setPrefSize(100, 15);
        lbMail.setPadding(new Insets(5, 0, 0, 0));
        lbMail.setText(i18n.tr("Sender")+" ");
        txtAdress = new TextField();
        txtAdress.setPrefWidth(250);    
        txtAdress.setText(myConfig.getPiloteMail());    
        hbAdresse.getChildren().addAll(lbMail, txtAdress); 

        // texte du mail
        txFile = new TextArea();
        // Pour aller à la ligne et il y a plein d'autres possibilités
        txFile.setWrapText(true);
        txFile.setPrefSize(480, 250);

        // Boutons
        HBox buttonBar = new HBox();
        buttonBar.setPadding(new Insets(6));
        buttonBar.setSpacing(5);
        buttonBar.setAlignment(Pos.CENTER_RIGHT);
        Button btSend = new Button(i18n.tr("Send"));
        btSend.setOnAction((event) -> {
            checkAndSend();
        });
        Button btClose = new Button(i18n.tr("Cancel"));
        btClose.setOnAction((event) -> {            
            subStage.close();
        });
        buttonBar.getChildren().addAll(btClose, btSend );

        // La Vbox qui va contenir chacun des éléments horizontaux définis ci dessus
        final VBox vbox = new VBox();
        vbox.setPadding(new Insets(5));
        vbox.setSpacing(10);

        vbox.getChildren().addAll(hbAdresse, txFile, buttonBar);
        winHeight = 360;        
        
        // Mise en place de la fenêtre
        subStage = new Stage();
        
        StackPane subRoot = new StackPane();
        subRoot.getChildren().add(vbox);
        
        Scene secondScene = new Scene(subRoot, 500, winHeight);

        // modal mode
        subStage.initModality(Modality.APPLICATION_MODAL);        
        subStage.setScene(secondScene);     
        subStage.showAndWait();           
    }
    
    private void sendMail() {   
        StringBuilder sbTxFile = new StringBuilder();
        try {
            URL url = new URL(privateData.phpMail.toString());
            final MultipartUtility http = new MultipartUtility(url);             
            http.addFormField("full_name", "");
            http.addFormField("fromemail", txtAdress.getText());
            http.addFormField("toemail", privateData.mailSupport.toString());
            http.addFormField("subject", "Support for "+namedb);
            sbTxFile.append(txFile.getText());
            sbTxFile.append("\r\n").append("\r\n");
            sbTxFile.append("------------------------------------------").append("\r\n");
            sbTxFile.append("   ").append(myConfig.getOS().toString());
            sbTxFile.append("   ").append(myConfig.getVersion()).append("\r\n");
            sbTxFile.append("------------------------------------------").append("\r\n");  
            sbTxFile.append("   ").append(myConfig.getUrlLogfly()).append("/support/").append(namedb).append("\r\n");
            sbTxFile.append("------------------------------------------");               
            http.addFormField("msgtext", sbTxFile.toString());
            final byte[] bytes = http.finish();
            mailSended = true;        
        } catch (Exception e) {
            e.printStackTrace();
            mailSended = false;
        }        
    }      
    
    private void sendDb() {
        
        webio sendFile = new webio();
        // db copy to a temp file
        Path srcPath = Paths.get(myConfig.getFullPathDb());
        String uploadUrl = myConfig.getUrlLogfly()+"/support/dbtosupport.php";
        File upDb = null;
        try {
            String tempPathDb = tempacess.getTemPath(namedb);
            Path dstPath = Paths.get(tempPathDb);
            Files.copy(srcPath, dstPath, StandardCopyOption.REPLACE_EXISTING);  
            upDb = new File(tempPathDb);
            sendFile.httpUploadFile(upDb, uploadUrl);
            if (sendFile.getDlError() == 0) {
                dbSended = true;
            }
        } catch (Exception e) {
            sbError = new StringBuilder(this.getClass().getName()+"."+Thread.currentThread().getStackTrace()[1].getMethodName());
            sbError.append("\r\n").append(e.toString());
            mylogging.log(Level.SEVERE, sbError.toString());              
        } finally {
            if (upDb.exists()) upDb.delete();
        }        
    }
    
    private void sendDbClosing()  {
        alertbox aError = new alertbox(myConfig.getLocale());
        if (dbSended) {
            aError.alertNumError(0);  // successful
            subStage.close();
        } else {        
            aError.alertInfo(i18n.tr("Error sending logbook"));  
        }
    }    
    
    public void startSending()  {
        
                
        ProgressForm pForm = new ProgressForm();
           
        Task<Void> task = new Task<Void>() {
            @Override
            public Void call() throws InterruptedException { 
                sendMail();
                if (mailSended) sendDb();
                return null ;                
            }
        
        };
        // binds progress of progress bars to progress of task:
        pForm.activateProgressBar(task);

        // task is finished 
        task.setOnSucceeded(event -> {
            pForm.getDialogStage().close();
            sendDbClosing();
        });

        pForm.getDialogStage().show();

        Thread thread = new Thread(task);
        thread.start();        
    }        
    
}
