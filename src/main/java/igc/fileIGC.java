/*
 * Copyright Gil THOMAS
 * Ce fichier fait partie intégrante du projet Logfly
 * Pour tous les détails sur la licence du projet Logfly
 * Consulter le fichier LICENSE distribué avec le code source
 */
package igc;

import dialogues.alertbox;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.time.format.DateTimeFormatter;
import org.xnap.commons.i18n.I18n;
import org.xnap.commons.i18n.I18nFactory;
import trackgps.traceGPS;

/**
 *
 * @author Gil Thomas logfly.org
 */
public class fileIGC {

    private I18n i18n = I18nFactory.getI18n(fileIGC.class.getClass());
    private String fileAbsPath;

    public String getFileAbsPath() {
        return fileAbsPath;
    }
                
    public int creaIgcForCalcul(traceGPS currTrace, String fName)  {            
        
        String igc_Lat;
        String igc_Long;
        pointIGC currPoint;
        String RC = "\r\n";        
                
        int res = -1;
        
        int iTotPoints = currTrace.Tb_Calcul.size();        
        
        StringBuilder sbIGC = new StringBuilder(); 
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HHmmss");
        for (int i = 0; i < iTotPoints; i+=1) {
            currPoint = currTrace.Tb_Calcul.get(i);            
            igc_Lat = geoutils.convigc.Lat_Dd_IGC(currPoint.Latitude);
            igc_Long = geoutils.convigc.Long_Dd_IGC(currPoint.Longitude);
            sbIGC.append("B"+currPoint.dHeure.format(formatter));
            sbIGC.append(igc_Lat+igc_Long+"A00000");
            sbIGC.append(String.format("%05d",currPoint.AltiGPS)+RC);
        }
        
        // On essaye la procedure temp classique 
        // File tempFile = systemio.tempacess.getTempFile("trk", "igc");                
        File tempFile = systemio.tempacess.getAppFile("Logfly", fName);
        try {        
            Writer output = new BufferedWriter(new FileWriter(tempFile));
            output.write(sbIGC.toString());
            output.close();
            fileAbsPath = tempFile.getAbsolutePath();
            res = 0;
        }
        catch (FileNotFoundException ex) {
            res = 1;   // Fichier non trouvé
        } catch (IOException ex) {
            res = 2; // Problème d'entrée sortie sur fichier
        }            
        
        return res;        
    }
    
     public  String creaTempIGC(traceGPS currTrace, String fName)  {            
        
        String igc_Lat;
        String igc_Long;
        pointIGC currPoint;
        String RC = "\r\n";
        int iReduction;
                
        String res = null;
        
        int iTotPoints = currTrace.Tb_Good_Points.size();        
        
        StringBuilder sbIGC = new StringBuilder(); 
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HHmmss");
        for (int i = 0; i < iTotPoints; i+=1) {
            currPoint = currTrace.Tb_Good_Points.get(i);          
            igc_Lat = geoutils.convigc.Lat_Dd_IGC(currPoint.Latitude);
            igc_Long = geoutils.convigc.Long_Dd_IGC(currPoint.Longitude);
            sbIGC.append("B"+currPoint.dHeure.format(formatter));
            sbIGC.append(igc_Lat+igc_Long+"A00000");
            sbIGC.append(String.format("%05d",currPoint.AltiGPS)+RC);
        }
        
        // On essaye la procedure temp classique 
        // File tempFile = systemio.tempacess.getTempFile("trk", "igc");                
        File tempFile = systemio.tempacess.getAppFile("Logfly", fName);
        try {        
            Writer output = new BufferedWriter(new FileWriter(tempFile));
            output.write(sbIGC.toString());
            output.close();
            res = tempFile.getAbsolutePath();
        }
        catch (FileNotFoundException ex) {
            alertbox aError = new alertbox();
            aError.alertError(i18n.tr("Fichier non trouvé"));
        } catch (IOException ex) {
            alertbox aError = new alertbox();
            aError.alertError(i18n.tr("Problème d'entrée sortie"));            
        }            
        
        return res;
        
    }
}
