/*
 * Copyright Gil THOMAS
 * This file forms an integral part of Logfly project
 * See the LICENSE file distributed with source code
 * for details of Logfly licence project
 */
package gps;

import java.io.File;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.logging.Level;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import settings.configProg;
import settings.osType;
import systemio.mylogging;
import systemio.textio;
import trackgps.traceGPS;

/**
 *
 * @author gil
 */
public class reversale {
    
    private boolean connected = false;
    private boolean airspConnected = false;
    private File fLog;
    private File fCompet;
    private File fWayp;      
    private boolean wpExist = false;
    private File fGoog;      
    private boolean googExist = false;    
    private File fAirsp;
    private boolean airspExist = false;
    private File fDrive;
    private int idxDrive;
    private String pathReverbin;
    private ObservableList <String> driveList;
    private String closingDate;
    private String msgClosingDate;
    private ArrayList<String> wpPathList;     
    private StringBuilder sbError;
    
    public boolean isConnected() {
        return connected;
    }

    private void setConnected(boolean connected) {
        this.connected = connected;
    }

    public void setAirspConnected(boolean airspConnected) {
        this.airspConnected = airspConnected;
    }

    public boolean isAirspConnected() {
        return airspConnected;
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

    public String getMsgClosingDate() {
        return msgClosingDate;
    }
    
    public ObservableList<String> getDriveList() {
        return driveList;
    }

    public File getfWayp() {
        return fWayp;
    }

    public File getfGoog() {
        return fGoog;
    }

    public File getfAirsp() {
        return fAirsp;
    }
        
    public boolean isWpExist() {
        return wpExist;
    }

    public boolean isGoogExist() {
        return googExist;
    }

    public String getPathReverbin() {
        return pathReverbin;
    }        
    
    public ArrayList<String> getWpPathList() {
        return wpPathList;
    }        

    
    public reversale(osType currOs, int gpsLimit) {
        boolean conn = false;
        fLog = null;
        fCompet = null;
        fDrive = null;   
        idxDrive = 0;
        driveList = FXCollections.observableArrayList();        
        
        setDateLevel(gpsLimit);
        
        conn = testConnection(currOs);              
        
    }
    
    private void setDateLevel(int gpsLimit) {
        if (gpsLimit == 0) gpsLimit = 99;
        Calendar myCalendar = Calendar.getInstance();
        myCalendar.add(Calendar.MONTH, -(gpsLimit));
        SimpleDateFormat sdf = new SimpleDateFormat("YYMMdd");
        closingDate = sdf.format(myCalendar.getTime());   
        SimpleDateFormat sdfMsg = new SimpleDateFormat("dd/MM/YY");
        msgClosingDate = sdfMsg.format(myCalendar.getTime());  
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
                                // In some cases, we found only COMPET
                                cond1 = true;
                            }
                            if (listFile[i].getName().equals("LOG") && listFile[i].isDirectory()) {
                                // LOG is in principle, the default folder for tracks
                                fLog = listFile[i];
                                cond1 = true;
                            }
                            if (listFile[i].getName().equals("PARAM.VGP")) {
                                cond2 = true;
                            }
                            if (listFile[i].getName().equals("GOOGLE") && listFile[i].isDirectory()) {
                                fGoog = listFile[i];
                                googExist = true;
                            }       
                            if (listFile[i].getName().equals("WPTS") && listFile[i].isDirectory()) {
                                fWayp = listFile[i];
                                wpExist = true;
                            }  
                            if (listFile[i].getName().equals("AIRSP") && listFile[i].isDirectory()) {
                                fAirsp = listFile[i];
                                airspExist = true;
                            }                                  
                        }
                        if (cond1 == true && cond2 == true) {
                            fDrive = aDrive;
                            idxDrive = nbDrive;
                            res = true;
                            setConnected(res);                             
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
    
    public int airspaceReady(osType currOs) {
        int res = -1;
        boolean airspFolderOK = false;   
        boolean reverbinOK = false;
        pathReverbin = "";

        if (!airspExist) {
            try{
                String airspPath = fDrive.getAbsolutePath()+File.separator+"AIRSP";
                File f = new File(airspPath);
                if(f.mkdir()) { 
                    fAirsp = f;
                    airspFolderOK = true;
                } else {
                    res = 202;   // Impossible de créer le dossier AIRSP
                    sbError = new StringBuilder(this.getClass().getName()+"."+Thread.currentThread().getStackTrace()[1].getMethodName());
                    sbError.append("\r\n").append("Unable to create Reversale airspace folder ");
                    mylogging.log(Level.SEVERE, sbError.toString());
                }              
            }catch (Exception e) {//Catch exception if any
                res = 202;   // Impossible de créer le dossier AIRSP
                sbError = new StringBuilder(this.getClass().getName()+"."+Thread.currentThread().getStackTrace()[1].getMethodName());
                sbError.append("\r\n").append(e.toString());
                mylogging.log(Level.SEVERE, sbError.toString());
            }             
        } else {
            airspFolderOK = true;
        }
        
        if (airspFolderOK) {
            String executionPath = System.getProperty("user.dir");
            switch (currOs) {
                case WINDOWS :
                    // to do windows path testing
                    pathReverbin = executionPath+File.separator+"Wreverbins.exe";    // Windows
                    File fwReverbin = new File(pathReverbin);
                    if(fwReverbin.exists()) reverbinOK = true;  
                    break;
                case MACOS :
                    pathReverbin = executionPath+File.separator+"Reverbin";
                    File fReverbin = new File(pathReverbin);
                    if(fReverbin.exists()) reverbinOK = true;                        
                    break;
            } 
            if (!reverbinOK) 
                res = 204;   // Module Reverbin introuvable
        }
        
        if (airspFolderOK && reverbinOK) res = 0;
                           
        return res;                        
        
    }
    
    
    private boolean createAirSpFolder() {
        
        boolean res = false;
        
        try{
            String airspPath = fDrive.getAbsolutePath()+File.separator+"AIRSP";
            File f = new File(airspPath);
            if(f.mkdir()) { 
                fAirsp = f;
                res = true;
            } else {
                sbError = new StringBuilder(this.getClass().getName()+"."+Thread.currentThread().getStackTrace()[1].getMethodName());
                sbError.append("\r\n").append("Unable to create Reversale airspace folder ");
                mylogging.log(Level.SEVERE, sbError.toString());
            }              
        }catch (Exception e) {//Catch exception if any
            sbError = new StringBuilder(this.getClass().getName()+"."+Thread.currentThread().getStackTrace()[1].getMethodName());
            sbError.append("\r\n").append(e.toString());
            mylogging.log(Level.SEVERE, sbError.toString());
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
                    // To manage the bug we can't use ClosingDate
                    //if (files[i].getName().substring(0,7).compareTo(closingDate) > 0) {
                        trackPathList.add(files[i].getPath());
                   // }
                }
            }
            if (files[i].isDirectory()) {
                exploreFolder(files[i], trackPathList);
            }
        }        
    }
            
    /**
     * Different from exploreFolder, not only an extension file difference
     * We don't take care of closingDate
     * @param dir
     * @param wpPathList
     * @throws Exception 
     */
    private void exploreFolderWp(File dir, ArrayList<String> wpNameList) throws Exception {       
        File[] files = dir.listFiles();
        for (int i = 0; i < files.length; i++) {
            // We had a problem with an empty folder.
            // this folder trigerred a dead loop
            // In this case files.length had a value of 1 instead of 0 !!!
            if (files[i].isDirectory() && !files[i].getName().equals(dir.getName())) {
                exploreFolder(files[i], wpNameList);                
            } else {
                String fileName = files[i].getName();
                if (fileName.endsWith(".wpt") || fileName.endsWith(".WPT") || fileName.endsWith(".kml") || fileName.endsWith(".KML")) {                                    
                    // Problem of dot files writed by MacOS 
                    if (files[i].isFile() && !fileName.startsWith("._") && files[i].getName().length() > 3) {    
                            wpNameList.add(files[i].getName()); 
                            wpPathList.add(files[i].getPath());                        
                    }
                }
            }
        }        
    }      
    
    /**
     * Recursive track search in selected folder and sub folders
     * @param dir
     * @throws Exception 
     */
    public void listTracksFiles(ArrayList<String> trackPathList) throws Exception {   
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
    
    public void listWaypFiles(ArrayList<String> waypPathList) throws Exception {   
        wpPathList = new ArrayList<>();        
        if (fWayp != null && fWayp.exists())  {        
           exploreFolderWp(fWayp, waypPathList);
        }
        if (fGoog != null && fGoog.exists())  {        
           exploreFolderWp(fGoog, waypPathList);
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
        
    
}
