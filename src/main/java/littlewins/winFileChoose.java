/*
 * Copyright Gil THOMAS
 * This file forms an integral part of Logfly project
 * See the LICENSE file distributed with source code
 * for details of Logfly licence project
 */
package littlewins;

import com.chainstaysoftware.filechooser.FileChooserFx;
import com.chainstaysoftware.filechooser.FileChooserFxImpl;
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
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.apache.commons.io.FilenameUtils;
import org.xnap.commons.i18n.I18n;
import settings.configProg;
import settings.fileType;
import systemio.mylogging;

/**
 *
 * @author gil
 * fileChooserFX from https://github.com/ricemery/FileChooserFx
 * A separate window is used to close the dialogue (FileChooserFx) before other treatments
 * Moreover it allows to give an explicit help message
 */
public class winFileChoose {
    
    private I18n i18n; 
    private configProg myConfig; 
    private Stage subStage;
    private Label lbInfo;    
    private StringBuilder sbInfo = new StringBuilder();
    private String sTitle;
    private String btCaption;
    private fileType winType;
    private File selectedFile = null;
    private String extFormat;
    private File initDir;
    private String RC = "\n";

    public winFileChoose(configProg pConfig, I18n pI18n, fileType selType, String pInitDir) {        
        myConfig = pConfig;        
        this.i18n = pI18n;  
        this.winType = selType;
        if (pInitDir != null && !pInitDir.equals("")) {
            File f = new File(pInitDir);
            if (f.exists() && f.isDirectory()) 
                initDir = f;
            else
                initDir = null;                   
        } else {
            switch (myConfig.getOS()) {
                case WINDOWS :
                    initDir = new File (System.getProperty("user.home")+"\\Documents");                      
                    break;
                case MACOS :
                    initDir = new File (System.getProperty("user.home")+"/Documents");   
                    break;
                case LINUX :
                    initDir = new File (System.getProperty("user.home")+"/.logfly");                    
                    break;
            }
        }
        setInfos();
        showWin();
    }

    public File getSelectedFile() {
        return selectedFile;
    }

    public void setSelectedFile(File selectedFile) {
        this.selectedFile = selectedFile;
    }        
    
    public String getExtFormat() {
        return extFormat;
    }         
    
    private void setInfos() {
        switch (winType) {
            case IgcGpx :
                // GPX or IGC used with FullMapController
                sbInfo.append(i18n.tr("Supported file types"));
                sbInfo.append(" ").append("IGC").append(" ").append(i18n.tr("or"));
                sbInfo.append(" ").append("GPX");                
                btCaption = i18n.tr("Select a track");
                sTitle = "";
                break;
            case OpenAir :
                // txt OpenAir used with AirSpaceController
                sbInfo.append(i18n.tr("Supported file types"));
                sbInfo.append(" : ").append("OpenAir");
                btCaption = i18n.tr("Select a file");
                sTitle = "";
                break;             
            case OACheck :
                // txt OpenAir used with FullMapController
                sbInfo.append(i18n.tr("Airspace infringements check")).append(RC);
                sbInfo.append(i18n.tr("Supported file types"));
                sbInfo.append(" : ").append("OpenAir");
                btCaption = i18n.tr("Select a file");
                sTitle = "";
                break;
            case db :
                // db file used with ConfigViewController
                sbInfo.append(i18n.tr("Select a logbook copy to restore")).append(RC);
                sbInfo.append(i18n.tr("Supported file types"));
                sbInfo.append(" : ").append("db");
                btCaption = i18n.tr("Select a file");
                sTitle = "";
                break;             
            case dbk :
                // dbk file used with winBackup
                sbInfo.append(i18n.tr("Choose the file to restore")).append(RC);
                sbInfo.append(i18n.tr("Supported file types"));
                sbInfo.append(" : ").append("dbk");
                btCaption = i18n.tr("Select a file");
                sTitle = "";
                break;           
            case csv :
                sbInfo.append(i18n.tr("Supported file types"));
                sbInfo.append(" : ").append("Csv");
                btCaption = i18n.tr("Select a file");
                sTitle = "";
                break;                   
            case wpt :
                sbInfo.append(i18n.tr("Supported formats")).append(" : ").append("\r\n");
                sbInfo.append("Ozi, PCX5, Kml, Gpx, Cup, CompeGPS, XCP");
                btCaption = i18n.tr("Select a file");
                sTitle = "";
                break;        
            case xcp :
                sbInfo.append(i18n.tr("Supported file types"));
                sbInfo.append(" : ").append("Xcp");
                btCaption = i18n.tr("Select a file");
                sTitle = "";
                break;                  
            default:
                throw new AssertionError();
        }
    }

    private void showWin() {
        
        subStage = new Stage();
         
        Group newRoot = new Group();
        Scene secondScene = new Scene(newRoot, 300, 130); 

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
                selectFile();
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
    
    public void winClose() {
        subStage.close();
    }
    
    private void selectFile() {
        final FileChooserFx fileChooser = new FileChooserFxImpl();
        fileChooser.setTitle(sTitle);
        fileChooser.setShowHiddenFiles(false);
        fileChooser.setViewType(ViewType.List);
        if (initDir != null) fileChooser.setInitialDirectory(initDir);
        switch (winType) {
            case IgcGpx :
                fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("Track files (igc)", "*.igc", "*.IGC")); 
                fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("Track files (gpx)", "*.gpx", "*.GPX")); 
                fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("All files", "*.*")); 
                fileChooser.setSelectedExtensionFilter(new FileChooser.ExtensionFilter("Track files (igc ", "*.igc"));  
                break;
            case OpenAir :
                fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("Airspaces (txt)", "*.txt","*.TXT"));        
                fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("All files", "*.*")); 
                fileChooser.setSelectedExtensionFilter(new FileChooser.ExtensionFilter("Airspaces (txt)", "*.txt"));        
                break;
            case OACheck :
                fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("Airspaces (txt)", "*.txt","*.TXT"));        
                fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("All files", "*.*")); 
                fileChooser.setSelectedExtensionFilter(new FileChooser.ExtensionFilter("Airspaces (txt)", "*.txt"));     
                break;      
            case db :
                fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("Logbook files (db)", "*.db"));  
                fileChooser.setSelectedExtensionFilter(new FileChooser.ExtensionFilter("Logbook files (db)", "*.db"));  
                break;      
            case dbk :
                fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("Logbook backup (dbk)", "*.dbk"));        
                fileChooser.setSelectedExtensionFilter(new FileChooser.ExtensionFilter("Logbook backup (dbk)", "*.dbk"));        
                break; 
            case csv : 
                fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("Csv files (csv)", "*.csv","*.CSV"));        
                fileChooser.setSelectedExtensionFilter(new FileChooser.ExtensionFilter("Csv files (csv)", "*.csv"));      
                break;                 
            case xcp :
                fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("Xcplanner files (xcp)", "*.xcp"));  
                fileChooser.setSelectedExtensionFilter(new FileChooser.ExtensionFilter("Xcplanner files (xcp)", "*.xcp"));  
                break;                      
        }                
        fileChooser.setShowMountPoints(true);       
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
            setSelectedFile(f);
            extFormat = FilenameUtils.getExtension(strChooser);
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
