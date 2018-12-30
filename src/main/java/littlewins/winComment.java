/* 
 * Copyright Gil THOMAS
 * This file forms an integral part of Logfly project
 * See the LICENSE file distributed with source code
 * for details of Logfly licence project
 */
package littlewins;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.xnap.commons.i18n.I18n;

/**
 *
 * @author gil
 * 
 * For simple windows, we don't use SceneBuilder
 * 
 */
public class winComment {
    private String commentTxt;
    private boolean Modif;
    private I18n i18n; 
    
    public winComment(String strComment, I18n pI18n) {
        setCommentTxt(strComment);
        setModif(false); 
        setI18n(pI18n);
        showComment();
    }

    public void setI18n(I18n i18n) {
        this.i18n = i18n;
    }
    
    public String getCommentTxt() {
        return commentTxt;
    }

    public void setCommentTxt(String commentTxt) {
        this.commentTxt = commentTxt;
    }

    public boolean isModif() {
        return Modif;
    }

    public void setModif(boolean Modif) {
        this.Modif = Modif;
    }
       
    private void showComment() {
        
        Stage subStage = new Stage();
        
        final VBox vbox = new VBox();
        vbox.setPadding(new Insets(10));
        vbox.setSpacing(5);
        
        TextArea txtComment = new TextArea();
        txtComment.setWrapText(true);
        txtComment.setText(commentTxt);
        // for buttons
        HBox buttonBar = new HBox();
        buttonBar.setPadding(new Insets(6));
        buttonBar.setSpacing(5);
        buttonBar.setAlignment(Pos.CENTER_RIGHT);
        Button btSave = new Button(i18n.tr("OK"));
        btSave.setOnAction((event) -> {
            setModif(true);
            setCommentTxt(txtComment.getText());
            subStage.close();
        });
        Button btCancel = new Button(i18n.tr("Cancel"));
        btCancel.setOnAction((event) -> {
            setModif(false);
            subStage.close();
        });
        buttonBar.getChildren().addAll(btCancel, btSave);
        
        vbox.getChildren().addAll(txtComment, buttonBar);
        
        StackPane subRoot = new StackPane();
        subRoot.getChildren().add(vbox);
        
        Scene secondScene = new Scene(subRoot, 500, 250);
        // modal mode
        subStage.initModality(Modality.APPLICATION_MODAL);        
        subStage.setScene(secondScene);     
        subStage.showAndWait();
    }
    
}
