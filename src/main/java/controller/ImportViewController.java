/*
 * Copyright Gil THOMAS
 * Ce fichier fait partie intégrante du projet Logfly
 * Pour tous les détails sur la licence du projet Logfly
 * Consulter le fichier LICENSE distribué avec le code source
 */
package controller;

import database.dbAdd;
import database.dbSearch;
import dialogues.dialogbox;
import java.io.File;
import java.util.ArrayList;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import model.Import;
import org.xnap.commons.i18n.I18n;
import org.xnap.commons.i18n.I18nFactory;
import settings.configProg;
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
    private Button btnNettoyage;
    // Localization
    private I18n i18n; 
    
    // Paramètres de configuration
    configProg myConfig;
     
    // Liste des traces à examiner
    ArrayList<String> trackPathList = new ArrayList<>(); 
    
    private ObservableList <Import> dataImport; 
     
    private Stage dialogStage;
    
    @FXML
    private HBox buttonBar;
    
   // @FXML
   // private AnchorPane anchorTable;
    
    @FXML
    private HBox hbTable;
    
    // Reference au controller de base RootLayout
    private RootLayoutController rootController;

    public void setMyConfig(configProg mainConfig) {
        this.myConfig = mainConfig;
        i18n = I18nFactory.getI18n(Logfly.Main.class.getClass(),myConfig.getLocale());
        winTraduction();
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
       
    @FXML
    private void selectImpFolder() throws Exception {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        File selectedDirectory = directoryChooser.showDialog(dialogStage);
        if(selectedDirectory != null){
            long tempsDebut = System.currentTimeMillis();
            listTracksFiles(selectedDirectory);
            long tempsFin = System.currentTimeMillis();
            float seconds = (tempsFin - tempsDebut) / 1000F;
            System.out.println("Nombre de traces : "+trackPathList.size());
            System.out.println("Opération effectuée en: "+ Float.toString(seconds) + " secondes.");
            if (trackPathList.size() > 0) {
                InitialiseTableData();
                
            }            
        }
        
    }
    
    /**
     * Recherche récursive de toutes les traces IGC ou GPX présntes dans le dosssier dir
     * et les sous dossiers de celui-ci
     * @param dir
     * @throws Exception 
     */
    private void listTracksFiles(File dir) throws Exception {        
        File[] files = dir.listFiles();
        for (int i = 0; i < files.length; i++) {
            String fileName = files[i].getName();
            // put in your filter here
            if (fileName.endsWith(".igc") || fileName.endsWith(".IGC")) {
            //if (fileName.endsWith(".igc") || fileName.endsWith(".IGC") || fileName.endsWith(".gpx") || fileName.endsWith(".GPX")) {                                    
                if (files[i].isFile()) {
                    trackPathList.add(files[i].getPath());                   
                }
            }
            if (files[i].isDirectory()) {
                listTracksFiles(files[i]);
            }
        }
    }
    
    //private ObservableList InitialiseTableData() 
    private void InitialiseTableData() {
        
        traceGPS myTrace;
        
        dataImport.clear();
        tableImp.getItems().clear();
        // Parcourt de l'arraylist des fichiers traces (contient le path)
        for (String sTracePath : trackPathList) {
            File fMyTrace = new File(sTracePath);
            if(fMyTrace.exists() && fMyTrace.isFile()) {           
                myTrace = new traceGPS(fMyTrace, "IGC",false);
                if (myTrace.isDecodage()) { 
                    Import imp = new Import();
                    dbSearch rechDeco = new dbSearch(myConfig); 
                    // Pour débugging 
                    boolean resDeco = rechDeco.searchVolByDeco(myTrace.getDT_Deco(),myTrace.getLatDeco(),myTrace.getLongDeco());
                    imp.setChecked(!resDeco);
                    imp.setDate(myTrace.getDate_Vol_SQL());
                    imp.setHeure(myTrace.getDate_Vol_SQL());
                    imp.setFileName(fMyTrace.getName());
                    imp.setPilotName(myTrace.getsPilote());
                    imp.setFilePath(sTracePath); 
                    dataImport.add(imp);
                    // Le pattern doit être appliqué au décodage...
                    System.out.println("Carnet : "+resDeco+" Decodage : "+fMyTrace.getName()+" "+myTrace.isDecodage()+"  "+myTrace.getsPilote());
                    // Arrêt boulot faire un sout des paramètres attendus pour la tableview
                    // Si c'est bon on poussera dans l'ObservableList
                
                // On vérifiait le décodage correct de la date
                //System.out.println("Decodage : "+myTrace.isDecodage()+"  "+myTrace.getDT_Deco().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                }
            
            }
        }        
        System.out.println("DataImp size : "+dataImport.size());
        tableImp.setItems(dataImport); 
        if (tableImp.getItems().size() > 0) {
            buttonBar.setVisible(true);
            hbTable.setVisible(true);            
        }
        
        
        // Gardé au cas où...
        // https://examples.javacodegeeks.com/desktop-java/javafx/tableview/javafx-tableview-example/
        
        //List list = new ArrayList();
        //    list.add(new Book("The Thief", "Fuminori Nakamura"));
        //ObservableList data = FXCollections.observableList(list);

       // return data;
    }
    
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
    
    public void insertionCarnet()  {
        ObservableList<Import> data = tableImp.getItems();
        int nbVols = 0;
        
        // Comptage du nombre de vols
        for (Import nbItem : data){
            if (nbItem.getChecked())  {               
                nbVols++;
            }
        }
        dialogbox dConfirm = new dialogbox();
        StringBuilder sbMsg = new StringBuilder();
        sbMsg.append(String.valueOf(nbVols)).append(" ").append(i18n.tr("vols à insérer")).append(" ?");
        if (dConfirm.YesNo("", sbMsg.toString()))   {       
            for (Import item : data){
                if (item.getChecked())  {     
                    File fMyTrace = new File(item.getFilePath());
                    if(fMyTrace.exists() && fMyTrace.isFile()) {           
                        traceGPS myTrace = new traceGPS(fMyTrace, "IGC",true);
                        if (myTrace.isDecodage()) { 
                            dbAdd myDbAdd = new dbAdd(myConfig);
                            myDbAdd.addVolCarnet(myTrace);
                        }
                    }
                }
            }
            rootController.changeCarnetView();
        }                                                                  
    }               
    
    /** 
     * Procédure qui balaie la table pour savoir ce qui est coché ou non... 
     */
    public void getValues(){        
        ObservableList<Import> data = tableImp.getItems();

        for (Import item : data){
            //check the boolean value of each item to determine checkbox state
            System.out.println("Valeur table : "+item.getChecked());
        }
    }

    /**
     * Appellée pour obtenir un pont de communication avec RootLayoutController 
     * @param rootlayout 
     */
    public void setRootBridge(RootLayoutController rootlayout) {
        this.rootController = rootlayout; 
        
    }

    private void winTraduction() {
        checkCol.setText(i18n.tr("Carnet"));
        dateCol.setText(i18n.tr("Date"));
        heureCol.setText(i18n.tr("Heure"));
        nomFichierCol.setText(i18n.tr("Nom Fichier"));
        nomPiloteCol.setText(i18n.tr("Nom pilote"));
        btnSelect.setText(i18n.tr("Sélectionner un dossier"));
        btnDecocher.setText(i18n.tr("Décocher"));
        btnMaj.setText(i18n.tr("Mise à jour Carnet"));
        btnNettoyage.setText(i18n.tr("Nettoyage dossier"));        
    }
    
}
