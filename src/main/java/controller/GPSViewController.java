/* 
 * Copyright Gil THOMAS
 * This file forms an integral part of Logfly project
 * See the LICENSE file distributed with source code
 * for details of Logfly licence project
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
import gps.flytec20;
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
 * setMyConfig : Start method, GPS choicebox is initialized [iniChbGPS]
 * inichbGPS : Fill the choicebox with supported GPS
 * Default GPS defined in settings is selected (idxGPS = myConfig.getIdxGPS()) 
 *    First is 1. Index 0 -> Select a GPS
 * choixGPS is launched when choicebox index change
 * choixGPS : run the method of each supported GPS
 *             - Flymaster serial port choiceboix become visible [listSerialPort()]
 *             - Flytec ...
 *             - Reversale ...
 *
 * listSerialPort : if at least one port is detected, start button become visible
 *
 * btnGo run lectureGPS to download GPS flight list.
 *
 * lectureGPS : at clic on btnGo, run the reading method of each supported GPS
 *             - Flymaster -> three cases
 *             		1 : first attempt runFlyWithProgress()
 *                      2 : successfull communication, a new run  clears the table
 *		        3 : unsuccess communication, listSerialPort() restart
 *              - Flytec20 like Flymaster
 *                      1 : first attempt runFlytec20WithProgress
 *
 * runFlyWithProgress : run readFlymaster in a different thread
 *		        at the end, generic afficheFlyList() is called	
 * runFlytec20WithProgress : run readFlytec20 in a different thread
 *                          at the end, generic afficheFlyList() is called	
 * 
 * 
 * When table is filled with GPS flight list :
 *  - logbook update is started by insertWithProgress
 *          insertWithProgress starts dedicated GPS methods : insertGPSxxWithProgress
 *                              after insertion returns the focus to carnetViewController
 *  - simple display of a track is started by askOneTrack 
 *          askOneTrack starts dedicated GPS methods : oneGPSxxWithProgress
 *          oneGPSxxWithProgress download requested track and starts showOneTrack 
 * 
 * 
 *
 * 
 * 
 * 
 * a reprendre onetrack pour Flymaster et vérifier maj carnet
 *          
 * 
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
    
    // Configuration settings
    configProg myConfig;
                
    private RootLayoutController rootController;
    
    // current GPS
    String currGPS;
    String currNamePort;  
    int resCom;   // 0 initial state  1 : successfull communication   2 : unsuccess communication
    String resIGC;
    int nbToInsert = 0;
    int nbInserted = 0;
    String errInsertion;
    
    
    @FXML
    private void initialize() {               
        dataImport = FXCollections.observableArrayList();  
        // This colums are always displayed
        dateCol.setCellValueFactory(new PropertyValueFactory<Gpsmodel, String>("date"));
        heureCol.setCellValueFactory(new PropertyValueFactory<Gpsmodel, String>("heure"));
        checkCol.setCellValueFactory(new PropertyValueFactory<Gpsmodel,Boolean>("checked"));
        checkCol.setCellFactory( CheckBoxTableCell.forTableColumn( checkCol ) );
    }
    
    /**
     * Uncheck all flights
     */
    @FXML
    private void unCheckList() {
        ObservableList <Gpsmodel> checkData = tableImp.getItems();        
        // Flight counting
        for (Gpsmodel nbItem : checkData){
            if (nbItem.getChecked())  {               
                nbItem.setChecked(Boolean.FALSE);
            }
        }
        
    }
   
    /**
     * Checked flights are inserted in the logbook
     */
   public void insertionCarnet()  {
        ObservableList <Gpsmodel> checkedData = tableImp.getItems(); 
        int nbVols = 0;
        // Flight counting
        for (Gpsmodel nbItem : checkedData){
            if (nbItem.getChecked())  {               
                nbVols++;
            }
        }
        dialogbox dConfirm = new dialogbox();
        StringBuilder sbMsg = new StringBuilder();
        sbMsg.append(String.valueOf(nbVols)).append(" ").append(i18n.tr("vols à insérer")).append(" ?");
        if (dConfirm.YesNo("", sbMsg.toString()))   {     
            nbToInsert = nbVols;
            insertWithProgress();            
        }              
    }
    
    /**
     * triggered by GPS choicebox, if necesary serial choice box becomes visible
     * @param idxGPS 
     */
    private void choixGPS(int idxGPS) {
                
        lbPort.setVisible(false);
        chbSerial.setVisible(false);
                
        switch (idxGPS) {
            case 0:
                // Select a GPS
                // No GPS defined in settings                
                break;
            case 1:
                // 6020/6030
                currGPS = "Flytec20";
                listSerialPort();
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
     * Choicebox is filled with supported GPS
     */
    private void iniChbGPS()  {          
        listGPS suppGPS = new listGPS(myConfig.getLocale());
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
        choixGPS(idxGPS);
    }
    
    /**
     * Refresh flights table
     */
    private void clearAndGo() {
        // Successfull communication, a new run  clears the table
        tableImp.getItems().clear();
        buttonBar.setVisible(false);
        hbTable.setVisible(false);  
        Image imgGo = new Image(getClass().getResourceAsStream("/images/refresh.png"));
        btnGo.setGraphic(new ImageView(imgGo));
        // with value 2, we are in refresh button case
        resCom = 2;
    }
    
    /**
     * Clic on btnGo, run the right method for each supported GPS
     */
    public void lectureGPS() {
                                       
        switch (currGPS) {
            case "Flytec20" :
                imgLed.setVisible(false);
                switch (resCom) {
                    case 0 :     
                        // initial state, we try a communication
                        currNamePort = chbSerial.getSelectionModel().getSelectedItem().toString();
                        runFlytec20WithProgress();
                        break;
                    case 1 :
                        // Successfull communication, a new run  clears the table
                        clearAndGo();
                        break;
                    case 2 :
                        // unsuccess communication, listSerialPort() is called for a new attempt
                        listSerialPort();
                        break; 
                }   
                break;
            case "FlymSD" :
                imgLed.setVisible(false);
                switch (resCom) {
                    case 0 :     
                        // initial state, we try a communication
                        currNamePort = chbSerial.getSelectionModel().getSelectedItem().toString();
                        runFlyWithProgress();
                        break;
                    case 1 :
                        // Successfull communication, a new run  clears the table
                        clearAndGo();
                        break;
                    case 2 :
                        // unsuccess communication, listSerialPort() is called for a new attempt
                        listSerialPort();
                        break;                        
                }               
            break;            
        }
        
    }
    
    /**
     * choicebox is filled with available ports
     * a filter is applied based on OS
     */
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
    
    
    
    /**
     * New Flymaster series communication method
     */
    private void readFlymaster()  {
        try {
            flymaster fms = new flymaster();
            if (fms.init(currNamePort)) {     
                // Communication OK
                resCom = 1;
                // flight list of GPS is dowloaded from fms.getDeviceInfo method
                // fms fills the observable list 
                // serial port is closed
                fms.getListFlights(dataImport);
                if (dataImport.size() > 0) {
                    // Checking of already stored flights in logbook 
                    for (Gpsmodel nbItem : dataImport){
                        if (!checkInCarnet(nbItem.getDate(),nbItem.getHeure(),nbItem.getCol4())) {
                            nbItem.setChecked(Boolean.TRUE);
                            nbItem.setCol6("NON");
                        } else {
                            nbItem.setCol6("OUI");
                        }
                        
                    }                                      
                    // Flymaster communication is OK
                    // GPS model and serial port are stored in settings
                    myConfig.setIdxGPS(11);
                    myConfig.setLastSerialCom(currNamePort);
                }
            } else {
                // Errror will be displayed in AfficheFlyList 
                resCom = 2;
            }
        } catch (Exception e) {
            
        }                
    }
    
    /**
     * Flytec 6020/6030 communication method
     */
    private void readFlytec20()  {
        try {
            flytec20 fls = new flytec20();
            if (fls.init(currNamePort)) {     
                // Communication OK
                resCom = 1;
                // flight list of GPS is dowloaded from fls.getDeviceInfo method
                // fls fills the observable list 
                // serial port is closed
                fls.getListFlights(dataImport);
                if (dataImport.size() > 0) {
                     // Checking of already stored flights in logbook 
                    for (Gpsmodel nbItem : dataImport){
                        if (!checkInCarnet(nbItem.getDate(),nbItem.getHeure(),nbItem.getCol4())) {
                            nbItem.setChecked(Boolean.TRUE);
                            nbItem.setCol6("NON");
                        } else {
                            nbItem.setCol6("OUI");
                        }
                        
                    }                                      
                    // Flyctec communication is OK
                    // GPS model and serial port are stored in settings
                    myConfig.setIdxGPS(1);
                    myConfig.setLastSerialCom(currNamePort);
                }
            } else {
                // Errror will be displayed in AfficheFlyList
                resCom = 2;
            }
        } catch (Exception e) {
            
        }                
    }
    
    /**
     * Table is filled with GPS flights list
     */
    private void afficheFlyList()  {
        // LED update
        actuLed();
        if (resCom == 2)  {
            alertbox aError = new alertbox(myConfig.getLocale());
            aError.alertNumError(1052);  // No GPS response            
        } else {
            if (dataImport.size() > 0) {
                // Under the GPS, columns are customized 
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
                // Special color if a flight is already in the logbook
                // http://stackoverflow.com/questions/32119277/colouring-table-row-in-javafx
                tableImp.setRowFactory(tbrow -> new TableRow<Gpsmodel>() {
                    @Override
                    public void updateItem(Gpsmodel item, boolean empty) {
                        super.updateItem(item, empty) ;
                        if (item == null) {
                            setStyle("");
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
   
    /**
     * run readFlymaster in a different thread with a progress bar
     */
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

        // we update the UI based on result of the task
        task.setOnSucceeded(event -> {
            pForm.getDialogStage().close();
            afficheFlyList();
        });

        pForm.getDialogStage().show();

        Thread thread = new Thread(task);
        thread.start();        
    }
    
    /**
     * run readFlytec20 in a different thread with a progressbar
     */
    private void runFlytec20WithProgress() {
        ProgressForm pForm = new ProgressForm();
           
        Task<Void> task = new Task<Void>() {
            @Override
            public Void call() throws InterruptedException { 
                readFlytec20();
                return null ;                
            }
        
        };
        // binds progress of progress bars to progress of task:
        pForm.activateProgressBar(task);

        // we update the UI based on result of the task
        task.setOnSucceeded(event -> {
            pForm.getDialogStage().close();
            afficheFlyList();
        });

        pForm.getDialogStage().show();

        Thread thread = new Thread(task);
        thread.start();        
    }
    
    /**
     * LED icon update
     */
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
    
    /**
     * Checked flights are inserted in the logbook in a different thread
     */
    private void insertWithProgress() {
        ProgressForm pForm = new ProgressForm();
           
        Task<Void> task = new Task<Void>() {
            @Override
            public Void call() throws InterruptedException { 
                switch (currGPS) {
                    case "Flytec20" :
                        insertFlytec20();
                        break;
                    case "FlymSD":
                        insertFlymaster();
                        break;
                    default:
                        throw new AssertionError();
                }
                return null ;                
            }
        
        };
        // binds progress of progress bars to progress of task:
        pForm.activateProgressBar(task);

        // when task ended, return to logbook view
        task.setOnSucceeded(event -> {
            pForm.getDialogStage().close();
            // Display number of flights inserted
            alertbox aInsFlights = new alertbox(myConfig.getLocale()); 
            aInsFlights.alertInfo(nbInserted+" / "+nbToInsert+i18n.tr(" vol(s) inséré(s) dans le carnet"));
            if(errInsertion != null && !errInsertion.isEmpty())  {
                aInsFlights.alertInfo(i18n.tr(" Traces non insérées dans le carnet")+"\r\n"+errInsertion);
            }
            rootController.changeCarnetView();
        });

        pForm.getDialogStage().show();

        Thread thread = new Thread(task);
        thread.start();        
    }
    
    /**
     * Flymaster insertion method called by insertWithProgress
     */
    private void insertFlymaster() {
        int nbFlightIn = 0;
        ObservableList <Gpsmodel> checkedData = tableImp.getItems(); 
        try {
            flymaster fms = new flymaster();
            if (fms.iniForFlights(currNamePort)) {      
                for (Gpsmodel item : checkedData){
                    if (item.getChecked())  {     
                        try {
                            // Download instruction of the flight is stored in column 5
                            // IGC date is compsed with column 1
                            // Col 1 [26.04.17] -> [260417]
                            String sDate = item.getDate().replaceAll("\\.", "");
                            if (fms.getIGC(item.getCol5(), sDate, myConfig.getDefaultPilote(), myConfig.getDefaultVoile())) {
                                traceGPS currTrace = new traceGPS(fms.getFinalIGC(), "IGC", "", true, myConfig);
                                if (currTrace.isDecodage()) { 
                                    dbAdd myDbAdd = new dbAdd(myConfig);
                                    myDbAdd.addVolCarnet(currTrace);
                                    nbFlightIn++;
                                }                            
                            }
                        } catch (Exception e) {

                        }                    
                    }
                }
                fms.closePort();
                nbInserted = nbFlightIn;
            }
        } catch (Exception e) {
            
        } 
    }
    
    /**
     * Flytec 6020/6030 insertion method called by insertWithProgress
     */
    private void insertFlytec20() {
        int nbFlightIn = 0;
        StringBuilder errMsg = new StringBuilder();
        ObservableList <Gpsmodel> checkedData = tableImp.getItems(); 
        try {
            flytec20 fls = new flytec20();
            if (fls.iniForFlights(currNamePort)){      
                for (Gpsmodel item : checkedData){
                    if (item.getChecked())  {     
                        try {
                            // Download instruction of the flight is stored in column 5
                            String strIGC = fls.getIGC(item.getCol5());
                            System.out.println("retour : "+strIGC.substring(0, 23));    
                            if (strIGC != null ) {                                
                                traceGPS flytecIGC = new traceGPS(strIGC,"IGC", "", true, myConfig);
                                if (flytecIGC.isDecodage()) { 
                                    System.out.println("trace décodée... if passé");
                                    dbAdd myDbAdd = new dbAdd(myConfig);
                                    int resAdd = myDbAdd.addVolCarnet(flytecIGC);
                                    if (resAdd == 0) nbFlightIn++;
                                    System.out.println("nbFlightIn : "+nbFlightIn);
                                } else {
                                    errMsg.append(item.getDate()+" "+item.getHeure()+"\r\n");
                                }                            
                            } else {
                                errMsg.append(item.getDate()+" "+item.getHeure()+"\r\n");
                            }
                        } catch (Exception e) {
                            errMsg.append(item.getDate()+" "+item.getHeure()+"\r\n");
                        }                    
                    }
                }
                fls.closePort();
                nbInserted = nbFlightIn;
                errInsertion = errMsg.toString();
            }
        } catch (Exception e) {
            
        } 
    }
    
    /**
     * Check if a flight already exists in the db file - called by readxxGPS method
     * @param strHeure
     * @param strDuree
     * @return 
     */
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
                    // qd ce sera résolu on gèrera l'affichage
                    dbSearch rech = new dbSearch(myConfig);
                    res = rech.Rech_Vol_by_Duree(sbDate.toString(),tbHeure[1],totSec);
                }
            }
        }    
        
        return res;
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
            Scene secondScene = new Scene(subRoot, 500, 400);
            Stage subStage = new Stage();
            // On veut que cette fenêtre soit modale
            subStage.initModality(Modality.APPLICATION_MODAL);
            subStage.setScene(secondScene); 
            subStage.setMaximized(true);
            subStage.show();
        }  else {
            alertbox aErrMap = new alertbox(myConfig.getLocale()); 
            aErrMap.alertError(i18n.tr("Une erreur est survenue pendant la génération de la carte"));     // Error during map generation
        }        
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
            errMsg = i18n.tr("Trace invalide - Points bruts : "+badTrack.Tb_Tot_Points.size()+" points valides : "+badTrack.Tb_Good_Points.size()); 
        } else {                            
            errMsg = i18n.tr("Aucun points valide dans ce fichier trace");
        }
        aError.alertError(errMsg);
    }
    
    /**
     * read a single track in a different thread - called by showOneTrack
     */
    private void oneFlytec20WithProgress(String reqFlytec) {        
        resIGC = null;
        ProgressForm pForm = new ProgressForm();
           
        Task<Void> task = new Task<Void>() {
            @Override
            public Void call() throws InterruptedException { 
                try {
                    flytec20 fls = new flytec20();
                    if (fls.iniForFlights(currNamePort)) { 
                        // Download instruction of the flight is stored in column 5
                        resIGC = fls.getIGC(reqFlytec);
                        fls.closePort(); 
                        resCom = 0;
                    } else {
                        resCom = 2;   // No GPS answer
                    }
                } catch (Exception e) {
                    resCom = 2;   // No GPS answer
                }
                finally  {
                    return null ;                
                }
            }
        
        };
        // binds progress of progress bars to progress of task:
        pForm.activateProgressBar(task);

        // we update the UI based on result of the task
        task.setOnSucceeded(event -> {
            pForm.getDialogStage().close();
            if (resCom == 0 && resIGC != null ) {                       
                traceGPS flytecIGC = new traceGPS(resIGC,"IGC", "", true, myConfig);
                if (flytecIGC.isDecodage()) { 
                    System.out.println("Decodage OK");
                    showOneTrack(flytecIGC);
                } else {
                    displayErrDwnl(flytecIGC);
                }
            } else {
                if (resCom == 2)  {
                    alertbox aError = new alertbox(myConfig.getLocale());
                    aError.alertNumError(1052);  // No GPS answer                       
                } else if (resIGC.equals(null)) {
                    alertbox aError = new alertbox(myConfig.getLocale());
                    aError.alertNumError(1054);  // IGC file is empty     
                } else {
                    alertbox aError = new alertbox(myConfig.getLocale());
                    aError.alertNumError(-1);  // Undefined error
                }
            }
        });

        pForm.getDialogStage().show();

        Thread thread = new Thread(task);
        thread.start();        
    }
    
    /**
     * read a single track in a different thread - called by showOneTrack
     */
    private void oneFlyWithProgress(Gpsmodel selLineTable) {   
        resIGC = null;
        ProgressForm pForm = new ProgressForm();
           
        Task<Void> task = new Task<Void>() {
            @Override
            public Void call() throws InterruptedException { 
                try {
                    flymaster fms = new flymaster();
                    if (fms.iniForFlights(currNamePort)) {
                        // Download instruction of the flight is stored in column 5
                        // IGC date is composed with column 1
                        // Col 1 [26.04.17] -> [260417]
                        String sDate = selLineTable.getDate().replaceAll("\\.", "");
                        if (fms.getIGC(selLineTable.getCol5(), sDate, myConfig.getDefaultPilote(), myConfig.getDefaultVoile())) {
                            resIGC = fms.getFinalIGC();
                            fms.closePort(); 
                            resCom = 0;
                        } else {
                            resCom = 2;   // No GPS answer
                        }
                    }
                } catch (Exception e) {
                    resCom = 2;   // No GPS answer
                }
                finally  {
                    return null ;                
                }
            }
        
        };
        // binds progress of progress bars to progress of task:
        pForm.activateProgressBar(task);

        // we update the UI based on result of the task
        task.setOnSucceeded(event -> {
            pForm.getDialogStage().close();
            if (resCom == 0) {                   
                traceGPS flymIGC = new traceGPS(resIGC,"IGC", "", true, myConfig);
                if (flymIGC.isDecodage()) { 
                    System.out.println("Decodage OK");
                    showOneTrack(flymIGC);
                } else {
                    displayErrDwnl(flymIGC);
                }
            } else {
                if (resCom == 2)  {
                    alertbox aError = new alertbox(myConfig.getLocale());
                    aError.alertNumError(1052);  // No GPS answer                       
                } else if (resIGC.equals(null)) {
                    alertbox aError = new alertbox(myConfig.getLocale());
                    aError.alertNumError(1054);  // IGC file is empty     
                } else {
                    alertbox aError = new alertbox(myConfig.getLocale());
                    aError.alertNumError(-1);  // Undefined error
                }
            }
        });

        pForm.getDialogStage().show();

        Thread thread = new Thread(task);
        thread.start();        
    }
    
    /**
     * Clic on button "Track display"
     */
    public void askOneTrack() {
        Gpsmodel currLineSelection = tableImp.getSelectionModel().getSelectedItem();  
        switch (currGPS) {
            case "Flytec20" :                
                oneFlytec20WithProgress(currLineSelection.getCol5());
                break;
            case "FlymSD" :
                oneFlyWithProgress(currLineSelection);
                break;
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
    public void setMyConfig(configProg mainConfig) {
        this.myConfig = mainConfig;
        i18n = I18nFactory.getI18n("","lang/Messages",GPSViewController.class.getClass().getClassLoader(),myConfig.getLocale(),0);
        winTraduction();
        iniChbGPS();
    }
    
    /**
    * Translate labels of the window
    */
    private void winTraduction() {
        lbPort.setText(i18n.tr("Port"));
        btnDecocher.setText(i18n.tr("Décocher"));
        btnMaj.setText(i18n.tr("Mise à jour Carnet"));
        Tooltip majToolTip = new Tooltip();
        majToolTip.setStyle("-fx-background-color: linear-gradient(#e2ecfe, #99bcfd);");
        majToolTip.setText(i18n.tr("Tous les vols cochés sont incorporés dans le carnet"));
        btnVisu.setTooltip(majToolTip);
        btnVisu.setText(i18n.tr("Visualisation trace")); 
        Tooltip viToolTip = new Tooltip();
        viToolTip.setStyle("-fx-background-color: linear-gradient(#e2ecfe, #99bcfd);");
        viToolTip.setText(i18n.tr("Le vol sélectionné (clic gauche) est visualisé sans incorporation dans le carnet"));
        btnVisu.setTooltip(viToolTip);        
        dateCol.setText(i18n.tr("Date"));
        heureCol.setText(i18n.tr("Heure"));
    }
    
}
