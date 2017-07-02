/*
 * Copyright Gil THOMAS
 * Ce fichier fait partie intégrante du projet Logfly
 * Pour tous les détails sur la licence du projet Logfly
 * Consulter le fichier LICENSE distribué avec le code source
 */
package controller;

import dialogues.ProgressForm;
import dialogues.alertbox;
import java.awt.Toolkit;
import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
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
        // Cette méthode indispensable est laissée vide
        // car toute l'initialisation des champs se fait à partir de la classe de configuration [myConfig]
        // Or celle celle ci ne peut être récupérée qu'après initialisation.
        // toute tentative dans cette méthode provoquait une exception
        // La méthode d'initialisation setMyConfig se trouve ci dessous       
    }    
    
    public void setMyConfig(configProg mainConfig) {
        this.myConfig = mainConfig;
        i18n = I18nFactory.getI18n(Logfly.Main.class.getClass(), myConfig.getLocale());  
        winTraduction();
        
        // Remplissage des champs APRES récupération de myConfig
        ObservableList <String> listDb;
        int idxDb = 0;                
        
        listDb = FXCollections.observableArrayList();
               
        
        // Recherche de tous les fichiers db existant dans le dossier db (pathDb)
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
        
        // Initialisation onglet Carnet
        lbCurrFolder.setText(myConfig.getPathW());
        lbCurrDbPath.setText(myConfig.getPathDb());
        
        // Initialisation Onglet Pilote
        txPilote.setText(myConfig.getDefaultPilote());
        txVoile.setText(myConfig.getDefaultVoile());
        iniChbGPS();
        txIntegration.setText(String.valueOf(myConfig.getIntegration()));
        txMailPilote.setText(myConfig.getPiloteMail());
        iniChbLeague();
        txIdentif.setText(myConfig.getPiloteID());
        txPass.setText(myConfig.getPilotePass());
        
        // Initialisation Onglet Cartes
        if (myConfig.isVisuGPSinNav())  {
            checkBrowser.setSelected(true);
        } else {
            checkBrowser.setSelected(false);
        }
        iniChbCarte();
        txFinderLat.setText(myConfig.getFinderLat());
        txFinderLong.setText(myConfig.getFinderLong());
        txSeuilAb.setText(String.valueOf(myConfig.getSeuilAberrants()));
        
        // Initialisation Onglet Divers
        iniChbLang();
        lbImpFolder.setText(myConfig.getPathImport());
        if (myConfig.isPhotoAuto())  {
            checkPhoto.setSelected(true);
        } else {
            checkPhoto.setSelected(false);
        }
        
        // Initialisation Onglet Internet
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
     * Valide les changements &ffectués dans la fenêtre de configuration
     */
    @FXML
    private void handleOk() {
        // Onglet Pilote
        myConfig.setDefaultPilote(txPilote.getText());
        myConfig.setDefaultVoile(txVoile.getText());
        myConfig.setIdxGPS(chbGPS.getSelectionModel().getSelectedIndex()); 
        myConfig.setIntegration(Integer.parseInt(txIntegration.getText()));
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
        myConfig.setWinLangue(chbLang.getSelectionModel().getSelectedIndex());
        myConfig.setLocale(chbLang.getSelectionModel().getSelectedIndex());
        myConfig.setPathImport(lbImpFolder.getText());
        myConfig.setPhotoAuto(checkPhoto.isSelected());
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
     * Fermeture de la fenêtre de configuration sans valider les changements
     */
    @FXML
    private void handleCancel() {
        dialogStage.close();
    }
    
    /**
     * Création d'un nouveau carnet
     * Les champs nécessaires deviennent visibles
     */
    @FXML
    private void displayTextCarnet() {
        txnewdb.setVisible(true);
        txnewdb.requestFocus();
        btnewcarnetok.setVisible(true);
        btnewcarnetcancel.setVisible(true);
    }
    
    /**
     * La création d'un nouveau carnet est annulée
     * Les champs nécessaires deviennent invisibles
     */
    @FXML
    private void annulNewCarnet() {
        txnewdb.setVisible(false);
        btnewcarnetok.setVisible(false);
        btnewcarnetcancel.setVisible(false);        
    }
    
    /**
     * Après vérification et mise aux normes du nom du nouveau carnet
     * Le fichier SQLite est créé
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
            // Pas d'accès au beep par défaut de l'OS dans JavaFX
            // on est obligé de passer par awt
            Toolkit.getDefaultToolkit().beep();
        }
    }
    
    /**
     * Changement du dossier de travail
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
     * Changement du dossier d'import
     * Le dossier d'import est le dossier contenant les traces GPS à importer dans le carnet de vol
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
     * Changement de dossier pour le(s) carnet(s)
     * Après le choix d'un nouveau répertoire
     * Tous les fichiers SQLite .db sont déplacés
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
                filesmove transDb = new filesmove(files, newPath);
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
     * Un nouveau répertoire de carnets est choisi
     * Ce choix est validé si le dossier contient des fichiers .db
     * @throws InterruptedException 
     */
    @FXML
    private void selectNewFolderDb() throws InterruptedException {
        i18n = I18nFactory.getI18n(Logfly.Main.class.getClass());
        
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
                    alertbox aError = new alertbox();
                    aError.alertError(i18n.tr("Pas de carnets dans le dossier choisi..."));  
                }
            }
        }
    }
    
    /**
     * Remplissage de la choicebox avec tous les fichiers .db trouvés dans le répertoire 
     * @param files Array des fichiers .db recensés
     */
    private void iniChbCarnet(File[] files)  { 
        ObservableList <String> listDb;
        int idxDb = -1;
        
        listDb = FXCollections.observableArrayList();
        
        // Récupération du nom de tous les fichiers .db figurant dans le dossier
        int idx = -1;
        for (File f : files) {
            listDb.add(f.getName());
            idx++;
            if (myConfig.getDbName().equals(f.getName())) 
                idxDb = idx;                    
        }
        // Initialisation de la choicebox permettant de choisir le carnet
        chbCarnet.getItems().clear();
        chbCarnet.setItems(listDb);
        if (idxDb > -1)  {
            // On se positionne sur le fichier enregistré comme db en cours
            chbCarnet.getSelectionModel().select(idxDb);   
        } else {
            // Pas de fichier correspondant à la db en cours mémorisée
            // On ouvre la choicebox pour sélection
            chbCarnet.show();
        }
        chbCarnet.setOnAction((event) -> {
            String selectDbName = (String)chbCarnet.getSelectionModel().getSelectedItem();
            if (selectDbName != null && !selectDbName.isEmpty())
                changeDb((String)chbCarnet.getSelectionModel().getSelectedItem());
        });
        
    }
    
    /**
     * Initialisation de la choicebox permettant de choisir le GPS couramment utilisé
     * parmi les GPS supportés par Logfly
     */
    private void iniChbGPS()  { 
        
        listGPS supportedGPS = new listGPS();
        ObservableList <String> allGPS = listGPS.fill();        
        
        chbGPS.getItems().clear();
        chbGPS.setItems(allGPS);
        chbGPS.getSelectionModel().select(myConfig.getIdxGPS());                       
        
    }
    
    /**
     * Remplissage de la choicebox permettant de choisir la league du contest
     * parmi les différentes leagues supportées par le module hspoints
     */
    private void iniChbLeague()  { 
        
        listLeague suppLeagues = new listLeague();
        ObservableList <String> allLeagues = suppLeagues.fill();        
        
        chbLeague.getItems().clear();
        chbLeague.setItems(allLeagues);
        chbLeague.getSelectionModel().select(myConfig.getIdxLeague());                       
        
    }
    
    /**
     * Remplissage de la choicebox permettant de choisir la langue à utiliser
     */
    private void iniChbLang()  { 
        
        listLangues suppLangues = new listLangues();
        ObservableList <String> allLangues = suppLangues.fill(i18n);        
        chbLang.getItems().clear();
        chbLang.setItems(allLangues);
        chbLang.getSelectionModel().select(myConfig.getIdxLang());                       
        
    }
    
    /**
     * Remplissage de la choicebox permettant de choisir la carte par défaut
     * parmi les différents formats de carte supportés par Logfly
     */
    private void iniChbCarte()  { 
        
        listCarte suppCartes = new listCarte();
        ObservableList <String> allCartes = suppCartes.fill();        
        
        chbCarte.getItems().clear();
        chbCarte.setItems(allCartes);
        chbCarte.getSelectionModel().select(myConfig.getIdxMap());                               
    }
        
    /**
     * Vérification et mise à la norme du nom de fichier
     * lors de création d'un nouveau carnet
     * @param checkName
     * @return 
     */
    private String checkNewDbName(String checkName) {
        String res = null;
        
        // remplacemlent des espaces par des _ et passage en minuscules
        checkName = checkName.replaceAll(" ", "_").toLowerCase(); 
        int dotIndex = checkName.lastIndexOf('.');
        if(dotIndex>=0) {   // pour prévenir l'exception s'il n'y a pas de point
            res = checkName.substring(0,dotIndex);
        } else {
            res = checkName;
        }
        
        return res;        
    }
    
    /**
     * Appellée pour obtenir un pont de communication avec RootLayoutController 
     * @param rootlayout 
     */
    public void setRootBridge(RootLayoutController rootlayout) {
        this.rootController = rootlayout;        
    }

    /**
     * Changement de carnet géré par la classe ConfigProg
     * @param selectedDb 
     */
    private void changeDb(String selectedDb) {
        if (myConfig.dbSwitch(selectedDb))
        rootController.changeCarnetView();
    }
    
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
        lbMail.setText(i18n.tr("Mail pilote"));
        lbLeague.setText(i18n.tr("League"));
        lbIdentif.setText(i18n.tr("Identifiant"));
        lbPass.setText(i18n.tr("Mot de passe"));
        btPilotAnnuler.setText(i18n.tr("Annuler"));
        btPilotOK.setText(i18n.tr("Valider"));
        lbNavigateur.setText(i18n.tr("VisuGPS dans le navigateur"));
        lbDefaultMap.setText(i18n.tr("Carte par défaut"));
        lbLongitude.setText(i18n.tr("Longitude"));
        lbAberrants.setText(i18n.tr("Seuil points aberrants"));
        btMapOK.setText(i18n.tr("Valider"));
        btMapAnnuler.setText(i18n.tr("Annuler"));
        lbLanguage.setText(i18n.tr("Langue"));
        lbImport.setText(i18n.tr("Dossier d'import"));
        lbPhotoAuto.setText(i18n.tr("Affichage automatique des photos"));
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
