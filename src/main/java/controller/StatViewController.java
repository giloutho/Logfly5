/*
 * Copyright Gil THOMAS
 * This file forms an integral part of Logfly project
 * See the LICENSE file distributed with source code
 * for details of Logfly licence project
 */
package controller;

import Logfly.Main;
import dialogues.alertbox;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import littlewins.winStatHours;
import org.xnap.commons.i18n.I18n;
import org.xnap.commons.i18n.I18nFactory;
import settings.configProg;
import systemio.mylogging;

/**
 *
 * @author gil
 * 
 * On ne prend pas vraiment en charge la taille des barres de l'histogramme selon le nombre d'années en présence
 * Avec un carnet de vol comportant 30 années les barres sont quasiment illisibles
 * Si on veut gérer ce genre de problème consulter par ex https://stackoverflow.com/questions/27302875/set-bar-chart-column-width-size                         
**/

public class StatViewController {
    @FXML
    Label label1;
    @FXML
    Label label2;
    @FXML
    Label label3;
    @FXML
    Label label4;
    @FXML
    Label label5;
    @FXML
    Label label6;
    @FXML
    Label label7;
    @FXML
    Label label8;
    @FXML
    Label label9;
    @FXML
    Label label10;
    @FXML
    Label label11;
    @FXML
    Label label12;    
    @FXML
    Label label14;
    @FXML
    Label label15;
    @FXML
    Label label16;
    @FXML
    Label label17;
    @FXML
    Label label18;    
    @FXML
    Label lb_Hours; 
    @FXML
    Label lb_NbVols;
    @FXML
    Label lb_Period;
    @FXML
    Label lb_First;
    @FXML
    Label lb_Last;   
    @FXML
    Label lb_M_NbVols;
    @FXML
    Label lb_M_NbHours;     
    @FXML
    Label lb_M_Duree;
    @FXML
    Label lb_M_VolsM;   
    @FXML
    Label lb_M_DureeM;
    @FXML
    Label lb_Curr_Hours; 
    @FXML
    Label lb_Curr_NbVols;
    @FXML
    Label lb_Curr_Duree;   
    @FXML
    Label lb_Curr_VolsM;
    @FXML
    Label lb_Curr_DureeM;           
    @FXML
    Button btGliders;
    @FXML
    Button btSites;
    @FXML
    Button btPrev;
    @FXML
    Button btNext;    
    @FXML
    Button btList;    
    @FXML
    private ChoiceBox chbYear;     
    @FXML
    private ChoiceBox chbYear1;  
    @FXML
    private ChoiceBox chbYear2;    
    @FXML
    private ChoiceBox chbVols;  
    @FXML
    private ChoiceBox chbHours;
    @FXML
    private BarChart barChart1;
    @FXML
    private CategoryAxis catAxis2;
    @FXML
    private LineChart lineChart1;
    @FXML
    private BarChart barChart2;    
    
 // Reference to the main application.
    private Main mainApp;    
    private RootLayoutController rootController;  
    // Localization
    private I18n i18n; 
    
    // Settings
    private configProg myConfig;  
    private StringBuilder sbError; 
    
    
    private ObservableList <String> dataYear; 
    private ObservableList <String> statVols; 
    private ObservableList <String> statHours; 
    private List<chartDbData> dbList = new ArrayList<>();
    private XYChart.Series dbSerie;
    private int dbBegLimit;
    private int dbEndLimit;
    private String barChartTitle;
    private String [] sMonth = {null, null, null, null, null, null, null, null, null, null, null, null};
    private boolean firstGetDate;    
    private int totFlights = 1;
    private double MoyVolsMens = 1;
    private double MoyVols = 1;
    private int MoyFlightSeconds;
    private int totFlightSeconds;
    private DateTimeFormatter totalDtf = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private DateTimeFormatter yearDtf = DateTimeFormatter.ofPattern("yyyy");
    
     @FXML
    private void initialize() {
        
        firstGetDate = true;
        
        dataYear = FXCollections.observableArrayList();        
        
        chbYear.setOnAction((event) -> {
            String selectedYear = (String) chbYear.getSelectionModel().getSelectedItem();
            onChangeChbYear(selectedYear);
        }); 
        
        chbYear1.setOnAction((event) -> {
            if (!firstGetDate) {
                newPeriodStat();
            }                
        });   
        
        chbYear2.setOnAction((event) -> {
            if (!firstGetDate) {
                newPeriodStat();
            }                
        });           

        chbVols.getSelectionModel().selectedItemProperty()
                .addListener((ObservableValue observable, Object oldValue, Object newValue) -> {                    
                    switch (chbVols.getSelectionModel().getSelectedIndex()) {
                        case 1 :
                            newPeriodStat();
                            break;
                        case 2 :
                            String selectedYear1 = (String) chbYear1.getSelectionModel().getSelectedItem();
                            String selectedYear2 = (String) chbYear2.getSelectionModel().getSelectedItem();
                            lineChartTotVols(selectedYear1, selectedYear2);
                            break;
                    }
                });                
        
        chbHours.getSelectionModel().selectedItemProperty()
                .addListener((ObservableValue observable, Object oldValue, Object newValue) -> {
                    String selectedYear1 = (String) chbYear1.getSelectionModel().getSelectedItem();
                    String selectedYear2 = (String) chbYear2.getSelectionModel().getSelectedItem();
                    switch (chbHours.getSelectionModel().getSelectedIndex()) {
                        case 1:
                            barChartTotHours(selectedYear1, selectedYear2);
                            break;
                        case 2:
                            lineChartTotHours(selectedYear1, selectedYear2);
                            break;
                    }                            
                });                  
    }
    
   
    
    private void statLogbook(dateInterval currInterval) {
            
        if (currInterval.getDateBeg() != null && currInterval.getDateEnd() != null) {            
            LocalDate dateDeb = currInterval.getDateBeg();
            LocalDate dateFin = currInterval.getDateEnd();
            Period intervalPeriod = currInterval.getDateInterval();            
            lb_First.setText(totalDtf.format(dateDeb));
            lb_Last.setText(totalDtf.format(dateFin));
            // Compute duration in years and months
            StringBuilder sb = new StringBuilder();
            sb.append(String.format("%2d", intervalPeriod.getYears())).append(" ").append(i18n.tr("years"));
            sb.append(String.format("%2d", intervalPeriod.getMonths())).append(" ").append(i18n.tr("monthes"));
            lb_Period.setText(sb.toString());                               
            // Mise à jour libellé Totaux et Moyennes
            sb.setLength(0);
            sb.append(" ").append(yearDtf.format(dateDeb)).append("-").append(yearDtf.format(dateFin));
            label1.setText(i18n.tr("Totals")+sb.toString());
            label7.setText(i18n.tr("Averages")+sb.toString());
            if (firstGetDate) buildChbYear();   
            String date1 = (String) chbYear1.getSelectionModel().getSelectedItem();
            String date2 = (String) chbYear2.getSelectionModel().getSelectedItem();            
            if (getNbFlights(date1, date2, currInterval)) {
                displayTotFlights();
            }
            if (getTotHours(date1, date2, currInterval)) {
                displayTotHours();
            }              
            barChartTotVols(date1, date2);
            chbVols.getSelectionModel().selectFirst();
            chbHours.getSelectionModel().selectFirst();            
        } else {
            sbError = new StringBuilder(i18n.tr("undefined start and end date"));  
            mylogging.log(Level.SEVERE, sbError.toString());   
            alertbox aError = new alertbox(myConfig.getLocale());
            aError.alertError(sbError.toString());                                                                                   
        }        

    }
    
    private void onChangeChbYear(String selectedYear) {
        
        dateInterval onePeriod = getDates(selectedYear,selectedYear);
        if(onePeriod != null)  {              
            // Nombre total de vols de l'année en cours
            if (getNbFlights(selectedYear, selectedYear, onePeriod)) {
                displayCurrFlights();
            }
            if (getTotHours(selectedYear, selectedYear, onePeriod)) {
                displayCurrHours();
            }            
        }
    }
    
    private void newPeriodStat()  {
        
        String selectedYear1 = (String) chbYear1.getSelectionModel().getSelectedItem();
        String selectedYear2 = (String) chbYear2.getSelectionModel().getSelectedItem();
        dateInterval specPeriod = getDates(selectedYear1, selectedYear2);
        if (specPeriod != null) {            
            statLogbook(specPeriod);
        }        
    }
    
    private dateInterval getDates(String yearFilter1, String yearFilter2) {
        Statement stmt = null;
        ResultSet rs = null;   
        
        dateInterval compInterval = null;
        String date1 = null;
        String date2 = null;
        
        String sReq1;
        String sReq2;
        if (yearFilter1 == null && yearFilter2 == null) {
            sReq1 = "select V_Date from Vol order by V_Date";
            sReq2 = "select V_Date from Vol order by V_Date DESC LIMIT 1";
        } else {
            sReq1 = "select V_Date from Vol WHERE strftime('%Y-%m',V_date) >= '"+yearFilter1+"-01' AND strftime('%Y-%m',V_date) <= '"+yearFilter2+"-12' order by V_Date";
            sReq2 = "select V_Date from Vol WHERE strftime('%Y-%m',V_date) >= '"+yearFilter1+"-01' AND strftime('%Y-%m',V_date) <= '"+yearFilter2+"-12' order by V_Date DESC LIMIT 1";
        }
        try {
            stmt = myConfig.getDbConn().createStatement();      
            rs = stmt.executeQuery(sReq1); 
            if (rs.next())  {    
                date1 = rs.getString("V_Date");
                // rs.last triggers an error "Resultset is TYPE_FORWARD_ONLY. No explanation found
                rs = stmt.executeQuery(sReq2); 
                if (rs.next()) {
                    date2 = rs.getString("V_Date");
                }
                compInterval = new dateInterval(date1, date2);
            }
        } catch ( Exception e ) {
            alertbox aError = new alertbox(myConfig.getLocale());
            aError.alertError(e.getClass().getName() + ": " + e.getMessage());                                 
        }  finally {
            try{
                rs.close(); 
                stmt.close();
            } catch(Exception e) { 
                sbError = new StringBuilder(this.getClass().getName()+"."+Thread.currentThread().getStackTrace()[1].getMethodName());
                sbError.append("\r\n").append(e.toString());
                mylogging.log(Level.SEVERE, sbError.toString());                
            } 
        } 
        
        return compInterval;
    }
    
    private void displayTotFlights() {
        lb_NbVols.setText(String.valueOf(totFlights));      
        lb_M_NbVols.setText(String.format("%3.0f",MoyVols));  // Nombre vols moyen par an
        lb_M_VolsM.setText(String.format("%3.2f",MoyVolsMens)); 
    }
    
    private void displayCurrFlights() {
        lb_Curr_NbVols.setText(String.valueOf(totFlights));
        lb_Curr_VolsM.setText(String.format("%3.2f",MoyVolsMens));  // Nombre vols moyen par an
    }
   
    
    private boolean getNbFlights(String yearFilter1, String yearFilter2, dateInterval myPeriod) {
        boolean res = false;
        Statement stmt = null;
        ResultSet rs = null;  

        String sReq;
        if (yearFilter1 == null && yearFilter2 == null) {
            sReq = "select Count(V_ID) from Vol";
        } else {
            sReq = "select Count(V_ID) from Vol WHERE strftime('%Y-%m',V_date) >= '"+yearFilter1+"-01' AND strftime('%Y-%m',V_date) <= '"+yearFilter2+"-12'";
        }
        try {
            stmt = myConfig.getDbConn().createStatement();      
            rs = stmt.executeQuery(sReq); 
            if (rs.next())  {   
                totFlights = rs.getInt("Count(V_ID)");   
                // annual average  
                // Problème soulevé par le carnet de Norbert qui n'avait fait que 4 vols au mois de mars
                // Soit 4 vols en un mois ou 4 vols en trois mois [depuis le 1er janvier]. On part sur un concept depuis le 1er janvier
                // Est ce que l'on a plus de 12 mois ou non d'historique
                if (myPeriod.getDateInterval().getYears() < 1) {
                    MoyVolsMens = (((double)totFlights)/myPeriod.getDateEnd().getMonthValue());
                    MoyVols =  MoyVolsMens*12;
                } else {
                    MoyVolsMens = (((double)totFlights)/((myPeriod.getDateInterval().getYears()*12)+myPeriod.getDateInterval().getMonths()));
                    MoyVols = MoyVolsMens*12;
                }
                res = true;
            }
        } catch ( Exception e ) {
            alertbox aError = new alertbox(myConfig.getLocale());
            aError.alertError(e.getClass().getName() + ": " + e.getMessage());                                  
        }  finally {
            try{
                rs.close(); 
                stmt.close();
            } catch(Exception e) { 
                sbError = new StringBuilder(this.getClass().getName()+"."+Thread.currentThread().getStackTrace()[1].getMethodName());
                sbError.append("\r\n").append(e.toString());
                mylogging.log(Level.SEVERE, sbError.toString());                
            } 
        } 
        
        return res;
    }    
    
    private void displayTotHours() {
        lb_Hours.setText(String.format("%d:%02d", totFlightSeconds / 3600, (totFlightSeconds % 3600) / 60));   
        lb_M_NbHours.setText(String.format("%d:%02d",MoyFlightSeconds / 3600, (MoyFlightSeconds % 3600) / 60));  
        // Durée moyenne d'un vol
        int DureeMoyVol = totFlightSeconds / totFlights;
        lb_M_Duree.setText(String.format("%d:%02d", DureeMoyVol / 3600, (DureeMoyVol % 3600) / 60)); 
        // Nombre d'heure de vols moyen par mois
        int DureeMoyMois ;
        DureeMoyMois = (int) (MoyVolsMens * DureeMoyVol);
        lb_M_DureeM.setText(String.format("%d:%02d", DureeMoyMois / 3600, (DureeMoyMois % 3600) / 60));         
    }
    
    private void displayCurrHours() {
        lb_Curr_Hours.setText(String.format("%d:%02d", totFlightSeconds / 3600, (totFlightSeconds % 3600) / 60));  
        // Durée moyenne d'un vol
        int DureeMoyVol = totFlightSeconds / totFlights;
        lb_Curr_Duree.setText(String.format("%d:%02d", DureeMoyVol / 3600, (DureeMoyVol % 3600) / 60)); 
        // Nombre d'heure de vols moyen par mois
        int DureeMoyMois ;
        DureeMoyMois = (int) (MoyVolsMens * DureeMoyVol);
        lb_Curr_DureeM.setText(String.format("%d:%02d", DureeMoyMois / 3600, (DureeMoyMois % 3600) / 60));                 
    }
    
    private boolean getTotHours(String yearFilter1, String yearFilter2, dateInterval myPeriod) {
        boolean res = false;
        Statement stmt = null;
        ResultSet rs = null;  

        String sReq;
        if (yearFilter1 == null && yearFilter2 == null) {
            sReq = "select SUM(V_Duree) from Vol";
        } else {
            sReq = "select SUM(V_Duree) from Vol WHERE strftime('%Y-%m',V_date) >= '"+yearFilter1+"-01' AND strftime('%Y-%m',V_date) <= '"+yearFilter2+"-12'";
        }
        try {
            stmt = myConfig.getDbConn().createStatement();      
            rs = stmt.executeQuery(sReq); 
            if (rs.next())  {   
                totFlightSeconds = rs.getInt("SUM(V_Duree)"); 
                // Durée moyenne vol annuelle
                if (myPeriod.getDateInterval().getYears() < 1) {
                    MoyFlightSeconds = (totFlightSeconds/myPeriod.getDateEnd().getMonthValue());
                } else {                    
                    MoyFlightSeconds = (totFlightSeconds/((myPeriod.getDateInterval().getYears()*12)+myPeriod.getDateInterval().getMonths()))*12;                    
                }  
                res = true;
            }
        } catch ( Exception e ) {
            alertbox aError = new alertbox(myConfig.getLocale());
            aError.alertError(e.getClass().getName() + ": " + e.getMessage());                                  
        }  finally {
            try{
                rs.close(); 
                stmt.close();
            } catch(Exception e) { 
                sbError = new StringBuilder(this.getClass().getName()+"."+Thread.currentThread().getStackTrace()[1].getMethodName());
                sbError.append("\r\n").append(e.toString());
                mylogging.log(Level.SEVERE, sbError.toString());                
            } 
        } 
        
        return res;
    }    
    
    private void buildChbYear() {
        
        Statement stmt = null;
        ResultSet rsYear = null;                
        try {
            stmt = myConfig.getDbConn().createStatement();                        
            // We search years in the logbook
            rsYear = stmt.executeQuery("SELECT strftime('%Y',V_date) FROM Vol GROUP BY strftime('%Y',V_date) ORDER BY strftime('%Y',V_date) DESC");
            if (rsYear != null)  {             
                while (rsYear.next()) {
                    String currYear = rsYear.getString(1);
                    if (currYear != null && !currYear.equals("")) dataYear.add(rsYear.getString(1));
                }
                // Year choicebox initialization
                chbYear.setItems(dataYear);
                chbYear.getSelectionModel().select(0);            
                chbYear1.setItems(dataYear);
                chbYear1.getSelectionModel().select(dataYear.size()-1);        
                chbYear2.setItems(dataYear);
                chbYear2.getSelectionModel().select(0);      
            }
        } catch ( Exception e ) {
            alertbox aError = new alertbox(myConfig.getLocale());
            aError.alertError(e.getClass().getName() + ": " + e.getMessage());                              
        }  finally {
            try{
                rsYear.close(); 
                stmt.close();
            } catch(Exception e) { } 
        }                
    }
    
    private void lineChartTotVols(String yearFilter1, String yearFilter2) {
        Statement stmt = null;
        ResultSet rs = null;
        setLineChart1();        
        XYChart.Series flightSerie  = new XYChart.Series();
        flightSerie.setName(yearFilter1+"-"+yearFilter2);
        
        StringBuilder sbReq = new StringBuilder();
        sbReq.append("select strftime('%Y',V_date),Count(V_ID)from Vol WHERE strftime('%Y-%m',V_date) >= '");
        sbReq.append(yearFilter1).append("-01' AND strftime('%Y-%m',V_date) <= '");
        sbReq.append(yearFilter2).append("-12' group by strftime('%Y',V_date)");        
        try {
            stmt = myConfig.getDbConn().createStatement();      
            rs = stmt.executeQuery(sbReq.toString()); 
            if (rs != null)  {             
                while (rs.next()) {
                    if (rs.getInt(2) >= 0) {
                        flightSerie.getData().add(new XYChart.Data(rs.getString(1), rs.getInt(2)));
                    }
                }
                lineChart1.getData().add(flightSerie);
            }
        } catch ( Exception e ) {
            alertbox aError = new alertbox(myConfig.getLocale());
            aError.alertError(e.getClass().getName() + ": " + e.getMessage());                             
        }  finally {
            try{
                rs.close(); 
                stmt.close();
            } catch(Exception e) { 
                sbError = new StringBuilder(this.getClass().getName()+"."+Thread.currentThread().getStackTrace()[1].getMethodName());
                sbError.append("\r\n").append(e.toString());
                mylogging.log(Level.SEVERE, sbError.toString());                
            } 
        } 
    }    
    
    private void barChartTotVols(String yearFilter1, String yearFilter2) {

        Statement stmt = null;
        ResultSet rs = null;
        String sAnnee = "1900";
        String recordYear;
        int [] dbData = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
        setBarChart1();        
        XYChart.Series flightSerie  = new XYChart.Series();
        
        StringBuilder sbReq = new StringBuilder();
        sbReq.append("select strftime('%Y',V_date),strftime('%m',V_date),Count(V_ID) from Vol WHERE strftime('%Y-%m',V_date) >= '");
        sbReq.append(yearFilter1).append("-01' AND strftime('%Y-%m',V_date) <= '");
        sbReq.append(yearFilter2).append("-12' group by strftime('%Y',V_date),strftime('%m',V_date)");        
        try {
            stmt = myConfig.getDbConn().createStatement();      
            rs = stmt.executeQuery(sbReq.toString()); 
            if (rs != null)  {             
                while (rs.next()) {
                    recordYear = rs.getString(1).trim();
                    if (recordYear != "") { 
                        if (!sAnnee.equals(recordYear)) {
                            if (!sAnnee.equals("1900")) {
                                // Year is finished
                                for (int i=0; i<dbData.length; i++) {
                                    flightSerie.getData().add(new XYChart.Data(sMonth[i], dbData[i]));
                                }                                   
                                barChart1.getData().add(flightSerie);
                            }
                            // Nouvelle année
                            sAnnee = recordYear;
                            flightSerie = new XYChart.Series();
                            flightSerie.setName(sAnnee);
                            // Remise à zéro du tableau des douze valeurs mensuelles
                            for (int i=0; i<dbData.length; i++) {
                                dbData[i] = 0;
                            }                            
                        }
                        // Ajout dans la liste
                        if (rs.getInt(2) > 0 && rs.getInt(2) < 13) {
                            int idx = rs.getInt(2) - 1;                                                        
                            dbData[idx] = rs.getInt(3); 
                        }
                    }
                }            
            }
            if ( !sAnnee.equals("1900")) {
                // Last year added
                for (int i=0; i<dbData.length; i++) {
                    flightSerie.getData().add(new XYChart.Data(sMonth[i], dbData[i]));
                }                                   
                barChart1.getData().add(flightSerie);
              }
        } catch ( Exception e ) {
            alertbox aError = new alertbox(myConfig.getLocale());
            aError.alertError(e.getClass().getName() + ": " + e.getMessage());                             
        }  finally {
            try{
                rs.close(); 
                stmt.close();
            } catch(Exception e) { 
                sbError = new StringBuilder(this.getClass().getName()+"."+Thread.currentThread().getStackTrace()[1].getMethodName());
                sbError.append("\r\n").append(e.toString());
                mylogging.log(Level.SEVERE, sbError.toString());                
            } 
        } 
        
    }
    
    private void lineChartTotHours(String yearFilter1, String yearFilter2) {
        Statement stmt = null;
        ResultSet rs = null;
        setLineChart1();        
        XYChart.Series flightSerie  = new XYChart.Series();
        flightSerie.setName(yearFilter1+"-"+yearFilter2);
        
        StringBuilder sbReq = new StringBuilder();
        sbReq.append("select strftime('%Y',V_date),Sum(V_Duree) from Vol WHERE strftime('%Y-%m',V_date) >= '");
        sbReq.append(yearFilter1).append("-01' AND strftime('%Y-%m',V_date) <= '");
        sbReq.append(yearFilter2).append("-12' group by strftime('%Y',V_date)");        
        try {
            stmt = myConfig.getDbConn().createStatement();      
            rs = stmt.executeQuery(sbReq.toString()); 
            if (rs != null)  {             
                while (rs.next()) {
                    if (rs.getInt(2) >= 0) {
                        flightSerie.getData().add(new XYChart.Data(rs.getString(1), ((double) rs.getInt(2)/3600)));
                    }
                }
                lineChart1.getData().add(flightSerie);
            }
        } catch ( Exception e ) {
            alertbox aError = new alertbox(myConfig.getLocale());
            aError.alertError(e.getClass().getName() + ": " + e.getMessage());                             
        }  finally {
            try{
                rs.close(); 
                stmt.close();
            } catch(Exception e) { 
                sbError = new StringBuilder(this.getClass().getName()+"."+Thread.currentThread().getStackTrace()[1].getMethodName());
                sbError.append("\r\n").append(e.toString());
                mylogging.log(Level.SEVERE, sbError.toString());                
            } 
        } 
    }      
    
    private void barChartTotHours(String yearFilter1, String yearFilter2) {

        Statement stmt = null;
        ResultSet rs = null;
        String sAnnee = "1900";
        String recordYear;
        double [] dbData = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
        setBarChart1();        
        XYChart.Series flightSerie  = new XYChart.Series();
        
        StringBuilder sbReq = new StringBuilder();
        sbReq.append("select strftime('%Y',V_date),strftime('%m',V_date),Sum(V_Duree) from Vol WHERE strftime('%Y-%m',V_date) >= '");
        sbReq.append(yearFilter1).append("-01' AND strftime('%Y-%m',V_date) <= '");
        sbReq.append(yearFilter2).append("-12' group by strftime('%Y',V_date),strftime('%m',V_date)");        
        try {
            stmt = myConfig.getDbConn().createStatement();      
            rs = stmt.executeQuery(sbReq.toString()); 
            if (rs != null)  {             
                while (rs.next()) {
                    recordYear = rs.getString(1).trim();
                    if (recordYear != "") { 
                        if (!sAnnee.equals(recordYear)) {
                            if (!sAnnee.equals("1900")) {
                                // Year is finished
                                for (int i=0; i<dbData.length; i++) {
                                    flightSerie.getData().add(new XYChart.Data(sMonth[i], dbData[i]));
                                }                                   
                                barChart1.getData().add(flightSerie);
                            }
                            // Nouvelle année
                            sAnnee = recordYear;
                            flightSerie = new XYChart.Series();
                            flightSerie.setName(sAnnee);
                            // Remise à zéro du tableau des douze valeurs mensuelles
                            for (int i=0; i<dbData.length; i++) {
                                dbData[i] = 0;
                            }                            
                        }
                        // Ajout dans la liste
                        if (rs.getInt(2) > 0 && rs.getInt(2) < 13) {
                            int idx = rs.getInt(2) - 1;                                
                            dbData[idx] = ((double) rs.getInt(3)/3600); 
                        }
                    }
                }            
            }
            if ( !sAnnee.equals("1900")) {
                // Last year added
                for (int i=0; i<dbData.length; i++) {
                    flightSerie.getData().add(new XYChart.Data(sMonth[i], dbData[i]));
                }                                   
                barChart1.getData().add(flightSerie);
              }
        } catch ( Exception e ) {
            alertbox aError = new alertbox(myConfig.getLocale());
            aError.alertError(e.getClass().getName() + ": " + e.getMessage());                             
        }  finally {
            try{
                rs.close(); 
                stmt.close();
            } catch(Exception e) { 
                sbError = new StringBuilder(this.getClass().getName()+"."+Thread.currentThread().getStackTrace()[1].getMethodName());
                sbError.append("\r\n").append(e.toString());
                mylogging.log(Level.SEVERE, sbError.toString());                
            } 
        }         
    }   
    
    private void collectGliders(String yearFilter1, String yearFilter2) {
        Statement stmt = null;
        ResultSet rs = null;
        String gliderName;
        dbList.clear();
        barChartTitle = yearFilter1+"-"+yearFilter2;
        StringBuilder sbReq = new StringBuilder();
        sbReq.append("SELECT V_Engin,Count(V_ID),Sum(V_Duree) FROM Vol WHERE strftime('%Y-%m',V_date) >= '");
        sbReq.append(yearFilter1).append("-01' AND strftime('%Y-%m',V_date) <= '");
        sbReq.append(yearFilter2).append("-12' GROUP BY upper(V_Engin) ORDER BY Sum(V_Duree) DESC ");        
        try {
            stmt = myConfig.getDbConn().createStatement();      
            rs = stmt.executeQuery(sbReq.toString()); 
            if (rs != null)  { 
                while (rs.next()) {
                    if (rs.getInt(3) >= 0) {
                        gliderName = rs.getString(1).trim();
                        if (gliderName == null || gliderName.equals("")) gliderName = "Indéfini";
                        chartDbData currDbRecord = new chartDbData(gliderName, rs.getInt(3)); 
                        dbList.add(currDbRecord);
                    }
                } 
            }     
        } catch ( Exception e ) {
            alertbox aError = new alertbox(myConfig.getLocale());
            aError.alertError(e.getClass().getName() + ": " + e.getMessage());                             
        }  finally {
            try{
                rs.close(); 
                stmt.close();
            } catch(Exception e) { 
                sbError = new StringBuilder(this.getClass().getName()+"."+Thread.currentThread().getStackTrace()[1].getMethodName());
                sbError.append("\r\n").append(e.toString());
                mylogging.log(Level.SEVERE, sbError.toString());                
            } 
        }         
    }
    
    private void collectSites(String yearFilter1, String yearFilter2) {
        Statement stmt = null;
        ResultSet rs = null;
        String siteName;
        dbList.clear();
        barChartTitle = yearFilter1+"-"+yearFilter2;
        StringBuilder sbReq = new StringBuilder();
        sbReq.append("SELECT V_Site,Count(V_ID),Sum(V_Duree) FROM Vol l WHERE strftime('%Y-%m',V_date) >= '");
        sbReq.append(yearFilter1).append("-01' AND strftime('%Y-%m',V_date) <= '");
        sbReq.append(yearFilter2).append("-12' GROUP BY upper(V_Site) ORDER BY Sum(V_Duree)  DESC");        
        try {
            stmt = myConfig.getDbConn().createStatement();      
            rs = stmt.executeQuery(sbReq.toString()); 
            if (rs != null)  { 
                while (rs.next()) {
                    if (rs.getInt(3) >= 0) {
                        siteName = rs.getString(1).trim();
                        if (siteName == null || siteName.equals("")) siteName = "Indéfini";
                        chartDbData currDbRecord = new chartDbData(siteName, rs.getInt(3)); 
                        dbList.add(currDbRecord);
                    }
                } 
            }     
        } catch ( Exception e ) {
            alertbox aError = new alertbox(myConfig.getLocale());
            aError.alertError(e.getClass().getName() + ": " + e.getMessage());                             
        }  finally {
            try{
                rs.close(); 
                stmt.close();
            } catch(Exception e) { 
                sbError = new StringBuilder(this.getClass().getName()+"."+Thread.currentThread().getStackTrace()[1].getMethodName());
                sbError.append("\r\n").append(e.toString());
                mylogging.log(Level.SEVERE, sbError.toString());                
            } 
        }         
    }    
    
    /**
     * 
     * @param yearFilter1
     * @param yearFilter2 
     * 
     * Impossible d'obtenir un horizontal barchart avec SceneBuilder. Apparemment il y aurait un bug
     * Changement de couleur pas évident. Solution trouvée ici https://stackoverflow.com/questions/14158104/javafx-barchart-color
     * palette de couleur généré avec https://mycolor.space/
     * 
     */
    private void showBarChart2() {
        Tooltip tip;
        
        setBarChart2();
        barChart2.getData().add(new XYChart.Series(FXCollections.observableArrayList(new XYChart.Data("",0))));
        barChart2.getData().clear();                    
        barChart2.getData().add(dbSerie);     
        barChart2.setLegendVisible(false);
        
        // Affectation de couleur différentes
        int i = 0;
        for(Node n:barChart2.lookupAll(".default-color0.chart-bar")) {
            tip = new Tooltip();        
            tip.setStyle(myConfig.getDecoToolTip());                      
            tip.setText(dbList.get((i)+dbBegLimit).getName()+" : "+dbList.get((i)+dbBegLimit).getStrValue());
            Tooltip.install(n, tip);            
            i++;
            switch (i) {
                case 1:
                    n.setStyle("-fx-bar-fill: #FF1493;");   
                    break;
                case 2:
                    n.setStyle("-fx-bar-fill: #FF4A73;");
                    break;       
                case 3:
                    n.setStyle("-fx-bar-fill: #FF7B59;");
                    break;
                case 4:
                    n.setStyle("-fx-bar-fill: #FFA94B;");
                    break;        
               case 5:
                    n.setStyle("-fx-bar-fill: #FFD251;");
                    break;
                case 6:
                    n.setStyle("-fx-bar-fill: #F9F871;");
                    break;       
                case 7:
                    n.setStyle("-fx-bar-fill: #9BDE7E;");
                    break;
                case 8:
                    n.setStyle("-fx-bar-fill: #4BBC8E;");
                    break;                                         
                default:
                    n.setStyle("-fx-bar-fill: #039590;");
            }
        }
    }        
    
    @FXML
    private void handleSites() {
        String selectedYear1 = (String) chbYear1.getSelectionModel().getSelectedItem();
        String selectedYear2 = (String) chbYear2.getSelectionModel().getSelectedItem();      
        collectSites(selectedYear1, selectedYear2);
        if(dbList.size() > 0) {
            dbBegLimit = 0;
            dbEndLimit = 0;
            handleNext();
            showBarChart2();
            if (dbList.size() > 8) {
                btPrev.setVisible(true);
                btNext.setVisible(true);
            }
        }        
    }
    
    @FXML
    private void handleList() {
        if (dbList.size() > 0) {
            winStatHours listWin = new winStatHours(myConfig.getLocale());
            for (int i = 0; i < dbList.size(); i++) {
                winStatHours.hoursRecord gliderRec = listWin.new hoursRecord(String.format("%3d",i+1),dbList.get(i).getName(),dbList.get(i).getStrValue());
                listWin.fillList(gliderRec);
            }
            listWin.showTable();
        }
    }
    
    @FXML
    private void handlePrev() {
        dbBegLimit = dbBegLimit - 8;
        if (dbBegLimit < 0) dbBegLimit = 0;
        dbEndLimit = dbBegLimit + 8;
        if (dbEndLimit > dbList.size()) dbEndLimit = dbList.size();   
        dbSerie  = new XYChart.Series();
        dbSerie.setName(barChartTitle);
        for (int i = dbBegLimit; i < dbEndLimit; i++) {
            dbSerie.getData().add(new XYChart.Data(dbList.get(i).getName(), dbList.get(i).getdValue()));                        
        }   
        for (int i = 0; i < dbSerie.getData().size(); i++) {
             XYChart.Data item = (XYChart.Data)dbSerie.getData().get(i);
                Tooltip tip = new Tooltip();        
                tip.setStyle(myConfig.getDecoToolTip());
                tip.setText("Coucou...");
                Tooltip.install(item.getNode(), tip);
        }
        showBarChart2();        
    }
    
    @FXML
    private void handleNext() {
        dbBegLimit = dbEndLimit;
        if (dbBegLimit == dbList.size()) {
            dbBegLimit = dbBegLimit - 8;
            dbEndLimit = dbBegLimit + 8;
        } else {
            dbEndLimit = dbBegLimit + 8;
            if (dbEndLimit > dbList.size()) dbEndLimit = dbList.size();
        }
        dbSerie  = new XYChart.Series();
        dbSerie.setName(barChartTitle);
        for (int i = dbBegLimit; i < dbEndLimit; i++) {
            dbSerie.getData().add(new XYChart.Data(dbList.get(i).getName(), dbList.get(i).getdValue()));             
        } 
        for (int i = 0; i < dbSerie.getData().size(); i++) {
             XYChart.Data item = (XYChart.Data)dbSerie.getData().get(i);
                Tooltip tip = new Tooltip();        
                tip.setStyle(myConfig.getDecoToolTip());
                tip.setText("Coucou...");
                Tooltip.install(item.getNode(), tip);
        }        
        
        showBarChart2();
    }    
    
    @FXML
    private void handleGliders() {
        String selectedYear1 = (String) chbYear1.getSelectionModel().getSelectedItem();
        String selectedYear2 = (String) chbYear2.getSelectionModel().getSelectedItem();      
        collectGliders(selectedYear1, selectedYear2);
        if(dbList.size() > 0) {
            dbBegLimit = 0;
            dbEndLimit = 0;
            handleNext();
            showBarChart2();
            if (dbList.size() > 8) {
                btPrev.setVisible(true);
                btNext.setVisible(true);
            }
        }
    }
    
    public void setMyConfig(configProg mainConfig) {
        this.mainApp = mainApp; 
        this.myConfig = mainConfig;
        i18n = myConfig.getI18n();
        rootController.updateMsgBar("", false,50); 
        winTraduction();
        dateInterval totPeriod = getDates(null, null);
        if (totPeriod != null) {
            System.out.println("dateInterval : "+totPeriod.getDateBeg()+" "+totPeriod.getDateEnd()+" "+totPeriod.getDateInterval().getYears()+" ans"+totPeriod.dateInterval.getMonths()+" mois");  
            statLogbook(totPeriod);
        }
        firstGetDate = false;
    }
    
    /**
     * set the bridge with RootLayoutController  
     * @param rootlayout 
     */
    public void setRootBridge(RootLayoutController rootlayout, Main mainApp) {
        this.rootController = rootlayout; 
        this.mainApp = mainApp;         
    }     
    
    private void setBarChart1() {
        barChart1.getData().clear();         
        barChart1.setVisible(true);
        lineChart1.setVisible(false);
        barChart2.setVisible(false);
        btPrev.setVisible(false);
        btNext.setVisible(false);   
        btList.setVisible(false);
        mainApp.rootLayoutController.updateMsgBar("", false, 120);               
    }
    
    private void setLineChart1() {                
        lineChart1.getData().clear();
        lineChart1.setVisible(true);
        barChart1.setVisible(false);
        barChart2.setVisible(false);
        btPrev.setVisible(false);
        btNext.setVisible(false);        
        btList.setVisible(false);
        mainApp.rootLayoutController.updateMsgBar("", false, 120);        
    }    
    
    private void setBarChart2() {
        barChart2.getData().clear();         
        barChart2.setVisible(true);
        lineChart1.setVisible(false);
        barChart1.setVisible(false);     
        btList.setVisible(true);
        StringBuilder sbMsg = new StringBuilder();
        sbMsg.append(i18n.tr("Move the mouse over the bar to display the value")).append("      << ").append(i18n.tr("and"));
        sbMsg.append(" >>   ").append(i18n.tr("to display the different parts of the graph"));
        sbMsg.append("     ").append(i18n.tr("List")).append(" : ").append(i18n.tr("plaintext visualization"));        
        mainApp.rootLayoutController.updateMsgBar(sbMsg.toString(), true, 120);
    }    
    
    /**
    * Translate labels of the window
    */
    private void winTraduction() {
        label2.setText(i18n.tr("Flight hours"));
        label3.setText(i18n.tr("Total number of flights"));
        label4.setText(i18n.tr("Flight period displayed"));
        label5.setText(i18n.tr("First flight of the period"));
        label6.setText(i18n.tr("Last flight of the period"));
        label8.setText(i18n.tr("Average number of flights per year"));
        label9.setText(i18n.tr("Average annual flight time"));
        label10.setText(i18n.tr("Average flight time"));
        label11.setText(i18n.tr("Average number of flights per month"));
        label12.setText(i18n.tr("Average monthly duration"));
        label14.setText(i18n.tr("Flight hours"));
        label15.setText(i18n.tr("Total number of flights"));
        label16.setText(i18n.tr("Average flight time"));
        label17.setText(i18n.tr("Average number of flights per month"));
        label18.setText(i18n.tr("Average monthly duration"));
        btGliders.setText(i18n.tr("Gliders"));
        btSites.setText(i18n.tr("Sites"));
        btList.setText(i18n.tr("List"));
        sMonth[0] = i18n.tr("Jan");
        sMonth[1] = i18n.tr("Feb");
        sMonth[2] = i18n.tr("Mar");
        sMonth[3] = i18n.tr("Apr");
        sMonth[4] = i18n.tr("May");
        sMonth[5] = i18n.tr("Jun");
        sMonth[6] = i18n.tr("Jul");
        sMonth[7] = i18n.tr("Aug");
        sMonth[8] = i18n.tr("Sep");
        sMonth[9] = i18n.tr("Oct");
        sMonth[10] = i18n.tr("Nov");
        sMonth[11] = i18n.tr("Dec");        
        // Choicebox initialization
        statVols = FXCollections.observableArrayList(i18n.tr("Flights stats"), i18n.tr("Monthly mean"), i18n.tr("Year-to-date"));        
        chbVols.setItems(statVols);
        chbVols.getSelectionModel().selectFirst();
        statHours = FXCollections.observableArrayList(i18n.tr("Flights hours stat"), i18n.tr("Monthly mean"), i18n.tr("Year-to-date"));        
        chbHours.setItems(statHours);
        chbHours.getSelectionModel().selectFirst();        
    }       

    public class dateInterval {
        
        private LocalDate dateEnd;
        private LocalDate dateBeg;
        private Period dateInterval;
        
        dateInterval(String pDateBeg, String pDateEnd){
            this.dateBeg = setDate(pDateBeg);
            this.dateEnd = setDate(pDateEnd);
            if (dateBeg != null && dateEnd != null)
            this.dateInterval = Period.between(this.dateBeg, this.dateEnd);           
        }           

        public LocalDate getDateEnd() {
            return dateEnd;
        }

        public void setDateEnd(LocalDate dateEnd) {
            this.dateEnd = dateEnd;
        }

        public LocalDate getDateBeg() {
            return dateBeg;
        }

        public void setDateBeg(LocalDate dateBeg) {
            this.dateBeg = dateBeg;
        }

        public Period getDateInterval() {
            return dateInterval;
        }

        public void setDateInterval(Period dateInterval) {
            this.dateInterval = dateInterval;
        }
     
        private LocalDate setDate(String dateStr) {            
            
            // in database, date is in principle YYYY-MM-DD HH:MM:SS      
            // but sometimes we have only YYYY-MM-DD
            LocalDate resDate = null;
            DateTimeFormatter formatterSQL = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            Pattern fullDate = Pattern.compile("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}");
            Matcher matchFull = fullDate.matcher(dateStr);
            try {
                if(! matchFull.find()) {
                    // Date in ot YYYY-MM-DD HH:MM, check for YYYY-MM-DD            
                    Pattern dayDate = Pattern.compile("\\d{4}-\\d{2}-\\d{2}");
                    Matcher matchDay = dayDate.matcher(dateStr);
                    if(matchDay.find()) {          
                        // Direct parsing is possible because we have default ISO_LOCAL_DATE format
                        resDate = LocalDate.parse(dateStr);                    
                    } else {
                        resDate = LocalDate.parse("2000-01-01");                    
                    }
                } else {
                    LocalDateTime ldtFromDb = LocalDateTime.parse(dateStr, formatterSQL);
                    resDate = ldtFromDb.toLocalDate();                
                }
            } catch (Exception e) {
                    sbError = new StringBuilder(this.getClass().getName()+"."+Thread.currentThread().getStackTrace()[1].getMethodName());
                    sbError.append("\r\n").append(e.toString());
                    mylogging.log(Level.SEVERE, sbError.toString());   
            }  
            return resDate;
        }            
        
    } 
    
    public class chartDbData {
        
        private String Name;
        private double dValue;
        private String strValue;  
        
        chartDbData(String pName, int pValue){
            this.Name = pName;
            this.dValue = (double) pValue /3600;   // In db this is total seconds, we want hours      
            setStrValue(pValue);
        }        

        public String getName() {
            return Name;
        }

        public double getdValue() {
            return dValue;
        }

        public String getStrValue() {
            return strValue;
        }

        public void setStrValue(int pValue) {
            int h;
            int mn;
            h = pValue/3600;
            mn = (pValue - (h*3600))/60;
            this.strValue = String.format("%02d", h)+"h"+String.format("%02d", mn);            
        }
    }    
}
