/*
 * Copyright Gil THOMAS
 * This file forms an integral part of Logfly project
 * See the LICENSE file distributed with source code
 * for details of Logfly licence project
 */
package controller;

import Logfly.Main;
import dialogues.alertbox;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import static java.time.temporal.ChronoField.HOUR_OF_DAY;
import static java.time.temporal.ChronoField.MINUTE_OF_HOUR;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.logging.Level;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.AreaChart;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.collections.FXCollections;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.Screen;
import javafx.stage.Stage;
import leaflet.map_visu;
import model.Carnet;
import org.xnap.commons.i18n.I18n;
import org.xnap.commons.i18n.I18nFactory;
import settings.configProg;
import settings.osType;
import systemio.mylogging;
import trackgps.traceGPS;

/**
 *
 * @author gil
 *
 * tuto simple http://java-buddy.blogspot.fr/2012/04/javafx-2-barchart.html
 * http://java-buddy.blogspot.fr/2013/03/javafx-2-update-stackedbarchart.html
 * 
 * areachart simple : http://java-buddy.blogspot.fr/2012/04/create-simple-area-chart-using-javafx-2.html
 * areachart 2 séries : http://java-buddy.blogspot.fr/2012/04/area-chart-with-two-series-of-data.html
 *
 * https://docs.oracle.com/javafx/2/charts/css-styles.htm
 * 
 *  Année en cours
*   SELECT strftime('%m',V_date),Count(V_ID),Sum(V_Duree) FROM Vol WHERE V_Date BETWEEN datetime('now', '-365 days') AND datetime('now', 'localtime') group by strftime('%m',V_date) ORDER BY V_Date DESC
*
*   Année - 1
*   SELECT strftime('%m',V_date),Count(V_ID),Sum(V_Duree) FROM Vol WHERE V_Date BETWEEN datetime('now', '-731 days') AND datetime('now', '-366 days') group by strftime('%m',V_date) ORDER BY V_Date DESC
*
* Utilisation css perso vue sur http://code.makery.ch/library/javafx-8-tutorial/fr/part4/
*/
public class DashViewController {

    @FXML
    Label lbVols;
    @FXML
    Label lbDur; 
    @FXML
    Label lbSyntheseDate;
    @FXML
    DatePicker dpDate;
    @FXML
    Label lbComparaison;    
    @FXML
    ChoiceBox chYear;
    @FXML
    Label lbMonth1;
    @FXML
    Label lbMonth2;   
    @FXML
    Label lbMonth3;
    @FXML
    Label lbMonth4;     
    @FXML
    Label lbMonth5;
    @FXML
    Label lbMonth6;   
    @FXML
    Label lbMonth7;
    @FXML
    Label lbMonth8; 
    @FXML
    Label lbMonth9;
    @FXML
    Label lbMonth10;   
    @FXML
    Label lbMonth11;
    @FXML
    Label lbMonth12;     
    @FXML
    Label lbMonthH1;
    @FXML
    Label lbMonthH2;   
    @FXML
    Label lbMonthH3;
    @FXML
    Label lbMonthH4;     
    @FXML
    Label lbMonthH5;
    @FXML
    Label lbMonthH6;   
    @FXML
    Label lbMonthH7;
    @FXML
    Label lbMonthH8; 
    @FXML
    Label lbMonthH9;
    @FXML
    Label lbMonthH10;   
    @FXML
    Label lbMonthH11;
    @FXML
    Label lbMonthH12;     
    @FXML
    Label lbFlights1;
    @FXML
    Label lbFlights2;   
    @FXML
    Label lbFlights3;
    @FXML
    Label lbFlights4;     
    @FXML
    Label lbFlights5;
    @FXML
    Label lbFlights6;   
    @FXML
    Label lbFlights7;
    @FXML
    Label lbFlights8; 
    @FXML
    Label lbFlights9;
    @FXML
    Label lbFlights10;   
    @FXML
    Label lbFlights11;
    @FXML
    Label lbFlights12; 
    @FXML
    Label lbDur1;
    @FXML
    Label lbDur2;   
    @FXML
    Label lbDur3;
    @FXML
    Label lbDur4;     
    @FXML
    Label lbDur5;
    @FXML
    Label lbDur6;   
    @FXML
    Label lbDur7;
    @FXML
    Label lbDur8; 
    @FXML
    Label lbDur9;
    @FXML
    Label lbDur10;   
    @FXML
    Label lbDur11;
    @FXML
    Label lbDur12;    
    @FXML
    Hyperlink lnkBestFlight;
    @FXML
    Label lbTopSite;    
    @FXML
    BarChart<String, Number> barChart1; 
    @FXML
    private CategoryAxis bc_xAxis;
    @FXML
    private CategoryAxis ac_xAxis;    
    @FXML
    AreaChart<String, Number> areaChart1; 
    @FXML
    private PieChart pieChart1;
    @FXML
    private PieChart pieChart2;    
            
    // Reference to the main application.
    private Main mainApp;    
    private RootLayoutController rootController;  
    // Localization
    private I18n i18n; 
    
    // Settings
    private configProg myConfig;  
    private StringBuilder sbError;
    
    private List<monthData> currYearList = new ArrayList<>();
    private List<monthData> compYearList = new ArrayList<>();
    private ObservableList<String> monthNames = FXCollections.observableArrayList();
    private ObservableList<PieChart.Data> glidersData;
    private ObservableList<PieChart.Data> sitesData;
    private ObservableList <String> dataYear; 
    String bestSite = null;
    String bestFlightId = null;
    LocalDate endDate;
    String compDate;
    StringBuilder compTitle;
    
    @FXML
    private void initialize() {
        
        endDate = LocalDate.now();
        // ChYear initialization
        dataYear = FXCollections.observableArrayList();
        dataYear.clear();
        dataYear.add("...");
        chYear.setItems(dataYear);
        // It will be completed by dpDate event
        chYear.setOnAction((event) -> {
            int idx = chYear.getSelectionModel().getSelectedIndex();
            if (idx == 0) {
                compDate = null;
                setDisplay(false);                  
            } else if (idx > 0) {
                String selectedYear = (String) chYear.getSelectionModel().getSelectedItem();          
                DateTimeFormatter dtMonth = DateTimeFormatter.ofPattern("MM");
                String lastMonth = endDate.format(dtMonth);
                compDate = lastMonth+"-"+selectedYear;
                setDisplay(true);                                                       
            }        
        }); 
        
        dpDate.valueProperty().addListener((ov, oldValue, newValue) -> {
            endDate = newValue;
            compDate = null;
            fillingYear();
            chYear.getSelectionModel().selectFirst();
            setDisplay(false);
        });
        


         
        
        // we had many problems to refresh xAxis with new data
        // after hours inspired by http://code.makery.ch/library/javafx-8-tutorial/part6/ 
        // we try this
        bc_xAxis.setCategories(monthNames); 
        ac_xAxis.setCategories(monthNames);
    }    
    
    private void getLastMonths(boolean isCompared) {        
        
        compTitle = new StringBuilder();        
        String compDebTitle = "";
        String compFinTitle = "";
        try {
            SimpleDateFormat monthDate = new SimpleDateFormat("MM-yyyy");
            SimpleDateFormat yearMonth = new SimpleDateFormat("yyyy MM");
            SimpleDateFormat monthYear = new SimpleDateFormat("MM-yyyy");            
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("MM-yyyy");            
            String maxDate = endDate.format(dtf);
            Calendar cal = Calendar.getInstance(); 
            Calendar calComp = Calendar.getInstance();
            cal.setTime(monthDate.parse(maxDate));
            if (isCompared && compDate != null) {                
                calComp.setTime(monthDate.parse(compDate));
            }
            for (int i = 1; i <= 12; i++) {
                getDbData(yearMonth.format(cal.getTime()),monthYear.format(cal.getTime()));
                if (isCompared) {
                    if (i== 1) compFinTitle = monthYear.format(calComp.getTime());
                    if (i== 12) compDebTitle = monthYear.format(calComp.getTime());
                    getCompareData(yearMonth.format(calComp.getTime()),monthYear.format(cal.getTime()));
                    calComp.add(Calendar.MONTH, -1);
                }
                cal.add(Calendar.MONTH, -1);
            }
            if (isCompared) {
                compTitle.append(compDebTitle).append("  ").append(compFinTitle);
            }            
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }     
    
    
    private void fillingYear() {
        PreparedStatement pstmt = null;
        ResultSet rsYear = null;     
        DateTimeFormatter dtyear = DateTimeFormatter.ofPattern("yyyy"); 
        String lastYear = endDate.format(dtyear);
        dataYear.clear();
        dataYear.add("...");
        String sReq = "SELECT strftime('%Y',V_date) FROM Vol WHERE strftime('%Y',V_date) < ? GROUP BY strftime('%Y',V_date) ORDER BY strftime('%Y',V_date) DESC ";
        try {
            pstmt = myConfig.getDbConn().prepareStatement(sReq);   
            pstmt.setString(1, lastYear);                    
            // We search years in the logbook
            rsYear = pstmt.executeQuery();
            if (rsYear != null)  {             
                while (rsYear.next()) {
                    dataYear.add(rsYear.getString(1));
                }                                                                                           
            }
        } catch ( Exception e ) {
            alertbox aError = new alertbox(myConfig.getLocale());
            aError.alertInfo(i18n.tr("Problème de lecture dans le carnet")); 
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
            
    private void getCompareData(String reqMonth, String labelMonth) {
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
                totSec = rsMonth.getInt("Sum(V_Duree)");
            } else {
                count = 0;
                totSec = 0;
            }
            compYearList.add(new monthData(labelMonth, count, totSec)); 
        } catch ( Exception e ) {     
            alertbox aError = new alertbox(myConfig.getLocale());
            aError.alertInfo(i18n.tr("Problème de lecture dans le carnet"));             
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
    
    private void getDbData(String reqMonth, String labelMonth) {
        
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
                totSec = rsMonth.getInt("Sum(V_Duree)");
            } else {
                count = 0;
                totSec = 0;
            }
            currYearList.add(new monthData(labelMonth, count, totSec)); 
        } catch ( Exception e ) {     
            alertbox aError = new alertbox(myConfig.getLocale());
            aError.alertInfo(i18n.tr("Problème de lecture dans le carnet"));             
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
    
    private String getBestFlight(String refDate) {
        String res = null;
        Statement stmt = null;
        ResultSet rs = null;   
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yy");
        DateTimeFormatter dtfDuree = new DateTimeFormatterBuilder().appendValue(HOUR_OF_DAY, 2).appendLiteral("h").appendValue(MINUTE_OF_HOUR, 2).appendLiteral("mn").toFormatter();
        StringBuilder sbReq = new StringBuilder();
        sbReq.append("SELECT * FROM Vol WHERE V_Date BETWEEN datetime('").append(refDate);
        sbReq.append("', '-1 years') AND datetime('").append(refDate).append("') ORDER BY V_Duree DESC LIMIT 1");       
        try {
            stmt = myConfig.getDbConn().createStatement();      
            rs = stmt.executeQuery(sbReq.toString()); 
            if (rs.next())  {    
                // We use Carnet for date formatting without fatigue
                Carnet ca = new Carnet();                    
                ca.setIdVol(rs.getString("V_ID"));
                ca.setDate(rs.getString("V_Date"));
                ca.setHeure(rs.getString("V_Date"));
                ca.setDuree(rs.getString("V_Duree"));
                ca.setSite(rs.getString("V_Site"));  
                bestFlightId = rs.getString("V_ID");
                res = i18n.tr("Meilleur vol : ")+dtf.format(ca.getDate())+" "+dtfDuree.format(ca.getDuree())+" "+ca.getSite();
            }
        } catch ( Exception e ) {
            alertbox aError = new alertbox(myConfig.getLocale());
            aError.alertInfo(i18n.tr("Problème de lecture dans le carnet"));             
            sbError = new StringBuilder(this.getClass().getName()+"."+Thread.currentThread().getStackTrace()[1].getMethodName());
            sbError.append("\r\n").append(e.toString());
            sbError.append(sbReq);
            mylogging.log(Level.SEVERE, sbError.toString());                            
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
    
    private void getLastGliders(String refDate) {
        Statement stmt = null;
        ResultSet rs = null;   
        String nameGlider;
        int totSec;
        glidersData = FXCollections.observableArrayList();
        glidersData.clear();
        // au départ on utilisait
        // SELECT V_Engin,Count(V_ID),Sum(V_Duree) FROM Vol WHERE V_Date BETWEEN datetime('now', '-365 days') AND datetime('now', 'localtime') GROUP BY upper(V_Engin)
        // On a paramètré l'année pour pouvoir changer les périodes affichées
        // Détails sur fonctions datetime Sqlite sur https://www.techonthenet.com/sqlite/functions/datetime.php   
        StringBuilder sbReq = new StringBuilder();
        sbReq.append("SELECT V_Engin,Count(V_ID),Sum(V_Duree) FROM Vol WHERE V_Date BETWEEN datetime('").append(refDate);
        sbReq.append("', '-1 years') AND datetime('").append(refDate).append("') GROUP BY upper(V_Engin)");
        //String sReq = "SELECT V_Engin,Count(V_ID),Sum(V_Duree) FROM Vol WHERE V_Date BETWEEN datetime('now', '-365 days') AND datetime('now', 'localtime') GROUP BY upper(V_Engin)";
        try {
            stmt = myConfig.getDbConn().createStatement();      
            rs = stmt.executeQuery(sbReq.toString()); 
            if (rs != null)  {             
                while (rs.next()) {
                    nameGlider = rs.getString(1).trim();
                    glidersData.add(new PieChart.Data(nameGlider, rs.getInt(3)));
                }
                glidersData.forEach(d -> {
                    Tooltip tip = new Tooltip();
                    tip.setText(d.getName() + "");
                    Tooltip.install(d.getNode(), tip);
                });
            }
        } catch ( Exception e ) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(i18n.tr("Problème de lecture dans le carnet"));            
            String s = e.getClass().getName() + ": " + e.getMessage();
            alert.setContentText(s);
            alert.showAndWait();                                
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

    private void getLastSites(String refDate) {
        Statement stmt = null;
        ResultSet rs = null;   
        String nameSite;
        sitesData = FXCollections.observableArrayList();
        sitesData.clear();
        // au départ on utilisait
        // SELECT V_Site,Count(V_ID),Sum(V_Duree) FROM Vol WHERE V_Date BETWEEN datetime('now', '-365 days') AND datetime('now', 'localtime') GROUP BY upper(V_Site) ORDER BY Sum(V_Duree) DESC
        // On a paramètré l'année pour pouvoir changer les périodes affichées
        // Détails sur fonctions datetime Sqlite sur https://www.techonthenet.com/sqlite/functions/datetime.php   
        StringBuilder sbReq = new StringBuilder();
        sbReq.append("SELECT V_Site,Count(V_ID),Sum(V_Duree) FROM Vol WHERE V_Date BETWEEN datetime('").append(refDate);
        sbReq.append("', '-1 years') AND datetime('").append(refDate).append("') GROUP BY upper(V_Site) ORDER BY Sum(V_Duree) DESC");      
        try {
            stmt = myConfig.getDbConn().createStatement();      
            rs = stmt.executeQuery(sbReq.toString()); 
            if (rs != null)  {             
                while (rs.next()) {
                    nameSite = rs.getString(1).trim();
                    sitesData.add(new PieChart.Data(nameSite, rs.getInt(3)));
                    if (bestSite == null) {
                        bestSite = i18n.tr("Top site : ")+nameSite+" "+translateHours(rs.getInt(3));
                    }
                }
            }
        } catch ( Exception e ) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(i18n.tr("Problème de lecture dans le carnet"));            
            String s = e.getClass().getName() + ": " + e.getMessage();
            alert.setContentText(s);
            alert.showAndWait();                                
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
    
    @FXML 
    private void showFullMap()  {        
        Statement stmt = null;
        ResultSet rs = null;
        
        String sReq = "SELECT V_IGC FROM Vol WHERE V_ID = "+bestFlightId;
        try {
            stmt = myConfig.getDbConn().createStatement();
            rs =  stmt.executeQuery(sReq);
            if (rs != null)  { 
                if (rs.getString("V_IGC") != null && !rs.getString("V_IGC").equals(""))  {   
                    traceGPS currTrace = new traceGPS(rs.getString("V_IGC"),"",true, myConfig);   
                    if (currTrace.isDecodage()) {  
                        map_visu visuFullMap = new map_visu(currTrace, myConfig);
                        if (visuFullMap.isMap_OK()) {            
                            try {
                                String sHTML = visuFullMap.getMap_HTML();         
                                /** ----- Begin Debug --------*/                 
//                                final Clipboard clipboard = Clipboard.getSystemClipboard();
//                                final ClipboardContent content = new ClipboardContent();
//                                content.putString(sHTML);            
//                                clipboard.setContent(content);                                
                                /**------ End Debug --------- */                       
                                FXMLLoader loader = new FXMLLoader();
                                loader.setLocation(Main.class.getResource("/fullmap.fxml"));                    

                                AnchorPane page = (AnchorPane) loader.load();
                                Stage fullMap = new Stage();            
                                fullMap.initModality(Modality.WINDOW_MODAL);       
                                fullMap.initOwner(mainApp.getPrimaryStage());
                                Scene scene = null;
                                if (myConfig.getOS() == osType.LINUX) {
                                    // With this code for Linux, this is not OK with Win and Mac 
                                    // This code found on http://java-buddy.blogspot.fr/2012/02/javafx-20-full-screen-scene.html
                                    Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
                                    scene = new Scene(page, screenBounds.getWidth(), screenBounds.getHeight());
                                } else {
                                    // With this code, subStage.setMaximized(true) don't run under Linux
                                    scene = new Scene(page, 500, 400);
                                }                                    
                                fullMap.setScene(scene);
                   
                                // Initialization of a communication bridge between CarnetView and KmlView
                                FullMapController controller = loader.getController();
                                controller.setDashBridge(this,currTrace);
                                controller.setMapStage(fullMap);  
                                controller.setParams(myConfig, sHTML, -1);
                                controller.setWinMax();
                                fullMap.showAndWait();
                            } catch (IOException e) {
                                sbError = new StringBuilder(this.getClass().getName()+"."+Thread.currentThread().getStackTrace()[1].getMethodName());
                                sbError.append("\r\n").append(e.toString());
                                mylogging.log(Level.SEVERE, sbError.toString());
                            }
                        }                        
                    }                  
                } else {
                    alertbox aInfo = new alertbox(myConfig.getLocale());
                    aInfo.alertInfo(i18n.tr("Pas de trace à afficher"));                         
                }
            }
        } catch ( Exception e ) {
            alertbox aError = new alertbox(myConfig.getLocale());
            aError.alertError(e.getClass().getName() + ": " + e.getMessage());                          
        } finally {
            try{
                rs.close(); 
                stmt.close();
            } catch(Exception e) { } 
        }                     
    }
    
    
    private String translateHours(int duration) {
        
        int h = duration/3600;
        int mn = (duration - (h*3600))/60;              
        String sDuration = String.format("%02d", h)+"h"+String.format("%02d", mn);
        
        return sDuration;
    }
    
    /**
     * From http://java-buddy.blogspot.fr/2012/04/implement-event-handler-to-get-data-of.html
     */
    private void clicGliders() {
        for(PieChart.Data data : pieChart1.getData()){
            data.getNode().addEventHandler(
                javafx.scene.input.MouseEvent.MOUSE_PRESSED,
                new EventHandler<javafx.scene.input.MouseEvent>() {                     
                    @Override
                    public void handle(javafx.scene.input.MouseEvent mouseEvent) {
                        String name = data.getName();
                        int value = (int)data.getPieValue();
                        mainApp.rootLayoutController.updateMsgBar(name + " : " + translateHours(value), true, 220);
                    }
                });         
        }                
    }

    /**
     * From http://java-buddy.blogspot.fr/2012/04/implement-event-handler-to-get-data-of.html
     */
    private void clicSites() {
        for(PieChart.Data data : pieChart2.getData()){
            data.getNode().addEventHandler(
                javafx.scene.input.MouseEvent.MOUSE_PRESSED,
                new EventHandler<javafx.scene.input.MouseEvent>() {                     
                    @Override
                    public void handle(javafx.scene.input.MouseEvent mouseEvent) { 
                        String name = data.getName();
                        int value = (int)data.getPieValue();
                        mainApp.rootLayoutController.updateMsgBar(name + " : " + translateHours(value), true, 570);
                    }
                });         
        }                
    }    
    
    private void setDisplay(boolean isCompared) { 
        
        XYChart.Series lastFlightSerie = new XYChart.Series();
        XYChart.Series compFlightSerie = new XYChart.Series();
        XYChart.Series lastHoursSerie = new XYChart.Series();
        XYChart.Series compHoursSerie = new XYChart.Series();
        // Necessary to refresh xAxis, without refresh does not work
        CategoryAxis bc_xAxis = new CategoryAxis();
        
        currYearList.clear();       
        compYearList.clear();
        monthNames.clear();
        getLastMonths(isCompared);             
        getLastGliders(dpDate.getValue().toString());
        bestSite = null;
        getLastSites(dpDate.getValue().toString());
        String lbMonthField;
        int lbVolsField;
        String lbHoursField;
        Double dHours;
        Double oldHours;
        int h;
        int mn;
        StringBuilder myTitle = new StringBuilder();
        if (currYearList.size() > 0) {
            for (int i=currYearList.size()-1; i>-1; i-=1) {
                lbMonthField = currYearList.get(i).getFieldMonth();
                lbVolsField = currYearList.get(i).getFieldFlights(); 
                h = currYearList.get(i).getFieldDuration()/3600;
                mn = (currYearList.get(i).getFieldDuration() - (h*3600))/60;
                dHours = (double) currYearList.get(i).getFieldDuration()/3600;
                lbHoursField = String.format("%02d", h)+"h"+String.format("%02d", mn);
                lastFlightSerie.getData().add(new XYChart.Data(lbMonthField.substring(0,2), lbVolsField));
                monthNames.add(lbMonthField.substring(0,2));
                lastHoursSerie.getData().add(new XYChart.Data(lbMonthField.substring(0,2), dHours));                
                if (isCompared) {
                    compFlightSerie.getData().add(new XYChart.Data(lbMonthField.substring(0,2), compYearList.get(i).getFieldFlights()));
                    oldHours = (double) compYearList.get(i).getFieldDuration()/3600;
                    compHoursSerie.getData().add(new XYChart.Data(lbMonthField.substring(0,2), oldHours));    
                }
                switch (i) {
                    case 0:                        
                        lbMonth12.setText(lbMonthField);
                        lbMonthH12.setText(lbMonthField);
                        lbFlights12.setText(String.valueOf(lbVolsField));
                        lbDur12.setText(lbHoursField);  
                        myTitle.append(lbMonthField);
                        break;
                    case 1:
                        lbMonth11.setText(lbMonthField);
                        lbMonthH11.setText(lbMonthField);
                        lbFlights11.setText(String.valueOf(lbVolsField));
                        lbDur11.setText(lbHoursField);
                        break;
                    case 2:
                        lbMonth10.setText(lbMonthField);
                        lbMonthH10.setText(lbMonthField);
                        lbFlights10.setText(String.valueOf(lbVolsField));
                        lbDur10.setText(lbHoursField);
                        break;         
                    case 3:
                        lbMonth9.setText(lbMonthField);
                        lbMonthH9.setText(lbMonthField);
                        lbFlights9.setText(String.valueOf(lbVolsField));
                        lbDur9.setText(lbHoursField);
                        break;
                    case 4:
                        lbMonth8.setText(lbMonthField);
                        lbMonthH8.setText(lbMonthField);
                        lbFlights8.setText(String.valueOf(lbVolsField));
                        lbDur8.setText(lbHoursField);
                        break;
                    case 5:
                        lbMonth7.setText(lbMonthField);
                        lbMonthH7.setText(lbMonthField);
                        lbFlights7.setText(String.valueOf(lbVolsField));
                        lbDur7.setText(lbHoursField);
                        break;     
                    case 6:
                        lbMonth6.setText(lbMonthField);
                        lbMonthH6.setText(lbMonthField);
                        lbFlights6.setText(String.valueOf(lbVolsField));
                        lbDur6.setText(lbHoursField);
                        break;
                    case 7:
                        lbMonth5.setText(lbMonthField);
                        lbMonthH5.setText(lbMonthField);
                        lbFlights5.setText(String.valueOf(lbVolsField));
                        lbDur5.setText(lbHoursField);
                        break;
                    case 8:
                        lbMonth4.setText(lbMonthField);
                        lbMonthH4.setText(lbMonthField);
                        lbFlights4.setText(String.valueOf(lbVolsField));
                        lbDur4.setText(lbHoursField);
                        break;         
                    case 9:
                        lbMonth3.setText(lbMonthField);
                        lbMonthH3.setText(lbMonthField);
                        lbFlights3.setText(String.valueOf(lbVolsField));
                        lbDur3.setText(lbHoursField);
                        break;
                    case 10:
                        lbMonth2.setText(lbMonthField);
                        lbMonthH2.setText(lbMonthField);
                        lbFlights2.setText(String.valueOf(lbVolsField));
                        lbDur2.setText(lbHoursField);
                        break;
                    case 11:
                        lbMonth1.setText(lbMonthField);
                        lbMonthH1.setText(lbMonthField);
                        lbFlights1.setText(String.valueOf(lbVolsField));
                        lbDur1.setText(lbHoursField);
                        myTitle.append(lbMonthField).append("  ");
                        break;                        
                }
            }
            lastFlightSerie.setName(myTitle.toString());
            lastHoursSerie.setName(myTitle.toString());
            
            barChart1.getData().add(new XYChart.Series(FXCollections.observableArrayList(new XYChart.Data("",0))));
            barChart1.getData().clear();        
            if (isCompared) {
                compFlightSerie.setName(compTitle.toString());
                barChart1.getData().addAll(lastFlightSerie,compFlightSerie);
                barChart1.setLegendVisible(true);
            } else {
                barChart1.getData().addAll(lastFlightSerie);
                barChart1.setLegendVisible(false);
            }    
                        
            bc_xAxis.setAutoRanging(true);
            areaChart1.getData().clear();
            if (isCompared) {
                compHoursSerie.setName(compTitle.toString());
                areaChart1.getData().addAll(lastHoursSerie, compHoursSerie);
                areaChart1.setLegendVisible(true);
            } else {
                areaChart1.getData().addAll(lastHoursSerie);
                areaChart1.setLegendVisible(false);
            }
            pieChart1.getData().clear();
            pieChart1.setData(glidersData);
            glidersData.forEach(d -> {
                Tooltip tip = new Tooltip();        
                tip.setStyle(myConfig.getDecoToolTip());
                tip.setText(d.getName()+ " : "+translateHours((int) d.getPieValue()));
                Tooltip.install(d.getNode(), tip);
            });            
            clicGliders();
            for (Node node : pieChart1.lookupAll(".chart-legend-item")) {
                if (node instanceof Label) {
                    ((Label) node).setWrapText(false);
                    ((Label) node).setManaged(true);
                    ((Label) node).setPrefWidth(70);
                }
            }
            pieChart2.getData().clear();
            pieChart2.setData(sitesData);
            sitesData.forEach(d -> {
                Tooltip tip = new Tooltip();        
                tip.setStyle(myConfig.getDecoToolTip());
                tip.setText(d.getName()+ " : "+translateHours((int) d.getPieValue()));
                Tooltip.install(d.getNode(), tip);
            });              
            for (Node node : pieChart2.lookupAll(".chart-legend-item")) {
                if (node instanceof Label) {
                    ((Label) node).setWrapText(false);
                    ((Label) node).setManaged(true);
                    ((Label) node).setPrefWidth(70);
                    ((Label) node).setMaxHeight(10);
                }
            }            
            lnkBestFlight.setText(getBestFlight(dpDate.getValue().toString()));            
            lbTopSite.setText(bestSite);
            clicSites();
            mainApp.rootLayoutController.updateMsgBar(i18n.tr("Cliquer sur un secteur pour afficher la valeur"), true, 350);
        }
    }


    public void setMyConfig(configProg mainConfig) {
        this.mainApp = mainApp; 
        this.myConfig = mainConfig;
        i18n = I18nFactory.getI18n("","lang/Messages",ImportViewController.class.getClass().getClassLoader(),myConfig.getLocale(),0);
        rootController.updateMsgBar("", false,50); 
     //   endDate = LocalDate.now();
        dpDate.setValue(LocalDate.now());
        //fillYearComparison();
       // setDisplay(false);
        winTraduction();    
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
        lbVols.setText(i18n.tr("Vols"));
        lbDur.setText(i18n.tr("Heures"));
        lbComparaison.setText(i18n.tr("Comparaison"));
        lbSyntheseDate.setText(i18n.tr("Douze derniers mois depuis le "));
    }    
 
    public class monthData {
        
        private String fieldMonth;
        private int fieldFlights;
        private int fieldDuration;  
        
        monthData(String pMonth, int pFlights, int pDuration){
            this.fieldMonth = pMonth;
            this.fieldFlights = pFlights;
            this.fieldDuration = pDuration;
        }        

        public String getFieldMonth() {
            return fieldMonth;
        }

        public void setFieldMonth(String fieldMonth) {
            this.fieldMonth = fieldMonth;
        }

        public int getFieldFlights() {
            return fieldFlights;
        }

        public void setFieldFlights(int fieldFlights) {
            this.fieldFlights = fieldFlights;
        }

        public int getFieldDuration() {
            return fieldDuration;
        }

        public void setFieldDuration(int fieldDuration) {
            this.fieldDuration = fieldDuration;
        }
        
              
        
    }
}



//                switch (i) {
//                    case 0:                        
//                        lbMonth1.setText(lbMonthField);
//                        lbMonthH1.setText(lbMonthField);
//                        lbFlights1.setText(String.valueOf(lbVolsField));
//                        lbDur1.setText(lbHoursField);                        
//                        break;
//                    case 1:
//                        lbMonth2.setText(lbMonthField);
//                        lbMonthH2.setText(lbMonthField);
//                        lbFlights2.setText(String.valueOf(lbVolsField));
//                        lbDur2.setText(lbHoursField);
//                        break;
//                    case 2:
//                        lbMonth3.setText(lbMonthField);
//                        lbMonthH3.setText(lbMonthField);
//                        lbFlights3.setText(String.valueOf(lbVolsField));
//                        lbDur3.setText(lbHoursField);
//                        break;         
//                    case 3:
//                        lbMonth4.setText(lbMonthField);
//                        lbMonthH4.setText(lbMonthField);
//                        lbFlights4.setText(String.valueOf(lbVolsField));
//                        lbDur4.setText(lbHoursField);
//                        break;
//                    case 4:
//                        lbMonth5.setText(lbMonthField);
//                        lbMonthH5.setText(lbMonthField);
//                        lbFlights5.setText(String.valueOf(lbVolsField));
//                        lbDur5.setText(lbHoursField);
//                        break;
//                    case 5:
//                        lbMonth6.setText(lbMonthField);
//                        lbMonthH6.setText(lbMonthField);
//                        lbFlights6.setText(String.valueOf(lbVolsField));
//                        lbDur6.setText(lbHoursField);
//                        break;     
//                    case 6:
//                        lbMonth7.setText(lbMonthField);
//                        lbMonthH7.setText(lbMonthField);
//                        lbFlights7.setText(String.valueOf(lbVolsField));
//                        lbDur7.setText(lbHoursField);
//                        break;
//                    case 7:
//                        lbMonth8.setText(lbMonthField);
//                        lbMonthH8.setText(lbMonthField);
//                        lbFlights8.setText(String.valueOf(lbVolsField));
//                        lbDur8.setText(lbHoursField);
//                        break;
//                    case 8:
//                        lbMonth9.setText(lbMonthField);
//                        lbMonthH9.setText(lbMonthField);
//                        lbFlights9.setText(String.valueOf(lbVolsField));
//                        lbDur9.setText(lbHoursField);
//                        break;         
//                    case 9:
//                        lbMonth10.setText(lbMonthField);
//                        lbMonthH10.setText(lbMonthField);
//                        lbFlights10.setText(String.valueOf(lbVolsField));
//                        lbDur10.setText(lbHoursField);
//                        break;
//                    case 10:
//                        lbMonth11.setText(lbMonthField);
//                        lbMonthH11.setText(lbMonthField);
//                        lbFlights11.setText(String.valueOf(lbVolsField));
//                        lbDur11.setText(lbHoursField);
//                        break;
//                    case 11:
//                        lbMonth12.setText(lbMonthField);
//                        lbMonthH12.setText(lbMonthField);
//                        lbFlights12.setText(String.valueOf(lbVolsField));
//                        lbDur12.setText(lbHoursField);
//                        break;                        
//                }