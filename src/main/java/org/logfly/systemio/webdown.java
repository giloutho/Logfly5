/*
 * Copyright Gil THOMAS
 * This file forms an integral part of Logfly project
 * See the LICENSE file distributed with source code
 * for details of Logfly licence project
 */
package org.logfly.systemio;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import javafx.application.Platform;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.xnap.commons.i18n.I18n;

/**
 *
 * @author gil with great help of java-buddy.blogspot.fr/2013/08/implement-custom-properties-for-bind.html
 */

public class webdown {

 private String strUrl;
    private String dirDestination;
    private String strSuccessMsg;
    private uploadService myService;
    private I18n i18n; 
    private boolean downSuccess;
    private String downPath;
    private String textButton;
    
    public webdown(String reqUrl, String reqDirDestination, I18n pI18n, String reqSuccessMsg) {
        
        this.strUrl = reqUrl;
        this.dirDestination = reqDirDestination;   
        this.i18n = pI18n;
        downSuccess = false;
        if (reqSuccessMsg.equals("IMPORT CSV")) {
            textButton = i18n.tr("Importer");
            this.strSuccessMsg = i18n.tr("Le fichier a été téléchargé"); 
        } else {
            this.strSuccessMsg = reqSuccessMsg; 
            textButton = i18n.tr("Fermer");
        }
        winDisplay();        
    }

    public boolean isDownSuccess() {
        return downSuccess;
    }

    public String getDownPath() {
        return downPath;
    }
                       
    private void winDisplay() {
        Stage subStage = new Stage();
        final ProgressBar progressBar = new ProgressBar();
        progressBar.setPrefWidth(200);
        progressBar.setVisible(false);
        final Label labelCount = new Label();
        final Label labelSucceeded = new Label();
        
        myService = new uploadService(strUrl,dirDestination);                                
        
        myService.processPercentProperty().addListener(new ChangeListener(){
 
            @Override
            public void changed(ObservableValue ov, Object t, Object t1) {
                 
                double p = (double)(((Integer)t1).intValue());
                progressBar.setProgress(p/100);
            }
        });
        
        labelCount.textProperty().bind(myService.MessagePercent);                
        
        Button btnClose = new Button(textButton);
        btnClose.setOnAction((event) -> {           
            subStage.close();
        });
        btnClose.setVisible(false);
        
        Button btnCancel = new Button(i18n.tr("Annuler"));
        btnCancel.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent t) {
               // labelState.setText(myService.getState().toString());
               myService.cancel();
               labelSucceeded.setText(i18n.tr("Le téléchargement est interrompu"));
               btnCancel.setVisible(false);
               btnClose.setVisible(true);
               downSuccess = false;
            }
        });
        
        myService.setOnFailed(new EventHandler<WorkerStateEvent>() {
            @Override
            public void handle(WorkerStateEvent t) {
                labelSucceeded.setText(i18n.tr("Le téléchargement a échoué"));
                btnCancel.setVisible(false);
                btnClose.setVisible(true);
                downSuccess = false;
            }
        });
        
        myService.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
            @Override
            public void handle(WorkerStateEvent t) {
                labelSucceeded.setText(i18n.tr(strSuccessMsg));
                btnCancel.setVisible(false);
                btnClose.setVisible(true);
                downSuccess = true;
            }
        });
        
        HBox hbox = new HBox();
        hbox.setPadding(new Insets(10, 10, 10, 10));
        hbox.setSpacing(10);
        hbox.getChildren().addAll(progressBar, labelCount);
         
        
        HBox btnBox = new HBox();
        btnBox.setPadding(new Insets(10, 10, 10, 10));
        btnBox.setSpacing(10);
        btnBox.getChildren().addAll(btnCancel, btnClose);
        btnBox.setAlignment(Pos.CENTER_RIGHT);
        
        VBox vBox = new VBox();
        vBox.setPadding(new Insets(10, 10, 10, 10));
        vBox.setSpacing(10);
        vBox.getChildren().addAll(hbox, labelSucceeded, btnBox);
        
        
        
        StackPane subRoot = new StackPane();
        subRoot.getChildren().add(vBox);
       
        
        Scene secondScene = new Scene(subRoot, 290, 150);
        // modal mode
        subStage.initModality(Modality.APPLICATION_MODAL);        
        subStage.setScene(secondScene);     
        
        progressBar.setVisible(true);
        labelSucceeded.setText(i18n.tr("Téléchargement en cours..."));
        myService.start();
        
        subStage.showAndWait();
    }
    
    private class uploadService extends Service<Void> {
        
        private  String strUrl;
        private String dirDestination;
        
        uploadService(String reqUrl, String reqDirDestination) {
            this.strUrl = reqUrl;
            this.dirDestination = reqDirDestination;
        }
 
        private final IntegerProperty processPercent = new SimpleIntegerProperty();
 
        public int getProcessPercent() {
            return processPercent.get();
        }
 
        public void setProcessPercent(int value) {
            processPercent.set(value);
        }
 
        public IntegerProperty processPercentProperty() {
            return processPercent;
        }
        private final StringProperty MessagePercent = new SimpleStringProperty();
 
        public String getMessagePercent() {
            return MessagePercent.get();
        }
 
        public void setMessagePercent(String value) {
            MessagePercent.set(value);
        }
 
        public StringProperty MessagePercentProperty() {
            return MessagePercent;
        }
 
        @Override
        protected Task<Void> createTask() {
            return new Task<Void>() {
                @Override
                protected Void call() throws Exception {
                    try {                   
                        URL url = new java.net.URL(strUrl);
                        downPath = dirDestination + File.separator + new File(url.getFile()).getName();                        
                        URLConnection conn = url.openConnection();
                        int sizeFile = conn.getContentLength();
                        System.out.println("url "+strUrl+" downPath "+downPath+" size : "+sizeFile);
                        java.io.InputStream in = conn.getInputStream();

                        File dstfile = new File(downPath);
                        System.out.println(dstfile.getAbsolutePath());
                        OutputStream out = new FileOutputStream(dstfile);

                        byte[] buffer = new byte[512];
                        int length;
                        long total = 0;
                        
                        while ((length = in.read(buffer)) > 0) {
                            if (isCancelled()) {
                                break;
                            }
                            out.write(buffer, 0, length);
                            total += length;
                            final int iTotal = (int)((total*100)/sizeFile);
                            Platform.runLater(new Runnable() {
                                    @Override
                                    public void run() {
                                        setProcessPercent(iTotal);
                                        setMessagePercent(String.valueOf(iTotal)+"%");    
                                    }
                                });
                        }
                        
                        in.close();
                        out.close();
                        
                    } catch (MalformedURLException e) {
                        System.out.println("MalformedURLException "+e.getMessage());
                    } catch (IOException e) {
                        System.out.println("IOException "+e.getMessage());
                    }                     
                    return null;
                }
            };
        }
    }    
    
}
