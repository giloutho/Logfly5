/*
 * Copyright Gil THOMAS
 * This file forms an integral part of Logfly project
 * See the LICENSE file distributed with source code
 * for details of Logfly licence project
 */
package controller;

import dialogues.alertbox;
import dialogues.dialogbox;
import geoutils.position;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.paint.Paint;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.converter.DoubleStringConverter;
import javafx.util.converter.IntegerStringConverter;
import model.Balisemodel;
import org.xnap.commons.i18n.I18n;
import org.xnap.commons.i18n.I18nFactory;
import settings.configProg;
import systemio.mylogging;

/**
 *
 * @author gil
 */
public class CoordController {
    @FXML
    private Label lbDesc;
    @FXML
    private TextField txDesc;  
    @FXML
    private Label lbBalise;    
    @FXML
    private TextField txBalise;        
    @FXML
    private Label lbLat1;
    @FXML
    private Label lbLat2;    
    @FXML
    private Label lbLat3;    
    @FXML
    private Label lbSeparator;
    @FXML
    private TextField txLat;
    @FXML
    private TextField txDMLatDeg;  
    @FXML
    private TextField txDMLatMin; 
    @FXML
    private TextField txDMLatHem;    
    @FXML
    private TextField txDMSLatDeg;
    @FXML
    private TextField txDMSLatMin;
    @FXML
    private TextField txDMSLatSec;
    @FXML
    private TextField txDMSLatHem;
    @FXML
    private Label lbLong1;
    @FXML
    private Label lbLong2;
    @FXML
    private Label lbLong3;    
    @FXML
    private TextField txLong;
    @FXML
    private TextField txDMLongDeg;
    @FXML
    private TextField txDMLongMin;
    @FXML
    private TextField txDMLongMer;
    @FXML
    private TextField txDMSLongDeg;
    @FXML
    private TextField txDMSLongMin;
    @FXML
    private TextField txDMSLongSec;
    @FXML
    private TextField txDMSLongMer;
    @FXML
    private Button btCoord;   
    @FXML
    private Button btManual; 
    
    private WaypViewController waypController;
    private Balisemodel ba = new Balisemodel();
    private Stage dialogStage;  
    
    private String wBalise;
    private String wDesc;    
    
    // Localization
    private I18n i18n; 
    // Settings
    private configProg myConfig;
    private StringBuilder sbError;
    
    private DecimalFormatSymbols decimalFormatSymbols = new DecimalFormatSymbols();    
    private DecimalFormat df2;
    private DecimalFormat df3;
    private position displayPos;
    private boolean updateProgress;
    

    private Paint colorBadValue = Paint.valueOf("FA6C04");
    private Paint colorGoodValue = Paint.valueOf("FFFFFF");
    private Pattern validDoubleText = Pattern.compile("-?((\\d*)|(\\d+\\.\\d*))");
    private Pattern validIntText = Pattern.compile("-?(\\d*)");
    
    

    @FXML
    private void initialize() {
                  
        // ********  DD Fields ************
        // Listener are waiting for numeric values
        // BE CAREFUL to set numeric strings without spaces     
        TextFormatter<Double> fmTxLat = new TextFormatter<Double>(new DoubleStringConverter(), 0.0, 
            change -> {
                String newText = change.getControlNewText() ;
                if (validDoubleText.matcher(newText).matches()) {
                    return change ;
                } else return null ;
            });                
        fmTxLat.valueProperty().addListener((obs, oldValue, newValue) -> {            
                if (newValue < -90 || newValue > 90) {                      
                    txLat.setStyle("-fx-control-inner-background: #"+colorBadValue.toString().toString().substring(2));
                    txLat.requestFocus();                
                } else {
                    txLat.setStyle("-fx-control-inner-background: #"+colorGoodValue.toString().substring(2));  
                    displayPos.setLatitudeDd(newValue);
                    if (!updateProgress) updateFieldsPos();
                }            
        });    
        txLat.setTextFormatter(fmTxLat);
        
        TextFormatter<Double> fmTxLong = new TextFormatter<Double>(new DoubleStringConverter(), 0.0, 
            change -> {
                String newText = change.getControlNewText() ;
                if (validDoubleText.matcher(newText).matches()) {
                    return change ;
                } else return null ;
            });          
        fmTxLong.valueProperty().addListener((obs, oldValue, newValue) -> {            
                if (newValue < -180 || newValue > 180) {                      
                    txLong.setStyle("-fx-control-inner-background: #"+colorBadValue.toString().toString().substring(2));
                    txLong.requestFocus();                
                } else {
                    txLong.setStyle("-fx-control-inner-background: #"+colorGoodValue.toString().substring(2));  
                    displayPos.setLongitudeDd(newValue);
                    if (!updateProgress) updateFieldsPos();
                }            
        });                            
        txLong.setTextFormatter(fmTxLong); 
        
        // ********  DMm fields
        // Listener are waiting for numeric values
        // BE CAREFUL to set numeric strings without spaces  
        TextFormatter<Integer> fmTxDMLatDeg = new TextFormatter<Integer>(new IntegerStringConverter(), 0, 
            change -> {
                String newText = change.getControlNewText() ;
                if (validIntText.matcher(newText).matches()) {
                    return change ;
                } else return null ;
            });             
        
        fmTxDMLatDeg.valueProperty().addListener((obs, oldValue, newValue) -> {     
                if (newValue < -90 || newValue > 90) {                      
                    txDMLatDeg.setStyle("-fx-control-inner-background: #"+colorBadValue.toString().toString().substring(2));
                    txDMLatDeg.requestFocus();                
                } else {
                    txDMLatDeg.setStyle("-fx-control-inner-background: #"+colorGoodValue.toString().substring(2));  
                    displayPos.setLatDegres(newValue);
                    if (!updateProgress) updateFieldsPos();
                }            
        });                
        txDMLatDeg.setTextFormatter(fmTxDMLatDeg);  
        
        TextFormatter<Double> fmDMLatMin = new TextFormatter<Double>(new DoubleStringConverter(), 0.0, 
            change -> {
                String newText = change.getControlNewText() ;
                if (validDoubleText.matcher(newText).matches()) {
                    return change ;
                } else return null ;
            });                
        fmDMLatMin.valueProperty().addListener((obs, oldValue, newValue) -> {            
                if (newValue < 0 || newValue > 60) {                      
                    txDMLatMin.setStyle("-fx-control-inner-background: #"+colorBadValue.toString().toString().substring(2));
                    txDMLatMin.requestFocus();                
                } else {
                    txDMLatMin.setStyle("-fx-control-inner-background: #"+colorGoodValue.toString().substring(2));  
                    displayPos.setLatMin_mm(newValue);
                    if (!updateProgress) {
                        updateFieldsPos();
                    }
                }            
        });        
        txDMLatMin.setTextFormatter(fmDMLatMin);

        txDMLatHem.textProperty().addListener((observable, oldValue, newValue) -> {                        
            if (!newValue.equals("N") && !newValue.equals("S")) {
                txDMLatHem.setStyle("-fx-control-inner-background: #"+colorBadValue.toString().toString().substring(2));
                txDMLatHem.requestFocus();    
            } else {
               txDMLatHem.setStyle("-fx-control-inner-background: #"+colorGoodValue.toString().substring(2));   
            }
        });        
        
        TextFormatter<Integer> fmTxDMLongDeg = new TextFormatter<Integer>(new IntegerStringConverter(), 0,
                change -> {
                    String newText = change.getControlNewText();
                    if (validIntText.matcher(newText).matches()) {
                        return change;
                    } else {
                        return null;
                    }
                });

        fmTxDMLongDeg.valueProperty().addListener((obs, oldValue, newValue) -> {            
            if (newValue < -180 || newValue > 180) {
                txDMLongDeg.setStyle("-fx-control-inner-background: #" + colorBadValue.toString().toString().substring(2));
                txDMLongDeg.requestFocus();
            } else {
                txDMLongDeg.setStyle("-fx-control-inner-background: #" + colorGoodValue.toString().substring(2));
                displayPos.setLongDegres(newValue);
                if (!updateProgress) updateFieldsPos();
            }
        });         
        txDMLongDeg.setTextFormatter(fmTxDMLongDeg);
        
        TextFormatter<Double> fmDMLongMin = new TextFormatter<Double>(new DoubleStringConverter(), 0.0, 
            change -> {                                                                                                                                                                                                                                                                                                                                                                                                                       
                String newText = change.getControlNewText() ;
                if (validDoubleText.matcher(newText).matches()) {
                    return change ;
                } else return null ;
            });                
        fmDMLongMin.valueProperty().addListener((obs, oldValue, newValue) -> {            
                if (newValue < 0 || newValue > 60) {                      
                    txDMLongMin.setStyle("-fx-control-inner-background: #"+colorBadValue.toString().toString().substring(2));
                    txDMLongMin.requestFocus();                
                } else {
                    txDMLongMin.setStyle("-fx-control-inner-background: #"+colorGoodValue.toString().substring(2));  
                    displayPos.setLongMin_mm(newValue);
                    if (!updateProgress) updateFieldsPos();
                }            
        });        
        txDMLongMin.setTextFormatter(fmDMLongMin);     
        
        txDMLongMer.textProperty().addListener((observable, oldValue, newValue) -> {                        
            if (!newValue.equals("W") && !newValue.equals("E")) {
                txDMLongMer.setStyle("-fx-control-inner-background: #"+colorBadValue.toString().toString().substring(2));
                txDMLongMer.requestFocus();    
            } else {
               txDMLongMer.setStyle("-fx-control-inner-background: #"+colorGoodValue.toString().substring(2));   
            }
        });          

        //********** DMS Fields ****************
        TextFormatter<Integer> fmTxDMSLatDeg = new TextFormatter<Integer>(new IntegerStringConverter(), 0, 
            change -> {
                String newText = change.getControlNewText() ;
                if (validIntText.matcher(newText).matches()) {
                    return change ;
                } else return null ;
            });             
        
        fmTxDMSLatDeg.valueProperty().addListener((obs, oldValue, newValue) -> {     
                if (newValue < -90 || newValue > 90) {                      
                    txDMSLatDeg.setStyle("-fx-control-inner-background: #"+colorBadValue.toString().toString().substring(2));
                    txDMSLatDeg.requestFocus();                
                } else {
                    txDMSLatDeg.setStyle("-fx-control-inner-background: #"+colorGoodValue.toString().substring(2));  
                    displayPos.setLatDegres(newValue);
                    if (!updateProgress) updateFieldsPos();
                }            
        });                
        txDMSLatDeg.setTextFormatter(fmTxDMSLatDeg);         

        TextFormatter<Integer> fmTxDMSLatMin = new TextFormatter<Integer>(new IntegerStringConverter(), 0, 
            change -> {
                String newText = change.getControlNewText() ;
                if (validIntText.matcher(newText).matches()) {
                    return change ;
                } else return null ;
            });                     
        fmTxDMSLatMin.valueProperty().addListener((obs, oldValue, newValue) -> {                 
                if (newValue < 0 || newValue > 60) {                      
                    txDMSLatMin.setStyle("-fx-control-inner-background: #"+colorBadValue.toString().toString().substring(2));
                    txDMSLatMin.requestFocus();                
                } else {
                    txDMSLatMin.setStyle("-fx-control-inner-background: #"+colorGoodValue.toString().substring(2));  
                    displayPos.setLatMin_ms(newValue);
                    if (!updateProgress) updateFieldsPos();
                }            
        });                
        txDMSLatMin.setTextFormatter(fmTxDMSLatMin);    
        
        TextFormatter<Double> fmDMSLatSec = new TextFormatter<Double>(new DoubleStringConverter(), 0.0, 
            change -> {
                String newText = change.getControlNewText() ;
                if (validDoubleText.matcher(newText).matches()) {
                    return change ;
                } else return null ;
            });                
        fmDMSLatSec.valueProperty().addListener((obs, oldValue, newValue) -> {            
                if (newValue < 0 || newValue > 60) {                      
                    txDMSLatSec.setStyle("-fx-control-inner-background: #"+colorBadValue.toString().toString().substring(2));
                    txDMSLatSec.requestFocus();                
                } else {
                    txDMSLatSec.setStyle("-fx-control-inner-background: #"+colorGoodValue.toString().substring(2));  
                    displayPos.setLatSec_ms(newValue);                    
                    if (!updateProgress) {
                        updateFieldsPos();
                    }
                }            
        });        
        txDMSLatSec.setTextFormatter(fmDMSLatSec);

        txDMSLatHem.textProperty().addListener((observable, oldValue, newValue) -> {                        
            if (!newValue.equals("N") && !newValue.equals("S")) {
                txDMSLatHem.setStyle("-fx-control-inner-background: #"+colorBadValue.toString().toString().substring(2));
                txDMSLatHem.requestFocus();    
            } else {
               txDMSLatHem.setStyle("-fx-control-inner-background: #"+colorGoodValue.toString().substring(2));   
            }
        });           
        
        TextFormatter<Integer> fmTxDMSLongDeg = new TextFormatter<Integer>(new IntegerStringConverter(), 0, 
            change -> {
                String newText = change.getControlNewText() ;
                if (validIntText.matcher(newText).matches()) {
                    return change ;
                } else return null ;
            });                     
        fmTxDMSLongDeg.valueProperty().addListener((obs, oldValue, newValue) -> {    
                if (newValue < -180 || newValue > 180) {                      
                    txDMSLongDeg.setStyle("-fx-control-inner-background: #"+colorBadValue.toString().toString().substring(2));
                    txDMSLongDeg.requestFocus();                
                } else {
                    txDMSLongDeg.setStyle("-fx-control-inner-background: #"+colorGoodValue.toString().substring(2));  
                    displayPos.setLongDegres(newValue);
                    if (!updateProgress) updateFieldsPos();
                }            
        });                
        txDMSLongDeg.setTextFormatter(fmTxDMSLongDeg); 

        TextFormatter<Integer> fmTxDMSLongMin = new TextFormatter<Integer>(new IntegerStringConverter(), 0, 
            change -> {
                String newText = change.getControlNewText() ;
                if (validIntText.matcher(newText).matches()) {
                    return change ;
                } else return null ;
            });             
        
        fmTxDMSLongMin.valueProperty().addListener((obs, oldValue, newValue) -> {            
                if (newValue < 0 || newValue > 60) {                      
                    txDMSLongMin.setStyle("-fx-control-inner-background: #"+colorBadValue.toString().toString().substring(2));
                    txDMSLongMin.requestFocus();                
                } else {
                    txDMSLongMin.setStyle("-fx-control-inner-background: #"+colorGoodValue.toString().substring(2));  
                    displayPos.setLongMin_ms(newValue);
                    if (!updateProgress) updateFieldsPos();
                }            
        });                
        txDMSLongMin.setTextFormatter(fmTxDMSLongMin); 
    
        TextFormatter<Double> fmTxDMSLongSec = new TextFormatter<Double>(new DoubleStringConverter(), 0.0, 
            change -> {
                String newText = change.getControlNewText() ;
                if (validDoubleText.matcher(newText).matches()) {
                    return change ;
                } else return null ;
            });                
        fmTxDMSLongSec.valueProperty().addListener((obs, oldValue, newValue) -> {            
                if (newValue < 0 || newValue > 60) {                      
                    txDMSLongSec.setStyle("-fx-control-inner-background: #"+colorBadValue.toString().toString().substring(2));
                    txDMSLongSec.requestFocus();                
                } else {
                    txDMSLongSec.setStyle("-fx-control-inner-background: #"+colorGoodValue.toString().substring(2));  
                    displayPos.setLongSec_ms(newValue);                    
                    if (!updateProgress) {
                        updateFieldsPos();
                    }
                }            
        });        
        txDMSLongSec.setTextFormatter(fmTxDMSLongSec);        

        txDMSLongMer.textProperty().addListener((observable, oldValue, newValue) -> {                        
            if (!newValue.equals("W") && !newValue.equals("E")) {
                txDMSLongMer.setStyle("-fx-control-inner-background: #"+colorBadValue.toString().toString().substring(2));
                txDMSLongMer.requestFocus();    
            } else {
               txDMSLongMer.setStyle("-fx-control-inner-background: #"+colorGoodValue.toString().substring(2));   
            }
        });
        
        txBalise.textProperty().addListener((observable, oldValue, newValue) -> {                        
            if (!validBalise(newValue)) {                
                txBalise.setStyle("-fx-control-inner-background: #"+colorBadValue.toString().toString().substring(2));
                txBalise.requestFocus();    
            } else {
               txBalise.setStyle("-fx-control-inner-background: #"+colorGoodValue.toString().substring(2));   
            }
        });
    }    
    
    /**
     * Numeric fields have listener wich are waiting for numeric values
     * BE CAREFUL -> we must set string values not formatted strings
     *     String.Value OK
     *     String.format("%3d", xx )  -> BAD
     * Formatted strings include spaces. 
     * This spaces trigger a silencious exception
     * three days for fix the mystake...
     */
    private void updateFieldsPos() {
        decimalFormatSymbols.setDecimalSeparator('.'); 
        // fields update       
        df2 = new DecimalFormat("#0.0000", decimalFormatSymbols);
        df3 = new DecimalFormat("##0.0000", decimalFormatSymbols);
        updateProgress = true;
        
        txLat.setText(df2.format(displayPos.getLatitude()));      
        txLong.setText(df3.format(displayPos.getLongitude()));

        txDMLatDeg.setText(String.valueOf(displayPos.getLatDegres()));            
        txDMLatMin.setText(df2.format(displayPos.getLatMin_mm())); 
        txDMLatHem.setText(displayPos.getHemisphere());         
        txDMLongDeg.setText(String.valueOf(displayPos.getLongDegres()));        
        txDMLongMin.setText(df2.format(displayPos.getLongMin_mm()));
        txDMLongMer.setText(displayPos.getMeridien());
                
        txDMSLatDeg.setText(String.valueOf(displayPos.getLatDegres()));
        txDMSLatMin.setText(String.valueOf(displayPos.getLatMin_ms()));
        txDMSLatSec.setText(df2.format(displayPos.getLatSec_ms())); 
        txDMSLatHem.setText(displayPos.getHemisphere());
        txDMSLongDeg.setText(String.valueOf(displayPos.getLongDegres()));
        txDMSLongMin.setText(String.valueOf(displayPos.getLongMin_ms()));
        txDMSLongSec.setText(df2.format(displayPos.getLongSec_ms()));
        txDMSLongMer.setText(displayPos.getMeridien());
                
        updateProgress = false;
    }    
    
    private boolean checkBalise() {
        if (!validBalise(txBalise.getText())) {
            alertbox al = new alertbox(myConfig.getLocale());
            al.alertError(i18n.tr("Input allowed: letters, numbers, underscore"));
            return false;
        } else 
            return true;
    }
         
    
    @FXML
    private void handleManual() {
        int res = 0;
        
        if (checkBalise()) {
            try {
                if (txDesc.getText().trim() != null && !txDesc.getText().trim().equals("")) {
                    ba.setNomLong(txDesc.getText().trim());
                    if (txBalise.getText().trim() != null && !txBalise.getText().trim().equals("")) {
                        ba.setNomCourt(txBalise.getText().trim()); 
                        res = 1;                                                         
                    }            
                }            
            } catch (Exception e) {
                alertbox aError = new alertbox(myConfig.getLocale());
                aError.alertError(e.getClass().getName() + ": " + e.getMessage());  
                sbError = new StringBuilder(this.getClass().getName()+"."+Thread.currentThread().getStackTrace()[1].getMethodName());
                sbError.append("\r\n").append(e.getMessage());
                mylogging.log(Level.SEVERE, sbError.toString());             
            } finally {      
                waypController.returnCoordForm(res, ba);   
                dialogStage.close();              
            }  
        } else {
            txBalise.requestFocus();
        }
    }
    
    /**
     * valid changes
     */
    @FXML
    private void handleCoord() {
       int res = 0;
        
        if (checkBalise()) {
            try {
                if (txDesc.getText().trim() != null && !txDesc.getText().trim().equals("")) {
                    ba.setNomLong(txDesc.getText().trim());
                    if (txBalise.getText().trim() != null && !txBalise.getText().trim().equals("")) {
                        ba.setNomCourt(txBalise.getText().trim()); 
                        res = 1;
                        double dLat = Double.parseDouble(txLat.getText());
                        double dLong = Double.parseDouble(txLong.getText());
                        if (dLat != 0) {
                            ba.setLatitude(txLat.getText());
                            if (dLong != 0) {
                                ba.setLongitude(txLong.getText());   
                                res = 2; 
                            }
                        }                                           
                    }            
                }            
            } catch (Exception e) {
                alertbox aError = new alertbox(myConfig.getLocale());
                aError.alertError(e.getClass().getName() + ": " + e.getMessage());  
                sbError = new StringBuilder(this.getClass().getName()+"."+Thread.currentThread().getStackTrace()[1].getMethodName());
                sbError.append("\r\n").append(e.getMessage());
                mylogging.log(Level.SEVERE, sbError.toString());                    
            } finally {      
                waypController.returnCoordForm(res, ba);   
                dialogStage.close();              
            }            
        } else {
            txBalise.requestFocus();
        }
    }
        
    /**
     * Check form before exit.
     * @param exitMode   
     * @return 
     */
    private Integer validInput() {
       int res = 0;
        
        try {
            if (txDesc.getText().trim() != null && !txDesc.getText().trim().equals("")) {
                ba.setNomLong(txDesc.getText().trim());
                if (txBalise.getText().trim() != null && !txBalise.getText().trim().equals("")) {
                    ba.setNomCourt(txBalise.getText().trim()); 
                    res = 1;
                    String myLat = txLat.getText();
                    if (txLat.getText().trim() != null && !txLat.getText().trim().equals("")) {
                        ba.setLatitude(txLat.getText());
                        if (txLat.getText().trim() != null && !txLat.getText().trim().equals("")) {
                            ba.setLongitude(txLong.getText());   
                            res = 2; 
                        }
                    }                                           
                }            
            }            
        } catch (Exception e) {
            alertbox aError = new alertbox(myConfig.getLocale());
            aError.alertError(e.getClass().getName() + ": " + e.getMessage());  
            sbError = new StringBuilder(this.getClass().getName()+"."+Thread.currentThread().getStackTrace()[1].getMethodName());
            sbError.append("\r\n").append(e.getMessage());
            mylogging.log(Level.SEVERE, sbError.toString());             
        }
        
        return res;
    }    
    
    
    /**
     * Initialize communication brdige with SitesViewController 
     * @param pWaypController 
     */
    public void setCoordBridge(WaypViewController pWaypController) {
        this.waypController = pWaypController;        
    }        
    
    /**
     * Sets the stage of this dialog.
     *
     * @param dialogStage
     */
    public void setDialogStage(Stage dialogStage, configProg mainConfig, String pDesc, String pBalise) {
        this.dialogStage = dialogStage;   
        dialogStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            public void handle(WindowEvent we) {
                System.out.println("Stage is closing"); 
                waypController.returnCoordForm(0, ba);  
            }
        });              
        
        this.myConfig = mainConfig;
        i18n = myConfig.getI18n();
        winTraduction();
        txDesc.setText(pDesc);
        txBalise.setText(pBalise);
        displayPos = new position();
        this.dialogStage.initModality(Modality.APPLICATION_MODAL);   
        this.dialogStage.showAndWait();
    }    

    private boolean validBalise(String sText) {
        Pattern p = Pattern.compile("^[a-zA-Z0-9_]+$");
        Matcher matcher = p .matcher(sText);
        return matcher.find();
    }   
    
    /**
     * Translate labels of the window
     */
    private void winTraduction() {
        lbDesc.setText(i18n.tr("Description"));   
        lbBalise.setText(i18n.tr("Short name"));  
        lbLat1.setText(i18n.tr("Latitude"));   
        lbLat2.setText(i18n.tr("Latitude"));   
        lbLat3.setText(i18n.tr("Latitude"));   
        lbLong1.setText(i18n.tr("Longitude"));   
        lbLong2.setText(i18n.tr("Longitude"));   
        lbLong3.setText(i18n.tr("Longitude"));    
        lbSeparator.setText(i18n.tr("Decimal separator must be a point"));   
        btCoord.setText(i18n.tr("Place with coordinates"));      
        btManual.setText(i18n.tr("Manually place the point on the map"));  
    }    
    
}
