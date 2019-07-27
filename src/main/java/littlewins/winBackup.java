/*
 * Copyright Gil THOMAS
 * This file forms an integral part of Logfly project
 * See the LICENSE file distributed with source code
 * for details of Logfly licence project
 */
package littlewins;

import com.chainstaysoftware.filechooser.DirectoryChooserFx;
import com.chainstaysoftware.filechooser.DirectoryChooserFxImpl;
import com.chainstaysoftware.filechooser.FileChooserFx;
import com.chainstaysoftware.filechooser.FileChooserFxImpl;
import com.chainstaysoftware.filechooser.ViewType;
import controller.RootLayoutController;
import dialogues.alertbox;
import dialogues.dialogbox;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.logging.Level;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.xnap.commons.i18n.I18n;
import settings.configProg;
import systemio.mylogging;

/**
 *
 * @author gil
 * fileChooserFX from https://github.com/ricemery/FileChooserFx
 */
public class winBackup {
    
    private Stage subStage;
    
    // Reference to the main application.
    private RootLayoutController rootController;    
    
    private I18n i18n; 
    private configProg myConfig;     
    private StringBuilder sbError;
    
    
    public winBackup(configProg pConfig, I18n pI18n,RootLayoutController rootlayout) {        
        myConfig = pConfig;        
        this.i18n = pI18n;        
        this.rootController = rootlayout;
        showWin();
    }    
    
    private void showWin() {
        subStage = new Stage();    
        Button btBackup = new Button(i18n.tr("Save the current flights log file"));
        btBackup.setOnAction((event) -> {
            backupLogFile();
        });
        Button btRestore = new Button(i18n.tr("Restore as the current flights log file"));
        btRestore.setOnAction((event) -> {            
            restoreLogFile();
        });
        
        // La Vbox qui va contenir chacun des éléments horizontaux définis ci dessus
        final VBox vbox = new VBox();
        vbox.setPadding(new Insets(20));
        vbox.setSpacing(20);      
        vbox.setAlignment(Pos.CENTER);
        vbox.getChildren().addAll(btBackup, btRestore);
        
        // Mise en place de la fenêtre
        subStage = new Stage();
        
        StackPane subRoot = new StackPane();
        subRoot.getChildren().add(vbox);
        
        Scene secondScene = new Scene(subRoot, 320, 120);

        // modal mode
        subStage.initModality(Modality.APPLICATION_MODAL);        
        subStage.setScene(secondScene);     
        subStage.showAndWait();                    
    }
    
    private void backupLogFile() {
        String dbName;     
        String backupName;    
        LocalDateTime currentTime = LocalDateTime.now();
        DateTimeFormatter myFormatter = DateTimeFormatter.ofPattern("yyMMdd_HHmmss");   
        String dateName = currentTime.format(myFormatter);        
        String[] sDb = myConfig.getDbName().split("\\.");
        if (sDb.length > 0) 
            dbName = sDb[0];            
        else
            dbName = myConfig.getDbName();               
        backupName = dbName+"_"+dateName+".dbk";

        if (myConfig.isValidConfig()) {             
            final DirectoryChooserFx dirChooser = new DirectoryChooserFxImpl();        
            dirChooser.setViewType(ViewType.ListWithPreview);
            dirChooser.setShowMountPoints(true);
            dirChooser.setDividerPosition(.15);
            dirChooser.setTitle(i18n.tr("Choose the destination folder"));       
            dirChooser.showDialog(null,fileOptional -> { 
                final String res = fileOptional.toString();
                String sPath;
                // Cancel result string is : Optional.empty
                if (res.contains("empty")) {
                    sPath = null;
                } else {
                    // result string is Optional[absolute_path...]
                    String[] s = res.split("\\[");
                    if (s.length > 1)
                        sPath = s[1].substring(0, s[1].length()-1);
                    else
                        sPath = res;
                }
                replyChooser(sPath,backupName);
            });       
        } else {
            alertbox aError = new alertbox(myConfig.getLocale());
            aError = new alertbox(myConfig.getLocale());
            aError.alertNumError(20);   // Invalid configuration            
        }                                                        
    }
    
    private void replyChooser(String strChooser, String backupName) {
        
        alertbox aError = new alertbox(myConfig.getLocale());
        if (strChooser != null) {
            try {
                File selectedDirectory = new File(strChooser);            
                if(selectedDirectory.exists() && selectedDirectory.isDirectory()){
                    Path srcPath = Paths.get(myConfig.getFullPathDb());
                    Path dstPath = Paths.get(selectedDirectory.getAbsolutePath()+File.separator+backupName);
                    Files.copy(srcPath, dstPath, StandardCopyOption.REPLACE_EXISTING);  
                    aError.alertNumError(0);   // Successful operation 
                    subStage.close();
                }                
            } catch (Exception ex) {
                sbError = new StringBuilder(this.getClass().getName()+"."+Thread.currentThread().getStackTrace()[1].getMethodName());
                sbError.append("\r\n").append(ex.toString());
                mylogging.log(Level.SEVERE, sbError.toString());  
                aError = new alertbox(myConfig.getLocale());
                aError.alertError(ex.getClass().getName() + ": " + ex.getMessage());                
            }                    
        }
    }
    
    private void restoreLogFile() {
        if (myConfig.isValidConfig()) { 
            final FileChooserFx fileChooser = new FileChooserFxImpl();
            fileChooser.setTitle(i18n.tr("Choose the file to restore"));
            fileChooser.setShowHiddenFiles(false);
            fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("DBK files (dbk)", "*.dbk"));
            fileChooser.setShowMountPoints(true);       
            fileChooser.setViewType(ViewType.List);
            fileChooser.setDividerPositions(.15, .30);
            fileChooser.showOpenDialog(null,fileOptional -> { 
                final String res = fileOptional.toString();
                String sPath;
                // Cancel result string is : Optional.empty
                if (res.contains("empty")) {
                    sPath = null;
                } else {
                    // result string is Optional[absolute_path...]
                    String[] s = res.split("\\[");
                    if (s.length > 1)
                        sPath = s[1].substring(0, s[1].length()-1);
                    else
                        sPath = res;
                }
                replyRestoreChooser(sPath);
            });
        } else {
            alertbox aError = new alertbox(myConfig.getLocale());
            aError = new alertbox(myConfig.getLocale());
            aError.alertNumError(20);   // Invalid configuration            
        }        
    }
    
    private void replyRestoreChooser(String strChooser) {
        alertbox aError = new alertbox(myConfig.getLocale());
        if (strChooser != null) {
            try {
                File selectedFile = new File(strChooser);            
                if(selectedFile.exists()){
                    dialogbox dConfirm = new dialogbox(i18n);
                    StringBuilder sbMsg = new StringBuilder(); 
                    sbMsg.append(i18n.tr("Replace current log file by ")).append(selectedFile.getName());
                    if (dConfirm.YesNo(i18n.tr("Restore"), sbMsg.toString()))   { 
                        // step 1 : backup renamed
                        String s[] = selectedFile.getName().split("\\_");
                        if (s.length > 1) {
                            String newName = s[0]+".db"; 
                            // step 2 : file copy
                            Path srcPath = Paths.get(selectedFile.getAbsolutePath());
                            Path dstPath = Paths.get(myConfig.getPathDb()+File.separator+newName);                        
                            Files.copy(srcPath, dstPath, StandardCopyOption.REPLACE_EXISTING);                          
                            // step 3 : change current log file
                            if (myConfig.dbSwitch(newName)) {
                                rootController.changeCarnetView();
                                myConfig.setValidConfig(true);
                                myConfig.writeProperties();
                            }
                            aError.alertNumError(0);   // Successful operation 
                            subStage.close();                            
                        }
                    }
                }
            } catch (Exception ex) {
                sbError = new StringBuilder(this.getClass().getName()+"."+Thread.currentThread().getStackTrace()[1].getMethodName());
                sbError.append("\r\n").append(ex.toString());
                mylogging.log(Level.SEVERE, sbError.toString());  
                aError = new alertbox(myConfig.getLocale());
                aError.alertError(ex.getClass().getName() + ": " + ex.getMessage());                
            }             
        }
    }
}
