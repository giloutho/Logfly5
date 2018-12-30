/* 
 * Copyright Gil THOMAS
 * This file forms an integral part of Logfly project
 * See the LICENSE file distributed with source code
 * for details of Logfly licence project
 */
package controller;

import database.dbAdd;
import database.dbSearch;
import dialogues.alertbox;
import dialogues.dialogbox;
import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.logging.Level;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.web.WebView;
import javafx.stage.DirectoryChooser;
import javafx.stage.Modality;
import javafx.stage.Screen;
import javafx.stage.Stage;
import leaflet.map_visu;
import model.Import;
import org.xnap.commons.i18n.I18n;
import org.xnap.commons.i18n.I18nFactory;
import settings.configProg;
import settings.osType;
import systemio.mylogging;
import systemio.textio;
import trackgps.traceGPS;

/**
 *
 * @author gil
 */
public class ImportViewController {
    
    @FXML
    private TableView<Import> tableImp;
    @FXML
    private TableColumn<Import, Boolean> checkCol;
    @FXML
    private TableColumn<Import, String> dateCol;
    @FXML
    private TableColumn<Import, String> heureCol;
    @FXML
    private TableColumn<Import, String> nomFichierCol;
    @FXML
    private TableColumn<Import, String> nomPiloteCol; 
    @FXML
    private Button btnSelect;
    @FXML
    private Button btnDecocher;
    @FXML
    private Button btnMaj;
    @FXML
    private Button btnVisu;    
    @FXML
    private Button btnNettoyage;
    // Localization
    private I18n i18n; 
    
    // Settings
    configProg myConfig;
    StringBuilder sbError;
     
    // Track list
    ArrayList<String> trackPathList = new ArrayList<>(); 
    
    private ObservableList <Import> dataImport; 
     
    private Stage dialogStage;
    
    @FXML
    private HBox buttonBar;
    
    @FXML
    private HBox hbTable;
    
    private RootLayoutController rootController;
    private File importDirectory;

    /**
     * recovered settings
     * @param mainConfig 
     */
    public void setMyConfig(configProg mainConfig) {
        this.myConfig = mainConfig;
        i18n = I18nFactory.getI18n("","lang/Messages",ImportViewController.class.getClass().getClassLoader(),myConfig.getLocale(),0);
        winTraduction();
        rootController.updateMsgBar("", false,50);                
    }
        
    
    @FXML
    private void initialize() {                
        dataImport = FXCollections.observableArrayList();       
               
        dateCol.setCellValueFactory(new PropertyValueFactory<Import, String>("date"));
        heureCol.setCellValueFactory(new PropertyValueFactory<Import, String>("heure"));
        nomFichierCol.setCellValueFactory(new PropertyValueFactory<Import, String>("fileName"));
        nomPiloteCol.setCellValueFactory(new PropertyValueFactory<Import, String>("pilotName"));   
        checkCol.setCellValueFactory(new PropertyValueFactory<Import,Boolean>("checked"));
        checkCol.setCellFactory( CheckBoxTableCell.forTableColumn( checkCol ) );       
    }
       
    /**
     * Select import folder
     * @throws Exception 
     */
    @FXML
    private void selectImpFolder() throws Exception {
        //if (myConfig.getPathImport() != null)
        DirectoryChooser directoryChooser = new DirectoryChooser();     
        String iniPath = myConfig.getPathImport();
        if (iniPath != null && !iniPath.equals("")) {
            File iniImport = new File(iniPath);
            if (iniImport.exists()) {
                directoryChooser.setInitialDirectory(new File(iniPath));
            }
        }
        File selectedDirectory = directoryChooser.showDialog(dialogStage);
        if(selectedDirectory != null){
            displayFlights(selectedDirectory);
        }        
    }
    
    private void displayFlights(File fImport)  {
        try {
            // This clear section was in listtracksFiles
            // this was a big bug cause by recursive call of listTracksFiles
            dataImport.clear();
            tableImp.getItems().clear();
            trackPathList.clear();
            long tempsDebut = System.currentTimeMillis();
            listTracksFiles(fImport);
            System.out.println("au retour size liste : "+trackPathList.size());
            long tempsFin = System.currentTimeMillis();
            float seconds = (tempsFin - tempsDebut) / 1000F;
            System.out.println("Nombre de traces : "+trackPathList.size());
            System.out.println("Opération effectuée en: "+ Float.toString(seconds) + " secondes.");
            if (trackPathList.size() > 0) {
                importDirectory = fImport;
                InitialiseTableData();
            } else {
                clearData();
                StringBuilder sbMsg = new StringBuilder();
                sbMsg.append(i18n.tr("Tracks in folder")).append(" ");
                sbMsg.append(fImport.getAbsolutePath()).append(" : ") .append("0");
                rootController.updateMsgBar(sbMsg.toString(), true, 60);
            }               
        } catch (Exception e) {
            sbError = new StringBuilder(this.getClass().getName()+"."+Thread.currentThread().getStackTrace()[1].getMethodName());
            sbError.append("\r\n").append(e.toString());
            mylogging.log(Level.SEVERE, sbError.toString());             
        }
    }
    
    public void defaultFolder() {
        if (myConfig.getPathImport() != null && !myConfig.getPathImport().equals(""))
        {
            try {
                File fImport = new File(myConfig.getPathImport());
                if (fImport.exists() && fImport.isDirectory()) 
                    displayFlights(fImport);                
            } catch (Exception e) {
                sbError = new StringBuilder(this.getClass().getName()+"."+Thread.currentThread().getStackTrace()[1].getMethodName());
                sbError.append("\r\n").append(e.toString());
                mylogging.log(Level.SEVERE, sbError.toString());                 
            }
        }
    }
    
    /**
     * Recursive track search in selected folder and sub folders
     * @param dir
     * @throws Exception 
     */
    private void listTracksFiles(File dir) throws Exception {              
        File[] files = dir.listFiles();
        for (int i = 0; i < files.length; i++) {
            String fileName = files[i].getName();
            // put in your filter here
            if (fileName.endsWith(".igc") || fileName.endsWith(".IGC") || fileName.endsWith(".gpx") || fileName.endsWith(".GPX")) {                                   
                if (files[i].isFile()) {
                    trackPathList.add(files[i].getPath());       
                }
            }
            if (files[i].isDirectory()) {
                listTracksFiles(files[i]);
            }
        }
    }
    
    /**
     * each IGC or GPX file is decoded
     * Flight parameters pushed in the table
     * if a flight not exists in  the logbook, the line is checked
     */
    private void InitialiseTableData() {
        
        traceGPS myTrace;
        int nbTracks = 0;
        int nbNewTracks = 0;
        
        // Loop on arraylist of track files with path 
        for (String sTracePath : trackPathList) {
            nbTracks++;
            File fMyTrace = new File(sTracePath);
            if(fMyTrace.exists() && fMyTrace.isFile()) {           
                myTrace = new traceGPS(fMyTrace,false, myConfig);
                if (myTrace.isDecodage()) { 
                    Import imp = new Import();
                    dbSearch rechDeco = new dbSearch(myConfig); 
                    boolean resDeco = rechDeco.searchVolByDeco(myTrace.getDT_Deco(),myTrace.getLatDeco(),myTrace.getLongDeco());
                    if (!resDeco) nbNewTracks++;
                    imp.setChecked(!resDeco);
                    imp.setDate(myTrace.getDate_Vol_SQL());
                    imp.setHeure(myTrace.getDate_Vol_SQL());
                    imp.setFileName(fMyTrace.getName());
                    imp.setPilotName(myTrace.getsPilote());
                    imp.setFilePath(sTracePath); 
                    // for sorting the list we keep SQL date
                    imp.setColSort(myTrace.getDate_Vol_SQL());                    
                    dataImport.add(imp);                   
                }            
            }
        }   
        Comparator<? super Import> comparatorDate = new Comparator<Import>() {
            @Override
            public int compare(Import o1, Import o2) {
                // order asc -> o1.getCol7().compareTo(o2.getCol7());
                // order desc
                return o2.getColSort().compareTo(o1.getColSort());
            }
        };          
        tableImp.setItems(dataImport); 
        if (tableImp.getItems().size() > 0) {                        
            buttonBar.setVisible(true);
            hbTable.setVisible(true);  
            
            FXCollections.sort(dataImport, comparatorDate);
            
            tableImp.setRowFactory(tbrow -> new TableRow<Import>() {
                @Override
                public void updateItem(Import item, boolean empty) {
                    super.updateItem(item, empty) ;
                    if (item == null) {
                        setStyle("");
                    } else if (item.getChecked()) {
                        setStyle("-fx-background-color: lightsalmon;");
                    } else {
                        setStyle("-fx-background-color: cadetblue;");
                    }
                }
            });
            // Update status message
            StringBuilder sbMsg = new StringBuilder();
            sbMsg.append(i18n.tr("Tracks in folder")).append(" ").append(importDirectory.getAbsolutePath());
            sbMsg.append(" : ").append(String.valueOf(nbTracks));
            sbMsg.append("   ").append(i18n.tr("Tracks to be added")).append(" : ").append(String.valueOf(nbNewTracks));
            rootController.updateMsgBar(sbMsg.toString(), true, 60);
        }
        
        
        // Just in case...
        // https://examples.javacodegeeks.com/desktop-java/javafx/tableview/javafx-tableview-example/
        
        //List list = new ArrayList();
        //    list.add(new Book("The Thief", "Fuminori Nakamura"));
        //ObservableList data = FXCollections.observableList(list);

    }
    
    /**
     * Display messages about error decoding track file
     * @param badTrack 
     */
    private void displayErrDwnl(traceGPS badTrack) {
        alertbox aError = new alertbox(myConfig.getLocale());
        String errMsg;
        if (badTrack.Tb_Tot_Points.size() > 0)  { 
            // "Unvalid track - Gross points : "+instance.Tb_Tot_Points.size()+" valid points : "+instance.Tb_Good_Points.size());
            StringBuilder sbMsg = new StringBuilder();
            sbMsg.append(i18n.tr("Invalid Track")).append(" - ").append(i18n.tr("Rough Points")).append(" : ");
            sbMsg.append(badTrack.Tb_Tot_Points.size()).append(" ").append(i18n.tr("valid points")).append(" : ").append(badTrack.Tb_Good_Points.size());
            errMsg = sbMsg.toString(); 
        } else {                            
            errMsg = i18n.tr("No valid points in this track file");
        }
        aError.alertError(errMsg);
    }

    /**
     * Display a full screen map of tje selected map after downloading from GPS
     * @param igcToShow 
     */
    public void showOneTrack(traceGPS igcToShow)  {
        // copied/pasted of showFullMap;
        map_visu visuFullMap = new map_visu(igcToShow, myConfig);
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
            // On veut que cette fenêtre soit modale
            subStage.initModality(Modality.APPLICATION_MODAL);
            subStage.setScene(secondScene); 
            subStage.setMaximized(true);
            subStage.show();
        }  else {
            alertbox aErrMap = new alertbox(myConfig.getLocale()); 
            aErrMap.alertError(i18n.tr("An error occurred during the map generation"));     // Error during map generation
        }        
    }    
    
    @FXML
    private void handleVisu() {
        if(tableImp.getSelectionModel().getSelectedItem() != null)  {
            int idx;
            Import currLineSelection = tableImp.getSelectionModel().getSelectedItem();
            String trackPath = currLineSelection.getFilePath();
            if (trackPath != null && !trackPath.equals("")) {
                try {
                    // track file is read
                    File fTrack = new File(trackPath);
                    textio fread = new textio();   
                    String pFichier = fread.readTxt(fTrack);
                    if (pFichier != null && !pFichier.isEmpty())  {
                        traceGPS reqIGC = new traceGPS(pFichier, "", true, myConfig);
                        if (reqIGC.isDecodage()) { 
                            showOneTrack(reqIGC);
                        } else {
                            displayErrDwnl(reqIGC);
                        }
                    }                    
                } catch (Exception e) {
                    
                }                    
            }
        } else {
            alertbox aInfo = new alertbox(myConfig.getLocale());
            aInfo.alertInfo(i18n.tr("Left-click to select a flight"));
        }        
    }
    
    @FXML
    private void handleClean() {
        ObservableList<Import> data = tableImp.getItems();
        int nbVols = 0;
        
        // Comptage du nombre de vols
        for (Import nbItem : data){
            if (!nbItem.getChecked())  {               
                nbVols++;
            }
        }
        if (nbVols > 0) {
            dialogbox dConfirm = new dialogbox(i18n);
            StringBuilder sbMsg = new StringBuilder();
            sbMsg.append(String.valueOf(nbVols)).append(" ").append(i18n.tr("tracks to delete in the folder")).append(" ?");
            if (dConfirm.YesNo("", sbMsg.toString()))   {       
                for (Import item : data){
                    if (!item.getChecked())  {     
                        File fMyTrace = new File(item.getFilePath());
                        if(fMyTrace.exists() && fMyTrace.isFile()) { 
                            fMyTrace.delete();
                        }
                    }
                }
                displayFlights(importDirectory);
            }
        }        
    }
    
    /**
     * Uncheck all flights
     */
    @FXML
    private void unCheckList() {
        ObservableList<Import> data = tableImp.getItems();        
        // Comptage du nombre de vols
        for (Import nbItem : data){
            if (nbItem.getChecked())  {               
                nbItem.setChecked(Boolean.FALSE);
            }
        }
    }
    
    /**
     * Checked flights are inserted in the logbook
     */
    public void insertionCarnet()  {
        ObservableList<Import> data = tableImp.getItems();
        int nbVols = 0;
        
        // Comptage du nombre de vols
        for (Import nbItem : data){
            if (nbItem.getChecked())  {               
                nbVols++;
            }
        }
        if (nbVols > 0) {
            dialogbox dConfirm = new dialogbox(i18n);
            StringBuilder sbMsg = new StringBuilder();
            sbMsg.append(String.valueOf(nbVols)).append(" ").append(i18n.tr("flights to insert")).append(" ?");
            if (dConfirm.YesNo("", sbMsg.toString()))   {       
                for (Import item : data){
                    if (item.getChecked())  {     
                        File fMyTrace = new File(item.getFilePath());
                        if(fMyTrace.exists() && fMyTrace.isFile()) {           
                            traceGPS myTrace = new traceGPS(fMyTrace,true, myConfig);
                            if (myTrace.isDecodage()) { 
                                dbAdd myDbAdd = new dbAdd(myConfig, i18n);
                                myDbAdd.addVolCarnet(myTrace);
                            }
                        }
                    }
                }
                myConfig.setPathImport(importDirectory.getAbsolutePath());
                rootController.changeCarnetView();
            }
        }
    }               
    
    /** 
     * check the boolean value of each item to determine checkbox state
     */
    public void getValues(){        
        ObservableList<Import> data = tableImp.getItems();

        for (Import item : data){            
            System.out.println("Valeur table : "+item.getChecked());
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
     * Clear table view before folder exploration
     */
    private void clearData() {
        
        dataImport.clear();
        trackPathList.clear();
        tableImp.getItems().clear();
        buttonBar.setVisible(false);
        hbTable.setVisible(false);  
    }
    
    /**
    * Translate labels of the window
    */
    private void winTraduction() {
        checkCol.setText(i18n.tr("Logbook"));
        dateCol.setText(i18n.tr("Date"));
        heureCol.setText(i18n.tr("Time"));
        nomFichierCol.setText(i18n.tr("File name"));
        nomPiloteCol.setText(i18n.tr("Pilot name"));
        btnSelect.setText(i18n.tr("Select a folder"));
        btnDecocher.setText(i18n.tr("Unselect"));
        btnMaj.setText(i18n.tr("Logbook update"));
        btnNettoyage.setText(i18n.tr("Cleaning folder"));        
        btnVisu.setText(i18n.tr("Track visualization"));
    }
    
}
