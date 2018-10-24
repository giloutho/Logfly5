/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.logfly.littlewins;

import com.sun.javafx.scene.traversal.Direction;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.regex.Pattern;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;

import org.logfly.airspacelib.AirspaceCategory;
import org.logfly.model.airdraw;
import org.xnap.commons.i18n.I18n;

/**
 *
 * @author gil
 * 
 *  Editable tableview designed with help of
 *    http://java-buddy.blogspot.com/2013/03/javafx-editable-multi-column-tableview.html
 *    https://gist.github.com/haisi/0a82e17daf586c9bab52
 * 
 */
public class winAirDraw {
    
    private I18n i18n; 
    private TableView<airdraw> tableView = new TableView<>();
    private ObservableList<airdraw> drawlist = FXCollections.observableArrayList();
    private String errMsg;
    private StringBuilder sbDrawing;
    private double m2ft = 0.3048;
    
    public winAirDraw(I18n pI18n,ObservableList<airdraw> pList) {  
        i18n = pI18n;
        drawlist = pList;
        showWin();
    }        
    
    private void showWin() {
        Stage subStage = new Stage();
        
        subStage.setTitle(i18n.tr("[Enter] pour valider une saisie"));
        tableView.setEditable(true);
        Callback<TableColumn<airdraw, String>, TableCell<airdraw, String>> cellFactory
                = (TableColumn<airdraw, String> param) -> new stringEditingCell(); 
        Callback<TableColumn<airdraw, String>, TableCell<airdraw, String>> choiceFactory
                = (TableColumn<airdraw, String> param) -> new choiceEditingCell();    
        Callback<TableColumn<airdraw, Integer>, TableCell<airdraw, Integer>> intFactory
                = (TableColumn<airdraw, Integer> param) -> new intEditingCell();
        
        TableColumn columnName = new TableColumn(i18n.tr("Nom"));
        columnName.setCellValueFactory(new PropertyValueFactory<airdraw,String>("name"));
        columnName.setMinWidth(120);       
        columnName.setCellFactory(cellFactory);
        columnName.setOnEditCommit(
            new EventHandler<TableColumn.CellEditEvent<airdraw, String>>() {

                @Override public void handle(TableColumn.CellEditEvent<airdraw, String> t) {
                    ((airdraw)t.getTableView().getItems().get(
                            t.getTablePosition().getRow())).setName(t.getNewValue());
                }
        });        
                       
        TableColumn columnCat = new TableColumn(i18n.tr("Classe"));
        columnCat.setCellValueFactory(new PropertyValueFactory<airdraw,String>("category"));
        columnCat.setMinWidth(40);   
        columnCat.setCellFactory(choiceFactory);  
        columnCat.setOnEditCommit(
            new EventHandler<TableColumn.CellEditEvent<airdraw, String>>() {

                @Override public void handle(TableColumn.CellEditEvent<airdraw, String> t) {
                    ((airdraw)t.getTableView().getItems().get(
                            t.getTablePosition().getRow())).setCategory(t.getNewValue());
                }
        });         
        
        TableColumn columnFloor = new TableColumn(i18n.tr("Plancher"));
        columnFloor.setStyle( "-fx-alignment: CENTER-RIGHT;");
        columnFloor.setCellValueFactory(
                new PropertyValueFactory<airdraw,Integer>("floor"));
        columnFloor.setMinWidth(50);     
        columnFloor.setCellFactory(intFactory);
        columnFloor.setOnEditCommit(
            new EventHandler<TableColumn.CellEditEvent<airdraw, Integer>>() {

                @Override public void handle(TableColumn.CellEditEvent<airdraw, Integer> t) {
                    ((airdraw)t.getTableView().getItems().get(
                            t.getTablePosition().getRow())).setFloor(t.getNewValue());
                }
            });        
        
        TableColumn columnCeiling = new TableColumn(i18n.tr("Plafond"));
        columnCeiling.setStyle( "-fx-alignment: CENTER-RIGHT;");        
        columnCeiling.setCellValueFactory(
                new PropertyValueFactory<airdraw,Integer>("ceiling"));
        columnCeiling.setMinWidth(50);     
        columnCeiling.setCellFactory(intFactory);
        columnCeiling.setOnEditCommit(
            new EventHandler<TableColumn.CellEditEvent<airdraw, Integer>>() {

                @Override public void handle(TableColumn.CellEditEvent<airdraw, Integer> t) {
                    ((airdraw)t.getTableView().getItems().get(
                            t.getTablePosition().getRow())).setCeiling(t.getNewValue());
                }
            });      
        
        tableView.setItems(drawlist);
        tableView.getColumns().addAll(columnName, columnCat, columnFloor, columnCeiling);

        HBox buttonBar = new HBox();
        buttonBar.setPadding(new Insets(6));
        buttonBar.setSpacing(5);
        buttonBar.setAlignment(Pos.CENTER_RIGHT);
        Button btValid = new Button(i18n.tr("Valider"));
        btValid.setOnAction((event) -> {
            if (checkTable()) {
                saveOnDisk();
                subStage.close();
            } else
                System.out.println("Erreur : "+errMsg);
        });
        Button btCancel = new Button(i18n.tr("Annuler"));
        btCancel.setOnAction((event) -> {

            subStage.close();
        });
        buttonBar.getChildren().addAll(btCancel, btValid);
        
        VBox vBox = new VBox();
        vBox.setSpacing(10);
        vBox.getChildren().addAll(tableView,buttonBar);
        StackPane subRoot = new StackPane();
        subRoot.getChildren().add(vBox); 

        Scene secondScene = new Scene(subRoot, 350, 300);
        // modal mode
        subStage.initModality(Modality.APPLICATION_MODAL);        
        subStage.setScene(secondScene);     
        subStage.showAndWait();        
    }
    
    private boolean checkTable() {
     
        String sCategory;
        String sName;
        int iFloor;
        int iCeiling;
        String sOA;
        boolean validInput = false;
        sbDrawing = new StringBuilder();

        for (int i = 0; i < drawlist.size(); i++) {
            sCategory = drawlist.get(i).getCategory();
            sName = drawlist.get(i).getName();
            iFloor = (int) (drawlist.get(i).getFloor()/m2ft);
            iCeiling  = (int) (drawlist.get(i).getCeiling()/m2ft);
            sOA = drawlist.get(i).getOatext();
            if(sCategory != null) {
                if (sName != null) {
                    if (iCeiling > iFloor) {
                        validInput = true;
                        sbDrawing.append("AC ").append(sCategory).append("\r\n");
                        sbDrawing.append("AN ").append(sName).append("\r\n");
                        sbDrawing.append("AH ").append(String.valueOf(iCeiling)).append("\r\n");
                        sbDrawing.append("AL ").append(String.valueOf(iFloor)).append("\r\n");
                        sbDrawing.append(sOA).append("\r\n");                        
                    } else {                        
                        errMsg = i18n.tr("Le plafond doit être supérieur au plancher");
                        validInput = false;
                        break;
                    }
                } else {
                    errMsg = i18n.tr("Le nom ne doit pas être nul");
                    validInput = false;
                    break;
                }
            } else {
                errMsg = i18n.tr("La classe ne doit pas être nulle");
                validInput = false;
                break;
            }
        }         

        return validInput;
    }
    
    private void saveOnDisk() {
        int res = -1;
        
        StringBuilder sbExp = new StringBuilder();
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        String dayDate = sdf.format(new java.util.Date());
        sbExp.append("**********************************************************************\r\n");
        sbExp.append("*                                                                    *\r\n");
        sbExp.append("*                       OPEN AIR TEST FILE                           *\r\n");
        sbExp.append("*                      Generated by LOGFLY                           *\r\n");
        sbExp.append("*                      ").append(dayDate).append("                          *\r\n");
        sbExp.append("*                                                                    *\r\n");
        sbExp.append("**********************************************************************\r\n\r\n");   
        sbExp.append(sbDrawing.toString());
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter(i18n.tr("Format OpenAir"), "*.txt"));              
        File selectedFile = fileChooser.showSaveDialog(null);        
        if(selectedFile != null){
            try {
                FileWriter fileWriter = null;
                fileWriter = new FileWriter(selectedFile);
                fileWriter.write(sbExp.toString());
                fileWriter.close();
                res = 0;
            } catch (IOException ex) {
                res = 2;
            }
        }         
        
    }
        
            
    

    class stringEditingCell extends TableCell<airdraw, String> {

        private TextField textField;

        private stringEditingCell() {
        }

        @Override
        public void startEdit() {
            if (!isEmpty()) {
                super.startEdit();
                createTextField();
                setText(null);
                setGraphic(textField);
                textField.selectAll();
            }
        }

        @Override
        public void cancelEdit() {
            super.cancelEdit();

            setText((String) getItem());
            setGraphic(null);
        }

        @Override
        public void updateItem(String item, boolean empty) {
            super.updateItem(item, empty);

            if (empty) {
                setText(item);
                setGraphic(null);
            } else {
                if (isEditing()) {
                    if (textField != null) {
                        textField.setText(getString());
//                        setGraphic(null);
                    }
                    setText(null);
                    setGraphic(textField);
                } else {
                    setText(getString());
                    setGraphic(null);
                }
            }
        }

        private void createTextField() {
            textField = new TextField(getString());
            textField.setMinWidth(this.getWidth() - this.getGraphicTextGap() * 2);
            textField.setOnAction((e) -> commitEdit(textField.getText()));
            textField.focusedProperty().addListener((ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) -> {
                if (!newValue) {
                    System.out.println("Commiting " + textField.getText());
                    commitEdit(textField.getText());
                }
            });
        }

        private String getString() {
            return getItem() == null ? "" : getItem();
        }
    }

    class intEditingCell extends TableCell<airdraw, Integer> {
        private TextField textField;
        private Pattern validIntText = Pattern.compile("-?(\\d*)");
         
        public intEditingCell() {}
         
        @Override
        public void startEdit() {
            super.startEdit();
             
            if (textField == null) {
                createTextField();
            }
             
            setGraphic(textField);
            setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
            textField.selectAll();
        }
         
        @Override
        public void cancelEdit() {
            super.cancelEdit();
             
            setText(String.valueOf(getItem()));
            setContentDisplay(ContentDisplay.TEXT_ONLY);
        }
         
        @Override
        public void updateItem(Integer item, boolean empty) {
            super.updateItem(item, empty);
          
            if (empty) {
                setText(null);
                setGraphic(null);
            } else {
                if (isEditing()) {
                    if (textField != null) {
                        textField.setText(getString());
                    }
                     
                    setGraphic(textField);
                    setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
                } else {
                    setText(getString());
                    setContentDisplay(ContentDisplay.TEXT_ONLY);
                }
            }
        }
         
        private void createTextField() {
            textField = new TextField(getString());
            textField.setMinWidth(this.getWidth() - this.getGraphicTextGap()*2);
            textField.setOnKeyPressed(new EventHandler<KeyEvent>() {              
                @Override
                public void handle(KeyEvent t) {
                    if (t.getCode() == KeyCode.ENTER) {
                        String sInput = textField.getText();
                         if (validIntText.matcher(sInput).matches()) {
                            commitEdit(Integer.parseInt(sInput));
                        } else {             
                            java.awt.Toolkit.getDefaultToolkit().beep();
                            startEdit();
                        }
                    } else if (t.getCode() == KeyCode.ESCAPE) {
                        cancelEdit();
                    }
                }
            });
        }
      
        private String getString() {
            return getItem() == null ? "" : getItem().toString();
        }
    } 
    
    private class choiceEditingCell extends TableCell<airdraw, String> {
        
        ChoiceBox<String> catBox;

        private choiceEditingCell() {
        }

        @Override
        public void startEdit() {
            if (!isEmpty()) {
                super.startEdit();
                createChoiceBox();
                setText(null);
                setGraphic(catBox);
            }
        }

        @Override
        public void cancelEdit() {
            super.cancelEdit();

            setText((String) getItem());
            setGraphic(null);
        }

        @Override
        public void updateItem(String item, boolean empty) {
            super.updateItem(item, empty);
            if (item == null || empty) {
                setText(null);
            } else {
                setText(item);
            }
        }

        private void createChoiceBox() {
            catBox = new ChoiceBox<>();
            for (AirspaceCategory airCat : AirspaceCategory.values()) {
                catBox.getItems().add(airCat.toString());
            }
            catBox.setMinWidth(this.getWidth() - this.getGraphicTextGap() * 2);
           // catBox.getSelectionModel().selectFirst();  // creates problems
            catBox.setOnAction((e) -> {
                System.out.println("Committed: " + catBox.getSelectionModel().getSelectedItem());
                commitEdit(catBox.getSelectionModel().getSelectedItem());
            });
        }
    }        
    
}
