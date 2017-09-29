/*
 * Copyright Gil THOMAS
 * This file forms an integral part of Logfly project
 * See the LICENSE file distributed with source code
 * for details of Logfly licence project
 */
package gps;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import settings.osType;
import systemio.textio;

/**
 *
 * @author gil
 */
public class reversale {
    
    private boolean connected = false;
    private File fLog;
    private File fCompet;
    private File fDrive;
    private int idxDrive;
    private ObservableList <String> driveList;
    private String closingDate;
    
    public boolean isConnected() {
        return connected;
    }

    private void setConnected(boolean connected) {
        this.connected = connected;
    }

    public File getfLog() {
        return fLog;
    }

    public File getfCompet() {
        return fCompet;
    }

    public File getfDrive() {
        return fDrive;
    }

    public int getIdxDrive() {
        return idxDrive;
    }

    public ObservableList<String> getDriveList() {
        return driveList;
    }
    
    
    
    public reversale(osType currOs, int gpsLimit) {
        boolean conn = false;
        fLog = null;
        fCompet = null;
        fDrive = null;   
        
        setDateLevel(gpsLimit);
        
        conn = testConnection(currOs);
        
        setConnected(conn);                
        
    }
    
    private void setDateLevel(int gpsLimit) {
        
        Calendar myCalendar = Calendar.getInstance();
        myCalendar.add(Calendar.MONTH, -(gpsLimit));
        SimpleDateFormat sdf = new SimpleDateFormat("YYMMdd");
        closingDate = sdf.format(myCalendar.getTime());
                
    }
    
    public boolean testConnection(osType currOs) {
        
        boolean res = false;
        boolean cond1 = false;
        boolean cond2 = false;
        File[] drives = null;
        int nbDrive = 0;
            
        switch (currOs) {
            case WINDOWS:
                drives = File.listRoots();
                break;
            case MACOS :
                drives = new File("/Volumes").listFiles();
                break;
            case LINUX :
                File fmedia = new File("/media");
                for(File fls : fmedia.listFiles()){
                    // first pass : /media/user
                    drives = fls.listFiles();                    
                }
                break;
            default:
                throw new AssertionError();
        }
        if (drives != null && drives.length > 0) {
            driveList = FXCollections.observableArrayList();
            driveList.clear();
            for (File aDrive : drives) {       
                long size = aDrive.getTotalSpace();
                float sizeGo = size / 1000000000;    
                StringBuilder sb = new StringBuilder();
                sb.append(aDrive.getName()).append(" ").append(String.format("%4.0f", sizeGo)).append(" Go");
                driveList.add(sb.toString());
                // Capacity > 64 Go jumped
                if (size < 63999999999L) {
                    File listFile[] = aDrive.listFiles();
                    if (listFile != null) {
                        for (int i=0; i<listFile.length; i++)         
                        {
                            //System.out.println("Fichier : "+listFile[i].getName()+" "+listFile[i].isDirectory());
                            if (listFile[i].getName().equals("COMPET") && listFile[i].isDirectory()) {
                                fCompet = listFile[i];
                            }
                            if (listFile[i].getName().equals("LOG") && listFile[i].isDirectory()) {
                                //System.out.println("Fichier : "+listFile[i].getName()+" "+listFile[i].getAbsolutePath());
                                fLog = listFile[i];
                                cond1 = true;
                            }
                            if (listFile[i].getName().equals("PARAM.VGP")) {
                                cond2 = true;
                            }
                        }
                        if (cond1 == true && cond2 == true) {
                            fDrive = aDrive;
                            idxDrive = nbDrive;
                            res = true;
                            break;
                        } else {
                            idxDrive = 0;
                        }
                    }
                }
                nbDrive++;
            }
        }                                    
        return res;                
    }
    
    private void exploreFolder(File dir, ArrayList<String> trackPathList) throws Exception {  
        // Recursivité à vérifier pour le skytraax        
        File[] files = dir.listFiles();
        for (int i = 0; i < files.length; i++) {
            String fileName = files[i].getName();
            if (fileName.endsWith(".igc") || fileName.endsWith(".IGC") || fileName.endsWith(".gpx") || fileName.endsWith(".GPX")) {                                    
                // Problem of dot files writed by MacOS 
                if (files[i].isFile() && !fileName.startsWith("._")) {
                    if (files[i].getName().substring(0,7).compareTo(closingDate) > 0) {
                        trackPathList.add(files[i].getPath());
                    }
                }
            }
            if (files[i].isDirectory()) {
                exploreFolder(files[i], trackPathList);
            }
        }        
    }
    
    /**
     * Recursive track search in selected folder and sub folders
     * @param dir
     * @throws Exception 
     */
    public void listTracksFiles(ArrayList<String> trackPathList) throws Exception {   
        // Recursivité à vérifier pour le skytraax
        // We begin by the most used folder : LOG
        if (fLog != null && fLog.exists())  {
            exploreFolder(fLog, trackPathList);
        }
        if (fCompet != null && fCompet.exists())  {
            exploreFolder(fCompet, trackPathList);
        }        
        // In this case, sorting the list is easy, file name is YYMMDDNumber
        // for other GPS, we must use lambdas exp -> https://www.leveluplunch.com/java/tutorials/007-sort-arraylist-stream-of-objects-in-java8/
//        Collections.sort(trackPathList);
//        Collections.reverse(trackPathList);
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
        
    
}
