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
import geoutils.position;
import igc.pointIGC;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.logging.Level;
import java.util.regex.Pattern;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.Tooltip;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import leaflet.map_markers;
import leaflet.map_photos;
import littlewins.winCoord;
import littlewins.winFileChoose;
import littlewins.winMapPhotoPoint;
import littlewins.winPhoto;
import littlewins.winTrackFile;
import littlewins.winWebFile;
import model.Carnet;
import model.Photo;
import org.xnap.commons.i18n.I18n;
import org.xnap.commons.i18n.I18nFactory;
import photos.exifReader;
import photos.filesUtils;
import settings.configProg;
import settings.fileType;
import systemio.checking;
import systemio.mylogging;
import static systemio.textio.getFileExtension;
import trackgps.traceGPS;

/**
 *
 * @author gil
 */
public class PhotoTagViewController {
    
    @FXML
    private TableView<Photo> tablePhotos;
    @FXML
    private TableColumn<Photo, Boolean> colCheck;
    @FXML
    private TableColumn<Photo, String> colName;    
    @FXML
    private TableColumn<Photo, String> colDate;
    @FXML
    private TableColumn<Photo, String> colHour;
    @FXML
    private TableColumn<Photo, String> colGps;
    @FXML
    private Button btnFolder;        
    @FXML
    private Button btnTrack;  
    @FXML
    private HBox hbInfo;
    @FXML 
    private Label lbFlightDate;
    @FXML 
    private Label lbTakeoff;
    @FXML 
    private Label lbLanding;  
    @FXML
    private ImageView top_Menu;
    @FXML
    private Button btnOnTrack;  
    @FXML
    private Button btnAlbum;  
    @FXML
    private StackPane stackP;
    @FXML
    private SplitPane splitPhotos;
    
    // Localization
    private I18n i18n; 
    
    // Configuration settings
    configProg myConfig;
    // Reference to the main application.
    private Main mainApp;
    
    private StringBuilder sbError;
                
    private RootLayoutController rootController; 
    
    private Stage dialogStage;
    
    private ArrayList<String> photoPathList;
    private ObservableList <Photo> setPhoto; 
    private String lbTakeOffBegin;
    private String lbLandingBegin;
    private String lbGPSTimeBegin;
    private String lbPhotoTimeBegin;
    
    private File photoDirectory;
    private Pattern validIntText = Pattern.compile("-?(\\d*)");
    
    //========= pour mise au point
    private LocalDateTime DT_Deco;   
    private LocalDateTime DT_Attero;
    private LocalDateTime DT_GPS;
    private LocalDateTime DT_Photo;
    private DateTimeFormatter dtfExif = DateTimeFormatter.ofPattern("yyyy:MM:dd HH:mm:ss");
    private DateTimeFormatter dtfDay = DateTimeFormatter.ofPattern("dd/MM/yy");
    private DateTimeFormatter dtfHour = DateTimeFormatter.ofPattern("HH:mm:ss");
    private ContextMenu tableContextMenu;
    private traceGPS extTrack;
    private boolean trackIsPresent = false;
    private StringBuilder statusInfo;
    private MenuItem cmFullMap;
    private BooleanProperty fullMapMenu = new SimpleBooleanProperty(false);
    
    @FXML
    private void initialize() {
                
        setPhoto = FXCollections.observableArrayList();       
              
        colName.setCellValueFactory(new PropertyValueFactory<Photo, String>("fileName"));
        colDate.setCellValueFactory(new PropertyValueFactory<Photo, String>("date"));
        colDate.setStyle("-fx-alignment: center-right;");
        colHour.setCellValueFactory(new PropertyValueFactory<Photo, String>("heure"));
        colHour.setStyle("-fx-alignment: center-right;");
        colGps.setCellValueFactory(new PropertyValueFactory<Photo, String>("gpsTag"));   
        colGps.setStyle("-fx-alignment: center;");
        colCheck.setCellValueFactory(new PropertyValueFactory<Photo,Boolean>("checked"));
        colCheck.setCellFactory( CheckBoxTableCell.forTableColumn( colCheck ) );          
        
        // Listener for line changes and  display relevant details
        tablePhotos.getSelectionModel().selectedItemProperty().addListener(
        (observable, oldValue, newValue) -> showPhoto((Photo) newValue));  
        
        top_Menu.addEventHandler(MouseEvent.MOUSE_CLICKED,
            new EventHandler<MouseEvent>() {
                @Override public void handle(MouseEvent e) {                        
                        clicTop_Menu().show(top_Menu, e.getScreenX(), e.getScreenY());
                }
        });  
        
        hbInfo.setVisible(false);
        splitPhotos.setVisible(false);
        fullMapMenu.set(false);        
    }    
    
    /**
    * Adding Context Menus, last paragraph
    *     http://docs.oracle.com/javafx/2/ui_controls/menu_controls.htm    
    */
    private ContextMenu clicTop_Menu()   {
        final ContextMenu cm = new ContextMenu();
        
        MenuItem cmItem0 = new MenuItem(i18n.tr("Track content"));      
        cmItem0.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent e) {
                winTrackFile myTrace = new winTrackFile(extTrack.getFicIGC());            
            }
        });
        cm.getItems().add(cmItem0); 
                            
        cmFullMap.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent e) {
                showFullMap();
            }            
        });
        cm.getItems().add(cmFullMap);     
        // binding explanations : http://bekwam.blogspot.fr/2015/08/disabling-menuitems-with-binding.html
        cmFullMap.disableProperty().bind(fullMapMenu.not());    
        
        
        MenuItem cmItem2 = new MenuItem(i18n.tr("Delete geotag folder"));        
        cmItem2.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent e) {
                deleteGeoFolder();
            }            
        });
        cm.getItems().add(cmItem2);        
                
        return cm;
    }
    
    
    private void buildContextMenu() {
        
        tableContextMenu = new ContextMenu();
        
        MenuItem cmItem0 = new MenuItem(i18n.tr("Show full size"));
        cmItem0.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent e) {
                showFullPhoto();
            }
        });
        tableContextMenu.getItems().add(cmItem0);                        
        
        MenuItem cmItem1 = new MenuItem(i18n.tr("Show on map"));        
        cmItem1.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent e) {
                showOnMap();
            }            
        });
        tableContextMenu.getItems().add(cmItem1);
        
        MenuItem cmItem5 = new MenuItem(i18n.tr("Adjust on the track"));        
        cmItem5.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent e) {
                showForAdjust();
            }            
        });
        tableContextMenu.getItems().add(cmItem5);        
        
        MenuItem cmItem2 = new MenuItem(i18n.tr("Display infos"));
        cmItem2.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent e) {
                displayExifInfos();
            }
        });
        tableContextMenu.getItems().add(cmItem2);         
        
        MenuItem cmItem3 = new MenuItem(i18n.tr("Manually geotag"));
        cmItem3.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent e) {
               manualGeoTag();
            }
        });
        tableContextMenu.getItems().add(cmItem3);
        
        MenuItem cmItem4 = new MenuItem(i18n.tr("Remove geotag"));
        cmItem4.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent e) {
                removeGeoTag();
            }
        });
        tableContextMenu.getItems().add(cmItem4);        
             
    }    
    
    @FXML
    private void handleChoiceFolder(ActionEvent event) {
        choiceFolder();     
    }
    
    @FXML
    private void handleChoiceTrack(ActionEvent event) {
        winFileChoose wf = new winFileChoose(myConfig, i18n, fileType.IgcGpx, null);  
        File selectedFile = wf.getSelectedFile();        
        if(selectedFile != null && selectedFile.exists()){ 
            String extension = getFileExtension(selectedFile);
            if (extension.equals("IGC") || extension.equals("igc") || extension.equals("GPX") || extension.equals("gpx")) {
                selectTrack(selectedFile);
            }
        }
    }   
        
    @FXML 
    private void handlePhotosOnTrack(ActionEvent event) {
        placePhotosOnTrack();
    }
    
    @FXML 
    private void handlePhotosAlbum(ActionEvent event) {
        placePhotosOnAlbum();
    }
    
    private void choiceFolder() {
        DirectoryChooser folderChooser = new DirectoryChooser();
        File selectedFolder = folderChooser.showDialog(dialogStage);        
        if(selectedFolder != null){      
            btnOnTrack.setDisable(true);
            displayPhotos(selectedFolder);             
        }  
    }
    
    private void displayPhotos(File fPhotos)  {
        
        try {
            photoPathList = new ArrayList<>(); 
            listPhotoFiles(fPhotos);
            if (photoPathList.size() > 0) {
                photoDirectory = fPhotos;
                btnAlbum.setDisable(false);
                fillTableData();
                splitPhotos.setVisible(true);
                statusInfo = new StringBuilder();
                statusInfo.append(fPhotos.getAbsolutePath()).append(" ").append(String.valueOf(photoPathList.size())).append(" ").append(i18n.tr("photos"));
                mainApp.rootLayoutController.updateMsgBar(statusInfo.toString(), true, 60);
            } else {
                alertbox aError = new alertbox(myConfig.getLocale());
                aError.alertInfo(i18n.tr("No photos to display"));               
                statusInfo = new StringBuilder();
                this.mainApp.rootLayoutController.updateMsgBar("", false, 50); 
                splitPhotos.setVisible(false);
            }            
        } catch (Exception e) {
            sbError = new StringBuilder(this.getClass().getName()+"."+Thread.currentThread().getStackTrace()[1].getMethodName());
            sbError.append("\r\n").append(fPhotos.getAbsolutePath());
            mylogging.log(Level.SEVERE, sbError.toString());  
        }        
    }               
    
    /**
     * Recursive track search in selected folder and sub folders
     * @param dir
     * @throws Exception 
     */
    private void listPhotoFiles(File dir) throws Exception {              
        File[] files = dir.listFiles();
        for (int i = 0; i < files.length; i++) {
            String fileName = files[i].getName();
            // put in your filter here
            if (fileName.endsWith(".jpg") || fileName.endsWith(".JPG")) {                                   
                if (files[i].isFile()) {
                    photoPathList.add(files[i].getPath());       
                }
            }
            if (files[i].isDirectory()) {
                listPhotoFiles(files[i]);
            }
        }
    }   
        
    private void offsetCompute( Photo selectedPhoto, int idxPoint) {
        
        pointIGC trackPoint = extTrack.Tb_Good_Points.get(idxPoint);        
        int diffSec = (int) ChronoUnit.SECONDS.between(selectedPhoto.getLdtOriginal(),trackPoint.dHeure);
        offsetDisplay(diffSec);        
    }
        
    private void offsetDisplay(int newOffset) {
        
        int nbPhotosTrack = 0;
        DT_Photo = DT_GPS.minusSeconds(newOffset);
        // rafraichissement de la table
        for (Photo aPhoto : setPhoto) {                        
            LocalDateTime ldt = aPhoto.getLdtOriginal();
            ldt = ldt.plusSeconds(newOffset);
            aPhoto.setModifiedDateTime(dtfExif.format(ldt));
            aPhoto.setDate(dtfDay.format(ldt));
            aPhoto.setHeure(dtfHour.format(ldt));
            if (ldt.isAfter(DT_Deco) && ldt.isBefore(DT_Attero)) {
                aPhoto.setInRange(Boolean.TRUE);
                aPhoto.setChecked(true);
                int idx = trackEvaluation(ldt);
                aPhoto.setIdx(idx);
                nbPhotosTrack++;
            } else {
                aPhoto.setInRange(Boolean.FALSE);    
                aPhoto.setChecked(false);
            }
        }
        tablePhotos.refresh();
        if (nbPhotosTrack > 0)
            btnOnTrack.setDisable(false);
        else
            btnOnTrack.setDisable(true);
    }
    
    private void fillTableData() {

        int nbPhotos = 0;
        int nbPhotosTrack = 0;
        
        tablePhotos.getItems().clear();        
        // Loop on arraylist of track files with path 
        for (String sPhotoPath : photoPathList) {
            File fPhoto = new File(sPhotoPath);
            if(fPhoto.exists() && fPhoto.isFile()) {     
                try {
                    exifReader metaPhoto = new exifReader(i18n);
                    metaPhoto.decodeGPS(fPhoto);
                    if(metaPhoto.isInfoExif()) {
                        LocalDateTime ldt = metaPhoto.getLdtOriginal();
                        Photo ph = new Photo();                        
                        ph.setFileName(fPhoto.getName());
                        ph.setFilePath(fPhoto.getAbsolutePath());
                        ph.setDate(dtfDay.format(ldt));
                        ph.setHeure(dtfHour.format(ldt));
                        ph.setLdtOriginal(ldt);
                        ph.setOriginalDateTime(metaPhoto.getTagDateTimeOriginal());
                        ph.setModifiedDateTime(metaPhoto.getTagDateTimeOriginal());
                        if (metaPhoto.isInfoGPS()) {
                            ph.setGpsTag("X");
                            ph.setLatitude(metaPhoto.getTagLatitude());
                            ph.setLongitude(metaPhoto.getTagLongitude());                 
                        }
                        if (trackIsPresent) {
                            if (ldt.isAfter(DT_Deco) && ldt.isBefore(DT_Attero)) {
                                ph.setInRange(Boolean.TRUE);                                
                                ph.setChecked(true);
                                int idx = trackEvaluation(ldt);
                                ph.setIdx(idx);
                                nbPhotosTrack++;
                            } else if (metaPhoto.isInfoGPS()){
                                ph.setInRange(Boolean.FALSE);
                                double dLat = ph.getLatitude();
                                double dLong = ph.getLongitude();
                                if (dLat < extTrack.getLatMaxi() && dLat > extTrack.getLatMini() && dLong > extTrack.getLongMini() && dLong < extTrack.getLongMaxi())
                                    ph.setChecked(true);                                
                            } else 
                                ph.setInRange(Boolean.FALSE);
                            //https://stackoverflow.com/questions/27468546/how-to-use-simpleintegerproperty-in-javafx                        
                        } else {
                            ph.setInRange(Boolean.FALSE);
                        }
                        setPhoto.add(ph);       
                        nbPhotos++;
                    }
                } catch (Exception e) {
                    sbError = new StringBuilder(this.getClass().getName()+"."+Thread.currentThread().getStackTrace()[1].getMethodName());
                    sbError.append("\r\n").append(fPhoto.getAbsolutePath());
                    mylogging.log(Level.SEVERE, sbError.toString());  
                }                        
            }
            if (nbPhotosTrack > 0) btnOnTrack.setDisable(false);
        }
        Comparator<? super Photo> comparatorDate = new Comparator<Photo>() {
            @Override
            public int compare(Photo p1, Photo p2) {
                // order asc -> p1... .compareTo(p2...);
                // order desc -> p2... .compareTo(p1...);
                return p1.getModifiedDateTime().compareTo(p2.getModifiedDateTime());
            }
        };                
        FXCollections.sort(setPhoto, comparatorDate);           
        tablePhotos.setItems(setPhoto); 
        // At least one record
        if (tablePhotos.getItems().size() > 0) {
            tablePhotos.setRowFactory(tbrow -> new TableRow<Photo>() {
                @Override
                public void updateItem(Photo item, boolean empty) {
                    super.updateItem(item, empty) ;
                    if (item == null) {
                        setStyle("");       
                    } else if (item.getInrange()) {
                        setStyle("-fx-background-color: lightgreen;");
                    } else {
                        setStyle("-fx-background-color: lightsalmon;");
                    }
                }
            });            
            tablePhotos.getSelectionModel().select(0);   
            
        // ------------ Special Procedure -------------
        // with our first code (more simple), context menu is always displayed
        // Escape touch is necessary to hide context menu
        // with this function, context menu is hidden when a line is unselected 
        
        buildContextMenu();
        
        // Cette procedure provient de https://kubos.cz/2016/04/01/javafx-dynamic-context-menu-on-treeview.html
        tablePhotos.addEventHandler(MouseEvent.MOUSE_RELEASED, e->{ 
            if (e.getButton()==MouseButton.SECONDARY) { 
                Photo selectedPhoto = tablePhotos.getSelectionModel().getSelectedItem();
                
                //item is selected - this prevents fail when clicking on empty space 
                if (selectedPhoto!=null) { 
                    //open context menu on current screen position  
                    tableContextMenu.show(tablePhotos, e.getScreenX(), e.getScreenY());
                } 
            } else { 
                //any other click cause hiding menu 
                tableContextMenu.hide(); 
            } 
        });      
        // --------------------------------------------------------------------------------------            
            
        }            
    }    
    
    private void placePhotosOnTrack() {
        int nbPhotos = 0;
        dialogbox dConfirm = new dialogbox(i18n);                
        if (dConfirm.YesNo(i18n.tr("Geotaging"), i18n.tr("Ready to process photos"))) {            
            filesUtils fUtils = new filesUtils(myConfig, extTrack);
            if (fUtils.createFolder() == 0) {
                for (Photo aPhoto : setPhoto) {
                    if (aPhoto.getChecked()) {
                        try {
                            Path originalPath = Paths.get(aPhoto.getFilePath());
                            File startPh = new File(aPhoto.getFilePath());                  
                            Path destinationPath = Paths.get(fUtils.getDestinationPath()+aPhoto.getFileName());                            
                            if (aPhoto.getInrange()) {
                                // if a photo is in track range, it's geotagged and copy in destination folder
                                Files.copy(originalPath,destinationPath,REPLACE_EXISTING);
                                int trackIndex = aPhoto.getIdx();
                                try {
                                    pointIGC currPoint = extTrack.Tb_Good_Points.get(trackIndex);
                                    position myPos = new position(); 
                                    myPos.setLatitudeDd(currPoint.getLatitude());
                                    myPos.setLongitudeDd(currPoint.getLongitude());
                                    File destPh = destinationPath.toFile();
                                    if (destPh.exists()) {
                                        if (photos.exifWriter.setExifGPSTag(startPh,destPh,myPos) == 0) nbPhotos++;
                                    } else {
                                        sbError = new StringBuilder(this.getClass().getName()+"."+Thread.currentThread().getStackTrace()[1].getMethodName());
                                        sbError.append("\r\n").append(destPh).append(" doesn't exists");
                                        mylogging.log(Level.SEVERE, sbError.toString());                                  
                                    }                                      
                                } catch (Exception e) {
                                    sbError = new StringBuilder(this.getClass().getName()+"."+Thread.currentThread().getStackTrace()[1].getMethodName());
                                    sbError.append("\r\n").append("Problem during geotagging of ").append(aPhoto.getFileName());
                                    mylogging.log(Level.SEVERE, sbError.toString());                                                   
                                }
                            } else {
                                // if a photo isn't in track range, it's copied in destination folder only if the photo is geotagged
                                if (aPhoto.getGpsTag().equals("X")) {
                                    Files.copy(originalPath,destinationPath,REPLACE_EXISTING);
                                    nbPhotos++;
                                }
                            } 
                        } catch (Exception ex) {
                            sbError = new StringBuilder(this.getClass().getName()+"."+Thread.currentThread().getStackTrace()[1].getMethodName());
                            sbError.append("\r\n").append(ex.toString());                            
                            mylogging.log(Level.SEVERE, sbError.toString());                              
                        }
                    }
                }
                if (nbPhotos > 0) {
                    String idDb = extTrack.getIdDatabase();
                    if ( idDb != null) updateComment(idDb, true);
                    String sText= String.valueOf(nbPhotos)+" "+i18n.tr("photos processed");
                    if (dConfirm.YesNo(i18n.tr("See the map"), sText)) {    
                        map_photos resultMap = new map_photos(extTrack, new File(fUtils.getDestinationPath()), myConfig, true);
                        if (resultMap.isMap_OK()) {
                            winWebFile winMap = new winWebFile(myConfig, resultMap.getMap_HTML(),false);
                        } 
                    }
                }
            } else {
                sbError = new StringBuilder(this.getClass().getName()+"."+Thread.currentThread().getStackTrace()[1].getMethodName());
                sbError.append("\r\n").append("Unable to create photo folder");
                mylogging.log(Level.SEVERE, sbError.toString());  
            }            
        }
    }
    
    private void updateComment(String idDb, boolean addTag) {
        
        Statement stmt = null;
        ResultSet rs = null;
        String sReq = "SELECT V_Commentaire FROM Vol WHERE V_ID = "+idDb;
        try {
            stmt = myConfig.getDbConn().createStatement();
            rs =  stmt.executeQuery(sReq);
            if (rs != null)  {                 
                String sComment = rs.getString("V_Commentaire");
                if (sComment != null) {
                    if (addTag) {
                        sComment = "[Photos]<br />"+sComment;
                    } else {
                        sComment = sComment.replace("[Photos]<br />", "");
                    }
                } else 
                     if (addTag) sComment = "[Photos]<br />";
                sReq = "UPDATE Vol SET V_Commentaire= ? WHERE V_ID = ?"; 
                PreparedStatement pstmt = myConfig.getDbConn().prepareStatement(sReq);
                pstmt.setString(1,sComment); 
                pstmt.setString(2, idDb);
                pstmt.executeUpdate();  
                pstmt.close();                                
            }
        } catch (Exception e) {
            sbError = new StringBuilder(this.getClass().getName()+"."+Thread.currentThread().getStackTrace()[1].getMethodName());
            sbError.append("\r\n").append("Unable to update record ").append(idDb);
            mylogging.log(Level.SEVERE, sbError.toString());                            
        }             
    }
    
    private void placePhotosOnAlbum() {
        int nbPhotos = 0;
        dialogbox dConfirm = new dialogbox(i18n);                
        if (dConfirm.YesNo(i18n.tr("Album generation"), i18n.tr("Ready to process photos"))) {            
            filesUtils fUtils = new filesUtils(myConfig, extTrack);
            if (fUtils.createFolder() == 0) {
                for (Photo aPhoto : setPhoto) {
                    if (aPhoto.getChecked()) {
                        try {
                            Path originalPath = Paths.get(aPhoto.getFilePath());
                            File startPh = new File(aPhoto.getFilePath());                  
                            Path destinationPath = Paths.get(fUtils.getDestinationPath()+aPhoto.getFileName());                            
                            Files.copy(originalPath,destinationPath,REPLACE_EXISTING);
                            nbPhotos++;                                
                        } catch (Exception ex) {
                            sbError = new StringBuilder(this.getClass().getName()+"."+Thread.currentThread().getStackTrace()[1].getMethodName());
                            sbError.append("\r\n").append("Problem during copy of ").append(aPhoto.getFileName());  
                            sbError.append("\r\n").append(ex.toString());                            
                            mylogging.log(Level.SEVERE, sbError.toString());                              
                        }
                    }
                }
                if (nbPhotos > 0) {
                    String sTitle = String.valueOf(nbPhotos)+" "+i18n.tr("photos processed");
                    String sText = i18n.tr("Photo gallery is available in full map track");
                    alertbox aInfo = new alertbox(myConfig.getLocale());
                    aInfo.alertWithTitle(sTitle, sText);
                }
            } else {
                sbError = new StringBuilder(this.getClass().getName()+"."+Thread.currentThread().getStackTrace()[1].getMethodName());
                sbError.append("\r\n").append("Unable to create photo folder");
                mylogging.log(Level.SEVERE, sbError.toString());  
            }            
        }        
    }
    
    private void deleteGeoFolder() {
    
        filesUtils fUtils = new filesUtils(myConfig, extTrack);
        if (fUtils.checkFolder(false)) {
            dialogbox dConfirm = new dialogbox(i18n);
            if (dConfirm.YesNo(i18n.tr("Geotag folder"), i18n.tr("Delete this folder")))   {
                File folderToDelete = new File(fUtils.getDestinationPath());
                String[]entries = folderToDelete.list();
                for(String s: entries){
                    File currentFile = new File(folderToDelete.getPath(),s);
                    currentFile.delete();
                }
                folderToDelete.delete();
                String idDb = extTrack.getIdDatabase();
                if (idDb != null) updateComment(idDb, false);
                hbInfo.setVisible(false);
                statusInfo = new StringBuilder();
                this.mainApp.rootLayoutController.updateMsgBar("", false, 50); 
                splitPhotos.setVisible(false);
                fullMapMenu.set(false);  
            }                    
        }                
    }
            
    private void showPhoto(Photo currPhoto) {
        if (currPhoto != null) {                                   
            File fileImg = new File(currPhoto.getFilePath());                 
            Image img = new Image(fileImg.toURI().toString());           
            ImageView mainImageView = new ImageView(img);
            mainImageView.setFitHeight(stackP.getHeight());
            mainImageView.setFitWidth(stackP.getWidth());
            mainImageView.fitWidthProperty().bind(stackP.widthProperty());
            mainImageView.fitHeightProperty().bind(stackP.heightProperty());            
            mainImageView.setPreserveRatio(true);
            stackP.getChildren().clear();
            stackP.getChildren().add(mainImageView);                
        }          
    }   
    
    private void showFullPhoto() {
        Photo aPhoto = tablePhotos.getSelectionModel().getSelectedItem();
        if (aPhoto != null) {                                   
            File fileImg = new File(aPhoto.getFilePath());                 
            Image img = new Image(fileImg.toURI().toString()); 
            winPhoto fullPhoto = new winPhoto();    
            fullPhoto.displayPhoto(img);
        } else {
            alertbox aInfo = new alertbox(myConfig.getLocale());
            aInfo.alertError(i18n.tr("Current photo is null"));
        }    
    }
    
    private void showOnMap() {
        Photo selectedPhoto = tablePhotos.getSelectionModel().getSelectedItem();
        
        if (selectedPhoto.getGpsTag() != null && selectedPhoto.getGpsTag().equals("X")) {
                pointIGC pointPhoto = new pointIGC();
                pointPhoto.setLatitude(selectedPhoto.getLatitude());
                pointPhoto.setLongitude(selectedPhoto.getLongitude());       
                showWithoutTrack(pointPhoto);
        } else {          
            if (selectedPhoto.getIdx() > 0) {
                showWithoutTrack(extTrack.Tb_Good_Points.get(selectedPhoto.getIdx()));
            } else {
                alertbox aError = new alertbox(myConfig.getLocale());
                aError.alertInfo(i18n.tr("No geographical information to display the position"));      
            }   
        }      
    }
    
    private void showForAdjust() {
        Photo selectedPhoto = tablePhotos.getSelectionModel().getSelectedItem();
         if (selectedPhoto.getGpsTag() != null && selectedPhoto.getGpsTag().equals("X")) {
            alertbox aInfo = new alertbox(myConfig.getLocale());
            aInfo.alertInfo(i18n.tr("To change position, remove geotag information"));             
         } else {
            winMapPhotoPoint mapWin = new winMapPhotoPoint(myConfig, extTrack,selectedPhoto.getIdx(), selectedPhoto.getFilePath()); 
            int backPoint = mapWin.getBackPoint();
            if (backPoint > 0) offsetCompute(selectedPhoto,backPoint);         
         }
    }
    
    private void showWithoutTrack(pointIGC pPoint) {
        
        double dLatitude = pPoint.getLatitude();
        if (dLatitude > 90 || dLatitude > -90)  {                
            double dLongitude = pPoint.getLongitude();
            if (dLongitude < 180 || dLongitude > -180){
                map_markers mapPoint = new map_markers(i18n, myConfig.getIdxMap());
                mapPoint.getPointsList().add(pPoint);
                if (mapPoint.genMap() == 0) {
                    winWebFile winMap = new winWebFile(myConfig, mapPoint.getMap_HTML(),false);
                }            
            }                
        }        
    }
    
    private void showFullMap() {
        filesUtils fUtils = new filesUtils(myConfig, extTrack);
        if (fUtils.checkFolder(true)) {
            // photos and track exist
            map_photos myMap = new map_photos(extTrack, new File(fUtils.getDestinationPath()), myConfig, true);
            if (myMap.isMap_OK()) {
                winWebFile winMap = new winWebFile(myConfig, myMap.getMap_HTML(),false);
            }            
        }
    }
    
    private void removeGeoTag() {
        Photo selectedPhoto = tablePhotos.getSelectionModel().getSelectedItem();
        dialogbox dConfirm = new dialogbox(i18n);
        if (dConfirm.YesNo(i18n.tr("Photo geotagging"), i18n.tr("Remove coordinates in the photo metadata"))) {
            try {
                Path originalPath = Paths.get(selectedPhoto.getFilePath());   
                File fStart = new File(selectedPhoto.getFilePath());                    
                Path destPath = Paths.get(fStart.getParent().toString()+File.separator+"tmp.jpg");
                File fDest = destPath.toFile();
                Files.copy(originalPath,destPath,REPLACE_EXISTING);     
                if (photos.exifWriter.removeExifTag(fStart,fDest) == 0) {
                    Files.copy(destPath,originalPath,REPLACE_EXISTING);    
                    Files.delete(destPath);
                    selectedPhoto.setGpsTag(null);
                    selectedPhoto.setLatitude(0);
                    selectedPhoto.setLongitude(0);
                    tablePhotos.refresh();
                } else {
                    alertbox aError = new alertbox(myConfig.getLocale());
                    aError.alertInfo(i18n.tr("Problem during geotagging process"));                        
                }                
            } catch (Exception e) {
                sbError = new StringBuilder(this.getClass().getName()+"."+Thread.currentThread().getStackTrace()[1].getMethodName());
                sbError.append("\r\n").append("Problem during geotag removing process");
                mylogging.log(Level.SEVERE, sbError.toString());                 
            }

        }
    }
    
    private void displayExifInfos() {
        
        Photo selectedPhoto = tablePhotos.getSelectionModel().getSelectedItem();
        if (selectedPhoto != null) {                                   
            File fileImg = new File(selectedPhoto.getFilePath());                 
            exifReader metaPhoto = new exifReader(i18n);
            metaPhoto.decodeExifInfos(fileImg);
            if (metaPhoto.isInfoExif()) {
                winTrackFile exifWin = new winTrackFile(metaPhoto.getPhotoInfos());
            }    
        }        
        
    }
    
    private void manualGeoTag()  {
        winCoord myWinCoord;
        
        Photo selectedPhoto = tablePhotos.getSelectionModel().getSelectedItem();
        if (selectedPhoto.getIdx() > 0) {
            alertbox aError = new alertbox(myConfig.getLocale());
            aError.alertInfo(i18n.tr("The photo is placed on the GPS track")); 
        } else if (selectedPhoto.getGpsTag() != null && selectedPhoto.getGpsTag().equals("X")) {
            alertbox aError = new alertbox(myConfig.getLocale());
            aError.alertInfo(i18n.tr("The photo is already geotagged"));
        } else {
            String sLat = myConfig.getFinderLat();
            String sLong = myConfig.getFinderLong();
            if (trackIsPresent) {
                double dLat = extTrack.getLatMini() + ((extTrack.getLatMaxi() - extTrack.getLatMini())/2);
                double dLong = extTrack.getLongMini() + ((extTrack.getLongMaxi() - extTrack.getLongMini())/2);
                sLat = String.valueOf(dLat);
                sLong = String.valueOf(dLong);                 
                myWinCoord = new winCoord(myConfig, sLat, sLong, extTrack);
            } else {
                myWinCoord = new winCoord(myConfig, sLat, sLong, null);
            }
            try {
                double dLat = -100;
                double dLong = -200;
                sLat = myWinCoord.getMapLat();
                sLong = myWinCoord.getMapLong();
                if (sLat != null && !sLat.equals("") && checking.parseDouble(sLat)) dLat = Double.parseDouble(sLat);
                if (sLong != null && !sLong.equals("") && checking.parseDouble(sLong)) dLong = Double.parseDouble(sLong);
                if (dLat > -90 && dLat < 90 && dLong > -180 && dLong < 180)  {
                    dialogbox dConfirm = new dialogbox(i18n);
                    if (dConfirm.YesNo(i18n.tr("Photo geotagging"), i18n.tr("Write the coordinates in the photo metadata")))   {
                        position myPos = new position();   
                        myPos.setLatitudeDd(dLat);
                        myPos.setLongitudeDd(dLong);
                        // We are obliged to copy original photo
                        // Apache.imaging does not work with a same file for original and destination
                        Path originalPath = Paths.get(selectedPhoto.getFilePath());   
                        File fStart = new File(selectedPhoto.getFilePath());                    
                        Path destPath = Paths.get(fStart.getParent().toString()+File.separator+"tmp.jpg");
                        File fDest = destPath.toFile();
                        Files.copy(originalPath,destPath,REPLACE_EXISTING);                                                       
                        if (photos.exifWriter.setExifGPSTag(fStart,fDest,myPos) == 0) {
                            Files.copy(destPath,originalPath,REPLACE_EXISTING);    
                            Files.delete(destPath);                            
                            //displayPhotos(photoDirectory);
                            selectedPhoto.setGpsTag("X");
                            selectedPhoto.setLatitude(dLat);
                            selectedPhoto.setLongitude(dLong);   
                            tablePhotos.refresh();
                        } else {
                            alertbox aError = new alertbox(myConfig.getLocale());
                            aError.alertInfo(i18n.tr("Problem during geotagging process"));                        
                        }
                    }
                }                                  
            } catch (Exception e) {
                sbError = new StringBuilder(this.getClass().getName()+"."+Thread.currentThread().getStackTrace()[1].getMethodName());
                sbError.append("\r\n").append(" geotagging problem");
                mylogging.log(Level.SEVERE, sbError.toString());                   
            }                                    
        }            
    }            
    
        
    /**
     * set the bridge with RootLayoutController 
     * @param rootlayout 
     */
    public void setRootBridge(RootLayoutController rootlayout) {
        this.rootController = rootlayout; 
        
    }    
    
    /**
     * Starting method, recovered settings and GPS choicebox is filled
     * @param mainConfig 
     */
    public void setMyConfig(Main mainApp, configProg mainConfig) {
        this.myConfig = mainConfig;
        this.mainApp = mainApp; 
        this.mainApp.rootLayoutController.updateMsgBar("", false, 50); 
        i18n = I18nFactory.getI18n("","lang/Messages",PhotoTagViewController.class.getClass().getClassLoader(),myConfig.getLocale(),0);
        // clear status bar
        rootController.updateMsgBar("", false, 60);
        cmFullMap = new MenuItem(i18n.tr("Display the entire map"));
        winTraduction();
    }
    
    public void displayLogTrack(traceGPS pTrack) {            
        extTrack = pTrack;   
        updateTrackInfos();
        alertbox aError = new alertbox(myConfig.getLocale());
        aError.alertInfo(i18n.tr("Please choose the photo folder to associate with the track")); 
        choiceFolder();   
    }       
    
    
    private int trackEvaluation(LocalDateTime ldt) {       
        int idx = -1;
        for (int i = 0; i < extTrack.Tb_Good_Points.size(); i++) {            
            if (ldt.isBefore(extTrack.Tb_Good_Points.get(i).dHeure)) {
                idx = i - 1;
                return idx;
            }
        }
        
        return idx;
    }
    
    private void selectTrack(File selectedFile) {
        extTrack = new traceGPS(selectedFile,true, myConfig);
        if (extTrack.isDecodage()) {   
            updateTrackInfos();
        } else {
            alertbox aError = new alertbox(myConfig.getLocale());
            aError.alertNumError(1062);
            trackIsPresent = false;
            hbInfo.setVisible(false);
        }
    }  
    
    private void updateTrackInfos() {
        filesUtils fUtils = new filesUtils(myConfig, extTrack);
        if (fUtils.checkFolder(true)) {
            fullMapMenu.set(true);
        } else {
            fullMapMenu.set(false);
        }
        DT_Deco = extTrack.getDT_Deco();
        DT_Attero = extTrack.getDT_Attero();
        lbTakeoff.setText(lbTakeOffBegin+DT_Deco.format(dtfHour));               
        lbLanding.setText(lbLandingBegin+DT_Attero.format(dtfHour));           
        //DT_GPS = DT_Deco;
        DT_GPS = LocalDateTime.of(2000, Month.JANUARY, 01, 00, 00, 00);
        DT_Photo = DT_Deco;                   
        lbFlightDate.setText(DT_Deco.format(dtfDay));
        trackIsPresent = true;
        hbInfo.setVisible(true);
        offsetDisplay(0);        
    }
    
    private void winTraduction() {
        btnFolder.setText(i18n.tr("Folder"));
        Tooltip foldToolTip = new Tooltip();
        foldToolTip.setStyle(myConfig.getDecoToolTip());
        foldToolTip.setText(i18n.tr("Choose the folder of the original photos"));
        btnFolder.setTooltip(foldToolTip);        
        btnTrack.setText(i18n.tr("Track"));
        Tooltip trkToolTip = new Tooltip();
        trkToolTip.setStyle(myConfig.getDecoToolTip());
        trkToolTip.setText(i18n.tr("Choose an external GPS track"));
        btnTrack.setTooltip(trkToolTip);             
        btnOnTrack.setText(i18n.tr("Generate on the track"));
        btnOnTrack.setDisable(true);
        Tooltip gentrkToolTip = new Tooltip();
        gentrkToolTip.setStyle(myConfig.getDecoToolTip());
        gentrkToolTip.setText(i18n.tr("Generate a folder with the photos on the track"));
        btnOnTrack.setTooltip(gentrkToolTip);             
        btnAlbum.setText(i18n.tr("Generate an album"));
        Tooltip genalbToolTip = new Tooltip();
        genalbToolTip.setStyle(myConfig.getDecoToolTip());
        genalbToolTip.setText(i18n.tr("Generate a folder for a single album"));
        btnAlbum.setTooltip(genalbToolTip);    
        btnAlbum.setDisable(true);
        colName.setText(i18n.tr("File name"));   
        colDate.setText(i18n.tr("Date"));
        colHour.setText(i18n.tr("Hour"));    
        lbTakeOffBegin = i18n.tr("Take off")+" : ";
        lbLandingBegin = i18n.tr("Landing")+" : ";       
    }
    
}
