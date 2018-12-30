/*
 * Copyright Gil THOMAS
 * This file forms an integral part of Logfly project
 * See the LICENSE file distributed with source code
 * for details of Logfly licence project
 */
package littlewins;

import com.serialpundit.core.SerialComPlatform;
import com.serialpundit.core.SerialComSystemProperty;
import com.serialpundit.serial.SerialComManager;
import gps.compass;
import gps.connect;
import gps.element;
import gps.flymaster;
import gps.flymasterold;
import gps.flynet;
import gps.flytec15;
import gps.flytec20;
import gps.oudie;
import gps.reversale;
import gps.sensbox;
import gps.skytraax;
import gps.skytraxx3;
import gps.syride;
import gps.xctracer;
import java.io.IOException;
import java.util.logging.Level;
import java.util.regex.Pattern;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.xnap.commons.i18n.I18n;
import settings.configProg;
import settings.listGPS;
import systemio.mylogging;

/**
 *
 * @author gil
 * inichbGPS : 
 *          Fill the choicebox with supported GPS
 *          Default GPS defined in settings is selected (idxGPS = myConfig.getIdxGPS()) 
 *          First is 1. Index 0 is reserved for -> Select a GPS
 *          when choicebox index change, choixGPS is launched 
 * choixGPS : set the current GPS and run testGPS
 * testGPS  : method for each supported GPS
 *             - Flymaster and Flymaster Old -> serial port choiceboix become visible [listSerialPort()]
 *             - Flytec 6020/6030 and 6015 -> serial port choiceboix become visible [listSerialPort()]
 *             - Reversale and other USB GPS detetc flights and waypoint folders
 * listSerialPort : fill the choicebox with available ports
 *      
 */
public class winGPS {
    
    private Stage subStage;
    private HBox hBox2;
    private HBox hBox3;
    private ChoiceBox<listGPS.idGPS> chbGPS;  
    private int idxChbGPS;
    private Button btConnexion;    
    private Button btRefresh;
    private Label lbPort;
    private ChoiceBox cbSerial;  
    
    private I18n i18n; 
    private configProg myConfig;   
    
    public enum gpsType {Flytec20,Flytec15,Flynet,FlymOld,Rever,Sky,Oudie,Element,Sensbox,Syride,FlymSD,Connect,Sky3,CPilot,XCTracer,FlymPlus}    
    private ObservableList <listGPS.idGPS> allGPS;
    // current GPS
    private gpsType currGPS;
    private String currNamePort = null;
    private boolean gpsConnect;
    private int currTypeName = -1;
    
    private reversale usbRever;
    private skytraax usbSky;
    private skytraxx3 usbSky3;
    private oudie usbOudie;
    private flynet usbFlynet;
    private sensbox usbSensbox;
    private syride diskSyr;
    private connect usbConnect;
    private compass usbCompass;
    private element usbElem;
    private xctracer usbXctracer;    
    private boolean wpCall;
    private String usbWaypPath;
    
    private StringBuilder sbError;    
    
    /**
     * 
     * @param pConfig   Send Logfly settings
     * @param pI18n     Send current language
     * @param wpCall    True if calling by WaypViewController
     */
    public winGPS(configProg pConfig, I18n pI18n, boolean pWpCall)  {
        myConfig = pConfig;        
        this.i18n = pI18n;
        this.wpCall = pWpCall;
        gpsConnect = false;
        showWin();        
    }   

    public gpsType getCurrGPS() {
        return currGPS;
    }

    public String getCurrNamePort() {
        return currNamePort;
    }        

    public int getCurrTypeName() {
        return currTypeName;
    }      

    public boolean isGpsConnect() {
        return gpsConnect;
    }
        
                
    private void showWin() {
        subStage = new Stage();   
        subStage.setTitle(i18n.tr("GPS Choice"));
        subStage.initModality(Modality.APPLICATION_MODAL);
        Label lbGPS = new Label("GPS ");
        lbGPS.setMinWidth(50);
        
        chbGPS = new ChoiceBox(); 
        
        chbGPS.setMinWidth(120);
        
        HBox hBox1 = new HBox();
        
        hBox1.setSpacing(10);
        hBox1.setMaxHeight(25);
        hBox1.setMinWidth(180);
        hBox1.setAlignment(Pos.CENTER_LEFT);
        hBox1.getChildren().addAll(lbGPS, chbGPS);
        
        lbPort = new Label(i18n.tr("Port"));
        lbPort.setMinWidth(50);
        lbPort.setVisible(false);
        
        cbSerial = new ChoiceBox();    
                
        cbSerial.setMinWidth(200);
                
        hBox2 = new HBox();
        hBox2.getChildren().addAll(lbPort, cbSerial);
        hBox2.setSpacing(10);
        hBox2.setMaxHeight(25);
        hBox2.setMinWidth(260);
        hBox2.setAlignment(Pos.CENTER_LEFT);
        
        Label lbName = new Label(i18n.tr("Format"));
        lbName.setMinWidth(50);
        
        ChoiceBox cbName = new ChoiceBox();

        cbName.getItems().add(i18n.tr("Long names"));
        cbName.getItems().add(i18n.tr("Short names"));
        cbName.getItems().add(i18n.tr("Mixed"));
        cbName.getSelectionModel().selectedIndexProperty()
        .addListener(new ChangeListener<Number>() {
          public void changed(ObservableValue ov, Number value, Number new_value) {
              currTypeName = new_value.intValue();
          }
        }); 
        cbName.getSelectionModel().select(0);
        
        cbName.setMinWidth(150);
        
        // Positionne au premier
        cbName.getSelectionModel().select(0);  
                
        hBox3 = new HBox();
        hBox3.getChildren().addAll(lbName, cbName);
        hBox3.setSpacing(10);
        hBox3.setMaxHeight(25);
        hBox3.setMinWidth(260);
        hBox3.setAlignment(Pos.CENTER_LEFT);        
        
        
        final VBox vbox = new VBox();
        vbox.setPadding(new Insets(10));
        vbox.setSpacing(5);                
        
        // Pour contenir les boutons
        HBox buttonBar = new HBox();
        buttonBar.setPadding(new Insets(6));
        buttonBar.setSpacing(5);
        buttonBar.setAlignment(Pos.CENTER_RIGHT);
        btConnexion = new Button("Connexion");
        btConnexion.setOnAction((event) -> {
            testGPS();
        });
        Tooltip connToolTip = new Tooltip();
        connToolTip.setStyle(myConfig.getDecoToolTip());
        connToolTip.setText(i18n.tr("Test GPS connection"));
        btConnexion.setTooltip(connToolTip);
        
        Button btCancel = new Button(i18n.tr("Close"));
        btCancel.setOnAction((event) -> {
            currGPS = null;
            currNamePort = null;
            gpsConnect = false;
            subStage.close();
        });
        btRefresh = new Button(i18n.tr("Update"));
        btRefresh.setOnAction((event) -> {
            choixGPS(allGPS.get(chbGPS.getSelectionModel().getSelectedIndex()).getIdModel());
        });        
        btRefresh.setVisible(false);
        Tooltip refToolTip = new Tooltip();
        refToolTip.setStyle(myConfig.getDecoToolTip());
        refToolTip.setText(i18n.tr("Refreshes the list of ports or disks"));
        btRefresh.setTooltip(refToolTip);
        
        buttonBar.getChildren().addAll(btRefresh, btConnexion, btCancel);
        
        vbox.getChildren().addAll(hBox1, hBox2, hBox3, buttonBar);
        
        // visibilité
        hBox3.setVisible(false);
        btConnexion.setVisible(false);    
        
        StackPane subRoot = new StackPane();
        subRoot.getChildren().add(vbox);
        subStage.setScene(new Scene(subRoot, 280, 150));
        iniChbGPS();
        if (!gpsConnect) subStage.showAndWait();         
    }    
    
    private void iniChbGPS()  {          
        listGPS suppGPS = new listGPS(myConfig.getLocale());
        allGPS = suppGPS.newFill();  
        chbGPS.getItems().clear();
        chbGPS.setItems(allGPS);
        int idxGPS = 0;        
        int i = 0;
        for (listGPS.idGPS setModel : allGPS){            
          if (setModel.getIdModel() == myConfig.getIdxGPS()){
            idxGPS = i;            
            break;
          }
          i++;
        }           
        chbGPS.getSelectionModel().select(idxGPS);    
        chbGPS.getSelectionModel().selectedIndexProperty()
        .addListener(new ChangeListener<Number>() {
          public void changed(ObservableValue ov, Number value, Number new_value) {
              // some troubles with btRefresh.setOnAction chbGPS.getSelectionModel().getSelectedIndex()) returns bad values
              idxChbGPS = new_value.intValue();
              choixGPS(allGPS.get(new_value.intValue()).getIdModel());              
          }
        });        
        choixGPS(myConfig.getIdxGPS());
    }    
    
    /**
     * triggered by GPS choicebox, if necessary serial choice box becomes visible
     * @param idxGPS 
     */    
    private void choixGPS(int idxGPS) {
        lbPort.setVisible(false);
        cbSerial.setVisible(false);
                
        switch (idxGPS) {
            case 0:
                // Select a GPS
                // No GPS defined in settings
                currGPS = null;
                currNamePort = null;
                break;
            case 1:
                // 6020/6030
                currGPS = gpsType.Flytec20;
                if (wpCall) hBox3.setVisible(true);
                listSerialPort();
                break;
            case 2:
                // 6015
                currGPS = gpsType.Flytec15;
                if (wpCall) hBox3.setVisible(true);
                listSerialPort();                
                break;
            case 3:
                currGPS = gpsType.Flynet; 
                hBox3.setVisible(false);
                currNamePort = "nil";
                testGPS();
                break;
            case 4:    
                // Flymaster old series
                currGPS = gpsType.FlymOld;
                if (wpCall) hBox3.setVisible(true);
                listSerialPort();                
                break;
            case 5:
                // Reversale
                currGPS = gpsType.Rever;   
                hBox3.setVisible(false);
                currNamePort = "nil";
                testGPS();
                break;
            case 6:
                currGPS = gpsType.Sky;
                hBox3.setVisible(false);
                currNamePort = "nil";
                testGPS();   
                break;
            case 7:
                currGPS = gpsType.Oudie;
                hBox3.setVisible(false);
                currNamePort = "nil";
                testGPS();   
                break;                
            case 8:
                currGPS = gpsType.Element;
                hBox3.setVisible(false);
                currNamePort = "nil";
                testGPS();   
                break;
            case 9:
                currGPS = gpsType.Sensbox;
                hBox3.setVisible(false);
                currNamePort = "nil";
                testGPS();   
                break;
            case 10:
                currGPS = gpsType.Syride;
                hBox3.setVisible(false);
                currNamePort = "nil";
                testGPS(); 
                break;
            case 11:
                // Flymaster SD will be read with GPSDump
                currGPS = currGPS = gpsType.FlymSD;  
                if (wpCall) hBox3.setVisible(true);
                listSerialPort();
                break;
            case 12:  // Connect
                currGPS = gpsType.Connect;  
                hBox3.setVisible(false);
                currNamePort = "nil";
                testGPS();   
                break;
            case 13:
                currGPS = gpsType.Sky3;
                hBox3.setVisible(false);
                currNamePort = "nil";
                testGPS();   
                break;
            case 14:
                currGPS = gpsType.CPilot;
                hBox3.setVisible(false);
                currNamePort = "nil";
                testGPS();   
                break;
            case 15:
                currGPS = gpsType.XCTracer;
                hBox3.setVisible(false);
                currNamePort = "nil";
                testGPS();   
                break;   
            case 16:
                // Flymaster will be read with Flymaster.java
                currGPS = gpsType.FlymPlus;  
                if (wpCall) hBox3.setVisible(true);
                listSerialPort();
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
                            if (lastSerialUsed.equals(port)) {
                                idxSerialList = idxListPort;
                            } 
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
                    cbSerial.getItems().clear();
                    cbSerial.setItems(portList);  
                    cbSerial.setVisible(true);
                    cbSerial.getSelectionModel().select(idxSerialList); 
                    lbPort.setVisible(true);                           
                    cbSerial.getSelectionModel().selectedItemProperty().addListener((ObservableValue observable, Object oldValue, Object newValue) -> {
                        currNamePort = (String) newValue;
                    });                                        
                    currNamePort = cbSerial.getSelectionModel().getSelectedItem().toString();
                    
                    testGPS();
                } else {
                    currNamePort = "nil";
                    // Rafriachr les listes
                    // pas sûr que ce soit pertinent...
                    // on devrait afficher un msg erreur et demander relance totale
                 //   resCom = 3;
                 //   actuLed();   
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
    
    private void gpsNotPresent() {
        gpsConnect = false;
        btRefresh.setVisible(true);
        btConnexion.setVisible(true);  
        subStage.setTitle(i18n.tr("GPS not detected"));        
    }
    
    private void gpsPresent() {
        gpsConnect= true;
        subStage.close();        
    }
    
    private void testGPS() {
        if (currGPS != null) {
            switch (currGPS) {
                case Flytec20 :        
                    if (currNamePort != null && !currNamePort.equals("")) {
                        try {
                            flytec20 fls = new flytec20();
                            if (fls.isPresent(currNamePort)) {    
                                gpsPresent();
                            } else {
                                gpsNotPresent();
                            } 
                            fls.closePort();
                        } catch (Exception e) {
                            sbError = new StringBuilder(this.getClass().getName()+"."+Thread.currentThread().getStackTrace()[1].getMethodName());
                            sbError.append("\r\n").append(e.toString());
                            mylogging.log(Level.SEVERE, sbError.toString());            
                        }   
                    }                    
                    break;
                case Flytec15 :
                    if (currNamePort != null && !currNamePort.equals("")) {
                        try {
                            flytec15 fl15 = new flytec15();
                            if (fl15.isPresent(currNamePort)) {    
                                gpsPresent();
                            } else {
                                gpsNotPresent();
                            } 
                            fl15.closePort();
                        } catch (Exception e) {
                            sbError = new StringBuilder(this.getClass().getName()+"."+Thread.currentThread().getStackTrace()[1].getMethodName());
                            sbError.append("\r\n").append(e.toString());
                            mylogging.log(Level.SEVERE, sbError.toString());            
                        }   
                    }
                    break;
                case FlymSD :
                case FlymPlus :
                    if (currNamePort != null && !currNamePort.equals("")) {
                        try {
                            flymaster fms = new flymaster();
                            if (fms.isPresent(currNamePort)) {    
                                gpsPresent();
                            } else {
                                gpsNotPresent();
                            } 
                            fms.closePort();
                        } catch (Exception e) {
                            sbError = new StringBuilder(this.getClass().getName()+"."+Thread.currentThread().getStackTrace()[1].getMethodName());
                            sbError.append("\r\n").append(e.toString());
                            mylogging.log(Level.SEVERE, sbError.toString());            
                        }   
                    }                    
                    break;
                case FlymOld :
                    if (currNamePort != null && !currNamePort.equals("")) {
                        try {
                            flymasterold fmsold = new flymasterold();
                            if (fmsold.isPresent(currNamePort)) {    
                                gpsPresent();
                            } else {
                                gpsNotPresent();
                            } 
                            fmsold.closePort();
                        } catch (Exception e) {
                            sbError = new StringBuilder(this.getClass().getName()+"."+Thread.currentThread().getStackTrace()[1].getMethodName());
                            sbError.append("\r\n").append(e.toString());
                            mylogging.log(Level.SEVERE, sbError.toString());            
                        }   
                    }                                                            
                    break;
                case Rever :  
                    usbRever = new reversale(myConfig.getOS(), myConfig.getGpsLimit());
                    displayDrives(usbRever.getDriveList(),usbRever.getIdxDrive());          
                    if (usbRever.isConnected()) {
                        gpsPresent();
                    } else {
                        gpsNotPresent();
                    }                    
                    break;
                case Sky :
                    usbSky = new skytraax(myConfig.getOS(), myConfig.getGpsLimit());
                    displayDrives(usbSky.getDriveList(),usbSky.getIdxDrive());  
                    if (usbSky.isConnected()) {
                        gpsPresent();
                    } else {
                        gpsNotPresent();
                    }                                                   
                    break;
                case Sky3 :          
                    usbSky3 = new skytraxx3(myConfig.getOS(), myConfig.getGpsLimit());
                    displayDrives(usbSky3.getDriveList(),usbSky3.getIdxDrive());  
                    if (usbSky3.isConnected()) {      
                        gpsPresent();
                    } else {
                        gpsNotPresent();
                    }                                                             
                    break;       
                case Flynet :  
                    usbFlynet = new flynet(myConfig.getOS(), myConfig.getGpsLimit());
                    displayDrives(usbFlynet.getDriveList(),usbFlynet.getIdxDrive());                      
                    if (usbFlynet.isConnected()) {    
                        gpsPresent();
                    } else {
                        gpsNotPresent();                        
                    }                
                    break;  
                case Sensbox :        
                    usbSensbox = new sensbox(myConfig.getOS(), myConfig.getGpsLimit());
                    displayDrives(usbSensbox.getDriveList(),usbSensbox.getIdxDrive());                     
                    if (usbSensbox.isConnected()) {  
                        gpsPresent();
                    } else {
                        gpsNotPresent();                        
                    }                  
                    break;                         
                case Oudie :   
                    usbOudie = new oudie(myConfig.getOS(), myConfig.getGpsLimit());
                    displayDrives(usbOudie.getDriveList(),usbOudie.getIdxDrive());  
                    if (usbOudie.isConnected()) {
                        gpsPresent();
                    } else {
                        gpsNotPresent();
                    }                                
                    break;  
                case Syride :                        
                    diskSyr = new syride(myConfig.getOS(), myConfig.getGpsLimit());                    
                    if (diskSyr.isConnected()) {
                        gpsPresent();
                    } else {
                        gpsNotPresent();                        
                    }
                    break;                            
                case Connect :
                    usbConnect = new connect(myConfig.getOS(), myConfig.getGpsLimit());
                    displayDrives(usbConnect.getDriveList(),usbConnect.getIdxDrive());  
                    if (usbConnect.isConnected()) { 
                        gpsPresent();
                    } else {
                        gpsNotPresent();
                    }                    
                    break;   
                case Element :     
                    usbElem = new element(myConfig.getOS(), myConfig.getGpsLimit());
                    displayDrives(usbElem.getDriveList(),usbElem.getIdxDrive()); 
                    if (usbElem.isConnected()) {   
                        gpsPresent();
                    } else {
                        gpsNotPresent();                        
                    }                 
                    break;                         
                case CPilot :     
                    usbCompass = new compass(myConfig.getOS(), myConfig.getGpsLimit());
                    displayDrives(usbCompass.getDriveList(),usbCompass.getIdxDrive());                      
                    if (usbCompass.isConnected()) { 
                       gpsPresent();
                    } else {
                        gpsNotPresent();                        
                    }                   
                    break;  
                case XCTracer :
                    usbXctracer = new xctracer(myConfig.getOS(), myConfig.getGpsLimit());
                    displayDrives(usbXctracer.getDriveList(),usbXctracer.getIdxDrive()); 
                    if (usbXctracer.isConnected()) { 
                       gpsPresent();
                    } else {
                        gpsNotPresent();                           
                    }                   
                    break;                        
            } 
        }
    }
    
    private void displayDrives(ObservableList <String> driveList, int idxList) {
        if (driveList.size() > 0) {
            // Au début je faisais 
            //   chbSerial.getItems().clear();
            // J'ai mis lontemps à compredre qu'avec une Observable list
            // il était inutile de vider la choicebox
            // elle EST la liste. Il y a un binding définitif
            cbSerial.setItems(driveList);  
            cbSerial.setVisible(true);     
            cbSerial.getSelectionModel().select(idxList);       
            cbSerial.setVisible(true);             
        }                   
    }
    
    private void goodListDrives(ObservableList <String> driveList, int idxList) {
        if (driveList.size() > 0) {
            // Au début je faisais 
            //   chbSerial.getItems().clear();
            // J'ai mis lontemps à compredre qu'avec une Observable list
            // il était inutile de vider la choicebox
            // elle EST la liste. Il y a un binding définitif
            cbSerial.setItems(driveList);  
            cbSerial.setVisible(true);     
            cbSerial.getSelectionModel().select(idxList);       
            cbSerial.setVisible(true); 
            btConnexion.setVisible(true);              
            // resCom = 5;
            // actuLed(); 
            // flightListWithProgress();   
           
        }                    
    }   
    
    
    private void badListDrives(ObservableList <String> driveList, int idxList) {
        if (driveList.size() > 0) {
            // Au début je faisais 
            //   chbSerial.getItems().clear();
            // J'ai mis lontemps à compredre qu'avec une Observable list
            // il était inutile de vider la choicebox
            // elle EST la liste. Il y a un binding définitif
            cbSerial.setItems(driveList);  
            cbSerial.setVisible(true);     
            cbSerial.getSelectionModel().select(idxList);       
            cbSerial.setVisible(true);     
        }
//        resCom = 4;
//        actuLed();  
//        // Window must be intilaized/refreshed
//        buttonBar.setVisible(false);
//        hbTable.setVisible(false);          
    }    
    
}
