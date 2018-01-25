/*
 * Copyright Gil THOMAS
 * This file forms an integral part of Logfly project
 * See the LICENSE file distributed with source code
 * for details of Logfly licence project
 */
package controller;

import Logfly.Main;
import dialogues.alertbox;
import igc.pointIGC;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import leaflet.map_markers_coord;
import netscape.javascript.JSObject;
import org.xnap.commons.i18n.I18n;
import org.xnap.commons.i18n.I18nFactory;
import settings.configProg;
import systemio.mylogging;

/**
 *
 * @author gil
 */
public class SiteFormController {
    
    @FXML
    private Label lbNom;
    @FXML
    private TextField txNom;    
    @FXML
    private Label lbOrien;
    @FXML
    private TextField txOrien;
    @FXML
    private Label lbLocalite;
    @FXML
    private TextField txLocalite;
    @FXML
    private Label lbCP;
    @FXML
    private TextField txCP;       
    @FXML
    private Label lbPays;
    @FXML
    private RadioButton rdDeco;    
    @FXML
    private RadioButton rdAttero;    
    @FXML
    private TextField txPays;  
    @FXML
    private Label lbLat;
    @FXML
    private TextField txLat;
    @FXML
    private Label lbLong;
    @FXML
    private TextField txLong;    
    @FXML
    private Button btRefreshMap;     
    @FXML
    private Label lbAlt;
    @FXML
    private TextField txAlt;   
    @FXML
    private Label lbComment;
    @FXML
    private TextArea txComment; 
    @FXML
    private WebView mapViewer;    
    @FXML
    private Label lbMarqueur;
    @FXML
    private Button btCancel;
    @FXML
    private Button btOk;    
    
    ToggleGroup rdGroup;
    
    // Reference to SiteViewController
    private SitesViewController siteController;
    
    private Stage dialogStage;    
    
    // Localization
    private I18n i18n; 
    
    // bridge between java code and javascript map
    private final Bridge pont = new Bridge();
    
    // Settings
    private configProg myConfig;
    private StringBuilder sbError;
    private int editMode;
    private String idSite;
    private String oldName;
    private double latDep;
    private double longDep;
    
    
    //START | SQLITE
    private static Statement stat;
    private PreparedStatement prep;
    //END | SQLITE  
    
    @FXML
    private void initialize() {
        // We need to intialize i18n before TableView building
        // For this reason we put building code in iniTable() 
        // This procedure will be called after setMainApp()   
        rdGroup = new ToggleGroup();    
        rdDeco.setToggleGroup(rdGroup);        
        rdAttero.setToggleGroup(rdGroup);       
    }  
    
    /**
     * Define Edit conditions
     * ModeEdit = 0 Modification d'une fiche
     * ModeEdit = 1 Création d'une fiche depuis la liste des sites
     * ModeEdit = 2 Modification d'une fiche dynamiquement depuis le carnet.  ' Genre Site Noxx à renommer
     * ModeEdit = 3 Création d'une fiche dynamiquement depuis le carnet avec les coord déco  '  Option Site différent
     * @param mainConfig
     * @param pIdSite
     * @param modeEdit 
     */
    public void setEditForm(configProg mainConfig, String pIdSite, int modeEdit) {
        this.myConfig = mainConfig;
        this.idSite = pIdSite;
        this.editMode = modeEdit;
        i18n = I18nFactory.getI18n("","lang/Messages",SiteFormController.class.getClass().getClassLoader(),myConfig.getLocale(),0);
        winTraduction();
        iniForm();
    }
    
    private void iniForm() {
            
        ResultSet rs = null;
        Statement stmt = null;
        
        String sReq = "SELECT * FROM Site WHERE S_ID = "+idSite;
        try {        
            stmt = myConfig.getDbConn().createStatement();
            rs =  stmt.executeQuery(sReq);
            if (rs != null)  { 
                txNom.setText(rs.getString(2));
                oldName = rs.getString(2);
                txLocalite.setText(rs.getString(3));
                txCP.setText(rs.getString(4));
                txPays.setText(rs.getString(5));
                txOrien.setText(rs.getString(7));
                txAlt.setText(rs.getString(8));
                txLat.setText(rs.getString(9));
                latDep = rs.getDouble(9);
                txLong.setText(rs.getString(10));
                longDep = rs.getDouble(10);
                txComment.setText(rs.getString(11));
                switch (rs.getString(6)) {
                    case "A":
                        rdAttero.setSelected(true);
                        break;
                    case "D":
                        rdDeco.setSelected(true);
                        break;
                }
                setUpdateDate(rs.getString(12));
                iniMap();
               // debugMap();
            }
        } catch ( Exception e ) {
            alertbox aError = new alertbox(myConfig.getLocale());
            aError.alertError(e.getClass().getName() + ": " + e.getMessage());  
            sbError = new StringBuilder(this.getClass().getName()+"."+Thread.currentThread().getStackTrace()[1].getMethodName());
            sbError.append("\r\n").append(e.getMessage());
            mylogging.log(Level.SEVERE, sbError.toString());            
        } finally {
            try{
                rs.close(); 
                stmt.close();
            } catch(Exception e) { } 
        }       
    }
    
    /**
     * date format must be checked, in very old versions format is dd/mm/yy
     * @param dateStr 
     */
    private void setUpdateDate(String dateStr) {
        DateTimeFormatter formatterSQL = DateTimeFormatter.ofPattern("yyyy-MM-dd");          
        Pattern dayDate = Pattern.compile("\\d{4}-\\d{2}-\\d{2}");
        Matcher matchDay = dayDate.matcher(dateStr);        
        try {        
            if(matchDay.find()) {          
                // Direct parsing is possible because we have default ISO_LOCAL_DATE format
                LocalDate localDate = LocalDate.parse(dateStr);
                dialogStage.setTitle(i18n.tr("Date de mise à jour : "+localDate));                
            } 
        } catch (Exception e) {
            sbError = new StringBuilder(this.getClass().getName()+"."+Thread.currentThread().getStackTrace()[1].getMethodName());
            sbError.append("\r\n").append(e.getMessage());
            mylogging.log(Level.SEVERE, sbError.toString()); 
        }        
    }    
    
    private void debugMap() {
        try {
            File f = new File("site3.html");
            String sHTML = new String(Files.readAllBytes(Paths.get(f.getAbsolutePath())));
            mapViewer.getEngine().loadContent(sHTML,"text/html");   
            // On passe maintenant l'objet java au formulaire. 
            final JSObject jsobj = (JSObject) mapViewer.getEngine().executeScript("window"); 
            jsobj.setMember("java", pont); 
        } catch (Exception e) {
            e.printStackTrace();
        }
  
        
    }
    
    private void iniMap() {
        
        pointIGC pPoint1 = new pointIGC();      
        double dLatitude = latDep;
        if (latDep > 90 || latDep < -90) dLatitude = 0;
        pPoint1.setLatitude(dLatitude);
        double dLongitude = longDep;
        if (dLongitude > 180 || dLongitude < -180) dLongitude = 0;
        pPoint1.setLongitude(dLongitude);
        pPoint1.setAltiGPS(Integer.parseInt(txAlt.getText()));
        map_markers_coord myMap = new map_markers_coord(i18n, myConfig.getIdxMap(), pPoint1); 
        if (myMap.isMap_OK()) {   
            // with first maps, we made this            
            // mapViewer.getEngine().load("about:blank");                     
            // to delete cache for navigate back
            // with this bridge (jsobject) does not work
            mapViewer.getEngine().loadContent(myMap.getMap_HTML());   
            // java object is sent to webwiew
            final JSObject jsobj = (JSObject) mapViewer.getEngine().executeScript("window"); 
            jsobj.setMember("java", pont); 
        }
        
    }

    private void updateDb() {
        
        String siteType;
        StringBuilder sbReq = new StringBuilder();   
        String Quote = "'";
        boolean badCoord;
        LocalDate today = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("YYYY-MM-dd");
        
        if (rdDeco.isSelected()) 
            siteType = "A";
        else
            if (rdDeco.isSelected())
                siteType = "D";
            else
                siteType = "''";
        
        if (txLat.getText().trim().equals("")) 
            badCoord = true;
        else if (txLong.getText().trim().equals(""))
            badCoord = true;
        else
            badCoord = false;
  
        // editMode  = 0 modification of a site form
        // editMode = 1 form creation from site list
        // editMode = 2 modifying a form dynamically from the logbook like Site Noxx à renommer
        // editMode = 3 Creating a form dynamically from the logbook with take off coordinates (option different site)
        
        if (!badCoord) {
            String sNom = txNom.getText();
            String sLocalite = txLocalite.getText();
            String sPays = txPays.getText();
            String sComment = txComment.getText();
            PreparedStatement pstmt = null;
            String sReq = "";
            try {
                if (editMode == 0 || editMode == 2) {
                    sReq = "UPDATE Site SET S_Nom=?, S_Localite=?, S_CP=?, S_Pays=?, S_Type=?, S_Orientation=?, S_Alti=?, S_Latitude=?, S_Longitude=?, S_Commentaire=?, S_Maj=? WHERE S_ID =?";  
                } else {
                    sReq = "INSERT INTO Site (S_Nom,S_Localite,S_CP,S_Pays,S_Type,S_Orientation,S_Alti,S_Latitude,S_Longitude,S_Commentaire,S_Maj) VALUES (?,?,?,?,?,?,?,?,?,?,?)";
                }
                pstmt = myConfig.getDbConn().prepareStatement(sReq);
                pstmt.setString(1,sNom); 
                pstmt.setString(2,sLocalite);
                pstmt.setString(3,txCP.getText());
                pstmt.setString(4,sPays);
                pstmt.setString(5,siteType);
                pstmt.setString(6,txOrien.getText());
                pstmt.setString(7,txAlt.getText());
                pstmt.setString(8,txLat.getText());
                pstmt.setString(9,txLong.getText());
                pstmt.setString(10,sComment);
                pstmt.setString(11,today.format(formatter));
                pstmt.setInt(12, Integer.valueOf(idSite));
                pstmt.executeUpdate(); 
            } catch (Exception e) {
                alertbox aError = new alertbox(myConfig.getLocale());
                aError.alertError(e.getClass().getName() + ": " + e.getMessage());  
                sbError = new StringBuilder(this.getClass().getName()+"."+Thread.currentThread().getStackTrace()[1].getMethodName());
                sbError.append("\r\n").append(e.getMessage());
                mylogging.log(Level.SEVERE, sbError.toString()); 
            }                       
        }
    }
    
    /**
     * valid changes
     */
    @FXML
    private void handleOk() {
        updateDb();
        dialogStage.close();
    }      
    
    /**
     * Discard changes
     */
    @FXML
    private void handleCancel() {
        dialogStage.close();
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
     * Initialize communication brdige with SitesViewController 
     * @param pSiteController 
     */
    public void setSiteBridge(SitesViewController pSiteController) {
        this.siteController = pSiteController;        
    }    

    /**
     * Translate labels of the window
     */
    private void winTraduction() {
                  
        lbNom.setText(i18n.tr("Nom"));
        lbOrien.setText(i18n.tr("Orientation"));   
        lbLocalite.setText(i18n.tr("Localité"));   
        lbCP.setText(i18n.tr("Code Postal"));   
        lbPays.setText(i18n.tr("Pays"));   
        rdDeco.setText(i18n.tr("Décollage"));     
        rdAttero.setText(i18n.tr("Atterissage"));       
        lbLat.setText(i18n.tr("Latitude"));   
        lbLong.setText(i18n.tr("Longitude"));   
        lbAlt.setText(i18n.tr("Alti déco"));   
        lbComment.setText(i18n.tr("Commentaire"));   
        lbMarqueur.setText(i18n.tr("Déplacer le marqueur avec un clic maintenu pour actualiser les coordonnées"));
        btCancel.setText(i18n.tr("Annuler"));  
        btOk.setText(i18n.tr("Valider"));          
    }    
    
    public class Bridge { 
  
        public void setLatitude(String value) { 
            txLat.setText(value);            
        } 
  
        public void setLongitude(String value) { 
            txLong.setText(value);
        } 
        
        public void setAltitude(String value) { 
            txAlt.setText(value);
        }        
    }    
    
}
