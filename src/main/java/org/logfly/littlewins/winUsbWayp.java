/*
 * Copyright Gil THOMAS
 * This file forms an integral part of Logfly project
 * See the LICENSE file distributed with source code
 * for details of Logfly licence project
 */
package org.logfly.littlewins;

import java.util.ArrayList;
import java.util.logging.Level;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import org.logfly.gps.compass;
import org.logfly.gps.connect;
import org.logfly.gps.element;
import org.logfly.gps.oudie;
import org.logfly.gps.reversale;
import org.logfly.gps.skytraax;
import org.logfly.gps.skytraxx3;
import org.logfly.settings.configProg;
import org.logfly.systemio.mylogging;
import org.xnap.commons.i18n.I18n;

/**
 *
 * @author gil
 */
public class winUsbWayp {
    
    private Stage subStage;
    
    private TableView tableWayp = new TableView();    
    private TableColumn<String, String> colName;     
    
    private I18n i18n; 
    private configProg myConfig;
    private String wTitle;
    // current GPS
    private winGPS.gpsType currGPS;    
    private StringBuilder sbError;
    private ArrayList<String> waypPathList;    
    private ObservableList<String> gpsWayp = FXCollections.observableArrayList();    
    private String selWaypPath = null;

    public String getSelWaypPath() {
        return selWaypPath;
    }        
    
    public winUsbWayp(configProg pConfig, I18n pI18n, winGPS.gpsType pGPS, String pTitle)  {
        myConfig = pConfig;        
        this.i18n = pI18n;
        currGPS = pGPS;
        this.wTitle = pTitle;
        fillData();
        showWin();
    }    
    
    private void showWin() {
        subStage = new Stage();
        subStage.initModality(Modality.APPLICATION_MODAL);
        tableWayp.prefHeightProperty().bind(subStage.heightProperty());
        tableWayp.prefWidthProperty().bind(subStage.widthProperty());  
        
        colName = new TableColumn("Fichier");
        tableWayp.getColumns().add(colName);
       // solutin for a tableview with single string column https://stackoverflow.com/questions/34570505/basic-javafx-tableview-of-single-string-column         
        colName.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(cellData.getValue()));
        
        colName.prefWidthProperty().bind(tableWayp.widthProperty().multiply(1));
        
        StackPane root = new StackPane();
        VBox vBox = new VBox();          
        vBox.setPadding(new Insets(20));
        //vBox.setSpacing(10); 
        vBox.getChildren().add(tableWayp);
        root.getChildren().add(vBox);              
        
        tableWayp.setItems(gpsWayp);   
        
        tableWayp.getSelectionModel().selectedIndexProperty().addListener((obs, oldSelection, newSelection) -> {
            selectFile(newSelection.intValue());
        });        
                        
        Scene scene = new Scene(root, 280, 400);
       
        subStage.setTitle(wTitle);
        subStage.setScene(scene);
        subStage.showAndWait();       
    }
    
    private void selectFile(int idx) {
        selWaypPath = waypPathList.get(idx);
        subStage.close();
    }
    
    private void fillData() {
        ArrayList<String> waypNameList = new ArrayList<>(); 
        waypPathList = new ArrayList<>();
        try {
            switch (currGPS) {
                case Rever :
                   reversale usbRev = new reversale(myConfig.getOS(), myConfig.getGpsLimit());
                    if (usbRev.isConnected()) {
                        if(usbRev.isWpExist() || usbRev.isGoogExist()) {
                            usbRev.listWaypFiles(waypNameList);
                            if (waypNameList.size() > 0) {
                                waypPathList = usbRev.getWpPathList();
                            }
                        }
                    }
                    break;
                case Sky :
                    skytraax usbSky = new skytraax(myConfig.getOS(), myConfig.getGpsLimit());
                    if (usbSky.isConnected()) {
                        if(usbSky.isWpExist()) {
                            usbSky.listWaypFiles(waypNameList);
                            if (waypNameList.size() > 0) {
                                waypPathList = usbSky.getWpPathList();
                            }
                        }
                    }
                    break;
                case Sky3 :
                    skytraxx3 usbSky3 = new skytraxx3(myConfig.getOS(), myConfig.getGpsLimit());
                    if (usbSky3.isConnected()) {
                        if(usbSky3.isWpExist()) {
                            usbSky3.listWaypFiles(waypNameList);
                            if (waypNameList.size() > 0) {
                                waypPathList = usbSky3.getWpPathList();
                            }
                        }
                    }
                    break;                             
                case Oudie :
                    oudie usbOudie = new oudie(myConfig.getOS(), myConfig.getGpsLimit());
                    if (usbOudie.isConnected()) {
                        if(usbOudie.isWpExist()) {
                            usbOudie.listWaypFiles(waypNameList);
                            if (waypNameList.size() > 0) {
                                waypPathList = usbOudie.getWpPathList();
                            }
                        }
                    }
                    break;  
                case Syride :
                   // readUSBGps();
                    break;                            
                case Connect :
                    connect usbConn = new connect(myConfig.getOS(), myConfig.getGpsLimit());
                    if (usbConn.isConnected()) {
                        if(usbConn.isWpExist()) {
                            usbConn.listWaypFiles(waypNameList);
                            if (waypNameList.size() > 0) {
                                waypPathList = usbConn.getWpPathList();
                            }
                        }
                    }
                    break;   
                case Element :
                    element usbElem = new element(myConfig.getOS(), myConfig.getGpsLimit());
                    if (usbElem.isConnected()) {
                        if(usbElem.isWpExist()) {
                            usbElem.listWaypFiles(waypNameList);
                            if (waypNameList.size() > 0) {
                                waypPathList = usbElem.getWpPathList();
                            }
                        }
                    }
                    break;                         
                case CPilot :
                    compass usbCpilot = new compass(myConfig.getOS(), myConfig.getGpsLimit());
                    if (usbCpilot.isConnected()) {
                        if(usbCpilot.isWpExist()) {
                            usbCpilot.listWaypFiles(waypNameList);
                            if (waypNameList.size() > 0) {
                                waypPathList = usbCpilot.getWpPathList();
                            }
                        }
                    }
                    break;  
                case XCTracer :
                   // readUSBGps();
                    break;                        
            }
            if (waypNameList.size() > 0) {
                for (int i = 0; i < waypPathList.size(); i++) {
                    gpsWayp.add(waypNameList.get(i));
                    System.out.println(gpsWayp.get(i));
                }
            }
        } catch (Exception e) {
            sbError = new StringBuilder(this.getClass().getName()+"."+Thread.currentThread().getStackTrace()[1].getMethodName());
            sbError.append("\r\n").append(e.toString());
            mylogging.log(Level.SEVERE, sbError.toString());
        }        
    }        
}
