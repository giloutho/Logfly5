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
import gps.connect;
import gps.flymaster;
import gps.flymasterold;
import gps.flytec15;
import gps.flytec20;
import gps.oudie;
import gps.reversale;
import gps.skytraax;
import gps.skytraxx3;
import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.logging.Level;
import java.util.regex.Pattern;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
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
import javafx.scene.web.WebView;
import javafx.stage.Modality;
import javafx.stage.Screen;
import javafx.stage.Stage;
import leaflet.map_visu;
import model.Gpsmodel;
import org.xnap.commons.i18n.I18n;
import org.xnap.commons.i18n.I18nFactory;
import settings.configProg;
import settings.listGPS;
import settings.osType;
import systemio.mylogging;
import trackgps.traceGPS;

/**
 * FXML Controller class
 *
 * @author gil
 * 
 * setMyConfig : 
 *          Start method, GPS choicebox is initialized [iniChbGPS]
 * inichbGPS : 
 *          Fill the choicebox with supported GPS
 *          Default GPS defined in settings is selected (idxGPS = myConfig.getIdxGPS()) 
 *          First is 1. Index 0 is reserved for -> Select a GPS
 *          when choicebox index change, choixGPS is launched 
 * choixGPS : run the method of each supported GPS
 *             - Flymaster serial port choiceboix become visible [listSerialPort()]
 *             - Flytec 6020/6030 serial port choiceboix become visible [listSerialPort()]
 *             - Reversale USB GPS
 * listSerialPort : 
 *          if at least one port is detected, start button become visible
 * btnGo : 
 *          run readGPS to download GPS flight list (FXML command) only for serial GPS
 *          for usb GPS, readGPS is started automatically
 * readGPS : at clic on btnGo, run flightListWithProgress();
 * 
 *              this method run the reading method of each supported GPS
 *                  - Flytec 6015 -> readFlytec15
 *                  - Flytec 6020/6030 -> readFlytec20
 *                  - Flymaster new series 
 *              
 *              Tableview is filled with each flight and its own download instruction
 *              
 *              AfficheFlyList is called
 * afficheFlyList :   
 *         - Logbook update : insertLog   
 *         - selected track is displayed : AskOneTrack 
 * btnMaj :
 *          run insertLog
 * insertLog :
 *          Count flights to insert and call insertWithProgress
 * insertWithProgress :
 *          run insertFromGPS in a different thread
 *          number of inserted flights is displayed in a dialog box
 * insertFromGPS : 
 *          For each flight to insert, specific download instruction is called
 *          IGC file is inserted in database
 * askOneTrack :
 *          starts oneFlightWithProgress
 * oneFlightWithProgress :
 *          donwload selected track with special class for each GPS model
 *          if success an IGC String is returned, showOneTrack is called
 *          
 * showOneTrack :
 *          Display the IGC string parameter in a full screen map.
 * 
 * run the reading method of each supported GPS
 *             - Flymaster -> three cases
 *             		1 : first attempt runFlyWithProgress()
 *                      2 : successfull communication, a new run  clears the table
 *		        3 : unsuccess communication, listSerialPort() restart
 *              - Flytec20 like Flymaster
 *                      1 : first attempt runFlytec20WithProgress
 *
 * runGPSxxWithProgress      : run a dedicated GPSxxread in a different thread
 *		              at the end, generic afficheFlyList() is called	
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
 * For a new USB GPS, follow an existing like Oudie -> Ctrl F Oudie and Ctrl F usbOudie
 * Don't forget to add a reference in flightListWithProgress() 
 * and memorize last used GPS at the end of readUSBGps()
 * 
 * 
 * 
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
    
    private enum gpsType {Flytec20,Flytec15,Flynet,FlymOld,Rever,Sky,Oudie,Element,Sensbox,Syride,FlymSD,Connect,Sky3,CPilot}
    
    // current GPS
    private gpsType currGPS;
    private String currNamePort;  
    private int resCom;   // 0 initial state  1 : successfull communication   2 : unsuccess communication
    private int nbToInsert = 0;
    private int nbInserted = 0;
    private String errInsertion;
    private StringBuilder sbError;
    private reversale usbRever;
    private skytraax usbSky;
    private skytraxx3 usbSky3;
    private oudie usbOudie;
    private connect usbConnect;
    private String strTrack;
    private String errorComMsg;
    private int nbTracks = 0;
    private int nbNewTracks = 0;
    private String idGPS = "";
    
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
        for (Gpsmodel nbItem : checkData){
            if (nbItem.getChecked())  {               
                nbItem.setChecked(Boolean.FALSE);
            }
        }        
    }
    
    private void actuMsgBar() {
        ObservableList <Gpsmodel> tableData = tableImp.getItems();        
        // Flight counting      
        nbTracks = 0;
        nbNewTracks = 0;
        for (Gpsmodel item : tableData){
            nbTracks++;
            if (item.getChecked())  {               
                nbNewTracks++;
            }
        } 
        StringBuilder sbMsg = new StringBuilder();
        sbMsg.append(idGPS).append(i18n.tr("   Traces dans le GPS : ")).append(String.valueOf(nbTracks));
        sbMsg.append("   ").append(i18n.tr("Traces à incorporer : ")).append(String.valueOf(nbNewTracks));
        rootController.updateMsgBar(sbMsg.toString(), true, 60);
    }
   
    /**
     * Count flights to insert in the logbook, triggered by btnMaj
     */
   public void insertLog()  {
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
     * triggered by GPS choicebox, if necessary serial choice box becomes visible
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
                currGPS = gpsType.Flytec20;
                listSerialPort();
                break;
            case 2:
                // 6015
                currGPS = gpsType.Flytec15;
                listSerialPort();
                break;
            case 3:
                currGPS = gpsType.Flynet;                
                break;
            case 4:    
                // Flymaster old series
                currGPS = gpsType.FlymOld;
                listSerialPort();                
                break;
            case 5:
                // Reversale
                currGPS = gpsType.Rever;   
                usbRever = new reversale(myConfig.getOS(), myConfig.getGpsLimit());
                if (usbRever.isConnected()) {
                    goodListDrives(usbRever.getDriveList(),usbRever.getIdxDrive());
                } else {
                    badListDrives(usbRever.getDriveList(), usbRever.getIdxDrive());
                }
                break;
            case 6:
                currGPS = gpsType.Sky;
                usbSky = new skytraax(myConfig.getOS(), myConfig.getGpsLimit());
                if (usbSky.isConnected()) {
                    goodListDrives(usbSky.getDriveList(),usbSky.getIdxDrive());
                } else {
                    badListDrives(usbSky.getDriveList(), usbSky.getIdxDrive());
                }
                break;
            case 7:
                currGPS = gpsType.Oudie;
                usbOudie = new oudie(myConfig.getOS(), myConfig.getGpsLimit());
                if (usbOudie.isConnected()) {
                    goodListDrives(usbOudie.getDriveList(),usbOudie.getIdxDrive());
                } else {
                    badListDrives(usbOudie.getDriveList(), usbOudie.getIdxDrive());
                }
                break;                
            case 8:
                currGPS = gpsType.Element;
                //AskGPS(8)
                break;
            case 9:
                currGPS = gpsType.Sensbox;
                //AskGPS(9)
                break;
            case 10:
                currGPS = gpsType.Syride;
                // AskGPS(10)
                break;
            case 11:
                // Flymaster SD 
                currGPS = currGPS = gpsType.FlymSD;
                listSerialPort();
                break;
            case 12:
                currGPS = gpsType.Connect;
                usbConnect = new connect(myConfig.getOS(), myConfig.getGpsLimit());
                if (usbConnect.isConnected()) {
                    goodListDrives(usbConnect.getDriveList(),usbConnect.getIdxDrive());
                } else {
                    badListDrives(usbConnect.getDriveList(), usbConnect.getIdxDrive());
                }
                break;
            case 13:
                currGPS = gpsType.Sky3;
                usbSky3 = new skytraxx3(myConfig.getOS(), myConfig.getGpsLimit());
                if (usbSky3.isConnected()) {
                    goodListDrives(usbSky3.getDriveList(),usbSky3.getIdxDrive());
                } else {
                    badListDrives(usbSky3.getDriveList(), usbSky3.getIdxDrive());
                }
                break;
            case 14:
                currGPS = gpsType.CPilot;
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
              winReset();
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
     * Clic on btnGo, run the right method for each supported GPS (FXML triggered)
     */
    public void readGPS() {
                                       
        switch (currGPS) {
            case Flytec20 :
                imgLed.setVisible(false);
                switch (resCom) {
                    case 0 :     
                        // initial state, we try a communication
                        currNamePort = chbSerial.getSelectionModel().getSelectedItem().toString();
                        flightListWithProgress();
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
            case Flytec15 :
                imgLed.setVisible(false);
                switch (resCom) {
                    case 0 :     
                        // initial state, we try a communication
                        currNamePort = chbSerial.getSelectionModel().getSelectedItem().toString();
                        flightListWithProgress();
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
            case FlymSD :
                imgLed.setVisible(false);
                switch (resCom) {
                    case 0 :     
                        // initial state, we try a communication
                        currNamePort = chbSerial.getSelectionModel().getSelectedItem().toString();
                        flightListWithProgress();
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
            case FlymOld :
                imgLed.setVisible(false);
                switch (resCom) {
                    case 0 :     
                        // initial state, we try a communication
                        currNamePort = chbSerial.getSelectionModel().getSelectedItem().toString();
                        flightListWithProgress();
                       // runFlyOldWithProgress();
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
            case Rever :
                // Rever not connected, new attempt
                if (usbRever.testConnection(myConfig.getOS())) {
                    goodListDrives(usbRever.getDriveList(),usbRever.getIdxDrive());
                } else {
                    badListDrives(usbRever.getDriveList(), usbRever.getIdxDrive());
                }
                break;
            case Sky :
                // Skytraxx 2 not connected, new attempt
                if (usbSky.testConnection(myConfig.getOS())) {
                    goodListDrives(usbSky.getDriveList(),usbSky.getIdxDrive());
                } else {
                    badListDrives(usbSky.getDriveList(), usbSky.getIdxDrive());
                }
                break;     
            case Sky3 :
                // Skytraxx 3 not connected, new attempt
                if (usbSky3.testConnection(myConfig.getOS())) {
                    goodListDrives(usbSky3.getDriveList(),usbSky3.getIdxDrive());
                } else {
                    badListDrives(usbSky3.getDriveList(), usbSky3.getIdxDrive());
                }
                break;                    
            case Oudie :
                // Oudie not connected, new attempt
                if (usbOudie.testConnection(myConfig.getOS())) {
                    goodListDrives(usbOudie.getDriveList(),usbOudie.getIdxDrive());
                } else {
                    badListDrives(usbOudie.getDriveList(), usbOudie.getIdxDrive());
                }
                break;    
            case Connect :
                // Connect not connected, new attempt
                if (usbConnect.testConnection(myConfig.getOS())) {
                    goodListDrives(usbConnect.getDriveList(),usbConnect.getIdxDrive());
                } else {
                    badListDrives(usbConnect.getDriveList(), usbConnect.getIdxDrive());
                }
                break;                 
        }
        
    }
    
    private void goodListDrives(ObservableList <String> driveList, int idxList) {
        if (driveList.size() > 0) {
            chbSerial.getItems().clear();
            chbSerial.setItems(driveList);  
            chbSerial.setVisible(true);     
            chbSerial.getSelectionModel().select(idxList);       
            chbSerial.setVisible(true);     
            resCom = 5;
            actuLed();   
            flightListWithProgress();            
        }            
        
    }
    
    private void badListDrives(ObservableList <String> driveList, int idxList) {
        
        if (driveList.size() > 0) {
            chbSerial.getItems().clear();
            chbSerial.setItems(driveList);  
            chbSerial.setVisible(true);     
            chbSerial.getSelectionModel().select(idxList);       
            chbSerial.setVisible(true);     
            resCom = 4;
            actuLed();   
        }
        // Window must be intilaized/refreshed
        buttonBar.setVisible(true);
        hbTable.setVisible(false);          
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
                    } else {
                        portList.add(port);
                        if (lastSerialUsed.equals(port)) idxSerialList = idxListPort; 
                        idxListPort++;
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
            sbError = new StringBuilder(this.getClass().getName()+"."+Thread.currentThread().getStackTrace()[1].getMethodName());
            sbError.append("\r\n").append(ex.toString());
            mylogging.log(Level.SEVERE, sbError.toString());

        } catch (IOException ex) {
            sbError = new StringBuilder(this.getClass().getName()+"."+Thread.currentThread().getStackTrace()[1].getMethodName());
            sbError.append("\r\n").append(ex.toString());
            mylogging.log(Level.SEVERE, sbError.toString());
        }
    }
    
    /**
     * Old Flymaster series communication method
     */
    private void readFlymOld()  {
        try {
            flymasterold fmold = new flymasterold();
            if (fmold.init(currNamePort)) {     
                // Communication OK
                resCom = 1;
                // flight list of GPS is dowloaded from fms.getDeviceInfo method
                // fms fills the observable list 
                // serial port is closed
                idGPS = "Flymaster "+fmold.getDeviceType()+" "+fmold.getDeviceFirm();
                fmold.getListFlights(dataImport);
                if (dataImport.size() > 0) {
                    // Checking of already stored flights in logbook 
                    for (Gpsmodel nbItem : dataImport){                        
                        if (!checkInCarnet(nbItem.getDate(),nbItem.getHeure(),nbItem.getCol4())) {
                            String sdebug = "";
                            nbItem.setChecked(Boolean.TRUE);
                            nbItem.setCol6("NON");
                        } else {
                            nbItem.setCol6("OUI");
                        }
                        
                    }                                      
                    // Flymaster communication is OK
                    // GPS model and serial port are stored in settings
                    myConfig.setIdxGPS(4);
                    myConfig.setLastSerialCom(currNamePort);
                } else {
                    // Errror will be displayed in AfficheFlyList 
                    resCom = 6;
                }
            } else {
                // Errror will be displayed in AfficheFlyList 
                resCom = 2;
            }
        } catch (Exception e) {
            sbError = new StringBuilder(this.getClass().getName()+"."+Thread.currentThread().getStackTrace()[1].getMethodName());
            sbError.append("\r\n").append(e.toString());
            mylogging.log(Level.SEVERE, sbError.toString());            
        }                
    }
    
    
    /**
     * Flymaster SD series communication method
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
                idGPS = "Flymaster "+fms.getDeviceType()+" "+fms.getDeviceFirm();
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
                } else {
                    // Errror will be displayed in AfficheFlyList 
                    resCom = 6;                    
                }                    
            } else {
                // Errror will be displayed in AfficheFlyList 
                resCom = 2;
            }
        } catch (Exception e) {
            sbError = new StringBuilder(this.getClass().getName()+"."+Thread.currentThread().getStackTrace()[1].getMethodName());
            sbError.append("\r\n").append(e.toString());
            mylogging.log(Level.SEVERE, sbError.toString());            
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
                idGPS = fls.getDeviceType()+" "+fls.getDeviceFirm();
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
                } else {
                    // Errror will be displayed in AfficheFlyList 
                    resCom = 6;                             
                }
            } else {
                // Errror will be displayed in AfficheFlyList
                resCom = 2;
            }
        } catch (Exception e) {
            sbError = new StringBuilder(this.getClass().getName()+"."+Thread.currentThread().getStackTrace()[1].getMethodName());
            sbError.append("\r\n").append(e.toString());
            mylogging.log(Level.SEVERE, sbError.toString());            
        }                
    }
    
    /**
     * Flytec 6015 Brauniger IQ-Basic communication method
     */
    private void readFlytec15()  {
        try {
            flytec15 fliq = new flytec15();
            if (fliq.init(currNamePort)) {     
                // Communication OK
                resCom = 1;
                // flight list of GPS is dowloaded from fls.getDeviceInfo method
                // fls fills the observable list 
                // serial port is closed
                idGPS = fliq.getDeviceId();
                fliq.getListFlights(dataImport);
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
                    // Flyctec 6015 communication is OK
                    // GPS model and serial port are stored in settings
                    myConfig.setIdxGPS(2);
                    myConfig.setLastSerialCom(currNamePort);
                } else {
                    // Errror will be displayed in AfficheFlyList 
                    resCom = 6;                       
                }
            } else {
                // Errror will be displayed in AfficheFlyList
                resCom = 2;
            }
        } catch (Exception e) {
            sbError = new StringBuilder(this.getClass().getName()+"."+Thread.currentThread().getStackTrace()[1].getMethodName());
            sbError.append("\r\n").append(e.toString());
            mylogging.log(Level.SEVERE, sbError.toString());            
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
        } else if (resCom == 6) {
            alertbox aError = new alertbox(myConfig.getLocale());
            if (errorComMsg != null && !errorComMsg.isEmpty())
                aError.alertError(errorComMsg);
            else
                aError.alertNumError(1056);  // No valid tracks in GPS    
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
                // Sum checked flights
                for (Gpsmodel checkedData : dataImport) {
                    checkedData.checkedProperty().addListener((obs, wasChecked, isNowChecked) -> {
                        System.out.println("Touché");
                        actuMsgBar();
                    });
                }    
                
                if (tableImp.getItems().size() > 0) {
                    buttonBar.setVisible(true);
                    hbTable.setVisible(true);   
                    actuMsgBar();
                }
            }
        }
    }
    
    private void readUSBGps() {
        ArrayList<String> trackPathList = new ArrayList<>(); 
      //  ObservableList <Gpsmodel> importBrut = FXCollections.observableArrayList();  
        configProg otherConfig = new configProg();        
        dbSearch rechDeco = new dbSearch(myConfig); 
        String limitMsg = null;
        try {
            switch (currGPS) {
            case Rever:                
                idGPS = "Reversale ";
                usbRever.listTracksFiles(trackPathList);
                limitMsg = usbRever.getMsgClosingDate();
                break;   
            case Sky :
                idGPS = "Skytraxx 2 "+usbSky.getVerFirmware();
                usbSky.listTracksFiles(trackPathList);
                limitMsg = usbSky.getMsgClosingDate();
                break;
            case Sky3 :
                idGPS = "Skytraxx 3";
                usbSky3.listTracksFiles(trackPathList);
                limitMsg = usbSky3.getMsgClosingDate();
                break;                
            case Oudie :
                idGPS = "Naviter Oudie";
                usbOudie.listTracksFiles(trackPathList);  
                limitMsg = usbOudie.getMsgClosingDate();
                break;
            case Connect :
                idGPS = "Connect/Volirium";
                usbConnect.listTracksFiles(trackPathList);  
                limitMsg = usbConnect.getMsgClosingDate();
                break;                
            }
            // each gps track header must be decoded
            if (trackPathList.size() > 0) {
                for (String trackPath : trackPathList) {  
                    File fTrack = new File(trackPath);                    
                   // traceGPS tempTrack = new traceGPS(fTrack, false, myConfig);
                    traceGPS tempTrack = new traceGPS(fTrack, false, otherConfig);
                    if (tempTrack.isDecodage()) {                                                                                                
                        SimpleDateFormat sdfSql = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");                        
                        java.util.Date igcDate = sdfSql.parse(tempTrack.getDate_Vol_SQL());     
                        // For uniform format in AfficheFlyList, we want date like DD.MM.YY and time HH:MM:SS this is German display format.
                        DateFormat dfDge = DateFormat.getDateInstance(DateFormat.SHORT,java.util.Locale.GERMAN);
                        DateFormat dfTge = DateFormat.getTimeInstance(DateFormat.MEDIUM, java.util.Locale.GERMAN);                             
                        Gpsmodel oneFlight = new Gpsmodel();                                             
                        oneFlight.setDate(dfDge.format(igcDate));                
                        oneFlight.setHeure(dfTge.format(igcDate));                         
                        oneFlight.setCol4(tempTrack.getColDureeVol());
                        oneFlight.setCol5(trackPath);                            
                        // Is this flight already in logbook ?
                        boolean resDeco = rechDeco.searchVolByDeco(tempTrack.getDT_Deco(),tempTrack.getLatDeco(),tempTrack.getLongDeco());                        
                        if (!resDeco) {
                            oneFlight.setChecked(Boolean.TRUE);
                            oneFlight.setCol6("NON");
                        } else {
                            oneFlight.setCol6("OUI");
                        }
                        // for sorting the list we keep SQL date
                        oneFlight.setCol7(tempTrack.getDate_Vol_SQL());
                        dataImport.add(oneFlight);    
                    }                    
                }
                Comparator<? super Gpsmodel> comparatorDate = new Comparator<Gpsmodel>() {
                    @Override
                    public int compare(Gpsmodel o1, Gpsmodel o2) {
                        // order asc -> o1.getCol7().compareTo(o2.getCol7());
                        // order desc
                        return o2.getCol7().compareTo(o1.getCol7());
                    }
                };                
                FXCollections.sort(dataImport, comparatorDate);                
                resCom = 5;
                // GPS model is stored in settings
                switch (currGPS) {
                    case Rever:                                            
                        myConfig.setIdxGPS(5);
                        break;      
                    case Sky:                                            
                        myConfig.setIdxGPS(6);
                        break;   
                    case Sky3:                                            
                        myConfig.setIdxGPS(13);
                        break;                        
                    case Oudie:                                            
                        myConfig.setIdxGPS(7);
                        break;     
                    case Connect:                                            
                        myConfig.setIdxGPS(12);
                        break;                         
                }
            } else {
                // No alert box possible in this thread
                errorComMsg = i18n.tr("Pas de traces depuis le ")+limitMsg;
                resCom = 6;  
            }
        } catch (Exception e) {
            sbError = new StringBuilder(this.getClass().getName()+"."+Thread.currentThread().getStackTrace()[1].getMethodName());
            sbError.append("\r\n").append(e.toString());
            mylogging.log(Level.SEVERE, sbError.toString());
        }
        
    }
    
    
    private void flightListSimple() {
        System.out.println("Liste simple");
        readUSBGps();
        afficheFlyList();
    }
    
    /**
     * download flights list from GPS in a different thread with a progressbar
     */
    private void flightListWithProgress() {
        
        errorComMsg = null;
        
        ProgressForm pForm = new ProgressForm();
           
        Task<Void> task = new Task<Void>() {
            @Override
            public Void call() throws InterruptedException { 
                switch (currGPS) {
                    case Flytec20 :
                        readFlytec20();
                        break;
                    case Flytec15 :
                        readFlytec15();
                        break;
                    case FlymSD :
                        readFlymaster();
                        break;
                    case FlymOld :
                        readFlymOld();
                        break;
                    case Rever :
                        readUSBGps();
                        break;
                    case Sky :
                        readUSBGps();
                        break;
                    case Sky3 :
                        readUSBGps();
                        break;                        
                    case Oudie :
                        readUSBGps();
                        break;                        
                    case Connect :
                        readUSBGps();
                        break;                          
                }       
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
                goToolTip.setText(i18n.tr("Actualiser la liste"));
                btnGo.setTooltip(goToolTip);
                imgLed.setVisible(true);
                break;
            case 3 :
                // No serial port
                btnGo.setVisible(false);
                imgLed.setVisible(false);
                break;
            case 4 :
                // USB Gps no connected
                btnGo.setVisible(false);
                imgImgLed = new Image(getClass().getResourceAsStream("/images/Led_red.png"));
                imgLed.setImage(imgImgLed);                
                imgLed.setVisible(true);
                imgGo = new Image(getClass().getResourceAsStream("/images/Refresh.png"));
                btnGo.setGraphic(new ImageView(imgGo));                
                goToolTip.setText(i18n.tr("Actualiser la liste"));
                btnGo.setVisible(true);
                break;
            case 5 :
                // USB Gps connected
                btnGo.setVisible(false);
                imgImgLed = new Image(getClass().getResourceAsStream("/images/Led_Green.png"));
                imgLed.setImage(imgImgLed);                
                imgLed.setVisible(true);
                break;
        }
    }
    
    /**
     * Insertion of checked flights in the logbook with a different thread
     */
    private void insertWithProgress() {
        ProgressForm pForm = new ProgressForm();
           
        Task<Void> task = new Task<Void>() {
            @Override
            public Void call() throws InterruptedException { 
                // actually we can use a common method
                // if necessary, we put a switch (currGPS) here
                insertFromGPS();
                
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
    * For each flight to insert, specific download GPS instruction is called
    * IGC file is inserted in database
    */
    private void insertFromGPS() {                        
        int nbFlightIn = 0;
        boolean gpsOK = false;
        StringBuilder errMsg = new StringBuilder();         
        
        ObservableList <Gpsmodel> checkedData = tableImp.getItems(); 
        try {
            flytec20 fls = new flytec20();                      
            flytec15 fliq = new flytec15();
            flymaster fms = new flymaster();
            flymasterold fmold = new flymasterold();
            switch (currGPS) {
                    case Flytec20 :
                        if (fls.iniForFlights(currNamePort)) gpsOK = true;  
                        break;
                    case Flytec15 :
                        if (fliq.iniForFlights(currNamePort)) gpsOK = true;  
                        break;    
                    case FlymSD :
                        if (fms.iniForFlights(currNamePort)) gpsOK = true;  
                        break;
                    case FlymOld :
                        if (fmold.iniForFlights(currNamePort)) gpsOK = true;
                        break;  
                    case Rever :
                        gpsOK = usbRever.isConnected();
                        break;
                    case Sky :
                        gpsOK = usbSky.isConnected();
                        break;        
                    case Sky3 :
                        gpsOK = usbSky3.isConnected();
                        break;                         
                    case Oudie :
                        gpsOK = usbOudie.isConnected();
                        break;   
                    case Connect :
                        gpsOK = usbConnect.isConnected();
                        break;                           
            }            
            if (gpsOK){      
                for (Gpsmodel item : checkedData){
                    strTrack = null;   
                    if (item.getChecked())  {     
                        try {
                            // Download instruction of the flight is stored in column 5
                            switch (currGPS) {
                            case Flytec20 :
                                strTrack = fls.getIGC(item.getCol5());
                                break;
                            case Flytec15 :
                                strTrack = fliq.getIGC(item.getCol5());
                                break;    
                            case FlymSD :
                                // Download instruction of the flight is stored in column 5
                                // IGC date is compsed with column 1
                                // Col 1 [26.04.17] -> [260417]
                                String sDate = item.getDate().replaceAll("\\.", "");
                                if (fms.getIGC(item.getCol5(), sDate, myConfig.getDefaultPilote(), myConfig.getDefaultVoile())) {
                                    strTrack = fms.getFinalIGC();
                                } else {
                                    strTrack = null;
                                }
                                break;
                            case FlymOld :
                                if (fmold.getIGC(item.getCol5(), myConfig.getDefaultPilote(), myConfig.getDefaultVoile())) {                            
                                    strTrack = fmold.getFinalIGC();
                                } else {
                                    strTrack = null;
                                }
                                break;
                            case Rever :
                                strTrack = usbRever.getTrackFile(item.getCol5());
                                break;
                            case Sky :
                                strTrack = usbSky.getTrackFile(item.getCol5());
                                break;  
                            case Sky3 :
                                strTrack = usbSky3.getTrackFile(item.getCol5());
                                break;                                  
                            case Oudie :
                                strTrack = usbOudie.getTrackFile(item.getCol5());
                                break;         
                            case Connect :
                                strTrack = usbConnect.getTrackFile(item.getCol5());
                                break;                                      
                            }                                  
                            if (strTrack != null ) {                                
                                traceGPS downTrack = new traceGPS(strTrack, "", true, myConfig);
                                if (downTrack.isDecodage()) { 
                                    dbAdd myDbAdd = new dbAdd(myConfig);
                                    int resAdd = myDbAdd.addVolCarnet(downTrack);
                                    if (resAdd == 0) nbFlightIn++;
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
                switch (currGPS) {
                    case Flytec20 :
                        fls.closePort();
                        break;
                    case Flytec15 :
                        fliq.closePort();
                        break;    
                    case FlymSD :
                        fms.closePort();
                        break;
                    case FlymOld :
                        fmold.closePort();
                        break;
                }                              
                nbInserted = nbFlightIn;
                errInsertion = errMsg.toString();
            }
        } catch (Exception e) {
            sbError = new StringBuilder(this.getClass().getName()+"."+Thread.currentThread().getStackTrace()[1].getMethodName());
            sbError.append("\r\n").append(e.toString());
            mylogging.log(Level.SEVERE, sbError.toString());            
        } 
    }
            
    /**
     * Check if a flight already exists in the db file - called by readxxGPS method
     * strDate should be in "dd.MM.yy" format
     * strHeure should be in "HH:mm:ss" format
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
                    res = rech.Rech_Vol_by_Duree(sbDate.toString(),tbHeure[1],tbHeure[2],totSec);
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
     * read a single track in a different thread - called by askOneTrack
     * reqGPS is the dedicated download instruction 
     */
    private void oneFlightWithProgress(Gpsmodel selLineTable) {        
        strTrack = null;   
        errorComMsg = null;
        
        ProgressForm pForm = new ProgressForm();
           
        Task<Void> task = new Task<Void>() {
            @Override
            public Void call() throws InterruptedException { 
                try {
                    switch (currGPS) {
                    case Flytec20 :
                        flytec20 fls = new flytec20();
                        if (fls.iniForFlights(currNamePort)) { 
                            // Download instruction of the flight is stored in column 5
                            strTrack = fls.getIGC(selLineTable.getCol5());
                            fls.closePort(); 
                            resCom = 0;
                        } else {
                            errorComMsg = fls.getError();
                        }
                        break;
                    case Flytec15 :
                        flytec15 fliq = new flytec15();
                        if (fliq.iniForFlights(currNamePort)) { 
                            // Download instruction of the flight is stored in column 5
                            strTrack = fliq.getIGC(selLineTable.getCol5());
                            fliq.closePort(); 
                            resCom = 0;
                        } else {
                            errorComMsg = fliq.getError();
                        }
                        break;
                    case FlymSD :
                        flymaster fms = new flymaster();
                        if (fms.iniForFlights(currNamePort)) {
                            // Download instruction of the flight is stored in column 5
                            // IGC date is composed with column 1
                            // Col 1 [26.04.17] -> [260417]
                            String sDate = selLineTable.getDate().replaceAll("\\.", "");
                            if (fms.getIGC(selLineTable.getCol5(), sDate, myConfig.getDefaultPilote(), myConfig.getDefaultVoile())) {
                                strTrack = fms.getFinalIGC();
                                fms.closePort(); 
                                resCom = 0;
                            } else {
                                errorComMsg = fms.getError();
                            }
                        } else {
                            errorComMsg = fms.getError();
                        }
                        break;
                    case FlymOld :
                        flymasterold fmold = new flymasterold();
                        if (fmold.iniForFlights(currNamePort)) {          
                            if (fmold.getIGC(selLineTable.getCol5(), myConfig.getDefaultPilote(), myConfig.getDefaultVoile())) {
                                strTrack = fmold.getFinalIGC();
                                fmold.closePort(); 
                                resCom = 0;
                            } else {
                                resCom = 2;   // No GPS answer
                            }
                        }
                        break;
                    case Rever :
                        strTrack = usbRever.getTrackFile(selLineTable.getCol5());
                        if (strTrack != null && !strTrack.isEmpty()) {
                            resCom = 0;
                        } else {
                            resCom = 2;   // No GPS answer
                        }
                        break;
                    case Sky :
                        strTrack = usbSky.getTrackFile(selLineTable.getCol5());
                        if (strTrack != null && !strTrack.isEmpty()) {
                            resCom = 0;
                        } else {
                            resCom = 2;   // No GPS answer
                        }     
                        break;
                    case Sky3 :
                        strTrack = usbSky3.getTrackFile(selLineTable.getCol5());
                        if (strTrack != null && !strTrack.isEmpty()) {
                            resCom = 0;
                        } else {
                            resCom = 2;   // No GPS answer
                        }     
                        break;                        
                    case Oudie :
                        strTrack = usbOudie.getTrackFile(selLineTable.getCol5());
                        if (strTrack != null && !strTrack.isEmpty()) {
                            resCom = 0;
                        } else {
                            resCom = 2;   // No GPS answer
                        }     
                        break;
                    case Connect :
                        strTrack = usbConnect.getTrackFile(selLineTable.getCol5());
                        if (strTrack != null && !strTrack.isEmpty()) {
                            resCom = 0;
                        } else {
                            resCom = 2;   // No GPS answer
                        }     
                        break;                        
                    default:
                        throw new AssertionError();
                    }                    
                } catch (Exception e) {
                    resCom = 2;   // No GPS answer
                    sbError = new StringBuilder(this.getClass().getName()+"."+Thread.currentThread().getStackTrace()[1].getMethodName());
                    sbError.append("\r\n").append(e.toString());
                    mylogging.log(Level.SEVERE, sbError.toString());                    
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
            if (resCom == 0 && strTrack != null && !strTrack.isEmpty()) {                       
                traceGPS reqIGC = new traceGPS(strTrack, "", true, myConfig);
                if (reqIGC.isDecodage()) { 
                    showOneTrack(reqIGC);
                } else {
                    displayErrDwnl(reqIGC);
                }
            } else {
                if (errorComMsg != null)  {
                    System.out.println("errorComMsg : "+errorComMsg);
                    alertbox aError = new alertbox(myConfig.getLocale());
                    aError.alertError(errorComMsg);
                } else if (strTrack == null) {
                    alertbox aError = new alertbox(myConfig.getLocale());
                    aError.alertNumError(1054);  // track file is empty       
                } else if (resCom == 2)  {
                    alertbox aError = new alertbox(myConfig.getLocale());
                    aError.alertNumError(1052);  // No GPS answer
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
     * Clic on button "Track display" (FXML triggered)
     */
    public void askOneTrack() {
        if(tableImp.getSelectionModel().getSelectedItem() != null)  {
            Gpsmodel currLineSelection = tableImp.getSelectionModel().getSelectedItem();  
            oneFlightWithProgress(currLineSelection);              
        } else {
            alertbox aInfo = new alertbox(myConfig.getLocale());
            aInfo.alertInfo(i18n.tr("Sélectionnez un vol par un clic gauche"));
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
        // clear status bar
        rootController.updateMsgBar("", false, 60);
        winTraduction();
        iniChbGPS();
    }
    
    /**
     * New GPS -> window is cleaned
     */
    private void winReset() {
        
        buttonBar.setVisible(false);
        hbTable.setVisible(false);    
        lbPort.setVisible(false);
        chbSerial.setVisible(false);
        btnGo.setVisible(false); 
        imgLed.setVisible(false);                
        tableImp.getItems().clear();        
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
