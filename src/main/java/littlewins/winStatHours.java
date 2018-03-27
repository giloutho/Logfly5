/*
 * Copyright Gil THOMAS
 * This file forms an integral part of Logfly project
 * See the LICENSE file distributed with source code
 * for details of Logfly licence project
 */
package littlewins;

import java.util.Locale;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.xnap.commons.i18n.I18n;
import org.xnap.commons.i18n.I18nFactory;
/**
 *
 * @author gil
 */
public class winStatHours {
    
    // Localization
    private I18n i18n; 
    private TableView<hoursRecord> tableView = new TableView<>(); 
    private TableColumn<hoursRecord, String> colNum;     
    private TableColumn<hoursRecord, String> colName;   
    private TableColumn<hoursRecord, String> colHours;    
    private ObservableList<hoursRecord> hoursList = FXCollections.observableArrayList();
    
    public winStatHours(Locale currLocale)  {
        i18n = I18nFactory.getI18n("","lang/Messages",winPoints.class.getClass().getClassLoader(),currLocale,0);
    }
    
    public void showTable() {
        
        Stage subStage = new Stage();
        subStage.initModality(Modality.APPLICATION_MODAL);
       // tableView.prefHeightProperty().bind(subStage.heightProperty());
       // tableView.prefWidthProperty().bind(subStage.widthProperty());
        tableView.setEditable(false);
        
        colNum = new TableColumn();
        tableView.getColumns().add(colNum);
        colNum.setCellValueFactory(new PropertyValueFactory<hoursRecord, String>("fieldNum"));         
        colNum.setStyle( "-fx-alignment: CENTER-RIGHT;");
        
        colName = new TableColumn();
        tableView.getColumns().add(colName);
        colName.setCellValueFactory(new PropertyValueFactory<hoursRecord, String>("fieldName"));    
        
        colHours = new TableColumn();
        tableView.getColumns().add(colHours);
        colHours.setCellValueFactory(new PropertyValueFactory<hoursRecord, String>("fieldHours"));
        colHours.setStyle( "-fx-alignment: CENTER-RIGHT;  -fx-padding: 1 30 1 1;"); 
        
        colNum.prefWidthProperty().bind(tableView.widthProperty().multiply(0.10));
        colName.prefWidthProperty().bind(tableView.widthProperty().multiply(0.65));
        colHours.prefWidthProperty().bind(tableView.widthProperty().multiply(0.25));
        colNum.setResizable(false);        
        colName.setResizable(false);        
        colHours.setResizable(false);
        tableView.setItems(hoursList);
        
        StackPane root = new StackPane();
        //Group root = new Group();
        VBox vBox = new VBox();
        //vBox.setSpacing(10); 
        vBox.setPadding(new Insets(10, 10, 10, 10));
        vBox.getChildren().add(tableView);
        root.getChildren().add(vBox);
        subStage.setScene(new Scene(root, 350, 400));
        subStage.showAndWait();      
    }
    
    public void fillList(hoursRecord pRec) {
        hoursList.add(pRec);
    }   
    
    public class hoursRecord{

        private SimpleStringProperty fieldNum;
        private SimpleStringProperty fieldName;
        private SimpleStringProperty fieldHours;
    
        public hoursRecord(String pNum, String pName, String pHours){
            this.fieldNum = new SimpleStringProperty(pNum);
            this.fieldName = new SimpleStringProperty(pName);
            this.fieldHours = new SimpleStringProperty(pHours);            
        }
        
        public String getFieldNum() {
            return fieldNum.get();
        }        
    
        public String getFieldName() {
            return fieldName.get();
        }

        public String getFieldHours() {
            return fieldHours.get();
        }
    
  }          
    
}
