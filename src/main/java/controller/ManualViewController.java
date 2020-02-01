/*
 * Copyright Gil THOMAS
 * This file forms an integral part of Logfly project
 * See the LICENSE file distributed with source code
 * for details of Logfly licence project
 */
package controller;

import Logfly.Main;
import dialogues.alertbox;
import dialogues.dialogbox;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.logging.Level;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Paint;
import javafx.stage.Modality;
import javafx.stage.Stage;
import littlewins.winSiteChoice;
import model.Sitemodel;
import org.xnap.commons.i18n.I18n;
import org.xnap.commons.i18n.I18nFactory;
import settings.configProg;
import systemio.mylogging;

/**
 *
 * @author gil
 */
public class ManualViewController  {
    
    @FXML
    private DatePicker pickDate;
    @FXML
    private Spinner spinStartH;
    @FXML
    private Spinner spinStartMn;  
    @FXML
    private Spinner spinDurH;
    @FXML
    private Spinner spinDurMn; 
    @FXML 
    private Label lbDate;
    @FXML 
    private Label lbHDeco;
    @FXML 
    private Label lbFormat;
    @FXML 
    private Label lbDuree;
    @FXML 
    private Label lbVoile;
    @FXML 
    private ComboBox cbGliders; 
    @FXML 
    private Label lbDeco;
    @FXML
    private Label lbAlt;
    @FXML 
    private Label lbComment;  
    @FXML
    private Button btSites;
    @FXML
    private TextField txSite;
    @FXML
    private TextField txAlt;    
    @FXML
    private TextArea txComment;      
    @FXML
    private Button btDuplicate;
    @FXML
    private Button btOk;
    @FXML
    private Button btCancel;    
    
    // Reference to the main application.
    private Main mainApp;    
    private RootLayoutController rootController;  
    // Localization
    private I18n i18n; 
    private Sitemodel selectedSite;
    
    // Settings
    private configProg myConfig;  
    private StringBuilder sbError;
    private int editMode;
    private String idVol;
    
    private int iniStartH;
    private int iniStartMn; 
    private int iniDurH;
    private int iniDurMn;   
    private Paint valueWhite = Paint.valueOf("FFFFFF");    
    private String addStatusMsg;
    private String modStatusMsg;    
    private String initialDate;
    
    @FXML
    private void initialize() {    
        pickDate.setOnAction(event -> {
            LocalDate date = pickDate.getValue();
            System.out.println("Selected date: " + date);
        });
        // Init spinner décollage-heure
        iniStartH = 12;
        SpinnerValueFactory<Integer> factoryStartH = new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 23, iniStartH); 
        spinStartH.setValueFactory(factoryStartH);        
        // Init spinner décollage-minutes
        iniStartMn = 30;
        SpinnerValueFactory<Integer> factoryStartMn = new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 59, iniStartMn); 
        spinStartMn.setValueFactory(factoryStartMn);                  
        // Init spinner durée-heure
        iniDurH = 1;
        SpinnerValueFactory<Integer> factoryDurH = new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 18, iniDurH); 
        spinDurH.setValueFactory(factoryDurH);        
        // Init spinner durée-minutes
        iniDurMn = 30;
        SpinnerValueFactory<Integer> factoryDurMn = new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 59, iniDurMn); 
        spinDurMn.setValueFactory(factoryDurMn);  
        
        // if pickDate got focus with Orange color, we put white color when focus is lost
        pickDate.focusedProperty().addListener((ov, oldV, newV) -> {
            if (!newV) { 
                pickDate.setStyle("-fx-control-inner-background: #"+valueWhite.toString().substring(2));
           }
        });    
        cbGliders.focusedProperty().addListener((ov, oldV, newV) -> {
            if (!newV) { 
                cbGliders.setStyle("-fx-control-inner-background: #"+valueWhite.toString().substring(2));
           }
        });                                   
    }
    
    /**
     * It was first placed in code initialize() but from 5.022, it crashed. 
     * Normal, myConfig was not initalized. 
     * Why did it work until 5.021 ? Mystery !
     * Now it's called in setMyConfig, after myConfig initialization
     */
    private void fillCbGlider() {
        Statement stmt = null;
        ResultSet rs = null;
        
        try {
            stmt = myConfig.getDbConn().createStatement();                       
            rs = stmt.executeQuery("SELECT V_Engin,Count(V_ID) FROM Vol GROUP BY upper(V_Engin)");
            if (rs != null)  {             
                while (rs.next()) {
                    String gl = rs.getString(1);
                    if (gl != null && !gl.isEmpty() && !gl.equals("null")) {
                        cbGliders.getItems().add(gl);
                    }
                }
            }
        } catch (Exception e) {
            alertbox aError = new alertbox(myConfig.getLocale());
            aError.alertError(e.getMessage());                 
        } finally {
            try{
                rs.close(); 
                stmt.close();
            } catch(Exception e) { } 
        }        
    }
    
    @FXML
    private void askSiteList(ActionEvent event) {
        selectedSite = new Sitemodel();        
        winSiteChoice myWin = new winSiteChoice(myConfig,i18n, this);
    } 
    
    @FXML
    private void handleDup(ActionEvent event) {
        
        String sHour = String.format("%02d", spinStartH.getValue());
        String sMin = String.format("%02d", spinStartMn.getValue());
        String sSQL_Date = pickDate.getValue()+" "+sHour+":"+sMin+":00";        
        if (sSQL_Date.equals(initialDate)) {
            alertbox aError = new alertbox(myConfig.getLocale());
            aError.alertInfo(i18n.tr("Date and time unchanged"));             
        } else {
            if (checkFields()) {
                editMode = 0;
                dbUpdate();
                clearFields();
                btDuplicate.setVisible(false);
            }            
        }
           
    
    }
    
    @FXML
    private void handleOk(ActionEvent event) {
        // by default datePicker.getValue gives yyyy-mm-dd. 
        // Ok for db insertion... Pour une date il faudrait LocalDate date = pickDate.getValue();        
        if (checkFields()) {
            dbUpdate();            
            editMode = 0;   // Force for a new record
            clearFields();                            
        }
    }    
    
    @FXML
    private void handleCancel(ActionEvent event) {  
        
        boolean saisieEnCours;
        if (pickDate.getValue() == null || pickDate.getValue().toString().equals("")) {
            saisieEnCours = false;
        } else {
            saisieEnCours= true; 
        }
        if (!saisieEnCours) {
                if (cbGliders.getValue() == null || cbGliders.getValue().toString().equals("")) {
                    saisieEnCours = false;
                } else {
                    saisieEnCours = true;
                }
                if (!saisieEnCours) {
                    if (txSite.getText() == null || txSite.getText().equals("")) {
                        saisieEnCours = false;
                    } else {
                    saisieEnCours = true;
                }                
            }
        }
        if (saisieEnCours) {
            dialogbox dConfirm = new dialogbox(i18n);    
            if (dConfirm.YesNo("", i18n.tr("Cancel input")+" ?"))  { 
                saisieEnCours = false;
            }            
        } 
        if (!saisieEnCours) {
            exitController();
        }
    } 

    private void exitController() {
        mainApp.rootLayoutController.updateMsgBar("", false, 60);
        rootController.switchMenu(1);
        mainApp.showCarnetOverview();        
    }
    
    public void updateSelectedSite(Sitemodel pSelectedSite) {
        selectedSite = pSelectedSite;
        if (selectedSite.getIdSite() != null) { 
            if (selectedSite.getIdSite().equals("NEW")) {
                editSite();
            } else {
                txSite.setText(selectedSite.getNom());
                txAlt.setText(selectedSite.getAlt());
            }
        }      
    }    
    
    private void editSite() {
        try {                     
            // Load the fxml file and create a new stage for the popup dialog.
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(Main.class.getResource("/SiteForm.fxml")); 
            AnchorPane page = (AnchorPane) loader.load();

            Stage dialogStage = new Stage();
            dialogStage.initModality(Modality.WINDOW_MODAL);       
            dialogStage.initOwner(mainApp.getPrimaryStage());
            Scene scene = new Scene(page);
            dialogStage.setScene(scene);
            
            // Communication bridge between SiteForm and SiteView controllers
            SiteFormController controller = loader.getController();
            controller.setManualBridge(this);            
            controller.setDialogStage(dialogStage); 
            // controller is initialized for a new site with no launching coordinates         
            controller.setEditForm(myConfig,"",4);            
            // This window will be modal
            dialogStage.showAndWait();
                       
        } catch (IOException e) {
            sbError = new StringBuilder(this.getClass().getName()+"."+Thread.currentThread().getStackTrace()[1].getMethodName());
            sbError.append("\r\n").append(e.toString());
            mylogging.log(Level.SEVERE, sbError.toString());            
        }
    }      
    
    private boolean dbRead() {
        boolean res = false;
        Statement stmt = null;
        ResultSet rs = null;
        PreparedStatement pstmtSite = null;
        ResultSet rsSite = null;
        String siteName;
        
        String sReq = "SELECT V_Date,V_Duree,V_sDuree,V_AltDeco,V_Site,V_Engin,V_Commentaire FROM Vol WHERE V_ID = "+idVol;
        try {
            stmt = myConfig.getDbConn().createStatement();
            rs =  stmt.executeQuery(sReq);
            if (rs != null)  { 
                initialDate = rs.getString("V_Date");
                // la date au format 
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");            
                Date date = sdf.parse(initialDate);
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(date);
                Instant instant = date.toInstant();
                LocalDate ld = instant.atZone(ZoneOffset.UTC).toLocalDate();
                pickDate.setValue(ld);
                int totSec = rs.getInt("V_Duree");
                int totHr = totSec/3600;
                int totMin = (totSec - (totHr*3600))/60;                
                SpinnerValueFactory<Integer> factoryStartH = new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 23, calendar.get(Calendar.HOUR_OF_DAY)); 
                spinStartH.setValueFactory(factoryStartH);                
                SpinnerValueFactory<Integer> factoryStartMn = new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 59, calendar.get(Calendar.MINUTE)); 
                spinStartMn.setValueFactory(factoryStartMn);                  
                SpinnerValueFactory<Integer> factoryDurH = new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 18, totHr); 
                spinDurH.setValueFactory(factoryDurH);                
                SpinnerValueFactory<Integer> factoryDurMn = new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 59, totMin); 
                spinDurMn.setValueFactory(factoryDurMn);                          
                cbGliders.setValue(rs.getString("V_Engin"));
                siteName = rs.getString("V_Site");
                txSite.setText(siteName);
                txAlt.setText(rs.getString("V_AltDeco"));
                txComment.setText(rs.getString("V_Commentaire"));  
                // Site record 
                String sReqSite = "SELECT * FROM Site WHERE S_Nom = ?";
                pstmtSite = myConfig.getDbConn().prepareStatement(sReqSite);                      
                pstmtSite.setString(1, siteName); 
                rsSite = pstmtSite.executeQuery();
                if (rsSite.next()) {  
                    selectedSite = new Sitemodel();
                    selectedSite.setIdSite(rsSite.getString("S_ID"));
                    selectedSite.setNom(rsSite.getString("S_Nom"));
                    selectedSite.setLatitude(rsSite.getDouble("S_Latitude"));
                    selectedSite.setLongitude(rsSite.getDouble("S_Longitude"));
                    selectedSite.setPays(rsSite.getString("S_Pays"));
                    selectedSite.setAlt(rsSite.getString("S_Alti"));
                    res = true;
                } 
            }   
        } catch ( Exception e ) {
            alertbox aError = new alertbox(myConfig.getLocale());
            aError.alertError(e.getClass().getName() + ": " + e.getMessage());                          
        } finally {
            try{                
                rs.close(); 
                stmt.close();
                rsSite.close();
                pstmtSite.close();
            } catch(Exception e) { } 
        }       
        
        return res;
            
    }
    
    private void dbUpdate() {
        
        PreparedStatement pstmt = null;
        String sReq = "";   
        String Quote = "'";
        StringBuilder insertTableSQL = new StringBuilder();
        StringBuilder sbMsg =  new StringBuilder();
        try {
            String sHour = String.format("%02d", spinStartH.getValue());
            String sMin = String.format("%02d", spinStartMn.getValue());
            String sSQL_Date = pickDate.getValue()+" "+sHour+":"+sMin+":00";
            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
            LocalDate date = pickDate.getValue(); 
            int iHour = (int)spinDurH.getValue();
            int iMin = (int)spinDurMn.getValue();
            int iDuree = (iHour*3600) + (iMin*60);
            String sDuree = String.valueOf(iHour)+"h"+String.valueOf(iMin)+"mn";
            String sLong = String.valueOf(selectedSite.getLongitude());
            if (editMode == 1) {
                sReq = "UPDATE Vol SET V_Date=?, V_Duree=?, V_sDuree=?, V_LatDeco=?, V_LongDeco=?, V_AltDeco=?, V_Site=?, V_Pays=?, V_Engin=?, V_Commentaire= ? WHERE V_ID =?";  
                pstmt = myConfig.getDbConn().prepareStatement(sReq);
                pstmt.setString(1, sSQL_Date);                    
                pstmt.setLong(2, iDuree);
                pstmt.setString(3,sDuree);
                pstmt.setDouble(4,selectedSite.getLatitude());
                pstmt.setDouble(5,selectedSite.getLongitude());
                pstmt.setString(6,selectedSite.getAlt());
                pstmt.setString(7,selectedSite.getNom());
                pstmt.setString(8,selectedSite.getPays());
                pstmt.setString(9,cbGliders.getValue().toString());          
                pstmt.setString(10, txComment.getText());
                pstmt.setString(11,idVol);   
                pstmt.executeUpdate();                      
                sbMsg.append(modStatusMsg).append( ": ").append(dateFormatter.format(date)).append(" ");
                sbMsg.append(sDuree).append(" ").append(selectedSite.getNom());
                mainApp.rootLayoutController.updateMsgBar(sbMsg.toString(), true, 60);   
            } else if (editMode == 0) {
                insertTableSQL.append("INSERT INTO Vol (V_Date,V_Duree,V_sDuree,V_LatDeco,V_LongDeco,V_AltDeco,V_Site,V_Pays,V_IGC,UTC,V_Engin,V_Commentaire) VALUES");
                insertTableSQL.append("(?,?,?,?,?,?,?,?,?,?,?,?)");
                pstmt = myConfig.getDbConn().prepareStatement(insertTableSQL.toString());
                pstmt.setString(1, sSQL_Date);
                pstmt.setLong(2, iDuree);
                pstmt.setString(3,sDuree);
                pstmt.setDouble(4,selectedSite.getLatitude());
                pstmt.setDouble(5,selectedSite.getLongitude());
                pstmt.setString(6,selectedSite.getAlt());
                pstmt.setString(7,selectedSite.getNom());
                pstmt.setString(8,selectedSite.getPays());
                pstmt.setString(9,"");
                pstmt.setString(10,"0");
                pstmt.setString(11,cbGliders.getValue().toString());
                pstmt.setString(12, txComment.getText());
                pstmt.executeUpdate();   
                sbMsg.append(addStatusMsg).append( ": ").append(dateFormatter.format(date)).append(" ");
                sbMsg.append(sDuree).append(" ").append(selectedSite.getNom());
                mainApp.rootLayoutController.updateMsgBar(sbMsg.toString(), true, 60);            
            }             
        } catch (Exception e) {
            alertbox aError = new alertbox(myConfig.getLocale());
            aError.alertError(e.getClass().getName() + ": " + e.getMessage());  
            sbError = new StringBuilder(this.getClass().getName()+"."+Thread.currentThread().getStackTrace()[1].getMethodName());
            sbError.append("\r\n").append(e.getMessage());
            mylogging.log(Level.SEVERE, sbError.toString()); 
        }         
        
    }
    
    private void clearFields() {
        pickDate.setValue(null);
        SpinnerValueFactory<Integer> factoryStartH = new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 23, iniStartH); 
        spinStartH.setValueFactory(factoryStartH);        
        SpinnerValueFactory<Integer> factoryStartMn = new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 59, iniStartMn); 
        spinStartMn.setValueFactory(factoryStartMn);                  
        SpinnerValueFactory<Integer> factoryDurH = new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 18, iniDurH); 
        spinDurH.setValueFactory(factoryDurH);        
        SpinnerValueFactory<Integer> factoryDurMn = new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 59, iniDurMn); 
        spinDurMn.setValueFactory(factoryDurMn);          
        cbGliders.setValue(null);
        txSite.setText("");
        txAlt.setText("");
        txComment.setText("");        
    }    

    private boolean checkFields() {
        
        boolean res = false;
        Paint value0 = Paint.valueOf("FA6C04");
           
        if (pickDate.getValue() == null || pickDate.getValue().toString().equals("")) {
            res = false;
        } else {
            res = true;
        }
        
        if (res) {
            if (cbGliders.getValue() == null || cbGliders.getValue().toString().equals("")) {
                res = false;
            } else {
                res = true;
            }
            if (res) {
                if (txSite.getText() == null || txSite.getText().equals("")) {
                    res = false;
                } else {
                    res = true;
                }
                if (res) {
                    return res;
                } else {
                    txSite.setStyle("-fx-control-inner-background: #"+value0.toString().substring(2));
                    txSite.requestFocus();                          
                }     
            } else {
                cbGliders.setStyle("-fx-control-inner-background: #"+value0.toString().substring(2));
                cbGliders.requestFocus();                  
            }
        } else {
            pickDate.setStyle("-fx-control-inner-background: #"+value0.toString().substring(2));
            pickDate.requestFocus();              
        }
        
        return res;
    }
    
    
    /**
     * set the bridge with RootLayoutController  
     * @param rootlayout 
     */
    public void setRootBridge(RootLayoutController rootlayout, Main mainApp) {
        this.rootController = rootlayout; 
        this.mainApp = mainApp;         
    }    
        
    /**
     * recovered settings
     * @param mainConfig 
     */
    public void setMyConfig(int pModeEdit, configProg mainConfig, String pIdVol) {
        this.mainApp = mainApp; 
        // 0 -> new flight
        // 1 -> edit existing flight 
        this.editMode = pModeEdit;
        this.idVol = pIdVol;
        this.myConfig = mainConfig;
        i18n = myConfig.getI18n();
        winTraduction();
        fillCbGlider();
        rootController.updateMsgBar("", false,50);      
        if (pModeEdit == 1) {
            if (!dbRead()) {
                alertbox aError = new alertbox(myConfig.getLocale());
                StringBuilder sbMsg = new StringBuilder();
                sbMsg.append(i18n.tr("Site not found")).append(" - ").append("Unable to edit");
                aError.alertInfo(sbMsg.toString()); 
                exitController();
            } else {
                btDuplicate.setVisible(true);
                // Refresh menu option
                rootController.switchMenu(4);
            }
        } else {
            btDuplicate.setVisible(false);
        }
    }    
    
   /**
    * Translate labels of the window
    */
    private void winTraduction() {
        lbDate.setText(i18n.tr("Date"));
        lbHDeco.setText(i18n.tr("Take-off time"));
        //lbFormatsetText(i18n.tr("Format 24h"));
        lbDuree.setText(i18n.tr("Duration"));
        lbVoile.setText(i18n.tr("Glider"));
        cbGliders.setPromptText(i18n.tr("Click to list"));
        lbDeco.setText(i18n.tr("Take off"));
        lbAlt.setText(i18n.tr("Altitude"));
        lbComment.setText(i18n.tr("Comment"));
        btSites.setText(i18n.tr("Site file"));
        txSite.setPromptText(i18n.tr("Click site File"));
        btOk.setText(i18n.tr("Add"));
        btDuplicate.setText(i18n.tr("Duplicate"));
        btCancel.setText(i18n.tr("Close"));
        addStatusMsg = i18n.tr("Flight added");
        modStatusMsg = i18n.tr("Flight updated");
    }    
    
}
