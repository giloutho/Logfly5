/*
 * Copyright Gil THOMAS
 * This file forms an integral part of Logfly project
 * See the LICENSE file distributed with source code
 * for details of Logfly licence project
 */
package littlewins;

import controller.SiteFormController;
import controller.WaypViewController;
import controller.XcpViewController;
import javafx.collections.ObservableList;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import model.Sitemodel;
import org.xnap.commons.i18n.I18n;
import settings.configProg;

/**
 *
 * @author gil
 */
public class winOsmCities {
    
    private Stage subStage;
    
    private TableView tableView = new TableView();    
    private TableColumn<Sitemodel, String> colVille;
    private TableColumn<Sitemodel, String> colCp;
    private TableColumn<Sitemodel, String> colPays;    
    private ObservableList <Sitemodel> dataCities;
    
    // Reference to Controllers
    private WaypViewController waypController;
    private SiteFormController sitefController;
    private XcpViewController xcpController;
    private int typeController;
    
    // Localization
    private I18n i18n; 
    
    // Settings
    configProg myConfig;
    StringBuilder sbError;
    String RC = "\n";

    public winOsmCities(I18n pI18n, ObservableList <Sitemodel> pDataCities, WaypViewController pWaypController)  {       
        this.i18n = pI18n;     
        dataCities = pDataCities;
        this.waypController = pWaypController; 
        typeController = 1;
        showWin();
    }       
    
    public winOsmCities(I18n pI18n, ObservableList <Sitemodel> pDataCities, SiteFormController pSiteController)  {  
        this.i18n = pI18n;     
        dataCities = pDataCities;
        this.sitefController = pSiteController;   
        typeController = 2;
        showWin();
    }       
    
    public winOsmCities(I18n pI18n, ObservableList <Sitemodel> pDataCities, XcpViewController pXcpController)  {  
        this.i18n = pI18n;     
        dataCities = pDataCities;
        this.xcpController = pXcpController;   
        typeController = 3;
        showWin();
    }        
            
    private void showWin() {
        subStage = new Stage();    
        
        tableView.prefHeightProperty().bind(subStage.heightProperty());
        tableView.prefWidthProperty().bind(subStage.widthProperty());

        colVille = new TableColumn(i18n.tr("City"));
        tableView.getColumns().add(colVille);
        colVille.setCellValueFactory(
            new PropertyValueFactory<Sitemodel, String>("ville"));  
        
        colCp = new TableColumn(i18n.tr("ZIP"));
        tableView.getColumns().add(colCp);
        colCp.setCellValueFactory(
            new PropertyValueFactory<Sitemodel, String>("cp"));    
        
        colPays = new TableColumn(i18n.tr("Country"));
        tableView.getColumns().add(colPays);
        colPays.setCellValueFactory(
            new PropertyValueFactory<Sitemodel, String>("pays"));           
        
        colVille.prefWidthProperty().bind(tableView.widthProperty().multiply(0.40));
        colCp.prefWidthProperty().bind(tableView.widthProperty().multiply(0.22));
        colPays.prefWidthProperty().bind(tableView.widthProperty().multiply(0.38));
        colVille.setResizable(false);
        colCp.setResizable(false);        
        colPays.setResizable(false);    
        
       // tableView.setPadding(new Insets(20, 20, 30, 20));
       tableView.setRowFactory(tv -> {
            TableRow<Sitemodel> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && (! row.isEmpty()) ) {
                    Sitemodel rowData = row.getItem();
                    subStage.close();
                    switch (typeController) {
                        case 1:
                            System.out.println("Lat : "+rowData.getLatitude());
                            waypController.returnFromOsmCities(rowData);
                            break;
                        case 2:
                            sitefController.returnFromOsmCities(rowData);
                            break;
                        case 3:
                            xcpController.returnFromOsmCities(rowData);
                            break;                            
                    } 
                }
            });
            return row ;
        });
       
       
       
        tableView.setItems(dataCities);

        
        Group root = new Group();
        VBox vBox = new VBox();                   
        vBox.getChildren().add(tableView);
        root.getChildren().add(vBox);
        subStage.setScene(new Scene(root, 350, 300));
        subStage.showAndWait();           
    }
    
}
