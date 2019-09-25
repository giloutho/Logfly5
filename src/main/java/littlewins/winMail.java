/*
 * Copyright Gil THOMAS
 * This file forms an integral part of Logfly project
 * See the LICENSE file distributed with source code
 * for details of Logfly licence project
 */
package littlewins;

import dialogues.alertbox;
import java.io.File;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Paint;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.controlsfx.dialog.ProgressDialog;
import org.xnap.commons.i18n.I18n;
import org.xnap.commons.i18n.I18nFactory;
import settings.configProg;
import settings.privateData;
import systemio.MultipartUtility;

/**
 *
 * @author gil
 * 
 * We have a big restriction with yahoo adresses 
 * If they are not sended from yahoo SMTP they are rejected by gmail
 * See https://www.arobase.org/forums/depuis-formulaire-contact-gmail-accepte-pas-yahoo-t27879.html
 */
public class winMail {
    
    private configProg myConfig;
    private I18n i18n; 
    
    private Stage subStage;
    private TextField txtAdress; 
    private TextField txtDest;
    private TextField txtSubject;
    private TextArea txFile;
    private CheckBox chPJ;
    private File uploadFile;
    private String filePath = null;
    private boolean mailSended = false;
    private boolean supportMsg;

    /**
     * Display form mail
     * @param currConfig   translation parameters
     * @param pFilePath    file path for attachment
     * @param pSupport     recipient is support 
     */
    public winMail(configProg currConfig, String pFilePath, boolean pSupport) {
        myConfig = currConfig;
        supportMsg = pSupport;  
        i18n = I18nFactory.getI18n("","lang/Messages",winMail.class.getClass().getClassLoader(),myConfig.getLocale(),0);   
        if (pFilePath != null)  {
            filePath = pFilePath;
            uploadFile = new File(filePath);
        }
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
            }
        }
        
        if (res) {
            stChecking = txtDest.getText();
            if (stChecking == null || stChecking.equals("")) {
                res = false;
            } else {
                if (!mailValid(stChecking)) {
                    res = false;
                } else {
                    res = true;
                }
            }
            if (res)  {
                stChecking = txtSubject.getText();
                if (stChecking == null || stChecking.equals("")) {
                    res = false;
                } else {
                    res = true;
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
                    txtSubject.setStyle("-fx-control-inner-background: #"+value0.toString().substring(2));
                    txtSubject.requestFocus();                    
                }
            } else {
                txtDest.setStyle("-fx-control-inner-background: #"+value0.toString().substring(2));
                txtDest.requestFocus();                
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
    
    private void showWin()  {
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

        // Element horizontal : Label + champ destinataire
        HBox hbDest = new HBox();
        // top, right, bottom and left
        hbDest.setPadding(new Insets(5, 12, 5, 12));
        hbDest.setSpacing(10);
        
        Label lbDest = new Label();
        lbDest.setPrefSize(100, 15);
        lbDest.setPadding(new Insets(5, 0, 0, 0));
        lbDest.setText(i18n.tr("Recipient")+" ");
        txtDest = new TextField();
        txtDest.setPrefWidth(250);
        if (supportMsg) {
            txtDest.setText(privateData.mailSupport.toString());
            txtDest.setDisable(true);
        }
        // récup nom pilote
        hbDest.getChildren().addAll(lbDest, txtDest);

       // Element horizontal : Label + champ sujet
        HBox hbSubject = new HBox();
        // top, right, bottom and left
        hbSubject.setPadding(new Insets(5, 12, 5, 12));
        hbSubject.setSpacing(10);
        
        Label lbSubject = new Label();
        lbSubject.setPrefSize(100, 15);
        lbSubject.setPadding(new Insets(5, 0, 0, 0));
        lbSubject.setText(i18n.tr("Subject")+" ");
        txtSubject = new TextField();
        txtSubject.setPrefWidth(300);
        hbSubject.getChildren().addAll(lbSubject, txtSubject);        
                
        // texte du mail
        txFile = new TextArea();
        // Pour aller à la ligne et il y a plein d'autres possibilités
        txFile.setWrapText(true);
        txFile.setPrefSize(480, 250);
        //txFile.setPadding(new Insets(10, 20, 10, 20)); 
        
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

        // Avec ou sans pièce jointe
        if (filePath != null) {
           // Element horizontal : Pièce jointe
            HBox hbPJ = new HBox();
            // top, right, bottom and left
            hbPJ.setPadding(new Insets(5, 12, 0, 12));
            hbPJ.setSpacing(10);        
            chPJ = new CheckBox();   
            chPJ.setText(filePath);
            chPJ.setSelected(true);
            hbPJ.getChildren().addAll(chPJ);         
            
            vbox.getChildren().addAll(hbAdresse,hbDest, hbSubject, txFile, hbPJ, buttonBar);
            winHeight = 500;
        } else {    
            vbox.getChildren().addAll(hbAdresse,hbDest, hbSubject, txFile, buttonBar);
            winHeight = 460;
        }
        
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
            http.addFormField("toemail", txtDest.getText());
            //byte[] b = txtSubject.getText().getBytes(StandardCharsets.UTF_8);
           // String utfSubject = new String(b);
            //http.addFormField("subject", utfSubject);
            http.addFormField("subject", txtSubject.getText());
            sbTxFile.append(txFile.getText());
            if (supportMsg) {
                sbTxFile.append("\r\n").append("\r\n");
                sbTxFile.append("------------------------------------------").append("\r\n");
                sbTxFile.append("   ").append(myConfig.getOS().toString());
                sbTxFile.append("   ").append(myConfig.getVersion()).append("\r\n");
                sbTxFile.append("------------------------------------------");
            }
            http.addFormField("msgtext", sbTxFile.toString());
            if (filePath != null && chPJ.isSelected()) {
                http.addFilePart("Fichier", uploadFile);
            }
            final byte[] bytes = http.finish();
            mailSended = true;
        
        } catch (Exception e) {
            e.printStackTrace();
            mailSended = false;
        }        
    }  
    
    private void mailClosing()  {
        if (mailSended) {
            subStage.close();
        } else {
            alertbox aError = new alertbox(myConfig.getLocale());
            aError.alertInfo(i18n.tr("Error sending mail"));  
        }
    }
    
    public void startSending()  {
        
                
        Task<Object> worker = new Task<Object>() {
            @Override
            protected Object call() throws Exception {
                sendMail();
                return null ;
                
            }
        
        };
        worker.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
            @Override
            public void handle(WorkerStateEvent t) {
                mailClosing();
            }
        });    

        ProgressDialog dlg = new ProgressDialog(worker);
        dlg.setHeaderText(i18n.tr("Send via e-mail"));
        dlg.setTitle("");
        Thread th = new Thread(worker);
        th.setDaemon(true);
        th.start();  
    }    
    
}
