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
import dialogues.alertbox;
import gps.compass;
import gps.connect;
import gps.element;
import gps.flynet;
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
 */
public class winGPS {
    
    HBox hBox2;
    HBox hBox3;
    ChoiceBox<listGPS.idGPS> chbGPS;  
    Button btGo;    
    Label lbPort;
    ChoiceBox cbSerial;  
    
    private I18n i18n; 
    private configProg myConfig;   
    
    public enum gpsType {Flytec20,Flytec15,Flynet,FlymOld,Rever,Sky,Oudie,Element,Sensbox,Syride,FlymSD,Connect,Sky3,CPilot,XCTracer}    
    // current GPS
    private gpsType currGPS;
    private String currNamePort = null; 
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
                
    private void showWin() {
        Stage subStage = new Stage();   
        subStage.initModality(Modality.APPLICATION_MODAL);
        
        Label lbGPS = new Label("GPS ");
        lbGPS.setMinWidth(50);
        
        chbGPS = new ChoiceBox(); 
        
                    
        
//        chbGPS.getSelectionModel().selectedIndexProperty()
//        .addListener(new ChangeListener<Number>() {
//          public void changed(ObservableValue ov, Number value, Number new_value) {
//              switch (new_value.intValue()) {
//                  case 1:
//                    hBox2.setVisible(true);
//                    btGo.setVisible(true);       
//                    if (wpCall) hBox3.setVisible(true);
//                    break;
//                  case 2:
//                    hBox2.setVisible(true);
//                    btGo.setVisible(true);                      
//                    if (wpCall) hBox3.setVisible(true);
//                    break;                    
//                  case 3:
//                    hBox2.setVisible(true);
//                    btGo.setVisible(true);                      
//                    if (wpCall) hBox3.setVisible(true);
//                    break;
//                  case 4:
//                    hBox2.setVisible(false);
//                    btGo.setVisible(true);                      
//                    if (wpCall) hBox3.setVisible(true);
//                    break;                    
//              }
//          }
//        }); 
        
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

        cbName.getItems().add(i18n.tr("Noms longs"));
        cbName.getItems().add(i18n.tr("Noms courts"));
        cbName.getItems().add(i18n.tr("Mixte"));
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
        btGo = new Button("Go...");
        btGo.setOnAction((event) -> {
            System.out.println("curtypename in Go : "+currTypeName);
            subStage.close();
        });
        Button btCancel = new Button(i18n.tr("Annuler"));
        btCancel.setOnAction((event) -> {
            currGPS = null;
            currNamePort = null;
            subStage.close();
        });
        buttonBar.getChildren().addAll(btCancel, btGo);
        
        vbox.getChildren().addAll(hBox1, hBox2, hBox3, buttonBar);
        
        // visibilité
        hBox3.setVisible(false);
        btGo.setVisible(false);    
        
        StackPane subRoot = new StackPane();
        subRoot.getChildren().add(vbox);
        subStage.setScene(new Scene(subRoot, 280, 150));
        iniChbGPS();
        subStage.showAndWait(); 
        
    }    
    
    private void iniChbGPS()  {          
        listGPS suppGPS = new listGPS(myConfig.getLocale());
        ObservableList <listGPS.idGPS> allGPS = suppGPS.newFill();  
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
              choixGPS(allGPS.get(new_value.intValue()).getIdModel());
          }
        });        
        choixGPS(myConfig.getIdxGPS());
    }    
    
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
                usbFlynet = new flynet(myConfig.getOS(), myConfig.getGpsLimit());
                if (usbFlynet.isConnected()) {
                    goodListDrives(usbFlynet.getDriveList(),usbFlynet.getIdxDrive());
                } else {
                    badListDrives(usbFlynet.getDriveList(), usbFlynet.getIdxDrive());
                }                
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
                usbRever = new reversale(myConfig.getOS(), myConfig.getGpsLimit());
                if (usbRever.isConnected()) {
                    goodListDrives(usbRever.getDriveList(),usbRever.getIdxDrive());
                } else {
                    badListDrives(usbRever.getDriveList(), usbRever.getIdxDrive());
                }
                break;
            case 6:
                currGPS = gpsType.Sky;
                hBox3.setVisible(false);
                currNamePort = "nil";
                usbSky = new skytraax(myConfig.getOS(), myConfig.getGpsLimit());
                if (usbSky.isConnected()) {
                    goodListDrives(usbSky.getDriveList(),usbSky.getIdxDrive());
                } else {
                    badListDrives(usbSky.getDriveList(), usbSky.getIdxDrive());
                }
                break;
            case 7:
                currGPS = gpsType.Oudie;
                hBox3.setVisible(false);
                currNamePort = "nil";
                usbOudie = new oudie(myConfig.getOS(), myConfig.getGpsLimit());
                if (usbOudie.isConnected()) {
                    goodListDrives(usbOudie.getDriveList(),usbOudie.getIdxDrive());
                } else {
                    badListDrives(usbOudie.getDriveList(), usbOudie.getIdxDrive());
                }
                break;                
            case 8:
                currGPS = gpsType.Element;
                hBox3.setVisible(false);
                currNamePort = "nil";
                usbElem = new element(myConfig.getOS(), myConfig.getGpsLimit());
                if (usbElem.isConnected()) {
                    goodListDrives(usbElem.getDriveList(),usbElem.getIdxDrive());
                } else {
                    badListDrives(usbElem.getDriveList(), usbElem.getIdxDrive());
                }
                break;
            case 9:
                currGPS = gpsType.Sensbox;
                hBox3.setVisible(false);
                currNamePort = "nil";
                usbSensbox = new sensbox(myConfig.getOS(), myConfig.getGpsLimit());
                if (usbSensbox.isConnected()) {
                    goodListDrives(usbSensbox.getDriveList(),usbSensbox.getIdxDrive());
                } else {
                    badListDrives(usbSensbox.getDriveList(), usbSensbox.getIdxDrive());
                }
                break;
            case 10:
                currGPS = gpsType.Syride;
                hBox3.setVisible(false);
                currNamePort = "nil";
                diskSyr = new syride(myConfig.getOS(), myConfig.getGpsLimit());
                if (diskSyr.isConnected()) {
                //    resCom = 5;
               //     actuLed();   
               //     flightListWithProgress(); 
                } else {
                    alertbox noSyride = new alertbox(myConfig.getLocale()); 
                    noSyride.alertError(i18n.tr("Sys-PC-Tool n'est pas installé"));
                }
                break;
            case 11:
                // Flymaster SD 
                currGPS = currGPS = gpsType.FlymSD;  
                if (wpCall) hBox3.setVisible(true);
                listSerialPort();
                break;
            case 12:
                currGPS = gpsType.Connect;
                hBox3.setVisible(false);
                currNamePort = "nil";
                usbConnect = new connect(myConfig.getOS(), myConfig.getGpsLimit());
                if (usbConnect.isConnected()) {
                    goodListDrives(usbConnect.getDriveList(),usbConnect.getIdxDrive());
                } else {
                    badListDrives(usbConnect.getDriveList(), usbConnect.getIdxDrive());
                }
                break;
            case 13:
                currGPS = gpsType.Sky3;
                hBox3.setVisible(false);
                currNamePort = "nil";
                usbSky3 = new skytraxx3(myConfig.getOS(), myConfig.getGpsLimit());
                if (usbSky3.isConnected()) {
                    goodListDrives(usbSky3.getDriveList(),usbSky3.getIdxDrive());
                } else {
                    badListDrives(usbSky3.getDriveList(), usbSky3.getIdxDrive());
                }
                break;
            case 14:
                currGPS = gpsType.CPilot;
                hBox3.setVisible(false);
                currNamePort = "nil";
                usbCompass = new compass(myConfig.getOS(), myConfig.getGpsLimit());
                if (usbCompass.isConnected()) {
                    goodListDrives(usbCompass.getDriveList(),usbCompass.getIdxDrive());
                } else {
                    badListDrives(usbCompass.getDriveList(), usbCompass.getIdxDrive());
                }
                break;
            case 15:
                currGPS = gpsType.XCTracer;
                hBox3.setVisible(false);
                currNamePort = "nil";
                usbXctracer = new xctracer(myConfig.getOS(), myConfig.getGpsLimit());
                if (usbXctracer.isConnected()) {
                    goodListDrives(usbXctracer.getDriveList(),usbXctracer.getIdxDrive());
                } else {
                    badListDrives(usbXctracer.getDriveList(), usbXctracer.getIdxDrive());
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
                    btGo.setVisible(true);  
                 //   resCom = 0;
                 //   actuLed();
                } else {
                    currNamePort = "nil";
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
            btGo.setVisible(true);              
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
