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
import gps.compass;
import gps.connect;
import gps.digifly;
import gps.element;
import gps.flymaster;
import gps.flymasterold;
import gps.flynet;
import gps.flytec15;
import gps.flytec20;
import gps.gpsdump;
import gps.oudie;
import gps.reversale;
import gps.sensbox;
import gps.skytraax;
import gps.skytraxx3;
import gps.syride;
import gps.varduino;
import gps.xctracer;
import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.logging.Level;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.Tooltip;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
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
import littlewins.winGPS;
import model.Gpsmodel;
import org.controlsfx.dialog.ProgressDialog;
import org.xnap.commons.i18n.I18n;
import org.xnap.commons.i18n.I18nFactory;
import settings.configProg;
import settings.osType;
import systemio.mylogging;
import trackgps.traceGPS;

/**
 * FXML Controller class
 *
 * @author gil
 * 
 * setMyConfig : 
 *          Start method, displayWinGPS is called. 
 * Select GPS : display winGPS
 * displayWinGPS : return from winGPS, if a GPS is connected, FlightListWithProgress is started
 * readGPS : at clic on btnGo, run flightListWithProgress()
 * afficheFlyList :   started at the end of flightListWithProgress()
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
    private Button btnSelectGPS;
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
    private HBox buttonBar;     // bar with btnMaj, btnVisu and BtnDecocher
    @FXML
    private HBox hbTable;

    // Localization
    private I18n i18n; 
    
    // Configuration settings
    configProg myConfig;
                
    private RootLayoutController rootController;
    
    private enum gpsType {Flytec20,Flytec15,Flynet,FlymOld,Rever,Sky,Oudie,Element,Sensbox,Syride,FlymSD,Connect,Sky3,CPilot,XCTracer, FlymPlus }
    
    // current GPS
  //  private gpsType currGPS;
    
    private winGPS.gpsType currGPS;    
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
    private flynet usbFlynet;
    private varduino usbVarduino;
    private sensbox usbSensbox;
    private syride diskSyr;
    private connect usbConnect;
    private compass usbCompass;
    private element usbElem;
    private xctracer usbXctracer;
    private String strTrack;
    private String errorComMsg;
    private int nbTracks = 0;
    private int nbNewTracks = 0;
    private String idGPS = "";
    private boolean mDebug;
    private File fDebug = null;
    
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
        sbMsg.append(idGPS).append("   ").append(i18n.tr("GPS tracks")).append(" : ").append(String.valueOf(nbTracks));
        sbMsg.append("   ").append(i18n.tr("Tracks to be added")).append(" : ").append(String.valueOf(nbNewTracks));
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
        dialogbox dConfirm = new dialogbox(i18n);
        StringBuilder sbMsg = new StringBuilder();
        sbMsg.append(String.valueOf(nbVols)).append(" ").append(i18n.tr("flights to insert")).append(" ?");
        if (dConfirm.YesNo("", sbMsg.toString()))   {     
            nbToInsert = nbVols;
           // insertWithProgress();        
           gpsInsertion();
        }              
    }
   
    public void displayWinGPS() {    
        if (selectGPS()) {
            flightListWithProgress();    
        }
    }   
    
    @FXML
    private void handleGPS() {
        winReset();
        // Necessary because we are in a loop with last GPS memorized in settings
        // We can't change GPS model
        myConfig.setIdxGPS(0);
        displayWinGPS();
    }
    
   /**
     * displayName is a flag for waypoint type name textfield display
     * @param displayName
     * @return 
     */
    private boolean selectGPS() {
        boolean res = false;   
        winGPS myWin = new winGPS(myConfig, i18n, false);    
        if (myWin.getCurrGPS() != null && myWin.getCurrNamePort() != null && myWin.isGpsConnect()) {            
            currGPS = myWin.getCurrGPS();
            currNamePort = myWin.getCurrNamePort();
            myConfig.setLastSerialCom(currNamePort);
            idGPS = myWin.getGpsCharac();
            res = true;
        }
        
        return res;
    }    
                                    
    private void gpsdReadFlightList() {
        if (mDebug) mylogging.log(Level.INFO, "gpsdReadFlightList on GPS "+currGPS);
        gpsdump gpsd = new gpsdump(this, 8, currNamePort, myConfig, i18n);
        switch (currGPS) {
            case FlymSD :
                gpsd.askFlightsList(1);
                break;
            case FlymOld :
                gpsd.askFlightsList(2);
                break;
            case Flytec20 :
                gpsd.askFlightsList(3);
                break;   
            case Flytec15 :
                gpsd.askFlightsList(8);
                break;
        }
        dataImport = gpsd.getListFlights();
        if (dataImport.size() > 0) {
            resCom = 1;
            // Checking of already stored flights in logbook 
            for (Gpsmodel nbItem : dataImport){
                if (!checkInCarnet(nbItem.getDate(),nbItem.getHeure(),nbItem.getCol4())) {
                    nbItem.setChecked(Boolean.TRUE);
                    nbItem.setCol6("NON");
                } else {
                    nbItem.setCol6("OUI");
                }                        
            }      
            switch (currGPS) {
            case FlymSD :
                myConfig.setIdxGPS(11);
                break;
            case FlymOld :
                myConfig.setIdxGPS(4); 
                break;
            case Flytec20 :
                myConfig.setIdxGPS(1); 
                break;   
            case Flytec15 :
                myConfig.setIdxGPS(2);
                break;
            }
        } else  {
            // Errror will be displayed in AfficheFlyList 
            resCom = 6;      
            errorComMsg = gpsd.getStrLog();
        }        
    }
    
    /**
     * Flymaster SD series communication method
     */
    private void readFlymaster()  {
        
        String debugPath = "";
        try {
            if (fDebug != null && fDebug.exists()) debugPath = fDebug.getAbsolutePath()+File.separator;
            flymaster fms = new flymaster(myConfig.isDebugMode(), debugPath);
            String s = "fms called sur "+currNamePort;
            if (mDebug) mylogging.log(Level.INFO, s);   
            System.out.println(s);
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
                    switch (currGPS) {
                        case FlymSD:
                            myConfig.setIdxGPS(11);        
                            break;
                        case FlymPlus:
                            myConfig.setIdxGPS(16);
                    }                    
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
     * Old Flymaster series communication method
     */
    private void readFlymOld()  {
        String debugPath = "";
        if (fDebug != null && fDebug.exists()) debugPath = fDebug.getAbsolutePath()+File.separator;
        try {
            flymasterold fmold = new flymasterold(myConfig.isDebugMode(), debugPath);
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
     * Old Flymaster series communication method
     */
    private void readDigifly()  {
        String debugPath = "";
        try {
            if (fDebug != null && fDebug.exists()) debugPath = fDebug.getAbsolutePath()+File.separator;
            digifly dig = new digifly(myConfig.isDebugMode(), debugPath);
            String s = "dig called sur "+currNamePort;
            if (mDebug) mylogging.log(Level.INFO, s);   
            System.out.println(s);
            if (dig.init(currNamePort)) {     
                // Communication OK
                resCom = 1;
                // flight list of GPS is dowloaded from fms.getDeviceInfo method
                // fms fills the observable list 
                // serial port is closed
                idGPS = "Digifly Air "+dig.getDeviceType()+" "+dig.getDeviceFirm();
                dig.getListFlights(dataImport);
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
                    // Digifly communication is OK
                    // GPS model and serial port are stored in settings
                    myConfig.setIdxGPS(17);                         
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
            String s = "readFlytec 20 sur "+currNamePort;
            if (mDebug) mylogging.log(Level.INFO, s);   
            System.out.println(s);
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
            String s = "readFlytec 15 sur "+currNamePort;
            if (mDebug) mylogging.log(Level.INFO, s);   
            System.out.println(s);
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
                Column4.setText(i18n.tr("Duration"));
                Column5.setVisible(false);
                Column6.setVisible(false);
                tableImp.setMaxWidth(300);
                tableImp.setStyle("-fx-selection-bar: red;");
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
                usbRever = new reversale(myConfig.getOS(), myConfig.getGpsLimit());
                if (usbRever.isConnected()) {
                    idGPS = "Reversale ";
                    usbRever.listTracksFiles(trackPathList);
                    limitMsg = usbRever.getMsgClosingDate();
                } else {
                    resCom = 2;
                }                
                break;   
            case Sky :
                usbSky = new skytraax(myConfig.getOS(), myConfig.getGpsLimit());
                if (usbSky.isConnected()) {
                    idGPS = "Skytraxx 2 "+usbSky.getVerFirmware();
                    usbSky.listTracksFiles(trackPathList);
                    limitMsg = usbSky.getMsgClosingDate();
                } else {
                    resCom = 2;
                }  
                break;
            case Sky3 :
                usbSky3 = new skytraxx3(myConfig.getOS(), myConfig.getGpsLimit());
                if (usbSky3.isConnected()) {
                    idGPS = "Skytraxx 3";
                    usbSky3.listTracksFiles(trackPathList);
                    limitMsg = usbSky3.getMsgClosingDate();
                } else {
                    resCom = 2;
                }                     
                break;    
            case Flynet :
                usbFlynet = new flynet(myConfig.getOS(), myConfig.getGpsLimit());
                if (usbFlynet.isConnected()) {                
                    idGPS = "Flynet XC "+usbFlynet.getVerFirmware();
                    usbFlynet.listTracksFiles(trackPathList);
                    limitMsg = usbFlynet.getMsgClosingDate();  
                } else {
                    resCom = 2;
                }                                                        
                break;
            case Sensbox :
                usbSensbox = new sensbox(myConfig.getOS(), myConfig.getGpsLimit());
                if (usbSensbox.isConnected()) {                
                    idGPS = "Flytec Sensbox";
                    usbSensbox.listTracksFiles(trackPathList);  
                    limitMsg = usbSensbox.getMsgClosingDate();
                } else {
                    resCom = 2;
                }                         
                break;                
            case Oudie :                 
                usbOudie = new oudie(myConfig.getOS(), myConfig.getGpsLimit());
                if (usbOudie.isConnected()) {
                    idGPS = "Naviter Oudie";
                    usbOudie.listTracksFiles(trackPathList);  
                    limitMsg = usbOudie.getMsgClosingDate();
                } else {
                    resCom = 2;
                }  
                break;
            case Syride :
                diskSyr = new syride(myConfig.getOS(), myConfig.getGpsLimit(),myConfig.getPathSyride()); 
                if (currGPS.equals(winGPS.gpsType.Syride)) System.out.println("Coucou 2 ");
                if (diskSyr.isConnected()) {                
                    idGPS = "Sys PC Tool";
                    diskSyr.listTracksFiles(trackPathList);  
                    limitMsg = "";
                    if (currGPS.equals(winGPS.gpsType.Syride)) System.out.println("Coucou 3");
                } else {
                    resCom = 2;
                }                      
                break;                
            case Connect :
                usbConnect = new connect(myConfig.getOS(), myConfig.getGpsLimit());
                if (usbConnect.isConnected()) {
                    idGPS = "Connect/Volirium";
                    usbConnect.listTracksFiles(trackPathList);  
                    limitMsg = usbConnect.getMsgClosingDate();
                } else {
                    resCom = 2;
                } 
                break;   
            case Element :
                usbElem = new element(myConfig.getOS(), myConfig.getGpsLimit());
                if (usbElem.isConnected()) {                
                    idGPS = "Flytec Element";
                    usbElem.listTracksFiles(trackPathList);  
                    limitMsg = usbElem.getMsgClosingDate();
                } else {
                    resCom = 2;
                }                     
                break;                   
            case CPilot :
                usbCompass = new compass(myConfig.getOS(), myConfig.getGpsLimit());
                if (usbCompass.isConnected()) {                
                    idGPS = "C-Pilot Evo";
                    usbCompass.listTracksFiles(trackPathList);  
                    limitMsg = usbCompass.getMsgClosingDate();
                } else {
                    resCom = 2;
                }
                break;                                               
            case XCTracer :
                usbXctracer = new xctracer(myConfig.getOS(), myConfig.getGpsLimit());
                if (usbXctracer.isConnected()) {
                    idGPS = "XC Tracer II";
                    usbXctracer.listTracksFiles(trackPathList);  
                    limitMsg = usbXctracer.getMsgClosingDate();
                } else {
                    resCom = 2;
                }                    
                break;                 
            case Vardui :
                usbVarduino = new varduino(myConfig.getOS(), myConfig.getGpsLimit());
                if (usbVarduino.isConnected()) {
                    idGPS = "Varduino";
                    usbVarduino.listTracksFiles(trackPathList);  
                    limitMsg = usbVarduino.getMsgClosingDate();
                } else {
                    resCom = 2;
                }                    
                break;                  
            }       
            // each gps track header must be decoded
            if (trackPathList.size() > 0) {
                for (String trackPath : trackPathList) {  
                    File fTrack = new File(trackPath);                    
                   // traceGPS tempTrack = new traceGPS(fTrack, false, myConfig);
                    traceGPS tempTrack = new traceGPS(fTrack, false, otherConfig);
                    if (tempTrack.isDecodage()) {            
                        if (currGPS == winGPS.gpsType.Rever) {
                            int Annee = tempTrack.getDate_Vol().getYear();
                            if (Annee > 2098 || Annee < 2011) {                                               
                                traceGPS newTrack = bug2019(tempTrack,false);
                                if (newTrack.isDecodage()) tempTrack = newTrack;
                            }
                        }      
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
                    case Flynet:
                        myConfig.setIdxGPS(3);
                        break;   
                    case Sensbox:                                            
                        myConfig.setIdxGPS(9);
                        break;                           
                    case Oudie:                                            
                        myConfig.setIdxGPS(7);
                        break;   
                    case Syride:                                            
                        myConfig.setIdxGPS(10);
                        break;                           
                    case Connect:                                            
                        myConfig.setIdxGPS(12);
                        break;     
                    case Element :                                            
                        myConfig.setIdxGPS(8);
                        break;                         
                    case CPilot:                                            
                        myConfig.setIdxGPS(14);
                        break;    
                    case Vardui:                                            
                        myConfig.setIdxGPS(18);
                        break;                            
                }
            } else {
                // No alert box possible in this thread
                if (currGPS == winGPS.gpsType.Syride) {
                    errorComMsg = i18n.tr("No tracks in the Syride folder");
                } else {
                    errorComMsg = i18n.tr("No tracks since the")+"  "+limitMsg;
                }
                resCom = 6;  
            }
        } catch (Exception e) {
            sbError = new StringBuilder(this.getClass().getName()+"."+Thread.currentThread().getStackTrace()[1].getMethodName());
            sbError.append("\r\n").append(e.toString());
            mylogging.log(Level.SEVERE, sbError.toString());
        }
        
    }
    
    /**
     * download flights list from GPS in a different thread with a progressbar
     * This is a second version. First version crashed in MacOS versions < 10.14 with update JDK 212
     * Code development and debugging in controlprog project
     */
    private void flightListWithProgress() {
        Task<Object> worker = new Task<Object>() {
            @Override
            protected Object call() throws Exception {
                updateMessage(i18n.tr("Retrieving the list of flights in progress"));
                switch (currGPS) {
                    case Flytec20 :   
                        // We don't use gpsDump beacause our old Flytec 6030 
                        // has flights dated 00.00.00, it causes a crash
                        readFlytec20();
                        break;
                    case FlymOld :
                        gpsdReadFlightList();                     
                        break;  
                    case FlymPlus :    
                        readFlymaster();
                        break;
                    case FlymSD :
                        gpsdReadFlightList();
                        break;                        
                    case Flytec15 :
                        switch (myConfig.getOS()) {
                            case WINDOWS :
                                gpsdReadFlightList();
                                break;
                            case LINUX :     
                            case MACOS :
                                readFlytec15();
                                break;                                
                        } 
                        break;
                   case Digifly:
                        readDigifly();
                        break;
                    case Rever :
                    case Sky :
                    case Sky3 :    
                    case Flynet :
                    case Sensbox :                      
                    case Oudie :
                    case Syride :                          
                    case Connect : 
                    case Element :                    
                    case CPilot : 
                    case XCTracer :
                    case Vardui :
                        readUSBGps();
                        break;                        
                }
                return null;
            }
        };  
        
        worker.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
            @Override
            public void handle(WorkerStateEvent t) {
                afficheFlyList();
            }
        });        
        
        ProgressDialog dlg = new ProgressDialog(worker);
        dlg.setHeaderText(i18n.tr("GPS import"));
        dlg.setTitle("");

        Thread th = new Thread(worker);
        th.setDaemon(true);
        th.start();        
    }    
                    
    /**
    * For each flight to insert, specific download GPS instruction is called
    * IGC file is inserted in database
    */
    private void gpsInsertion() {                        
        boolean gpsOK = false;
        String debugPath = "";
        StringBuilder errMsg = new StringBuilder();      
        if (mDebug) mylogging.log(Level.INFO, "gpsInsertion begin");
        
        ObservableList <Gpsmodel> checkedData = tableImp.getItems(); 
        try {
            flytec20 fls = new flytec20(); 
            flytec15 fliq = new flytec15();
            if (fDebug != null && fDebug.exists()) debugPath = fDebug.getAbsolutePath()+File.separator;
            flymaster fms = new flymaster(myConfig.isDebugMode(), debugPath);
            digifly dig = new digifly(myConfig.isDebugMode(), debugPath);            
            flymasterold fmold = new flymasterold(myConfig.isDebugMode(), debugPath);            
            gpsdump gpsd = new gpsdump(this, 7, currNamePort, myConfig, i18n);
            switch (currGPS) {
                    case Flytec20 :
                        // if we're here, it's because the flightlist has been posted
                        switch (myConfig.getOS()) {
                            case WINDOWS :
                                gpsOK = true; 
                                break;
                            case LINUX :
                            case MACOS :                                
                                if (fls.isPresent(currNamePort)) {
                                    gpsOK = true;
                                    // if GPSDump used, we must release the port 
                                    // Otherwise, port must stay open
                                    //  fliq.closePort();
                                }          
                        }       
                        break;
                    case Flytec15 :
                        // if we're here, it's because the flightlist has been posted
                        switch (myConfig.getOS()) {
                            case WINDOWS :
                                gpsOK = true; 
                                break;
                            case LINUX :
                            case MACOS :                                
                                if (fliq.isPresent(currNamePort)) {
                                    gpsOK = true;
                                    // if GPSDump used, we must release the port 
                                    // Otherwise, port must stay open
                                    //  fliq.closePort();
                                }          
                        }
                        break;    
                    case FlymSD :
                        // if we're here, it's because the flightlist has been downloaded
                        gpsOK = true; 
                        break;                                                                     
                    case FlymPlus :
                        if (fms.isPresent(currNamePort)) {
                            gpsOK = true;  
                        }                            
                        break;    
                    case Digifly :
                        if (dig.isPresent(currNamePort)) {
                            gpsOK = true;  
                        }                            
                        break;                         
                    case FlymOld :   
                        gpsOK = true;      
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
                    case Flynet :
                        gpsOK = usbFlynet.isConnected();
                        break;      
                    case Sensbox :
                        gpsOK = usbSensbox.isConnected();
                        break;                          
                    case Oudie :
                        gpsOK = usbOudie.isConnected();
                        break;   
                    case Syride :
                        gpsOK = diskSyr.isConnected();
                        break;                           
                    case Connect :
                        gpsOK = usbConnect.isConnected();
                        break;   
                    case Element :
                        gpsOK = usbElem.isConnected();
                        break;                           
                    case CPilot :
                        gpsOK = usbCompass.isConnected();
                        break;  
                    case XCTracer :
                        gpsOK = usbXctracer.isConnected();
                        break;                        
                    case Vardui :
                        gpsOK = usbVarduino.isConnected();
                        break;                           
            }            
            if (gpsOK){                      
                   
                if (mDebug) mylogging.log(Level.INFO, "gpsInsertion begin loop");
                Task<Object> worker = new Task<Object>()  {
                    
                    @Override
                    protected Object call() throws Exception {
                        int idxTable = -1;
                        int nbFlightIn = 0;
                        int nbFlightRead = 0;
                        int totalInsert = 0;
                        for (Gpsmodel itemCount : checkedData){                    
                            if (itemCount.getChecked()) totalInsert++;                    
                        }                                
                        for (Gpsmodel item : checkedData){                    
                            strTrack = null;   
                            idxTable++;
                            if (item.getChecked())  { 
                                nbFlightRead++;
                                try {
                                    // Download instruction of the flight is stored in column 5
                                    switch (currGPS) {
                                    case Flytec20 :                                           
                                        switch (myConfig.getOS()) {
                                            case WINDOWS :                                        
                                                strTrack = gpsd.directFlight(3,idxTable);
                                                break;
                                            case LINUX :
                                            case MACOS :
                                                strTrack = fls.getIGC(item.getCol5());
                                                break;
                                        }                                        
                                        break;
                                    case Flytec15 :
                                        switch (myConfig.getOS()) {
                                            case WINDOWS :                                        
                                                strTrack = gpsd.directFlight(8,idxTable);
                                                break;
                                            case LINUX :
                                            case MACOS :
                                                strTrack = fliq.getIGC(item.getCol5());
                                                break;
                                        }
                                        break;    
                                    case FlymSD :
                                        strTrack = gpsd.directFlight(1,idxTable);
                                        break;                                          
                                    case FlymPlus :
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
                                    case Digifly :
                                        // Download instruction of the flight is stored in column 5
                                        if (dig.getIGC(item.getCol5(), myConfig.getDefaultPilote(), myConfig.getDefaultVoile())) {
                                            strTrack = dig.getFinalIGC();
                                        } else {
                                            strTrack = null;
                                        }                                           
                                        break;                                        
                                    case FlymOld :
                                        switch (myConfig.getOS()) {
                                            case WINDOWS :
                                                // Strangely, for Flymater Old, the first record has the index 0 so -> idx - 1
                                                idxTable = idxTable - 1;
                                                strTrack = gpsd.directFlight(2,idxTable);
                                                break;
                                            case MACOS :                              
                                            case LINUX : 
                                                strTrack = gpsd.directFlight(2,idxTable);
                                            break;
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
                                    case Flynet :
                                        strTrack = usbFlynet.getTrackFile(item.getCol5());
                                        break;  
                                    case Sensbox :
                                        strTrack = usbSensbox.getTrackFile(item.getCol5());
                                        break;                                   
                                    case Oudie :
                                        strTrack = usbOudie.getTrackFile(item.getCol5());
                                        break;     
                                    case Syride :
                                        strTrack = diskSyr.getTrackFile(item.getCol5());
                                        break;                                  
                                    case Connect :
                                        strTrack = usbConnect.getTrackFile(item.getCol5());
                                        break;    
                                    case Element :
                                        strTrack = usbElem.getTrackFile(item.getCol5());
                                        break;                                   
                                    case CPilot :
                                        strTrack = usbCompass.getTrackFile(item.getCol5());
                                        break;                                  
                                    case XCTracer :
                                        strTrack = usbXctracer.getTrackFile(item.getCol5());
                                        break;  
                                    case Vardui :
                                        strTrack = usbVarduino.getTrackFile(item.getCol5());
                                        break;                                          
                                    }                                  
                                    if (strTrack != null ) {                                
                                        traceGPS downTrack = new traceGPS(strTrack, "", true, myConfig);
                                        if (downTrack.isDecodage()) {  
                                            if (currGPS == winGPS.gpsType.Rever) {
                                                int Annee = downTrack.getDate_Vol().getYear();
                                                if (Annee > 2098 || Annee < 2011) {                                               
                                                    traceGPS newTrack = bug2019(downTrack,true);
                                                    if (newTrack.isDecodage()) downTrack = newTrack;
                                                }
                                            }
                                            dbAdd myDbAdd = new dbAdd(myConfig,i18n);
                                            int resAdd = myDbAdd.addVolCarnet(downTrack);
                                            if (resAdd == 0) nbFlightIn++;
                                        } else {
                                            sbError = new StringBuilder("GPS download problem for track : ");
                                            sbError.append(item.getDate()+" "+item.getHeure()+"\r\n");
                                            sbError.append("Unable to decode track before database insertion\r\n");
                                            mylogging.log(Level.SEVERE, sbError.toString());                                         
                                        }                            
                                    } else {
                                        sbError = new StringBuilder("GPS download problem for track : ");
                                        sbError.append(item.getDate()+" "+item.getHeure()+"\r\n");
                                        sbError.append("track is null\r\n");
                                        mylogging.log(Level.SEVERE, sbError.toString());    
                                    }
                                    updateMessage(String.valueOf(nbFlightRead)+" "+i18n.tr("flights inserted"));
                                } catch (Exception e) {
                                    sbError = new StringBuilder("GPS download problem for track : ");
                                    sbError.append(item.getDate()+" "+item.getHeure()+"\r\n");
                                    sbError.append(e.toString()).append("\r\n");
                                    mylogging.log(Level.SEVERE, sbError.toString());    
                                }                    
                            }
                        }                          
                        try {
                            switch (currGPS) {  
                                case Flytec20 :
                                    fls.closePort();
                                    break;
                                case Flytec15 :
                                    fliq.closePort();
                                    break;
                                case FlymPlus :
                                    fms.closePort();
                                    break;   
                                case Digifly :
                                    dig.closePort();
                                    break;                                           
                                case FlymOld :
                                    fmold.closePort();
                                    break;
                            }
                        } catch (Exception e) {

                        }                                            
                        nbInserted = nbFlightIn;
                        
                        return null ;                 
                    }                
                };
                
                worker.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
                    @Override
                    public void handle(WorkerStateEvent t) {
                        if (currGPS == winGPS.gpsType.Syride)  {
                            String resInsertion = nbInserted+" / "+nbToInsert+" "+i18n.tr("flights inserted in logbook");
                            dialogbox dConfirm = new dialogbox(i18n); 
                            // Display number of flights inserted and for moving tracks in archives folder
                            if (dConfirm.YesNo(resInsertion,i18n.tr("Start archiving")+" ?")) {
                                archiveSyride();
                            }
                        }
                        if(errInsertion != null && !errInsertion.isEmpty())  {
                            alertbox aInsFlights = new alertbox(myConfig.getLocale()); 
                            aInsFlights.alertInfo("  "+i18n.tr("tracks not inserted in the logbook")+"\r\n"+errInsertion);
                        }
                        rootController.changeCarnetView();
                    }
                });                

                ProgressDialog dlg = new ProgressDialog(worker);
                dlg.setHeaderText(i18n.tr("GPS import"));
                dlg.setTitle("");
                Thread th = new Thread(worker);
                th.setDaemon(true);
                th.start();                
            }
        } catch (Exception e) {
            sbError = new StringBuilder(this.getClass().getName()+"."+Thread.currentThread().getStackTrace()[1].getMethodName());
            sbError.append("\r\n").append(e.toString());
            mylogging.log(Level.SEVERE, sbError.toString());            
        } 
    }    
    
    private traceGPS bug2019(traceGPS originalTrack, boolean totalPoints) {
        traceGPS newTrack = null;
        
        DateTimeFormatter fHDTE = DateTimeFormatter.ofPattern("ddMMYY");
        DateTimeFormatter fGPX = DateTimeFormatter.ofPattern("YYYY-MM-dd");
        String strDateOriginal = "HFDTE"+originalTrack.getDate_Vol().format(fHDTE);
        String gpxDateOriginal = originalTrack.getDate_Vol().format(fGPX);
        String sOriginalYear = String.valueOf(originalTrack.getDate_Vol().getYear());
        if (sOriginalYear.length() == 4) {
            String sYear = sOriginalYear.substring(2, 4);
            int iYear = Integer.parseInt(sYear);
            int iMonth = originalTrack.getDate_Vol().getMonthValue();
            int iDay = originalTrack.getDate_Vol().getDayOfMonth();                
            int Annee;
            if (iYear > 98)
                Annee = Integer.parseInt("19"+sYear);
            else
                Annee = Integer.parseInt("20"+sYear);
            LocalDateTime bugLdt = LocalDateTime.of(Annee, iMonth, iDay,0,0,0);
            LocalDateTime wnro = LocalDateTime.of(1999, 8, 22, 00, 00, 00);
            LocalDateTime newWnro = LocalDateTime.of(2019, 4, 7, 00, 00, 00);
            long weeks = ChronoUnit.WEEKS.between(wnro, bugLdt);
            long days = ChronoUnit.DAYS.between(wnro,bugLdt);
            LocalDateTime goodDate = newWnro.plusDays(days);
            String strDateNew = "HFDTE"+goodDate.format(fHDTE);   
            String newTrackString = null;
            if (originalTrack.getFicGPX()!= null) {
                String gpxDateNew = goodDate.format(fGPX);
                newTrackString = originalTrack.getFicGPX().replace(gpxDateOriginal, gpxDateNew);
            } else {  
                if (originalTrack.getFicIGC() != null) {
                    newTrackString = originalTrack.getFicIGC().replace(strDateOriginal, strDateNew);
                }
            }
            if (newTrackString != null ) {
                if (totalPoints) {
                    newTrack = new traceGPS(newTrackString, "", true, myConfig);             
                } else {
                    configProg otherConfig = new configProg();
                    newTrack = new traceGPS(newTrackString,"", false, otherConfig);                
                }
            }
        } 
        
        return newTrack; 
    }    
    
    private LocalDateTime calcGoodDate(int iYear, int iMonth, int iDay)  {
        
        LocalDateTime bugLdt = LocalDateTime.of(iYear, iMonth, iDay,0,0,0);
        LocalDateTime wnro = LocalDateTime.of(1999, 8, 22, 00, 00, 00);
        LocalDateTime newWnro = LocalDateTime.of(2019, 4, 7, 00, 00, 00);
        long weeks = ChronoUnit.WEEKS.between(wnro, bugLdt);
        long days = ChronoUnit.DAYS.between(wnro,bugLdt);
        LocalDateTime goodDate = newWnro.plusDays(days);
        
        return goodDate;
    }
        
    private void archiveSyride() {
        
        ObservableList <Gpsmodel> checkedData = tableImp.getItems();

        for (Gpsmodel item : checkedData){
            if (item.getChecked())  {
                diskSyr.archiveTracks(item.getCol5());
            }
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
            aErrMap.alertError(i18n.tr("An error occurred during the map generation"));     // Error during map generation
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
     * read a single track in a different thread - called by askOneTrack
     * reqGPS is the dedicated download instruction 
     */
    private void oneFlightWithProgress(Gpsmodel selLineTable) {        
        strTrack = null;   
        errorComMsg = null;
           
        Task<Object> worker = new Task<Object>() {
            @Override
            protected Object call() throws Exception {
                String debugPath = "";
                try {
                    switch (currGPS) {
                        case Flytec20 :
                            flytec20 fls = new flytec20();
                            if (fls.isPresent(currNamePort)) { 
                                // Download instruction of the flight is stored in column 5
                                System.out.println("flytec20 sur "+currNamePort);
                                strTrack = fls.getIGC(selLineTable.getCol5());
                                fls.closePort(); 
                                resCom = 0;
                            } else {
                                errorComMsg = fls.getError();
                            }                            
                            break;
                        case Flytec15 :
                            flytec15 fliq = new flytec15();
                            if (fliq.isPresent(currNamePort)) { 
                                // Download instruction of the flight is stored in column 5
                                strTrack = fliq.getIGC(selLineTable.getCol5());
                                fliq.closePort(); 
                                resCom = 0;
                            } else {
                                errorComMsg = fliq.getError();
                            }
                            break; 
                        case Digifly:
                            String debugPathdigi = "";
                            if (fDebug != null && fDebug.exists()) debugPathdigi = fDebug.getAbsolutePath()+File.separator;
                            digifly digi = new digifly(myConfig.isDebugMode(), debugPathdigi);
                            if (digi.isPresent(currNamePort)) {
                                String sDate = selLineTable.getDate().replaceAll("\\.", "");
                                if (digi.getIGC(selLineTable.getCol5(), myConfig.getDefaultPilote(), myConfig.getDefaultVoile())) {
                                    strTrack = digi.getFinalIGC();
                                    digi.closePort(); 
                                    resCom = 0;
                                } else {
                                    errorComMsg = digi.getError();
                                }
                            } else {
                                errorComMsg = digi.getError();
                            }
                            break;     
                        case FlymPlus :    
                        case FlymSD :
                            debugPath = "";
                            if (fDebug != null && fDebug.exists()) debugPath = fDebug.getAbsolutePath()+File.separator;
                            flymaster fms = new flymaster(myConfig.isDebugMode(), debugPath);
                            if (fms.isPresent(currNamePort)) {
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
                            debugPath = "";
                            if (fDebug != null && fDebug.exists()) debugPath = fDebug.getAbsolutePath()+File.separator;
                            flymasterold fmold = new flymasterold(myConfig.isDebugMode(), debugPath);
                            if (fmold.isPresent(currNamePort)) {
                                if (fmold.getIGC(selLineTable.getCol5(), myConfig.getDefaultPilote(), myConfig.getDefaultVoile())) {
                                    strTrack = fmold.getFinalIGC();
                                    fmold.closePort(); 
                                    resCom = 0;
                                } else {
                                    errorComMsg = fmold.getError();
                                }
                            } else {
                                errorComMsg = fmold.getError();
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
                    case Flynet :
                        strTrack = usbFlynet.getTrackFile(selLineTable.getCol5());
                        if (strTrack != null && !strTrack.isEmpty()) {
                            resCom = 0;
                        } else {
                            resCom = 2;   // No GPS answer
                        }     
                        break;    
                    case Sensbox :
                        strTrack = usbSensbox.getTrackFile(selLineTable.getCol5());
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
                    case Syride :
                        strTrack = diskSyr.getTrackFile(selLineTable.getCol5());
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
                    case Element :
                        strTrack = usbElem.getTrackFile(selLineTable.getCol5());
                        if (strTrack != null && !strTrack.isEmpty()) {
                            resCom = 0;
                        } else {
                            resCom = 2;   // No GPS answer
                        }     
                        break;                           
                    case CPilot :
                        strTrack = usbCompass.getTrackFile(selLineTable.getCol5());
                        if (strTrack != null && !strTrack.isEmpty()) {
                            resCom = 0;
                        } else {
                            resCom = 2;   // No GPS answer
                        }     
                        break;
                    case XCTracer :
                        strTrack = usbXctracer.getTrackFile(selLineTable.getCol5());
                        if (strTrack != null && !strTrack.isEmpty()) {
                            resCom = 0;
                        } else {
                            resCom = 2;   // No GPS answer
                        }     
                        break;                             
                    case Vardui :
                        strTrack = usbVarduino.getTrackFile(selLineTable.getCol5());
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
        
        worker.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
            @Override
            public void handle(WorkerStateEvent t) {
                if (resCom == 0 && strTrack != null && !strTrack.isEmpty()) {                       
                    traceGPS reqIGC = new traceGPS(strTrack, "", true, myConfig);
                    if (reqIGC.isDecodage()) { 
                        showOneTrack(reqIGC);
                    } else {
                        displayErrDwnl(reqIGC);
                    }
                } else {
                    if (errorComMsg != null)  {
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
            }
        }); 
        
        ProgressDialog dlg = new ProgressDialog(worker);
        dlg.setHeaderText(i18n.tr("GPS import"));
        dlg.setTitle("");
        Thread th = new Thread(worker);
        th.setDaemon(true);
        th.start();               
      
    }
    
    private void oneFlightWithGpsDump(int idGPS, int idFlight) {
        // 6 is a flag for GpsDump -> calling method is oneFlightWithGpsDump
        gpsdump gpsd = new gpsdump(this, 6, currNamePort, myConfig,i18n);
        gpsd.start(idGPS, idFlight);        
    }
    
    public void returnGpsDump(String sIGC) {
        // If gpsdump call failed, error message was sent by gpsdump class
        if (sIGC != null && !sIGC.isEmpty()) {                       
            traceGPS reqIGC = new traceGPS(sIGC, "", true, myConfig);
            if (reqIGC.isDecodage()) { 
                showOneTrack(reqIGC);
            } else {
                displayErrDwnl(reqIGC);
            }        
        } else {
            alertbox aInfo = new alertbox(myConfig.getLocale());
            aInfo.alertInfo(i18n.tr("GPSDump did not return the requested track"));
        }
    }
                        
    /**
     * Clic on button "Track display" (FXML triggered)
     */
     public void askOneTrack() {
        if(tableImp.getSelectionModel().getSelectedItem() != null)  {
            int idx;
            Gpsmodel currLineSelection = tableImp.getSelectionModel().getSelectedItem();
            switch (currGPS) {                    
                    case Flytec20 :                
                        idx = tableImp.getSelectionModel().getSelectedIndex();
                        // 3 is id of Flytec
                        oneFlightWithGpsDump(3,idx); 
                        break;
                    case Flytec15 :                        
                        idx = tableImp.getSelectionModel().getSelectedIndex();
                        // 8 is id of Flytec 6015
                        oneFlightWithGpsDump(8,idx);                             
                        break;
                    case FlymSD :
                        switch (myConfig.getOS()) {
                            case WINDOWS :
                                idx = tableImp.getSelectionModel().getSelectedIndex();
                                // 1 is id of Flymaster SD for GPSDump
                                oneFlightWithGpsDump(1,idx);                                
                                break;
                            case LINUX :
                                idx = tableImp.getSelectionModel().getSelectedIndex();
                                // 2 is id of Flymaster Old
                                oneFlightWithGpsDump(1,idx);                                                        
                                break;
                            case MACOS :                               
                                idx = tableImp.getSelectionModel().getSelectedIndex();
                                // 2 is id of Flymaster Old
                                oneFlightWithGpsDump(1,idx);                                 
                                break;                                 
                        }                                               
                        break;
                    case FlymPlus :
                        oneFlightWithProgress(currLineSelection);  
                        break;
                    case FlymOld :
                        switch (myConfig.getOS()) {
                            case WINDOWS :
                                // Strangely, for Flymater Old, the first record has the index 0 so -> idx - 1
                                idx = tableImp.getSelectionModel().getSelectedIndex() - 1;
                                // 2 is id of Flymaster Old
                                oneFlightWithGpsDump(2,idx);                                                        
                                break;                               
                            case LINUX :
                                idx = tableImp.getSelectionModel().getSelectedIndex();
                                // 2 is id of Flymaster Old
                                oneFlightWithGpsDump(2,idx);                                                        
                                break;
                            case MACOS :                               
                                idx = tableImp.getSelectionModel().getSelectedIndex();
                                // 2 is id of Flymaster Old
                                oneFlightWithGpsDump(2,idx);                                 
                                break;
                        }                                                       
                        break;
                    case Digifly:
                        oneFlightWithProgress(currLineSelection);  
                        break;
                    case Rever :
                    case Sky :
                    case Sky3 :      
                    case Flynet :   
                    case Sensbox :                       
                    case Oudie :    
                    case Syride :                        
                    case Connect :  
                    case Element :                           
                    case CPilot : 
                    case XCTracer :
                    case Vardui :
                        oneFlightWithProgress(currLineSelection); 
                        break; 
            }
        } else {
            alertbox aInfo = new alertbox(myConfig.getLocale());
            aInfo.alertInfo(i18n.tr("Left-click to select a flight"));
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
        if (myConfig.isDebugMode()) {
            mDebug = true;
            fDebug = new File(myConfig.getPathW()+File.separator+"Debug");
            if (!fDebug.exists()) fDebug.mkdirs();            
        } else
            mDebug = false;
        i18n = myConfig.getI18n();
        // clear status bar
        rootController.updateMsgBar("", false, 60);
        winTraduction();
                
    }
    
    /**
     * New GPS -> window is cleaned
     */
    private void winReset() {
        buttonBar.setVisible(false);
        hbTable.setVisible(false);                  
        tableImp.getItems().clear();        
    }
    
    /**
    * Translate labels of the window
    */
    private void winTraduction() {
        btnSelectGPS.setText(i18n.tr("Select GPS"));
        btnDecocher.setText(i18n.tr("Unselect"));
        btnMaj.setText(i18n.tr("Logbook update"));
        Tooltip majToolTip = new Tooltip();
        majToolTip.setStyle(myConfig.getDecoToolTip());
        majToolTip.setText(i18n.tr("All checked flights will be included in the logbook"));
        btnVisu.setTooltip(majToolTip);
        btnVisu.setText(i18n.tr("Track visualization")); 
        Tooltip viToolTip = new Tooltip();
        viToolTip.setStyle(myConfig.getDecoToolTip());
        viToolTip.setText(i18n.tr("The selected flight (left-click) is viewed without incorporation into the logbook"));
        btnVisu.setTooltip(viToolTip);        
        dateCol.setText(i18n.tr("Date"));
        heureCol.setText(i18n.tr("Time"));
    }
    
}
