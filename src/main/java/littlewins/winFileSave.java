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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.controlsfx.dialog.CommandLinksDialog;
import org.xnap.commons.i18n.I18n;
import settings.configProg;
import settings.fileType;
import systemio.mylogging;

/**
 *
 * @author gil
 * it's necessary to close FileChooserFx before other treatments
 */
public class winFileSave {
    
    private I18n i18n; 
    private configProg myConfig; 
    private Stage subStage;
    private Label lbInfo;    
    private StringBuilder sbInfo = new StringBuilder();
    private String sTitle;
    private String btCaption;
    private String extFormat;
    private String wptFormat;
    private File selectedFile = null;
    private File initDir;
    private String initName;
    private String RC = "\n";     
    private fileType winType;
    private boolean noDisplay = false;
    
    public winFileSave(configProg pConfig, I18n pI18n, fileType selType, String pInitDir, String pInitName) {        
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
        initName = pInitName;
        setInfos();
        if (!noDisplay) {
            showWin();
        }
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

    public String getWptFormat() {
        return wptFormat;
    }        
    
    private void setInfos() {
        List<CommandLinksDialog.CommandLinksButtonType> links = new ArrayList<>(); 
        CommandLinksDialog dg;
        Optional<ButtonType> result;
        String resDg;
        switch (winType) {      
            case IgcGpx :     
                links.add(new CommandLinksDialog.CommandLinksButtonType(i18n.tr("IGC Format"),i18n.tr("The track is saved in IGC format with the .igc extension"),true));
                links.add(new CommandLinksDialog.CommandLinksButtonType(i18n.tr("GPX Format"),i18n.tr("The track is saved in GPX format with the .gpx extension"),false));        
                dg = new CommandLinksDialog(links);
                dg.setTitle(i18n.tr("Exported file type"));
                result = dg.showAndWait();
                resDg = result.get().getText(); 
                if (resDg != null && resDg != "") {
                    // resDg aura une valeur différente selon la langue utilisée Voir plus bas
                    String upRes = resDg.toUpperCase();
                    if (upRes.contains("IGC")) {
                        sbInfo.append(i18n.tr("Supported file types"));
                        sbInfo.append(" : ").append("IGC").append(" (*.igc)");
                        btCaption = i18n.tr("Choose a destination and a filename");
                        sTitle = "";                    
                        extFormat = ".igc";
                    } else if (upRes.contains("GPX")) {
                        sbInfo.append(i18n.tr("Supported file types"));
                        sbInfo.append(" : ").append("GPX").append(" (*.gpx)");
                        btCaption = i18n.tr("Choose a destination and a filename");
                        sTitle = "";                    
                        extFormat = ".gpx";
                    }
                    else
                        extFormat = null; 
                } else {
                    noDisplay = true;
                }
                break;
            case OpenAir :
                // txt OpenAir used with AirSpaceController
                sbInfo.append(i18n.tr("Supported file types"));
                sbInfo.append(" : ").append("OpenAir").append(" (*.txt)");
                btCaption = i18n.tr("Choose a destination and a filename");
                sTitle = "";
                extFormat = ".txt";
                break;    
            case Gpx :
                sbInfo.append(i18n.tr("Supported file types"));
                sbInfo.append(" : ").append("GPX").append(" (*.gpx)");
                btCaption = i18n.tr("Choose a destination and a filename");
                sTitle = "";
                extFormat = ".gpx";
                break;                    
            case Kml :
                sbInfo.append(i18n.tr("Supported file types"));
                sbInfo.append(" : ").append("KML").append(" (*.kml)");
                btCaption = i18n.tr("Choose a destination and a filename");
                sTitle = "";
                extFormat = ".kml";
                break;     
            case csv :
                sbInfo.append(i18n.tr("Supported file types"));
                sbInfo.append(" : ").append("CSV").append(" (*.csv)");
                btCaption = i18n.tr("Choose a destination and a filename");
                sTitle = "";
                extFormat = ".csv";
                break;         
           case wpt :      
                links.add(new CommandLinksDialog.CommandLinksButtonType(i18n.tr("OziExplorer Format"),i18n.tr("The file is saved in Oziexplorer format with the .wpt extension"),true));
                links.add(new CommandLinksDialog.CommandLinksButtonType(i18n.tr("CompeGPS Format"),i18n.tr("The file is saved in CompeGPS format with the .wpt extension"),false));
                links.add(new CommandLinksDialog.CommandLinksButtonType(i18n.tr("PCX5 Format"),i18n.tr("The file is saved in PCX5 format with the .pcx extension"),false));
                links.add(new CommandLinksDialog.CommandLinksButtonType(i18n.tr("Kml Format"),i18n.tr("The file is saved in kml format with the .kml extension"),false));                
                links.add(new CommandLinksDialog.CommandLinksButtonType(i18n.tr("GPX Format"),i18n.tr("The file is saved in GPX format with the .gpx extension"),false));
                links.add(new CommandLinksDialog.CommandLinksButtonType(i18n.tr("Cup Format"),i18n.tr("The file is saved in Cup format with the .cup extension"),false));                          
                dg = new CommandLinksDialog(links);
                dg.setTitle(i18n.tr("Exported file format"));
                result = dg.showAndWait();
                resDg = result.get().getText(); 
                if (resDg != null && resDg != "") {
                    // resDg aura une valeur différente selon la langue utilisée
                    // Ozi explorer format (english)
                    // Format OZI (french) it's uppercase !!!
                    String upRes = resDg.toUpperCase();
                    if (upRes.contains("OZI")) {
                        sbInfo.append(i18n.tr("OziExplorer Format"));
                        sbInfo.append(" : ").append("WPT").append(" (*.wpt)");
                        btCaption = i18n.tr("Choose a destination and a filename");
                        sTitle = "";                    
                        extFormat = ".wpt";
                        wptFormat = "1";
                    } else if (upRes.contains("COMPE")) {
                        sbInfo.append(i18n.tr("CompeGPS Format"));
                        sbInfo.append(" : ").append("WPT").append(" (*.wpt)");
                        btCaption = i18n.tr("Choose a destination and a filename");
                        sTitle = "";                    
                        extFormat = ".wpt";
                        wptFormat = "2";
                    } else if (upRes.contains("PCX")) {
                        sbInfo.append(i18n.tr("PCX5 Format"));
                        sbInfo.append(" : ").append("PCX5").append(" (*.pcx)");
                        btCaption = i18n.tr("Choose a destination and a filename");
                        sTitle = "";                    
                        extFormat = ".pcx";
                        wptFormat = "3";
                    } else if (upRes.contains("KML")) {
                        sbInfo.append(i18n.tr("Kml Format"));
                        sbInfo.append(" : ").append("KML").append(" (*.kml)");
                        btCaption = i18n.tr("Choose a destination and a filename");
                        sTitle = "";                    
                        extFormat = ".kml";
                        wptFormat = "4";
                    } else if (upRes.contains("GPX")) {
                        sbInfo.append(i18n.tr("GPX Format"));
                        sbInfo.append(" : ").append("GPX").append(" (*.gpx)");
                        btCaption = i18n.tr("Choose a destination and a filename");
                        sTitle = "";                    
                        extFormat = ".gpx";
                        wptFormat = "5";
                    } else if (upRes.contains("CUP")) {
                        sbInfo.append(i18n.tr("Cup Format"));
                        sbInfo.append(" : ").append("CUP").append(" (*.cup)");
                        btCaption = i18n.tr("Choose a destination and a filename");
                        sTitle = "";                    
                        extFormat = ".cup";
                        wptFormat = "6";
                    }
                    else
                        extFormat = null; 
                } else {
                    noDisplay = true;
                }
                break;                
            default:
                throw new AssertionError();
        }
    }

    private void showWin() {
        
        subStage = new Stage();
         
        Group newRoot = new Group();
        Scene secondScene = new Scene(newRoot, 380, 130); 

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

    private void selectFile() {
        final FileChooserFx fileChooser = new FileChooserFxImpl();
        fileChooser.setTitle(sTitle);
        fileChooser.setShowHiddenFiles(false);      
        fileChooser.setShowMountPoints(true);       
        fileChooser.setViewType(ViewType.List);
        fileChooser.setDividerPositions(.15, .30);
        if (initDir != null) fileChooser.setInitialDirectory(initDir);
        if (initName != null) fileChooser.setInitialFileName(initName);
        switch (winType) {
            case IgcGpx :
                switch (extFormat) {
                    case ".igc":
                        fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("IGC files (igc)", "*.igc", "*.IGC"));        
                        break;
                    case ".gpx" :
                        fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("GPX files (gpx)", "*.gpx", "*.GPX"));  
                    break;                        
                }    
                break;
            case OpenAir :
                fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("airspaces files (txt)", "*.txt", "*.TXT"));        
                break;
            case Gpx :
                fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("GPX files (gpx)", "*.gpx", "*.GPX"));  
                break;                
            case Kml :
                fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("KML files (kml)", "*.kml", "*.KML"));  
                break;                  
            case csv :
                fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("CSV files (csv)", "*.csv", "*.CSV"));  
                break;   
            case wpt :
                switch (wptFormat) {
                    case "1":
                        fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("OZI files (wpt)", "*.wpt", "*.WPT"));    
                        break;
                    case "2":
                        fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("CompeGPS files (wpt)", "*.wpt", "*.WPT"));        
                        break;                        
                    case "3":
                        fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("PCX5 files (pcx)", "*.pcx", "*.PCX"));        
                        break;    
                    case "4":
                        fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("KML files (kml)", "*.kml", "*.KML"));        
                        break;                        
                    case "5":
                        fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("GPX files (gpx)", "*.gpx", "*.GPX"));        
                        break;                        
                    case "6" :
                        fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("CUP files (cup)", "*.cup", "*.CUP"));  
                    break;                        
                }    
                break;                
            default:
                throw new AssertionError();
        }           
        fileChooser.showSaveDialog(null,fileOptional -> { 
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
        int res = -1;
        alertbox aError = new alertbox(myConfig.getLocale());
        if (strChooser != null) {
            try {
                File save = new File(strChooser);  
                if (!save.getPath().toLowerCase().endsWith(extFormat)) { 
                    save = new File(save.getPath() + extFormat); 
                }
                setSelectedFile(save);
                subStage.close();
            } catch (Exception ex) {
                StringBuilder sbError = new StringBuilder(this.getClass().getName()+"."+Thread.currentThread().getStackTrace()[1].getMethodName());
                sbError.append("\r\n").append(ex.toString());
                mylogging.log(Level.SEVERE, sbError.toString());  
                aError = new alertbox(myConfig.getLocale());
                aError.alertError(ex.getClass().getName() + ": " + ex.getMessage());                
            }                    
        }                
    }    
    
    
}
