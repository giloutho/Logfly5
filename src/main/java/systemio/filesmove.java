/*
 * Copyright Gil THOMAS
 * Ce fichier fait partie intégrante du projet Logfly
 * Pour tous les détails sur la licence du projet Logfly
 * Consulter le fichier LICENSE distribué avec le code source
 */
package systemio;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
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
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.xnap.commons.i18n.I18n;
import org.xnap.commons.i18n.I18nFactory;

/**
 *
 * @author gil
 */
public class filesmove {
    
    private static I18n i18n;     
    private final Stage dialogStage;
    private final ProgressBar pb = new ProgressBar();
    private final Label labelCount = new Label();
    private final Label labelSucceeded = new Label();
    private final Button btClose = new Button();
    private boolean taskOK;
    private static File[] filesToMove;
    private static String destPath;

    public boolean isTaskOK() {
        return taskOK;
    }
    
    public filesmove(File[] files, String newPath) throws InterruptedException {
        
        i18n = I18nFactory.getI18n(Logfly.Main.class.getClass());
        taskOK = false;
        filesToMove = files;
        destPath = newPath;
        
        // Mise en place de la fenêtre jauge
        dialogStage = new Stage();
        dialogStage.initStyle(StageStyle.UTILITY);
        dialogStage.setResizable(false);
        dialogStage.initModality(Modality.APPLICATION_MODAL);
               
        pb.setProgress(-1F);      
        
        VBox vBox = new VBox();
        vBox.setPadding(new Insets(5, 5, 5, 5));
        vBox.setSpacing(5);
        btClose.setVisible(false);
        btClose.setText(i18n.tr("Fermer"));
        btClose.setOnAction(new EventHandler<ActionEvent>() {            
            @Override
            public void handle(ActionEvent event) {
                dialogStage.close();
            }
        });
        vBox.setAlignment(Pos.CENTER);
        vBox.getChildren().addAll(pb,labelCount,labelSucceeded,btClose);

        Scene scene = new Scene(vBox, 300, 150);
        dialogStage.setScene(scene);
        
        serviceMove myService = new serviceMove();
        
        myService.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
            @Override
            public void handle(WorkerStateEvent t) {
                labelSucceeded.setText(i18n.tr("Transfert terminé..."));
                taskOK = true;                
                btClose.setVisible(true);
            }
        });
        
        myService.setOnFailed(new EventHandler<WorkerStateEvent>() {
            @Override
            public void handle(WorkerStateEvent t) {
                taskOK = false; 
                labelSucceeded.setText(i18n.tr("Problème pendant le transfert..."));
            }
        });
        
        // Binding des éléments de la fenêtre avec les évènements de la task
        pb.progressProperty().bind(myService.progressProperty());
        labelCount.textProperty().bind(myService.messageProperty());

        myService.start();
        dialogStage.showAndWait();
        
    }
    
    public class serviceMove extends Service<Void> {
 
        @Override
        protected Task<Void> createTask() {
            return new Task<Void>() {
                @Override
                protected Void call() throws Exception {
                    int lengthFiles = filesToMove.length;
                    int idx = 0;
                    String partMsg = " / "+String.valueOf(lengthFiles);
                    for (File f : filesToMove) {
                    try {                            
                        File dest = new File(destPath+f.getName());
                        Files.move(f.toPath(), dest.toPath(),StandardCopyOption.REPLACE_EXISTING);
                        idx++;
                        updateProgress(idx, lengthFiles);
                        updateMessage(String.valueOf(idx)+partMsg);
                        Thread.sleep(700);
                    } catch (IOException e) {
                        updateProgress(lengthFiles, lengthFiles);
                        updateMessage(String.valueOf(idx));                        
                    }                    
                }  
                
                    return null;
                }
            };
        }
    }
    
}
