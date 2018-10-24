/* 
 * Copyright Gil THOMAS
 * This file forms an integral part of Logfly project
 * See the LICENSE file distributed with source code
 * for details of Logfly licence project
 */
package org.logfly;

import java.io.IOException;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import java.sql.SQLException;
import java.util.logging.Level;
import javafx.geometry.Rectangle2D;
import javafx.stage.Modality;
import javafx.stage.Screen;

import org.logfly.controller.AirspaceController;
import org.logfly.controller.CarnetViewController;
import org.logfly.controller.CartoViewController;
import org.logfly.controller.DashViewController;
import org.logfly.controller.GPSViewController;
import org.logfly.controller.ImportViewController;
import org.logfly.controller.ManualViewController;
import org.logfly.controller.RootLayoutController;
import org.logfly.controller.SitesViewController;
import org.logfly.controller.StatViewController;
import org.logfly.controller.TraceViewController;
import org.logfly.controller.WaypViewController;
import org.logfly.controller.XcpViewController;
import org.logfly.dialog.alertbox;
import org.logfly.liveUpdate.checkUpdate;
import org.logfly.liveUpdate.objects.Release;
import org.logfly.settings.configProg;
import org.logfly.settings.osType;
import org.logfly.systemio.mylogging;
import org.xnap.commons.i18n.I18n;
import org.xnap.commons.i18n.I18nFactory;

public class Main extends Application {
    private Stage primaryStage;
    private I18n i18n;
    private BorderPane rootLayout;
    private CarnetViewController controlCarnet;
    private TraceViewController controlTrace;
    private GPSViewController controlGPS;
    private SitesViewController controlSites;
    private WaypViewController controlWayp;
    private XcpViewController controlXcp;
    private AirspaceController controlAirSp;
    private CartoViewController controlCarto;
    private StringBuilder sbError;
    
    
    public configProg myConfig;
    
    public RootLayoutController rootLayoutController;
    
    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;        
        
        // Current version
        Release release = new Release();
        release.setpkgver("5.0");        
        release.setPkgrel("17");
        // last bundle
        release.setseverity("5.17");
        
        String currVersion = "Logfly "+release.getpkgver()+release.getPkgrel();
        this.primaryStage.setTitle(currVersion);
        
        // Reading settings
        myConfig = new configProg();
        myConfig.readSettings();         
                                  
        if (myConfig.isValidConfig()) {
            myConfig.setVersion(currVersion);           
            i18n = I18nFactory.getI18n("","lang/Messages",Main.class.getClass().getClassLoader(),myConfig.getLocale(),0);
                                    
            initRootLayout();        
            showCarnetOverview();  
                        
            try {
                checkUpdate checkUpgrade = new checkUpdate(release, myConfig);
            } catch (Exception e) {
                sbError = new StringBuilder(this.getClass().getName()+"."+Thread.currentThread().getStackTrace()[1].getMethodName());
                sbError.append("\r\n").append(e.toString());
                mylogging.log(Level.SEVERE, sbError.toString());
            }            
        } else  {            
            alertbox aError = new alertbox(java.util.Locale.ENGLISH);            
            StringBuilder errMsg = new StringBuilder();
            
            errMsg.append("myConfig.isValidConfig() = false\n");
            if (myConfig.isReadConfig())  {
                errMsg.append("Logfly.properties file OK\n");
                if (myConfig.isConfigDefault())  {
                    errMsg.append("Default settings applied\n");
                } else {
                    errMsg.append("Custom settings applied\n");
                }
                errMsg.append("Working path : "+myConfig.getPathW()).append("\n");
                errMsg.append("Logbook path : "+myConfig.getFullPathDb()).append("\n");
            } else {
                errMsg.append("Logfly.properties not read\n");
            }              
            mylogging.log(Level.SEVERE, errMsg.toString());
            aError.alertError(errMsg.toString());              
            initRootLayout();  
            rootLayoutController.showConfigDialog();
           // System.exit(0);                            
        }
    }
    
    /**
     * Initialization of root window with lateral menu 
     */
    public void initRootLayout() {
        try {
            // Load root layout from fxml file.
            FXMLLoader loader = new FXMLLoader();
            //loader.setLocation(Main.class.getResource("./src/main/java/view/RootLayout.fxml"));
            loader.setLocation(Main.class.getResource("/RootLayout.fxml"));
            rootLayout = (BorderPane) loader.load();   
            
            // Controller needs an access to main app
            // with this, we avoid a NullPointerException with a call to
            // mainApp.getPrimaryStage() in RootLayoutController                        
           
            rootLayoutController = loader.getController();
            rootLayoutController.setMainApp(this);

            Scene scene = new Scene(rootLayout);
            primaryStage.setScene(scene);
            primaryStage.show();
        } catch (IOException e) {
            sbError = new StringBuilder(this.getClass().getName()+"."+Thread.currentThread().getStackTrace()[1].getMethodName());
            sbError.append("\r\n").append(e.toString());
            mylogging.log(Level.SEVERE, sbError.toString());
        }
    }
    
    /**
     * Display logbook inside root window
     */
    public void showCarnetOverview() {
        try {
            FXMLLoader loader = new FXMLLoader();            
            loader.setLocation(org.logfly.Main.class.getResource("/CarnetView.fxml"));
            AnchorPane carnetOverview = (AnchorPane) loader.load();            
            
            // Initialization of a communication bridge between Carnetcontroller an RootLayout
            controlCarnet = loader.getController();  
            // Controller needs an access to main app
            // with this, we avoid a NullPointerException with a call to
            // mainApp.getPrimaryStage() in RootLayoutController     
            controlCarnet.setMainApp(this);
            
            // Place logbook window in center of RootLayout.
            rootLayout.setCenter(carnetOverview);                                                      
            
        } catch (IOException e) {
            sbError = new StringBuilder(this.getClass().getName()+"."+Thread.currentThread().getStackTrace()[1].getMethodName());
            sbError.append("\r\n").append(e.toString());
            mylogging.log(Level.SEVERE, sbError.toString());
        }
    }
    
    /**
    *   Display Import window inside root window
    */     
    public void showImportview() {
        try {
            FXMLLoader loader = new FXMLLoader();            
            loader.setLocation(org.logfly.Main.class.getResource("/ImportView.fxml"));
            AnchorPane importOverview = (AnchorPane) loader.load();            
            
            // Initialization of a communication bridge between ImportView and RootLayout
            ImportViewController controlImport = loader.getController(); 
            controlImport.setRootBridge(rootLayoutController);
            controlImport.setMyConfig(myConfig);
            controlImport.defaultFolder();
            // Place Import window in center of RootLayout.
            rootLayout.setCenter(importOverview);                                                      
            
        } catch (IOException e) {
            sbError = new StringBuilder(this.getClass().getName()+"."+Thread.currentThread().getStackTrace()[1].getMethodName());
            sbError.append("\r\n").append(e.toString());
            mylogging.log(Level.SEVERE, sbError.toString());
        }
    }
    
    /**
    *   Display Manual import window inside root window
    */     
    public void showManualview(int editMode, String idVol) {
        try {
            FXMLLoader loader = new FXMLLoader();            
            loader.setLocation(org.logfly.Main.class.getResource("/ManualView.fxml"));
            AnchorPane manualOverview = (AnchorPane) loader.load();     
            
            // Initialization of a communication bridge between ManualView and RootLayout
            ManualViewController controlManual = loader.getController(); 
            controlManual.setRootBridge(rootLayoutController, this);
            // Place Manual window in center of RootLayout.
            rootLayout.setCenter(manualOverview); 
            controlManual.setMyConfig(editMode,myConfig, idVol);
                                                                             
        } catch (IOException e) {
            sbError = new StringBuilder(this.getClass().getName()+"."+Thread.currentThread().getStackTrace()[1].getMethodName());
            sbError.append("\r\n").append(e.toString());
            mylogging.log(Level.SEVERE, sbError.toString());
        }
    }  
    
    public void showDashView() {
        
        try {
            FXMLLoader loader = new FXMLLoader();            
            loader.setLocation(org.logfly.Main.class.getResource("/DashView.fxml"));
            AnchorPane dashOverview = (AnchorPane) loader.load();  
            
            // Initialization of a communication bridge between DashView and RootLayout
            DashViewController controlDash = loader.getController(); 
            controlDash.setRootBridge(rootLayoutController, this);
            // Place Import window in center of RootLayout.
            rootLayout.setCenter(dashOverview); 
            controlDash.setMyConfig(myConfig);
                                                                             
        } catch (IOException e) {
            sbError = new StringBuilder(this.getClass().getName()+"."+Thread.currentThread().getStackTrace()[1].getMethodName());
            sbError.append("\r\n").append(e.toString());
            mylogging.log(Level.SEVERE, sbError.toString());
        }        
        
    }
    
    public void showStatView() {
        
        try {
            FXMLLoader loader = new FXMLLoader();            
            loader.setLocation(org.logfly.Main.class.getResource("/StatView.fxml"));            
            AnchorPane statOverview = (AnchorPane) loader.load();  
            
            // Initialization of a communication bridge between DashView and RootLayout
            StatViewController controlStat = loader.getController(); 
            controlStat.setRootBridge(rootLayoutController, this);
            // Place Import window in center of RootLayout.
            rootLayout.setCenter(statOverview); 
            controlStat.setMyConfig(myConfig);
                                                                             
        } catch (IOException e) {
            sbError = new StringBuilder(this.getClass().getName()+"."+Thread.currentThread().getStackTrace()[1].getMethodName());
            sbError.append("\r\n").append(e.toString());
            mylogging.log(Level.SEVERE, sbError.toString());
        }        
        
    }    

    /**
     * Display External GPS track window
     */
    public void showTraceview() {
        try {
            FXMLLoader loader = new FXMLLoader();            
            loader.setLocation(org.logfly.Main.class.getResource("/TraceView.fxml"));
            AnchorPane traceOverview = (AnchorPane) loader.load();            
            
            // Initialization of a communication bridge between TraceView and RootLayout
            controlTrace = loader.getController();     
            // Controller needs an access to main app
            // with this, we avoid a NullPointerException with a call to
            // mainApp.getPrimaryStage() in RootLayoutController        
            controlTrace.setMainApp(this);
            
            // Place External track window in center of RootLayout.
            rootLayout.setCenter(traceOverview);                                                      
            
        } catch (IOException e) {
            sbError = new StringBuilder(this.getClass().getName()+"."+Thread.currentThread().getStackTrace()[1].getMethodName());
            sbError.append("\r\n").append(e.toString());
            mylogging.log(Level.SEVERE, sbError.toString());
        }
    }
    
    /*
    * Display GPS communication window
    */
    public void showGPSview() {
        try {
            FXMLLoader loader = new FXMLLoader();            
            loader.setLocation(org.logfly.Main.class.getResource("/GPSView.fxml"));
            AnchorPane gpsOverview = (AnchorPane) loader.load();            
            
            // Initialization of a communication bridge between GPSView and RootLayout
            controlGPS = loader.getController();     
            controlGPS.setRootBridge(rootLayoutController);
            controlGPS.setMyConfig(myConfig);
            
            // Place GPS Import window in center of RootLayout..
            rootLayout.setCenter(gpsOverview); 
            controlGPS.displayWinGPS();
            
        } catch (IOException e) {
            sbError = new StringBuilder(this.getClass().getName()+"."+Thread.currentThread().getStackTrace()[1].getMethodName());
            sbError.append("\r\n").append(e.toString());
            mylogging.log(Level.SEVERE, sbError.toString());
        }
    }
    
    
    
    /**
     * Display waypoint form inside root window
     */
    public void showWaypOverview() {
        try {
            FXMLLoader loader = new FXMLLoader();            
            loader.setLocation(org.logfly.Main.class.getResource("/WaypView.fxml"));
            AnchorPane waypOverview = (AnchorPane) loader.load();            
// Litlle window            
//                    // Initialization of a communication bridge between Sitescontroller an RootLayout
//                    controlWayp = loader.getController();  
//                    // Controller needs an access to main app
//                    // with this, we avoid a NullPointerException with a call to
//                    // mainApp.getPrimaryStage() in RootLayoutController     
//                    controlWayp.setMainApp(this);
//
//                    // Place logbook window in center of RootLayout.
//                    rootLayout.setCenter(waypOverview);                                                      
// Full window
                    Stage fullWayp = new Stage();            
                    fullWayp.initModality(Modality.WINDOW_MODAL);       
                    fullWayp.initOwner(this.getPrimaryStage());
                    Scene scene = null;
                    if (myConfig.getOS() == osType.LINUX) {
                        // With this code for Linux, this is not OK with Win and Mac 
                        // This code found on http://java-buddy.blogspot.fr/2012/02/javafx-20-full-screen-scene.html
                        Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
                        scene = new Scene(waypOverview, screenBounds.getWidth(), screenBounds.getHeight());
                    } else {
                        // With this code, subStage.setMaximized(true) don't run under Linux
                        // PROVISOIREMENT scene = new Scene(waypOverview, 500, 400);
                        scene = new Scene(waypOverview, 1100, 600);
                    }                                    
                    fullWayp.setScene(scene);
                   
                    // Initialization of a communication bridge between CarnetView and KmlView
                    controlWayp = loader.getController();
                    controlWayp.setWaypStage(fullWayp);  
                    controlWayp.setRootBridge(rootLayoutController);
                    controlWayp.setMainApp(this); 
                    controlWayp.setWinMax();
                    fullWayp.showAndWait();
        } catch (IOException e) {
            sbError = new StringBuilder(this.getClass().getName()+"."+Thread.currentThread().getStackTrace()[1].getMethodName());
            sbError.append("\r\n").append(e.toString());
            mylogging.log(Level.SEVERE, sbError.toString());
        }
    }

    /**
     * Display airspaces utilities form inside root window
     */
    public void showAirspaceView() {
        try {
            FXMLLoader loader = new FXMLLoader();            
            loader.setLocation(org.logfly.Main.class.getResource("/airspace.fxml"));
            AnchorPane openAirOverview = (AnchorPane) loader.load();            
// ==========       Litlle window            
//                    controlAirSp = loader.getController();  
//                    rootLayout.setCenter(openAirOverview);                                                      
// =========== Full window
            Stage fullAirSp = new Stage();            
            fullAirSp.initModality(Modality.WINDOW_MODAL);       
            fullAirSp.initOwner(this.getPrimaryStage());
            Scene scene = null;
            if (myConfig.getOS() == osType.LINUX) {
                // With this code for Linux, this is not OK with Win and Mac 
                // This code found on http://java-buddy.blogspot.fr/2012/02/javafx-20-full-screen-scene.html
                Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
                scene = new Scene(openAirOverview, screenBounds.getWidth(), screenBounds.getHeight());
            } else {
                // With this code, subStage.setMaximized(true) don't run under Linux
                // PROVISOIREMENT scene = new Scene(waypOverview, 500, 400);
                scene = new Scene(openAirOverview, 1100, 600);
            }                                    
            fullAirSp.setScene(scene);

            // Initialization of a communication bridge between CarnetView and KmlView
            controlAirSp = loader.getController();
            controlAirSp.setAirStage(fullAirSp);  
            controlAirSp.setWinMax();
            
            controlAirSp.setRootBridge(rootLayoutController);
            controlAirSp.setMainApp(this); 

            fullAirSp.showAndWait();
        } catch (IOException e) {
            sbError = new StringBuilder(this.getClass().getName()+"."+Thread.currentThread().getStackTrace()[1].getMethodName());
            sbError.append("\r\n").append(e.toString());
            mylogging.log(Level.SEVERE, sbError.toString());
        }
    }    
    
    
    /**
     * Display mùap utilities form inside root window
     */
    public void showCartoOverview() {
        try {
            FXMLLoader loader = new FXMLLoader();            
            loader.setLocation(org.logfly.Main.class.getResource("/CartoView.fxml"));
            AnchorPane cartoOverview = (AnchorPane) loader.load();            
// ==========       Litlle window            
            //controlCarto = loader.getController();  
            //rootLayout.setCenter(cartoOverview);                                                  
// Full window
            Stage fullCarto = new Stage();            
            fullCarto.initModality(Modality.WINDOW_MODAL);       
            fullCarto.initOwner(this.getPrimaryStage());
            Scene scene = null;
            if (myConfig.getOS() == osType.LINUX) {
                // With this code for Linux, this is not OK with Win and Mac 
                // This code found on http://java-buddy.blogspot.fr/2012/02/javafx-20-full-screen-scene.html
                Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
                scene = new Scene(cartoOverview, screenBounds.getWidth(), screenBounds.getHeight());
            } else {
                // With this code, subStage.setMaximized(true) don't run under Linux
                // PROVISOIREMENT scene = new Scene(waypOverview, 500, 400);
                scene = new Scene(cartoOverview, 1100, 600);
            }                                    
            fullCarto.setScene(scene);

            // Initialization of a communication bridge between CarnetView and KmlView
            controlCarto = loader.getController();
            controlCarto.setCartoStage(fullCarto);                
            controlCarto.setWinMax();
                   
            controlCarto.setRootBridge(rootLayoutController);
            controlCarto.setMainApp(this);         
            fullCarto.showAndWait();

        } catch (IOException e) {
            sbError = new StringBuilder(this.getClass().getName()+"."+Thread.currentThread().getStackTrace()[1].getMethodName());
            sbError.append("\r\n").append(e.toString());
            mylogging.log(Level.SEVERE, sbError.toString());
        }
    }    
       
    /**
     * Display Sites form inside root window
     */
    public void showSitesOverview() {
        try {
            FXMLLoader loader = new FXMLLoader();            
            loader.setLocation(org.logfly.Main.class.getResource("/SitesView.fxml"));
            AnchorPane sitesOverview = (AnchorPane) loader.load();            
            
            // Initialization of a communication bridge between Sitescontroller an RootLayout
            controlSites = loader.getController();  
            // Controller needs an access to main app
            // with this, we avoid a NullPointerException with a call to
            // mainApp.getPrimaryStage() in RootLayoutController     
            controlSites.setMainApp(this);
            
            // Place logbook window in center of RootLayout.
            rootLayout.setCenter(sitesOverview);                                                      
            
        } catch (IOException e) {
            sbError = new StringBuilder(this.getClass().getName()+"."+Thread.currentThread().getStackTrace()[1].getMethodName());
            sbError.append("\r\n").append(e.toString());
            mylogging.log(Level.SEVERE, sbError.toString());
        }
    }        
    
    /**
     * Display Sites form inside root window
     */
    public void showXcplannerview() {
        try {
            FXMLLoader loader = new FXMLLoader();            
            loader.setLocation(org.logfly.Main.class.getResource("/XcplannerView.fxml"));
            AnchorPane xcpview = (AnchorPane) loader.load();     
            
            Stage fullXcp = new Stage();            
            fullXcp.initModality(Modality.WINDOW_MODAL);       
            fullXcp.initOwner(this.getPrimaryStage());
            Scene scene = null;
            if (myConfig.getOS() == osType.LINUX) {
                // With this code for Linux, this is not OK with Win and Mac 
                // This code found on http://java-buddy.blogspot.fr/2012/02/javafx-20-full-screen-scene.html
                Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
                scene = new Scene(xcpview, screenBounds.getWidth(), screenBounds.getHeight());
            } else {
                // With this code, subStage.setMaximized(true) don't run under Linux
                // PROVISOIREMENT scene = new Scene(waypOverview, 500, 400);
                scene = new Scene(xcpview, 1100, 600);
            }                                    
            fullXcp.setScene(scene);            
            
            // Initialization of a communication bridge between Xccontroller an RootLayout
            controlXcp = loader.getController();  
            controlXcp.setXcpStage(fullXcp);                
            controlXcp.setWinMax();  
            controlXcp.setRootBridge(rootLayoutController);
            controlXcp.setMainApp(this);
            fullXcp.showAndWait();                                                      
            
        } catch (IOException e) {
            sbError = new StringBuilder(this.getClass().getName()+"."+Thread.currentThread().getStackTrace()[1].getMethodName());
            sbError.append("\r\n").append(e.toString());
            mylogging.log(Level.SEVERE, sbError.toString());
        }
    }          
    
    
    /**
     * Return main stage
     * @return
     */
    public Stage getPrimaryStage() {
        return primaryStage;
    }       
    
    @Override
    public void stop() throws SQLException{
        if (myConfig.isValidConfig())  {
            int currWidth = (int)primaryStage.getWidth();
            int currHeight = (int)primaryStage.getHeight();
            if (currWidth > 670 && currHeight > 630) { 
                myConfig.setMainWidth(currWidth);
                myConfig.setMainHeight(currHeight);            
                myConfig.writeProperties();
            }
            myConfig.getDbConn().close();
        }
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}
