/*
 * Copyright Gil THOMAS
 * This file forms an integral part of Logfly project
 * See the LICENSE file distributed with source code
 * for details of Logfly licence project
 */
package photos;

import java.io.File;
import java.io.FileWriter;
import settings.configProg;
import trackgps.traceGPS;

/**
 *
 * @author gil
 */
public class filesUtils {
    
    private configProg myConfig;
    private traceGPS currTrack;
    private String destinationPath = null;
    
    public filesUtils(configProg pConfig, traceGPS pTrack) {
        this.myConfig = pConfig;
        this.currTrack = pTrack;
    }

    public String getDestinationPath() {
        return destinationPath;
    }        
    
    public int createFolder() {

        int res = -1;
        File folderTrack = null;
        
        File fPhotos = new File(myConfig.getPathW()+File.separator+"Photos");
        if (!fPhotos.exists()) fPhotos.mkdirs();
        if (fPhotos.exists()) {
            String trackName = currTrack.suggestName();
            if (trackName != null) {
                folderTrack = new File(fPhotos.getAbsolutePath()+File.separator+trackName);
                if (!folderTrack.exists()) {
                    folderTrack.mkdirs();
                    if (folderTrack.exists()) {
                        res = exportIGC(folderTrack);                   
                    }
                } else {       
                    File fTrackIgc = new File(folderTrack.getAbsolutePath()+File.separator+"track.igc");
                    if (!fTrackIgc.exists()) {
                       res = exportIGC(folderTrack); 
                    } else {
                        res = 0;
                    }
                }
            }
        }          
        
        if (res == 0) destinationPath = folderTrack.getAbsolutePath()+File.separator; 
        
        return res;  
        
    }
    
    public boolean checkFolder(boolean withPhotos) {
        boolean res = false;
        File folderTrack = null;
        
        File fPhotos = new File(myConfig.getPathW()+File.separator+"Photos");
        if (fPhotos.exists()) {
            String trackName = currTrack.suggestName();
            if (trackName != null) {
                folderTrack = new File(fPhotos.getAbsolutePath()+File.separator+trackName);
                if (folderTrack.exists()) {                 
                    File fIGC = new File(folderTrack.getAbsolutePath()+File.separator+"track.igc");
                    if (fIGC.exists()) {
                        if (withPhotos) {
                            File[] files = folderTrack.listFiles();
                            for (int i = 0; i < files.length; i++) {
                                String fileName = files[i].getName();            
                                if (fileName.endsWith(".jpg") || fileName.endsWith(".JPG")) {                                
                                    if (files[i].isFile()) {
                                        destinationPath = folderTrack.getAbsolutePath()+File.separator;
                                        return true;
                                    }
                                }                            
                            }
                        } else {
                            destinationPath = folderTrack.getAbsolutePath()+File.separator;
                            return true;
                        }
                    }
                }
            }
        }                  
        
        return res;
    }
    
    private int exportIGC(File folderExport) {
        
        int res = -1;
        
        try {
            File fTrackIgc = new File(folderExport.getAbsolutePath()+File.separator+"track.igc");
            FileWriter fileWriter = null;
            fileWriter = new FileWriter(fTrackIgc);
            fileWriter.write(currTrack.getFicIGC());
            fileWriter.close();  
            res = 0;
        } catch (Exception e) {
            res = -1;
        }           
        
        return res;
        
    }
    
}
