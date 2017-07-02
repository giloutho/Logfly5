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
import settings.configProg;

public class Main extends Application {
    private Stage primaryStage;
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
        
        // Lecture paramètres
        myConfig = new configProg();
        myConfig.readSettings(); 
                                  
        if (myConfig.isValidConfig()) {
            initRootLayout();        
            showCarnetOverview();           
        } else  {            
            alertbox aError = new alertbox();
            // Localisation nécessaire
            // aError.alertError(i18n.tr("Fichier non trouvé"));  
            StringBuilder errMsg = new StringBuilder();
            
            if (myConfig.isReadConfig())  {
                errMsg.append("Logfly.properties lu\n");
            } else {
                errMsg.append("Logfly.properties non lu\n");
            }
            if (myConfig.isConfigDefault())  {
                errMsg.append("Configuration par défaut\n");
            } else {
                errMsg.append("Configuration personnalisée\n");
            }
            errMsg.append("Chemin carnet : "+myConfig.getFullPathDb());                
            aError.alertError(errMsg.toString());  
            System.exit(0);                            
        }
//        primaryStage.setOnCloseRequest(event -> {
//            System.out.println("Stage is closing");
//            // Save    file
//        });
    }
    
    /**
     * Initialise la fenêtre racine qui contient le menu latéral
     */
    public void initRootLayout() {
        try {
            // Load root layout from fxml file.
            FXMLLoader loader = new FXMLLoader();
            //loader.setLocation(Main.class.getResource("./src/main/java/view/RootLayout.fxml"));
            loader.setLocation(Main.class.getResource("/RootLayout.fxml"));
            rootLayout = (BorderPane) loader.load();   
            
             // Le controller va avoir besoin d'un accès au stage de l'appli principale
             // Grâce à cela on évite un NullPointerException lors de l'appel 
             // à mainApp.getPrimaryStage()  dans le RootLayoutController                        
           
            rootLayoutController = loader.getController();
            rootLayoutController.setMainApp(this);

            // Show the scene containing the root layout.
            Scene scene = new Scene(rootLayout);
            primaryStage.setScene(scene);
            primaryStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Affiche le carnet de vols dans la fenêtre racine (root layout).
     */
    public void showCarnetOverview() {
        try {
            // Load carnet overview.
            FXMLLoader loader = new FXMLLoader();            
            loader.setLocation(Logfly.Main.class.getResource("/CarnetView.fxml"));
            AnchorPane carnetOverview = (AnchorPane) loader.load();            
            
            // Initialisation d'un pont de communication entre le controller Carnet et RootLayout
            controlCarnet = loader.getController();  
            // Le controller va avoir besoin d'un accès au stage de l'appli principale
            // Grâce à cela on évite un NullPointerException lors de l'appel à mainApp.getPrimaryStage()         
            controlCarnet.setMainApp(this);
            
            // SPositionne le carnet sur le centre du root layout.
            rootLayout.setCenter(carnetOverview);                                                      
            
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Affiche le carnet de vols dans la fenêtre racine (root layout).
    */     
    public void showImportview() {
        try {
            // Load carnet overview.
            FXMLLoader loader = new FXMLLoader();            
            loader.setLocation(Logfly.Main.class.getResource("/ImportView.fxml"));
            AnchorPane importOverview = (AnchorPane) loader.load();            
            
            // Initialisation d'un pont de communication entre le controller ImportView et RootLayout
            ImportViewController controlImport = loader.getController(); 
            controlImport.setRootBridge(rootLayoutController);
            controlImport.setMyConfig(myConfig);
            
            // SPositionne le carnet sur le centre du root layout.
            rootLayout.setCenter(importOverview);                                                      
            
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    
    public void showTraceview() {
        try {
            // Load carnet overview.
            FXMLLoader loader = new FXMLLoader();            
            loader.setLocation(Logfly.Main.class.getResource("/TraceView.fxml"));
            AnchorPane traceOverview = (AnchorPane) loader.load();            
            
            // Initialisation d'un pont de communication entre le controller TraceView et RootLayout
            controlTrace = loader.getController();     
            // Le controller va avoir besoin d'un accès au stage de l'appli principale
            // Grâce à cela on évite un NullPointerException lors de l'appel à mainApp.getPrimaryStage()         
            controlTrace.setMainApp(this);
            
            // SPositionne le carnet sur le centre du root layout.
            rootLayout.setCenter(traceOverview);                                                      
            
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public void showGPSview() {
        try {
            // Load carnet overview.
            FXMLLoader loader = new FXMLLoader();            
            loader.setLocation(Logfly.Main.class.getResource("/GPSView.fxml"));
            AnchorPane gpsOverview = (AnchorPane) loader.load();            
            
            // Initialisation d'un pont de communication entre le controller GPSView et RootLayout
            controlGPS = loader.getController();     
            controlGPS.setRootBridge(rootLayoutController);
            controlGPS.setMyConfig(myConfig);
            
            // SPositionne le carnet sur le centre du root layout.
            rootLayout.setCenter(gpsOverview);                                                      
            
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Renvoie la scène principale.
     * @return
     */
    public Stage getPrimaryStage() {
        return primaryStage;
    }       
    
    @Override
    public void stop(){
        if (myConfig.isValidConfig())  {
            myConfig.writeProperties();
           // System.out.println("Stage is closing");
        }
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}
