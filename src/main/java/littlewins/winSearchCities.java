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
import dialogues.alertbox;
import geoutils.geonominatim;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import model.Sitemodel;
import org.xnap.commons.i18n.I18n;
import settings.configProg;

/**
 *
 * @author gil
 */
public class winSearchCities {
    
    private Stage subStage;
    
    private TextField txtSearch; 
    private Label lbNote;
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

    public winSearchCities(I18n pI18n, WaypViewController pWaypController)  {       
        this.i18n = pI18n;     
        this.waypController = pWaypController; 
        typeController = 1;
        showWin();
    }     
    
    private void showWin() {
        subStage = new Stage();    
        subStage.setTitle(i18n.tr("Where to place the map"));
        HBox hbSearch = new HBox();
        hbSearch.setPadding(new Insets(5, 12, 5, 12));
        hbSearch.setSpacing(10);
        txtSearch = new TextField();        
        txtSearch.setPrefWidth(200);
        Button btSearch = new Button(i18n.tr("Search"));
        btSearch.setOnAction((event) -> {
           searchOsmName();
        });
        Button btDefault = new Button(i18n.tr("Default"));
        btDefault.setOnAction((event) -> {
            returnDefault();
        });        
        hbSearch.getChildren().addAll(txtSearch, btSearch, btDefault);  
        HBox hbMsg = new HBox();
        lbNote = new Label();
        lbNote.setPrefSize(300, 15);
        lbNote.setPadding(new Insets(5, 12, 5, 12));
        lbNote.setTextFill(Color.web("#ff0000", 0.8));
        lbNote.setText(i18n.tr("Double click to select a town"));
        lbNote.setVisible(false);
        hbMsg.getChildren().add(lbNote);
        
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
                             waypController.returnFromSearchCities(rowData);
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
        tableView.setVisible(false);
                            
        Group root = new Group();
        VBox vBox = new VBox();                   
        vBox.getChildren().addAll(hbSearch, hbMsg, tableView);
        root.getChildren().add(vBox);
        subStage.setScene(new Scene(root, 450, 300));
        subStage.showAndWait();           
    }
    
    private void searchOsmName() {
        if (txtSearch.getText() != null && !txtSearch.getText().equals("")) {
            String pSearch = txtSearch.getText().trim();
            geonominatim debGeo = new geonominatim();         
            debGeo.askGeo(pSearch);
            dataCities = debGeo.getOsmTowns(); 
            int lsSize = dataCities.size();         
            if (lsSize > 0) {
                tableView.setItems(dataCities); 
                tableView.setVisible(true);
                lbNote.setVisible(true);              
            } else {
                alertbox aError = new alertbox(myConfig.getLocale());
                aError.alertNumError(debGeo.getGeoError());
            }
        }
    } 
    
    private void returnDefault() {
        subStage.close();
        waypController.displayDefault();
    }
    
    
}
