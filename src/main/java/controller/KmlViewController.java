/*
 * Copyright Gil THOMAS
 * Ce fichier fait partie intégrante du projet Logfly
 * Pour tous les détails sur la licence du projet Logfly
 * Consulter le fichier LICENSE distribué avec le code source
 */
package controller;

import dialogues.alertbox;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import kml.makingKml;
import org.xnap.commons.i18n.I18n;
import org.xnap.commons.i18n.I18nFactory;
import settings.configProg;
import trackgps.traceGPS;

/**
 * FXML Controller class
 *
 * @author gil
 */
public class KmlViewController implements Initializable {
    @FXML
    private CheckBox checkReduc;
    
    @FXML
    private CheckBox checkTrace;
    
    @FXML
    private CheckBox checkColorAlti;
    
    @FXML
    private CheckBox checkColorVario;
    
    @FXML
    private CheckBox checkColorSpeed;
       
    @FXML
    private CheckBox checkScore;
    
    @FXML
    private CheckBox checkThermiq;

    @FXML
    private CheckBox checkReplay;
    
    @FXML
    private TextField txCamStep;
    
    @FXML
    private TextField txCamDessus;
    
     @FXML
    private TextField txCamTimer;
    
    @FXML
    private TextField txCamRecul;
    
    @FXML
    private TextField txCamIncli;
    
    @FXML
    private CheckBox checkSave;
    
    @FXML
    private CheckBox checkMail;

    @FXML
    private CheckBox checkRunGE;
    
    @FXML
    private Label lbKmlPath;
    
    @FXML
    private Label lbTitle;
    
    @FXML
    private Label lbEchantillon;
    
    @FXML
    private Label lbDessus;
    
    @FXML
    private Label lbTimer;
    
    @FXML
    private Label lbRecul;
    
    @FXML
    private Label lbInclinaison;
    
    @FXML
    private RadioButton rdAltGPS;
    
    @FXML
    private RadioButton rdAltBaro;
    
    @FXML
    private ToggleGroup altitude;
    
    @FXML
    private Button btAnnuler;
    
    @FXML
    private Button btValider;
    
            
    private CarnetViewController carnetController;
    private TraceViewController extController;
    
    private String typeAlti;
    
    private Stage dialogStage;
    private int appel;
        
    configProg myConfig;
    
    private static I18n i18n;
    
    traceGPS kmlTrace;

    public void setAppel(int appel, configProg currConfig) {
        // 1 c'est CarnetViewController qui appelle
        // 2 c'est TraceViewController qui appelle
        this.appel = appel;
        myConfig = currConfig;
        i18n = I18nFactory.getI18n(Logfly.Main.class.getClass(),myConfig.getLocale());
        // Traduction des expressions
        winTraduction(); 
        iniTitre();  
    }
        
    
    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {               
        // Initialisation valeurs par défaut    
        rdAltGPS.setSelected(true);  
        rdAltGPS.setUserData("GPS");
        rdAltBaro.setUserData("Baro");
        checkReduc.setSelected(true);
        checkTrace.setSelected(true);
        checkColorAlti.setSelected(true);
        checkColorVario.setSelected(true);
        checkColorSpeed.setSelected(true);
        checkThermiq.setSelected(true);
        checkReplay.setSelected(true);
        checkRunGE.setSelected(true);        
        // On prépare un formatter pour limiter les textfields à la saisie numérique
        // vu sur http://www.javaworld.com/article/2991463/learn-java/javafx-improvements-in-java-se-8u40.html?page=4
        StringConverter<Integer> formatter;
        formatter = new StringConverter<Integer>()
        {
           @Override
           public Integer fromString(String string)
           {
              return Integer.parseInt(string);
           }

           @Override
           public String toString(Integer object)
           {
              if (object == null)
                 return "0";              
              return object.toString();
           }
        };
        txCamStep.setTextFormatter(new TextFormatter<Integer>(formatter, 120));
        txCamDessus.setTextFormatter(new TextFormatter<Integer>(formatter,50));
        txCamTimer.setTextFormatter(new TextFormatter<Integer>(formatter,5));
        txCamRecul.setTextFormatter(new TextFormatter<Integer>(formatter,100));
        txCamIncli.setTextFormatter(new TextFormatter<Integer>(formatter,70));        
        lbKmlPath.setText(null);
                
        altitude.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
            public void changed(ObservableValue<? extends Toggle> ov,
              Toggle old_toggle, Toggle new_toggle) {
            if (altitude.getSelectedToggle() != null) {
                typeAlti = altitude.getSelectedToggle().getUserData().toString();
            }
            }
        });
        
    }    
    
    /**
     * Sets the stage of this dialog.
     *
     * @param dialogStage
     */
    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }
    
    /**
     * Appellée pour obtenir un pont de communication avec CarnetViewController 
     * @param callExterne 
     */
    public void setCarnetBridge(CarnetViewController callCarnet)  {
        this.carnetController = callCarnet;   
        kmlTrace = carnetController.currTrace;     
    }
    
    /**
     * Appellée pour obtenir un pont de communication avec TraceViewController 
     * @param callExterne 
     */
    public void setTraceBridge(TraceViewController callExterne)  {
        this.extController = callExterne;  
        kmlTrace = extController.extTrace;      
    }
    
    private void iniTitre() {
        StringBuilder sbTitre = new StringBuilder();
        sbTitre.append(kmlTrace.getsDate_Vol()).append(" ");
        sbTitre.append(kmlTrace.getsDuree_Vol()).append(" ");
        sbTitre.append(String.valueOf(kmlTrace.getNbPoints())).append(" ").append(i18n.tr("Points"));
        lbTitle.setText(sbTitre.toString());
    }
    
    @FXML
    private void checkSaveState() throws InterruptedException {
        if (checkSave.isSelected() )
            selectDiskKml();
        else
            lbKmlPath.setText(null);
    }
    
    @FXML
    private void selectDiskKml() throws InterruptedException {
        // Création du nom par défaut du fichier kml
        // Composition du nom par défaut de la trace
        String suggName1 = kmlTrace.getDate_Vol_SQL().replaceAll("-","_");
        String suggName2 = suggName1.replaceAll(":","_");
        String suggName = suggName2.replaceAll(" ","_");
        // Il y a un problème s'il y a un point dans l'expression comme Gégé avait fait -> sPilote = G. LEGRAS...        
        String suggPilote;
        String finalName;
        String sPilote = kmlTrace.getsPilote();
        if (sPilote != null && !sPilote.equals(""))  {
            // Il y a un problème s'il y a un point dans l'expression [le pilote saisit -> "G. DUPOND"]        
            String suggPilote1 = sPilote.replaceAll("\\.","_");
            suggPilote = suggPilote1.replaceAll(" ","_");      
            finalName = suggPilote+"_"+suggName+".kml"; 
        } else {
            suggPilote = "";
            finalName = suggName+".kml"; 
        }        
        
        FileChooser fileChooser = new FileChooser();
        fileChooser.setInitialFileName(finalName);
        File savedFile = fileChooser.showSaveDialog(dialogStage);
        if (savedFile != null) {
            String sPath = savedFile.getAbsolutePath();
            if (!sPath.toLowerCase().contains(".kml")) sPath = sPath + ".kml";
            lbKmlPath.setText(sPath);  
            lbKmlPath.setVisible(true);
            if ( !checkSave.isSelected()) checkSave.setSelected(true);
        }                
    }
    
    /**
     * Valide les paramètres de génération
     */
    @FXML
    private void handleOk() throws IOException {
        makingKml currKml = new makingKml();
        // Les valeurs saisies sont obligatoirement des entiers
        currKml.setCamStep(Integer.parseInt(txCamStep.getText()));  
        currKml.setCamDessus(Integer.parseInt(txCamDessus.getText()));                          
        currKml.setCamTimer(Integer.parseInt(txCamTimer.getText()));   
        currKml.setCamRecul(Integer.parseInt(txCamRecul.getText()));   
        currKml.setCamIncli(Integer.parseInt(txCamIncli.getText()));   
        
        currKml.setUseReduc(checkReduc.isSelected());        
        currKml.setTraceSimple(checkTrace.isSelected());  
        currKml.setDrawScore(checkScore.isSelected());  
        currKml.setColorByAlti(checkColorAlti.isSelected());   
        currKml.setColorByVario(checkColorVario.isSelected());   
        currKml.setColorBySpeed(checkColorSpeed.isSelected());   
        currKml.setDrawThermal(checkThermiq.isSelected());   
        currKml.setReplay(checkReplay.isSelected());   
        currKml.setRunGE(checkRunGE.isSelected());     
        currKml.setExport(checkSave.isSelected());
        currKml.setMail(checkMail.isSelected());      
        currKml.setExportPath(lbKmlPath.getText());
        if (typeAlti == "Baro") 
            currKml.setGraphAltiBaro(true);
        else
            currKml.setGraphAltiBaro(false);   
        switch (appel) {
            case 1:
                carnetController.configKml(currKml); 
                break;
            case 2:
                extController.configKml(currKml); 
                break;
        }         
        dialogStage.close();
    }
        
    
    /**
     * Abanadon génération fichier kml
     */
    @FXML
    private void handleCancel() {
        dialogStage.close();
    }

    private void winTraduction() {
        checkReduc.setText(i18n.tr("Réduction du nombre de points"));
        checkTrace.setText(i18n.tr("Trace avec profil"));
        checkColorAlti.setText(i18n.tr("Trace colorée par altitude"));
        checkColorVario.setText(i18n.tr("Trace colorée par vario"));
        checkColorSpeed.setText(i18n.tr("Trace colorée par vitesse"));
        checkScore.setText(i18n.tr("Score"));
        checkThermiq.setText(i18n.tr("Thermiques"));
        checkReplay.setText(i18n.tr("Replay"));
        checkSave.setText(i18n.tr("Exporter le fichier Google Earth"));
        checkMail.setText(i18n.tr("Envoyer par mail"));
        checkRunGE.setText(i18n.tr("Lancer Google Earth"));
        lbEchantillon.setText(i18n.tr("Echantillonnage de points"));
        lbDessus.setText(i18n.tr("Distance au dessus de la trace"));
        lbTimer.setText(i18n.tr("Timer de la caméra"));
        lbRecul.setText(i18n.tr("Recul de la camera"));
        lbInclinaison.setText(i18n.tr("Inclinaison caméra"));
        rdAltGPS.setText(i18n.tr("Alti GPS"));
        rdAltBaro.setText(i18n.tr("Alti Baro"));
        btAnnuler.setText(i18n.tr("Annuler"));
        btValider.setText(i18n.tr("Valider"));
    }
    
}
