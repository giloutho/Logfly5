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
        alert.setTitle(i18n.tr("Erreur programme"));
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
                msg = i18n.tr("Erreur indéterminée");
                break;
            case 0 :
                typeAlert = Alert.AlertType.INFORMATION;
                msg = i18n.tr("Opération réussie");
                break;
            case 1 :
                msg = i18n.tr("Fichier non trouvé");
                break;
            case 2:    
                msg = i18n.tr("Problème d'entrée sortie sur fichier");                          
                break;
            case 3:    
                msg = i18n.tr("Fichier non trouvé");                          
                break;
            case 4:    
                msg = i18n.tr("Erreur de parsing");                          
                break;
            case 5:    
                msg = i18n.tr("Erreur NullPointerException");                          
                break;
            case 6:    
                msg = i18n.tr("Erreur InterruptedException");                          
                break;
            case 8:    
                msg = i18n.tr("Problème de chargement de la ressource");                          
                break;
            case 9:    
                msg = i18n.tr("Impossible de créer le ficher temporaire");                          
                break;
            case 10:    
                msg = i18n.tr("Pas de connection internet");                          
                break;
            case 20:    
                msg = i18n.tr("La configuration n'est pas valide");                          
                break;                
            case 100:    
                msg = i18n.tr("Connection base de données impossible");                          
                break;
            case 102:    
                msg = i18n.tr("Problème de lecture sur le carnet de vol");                          
                break;
            case 1000:
                // Error in reading the parameters
                msg = i18n.tr("Problème de lecture sur le fichier des paramètres");                          
                break;
            case 1001:
                msg = i18n.tr("Module Points non trouvé...");                          
                break;
            case 1002:
                msg = i18n.tr("JSON Score incorrect...");                          
                break;
            case 1012:
                // Kml generation
                msg = i18n.tr("Erreur pendant la génération de la trace simple");                          
                break;
            case 1014:
                // Kml generation
                msg = i18n.tr("Erreur pendant la génération de la trace colorée par vario");                          
                break;
            case 1016:
                // Kml generation
                msg = i18n.tr("Erreur pendant la génération de la trace colorée par altitude");                          
                break;
            case 1018:
                // Kml generation
                msg = i18n.tr("Erreur pendant la génération de la trace colorée par vitesse");                          
                break;
            case 1020:
                // Kml generation
                msg = i18n.tr("Erreur pendant la génération de la trace du score");                          
                break;
            case 1022:
                // Kml generation
                msg = i18n.tr("Erreur pendant la génération de la trace des thermiques");                          
                break;
            case 1024:
                // Kml generation
                msg = i18n.tr("Erreur pendant la génération de la trace du replay");                          
                break;
            case 1030:                
                msg = i18n.tr("Impossible de lancer Google Earth");                          
                break;
            case 1042:                
                msg = i18n.tr("Problème sur le décodage de la photo");                          
                break;    
            case 1052:                
                msg = i18n.tr("Pas de réponse du GPS");                          
                break;  
            case 1054:                
                msg = i18n.tr("Le fichier trace est vide");                          
                break;  
            case 1056:                
                msg = i18n.tr("Pas de traces dans le GPS");                          
                break; 
            // Trace GPS decoding    
            case 1060 :
                 msg = i18n.tr("Extension de fichier non reconnue");                          
                break; 
            case 1102:                
                msg = i18n.tr("Problème de lecture sur le fichier des vols");                          
                break; 
            case 1104:                
                msg = i18n.tr("Problème d'insertion dans le fichier des vols");                          
                break; 
            case 1110 :                
                msg = i18n.tr("Les vols doivent être du même jour");                          
                break;
            case 1112 :                
                msg = i18n.tr("Un seul vol sélectionné");                          
                break;
            case 1201:
                msg = i18n.tr("Module GpsDump non trouvé...");                          
                break;             
            // upload igc file for VisuGPS    
            case 1301:
                msg = i18n.tr("Quota affichage VisuGPS dépassé...");                          
                break;        
            case 1305:
                msg = i18n.tr("Le fichier est trop volumineux...");                          
                break;     
            case 1310:
                msg = i18n.tr("Erreur pendant le transfert du fichier...");                          
                break;             
            case 1350:
                msg = i18n.tr("Erreur de décodage du fichier Open Air...");                          
                break;         
            case 1370:
                msg = i18n.tr("Erreur de génération GeoJson");                          
                break;                  
            default:
                msg = i18n.tr("Erreur indéterminée");
        }        
        Alert alert = new Alert(typeAlert);        
        alert.setTitle(i18n.tr("Tâche terminée"));
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
        alert.setTitle(i18n.tr("Information importante"));
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
     
}
