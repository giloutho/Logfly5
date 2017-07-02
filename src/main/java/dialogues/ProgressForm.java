/*
 * Copyright Gil THOMAS
 * Ce fichier fait partie intégrante du projet Logfly
 * Pour tous les détails sur la licence du projet Logfly
 * Consulter le fichier LICENSE distribué avec le code source
 */
package dialogues;

import javafx.concurrent.Task;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

/**
 *
 * @author James_D / stackoverflow
 * http://stackoverflow.com/questions/29625170/display-popup-with-progressbar-in-javafx
 * Implantation d'une progressbar qui se ferme quand le process est terminé
 * 
 * En cas de besion un autre exemple excellent
 * http://stackoverflow.com/questions/30980138/javafx-indeterminate-progress-bar-while-doing-a-process
 * 
 */

public class ProgressForm {
    
    private final Stage dialogStage;
    private final ProgressBar pb = new ProgressBar();
    private final ProgressIndicator pin = new ProgressIndicator();
    
    public ProgressForm() {
            dialogStage = new Stage();
            dialogStage.initStyle(StageStyle.UTILITY);
            dialogStage.setResizable(false);
            dialogStage.initModality(Modality.APPLICATION_MODAL);
          //  dialogStage.setTitle(winTitle);

            // PROGRESS BAR            
            pb.setProgress(-1F);
            pin.setProgress(-1F);

            final HBox hb = new HBox();
            hb.setSpacing(5);
            hb.setAlignment(Pos.CENTER);
            hb.getChildren().addAll(pb, pin);

            Scene scene = new Scene(hb);
            dialogStage.setScene(scene);
        }
    
        public void activateProgressBar(final Task<?> task)  {
            pb.progressProperty().bind(task.progressProperty());
            pin.progressProperty().bind(task.progressProperty());
            dialogStage.show();
        }

        public Stage getDialogStage() {
            return dialogStage;
        }    
    
}
