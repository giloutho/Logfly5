/*
 * Copyright Gil THOMAS
 * Ce fichier fait partie intégrante du projet Logfly
 * Pour tous les détails sur la licence du projet Logfly
 * Consulter le fichier LICENSE distribué avec le code source
 */
package littlewins;

import igc.pointIGC;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.TimeZone;
import javafx.scene.Scene;
import javafx.scene.control.TextArea;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import trackgps.traceGPS;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import org.xnap.commons.i18n.I18n;
import org.xnap.commons.i18n.I18nFactory;
import settings.configProg;


/**
 * Affichage dans une fenêtre modale de la liste des points de la trace
 * Directement inspiré par http://java-buddy.blogspot.fr/2013/05/detect-mouse-click-on-javafx-tableview.html
 * voir aussi https://examples.javacodegeeks.com/desktop-java/javafx/tableview/javafx-tableview-example/
 * @author gil
 */
public class winPoints {
    
    public static class trackPoint {
 
        private final SimpleStringProperty numero;
        private final SimpleStringProperty heure;
        private final SimpleStringProperty altibaro;
        private final SimpleStringProperty altigps;
        private final SimpleStringProperty latitude;
        private final SimpleStringProperty longitude;
        private final SimpleStringProperty distance;
        private final SimpleStringProperty vsol;
        private final SimpleStringProperty vz;
        
        private trackPoint(String numero, String heure, String altibaro, String altigps, String latitude, String longitude, String distance, String vsol, String vz) {
            this.numero = new SimpleStringProperty(numero);
            this.heure = new SimpleStringProperty(heure);
            this.altibaro = new SimpleStringProperty(altibaro);
            this.altigps = new SimpleStringProperty(altigps);
            this.latitude = new SimpleStringProperty(latitude);
            this.longitude = new SimpleStringProperty(longitude);
            this.distance = new SimpleStringProperty(distance);
            this.vsol = new SimpleStringProperty(vsol);
            this.vz = new SimpleStringProperty(vz);
        }       
        
        public String getNumero() {
            return numero.get();
        }
        
        public String getHeure() {
            return heure.get();
        }
        
        public String getAltibaro() {
            return altibaro.get();
        }
        
        public String getAltigps() {
            return altigps.get();
        }
        
        public String getLatitude() {
            return latitude.get();
        }
        
        public String getLongitude() {
            return longitude.get();
        }
        
        public String getDistance() {
            return distance.get();
        }
        
        public String getVsol() {
            return vsol.get();
        }
        
        public String getVz() {
            return vz.get();
        }
    }
    
    private TableView<trackPoint> tableView = new TableView<>();
    private final ObservableList<trackPoint> pointList = FXCollections.observableArrayList();
    

    
    // Localization
    private I18n i18n; 
    
    // Paramètres de configuration
     configProg myConfig;
    
    private void preparePointList(traceGPS currTrace) {
        int sizeTb = currTrace.Tb_Tot_Points.size();
        
        // si on veut le séparateur correpsondant à la nationalité de l'OS decimalFormatSymbols.getDecimalSeparator();
        // On force le point comme séparateur
        DecimalFormatSymbols decimalFormatSymbols = new DecimalFormatSymbols();
        decimalFormatSymbols.setDecimalSeparator('.');
        // voir http://stackoverflow.com/questions/4885254/string-format-to-format-double-in-java
        DecimalFormat coordFormat = new DecimalFormat("###.00000", decimalFormatSymbols);
        DecimalFormat centiFormat = new DecimalFormat("##0.00", decimalFormatSymbols);
        
        for (int i = 0; i < sizeTb; i++) {
            pointIGC currPoint = currTrace.Tb_Tot_Points.get(i);
            pointList.add(new trackPoint(String.format("%05d", i),currPoint.dHeure.format(DateTimeFormatter.ofPattern("HH:mm:ss")),
                            String.format("%04d", currPoint.AltiBaro),String.format("%04d", currPoint.AltiGPS),
                            coordFormat.format(currPoint.Latitude),coordFormat.format(currPoint.Longitude),
                            centiFormat.format(currPoint.DistPtPcdt), centiFormat.format(currPoint.Vitesse),
                            centiFormat.format(currPoint.Vario)));
            if (i == 8)  {
                System.out.println("Vz : "+currPoint.Vario);
            }
        }   
    }
          
    public void showTablePoints(traceGPS currTrace)  {
        i18n = I18nFactory.getI18n(Logfly.Main.class.getClass(),myConfig.getLocale());
        
        preparePointList(currTrace);

        tableView.setEditable(false);
        
        TableColumn colNum = new TableColumn(i18n.tr("No"));
        colNum.setCellValueFactory(new PropertyValueFactory<trackPoint, String>("numero"));
        TableColumn colHeure = new TableColumn(i18n.tr("Heure"));
        colHeure.setCellValueFactory(new PropertyValueFactory<trackPoint, String>("heure"));
        TableColumn colAltib = new TableColumn(i18n.tr("Alti Baro"));
        colAltib.setCellValueFactory(new PropertyValueFactory<trackPoint, String>("altibaro"));
        TableColumn colAltig = new TableColumn(i18n.tr("Alti GPS"));
        colAltig.setCellValueFactory(new PropertyValueFactory<trackPoint, String>("altigps"));
        TableColumn colLat = new TableColumn(i18n.tr("Lat"));
        colLat.setCellValueFactory(new PropertyValueFactory<trackPoint, String>("latitude"));
        TableColumn colLong = new TableColumn(i18n.tr("Long"));
        colLong.setCellValueFactory(new PropertyValueFactory<trackPoint, String>("longitude"));
        TableColumn colDist = new TableColumn(i18n.tr("Dist (m)"));
        colDist.setCellValueFactory(new PropertyValueFactory<trackPoint, String>("distance"));
        TableColumn colVit = new TableColumn(i18n.tr("Vit Sol"));
        colVit.setCellValueFactory(new PropertyValueFactory<trackPoint, String>("vsol"));
        TableColumn colVz = new TableColumn("Vz");
        colVz.setCellValueFactory(new PropertyValueFactory<trackPoint, String>("vz"));
        
        tableView.setItems(pointList);
        tableView.getColumns().addAll(colNum, colHeure, colAltib, colAltig, colLat, colLong, colDist, colVit, colVz);
        tableView.setPrefWidth(600);
        tableView.setPrefHeight(380);        
        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        
        final VBox vbox = new VBox();
        vbox.setSpacing(5);
        vbox.setPadding(new Insets(10, 0, 0, 10));
        vbox.getChildren().add(tableView);
        
        StackPane subRoot = new StackPane();
        subRoot.getChildren().add(vbox);
        Scene secondScene = new Scene(subRoot, 650, 400);
        Stage subStage = new Stage();
        // On veut que cette fenêtre soit modale
        subStage.initModality(Modality.APPLICATION_MODAL);
        
        // Préparation du titre avec la zone horaire et le décalage
        StringBuilder zdtTitle = new StringBuilder();
        zdtTitle.append(currTrace.getTzVol().getDisplayName()).append("  ").append(String.format("%+2.2f",currTrace.getUtcOffset()));
        if (currTrace.isDstOffset())  {
            zdtTitle.append("  ").append(i18n.tr("Eté"));
        }  else  {
            zdtTitle.append(" ").append(i18n.tr("Hiver"));            
        }            
        subStage.setTitle(zdtTitle.toString());
        subStage.setScene(secondScene);
        // Définit la position de la 2eme fenêtre par rapport à la première
        //subStage.setX(primaryStage.getX() + 250);
        //subStage.setY(primaryStage.getY() + 100);
        subStage.show();        
    }        
}
