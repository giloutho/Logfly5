/*
 * Copyright Gil THOMAS
 * This file forms an integral part of Logfly project
 * See the LICENSE file distributed with source code
 * for details of Logfly licence project
 */
package littlewins;

import controller.CarnetViewController;
import controller.ManualViewController;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.logging.Level;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import model.Sitemodel;
import org.xnap.commons.i18n.I18n;
import settings.configProg;
import systemio.mylogging;

/**
 *
 * @author gil
 */
public class winSiteChoice {
    
    private Stage subStage;
    
    private TextField txtSearch; 
    private TableView tableView = new TableView();    
    private TableColumn<Sitemodel, String> colNom;
    private TableColumn<Sitemodel, String> colVille;
    private TableColumn<Sitemodel, String> colCp;
    private TableColumn<Sitemodel, String> colAlt;        
    
    // Localization
    private I18n i18n; 
    
    // Settings
    configProg myConfig;
    StringBuilder sbError;
    String RC = "\n";
    
    // Reference to CarnetViewController
    private CarnetViewController carnetController; 
    // Reference to ManualViewController
    private ManualViewController manualController;
    private int typeController;
    
    //START | SQLITE
    private static Statement stat;
    private PreparedStatement prep;
    //END | SQLITE        
    
    private ObservableList <Sitemodel> dataSites; 
    private FilteredList<Sitemodel> filteredData;
    private SortedList<Sitemodel> sortedData;  
    
    public winSiteChoice(configProg pConfig, I18n pI18n, CarnetViewController pCarnetController)  {
        myConfig = pConfig;        
        this.i18n = pI18n;     
        this.carnetController = pCarnetController; 
        typeController = 1;
        showWin();
    }   
    
    public winSiteChoice(configProg pConfig, I18n pI18n, ManualViewController pManualController)  {
        myConfig = pConfig;        
        this.i18n = pI18n;     
        this.manualController = pManualController;   
        typeController = 2;
        showWin();
    }             
            
    private void showWin() {
        subStage = new Stage();
                
        subStage.setTitle(i18n.tr("Double click to select a site in the list"));
        HBox hbSearch = new HBox();
        hbSearch.setPadding(new Insets(5, 12, 5, 12));
        hbSearch.setSpacing(100);
        txtSearch = new TextField();        
        txtSearch.setPrefWidth(200);
        Button btCreation = new Button(i18n.tr("New"));
        btCreation.setOnAction((event) -> {
            newSite();
        });
        hbSearch.getChildren().addAll( txtSearch, btCreation);        
                
        tableView.prefHeightProperty().bind(subStage.heightProperty());
        tableView.prefWidthProperty().bind(subStage.widthProperty());

        colNom = new TableColumn(i18n.tr("Site"));
        tableView.getColumns().add(colNom);
        colNom.setCellValueFactory(
            new PropertyValueFactory<Sitemodel, String>("nom"));
        
        colVille = new TableColumn(i18n.tr("City"));
        tableView.getColumns().add(colVille);
        colVille.setCellValueFactory(
            new PropertyValueFactory<Sitemodel, String>("ville"));  
        
        colCp = new TableColumn(i18n.tr("ZIP"));
        tableView.getColumns().add(colCp);
        colCp.setCellValueFactory(
            new PropertyValueFactory<Sitemodel, String>("cp"));     
        
        colAlt = new TableColumn(i18n.tr("Alt"));
        tableView.getColumns().add(colAlt);
        colAlt.setCellValueFactory(
            new PropertyValueFactory<Sitemodel, String>("alt"));     
        
        colNom.prefWidthProperty().bind(tableView.widthProperty().multiply(0.40));
        colVille.prefWidthProperty().bind(tableView.widthProperty().multiply(0.35));
        colCp.prefWidthProperty().bind(tableView.widthProperty().multiply(0.12));
        colAlt.prefWidthProperty().bind(tableView.widthProperty().multiply(0.10));
        colNom.setResizable(false);
        colVille.setResizable(false);
        colCp.setResizable(false);
        colAlt.setResizable(false);
        
        dataSites = FXCollections.observableArrayList(); 
        
        // filter process read on http://code.makery.ch/blog/javafx-8-tableview-sorting-filtering/
        // wrap the ObservableList in a FilteredList (initially display all data).
        // the same site has an old version of code : http://code.makery.ch/blog/javafx-2-tableview-filter/
        filteredData = new FilteredList<>(dataSites, p -> true);
        
        // set the filter Predicate whenever the filter changes.
        txtSearch.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredData.setPredicate(site -> {
                // If filter text is empty, display all persons.
                if (newValue == null || newValue.isEmpty()) {
                    return true;
                }

                // Compare fields of every site with filter text.
                String lowerCaseFilter = newValue.toLowerCase();

                if (site.getNom().toLowerCase().contains(lowerCaseFilter)) {
                    return true; // Filter matches site name
                } 
                else if (site.getVille() != null && !site.getVille().equals("")) {
                    if (site.getVille().toLowerCase().contains(lowerCaseFilter)) 
                        return true; // Filter matches site locality
                    
                    else if (site.getCp() != null && !site.getCp().equals("")) {
                        if (site.getCp().toLowerCase().contains(lowerCaseFilter)) 
                            return true; // Filter matches site locality
                    }    
                }
                
                return false; // Does not match.
            });
        });            
        
        // wrap the FilteredList in a SortedList. 
        sortedData = new SortedList<>(filteredData);
        
        // bind the SortedList comparator to the TableView comparator.
        sortedData.comparatorProperty().bind(tableView.comparatorProperty());   
        
        tableView.setRowFactory(tv -> {
            TableRow<Sitemodel> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && (! row.isEmpty()) ) {
                    Sitemodel rowData = row.getItem();
                    subStage.close();
                    switch (typeController) {
                        case 1:
                            carnetController.updateSelectedSite(rowData);
                            break;
                        case 2:
                            manualController.updateSelectedSite(rowData);
                            break;
                    } 
                }
            });
            return row ;
        });
                
        fillTable();
        
        Group root = new Group();
        VBox vBox = new VBox();
        vBox.getChildren().addAll(hbSearch,tableView);
        root.getChildren().add(vBox);
        subStage.setScene(new Scene(root, 500, 400));
        subStage.initModality(Modality.WINDOW_MODAL);       
        subStage.show();            
    }
    
    private void fillTable() {
        Statement stmt = null;
        ResultSet rs = null;
        String sReq = "SELECT * FROM Site WHERE S_Type = 'D' ORDER BY S_Nom";
        try {
            stmt = myConfig.getDbConn().createStatement();
            rs = stmt.executeQuery(sReq);
            if (rs != null)  { 
                while (rs.next()) {
                    Sitemodel si = new Sitemodel();  
                    si.setIdSite(rs.getString("S_ID"));
                    si.setNom(rs.getString("S_Nom"));
                    si.setVille(rs.getString("S_Localite"));
                    si.setCp(rs.getString("S_CP"));
                    si.setPays(rs.getString("S_Pays"));
                    si.setAlt(rs.getString("S_Alti"));
                    si.setOrient(rs.getString("S_Orientation"));  
                    si.setType(rs.getString("S_Type"));         
                    si.setLatitude(rs.getDouble("S_Latitude"));
                    si.setLongitude(rs.getDouble("S_Longitude"));
                    dataSites.add(si);                
                }    
                tableView.setItems(sortedData);
                if (tableView.getItems().size() > 0) {
                    tableView.getSelectionModel().select(0);                    
                }                
            }            
        } catch ( Exception e ) {
            sbError = new StringBuilder(this.getClass().getName()+"."+Thread.currentThread().getStackTrace()[1].getMethodName());
            sbError.append("\r\n").append(e.toString());
            mylogging.log(Level.SEVERE, sbError.toString());   
        } finally {
            try{
                rs.close(); 
                stmt.close();
            } catch(Exception e) { } 
        }                 
    }

    private void newSite() {
        Sitemodel newSite = new Sitemodel();
        newSite.setIdSite("NEW");
        subStage.close();
        switch (typeController) {
            case 1:
                carnetController.updateSelectedSite(newSite);
                break;
            case 2:
                manualController.updateSelectedSite(newSite);
                break;
        }                 
    }

}
