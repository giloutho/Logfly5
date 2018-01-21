/*
 * Copyright Gil THOMAS
 * This file forms an integral part of Logfly project
 * See the LICENSE file distributed with source code
 * for details of Logfly licence project
 */
package controller;

import Logfly.Main;
import dialogues.alertbox;
import igc.pointIGC;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.logging.Level;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.RadioButton;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.ImageView;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.web.WebView;
import leaflet.map_markers;
import littlewins.winPhoto;
import model.Sitemodel;
import org.xnap.commons.i18n.I18n;
import org.xnap.commons.i18n.I18nFactory;
import settings.configProg;
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
    private Button btnSearch;      
    @FXML
    private RadioButton rdAll;    
    @FXML
    private RadioButton rdDeco; 
    @FXML
    private RadioButton rdAttero;
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
    
    //START | SQLITE
    private static Statement stat;
    private PreparedStatement prep;
    //END | SQLITE    
    
    private ObservableList <Sitemodel> dataSites; 
    
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
    }    
    
    /**
     * Initialization of the TableView
     * Fill the table with data from db
     */
    private void iniTable() {
        
        dataSites = FXCollections.observableArrayList();     
        
        tableSites.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
                       
        nomCol.setCellValueFactory(new PropertyValueFactory<Sitemodel, String>("nom"));
        villeCol.setCellValueFactory(new PropertyValueFactory<Sitemodel, String>("ville"));
        cpCol.setCellValueFactory(new PropertyValueFactory<Sitemodel, String>("cp"));        
        altCol.setCellValueFactory(new PropertyValueFactory<Sitemodel, String>("alt"));     
        orientCol.setCellValueFactory(new PropertyValueFactory<Sitemodel, String>("orient"));
        
        // Listener for line changes and  display relevant details
        tableSites.getSelectionModel().selectedItemProperty().addListener(
        (observable, oldValue, newValue) -> showSiteMap((Sitemodel) newValue));          
        
        fillTable("SELECT S_ID, S_Nom, S_Localite, S_CP,S_Alti, S_Orientation,S_Type FROM Site ORDER BY S_Nom");
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
                tableSites.getItems().clear();  
                while (rs.next()) {
                    Sitemodel si = new Sitemodel();  
                    si.setIdSite(rs.getString("S_ID"));
                    si.setNom(rs.getString("S_Nom"));
                    si.setVille(rs.getString("S_Localite"));
                    si.setCp(rs.getString("S_CP"));
                    si.setAlt(rs.getString("S_Alti"));
                    si.setOrient(rs.getString("S_Orientation"));  
                    si.setType(rs.getString("S_Type"));                     
                    dataSites.add(si);                
                }   
                tableSites.setItems(dataSites); 
                if (tableSites.getItems().size() > 0) {
                    tableSites.getSelectionModel().select(5);                    
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

    @FXML
    private void pushAll() {
        if (rdAll.isSelected()) {
            String sReq = "SELECT S_ID, S_Nom, S_Localite, S_CP,S_Alti, S_Orientation,S_Type FROM Site ORDER BY S_Nom";
            fillTable(sReq);
        }
    }       

    @FXML
    private void pushDeco() {
        if (rdDeco.isSelected()) {
            String sReq = "SELECT S_ID, S_Nom, S_Localite, S_CP,S_Alti, S_Orientation,S_Type FROM Site WHERE S_Type = 'D' ORDER BY S_Nom";
            fillTable(sReq);
        }
    }       
    
    @FXML
    private void pushAttero() {
        if (rdAttero.isSelected()) {
            alertbox aError = new alertbox(myConfig.getLocale());
            aError.alertError("Attero enfoncé");   
        }
    }       
    
    
    /**
     * Db search request
     */
    @FXML
    private void askSearch() {
        // to do      
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
       // iniEventBar();     
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
        btnSearch.setStyle("-fx-background-color: transparent;");                
    }    
    
}

// Comptage site
// SELECT S_Type,Count(S_ID) FROM Site GROUP BY upper(S_Type)
