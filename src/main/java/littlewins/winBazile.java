/*
 * Copyright Gil THOMAS
 * This file forms an integral part of Logfly project
 * See the LICENSE file distributed with source code
 * for details of Logfly licence project
 */
package littlewins;

import dialogues.alertbox;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.util.Callback;
import model.baziledownl;
import org.xnap.commons.i18n.I18n;
import settings.configProg;
import settings.privateData;
import systemio.mylogging;
import systemio.textdownload;
import systemio.webdown;

/**
 *
 * @author gil
 */
public class winBazile {
        
    private ObservableList<baziledownl> dataObs = FXCollections.observableArrayList();
    
    private String dataList;
    private I18n i18n; 
    private StringBuilder sbError;
    private configProg myConfig;
    private boolean successLoad;
    private File downFile = null; 
    private Stage subStage;
    
    //https://java-buddy.blogspot.com/2013/02/example-to-apply-font.html

    public winBazile(configProg pConfig, I18n pI18n)  {
        myConfig = pConfig;        
        this.i18n = pI18n;
        textdownload downlist = new textdownload(2);        
        dataList = downlist.askList();          
        if (dataList != null) {
            fillData();
            showWin();
        } else {
            alertbox aError = new alertbox(myConfig.getLocale());
            aError.alertNumError(310);     // Can't get response from web service
            sbError = new StringBuilder("Can't get response from http://pascal.bazile.free.fr").append("\r\n");
            mylogging.log(Level.SEVERE, sbError.toString());              
        }
    } 

    public File getDownFile() {
        return downFile;
    }
        
    private void fillData() {
        //String RC = "\n";
        String RC = "\r\n";
        String[] lines = dataList.split(RC);
        if (lines.length > 0) {
            try {
                // first line (descriptor) is skipped
                for (int i = 1; i < lines.length; i++) {                
                    String[]lgBazile = lines[i].split(";");
                    if (lgBazile.length > 3) {
                          baziledownl fileOA = new baziledownl(lgBazile[0],lgBazile[1],lgBazile[2],lgBazile[3]);                                                   dataObs.add(fileOA);
                    }
              }          
            } catch (Exception e) {
                alertbox aError = new alertbox(myConfig.getLocale());
                aError.alertNumError(320);     // Unusable response from web service
                sbError = new StringBuilder("Unable to split txt file from http://pascal.bazile.free.fr");
                sbError.append("\r\n").append(dataList);
                mylogging.log(Level.SEVERE, sbError.toString());   
            }  
        }
    }  
    
    private void showWin() {
        subStage = new Stage();
        subStage.setTitle("http://pascal.bazile.free.fr/");
        Group root = new Group();
        Scene scene = new Scene(root, 550, 250, Color.WHITE);
        // create a grid pane
        GridPane gridpane = new GridPane();
        gridpane.setPadding(new Insets(5));
        gridpane.setHgap(10);
        gridpane.setVgap(10);
        
        VBox vData = new VBox();
        //hbAdresse.setPadding(new Insets(5, 12, 5, 12));
        vData.setSpacing(8);
        Label lbOrigin = new Label();
        lbOrigin.setPrefSize(150, 15);
        lbOrigin.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        //lbMail.setPadding(new Insets(5, 0, 0, 0));
        lbOrigin.setText("Date d'origine");

        Label dtOrigin = new Label();
        dtOrigin.setPrefSize(100, 15);
        //lbMail.setPadding(new Insets(5, 0, 0, 0));             
        
        Label lbTransfo = new Label();
        lbTransfo.setPrefSize(150, 15);
        lbTransfo.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        //lbMail.setPadding(new Insets(5, 0, 0, 0));
        lbTransfo.setText("Date de transformation");
        
        Label dtTransfo = new Label();
        dtTransfo.setPrefSize(100, 15);
        //lbMail.setPadding(new Insets(5, 0, 0, 0));
        
        TextArea txDesc = new TextArea();
        // Pour aller à la ligne et il y a plein d'autres possibilités
        txDesc.setWrapText(true);
        txDesc.setEditable(false);
        txDesc.setPrefSize(210, 100);
               
        ListView<baziledownl> bazListView = new ListView<baziledownl>(dataObs);
        bazListView.setPrefWidth(300);
        bazListView.setPrefHeight(150); 
        
        bazListView
            .setCellFactory(new Callback<ListView<baziledownl>, ListCell<baziledownl>>() {

              public ListCell<baziledownl> call(ListView<baziledownl> param) {
                final ListCell<baziledownl> cell = new ListCell<baziledownl>() {
                  @Override
                  public void updateItem(baziledownl item, boolean empty) {
                    super.updateItem(item, empty);
                    if (item != null) {
                        setText(item.getFilename());
                    }
                  }
                }; // ListCell
                return cell;
              }
            }); // setCellFactory

        gridpane.add(bazListView, 0, 1); 
            
        bazListView.getSelectionModel().selectedItemProperty()
            .addListener(new ChangeListener<baziledownl>() {
              public void changed(ObservableValue<? extends baziledownl> observable,
                  baziledownl oldValue, baziledownl newValue) {
                        dtOrigin.setText(formatDate(newValue.getDateOrigin()));
                        dtTransfo.setText(formatDate(newValue.getDateTrans()));
                        txDesc.setText(newValue.getDescription()); 
                        formatDate(newValue.getDateOrigin());
              }
            });
        
        bazListView.getSelectionModel().select(0);
                
        HBox hbButton = new HBox();
        hbButton.setAlignment(Pos.CENTER);
        Button btDownl = new Button("Télécharger");
        btDownl.setOnAction((event) -> {
            baziledownl selectedFile = bazListView. getSelectionModel(). getSelectedItem();
            downInstall(selectedFile.getFilename());
        });    
        hbButton.getChildren().addAll(btDownl);        

        vData.getChildren().addAll(lbOrigin, dtOrigin, lbTransfo, dtTransfo, txDesc,hbButton);         
                
        gridpane.add(vData, 1, 1);    
        

        root.getChildren().add(gridpane);  
        
        subStage.setScene(scene);
        
        subStage.showAndWait();           
    }
    
    private String formatDate(String sDate) {
        String res = sDate;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        SimpleDateFormat sdf2 = new SimpleDateFormat("dd/MM/yyyy");
        try {
            Date  date = sdf.parse(sDate);
            sDate = sdf2.format(date);
        } catch (Exception e) {
            
        } 
        
        return sDate;
    }
    
    private void downInstall(String fileName) {
        String sUrl = privateData.bazileUrl.toString()+"/"+fileName;
        String downlPath = myConfig.getPathW();
        File f = new File(downlPath+File.separator+fileName);
        if (f.exists()) f.delete();     
        webdown myLoad = new webdown(sUrl,downlPath, i18n, "");
        if (myLoad.isDownSuccess()) {
            downFile = new File(myLoad.getDownPath());
            subStage.close();
        }        
    }
}
