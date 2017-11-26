/*
 * Copyright Gil THOMAS
 * This file forms an integral part of Logfly project
 * See the LICENSE file distributed with source code
 * for details of Logfly licence project
 */
package gps;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.logging.Level;
import javafx.collections.ObservableList;
import settings.osType;
import systemio.mylogging;
import systemio.textio;

/**
 *
 * @author gil
 * 
 * Communication protocol with Syride is not published
 * User must use a soft called SYS-PC-TOOL to download tracks
 * Sys-PC-Tool create a folder called parapente where tracks are downloaded
 * One folder by day YYYY-MM-DD
 * Name char
 */
public class syride {
    
    private boolean connected = false;
    private File fFlights;   
    private File fArchives = null;
    private int idxDrive;    
    private ObservableList <String> driveList;   
    private ArrayList<String> flightFolderList;
    private StringBuilder sbError;
    private osType currentOS;

    public boolean isConnected() {
        return connected;
    }

    private void setConnected(boolean connected) {
        this.connected = connected;
    }


    public File getfFlights() {
        return fFlights;
    }

    public File getfArchives() {
        return fArchives;
    }

    public int getIdxDrive() {
        return idxDrive;
    }

    public ObservableList<String> getDriveList() {
        return driveList;
    }
    
    public syride (osType currOs, int gpsLimit) {
        boolean conn = false;
        fFlights = null;    
        currentOS = currOs;
                
        conn = testSysPCTools();
                     
    }
    
    public boolean testSysPCTools() {
        
        boolean res = false;
        
        switch (currentOS) {
            case WINDOWS:
                // https://stackoverflow.com/questions/9677692/getting-my-documents-path-in-java 
                // System.getProperty("user.home")+File.separatorChar + "Documents"  
                fFlights = new File(System.getProperty("user.home")+File.separatorChar + "Documents"+"\\Syride\\Parapente");
                if (fFlights.exists() && fFlights.isDirectory()) {
                    res = true;
                    setConnected(res);
                    fArchives = new File(System.getProperty("user.home")+File.separatorChar + "Documents"+"\\Syride\\archives");
                    if (!fArchives.exists()) {
                        try {
                            boolean okArchives = fArchives.mkdir();
                            if (!okArchives) {
                                fArchives = null;
                            }
                        } catch (Exception e) {
                            sbError = new StringBuilder(this.getClass().getName()+"."+Thread.currentThread().getStackTrace()[1].getMethodName());
                            sbError.append("\r\n").append(e.toString()).append("\r\n");
                            sbError.append("Unable to create syride/archives");
                            mylogging.log(Level.SEVERE, sbError.toString()); 
                        }                             
                    }                    
                }
                break;
            case MACOS :
                fFlights = new File(System.getProperty("user.home")+"/syride/parapente");
                if (fFlights.exists() && fFlights.isDirectory()) {
                    res = true;
                    setConnected(res);                     
                    fArchives = new File(System.getProperty("user.home")+"/syride/archives");
                    if (!fArchives.exists()) {
                        try {
                            fArchives = new File(System.getProperty("user.home")+"/syride/archives");
                            boolean okArchives = fArchives.mkdir();
                            if (!okArchives) {
                                fArchives = null;
                            }
                        } catch (Exception e) {
                            sbError = new StringBuilder(this.getClass().getName()+"."+Thread.currentThread().getStackTrace()[1].getMethodName());
                            sbError.append("\r\n").append(e.toString()).append("\r\n");
                            sbError.append("Unable to create syride/archives");
                            mylogging.log(Level.SEVERE, sbError.toString()); 
                        }                             
                    }
                }
                break;
            case LINUX :
                fFlights = new File(System.getProperty("user.home")+"/syride/parapente");
                if (fFlights.exists() && fFlights.isDirectory()) {
                    res = true;
                    setConnected(res);                     
                    fArchives = new File(System.getProperty("user.home")+"/syride/archives");
                    if (!fArchives.exists()) {
                        try {
                            fArchives = new File(System.getProperty("user.home")+"/syride/archives");
                            boolean okArchives = fArchives.mkdir();
                            if (!okArchives) {
                                fArchives = null;
                            }
                        } catch (Exception e) {
                            sbError = new StringBuilder(this.getClass().getName()+"."+Thread.currentThread().getStackTrace()[1].getMethodName());
                            sbError.append("\r\n").append(e.toString()).append("\r\n");
                            sbError.append("Unable to create syride/archives");
                            mylogging.log(Level.SEVERE, sbError.toString()); 
                        }                             
                    }
                }
                break;
            default:
                throw new AssertionError();
        }        
        
        return res;        
    }
    
    private void exploreFolder(File dir, ArrayList<String> trackPathList) throws Exception {  
        // Recursivité à vérifier pour le skytraax        
        File[] files = dir.listFiles();
        for (int i = 0; i < files.length; i++) {
            String fileName = files[i].getName();
            if (fileName.endsWith(".igc") || fileName.endsWith(".IGC")) {                                    
                // Problem of dot files writed by MacOS 
                if (files[i].isFile() && !fileName.startsWith("._") && files[i].getName().length() > 9) {
                    trackPathList.add(files[i].getPath());
                }
            }
            if (files[i].isDirectory()) {
                exploreFolder(files[i], trackPathList);
            }
        }        
    }       
 
    public void listTracksFiles(ArrayList<String> trackPathList) throws Exception {   

        if (fFlights != null && fFlights.exists())  {        
           exploreFolder(fFlights, trackPathList);
        }
    }     
    
    public String getTrackFile(String igcPath) {
        
        String res = null;
        
        File fIGC = new File(igcPath);
        textio fread = new textio();                                    
        String pFichier = fread.readTxt(fIGC);
        if (pFichier != null && !pFichier.isEmpty())  {
            res = pFichier;
        }
        
        return res;        
    }   

    public boolean archiveTracks(String igcPath) {
        
        boolean res = false;
        String sPathArchivesDay = null;
        
        if (fArchives != null)  {
            try {
                File fIGC = new File(igcPath);
                String nameDay = fIGC.getName();
                String sPathParapenteDay = fIGC.getParent();  
                Path pathParapenteDay = Paths.get(sPathParapenteDay);
                switch (currentOS) {
                    case WINDOWS:
                        sPathArchivesDay = sPathParapenteDay.replaceAll("Parapente", "archives");
                        break;
                    case MACOS:
                        sPathArchivesDay = sPathParapenteDay.replaceAll("parapente", "archives");
                        break;
                    case LINUX:
                        sPathArchivesDay = sPathParapenteDay.replaceAll("parapente", "archives");
                        break;                        
                }
                sPathArchivesDay = sPathParapenteDay.replaceAll("parapente", "archives");
                Path pathArchivesDay = Paths.get(sPathArchivesDay);  
                File fArchivesDay = new File(sPathArchivesDay);
                if (!fArchivesDay.exists()) {
                    Files.move(pathParapenteDay, pathArchivesDay);
                } 
            } catch (Exception e) {
                sbError = new StringBuilder(this.getClass().getName()+"."+Thread.currentThread().getStackTrace()[1].getMethodName());
                sbError.append("\r\n").append(e.toString());
                sbError.append("\r\n").append(e.toString()).append("\r\n");
                sbError.append("Unable to move "+sPathArchivesDay);
            }
        }
        
        return res;
    }
    
}
