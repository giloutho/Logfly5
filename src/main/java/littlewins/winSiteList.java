/*
 * Copyright Gil THOMAS
 * This file forms an integral part of Logfly project
 * See the LICENSE file distributed with source code
 * for details of Logfly licence project
 */
package littlewins;

import database.dbImport;
import java.io.File;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Callback;
import model.sitedownl;
import org.xnap.commons.i18n.I18n;
import settings.configProg;
import settings.privateData;
import systemio.siteslistdown;
import systemio.tempacess;
import systemio.webdown;

/**
 *
 * @author gil
 * avec l'aide de http://java-buddy.blogspot.fr/2013/03/javafx-embed-button-in-tableview.html
 */
public class winSiteList {
    
    private TableView tableView = new TableView();    
    private TableColumn<sitedownl, String> colPays;   
    private TableColumn<sitedownl, String> colRegion;    
    private TableColumn<sitedownl, String> colOrigine;   
    private TableColumn colButton;
    
    private ObservableList<sitedownl> dataObs = FXCollections.observableArrayList();
    
    private String dataList;
    private I18n i18n; 
    private configProg myConfig;
    private boolean successLoad;
    private File loadFile;
    
    
    public winSiteList(configProg pConfig, I18n pI18n)  {
        myConfig = pConfig;        
        this.i18n = pI18n;
        siteslistdown downlist = new siteslistdown();        
        dataList = downlist.askList();        
        fillData();
        showWin();
    }
    
    private void fillData() {
        String RC = "\n";
        String[] lines = dataList.split(RC);
        try {
          for (int i = 0; i < lines.length; i++) {
              String[] partLine = lines[i].split("\\*");
              if (partLine.length > 3) {
                    sitedownl fileSite = new sitedownl(partLine[0],partLine[1],partLine[2],partLine[3]);                      
                    dataObs.add(fileSite);
              }
          }          
        } catch (Exception e) {
        }                              
    }
    
    private void showWin() {
        Stage subStage = new Stage();
                
        tableView.prefHeightProperty().bind(subStage.heightProperty());
        tableView.prefWidthProperty().bind(subStage.widthProperty());

        colPays = new TableColumn(i18n.tr("Country"));
        tableView.getColumns().add(colPays);
        colPays.setCellValueFactory(
                    new PropertyValueFactory<sitedownl, String>("pays"));  
                
        colRegion = new TableColumn(i18n.tr("Area"));
        tableView.getColumns().add(colRegion);
        colRegion.setCellValueFactory(
                    new PropertyValueFactory<sitedownl, String>("region"));  
        
        colOrigine = new TableColumn(i18n.tr("Source"));
        tableView.getColumns().add(colOrigine);
        colOrigine.setCellValueFactory(
                    new PropertyValueFactory<sitedownl, String>("origine")); 
        
        // Upload button
        colButton = new TableColumn<>("");
        colButton.setSortable(false);
         
        colButton.setCellValueFactory(
                new Callback<TableColumn.CellDataFeatures<sitedownl, Boolean>, ObservableValue<Boolean>>() {
            @Override
            public ObservableValue<Boolean> call(TableColumn.CellDataFeatures<sitedownl, Boolean> p) {
                return new SimpleBooleanProperty(p.getValue() != null);
            }
        });
 
        colButton.setCellFactory(
                new Callback<TableColumn<sitedownl, Boolean>, TableCell<sitedownl, Boolean>>() {
 
            @Override
            public TableCell<sitedownl, Boolean> call(TableColumn<sitedownl, Boolean> p) {
                return new ButtonCell();
            }
         
        });
        tableView.getColumns().add(colButton); 
        colPays.prefWidthProperty().bind(tableView.widthProperty().multiply(0.3));
        colRegion.prefWidthProperty().bind(tableView.widthProperty().multiply(0.3));
        colOrigine.prefWidthProperty().bind(tableView.widthProperty().multiply(0.15));
        colButton.prefWidthProperty().bind(tableView.widthProperty().multiply(0.25));
        colPays.setResizable(false);
        colRegion.setResizable(false);
        colOrigine.setResizable(false);
        colButton.setResizable(false);
        tableView.setItems(dataObs);
        
        Group root = new Group();
        VBox vBox = new VBox(8);            
        vBox.setPadding(new Insets(20));
        //  vBox.setSpacing(10); 
        //  vBox.setPadding(new Insets(10, 10, 10, 10));
        vBox.getChildren().add(tableView);
        root.getChildren().add(vBox);
        subStage.setScene(new Scene(root, 500, 400));
        subStage.showAndWait();        
    }
    
    //Define the button cell
    private class ButtonCell extends TableCell<sitedownl, Boolean> {
        final Button cellButton = new Button(i18n.tr("Download"));
         
        ButtonCell(){
             
            cellButton.setOnAction(new EventHandler<ActionEvent>(){
 
                @Override
                public void handle(ActionEvent t) {
                    int selectdIndex = getTableRow().getIndex();
                    sitedownl selectedFile = (sitedownl)tableView.getItems().get(selectdIndex);
                    downInstall(selectedFile.getFichier());
                }
            });
        }
 
        //Display button if the row is not empty
        @Override
        protected void updateItem(Boolean t, boolean empty) {
            super.updateItem(t, empty);
            if(!empty){
                setGraphic(cellButton);
            }
        }
    } 
    
    private void downInstall(String fileName) {
        String sUrl = privateData.sitesUrl.toString()+"/"+fileName;
        String tmpUpdateFiles = tempacess.getTemPath(null);
        //String tmpUpdateFiles = "/Users/gil/Documents";        
        String msg = "IMPORT CSV";  // It's a label, text will be translated in myLoad        
        webdown myLoad = new webdown(sUrl,tmpUpdateFiles, i18n, msg);
        if (myLoad.isDownSuccess()) {
            File downFile = new File(myLoad.getDownPath());
            dbImport myImport = new dbImport(myConfig, i18n, null);
            myImport.importCsv(downFile);
        }
    }
    
}
