/*
 * Copyright Gil THOMAS
 * This file forms an integral part of Logfly project
 * See the LICENSE file distributed with source code
 * for details of Logfly licence project
 */
package littlewins;

import com.chainstaysoftware.filechooser.DirectoryChooserFx;
import com.chainstaysoftware.filechooser.DirectoryChooserFxImpl;
import com.chainstaysoftware.filechooser.ViewType;
import dialogues.alertbox;
import java.io.File;
import java.util.logging.Level;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.xnap.commons.i18n.I18n;
import settings.configProg;
import systemio.mylogging;

/**
 *
 * @author gil
 * fileChooserFX from https://github.com/ricemery/FileChooserFx
 * A separate window is used to close the dialogue (FileChooserFx) before other treatments
 * Moreover it allows to give an explicit help message 
 * 
 */
public class winDirChoose {
    
    private I18n i18n; 
    private configProg myConfig; 
    private Stage subStage;
    private Label lbInfo;    
    private StringBuilder sbInfo = new StringBuilder();
    private String sTitle;
    private String btCaption;
    private int winType;
    private File selectedFolder = null;
    private File initDir;
    private String RC = "\n";    
    
    public winDirChoose(configProg pConfig, I18n pI18n, int pWinType, String pInitDir) {        
        myConfig = pConfig;        
        this.i18n = pI18n;  
        this.winType = pWinType;
        if (pInitDir != null && !pInitDir.equals("")) {
            File f = new File(pInitDir);
            if (f.exists() && f.isDirectory()) 
                initDir = f;
            else
                initDir = null;                   
        } else
            initDir = null;
        setInfos(winType);
        showWin();
    }    

    public void setSelectedFolder(File selectedFolder) {
        this.selectedFolder = selectedFolder;
    }
    
    public File getSelectedFolder() {
        return selectedFolder;
    }
    
    private void setInfos(int num) {
        switch (winType) {
            case 1 :
                // ImportViewController
                sbInfo.append(i18n.tr("Select the folder of GPS tracks to import")).append(RC);
                sbInfo.append("** " ).append(i18n.tr("Please note, only folders are displayed")).append(" **");
                btCaption = i18n.tr("Select a folder");
                sTitle = "";
                break;      
            case 2 :
                // ConfigViewController  Change PathW
                sbInfo.append(i18n.tr("Select the default Logfly folder")).append(RC);
                sbInfo.append("** " ).append(i18n.tr("Please note, only folders are displayed")).append(" **");           
                btCaption = i18n.tr("Select a folder");
                sTitle = "";
                break;
            case 3 :
                // ConfigViewController  copyInNewFolder  selectNewFolderDb
                sbInfo.append(i18n.tr("Select the new logbook folder")).append(RC);
                sbInfo.append("** " ).append(i18n.tr("Please note, only folders are displayed")).append(" **");            
                btCaption = i18n.tr("Select a folder");
                sTitle = "";
                break;       
            case 4 :
                // ConfigViewController  changePathContest
                sbInfo.append(i18n.tr("Select the folder for online contest")).append(RC);
                sbInfo.append("** " ).append(i18n.tr("Please note, only folders are displayed")).append(" **");            
                btCaption = i18n.tr("Select a folder");
                sTitle = "";
                break;       
            case 5 :
                // ConfigViewController  moveDb
                sbInfo.append(i18n.tr("Select the new logbook folder")).append(RC);
                sbInfo.append("** " ).append(i18n.tr("Please note, only folders are displayed")).append(" **");            
                btCaption = i18n.tr("Select a folder");
                sTitle = "";
                break;
            case 6 :
                // winbackup backupLogFile   and RootLayoutController Copydb
                sbInfo.append(i18n.tr("Select the folder where the logbook will be saved")).append(RC);
                sbInfo.append("** " ).append(i18n.tr("Please note, only folders are displayed")).append(" **");           
                btCaption = i18n.tr("Select a folder");
                sTitle = "";
                break;    
            case 7 :
                // ConfigViewController  Import Folder
                sbInfo.append(i18n.tr("Select the import folder for GPS tracks")).append(RC);
                sbInfo.append("** " ).append(i18n.tr("Please note, only folders are displayed")).append(" **");            
                btCaption = i18n.tr("Select a folder");
                sTitle = "";
                break;      
            case 8 :
                // ConfigViewController  Syride Folder
                sbInfo.append(i18n.tr("Select the Syride folder for GPS tracks")).append(RC);
                sbInfo.append("** " ).append(i18n.tr("Please note, only folders are displayed")).append(" **");            
                btCaption = i18n.tr("Select a folder");
                sTitle = "";
                break;                     
            default:
                throw new AssertionError();
        }
    }    
    
    private void showWin() {
        
        subStage = new Stage();
         
        Group newRoot = new Group();
        Scene secondScene = new Scene(newRoot, 350, 130); 

        VBox vBox = new VBox(20);
       // vBox.setSpacing(10); 
        vBox.setPadding(new Insets(20, 10, 10, 10));
       

        lbInfo = new Label();
        lbInfo.setText(sbInfo.toString());      
        lbInfo.setAlignment(Pos.CENTER);
        lbInfo.setMinWidth(secondScene.getWidth() - 20);

        HBox hBox = new HBox();
        hBox.setSpacing(10); 
        hBox.setPadding(new Insets(10, 10, 10, 10));
        hBox.setAlignment(Pos.CENTER);
        
        Button btnOpen = new Button(btCaption);
        btnOpen.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent t) {
                selectFolder();
            }
        });

        Button btnClose = new Button(i18n.tr("Cancel"));
        btnClose.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent t) {
                subStage.close();
            }
        });
                
        hBox.getChildren().addAll(btnOpen, btnClose);
        
        vBox.getChildren().addAll(lbInfo, hBox);
        newRoot.getChildren().add(vBox);  
        
        subStage.initModality(Modality.APPLICATION_MODAL);        
        subStage.setScene(secondScene);     
        subStage.showAndWait();        
    }     
    
    private void selectFolder() {
        final DirectoryChooserFx dirChooser = new DirectoryChooserFxImpl();
        dirChooser.setTitle(sTitle);        
        if (initDir != null) dirChooser.setInitialDirectory(initDir);          
        dirChooser.setShowMountPoints(true);       
        dirChooser.setViewType(ViewType.List);
        dirChooser.setDividerPosition(.15);      
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
            replyChooser(sPath);
        });        
    }
    
    private void replyChooser(String strChooser) {
        File f = null;
        alertbox aError = new alertbox(myConfig.getLocale());        
        try {
            if (strChooser != null) {
                f = new File(strChooser);            
            }                   
            setSelectedFolder(f);
        } catch (Exception ex) {
            StringBuilder sbError = new StringBuilder(this.getClass().getName()+"."+Thread.currentThread().getStackTrace()[1].getMethodName());
            sbError.append("\r\n").append(ex.toString());
            mylogging.log(Level.SEVERE, sbError.toString());  
            aError = new alertbox(myConfig.getLocale());
            aError.alertError(ex.getClass().getName() + ": " + ex.getMessage());                
        } finally {
            subStage.close();
        }                    
    }    
}
