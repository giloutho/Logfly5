/*
 * Copyright Gil THOMAS
 * This file forms an integral part of Logfly project
 * See the LICENSE file distributed with source code
 * for details of Logfly licence project
 */
package littlewins;

import geoutils.googlegeo;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.DirectoryChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.xnap.commons.i18n.I18n;
import settings.privateData;
import systemio.mylogging;
import systemio.textio;
import waypio.pointRecord;
import waypio.wpwritefile;

/**
 *
 * @author gil
 */
public class winSaveXcp {
    private Stage subStage;
    private StringBuilder sbText = new StringBuilder();  
    private StringBuilder sbType = new StringBuilder();
    private StringBuilder sbScore = new StringBuilder();
    private String xcpUrl;
    private Paint colorBadValue = Paint.valueOf("FA6C04");
    private Paint colorGoodValue = Paint.valueOf("FFFFFF");
            
    private String[] aTp = null;
    private String sDash = "----------------------------------------------\r\n";
    private TextArea txWayp;
    private TextField txPrefix;
    private TextField txFile;
    private CheckBox chXcp = new CheckBox();
    private CheckBox chOzi = new CheckBox();
    private CheckBox chCompe = new CheckBox();
    private CheckBox chPcx = new CheckBox();
    private CheckBox chKml = new CheckBox();
    private CheckBox chGpx = new CheckBox();
    private CheckBox chCup = new CheckBox();
    
    private String currPrefix;
    private ArrayList<pointRecord> pointList;
    
    // Localization
    private I18n i18n; 
    private StringBuilder sbError = new StringBuilder();
    private String urlBaseXC = privateData.xcplannerUrl.toString();
    
    public winSaveXcp(I18n pI18n, String pXcpUrl, String pPrefix) {
        this.xcpUrl = pXcpUrl;
        currPrefix = (pPrefix == null) ? "WP" : pPrefix;
        this.i18n = pI18n;
        decodeUrl(xcpUrl);
        showWin();
    }
    
    private void decodeUrl(String pXcUrl) {
        String sDistance = null;
        String sScore = null;
        String sLocation = null;
        String sType = null;
        String sTp = null;
        String sStart = null;
                
        if (pXcUrl.contains("turnpoints")) {
            String[] urlPart = pXcUrl.split("&");

            for (int i = 0; i < urlPart.length; i++) {
                String s = urlPart[i];
                // case 0 is the timestamp
                switch (i) {
                    case 1 :
                        // http://alpidev.com/xcplanner/?Distance=84533.56737426427
                        if (s.contains("Distance")) {
                            String[] distPart = s.split("=");
                            if (distPart.length > 1) {
                                try {
                                    double dDist = Double.valueOf(distPart[1])/1000;   
                                    sDistance = String.format("%3.2f" , dDist) ;
                                } catch (Exception e) {
                                    sDistance = "NC";
                                }                                    
                            } 
                        }
                        break;
                    case 2 :
                        // Score=118.34699432396997
                        if (s.contains("Score")) {
                            String[] scPart = s.split("=");
                            if (scPart.length > 1) {
                                try {
                                    double dScore = Double.valueOf(scPart[1]);   
                                    sScore = String.format("%3.2f" , dScore) ;
                                } catch (Exception e) {
                                    sDistance = "NC";
                                }                                                                                             
                            }
                        }
                        break;     
                    case 3 :
                        // location=Doussard
                        if (s.contains("location")) {
                            String[] locPart = s.split("=");
                            if (locPart.length > 1) sLocation = locPart[1]; 
                        }
                        break;                     
                    case 4 :
                        // flightType=cfd3c
                        if (s.contains("flightType")) {
                            String[] flPart = s.split("=");
                            String sCode = "";
                            if (flPart.length > 1) sCode = flPart[1];
                            switch (sCode) {
                                case "cfd2": 
                                    sType = "CFD Distance libre";
                                    break;
                                case "cfd3":
                                    sType = "CFD Distance libre (1 point)";
                                    break;                                
                                case "cfd4":
                                    sType = "CFD Distance libre (2 points)";
                                    break;
                                case "cfd2c":
                                    sType = "CFD Aller-retour";
                                    break;
                                case "cfd3c":
                                    sType = "CFD Triangle plat ou FAI";
                                    break;
                                case "cfd4c":
                                    sType = "CFD Quadrilatère";
                                    break;
				case "olc2":
                                    sType = "OLC Free flight";
                                    break;
				case "olc3":
                                    sType = "OLC Free flight via a turnpoint";
                                    break;
				case "olc4":
                                    sType = "OLC Free flight via 2 turnpoints";
                                    break;
				case "olc5":
                                    sType = "OLC Free flight via 3 turnpoints";
                                    break;
				case "olc3c":
                                    sType = "OLC Flat or FAI triangle";
                                    break;
				case "ukxcl2":
                                    sType = "UK XC Open distance";
                                    break;
				case "ukxcl3":
                                    sType = "UK XC Turnpoint flight";
                                    break;
				case "ukxcl4":
                                    sType = "UK XC Turnpoint flight (2 turnpoints)";
                                    break;
				case "ukxcl5":
                                    sType = "UK XC Turnpoint flight (3 turnpoints)";
                                    break;
				case "ukxcl2c":
                                    sType = "UK XC Out and return";
                                    break;
				case "ukxcl3c":
                                    sType = "UK XC Flat or FAI triangle";
                                    break;
				case "ukxcl2d":
                                    sType = "UK XC Flight to goal";
                                    break;   
				case "xc2":
                                    sType = "XContest Free flight";
                                    break;
				case "xc3":
                                    sType = "XContest Free flight via a turnpoint";
                                    break;
				case "xc4":
                                    sType = "XContest Free flight via 2 turnpoints";
                                    break;
				case "xc5":
                                    sType = "XContest Free flight via 3 turnpoints";
                                    break;
				case "xc3c":
                                    sType = "XContest Flat or FAI triangle";
                                    break;                                    
                            }
                        }
                        break;   
                    case 5 :
                        // turnpoints=%5B%5B45.86131,6.04244%5D,%5B45.91580,6.36563%5D,%5B45.69050,6.39859%5D%5D
                        if (s.contains("turnpoints")) {
                            String[] tpPart = s.split("=");
                            if (tpPart.length > 1) {
                                aTp = tpPart[1].split("%5D,%5B");
                                for (int j = 0; j < aTp.length; j++) {
                                    aTp[j] = aTp[j].replaceAll("%5B", "").replaceAll("%5D", "");
                                }
                            } 
                        }
                        break;                      
                    case 6 :
                        // start=%5B45.77495,6.16970%5D
                        if (s.contains("start")) {
                            String[] stPart = s.split("=");
                            if (stPart.length > 1) {
                                sStart = stPart[1].replaceAll("%5B", "").replaceAll("%5D", "");
                            } 
                        }
                        break;                      
                }
            }
            
            sbType.append(sType).append("  ").append("Secteur").append(" : ").append(sLocation);
            sbScore.append("Distance").append(" : ").append(sDistance).append(" ").append("km").append("   ");
            sbScore.append("Score").append(" : ").append(sScore).append(" ").append("points").append("\r\n");
        } else {
            sbText.append("Pas de décodage");
        }        
    }
    
    private void addPointList(String sName, String sCoord, int idx) {
        pointRecord currPoint;
        
        String arCoord[] = sCoord.split(",");
        if (arCoord.length > 1) {
               // Ajout dans la liste
                currPoint = new pointRecord(sName, "",sName);     
                // First index = 0
                currPoint.setFIndex(idx);
                String sLat = arCoord[0];
                currPoint.setFLat(sLat);
                String sLong = arCoord[1];
                currPoint.setFLong(sLong);
                // altitude request
                googlegeo myGoog = new googlegeo();
                // Best results with a low precision in coordinates
                if (sLat.length() > 6) sLat = sLat.substring(0, 6);
                if (sLong.length() > 6) sLong = sLong.substring(0, 6);
                String googCoord = sLat+","+sLong;  
                if (myGoog.googleElevation(sCoord) == 0) {
                    currPoint.setFAlt(myGoog.getGeoAlt().trim());
                } else {
                    currPoint.setFAlt("");
                } 
                pointList.add(currPoint);            
        }
    }    
    
    private void fillText() {
        
        StringBuilder sbTp = new StringBuilder();      
        pointList = new ArrayList<>();
        if (aTp != null && aTp.length > 0) {
            for (int j = 0; j < aTp.length; j++) {
                StringBuilder sbName = new StringBuilder();
                sbName.append(currPrefix).append(String.format("%03d", j+1));
                addPointList(sbName.toString(), aTp[j], j);
                sbTp.append(sbName.toString()).append(" ").append(aTp[j]).append("\r\n");
            }          
        } 
        txWayp.setText(sbText.toString()+sbTp.toString());       
    }
    
    private void writeWaypFiles(String sPath) {
        File file;    
        boolean resWrite = false;        
        wpwritefile wfile;
        if (chOzi.isSelected()) {
            try {
                wfile = new wpwritefile();    
                file = new File(sPath+".wpt");    
                resWrite = wfile.writeOzi(pointList, file);                 
            } catch (Exception e) {
                sbError = new StringBuilder(this.getClass().getName()+"."+Thread.currentThread().getStackTrace()[1].getMethodName());
                sbError.append("\r\n").append(e.toString());
                mylogging.log(Level.SEVERE, sbError.toString());                 
            }            
        }
        if (chCompe.isSelected()) {
            try {
                wfile = new wpwritefile();    
                file = new File(sPath+".wpt");    
                resWrite = wfile.writeComp(pointList, file);                 
            } catch (Exception e) {
                sbError = new StringBuilder(this.getClass().getName()+"."+Thread.currentThread().getStackTrace()[1].getMethodName());
                sbError.append("\r\n").append(e.toString());
                mylogging.log(Level.SEVERE, sbError.toString());                 
            }            
        }
        if (chPcx.isSelected()) {
            try {
                wfile = new wpwritefile();    
                file = new File(sPath+".pcx");    
                resWrite = wfile.writePCX(pointList, file);                 
            } catch (Exception e) {
                sbError = new StringBuilder(this.getClass().getName()+"."+Thread.currentThread().getStackTrace()[1].getMethodName());
                sbError.append("\r\n").append(e.toString());
                mylogging.log(Level.SEVERE, sbError.toString());                 
            }            
        }
        if (chKml.isSelected()) {
            try {
                wfile = new wpwritefile();    
                file = new File(sPath+".kml");    
                resWrite = wfile.writeKml(pointList, file);                 
            } catch (Exception e) {
                sbError = new StringBuilder(this.getClass().getName()+"."+Thread.currentThread().getStackTrace()[1].getMethodName());
                sbError.append("\r\n").append(e.toString());
                mylogging.log(Level.SEVERE, sbError.toString());                 
            }            
        }
        if (chGpx.isSelected()) {
            try {
                wfile = new wpwritefile();    
                file = new File(sPath+".gpx");    
                resWrite = wfile.writeGpx(pointList, file);                 
            } catch (Exception e) {
                sbError = new StringBuilder(this.getClass().getName()+"."+Thread.currentThread().getStackTrace()[1].getMethodName());
                sbError.append("\r\n").append(e.toString());
                mylogging.log(Level.SEVERE, sbError.toString());                 
            }            
        }
        if (chCup.isSelected()) {
            try {
                wfile = new wpwritefile();    
                file = new File(sPath+".cup");    
                resWrite = wfile.writeCup(pointList, file);                 
            } catch (Exception e) {
                sbError = new StringBuilder(this.getClass().getName()+"."+Thread.currentThread().getStackTrace()[1].getMethodName());
                sbError.append("\r\n").append(e.toString());
                mylogging.log(Level.SEVERE, sbError.toString());                 
            }            
        }
    }
    
    private void saveXcp(File selFolder) {
 
        try {
            File fName = new File(selFolder.getAbsolutePath()+File.separator+txFile.getText().trim()+".xcp");
            if (fName.exists()) fName.delete();
            JSONObject obj = new JSONObject();		
            obj.put("url", xcpUrl);
            obj.put("prefix", currPrefix);
            JSONArray waypoints = new JSONArray();
            for (int i = 0; i < pointList.size(); i++) {
                StringBuilder sbWayp = new StringBuilder();
                sbWayp.append(pointList.get(i).getFBalise()).append(",");
                sbWayp.append(pointList.get(i).getFLat()).append(",").append(pointList.get(i).getFLong()).append(",");
                sbWayp.append(pointList.get(i).getFAlt());
                waypoints.add(sbWayp.toString());
            }
            obj.put("waypoints", waypoints);

            FileWriter file = new FileWriter(fName.getAbsolutePath());
            file.write(obj.toJSONString());
            file.flush();
            file.close();                
                
        } catch (Exception e) {
            sbError = new StringBuilder(this.getClass().getName()+"."+Thread.currentThread().getStackTrace()[1].getMethodName());
            sbError.append("\r\n").append(e.toString());
            mylogging.log(Level.SEVERE, sbError.toString());             
        }
    }
    
    private void saveOnDisk(File selFolder) {
        
        if (chXcp.isSelected()) {
            saveXcp(selFolder);           
        }
        writeWaypFiles(selFolder.getAbsolutePath()+File.separator+txFile.getText().trim());
        subStage.close();        
    }
    
    private void checkAndSave() {
        String sNameFile = txFile.getText().trim();
        if (sNameFile != null & !sNameFile.equals("")) {
            txFile.setStyle("-fx-control-inner-background: #"+colorGoodValue.toString().toString().substring(2));   
            DirectoryChooser directoryChooser = new DirectoryChooser();
            File selectedDirectory = directoryChooser.showDialog(null);
            if(selectedDirectory != null) {
                saveOnDisk(selectedDirectory);
            }            
        } else {
            txFile.setStyle("-fx-control-inner-background: #"+colorBadValue.toString().toString().substring(2));
            txFile.requestFocus();             
        }
    }
    
    private void showWin() {

        subStage = new Stage();
        
        Text lbType = new Text(sbType.toString()); 
        lbType.setFont(Font.font ("Verdana", 14));        
        
        Text lbScore = new Text(sbScore.toString()); 
        lbScore.setFont(Font.font ("Verdana", 14));        
        
        Text lbWayp = new Text(i18n.tr("Turnpoints List")+" "); 
        lbWayp.setFont(Font.font ("Verdana", 14));
        lbWayp.setFill(Color.BLUE);
        
        Text lbPrefix = new Text(i18n.tr("Turnpoint prefix")+" ");
        lbPrefix.setFont(Font.font ("Verdana", 14));
        lbPrefix.setFill(Color.BLUE);
        
        HBox hbPrefix = new HBox();
        hbPrefix.setSpacing(10);
        hbPrefix.setPadding(new Insets(0, 0, 0, 20));
        
        txPrefix = new TextField();
        txPrefix.setPrefWidth(100);        
        txPrefix.setText(currPrefix);
        txPrefix.textProperty().addListener((observable, oldValue, newValue) -> {
            currPrefix = txPrefix.getText();
            fillText();
        });
        hbPrefix.getChildren().addAll(txPrefix);         

        // Liste des points
        HBox hbWayp = new HBox();
        hbWayp.setMaxWidth(300);
        hbWayp.setPadding(new Insets(5, 12, 5, 12));
        txWayp = new TextArea();       
        txWayp.setWrapText(true);
        txWayp.setEditable(false);       
        fillText();
        hbWayp.getChildren().addAll(txWayp);       
        
        Text lbSave = new Text(i18n.tr("Saving"));
        lbSave.setFont(Font.font ("Verdana", 14));
        lbSave.setFill(Color.BLUE); 
        
        final VBox vbCheck = new VBox();
        vbCheck.setPadding(new Insets(0,0,0,20));
        vbCheck.setSpacing(10); 
        
        
        HBox hbFile = new HBox();
        hbFile.setSpacing(10);
        
        Label lbFile = new Label();
        lbFile.setPrefSize(100, 15);
        lbFile.setText(i18n.tr("File name")+" ");
        lbFile.setPadding(new Insets(5, 0, 0, 0));
        txFile = new TextField();        
        txFile.setPrefWidth(100);        
        hbFile.getChildren().addAll(lbFile, txFile);         
         
        chXcp.setText(i18n.tr("Logfly Xcp format"));
        chXcp.setSelected(true);
         
        chOzi.setText(i18n.tr("Ozi format"));
        chOzi.setSelected(false);  
        
        chCompe.setText(i18n.tr("CompeGPS format"));
        chCompe.setSelected(false);
          
        chPcx.setText(i18n.tr("PCX5 format"));
        chPcx.setSelected(false);
          
        chKml.setText(i18n.tr("Kml format"));
        chKml.setSelected(false);
         
        chGpx.setText(i18n.tr("Gpx format"));
        chGpx.setSelected(false);
         
        chCup.setText(i18n.tr("Cup format"));
        chCup.setSelected(false);
        
        vbCheck.getChildren().addAll(hbFile,chXcp,chOzi,chCompe,chPcx,chKml,chGpx,chCup);
        
        // Boutons
        HBox buttonBar = new HBox();
        buttonBar.setPadding(new Insets(6));
        buttonBar.setSpacing(5);
        buttonBar.setAlignment(Pos.CENTER_RIGHT);
        Button btSend = new Button(i18n.tr("Save"));
        btSend.setOnAction((event) -> {
            checkAndSave();
        });
        Button btClose = new Button(i18n.tr("Cancel"));
        btClose.setOnAction((event) -> {            
            subStage.close();
        });
        buttonBar.getChildren().addAll(btClose, btSend );  
        
        // La Vbox qui va contenir chacun des éléments horizontaux définis ci dessus
        final VBox vbox = new VBox();
        vbox.setPadding(new Insets(10));
        vbox.setSpacing(10);        
        
        vbox.getChildren().addAll(lbType,lbScore,lbPrefix, hbPrefix,lbWayp,hbWayp,lbSave,vbCheck,buttonBar);        
        StackPane subRoot = new StackPane();
        subRoot.getChildren().add(vbox);
        
        Scene secondScene = new Scene(subRoot, 470, 600);

        // modal mode
        subStage.initModality(Modality.APPLICATION_MODAL);        
        subStage.setScene(secondScene);     
        subStage.showAndWait();   
    }              

}
