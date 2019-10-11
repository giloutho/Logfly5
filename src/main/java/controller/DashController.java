/*
 * Copyright Gil THOMAS
 * This file forms an integral part of Logfly project
 * See the LICENSE file distributed with source code
 * for details of Logfly licence project
 */
package controller;

import Logfly.Main;
import dialogues.alertbox;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.logging.Level;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.AreaChart;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import model.dashGliders;
import model.dashMonths;
import org.xnap.commons.i18n.I18n;
import org.xnap.commons.i18n.I18nFactory;
import settings.configProg;
import systemio.mylogging;

/**
 *
 * @author gil
 */
public class DashController {
    
    @FXML
    ChoiceBox chAllYears;
    @FXML
    ChoiceBox chOldYears1;
    @FXML
    ChoiceBox chOldYears2;
    @FXML
    BarChart<String, Number> barChart1; 
    @FXML
    private CategoryAxis bc_xAxis;
    @FXML
    private CategoryAxis ac_xAxis;    
    @FXML
    AreaChart<String, Number> areaChart1; 
    @FXML
    private TableView<dashMonths> tableMonths;
    @FXML
    private TableColumn<dashMonths, String> mMonthsCol;
    @FXML
    private TableColumn<dashMonths, String> mFlightsCol;
    @FXML
    private TableColumn<dashMonths, LocalTime> mDurCol;    
    @FXML
    private TableView<dashGliders> tableGliders;
    @FXML
    private TableColumn<dashGliders, String> gGlidersCol;
    @FXML
    private TableColumn<dashGliders, LocalDate> gFlightsCol;
    @FXML
    private TableColumn<dashGliders, String> gDurCol; 
    @FXML
    private Button btnBestFlight;

    // Reference to the main application.
    private Main mainApp;    
    private RootLayoutController rootController;  
    // Localization
    private I18n i18n; 
    
    // Settings
    private configProg myConfig;  
    private StringBuilder sbError;
    
    private ObservableList <String> lstAllYears; 
    private ObservableList <String> lstOldYears; 
    private ObservableList <dashMonths> currMonthsList; 
    private LocalDate endDate;

    @FXML
    private void initialize() {
        // We need to intialize i18n before TableView building
        // For this reason we put building code in iniTable() 
        // This procedure will be called after setMainApp()           
        lstAllYears = FXCollections.observableArrayList();        
        lstOldYears = FXCollections.observableArrayList();   
    }    
    
    private void winStart() {        
        fillingAllYears();
        chAllYears.setItems(lstAllYears);
        chAllYears.getSelectionModel().selectFirst();
        endDate = LocalDate.now();
        fillingOldYears();
        chOldYears1.setItems(lstOldYears);
        chOldYears1.getSelectionModel().selectFirst();    
        //fillingTbMonths((String) chAllYears.getSelectionModel().getSelectedItem());
        fillDataMonths(false);
    }
    
    private void fillDataMonths(boolean isCompared) {
        //compTitle = new StringBuilder();        
        //String compDebTitle = "";
        //String compFinTitle = "";
        try {
            SimpleDateFormat monthDate = new SimpleDateFormat("MM-yyyy");
            SimpleDateFormat yearMonth = new SimpleDateFormat("yyyy MM");
            SimpleDateFormat monthYear = new SimpleDateFormat("MM-yyyy");    
            
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("MM-yyyy");    
            LocalDate startDate = LocalDate.of(endDate.getYear(), 1, 1);
            String sStartDate = startDate.format(dtf);
            Calendar cal = Calendar.getInstance(); 
            Calendar calComp = Calendar.getInstance();
            cal.setTime(monthDate.parse(sStartDate));
//            if (isCompared && compDate != null) {                
//                calComp.setTime(monthDate.parse(compDate));
//            }
            currMonthsList.clear();
            for (int i = 1; i <= 12; i++) {
                getDbData(yearMonth.format(cal.getTime()),monthYear.format(cal.getTime()));
//                if (isCompared) {
//                    if (i== 1) compFinTitle = monthYear.format(calComp.getTime());
//                    if (i== 12) compDebTitle = monthYear.format(calComp.getTime());
//                    getCompareData(yearMonth.format(calComp.getTime()),monthYear.format(cal.getTime()));
//                    calComp.add(Calendar.MONTH, -1);
//                }
                cal.add(Calendar.MONTH, 1);
            }
            tableMonths.setItems(currMonthsList); 
            System.out.println("table size "+tableMonths.getItems().size());
            if (isCompared) {
              //  compTitle.append(compDebTitle).append("  ").append(compFinTitle);
            }            
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }        
    }
    
    private void getDbData(String reqMonth, String labelMonth) {
        
        PreparedStatement pstmt = null;
        ResultSet rsMonth = null;   
        String count;
        String totSec;
        String sReq = "SELECT strftime('%m',V_date),Count(V_ID),Sum(V_Duree) FROM Vol WHERE strftime('%Y %m',V_date) = ?";
        try {
            pstmt = myConfig.getDbConn().prepareStatement(sReq);   
            pstmt.setString(1, reqMonth); 
            rsMonth = pstmt.executeQuery();
            if (rsMonth.next()) {  
                count = rsMonth.getString("Count(V_ID)");
                if (count == null) count ="";
                totSec = rsMonth.getString("Sum(V_Duree)");
                if (totSec == null) totSec ="0";
            } else {
                count = "";
                totSec = "0";
            }
            System.out.println(labelMonth+" "+count+" "+totSec);
            currMonthsList.add(new dashMonths(labelMonth, count, totSec)); 
        } catch ( Exception e ) {     
            alertbox aError = new alertbox(myConfig.getLocale());
            aError.alertInfo(i18n.tr("Could not read logbook"));             
            sbError = new StringBuilder(this.getClass().getName()+"."+Thread.currentThread().getStackTrace()[1].getMethodName());
            sbError.append("\r\n").append(e.toString());
            sbError.append(sReq).append(" -> ").append(reqMonth);
            mylogging.log(Level.SEVERE, sbError.toString());                  
        }  finally {
            try{
                rsMonth.close(); 
                pstmt.close();
            } catch(Exception e) { 
                sbError = new StringBuilder(this.getClass().getName()+"."+Thread.currentThread().getStackTrace()[1].getMethodName());
                sbError.append("\r\n").append(e.toString());
                mylogging.log(Level.SEVERE, sbError.toString());                
            } 
        }             
    }    
    
    /**
     * Code qui ne fonctionne pas vraiment
     * @param yearFilter 
     */
    private void fillingTbMonths(String yearFilter) {
        PreparedStatement pstmt = null;
        ResultSet rs = null;     
        StringBuilder sbReq = new StringBuilder();
        sbReq.append("SELECT strftime('%m',V_date),Count(V_ID),Sum(V_Duree) FROM Vol WHERE strftime('%Y',V_date) = '");
        sbReq.append(yearFilter);
        sbReq.append("' group by strftime('%m',V_date)");
        String sReq = sbReq.toString();
        int monthNumber = 1;
        
        try {
            pstmt = myConfig.getDbConn().prepareStatement(sReq);                    
            rs = pstmt.executeQuery();
            if (rs != null)  {             
                while (rs.next()) {
                    System.out.println("Monthnumber = "+monthNumber+" "+rs.getInt(1)+" "+rs.getString(2)+" "+rs.getString(3));
                    if (rs.getInt(1) == monthNumber) {
                        System.out.println(rs.getInt(1)+" "+rs.getString(2)+" "+rs.getString(3));
                    } else {
                        System.out.println(monthNumber+" 0 0");
                    }
                    monthNumber++;
                }                                                                                           
            }
            for (int i = monthNumber; i < 12; i++) {
                System.out.println(i+" 0 0");
            }
        } catch ( Exception e ) {
            alertbox aError = new alertbox(myConfig.getLocale());
            aError.alertInfo(i18n.tr("Could not read logbook")); 
            sbError = new StringBuilder(this.getClass().getName()+"."+Thread.currentThread().getStackTrace()[1].getMethodName());
            sbError.append("\r\n").append(e.toString());
            sbError.append(sReq.toString());
            mylogging.log(Level.SEVERE, sbError.toString());             
        }  finally {
            try{
                rs.close(); 
                pstmt.close();
            } catch(Exception e) { 
                sbError = new StringBuilder(this.getClass().getName()+"."+Thread.currentThread().getStackTrace()[1].getMethodName());
                sbError.append("\r\n").append(e.toString());
                mylogging.log(Level.SEVERE, sbError.toString());                
            } 
        }          
        
    }
    
    private void fillingAllYears() {
        PreparedStatement pstmt = null;
        ResultSet rsYear = null;     
        lstAllYears.clear();
        String sReq = "SELECT strftime('%Y',V_date) FROM Vol GROUP BY strftime('%Y',V_date) ORDER BY strftime('%Y',V_date) DESC";
        try {
            pstmt = myConfig.getDbConn().prepareStatement(sReq);                    
            // We search years in the logbook
            rsYear = pstmt.executeQuery();
            if (rsYear != null)  {             
                while (rsYear.next()) {
                    lstAllYears.add(rsYear.getString(1));
                }                                                                                           
            }
        } catch ( Exception e ) {
            alertbox aError = new alertbox(myConfig.getLocale());
            aError.alertInfo(i18n.tr("Could not read logbook")); 
            sbError = new StringBuilder(this.getClass().getName()+"."+Thread.currentThread().getStackTrace()[1].getMethodName());
            sbError.append("\r\n").append(e.toString());
            sbError.append(sReq);
            mylogging.log(Level.SEVERE, sbError.toString());             
        }  finally {
            try{
                rsYear.close(); 
                pstmt.close();
            } catch(Exception e) { 
                sbError = new StringBuilder(this.getClass().getName()+"."+Thread.currentThread().getStackTrace()[1].getMethodName());
                sbError.append("\r\n").append(e.toString());
                mylogging.log(Level.SEVERE, sbError.toString());                
            } 
        }             
    }
    
    private void fillingOldYears() {
        PreparedStatement pstmt = null;
        ResultSet rsYear = null;     
        DateTimeFormatter dtyear = DateTimeFormatter.ofPattern("yyyy"); 
        String lastYear = endDate.format(dtyear);
        lstOldYears.clear();
        String sReq = "SELECT strftime('%Y',V_date) FROM Vol WHERE strftime('%Y',V_date) < ? GROUP BY strftime('%Y',V_date) ORDER BY strftime('%Y',V_date) DESC ";
        try {
            pstmt = myConfig.getDbConn().prepareStatement(sReq);   
            pstmt.setString(1, lastYear);                    
            // We search years in the logbook
            rsYear = pstmt.executeQuery();
            if (rsYear != null)  {             
                while (rsYear.next()) {
                    lstOldYears.add(rsYear.getString(1));
                }                                                                                           
            }
        } catch ( Exception e ) {
            alertbox aError = new alertbox(myConfig.getLocale());
            aError.alertInfo(i18n.tr("Could not read logbook")); 
            sbError = new StringBuilder(this.getClass().getName()+"."+Thread.currentThread().getStackTrace()[1].getMethodName());
            sbError.append("\r\n").append(e.toString());
            sbError.append(sReq).append(" -> ").append(lastYear);
            mylogging.log(Level.SEVERE, sbError.toString());             
        }  finally {
            try{
                rsYear.close(); 
                pstmt.close();
            } catch(Exception e) { 
                sbError = new StringBuilder(this.getClass().getName()+"."+Thread.currentThread().getStackTrace()[1].getMethodName());
                sbError.append("\r\n").append(e.toString());
                mylogging.log(Level.SEVERE, sbError.toString());                
            } 
        }             
    }
    
    
    private void iniTable() {
        currMonthsList = FXCollections.observableArrayList();
        // https://stackoverflow.com/questions/21946789/how-to-change-row-height-of-tableview-on-the-fly
     //   tableMonths.setFixedCellSize(15.0);
        mMonthsCol.setCellValueFactory(new PropertyValueFactory<dashMonths, String>("month"));
        mFlightsCol.setCellValueFactory(new PropertyValueFactory<dashMonths, String>("flights"));
        mDurCol.setCellValueFactory(new PropertyValueFactory<dashMonths, LocalTime>("duree")); 
    }
    
    public void setMyConfig(configProg mainConfig) {
        this.mainApp = mainApp; 
        this.myConfig = mainConfig;
        i18n = I18nFactory.getI18n("","lang/Messages",ImportViewController.class.getClass().getClassLoader(),myConfig.getLocale(),0);
        rootController.updateMsgBar("", false,50); 
        iniTable();
        winTraduction();        
        winStart();
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
    * Translate labels of the window
    */
    private void winTraduction() {
        mMonthsCol.setText(i18n.tr("Months"));
        mFlightsCol.setText(i18n.tr("Flights"));      
        gFlightsCol.setText(i18n.tr("Flights"));    
        mDurCol.setText(i18n.tr("Duration"));
        gDurCol.setText(i18n.tr("Duration"));
        gGlidersCol.setText(i18n.tr("Gliders"));
    }        
    
}
