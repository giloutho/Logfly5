/* 
 * Copyright Gil THOMAS
 * This file forms an integral part of Logfly project
 * See the LICENSE file distributed with source code
 * for details of Logfly licence project
 */
package org.logfly.igc;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import org.logfly.dialog.alertbox;
import org.logfly.trackgps.traceGPS;

/**
 *
 * @author Gil Thomas logfly.org
 * Creation of IGC files
 */
public class fileIGC {

    private Locale currLocale;
    private String fileAbsPath;
    
    public fileIGC(Locale myLocale)  {
        currLocale = myLocale;
    }

    public String getFileAbsPath() {
        return fileAbsPath;
    }
     
    /**
     * Creation of an IGC file with Tb_Calcul (number of points is reduced)
     * @param currTrace
     * @param fName
     * @return 
     */
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
            igc_Lat = org.logfly.geoutils.convigc.Lat_Dd_IGC(currPoint.Latitude);
            igc_Long = org.logfly.geoutils.convigc.Long_Dd_IGC(currPoint.Longitude);
            sbIGC.append("B"+currPoint.dHeure.format(formatter));
            sbIGC.append(igc_Lat+igc_Long+"A00000");
            sbIGC.append(String.format("%05d",currPoint.AltiGPS)+RC);
        }
                       
        File tempFile = org.logfly.systemio.tempacess.getAppFile("Logfly", fName);
        try {        
            Writer output = new BufferedWriter(new FileWriter(tempFile));
            output.write(sbIGC.toString());
            output.close();
            fileAbsPath = tempFile.getAbsolutePath();
            res = 0;
        }
        catch (FileNotFoundException ex) {
            res = 1;   // File not found
        } catch (IOException ex) {
            res = 2; // File input/output error
        }            
        
        return res;        
    }
    
    /**
     * Creation of an IGC file with Tb_Good_Points (track valid points only) 
     * @param currTrace
     * @param fName
     * @return 
     */
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
            igc_Lat = org.logfly.geoutils.convigc.Lat_Dd_IGC(currPoint.Latitude);
            igc_Long = org.logfly.geoutils.convigc.Long_Dd_IGC(currPoint.Longitude);
            sbIGC.append("B"+currPoint.dHeure.format(formatter));
            sbIGC.append(igc_Lat+igc_Long+"A00000");
            sbIGC.append(String.format("%05d",currPoint.AltiGPS)+RC);
        }
                       
        File tempFile = org.logfly.systemio.tempacess.getAppFile("Logfly", fName);
        try {        
            Writer output = new BufferedWriter(new FileWriter(tempFile));
            output.write(sbIGC.toString());
            output.close();
            res = tempFile.getAbsolutePath();
        }
        catch (FileNotFoundException ex) {
            alertbox aError = new alertbox(currLocale);
            aError.alertNumError(1);  // file not found
        } catch (IOException ex) {
            alertbox aError = new alertbox(currLocale);
            aError.alertNumError(2);  // file input/output error           
        }            
        
        return res;        
    }
}
