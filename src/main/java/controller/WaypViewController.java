/*
 * Copyright Gil THOMAS
 * This file forms an integral part of Logfly project
 * See the LICENSE file distributed with source code
 * for details of Logfly licence project
 */
package controller;

import Logfly.Main;
import dialogues.alertbox;
import dialogues.dialogbox;
import geoutils.elevationapi;
import geoutils.position;
import gps.compass;
import gps.connect;
import gps.element;
import gps.gpsdump;
import static gps.gpsutils.ajouteChecksum;
import gps.jsFlytec15;
import gps.oudie;
import gps.reversale;
import gps.skytraax;
import gps.skytraxx3;
import igc.pointIGC;
import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import leaflet.map_waypoints;
import littlewins.winFileChoose;
import littlewins.winFileSave;
import littlewins.winGPS;
import littlewins.winMail;
import littlewins.winPoint;
import littlewins.winSearchCities;
import littlewins.winTrackFile;
import littlewins.winUsbWWayp;
import littlewins.winUsbWayp;
import model.Balisemodel;
import model.Sitemodel;
import netscape.javascript.JSObject;
import org.controlsfx.dialog.CommandLinksDialog;
import org.controlsfx.dialog.ProgressDialog;
import org.xnap.commons.i18n.I18n;
import org.xnap.commons.i18n.I18nFactory;
import settings.configProg;
import settings.fileType;
import systemio.mylogging;
import waypio.pointRecord;
import waypio.wpreadfile;
import waypio.wpwritefile;


/**
 *
 * @author gil
 * 
 * interaction between WaypViewController and Javascript
 * 
 *      - New waypoint -> short name (Balise) and long name (Desc) are defined in a form called Coord
 *      - a javascript function createNew(NomPos) is called
 *          this function call an elevation from google.maps.ElevationService
 *          result index point, latitude, longitude and altitude are pushed in the title of html page
 *      - a java function decodeCoord is triggered with a title changement
 *      - decodeCoord call updateDescription to update the waypoint description with altitude
 *      - updateDescription call a javascript function ChangeDesc(i,NewDesc) 
 *      
 */
public class WaypViewController {
      
    @FXML
    private TextField txPrefix;
    @FXML
    private Button btNewSet;        
    @FXML
    private Label lbInfo;
    @FXML
    private Button btReadFile;    
    @FXML
    private Button btReadGps;
    @FXML
    private Button btNew;
    @FXML
    private Button btWriteFile;
    @FXML
    private Button btWriteGPS;
    @FXML
    private Button btMail;
    @FXML
    private Button btEarth;  
    @FXML
    private Button btBbox;
    @FXML
    private CheckBox chkNoms;
    @FXML
    HBox hbMenu;    
    @FXML
    HBox hbInput;
    @FXML
    HBox hbCancel;  
    @FXML
    private Button btCancelPos;    
    @FXML
    HBox hbAction;
    @FXML
    SplitPane mapPane;
    @FXML
    private TableView<pointRecord> tablePoints;
    @FXML
    private TableColumn<pointRecord, String> colBalise; 
    @FXML
    private TableColumn<pointRecord, String> colAlt;
    @FXML
    private TableColumn<pointRecord, String> colDesc;     
    @FXML
    private WebView viewMap;         
    private WebEngine eng;    
    // bridge between java code and javascript map
    private Bridge pont;
    
    private Stage waypStage;    
    private RootLayoutController rootController;  
    
    // Reference to the main application.
    private Main mainApp;
    
    // Localization
    private I18n i18n; 
    
    // Settings
    configProg myConfig;
    StringBuilder sbError;
    String RC = "\n";    
    
    private ObservableList<pointRecord> pointList; 
    private pointRecord currPoint;
    private wpreadfile waypFile;
    private File originalFile = null;
    private String originalType;

    private Stage dialogStage;    
    private String debStatusBar;
    private position defaultPos = new position();
    
    private String errorComMsg;
    private String strWpBrut;
    private int resCom;   // 0 initial state  1 : successfull communication   2 : unsuccess communication
    // current GPS
    private winGPS.gpsType currGPS;    
    private String currNamePort; 
    private ArrayList<pointRecord> gpsReadList;
    private StringBuilder gpsInfo;
    private String gpsCharac;
    private ArrayList<String> listForGps = new ArrayList<>();
    private int gpsTypeName;   // Name type : long, short or mixed
    private ContextMenu tableContextMenu;
    private String gpsdWaypWriteReport;
    
    @FXML
    public void initialize() {
        
        pointList = FXCollections.observableArrayList();
        
        colBalise.setCellValueFactory(new PropertyValueFactory<pointRecord, String>("fBalise"));
        colAlt.setCellValueFactory(new PropertyValueFactory<pointRecord, String>("fAlt"));
        colDesc.setCellValueFactory(new PropertyValueFactory<pointRecord, String>("fDesc"));
        
        tablePoints.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                centerMap();
            }
        });        
        
        tablePoints.setItems(pointList); 
        
        // We can't put buildContextMenu() here
        // because we need to intialize i18n before 
        // buildContextMenu() is in setMainApp()
        
        // Cette procedure provient de https://kubos.cz/2016/04/01/javafx-dynamic-context-menu-on-treeview.html
        tablePoints.addEventHandler(MouseEvent.MOUSE_RELEASED, e->{ 
            if (e.getButton()==MouseButton.SECONDARY) { 
                pointRecord selectedPoint = tablePoints.getSelectionModel().getSelectedItem();
                
                //item is selected - this prevents fail when clicking on empty space 
                if (selectedPoint!=null) { 
                    //open context menu on current screen position  
                    tableContextMenu.show(tablePoints, e.getScreenX(), e.getScreenY());
                } 
            } else { 
                //any other click cause hiding menu 
                tableContextMenu.hide(); 
            } 
        });      
        // --------------------------------------------------------------------------------------        
    }    
    
    
    private void buildContextMenu() {
        
        tableContextMenu = new ContextMenu();
        
        MenuItem cmPosition = new MenuItem(i18n.tr("Position"));
        cmPosition.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent e) {
                changePosition();
            }
        });
        tableContextMenu.getItems().add(cmPosition);        
        
        MenuItem cmEdit = new MenuItem(i18n.tr("Edit"));        
        cmEdit.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent e) {
               editPoint();
            }            
        });
        tableContextMenu.getItems().add(cmEdit);
        
        MenuItem cmDelete = new MenuItem(i18n.tr("Delete"));
        cmDelete.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent e) {
                deletePoint();
            }
        });
        tableContextMenu.getItems().add(cmDelete);     
        
        MenuItem cmCoord = new MenuItem(i18n.tr("Coordinates"));
        cmCoord.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent e) {
                displayCoord();
            }
        });
        tableContextMenu.getItems().add(cmCoord);          
    }
    
    @FXML
    private void handleNewWayp() {
        disableInput();
//        newPoint();
        int numPoint = pointList.size()+1;
        String prefix = txPrefix.getText();
        String subPrefix;
        if (prefix != null && !prefix.equals("")) {
            if (prefix.length() > 2)
                subPrefix = prefix.substring(0, 2);
            else
                subPrefix = "WPT";
        } else {
            prefix = "WAYPOINT";
            subPrefix = "WPT";
        }
        String defBalise;
        if (chkNoms.isSelected())
            defBalise = "";
        else
            defBalise = subPrefix+String.format("%03d", numPoint);        
        String defDesc = txPrefix.getText()+" "+String.format("%03d", numPoint);
        openCoordForm(defDesc, defBalise);
    }   
    
    @FXML
    private void handleReadGPS() {    
        if (selectGPS()) {
            originalFile = null;
            readFromGPS();    
        }
    }
    
    @FXML
    private void handleWriteGPS() {
        if (pointList.size() > 0) {
            if (selectGPS()) {
                List<CommandLinksDialog.CommandLinksButtonType> links = new ArrayList<>();
                CommandLinksDialog dg;
                Optional<ButtonType> result;
                links.add(new CommandLinksDialog.CommandLinksButtonType(i18n.tr("1. Long names"),i18n.tr("Only long names are written e.g. MONTMIN FORCLAZ"),true)); 
                links.add(new CommandLinksDialog.CommandLinksButtonType(i18n.tr("2. Short names"),i18n.tr("Only short names are written e.g. D01127"),false));       
                links.add(new CommandLinksDialog.CommandLinksButtonType(i18n.tr("3. Mixed"),i18n.tr("Short name and the beginning of long name e.g. D01127 MONTMIN F"),false));   
                dg = new CommandLinksDialog(links);
                dg.setTitle(i18n.tr("Type of names"));
                result = dg.showAndWait();   
                String resDg = result.get().getText();
                if (resDg != null && resDg != "") {
                    if (resDg.contains("1")) {
                       gpsTypeName = 0;
                    } else if (resDg.contains("2")) {
                       gpsTypeName = 1;
                    } else if (resDg.contains("3")) {
                       gpsTypeName = 2;
                    }
                    writeToGPS();
                }    
            }
        }    
    }
    
    @FXML
    private void handleReadFile() {
        winFileChoose wf = new winFileChoose(myConfig, i18n, fileType.wpt, null);  
        File selectedFile = wf.getSelectedFile();     
        if(selectedFile != null && selectedFile.exists()){ 
            readFromFile(selectedFile.getAbsolutePath());
        }       
    }
    
    private void displayCoord() {
        currPoint = tablePoints.getSelectionModel().getSelectedItem();
        StringBuilder sb = new StringBuilder();
        sb.append(i18n.tr("Short name")).append(" : ").append(currPoint.getFBalise()).append("\r\n"); 
        sb.append(i18n.tr("Long name")).append(" : ").append(currPoint.getFDesc()).append("\r\n");    
        sb.append(i18n.tr("Latitude")).append(" : ").append(currPoint.getFLat()).append("\r\n"); 
        sb.append(i18n.tr("Longitude")).append(" : ").append(currPoint.getFLong()).append("\r\n"); 
        winTrackFile noTrack = new winTrackFile(sb.toString());            
    }
    
    private void openCoordForm(String pDesc, String pBalise) {
        try {                     
            // Load the fxml file and create a new stage for the popup dialog.
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(Main.class.getResource("/coord.fxml")); 
            AnchorPane page = (AnchorPane) loader.load();

            Stage dialogStage = new Stage();
            Scene scene = new Scene(page);
            dialogStage.setScene(scene);
            
            // Communication bridge between coord and WaypView controllers
            CoordController controller = loader.getController(); 
            controller.setCoordBridge(this);
            controller.setDialogStage(dialogStage, myConfig, pDesc, pBalise); 
        } catch (IOException e) {
            sbError = new StringBuilder(this.getClass().getName()+"."+Thread.currentThread().getStackTrace()[1].getMethodName());
            sbError.append("\r\n").append(e.toString());
            mylogging.log(Level.SEVERE, sbError.toString()); 
            
        }         
    }
    
    public void returnCoordForm(int exitCode, Balisemodel ba) {
        
        switch (exitCode) {
            case 1 :
                // Ajout dans la liste
                currPoint = new pointRecord(ba.getNomCourt(), "",ba.getNomLong());            
                currPoint.setFIndex(-1);
                // map updating
                StringBuilder sb = new StringBuilder();
                sb.append("createNew(\"");
                sb.append(ba.getNomLong());
                sb.append("<BR>").append("Altitude").append(" : ");    
                sb.append("").append(" m");
                sb.append("\")");
                if (eng != null) {              
                    eng.executeScript(sb.toString());
                }
                // Point is added to observable list in decodeCoord()
                // Index of javascript markers array will be stored                   
                break;
            case 2 :
                // Ajout dans la liste
                currPoint = new pointRecord(ba.getNomCourt(), "",ba.getNomLong());     
                // First index = 0
                currPoint.setFIndex(pointList.size());
                String sLat = ba.getLatitude();
                currPoint.setFLat(sLat);
                String sLong = ba.getLongitude();
                currPoint.setFLong(sLong);
                // altitude request
                // Google API -> best results with a low precision in coordinates 
                if (sLat.length() > 6) sLat = sLat.substring(0, 6);
                if (sLong.length() > 6) sLong = sLong.substring(0, 6);
                // elevation request       
                int iAlt = elevationapi.raceElevation(sLat, sLong);
                if (iAlt > -1) {
                    currPoint.setFAlt(String.valueOf(iAlt));
                } else {
                    currPoint.setFAlt("");
                }     
                pointList.add(currPoint);
                displayInfo(debStatusBar+String.valueOf(pointList.size())+" waypoints");   
                showMapPoints();
                break;
        }
        enableInput();
    }    
    
    /**
     * Most of waypoints files are encoded with ISO-8859-1 format
     * @param fichier
     * @return 
     */
    private String readTxt8859(File fichier){
        String res = null;
        
        try {
            res = new String(Files.readAllBytes(Paths.get(fichier.getAbsolutePath())),Charset.forName("ISO-8859-1"));     
        } catch (IOException e) {
            sbError = new StringBuilder(this.getClass().getName()+"."+Thread.currentThread().getStackTrace()[1].getMethodName());
            sbError.append("\r\n").append(e.toString());
            mylogging.log(Level.SEVERE, sbError.toString());
        }    
                                
        return res;
    }    
    
    private void readFromFile(String fPath) {
        boolean goodRead = false;
        File file = new File(fPath);       
        String ficType = null;                                    
        String pFichier = readTxt8859(file);   
        ficType = "nil";
        if (pFichier != null)  {            
            if (pFichier.indexOf("OziExplorer") > -1) {                
                ficType = "OZI";
                originalType = "1";
            } else if (pFichier.indexOf("PCX5") > -1) {
                ficType = "PCX";
                originalType = "3";
            } else if (pFichier.indexOf("<kml xmlns") > -1) {
                ficType = "KML";
                originalType = "4";
            } else if (pFichier.indexOf("<?xml version=\"1.0\"") > -1 && pFichier.indexOf("version=\"1.1\"") > -1) {
                ficType = "GPX";
                originalType = "5";
            } else if (pFichier.indexOf("code,country") > -1) {    // Vérifier si cela fonctionne sans les majuscules
                ficType = "CUP";
                originalType = "6";
            } else if (pFichier.indexOf("Code,Country") > -1) {    // Vérifier si cela fonctionne sans les majuscules
                ficType = "CUP"; 
                originalType = "6";
            } else if (pFichier.indexOf("Timestamp=") > -1) {
                ficType = "XCP";
            } else if (testCompeGPS(pFichier)) {
                ficType = "COM"; 
                originalType = "2";
            } else {
                alertbox aError = new alertbox(myConfig.getLocale());
                aError.alertInfo(i18n.tr("File format not recognized")); 
            }
        }
        waypFile = new wpreadfile();
        StringBuilder sbInfo = new StringBuilder();
        sbInfo.append(file.getAbsolutePath()).append("   ");
        switch (ficType) {
            case "OZI":
                goodRead = waypFile.litOzi(pFichier);
                sbInfo.append(i18n.tr("Format")).append(" OZI   ");                    
                break;                
            case "GPX":
                goodRead = waypFile.litGpx(file.getAbsolutePath());
                sbInfo.append(i18n.tr("Format")).append(" GPX   "); 
                break;        
            case "KML":
                goodRead = waypFile.litKml(pFichier);
                sbInfo.append(i18n.tr("Format")).append(" KML   "); 
                break;                            
            case "PCX":
                goodRead = waypFile.litPcx(pFichier);
                sbInfo.append(i18n.tr("Format")).append(" PCX   "); 
                break;      
            case "CUP":
                goodRead = waypFile.litCup(pFichier);
                sbInfo.append(i18n.tr("Format")).append(" CUP   "); 
                break;          
            case "COM":
                goodRead = waypFile.litComp(pFichier);
                sbInfo.append(i18n.tr("Format")).append(" CompeGPS   "); 
                break;     
            case "XCP":
                goodRead = waypFile.litXcp(pFichier);
                sbInfo.append(i18n.tr("Format")).append(" XC Planner   "); 
                break;                   
        }            
        if (goodRead) {
            displayWpFile(waypFile.getWpreadList(), sbInfo.toString());
            originalFile = file;
        } else {
            alertbox aError = new alertbox(myConfig.getLocale());
            aError.alertInfo(i18n.tr("Unable to decode the file"));                  
        }        
    }
    
    @FXML
    private void handleWriteFile() {
        String pInitDir = null;
        String pInitName = null;
        if (originalFile != null && originalFile.exists() && !originalFile.getName().contains("tempwp.wpt")) {
            pInitDir = originalFile.getParent();
            pInitName = originalFile.getName();
            dialogbox dConfirm = new dialogbox(i18n);
            StringBuilder sbMsg = new StringBuilder(); 
            if (dConfirm.YesNo(i18n.tr("Saving"),i18n.tr("Overwrite the existing file")))   { 
                writeToFile(originalType, originalFile.getAbsolutePath());
            } else {
                winFileSave wfs = new winFileSave(myConfig, i18n, fileType.wpt, pInitDir, pInitName);  
               File saveWpt = wfs.getSelectedFile();
               if (saveWpt != null) {        
                   writeToFile(wfs.getWptFormat(), saveWpt.getAbsolutePath());
               }               
            }
        } else { 
            winFileSave wfs = new winFileSave(myConfig, i18n, fileType.wpt, pInitDir, pInitName);  
            File saveWpt = wfs.getSelectedFile();
            if (saveWpt != null) {        
                writeToFile(wfs.getWptFormat(), saveWpt.getAbsolutePath());
            }
        }
    }    
    
    private void writeToFile(String sType, String fPath) {
        File file = new File(fPath);        
        boolean resWrite = false;
        StringBuilder sbInfo = new StringBuilder();
        sbInfo.append(fPath).append("   ");            
        switch (sType) {
            case "1":
                if (pointList.size() > 0) {
                    wpwritefile wfile = new wpwritefile();
                    sbInfo.append(i18n.tr("Format")).append(" OZI   ");    
                    resWrite = wfile.writeOzi(pointList, file, true);                        
                }
                break;
            case "2":
                if (pointList.size() > 0) {
                    wpwritefile wfile = new wpwritefile();
                    sbInfo.append(i18n.tr("Format")).append(" CompeGPS   ");
                    resWrite = wfile.writeComp(pointList, file);
                }                    
                break;
            case "3":
                if (pointList.size() > 0) {
                    wpwritefile wfile = new wpwritefile();
                    sbInfo.append(i18n.tr("Format")).append(" PCX   "); 
                    resWrite = wfile.writePCX(pointList, file);
                }                    
                break;                  
            case "4":
                if (pointList.size() > 0) {
                    wpwritefile wfile = new wpwritefile();
                    sbInfo.append(i18n.tr("Format")).append(" KML   ");                         
                    resWrite = wfile.writeKml(pointList, file);                        
                }                    
                break;  
            case "5":
                if (pointList.size() > 0) {
                    wpwritefile wfile = new wpwritefile();
                    resWrite = wfile.writeGpx(pointList, file);
                    sbInfo.append(i18n.tr("Format")).append(" GPX   "); 
                }                                        
                break;          
            case "6":
                if (pointList.size() > 0) {
                    wpwritefile wfile = new wpwritefile();
                    sbInfo.append(i18n.tr("Format")).append(" CUP   "); 
                    resWrite = wfile.writeCup(pointList, file);
                }                    
                break;                    
        }       
        if (resWrite) {
            displayInfo(sbInfo.toString()+String.valueOf(pointList.size())+" "+i18n.tr("waypoints"));                   
        }                
    }
    
    @FXML
    private void handleCancelPos() {
        showMapPoints();
        enableInput();
        disableCancelPos();
    }
    
    /**
    * Si d'aventure on voulait représenter le rectangle sur la carte : http://jsfiddle.net/y1nb7sow/2/
    */
    @FXML
    private void handleBbox() {
        DecimalFormatSymbols decimalFormatSymbols = new DecimalFormatSymbols();   
        decimalFormatSymbols.setDecimalSeparator('.');       
        DecimalFormat df6 = new DecimalFormat("####.000000", decimalFormatSymbols); 
        DecimalFormat df3 = new DecimalFormat("####.000", decimalFormatSymbols);
        
        if (pointList.size() > 1) {
            double latNord = Double.parseDouble(pointList.get(0).getFLat());
            String nameNord = pointList.get(0).getFDesc();
            String shortNord = pointList.get(0).getFBalise();
            double latSud = Double.parseDouble(pointList.get(0).getFLat());
            String nameSud = pointList.get(0).getFDesc();
            String shortSud = pointList.get(0).getFBalise();
            double longEst = Double.parseDouble(pointList.get(0).getFLong());
            String nameEst = pointList.get(0).getFDesc();
            String shortEst = pointList.get(0).getFBalise();
            double longWest = Double.parseDouble(pointList.get(0).getFLong());
            String nameWest = pointList.get(0).getFDesc();
            String shortWest = pointList.get(0).getFBalise();

            for (int i = 1; i < pointList.size(); i++) {
                double dLat = Double.parseDouble(pointList.get(i).getFLat());
                double dLong = Double.parseDouble(pointList.get(i).getFLong());
                if (dLat > latNord) {
                    latNord = dLat;
                    nameNord = pointList.get(i).getFDesc();
                    shortNord = pointList.get(i).getFBalise();
                }
                if (dLat < latSud) {
                    latSud = dLat;
                    nameSud = pointList.get(i).getFDesc();
                    shortSud = pointList.get(i).getFBalise();
                }
                if (dLong > longEst) {
                    longEst = dLong;
                    nameEst = pointList.get(i).getFDesc();
                    shortEst = pointList.get(i).getFBalise();
                }
                if (dLong < longWest) {
                    longWest = dLong;
                    nameWest = pointList.get(i).getFDesc();
                    shortWest = pointList.get(i).getFBalise();
                }
            }
            StringBuilder sb = new StringBuilder();
            sb.append(i18n.tr("Left")).append(" : ").append(shortWest).append(" - ").append(nameWest).append(" -> ").append(df6.format(longWest)).append("\r\n");                    
            sb.append(i18n.tr("Bottom")).append(" : ").append(shortSud).append(" - ").append(nameSud).append(" -> ").append(df6.format(latSud)).append("\r\n");
            sb.append(i18n.tr("Right")).append(" : ").append(shortEst).append(" - ").append(nameEst).append(" -> ").append(df6.format(longEst)).append("\r\n");
            sb.append(i18n.tr("Top")).append(" : ").append(shortNord).append(" - ").append(nameNord).append(" -> ").append(df6.format(latNord)).append("\r\n\r\n");
            
            // On prend une marge de 11 km soit 0.1 degrés
            longEst+= 0.1;
            longWest -= 0.1;
            latNord += 0.1;
            latSud -= 0.1;
            sb.append(i18n.tr("Bounding box with a margin of")).append(" ").append("11 km :").append("\r\n");
            sb.append("          ").append(df3.format(latNord)).append("\r\n");
            sb.append(df3.format(longWest)).append("           ").append(df3.format(longEst)).append(" ").append("\r\n");
            sb.append("          ").append(df3.format(latSud)).append("\r\n\r\n");
            sb.append(i18n.tr("Copy to clipboard"));
            
                                      
            final Clipboard clipboard = Clipboard.getSystemClipboard();
            final ClipboardContent content = new ClipboardContent();
            content.putString(sb.toString());            
            clipboard.setContent(content);
            
            winTrackFile myTrace = new winTrackFile(sb.toString()); 
        }
    }
        
    @FXML
    private void handleMail() {   
        if (pointList.size() > 0) {
            wpwritefile wzip = new wpwritefile();                       
            if (wzip.zipAllFormats(pointList)) {
                 winMail showMail = new winMail(myConfig,wzip.getZipPath(), false);     
            }
        }                           
    }
    
    @FXML
    private void handleGE() {
        
        wpwritefile wfile = new wpwritefile();       
        File ficKml = systemio.tempacess.getAppFile("Logfly", "logflywp.kml");
        boolean resWrite = wfile.writeKml(pointList, ficKml);    
        if (resWrite) {
            try {                        
                Desktop dt = Desktop.getDesktop();     
                dt.open(ficKml);            
            } catch (Exception e) {
                alertbox aError = new alertbox(myConfig.getLocale());
                aError.alertNumError(1030); 
            }               
        }
    }
    
    /**
     * displayName is a flag for type name textfield display
     * @param displayName
     * @return 
     */
    private boolean selectGPS() {
        boolean res = false;   
        winGPS myWin = new winGPS(myConfig, i18n, true);    
        if (myWin.getCurrGPS() != null && myWin.getCurrNamePort() != null && myWin.isGpsConnect()) {
            currGPS = myWin.getCurrGPS();
            currNamePort = myWin.getCurrNamePort();
            gpsTypeName = myWin.getCurrTypeName();
            gpsCharac = myWin.getGpsCharac();
            res = true;
        }
        
        return res;
    }
    
    private void displayWpFile(ArrayList<pointRecord> wpreadList, String infoFile) {
        
        ArrayList<pointRecord> wpTot = new ArrayList<>();
        int nbPoints = 0;
        if (pointList.size() > 0) {
            dialogbox dConfirm = new dialogbox(i18n);
            if (dConfirm.YesNo(i18n.tr("Turnpoints display"), i18n.tr("Merge display and new file"))) {
                for (int i = 0; i < pointList.size(); i++) {
                    pointRecord pl = pointList.get(i);
                    wpTot.add(pl);    
                    nbPoints++;
                }
                for (int j = 0; j < wpreadList.size(); j++) {
                    nbPoints++;
                    pointRecord pr = wpreadList.get(j);
                    pr.setFIndex(nbPoints - 1);
                    wpTot.add(pr);    
                }                                
            } else {
                wpTot = wpreadList;
            }
        } else {
            wpTot = wpreadList;
        }
        int sizeList = wpTot.size();
        tablePoints.getItems().clear();
        pointList = FXCollections.observableArrayList(wpTot);
        tablePoints.setItems(pointList);
        showMapPoints();
        hbMenu.setVisible(true);
        mapPane.setVisible(true);
        debStatusBar = infoFile;
        //this.mainApp.rootLayoutController.updateMsgBar(infoFile+String.valueOf(sizeList)+" waypoints");  
        displayInfo(infoFile+String.valueOf(sizeList)+" waypoints");                     
    }    
    
    private boolean testCompeGPS(String pFichier) {
        
        boolean res = false;
        String tbFile[];

        tbFile = pFichier.split(Character.toString((char)10));
        int lgTb = tbFile.length; 
        if (lgTb < 1) {
            // We read some files where there is only one character 13
            tbFile = pFichier.split(Character.toString((char)13));
            lgTb = tbFile.length; 
        }  
        if (lgTb > 2) {
            // first line is like "G  WGS 84"     [ Headboard : Line G: It identifies the datum of the map ]
            if (tbFile[0].substring(0,1).indexOf("G") > -1) {
                // second line is like "U  1"   [ Headboard : Line U: It identifies the system of coordinate ]
                if (tbFile[1].substring(0,4).indexOf("U  1") > -1) {
                    res = true;
                }                    
            }
        }
        
        return res;        
    }
    
    private void decodeCoord(String sCoord) {
        String[] tbCoord = sCoord.split(",");
        if (tbCoord.length == 4) {            
            int idx = Integer.parseInt(tbCoord[0]);
            String sLat = tbCoord[1];
            String sLong = tbCoord[2];
            String sAlt = tbCoord[3].trim();
            currPoint.setFLat(sLat);
            currPoint.setFLong(sLong);
            currPoint.setFAlt(sAlt);      
            if (chkNoms.isSelected()) {
                String prefix;
                if (currPoint.getFDesc().length() > 2) {
                    prefix = currPoint.getFDesc().substring(0, 3);                    
                    prefix = prefix.toUpperCase();
                } else
                    prefix = "WPT";
                int iAlt = Integer.parseInt(sAlt)/10;
                prefix = prefix+String.format("%03d", iAlt);
                currPoint.setFBalise(prefix);
            }
            if (currPoint.getFIndex() == -1) {
                // Index of javascript array stored
                currPoint.setFIndex(idx);       
                pointList.add(currPoint);
                // status bar updating                
                displayInfo(debStatusBar+String.valueOf(pointList.size())+" waypoints");                  
            }            
            updateDescription();  
        }              
    }
    
    private void displayGpsWaypList() {
        if (gpsReadList.size() > 0) {
            displayWpFile(gpsReadList, gpsInfo.toString());
        } else {
            alertbox aError = new alertbox(myConfig.getLocale());
            aError.alertInfo(i18n.tr("No waypoints in this GPS"));  
        }
    }
    
    private void writeGpsdProgress() {  
            
        gpsdWaypWriteReport = null;
        dialogbox dConfirm = new dialogbox(i18n);
        StringBuilder sbMsg = new StringBuilder(); 
        sbMsg.append(i18n.tr("GPS ready [Old Waypoints possibly erased]")).append(" ?");
        StringBuilder sbTitle = new StringBuilder(); 
        sbTitle.append(i18n.tr("Sending to GPS")).append(" [");
        switch (gpsTypeName) {
            case 0:
                sbTitle.append(i18n.tr("Long names"));
                break;
            case 1 :
                sbTitle.append(i18n.tr("Short names"));
                break;
            case 2 :
                sbTitle.append(i18n.tr("Mixed"));                
                break;            
        }
        sbTitle.append("]");
        
        if (dConfirm.YesNo(sbTitle.toString(), sbMsg.toString()))   {       
            gpsInfo = new StringBuilder();
            gpsInfo.append(gpsCharac);
            File wptFile = systemio.tempacess.getAppFile("Logfly", "tempwp.wpt"); 
            if (wptFile.exists()) wptFile.delete();
            wpwritefile wfile = new wpwritefile();
            boolean resw = false;
            if (gpsTypeName == 1)
                // short names
                resw = wfile.writeOzi(pointList, wptFile, false);
            else
                resw = wfile.writeOzi(pointList, wptFile, true);
            if (resw) {
                Task<Object> worker = new Task<Object>() {
                    @Override
                    protected Object call() throws Exception {
                    gpsdump gpsd = new gpsdump(currNamePort,myConfig, i18n);                  
                    switch (currGPS) {
                        case FlymSD :
                            gpsd.setOziWpt(1, wptFile.getAbsolutePath(),gpsTypeName);
                            gpsdWaypWriteReport = gpsd.getWaypWriteReport();
                            break;
                        case FlymOld :
                            gpsd.setOziWpt(2, wptFile.getAbsolutePath(),gpsTypeName);
                            gpsdWaypWriteReport = gpsd.getWaypWriteReport();
                            break;
                        case Flytec20 :
                            gpsd.setOziWpt(3, wptFile.getAbsolutePath(),gpsTypeName);
                            gpsdWaypWriteReport = gpsd.getWaypWriteReport();
                            break;   
                        case Flytec15 :
                            gpsd.setOziWpt(8, wptFile.getAbsolutePath(),gpsTypeName);
                            gpsdWaypWriteReport = gpsd.getWaypWriteReport();
                            break;
                    }       
                        return null ;                
                    }
                };

                worker.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
                    @Override
                    public void handle(WorkerStateEvent t) {                        
                        writeEnd();
                    }
                });  

                ProgressDialog dlg = new ProgressDialog(worker);
                dlg.setHeaderText(i18n.tr("Send to GPS"));
                dlg.setTitle("");
                Thread th = new Thread(worker);
                th.setDaemon(true);
                th.start();  
            } else {
                alertbox aError = new alertbox(myConfig.getLocale());
                aError.alertNumError(9);   // Unable to create temporary file
            }            
        }
    }   
            
    private void writeFlytec15() {
       
        try {
            prepWritingFly15();
            jsFlytec15 fly15 = new jsFlytec15(currNamePort);
            if (listForGps.size() > 0 ) {
                gpsInfo = new StringBuilder();
                gpsInfo.append(i18n.tr("Sending to")).append("  ").append("Flytec 6015/ IQ Basic ");
                fly15.setListPBRWP(listForGps);
                fly15.sendWaypoint();
            }    
        } catch (Exception e) {
            sbError = new StringBuilder(this.getClass().getName()+"."+Thread.currentThread().getStackTrace()[1].getMethodName());
            sbError.append("\r\n").append(e.toString());
            mylogging.log(Level.SEVERE, sbError.toString());            
        }          
    }   

    /**
     * Used to send wapoints with our own code
     * Currently used for a single device : 6015 (GPSDump Linux  don't support this GPS)
     */
    private void writeToGpsProgress() {
        dialogbox dConfirm = new dialogbox(i18n);
        StringBuilder sbMsg = new StringBuilder(); 
        sbMsg.append(i18n.tr("Ready")).append(" ?  ").append("(").append(i18n.tr("Gps memory cleared")).append(")");
        StringBuilder sbTitle = new StringBuilder(); 
        sbTitle.append(i18n.tr("Sending to GPS")).append(" [");
        switch (gpsTypeName) {
            case 0:
                sbTitle.append(i18n.tr("Long names"));
                break;
            case 1 :
                sbTitle.append(i18n.tr("Short names"));
                break;
            case 2 :
                sbTitle.append(i18n.tr("Mixed"));                
                break;            
        }
        sbTitle.append("]");
        
        if (dConfirm.YesNo(sbTitle.toString(), sbMsg.toString()))   {         

            errorComMsg = null;

            Task<Object> worker = new Task<Object>() {
                @Override
                protected Object call() throws Exception {
                    switch (currGPS) {
                        case Flytec15 :
                            writeFlytec15();                            
                            break;                 
                    }       
                    return null ;                
                }

            };

            worker.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
                @Override
                public void handle(WorkerStateEvent t) {
                    writeEnd();
                }
            });
            
            ProgressDialog dlg = new ProgressDialog(worker);
            dlg.setHeaderText(i18n.tr("Send to GPS"));
            dlg.setTitle("");
            Thread th = new Thread(worker);
            th.setDaemon(true);
            th.start();
        }                
    }
    
    private void writeToGpsSimple() {
        String fileName;
        String sPath;
        switch (currGPS) {                    
            case Rever :
                winUsbWWayp revWin = new winUsbWWayp(i18n, currGPS,"Reversale");
                fileName = revWin.getFilename();
                String fileExt = revWin.getFileExt();
                if (fileName != null) {
                    reversale usbRev = new reversale(myConfig.getOS(), myConfig.getGpsLimit());
                    if (usbRev.isConnected()) {
                        if (fileExt.equals(".wpt")) {
                            if(usbRev.isWpExist()) {
                                File revWpt = usbRev.getfWayp();
                                sPath = revWpt.getAbsolutePath()+File.separator+fileName;
                                writeToFile("1", sPath);     // OziExplorer
                            }                    
                        }
                        if (fileExt.equals(".kml")) {
                            if(usbRev.isGoogExist()) {
                                File revGoog = usbRev.getfWayp();
                                sPath = revGoog.getAbsolutePath()+File.separator+fileName;
                                writeToFile("4", sPath);     // format kml
                            }                    
                        }                        
                    }   
                }
                break;
            case Sky :
                winUsbWWayp skyWin = new winUsbWWayp(i18n, currGPS,"Skytraax 2");
                fileName = skyWin.getFilename();
                if (fileName != null) {
                    skytraax usbSky = new skytraax(myConfig.getOS(), myConfig.getGpsLimit());
                    if (usbSky.isConnected()) {
                        if(usbSky.isWpExist()) {
                            File skyWayp = usbSky.getfWayp();
                            sPath = skyWayp.getAbsolutePath()+File.separator+fileName;
                            writeToFile("2", sPath);     // CompeGPS
                        }                    
                    }                
                }  
                break;
            case Sky3 :
                winUsbWWayp sky3Win = new winUsbWWayp(i18n, currGPS,"Skytraax 3");
                fileName = sky3Win.getFilename();
                if (fileName != null) {
                    skytraxx3 usbSky3 = new skytraxx3(myConfig.getOS(), myConfig.getGpsLimit());
                    if (usbSky3.isConnected()) {
                        if(usbSky3.isWpExist()) {
                            File skyWayp = usbSky3.getfWayp();
                            sPath = skyWayp.getAbsolutePath()+File.separator+fileName;
                            writeToFile("2", sPath);     // CompeGPS
                        }                    
                    }                
                }               
                break;                           
            case Oudie :
                winUsbWWayp oudieWin = new winUsbWWayp(i18n, currGPS,"Oudie");
                fileName = oudieWin.getFilename();
                if (fileName != null) {
                    oudie usbOudie = new oudie(myConfig.getOS(), myConfig.getGpsLimit());
                    if (usbOudie.isConnected()) {
                        if(usbOudie.isWpExist()) {
                            File skyWayp = usbOudie.getfWayp();
                            sPath = skyWayp.getAbsolutePath()+File.separator+fileName;
                            writeToFile("6", sPath);     // Cup format
                        }                    
                    }                
                }                      
                break;  
            case Syride :
                break;                            
            case Connect :
                winUsbWWayp connWin = new winUsbWWayp(i18n, currGPS,"Connect");
                fileName = connWin.getFilename();
                if (fileName != null) {
                    connect usbConn = new connect(myConfig.getOS(), myConfig.getGpsLimit());
                    if (usbConn.isConnected()) {
                        if(usbConn.isWpExist()) {
                            File skyWayp = usbConn.getfWayp();
                            sPath = skyWayp.getAbsolutePath()+File.separator+fileName;
                            // No specifications in Flytec site, it would accept different formats
                            // I have a backup with CompeGPS format
                            writeToFile("2", sPath);     // CompeGPS
                        }                    
                    }                
                }                                        
                break;   
            case Element :
                winUsbWWayp elemWin = new winUsbWWayp(i18n, currGPS,"Element");
                fileName = elemWin.getFilename();
                if (fileName != null) {
                    element usbElem = new element(myConfig.getOS(), myConfig.getGpsLimit());
                    if (usbElem.isConnected()) {
                        if(usbElem.isWpExist()) {
                            File skyWayp = usbElem.getfWayp();
                            sPath = skyWayp.getAbsolutePath()+File.separator+fileName;
                            // Online doc : supports these formats and file extensions:
                            // CompeGPS (*.wpt or *.com.wpt)
                            //FS waypoints (*.wpt or *.geo.wpt)
                            // OziExplorer (*.wpt or *.ozi.wpt)
                            // SeeYou (*.cup)
                            // WinPilot (*.dat)
                            writeToFile("1", sPath);     // OziExplorer
                        }                    
                    }                
                }                         
                break;                         
            case CPilot :
                winUsbWWayp cpilWin = new winUsbWWayp(i18n, currGPS,"C Pilot");
                fileName = cpilWin.getFilename();
                if (fileName != null) {
                    compass usbCpilot = new compass(myConfig.getOS(), myConfig.getGpsLimit());
                    if (usbCpilot.isConnected()) {
                        if(usbCpilot.isWpExist()) {
                            File skyWayp = usbCpilot.getfWayp();
                            sPath = skyWayp.getAbsolutePath()+File.separator+fileName;
                            writeToFile("2", sPath);     // Compe GPS format
                        }                    
                    }                
                }                   
                break;                       
        }                  
    } 
    
    private void writeToGPS() {   

        alertbox aError = new alertbox(myConfig.getLocale());             
        switch (currGPS) {
            case Flytec20 :
                gpsInfo = new StringBuilder();
                gpsInfo.append(i18n.tr("Sending to")).append("  ").append("Flytec 6020/30 ").append("  ");
                displayInfo(gpsInfo.toString());
                switch (myConfig.getOS()) {
                    case WINDOWS :
                    case LINUX :
                        writeGpsdProgress();  
                        break;
                    case MACOS : 
                        // Up to GPSDump 0.53, waypoint reading was not supported
                        // writeToGpsProgress();
                        // now supported
                        writeGpsdProgress();
                        break;
                } 
                break;  
            case Flytec15 :
                switch (myConfig.getOS()) {
                    case WINDOWS :
                        gpsInfo = new StringBuilder();
                        gpsInfo.append(i18n.tr("Sending to")).append("  ").append("Flytec 6015/ IQ Basic ");
                        displayInfo(gpsInfo.toString());
                        writeGpsdProgress();  
                        break;
                case MACOS :        
                case LINUX : 
                    // writeToGpsProgress();
                    break;
                } 
                break;
            case FlymSD :
                gpsInfo = new StringBuilder();
                gpsInfo.append(i18n.tr("Sending to")).append("  ").append("Flymaster ").append("  ");
                displayInfo(gpsInfo.toString());
                switch (myConfig.getOS()) {
                    case WINDOWS :
                    case LINUX :
                        writeGpsdProgress();  
                        break;
                    case MACOS : 
                        // Up to GPSDump 0.53, waypoint reading was not supported
                        // writeToGpsProgress();
                        // now supported
                        writeGpsdProgress();  
                        break;
                } 
                break;                
            case FlymOld :
                gpsInfo = new StringBuilder();
                gpsInfo.append(i18n.tr("Sending to")).append("  ").append("Flymaster ").append("  ");
                displayInfo(gpsInfo.toString());
                switch (myConfig.getOS()) {
                    case WINDOWS :
                    case LINUX :
                        writeGpsdProgress();  
                        break;
                    case MACOS : 
                        // Up to GPSDump 0.53, waypoint reading was not supported
                        // writeToGpsProgress();
                        // now supported
                        writeGpsdProgress();
                        break;
                } 
                break;    
            case Rever :
                writeToGpsSimple();
                break;
            case Sky :
                writeToGpsSimple();
                break;
            case Sky3 :
                writeToGpsSimple();
                break;       
            case Flynet :
                aError.alertInfo(i18n.tr("No waypoints in this GPS"));  
                break;  
            case Sensbox :
                aError.alertInfo(i18n.tr("No waypoints in this GPS"));
                break;                         
            case Oudie :
                writeToGpsSimple();
                break;  
            case Syride :
                StringBuilder sbAlert = new StringBuilder();
                // For i18n, it's forbidden to put /r/n in the string
                sbAlert.append(i18n.tr("Save to disk"));
                sbAlert.append("\r\n");
                sbAlert.append(i18n.tr("Use GPSDump to download waypoints"));
                aError.alertInfo(sbAlert.toString()); 
                break;                            
            case Connect :
                writeToGpsSimple();
                break;   
            case Element :
                writeToGpsSimple();
                break;                         
            case CPilot :
                writeToGpsSimple();
                break;  
            case XCTracer :
                aError.alertInfo(i18n.tr("No waypoints in this GPS"));                  
                break;                        
        }               
    }            
    
    private void writeEnd() {
        
        int lg = pointList.size();
        StringBuilder sbMsg = new StringBuilder();
        sbMsg.append(String.valueOf(lg)).append(" ").append(i18n.tr("waypoints")).append(" ").append(i18n.tr("sent to GPS"));
        alertbox aInfo = new alertbox(myConfig.getLocale());
        aInfo.alertWithTitle(i18n.tr("GPSDump report"), gpsdWaypWriteReport);
        switch (currGPS) {
            case FlymOld :
            case FlymSD:               
            case Flytec15:
            case Flytec20:
                dialogbox dConfirm = new dialogbox(i18n);        
                if (dConfirm.YesNo("",i18n.tr("Check GPS content"))) { 
                    readFromGPS();
                }                  
                break;                                 
        }
              
    }
    
   private void readFlytec15()  {
        gpsReadList = new ArrayList<>();
        try {
            gpsInfo = new StringBuilder();
            gpsInfo.append(i18n.tr("Incoming")).append("  ").append("Flytec 6015 / IQ Basic ");                  
            jsFlytec15 fly15 = new jsFlytec15(currNamePort); 
            int nbWayp = fly15.getListWaypoints();
            if (nbWayp > 0) {
                gpsReadList = fly15.getWpreadList();            
            } else {
                gpsInfo.append(fly15.getError());
            }            
        } catch (Exception e) {
            sbError = new StringBuilder(this.getClass().getName()+"."+Thread.currentThread().getStackTrace()[1].getMethodName());
            sbError.append("\r\n").append(e.toString());
            mylogging.log(Level.SEVERE, sbError.toString());            
        }                
    }           
   
    private void returnGpsdump(File wptFile) {
        
        if (wptFile.exists()) {
            readFromFile(wptFile.getAbsolutePath());
        }
        else {
            alertbox aError = new alertbox(myConfig.getLocale());
            aError.alertNumError(1210);    // GpsDump did not write the file    
        }
    }
    
    private void readGpsdProgress() {          
        errorComMsg = null;
        File wptFile = systemio.tempacess.getAppFile("Logfly", "tempwp.wpt"); 
        
        Task<Object> worker = new Task<Object>() {
            @Override
            protected Object call() throws Exception {
            gpsdump gpsd = new gpsdump(currNamePort,myConfig, i18n);                  
            switch (currGPS) {
                case FlymSD :
                    gpsd.getOziWpt(1, wptFile);
                    break;
                case FlymOld :
                    gpsd.getOziWpt(2, wptFile);
                    break;
                case Flytec20 :
                    gpsd.getOziWpt(3, wptFile);
                    break;   
                case Flytec15 :
                    gpsd.getOziWpt(8, wptFile);
                    break;
            }       
                return null ;                
            }
        };
        worker.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
            @Override
            public void handle(WorkerStateEvent t) {
                returnGpsdump(wptFile);
            }
        });  

        ProgressDialog dlg = new ProgressDialog(worker);
        dlg.setHeaderText(i18n.tr("GPS import"));
        dlg.setTitle("");
        Thread th = new Thread(worker);
        th.setDaemon(true);
        th.start();         
    }   
   
    /**
     * Used to download wapoints with our own code
     * Currently used for a single device : 6015 (GPSDump Linux  don't support this GPS)
     */
    private void readFromGpsProgress() {          
        errorComMsg = null;
        
        Task<Object> worker = new Task<Object>() {
            @Override
            protected Object call() throws Exception {
                switch (currGPS) {
                    case Flytec15 :
                        readFlytec15();                        
                        break;                
                }       
                return null ;                
            }
        };
        
        worker.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
            @Override
            public void handle(WorkerStateEvent t) {
                displayGpsWaypList();
            }
        }); 

        ProgressDialog dlg = new ProgressDialog(worker);
        dlg.setHeaderText(i18n.tr("GPS import"));
        dlg.setTitle("");
        Thread th = new Thread(worker);
        th.setDaemon(true);
        th.start();  
        
    }   
    
    private void readFromGpsSimple() {
        String waypPath;
        switch (currGPS) {                    
            case Rever :
                winUsbWayp revWin = new winUsbWayp(myConfig,i18n, currGPS,"Reversale");
                waypPath = revWin.getSelWaypPath();
                if (waypPath != null) 
                    readFromFile(waypPath);   
                break;
            case Sky :
                winUsbWayp skyWin = new winUsbWayp(myConfig,i18n, currGPS,"Skytraax 2");
                waypPath = skyWin.getSelWaypPath();
                if (waypPath != null) 
                    readFromFile(waypPath);     
                break;
            case Sky3 :
                winUsbWayp sky3Win = new winUsbWayp(myConfig,i18n, currGPS,"Skytraax 3");
                waypPath = sky3Win.getSelWaypPath();
                if (waypPath != null) 
                    readFromFile(waypPath);  
                break;                              
            case Oudie :
                winUsbWayp oudWin = new winUsbWayp(myConfig,i18n, currGPS,"Oudie");
                waypPath = oudWin.getSelWaypPath();
                if (waypPath != null) 
                    readFromFile(waypPath);  
                break;                                      
            case Syride :
               // readUSBGps();
                break;                            
            case Connect :
                winUsbWayp connectWin = new winUsbWayp(myConfig,i18n, currGPS,"C Pilot");
                waypPath = connectWin.getSelWaypPath();
                if (waypPath != null) 
                    readFromFile(waypPath);  
                break;   
            case Element :
                winUsbWayp elemWin = new winUsbWayp(myConfig,i18n, currGPS,"C Pilot");
                waypPath = elemWin.getSelWaypPath();
                if (waypPath != null) 
                    readFromFile(waypPath);  
                break;                         
            case CPilot :
                winUsbWayp cpilotWin = new winUsbWayp(myConfig,i18n, currGPS,"C Pilot");
                waypPath = cpilotWin.getSelWaypPath();
                if (waypPath != null) 
                    readFromFile(waypPath);  
                break;                     
        }                       
    }
    
    /**
     * Dans V4 tout se tient dans repGPS
     */
    private void readFromGPS() {
        alertbox aError = new alertbox(myConfig.getLocale());             
        switch (currGPS) {
            case Flytec20 :
                switch (myConfig.getOS()) {
                    case WINDOWS :
                    case LINUX :
                        readGpsdProgress();
                        break;
                    case MACOS :     
                        // Up to GPSDump 0.53, waypoint reading was not supported
                        //readFromGpsProgress();
                        // now supported
                        readGpsdProgress();  
                        break;
                }                 
                break;
            case Flytec15 :
                // we arrive here only on Windows, 
                // not supported in Mac or Linux
                gpsInfo = new StringBuilder();
                gpsInfo.append(i18n.tr("Sending to")).append("  ").append("Flytec 6015/ IQ Basic ");
                displayInfo(gpsInfo.toString());
                switch (myConfig.getOS()) {
                    case WINDOWS :
                        readGpsdProgress();  
                        break;
                    case MACOS :        
                    case LINUX : 
                        // Ecriture waypoints non supportée sur Linux et Mac 
                        //writeToGpsProgress();
                        break;
                }
                break;
            case FlymSD :
                switch (myConfig.getOS()) {
                    case WINDOWS :
                    case LINUX :
                        readGpsdProgress();
                        break;
                    case MACOS :     
                        // Up to GPSDump 0.53, waypoint reading was not supported
                        //readFromGpsProgress();
                        // now supported
                        readGpsdProgress();                                                
                        break;
                }                 
                break;
            case FlymOld :
                switch (myConfig.getOS()) {
                    case WINDOWS :
                    case LINUX :
                        readGpsdProgress();
                        break;
                    case MACOS :     
                        // Up to GPSDump 0.53, waypoint reading was not supported
                        //readFromGpsProgress();
                        // now supported
                        readGpsdProgress();  
                        break;
                }                 
                break;
            case Rever :
                readFromGpsSimple();
                break;
            case Sky :
                readFromGpsSimple();
                break;
            case Sky3 :
                readFromGpsSimple();
                break;       
            case Flynet :
                aError.alertInfo(i18n.tr("No waypoints in this GPS"));  
                break;  
            case Sensbox :
                aError.alertInfo(i18n.tr("No waypoints in this GPS"));
                break;                         
            case Oudie :
                readFromGpsSimple();
                break;  
            case Syride :
                aError.alertInfo(i18n.tr("Use GPSDump to download waypoints"));                
                break;                            
            case Connect :
                readFromGpsSimple();                
                break;   
            case Element :
                readFromGpsSimple();
                break;                         
            case CPilot :
                readFromGpsSimple();
                break;  
            case XCTracer :
                aError.alertInfo(i18n.tr("No waypoints in this GPS"));                  
                break;                        
        }               
    }
    
    private void prepWritingFlym() {        
        listForGps = new ArrayList<>();
        String sName;
        int idx = 0;
        try {
            for(pointRecord onePoint : pointList){                     
                position myPos = new position();
                idx++;
                StringBuilder sbLine = new StringBuilder();
                myPos.setLatitudeDd(Double.parseDouble(onePoint.getFLat())); 
                myPos.setLongitudeDd(Double.parseDouble(onePoint.getFLong())); 
                
                // $PFMWPR,4546.878,N,00613.710,E,,DOU046          ,0462*
                sbLine.append("$PFMWPR,").append(myPos.getLatForFly());
                sbLine.append(",").append(myPos.getLongForFly()).append(",,");
                // to avoid empty values
                sName = "WP"+String.format("%03d", idx);   
                sName = String.format("%1$-16.16s",sName);
                switch (gpsTypeName) {
                    case 0:    // long name                    
                        sName = String.format("%1$-16.16s",onePoint.getFDesc());
                        break;
                    case 1 :     // short name
                        if (onePoint.getFBalise() != null && !onePoint.getFBalise().equals("")) 
                            sName = String.format("%1$-16.16s",onePoint.getFBalise());
                        else
                            sName = String.format("%1$-16.16s",onePoint.getFDesc());
                        break;
                    case 2 :      // mixed name
                        sName = String.format("%1$-16.16s",onePoint.getFBalise()+" "+onePoint.getFDesc());
                        break;
                }
                sbLine.append(sName).append(",");
                sbLine.append(String.format("%04d", Integer.parseInt(onePoint.getFAlt()))).append("*"); 
                String fullLine = ajouteChecksum(sbLine.toString())+"\r\n";
                listForGps.add(fullLine); 
            }
            
        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);                       
            alert.setContentText(i18n.tr("Could not translate the points"));
            alert.showAndWait();                  
        }         
    }

    private void prepWritingFly20() {

        listForGps = new ArrayList<>();
        String sName;
        int idx = 0;        
        for(pointRecord onePoint : pointList){         
            try {
                position myPos = new position();
                StringBuilder sbLine = new StringBuilder();
                myPos.setLatitudeDd(Double.parseDouble(onePoint.getFLat())); 
                myPos.setLongitudeDd(Double.parseDouble(onePoint.getFLong())); 
                
                // $PBRWPR,4546.878,N,00613.710,E,,DOU046           ,0462*
                sbLine.append("$PBRWPR,").append(myPos.getLatForFly());
                sbLine.append(",").append(myPos.getLongForFly()).append(",,");
                // to avoid empty values
                sName = "WP"+String.format("%03d", idx);   
                String.format("%1$-17.17s",onePoint.getFDesc());
                switch (gpsTypeName) {
                    case 0:    // long name                    
                        sName = String.format("%1$-17.17s",onePoint.getFDesc());
                        break;
                    case 1 :     // short name
                        if (onePoint.getFBalise() != null && !onePoint.getFBalise().equals("")) 
                            sName = String.format("%1$-17.17s",onePoint.getFBalise());
                        else
                            sName = String.format("%1$-17.17s",onePoint.getFDesc());
                        break;
                    case 2 :      // mixed name
                        sName = String.format("%1$-17.17s",onePoint.getFBalise()+" "+onePoint.getFDesc());
                        break;
                }                
                sbLine.append(sName).append(",");
                
                sbLine.append(String.format("%04d", Integer.parseInt(onePoint.getFAlt()))).append("*"); 
                String fullLine = ajouteChecksum(sbLine.toString())+"\r\n";
                listForGps.add(fullLine);                 
            } catch (Exception e) {
                Alert alert = new Alert(Alert.AlertType.ERROR);                       
                alert.setContentText(i18n.tr("Could not translate the points"));
                alert.showAndWait();                      
            }
            
        }
    }    

    private void prepWritingFly15() {

        listForGps = new ArrayList<>();
        String sName;
        int idx = 0;        
        for(pointRecord onePoint : pointList){         
            try {
                position myPos = new position();
                StringBuilder sbLine = new StringBuilder();
                myPos.setLatitudeDd(Double.parseDouble(onePoint.getFLat())); 
                myPos.setLongitudeDd(Double.parseDouble(onePoint.getFLong()));                 
                // DOU046          ;N  45'46.878;E 006'13.710;   462;   400
                
                // to avoid empty values
                sName = "WP"+String.format("%03d", idx);   
                sName = String.format("%1$-16.16s",sName);
                sName = sName.replaceAll("'", "-");
                switch (gpsTypeName) {
                    case 0:    // long name                    
                        sName = String.format("%1$-16.16s",onePoint.getFDesc());
                        break;
                    case 1 :     // short name
                        if (onePoint.getFBalise() != null && !onePoint.getFBalise().equals("")) 
                            sName = String.format("%1$-16.16s",onePoint.getFBalise());
                        else
                            sName = String.format("%1$-16.16s",onePoint.getFDesc());
                        break;
                    case 2 :      // mixed name
                        sName = String.format("%1$-16.16s",onePoint.getFBalise()+" "+onePoint.getFDesc());
                        break;
                }            
                
                sbLine.append(sName).append(";");                
                sbLine.append(myPos.getLatForFly15()).append(";");
                sbLine.append(myPos.getLongForFly15()).append(";");
                sbLine.append(String.format("%6d", Integer.parseInt(onePoint.getFAlt()))).append(";"); 
                sbLine.append("   400").append("\r\n");    //  Pour le 6015, le rayon  du cylindre est mis par defaut à 400)
                listForGps.add(sbLine.toString());  
            } catch (Exception e) {
                Alert alert = new Alert(Alert.AlertType.ERROR);                       
                alert.setContentText(i18n.tr("Could not translate the points"));
                alert.showAndWait();                      
            }            
        }
    }     
    
    private void showMapPoints() {
        
        String sHTML = null;

        ArrayList<pointIGC> pointsList = new ArrayList<>(); 

        for(pointRecord onePoint : pointList){   
            pointIGC pPoint1 = new pointIGC();
            double dLatitude = Double.parseDouble(onePoint.getFLat());            
            if (dLatitude > 90 || dLatitude < -90) dLatitude = 0;
            pPoint1.setLatitude(dLatitude);
            double dLongitude = Double.parseDouble(onePoint.getFLong());   
            if (dLongitude > 180 || dLongitude < -180) dLongitude = 0;
            pPoint1.setLongitude(dLongitude);
            String sAlt = onePoint.getFAlt();
            if (sAlt.trim() != null && !sAlt.trim().equals("")) {
                pPoint1.setAltiGPS(Integer.parseInt(onePoint.getFAlt()));
            } 
            
            StringBuilder sbComment = new StringBuilder();
            
            
            // pour debug
           // sbComment.append(onePoint.getFBalise()).append("<BR>").append(onePoint.getFDesc()).append("<BR>");
            sbComment.append(onePoint.getFDesc()).append("<BR>");
            
            
            sbComment.append(i18n.tr("Altitude")).append(" : ").append(String.valueOf(pPoint1.AltiGPS)).append(" m" );
            // remove apostrophe
            String sComment = sbComment.toString().replaceAll("'", " ");
            pPoint1.Comment = sbComment.toString();   
            pointsList.add(pPoint1);              
        }
        
        map_waypoints mapSite = new map_waypoints(i18n, myConfig.getIdxMap());
        mapSite.setPointsList(pointsList);
        mapSite.setDefaultPos(defaultPos);
        if (mapSite.genMap() == 0) { 
            sHTML = mapSite.getMap_HTML();  
                        /** ----- Debut Debug --------*/ 
                        final Clipboard clipboard = Clipboard.getSystemClipboard();
                        final ClipboardContent content = new ClipboardContent();
                        content.putString(sHTML);            
                        clipboard.setContent(content);
                        /** ----- Fin Debug --------*/               
                        
            eng.loadContent(sHTML,"text/html"); 
            pont = new Bridge();
            JSObject jsobj = (JSObject) eng.executeScript("window"); 
            jsobj.setMember("java", pont); 
            
        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR);                       
            alert.setContentText(i18n.tr("An error occurred during the map generation"));
            alert.showAndWait();   
        }                   
    }
    
    @FXML
    private void handleNewSet() {
            winSearchCities wCities = new winSearchCities(i18n, this);  
    }  
        
    public void returnFromSearchCities(Sitemodel pSelectedCity) {
        
        try {
            if (pSelectedCity.getLatitude() != null && pSelectedCity.getLongitude() != null) {
                defaultPos.setLatitudeDd(pSelectedCity.getLatitude());     
                defaultPos.setLongitudeDd(pSelectedCity.getLongitude()); 
                // an empty list is required for correct initialization of tableview
                ArrayList<pointRecord> emptyList = new ArrayList<pointRecord>();
                String infoFile = i18n.tr("New file")+"   ";
                displayWpFile(emptyList, infoFile); 
            } else {
                displayDefault();
            }
        } catch ( Exception e) {
            displayDefault();
        }            
    }
    
    public void displayDefault() {    
        
        try {    
            if (myConfig.getFinderLat() != null && myConfig.getFinderLong() != null) {
                defaultPos.setLatitudeDd(Double.parseDouble(myConfig.getFinderLat()));     
                defaultPos.setLongitudeDd(Double.parseDouble(myConfig.getFinderLong()));  
            } else {
                defaultPos.setLatitudeDd(45.00);
                defaultPos.setLongitudeDd(6.00);
            }                        
        } catch (Exception e) {
            defaultPos.setLatitudeDd(45.00);
            defaultPos.setLongitudeDd(6.00);                                        
        } finally {
            // an empty list is required for correct initialization of tableview
            ArrayList<pointRecord> emptyList = new ArrayList<pointRecord>();
            String infoFile = i18n.tr("New file")+"   ";
            displayWpFile(emptyList, infoFile);                                           
        }   
    }
    
    /**
     * after trying many solutions in javascript, this solution seems the simplest 
     */
    private void updateDescription() {
            // Marker description updated
            StringBuilder sb = new StringBuilder();
            sb.append("ChangeDesc(");
            sb.append(String.valueOf(currPoint.getFIndex())).append(",\"");
            sb.append(currPoint.getFDesc());
            sb.append("<BR>").append("Altitude").append(" : ");     
            sb.append(currPoint.getFAlt()).append(" m");
            sb.append("<BR>").append(i18n.tr("Latitude")).append(" : ").append(currPoint.getFLat());
            sb.append("<BR>").append(i18n.tr("Longitude")).append(" : ").append(currPoint.getFLong());
            sb.append("\")");                
            if (eng != null) {              
                eng.executeScript(sb.toString());
            }                   
            tablePoints.refresh();
            enableInput();
            disableCancelPos();        
    }
    
    private void centerMap() {
        currPoint = tablePoints.getSelectionModel().getSelectedItem();
        StringBuilder sb = new StringBuilder();
        sb.append("myclick(").append(String.valueOf(currPoint.getFIndex())).append(")");  
        if (eng != null) {              
            eng.executeScript(sb.toString());
        }    
    }
        
    private void changePosition() {
        int selectedIndex = tablePoints.getSelectionModel().getSelectedIndex();
        if (selectedIndex >= 0) {
            disableInput();
            enableCancelPos();
            currPoint = tablePoints.getSelectionModel().getSelectedItem();
            StringBuilder sb = new StringBuilder();
            sb.append("ChangePos(").append(String.valueOf(currPoint.getFIndex())).append(")");  
            if (eng != null) {              
                eng.executeScript(sb.toString());
            }
        }
    }
    
    private void editPoint() {
        int selectedIndex = tablePoints.getSelectionModel().getSelectedIndex();
        if (selectedIndex >= 0) {
            currPoint = tablePoints.getSelectionModel().getSelectedItem(); 
            winPoint winNewPoint = new winPoint(i18n,currPoint.getFAlt(), currPoint.getFDesc(), currPoint.getFBalise());   
            if (winNewPoint.isModif()) {  
                currPoint.setFBalise(winNewPoint.getBalise());
                currPoint.setFDesc(winNewPoint.getDesc());
                if (chkNoms.isSelected()) {
                    String prefix;
                    if (currPoint.getFDesc().length() > 2) {
                        prefix = currPoint.getFDesc().substring(0, 3);                    
                        prefix = prefix.toUpperCase();
                    } else
                        prefix = "WPT";
                    int iAlt = Integer.parseInt(currPoint.getFAlt())/10;
                    prefix = prefix+String.format("%03d", iAlt);
                    currPoint.setFBalise(prefix);
                }                                
                currPoint.setFAlt(winNewPoint.getAlt().trim());
                updateDescription();   
            }
        }
    }
    
    private void deletePoint() {
        int selectedIndex = tablePoints.getSelectionModel().getSelectedIndex();
        if (selectedIndex >= 0) {            
            pointRecord selectedPoint = tablePoints.getSelectionModel().getSelectedItem();            
            tablePoints.getItems().remove(selectedIndex);
            if (pointList.size() > 0) {
                // Index position must be refreshed
                int idx = 0;
                for(pointRecord onePoint : pointList){  
                    onePoint.setFIndex(idx);
                    idx++;
                }                    
            } else {
                defaultPos.setLatitudeDd(Double.parseDouble(selectedPoint.getFLat()));
                defaultPos.setLongitudeDd(Double.parseDouble(selectedPoint.getFLong()));
            }
            displayInfo(debStatusBar+String.valueOf(pointList.size())+" waypoints");               
            showMapPoints();
        }        
    }
    
    private void disableInput() {
        tablePoints.setDisable(true);
        hbInput.setDisable(true);
        hbAction.setDisable(true);
    }
    
    private void disableCancelPos() {
        hbCancel.setVisible(false);
    }
    
    private void enableCancelPos() {
        hbCancel.setVisible(true);
    }
    
    private void enableInput() {
        tablePoints.setDisable(false);
        hbInput.setDisable(false);
        hbAction.setDisable(false);
    }    
    
    private String debugLoad() {
        String res = null;
        try {
            File f = new File("wayp1.html");
            res = new String(Files.readAllBytes(Paths.get(f.getAbsolutePath())));      
        } catch (IOException e) {
            e.printStackTrace();
        }       
        
        return res;
    }
    
    public void setParam() {
        eng = viewMap.getEngine();
        eng.setUserAgent(" Mozilla/5.0 (Windows NT x.y; Win64; x64; rv:10.0) Gecko/20100101 Firefox/10.0");   
        eng.titleProperty().addListener( (observable, oldValue, newValue) -> {
            if(newValue != null && !newValue.isEmpty() && !newValue.equals("Leaflet"))
                decodeCoord(newValue);
        });
        txPrefix.setText("WAYPOINT");

    }    
    
    /**
     * Is called by the main application to give a reference back to itself.
     * 
     * @param mainApp
     */
    public void setMainApp(Main mainApp) {
        this.mainApp = mainApp; 
        myConfig = mainApp.myConfig;
        i18n = I18nFactory.getI18n("","lang/Messages",WaypViewController.class.getClass().getClassLoader(),myConfig.getLocale(),0);
        winTraduction();
        displayInfo(""); 
        eng = viewMap.getEngine();
        eng.setUserAgent(" Mozilla/5.0 (Windows NT x.y; Win64; x64; rv:10.0) Gecko/20100101 Firefox/10.0");   
        eng.titleProperty().addListener( (observable, oldValue, newValue) -> {
            if(newValue != null && !newValue.isEmpty() && !newValue.equals("Leaflet"))
                decodeCoord(newValue);
        });
        waypStage.setOnHiding( event -> {
            rootController.switchMenu(1);
            rootController.mainApp.showCarnetOverview();
        });        
        
        // to avoid an exception        
        defaultPos.setLatitudeDd(45);
        defaultPos.setLongitudeDd(6);
        txPrefix.setText("WAYPOINT");     
        hbMenu.setVisible(false);
        mapPane.setVisible(false);  
        
                buildContextMenu();
    }        
    
    public void setWinMax()  {           
        waypStage.setMaximized(true);
    }

    /**
     * Sets the stage of this Viewer.
     *
     * @param pWaypStage
     */
    public void setWaypStage(Stage pWaypStage) {
        this.waypStage = pWaypStage;
    }      
    
    /**
     * Set a communication bridge with CarnetViewController 
     * @param callExterne 
     */
    public void setRootBridge(RootLayoutController callRoot)  {
        this.rootController = callRoot;     
    }      
    
    private void displayInfo(String msg) {
        lbInfo.setText(msg);
    }
    
    private void winTraduction() {
        btReadFile.setText(i18n.tr("Read file"));
        btReadGps.setText(i18n.tr("Read GPS"));
        btNewSet.setText(i18n.tr("New"));
        chkNoms.setText(i18n.tr("Auto short names"));
        btNew.setText(i18n.tr("New"));
        btWriteFile.setText(i18n.tr("Write file"));
        btWriteGPS.setText(i18n.tr("Send to GPS"));
        btMail.setText(i18n.tr("Email"));
        colBalise.setText(i18n.tr("Turnpoint"));
        colAlt.setText(i18n.tr("Alt")+".");
        colDesc.setText(i18n.tr("Name"));
        btCancelPos.setText(i18n.tr("Cancel"));
    }   

    public class Bridge { 
        /**
         * In javascript, I'm unable to find index of a marker
         * But it's easy to find popup content
         * We search the description in popup content
         * and find in tableview
         * Tableview research is tricky... Solution in https://stackoverflow.com/questions/40398905/search-tableview-list-in-javafx
         * @param value 
         */
        public void getPopup(String value) { 
            String[] partValue = value.split("<BR>");
            if (partValue.length > 1) {
                tablePoints.getItems().stream()
                    .filter(item -> item.getFDesc().equals(partValue[0])).findAny()
                    .ifPresent(item -> {tablePoints.getSelectionModel().select(item);tablePoints.scrollTo(item);});           
            }
        }    
    }     
}
