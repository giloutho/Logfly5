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
import controller.RootLayoutController;
import controller.TraceViewController;
import org.xnap.commons.i18n.I18n;
import org.xnap.commons.i18n.I18nFactory;
import settings.configProg;

public class Main extends Application {
    private Stage primaryStage;
    private I18n i18n;
    private BorderPane rootLayout;
    private CarnetViewController controlCarnet;
    private TraceViewController controlTrace;
    private GPSViewController controlGPS;
    
    
    public configProg myConfig;
    
    private RootLayoutController rootLayoutController;
    
    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        this.primaryStage.setTitle("Logfly");
             
        // Reading settings
        myConfig = new configProg();
        myConfig.readSettings();         
                                  
        if (myConfig.isValidConfig()) {
            i18n = I18nFactory.getI18n("","lang/Messages",Main.class.getClass().getClassLoader(),myConfig.getLocale(),0);
            initRootLayout();        
            showCarnetOverview();           
        } else  {            
            alertbox aError = new alertbox(java.util.Locale.ENGLISH);            
            StringBuilder errMsg = new StringBuilder();
            
            if (myConfig.isReadConfig())  {
                errMsg.append("Logfly.properties read\n");
            } else {
                errMsg.append("Logfly.properties not read\n");
            }
            if (myConfig.isConfigDefault())  {
                errMsg.append("CDefault settings\n");
            } else {
                errMsg.append("custom settings\n");
            }
            errMsg.append("Logbook path : "+myConfig.getFullPathDb());                
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
            e.printStackTrace();
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
            e.printStackTrace();
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
            e.printStackTrace();
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
            e.printStackTrace();
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
            e.printStackTrace();
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
    public void stop(){
        if (myConfig.isValidConfig())  {
            myConfig.writeProperties();
        }
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}
