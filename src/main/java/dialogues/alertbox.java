/*
 * Copyright Gil THOMAS
 * Ce fichier fait partie intégrante du projet Logfly
 * Pour tous les détails sur la licence du projet Logfly
 * Consulter le fichier LICENSE distribué avec le code source
 */
package dialogues;

import javafx.scene.control.Alert;
import org.xnap.commons.i18n.I18n;
import org.xnap.commons.i18n.I18nFactory;

/**
 *
 * @author Gil Thomas logfly.org
 *  Toutes les explications sur http://code.makery.ch/blog/javafx-dialogs-official/
 */
public class alertbox {
    
    private I18n i18n = I18nFactory.getI18n(alertbox.class.getClass());
    
    public void alertError (String msg)  {
        // On extraiera la langue à employer d'un fichier de config
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(i18n.tr("Erreur programme"));
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
    
    /**
     * Renvoie le libellé d'une erreur après recherche dans une table
     * On garde des nombres inférieurs pour des erreurs système génériques
     * A partir de 1000 ce sont des codes Logfly précis
     * Grâce à cette procédure, la procédure qui renvoie l'erreur n'est pas obligé d'embarquer i18n
     * @param noError 
     */
    public void alertNumError (int noError)  {
        // On extraiera la langue à employer d'un fichier de config
        
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
            case 102:    
                msg = i18n.tr("Problème de lecture sur le carnet de vol");                          
                break;
            case 1001:
                msg = i18n.tr("Module Points non trouvé...");                          
                break;
            case 1002:
                msg = i18n.tr("JSON Score incorrect...");                          
                break;
            case 1012:
                // Generation Kml
                msg = i18n.tr("Erreur pendant la génération de la trace simple");                          
                break;
            case 1014:
                // Generation Kml
                msg = i18n.tr("Erreur pendant la génération de la trace colorée par vario");                          
                break;
            case 1016:
                // Generation Kml
                msg = i18n.tr("Erreur pendant la génération de la trace colorée par altitude");                          
                break;
            case 1018:
                // Generation Kml
                msg = i18n.tr("Erreur pendant la génération de la trace colorée par vitesse");                          
                break;
            case 1020:
                // Generation Kml
                msg = i18n.tr("Erreur pendant la génération de la trace du score");                          
                break;
            case 1022:
                // Generation Kml
                msg = i18n.tr("Erreur pendant la génération de la trace des thermiques");                          
                break;
            case 1024:
                // Generation Kml
                msg = i18n.tr("Erreur pendant la génération de la trace du replay");                          
                break;
            case 1030:                
                msg = i18n.tr("Impossible de lancer Google Earth");                          
                break;
            case 1042:                
                msg = i18n.tr("Problème sur le décodage de la photo");                          
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
    
    public void alertInfo (String msg)  {
        // On extraiera la langue à employer d'un fichier de config
        i18n = I18nFactory.getI18n(alertbox.class.getClass());
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(i18n.tr("Information importante"));
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
     
}
