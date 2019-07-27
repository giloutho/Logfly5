/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package controller;

import Logfly.Main;
import airspacelib.dbAirspace;
import com.chainstaysoftware.filechooser.FileChooserFx;
import com.chainstaysoftware.filechooser.FileChooserFxImpl;
import com.chainstaysoftware.filechooser.ViewType;
import dialogues.ProgressForm;
import dialogues.alertbox;
import geoutils.position;
import gps.reversale;
import io.jenetics.jpx.GPX;
import io.jenetics.jpx.Track;
import io.jenetics.jpx.TrackSegment;
import io.jenetics.jpx.WayPoint;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBoxTreeItem;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.control.cell.CheckBoxTreeCell;
import javafx.scene.image.ImageView;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import leaflet.map_air;
import leaflet.map_air_draw;
import littlewins.winAirDraw;
import littlewins.winAirSearch;
import model.airdraw;
import model.airspacetree;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.xnap.commons.i18n.I18n;
import org.xnap.commons.i18n.I18nFactory;
import settings.configProg;
import settings.osType;
import systemio.mylogging;
import systemio.tempacess;
import trackgps.checkAirspace;
import trackgps.traceGPS;

/**
 *
 * @author gil
 * 
 * we began with simple treeview eg https://o7planning.org/fr/11147/tutoriel-javafx-treeview
 * but we had some problems to check every checkbox
 * we modify treeitem for checkboxtreeitem (https://stackoverflow.com/questions/28393704/javafxcheck-every-single-checkboxtreeitem-if-selected)
 */
public class AirspaceController {
    
    @FXML
    private Button btReadFile;    
    @FXML
    private Button btDraw;
    @FXML
    private Button btDrawCancel;
    @FXML
    private Button btDrawValid;
    @FXML
    private ChoiceBox ch_Flight;   
    @FXML
    private Button btRefresh;
    @FXML
    private Button btReset;
    @FXML
    private Button btWrite;    
    @FXML
    private ImageView top_Menu; 
    @FXML
    private HBox hbAction;
    @FXML
    private SplitPane mapPane;
    @FXML
    private TreeView<airspacetree> treeAir;
    @FXML
    private WebView viewMap;
    private WebEngine eng;
 
    
    private Node componentsPane;
    
    // Reference to the main application.
    private Main mainApp;    
    // Localization
    private I18n i18n; 
    // Settings
    configProg myConfig;
    
    private Stage airStage;    
    private RootLayoutController rootController; 
    
    private final String RC = "\n";
    private dbAirspace currDbAir = null;
    CheckBoxTreeItem<airspacetree> rootItem;
    private StringBuilder sbTotalGeoJson;
    private boolean dbView;
    private int levelFlight;
    private double latMini;
    private double latMaxi;
    private double longMini;
    private double longMaxi;
    private String sLatMini;
    private String sLatMaxi;
    private String sLongMini;
    private String sLongMaxi;    
    private boolean actionDraw;
    private StringBuilder sbError;
    private ObservableList<airdraw> lsDraw = FXCollections.observableArrayList();
    private int airsDisplayed = 0;
    private int airTotal = 0;
    private String airNameFile = null;
    private reversale usbRever;
    private int comReversale;
    private boolean DrawScreen;
    
    @FXML
    private void initialize() {

        componentsPane=mapPane.getItems().get(0);
                        
        eng = viewMap.getEngine();
        // With this, OSM layer not displayed
       // eng.setUserAgent(" Mozilla/5.0 (Windows NT x.y; Win64; x64; rv:10.0) Gecko/20100101 Firefox/10.0");              

        eng.titleProperty().addListener( (observable, oldValue, newValue) -> {
            if(newValue != null && !newValue.isEmpty() && !newValue.equals("Leaflet")) {                
                decodeTitle(newValue);
            }
        });  
        
        dbView = false;        
        
        treeAir.setCellFactory(CheckBoxTreeCell.forTreeView());
        treeAir.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if(newValue != null)
                displaySelectedItem(newValue.getValue().getName());
        });       
        
        ObservableList<String> listAlt = FXCollections.observableArrayList();        
        listAlt.addAll("------", "1000m","2000m","3000m","4000m","5000m","6000m");        
        ch_Flight.setItems(listAlt);
        ch_Flight.getSelectionModel().selectFirst();
        levelFlight = 9999;
        
        ch_Flight.setOnAction((event) -> {
            String sLF = (String) ch_Flight.getSelectionModel().getSelectedItem();
            switch (sLF) {
                case "------" :
                    levelFlight = 9999;                    
                    break;
                case "1000m" :
                    levelFlight = 1000;
                    break;
                case "2000m" :
                    levelFlight = 2000;
                    break;
                case "3000m" :
                    levelFlight = 3000;
                    break;
                case "4000m" : 
                    levelFlight = 4000;
                    break;
                case "5000m" :
                    levelFlight = 5000;
                    break;
                case "6000m" :
                    levelFlight = 6000;
                    break;
                default:
                    throw new AssertionError();
            }
            resetVisuField();
            buildTree();
        });        
        
        top_Menu.addEventHandler(MouseEvent.MOUSE_CLICKED,
            new EventHandler<MouseEvent>() {
                @Override public void handle(MouseEvent e) {                        
                        clicTop_Menu().show(top_Menu, e.getScreenX(), e.getScreenY());
                }
        });            
        
    }       
        
    @FXML
    private void handleReadFile(ActionEvent event) {     
                
        // if it's not first time levelFlight must be reset
        if (ch_Flight.getSelectionModel().getSelectedIndex() > 0) {
            ch_Flight.getSelectionModel().selectFirst();
            levelFlight = 9999;            
        }
        
        final FileChooserFx fileChooser = new FileChooserFxImpl();
        fileChooser.setShowHiddenFiles(false);
        fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("airspaces files (txt)", "*.txt", "*.TXT"));          
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
            replyReadFile(sPath);
        });                
    }    
    
    private void replyReadFile(String strChooser) {
    alertbox aError = new alertbox(myConfig.getLocale());
    if (strChooser != null) {
        try {
            File selectedFile = new File(strChooser);            
            if(selectedFile.exists()){
                actionDraw = false;
                screenForTree();
                readAirspaceFile(selectedFile);                                
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
    
    @FXML
    private void handleWrite(ActionEvent event) {
        exportChoose();
    }
    
    @FXML
    private void handleDraw(ActionEvent event) {
      
        // if it's not first time
        treeAir.setRoot(null);
        screenForDraw();
        
        String sCoord = null;        
        
        if (myConfig.getFinderLat() != null && myConfig.getFinderLong() != null) {
            try {
                double testLat = Double.parseDouble(myConfig.getFinderLat());     
                double testLong = Double.parseDouble(myConfig.getFinderLong()); 
                sCoord = myConfig.getFinderLat()+","+myConfig.getFinderLong();
            } catch (Exception e) {
                // settings not valid, we put Annecy Lake
                sCoord = "45.863,6.1725";                       
            }                         
        }

        actionDraw = true;

        map_air_draw airDraw = new map_air_draw(i18n,sCoord); 
        
                            final Clipboard clipboard = Clipboard.getSystemClipboard();
                            final ClipboardContent content = new ClipboardContent();
                            content.putString(airDraw.getMap_HTML());            
                            clipboard.setContent(content);                
                            
        if (airDraw.isMap_OK()) {
            eng.loadContent(airDraw.getMap_HTML(),"text/html");            
        }   
    }    
    
    @FXML 
    private void handleDrawValid(ActionEvent event) {
        eng.executeScript("How_Shapes()");
        screenCloseDraw();
    }
    
    @FXML 
    private void handleDrawCancel(ActionEvent event) {
        screenCloseDraw();
    }
            
            
    @FXML
    private void handleRefresh(ActionEvent event) {
        buildTree(); 
    }    
    
    @FXML
    private void handleReset(ActionEvent event) {
       viewReset(true);
    }    
                    
    /**
    * Adding Context Menus, last paragraph
    *     http://docs.oracle.com/javafx/2/ui_controls/menu_controls.htm    
    */
    private ContextMenu clicTop_Menu()   {
        final ContextMenu cm = new ContextMenu();
        
        MenuItem cmItem0 = new MenuItem("Recherche");        
        cmItem0.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent e) {
                displaySearch();
            }            
        });
        cm.getItems().add(cmItem0);

        MenuItem cmItem1 = new MenuItem("Fusion");
        cmItem1.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent e) {
               fileMerge();
            }
        });
        cm.getItems().add(cmItem1);            
        
        MenuItem cmItem2 = new MenuItem("Export GPX");
        cmItem2.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent e) {
                gpxChoose();
            }
        });
        cm.getItems().add(cmItem2);            
                  
        MenuItem cmItem4 = new MenuItem("Reversale");
        cmItem4.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent e) {
                if (reversaleCheck()) reversaleCom();  
            }
        });                
        cm.getItems().add(cmItem4);        
        
        return cm;
    }
    
    public void readAirspaceFile(File pFile) {        

        currDbAir = new dbAirspace(pFile, null);
        if (currDbAir.isDbOK()) {
            airNameFile = pFile.getName();
            airTotal = currDbAir.getNbAirspaces();                        
            dbView = false;
            buildTree();            
        } else {           
            alertbox aError = new alertbox(myConfig.getLocale());
            aError.alertNumError(1350);   // Erreur de décodage du fichier Open Air...    
        }
    }    
    
    private void updateInfo(boolean displayInfo) {
        if (displayInfo) {
            StringBuilder sbInfo = new StringBuilder();        
            if (airNameFile != null) sbInfo.append(airNameFile).append("  ");        
            sbInfo.append(String.valueOf(airTotal)).append(" ").append(i18n.tr("airspaces")).append("  ");
            sbInfo.append(String.valueOf(airsDisplayed)).append(" ").append(i18n.tr("displayed"));
            airStage.setTitle(sbInfo.toString());          
        } else {
            airStage.setTitle("");
        }
    }
    
    
    private void buildTree() {
        
        try {
            Statement stmt = currDbAir.getDbConn().createStatement();     
            ResultSet rs = null;    
            String sReq = null;
            if (dbView) {
                sReq = "SELECT Classe,Count(Z_ID), Max(Floor) FROM SELECT_VIEW  WHERE Visu = '1' GROUP BY Classe";
            } else {
                sReq = "SELECT Classe,Count(Z_ID) FROM Zones  WHERE Visu = '1' GROUP BY Classe";
            }
            rs = stmt.executeQuery(sReq);
            if (rs != null)  {   
                // initialization
                airspacetree mainAirspec = new airspacetree("Espaces", true);
                airsDisplayed = 0;
                sbTotalGeoJson = new StringBuilder();
                sbTotalGeoJson.append("    var zoneReg = {").append("    \"type\": \"FeatureCollection\",").append(RC);
                sbTotalGeoJson.append("    \"crs\": { \"type\": \"name\", \"properties\": { \"name\": \"urn:ogc:def:crs:OGC:1.3:CRS84\" } },").append(RC);
                sbTotalGeoJson.append("    \"features\": [").append(RC);
                rootItem = new CheckBoxTreeItem<airspacetree>(mainAirspec);                                        
                rootItem.setExpanded(true);
                while (rs.next()) {                                                
                    String currClasse = rs.getString(1);                        
                    if (currClasse != null && !currClasse.equals("")) {
                        // Create tree items of an airspace category
                        rootItem.getChildren().add(createTreeItem(currClasse,rs.getInt(2)));                            
                    }   
                }
                // totalGeoJson is completed, last comma deleted
                if (sbTotalGeoJson.length() > 0) {
                    sbTotalGeoJson.setLength(sbTotalGeoJson.length() - 1);
                }               
                sbTotalGeoJson.append("]}");                
                treeAir.setRoot(rootItem);
                treeAir.setShowRoot(false);
                updateInfo(true);

                map_air airMap = new map_air(sbTotalGeoJson); 
                
                final Clipboard clipboard = Clipboard.getSystemClipboard();
                final ClipboardContent content = new ClipboardContent();
                content.putString(airMap.getMap_HTML());            
                clipboard.setContent(content);                
                
                if (airMap.isMap_OK()) {
                    eng.loadContent(airMap.getMap_HTML(),"text/html");
                }
            }                                            
        } catch (Exception ex) {
            sbError = new StringBuilder(this.getClass().getName()+"."+Thread.currentThread().getStackTrace()[1].getMethodName());
            sbError.append("\r\n").append(ex.toString());
            mylogging.log(Level.SEVERE, sbError.toString());  
            alertbox aError = new alertbox(myConfig.getLocale());
            aError.alertError(ex.getClass().getName() + ": " + ex.getMessage());  
        }        
    }
    
            
    private TreeItem<airspacetree> createTreeItem(String rootName, int nbItems) {
        StringBuilder sbClass = new StringBuilder();
        int currLevelFlight;
        String reqUpd = "UPDATE Zones SET Visu = '0' WHERE Z_ID = ?";
       
        sbClass.append(rootName).append("->"); 
        airspacetree subMainAirspace = new airspacetree(sbClass.toString(), true);
        CheckBoxTreeItem<airspacetree> rootItem = new CheckBoxTreeItem<airspacetree>(subMainAirspace);    
        if (levelFlight == 9999) {
            currLevelFlight = levelFlight;
            rootItem.setSelected(subMainAirspace.isSelected());
        } else {
            if (rootName.equals("E") || rootName.equals("G") || rootName.equals("F")) {
                currLevelFlight = -100;
                rootItem.setSelected(false);
            } else {
                currLevelFlight = levelFlight;
                rootItem.setSelected(subMainAirspace.isSelected());
            }
        }       
        
        try {            
            String reqDetails = null;
            if (dbView) {
                reqDetails = "SELECT Z_ID,Name,Geojson,Floor FROM SELECT_VIEW WHERE Classe = ? AND Visu = '1'";                 
            } else {
                reqDetails = "SELECT Z_ID,Name,Geojson,Floor FROM Zones WHERE Classe = ? AND Visu = '1'"; 
            }
            PreparedStatement ps = currDbAir.getDbConn().prepareStatement(reqDetails);
            ps.setString(1, rootName);
            ResultSet rsDetails = null;    
            rsDetails = ps.executeQuery();
            if (rsDetails != null)  {       
                int cptSel = 0;
                while (rsDetails.next()) {
                    airspacetree itemAirspace = new airspacetree(rsDetails.getString(2), true);
                    CheckBoxTreeItem<airspacetree> item = new CheckBoxTreeItem<airspacetree>(itemAirspace);                       
                    if (rsDetails.getInt(4) <= currLevelFlight) {
                        item.setSelected(itemAirspace.isSelected());
                        sbTotalGeoJson.append(rsDetails.getString(3)).append(",");
                        cptSel++;
                    } else {
                        item.setSelected(false);
                        // On le met à 0
                        try {
                            PreparedStatement ps2 = currDbAir.getDbConn().prepareStatement(reqUpd);
                            ps2.setInt(1, rsDetails.getInt(1));                        
                            ps2.executeUpdate();                                                                   
                        } catch (Exception ex) {
                            sbError = new StringBuilder(this.getClass().getName()+"."+Thread.currentThread().getStackTrace()[1].getMethodName());
                            sbError.append("\r\n").append(ex.toString());
                            sbError.append("\r\n").append(reqDetails);
                            mylogging.log(Level.SEVERE, sbError.toString());  
                            alertbox aError = new alertbox(myConfig.getLocale());
                            aError.alertError(ex.getClass().getName() + ": " + ex.getMessage());  
                        }
                    }
                    item.selectedProperty().addListener((obs, oldVal, newVal) -> {
                        updateVisu(item.getValue().toString(), newVal);
                    });                    
                    rootItem.getChildren().add(item);                   
                }
                // Name is updated
                sbClass.append(String.valueOf(cptSel)).append("/").append(String.valueOf(nbItems));
                subMainAirspace.setName(sbClass.toString());
                rootItem.setValue(subMainAirspace);
                airsDisplayed += cptSel;
            } 
        } catch (Exception ex) {
            sbError = new StringBuilder(this.getClass().getName()+"."+Thread.currentThread().getStackTrace()[1].getMethodName());
            sbError.append("\r\n").append(ex.toString());
            mylogging.log(Level.SEVERE, sbError.toString());  
            alertbox aError = new alertbox(myConfig.getLocale());
            aError.alertError(ex.getClass().getName() + ": " + ex.getMessage());  
        }        

        return rootItem;
    }    
    
    private void gpxChoose() {
        
        final FileChooserFx fileChooser = new FileChooserFxImpl();
        fileChooser.setShowHiddenFiles(false);                                              
        fileChooser.setShowMountPoints(true);       
        fileChooser.setViewType(ViewType.List);
        fileChooser.setDividerPositions(.15, .30);
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
            replyGpxChoose(sPath, ".gpx");
        });          
    }    
    
    private void replyGpxChoose(String strChooser, String formatExt) {
        int res = -1;
        alertbox aError = new alertbox(myConfig.getLocale());
        if (strChooser != null) {
            try {
                File save = new File(strChooser);  
                if (!save.getPath().toLowerCase().endsWith(formatExt)) { 
                    save = new File(save.getPath() + formatExt); 
                }
                buildGPX(save); 
            } catch (Exception ex) {
                sbError = new StringBuilder(this.getClass().getName()+"."+Thread.currentThread().getStackTrace()[1].getMethodName());
                sbError.append("\r\n").append(ex.toString());
                mylogging.log(Level.SEVERE, sbError.toString());  
                aError = new alertbox(myConfig.getLocale());
                aError.alertError(ex.getClass().getName() + ": " + ex.getMessage());                
            }                    
        } else {
            aError.alertNumError(res);
        }
    }        
    
    private void buildGPX(File pFile) {
        
        int res = -1;
        StringBuilder sbExp = new StringBuilder();
        String sReq = null;
        int iAlt;
        
        if (dbView) {
            sReq = "SELECT Name, Geojson, Classe, Ceiling FROM SELECT_VIEW WHERE VISU = ? AND FLOOR <= ?";                    
        } else {
            sReq = "SELECT Name, Geojson,Classe, Ceiling FROM Zones WHERE VISU = ? AND FLOOR <= ?";
        }                     
        try {
            PreparedStatement ps = currDbAir.getDbConn().prepareStatement(sReq);
            ps.setString(1, "1");
            ps.setInt(2, levelFlight);
            ResultSet rs = null;    
            rs = ps.executeQuery();
            if (rs != null && rs.isBeforeFirst())  {    
                GPX gpx = null;
                List<Track> lsTracks = new ArrayList<>();
                while (rs.next()) {  
                    String sClass = rs.getString(3);
                    if (sClass.contains("CTR")) 
                        iAlt = rs.getInt(4);
                    else
                        iAlt = 0;                        
                    List<WayPoint> lWayp = geojsonToGpx(rs.getString(2),iAlt);
                    if (lWayp.size() > 0) {
                        TrackSegment segment = TrackSegment.builder().points(lWayp).build();      
                        Track track = Track.builder().name(rs.getString(1)).addSegment(segment).build();
                        lsTracks.add(track);
                    }             
                }
                if (lsTracks.size() > 0) {
                    try {
                        gpx = GPX.builder().tracks(lsTracks).build();
                        GPX.write(gpx,pFile.getAbsolutePath());
                        res = 0;                        
                    } catch (Exception e) {
                        res = 2;
                    }
                } else {
                    res = 1380;   // Génération GPX impossible : lsTracks vide
                }                      
                alertbox finOp = new alertbox(myConfig.getLocale());
                finOp.alertNumError(res); 
            }      
        } catch (Exception ex ) {
            sbError = new StringBuilder(this.getClass().getName()+"."+Thread.currentThread().getStackTrace()[1].getMethodName());
            sbError.append("\r\n").append(ex.toString());
            mylogging.log(Level.SEVERE, sbError.toString());  
            alertbox aError = new alertbox(myConfig.getLocale());
            aError.alertError(ex.getClass().getName() + ": " + ex.getMessage());   
        }       
    }    
    
    private List<WayPoint> geojsonToGpx(String strGeoJson, int elev) {
        
        List<WayPoint> lWayp = new ArrayList<WayPoint>();
        
        JSONParser jsonParser = new JSONParser();
        try {
            JSONObject jsonObject = (JSONObject) jsonParser.parse(strGeoJson);
            JSONObject jsGeometry = (JSONObject) jsonObject.get("geometry");
            JSONArray totalCoord = (JSONArray) jsGeometry.get("coordinates");
            if (totalCoord != null)  {
                for(int i=0; i< totalCoord.size(); i++){                
                    JSONArray coord = (JSONArray) totalCoord.get(i);
                    for (int j = 0; j < coord.size(); j++) {
                        JSONArray latLong = (JSONArray) coord.get(j);
                        double dLat = Double.valueOf(latLong.get(1).toString());
                        double dLong = Double.valueOf(latLong.get(0).toString());
                        WayPoint point = WayPoint.builder().lat(dLat).lon(dLong).ele(elev).build(); 
                        lWayp.add(point);                                               
                    }
                }            
            } else {
                sbError = new StringBuilder(this.getClass().getName()+"."+Thread.currentThread().getStackTrace()[1].getMethodName());
                sbError.append("\r\n").append("JSON Array null "+totalCoord.toJSONString());
                mylogging.log(Level.SEVERE, sbError.toString());                  
                System.out.println("JSONArray nul");
            }
        } catch (Exception e) {
            System.out.println("Erreur "+e.getMessage());
        }
        
        return lWayp;
    }
    
    // https://stackoverflow.com/questions/28342309/iterate-treeview-nodes
    private void iterateTree(TreeItem<airspacetree> root) {

        for(TreeItem<airspacetree> child: root.getChildren()){
            if(child.getChildren().isEmpty()){
                if(((CheckBoxTreeItem)child).isSelected())
                if (child.getValue().isSelected()) {
                    System.out.println(child.getValue().getName());
                }
            } else {
                iterateTree(child);
            }
        }        
    }   
    
    
    private void updateVisu(String itemValue, boolean stateValue) {
        String visuType;
        String req = null;
        if (!itemValue.contains("->")) {
            if (stateValue == false) 
                visuType = "0";
            else 
                visuType = "1";
            try {                
                req = "UPDATE Zones SET Visu = ? WHERE Name LIKE ?";
                PreparedStatement ps = currDbAir.getDbConn().prepareStatement(req);
                ps.setString(1, visuType);
                ps.setString(2, itemValue+"%");
                ps.executeUpdate(); 
            } catch (Exception ex) {
                sbError = new StringBuilder(this.getClass().getName()+"."+Thread.currentThread().getStackTrace()[1].getMethodName());
                sbError.append("\r\n").append(ex.toString());
                sbError.append("\r\n").append(req);
                mylogging.log(Level.SEVERE, sbError.toString());  
                alertbox aError = new alertbox(myConfig.getLocale());
                aError.alertError(ex.getClass().getName() + ": " + ex.getMessage());  
            }  
        }              
    }
    
    private void displaySelectedItem(String itemValue) {      
        String req = null;
        if (itemValue != null && !itemValue.contains("->")) {
            try {
                if (dbView) {
                    req = "SELECT LatMini,LatMaxi,LongMini,LongMaxi,Name,Classe,Floor,Ceiling FROM SELECT_VIEW  WHERE Name LIKE ?";
                } else {
                    req = "SELECT LatMini,LatMaxi,LongMini,LongMaxi,Name,Classe,Floor,Ceiling FROM Zones WHERE Name LIKE ?";
                }
                PreparedStatement ps = currDbAir.getDbConn().prepareStatement(req);
                ps.setString(1, itemValue+"%");
                ResultSet rs = null;    
                rs = ps.executeQuery();
                if (rs != null && rs.next())  {       
                    StringBuilder sbCenter = new StringBuilder();
                    
                    StringBuilder sbInfo = new StringBuilder();
                    sbInfo.append("\"<h4>").append(rs.getString(5)).append("</h4>");
                    sbInfo.append("Class : ").append(rs.getString(6)).append("<br />");
                    sbInfo.append("Floor : ").append(rs.getString(7)).append(" m<br />");
                    sbInfo.append("Ceiling : ").append(rs.getString(8)).append(" m\"");
                    
                    sbCenter.append("spaceCenter(").append(rs.getString(1)).append(",").append(rs.getString(2)).append(",");
                    sbCenter.append(rs.getString(3)).append(",").append(rs.getString(4)).append(",").append(sbInfo.toString()).append(")");
                    eng.executeScript(sbCenter.toString());                   
                } 
            } catch (Exception ex) {
                sbError = new StringBuilder(this.getClass().getName()+"."+Thread.currentThread().getStackTrace()[1].getMethodName());
                sbError.append("\r\n").append(ex.toString());
                sbError.append("\r\n").append(req);
                mylogging.log(Level.SEVERE, sbError.toString());  
                alertbox aError = new alertbox(myConfig.getLocale());
                aError.alertError(ex.getClass().getName() + ": " + ex.getMessage());  
            }                
        }
    }

    /**
     * Compute selection corners
     * @param sCoord   string like "45.4601, 5.6085, 46.0809, 6.0425" sended by webviewer
     * @return 
     */
    private boolean bboxSelection(String sCoord) {
        boolean res = false;            
            
        try {
            String[] arCoord = sCoord.split(",");
            if (arCoord.length == 4) {
                // To check validity of coordinates
                latMini = Double.parseDouble(arCoord[0].trim());
                latMaxi = Double.parseDouble(arCoord[2].trim());
                longMini = Double.parseDouble(arCoord[1].trim());
                longMaxi = Double.parseDouble(arCoord[3].trim());
                sLatMini = arCoord[0].trim();
                sLatMaxi = arCoord[2].trim();
                sLongMini = arCoord[1].trim();
                sLongMaxi = arCoord[3].trim();                
                
                res = true;
            }
        } catch (Exception ex) {
            sbError = new StringBuilder(this.getClass().getName()+"."+Thread.currentThread().getStackTrace()[1].getMethodName());
            sbError.append("\r\n").append(ex.toString());
            mylogging.log(Level.SEVERE, sbError.toString());  
            alertbox aError = new alertbox(myConfig.getLocale());
            aError.alertError(ex.getClass().getName() + ": " + ex.getMessage());  
        }
        
        return res;
    }
   
    private void resetVisuField() {
        try {
            Connection conn = currDbAir.getDbConn();                                                      
            String req = "UPDATE Zones SET Visu = ?";
            PreparedStatement ps = currDbAir.getDbConn().prepareStatement(req);
            ps.setString(1, "1");
            ps.executeUpdate();                                                
        } catch (Exception ex) {
            sbError = new StringBuilder(this.getClass().getName()+"."+Thread.currentThread().getStackTrace()[1].getMethodName());
            sbError.append("\r\n").append(ex.toString());
            mylogging.log(Level.SEVERE, sbError.toString());  
            alertbox aError = new alertbox(myConfig.getLocale());
            aError.alertError(ex.getClass().getName() + ": " + ex.getMessage());               
        }          
    }
    
    private void viewReset(boolean visuTree) {
        
        try {
            Connection conn = currDbAir.getDbConn();                                
            // drop view if it already exists
            Statement st1 = conn.createStatement();
            st1.execute("DROP VIEW IF EXISTS SELECT_VIEW");
            st1.close(); 
            String req = "UPDATE Zones SET Visu = ?";
            PreparedStatement ps = currDbAir.getDbConn().prepareStatement(req);
            ps.setString(1, "1");
            ps.executeUpdate();                                    
            dbView = false;
            if (visuTree) 
                buildTree();
        } catch (Exception ex) {
            sbError = new StringBuilder(this.getClass().getName()+"."+Thread.currentThread().getStackTrace()[1].getMethodName());
            sbError.append("\r\n").append(ex.toString());
            mylogging.log(Level.SEVERE, sbError.toString());  
            alertbox aError = new alertbox(myConfig.getLocale());
            aError.alertError(ex.getClass().getName() + ": " + ex.getMessage());               
        }            
        
    }
    
    private void decodeTitle(String titleContent) {
        if (actionDraw) {
            decodeDrawing(titleContent);
        } else {
            viewCreation(titleContent);
        }
        
    }
    
    private void decodePolygon(String sJson, int idx) {
        
        JSONParser parser = new JSONParser();    
        
        try{
           Object obj = parser.parse(sJson);
           JSONArray array = (JSONArray)obj;
           
           if (array.size() > 0) {
                StringBuilder sDP = new StringBuilder();           
                for (int i = 0; i < array.size(); i++) {
                    JSONObject objCoord = (JSONObject)array.get(i);    
                    position currPoint = new position();
                    double dLat = ((Number)objCoord.get("lat")).doubleValue();
                    double dLong = ((Number)objCoord.get("lng")).doubleValue();
                    currPoint.setLatitudeDd(dLat);
                    currPoint.setLongitudeDd(dLong);
                    sDP.append("DP ").append(String.format("%02d", currPoint.getLatDegres())).append(":");
                    sDP.append(String.format("%02d", currPoint.getLatMin_ms())).append(":");
                    sDP.append(String.format("%02d", (int) currPoint.getLatSec_ms())).append(" ").append(currPoint.getHemisphere());
                    sDP.append(" ").append(String.format("%03d", currPoint.getLongDegres())).append(":");
                    sDP.append(String.format("%02d", currPoint.getLongMin_ms())).append(":");
                    sDP.append(String.format("%02d", (int) currPoint.getLongSec_ms())).append(" ").append(currPoint.getMeridien()).append("\r\n");
                }
                String sName = i18n.tr("Polygon")+String.valueOf(idx);
                airdraw currElem = new airdraw(sName, sDP.toString());
                lsDraw.add(currElem);
           }
            
        } catch(ParseException pe) {	
            sbError = new StringBuilder(this.getClass().getName()+"."+Thread.currentThread().getStackTrace()[1].getMethodName());
            sbError.append("\r\n").append("ParseException : ").append(pe.toString()).append("\r\n");
            sbError.append("position: ").append(pe.getPosition());
            mylogging.log(Level.SEVERE, sbError.toString());  
            alertbox aError = new alertbox(myConfig.getLocale());
            aError.alertError(pe.getClass().getName() + ": " + pe.getMessage());              
        }          
    }

    private void decodeCircle(String sJson, int idx) {
        
        DecimalFormatSymbols decimalFormatSymbols = new DecimalFormatSymbols();
        decimalFormatSymbols.setDecimalSeparator('.');
        DecimalFormat radFormat = new DecimalFormat("##.##", decimalFormatSymbols);
        
        JSONParser parser = new JSONParser();      
		
        try{
            Object obj = parser.parse(sJson);
            JSONObject jsonObject = (JSONObject) obj;

            double radius = (double) jsonObject.get("rad");
            radius = radius/1852;    // Rayon en mètres qui devient des miles nautiques
                              
            JSONObject jsonCoord = (JSONObject) jsonObject.get("coord");
          
            position currPoint = new position();
            double dLat = ((Number)jsonCoord.get("lat")).doubleValue();
            double dLong = ((Number)jsonCoord.get("lng")).doubleValue();
            currPoint.setLatitudeDd(dLat);
            currPoint.setLongitudeDd(dLong);
            StringBuilder sDP = new StringBuilder();
            sDP.append("V X=").append(String.format("%02d", currPoint.getLatDegres())).append(":");
            sDP.append(String.format("%02d", currPoint.getLatMin_ms())).append(":");
            sDP.append(String.format("%02d", (int) currPoint.getLatSec_ms())).append(" ").append(currPoint.getHemisphere());
            sDP.append(" ").append(String.format("%03d", currPoint.getLongDegres())).append(":");
            sDP.append(String.format("%02d", currPoint.getLongMin_ms())).append(":");
            sDP.append(String.format("%02d", (int) currPoint.getLongSec_ms())).append(" ").append(currPoint.getMeridien()).append("\r\n");
            sDP.append("DC ").append(radFormat.format(radius)).append("\r\n");
            String sName = i18n.tr("Circle")+String.valueOf(idx);
            airdraw currElem = new airdraw(sName, sDP.toString());
            lsDraw.add(currElem);                                
          } catch(ParseException pe) {	 	
            sbError = new StringBuilder(this.getClass().getName()+"."+Thread.currentThread().getStackTrace()[1].getMethodName());
            sbError.append("\r\n").append("ParseException : ").append(pe.toString()).append("\r\n");
            sbError.append("position: ").append(pe.getPosition());
            mylogging.log(Level.SEVERE, sbError.toString());  
            alertbox aError = new alertbox(myConfig.getLocale());
            aError.alertError(pe.getClass().getName() + ": " + pe.getMessage());    
          }        
    }
    
    
    /**
     * After decoding coordinates
     * we must assign name, class, floor and ceiling  for each drawing airspace
     * @param shapeCoord 
     */
    private void decodeDrawing(String shapeCoord) {
        int cpt = 0;
        lsDraw.clear();
        if (shapeCoord != null && !shapeCoord.equals("")) {
            String[] simpleShape = shapeCoord.split("%");
            if (simpleShape.length > -1) {
                for (int i = 0; i < simpleShape.length; i++) {  
                    cpt++;
                    if (simpleShape[i].contains("[")) {
                        decodePolygon(simpleShape[i],cpt);
                    } else {
                        decodeCircle(simpleShape[i],cpt);
                    }
                }
            } 
        }
        if (lsDraw.size() > 0) {            
            winAirDraw winAD = new winAirDraw(i18n, lsDraw);        
        }
    }    
    
    private void viewCreation(String sCoord) {
        
        StringBuilder sbReq = new StringBuilder();
        
        if (bboxSelection(sCoord)) {
            try {
                Connection conn = currDbAir.getDbConn();                                
                // drop view if it already exists
                Statement st1 = conn.createStatement();
                st1.execute("DROP VIEW IF EXISTS SELECT_VIEW");
                st1.close();       
                // With PreparedStatement we had an error 
                // [SQLITE_ERROR] SQL error or missing database (parameters are not allowed in views)
                //sbReq.append("CREATE VIEW SELECT_VIEW AS SELECT Z_ID,Name, Geojson, Openair, Classe,Floor,Ceiling,Visu FROM Zones ");
                sbReq.append("CREATE VIEW SELECT_VIEW AS SELECT * FROM Zones ");
                sbReq.append("WHERE Visu = '1' AND (((LatMini >= ").append(sLatMini).append(" AND LatMini <= ").append(sLatMaxi).append(") ");
                sbReq.append("OR (LatMaxi >= ").append(sLatMini).append(" AND LatMaxi <= ").append(sLatMaxi).append(")) ");
                sbReq.append("AND ((LongMini >= ").append(sLongMini).append(" AND LongMini <= ").append(sLongMaxi).append(") ");
                sbReq.append("OR (LongMaxi >= ").append(sLongMini).append(" AND LongMaxi <= ").append(sLongMaxi).append(")))");                
                Statement st2 = conn.createStatement();
                st2.execute(sbReq.toString());
                st2.close();       
                dbView = true;
                buildTree();
            } catch (Exception ex) {
                sbError = new StringBuilder(this.getClass().getName()+"."+Thread.currentThread().getStackTrace()[1].getMethodName());
                sbError.append("\r\n").append(ex.toString());
                sbError.append("\r\n").append(sbReq.toString());
                mylogging.log(Level.SEVERE, sbError.toString());  
                alertbox aError = new alertbox(myConfig.getLocale());
                aError.alertError(ex.getClass().getName() + ": " + ex.getMessage());                   
            }
      
        }        
    }
            
    private void displaySearch() {
        
        winAirSearch wSearch = new winAirSearch(i18n); 
        if (wSearch.isRunSearch()) {
            String searchAirSp = wSearch.getSearchName();
            if (searchAirSp != null && !searchAirSp.equals("")) {
                visuSearch(searchAirSp);
            }
        }           
    }
    
    private void visuSearch(String searchArg) {
        List<Integer> listID = new ArrayList<>();
        
        String sReq = null;
        if (dbView) {
            sReq = "SELECT Z_ID FROM SELECT_VIEW  WHERE UPPER(Name) like ?";                    
        } else {
            sReq = "SELECT Z_ID FROM Zones WHERE UPPER(Name) like ?";
        }                     
        try {
            PreparedStatement ps = currDbAir.getDbConn().prepareStatement(sReq);
            ps.setString(1, "%"+searchArg+"%");
            ResultSet rs = null;    
            rs = ps.executeQuery();
            if (rs != null && rs.isBeforeFirst())  { 
                while (rs.next()) {  
                    listID.add(rs.getInt(1));
                }
            }
            if (listID.size() > 0) {
                // drop view if it already exists
                Connection conn = currDbAir.getDbConn();                                                
                Statement st1 = conn.createStatement();
                st1.execute("DROP VIEW IF EXISTS SELECT_VIEW");
                st1.close(); 
                dbView = false;
                // All records become hidden                
                String reqAll = "UPDATE Zones SET Visu = ?";
                PreparedStatement ps2 = currDbAir.getDbConn().prepareStatement(reqAll);
                ps2.setString(1, "0");
                ps2.executeUpdate();                     
                // search results become visible
                 String reqUpd = "UPDATE Zones SET Visu = ? WHERE Z_ID = ?";         
                for (int i = 0; i < listID.size(); i++) {
                    PreparedStatement ps3 = currDbAir.getDbConn().prepareStatement(reqUpd);
                    ps3.setString(1, "1");
                    ps3.setInt(2, listID.get(i));
                    ps3.executeUpdate();                      
                }   
                buildTree();                
            }
               
        } catch (Exception ex) {
            sbError = new StringBuilder(this.getClass().getName()+"."+Thread.currentThread().getStackTrace()[1].getMethodName());
            sbError.append("\r\n").append(ex.toString());
            mylogging.log(Level.SEVERE, sbError.toString());  
            alertbox aError = new alertbox(myConfig.getLocale());
            aError.alertError(ex.getClass().getName() + ": " + ex.getMessage());      
        }           
    }
    
    private boolean reversaleCheck() {
        
        boolean res = false;
        if (myConfig.getOS() == osType.LINUX) {
            alertbox aInfo = new alertbox(myConfig.getLocale());
            aInfo.alertInfo(i18n.tr("Reversale uploading not supported in Linux"));
        } else {
            usbRever = new reversale(myConfig.getOS(), myConfig.getGpsLimit());
            if (usbRever.isConnected()) {
                int airspOk = usbRever.airspaceReady(myConfig.getOS());
                if (airspOk != 0) {
                    alertbox aError = new alertbox(myConfig.getLocale());
                    aError.alertNumError(airspOk);
                } else {
                    res = true;
                }
            } else {
                alertbox aError = new alertbox(myConfig.getLocale());
                aError.alertNumError(206);    // Reversale non connecté
            } 
        }        
        return res;
    }
    
    private void reversaleTransfer() {
        
        int errorRever = -1;
        int res;
        String nameData = "FRDATA.bin";
        String nameIdx = "FRIDX.bin";
        
        try{    
            // First we create a backup of original OpenAir file on Reversale 
            String sBackupName = "Reverbin.txt";
            File fBackup = new File(usbRever.getfAirsp().getAbsolutePath()+File.separator+sBackupName);
            if (fBackup.exists()) fBackup.delete();
            exportFile(fBackup, true);
            // Generation fichier .bin
            // We work on hard disk because read/write operations are very slowly on Reversale
            // 1. New generation of OA file on hard disk
            String tmpOAFile = tempacess.getTemPath("Reverbin.txt");
            File fTmpOA = new File(tmpOAFile);
            if (fTmpOA.exists()) fTmpOA.delete();
            exportFile(fTmpOA, true);
            System.out.println(fTmpOA.getAbsolutePath());
            // 2. Temp path for FRDATA.bin and FRIDX.bin
            String tmpFRFiles = tempacess.getTemPath(null);        
            // 3. Old files are deleted
            String tmpFRData = tempacess.getTemPath(nameData);
            File fTmpData = new File(tmpFRData);
            if (fTmpData.exists()) fTmpData.delete();
            String tmpFRIdx = tempacess.getTemPath(nameIdx);
            File fTmpIdx = new File(tmpFRIdx);
            if (fTmpIdx.exists()) fTmpIdx.delete();            
            // http://labs.excilys.com/2012/06/26/runtime-exec-pour-les-nuls-et-processbuilder/
            // the author has serious doubts : ok only if program run correctly or crashes                    
            //System.out.println("Run "+usbRever.getPathReverbin()+"   p1 : "+fTmpOA.getAbsolutePath()+"  p2 : "+tmpFRFiles);
            // 4. generation of FRDATA.bin and FRIDX.bin
            Process p = Runtime.getRuntime().exec(new String[]{usbRever.getPathReverbin(),fTmpOA.getAbsolutePath(), tmpFRFiles});
            //Process p = Runtime.getRuntime().exec(pathModPoints);           
            BufferedReader error = getError(p);
            String ligne = "";     
            StringBuilder sbError = new StringBuilder();
            while ((ligne = error.readLine()) != null) {
                sbError.append(ligne);
            }  
            p.waitFor();
            res = p.exitValue();  // 0 if all is OK
            if (res == 0) {         
                if (fTmpData.exists()) {
                    if (fTmpIdx.exists()) {
                        // 5. FRDATA.bin and FRIDX.bin copied to Reversale
                        try {
                            CopyOption[] options = new CopyOption[]{
                              StandardCopyOption.REPLACE_EXISTING,
                              StandardCopyOption.COPY_ATTRIBUTES
                            };                             
                            File fRevData = new File(usbRever.getfAirsp().getAbsolutePath()+File.separator+nameData);
                            Files.copy(fTmpData.toPath(), fRevData.toPath(),options);
                            File fRevIdx = new File(usbRever.getfAirsp().getAbsolutePath()+File.separator+nameIdx);
                            Files.copy(fTmpIdx.toPath(), fRevIdx.toPath(),options);
                            errorRever = 214;     // Génération des fichiers FRDATA.bin et FRIDX.bin réussie
                        } catch (Exception e) {
                            errorRever = 212;   // Erreur pendant la copie de FRDATA.bin et FRIDX.bin
                        }                                
                    } else
                        errorRever = 208;  // FRIDX.bin non généré            
                } else {
                    errorRever = 210;    // FRDATA.bin non généré
                }              
            } else {
                errorRever = res;
            }           
        } catch (FileNotFoundException ex) {          
            errorRever = 1;
        } catch (IOException ex) {
            errorRever = 2;               
        } catch (InterruptedException ex) {
            errorRever = 6;            
        } catch (NullPointerException ex) {
            errorRever = 5;         
        } finally {
            comReversale = errorRever;
        }         
    }   
    
    private void reversaleResult() {
        alertbox aError = new alertbox(myConfig.getLocale());
        if (comReversale == 214)
            aError.alertInfo(i18n.tr("FRDATA. bin and FRIDX. bin generation successful"));   
        else
            aError.alertNumError(comReversale);         
    }
    
    private void reversaleCom() {
        ProgressForm pForm = new ProgressForm();

        Task<Void> task = new Task<Void>() {
            @Override
            public Void call() throws InterruptedException { 
                reversaleTransfer();
                return null ;
            }

        };
        // binds progress of progress bars to progress of task:
        pForm.activateProgressBar(task);

        // task is finished 
        task.setOnSucceeded(event -> {
            pForm.getDialogStage().close();
            reversaleResult();
        });

        pForm.getDialogStage().show();

        Thread thread = new Thread(task);
        thread.start();           
    }
    
    private BufferedReader getError(Process p) {
        return new BufferedReader(new InputStreamReader(p.getErrorStream()));
    }
    
    private void screenForDraw() { 
        eng.loadContent("");
        DrawScreen = true;
        mapPane.setVisible(true);
        mapPane.getItems().remove(componentsPane);         
        hbAction.setVisible(false);
        btReadFile.setVisible(false);
        btDraw.setVisible(false);
        btDrawCancel.setVisible(true);
        btDrawValid.setVisible(true);  
        updateInfo(false);
    }
        
    private void screenForTree() {    
        eng.loadContent("");
        mapPane.setDividerPosition(1, 0.3486);  
        mapPane.setVisible(true);
        hbAction.setVisible(true);
        btReadFile.setVisible(true);
        btDraw.setVisible(true);
        btDrawCancel.setVisible(false);
        btDrawValid.setVisible(false);        
    }
    
    private void screenCloseDraw() {           
        mapPane.getItems().add(0, componentsPane); 
        eng.loadContent("");
        mapPane.setVisible(false);        
        hbAction.setVisible(false);
        btReadFile.setVisible(true);
        btDraw.setVisible(true);
        btDrawCancel.setVisible(false);
        btDrawValid.setVisible(false);  
        updateInfo(false);            
    }
    
    
    private void fileMerge() {
        
        final FileChooserFx fileChooser = new FileChooserFxImpl();
        fileChooser.setShowHiddenFiles(false);
        fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("Open Air files (*.txt)", "*.txt", "*.TXT"));          
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
            replyFileMerge(sPath);
        });        
    }
    
    private void replyFileMerge(String strChooser) {
        alertbox aError = new alertbox(myConfig.getLocale());
        if (strChooser != null) {
            try {
                File selectedFile = new File(strChooser);            
                StringBuilder sbInfo = new StringBuilder();
                viewReset(true);
                currDbAir = new dbAirspace(selectedFile, currDbAir.getDbConn());
                if (currDbAir.isDbOK()) {
                    sbInfo.append(i18n.tr("Merge into memory successful")).append("    ");
                    sbInfo.append(String.valueOf(currDbAir.getNbAirspaces())).append(" ").append(i18n.tr("decoded airspaces"));
                    mainApp.rootLayoutController.updateMsgBar(sbInfo.toString(), true, 60);
                    dbView = false;
                    buildTree();            
                } else {
                    sbInfo.append(i18n.tr("Decoding problem"));
                    mainApp.rootLayoutController.updateMsgBar(sbInfo.toString(), true, 60);
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
    
    private void exportChoose() {
        final FileChooserFx fileChooser = new FileChooserFxImpl();
        fileChooser.setShowHiddenFiles(false);                                              
        fileChooser.setShowMountPoints(true);       
        fileChooser.setViewType(ViewType.List);
        fileChooser.setDividerPositions(.15, .30);
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
            replyExportChoose(sPath, ".txt");
        });          
    }        
    
    private void replyExportChoose(String strChooser, String formatExt) {
        int res = -1;
        alertbox aError = new alertbox(myConfig.getLocale());
        if (strChooser != null) {
            try {
                File save = new File(strChooser);  
                if (!save.getPath().toLowerCase().endsWith(formatExt)) { 
                    save = new File(save.getPath() + formatExt); 
                }
                exportFile(save, false);
            } catch (Exception ex) {
                sbError = new StringBuilder(this.getClass().getName()+"."+Thread.currentThread().getStackTrace()[1].getMethodName());
                sbError.append("\r\n").append(ex.toString());
                mylogging.log(Level.SEVERE, sbError.toString());  
                aError = new alertbox(myConfig.getLocale());
                aError.alertError(ex.getClass().getName() + ": " + ex.getMessage());                
            }                    
        } 
    }       
    
    private void exportFile(File pFile, boolean Silent) {
        
        int res = -1;
        StringBuilder sbExp = new StringBuilder();
        
        String sReq = null;
        if (dbView) {
            sReq = "SELECT Z_ID, Openair FROM SELECT_VIEW WHERE VISU = ? AND FLOOR <= ?";                    
        } else {
            sReq = "SELECT Z_ID, Openair FROM Zones WHERE VISU = ? AND FLOOR <= ?";
        }                     
        try {
            PreparedStatement ps = currDbAir.getDbConn().prepareStatement(sReq);
            ps.setString(1, "1");
            ps.setInt(2, levelFlight);
            ResultSet rs = null;    
            rs = ps.executeQuery();
            if (rs != null && rs.isBeforeFirst())  { 
                SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
                String dayDate = sdf.format(new java.util.Date());
                sbExp.append("**********************************************************************\r\n");
                sbExp.append("*                                                                    *\r\n");
                sbExp.append("*                         OPEN AIR FILE                              *\r\n");
                sbExp.append("*                      Generated by LOGFLY                           *\r\n");
                sbExp.append("*                            ").append(dayDate).append("                              *\r\n");
                sbExp.append("*                                                                    *\r\n");
                sbExp.append("**********************************************************************\r\n\r\n");             
                while (rs.next()) {  
                    sbExp.append(rs.getString(2)).append("\r\n");
                }
                try {
                    FileWriter fileWriter = null;
                    fileWriter = new FileWriter(pFile);
                    fileWriter.write(sbExp.toString());
                    fileWriter.close();
                    res = 0;
                } catch (IOException ex) {
                    res = 2;
                }             
                if (!Silent) {
                    alertbox finOp = new alertbox(myConfig.getLocale());
                    finOp.alertNumError(res); 
                }
            }      
        } catch (Exception ex ) {
            sbError = new StringBuilder(this.getClass().getName()+"."+Thread.currentThread().getStackTrace()[1].getMethodName());
            sbError.append("\r\n").append(ex.toString());
            mylogging.log(Level.SEVERE, sbError.toString());  
            alertbox aError = new alertbox(myConfig.getLocale());
            aError.alertError(ex.getClass().getName() + ": " + ex.getMessage());   
        }                 
    }
    
    
    /**
     * Is called by the main application to give a reference back to itself.
     * 
     * @param mainApp
     */
    public void setMainApp(Main mainApp) {
        this.mainApp = mainApp; 
        myConfig = mainApp.myConfig;
        i18n = I18nFactory.getI18n("","lang/Messages",AirspaceController.class.getClass().getClassLoader(),myConfig.getLocale(),0);        
        this.mainApp.rootLayoutController.updateMsgBar("", false, 50);
        winTraduction();  
        airStage.setOnHiding( event -> {
            rootController.switchMenu(1);
            rootController.mainApp.showCarnetOverview();
        });  
    } 
    
    public void setWinMax()  {           
        airStage.setMaximized(true);
    }

    /**
     * Sets the stage of this Viewer.
     *
     * @param pAirStage
     */
    public void setAirStage(Stage pAirStage) {
        this.airStage = pAirStage;
    }      
    
    /**
     * Set a communication bridge with RootViewController 
     * @param callExterne 
     */
    public void setRootBridge(RootLayoutController callRoot)  {
        this.rootController = callRoot;     
    }     
    
    private void winTraduction() {

        btReadFile.setText(i18n.tr("OpenAir"));
        btDraw.setText(i18n.tr("Draw"));
        btDrawCancel.setText(i18n.tr("Cancel"));
        btDrawValid.setText(i18n.tr("Save"));
        btRefresh.setText(i18n.tr("Update"));
        btReset.setText(i18n.tr("Reset"));  
        btWrite.setText(i18n.tr("Save"));
    }        
    
}
