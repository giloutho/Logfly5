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
import igc.pointIGC;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.logging.Level;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Bounds;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.RadioButton;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.ImageView;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.scene.web.WebView;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Screen;
import javafx.stage.Stage;
import leaflet.map_X_markers;
import leaflet.map_markers;
import littlewins.winChoose;
import littlewins.winSiteList;
import model.Sitemodel;
import org.xnap.commons.i18n.I18n;
import org.xnap.commons.i18n.I18nFactory;
import settings.configProg;
import settings.osType;
import systemio.mylogging;

/**
 *
 * @author gil
 */
public class SitesViewController {
    
    @FXML
    private TableView<Sitemodel> tableSites;   
    @FXML
    private TableColumn<Sitemodel, String> nomCol;
    @FXML
    private TableColumn<Sitemodel, String> villeCol;
    @FXML
    private TableColumn<Sitemodel, String> cpCol;
    @FXML
    private TableColumn<Sitemodel, String> altCol;        
    @FXML
    private TableColumn<Sitemodel, String> orientCol;   
    @FXML
    private TextField txtSearch;    
    @FXML
    private RadioButton rdAll;    
    @FXML
    private RadioButton rdDeco; 
    @FXML
    private RadioButton rdAttero;
    @FXML
    private RadioButton rdNondef; 
    @FXML
    private Button btnMap;
    @FXML
    private ImageView top_Menu;
    @FXML
    private WebView mapViewer;    

    ToggleGroup rdGroup;
    
    // Reference to the main application.
    private Main mainApp;
    
    // Localization
    private I18n i18n; 
    
    // Settings
    configProg myConfig;
    StringBuilder sbError;
    String RC = "\n";
    
    //START | SQLITE
    private static Statement stat;
    private PreparedStatement prep;
    //END | SQLITE    
    
    private ObservableList <Sitemodel> dataSites; 
    private FilteredList<Sitemodel> filteredData;
    private SortedList<Sitemodel> sortedData;
    
    private int addNb = 0;
    private int addSitesOK = 0;
    private int addSitesBad = 0;
    private StringBuilder sbDoublons;
    private StringBuilder sbRejected;
    
    private ContextMenu tableContextMenu;
    
    @FXML
    private void initialize() {
        // We need to intialize i18n before TableView building
        // For this reason we put building code in iniTable() 
        // This procedure will be called after setMainApp()   
        rdGroup = new ToggleGroup();    
        rdAll.setToggleGroup(rdGroup);
        rdAll.setSelected(true);    
        rdDeco.setToggleGroup(rdGroup);
        rdAttero.setToggleGroup(rdGroup);
        rdNondef.setToggleGroup(rdGroup);        
    }    
    
    
    /**
     * Initialization of the TableView
     * Fill the table with data from db
     */
    private void iniTable() {
        
        dataSites = FXCollections.observableArrayList();    
        
        tableSites.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        
        // filter process read on http://code.makery.ch/blog/javafx-8-tableview-sorting-filtering/
        // wrap the ObservableList in a FilteredList (initially display all data).
        // the same site has an old version of code : http://code.makery.ch/blog/javafx-2-tableview-filter/
        filteredData = new FilteredList<>(dataSites, p -> true);
        
        // set the filter Predicate whenever the filter changes.
        txtSearch.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredData.setPredicate(site -> {
                // If filter text is empty, display all persons.
                if (newValue == null || newValue.isEmpty()) {
                    return true;
                }

                // Compare fields of every site with filter text.
                String lowerCaseFilter = newValue.toLowerCase();

                if (site.getNom().toLowerCase().contains(lowerCaseFilter)) {
                    return true; // Filter matches site name
                } 
                else if (site.getVille() != null && !site.getVille().equals("")) {
                    if (site.getVille().toLowerCase().contains(lowerCaseFilter)) 
                        return true; // Filter matches site locality
                    
                    else if (site.getCp() != null && !site.getCp().equals("")) {
                        if (site.getCp().toLowerCase().contains(lowerCaseFilter)) 
                            return true; // Filter matches site locality
                    }    
                }
                
                return false; // Does not match.
            });
        });    
        
        // http://stackoverflow.com/questions/32119277/colouring-table-row-in-javafx
        // Color chart in https://docs.oracle.com/javafx/2/api/javafx/scene/doc-files/cssref.html
        tableSites.setRowFactory(tbrow -> new TableRow<Sitemodel>() {
            @Override
            public void updateItem(Sitemodel item, boolean empty) {
                super.updateItem(item, empty) ;
                try {
                    if (item == null) {
                        setStyle("");
                    } else if (item.getType() != null) {
                            if (item.getType().equals("D")) {
                            setStyle("-fx-text-background-color: darkslateblue;");
                        } else if (item.getType().equals("A")) {                  
                            setStyle("-fx-text-background-color: darkolivegreen;");   
                        } else {
                            setStyle("-fx-text-background-color: darksalmon;");
                        }                    
                    } else {
                        setStyle("");
                    }
                } catch (Exception e) {
                    System.out.println("Erreur : "+item.getNom());
                    setStyle("");
                }
                        

            }
        });        
       
        // wrap the FilteredList in a SortedList. 
        sortedData = new SortedList<>(filteredData);
        
        // bind the SortedList comparator to the TableView comparator.
        sortedData.comparatorProperty().bind(tableSites.comparatorProperty());
                       
        nomCol.setCellValueFactory(new PropertyValueFactory<Sitemodel, String>("nom"));
        villeCol.setCellValueFactory(new PropertyValueFactory<Sitemodel, String>("ville"));
        cpCol.setCellValueFactory(new PropertyValueFactory<Sitemodel, String>("cp"));        
        altCol.setCellValueFactory(new PropertyValueFactory<Sitemodel, String>("alt"));     
        orientCol.setCellValueFactory(new PropertyValueFactory<Sitemodel, String>("orient"));
        
        // Listener for line changes and  display relevant details
        tableSites.getSelectionModel().selectedItemProperty().addListener(
        (observable, oldValue, newValue) -> showSiteMap((Sitemodel) newValue));    
                 
        // Cette procedure provient de https://kubos.cz/2016/04/01/javafx-dynamic-context-menu-on-treeview.html
        tableSites.addEventHandler(MouseEvent.MOUSE_RELEASED, e->{ 
            if (e.getButton()==MouseButton.SECONDARY) { 
                Sitemodel selectedSite = tableSites.getSelectionModel().getSelectedItem();
                
                //item is selected - this prevents fail when clicking on empty space 
                if (selectedSite != null) { 
                    //open context menu on current screen position  
                    tableContextMenu.show(tableSites, e.getScreenX(), e.getScreenY());
                } 
            } else { 
                //any other click cause hiding menu 
                tableContextMenu.hide(); 
            } 
        });           
        
        
        fillTable("SELECT * FROM Site ORDER BY S_Nom");
    }    
    
    /**
     * Fill table with filter or not in SQL request
     * @param sReq 
     */
    private void fillTable(String sReq) {
        Statement stmt = null;
        ResultSet rs = null;
        try {
            stmt = myConfig.getDbConn().createStatement();
            rs = stmt.executeQuery(sReq);
            if (rs != null)  { 
                while (rs.next()) {
                    Sitemodel si = new Sitemodel();  
                    si.setIdSite(rs.getString("S_ID"));
                    si.setNom(rs.getString("S_Nom"));
                    si.setVille(rs.getString("S_Localite"));
                    si.setCp(rs.getString("S_CP"));
                    si.setAlt(rs.getString("S_Alti"));
                    si.setOrient(rs.getString("S_Orientation"));  
                    si.setType(rs.getString("S_Type"));         
                    si.setLatitude(rs.getDouble("S_Latitude"));
                    si.setLongitude(rs.getDouble("S_Longitude"));
                    dataSites.add(si);                
                }    
                tableSites.setItems(sortedData);
                if (tableSites.getItems().size() > 0) {
                    tableSites.getSelectionModel().select(0);                    
                }                
            }
            
        } catch ( Exception e ) {
            sbError = new StringBuilder(this.getClass().getName()+"."+Thread.currentThread().getStackTrace()[1].getMethodName());
            sbError.append("\r\n").append(e.toString());
            mylogging.log(Level.SEVERE, sbError.toString());   
        } finally {
            try{
                rs.close(); 
                stmt.close();
            } catch(Exception e) { } 
        }                 
    }
    
    /**
     * Display details of a flight
     * 
     * curSite value is tested to solve a curious problem
     * First filling of table without problem showSiteMap called once
     * Second filling, showSiteMap is called with null currSite and is called a second time with correct currSite
     * @param currSite 
     */
    private void showSiteMap(Sitemodel currSite) {
        if (currSite != null) {
            Statement stmt = null;
            ResultSet rs = null; 

            String sReq = "SELECT S_Nom, S_Alti, S_Latitude,S_Longitude, S_Commentaire FROM Site WHERE S_ID ="+currSite.getIdSite();
            try {
                stmt = myConfig.getDbConn().createStatement();
                rs =  stmt.executeQuery(sReq);
                if (rs != null)  { 
                    pointIGC pPoint1 = new pointIGC();
                    double dLatitude = Double.parseDouble(rs.getString("S_Latitude"));
                    if (dLatitude > 90 || dLatitude < -90) dLatitude = 0;
                    pPoint1.setLatitude(dLatitude);
                    double dLongitude = Double.parseDouble(rs.getString("S_Longitude"));
                    if (dLongitude > 180 || dLongitude < -180) dLongitude = 0;
                    pPoint1.setLongitude(dLongitude);
                    pPoint1.setAltiGPS(Integer.parseInt(rs.getString("S_Alti")));
                    StringBuilder sbComment = new StringBuilder();
                    sbComment.append(rs.getString("S_Nom")).append("<BR>");
                    sbComment.append(i18n.tr("Altitude")).append(" : ").append(String.valueOf(pPoint1.AltiGPS)).append(" m" );
                    pPoint1.Comment = sbComment.toString();   

                    map_markers mapSite = new map_markers(i18n, myConfig.getIdxMap());
                    mapSite.getPointsList().add(pPoint1);
                    mapSite.setStrComment(rs.getString("S_Commentaire"));
                    if (mapSite.genMap() == 0) {
                        /** ----- Debut Debug --------*/ 
                        String sDebug = mapSite.getMap_HTML();
                        final Clipboard clipboard = Clipboard.getSystemClipboard();
                        final ClipboardContent content = new ClipboardContent();
                        content.putString(mapSite.getMap_HTML());            
                        clipboard.setContent(content);
                        /** ----- Fin Debug --------*/                     
                        // Delete cache for navigate back
                        mapViewer.getEngine().load("about:blank");                     
                        mapViewer.getEngine().loadContent(mapSite.getMap_HTML());  
                    }                  
                }            
            } catch ( Exception e ) {
                alertbox aError = new alertbox(myConfig.getLocale());
                aError.alertError(e.getClass().getName() + ": " + e.getMessage());   
                sbError = new StringBuilder(this.getClass().getName()+"."+Thread.currentThread().getStackTrace()[1].getMethodName());
                sbError.append("\r\n").append(e.toString());
                mylogging.log(Level.SEVERE, sbError.toString());             
            } finally {
                try{
                    rs.close(); 
                    stmt.close();
                } catch(Exception e) { } 
            } 
        }
    }    
    
    private void searchDuplicate() {
        double latMin, latMax, longMin, longMax;
        StringBuilder sbReq = new StringBuilder();
        
        Sitemodel selectedSite = tableSites.getSelectionModel().getSelectedItem();
        // On réduit à deux décimales, pour les explis voir l'aide à Ceil
        double arrLat = Math.ceil(selectedSite.getLatitude()*1000)/1000;
        double arrLong = Math.ceil(selectedSite.getLongitude()*1000)/1000;
        latMin = arrLat - 0.001;     // 0,01 1,13 km 0,001  113m
        latMax = arrLat + 0.001;
        longMin = arrLong - 0.001;
        longMax = arrLong + 0.001;
        sbReq.append("SELECT * FROM Site WHERE S_Latitude > ");
        sbReq.append(String.valueOf(latMin)).append(" AND S_Latitude < ").append(String.valueOf(latMax));
        sbReq.append(" AND S_Longitude > ").append(String.valueOf(longMin)).append(" AND S_Longitude < ").append(String.valueOf(longMax));
        dataSites.clear();
        mapViewer.getEngine().load("about:blank");  
        txtSearch.setText(null);
        fillTable(sbReq.toString());
    }    

    @FXML
    private void pushAll() {
        if (rdAll.isSelected()) {
            String sReq = "SELECT * FROM Site ORDER BY S_Nom";
            dataSites.clear();
            mapViewer.getEngine().load("about:blank");  
            fillTable(sReq);
        }
    }       

    @FXML
    private void pushDeco() {
        if (rdDeco.isSelected()) {
            String sReq = "SELECT * FROM Site WHERE S_Type = 'D' ORDER BY S_Nom";
            dataSites.clear(); 
            mapViewer.getEngine().load("about:blank");
            fillTable(sReq);   
        }
    }       
    
    @FXML
    private void pushAttero() {
        if (rdAttero.isSelected()) {
            String sReq = "SELECT * FROM Site WHERE S_Type = 'A' ORDER BY S_Nom";
            dataSites.clear(); 
            mapViewer.getEngine().load("about:blank");
            fillTable(sReq);
        }
    }       
    
    @FXML
    private void pushNondef() {
        if (rdNondef.isSelected()) {
            String sReq = "SELECT * FROM Site WHERE  (S_Type is null or S_Type = '') OR (S_Type <> 'D' AND S_Type <> 'A') ORDER BY S_Nom";
            dataSites.clear(); 
            mapViewer.getEngine().load("about:blank");  
            fillTable(sReq);
        }
    }     
    
    private void siteFormEdit() {
        try {                     
            // Load the fxml file and create a new stage for the popup dialog.
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(Main.class.getResource("/SiteForm.fxml")); 
            AnchorPane page = (AnchorPane) loader.load();

            Stage dialogStage = new Stage();
            dialogStage.initModality(Modality.WINDOW_MODAL);       
            dialogStage.initOwner(mainApp.getPrimaryStage());
            Scene scene = new Scene(page);
            dialogStage.setScene(scene);

            String st =   tableSites.getSelectionModel().getSelectedItem().getIdSite();
            // Communication bridge between SiteForm and SiteView controllers
            SiteFormController controller = loader.getController();
            controller.setSiteBridge(this);
            controller.setDialogStage(dialogStage); 
            controller.setEditForm(myConfig,tableSites.getSelectionModel().getSelectedItem().getIdSite(),0);   // 0 -> edit an existing file
            // This window will be modal
            dialogStage.showAndWait();
                       
        } catch (IOException e) {
            sbError = new StringBuilder(this.getClass().getName()+"."+Thread.currentThread().getStackTrace()[1].getMethodName());
            sbError.append("\r\n").append(e.toString());
            mylogging.log(Level.SEVERE, sbError.toString()); 
            
        }
    }
    
    public void returnEdit(boolean formUpdated, Sitemodel newsite) {
        
        if (formUpdated) {            
            tableSites.getSelectionModel().getSelectedItem().setNom(newsite.getNom());
            tableSites.getSelectionModel().getSelectedItem().setVille(newsite.getVille());
            tableSites.getSelectionModel().getSelectedItem().setCp(newsite.getCp());
            tableSites.getSelectionModel().getSelectedItem().setAlt(newsite.getAlt());
            tableSites.getSelectionModel().getSelectedItem().setOrient(newsite.getOrient());  
            tableSites.getSelectionModel().getSelectedItem().setType(newsite.getType());   
            tableSites .refresh();            
        }        
    }
    
    public void returnAdd(boolean formUpdated, Sitemodel newsite) {
        
        if (formUpdated) {
            fillTable("SELECT * FROM Site ORDER BY S_Nom");            
            tableSites.getSelectionModel().clearSelection();
            // we want to set focus at new site just inserted
            // from https://stackoverflow.com/questions/40398905/search-tableview-list-in-javafx
            System.out.println(newsite.getNom());
            tableSites.getItems().stream()
                .filter(Sitemodel -> Sitemodel.getNom().equals(newsite.getNom()))
                .findAny()
                .ifPresent(Sitemodel -> {
                    tableSites.getSelectionModel().select(Sitemodel);
                    tableSites.scrollTo(Sitemodel);
            });                       
        }        
    }    
    
    /**
     * Delete a site in database
     */
    private void siteDelete() {
        PreparedStatement pstmt = null;
        
        int selectedIndex = tableSites.getSelectionModel().getSelectedIndex();
        if (selectedIndex >= 0) {
            Sitemodel selSite = tableSites.getSelectionModel().getSelectedItem();
            dialogbox dConfirm = new dialogbox(i18n);
            StringBuilder sbMsg = new StringBuilder();             
            sbMsg.append(selSite.getNom());
            sbMsg.append(" ");
            sbMsg.append(selSite.getVille());                        
            if (dConfirm.YesNo(i18n.tr("Suppression du site"), sbMsg.toString()))   {                
                String sReq = "DELETE FROM Site WHERE S_ID = ?";
                try {
                    pstmt = myConfig.getDbConn().prepareStatement(sReq);
                    pstmt.setInt(1, Integer.valueOf(selSite.getIdSite()));
                    pstmt.executeUpdate();    
                    // With filtered list tableSites.getItems().remove(selectedIndex) does'nt work
                    int visibleIndex = tableSites.getSelectionModel().getSelectedIndex();
                    // Source index of master data.
                    int sourceIndex = sortedData.getSourceIndexFor(dataSites, visibleIndex);
                    dataSites.remove(sourceIndex);
                    pstmt.close();
                } catch (Exception e) {
                    alertbox aError = new alertbox(myConfig.getLocale());
                    aError.alertError(e.getMessage());                                                           
                }                                                
            }                                 
        } else {
            // no site selected
            alertbox aError = new alertbox(myConfig.getLocale());
            aError.alertError(i18n.tr("Aucun site sélectionné..."));                       
        }        
    }    
    
    private void siteFormAdd() {
        try {                     
            // Load the fxml file and create a new stage for the popup dialog.
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(Main.class.getResource("/SiteForm.fxml")); 
            AnchorPane page = (AnchorPane) loader.load();

            Stage dialogStage = new Stage();
            dialogStage.initModality(Modality.WINDOW_MODAL);       
            dialogStage.initOwner(mainApp.getPrimaryStage());
            Scene scene = new Scene(page);
            dialogStage.setScene(scene);

            // Communication bridge between SiteForm and SiteView controllers
            SiteFormController controller = loader.getController();
            controller.setSiteBridge(this);
            controller.setDialogStage(dialogStage); 
            controller.setEditForm(myConfig,null,1);   // 1 -> add a new site 
            // This window will be modal
            dialogStage.showAndWait();
            

        } catch (IOException e) {
            e.printStackTrace();
        }        
    }
    /**
     * We had a big problem with the webviewer
     * if there is long distances between markers, 
     * map.fitBounds(tabPoints,{maxZoom : 15}) doesn't work properly
     * We checke with browsers, HTML is correct and fitBounds is correct
     * webviewer displays the map at zoom level 0 (entire earth)
     * We put this instruction in html code : if (map.getZoom() < 5) map.setZoom(7);
     */
    @FXML
    private void showFullMap() {

        String sHTML = null;
        
        ArrayList<pointIGC> sitesList = new ArrayList<>();
        
        ObservableList<Sitemodel> selSites = tableSites.getSelectionModel().getSelectedItems();   

        for(Sitemodel oneSite : selSites){   
            pointIGC pPoint1 = new pointIGC();
            double dLatitude = oneSite.getLatitude();
            if (dLatitude > 90 || dLatitude < -90) dLatitude = 0;
            pPoint1.setLatitude(dLatitude);
            double dLongitude = oneSite.getLongitude();
            if (dLongitude > 180 || dLongitude < -180) dLongitude = 0;
            pPoint1.setLongitude(dLongitude);
            pPoint1.setAltiGPS(Integer.parseInt(oneSite.getAlt()));
            StringBuilder sbComment = new StringBuilder();
            sbComment.append(oneSite.getNom()).append("<BR>");
            sbComment.append(i18n.tr("Altitude")).append(" : ").append(String.valueOf(pPoint1.AltiGPS)).append(" m" );
            pPoint1.Comment = sbComment.toString();   
            sitesList.add(pPoint1);              
        }
        
        if (sitesList.size() > 1)  {
            map_X_markers mapSite = new map_X_markers(i18n, myConfig.getIdxMap());
            mapSite.setPointsList(sitesList);
            if (mapSite.genMap() == 0) {
                sHTML = mapSite.getMap_HTML();               
            } else {
                Alert alert = new Alert(Alert.AlertType.ERROR);                       
                alert.setContentText(i18n.tr("Une erreur est survenue pendant la génération de la carte"));
                alert.showAndWait();   
            }            
        } else if (sitesList.size() == 1)  {
            map_markers mapSite = new map_markers(i18n, myConfig.getIdxMap());
            mapSite.getPointsList().add(sitesList.get(0));
            mapSite.setStrComment(null);
            if (mapSite.genMap() == 0) {  
                sHTML = mapSite.getMap_HTML();               
            } else {
                Alert alert = new Alert(Alert.AlertType.ERROR);                       
                alert.setContentText(i18n.tr("Une erreur est survenue pendant la génération de la carte"));
                alert.showAndWait();   
            }
        } 
        if (sHTML != null)  {
            AnchorPane anchorPane = new AnchorPane();                
            WebView viewMap = new WebView();   
            AnchorPane.setTopAnchor(viewMap, 10.0);
            AnchorPane.setLeftAnchor(viewMap, 10.0);
            AnchorPane.setRightAnchor(viewMap, 10.0);
            AnchorPane.setBottomAnchor(viewMap, 10.0);
            anchorPane.getChildren().add(viewMap);  

            /** ----- Begin Debug --------*/                 
            final Clipboard clipboard = Clipboard.getSystemClipboard();
            final ClipboardContent content = new ClipboardContent();
            content.putString(sHTML);            
            clipboard.setContent(content);                                
            /**------ End Debug --------- */
            //viewMap.getEngine().loadContent(sHTML,"text/html");
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
            viewMap.getEngine().loadContent(sHTML,"text/html");
            // Ne fonctionnait pas sous Linux...
            subStage.setMaximized(true);
            subStage.show();            
        }
    }    
    
    /**
     * Sequence is patterned on FFVL format. 
     * But since 2014, FFVL remove export sites files
     * We need a field country, for this, we use SITE_STRUCTURE_NOM field
     */
    private void exportCsv() {
  
        String Pt_Virg = ";";
        String Req; 
        int res = -1;
        Statement stmt = null;
        ResultSet rs = null; ;
        String sReq;
        StringBuilder sbLine;
        
        ObservableList<Sitemodel> selSites = tableSites.getSelectionModel().getSelectedItems();   

        int nbSites = 0;
        for(Sitemodel oneSite : selSites){   
            nbSites++;
        }
        dialogbox dConfirm = new dialogbox(i18n);
        StringBuilder sbMsg = new StringBuilder(); 
        sbMsg.append(String.valueOf(nbSites)).append(" ").append(i18n.tr(" sites sélectionnés- Exporter ?"));
        if (dConfirm.YesNo("", sbMsg.toString()))   { 
            FileChooser fileChooser = new FileChooser();
            fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter(i18n.tr("Format csv"), "*.csv"),
                new FileChooser.ExtensionFilter(i18n.tr("Format texte"), "*.txt")
            );  
            File selectedFile = fileChooser.showSaveDialog(null);        
            if(selectedFile != null){
                String selExt = systemio.textio.getFileExtension(selectedFile);
                selExt = selExt.toUpperCase();            
                try {
                   // FileWriter fileWriter = null;
                    //fileWriter = new FileWriter(selectedFile);
                    FileOutputStream fileStream = new FileOutputStream(selectedFile);
                    Charset ENCODING = StandardCharsets.ISO_8859_1;
                    OutputStreamWriter writer = new OutputStreamWriter(fileStream, ENCODING);
                   // fileWriter.write(currTrace.getFicIGC());
                    for(Sitemodel oneSite : selSites){                          
                        sReq = "SELECT * FROM Site WHERE S_ID ="+oneSite.getIdSite();
                        stmt = myConfig.getDbConn().createStatement();
                        rs =  stmt.executeQuery(sReq);
                        if (rs != null)  {
                            sbLine = new StringBuilder();
                            sbLine.append("").append(Pt_Virg); // POINT_ID                            
                            sbLine.append(rs.getString(2)).append(Pt_Virg);  // POINT_NOM                            
                            sbLine.append(rs.getString(9)).append(Pt_Virg);   // POINT_LATITUDE                            
                            sbLine.append(rs.getString(10)).append(Pt_Virg);  // POINT_LONGITUDE                            
                            sbLine.append(rs.getString(8)).append(Pt_Virg);  // POINT_ALTITUDE
                            switch (rs.getString(6)) {
                                case "A":                                    
                                    sbLine.append("Atterrissage").append(Pt_Virg);   // POINT_TYPE
                                    break;
                                case "D":                                    
                                    sbLine.append("Décollage").append(Pt_Virg);   // //POINT_TYPE
                                    break;
                                default:                                    
                                    sbLine.append("Inconnu").append(Pt_Virg);   // POINT_TYPE
                            }                              
                            sbLine.append("").append(Pt_Virg);   // POINT_DEPARTEMENT                             
                            sbLine.append(rs.getString(7)).append(Pt_Virg);   // POINT_ORIENTATION                            
                            sbLine.append("").append(Pt_Virg);  // POINT_PRATIQUE                            
                            sbLine.append("").append(Pt_Virg);   // SITE_ID                            
                            sbLine.append(rs.getString(4)).append(Pt_Virg);  // SITE_CP                            
                            sbLine.append(rs.getString(3)).append(Pt_Virg);    // SITE_COMMUNE                            
                            sbLine.append(rs.getString(5)).append(Pt_Virg);   // SITE_STRUCTURE_NOM                            
                            sbLine.append("").append(Pt_Virg);   // SITE_STRUCTURE_URL                            
                            sbLine.append("").append(Pt_Virg);   // SITE_URL                            
                            sbLine.append("").append(Pt_Virg);    // DIRECTION                            
                            sbLine.append(rs.getString(11)).append(RC);    // COMMENTAIRE
                            writer.write(sbLine.toString());                            
                        }
                    }
                    writer.close();
                    alertbox aInfo = new alertbox(myConfig.getLocale());
                    aInfo.alertInfo(i18n.tr("Export terminé")); 
                } catch (Exception e) {
                    res = 2;
                }
                                  
            }        
        }
    }
           
    private void fileImportCsv() {
        winChoose myWinChoose = new winChoose(myConfig, i18n);
        rdAll.setSelected(true);        
        pushAll();
    }
           
    /**
     * Is called by the main application to give a reference back to itself.
     * 
     * @param mainApp
     */
    public void setMainApp(Main mainApp) {
        this.mainApp = mainApp; 
        myConfig = mainApp.myConfig;
        i18n = I18nFactory.getI18n("","lang/Messages",SitesViewController.class.getClass().getClassLoader(),myConfig.getLocale(),0);
        winTraduction();
        this.mainApp.rootLayoutController.updateMsgBar("", false, 50); 
        iniTable();
        iniEventBar();       
        buildContextMenu();
    }    
    
    private void iniEventBar() {
        top_Menu.addEventHandler(MouseEvent.MOUSE_CLICKED,
            new EventHandler<MouseEvent>() {
                @Override public void handle(MouseEvent e) {                        
                    clicTop_Menu().show(top_Menu, e.getScreenX(), e.getScreenY());
                }
        });          
    }
    
    /**
     * Adding Context Menus, last paragraph
    *     http://docs.oracle.com/javafx/2/ui_controls/menu_controls.htm    
    */
    private ContextMenu clicTop_Menu()   {
        final ContextMenu cm = new ContextMenu();
        
        MenuItem cmItem0 = new MenuItem(i18n.tr("Modifier"));        
        cmItem0.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent e) {
                siteFormEdit();
            }            
        });
        cm.getItems().add(cmItem0);
        
        MenuItem cmItem1 = new MenuItem(i18n.tr("Ajouter"));        
        cmItem1.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent e) {
                siteFormAdd();
            }            
        });
        cm.getItems().add(cmItem1);
        
        MenuItem cmItem2 = new MenuItem(i18n.tr("Supprimer"));        
        cmItem2.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent e) {
                siteDelete();
            }            
        });
        cm.getItems().add(cmItem2);
        
        MenuItem cmItem22 = new MenuItem(i18n.tr("Doublons"));        
        cmItem22.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent e) {
                searchDuplicate();
            }            
        });
        cm.getItems().add(cmItem22);   
        
        MenuItem cmItem23 = new MenuItem(i18n.tr("Export waypoints"));        
        cmItem23.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent e) {
                alertbox aMsg = new alertbox(myConfig.getLocale());
                aMsg.alertInfo(i18n.tr("Non implémenté...")); 
            }            
        });
        cm.getItems().add(cmItem23);  

        MenuItem cmItem24 = new MenuItem(i18n.tr("Télécharger"));        
        cmItem24.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent e) {
                winSiteList myWin = new winSiteList(myConfig,i18n);
                // Refresh table with all sites
                rdAll.setSelected(true);
                pushAll(); 
            }            
        });
        cm.getItems().add(cmItem24);         
        
        MenuItem cmItem3 = new MenuItem(i18n.tr("Importer"));        
        cmItem3.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent e) {
                fileImportCsv();
            }            
        });
        cm.getItems().add(cmItem3);        
        
        MenuItem cmItem4 = new MenuItem(i18n.tr("Exporter"));        
        cmItem4.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent e) {
                exportCsv();
            }            
        });
        cm.getItems().add(cmItem4);        

        
        return cm;        
    }
    
    private void buildContextMenu() {        
        
        tableContextMenu = new ContextMenu();
        
        MenuItem cmItem0 = new MenuItem(i18n.tr("Modifier"));        
        cmItem0.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent e) {
                siteFormEdit();
            }            
        });
        tableContextMenu.getItems().add(cmItem0);
        
        MenuItem cmItem1 = new MenuItem(i18n.tr("Ajouter"));        
        cmItem1.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent e) {
                siteFormAdd();
            }            
        });
        tableContextMenu.getItems().add(cmItem1);
        
        MenuItem cmItem2 = new MenuItem(i18n.tr("Supprimer"));        
        cmItem2.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent e) {
                siteDelete();
            }            
        });
        tableContextMenu.getItems().add(cmItem2);
        
        MenuItem cmPlus = new MenuItem(i18n.tr("Plus..."));
        cmPlus.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent e) {
                Bounds boundsInScreen = top_Menu.localToScreen(top_Menu.getBoundsInLocal());
                clicTop_Menu().show(top_Menu, boundsInScreen.getMinX(), boundsInScreen.getMinY());
            }
        });
        tableContextMenu.getItems().add(cmPlus);         
     
    }
    
    /**
    * Translate labels of the window
    */
    private void winTraduction() {        
        
        nomCol.setText(i18n.tr("Nom"));
        villeCol.setText(i18n.tr("Localité"));
        cpCol.setText(i18n.tr("CP"));
        altCol.setText(i18n.tr("Alt"));
        orientCol.setText(i18n.tr("Orientation"));
        rdAll.setText(i18n.tr("Tous"));
        rdDeco.setText(i18n.tr("Décollage"));
        rdAttero.setText(i18n.tr("Atterissage"));
        rdNondef.setText(i18n.tr("Non défini"));     
        btnMap.setStyle("-fx-background-color: transparent;");
        Tooltip mapToolTip = new Tooltip();
        mapToolTip.setStyle(myConfig.getDecoToolTip());
        mapToolTip.setText(i18n.tr("Carte plein écran"));
        btnMap.setTooltip(mapToolTip);        
    }    
    
}

// Comptage site
// SELECT S_Type,Count(S_ID) FROM Site GROUP BY upper(S_Type)
