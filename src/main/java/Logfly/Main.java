/* 
 * Copyright Gil THOMAS
 * This file forms an integral part of Logfly project
 * See the LICENSE file distributed with source code
 * for details of Logfly licence project
 */
package Logfly;

import dialogues.alertbox;
import java.io.IOException;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import controller.CarnetViewController;
import controller.GPSViewController;
import controller.ImportViewController;
import controller.ManualViewController;
import controller.RootLayoutController;
import controller.SitesViewController;
import controller.TraceViewController;
import java.sql.SQLException;
import java.util.logging.Level;
import javafx.stage.Modality;
import liveUpdate.checkUpdate;
import liveUpdate.objects.Release;
import org.xnap.commons.i18n.I18n;
import org.xnap.commons.i18n.I18nFactory;
import settings.configProg;
import systemio.mylogging;

public class Main extends Application {
    private Stage primaryStage;
    private I18n i18n;
    private BorderPane rootLayout;
    private CarnetViewController controlCarnet;
    private TraceViewController controlTrace;
    private GPSViewController controlGPS;
    private SitesViewController controlSites;
    private StringBuilder sbError;
    
    
    public configProg myConfig;
    
    public RootLayoutController rootLayoutController;
    
    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;        
        
        // Current version
        Release release = new Release();
        release.setpkgver("5.0");        
        release.setPkgrel("14");
        // last bundle
        release.setseverity("5.13");
        
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
            
            if (myConfig.isReadConfig())  {
                errMsg.append("Logfly.properties read\n");
            } else {
                errMsg.append("Logfly.properties not read\n");
            }
            if (myConfig.isConfigDefault())  {
                errMsg.append("Default settings\n");
            } else {
                errMsg.append("custom settings\n");
            }
            errMsg.append("Logbook path : "+myConfig.getFullPathDb());               
            mylogging.log(Level.SEVERE, errMsg.toString());
            aError.alertNumError(1000);   // Error in reading the parameters
            aError.alertError(errMsg.toString());  
            System.exit(0);                            
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
            loader.setLocation(Logfly.Main.class.getResource("/CarnetView.fxml"));
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
            loader.setLocation(Logfly.Main.class.getResource("/ImportView.fxml"));
            AnchorPane importOverview = (AnchorPane) loader.load();            
            
            // Initialization of a communication bridge between ImportView and RootLayout
            ImportViewController controlImport = loader.getController(); 
            controlImport.setRootBridge(rootLayoutController);
            controlImport.setMyConfig(myConfig);
            
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
            loader.setLocation(Logfly.Main.class.getResource("/ManualView.fxml"));
            AnchorPane manualOverview = (AnchorPane) loader.load();     
            
            // Initialization of a communication bridge between ImportView and RootLayout
            ManualViewController controlManual = loader.getController(); 
            controlManual.setRootBridge(rootLayoutController, this);
            // Place Import window in center of RootLayout.
            rootLayout.setCenter(manualOverview); 
            controlManual.setMyConfig(editMode,myConfig, idVol);
                                                                             
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
            loader.setLocation(Logfly.Main.class.getResource("/TraceView.fxml"));
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
            loader.setLocation(Logfly.Main.class.getResource("/GPSView.fxml"));
            AnchorPane gpsOverview = (AnchorPane) loader.load();            
            
            // Initialization of a communication bridge between GPSView and RootLayout
            controlGPS = loader.getController();     
            controlGPS.setRootBridge(rootLayoutController);
            controlGPS.setMyConfig(myConfig);
            
            // Place GPS Import window in center of RootLayout..
            rootLayout.setCenter(gpsOverview);                                                      
            
        } catch (IOException e) {
            sbError = new StringBuilder(this.getClass().getName()+"."+Thread.currentThread().getStackTrace()[1].getMethodName());
            sbError.append("\r\n").append(e.toString());
            mylogging.log(Level.SEVERE, sbError.toString());
        }
    }
    
    /**
     * Display logbook inside root window
     */
    public void showSitesOverview() {
        try {
            FXMLLoader loader = new FXMLLoader();            
            loader.setLocation(Logfly.Main.class.getResource("/SitesView.fxml"));
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
