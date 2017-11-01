/* 
 * Copyright Gil THOMAS
 * This file forms an integral part of Logfly project
 * See the LICENSE file distributed with source code
 * for details of Logfly licence project
 */
package controller;

import model.Carnet;
import dialogues.alertbox;
import dialogues.dialogbox;
import igc.pointIGC;
import java.awt.Desktop;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.web.WebView;
import org.xnap.commons.i18n.I18n;
import org.xnap.commons.i18n.I18nFactory;
import trackgps.traceGPS;
import leaflet.map_pm;
import settings.configProg;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javax.imageio.ImageIO;
import kml.makingKml;
import leaflet.map_markers;
import leaflet.map_visu;
import littlewins.winComment;
import littlewins.winPhoto;
import littlewins.winPoints;
import littlewins.winTrackFile;
import Logfly.Main;
import igc.mergingIGC;
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.Connection;
import static java.time.LocalDateTime.now;
import java.time.Period;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Level;
import javafx.collections.transformation.SortedList;
import javafx.geometry.Rectangle2D;
import javafx.scene.control.SelectionMode;
import javafx.stage.Screen;
import littlewins.winGlider;
import model.Gpsmodel;
import photo.imgmanip;
import settings.osType;
import systemio.mylogging;
import systemio.textio;
import systemio.webio;
import trackgps.scoring;

/**
 * FXML Controller class
 *
 * @author Gil Thomas logfly.org
 */
public class CarnetViewController  {

    @FXML
    private TableView<Carnet> tableVols;
    @FXML
    private TableColumn<Carnet, String> imgCol;
   // @FXML
   // private TableColumn<Carnet, Image> imgCol;
    @FXML
    private TableColumn<Carnet, String> dateCol;
    @FXML
    private TableColumn<Carnet, String> heureCol;
    @FXML
    private TableColumn<Carnet, String> dureeCol;
    @FXML
    private TableColumn<Carnet, String> siteCol;        
    @FXML
    private TableColumn<Carnet, String> voileCol;      
    @FXML
    private Button btnStat;        
    @FXML
    private Button btnMap; 
    @FXML
    private Button btnScore;      
    @FXML
    private Button btnGEarth;      
    @FXML
    private ChoiceBox top_chbYear;    
    @FXML
    private ImageView top_Menu;    
    @FXML
    private ImageView top_Visu_Menu;
    @FXML
    private WebView mapViewer;
    
    // Reference to the main application.
    private Main mainApp;
    
    // Localization
    private I18n i18n; 
    
    // Settings
    configProg myConfig;
    
    //START | SQLITE
    private static Statement stat;
    private PreparedStatement prep;
    //END | SQLITE
    
    traceGPS currTrace=null;    
    
    private StringBuilder sbError;

    private ObservableList <Carnet> dataCarnet; 
    private ObservableList <String> dataYear; 
    
    @FXML
    private void initialize() {
        // We need to intialize i18n before TableView building
        // For this reason we put building code in iniTable() 
        // This procedure will be called after setMainApp()                
        
    }
    
    /**
     * Initialization of the TableView
     * Fill the table with data from db
     */
    private void iniTable() {
        dataCarnet = FXCollections.observableArrayList();
        dataYear = FXCollections.observableArrayList();
        
        tableVols.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
                       
        imgCol.setCellValueFactory(new PropertyValueFactory<Carnet, String>("camera"));
        dateCol.setCellValueFactory(new PropertyValueFactory<Carnet, String>("date"));
        heureCol.setCellValueFactory(new PropertyValueFactory<Carnet, String>("heure"));
        dureeCol.setCellValueFactory(new PropertyValueFactory<Carnet, String>("duree"));
        siteCol.setCellValueFactory(new PropertyValueFactory<Carnet, String>("site"));     
        voileCol.setCellValueFactory(new PropertyValueFactory<Carnet, String>("engin"));     
        
        // Try to change look with value of Site column
        dateCol.setCellFactory(column -> {
            return new TableCell<Carnet, String>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);  // obligatoire
                    if (item == null || empty) {
                        setText(null);
                        setStyle("");
                    } else {
                        setText(item); // Fill cell with the string
                        // If needing, we can change all informations of the table
                        Carnet currLigne = getTableView().getItems().get(getIndex());  
                        if (currLigne.getComment()) {                       
                            setTextFill(Color.CORAL); //The text in red                            
                        } else {
                            setTextFill(Color.BLACK);
                            setStyle("");
                        }
                    }
                }
            };
        });
        
        imgCol.setCellFactory(column -> {
            TableCell<Carnet, String> cell = new TableCell<Carnet, String>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty) ;
                    String imgPath;
                    if (item == null || empty) {                                               
                            imgPath ="/images/Nocamera.png";                      
                    } else {    
                            Carnet currLigne = getTableView().getItems().get(getIndex());                         
                            if (currLigne.getPhoto()) {                                 
                                    imgPath = "/images/Camera.png";
                            } else {  
                                    imgPath = "/images/Nocamera.png";
                            }                              
                    }
                    Image img = new Image(imgPath);
                    ImageView imgView = new ImageView(img);
                    VBox vb = new VBox();
                    vb.setAlignment(Pos.CENTER);                        
                    vb.getChildren().addAll(imgView);
                    setGraphic(vb);   
                }
            };
            cell.setOnMouseClicked(e -> {
                if (tableVols.getSelectionModel().getSelectedItem().getPhoto()) {
                    String sPhoto = tableVols.getSelectionModel().getSelectedItem().getCamera();
                    imgmanip currImage = new imgmanip();
                    Image dbImage = currImage.strToImage(sPhoto, 700, 700);            
                    if (dbImage != null) {
                        winPhoto myPhoto = new winPhoto();    
                        myPhoto.showDbPhoto(dbImage);
                    }
                }
            });
            return cell ;
        });
        
        Statement stmt = null;
        ResultSet rsYear = null;                
        try {
            stmt = myConfig.getDbConn().createStatement();                        
            // We search years in the logbook
            rsYear = stmt.executeQuery("SELECT strftime('%Y',V_date) FROM Vol GROUP BY strftime('%Y',V_date) ORDER BY strftime('%Y',V_date) DESC");
            if (rsYear != null)  {             
                while (rsYear.next()) {
                    dataYear.add(rsYear.getString(1));
                }
                // Year choicebox initialization
                top_chbYear.setItems(dataYear);
                top_chbYear.getSelectionModel().select(0);
                top_chbYear.setOnAction((event) -> {
                    String selectedYear = (String) top_chbYear.getSelectionModel().getSelectedItem();
                    newVolsContent(selectedYear);
                });
                // Most recent year
                String yearFiltre = (String) top_chbYear.getSelectionModel().getSelectedItem();
                
                // Listener for line changes and  display relevant details
                tableVols.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> showCarnetDetails((Carnet) newValue));   
                
                newVolsContent(yearFiltre);
                                                                                             
            }

        } catch ( Exception e ) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(i18n.tr("Pas de vols dans le carnet"));            
            String s = e.getClass().getName() + ": " + e.getMessage();
            alert.setContentText(s);
            alert.showAndWait();                      
            System.exit(0);          
        }  finally {
            try{
                rsYear.close(); 
                stmt.close();
            } catch(Exception e) { } 
        }
        
    }
    
    /**
     * Initailization of top barmenu
     */
    private void iniEventBar() {
        
        top_Menu.addEventHandler(MouseEvent.MOUSE_CLICKED,
            new EventHandler<MouseEvent>() {
                @Override public void handle(MouseEvent e) {                        
                        clicTop_Menu().show(top_Menu, e.getScreenX(), e.getScreenY());
                }
        });    
        top_Visu_Menu.addEventHandler(MouseEvent.MOUSE_CLICKED,
            new EventHandler<MouseEvent>() {
                @Override public void handle(MouseEvent e) { 
                    if (myConfig.isVisuGPSinNav()) {
                        runVisuGPS(true);
                    } else {
                        clicTop_VisuMenu().show(top_Visu_Menu, e.getScreenX(), e.getScreenY());
                    }
                }
        });         
    }
    
    private void newVolsContent(String yearFiltre)  {
        boolean isCamera;
        Statement stmt = null;
        ResultSet rs = null;
        String sReq = "SELECT * from Vol WHERE V_Date >= '"+yearFiltre+"-01-01 00:01' AND V_Date <= '"+yearFiltre+"-12-31 23:59' ORDER BY V_Date DESC";
        try {
            stmt = myConfig.getDbConn().createStatement();
            rs = stmt.executeQuery(sReq);
            if (rs != null)  { 
                tableVols.getItems().clear();
                while (rs.next()) {
                    if (rs.getString("V_Photos") != null && !rs.getString("V_Photos").equals(""))
                        isCamera = true;
                    else
                        isCamera = false;
                    Carnet ca = new Carnet();                    
                    ca.setIdVol(rs.getString("V_ID"));
                    ca.setDate(rs.getString("V_Date"));
                    ca.setHeure(rs.getString("V_Date"));
                    ca.setDuree(rs.getString("V_sDuree"));
                    ca.setSite(rs.getString("V_Site"));  
                    ca.setAltiDeco(rs.getString("V_AltDeco")); 
                    ca.setLatDeco(rs.getString("V_LatDeco")); 
                    ca.setLongDeco(rs.getString("V_LongDeco")); 
                    ca.setEngin(rs.getString("V_Engin"));
                    if (rs.getString("V_Commentaire") != null && !rs.getString("V_Commentaire").equals("")) {
                        ca.setComment(true);
                        ca.setComTexte(rs.getString("V_Commentaire"));
                    } else {
                        ca.setComment(false);
                    }
                    ca.setPhoto(isCamera);
                    if (isCamera) 
                        ca.setCamera(rs.getString("V_Photos"));     
                    else 
                        ca.setCamera(null);
                    dataCarnet.add(ca);                
                }            
                tableVols.setItems(dataCarnet); 
                // At least one record
                if (tableVols.getItems().size() > 0)
                    tableVols.getSelectionModel().select(0);
            }
            
        } catch ( Exception e ) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(i18n.tr("Pas de vols dans le carnet"));            
            String s = e.getClass().getName() + ": " + e.getMessage();
            alert.setContentText(s);
            alert.showAndWait();                      
            System.exit(0);          
        } finally {
            try{
                rs.close(); 
                stmt.close();
            } catch(Exception e) { } 
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
        i18n = I18nFactory.getI18n("","lang/Messages",CarnetViewController.class.getClass().getClassLoader(),myConfig.getLocale(),0);
        winTraduction();
        iniTable();
        iniEventBar();                       
    }
    
    /**
     * Delete a flight in the logbook
     */
    private void supprimeVol() {
        PreparedStatement pstmt = null;
        
        int selectedIndex = tableVols.getSelectionModel().getSelectedIndex();
        if (selectedIndex >= 0) {
            Carnet selectedVol = tableVols.getSelectionModel().getSelectedItem();
            dialogbox dConfirm = new dialogbox();
            StringBuilder sbMsg = new StringBuilder(); 
            sbMsg.append(selectedVol.getDate()).append(" ").append(i18n.tr("Durée")).append(" : ").append(selectedVol.getDuree());                 
            if (dConfirm.YesNo(i18n.tr("Suppression du vol"), sbMsg.toString()))   {                
                String sReq = "DELETE FROM Vol WHERE V_ID = ?";
                try {
                    pstmt = myConfig.getDbConn().prepareStatement(sReq);
                    pstmt.setInt(1, Integer.valueOf(selectedVol.getIdVol()));
                    pstmt.executeUpdate();    
                    tableVols.getItems().remove(selectedIndex);
                    pstmt.close();
                } catch (Exception e) {
                    alertbox aError = new alertbox(myConfig.getLocale());
                    aError.alertError(e.getMessage());                                                           
                }                                                
            }                                 
        } else {
            // no flight selected
            alertbox aError = new alertbox(myConfig.getLocale());
            aError.alertError(i18n.tr("Aucun vol sélectionné..."));                       
        }        
    }
    
    /**
     * Extract and decode the track from the logbook
     * @param idVol 
     */
    private void decodeVolCarnet(String idVol)  {
        
        Image dbImage = null;
        Statement stmt = null;
        ResultSet rs = null;
        
        String sReq = "SELECT V_IGC,UTC,V_Date,V_Duree,V_sDuree,V_AltDeco,V_LatDeco,V_LongDeco,V_Site,V_Engin,V_Pays,V_Commentaire,V_Photos,V_League,V_Score FROM Vol WHERE V_ID = "+idVol;
        try {
            stmt = myConfig.getDbConn().createStatement();
            rs =  stmt.executeQuery(sReq);
            if (rs != null)  { 
                if (rs.getString("V_IGC") != null && !rs.getString("V_IGC").equals(""))  {                        
                    currTrace = new traceGPS(rs.getString("V_IGC"),"",true, myConfig);   // String pFichier, String pType, String pPath
                    if (currTrace.isDecodage()) {
                        // Like in xLogfly we put glider and site
                        if (!rs.getString("V_Engin").equals(currTrace.getsVoile()))
                            currTrace.setsVoile(rs.getString("V_Engin"));
                        currTrace.setsSite(rs.getString("V_Site"));
                        // in db, if V_League is null, rs.getInt("V_League") = 0                        
                        // For xLogfly compatibility, FR league FR is zero 
                        // therfore we need to test JSON presence                    
                        String sJSON = rs.getString("V_Score");                                               
                        if (sJSON != null && sJSON.contains("drawScore")) {
                            currTrace.setScore_Idx_League(rs.getInt("V_League"));
                            currTrace.setScore_JSON(rs.getString("V_Score"));
                            scoring currScore = new scoring(0, myConfig);
                            int resScoring = currScore.decodeStrJson(currTrace);
                            if (resScoring == 0)
                                currTrace.setScored(true);
                            else {
                                alertbox aError = new alertbox(myConfig.getLocale());
                                aError.alertNumError(resScoring); 
                            }
                        } else {
                            currTrace.setScored(false);
                        }
                        // is there a comment ?
                        String sComment = rs.getString("V_Commentaire");
                        if (sComment != null) {
                            currTrace.setComment(sComment);
                        }
                        // is ther a photo ?
                        String sPhoto = rs.getString("V_Photos");
                        if (sPhoto != null) {
                            if (myConfig.isPhotoAuto()) {
                                // Pour affichage dans une fenêtre externe
                                imgmanip currImage = new imgmanip();
                                dbImage = currImage.strToImage(sPhoto, 700, 700);  
                            }
                        }
                        map_pm visuMap = new map_pm(currTrace, true, myConfig.getIdxMap(), i18n); 
                        if (visuMap.isMap_OK()) {                              
                            // Delete cache for navigate back
                            mapViewer.getEngine().load("about:blank");
                            /** ----- Debut Debug --------*/ 
                            String sDebug = visuMap.getMap_HTML();
                            final Clipboard clipboard = Clipboard.getSystemClipboard();
                            final ClipboardContent content = new ClipboardContent();
                            content.putString(visuMap.getMap_HTML());            
                            clipboard.setContent(content);
                            mapViewer.getEngine().loadContent(visuMap.getMap_HTML()); 
                            if (dbImage != null) {
                                winPhoto myPhoto = new winPhoto();    
                                myPhoto.showDbPhoto(dbImage);
                            }

                        }
                    }  else {
                        alertbox decodageError = new alertbox(myConfig.getLocale());
                        decodageError.alertError(i18n.tr("Problème de décodage du fichier"));
                    }     
                } else {
                    // No track to display
                    displayNoIGC(rs);
                }
            }
        } catch ( Exception e ) {
            alertbox aError = new alertbox(myConfig.getLocale());
            aError.alertError(e.getClass().getName() + ": " + e.getMessage());                          
        } finally {
            try{
                rs.close(); 
                stmt.close();
            } catch(Exception e) { } 
        }
        
        
    }
    
    /**
     * Flight without track
     * @param rs
     * @throws SQLException 
     */
    private void displayNoIGC(ResultSet rs) throws SQLException {
        Image dbImage = null;
                
        currTrace = new traceGPS(null,"",true, myConfig);  
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");       
        currTrace.setDate_Vol(LocalDateTime.parse(rs.getString("V_Date"), formatter)); 
        currTrace.setDT_Deco(LocalDateTime.parse(rs.getString("V_Date"), formatter));          
        long lDuree = rs.getLong("V_Duree");
        currTrace.setDuree_Vol(rs.getLong("V_Duree")); 
        currTrace.setsDuree_Vol(rs.getString("V_sDuree")); 
        currTrace.setAlt_Deco_Baro(rs.getInt("V_AltDeco")); 
        currTrace.setAlt_Deco_GPS(rs.getInt("V_AltDeco")); 
        currTrace.setDT_Attero(currTrace.getDate_Vol().plusSeconds(lDuree));       
        currTrace.setLatDeco(rs.getDouble("V_LatDeco"));
        currTrace.setLongDeco(rs.getDouble("V_LongDeco"));
        currTrace.setsSite(rs.getString("V_Site"));
        currTrace.setsVoile(rs.getString("V_Engin"));
        // Est ce qu'il y avait un commentaire ?
        String sComment = rs.getString("V_Commentaire");
        if (sComment != null) {
            currTrace.setComment(sComment);
        }
        // Is ther a photo ?
        String sPhoto = rs.getString("V_Photos");
        if (sPhoto != null) {            
            if (myConfig.isPhotoAuto()) {
                // Pour affichage dans une fenêtre externe
                imgmanip currImage = new imgmanip();
                dbImage = currImage.strToImage(sPhoto, 700, 700);  
            }
        }
        // Map settings initialization
        // No track, we put only a marker in takeoff
        pointIGC pPoint1 = new pointIGC();
        double dLatitude = currTrace.getLatDeco();
        if (dLatitude > 90 || dLatitude < -90) dLatitude = 0;
        pPoint1.setLatitude(dLatitude);
        double dLongitude = currTrace.getLongDeco();
        if (dLongitude > 180 || dLongitude < -180) dLongitude = 0;
        pPoint1.setLongitude(dLongitude);
        pPoint1.setAltiGPS(currTrace.getAlt_Deco_GPS());
        StringBuilder sbComment = new StringBuilder();
        sbComment.append(currTrace.getsSite()).append("<BR>");
        sbComment.append(i18n.tr("Altitude")).append(" : ").append(String.valueOf(pPoint1.AltiGPS)).append(" m" );
        pPoint1.Comment = sbComment.toString();
  
        map_markers mapNoIGC = new map_markers(i18n, myConfig.getIdxMap());
        mapNoIGC.getPointsList().add(pPoint1);
        mapNoIGC.setStrComment(currTrace.getComment());
        if (mapNoIGC.genMap() == 0) {
            /** ----- Begin Debug --------*/ 
            String sDebug = mapNoIGC.getMap_HTML();
            final Clipboard clipboard = Clipboard.getSystemClipboard();
            final ClipboardContent content = new ClipboardContent();
            content.putString(mapNoIGC.getMap_HTML()); 
            System.out.println("C'est dans le clipboard...");
            clipboard.setContent(content);
            /** ----- End Debug ---------*/
            // Delete cache for navigate back
            mapViewer.getEngine().load("about:blank");            
            mapViewer.getEngine().loadContent(mapNoIGC.getMap_HTML()); 
            if (dbImage != null) {
                winPhoto myPhoto = new winPhoto();    
                myPhoto.showDbPhoto(dbImage);
            }            
        }                
    }
    
    /**
     * Track export
     */
    private void exportTrace() {
        int res = -1;
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter(i18n.tr("Format IGC"), "*.igc"),
                new FileChooser.ExtensionFilter(i18n.tr("Format GPX"), "*.gpx")
        );              
        File selectedFile = fileChooser.showSaveDialog(null);        
        if(selectedFile != null){
            String selExt = systemio.textio.getFileExtension(selectedFile);
            selExt = selExt.toUpperCase();
            switch (selExt) {
                case "IGC":
                    try {
                        FileWriter fileWriter = null;
                        fileWriter = new FileWriter(selectedFile);
                        fileWriter.write(currTrace.getFicIGC());
                        fileWriter.close();
                        res = 0;
                    } catch (IOException ex) {
                        res = 2;
                    }
                    break;
                case "GPX":
                    break;
            }
            alertbox finOp = new alertbox(myConfig.getLocale());
            finOp.alertNumError(res);
        }        
    }
    
    /**
     * Display details of a flight
     * @param currCarnet 
     */
    private void showCarnetDetails(Carnet currCarnet) {
        if (currCarnet != null) {                                   
            decodeVolCarnet(currCarnet.getIdVol());
                        
//            top_Menu.addEventHandler(MouseEvent.MOUSE_CLICKED,
//                new EventHandler<MouseEvent>() {
//                    @Override public void handle(MouseEvent e) {                        
//                            clicTop_Menu().show(top_Menu, e.getScreenX(), e.getScreenY());
//                    }
//            });    
//            top_Visu_Menu.addEventHandler(MouseEvent.MOUSE_CLICKED,
//                new EventHandler<MouseEvent>() {
//                    @Override public void handle(MouseEvent e) { 
//                        if (myConfig.isVisuGPSinNav()) {
//                            runVisuGPS(true);
//                        } else {
//                            clicTop_VisuMenu().show(top_Visu_Menu, e.getScreenX(), e.getScreenY());
//                        }
//                    }
//            }); 
        } else {
            // todo
           
        }              
    }
                  
    /**
     * Adding Context Menus, last paragraph
    *     http://docs.oracle.com/javafx/2/ui_controls/menu_controls.htm    
    */
    private ContextMenu clicTop_Menu()   {
        final ContextMenu cm = new ContextMenu();
        
        MenuItem cmItem0 = new MenuItem(i18n.tr("Photo du jour"));        
        cmItem0.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent e) {
                gestionPhoto();
            }            
        });
        cm.getItems().add(cmItem0);
        
        MenuItem cmItem1 = new MenuItem(i18n.tr("Commentaire"));
        cmItem1.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent e) {
               gestionComment();
            }
        });
        cm.getItems().add(cmItem1);
        
        MenuItem cmItemSup = new MenuItem(i18n.tr("Supprimer"));        
        cmItemSup.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent e) {
                supprimeVol();
            }
        });
        cm.getItems().add(cmItemSup);
        
        MenuItem cmItemLp = new MenuItem(i18n.tr("Liste points"));
        cmItemLp.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent e) {
                winPoints myGrid = new winPoints(myConfig.getLocale());    
                myGrid.showTablePoints(currTrace);
            }
        });
        cm.getItems().add(cmItemLp);
        
        MenuItem cmItemEx = new MenuItem(i18n.tr("Exporter"));
        cmItemEx.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent e) {
                exportTrace();
            }
        });
         cm.getItems().add(cmItemEx);
        
        MenuItem cmItemFic = new MenuItem(i18n.tr("Fichier trace"));
        cmItemFic.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent e) {
                winTrackFile myTrace = new winTrackFile(currTrace.getFicIGC());            
            }
        });
        cm.getItems().add(cmItemFic);
        
        MenuItem cmItemGlider = new MenuItem(i18n.tr("Modifier la voile"));
        cmItemGlider.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent e) {
                changeGlider();
            }
        });
        cm.getItems().add(cmItemGlider);
        
        MenuItem cmItemMerging = new MenuItem(i18n.tr("Fusionner les vols"));
        cmItemMerging.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent e) {
                askMergingIGC();
            }
        });
        cm.getItems().add(cmItemMerging);
        /**
         * Missing items :
         * Fusionne les vols
         * Modifier le décalage UTC        ? pertinent ou alors pour vieille compatibilitité
         * Fiche site
         * Actualisation Site
         * Attribution site
         * Site différent
         * Relocaliser le site
         */
        
        return cm;
    }
    
    private ContextMenu clicTop_VisuMenu()   {
        final ContextMenu cm = new ContextMenu();
        
        MenuItem cmItem0 = new MenuItem(i18n.tr("Visu GPS interne"));        
        cmItem0.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent e) {
                runVisuGPS(false);
            }            
        });
        cm.getItems().add(cmItem0);
        
        MenuItem cmItem1 = new MenuItem(i18n.tr("VisuGPS navigateur"));
        cmItem1.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent e) {
               runVisuGPS(true);
            }
        });
        cm.getItems().add(cmItem1);
        
        return cm;
    }
        
    
    private ObservableList<String> listGliders() {
        ObservableList<String> lsGliders = FXCollections.observableArrayList();       
        Statement stmt = null;
        ResultSet rs = null;
        
        try {
            stmt = myConfig.getDbConn().createStatement();                       
            rs = stmt.executeQuery("SELECT V_Engin,Count(V_ID) FROM Vol GROUP BY upper(V_Engin)");
            if (rs != null)  {             
                while (rs.next()) {
                    String gl = rs.getString(1);
                    if (gl != null && !gl.isEmpty() && !gl.equals("null")) {
                        lsGliders.add(gl);
                    }
                }
            }
        } catch (Exception e) {
            alertbox aError = new alertbox(myConfig.getLocale());
            aError.alertError(e.getMessage());                 
        } finally {
            try{
                rs.close(); 
                stmt.close();
            } catch(Exception e) { } 
        }
        
        return lsGliders;
    } 
    
    // pop combo https://stackoverflow.com/questions/27608380/populate-a-combobox-from-an-array-list-populated-from-an-sql-statment
    
    private void changeGlider() {
        ObservableList<String> lsGliders = listGliders();
                
        winGlider chgeGlider = new winGlider(lsGliders, myConfig.getDefaultPilote(), i18n); 
        if (chgeGlider.isModif()) {
            System.out.println(chgeGlider.getwPilot()+" "+chgeGlider.getwGlider());
            String strPilot = chgeGlider.getwPilot();
            String strGlider = chgeGlider.getwGlider();
            ObservableList<Carnet> selFlights = tableVols.getSelectionModel().getSelectedItems();         
            PreparedStatement pstmt = null;
            for(Carnet flight : selFlights){               
                try {           
                    String sReq = "UPDATE Vol SET V_Engin= ? WHERE V_ID = ?";   
                    pstmt = myConfig.getDbConn().prepareStatement(sReq);
                    pstmt.setString(1,strGlider); 
                    pstmt.setInt(2, Integer.valueOf(flight.getIdVol()));
                    pstmt.executeUpdate();                      
                    flight.setEngin(strGlider);
                } catch (Exception e) {
                    alertbox aError = new alertbox(myConfig.getLocale());
                    aError.alertError(e.getMessage()); 
                } finally {
                    try{                    
                        pstmt.close();
                    } catch(Exception e) { } 
                }        
            }
            tableVols.refresh();
        }        
    }
    
    private void askMergingIGC() {
        dialogbox dConfirm = new dialogbox();                        
        if (dConfirm.YesNo("",i18n.tr("Fusionner les traces IGC ?")))   {    
            mergeIGC();
        }
    }
    
    private void mergeIGC()  {
        int nbVol = 0;
        String sImage = null;
        long totTime = 0;
        int idRest = -1;
        LocalDateTime ldtRef = null;
        int sameDay = -1;
        LocalDateTime ldtFirst = now();
        DateTimeFormatter formatterSQL = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"); 
        
        mergingIGC mgIGC = new mergingIGC();
        
        ObservableList<Carnet> selFlights = tableVols.getSelectionModel().getSelectedItems();    
        /*
        * We must use a buffer because selFlights is NOT modifiable
        * When we tried a sort with the comparator the code throws
        * an exception ...java.lang.UnsupportedOperationException... Unmodifiable...
        * we create a modifiable list before sorting
        * https://stackoverflow.com/questions/21854353/why-does-collections-sort-throw-unsupported-operation-exception-while-sorting-by
        */
        List<Carnet> selBuffer = new ArrayList<Carnet>(selFlights);

        Comparator<? super Carnet> comparatorDate = new Comparator<Carnet>() {
            @Override
            public int compare(Carnet c1, Carnet c2) {
                return c1.getHeure().compareTo(c2.getHeure());
            }
        };                   
        Collections.sort(selBuffer, comparatorDate);
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        Statement stmt = null;
        for(Carnet flight : selBuffer){                 
            try { 
                String sReq = "SELECT * FROM Vol WHERE V_ID = "+flight.getIdVol();                
                stmt = myConfig.getDbConn().createStatement();
                rs = stmt.executeQuery(sReq);                
                if (rs.next()) {
                    LocalDateTime ldtFromDb = LocalDateTime.parse(rs.getString("V_Date"), formatterSQL); 
                    if (ldtRef == null) {
                        ldtRef = ldtFromDb;
                        sameDay = 0;
                    } else {
                        Period diffDays = Period.between(ldtRef.toLocalDate(), ldtFromDb.toLocalDate()); 
                        sameDay = diffDays.getYears()+diffDays.getMonths()+diffDays.getDays();
                    }
                    if (sameDay == 0) {
                        nbVol++;
                        totTime = totTime+rs.getLong("V_Duree");
                        mgIGC.extractBRec(rs.getString("V_IGC"));
                        String dbImage =rs.getString("V_Photos");
                        if (dbImage != null && !dbImage.isEmpty() && !dbImage.equals("null")) {
                            sImage = dbImage;
                        } else {
                            sImage = "";
                        }

                        if (ldtFromDb.isBefore(ldtFirst)) {
                            ldtFirst = ldtFromDb;
                            idRest = rs.getInt("V_ID");
                        }
                    } else {
                        alertbox aError = new alertbox(myConfig.getLocale());
                        aError.alertNumError(1110);   // Flights are not of on the same day 
                        return;
                    }
                }
            } catch (Exception e) {
                alertbox aError = new alertbox(myConfig.getLocale());
                aError.alertError(e.getMessage()); 
            } finally {
                try{     
                    rs.close();
                    pstmt.close();
                } catch(Exception e) { } 
            }        
        }        
        if (nbVol > 1) {
            String totIGC = mgIGC.getTotIGC();
            Connection conn = null;
            PreparedStatement pstmtDel = null;
            PreparedStatement pstmtUpd = null;
            try {
                conn = myConfig.getDbConn();
                if(conn == null)
                    return;
                conn.setAutoCommit(false);   
                // delete flights
                for(Carnet flight : selBuffer){  
                    if (idRest != Integer.valueOf(flight.getIdVol())) {
                        String sReq = "DELETE FROM Vol WHERE V_ID = ?";                        
                        pstmtDel = conn.prepareStatement(sReq);
                        pstmtDel.setInt(1, Integer.valueOf(flight.getIdVol()));
                        pstmtDel.executeUpdate();                                    
                    }
                }
                // We update the flight kept in logbook
                int h,mn;                
                String sQuote ="'";
                StringBuilder sbReq = new StringBuilder();
                h = (int) totTime/3600;
                mn = (int) (totTime - (h*3600))/60;
                //  V_Duree, V_sDuree,V_Commentaire must be updated                
                sbReq.append("UPDATE Vol SET V_Duree=").append(sQuote).append(String.valueOf(totTime)).append(sQuote+",");
                sbReq.append(" V_sDuree=").append(sQuote+String.valueOf(h)).append("h").append(String.valueOf(mn)).append("mn").append(sQuote).append(",");
                sbReq.append(" V_Commentaire=").append(sQuote).append(String.valueOf(nbVol)).append(" ").append(i18n.tr("vols fusionnés")).append(sQuote).append(",");
                if (sImage != null && !sImage.isEmpty()) {
                    sbReq.append(" V_Photos=").append(sQuote).append(sImage).append(sQuote).append(",");
                } 
                sbReq.append(" V_IGC =").append(sQuote).append(totIGC).append(sQuote).append(" WHERE V_ID = ?");
                pstmtUpd = conn.prepareStatement(sbReq.toString());
                pstmtUpd.setInt(1, idRest);
                pstmtUpd.executeUpdate();
                
                // commit work
                conn.commit();
            } catch (SQLException e1) {
                try {
                    if (conn != null) {
                        conn.rollback();
                    }
                } catch (SQLException e2) {
                    sbError = new StringBuilder(this.getClass().getName()+"."+Thread.currentThread().getStackTrace()[1].getMethodName());
                    sbError.append("\r\n").append(e2.toString());
                    mylogging.log(Level.SEVERE, sbError.toString());
                }
                sbError = new StringBuilder(this.getClass().getName()+"."+Thread.currentThread().getStackTrace()[1].getMethodName());
                sbError.append("\r\n").append(e1.toString());
                mylogging.log(Level.SEVERE, sbError.toString());
            } finally {
                try {                   
                    if (pstmtDel != null) {
                        pstmtDel.close();
                    }
                    if (pstmtUpd != null) {
                        pstmtUpd.close();
                    }
                    conn.setAutoCommit(true); 
                    String selectedYear = (String) top_chbYear.getSelectionModel().getSelectedItem();
                    newVolsContent(selectedYear);
                } catch (SQLException e3) {
                    sbError = new StringBuilder(this.getClass().getName()+"."+Thread.currentThread().getStackTrace()[1].getMethodName());
                    sbError.append("\r\n").append(e3.toString());
                    mylogging.log(Level.SEVERE, sbError.toString());
                }
            }                                    
        } else {
            alertbox aError = new alertbox(myConfig.getLocale());
            aError.alertNumError(1112);   // Only one flight selected
        }
    }
    
    /*
    * Run simple logbook statistics 
    */
    @FXML
    private void clicTop_Stat() {
        alertbox aInfo = new alertbox(myConfig.getLocale());
        aInfo.alertInfo(i18n.tr("Statistiques de vol"));              
    }   
        
    /**
     * Display a fullscreen map of the track with flght parameters
     */    
    @FXML
    private void showFullMap() {
        if (currTrace.isDecodage()) {        
            map_visu visuFullMap = new map_visu(currTrace, myConfig);
            if (visuFullMap.isMap_OK()) {
                AnchorPane anchorPane = new AnchorPane();                
                WebView viewMap = new WebView();   
                AnchorPane.setTopAnchor(viewMap, 10.0);
                AnchorPane.setLeftAnchor(viewMap, 10.0);
                AnchorPane.setRightAnchor(viewMap, 10.0);
                AnchorPane.setBottomAnchor(viewMap, 10.0);
                anchorPane.getChildren().add(viewMap);  
                
                String sHTML = visuFullMap.getMap_HTML();
                /** ----- Begin Debug --------*/                 
                final Clipboard clipboard = Clipboard.getSystemClipboard();
                final ClipboardContent content = new ClipboardContent();
                content.putString(sHTML);            
                clipboard.setContent(content);                                
                /**------ End Debug --------- */
                viewMap.getEngine().loadContent(sHTML,"text/html");
                StackPane subRoot = new StackPane();
                subRoot.getChildren().add(anchorPane);
                Scene secondScene = null;
                if (myConfig.getOS() == osType.LINUX) {
                    // With this code for Linux, this is not OK with Win and Mac 
                    // This code found on http://java-buddy.blogspot.fr/2012/02/javafx-20-full-screen-scene.html
                    Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
                    secondScene = new Scene(subRoot, screenBounds.getWidth(), screenBounds.getHeight());
                } else {
                    // With this code, subStage.setMaximized(true) don't run under Linux
                    secondScene = new Scene(subRoot, 500, 400);
                }
                Stage subStage = new Stage();
                // We want modal window
                subStage.initModality(Modality.APPLICATION_MODAL);
                subStage.setScene(secondScene); 
                // Ne fonctionnait pas sous Linux...
                subStage.setMaximized(true);
                subStage.show();
            }  else {
                Alert alert = new Alert(Alert.AlertType.ERROR);                       
                alert.setContentText(i18n.tr("Une erreur est survenue pendant la génération de la carte"));
                alert.showAndWait();   
            }
            
        }
    }
    
     /**
     * VisuGPS is a powerful webservice for gps track display
     * the track must be an http url
     * runVisuGPS upload the track with a special php script in a server
     * This script upload the track and delete old tracks     
     * VisuGPS has webGL functions. They cannot operate in the java webviewer
     * User can choose between javawbeview or default browser
     */
    private void runVisuGPS(boolean inBrowser) {
        if (currTrace.isDecodage()) { 
            webio myUpload = new webio();
            try {
                String uploadUrl = myConfig.getUrlLogflyIGC()+"jtransfert.php";
                if (myUpload.testURL(uploadUrl) == 200)  {
                    byte[] igcBytes = currTrace.exportBytes();
                    if (igcBytes.length > 100)  {
                        String webFicIGC = myUpload.httpUploadIgc(igcBytes, uploadUrl);
                        if (webFicIGC != null) {
                            if (inBrowser) 
                                showVisuInBrowser(webFicIGC);
                            else
                                showVisuDirect(webFicIGC);
                        } else {
                            Alert alert = new Alert(Alert.AlertType.ERROR);           
                            alert.setContentText(i18n.tr("Echec du téléchargement de la trace"));
                            alert.showAndWait();                                                        
                        }
                    }                                        
                } else {
                    Alert alert = new Alert(Alert.AlertType.ERROR);           
                    alert.setContentText(i18n.tr("Mauvaise url de téléchargement"));
                    alert.showAndWait();  
                }                
            } catch (Exception e) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle(i18n.tr("Problème de chargement de la trace"));            
                String s = e.getClass().getName() + ": " + e.getMessage();
                alert.setContentText(s);
                alert.showAndWait();     
            }       
        }
    }
    
    /**
     * track uploaded in a server with a name like
     * YYYYMMDDHHMMSS_Random  [Random = number between 1 and 1000]
     * @param webFicIGC 
     */ 
    private void showVisuInBrowser(String webFicIGC)  {
        StringBuilder visuUrl = new StringBuilder();
        
        visuUrl.append(myConfig.getUrlVisu()).append(myConfig.getUrlLogflyIGC());
        visuUrl.append(webFicIGC);
        // https://stackoverflow.com/questions/5226212/how-to-open-the-default-webbrowser-using-java
        // http://www.java2s.com/Code/Java/JDK-6/UsingtheDesktopclasstolaunchaURLwithdefaultbrowser.htm
        switch (myConfig.getOS()) {
            case WINDOWS :
                if(Desktop.isDesktopSupported()){
                    Desktop desktop = Desktop.getDesktop();
                    try {
                        desktop.browse(new URI(visuUrl.toString()));
                    } catch (IOException | URISyntaxException e) {
                        sbError = new StringBuilder(this.getClass().getName()+"."+Thread.currentThread().getStackTrace()[1].getMethodName());
                        sbError.append("\r\n").append(e.toString());
                        mylogging.log(Level.SEVERE, sbError.toString());
                    }
                }
                break;
            case MACOS :
                try {
                    Runtime rt = Runtime.getRuntime();
                    rt.exec("open " + visuUrl.toString());
                } catch (Exception e) {
                    sbError = new StringBuilder(this.getClass().getName()+"."+Thread.currentThread().getStackTrace()[1].getMethodName());
                    sbError.append("\r\n").append(e.toString());
                    mylogging.log(Level.SEVERE, sbError.toString());
                }
                break;
            case LINUX :
                Runtime runtime = Runtime.getRuntime();
                try {
                    runtime.exec("xdg-open " + visuUrl.toString());
                } catch (IOException e) {
                    sbError = new StringBuilder(this.getClass().getName()+"."+Thread.currentThread().getStackTrace()[1].getMethodName());
                    sbError.append("\r\n").append(e.toString());
                    mylogging.log(Level.SEVERE, sbError.toString());
                }
                break;
        }
    }
    
    /**
     * track uploaded in a server with a name like
     * YYYYMMDDHHMMSS_Random  [Random = number between 1 and 1000]
     * @param webFicIGC 
     */   
    private void showVisuDirect(String webFicIGC)  {
        StringBuilder visuUrl = new StringBuilder();
        
        visuUrl.append(myConfig.getUrlVisu()).append(myConfig.getUrlLogflyIGC());
        visuUrl.append(webFicIGC);
        AnchorPane anchorPane = new AnchorPane();                
        WebView viewMap = new WebView();   
        // WebEngine webEngine = viewMap.getEngine();
        AnchorPane.setTopAnchor(viewMap, 10.0);
        AnchorPane.setLeftAnchor(viewMap, 10.0);
        AnchorPane.setRightAnchor(viewMap, 10.0);
        AnchorPane.setBottomAnchor(viewMap, 10.0);
        anchorPane.getChildren().add(viewMap);                         
        viewMap.getEngine().load(visuUrl.toString());
        StackPane subRoot = new StackPane();
        subRoot.getChildren().add(anchorPane);
        Scene secondScene = null;
        if (myConfig.getOS() == osType.LINUX) {
            // With this code for Linux, this is not OK with Win and Mac 
            // This code found on http://java-buddy.blogspot.fr/2012/02/javafx-20-full-screen-scene.html
            Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
            secondScene = new Scene(subRoot, screenBounds.getWidth(), screenBounds.getHeight());
        } else {
            // With this code, subStage.setMaximized(true) don't run under Linux
            secondScene = new Scene(subRoot, 500, 400);
        }
        Stage subStage = new Stage();
        // On veut que cette fenêtre soit modale
        subStage.initModality(Modality.APPLICATION_MODAL);
        subStage.setScene(secondScene); 
        subStage.setMaximized(true);
        subStage.show();            
    }
   
    /**
     * if needed, call the scoring class
     */
    @FXML
    private void showScore() {
        if (currTrace.isDecodage()) {
            if (currTrace.isScored())  {
                showFullMap();                
            } else {
                // Launch computation with an external program "points" (Scoring class). We must wait the end of the process
                // Scoring claas come back to controller with scoreReturn()
                // Therefore a communication bridge is necessary
                scoring currScore = new scoring(this,1, myConfig);  
                currScore.start(currTrace, myConfig.getIdxLeague());                            
            }
        }
    }
    
    /**
     * Manage flight comment
     */
    private void gestionComment() {
        if (tableVols.getSelectionModel().getSelectedItem().Comment.getValue()) {
            dialogbox actionReq = new dialogbox();
            int actionType = actionReq.twoChoices(i18n.tr("Commentaire"), i18n.tr("Que voulez- vous faire ?"), i18n.tr("Supprimer"), i18n.tr("Changer"), i18n.tr("Annuler"));
            switch (actionType) {
                case 1:
                    // Delete
                    delComment();
                    break;
                case 2:
                    // Add / Update
                    majComment();
                    break;
            }
        } else {
            majComment();
        }
        
    }
    
    /**
     * Delete a flight comment
     */
    private void delComment() {
        int selectedIndex = tableVols.getSelectionModel().getSelectedIndex();
        if (selectedIndex >= 0) {
            Carnet currCarnet = tableVols.getSelectionModel().getSelectedItem();
            String sReq = "UPDATE Vol SET V_Commentaire= ? WHERE V_ID = ?";   
            try {
                PreparedStatement pstmt = myConfig.getDbConn().prepareStatement(sReq);
                pstmt.setString(1,null); 
                pstmt.setInt(2, Integer.valueOf(currCarnet.getIdVol()));
                pstmt.executeUpdate();    
                pstmt.close();
                // Pour rafrachir la carte
                decodeVolCarnet(currCarnet.getIdVol());
            } catch (Exception e) {
                alertbox aError = new alertbox(myConfig.getLocale());
                aError.alertError(e.getMessage()); 
            }  
            tableVols.getSelectionModel().getSelectedItem().comTexte.set(null);
            tableVols.getSelectionModel().getSelectedItem().Comment.set(false);                     
            tableVols.refresh();            
        }               
    }
    
    /**
     * Update a flight comment
     */
    private void majComment() {
        String commentStr = tableVols.getSelectionModel().getSelectedItem().comTexte.getValue();
        winComment myComment = new winComment(commentStr,i18n);                
        alertbox myInfo = new alertbox(myConfig.getLocale());
        if (myComment.isModif())  {                        
            // l'échappement des apostrophes est fait automatiquement
            String strComment = myComment.getCommentTxt();
            Carnet currCarnet = tableVols.getSelectionModel().getSelectedItem();
            String sReq = "UPDATE Vol SET V_Commentaire= ? WHERE V_ID = ?";                    
            try {
                PreparedStatement pstmt = myConfig.getDbConn().prepareStatement(sReq);
                pstmt.setString(1,strComment); 
                pstmt.setInt(2, Integer.valueOf(currCarnet.getIdVol()));
                pstmt.executeUpdate();  
                pstmt.close();
                // Pour rafrachir la carte
                decodeVolCarnet(currCarnet.getIdVol());
            } catch (Exception e) {
                alertbox aError = new alertbox(myConfig.getLocale());
                aError.alertError(e.getMessage()); 
            }  
            tableVols.getSelectionModel().getSelectedItem().comTexte.set(myComment.getCommentTxt());
            tableVols.getSelectionModel().getSelectedItem().Comment.set(true);                    
            tableVols.refresh();            
        }         
    }
    
    /**
     * Manage photo of the flight
     */
    private void gestionPhoto() {
        if (tableVols.getSelectionModel().getSelectedItem().Photo.getValue()) {
            dialogbox actionReq = new dialogbox();
            int actionType = actionReq.twoChoices(i18n.tr("Photo du jour"), i18n.tr("Que voulez- vous faire ?"), i18n.tr("Supprimer"), i18n.tr("Changer"), i18n.tr("Annuler"));
            switch (actionType) {
                case 1:
                    // Suppression
                    delPhoto();
                    break;
                case 2:
                    // Addition / Modidification
                    majPhoto();
                    break;
            }
        } else {
            majPhoto();
        }
        
    }
    
    /**
     * Delete photo of the flight
     */
    private void delPhoto() {
        int selectedIndex = tableVols.getSelectionModel().getSelectedIndex();
        if (selectedIndex >= 0) {
            Carnet currCarnet = tableVols.getSelectionModel().getSelectedItem();
            String sReq = "UPDATE Vol SET V_Photos= ? WHERE V_ID = ?";                    
            try {
                PreparedStatement pstmt = myConfig.getDbConn().prepareStatement(sReq);
                pstmt.setString(1,null); 
                pstmt.setInt(2, Integer.valueOf(currCarnet.getIdVol()));
                pstmt.executeUpdate(); 
                pstmt.close();
            } catch (Exception e) {
                alertbox aError = new alertbox(myConfig.getLocale());
                aError.alertError(e.getMessage()); 
            }  
            tableVols.getSelectionModel().getSelectedItem().camera.set(null);
            tableVols.getSelectionModel().getSelectedItem().Photo.set(false);                    
            tableVols.refresh();            
        }        
    }
    
    /**
     * Update photo of the flight
     */
    private void majPhoto() {
        int selectedIndex = tableVols.getSelectionModel().getSelectedIndex();
        if (selectedIndex >= 0) {
            Carnet currCarnet = tableVols.getSelectionModel().getSelectedItem();
            FileChooser fileChooser = new FileChooser();
            FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter(i18n.tr("fichiers photos (*.jpg)"), "*.jpg");
            fileChooser.getExtensionFilters().add(extFilter);
            File selectedFile = fileChooser.showOpenDialog(null);        
            if(selectedFile != null){
                BufferedImage bufferedImage;
                try {
                    bufferedImage = ImageIO.read(selectedFile);
                    imgmanip currImage = new imgmanip();
                    // A direct reduction during upload give bad results
                    // Image currImg = new Image(selectedFile.getAbsolutePath(), 700, 700, true, true);
                    BufferedImage redBufImage = currImage.reduitPhoto(bufferedImage,700,700);
                    String strImage = currImage.imgToBase64String(redBufImage, "jpg");
                    if (strImage != null)  {
                        currCarnet.setCamera(strImage);
                        // db storage
                        String sReq = "UPDATE Vol SET V_Photos= ? WHERE V_ID = ?";                    
                        try {
                            PreparedStatement pstmt = myConfig.getDbConn().prepareStatement(sReq);
                            pstmt.setString(1,strImage); 
                            pstmt.setInt(2, Integer.valueOf(currCarnet.getIdVol()));
                            pstmt.executeUpdate();   
                            pstmt.close();
                        } catch (Exception e) {
                            alertbox aError = new alertbox(myConfig.getLocale());
                            aError.alertError(e.getMessage()); 
                        }  
                        tableVols.getSelectionModel().getSelectedItem().camera.set(strImage);
                        tableVols.getSelectionModel().getSelectedItem().Photo.set(true);                    
                        tableVols.refresh();
                        Image redImg = SwingFXUtils.toFXImage(redBufImage, null);
                        winPhoto myPhoto = new winPhoto();    
                        myPhoto.showDbPhoto(redImg);    
                    }
                } catch (IOException ex) {
                    alertbox aError = new alertbox(myConfig.getLocale());
                    aError.alertError(ex.getMessage());                     
                }            
            }
        }
    }
    
    /**
     * Answer of scoring class
     * @param pRetour 
     */
    public void scoreReturn(int pRetour) {
        // If scoring failed, error message was sent by Scoring class
        if (currTrace.isScored())  {
            // Mise à jour de la db
            Carnet selectedVol = tableVols.getSelectionModel().getSelectedItem();
            StringBuilder sReq = new StringBuilder();
            sReq.append("UPDATE Vol SET V_League='").append(String.valueOf(currTrace.getScore_Idx_League())).append("'");
            sReq.append(",V_Score='").append(currTrace.getScore_JSON()).append("'");
            sReq.append(" WHERE V_ID = ?");            
            try {
                PreparedStatement pstmt = myConfig.getDbConn().prepareStatement(sReq.toString());
                pstmt.setInt(1, Integer.valueOf(selectedVol.getIdVol()));
                pstmt.executeUpdate(); 
                pstmt.close();
                switch (pRetour) {
                    case 1:
                        showFullMap();
                        break;   
                    case 2:
                        showWinGE();
                        break;  
                }                
            } catch (Exception e) {
                alertbox aError = new alertbox(myConfig.getLocale());
                aError.alertError(e.getMessage()); 
            }       
        }         
    }

    /**
     * Manage Google Earth kml file generation
     */
    @FXML
    private void askWinGE() {
        // Track will be scored before generation
        if (currTrace.isDecodage()) {
            if (currTrace.isScored())  {               
                showWinGE();                
            } else {
                // Launch computation with an external program "points" (Scoring class). We must wait the end of the process
                // Scoring claas come back to controller with scoreReturn()
                // Therefore a communication bridge is necessary
                scoring currScore = new scoring(this,2, myConfig);  
                currScore.start(currTrace, myConfig.getIdxLeague());                            
            }
        }        
    }
    
    /**
     * Display window with parameters kml generation
     * @return 
     */
    private boolean showWinGE() {
        try {                                  
            // Load the fxml file and create a new stage for the popup dialog.
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(Main.class.getResource("/KmlView.fxml")); 
            
            AnchorPane page = (AnchorPane) loader.load();
            Stage dialogStage = new Stage();
            dialogStage.setTitle(i18n.tr("Génération fichier kml"));
            dialogStage.initModality(Modality.WINDOW_MODAL);       
            dialogStage.initOwner(mainApp.getPrimaryStage());
            Scene scene = new Scene(page);
            dialogStage.setScene(scene);

            // Initialization of a communication bridge between CarnetView and KmlView
            KmlViewController controller = loader.getController();
            controller.setCarnetBridge(this);
            controller.setDialogStage(dialogStage); 
            controller.setAppel(1, myConfig);
            dialogStage.showAndWait();
            
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Answer of KmlViewController when kml generation is finished 
     * @param currKml
     * @throws IOException 
     */
    public void configKml(makingKml currKml) throws IOException {  
        boolean kmlDisk = false;
        File ficKml = null;
        
        currKml.genKml(currTrace);
        if (currKml.isKmlOK())  {
            if (currKml.getErrorCode() == 0) {
                if (currKml.isExport() && currKml.getExportPath() != null)  {
                    // Génération du fichier demandé
                    ficKml = new File(currKml.getExportPath());
                    if (textio.writeTxtFile(ficKml, currKml.getKmlString())) {                       
                        kmlDisk = true;
                    }
                }
                if (currKml.isRunGE()) {
                    if (!kmlDisk) {
                        ficKml = systemio.tempacess.getAppFile("Logfly", "temp.kml");
                        System.out.println("fichier kml : "+ficKml.getAbsolutePath());
                        if (textio.writeTxtFile(ficKml, currKml.getKmlString())) kmlDisk = true;
                    }
                    if (kmlDisk) {
                        try {                        
                            Desktop dt = Desktop.getDesktop();     
                            dt.open(ficKml);            
                        } catch (Exception e) {
                            alertbox aError = new alertbox(myConfig.getLocale());
                            aError.alertNumError(1030); 
                        }       
                    }
                } else {
                    alertbox aInfo = new alertbox(myConfig.getLocale());
                    aInfo.alertInfo(i18n.tr("Génération du fichier terminée")); 
                }
            } else {
                alertbox aError = new alertbox(myConfig.getLocale());
                aError.alertNumError(currKml.getErrorCode()); 
            }
        }
    }

    /**
    * Translate labels of the window
    */
    private void winTraduction() {
        dateCol.setText(i18n.tr("Date"));
        heureCol.setText(i18n.tr("Heure"));
        dureeCol.setText(i18n.tr("Durée"));
        siteCol.setText(i18n.tr("Site"));
        voileCol.setText(i18n.tr("Voile"));
        btnMap.setStyle("-fx-background-color: transparent;");
        Tooltip mapToolTip = new Tooltip();
        mapToolTip.setStyle("-fx-background-color: linear-gradient(#e2ecfe, #99bcfd);");
        mapToolTip.setText(i18n.tr("Carte Google Maps plein écran"));
        btnMap.setTooltip(mapToolTip);
        
        btnStat.setStyle("-fx-background-color: transparent;");      
        Tooltip statToolTip = new Tooltip();
        statToolTip.setStyle("-fx-background-color: linear-gradient(#e2ecfe, #99bcfd);");
        statToolTip.setText(i18n.tr("Statistiques de vol"));
        btnStat.setTooltip(statToolTip);
                
        btnScore.setStyle("-fx-background-color: transparent;");      
        Tooltip scoreToolTip = new Tooltip();
        scoreToolTip.setStyle("-fx-background-color: linear-gradient(#e2ecfe, #99bcfd);");
        scoreToolTip.setText(i18n.tr("Evaluation de la trace"));
        btnScore.setTooltip(scoreToolTip);
        
        btnGEarth.setStyle("-fx-background-color: transparent;");      
        Tooltip geToolTip = new Tooltip();
        geToolTip.setStyle("-fx-background-color: linear-gradient(#e2ecfe, #99bcfd);");
        geToolTip.setText(i18n.tr("Génération fichier Google Earth"));
        btnGEarth.setTooltip(geToolTip);
        
    }
    
}
