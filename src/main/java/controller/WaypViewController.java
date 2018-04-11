/*
 * Copyright Gil THOMAS
 * This file forms an integral part of Logfly project
 * See the LICENSE file distributed with source code
 * for details of Logfly licence project
 */
package controller;

import Logfly.Main;
import dialogues.dialogbox;
import geoutils.googlegeo;
import geoutils.position;
import igc.pointIGC;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ContextMenu;
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
import javafx.scene.layout.HBox;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import leaflet.map_waypoints;
import littlewins.winPoint;
import org.xnap.commons.i18n.I18n;
import org.xnap.commons.i18n.I18nFactory;
import settings.configProg;
import systemio.textio;
import waypio.pointRecord;
import waypio.wpreadfile;
import waypio.wpwritefile;


/**
 *
 * @author gil
 */
public class WaypViewController {
    
    @FXML   
    private TextField txLocality;   
    @FXML
    private TextField txPrefix;
    @FXML
    private Button btGo;        
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
    private Button btSites;   
    @FXML
    private CheckBox chkNoms;
    @FXML
    HBox hbMenu;    
    @FXML
    HBox hbInput;
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
    
    @FXML
    public void initialize() {
        
        pointList = FXCollections.observableArrayList();
        
        colBalise.setCellValueFactory(new PropertyValueFactory<pointRecord, String>("fBalise"));
        colAlt.setCellValueFactory(new PropertyValueFactory<pointRecord, String>("fAlt"));
        colDesc.setCellValueFactory(new PropertyValueFactory<pointRecord, String>("fDesc"));
        
        // Context menu added on a row of the tableview : https://stackoverflow.com/questions/21009377/context-menu-on-a-row-of-tableview
        tablePoints.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent t) {
                if(t.getButton() == MouseButton.SECONDARY) {
                    clicContextMenu().show(tablePoints, t.getScreenX(), t.getScreenY());
                }
            }
        });   
        
        tablePoints.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                centerMap();
            }
        });        
        
        tablePoints.setItems(pointList); 
    }    
    
    @FXML
    private void handleNewWayp() {
        disableInput();
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
        winPoint winNewPoint = new winPoint("", defDesc, defBalise);   
        if (winNewPoint.isModif()) {    
            // Ajout dans la liste
            currPoint = new pointRecord(winNewPoint.getBalise(), winNewPoint.getAlt().trim(), winNewPoint.getDesc());            
            currPoint.setFIndex(-1);
            // map updating
            StringBuilder sb = new StringBuilder();
            sb.append("createNew(\"");
            sb.append(winNewPoint.getDesc().trim());
            sb.append("<BR>").append("Altitude").append(" : ");    
            sb.append(winNewPoint.getAlt().trim()).append(" m");
            sb.append("\")");
            if (eng != null) {              
                eng.executeScript(sb.toString());
            }
            // Point is added to observable list in decodeCoord()
            // Index of javascript markers array will be stored
        }
    }
    
    @FXML
    private void handleReadGPS() {
        File selectedFile = new File("/Users/gil/Documents/Logflya/Balises/Lathuile_test.wpt");
        waypFile = new wpreadfile();
        textio fread = new textio();                                    
        String pFichier = fread.readTxt(selectedFile);   
        if (pFichier != null)  {
            waypFile.litOzi(pFichier);
            StringBuilder sbInfo = new StringBuilder();                    
            sbInfo.append(selectedFile.getAbsolutePath()).append("   ");
            sbInfo.append(i18n.tr("Format")).append(" OZI   ");       
            displayWpFile(waypFile.getWpreadList(), sbInfo.toString());
        }
    }
    
    @FXML
    private void handleWriteGPS() {
        if (pointList.size() > 0) {
            
            File fileTest = new File("test.pcx");
            
            wpwritefile wfile = new wpwritefile();
            wfile.writePCX(pointList, fileTest);
            System.out.println("OK...");
        }    
    }
    
    @FXML
    private void handleReadFile() {
        FileChooser fileChooser = new FileChooser();
        File selectedFile = fileChooser.showOpenDialog(dialogStage);        
        if(selectedFile != null){  
            String ficType = null;
            textio fread = new textio();                                    
            String pFichier = fread.readTxt(selectedFile);   
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
                } else if (pFichier.indexOf("XCPlanner") > -1) {
                    ficType = "XCP";
                } else if (testCompeGPS(pFichier)) {
                    ficType = "COM"; 
                    System.out.println("CompeGPS");
                } else {
                    System.out.println("Fichier non reconnu");
                }
            }
            waypFile = new wpreadfile();
            StringBuilder sbInfo = new StringBuilder();
            sbInfo.append(selectedFile.getAbsolutePath()).append("   ");
            switch (ficType) {
                case "OZI":
                    waypFile.litOzi(pFichier);
                    sbInfo.append(i18n.tr("Format")).append(" OZI   ");                    
                    break;                
                case "GPX":
                    waypFile.litGpx(selectedFile.getAbsolutePath());
                    sbInfo.append(i18n.tr("Format")).append(" GPX   "); 
                    break;        
                case "KML":
                    waypFile.litKml(pFichier);
                    sbInfo.append(i18n.tr("Format")).append(" KML   "); 
                    break;                            
                case "PCX":
                    waypFile.litPcx(pFichier);
                    sbInfo.append(i18n.tr("Format")).append(" PCX   "); 
                    break;      
                case "CUP":
                    waypFile.litCup(pFichier);
                    sbInfo.append(i18n.tr("Format")).append(" CUP   "); 
                    break;          
                case "COM":
                    waypFile.litComp(pFichier);
                    sbInfo.append(i18n.tr("Format")).append(" CompeGPS   "); 
                    break;                     
            }            
            displayWpFile(waypFile.getWpreadList(), sbInfo.toString());
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
            StringBuilder sbInfo = new StringBuilder();
            sbInfo.append(selectedFile.getAbsolutePath()).append("   ");            
            switch (sType) {
                case "1":
                    if (pointList.size() > 0) {
                        wpwritefile wfile = new wpwritefile();
                        sbInfo.append(i18n.tr("Format")).append(" OZI   ");    
                        resWrite = wfile.writeOzi(pointList, selectedFile);                        
                    }
                    break;
                case "2":
                    if (pointList.size() > 0) {
                        wpwritefile wfile = new wpwritefile();
                        sbInfo.append(i18n.tr("Format")).append(" CompeGPS   ");
                        resWrite = wfile.writeComp(pointList, selectedFile);
                    }                    
                    break;
                case "3":
                    if (pointList.size() > 0) {
                        wpwritefile wfile = new wpwritefile();
                        sbInfo.append(i18n.tr("Format")).append(" PCX   "); 
                        resWrite = wfile.writePCX(pointList, selectedFile);
                    }                    
                    break;                  
                case "4":
                    if (pointList.size() > 0) {
                        wpwritefile wfile = new wpwritefile();
                        sbInfo.append(i18n.tr("Format")).append(" KML   ");                         
                        resWrite = resWrite = wfile.writeKml(pointList, selectedFile);                        
                    }                    
                    break;  
                case "5":
                    if (pointList.size() > 0) {
                        wpwritefile wfile = new wpwritefile();
                        resWrite = wfile.writeGpx(pointList, selectedFile);
                        sbInfo.append(i18n.tr("Format")).append(" GPX   "); 
                    }                                        
                    break;          
                case "6":
                    if (pointList.size() > 0) {
                        wpwritefile wfile = new wpwritefile();
                        sbInfo.append(i18n.tr("Format")).append(" CUP   "); 
                        resWrite = wfile.writeCup(pointList, selectedFile);
                    }                    
                    break;                    
            }       
            if (resWrite) {
                this.mainApp.rootLayoutController.updateMsgBar(sbInfo.toString()+String.valueOf(pointList.size())+" waypoints", true, 50);                   
            }
        }
    }    
    
    private void displayWpFile(ArrayList<pointRecord> wpreadList, String infoFile) {
        int sizeList = wpreadList.size();
        tablePoints.getItems().clear();
        pointList = FXCollections.observableArrayList(wpreadList);
        tablePoints.setItems(pointList);
        showMapPoints();
        hbMenu.setVisible(true);
        mapPane.setVisible(true);
        debStatusBar = infoFile;
        this.mainApp.rootLayoutController.updateMsgBar(infoFile+String.valueOf(sizeList)+" waypoints", true, 50);             
        
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
                this.mainApp.rootLayoutController.updateMsgBar(debStatusBar+String.valueOf(pointList.size())+" waypoints", true, 50);                  
            }            
            updateDescription();  
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
            System.out.println(onePoint.getFDesc()+" Alt "+onePoint.getFAlt());
            pPoint1.setAltiGPS(Integer.parseInt(onePoint.getFAlt()));
            StringBuilder sbComment = new StringBuilder();
            sbComment.append(onePoint.getFBalise()).append("<BR>").append(onePoint.getFDesc()).append("<BR>");
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

                        // an empty list is required for correctinitialization of tableview
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
            }
        } else {
            dialogbox dConfirm = new dialogbox();
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
            sb.append("\")");                
            if (eng != null) {              
                eng.executeScript(sb.toString());
            }                   
            tablePoints.refresh();
            enableInput();        
        
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
            this.mainApp.rootLayoutController.updateMsgBar(debStatusBar+String.valueOf(pointList.size())+" waypoints", true, 50);               
            showMapPoints();
        }        
    }
    
    private void disableInput() {
        tablePoints.setDisable(true);
        hbInput.setDisable(true);
    }
    
    private void enableInput() {
        tablePoints.setDisable(false);
        hbInput.setDisable(false);
    }    
    
    private ContextMenu clicContextMenu() {        
        final ContextMenu cm = new ContextMenu();
        
        MenuItem cmPosition = new MenuItem("Position");
        cmPosition.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent e) {
                changePosition();
            }
        });
        cm.getItems().add(cmPosition);        
        
        MenuItem cmEdit = new MenuItem("Editer");        
        cmEdit.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent e) {
               editPoint();
            }            
        });
        cm.getItems().add(cmEdit);
        
        MenuItem cmDelete = new MenuItem("Supprimer");
        cmDelete.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent e) {
                deletePoint();
            }
        });
        cm.getItems().add(cmDelete);     
        
        return cm;
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
        this.mainApp.rootLayoutController.updateMsgBar("", false, 50); 
        eng = viewMap.getEngine();
        eng.setUserAgent(" Mozilla/5.0 (Windows NT x.y; Win64; x64; rv:10.0) Gecko/20100101 Firefox/10.0");   
        eng.titleProperty().addListener( (observable, oldValue, newValue) -> {
            if(newValue != null && !newValue.isEmpty() && !newValue.equals("Leaflet"))
                decodeCoord(newValue);
        });
        // to avoid an exception        
        defaultPos.setLatitudeDd(45);
        defaultPos.setLongitudeDd(6);
        txPrefix.setText("WAYPOINT");     
        hbMenu.setVisible(false);
        mapPane.setVisible(false);       
    }        
    
    private void winTraduction() {
        btReadFile.setText(i18n.tr("Lire fichier"));
        btReadGps.setText(i18n.tr("Lire GPS"));
        chkNoms.setText(i18n.tr("Noms courts auto"));
        btNew.setText(i18n.tr("Nouveau"));
        btWriteFile.setText(i18n.tr("Ecrire fichier"));
        btWriteGPS.setText(i18n.tr("Envoi GPS"));
        btMail.setText(i18n.tr("Mail"));
        btSites.setText(i18n.tr("Sites"));
        txLocality.setPromptText(i18n.tr("Lieu de centrage carte"));
        colBalise.setText(i18n.tr("Balise"));
        colAlt.setText(i18n.tr("Alt."));
        colDesc.setText(i18n.tr("Nom"));
    }       
}
