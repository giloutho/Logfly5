/* 
 * Copyright Gil THOMAS
 * This file forms an integral part of Logfly project
 * See the LICENSE file distributed with source code
 * for details of Logfly licence project
 */
package dialogues;

import java.util.Locale;
import javafx.scene.control.Alert;
import org.xnap.commons.i18n.I18n;
import org.xnap.commons.i18n.I18nFactory;

/**
 *
 * @author Gil Thomas logfly.org
 * from http://code.makery.ch/blog/javafx-dialogs-official/
 */
public class alertbox {
    
    private I18n i18n;
    
    public alertbox(Locale currLocale)  {
        i18n = I18nFactory.getI18n("","lang/Messages",alertbox.class.getClass().getClassLoader(),currLocale,0);
    }
    
    /**
     * Display an error message in a dialog box
     * @param msg 
     */
    public void alertError (String msg)  {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(i18n.tr("Program error"));
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
    
    /**
     * Display text error from a table 
     * lower numbers reserved for system errors
     * from 1000, this is special Logfly error codes
     * @param noError 
     */
    public void alertNumError (int noError)  {
        
        String msg;
        Alert.AlertType typeAlert = Alert.AlertType.ERROR;
        switch (noError) {
            case -1 :
                msg = i18n.tr("Undefined error");
                break;
            case 0 :
                typeAlert = Alert.AlertType.INFORMATION;
                msg = i18n.tr("Successful operation");
                break;
            case 1 :
                msg = i18n.tr("File not found");
                break;
            case 2:    
                msg = i18n.tr("File Output Input Problem");                          
                break;
            case 3:    
                msg = i18n.tr("File not found");                          
                break;
            case 4:    
                msg = i18n.tr("Parsing error");                          
                break;
            case 5:    
                msg = i18n.tr("NullPointerException error");                          
                break;
            case 6:    
                msg = i18n.tr("InterruptedException error");                          
                break;
            case 8:    
                msg = i18n.tr("Loading resource failed");                          
                break;
            case 9:    
                msg = i18n.tr("Unable to create temporary file");                          
                break;
            case 10:    
                msg = i18n.tr("No Internet connection");                          
                break;
            case 20:    
                msg = i18n.tr("Settings are not valid");                          
                break;                
            case 100:    
                msg = i18n.tr("Database connection failed");                          
                break;
            case 102:    
                msg = i18n.tr("Reading problem in logbook");                          
                break;
            case 202:    
                msg = i18n.tr("Unable to create AIRSP folder");                          
                break;   
            case 204:    
                msg = i18n.tr("Reverbin program not found");                          
                break;           
            case 206:    
                msg = i18n.tr("Reversale not connected");                          
                break;                    
            case 208:    
                msg = i18n.tr("FRIDX.Bin not generated");                          
                break;
            case 210:    
                msg = i18n.tr("FRDATA.Bin not generated");                          
                break;    
            case 212:    
                msg = i18n.tr("Error copying FRDATA. bin and FRIDX. bin");                          
                break;                   
            case 214:    
                msg = i18n.tr("FRDATA.bin and FRIDX.bin generation successful");                          
                break;           
            case 220:    
                msg = i18n.tr("Unable to create airspaces selection");                          
                break;         
            case 310:    
                msg = i18n.tr("Can't get response from web service");                          
                break;      
            case 320:    
                msg = i18n.tr("Unusable response from web service");                          
                break; 
            case 330:    
                msg = i18n.tr("Json response is null");                          
                break; 
            case 1000:
                // Error in reading the parameters
                msg = i18n.tr("Reading settings failed");                          
                break;
            case 1001:
                msg = i18n.tr("Points program not found");                          
                break;
            case 1002:
                msg = i18n.tr("Wrong JSON score");                          
                break;
            case 1012:
                // Kml generation
                msg = i18n.tr("Error during generation of simple track");                          
                break;
            case 1014:
                // Kml generation
                msg = i18n.tr("Error during generation of colored track by climb");                          
                break;
            case 1016:
                // Kml generation
                msg = i18n.tr("Error during generation of colored track by altitude");                          
                break;
            case 1018:
                // Kml generation
                msg = i18n.tr("Error during generation of colored track by speed");                          
                break;
            case 1020:
                // Kml generation
                msg = i18n.tr("Error during generation of scoring track");                          
                break;
            case 1022:
                // Kml generation
                msg = i18n.tr("Error during generation of thermals track");                          
                break;
            case 1024:
                // Kml generation
                msg = i18n.tr("Error during generation of track replay");                          
                break;
            case 1030:                
                msg = i18n.tr("Unable to run Google Earth");                          
                break;
            case 1042:                
                msg = i18n.tr("Decoding picture failed");                          
                break;    
            case 1052:                
                msg = i18n.tr("No GPS response");                          
                break;  
            case 1054:                
                msg = i18n.tr("Track file is empty");                          
                break;  
            case 1056:                
                msg = i18n.tr("No tracks in the GPS");                          
                break; 
            // Trace GPS decoding    
            case 1060 :
                 msg = i18n.tr("File Extension not recognized");                          
                break; 
            case 1062:
                msg = i18n.tr("Decoding problem in track file");                          
                break;                         
            case 1102:                
                msg = i18n.tr("Reading problem in flights file");                          
                break; 
            case 1104:                
                msg = i18n.tr("Inserting in the flights file failed");                          
                break; 
            case 1110 :                
                msg = i18n.tr("Flights must be on the same day");                          
                break;
            case 1112 :                
                msg = i18n.tr("Only one flight selected");                          
                break;
            case 1201:
                msg = i18n.tr("GpsDump program not found");                          
                break;             
            // upload igc file for VisuGPS    
            case 1301:
                msg = i18n.tr("Quota displaying VisuGPS exceeded");                          
                break;        
            case 1305:
                msg = i18n.tr("The file is too large");                          
                break;     
            case 1310:
                msg = i18n.tr("Error while transferring file");                          
                break;             
            case 1350:
                msg = i18n.tr("Decoding problem in Open Air file");                          
                break;         
            case 1370:
                msg = i18n.tr("Error during GeoJSON generation");                          
                break;             
            case 1380 :
                StringBuilder sbMsg = new StringBuilder();
                sbMsg.append(i18n.tr("Unable to generate GPX")).append(" : ").append("lsTracks ").append(i18n.tr("empty"));
                msg = i18n.tr(sbMsg.toString());                          
                break;                
            default:
                msg = i18n.tr("Undefined error");
        }        
        Alert alert = new Alert(typeAlert);        
        alert.setTitle(i18n.tr("Completed task"));
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
    
    /**
     * Display an information message in a dialog box
     * @param msg 
     */
    public void alertInfo (String msg)  {       
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(i18n.tr("Important information"));
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
    
    public void alertWithTitle (String sTitle, String msg)  {       
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(sTitle);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }    
     
}
