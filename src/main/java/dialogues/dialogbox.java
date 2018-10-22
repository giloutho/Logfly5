/* 
 * Copyright Gil THOMAS
 * This file forms an integral part of Logfly project
 * See the LICENSE file distributed with source code
 * for details of Logfly licence project
 */
package dialogues;

import java.util.Locale;
import java.util.Optional;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.Region;

import org.xnap.commons.i18n.I18n;
import org.xnap.commons.i18n.I18nFactory;

/**
 *
 * @author Gil Thomas logfly.org
 * from http://code.makery.ch/blog/javafx-dialogs-official/
 */
public class dialogbox {    
    private static final int MIN_HEIGHT=100;
    private static final int MIN_WIDTH=200;
    
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
        ButtonType myOk = new ButtonType(i18n.tr("Oui"), ButtonBar.ButtonData.OK_DONE);
        ButtonType myCancel = new ButtonType(i18n.tr("Non"), ButtonBar.ButtonData.CANCEL_CLOSE);
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION,"",myOk,myCancel);        
        alert.setHeaderText(msgH);
        alert.setContentText(msgT);
        alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() == myOk){
            answer = true;                                  
        } else {
            answer = false;
        }
        
        return answer;
    }
    
    public void info(String header, String msg) 
    {  
      ButtonType btnOk = new ButtonType(i18n.tr("Ok"), ButtonBar.ButtonData.OK_DONE);
      Alert alert = new Alert(Alert.AlertType.INFORMATION, msg, btnOk);        
      alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
      alert.setHeaderText(header);
      alert.showAndWait();
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
