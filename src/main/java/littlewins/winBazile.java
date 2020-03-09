/*
 * Copyright Gil THOMAS
 * This file forms an integral part of Logfly project
 * See the LICENSE file distributed with source code
 * for details of Logfly licence project
 */
package littlewins;

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
import javafx.scene.control.TextArea;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Callback;
import model.baziledownl;
import model.sitedownl;
import org.xnap.commons.i18n.I18n;
import settings.configProg;
import systemio.textdownload;

/**
 *
 * @author gil
 */
public class winBazile {
    
    private TableView tableView = new TableView();    
    private TableColumn<baziledownl, String> colFileName;   
    private TableColumn<baziledownl, String> colTrans;    
    private TableColumn<baziledownl, String> colOrigin;   
    private TableColumn colButton;    
    private TextArea taDescritption;
    
    private ObservableList<baziledownl> dataObs = FXCollections.observableArrayList();
    
    private String dataList;
    private I18n i18n; 
    private configProg myConfig;
    private boolean successLoad;
    private File loadFile;
    
    //https://java-buddy.blogspot.com/2013/02/example-to-apply-font.html

    public winBazile(configProg pConfig, I18n pI18n)  {
        myConfig = pConfig;        
        this.i18n = pI18n;
        textdownload downlist = new textdownload(2);        
        dataList = downlist.askList();        
        fillData();
        showWin();
    } 
    
    private void fillData() {
        String RC = "\n";
        String[] lines = dataList.split(RC);
        try {
            // first line (descriptor) is skipped
            for (int i = 1; i < lines.length; i++) {
                String[]lgBazile = lines[i].split(";");
                if (lgBazile.length > 3) {
                      baziledownl fileOA = new baziledownl(lgBazile[0],lgBazile[1],lgBazile[2],lgBazile[3]);                                          dataObs.add(fileOA);
                }
          }          
        } catch (Exception e) {
            
        }                              
    }  
    
    private void showWin() {
        Stage subStage = new Stage();

        subStage.setTitle("http://pascal.bazile.free.fr/");
    //    tableView.prefHeightProperty().bind(subStage.heightProperty());

        colFileName = new TableColumn(i18n.tr("File name"));
        tableView.getColumns().add(colFileName);
        colFileName.setCellValueFactory(
                    new PropertyValueFactory<baziledownl, String>("filename"));  
                
        colTrans = new TableColumn(i18n.tr("Processing"));
        tableView.getColumns().add(colTrans);
        colTrans.setCellValueFactory(
                    new PropertyValueFactory<baziledownl, String>("dateTrans"));  
        
        colOrigin = new TableColumn(i18n.tr("Origin"));
        tableView.getColumns().add(colOrigin);
        colOrigin.setCellValueFactory(
                    new PropertyValueFactory<baziledownl, String>("dateOrigin")); 
        
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
                return new winBazile.ButtonCell();
            }
         
        });
        tableView.getColumns().add(colButton); 
        colFileName.prefWidthProperty().bind(tableView.widthProperty().multiply(0.50));
        colTrans.prefWidthProperty().bind(tableView.widthProperty().multiply(0.15));
        colOrigin.prefWidthProperty().bind(tableView.widthProperty().multiply(0.15));
        colButton.prefWidthProperty().bind(tableView.widthProperty().multiply(0.20));
        colFileName.setResizable(false);
        colTrans.setResizable(false);
        colOrigin.setResizable(false);
        colButton.setResizable(false);
        tableView.setItems(dataObs);
        
        taDescritption = new TextArea();
        taDescritption.setWrapText(true);
        taDescritption.setEditable(false);
        taDescritption.setText("zz zzzazpozp japqa^^ua japzâzarj flsfnfqshpq qhpHEP EH  EL ;LHE PEH PHEP    HJ  LNCSLWNQLD");
        
        Group root = new Group();
        VBox vBox = new VBox();            
        //vBox.setPadding(new Insets(20));
          vBox.setSpacing(10); 
        //  vBox.setPadding(new Insets(10, 10, 10, 10));
        vBox.getChildren().addAll(taDescritption, tableView);
        root.getChildren().add(vBox);
        subStage.setScene(new Scene(root, 650, 400));
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
                    baziledownl selectedFile = (baziledownl)tableView.getItems().get(selectdIndex);
                    downInstall(selectedFile.getFilename());
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
        
    }
}
