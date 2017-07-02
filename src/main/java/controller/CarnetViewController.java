/*
 * Copyright Gil THOMAS
 * Ce fichier fait partie intégrante du projet Logfly
 * Pour tous les détails sur la licence du projet Logfly
 * Consulter le fichier LICENSE distribué avec le code source
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
import photo.imgmanip;
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
    private Button btnStat;        
    @FXML
    private Button btnMap;
    @FXML
    private Button btnVisuGPS;  
    @FXML
    private Button btnScore;      
    @FXML
    private Button btnGEarth;      
    @FXML
    private ChoiceBox top_chbYear;    
    @FXML
    private ImageView top_Menu;    
    @FXML
    private WebView mapViewer;
    
    // Reference to the main application.
    private Main mainApp;
    
    // Localization
    private I18n i18n; 
    
    // Paramètres de configuration
    configProg myConfig;
    
    //START | SQLITE
    private static Statement stat;
    private PreparedStatement prep;
    //END | SQLITE
    
    traceGPS currTrace=null;
   // WebView viewMap = new WebView();    

    private ObservableList <Carnet> dataCarnet; 
    private ObservableList <String> dataYear; 
    
    @FXML
    private void initialize() {
        // On besoin d'initialiser i18n avant de lancer la construction du TableView
        // c'est la raison pour laquelle on déporte les instructions de construction dans iniTable() 
        // Cette procédure sera appelée après setMainApp()
        
    }
    
    private void iniTable() {
        dataCarnet = FXCollections.observableArrayList();
        dataYear = FXCollections.observableArrayList();
                       
        imgCol.setCellValueFactory(new PropertyValueFactory<Carnet, String>("camera"));
        dateCol.setCellValueFactory(new PropertyValueFactory<Carnet, String>("date"));
        heureCol.setCellValueFactory(new PropertyValueFactory<Carnet, String>("heure"));
        dureeCol.setCellValueFactory(new PropertyValueFactory<Carnet, String>("duree"));
        siteCol.setCellValueFactory(new PropertyValueFactory<Carnet, String>("site"));     
        
        // essai de changement d'apparence sur la valeur de la colonne Site
        dateCol.setCellFactory(column -> {
            return new TableCell<Carnet, String>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);  // obligatoire
                    if (item == null || empty) {
                        setText(null);
                        setStyle("");
                    } else {
                        setText(item); // Remplit la cellule avec la string
                        // Si besoin on peut charger toutes les infos de la table
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
        
                
        try {
                                    
            // On cherche les années figurant dans le carnet
            ResultSet rsYear = myConfig.getDbConn().createStatement().executeQuery("SELECT strftime('%Y',V_date) FROM Vol GROUP BY strftime('%Y',V_date) ORDER BY strftime('%Y',V_date) DESC");
            if (rsYear != null)  {             
                while (rsYear.next()) {
                    dataYear.add(rsYear.getString(1));
                }
                // Initialisation de la choicebox permettant de choisir l'année à visualiser
                top_chbYear.setItems(dataYear);
                top_chbYear.getSelectionModel().select(0);
                top_chbYear.setOnAction((event) -> {
                    String selectedYear = (String) top_chbYear.getSelectionModel().getSelectedItem();
                    newVolsContent(selectedYear);
                });
                // Récupération de l'année la plus récente
                String yearFiltre = (String) top_chbYear.getSelectionModel().getSelectedItem();
                
                // Listener pour gérer le changment de ligne et afficher les détails correspondants
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
        }
        
    }
    
    private void newVolsContent(String yearFiltre)  {
        boolean isCamera;
        String sReq = "SELECT * from Vol WHERE V_Date >= '"+yearFiltre+"-01-01 00:01' AND V_Date <= '"+yearFiltre+"-12-31 23:59' ORDER BY V_Date DESC";
        try {
            ResultSet rs = myConfig.getDbConn().createStatement().executeQuery(sReq);
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
                // On s'assure qu'il y a au moins un enregistrement
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
        i18n = I18nFactory.getI18n(Logfly.Main.class.getClass(), myConfig.getLocale());  
        winTraduction();
        iniTable();
    }
    
    private void supprimeVol() {
        int selectedIndex = tableVols.getSelectionModel().getSelectedIndex();
        if (selectedIndex >= 0) {
            Carnet selectedVol = tableVols.getSelectionModel().getSelectedItem();
            dialogbox dConfirm = new dialogbox();
            StringBuilder sbMsg = new StringBuilder(); 
            sbMsg.append(selectedVol.getDate()).append(" ").append(i18n.tr("Durée")).append(" : ").append(selectedVol.getDuree());                 
            if (dConfirm.YesNo(i18n.tr("Suppression du vol"), sbMsg.toString()))   {                
                String sReq = "DELETE FROM Vol WHERE V_ID = ?";
                try {
                    PreparedStatement pstmt = myConfig.getDbConn().prepareStatement(sReq);
                    pstmt.setInt(1, Integer.valueOf(selectedVol.getIdVol()));
                    pstmt.executeUpdate();    
                    tableVols.getItems().remove(selectedIndex);
                } catch (Exception e) {
                    alertbox aError = new alertbox();
                    aError.alertError(e.getMessage()); 
                }                                                
            }                                 
        } else {
            // Aucun vol sélectionné
            alertbox aError = new alertbox();
            aError.alertError(i18n.tr("Aucun vol sélectionné..."));                       
        }        
    }
            
    private void decodeVolCarnet(String idVol)  {
        
        Image dbImage = null;
        
        String sReq = "SELECT V_IGC,UTC,V_Date,V_Duree,V_sDuree,V_AltDeco,V_LatDeco,V_LongDeco,V_Site,V_Engin,V_Pays,V_Commentaire,V_Photos,V_League,V_Score FROM Vol WHERE V_ID = "+idVol;
        try {
            ResultSet rs =  myConfig.getDbConn().createStatement().executeQuery(sReq);
            if (rs != null)  { 
                if (rs.getString("V_IGC") != null && !rs.getString("V_IGC").equals(""))  {                        
                    currTrace = new traceGPS(rs.getString("V_IGC"), "IGC","",true);   // String pFichier, String pType, String pPath
                    if (currTrace.isDecodage()) {
                        // Comme dans xLogfly on réaffecte la trace avec la voile et le site
                        if (!rs.getString("V_Engin").equals(currTrace.getsVoile()))
                            currTrace.setsVoile(rs.getString("V_Engin"));
                        currTrace.setsSite(rs.getString("V_Site"));
                        // dans la db, si V_League est null, rs.getInt("V_League") ramène 0
                        // or pour compatibilité avec xLogfly, league FR a un index 0
                        // donc on teste le JSON                        
                        String sJSON = rs.getString("V_Score");                                               
                        if (sJSON != null && sJSON.contains("drawScore")) {
                            currTrace.setScore_Idx_League(rs.getInt("V_League"));
                            currTrace.setScore_JSON(rs.getString("V_Score"));
                            scoring currScore = new scoring(0);
                            int resScoring = currScore.decodeStrJson(currTrace);
                            if (resScoring == 0)
                                currTrace.setScored(true);
                            else {
                                alertbox aError = new alertbox();
                                aError.alertNumError(resScoring); 
                            }
                        } else {
                            currTrace.setScored(false);
                        }
                        // Est ce qu'il y avait un commentaire ?
                        String sComment = rs.getString("V_Commentaire");
                        if (sComment != null) {
                            currTrace.setComment(sComment);
                        }
                        // Est ce qu'il y avait une photo ?
                        String sPhoto = rs.getString("V_Photos");
                        if (sPhoto != null) {
                            // Pour affichage dans le webview
                            //currTrace.setPhoto(sPhoto);
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
                        alertbox decodageError = new alertbox();
                        decodageError.alertError(i18n.tr("Problème de décodage du fichier"));
                    }     
                } else {
                    // Pas de trace à afficher
                    displayNoIGC(rs);
                }
            }
        } catch ( Exception e ) {
            alertbox aError = new alertbox();
            aError.alertError(e.getClass().getName() + ": " + e.getMessage());                          
        }
        
        
    }
    
    private void displayNoIGC(ResultSet rs) throws SQLException {
        Image dbImage = null;
                
        currTrace = new traceGPS(null, "NIL","",true);  
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
        // Est ce qu'il y avait une photo ?
        String sPhoto = rs.getString("V_Photos");
        if (sPhoto != null) {            
            if (myConfig.isPhotoAuto()) {
                // Pour affichage dans une fenêtre externe
                imgmanip currImage = new imgmanip();
                dbImage = currImage.strToImage(sPhoto, 700, 700);  
            }
        }
        // Initialisation des paramètres de la carte
        // Pas de trace IGC, on place simplement le point de décollage
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
            /** ----- Debut Debug --------*/ 
            String sDebug = mapNoIGC.getMap_HTML();
            final Clipboard clipboard = Clipboard.getSystemClipboard();
            final ClipboardContent content = new ClipboardContent();
            content.putString(mapNoIGC.getMap_HTML()); 
            System.out.println("C'est dans le clipboard...");
            clipboard.setContent(content);
            /** ----- Fin Debug ---------*/
            // Delete cache for navigate back
            mapViewer.getEngine().load("about:blank");            
            mapViewer.getEngine().loadContent(mapNoIGC.getMap_HTML()); 
            if (dbImage != null) {
                winPhoto myPhoto = new winPhoto();    
                myPhoto.showDbPhoto(dbImage);
            }            
        }                
    }
    
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
            alertbox finOp = new alertbox();
            finOp.alertNumError(res);
        }        
    }
    
    private void showCarnetDetails(Carnet currCarnet) {
        if (currCarnet != null) {                                   
            decodeVolCarnet(currCarnet.getIdVol());
                        
            top_Menu.addEventHandler(MouseEvent.MOUSE_CLICKED,
                new EventHandler<MouseEvent>() {
                    @Override public void handle(MouseEvent e) {                        
                            clicTop_Menu().show(top_Menu, e.getScreenX(), e.getScreenY());
                    }
            });                       
        } else {
            // à implémenter
           
        }              
    }
                  
    /**
     * Ajout d'un menu contextuel fondé sur le dernier paragraphe Adding Context Menus
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
                winPoints myGrid = new winPoints();    
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
        /**
         * Il manque :
         * Modifier la voile
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
    
    
    /*
    * Procédure qui lancera l'affichage des stats simplifiées du carnet
    */
    @FXML
    private void clicTop_Stat() {
        alertbox aInfo = new alertbox();
        aInfo.alertInfo(i18n.tr("Statistiques de vol"));              
    }   
        
        
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
                /** ----- Debut Debug --------*/                 
                final Clipboard clipboard = Clipboard.getSystemClipboard();
                final ClipboardContent content = new ClipboardContent();
                content.putString(sHTML);            
                clipboard.setContent(content);                                
                /**------ Fin Debug --------- */
                viewMap.getEngine().loadContent(sHTML,"text/html");
                StackPane subRoot = new StackPane();
                subRoot.getChildren().add(anchorPane);
                Scene secondScene = new Scene(subRoot, 500, 400);
                Stage subStage = new Stage();
                // On veut que cette fenêtre soit modale
                subStage.initModality(Modality.APPLICATION_MODAL);
                subStage.setScene(secondScene); 
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
     * la trace a été téléchargée sur un serveur avec un nom de la forme 
     * YYYYMMDDHHMMSS_Aleatoire  [Aléatoire = nombre entre 1 et 1000]
     * @param webFicIGC 
     */   
    private void showVisuGPS(String webFicIGC)  {
        StringBuilder visuUrl = new StringBuilder();
        visuUrl.append(myConfig.getUrlVisu()).append(myConfig.getUrlLogflyIGC());
        visuUrl.append(webFicIGC);
        System.out.println(visuUrl.toString());
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
        Scene secondScene = new Scene(subRoot, 500, 400);
        Stage subStage = new Stage();
        // On veut que cette fenêtre soit modale
        subStage.initModality(Modality.APPLICATION_MODAL);
        subStage.setScene(secondScene); 
        subStage.setMaximized(true);
        subStage.show();       
    }

    /**
     * VisuGPS ne fonctione qu'avec une trace ayant une adresse http
     * runVisuGPS télécharge la trace sur un serveur en utilisant un script php
     * Ce script fait notamment le ménage sur le serveur pour les traces précédemment chargées
     * afin de ne pas surcharger l'espace disuqe du serveur
     */
    @FXML
    private void runVisuGPS() {
        if (currTrace.isDecodage()) { 
            webio myUpload = new webio();
            try {
                String uploadUrl = myConfig.getUrlLogflyIGC()+"jtransfert.php";
                if (myUpload.testURL(uploadUrl) == 200)  {
                    byte[] igcBytes = currTrace.exportBytes();
                    if (igcBytes.length > 100)  {
                        System.out.println("Lg avant transfert : "+igcBytes.length);
                        String webFicIGC = myUpload.httpUploadIgc(igcBytes, uploadUrl);
                        if (webFicIGC != null) {
                            showVisuGPS(webFicIGC);
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

    @FXML
    private void showScore() {
        if (currTrace.isDecodage()) {
            if (currTrace.isScored())  {
                showFullMap();                
            } else {
                // On lance le calcul avec le module externe points (classe scoring) dont on DOIT attendre la fin d'execution
                // C'est la classe scoring qui reviendra vers le controller en appellant scoreReturn()
                // D'où la nécessité de mettre un pont avec ce controller
                scoring currScore = new scoring(this,1);  
                currScore.start(currTrace, myConfig.getIdxLeague());                            
            }
        }
    }
    
    private void gestionComment() {
        if (tableVols.getSelectionModel().getSelectedItem().Comment.getValue()) {
            dialogbox actionReq = new dialogbox();
            int actionType = actionReq.twoChoices(i18n.tr("Commentaire"), i18n.tr("Que voulez- vous faire ?"), i18n.tr("Supprimer"), i18n.tr("Changer"), i18n.tr("Annuler"));
            switch (actionType) {
                case 1:
                    // Suppression
                    delComment();
                    break;
                case 2:
                    // Addition / Modidification
                    majComment();
                    break;
            }
        } else {
            majComment();
        }
        
    }
    
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
                // Pour rafrachir la carte
                decodeVolCarnet(currCarnet.getIdVol());
            } catch (Exception e) {
                alertbox aError = new alertbox();
                aError.alertError(e.getMessage()); 
            }  
            tableVols.getSelectionModel().getSelectedItem().comTexte.set(null);
            tableVols.getSelectionModel().getSelectedItem().Comment.set(false);                     
            tableVols.refresh();            
        }               
    }
    
    private void majComment() {
        String commentStr = tableVols.getSelectionModel().getSelectedItem().comTexte.getValue();
        winComment myComment = new winComment(commentStr,i18n);                
            alertbox myInfo = new alertbox();
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
                // Pour rafrachir la carte
                decodeVolCarnet(currCarnet.getIdVol());
            } catch (Exception e) {
                alertbox aError = new alertbox();
                aError.alertError(e.getMessage()); 
            }  
            tableVols.getSelectionModel().getSelectedItem().comTexte.set(myComment.getCommentTxt());
            tableVols.getSelectionModel().getSelectedItem().Comment.set(true);                    
            tableVols.refresh();            
        }         
    }
    
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
            } catch (Exception e) {
                alertbox aError = new alertbox();
                aError.alertError(e.getMessage()); 
            }  
            tableVols.getSelectionModel().getSelectedItem().camera.set(null);
            tableVols.getSelectionModel().getSelectedItem().Photo.set(false);                    
            tableVols.refresh();            
        }        
    }
    
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
                    // Une réduction directe au chargement comme
                    // Image currImg = new Image(selectedFile.getAbsolutePath(), 700, 700, true, true);
                    // donnait des résultats désastreux
                    BufferedImage redBufImage = currImage.reduitPhoto(bufferedImage,700,700);
                    String strImage = currImage.imgToBase64String(redBufImage, "jpg");
                    if (strImage != null)  {
                        currCarnet.setCamera(strImage);
                        // Stockage db
                        String sReq = "UPDATE Vol SET V_Photos= ? WHERE V_ID = ?";                    
                        try {
                            PreparedStatement pstmt = myConfig.getDbConn().prepareStatement(sReq);
                            pstmt.setString(1,strImage); 
                            pstmt.setInt(2, Integer.valueOf(currCarnet.getIdVol()));
                            pstmt.executeUpdate();    
                        } catch (Exception e) {
                            alertbox aError = new alertbox();
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
                    alertbox aError = new alertbox();
                    aError.alertError(ex.getMessage());                     
                }            
            }
        }
    }
    
    public void scoreReturn(int pRetour) {
        // Si la trace n'a pas été évaluée, le message d'erreur a été envoyé par la classe scoring
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
                switch (pRetour) {
                    case 1:
                        showFullMap();
                        break;   
                    case 2:
                        showWinGE();
                        break;  
                }                
            } catch (Exception e) {
                alertbox aError = new alertbox();
                aError.alertError(e.getMessage()); 
            }       
        }         
    }

    @FXML
    private void askWinGE() {
        // La trace sera systématiquement scorée avant génération du Kml
        if (currTrace.isDecodage()) {
            if (currTrace.isScored())  {
                // Attention sous Linux on plante pas de score possible...
                showWinGE();                
            } else {
                // On lance le calcul avec le module externe points (classe scoring) dont on DOIT attendre la fin d'execution
                // C'est la classe scoring qui reviendra vers le controller en appellant scoreReturn()
                // D'où la nécessité de mettre un pont avec ce controller
                scoring currScore = new scoring(this,2);  
                currScore.start(currTrace, myConfig.getIdxLeague());                            
            }
        }        
    }
                
    private boolean showWinGE() {
        try {                                  
            // Load the fxml file and create a new stage for the popup dialog.
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(Main.class.getResource("view/KmlView.fxml")); 
            
            AnchorPane page = (AnchorPane) loader.load();
            // Creation de la scène pour la fenêtre de Configuration
            Stage dialogStage = new Stage();
            dialogStage.setTitle(i18n.tr("Génération fichier kml"));
            dialogStage.initModality(Modality.WINDOW_MODAL);       
            dialogStage.initOwner(mainApp.getPrimaryStage());
            Scene scene = new Scene(page);
            dialogStage.setScene(scene);

            // Initialisation d'un pont de communication entre le controller CarnetView et KmlView
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
                            alertbox aError = new alertbox();
                            aError.alertNumError(1030); 
                        }       
                    }
                } else {
                    alertbox aInfo = new alertbox();
                    aInfo.alertInfo(i18n.tr("Génération du fichier terminée")); 
                }
            } else {
                alertbox aError = new alertbox();
                aError.alertNumError(currKml.getErrorCode()); 
            }
        }
    }

    private void winTraduction() {
        dateCol.setText(i18n.tr("Date"));
        heureCol.setText(i18n.tr("Heure"));
        dureeCol.setText(i18n.tr("Durée"));
        siteCol.setText(i18n.tr("Site"));    
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
        
        btnVisuGPS.setStyle("-fx-background-color: transparent;"); 
        Tooltip visuToolTip = new Tooltip();
        visuToolTip.setStyle("-fx-background-color: linear-gradient(#e2ecfe, #99bcfd);");
        visuToolTip.setText(i18n.tr("Affichage VisuGPS"));
        btnVisuGPS.setTooltip(visuToolTip);
        
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
