/* 
 * Copyright Gil THOMAS
 * This file forms an integral part of Logfly project
 * See the LICENSE file distributed with source code
 * for details of Logfly licence project
 */
package controller;

import dialogues.alertbox;
import java.awt.Toolkit;
import java.io.File;
import java.io.FilenameFilter;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TextField;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import org.xnap.commons.i18n.I18n;
import org.xnap.commons.i18n.I18nFactory;
import settings.configProg;
import settings.listCarte;
import settings.listGPS;
import settings.listLangues;
import settings.listLeague;
import systemio.filesmove;

/**
 * FXML Controller class
 *
 * @author Gil Thomas logfly.org
 */
public class ConfigViewController implements Initializable {

    @FXML
    private Tab tabCarnet;    
    @FXML
    private Label lbWorkingPath;
    @FXML
    private Label lbWorkingLog;    
    @FXML
    private Button btNewLog;    
    @FXML
    private Button btMoveLog;    
    @FXML
    private Button btSelectLog;    
    @FXML
    private Button btCarnetClose;    
    @FXML
    private Button btCarnetEdit;    
    @FXML
    private Tab tabPilote;   
    @FXML
    private Label lbPilote;
    @FXML
    private Label lbVoile;
    @FXML
    private Label lbGPS;
    @FXML
    private Label lbIntegration;
    @FXML
    private Label lbLimite;
    @FXML
    private Label lbMail;
    @FXML
    private Label lbLeague;
    @FXML
    private Label lbIdentif;
    @FXML
    private Label lbPass;
    @FXML
    private Button btPilotAnnuler;
    @FXML
    private Button btPilotOK;
    @FXML
    private Tab tabCarte;    
    @FXML
    private Tab tabDivers;    
    @FXML
    private Tab tabInternet;    
    @FXML
    private Label lbCurrFolder;    
    @FXML
    private Label lbCurrDbPath;    
    @FXML
    private ChoiceBox chbCarnet;    
    @FXML
    private TextField txPilote;    
    @FXML
    private TextField txVoile;    
    @FXML
    private ChoiceBox chbGPS;    
    @FXML
    private TextField txIntegration;    
    @FXML
    private TextField txLimite;    
    @FXML
    private TextField txMailPilote;    
    @FXML
    private ChoiceBox chbLeague;    
    @FXML
    private TextField txIdentif;    
    @FXML
    private TextField txPass;    
    @FXML
    private CheckBox checkBrowser;    
    @FXML
    private ChoiceBox chbCarte;     
    @FXML
    private TextField txFinderLat;    
    @FXML
    private TextField txFinderLong;    
    @FXML
    private TextField txSeuilAb;    
    @FXML
    private ChoiceBox chbLang;    
    @FXML
    private Label lbImpFolder;    
    @FXML
    private TextField txUrlSite;    
    @FXML
    private TextField txUrlIcones;    
    @FXML
    private TextField txVisuUpload;      
    @FXML
    private TextField txVisuGPS;    
    @FXML
    private TextField txMailPass;    
    @FXML
    private TextField txUrlContest;    
    @FXML
    private Label lbContestPath;    
    @FXML
    private TextField txnewdb;    
    @FXML
    private Button btnewcarnetok;    
    @FXML
    private Button btnewcarnetcancel;
    @FXML
    private Label lbNavigateur;
    @FXML
    private Label lbDefaultMap;
    @FXML
    private Label lbLatitude;
    @FXML
    private Label lbLongitude;
    @FXML
    private Label lbAberrants;
    @FXML
    private Button btMapOK;
    @FXML
    private Button btMapAnnuler;
    @FXML
    private Label lbLanguage;
    @FXML
    private Label lbImport;
    @FXML
    private Label lbPhotoAuto;
    @FXML
    private CheckBox checkPhoto;
    @FXML
    private Label lbUpdateAuto;  
    @FXML
    private CheckBox checkUpdate;
    @FXML
    private Button btDiversAnnuler;
    @FXML
    private Button btDiversOk;
    @FXML
    private Label lbLogfly;
    @FXML
    private Label lbIcones;
    @FXML
    private Label lbLoadUrl;
    @FXML
    private Label lbVisuAdress;
    @FXML
    private Label lbSupport;
    @FXML
    private Label lbDeclaration;
    @FXML
    private Label lbExport;
    @FXML
    private Button btWebAnnuler;
    @FXML
    private Button btWebOk;            
    
    // Reference to the main application.
    private RootLayoutController rootController;
    
    private Stage dialogStage;
    
    configProg myConfig = new configProg();
    
    private static I18n i18n;
            
    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {   
        // Empty but necessary
        // fields initialization is in [myConfig]
        // But we can read it only after initialization
        // Any attempt in this method throw an exception
        // setMyConfig is just below
    }    
    
    public void setMyConfig(configProg mainConfig) {
        this.myConfig = mainConfig;
        i18n = I18nFactory.getI18n("","lang/Messages",ConfigViewController.class.getClass().getClassLoader(),myConfig.getLocale(),0);
        winTraduction();
        
        // Fields will be filled after myConfig reading
        ObservableList <String> listDb;
        int idxDb = 0;                
        
        listDb = FXCollections.observableArrayList();
               
        
        // Search all existing db files in the db folder (pathDb)
        File folderDb = new File(myConfig.getPathDb());
        if (folderDb.exists() && folderDb.isDirectory() ) {
            FilenameFilter filter = new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                   return name.endsWith(".db");
                }                
            };
            File[] files = new File(myConfig.getPathDb()).listFiles(filter);
            iniChbCarnet(files);            
        }    
        
        // Logbook Tab
        lbCurrFolder.setText(myConfig.getPathW());
        lbCurrDbPath.setText(myConfig.getPathDb());
        
        // Pilot Tab
        txPilote.setText(myConfig.getDefaultPilote());
        txVoile.setText(myConfig.getDefaultVoile());
        iniChbGPS();
        txIntegration.setText(String.valueOf(myConfig.getIntegration()));
        txLimite.setText(String.valueOf(myConfig.getGpsLimit()));
        txMailPilote.setText(myConfig.getPiloteMail());
        iniChbLeague();
        txIdentif.setText(myConfig.getPiloteID());
        txPass.setText(myConfig.getPilotePass());
        
        // Map Tab
        if (myConfig.isVisuGPSinNav())  {
            checkBrowser.setSelected(true);
        } else {
            checkBrowser.setSelected(false);
        }
        iniChbCarte();
        txFinderLat.setText(myConfig.getFinderLat());
        txFinderLong.setText(myConfig.getFinderLong());
        txSeuilAb.setText(String.valueOf(myConfig.getSeuilAberrants()));
        
        // Miscellaneous Tab
        iniChbLang();
        lbImpFolder.setText(myConfig.getPathImport());
        if (myConfig.isPhotoAuto())  {
            checkPhoto.setSelected(true);
        } else {
            checkPhoto.setSelected(false);
        }
        if (myConfig.isUpdateAuto())  {
            checkUpdate.setSelected(true);
        } else {
            checkUpdate.setSelected(false);
        }
        
        // Internet Tab
        txUrlSite.setText(myConfig.getUrlLogfly());
        txUrlIcones.setText(myConfig.getUrlIcones());
        txVisuUpload.setText(myConfig.getUrlLogflyIGC());
        txVisuGPS.setText(myConfig.getUrlVisu());
        txMailPass.setText(myConfig.getMailPass());
        txUrlContest.setText(myConfig.getUrlContest());
        lbContestPath.setText(myConfig.getPathContest());     
    }
    
    /**
     * Sets the stage of this dialog.
     *
     * @param dialogStage
     */
    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }
     
    /**
     * Validate changes
     */
    @FXML
    private void handleOk() {
        // Onglet Pilote
        myConfig.setDefaultPilote(txPilote.getText());
        myConfig.setDefaultVoile(txVoile.getText());
        myConfig.setIdxGPS(chbGPS.getSelectionModel().getSelectedIndex()); 
        myConfig.setIntegration(Integer.parseInt(txIntegration.getText()));
        myConfig.setGpsLimit(Integer.parseInt(txLimite.getText()));
        myConfig.setPiloteMail(txMailPilote.getText());
        myConfig.setIdxLeague(chbLeague.getSelectionModel().getSelectedIndex());
        myConfig.setPiloteID(txIdentif.getText());
        myConfig.setPilotePass(txPass.getText());
        // Onglet Cartes
        myConfig.setVisuGPSinNav(checkBrowser.isSelected());
        myConfig.setIdxMap(chbCarte.getSelectionModel().getSelectedIndex()); 
        myConfig.setFinderLat(txFinderLat.getText());
        myConfig.setFinderLong(txFinderLong.getText());
        myConfig.setSeuilAberrants(Integer.parseInt(txSeuilAb.getText()));
        // Onglet Divers
        myConfig.setIdxLang(chbLang.getSelectionModel().getSelectedIndex());
        myConfig.setLocale(chbLang.getSelectionModel().getSelectedIndex());
        myConfig.setPathImport(lbImpFolder.getText());
        myConfig.setPhotoAuto(checkPhoto.isSelected());
        myConfig.setUpdateAuto(checkUpdate.isSelected());
        // Onglet Internet
        myConfig.setUrlLogfly(txUrlSite.getText());
        myConfig.setUrlIcones(txUrlIcones.getText());
        myConfig.setUrlLogflyIGC(txVisuUpload.getText());
        myConfig.setUrlVisu(txVisuGPS.getText());
        myConfig.setMailPass(txMailPass.getText());
        myConfig.setUrlContest(txUrlContest.getText());             
        
        dialogStage.close();
    }
    
    /**
     * Discard changes
     */
    @FXML
    private void handleCancel() {
        dialogStage.close();
    }
    
    /**
     * New logbook creation
     */
    @FXML
    private void displayTextCarnet() {
        txnewdb.setVisible(true);
        txnewdb.requestFocus();
        btnewcarnetok.setVisible(true);
        btnewcarnetcancel.setVisible(true);
    }
    
    /**
     * New logbook creation is cancelled     
     */
    @FXML
    private void annulNewCarnet() {
        txnewdb.setVisible(false);
        btnewcarnetok.setVisible(false);
        btnewcarnetcancel.setVisible(false);        
    }
    
    /**
     * Name is checked, SQLIte file is created     
     */
    @FXML
    private void createNewCarnet() {
        // Création nouvelle db
        String dbNewName = checkNewDbName(txnewdb.getText());
        if (dbNewName != null && !dbNewName.isEmpty())  {
            dbNewName = dbNewName+".db";            
            if (myConfig.dbNewOne(dbNewName)) {
                rootController.changeCarnetView();
                txnewdb.setVisible(false);
                btnewcarnetok.setVisible(false);
                btnewcarnetcancel.setVisible(false);  
            }                      
        } else {
            // No beep with JavaFX, awt is necessary            
            Toolkit.getDefaultToolkit().beep();
        }
    }
    
    /**
     * Working folder is modified
     */
    @FXML
    private void changePathW() {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        File selectedDirectory = directoryChooser.showDialog(dialogStage);
        if(selectedDirectory != null){
            lbCurrFolder.setText(selectedDirectory.getAbsolutePath());   
            myConfig.setPathW(selectedDirectory.getAbsolutePath());
        }
        
    }
    
    /**
     * Import folder is modified
     * Import folder is the usaul folder to import GPS tracks
     */
    @FXML
    private void changePathImport() {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        File selectedDirectory = directoryChooser.showDialog(dialogStage);
        if(selectedDirectory != null){
            lbImpFolder.setText(selectedDirectory.getAbsolutePath());   
            myConfig.setPathImport(selectedDirectory.getAbsolutePath());
        }        
    }  
    
    /**
     * Path for online contest is modified 
     */
    @FXML
    private void changePathContest() {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        File selectedDirectory = directoryChooser.showDialog(dialogStage);
        if(selectedDirectory != null){
            lbContestPath.setText(selectedDirectory.getAbsolutePath());   
            myConfig.setPathContest(selectedDirectory.getAbsolutePath());
        }        
    }  
    
    /**
     * Db folder is modified
     * all SQLire files .db are moved in the new folder
     * @throws InterruptedException 
     */
    @FXML
    private void moveDb() throws InterruptedException {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        File selectedDirectory = directoryChooser.showDialog(dialogStage);
        if(selectedDirectory != null){
            File folderDb = new File(myConfig.getPathDb());
            if (folderDb.exists() && folderDb.isDirectory() ) {
                FilenameFilter filter = new FilenameFilter() {
                    @Override
                    public boolean accept(File dir, String name) {
                        return name.endsWith(".db");                   
                    }                
                };
                File[] files = new File(myConfig.getPathDb()).listFiles(filter);
                String newPath = selectedDirectory.getAbsolutePath()+myConfig.getOsSeparator();                                       
                filesmove transDb = new filesmove(files, newPath, myConfig.getLocale());
                if (transDb.isTaskOK())  {
                    // On change getpathdb
                    myConfig.setPathDb(newPath);
                    lbCurrDbPath.setText(myConfig.getPathDb());
                    myConfig.setFullPathDb(newPath+myConfig.getDbName());                      
                    // On relance l'affichage
                    rootController.changeCarnetView();                    
                }
            }
        }
    }
    
    /**
     * User choose a new db folder
     * this choice is validated if db files exist in this folder
     * @throws InterruptedException 
     */
    @FXML
    private void selectNewFolderDb() throws InterruptedException {
        
        DirectoryChooser directoryChooser = new DirectoryChooser();
        File selectedDirectory = directoryChooser.showDialog(dialogStage);
        if(selectedDirectory != null){
            File folderDb = new File(myConfig.getPathDb());
            if (folderDb.exists() && folderDb.isDirectory() ) {
                FilenameFilter filter = new FilenameFilter() {
                    @Override
                    public boolean accept(File dir, String name) {
                        return name.endsWith(".db");                   
                    }                
                };
                File[] files = new File(selectedDirectory.getAbsolutePath()).listFiles(filter);
                int nbDb = files.length;
                if (nbDb > 0) {
                    myConfig.setPathDb(selectedDirectory.getAbsolutePath()+myConfig.getOsSeparator());      
                    lbCurrDbPath.setText(selectedDirectory.getAbsolutePath()+myConfig.getOsSeparator());
                    myConfig.setFullPathDb(selectedDirectory.getAbsolutePath()+myConfig.getOsSeparator()+myConfig.getDbName());
                    iniChbCarnet(files);
                } else {
                    alertbox aError = new alertbox(myConfig.getLocale());
                    aError.alertError(i18n.tr("Pas de carnets dans le dossier choisi..."));  
                }
            }
        }
    }
    
    /**
     * Choicebox is filled with all db files of the db folder
     * @param files (Array of db files)
     */
    private void iniChbCarnet(File[] files)  { 
        ObservableList <String> listDb;
        int idxDb = -1;
        
        listDb = FXCollections.observableArrayList();
                
        int idx = -1;
        for (File f : files) {
            listDb.add(f.getName());
            idx++;
            if (myConfig.getDbName().equals(f.getName())) 
                idxDb = idx;                    
        }        
        chbCarnet.getItems().clear();
        chbCarnet.setItems(listDb);
        if (idxDb > -1)  {
            // Current db is selected in the choicebox
            chbCarnet.getSelectionModel().select(idxDb);   
        } else {
            // No db file that match the db stored in settings
            // Choicebox is opened
            chbCarnet.show();
        }
        chbCarnet.setOnAction((event) -> {
            String selectDbName = (String)chbCarnet.getSelectionModel().getSelectedItem();
            if (selectDbName != null && !selectDbName.isEmpty())
                changeDb((String)chbCarnet.getSelectionModel().getSelectedItem());
        });
        
    }
    
    /**
     * Choicebox is filled with supported GPS     
     */
    private void iniChbGPS()  { 
        
        listGPS supportedGPS = new listGPS(myConfig.getLocale());
        ObservableList <String> allGPS = listGPS.fill();        
        
        chbGPS.getItems().clear();
        chbGPS.setItems(allGPS);
        chbGPS.getSelectionModel().select(myConfig.getIdxGPS());                       
        
    }
    
    /**
     * Choicebox is fille with online contest supported by scoring module     
     */
    private void iniChbLeague()  { 
        
        listLeague suppLeagues = new listLeague();
        ObservableList <String> allLeagues = suppLeagues.fill();        
        
        chbLeague.getItems().clear();
        chbLeague.setItems(allLeagues);
        chbLeague.getSelectionModel().select(myConfig.getIdxLeague());                       
        
    }
    
    /**
     * Choicebox is filled with supported languages
     */
    private void iniChbLang()  { 
        
        listLangues suppLangues = new listLangues(myConfig.getLocale());
        ObservableList <String> allLangues = suppLangues.fill(i18n);        
        chbLang.getItems().clear();
        chbLang.setItems(allLangues);
        chbLang.getSelectionModel().select(myConfig.getIdxLang());                       
        
    }
    
    /**
     * Choicebox is filled by supported maps     
     */
    private void iniChbCarte()  { 
        
        listCarte suppCartes = new listCarte();
        ObservableList <String> allCartes = suppCartes.fill();        
        
        chbCarte.getItems().clear();
        chbCarte.setItems(allCartes);
        chbCarte.getSelectionModel().select(myConfig.getIdxMap());                               
    }
        
    /**
     * For a newlogbook file, name is checked
     * @param checkName
     * @return 
     */
    private String checkNewDbName(String checkName) {
        String res = null;
        
        // Spaces are replaced by underscore and letters are written in lower case 
        checkName = checkName.replaceAll(" ", "_").toLowerCase(); 
        int dotIndex = checkName.lastIndexOf('.');
        if(dotIndex>=0) {   // to prevent exception if no point
            res = checkName.substring(0,dotIndex);
        } else {
            res = checkName;
        }
        
        return res;        
    }
    
    /**
     * Initialize communication brdige with RootLayoutController 
     * @param rootlayout 
     */
    public void setRootBridge(RootLayoutController rootlayout) {
        this.rootController = rootlayout;        
    }

    /**
     * ConfigProg manage a change of logbook
     * @param selectedDb 
     */
    private void changeDb(String selectedDb) {
        if (myConfig.dbSwitch(selectedDb))
        rootController.changeCarnetView();
    }
    
    /**
     * Translate labels of the window
     */
    private void winTraduction() {
        tabCarnet.setText(i18n.tr("Carnet"));        
        tabCarte.setText(i18n.tr("Carte"));
        tabDivers.setText(i18n.tr("Divers"));
        tabInternet.setText(i18n.tr("Internet"));
        lbWorkingPath.setText(i18n.tr("Chemin du dossier de travail"));
        btCarnetEdit.setText(i18n.tr("Modifier"));
        lbWorkingLog.setText(i18n.tr("Chemin carnet(s)"));
        btNewLog.setText(i18n.tr("Créer un nouveau carnet"));
        btMoveLog.setText(i18n.tr("Déplacer le(s) carnet(s) vers un autre dossier"));
        btSelectLog.setText(i18n.tr("Choisir un nouveau dossier de carnets"));
        btCarnetClose.setText(i18n.tr("Fermer"));
        tabPilote.setText(i18n.tr("Pilote"));
        lbPilote.setText(i18n.tr("Nom pilote"));
        lbVoile.setText(i18n.tr("Voile"));
        lbGPS.setText(i18n.tr("GPS habituel"));
        lbIntegration.setText(i18n.tr("Intégration"));
        lbLimite.setText(i18n.tr("Limite USB"));
        lbMail.setText(i18n.tr("Mail pilote"));
        lbLeague.setText(i18n.tr("League"));
        lbIdentif.setText(i18n.tr("Identifiant"));
        lbPass.setText(i18n.tr("Mot de passe"));
        btPilotAnnuler.setText(i18n.tr("Annuler"));
        btPilotOK.setText(i18n.tr("Valider"));
        lbNavigateur.setText(i18n.tr("VisuGPS dans le navigateur"));
        lbDefaultMap.setText(i18n.tr("Carte par défaut"));
        lbLatitude.setText(i18n.tr("Latitude"));
        lbLongitude.setText(i18n.tr("Longitude"));
        lbAberrants.setText(i18n.tr("Seuil points aberrants"));
        btMapOK.setText(i18n.tr("Valider"));
        btMapAnnuler.setText(i18n.tr("Annuler"));
        lbLanguage.setText(i18n.tr("Langue"));
        lbImport.setText(i18n.tr("Dossier d'import"));
        lbPhotoAuto.setText(i18n.tr("Affichage automatique des photos"));
        lbUpdateAuto.setText(i18n.tr("Mise à jour automatique"));
        btDiversAnnuler.setText(i18n.tr("Annuler"));
        btDiversOk.setText(i18n.tr("Valider"));
        lbLogfly.setText(i18n.tr("Url Site Logfly"));
        lbIcones.setText(i18n.tr("Url Icônes"));
        lbLoadUrl.setText(i18n.tr("Url Téléchargement"));
        lbVisuAdress.setText(i18n.tr("Url VisuGPS"));
        lbSupport.setText(i18n.tr("Url mail support"));
        lbDeclaration.setText(i18n.tr("Url Déclaration"));
        lbExport.setText(i18n.tr("Dossier Export IGC"));
        btWebAnnuler.setText(i18n.tr("Annuler"));
        btWebOk.setText(i18n.tr("Valider"));               
    }
}
