/*
 * Copyright Gil THOMAS
 * This file forms an integral part of Logfly project
 * See the LICENSE file distributed with source code
 * for details of Logfly licence project
 */
package database;

import controller.RootLayoutController;
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
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;
import littlewins.winTrackFile;
import org.controlsfx.dialog.ProgressDialog;
import org.xnap.commons.i18n.I18n;
import settings.configProg;
import systemio.mylogging;

/**
 *
 * @author gil
 */
public class dbParawing {
    
 // Localization
    private I18n i18n;
    
    // Settings
    private configProg myConfig;
    private RootLayoutController rootController;
    
    private int addNb;
    private int addFlightsOK;
    private int addFlightsBad = 0;
    private StringBuilder sbDuplicates = new StringBuilder();
    private StringBuilder sbRejected = new StringBuilder();
        
    private StringBuilder sbError;
    String RC = "\n";    

    public dbParawing(configProg pConfig, I18n pI18n, RootLayoutController rootlayout)  {
        myConfig = pConfig;      
        this.i18n = pI18n;
        this.rootController = rootlayout; 
    }     
    
    public void importCsv(File pFile) {
        csvImport(pFile);
    }

    private void csvImport(File pFile) {
        
        Task<Object> worker = new Task<Object>() {
            @Override
            protected Object call() throws Exception {
                csvImportFile(pFile);
                return null ;
            }

        };
        worker.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
            @Override
            public void handle(WorkerStateEvent t) {
                csvCloseImport();
            }
        });  

        ProgressDialog dlg = new ProgressDialog(worker);
        dlg.setHeaderText(i18n.tr("Csv import"));
        dlg.setTitle("");
        Thread th = new Thread(worker);
        th.setDaemon(true);
        th.start();                                        
    }    
    
    private void csvImportFile(File importFile)  {
        addNb = 0;
        addFlightsOK = 0;
        addFlightsBad = 0;
        sbDuplicates = new StringBuilder();
        sbRejected = new StringBuilder();     
        
        try {
            Charset ENCODING = StandardCharsets.ISO_8859_1;
            Path path = Paths.get(importFile.getAbsolutePath());
            List<String> lines = Files.readAllLines(path, ENCODING);
            for (String oneLine : lines) {                
                addNb++;
                if (addNb > 1) {
                    // delimiters removed
                    String currLine = oneLine.replace("\"", "");
                    String[] partLine = currLine.split(";");
                    // 17 fields needed
                    if (partLine.length >= 15 ) {
                        dbAdd addFlights = new dbAdd(myConfig, i18n);
                        boolean addRes = addFlights.importCsvFlight(partLine);
                        if (addRes) {
                            addFlightsOK++;                                                                        
                        } else {
                            addFlightsBad++;
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

    private void csvCloseImport() {
        boolean writeReject = false;
        
        alertbox aInfo = new alertbox(myConfig.getLocale());
        StringBuilder sbMsg = new StringBuilder();
        sbMsg.append(String.valueOf(addFlightsOK)).append(" ").append(i18n.tr("imported flights")).append(" / ").append(String.valueOf(addNb)).append(" ").append("lignes");
        if (addFlightsBad > 0) sbMsg.append(RC).append(String.valueOf(addFlightsBad)).append(" ").append(i18n.tr("rejected flights"));
        aInfo.alertInfo(sbMsg.toString());
        if (addFlightsOK < addNb-1) {    
            if (myConfig.isValidConfig()) {
                try {
                    File fileReject = new File(myConfig.getPathW()+"rejectedflights.csv");
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
                String msg = i18n.tr("List saved in")+" <rejectedflights.csv>"+RC+RC;
                sbRejected.insert(0,msg);
                winTrackFile displayDoub = new winTrackFile(sbRejected.toString());    
            }
        }    
        rootController.changeCarnetView();
    }    
    
}
