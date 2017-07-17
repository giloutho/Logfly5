/*
 * Copyright Gil THOMAS
 * Ce fichier fait partie intégrante du projet Logfly
 * Pour tous les détails sur la licence du projet Logfly
 * Consulter le fichier LICENSE distribué avec le code source
 */
package controller;

import com.serialpundit.core.SerialComPlatform;
import com.serialpundit.core.SerialComSystemProperty;
import com.serialpundit.serial.SerialComManager;
import database.dbAdd;
import database.dbSearch;
import dialogues.ProgressForm;
import dialogues.alertbox;
import dialogues.dialogbox;
import gps.flymaster;
import java.io.IOException;
import java.time.LocalTime;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.Tooltip;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.web.WebView;
import javafx.stage.Modality;
import javafx.stage.Stage;
import leaflet.map_visu;
import model.Gpsmodel;
import org.xnap.commons.i18n.I18n;
import org.xnap.commons.i18n.I18nFactory;
import settings.configProg;
import settings.listGPS;
import trackgps.traceGPS;

/**
 * FXML Controller class
 *
 * @author gil
 * 
 * setMyConfig : Procedure de démarrage, initialise la choicebox des GPS supportés avec iniChbGPS
 * inichbGPS : Remplissage de la choicebox permettant de choisir le modèle de GPS parmi les différents GPS supportés par Logfly.
 * La choicebox est positionné sur le GPS par défaut (idxGPS = myConfig.getIdxGPS()). 
 * Ils sont mémorisés à partir de 1. 0 -> Sélectionner un GPS.
 * On lance choixGPS à chaque changement de valeur dans la choicebox
 * choixGPS : lance le process spécifique au modèle de GPS
 *             - Flymaster listing des ports série dans une choicebox listSerialPort()
 *
 * listSerialPort : si au moins un port est détecté, le bouton Start (btnGo) est affiché
 *
 * btnGo déclenche lectureGPS.
 *
 * lectureGPS : lance le process spécifique au modèle de GPS
 *             - Flymaster -> trois cas
 *             		1 : Première tentative, on lance runFlyWithProgress()
 *                       2 : La com a réussi, si on relance, on vide la table
 *		        3 : La com n'a pas fonctionnée, on relance listSerialPort()
 *
 * runFlyWithProgress : lance readFlymaster dans un thread séparé
 *		       A la fin du process, lance afficheFlyList();	
*/

public class GPSViewController {
    
    @FXML
    private Button btnDecocher;
    @FXML
    private Button btnMaj;
    @FXML
    private Button btnVisu;
    @FXML
    private Button btnGo;
    @FXML
    private ChoiceBox chbGPS;   
    @FXML
    private ChoiceBox chbSerial;
    @FXML
    private Label lbPort;
    @FXML
    private ImageView imgLed;
    @FXML
    private TableView<Gpsmodel> tableImp;
    @FXML
    private TableColumn<Gpsmodel, Boolean> checkCol;
    @FXML
    private TableColumn<Gpsmodel, String> dateCol;
    @FXML
    private TableColumn<Gpsmodel, String> heureCol;
    @FXML
    private TableColumn<Gpsmodel, String> Column4;
    @FXML
    private TableColumn<Gpsmodel, String> Column5; 
    @FXML
    private TableColumn<Gpsmodel, String> Column6; 

    private ObservableList <Gpsmodel> dataImport; 
    
    @FXML
    private HBox buttonBar;
    @FXML
    private HBox hbTable;

    // Localization
    private I18n i18n; 
    
    // Paramètres de configuration
    configProg myConfig;
            
    // Reference au controller de base RootLayout
    private RootLayoutController rootController;
    
    // GPS choisi
    String currGPS;
    String currNamePort;  
    int resCom;   // 0 etat initial  1 : communication établie   2 : pas de communication
    
    @FXML
    private void initialize() {               
        dataImport = FXCollections.observableArrayList();  
        // Ces deux colonnes sont toujours présentes
        dateCol.setCellValueFactory(new PropertyValueFactory<Gpsmodel, String>("date"));
        heureCol.setCellValueFactory(new PropertyValueFactory<Gpsmodel, String>("heure"));
        checkCol.setCellValueFactory(new PropertyValueFactory<Gpsmodel,Boolean>("checked"));
        checkCol.setCellFactory( CheckBoxTableCell.forTableColumn( checkCol ) );
    }
            
    @FXML
    private void unCheckList() {
        ObservableList <Gpsmodel> checkData = tableImp.getItems();        
        // Comptage du nombre de vols
        for (Gpsmodel nbItem : checkData){
            if (nbItem.getChecked())  {               
                nbItem.setChecked(Boolean.FALSE);
            }
        }
        
    }
    
   public void insertionCarnet()  {
        ObservableList <Gpsmodel> checkedData = tableImp.getItems(); 
        int nbVols = 0;
        // Comptage du nombre de vols
        for (Gpsmodel nbItem : checkedData){
            if (nbItem.getChecked())  {               
                nbVols++;
            }
        }
        dialogbox dConfirm = new dialogbox();
        StringBuilder sbMsg = new StringBuilder();
        sbMsg.append(String.valueOf(nbVols)).append(" ").append(i18n.tr("vols à insérer")).append(" ?");
        if (dConfirm.YesNo("", sbMsg.toString()))   {       
            switch (currGPS) {
                case "FlymSD":
                    insertWithProgress();
                    break;
                default:
                    throw new AssertionError();
            }
        }              
    }
    
    private void choixGPS(int idxGPS) {
                
        lbPort.setVisible(false);
        chbSerial.setVisible(false);
                
        switch (idxGPS) {
            case 0:
                // Sélectionner le GPS
                // Pas de GPS mémorisé
                // A voir s'il faut un traitement ou non
                break;
            case 1:
                // 6020/6030
                // lb_Port.Visible = True
                // PopSerial.Visible = True
                break;
            case 2:
                // 6015
                //lb_Port.Visible = True
                //PopSerial.Visible = True
                break;
            case 3:
                // Flynet
                //AskGPS(3)
                break;
            case 4:    
                // Flymaster old
                // Voir ce source en C# https://github.com/takyonxxx/Flymaster-Variometer-Log-Pc/blob/master/FLYMASTER_GPS.cs
                //lb_Port.Visible = True
                //PopSerial.Visible = True
                //if App.SerialIdx > -1 Then 
                //    PopSerial.ListIndex = App.SerialIdx
                break;
            case 5:
                // Reversale
                // AskGPS(5)
                break;
            case 6:
                // Skytraax
                // AskGPS(6)
                break;
            case 7:
                // Oudie
                // AskGPS(7)
                break;
            case 8:
                // Element
                //AskGPS(8)
                break;
            case 9:
                // Sensbox
                //AskGPS(9)
                break;
            case 10:
                // Syride
                // AskGPS(10)
                break;
            case 11:
                // Flymaster SD 
                currGPS = "FlymSD";
                listSerialPort();
                break;
            case 12:
                // Connect
                // AskGPS(12)
                break;
            case 13:
                // Skytraax 3
                // AskGPS(13)
                break;
            case 14:
                // C Pilot Evo
                // AskGPS(14)
                break;
        }
        
    }
    
    /**
     * Remplissage de la choicebox permettant de choisir le modèle de GPS
     * parmi les différents GPS supportés par Logfly
     */
    private void iniChbGPS()  {          
        listGPS suppGPS = new listGPS();
        ObservableList <String> allGPS = suppGPS.fill();                
        chbGPS.getItems().clear();
        chbGPS.setItems(allGPS);
        int idxGPS = myConfig.getIdxGPS();
        chbGPS.getSelectionModel().select(idxGPS);    
        chbGPS.getSelectionModel().selectedIndexProperty()
        .addListener(new ChangeListener<Number>() {
          public void changed(ObservableValue ov, Number value, Number new_value) {
            choixGPS(new_value.intValue());
          }
        });
        // Pour lancer la prcédure adéquate correspondant au choix par défaut
        choixGPS(idxGPS);
    }
    
    public void LectureGPS() {
                                       
        switch (currGPS) {
            case "FlymSD" :
                imgLed.setVisible(false);
                switch (resCom) {
                    case 0 :     
                        // Etat initial, on tente une communication
                        currNamePort = chbSerial.getSelectionModel().getSelectedItem().toString();
                        runFlyWithProgress();
                        break;
                    case 1 :
                        // La com a réussi, si on relance la communication, on vide la table
                        tableImp.getItems().clear();
                        buttonBar.setVisible(false);
                        hbTable.setVisible(false);  
                        Image imgGo = new Image(getClass().getResourceAsStream("/images/refresh.png"));
                        btnGo.setGraphic(new ImageView(imgGo));
                        // en mettant rescom à 2, on se replace dans le cas du bouton refresh
                        resCom = 2;
                        break;
                    case 2 :
                        // La com n'a pas fonctionnée, on réinitialise la liste des ports 
                        listSerialPort();
                        break;                        
                }               
                break;            
        }
        
    }
    
    
    private void listSerialPort() {
        SerialComManager scm;     
        int idxSerialList = 0;
        int idxListPort = 0;
        try {
            scm = new SerialComManager();
            SerialComPlatform scp = new SerialComPlatform(new SerialComSystemProperty());
            int osType = scp.getOSType();
            String[] ports = scm.listAvailableComPorts();
            int idx = 0;
            if (ports.length > 0) {
                ObservableList <String> portList;
                portList = FXCollections.observableArrayList();
                // Dernier port série utilisé
                String lastSerialUsed = myConfig.getLastSerialCom();
                // Pour Linux, on prépare tous les ports qui ne devront pas être affichés
                Pattern p1 = Pattern.compile("^/dev/tty[0-9].*");
                Pattern p2 = Pattern.compile("^/dev/ttyS[0-9].*");
                Pattern p3 = Pattern.compile("^/dev/pts.*");
                Pattern p4 = Pattern.compile("^/dev/console.*");
                Pattern p5 = Pattern.compile("^/dev/ttyprintk.*");
                Pattern p6 = Pattern.compile("^/dev/ptmx.*");
                for(String port: ports){
                    if(osType == SerialComPlatform.OS_MAC_OS_X) {
                        // Pour éviter de lister 25000 ports inutilisables
                        if (ports[idx].substring(0,8).equals("/dev/cu."))  {                 
                            portList.add(port);
                            if (lastSerialUsed.equals(port)) idxSerialList = idxListPort; 
                            idxListPort++;
                        }
                    }else if(osType == SerialComPlatform.OS_LINUX) {
                        // Pour éviter de lister 25000 ports inutilisables
                        if (!p1.matcher(port).matches() && !p2.matcher(port).matches() && !p3.matcher(port).matches()
                             && !p4.matcher(port).matches() && !p5.matcher(port).matches() && !p6.matcher(port).matches())
                        {
                            portList.add(port);   
                            if (lastSerialUsed.equals(port)) idxSerialList = idxListPort; 
                            idxListPort++;
                        }   
                    }
                    idx ++; 
                }    
                if (portList.size() > 0) {
                    chbSerial.getItems().clear();
                    chbSerial.setItems(portList);  
                    chbSerial.setVisible(true);
                    chbSerial.getSelectionModel().select(idxSerialList); 
                    lbPort.setVisible(true);                    
                    btnGo.setVisible(true);  
                    resCom = 0;
                    actuLed();
                } else {
                    // pas sûr que ce soit pertinent...
                    // on devrait afficher un msg erreur et demander relance totale
                    resCom = 3;
                    actuLed();   
                }
            }                         
        } catch (SecurityException ex) {
            Logger.getLogger(GPSViewController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(GPSViewController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    
    
    
    private void readFlymaster()  {
        try {
            flymaster fms = new flymaster();
            if (fms.init(currNamePort)) {     
                // Communication OK
                resCom = 1;
                // liste brute des vols est chargée depuis getDeviceInfo
                // on demande à fms de remplir l'obs list à partir de sa liste
                // le port est fermé après la sortie de la liste
                fms.getListFlights(dataImport);
                if (dataImport.size() > 0) {
                    // on vérifie les vols déjà présents dans le carnet
                    // on a choisi de faire cela ici car le controller 
                    // possède la config pour travailler sur la db 
                    for (Gpsmodel nbItem : dataImport){
                        if (!checkInCarnet(nbItem.getDate(),nbItem.getHeure(),nbItem.getCol4())) {
                            nbItem.setChecked(Boolean.TRUE);
                            nbItem.setCol6("NON");
                        } else {
                            nbItem.setCol6("OUI");
                        }
                        
                    }                                      
                    // On a communiqué avec le Flymaster 
                    // donc on mémorise ce GPS et son port série
                    myConfig.setIdxGPS(11);
                    myConfig.setLastSerialCom(currNamePort);
                }
            } else {
                System.out.println("Pas de communication");   
                resCom = 2;
            }
        } catch (Exception e) {
            
        }                
    }
    
    private void afficheFlyList()  {
        // Mise à jour de la LED
        actuLed();
        if (resCom == 2)  {
            alertbox aError = new alertbox();
            aError.alertError(i18n.tr("Impossible d'ouvrir le port série sélectionné"));  
        } else {
            if (dataImport.size() > 0) {

                // On personnalise les colonnes au cas par cas
                Column4.setCellValueFactory(new PropertyValueFactory<Gpsmodel, String>("col4"));
                // on ajustera la largeur des colonnes au cas par cas
                dateCol.setMinWidth(80);
                heureCol.setMinWidth(80);
                Column4.setMinWidth(80);
                Column4.setText(i18n.tr("Durée"));
                Column5.setVisible(false);
                Column6.setVisible(false);
                tableImp.setMaxWidth(300);
                tableImp.setItems(dataImport); 
                // Coloration des lignes déjà présentes dans le carnet
                // http://stackoverflow.com/questions/32119277/colouring-table-row-in-javafx
                tableImp.setRowFactory(tbrow -> new TableRow<Gpsmodel>() {
                    @Override
                    public void updateItem(Gpsmodel item, boolean empty) {
                        super.updateItem(item, empty) ;
                        if (item == null) {
                            setStyle("");
    //                            } else if (item.getChecked()) {
                        } else if (item.getCol6().equals("NON")) {
                            setStyle("-fx-background-color: lightsalmon;");
                        } else {
                            setStyle("-fx-background-color: cadetblue;");
                        }
                    }
                });
                if (tableImp.getItems().size() > 0) {
                    buttonBar.setVisible(true);
                    hbTable.setVisible(true);            
                }
            }
        }
    }
    
    private void runFlyWithProgress() {
        ProgressForm pForm = new ProgressForm();
           
        Task<Void> task = new Task<Void>() {
            @Override
            public Void call() throws InterruptedException { 
                readFlymaster();
                return null ;                
            }
        
        };
        // binds progress of progress bars to progress of task:
        pForm.activateProgressBar(task);

        // in real life this method would get the result of the task
        // and update the UI based on its value:
        task.setOnSucceeded(event -> {
            pForm.getDialogStage().close();
            afficheFlyList();
        });

        pForm.getDialogStage().show();

        Thread thread = new Thread(task);
        thread.start();        
    }
    
    private void actuLed() {
        Image imgImgLed=null;
        Image imgGo = null;
        Tooltip goToolTip = new Tooltip();
        goToolTip.setStyle("-fx-background-color: linear-gradient(#e2ecfe, #99bcfd);");

        switch (resCom) {
            case 0 :
                imgGo = new Image(getClass().getResourceAsStream("/images/download.png"));
                btnGo.setGraphic(new ImageView(imgGo));
                btnGo.setVisible(true);
                goToolTip.setText(i18n.tr("Télécharger les traces du GPS"));
                btnGo.setTooltip(goToolTip);
                imgLed.setVisible(false);
                break;        
            case 1 :                
                imgImgLed = new Image(getClass().getResourceAsStream("/images/Led_Green.png"));
                goToolTip.setText(i18n.tr("Télécharger les traces du GPS"));
                btnGo.setTooltip(goToolTip);
                imgLed.setImage(imgImgLed);                
                imgLed.setVisible(true);
                break;
            case 2 :
                imgImgLed = new Image(getClass().getResourceAsStream("/images/Led_red.png"));
                imgLed.setImage(imgImgLed);
                imgGo = new Image(getClass().getResourceAsStream("/images/refresh.png"));
                btnGo.setGraphic(new ImageView(imgGo));                
                goToolTip.setText(i18n.tr("Actualiser la liste des ports"));
                btnGo.setTooltip(goToolTip);
                imgLed.setVisible(true);
                break;
            case 3 :
                // pas de port série détecté
                btnGo.setVisible(false);
                imgLed.setVisible(false);
                break;
        }
    }
    
    private void insertWithProgress() {
        ProgressForm pForm = new ProgressForm();
           
        Task<Void> task = new Task<Void>() {
            @Override
            public Void call() throws InterruptedException { 
                insertFlymaster();
                return null ;                
            }
        
        };
        // binds progress of progress bars to progress of task:
        pForm.activateProgressBar(task);

        // in real life this method would get the result of the task
        // and update the UI based on its value:
        task.setOnSucceeded(event -> {
            pForm.getDialogStage().close();
            rootController.changeCarnetView();
        });

        pForm.getDialogStage().show();

        Thread thread = new Thread(task);
        thread.start();        
    }
    
    private void insertFlymaster() {
        ObservableList <Gpsmodel> checkedData = tableImp.getItems(); 
        try {
            flymaster fms = new flymaster();
            if (fms.iniForFlights(currNamePort)) {      
                for (Gpsmodel item : checkedData){
                    if (item.getChecked())  {     
                        try {
                            // récupération de l'instruction de déchargement de la trace dans la colonne 5
                            // Composition de la date pour l'entête IGC, à partir de la colonne 1 
                            // Col 1 [26.04.17] -> [260417]
                            String sDate = item.getDate().replaceAll("\\.", "");
                            if (fms.getIGC(item.getCol5(), sDate, myConfig.getDefaultPilote(), myConfig.getDefaultVoile())) {
                                traceGPS currTrace = new traceGPS(fms.getFinalIGC(), "IGC", "", true);
                                if (currTrace.isDecodage()) { 
                                    dbAdd myDbAdd = new dbAdd(myConfig);
                                    myDbAdd.addVolCarnet(currTrace);
                                }                            
                            }
                        } catch (Exception e) {

                        }                    
                    }
                }
                fms.closePort();
                // mis en rem avec la jauge
              //  rootController.changeCarnetView();
            }
        } catch (Exception e) {
            
        } 
    }
    
    private boolean checkInCarnet(String strDate, String strHeure, String strDuree)  {
        boolean res = false;
        String[] tbDate = strDate.split("\\.");
        if (tbDate.length == 3) {
            String[] tbHeure = strHeure.split(":");
            if (tbHeure.length == 3) {                    
                 String[] partTime = strDuree.split(":");
                 if (partTime.length > 2) {
                   // LocalDateTime ldt = LocalDateTime.of(Integer.parseInt("20"+tbDate[2]), Integer.parseInt(tbDate[1]), 
                    //                    Integer.parseInt("20"+tbDate[0]), Integer.parseInt(tbHeure[0]), 
                    //                    Integer.parseInt(tbHeure[1]), Integer.parseInt(tbHeure[2]));
                    StringBuilder sbDate = new StringBuilder();
                    sbDate.append("20").append(tbDate[2]).append("-").append(tbDate[1]).append("-").append(tbDate[0]);             
                    LocalTime ltDuree = LocalTime.of(Integer.parseInt(partTime[0]), Integer.parseInt(partTime[1]), Integer.parseInt(partTime[2]));
                    int totSec = ltDuree.toSecondOfDay();
                    // il faut revoir dbsearch qui doit être silencieux si on est en multithread
                    // renvoyer une boolean et mettre le message d'erreur 
                    // mais on ne peut pas virer le myConfig pour l'accès à la db
                    // dans une string avec un getter pour afficher l'erreur au retour du traitement
                    // qd ce sera résolu on fera 
                    dbSearch rech = new dbSearch(myConfig);
                    res = rech.Rech_Vol_by_Duree(sbDate.toString(),tbHeure[1],totSec);
                }
            }
        }    
        
        return res;
    }
    
    public void showTrack()  {
        Gpsmodel selLine = tableImp.getSelectionModel().getSelectedItem();
        // On récupère l'instruction de déchargement
        System.out.println(selLine.getCol5());
        try {
            flymaster fms = new flymaster();
            if (fms.iniForFlights(currNamePort)) { 
                // récupération de l'instruction de déchargement de la trace dans la colonne 5
                // Composition de la date pour l'entête IGC, à partir de la colonne 1 
                // Col 1 [26.04.17] -> [260417]
                String sDate = selLine.getDate().replaceAll("\\.", "");
                if (fms.getIGC(selLine.getCol5(), sDate, myConfig.getDefaultPilote(), myConfig.getDefaultVoile())) {
                    traceGPS currTrace = new traceGPS(fms.getFinalIGC(), "IGC", "", true);
                    fms.closePort();
                    if (currTrace.isDecodage()) { 
                        // Copié collé showFullMap;
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
            }
        } catch (Exception e) {

        }               
    }
    
    /**
     * Appellée pour obtenir un pont de communication avec RootLayoutController 
     * @param rootlayout 
     */
    public void setRootBridge(RootLayoutController rootlayout) {
        this.rootController = rootlayout; 
        
    }
    
    /**
     * Procedure de démarrage, initialise la choicebox des GPS supportés
     * @param mainConfig 
     */
    public void setMyConfig(configProg mainConfig) {
        this.myConfig = mainConfig;
        i18n = I18nFactory.getI18n("","lang/Messages",GPSViewController.class.getClass().getClassLoader(),myConfig.getLocale(),0);
        winTraduction();
        iniChbGPS();
    }
    
    private void winTraduction() {
        lbPort.setText(i18n.tr("Port"));
        btnDecocher.setText(i18n.tr("Décocher"));
        btnMaj.setText(i18n.tr("Mise à jour Carnet"));
        Tooltip majToolTip = new Tooltip();
        majToolTip.setStyle("-fx-background-color: linear-gradient(#e2ecfe, #99bcfd);");
        majToolTip.setText(i18n.tr("Tous les vols cochés sont incorporés dans le carnet"));
        btnVisu.setTooltip(majToolTip);
        btnVisu.setText(i18n.tr("Visualisation simple")); 
        Tooltip viToolTip = new Tooltip();
        viToolTip.setStyle("-fx-background-color: linear-gradient(#e2ecfe, #99bcfd);");
        viToolTip.setText(i18n.tr("Le vol sélectionné (clic gauche) est visualisé sans incorporation dans le carnet"));
        btnVisu.setTooltip(viToolTip);        
        dateCol.setText(i18n.tr("Date"));
        heureCol.setText(i18n.tr("Heure"));
    }
    
}
