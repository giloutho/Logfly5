/*
 * Copyright Gil THOMAS
 * This file forms an integral part of Logfly project
 * See the LICENSE file distributed with source code
 * for details of Logfly licence project
 */
package database;

import dialogues.ProgressForm;
import dialogues.alertbox;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.logging.Level;
import javafx.concurrent.Task;
import littlewins.winTrackFile;
import org.xnap.commons.i18n.I18n;
import settings.configProg;
import systemio.mylogging;

/**
 *
 * @author gil
 */
public class dbImport {
    
 // Localization
    private I18n i18n;
    

    // Settings
    private configProg myConfig;
    private int addNb = 0;
    private int addSitesOK = 0;
    private int addSitesBad = 0;
    private StringBuilder sbDoublons;
    private StringBuilder sbRejected;    
    private StringBuilder sbError;
    String RC = "\n";
    
    public dbImport(configProg pConfig, I18n pI18n)  {
        myConfig = pConfig;      
        this.i18n = pI18n;
    } 
           
    public void importCsv(File pFile) {
        csvImport(pFile);
    }
    
    private void csvCloseImport() {
        boolean writeReject = false;
        
        alertbox aInfo = new alertbox(myConfig.getLocale());
        StringBuilder sbMsg = new StringBuilder();
        sbMsg.append(String.valueOf(addSitesOK)).append(" ").append(i18n.tr("sites importés")).append(" / ").append(String.valueOf(addNb)).append(" ").append("lignes");
        if (addSitesBad > 0) sbMsg.append(RC).append(String.valueOf(addSitesBad)).append(" ").append(i18n.tr("sites rejetés"));
        aInfo.alertInfo(sbMsg.toString());
        if (addSitesOK < addNb) {    
            if (myConfig.isValidConfig()) {
                try {
                    File fileReject = new File(myConfig.getPathW()+"rejectedsites.csv");
                    FileOutputStream fileStream = new FileOutputStream(fileReject);
                    Charset ENCODING = StandardCharsets.ISO_8859_1;
                    OutputStreamWriter writer = new OutputStreamWriter(fileStream, ENCODING);   
                    writer.write(sbRejected.toString());                            
                    writer.close();
                    writeReject = true;
                } catch (Exception e) {
                    sbError = new StringBuilder(this.getClass().getName()+"."+Thread.currentThread().getStackTrace()[1].getMethodName());
                    sbError.append("\r\n").append(e.toString());
                    mylogging.log(Level.SEVERE, sbError.toString());                
                }
            }
            if (writeReject) {
                String msg = i18n.tr("Liste enregistrée dans <rejectedsites.csv>")+RC+RC;
                sbDoublons.insert(0,msg);
            }
            winTrackFile displayDoub = new winTrackFile(sbDoublons.toString());             
        }        
    }
    
    private Double parseDouble(String s) {
        try {
            return Double.parseDouble(s);
        } catch (NumberFormatException e) {
            return Double.NaN;
        } 
    }    
    
    private void csvImportFile(File importFile)  {
        
        addNb = 0;
        addSitesOK = 0;
        addSitesBad = 0;
        sbDoublons = new StringBuilder();
        sbRejected = new StringBuilder();
        
        try {
            Charset ENCODING = StandardCharsets.ISO_8859_1;
            Path path = Paths.get(importFile.getAbsolutePath());
            List<String> lines = Files.readAllLines(path, ENCODING);
            for (String oneLine : lines) {
                addNb++;
                String[] partLine = oneLine.split(";");
                if (partLine.length > 3 && !partLine[0].equals("POINT_ID")) {
                   dbSearch rechSite = new dbSearch(myConfig);
                   double dLat = parseDouble(partLine[2]);
                   double dLong = parseDouble(partLine[3]);
                   if (!Double.isNaN(dLat) && !Double.isNaN(dLong)) {
                        //String siteExist = rechSite.existeSite(Double.parseDouble(partLine[2]), Double.parseDouble(partLine[3]));
                        String siteExist = null;
                        if (siteExist == null) {
                            dbAdd addSite = new dbAdd(myConfig, i18n);
                            boolean addRes = addSite.importSite(partLine);
                            if (addRes) {
                                addSitesOK++;                                                                        
                            } else {
                                addSitesBad++;
                                System.out.println("bad : "+partLine[1]+" "+partLine[2]+" "+partLine[3]);
                            }
                         } else {
                            sbDoublons.append(partLine[1]).append(" ").append(siteExist).append(RC);   
                            sbRejected.append(oneLine).append(RC);
                        }
                   }
                }
            } 
        } catch (Exception e) {
            sbError = new StringBuilder(this.getClass().getName()+"."+Thread.currentThread().getStackTrace()[1].getMethodName());
            sbError.append("\r\n").append(e.toString());
            mylogging.log(Level.SEVERE, sbError.toString());
        }                    
    }    
    
    private void csvImport(File pFile) {
        
        ProgressForm pForm = new ProgressForm();

        Task<Void> task = new Task<Void>() {
            @Override
            public Void call() throws InterruptedException { 
                csvImportFile(pFile);
                return null ;
            }

        };
        // binds progress of progress bars to progress of task:
        pForm.activateProgressBar(task);

        // task is finished 
        task.setOnSucceeded(event -> {
            pForm.getDialogStage().close();
            csvCloseImport();
        });

        pForm.getDialogStage().show();

        Thread thread = new Thread(task);
        thread.start();                                       
    }    
        
}
