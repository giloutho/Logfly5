/* 
 * Copyright Gil THOMAS
 * This file forms an integral part of Logfly project
 * See the LICENSE file distributed with source code
 * for details of Logfly licence project
 */
package dialogues;

import java.util.Optional;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import org.xnap.commons.i18n.I18n;

/**
 *
 * @author Gil Thomas logfly.org
 * from http://code.makery.ch/blog/javafx-dialogs-official/
 */
public class dialogbox {    
    
    private I18n i18n; 

    public dialogbox(I18n pI18n) {
        this.i18n = pI18n;
    }
    
    /**
     * Dialog box with Yes or No
     * @param msgH
     * @param msgT
     * @return 
     */
    public boolean YesNo(String msgH, String msgT )  {
        
        boolean answer = false;
        ButtonType myOk = new ButtonType(i18n.tr("Yes"), ButtonBar.ButtonData.OK_DONE);
        ButtonType myCancel = new ButtonType(i18n.tr("No"), ButtonBar.ButtonData.CANCEL_CLOSE);
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION,"",myOk,myCancel);        
        alert.setHeaderText(msgH);
        alert.setContentText(msgT);
        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() == myOk){
            answer = true;                                  
        } else {
            answer = false;
        }
        
        return answer;
    }    
    
    /**
     * Dialog box with Yes or Cancel
     * @param msgH
     * @param msgT
     * @return 
     */
    public boolean YesCancel(String msgH, String msgT )  {
        
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
     * Dialogbox with two choices and cancel option
     * @param msgH  Header
     * @param msgT  Message text
     * @param choice1 First text choice
     * @param choice2 Second text choice
     * @param sCancel Cancel text
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
