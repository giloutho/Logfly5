/*
 * Copyright Gil THOMAS
 * This file forms an integral part of Logfly project
 * See the LICENSE file distributed with source code
 * for details of Logfly licence project
 */
package littlewins;

import dialogues.ProgressForm;
import dialogues.alertbox;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.xnap.commons.i18n.I18n;
import settings.configProg;

/**
 *
 * @author gil
 */
public class winCsv {

    private Stage subStage;
     
    private I18n i18n; 
    private configProg myConfig; 
    private ChoiceBox chbYears;
    private CheckBox chIGC;
    private ObservableList <String> dataYear; 
    private String RC = "\r\n";
    private String exportPath;
    private int nbFlights;
    private boolean exportOK = false;
     
    public winCsv(configProg pConfig, I18n pI18n) {
        myConfig = pConfig;        
        this.i18n = pI18n;       
        dataYear = FXCollections.observableArrayList();
        showWin();            
    }
    
    private void showWin() {
        subStage = new Stage();   
        subStage.initModality(Modality.APPLICATION_MODAL);        
        Label lbYears = new Label("Periods");   // Years déjà pris et dtraduit pas "ans" donc pas top
        lbYears.setMinWidth(50);
        
        chbYears = new ChoiceBox(); 
        
        chbYears.setMinWidth(100);
        fillChbYears();
        
        HBox hBox1 = new HBox();
        
        hBox1.setSpacing(10);
        hBox1.setMaxHeight(25);
        // top, right, bottom and left
        hBox1.setPadding(new Insets(5, 12, 5, 12));
        hBox1.setAlignment(Pos.CENTER);
        hBox1.getChildren().addAll(lbYears, chbYears);        
        
        chIGC = new CheckBox();   
        chIGC.setText(i18n.tr("Include tracks"));
        chIGC.setSelected(false);                         
                
        HBox hBox2 = new HBox();
        hBox2.getChildren().addAll(chIGC);
        hBox2.setMaxHeight(25);
        // top, right, bottom and left
        hBox2.setPadding(new Insets(5, 12, 5, 50));
        hBox2.setAlignment(Pos.CENTER);        
        
        // Boutons
        HBox buttonBar = new HBox();
        buttonBar.setPadding(new Insets(6));
        buttonBar.setSpacing(20);
        buttonBar.setAlignment(Pos.CENTER);
        Button btSend = new Button(i18n.tr("Export"));
        btSend.setOnAction((event) -> {
            exportCsv();
        });
        Button btClose = new Button(i18n.tr("Cancel"));
        btClose.setOnAction((event) -> {            
            subStage.close();
        });
        buttonBar.getChildren().addAll(btClose, btSend );   
        
        // La Vbox qui va contenir chacun des éléments horizontaux définis ci dessus
        final VBox vbox = new VBox();
        vbox.setPadding(new Insets(20));
        vbox.setSpacing(20);      
        vbox.getChildren().addAll(hBox1,hBox2,buttonBar);
        
        // Mise en place de la fenêtre
        subStage = new Stage();
        
        StackPane subRoot = new StackPane();
        subRoot.getChildren().add(vbox);
        
        Scene secondScene = new Scene(subRoot, 200, 150);

        // modal mode
        subStage.initModality(Modality.APPLICATION_MODAL);        
        subStage.setScene(secondScene);     
        subStage.showAndWait();                    
    }
    
    private void fillChbYears(){
        Statement stmt = null;
        ResultSet rsYear = null;                
        try {
            stmt = myConfig.getDbConn().createStatement();                        
            // We search years in the logbook
            rsYear = stmt.executeQuery("SELECT strftime('%Y',V_date) FROM Vol GROUP BY strftime('%Y',V_date) ORDER BY strftime('%Y',V_date) DESC");
            if (rsYear != null)  {             
                while (rsYear.next()) {
                    dataYear.add(rsYear.getString(1));
                }
                dataYear.add(i18n.tr("All"));
                // Year choicebox initialization
                chbYears.setItems(dataYear);
                chbYears.getSelectionModel().select(0);                                                                                                                           
            }

        } catch ( Exception e ) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(i18n.tr("No flights in the logbook"));            
            String s = e.getClass().getName() + ": " + e.getMessage();
            alert.setContentText(s);
            alert.showAndWait();                      
            subStage.close();
        }  finally {
            try{
                rsYear.close(); 
                stmt.close();
            } catch(Exception e) { } 
        }        
    }
    
    private void genCsv() {
        String dbName;     
        String backupName;    
        String yearFilter;
        boolean expIGC = false;
        String yearRange = (String) chbYears.getSelectionModel().getSelectedItem();        
        boolean withIGC = chIGC.isSelected();
        LocalDateTime currentTime = LocalDateTime.now();
        DateTimeFormatter myFormatter = DateTimeFormatter.ofPattern("yyMMdd_HHmmss");   
        String dateName = currentTime.format(myFormatter);        
        String[] sDb = myConfig.getDbName().split("\\.");
        if (sDb.length > 0) {
            dbName = sDb[0];            
        } else
            dbName = myConfig.getDbName();        
        if (yearRange.equals(i18n.tr("All"))) {
            backupName = dbName+"_"+dateName;
            yearFilter = null;
        } else {
            backupName = dbName+"_"+yearRange+"_"+dateName;
            yearFilter = yearRange;
        }
        try {
            String expIGCPath = null;
            if (checkExportFolder()) {
                if (withIGC) {
                    // a subfolder is created for track files
                    File subFolder = new File(exportPath+File.separator+backupName);
                    if (subFolder.mkdirs()) {
                       expIGC = true;
                       expIGCPath = subFolder.getAbsolutePath();
                    }  
                }
                String csvBody = genDataCsv(yearFilter, expIGC, expIGCPath);
                if (csvBody != null) {
                    FileWriter fileWriter = new FileWriter(exportPath+File.separator+backupName+".csv");
                    fileWriter.write(csvBody);
                    fileWriter.close();
                    exportOK = true;
                }
            }
        } catch (Exception e) {
            exportOK = false;
        }
    }
    
    private void endExport()  {
        alertbox aError = new alertbox(myConfig.getLocale());
        if (exportOK) {
            aError.alertNumError(0);   // Successful operation 
            subStage.close();
        } else {            
            aError.alertInfo(i18n.tr("Error while exporting data"));  
        }
    }    
    
    public void exportCsv()  {
        
                
        ProgressForm pForm = new ProgressForm();
           
        Task<Void> task = new Task<Void>() {
            @Override
            public Void call() throws InterruptedException { 
                genCsv();
                return null ;                
            }
        
        };
        // binds progress of progress bars to progress of task:
        pForm.activateProgressBar(task);

        // task is finished 
        task.setOnSucceeded(event -> {
            pForm.getDialogStage().close();
            endExport();
        });

        pForm.getDialogStage().show();

        Thread thread = new Thread(task);
        thread.start();        
    }        
        
    private boolean checkExportFolder() {
        boolean folderOK = false;
        String pathExport;
        switch (myConfig.getOS()) {
            case WINDOWS : 
                pathExport = System.getProperty("user.home")+"\\Documents\\Logfly\\Export";
                break;
            case MACOS :
                pathExport = System.getProperty("user.home")+"/Documents/Logfly/Export";
                break;
            case LINUX :
                pathExport = System.getProperty("user.home")+"/.logfly/Export";
                break;
            default:                     
                pathExport = null;
        }
        if (pathExport != null) {
            File f = new File(pathExport);
            if(f.exists() && f.isDirectory()) {
                exportPath = f.getAbsolutePath();
                folderOK = true;
            } else {
                File dir = new File(pathExport);
                if (dir.mkdirs()) {
                    exportPath = dir.getAbsolutePath();
                    folderOK = true;
                }                   
            }
        } else {
            folderOK = false;
        }
        
        return folderOK;
    }
    
    private String genDataCsv(String yearFiltre,boolean expIGC, String expIGCPath) {
        Statement stmt = null;
        ResultSet rs = null;
        String sReq = null;
        nbFlights = 0;
        StringBuilder sbRes = new StringBuilder();
        if (yearFiltre != null) {
            sReq = "SELECT * FROM Vol WHERE V_Date >= '"+yearFiltre+"-01-01 00:01' AND V_Date <= '"+yearFiltre+"-12-31 23:59' ORDER BY V_Date DESC";
        } else {
            sReq = "SELECT * FROM Vol ORDER BY V_Date DESC";
        }
        try {
            stmt = myConfig.getDbConn().createStatement();
            rs = stmt.executeQuery(sReq);
            if (rs != null)  { 
                if (expIGC) sbRes.append(i18n.tr("IGC File"));
                sbRes.append(i18n.tr("Date")).append(";");
                sbRes.append(i18n.tr("Hour")).append(";");
                sbRes.append("UTC").append(";");
                sbRes.append(i18n.tr("Duration")).append(";");
                sbRes.append(i18n.tr("Site")).append(";");
                sbRes.append(i18n.tr("Country")).append(";");
                sbRes.append(i18n.tr("Alt")).append(";");
                sbRes.append(i18n.tr("Latitude")).append(";");
                sbRes.append(i18n.tr("Longitude")).append(";");
                sbRes.append(i18n.tr("Glider")).append(";");
                sbRes.append(i18n.tr("Comment")).append(RC);
                while (rs.next()) {   
                    if (expIGC) {
                        String igcData = rs.getString("V_IGC");
                        if ( igcData != null && !rs.getString("V_IGC").equals("")) {
                            // A track name is defined with date
                            String s1 = rs.getString("V_Date").replaceAll("-","_");
                            String s2 = s1.replaceAll(":","_");
                            String finalName = s2.replaceAll("\\s","_");
                            try {
                                FileWriter fileWriter = new FileWriter(expIGCPath+File.separator+finalName+".igc");
                                fileWriter.write(igcData);
                                fileWriter.close();
                                sbRes.append(finalName).append(".IGC;");
                            } catch (IOException ex) {
                                sbRes.append("I/O error").append(";");
                            }
                        } else {
                            sbRes.append(";");
                        }
                    }                                                 
                    sbRes.append(rs.getString("V_Date").substring(0, 11)).append(";");
                    sbRes.append(rs.getString("V_Date").substring(12)).append(";");
                    sbRes.append(rs.getString("UTC")).append(";");
                    sbRes.append(rs.getString("V_sDuree")).append(";");  
                    sbRes.append(rs.getString("V_Site")).append(";");
                    sbRes.append(rs.getString("V_Pays")).append(";");
                    sbRes.append(rs.getString("V_AltDeco")).append(";"); 
                    sbRes.append(rs.getString("V_LatDeco")).append(";"); 
                    sbRes.append(rs.getString("V_LongDeco")).append(";"); 
                    sbRes.append(rs.getString("V_Engin")).append(";");
                    sbRes.append(rs.getString("V_Commentaire")).append(RC);   
                    nbFlights++;
                }            
            }            
        } catch ( Exception e ) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(i18n.tr("No flights in logbook"));            
            String s = e.getClass().getName() + ": " + e.getMessage();
            alert.setContentText(s);
            alert.showAndWait();                      
            System.exit(0);          
        } finally {
            try{
                rs.close(); 
                stmt.close();
            } catch(Exception e) { } 
        }   
        
        return sbRes.toString();
    }
    
}
