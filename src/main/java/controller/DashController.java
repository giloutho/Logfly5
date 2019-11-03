/*
 * Copyright Gil THOMAS
 * This file forms an integral part of Logfly project
 * See the LICENSE file distributed with source code
 * for details of Logfly licence project
 */
package controller;

import Logfly.Main;
import com.sun.javafx.charts.Legend;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.logging.Level;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import model.dashGliders;
import model.dashMonths;
import model.dashSites;
import org.xnap.commons.i18n.I18n;
import org.xnap.commons.i18n.I18nFactory;
import settings.configProg;
import systemio.mylogging;
import dialogues.alertbox;
import javafx.scene.control.TableRow;

/**
 *
 * @author gil
 */
public class DashController {
    
    @FXML
    ChoiceBox chAllYears;
    @FXML
    ChoiceBox chOldYears;
    @FXML
    private RadioButton rdFlights;    
    @FXML
    private RadioButton rdHours;     
    @FXML
    BarChart<String, Number> barChart1; 
    @FXML
    private CategoryAxis bc_xAxis;
    @FXML
    private CategoryAxis lc_xAxis;    
    @FXML
    LineChart<String, Number> lineChart1; 
    @FXML
    private TableView<dashMonths> tableMonths;
    @FXML
    private TableColumn<dashMonths, String> mMonthsCol;
    @FXML
    private TableColumn<dashMonths, String> mFlightsCol;
    @FXML
    private TableColumn<dashMonths, String> mDurCol;    
    @FXML
    private TableView<dashGliders> tableGliders;
    @FXML
    private TableColumn<dashGliders, String> gGlidersCol;
    @FXML
    private TableColumn<dashGliders, String> gFlightsCol;
    @FXML
    private TableColumn<dashGliders, String> gDurCol; 
    @FXML
    private TableView<dashSites> tableSites;
    @FXML
    private TableColumn<dashGliders, String> sSiteCol;
    @FXML
    private TableColumn<dashGliders, String> sFlightsCol;
    @FXML
    private TableColumn<dashGliders, String> sDurCol; 
    // Reference to the main application.
    private Main mainApp;    
    private RootLayoutController rootController;  
    // Localization
    private I18n i18n; 
    
    // Settings
    private configProg myConfig;  
    private StringBuilder sbError;
    
    private ToggleGroup rdGroup;
    private ObservableList <String> lstAllYears; 
    private ObservableList <String> lstOldYears; 
    private ObservableList <dashMonths> currMonthsList; 
    private ObservableList <dashMonths> compMonthsList; 
    private ObservableList <dashGliders> currGlidersList; 
    private ObservableList <dashSites> currSitesList; 
    private ObservableList<String> monthNames = FXCollections.observableArrayList();
    private LocalDate endDate;
    private int sumCurrFlights;
    private int sumCurrDuration;
    private int sumCompFlights;
    private int sumCompDuration;    
    private int sumGlidersFlights;
    private int sumGlidersDuration; 
    private int sumSitesFlights;
    private int sumSitesDuration;   
    String[] lbMonth;
    
    @FXML
    private void initialize() {
        // We need to intialize i18n before TableView building
        // For this reason we put building code in iniTable() 
        // This procedure will be called after setMainApp()           
        lstAllYears = FXCollections.observableArrayList();        
        lstOldYears = FXCollections.observableArrayList();  
        tableMonths.setSelectionModel(null);   
        rdGroup = new ToggleGroup();    
        rdFlights.setToggleGroup(rdGroup);
        rdFlights.setSelected(true);    
        rdHours.setToggleGroup(rdGroup);        
        // we had many problems to refresh xAxis with new data
        // after hours inspired by http://code.makery.ch/library/javafx-8-tutorial/part6/ 
        // we try this
        bc_xAxis.setCategories(monthNames); 
        lc_xAxis.setCategories(monthNames);       
        chAllYears.setOnAction((event) -> {
            updateCurrYear();
        });         
        chOldYears.setOnAction((event) -> {
            updateCompYear();
        });    

        tableMonths.setRowFactory(tv -> new TableRow<dashMonths>() {
            @Override
            public void updateItem(dashMonths item, boolean empty) {
                super.updateItem(item, empty) ;
                if (item != null) {
                    String  s = item.getMonth().trim();
                    if (s.length() > 3) {                        
                        setStyle("-fx-background-color: lightsteelblue;");
                    }
                }
            }
        });  

        tableGliders.setRowFactory(tv -> {
            TableRow<dashGliders> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                    dashGliders rowData = row.getItem();
                    displayGliderHours(rowData.getGlider());                
            });
            return row ;
        });        
    }    
    
    private void winStart() {        

        try {           
            fillAllYears();
            if (lstAllYears.size() > 0) {
                chAllYears.setItems(lstAllYears);
                // last year of logbook
                fillOldYears(lstAllYears.get(0));
                 if (lstOldYears.size() > 0) {            
                    chOldYears.setItems(lstOldYears); 
                } else {
                    chOldYears.setVisible(false);          
                }      
                chAllYears.getSelectionModel().selectFirst();                  
            }         	
        } catch (Exception e) {
          //  alertbox aError = new alertbox();
          //  aError.alertError("");                  
        }

    }
    
    private void updateCurrYear() {        
        fillDataCurrYear();
        fillDataGliders();
        fillDataSites();       
        int idxSelYear = chAllYears.getSelectionModel().getSelectedIndex();
        if (lstOldYears.size() > idxSelYear) {
            chOldYears.getSelectionModel().select(idxSelYear);
        } else {
            // To force refresh
            chOldYears.getSelectionModel().select(-1);
            updateCharts(false);
        }           
    }
    
    private void updateCompYear() {
        fillDataCompYear();
    }
        
    private void fillDataCurrYear() {

        LocalDate compDate;
        Calendar calComp = Calendar.getInstance();
        
        try {
            SimpleDateFormat monthDate = new SimpleDateFormat("MM-yyyy");
            SimpleDateFormat yearMonth = new SimpleDateFormat("yyyy MM");
            SimpleDateFormat monthYear = new SimpleDateFormat("MM-yyyy");    
            
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("MM-yyyy");   
            int iSelYear = Integer.parseInt((String) chAllYears.getSelectionModel().getSelectedItem()); 
            LocalDate startDate = LocalDate.of(iSelYear, 1, 1);
            String sStartDate = startDate.format(dtf);
            Calendar cal = Calendar.getInstance();             
            cal.setTime(monthDate.parse(sStartDate));                        
            currMonthsList.clear();
            sumCurrFlights = 0;
            sumCurrDuration = 0;
            for (int i = 1; i <= 12; i++) {
                getDbCurrYear(yearMonth.format(cal.getTime()),lbMonth[i-1]);
                cal.add(Calendar.MONTH, 1);                
            }            
            currMonthsList.add(new dashMonths(i18n.tr("Totals"), sumCurrFlights, sumCurrDuration)); 
            
            tableMonths.setItems(currMonthsList); 
  
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }        
    }    
    
    private void fillDataCompYear() {
        LocalDate compDate;
        Calendar calComp = Calendar.getInstance();
        
        try {
            SimpleDateFormat monthDate = new SimpleDateFormat("MM-yyyy");
            SimpleDateFormat yearMonth = new SimpleDateFormat("yyyy MM");
            SimpleDateFormat monthYear = new SimpleDateFormat("MM-yyyy");    
            
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("MM-yyyy");   

           String compYear = (String) chOldYears.getSelectionModel().getSelectedItem(); 
            try {
                int iCompYear = Integer.parseInt(compYear);
                compDate = LocalDate.of(iCompYear, 1, 1);
                String sCompDate = compDate.format(dtf);
                calComp.setTime(monthDate.parse(sCompDate));                    
            } catch (Exception e) {
                //isCompared = false;
            }                              
            compMonthsList.clear();
            sumCompFlights = 0;
            sumCompDuration = 0;
            for (int i = 1; i <= 12; i++) {
                getDbCompYear(yearMonth.format(calComp.getTime()),monthYear.format(calComp.getTime()));
                calComp.add(Calendar.MONTH, 1);                
            }            
            compMonthsList.add(new dashMonths("", sumCompFlights, sumCompDuration));
            // 
            updateCharts(true);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }         
    }    
    
    private void getDbCurrYear(String reqMonth, String labelMonth) {
        PreparedStatement pstmt = null;
        ResultSet rsMonth = null;   
        int count;
        int totSec;       
        String sReq = "SELECT strftime('%m',V_date),Count(V_ID),Sum(V_Duree) FROM Vol WHERE strftime('%Y %m',V_date) = ?";
        try {
            pstmt = myConfig.getDbConn().prepareStatement(sReq);   
            pstmt.setString(1, reqMonth); 
            rsMonth = pstmt.executeQuery();
            if (rsMonth.next()) {  
                count = rsMonth.getInt("Count(V_ID)");
                sumCurrFlights += rsMonth.getInt("Count(V_ID)");
                totSec = rsMonth.getInt("Sum(V_Duree)");
                sumCurrDuration += rsMonth.getInt("Sum(V_Duree)");
            } else {
                count = 0;
                totSec = 0;
            }
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
    
    private void getDbCompYear(String reqMonth, String labelMonth) {
        PreparedStatement pstmt = null;
        ResultSet rsMonth = null;   
        int count;
        int totSec;       
        String sReq = "SELECT strftime('%m',V_date),Count(V_ID),Sum(V_Duree) FROM Vol WHERE strftime('%Y %m',V_date) = ?";
        try {
            pstmt = myConfig.getDbConn().prepareStatement(sReq);   
            pstmt.setString(1, reqMonth); 
            rsMonth = pstmt.executeQuery();
            if (rsMonth.next()) {  
                count = rsMonth.getInt("Count(V_ID)");
                sumCompFlights += rsMonth.getInt("Count(V_ID)");
                totSec = rsMonth.getInt("Sum(V_Duree)");
                sumCompDuration += rsMonth.getInt("Sum(V_Duree)");
            } else {
                count = 0;
                totSec = 0;
            }
            compMonthsList.add(new dashMonths(labelMonth, count, totSec)); 
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
    
    @FXML
    public void pushFlights() {
        if (!rdFlights.isSelected()) rdFlights.setSelected(true);        
     //   updateBarChart(true);
        lineChart1.setVisible(false);
        barChart1.setVisible(true);
    }        
    
    @FXML
    public void pushHours() {
        if (rdHours.isSelected()){
           // updateAreaChart(true);           
            barChart1.setVisible(false);
            lineChart1.setVisible(true);
        }
    }      
    
    private void updateCharts(boolean isCompared) {
        
        XYChart.Series lastFlightSerie = new XYChart.Series();
        XYChart.Series compFlightSerie = new XYChart.Series(); 
        XYChart.Series lastHoursSerie = new XYChart.Series();
        XYChart.Series compHoursSerie = new XYChart.Series();
        monthNames.clear();
        int nbFlights;
        int nbCompFlights;
        Double dHours;
        Double compDHours;
        String totCurrFlights = null;
        String totCompFlights = null;
        String totCurrHours = null;
        String totCompHours = null;        
        if (currMonthsList.size() == 13) {
            for (int i = 0; i < 12; i++) {
                nbFlights = currMonthsList.get(i).getIntFlights();
                lastFlightSerie.getData().add(new XYChart.Data(lbMonth[i], nbFlights));
                dHours = (double) currMonthsList.get(i).getSeconds()/3600;
                lastHoursSerie.getData().add(new XYChart.Data(lbMonth[i], dHours));                
                monthNames.add(String.valueOf(i+1));
                if (isCompared) {
                    nbCompFlights = compMonthsList.get(i).getIntFlights();
                    compFlightSerie.getData().add(new XYChart.Data(lbMonth[i], nbCompFlights));
                    compDHours = (double) compMonthsList.get(i).getSeconds()/3600;
                    compHoursSerie.getData().add(new XYChart.Data(lbMonth[i], compDHours));                    
                }
            }
            totCurrFlights = String.valueOf(currMonthsList.get(12).getIntFlights());
            totCurrHours = String.valueOf(currMonthsList.get(12).getDuration());
            if (isCompared) {
                totCompFlights = String.valueOf(compMonthsList.get(12).getIntFlights());
                totCompHours = String.valueOf(compMonthsList.get(12).getDuration());          
            }
        }
        barChart1.getData().add(new XYChart.Series(FXCollections.observableArrayList(new XYChart.Data("",0))));
        barChart1.getData().clear();    
        barChart1.setBarGap(0);
        if (isCompared) {
            String currBarLegend = (String) chAllYears.getSelectionModel().getSelectedItem() +"  ("+totCurrFlights+")   ";
            String compBarLegend = (String) chOldYears.getSelectionModel().getSelectedItem() +"  ("+totCompFlights+")"; 
            barChart1.getData().addAll(lastFlightSerie,compFlightSerie);
            barChart1.setLegendVisible(true);
            // first bar color
            for(Node n:barChart1.lookupAll(".default-color0.chart-bar")) {
                      n.setStyle("-fx-bar-fill: lime;");
                  }
            //second bar color
            for(Node n:barChart1.lookupAll(".default-color1.chart-bar")) {
                     n.setStyle("-fx-bar-fill: red;");
            }
            Legend barLegend = (Legend)barChart1.lookup(".chart-legend");
            Legend.LegendItem bc1 = new Legend.LegendItem(currBarLegend, new Rectangle(10,4,Color.LIME));
            Legend.LegendItem bc2 = new Legend.LegendItem(compBarLegend, new Rectangle(10,4,Color.RED));
            barLegend.getItems().setAll(bc1,bc2);       
            barChart1.setLegendSide(Side.TOP);
        } else {
            barChart1.getData().addAll(lastFlightSerie);
            barChart1.setLegendVisible(false);
        }    
        bc_xAxis.setAutoRanging(true);
        
        lineChart1.getData().clear();
        if (isCompared) {
         //   compHoursSerie.setName(compTitle.toString());
            lineChart1.getData().addAll(lastHoursSerie, compHoursSerie);
            lineChart1.setLegendVisible(true);
            String currLineLegend = (String) chAllYears.getSelectionModel().getSelectedItem() +"  ("+totCurrHours+")   ";
            String compLineLegend = (String) chOldYears.getSelectionModel().getSelectedItem() +"  ("+totCompHours+")"; 
            Legend lineLegend = (Legend)lineChart1.lookup(".chart-legend");
            Legend.LegendItem li1 = new Legend.LegendItem(currLineLegend, new Rectangle(10,4,Color.LIME));
            Legend.LegendItem li2 = new Legend.LegendItem(compLineLegend, new Rectangle(10,4,Color.RED));
            lineLegend.getItems().setAll(li1,li2);       
            lineChart1.setLegendSide(Side.TOP);
            Node lastLine = lastHoursSerie.getNode().lookup(".chart-series-line");
            lastLine.setStyle("-fx-stroke: lime; -fx-stroke-width: 3px;");
            Node compLine = compHoursSerie.getNode().lookup(".chart-series-line");
            compLine.setStyle("-fx-stroke: red; -fx-stroke-width: 3px;");              
        } else {
            lineChart1.getData().addAll(lastHoursSerie);
            lineChart1.setLegendVisible(false);
        }              
        lineChart1.setCreateSymbols(false); //hide dots
        lc_xAxis.setAutoRanging(true);
    }
    
    private void fillDataGliders() {
        try {
            currGlidersList.clear();
            getDbGliders();
            if (sumGlidersFlights > 0) {
                currGlidersList.add(new dashGliders("", sumGlidersFlights, sumGlidersDuration)); 
            }
            tableGliders.setItems(currGlidersList); 
            StringBuilder sbMsg = new StringBuilder();
            sbMsg.append("                       ").append(i18n.tr("Click on a glider to display its total number of hours"));
            mainApp.rootLayoutController.updateMsgBar(sbMsg.toString(), true, 60);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }        
    }        

    private void getDbGliders() {
        
        PreparedStatement pstmt = null;
        ResultSet rs = null;   
        int count;
        int totSec;
        sumGlidersFlights = 0;
        sumGlidersDuration = 0;
        String nameGlider;
        String yearFilter = chAllYears.getSelectionModel().getSelectedItem().toString();
        String sReq = "SELECT V_Engin,Count(V_ID),Sum(V_Duree) FROM Vol WHERE strftime('%Y',V_date) = ? GROUP BY V_Engin ORDER BY Count(V_ID) DESC";
        try {
            pstmt = myConfig.getDbConn().prepareStatement(sReq);   
            pstmt.setString(1, yearFilter); 
            rs = pstmt.executeQuery();
            if (rs != null)  {             
                while (rs.next()) {
                    nameGlider = rs.getString(1);
                    count = rs.getInt("Count(V_ID)");                    
                    sumGlidersFlights += rs.getInt("Count(V_ID)");
                    totSec = rs.getInt("Sum(V_Duree)");
                    sumGlidersDuration += rs.getInt("Sum(V_Duree)");
                    currGlidersList.add(new dashGliders(nameGlider, count, totSec)); 
                }
            }
        } catch ( Exception e ) {     
            alertbox aError = new alertbox(myConfig.getLocale());
            aError.alertInfo(i18n.tr("Could not read logbook"));             
            sbError = new StringBuilder(this.getClass().getName()+"."+Thread.currentThread().getStackTrace()[1].getMethodName());
            sbError.append("\r\n").append(e.toString());
            sbError.append(sReq).append(" -> ").append(sReq);
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
    
    private void fillDataSites() {
        try {
            currSitesList.clear();
            getDbSites();
            if (sumSitesFlights > 0) {
                currSitesList.add(new dashSites("", sumSitesFlights, sumSitesDuration)); 
            }
            tableSites.setItems(currSitesList);        
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }        
    }    
    
    private void getDbSites() {
        
        PreparedStatement pstmt = null;
        ResultSet rs = null;   
        int count;
        int totSec;
        sumSitesFlights = 0;
        sumSitesDuration = 0;
        String nameSite;
        String yearFilter = chAllYears.getSelectionModel().getSelectedItem().toString();
        String sReq = "SELECT V_Site,Count(V_ID),Sum(V_Duree) FROM Vol WHERE strftime('%Y',V_date) = ? GROUP BY upper(V_Site) ORDER BY Count(V_ID) DESC";
        try {
            pstmt = myConfig.getDbConn().prepareStatement(sReq);   
            pstmt.setString(1, yearFilter); 
            rs = pstmt.executeQuery();
            if (rs != null)  {             
                while (rs.next()) {
                    nameSite = rs.getString(1).trim();
                    count = rs.getInt("Count(V_ID)");                    
                    sumSitesFlights += rs.getInt("Count(V_ID)");
                    totSec = rs.getInt("Sum(V_Duree)");
                    sumSitesDuration += rs.getInt("Sum(V_Duree)");
                    currSitesList.add(new dashSites(nameSite, count, totSec)); 
                }
            }
        } catch ( Exception e ) {     
            alertbox aError = new alertbox(myConfig.getLocale());
            aError.alertInfo(i18n.tr("Could not read logbook"));             
            sbError = new StringBuilder(this.getClass().getName()+"."+Thread.currentThread().getStackTrace()[1].getMethodName());
            sbError.append("\r\n").append(e.toString());
            sbError.append(sReq).append(" -> ").append(sReq);
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
    
    private void fillAllYears() {
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
    
    private void fillOldYears(String lastYear) {
        PreparedStatement pstmt = null;
        ResultSet rsYear = null;     
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
    
    private void displayGliderHours(String gliderName) {
        
        StringBuilder sbDur = new StringBuilder();
        sbDur.append(gliderName).append("\n");
        PreparedStatement pstmt = null;
        ResultSet rsGlider = null;                   
        String sReq = "SELECT Sum(V_Duree) FROM Vol WHERE V_Engin = ?";
        try {
            pstmt = myConfig.getDbConn().prepareStatement(sReq);   
            pstmt.setString(1, gliderName); 
            rsGlider = pstmt.executeQuery();            
            if (rsGlider.next()) {  
                int iDuration = rsGlider.getInt("Sum(V_Duree)");
                int nbHour = iDuration/3600;
                int nbMn = (iDuration - (nbHour*3600))/60;
                sbDur.append(i18n.tr("Flight hours")).append(" ");
                sbDur.append(String.format("%3d", nbHour)).append("h");
                sbDur.append(String.format("%02d", nbMn)).append("mn");
            } else {
                sbDur.append(i18n.tr("No flights counted for this glider"));
            }
            alertbox aInfo = new alertbox(myConfig.getLocale());
            aInfo.alertWithTitle("",sbDur.toString()); 
        } catch ( Exception e ) {     
            alertbox aError = new alertbox(myConfig.getLocale());
            aError.alertInfo(i18n.tr("Could not read logbook"));             
            sbError = new StringBuilder(this.getClass().getName()+"."+Thread.currentThread().getStackTrace()[1].getMethodName());
            sbError.append("\r\n").append(e.toString());
            sbError.append(sReq).append(" -> ").append(gliderName);
            mylogging.log(Level.SEVERE, sbError.toString());                  
        }  finally {
            try{
                rsGlider.close(); 
                pstmt.close();
            } catch(Exception e) { 
                sbError = new StringBuilder(this.getClass().getName()+"."+Thread.currentThread().getStackTrace()[1].getMethodName());
                sbError.append("\r\n").append(e.toString());
                mylogging.log(Level.SEVERE, sbError.toString());                
            } 
        }                   
    }
    
    
    private void tablesIni() {
        currMonthsList = FXCollections.observableArrayList();
        compMonthsList = FXCollections.observableArrayList(); 
        monthNames.clear();
        // https://stackoverflow.com/questions/21946789/how-to-change-row-height-of-tableview-on-the-fly
     //   tableMonths.setFixedCellSize(15.0);
        mMonthsCol.setCellValueFactory(new PropertyValueFactory<dashMonths, String>("month"));
        mFlightsCol.setCellValueFactory(new PropertyValueFactory<dashMonths, String>("flights"));
        mDurCol.setCellValueFactory(new PropertyValueFactory<dashMonths, String>("duration")); 
        currGlidersList = FXCollections.observableArrayList();
        gGlidersCol.setCellValueFactory(new PropertyValueFactory<dashGliders, String>("glider"));
        gFlightsCol.setCellValueFactory(new PropertyValueFactory<dashGliders, String>("flights"));
        gDurCol.setCellValueFactory(new PropertyValueFactory<dashGliders, String>("duration"));         
        currSitesList = FXCollections.observableArrayList();
        sSiteCol.setCellValueFactory(new PropertyValueFactory<dashGliders, String>("site"));
        sFlightsCol.setCellValueFactory(new PropertyValueFactory<dashGliders, String>("flights"));
        sDurCol.setCellValueFactory(new PropertyValueFactory<dashGliders, String>("duration"));         
    }
    
    public void setMyConfig(configProg mainConfig) {
        this.mainApp = mainApp; 
        this.myConfig = mainConfig;
        i18n = I18nFactory.getI18n("","lang/Messages",ImportViewController.class.getClass().getClassLoader(),myConfig.getLocale(),0);
        rootController.updateMsgBar("", false,50); 
        tablesIni();
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
        lbMonth = new String[12];
        lbMonth[0] = i18n.tr("Jan");
        lbMonth[1] = i18n.tr("Feb");
        lbMonth[2] = i18n.tr("Mar");
        lbMonth[3] = i18n.tr("Apr");
        lbMonth[4] = i18n.tr("May");
        lbMonth[5] = i18n.tr("Jun");
        lbMonth[6] = i18n.tr("Jul");
        lbMonth[7] = i18n.tr("Aug");
        lbMonth[8] = i18n.tr("Sep");
        lbMonth[9] = i18n.tr("Oct");
        lbMonth[10] = i18n.tr("Nov");
        lbMonth[11] = i18n.tr("Dec");
       
        mMonthsCol.setText(i18n.tr("Months"));
        mFlightsCol.setText(i18n.tr("Flights"));      
        gFlightsCol.setText(i18n.tr("Flights"));    
        mDurCol.setText(i18n.tr("Duration"));
        gDurCol.setText(i18n.tr("Duration"));
        gGlidersCol.setText(i18n.tr("Gliders"));
        sSiteCol.setText(i18n.tr("Sites"));
        sFlightsCol.setText(i18n.tr("Flights")); 
        sDurCol.setText(i18n.tr("Duration"));
    }        
    
}

       // String[] lbMonth = {i18n.tr("Jan"), i18n.tr("Feb"), i18n.tr("Mar"), i18n.tr("Apr"), i18n.tr("May"), i18n.tr("Jun"), i18n.tr("Jul"), i18n.tr("Aug"), i18n.tr("Sep"), i18n.tr("Oct"), i18n.tr("Nov"), i18n.tr("Dec")};