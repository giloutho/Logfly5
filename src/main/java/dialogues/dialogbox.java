/*
 * Copyright Gil THOMAS
 * Ce fichier fait partie intégrante du projet Logfly
 * Pour tous les détails sur la licence du projet Logfly
 * Consulter le fichier LICENSE distribué avec le code source
 */
package dialogues;

import java.util.Optional;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;

/**
 *
 * @author Gil Thomas logfly.org
 * Toutes les explications sur http://code.makery.ch/blog/javafx-dialogs-official/
 */
public class dialogbox {
         
    public boolean YesNo(String msgH, String msgT )  {
        
        boolean answer = false;
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(null);
        alert.setHeaderText(msgH);
        alert.setContentText(msgT);
        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() == ButtonType.OK){
            answer = true;                                  
        } else {
            answer = false;
        }
        
        return answer;
    }
    
    /**
     * Dialogbox pour deux choix possibles + abandon
     * @param msgH  Header
     * @param msgT  Texte dans la boite
     * @param choice1 Libellé Premier choix
     * @param choice2 Libellé deuxième choix
     * @param sCancel Libellé abandon
     * @return 
     */
    public int twoChoices(String msgH, String msgT, String choice1, String choice2, String sCancel )  {
        
        int res = 0;
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(null);
        alert.setHeaderText(msgH);
        alert.setContentText(msgT);
        ButtonType buttonTypeOne = new ButtonType(choice1);
        ButtonType buttonTypeTwo = new ButtonType(choice2);
        ButtonType buttonTypeCancel = new ButtonType(sCancel, ButtonData.CANCEL_CLOSE);
        alert.getButtonTypes().setAll(buttonTypeOne, buttonTypeTwo, buttonTypeCancel);
        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() == buttonTypeOne){
            res = 1;
        } else if (result.get() == buttonTypeTwo) {
            res = 2;
        } else {
            res = 0;
        }
        
        return res;
    }
    
}
