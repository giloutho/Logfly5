/*
 * Copyright Gil THOMAS
 * This file forms an integral part of Logfly project
 * See the LICENSE file distributed with source code
 * for details of Logfly licence project
 */
package controller;

import Logfly.Main;
import dialogues.ProgressForm;
import dialogues.alertbox;
import dialogues.dialogbox;
import geoutils.googlegeo;
import geoutils.position;
import gps.compass;
import gps.connect;
import gps.element;
import gps.flymaster;
import gps.flymasterold;
import gps.flytec15;
import gps.flytec20;
import static gps.gpsutils.ajouteChecksum;
import gps.oudie;
import gps.reversale;
import gps.skytraax;
import gps.skytraxx3;
import igc.pointIGC;
import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.logging.Level;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
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
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import leaflet.map_waypoints;
import littlewins.winGPS;
import littlewins.winMail;
import littlewins.winPoint;
import littlewins.winTrackFile;
import littlewins.winUsbWWayp;
import littlewins.winUsbWayp;
import model.Balisemodel;
import netscape.javascript.JSObject;
import org.xnap.commons.i18n.I18n;
import org.xnap.commons.i18n.I18nFactory;
import settings.configProg;
import systemio.mylogging;
import systemio.textio;
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
    private TextField txLocality;   
    @FXML
    private TextField txPrefix;
    @FXML
    private Button btGo;        
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
    private ArrayList<String> listForGps = new ArrayList<>();
    private int gpsTypeName;   // Name type : long, short or mixed
    private ContextMenu tableContextMenu;
    
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
        
        MenuItem cmEdit = new MenuItem(i18n.tr("Editer"));        
        cmEdit.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent e) {
               editPoint();
            }            
        });
        tableContextMenu.getItems().add(cmEdit);
        
        MenuItem cmDelete = new MenuItem(i18n.tr("Supprimer"));
        cmDelete.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent e) {
                deletePoint();
            }
        });
        tableContextMenu.getItems().add(cmDelete);     
        
        MenuItem cmCoord = new MenuItem(i18n.tr("Coordonnées"));
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
        if (selectGPS(false)) {
            readFromGPS();    
        }
    }
    
    @FXML
    private void handleWriteGPS() {
        if (pointList.size() > 0) {
            if (selectGPS(true)) {
                writeToGPS();    
            }
        }    
    }
    
    @FXML
    private void handleReadFile() {
        FileChooser fileChooser = new FileChooser();
        File selectedFile = fileChooser.showOpenDialog(dialogStage);        
        if(selectedFile != null){ 
            readFromFile(selectedFile.getAbsolutePath());
        }       
    }
    
    private void displayCoord() {
        currPoint = tablePoints.getSelectionModel().getSelectedItem();
        StringBuilder sb = new StringBuilder();
        sb.append(i18n.tr("Nom court : ")).append(currPoint.getFBalise()).append("\r\n"); 
        sb.append(i18n.tr("Nom long : ")).append(currPoint.getFDesc()).append("\r\n");    
        sb.append(i18n.tr("Latitude : ")).append(currPoint.getFLat()).append("\r\n"); 
        sb.append(i18n.tr("Longitude : ")).append(currPoint.getFLong()).append("\r\n"); 
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
                googlegeo myGoog = new googlegeo();
                // Best results with a low precision in coordinates
                if (sLat.length() > 6) sLat = sLat.substring(0, 6);
                if (sLong.length() > 6) sLong = sLong.substring(0, 6);
                String sCoord = sLat+","+sLong;  
                if (myGoog.googleElevation(sCoord) == 0) {
                    currPoint.setFAlt(myGoog.getGeoAlt().trim());
                } 
                pointList.add(currPoint);
                displayInfo(debStatusBar+String.valueOf(pointList.size())+" waypoints");   
                showMapPoints();
                break;
        }
        enableInput();
    }    
    
    private void readFromFile(String fPath) {
        boolean goodRead = false;
        File file = new File(fPath);
        String ficType = null;
        textio fread = new textio();                                    
        String pFichier = fread.readTxt(file);   
        ficType = "nil";
        if (pFichier != null)  {
            if (pFichier.indexOf("OziExplorer") > -1) {
                ficType = "OZI";
            } else if (pFichier.indexOf("PCX5") > -1) {
                ficType = "PCX";
            } else if (pFichier.indexOf("<kml xmlns") > -1) {
                ficType = "KML";
            } else if (pFichier.indexOf("<?xml version=\"1.0\"") > -1 && pFichier.indexOf("version=\"1.1\"") > -1) {
                ficType = "GPX";
            } else if (pFichier.indexOf("code,country") > -1) {    // Vérifier si cela fonctionne sans les majuscules
                ficType = "CUP";
            } else if (pFichier.indexOf("Code,Country") > -1) {    // Vérifier si cela fonctionne sans les majuscules
                ficType = "CUP";                  
            } else if (pFichier.indexOf("Timestamp=") > -1) {
                ficType = "XCP";
            } else if (testCompeGPS(pFichier)) {
                ficType = "COM"; 
            } else {
                alertbox aError = new alertbox(myConfig.getLocale());
                aError.alertInfo(i18n.tr("Format de fichier non reconnu")); 
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
        } else {
            alertbox aError = new alertbox(myConfig.getLocale());
            aError.alertInfo(i18n.tr("Impossible de décoder le fichier"));                  
        }        
    }
    
    @FXML
    private void handleWriteFile() {
        boolean resWrite = false;
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choisir le format de fichier...");
        FileChooser.ExtensionFilter wptFilter = new FileChooser.ExtensionFilter("1. format OziExplorer (*.wpt)", "*.wpt");
        FileChooser.ExtensionFilter compFilter = new FileChooser.ExtensionFilter("2. format CompeGPS (*.wpt)", "*.wpt");
        FileChooser.ExtensionFilter pcxFilter = new FileChooser.ExtensionFilter("3. format PCX5 (*.pcx)", "*.pcx");
        FileChooser.ExtensionFilter kmlFilter = new FileChooser.ExtensionFilter("4. format KML (*.kml)", "*.kml");
        FileChooser.ExtensionFilter gpxFilter = new FileChooser.ExtensionFilter("5. format GPX (*.gpx)", "*.gpx");
        FileChooser.ExtensionFilter cupFilter = new FileChooser.ExtensionFilter("6. format CUP (*.cup)", "*.cup");
       // FileChooser.ExtensionFilter xcpFilter = new FileChooser.ExtensionFilter("fichiers waypoint (*.xcp)", "*.xcp");
        fileChooser.getExtensionFilters().addAll(wptFilter,compFilter,pcxFilter,kmlFilter,gpxFilter,cupFilter);
        File selectedFile = fileChooser.showSaveDialog(dialogStage);        
        if(selectedFile != null){  
            String sType = fileChooser.getSelectedExtensionFilter().getDescription().substring(0,1);
            writeToFile(sType, selectedFile.getAbsolutePath());
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
                    resWrite = wfile.writeOzi(pointList, file);                        
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
            displayInfo(sbInfo.toString()+String.valueOf(pointList.size())+" waypoints");                   
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
            sb.append(i18n.tr("Gauche : ")).append(shortWest).append(" - ").append(nameWest).append(" -> ").append(df6.format(longWest)).append("\r\n");                    
            sb.append(i18n.tr("Bas : ")).append(shortSud).append(" - ").append(nameSud).append(" -> ").append(df6.format(latSud)).append("\r\n");
            sb.append(i18n.tr("Droit : ")).append(shortEst).append(" - ").append(nameEst).append(" -> ").append(df6.format(longEst)).append("\r\n");
            sb.append(i18n.tr("Haut : ")).append(shortNord).append(" - ").append(nameNord).append(" -> ").append(df6.format(latNord)).append("\r\n\r\n");
            
            // On prend une marge de 11 km soit 0.1 degrés
            longEst+= 0.1;
            longWest -= 0.1;
            latNord += 0.1;
            latSud -= 0.1;
            sb.append(i18n.tr("Bounding box avec une marge de 11 km :")).append("\r\n");
            sb.append("          ").append(df3.format(latNord)).append("\r\n");
            sb.append(df3.format(longWest)).append("           ").append(df3.format(longEst)).append(" ").append("\r\n");
            sb.append("          ").append(df3.format(latSud)).append("\r\n\r\n");
            sb.append(i18n.tr("Placé dans le presse papier"));
            
                                      
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
    private boolean selectGPS(boolean displayName) {
        boolean res = false;   
        winGPS myWin = new winGPS(myConfig, i18n, displayName);    
        if (myWin.getCurrGPS() != null && myWin.getCurrNamePort() != null && myWin.isGpsConnect()) {
            currGPS = myWin.getCurrGPS();
            currNamePort = myWin.getCurrNamePort();
            gpsTypeName = myWin.getCurrTypeName();
            res = true;
        }
        
        return res;
    }
    
    private void displayWpFile(ArrayList<pointRecord> wpreadList, String infoFile) {
        
        ArrayList<pointRecord> wpTot = new ArrayList<>();
        int nbPoints = 0;
        if (pointList.size() > 0) {
            dialogbox dConfirm = new dialogbox(i18n);
            if (dConfirm.YesNo(i18n.tr("Affichage des balises"), i18n.tr("Fusionner l'affichage et le nouveau fichier"))) {
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
            aError.alertInfo(i18n.tr("Pas de waypoints sur ce GPS"));  
        }
    }
    
    private void writeFlymaster() {
       
        try {
            prepWritingFlym();
            flymaster fms = new flymaster();
            if (listForGps.size() > 0 && fms.isPresent(currNamePort)) {             
                gpsInfo = new StringBuilder();
                gpsInfo.append(i18n.tr("Envoi")).append("  ").append("Flymaster ").append(fms.getDeviceType()).append(" ").append(fms.getDeviceFirm()).append("  ");
                fms.setListPFMWP(listForGps);
                fms.sendWaypoint();
                fms.closePort();
            }    
        } catch (Exception e) {
            sbError = new StringBuilder(this.getClass().getName()+"."+Thread.currentThread().getStackTrace()[1].getMethodName());
            sbError.append("\r\n").append(e.toString());
            mylogging.log(Level.SEVERE, sbError.toString());            
        }          
    }
    
    private void writeFlymOld() {
       
        try {
            prepWritingFlym();
            flymasterold fmold = new flymasterold();
            if (listForGps.size() > 0 && fmold.isPresent(currNamePort)) {             
                gpsInfo = new StringBuilder();
                gpsInfo.append(i18n.tr("Envoi")).append("  ").append("Flymaster ").append(fmold.getDeviceType()).append(" ").append(fmold.getDeviceFirm()).append("  ");
                fmold.setListPFMWP(listForGps);
                fmold.sendWaypoint();
                fmold.closePort();
            }    
        } catch (Exception e) {
            sbError = new StringBuilder(this.getClass().getName()+"."+Thread.currentThread().getStackTrace()[1].getMethodName());
            sbError.append("\r\n").append(e.toString());
            mylogging.log(Level.SEVERE, sbError.toString());            
        }          
    }    
    
    private void writeFlytec20() {
       
        try {
            prepWritingFly20();
            flytec20 fls = new flytec20();
            if (listForGps.size() > 0 && fls.isPresent(currNamePort)) {             
                gpsInfo = new StringBuilder();
                gpsInfo.append(i18n.tr("Envoi")).append("  ").append("Flytec 6020/30 ").append(fls.getDeviceType()).append(" ").append(fls.getDeviceFirm()).append("  ");
                fls.setListPBRWP(listForGps);
                fls.sendWaypoint();
                fls.closePort();
            }    
        } catch (Exception e) {
            sbError = new StringBuilder(this.getClass().getName()+"."+Thread.currentThread().getStackTrace()[1].getMethodName());
            sbError.append("\r\n").append(e.toString());
            mylogging.log(Level.SEVERE, sbError.toString());            
        }  
        
    }    

    private void writeFlytec15() {
       
        try {
            prepWritingFly15();
            flytec15 fl15 = new flytec15();
            if (listForGps.size() > 0 && fl15.isPresent(currNamePort)) {
                gpsInfo = new StringBuilder();
                gpsInfo.append(i18n.tr("Envoi")).append("  ").append("Flytec 6015/ IQ Basic ");
                fl15.setListPBRWP(listForGps);
                fl15.sendWaypoint();
                fl15.closePort();
            }    
        } catch (Exception e) {
            sbError = new StringBuilder(this.getClass().getName()+"."+Thread.currentThread().getStackTrace()[1].getMethodName());
            sbError.append("\r\n").append(e.toString());
            mylogging.log(Level.SEVERE, sbError.toString());            
        }          
    }   

    
    private void writeToGpsProgress() {
        dialogbox dConfirm = new dialogbox(i18n);
        StringBuilder sbMsg = new StringBuilder(); 
        sbMsg.append(i18n.tr("Anciens waypoints éventuellement effacés ?"));
        StringBuilder sbTitle = new StringBuilder(); 
        sbTitle.append(i18n.tr("Transfert GPS")).append(" [");
        switch (gpsTypeName) {
            case 0:
                sbTitle.append(i18n.tr("Noms longs"));
                break;
            case 1 :
                sbTitle.append(i18n.tr("Noms courts"));
                break;
            case 2 :
                sbTitle.append(i18n.tr("Mixte"));                
                break;            
        }
        sbTitle.append("]");
        
        if (dConfirm.YesNo(sbTitle.toString(), sbMsg.toString()))   {         

            errorComMsg = null;

            ProgressForm pForm = new ProgressForm();

            Task<Void> task = new Task<Void>() {
                @Override
                public Void call() throws InterruptedException { 
                    switch (currGPS) {
                        case Flytec20 :
                            writeFlytec20();
                            break;
                        case Flytec15 :
                            writeFlytec15();                            
                            break;
                        case FlymSD :
                            writeFlymaster();
                            break;
                        case FlymOld :
                            break;                   
                    }       
                    return null ;                
                }

            };
            // binds progress of progress bars to progress of task:
            pForm.activateProgressBar(task);

            // we update the UI based on result of the task
            task.setOnSucceeded(event -> {
                pForm.getDialogStage().close();
                writeEnd();                       
            });

            pForm.getDialogStage().show();

            Thread thread = new Thread(task);
            thread.start();  
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
                writeToGpsProgress();                
                break;
            case Flytec15 :
                writeToGpsProgress();                
                break;
            case FlymSD :
                writeToGpsProgress();
                break;
            case FlymOld :
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
                aError.alertInfo(i18n.tr("Pas de waypoints sur ce GPS"));  
                break;  
            case Sensbox :
                aError.alertInfo(i18n.tr("Pas de waypoints sur ce GPS"));
                break;                         
            case Oudie :
                writeToGpsSimple();
                break;  
            case Syride :
                aError.alertInfo(i18n.tr("Sauvegarder sur le disque et\r\nutiliser GPSDump pour transférer les waypoints")); 
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
                aError.alertInfo(i18n.tr("Pas de waypoints sur ce GPS"));                  
                break;                        
        }               
    }            
    
    private void writeEnd() {
        
        int lg = pointList.size();
        StringBuilder sbMsg = new StringBuilder();
        sbMsg.append(i18n.tr("Envoi")).append("  ").append(String.valueOf(lg)).append(" ").append(i18n.tr("waypoints"));
        switch (currGPS) {
            case FlymOld :
            case FlymSD:               
            case Flytec15:
            case Flytec20:
                dialogbox dConfirm = new dialogbox(i18n);        
                if (dConfirm.YesNo(i18n.tr("Vérifier le contenu du GPS"), sbMsg.toString())) { 
                    readFromGPS();
                }                  
                break;                                 
        }
              
    }

    private void readFlymOld()  {
        gpsReadList = new ArrayList<>();
        try {
            flymasterold fmsold = new flymasterold();
            if (fmsold.isPresent(currNamePort)) {             
                gpsInfo = new StringBuilder();
                gpsInfo.append(i18n.tr("Réception")).append("  ").append("Flymaster ").append(fmsold.getDeviceType()).append(" ").append(fmsold.getDeviceFirm()).append("  ");            
                int nbWayp = fmsold.getListWaypoints();
                fmsold.closePort();
                if (nbWayp > 0) {
                    gpsReadList = fmsold.getWpreadList();
                } 
            } else {
                gpsInfo.append(fmsold.getError());
            }            
        } catch (Exception e) {
            sbError = new StringBuilder(this.getClass().getName()+"."+Thread.currentThread().getStackTrace()[1].getMethodName());
            sbError.append("\r\n").append(e.toString());
            mylogging.log(Level.SEVERE, sbError.toString());            
        }                
    }    
    
    private void readFlymaster()  {
        gpsReadList = new ArrayList<>();
        try {
            flymaster fms = new flymaster();
            if (fms.isPresent(currNamePort)) {             
                gpsInfo = new StringBuilder();
                gpsInfo.append(i18n.tr("Réception")).append("  ").append("Flymaster ").append(fms.getDeviceType()).append(" ").append(fms.getDeviceFirm()).append("  ");            
                int nbWayp = fms.getListWaypoints();
                fms.closePort();
                if (nbWayp > 0) {
                    gpsReadList = fms.getWpreadList();
                }
            } else {
                gpsInfo.append(fms.getError());
            }            
        } catch (Exception e) {
            sbError = new StringBuilder(this.getClass().getName()+"."+Thread.currentThread().getStackTrace()[1].getMethodName());
            sbError.append("\r\n").append(e.toString());
            mylogging.log(Level.SEVERE, sbError.toString());            
        }                
    }    
    
   private void readFlytec20()  {
        gpsReadList = new ArrayList<>();
        try {
            flytec20 fls = new flytec20();
            if (fls.isPresent(currNamePort)) {             
                gpsInfo = new StringBuilder();
                gpsInfo.append(i18n.tr("Réception")).append("  ").append("Flytec 6020/30 ").append(fls.getDeviceType()).append(" ").append(fls.getDeviceFirm()).append("  ");            
                int nbWayp = fls.getListWaypoints();
                fls.closePort();
                if (nbWayp > 0) {
                    gpsReadList = fls.getWpreadList();
                }
            } else {
                gpsInfo.append(fls.getError());
            }            
        } catch (Exception e) {
            sbError = new StringBuilder(this.getClass().getName()+"."+Thread.currentThread().getStackTrace()[1].getMethodName());
            sbError.append("\r\n").append(e.toString());
            mylogging.log(Level.SEVERE, sbError.toString());            
        }                
    }        
    
   private void readFlytec15()  {
        gpsReadList = new ArrayList<>();
        try {
            flytec15 fl15 = new flytec15();
            if (fl15.isPresent(currNamePort)) {             
                gpsInfo = new StringBuilder();
                gpsInfo.append(i18n.tr("Réception")).append("  ").append("Flytec 6015 / IQ Basic ");          
                int nbWayp = fl15.getListWaypoints();
                fl15.closePort();
                if (nbWayp > 0) {
                    gpsReadList = fl15.getWpreadList();
                }
            } else {
                gpsInfo.append(fl15.getError());
            }            
        } catch (Exception e) {
            sbError = new StringBuilder(this.getClass().getName()+"."+Thread.currentThread().getStackTrace()[1].getMethodName());
            sbError.append("\r\n").append(e.toString());
            mylogging.log(Level.SEVERE, sbError.toString());            
        }                
    }           
   
    private void readFromGpsProgress() {          
        errorComMsg = null;
        
        ProgressForm pForm = new ProgressForm();
           
        Task<Void> task = new Task<Void>() {
            @Override
            public Void call() throws InterruptedException { 
                switch (currGPS) {
                    case Flytec20 :
                        readFlytec20();
                        break;
                    case Flytec15 :
                        readFlytec15();                        
                        break;
                    case FlymSD :
                        readFlymaster();
                        break;
                    case FlymOld :
                        readFlymOld();
                        break;                  
                }       
                return null ;                
            }
        };
        // binds progress of progress bars to progress of task:
        pForm.activateProgressBar(task);

        // we update the UI based on result of the task
        task.setOnSucceeded(event -> {
            pForm.getDialogStage().close();
            displayGpsWaypList();                       
        });

        pForm.getDialogStage().show();

        Thread thread = new Thread(task);
        thread.start();         
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
                readFromGpsProgress();
                break;
            case Flytec15 :
                readFromGpsProgress();                
                break;
            case FlymSD :
                readFromGpsProgress();
                break;
            case FlymOld :
                readFlymOld();
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
                aError.alertInfo(i18n.tr("Pas de waypoints sur ce GPS"));  
                break;  
            case Sensbox :
                aError.alertInfo(i18n.tr("Pas de waypoints sur ce GPS"));
                break;                         
            case Oudie :
                readFromGpsSimple();
                break;  
            case Syride :
                aError.alertInfo(i18n.tr("Utiliser GPSDump pour décharger les waypoints"));                
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
                aError.alertInfo(i18n.tr("Pas de waypoints sur ce GPS"));                  
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
            alert.setContentText(i18n.tr("Une erreur est survenue pendant la préparation des points"));
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
                alert.setContentText(i18n.tr("Une erreur est survenue pendant la préparation des points"));
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
                alert.setContentText(i18n.tr("Une erreur est survenue pendant la préparation des points"));
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
            alert.setContentText(i18n.tr("Une erreur est survenue pendant la génération de la carte"));
            alert.showAndWait();   
        }                   
    }
    
    @FXML
    private void handleGo() {
        if (pointList.size() == 0) {
            if (txLocality.getText() != null && !txLocality.getText().equals("")) {
                try {
                    googlegeo myGoog = new googlegeo();
                    if (myGoog.googleLatLong(txLocality.getText().trim()) == 0) {
                        defaultPos.setLatitudeDd(Double.parseDouble(myGoog.getGeoLat()));     
                        defaultPos.setLongitudeDd(Double.parseDouble(myGoog.getGeoLong())); 

                        // an empty list is required for correct initialization of tableview
                        ArrayList<pointRecord> emptyList = new ArrayList<pointRecord>();
                        String infoFile = i18n.tr("Nouveau fichier")+"   ";
                        displayWpFile(emptyList, infoFile);                    
                    } else {
                        txLocality.clear();
                        txLocality.setPromptText(i18n.tr("Lieu non trouvé..."));                    
                    }
                } catch (Exception e) {
                    txLocality.clear();
                    txLocality.setPromptText(i18n.tr("Problème de geolocalisation"));                                            
                }                      
            } else {
                if (myConfig.getFinderLat() != null && myConfig.getFinderLong() != null) {
                    try {
                        defaultPos.setLatitudeDd(Double.parseDouble(myConfig.getFinderLat()));     
                        defaultPos.setLongitudeDd(Double.parseDouble(myConfig.getFinderLong())); 
                        // an empty list is required for correct initialization of tableview
                        ArrayList<pointRecord> emptyList = new ArrayList<pointRecord>();
                        String infoFile = i18n.tr("Nouveau fichier")+"   ";
                        displayWpFile(emptyList, infoFile);                           
                    } catch (Exception e) {
                        txLocality.clear();
                        txLocality.setPromptText(i18n.tr("Problème sur paramètres"));                             
                    }                         
                }
            }
        } else {
            dialogbox dConfirm = new dialogbox(i18n);
            StringBuilder sbMsg = new StringBuilder(); 
            sbMsg.append(i18n.tr("Abandonner la liste en cours"));
            if (dConfirm.YesNo(sbMsg.toString(),""))  { 
                tablePoints.getItems().clear();
                handleGo();
            }            
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
            winPoint winNewPoint = new winPoint(currPoint.getFAlt(), currPoint.getFDesc(), currPoint.getFBalise());   
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
        btReadFile.setText(i18n.tr("Lire fichier"));
        btReadGps.setText(i18n.tr("Lire GPS"));
        chkNoms.setText(i18n.tr("Noms courts auto"));
        btNew.setText(i18n.tr("Nouveau"));
        btWriteFile.setText(i18n.tr("Ecrire fichier"));
        btWriteGPS.setText(i18n.tr("Envoi GPS"));
        btMail.setText(i18n.tr("Mail"));
        StringBuilder sbLoc = new StringBuilder();
        sbLoc.append(i18n.tr("Nouveau")).append("... ").append("Saisir une ville").append(" + Go");
        txLocality.setPromptText(sbLoc.toString());
        colBalise.setText(i18n.tr("Balise"));
        colAlt.setText(i18n.tr("Alt."));
        colDesc.setText(i18n.tr("Nom"));
        btCancelPos.setText(i18n.tr("Abandon"));
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
