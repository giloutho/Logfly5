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
 * Compass C pilot is a USB GPS where tracks are stored in a folder called "tracks"
 * each track is named like : YYYY-MM-DD-CPP000-xx-pilot-name.igc eg 2016-10-02-CPP000-02-dupont-philippe.igc
 */
public class compass {
    
    private boolean connected = false;
    private File fFlights; 
    private File fConfig;
    private File fWaypoint;
    private File fDrive;
    private int idxDrive;    
    private ObservableList <String> driveList;  
    private String closingDate;
    private String msgClosingDate;   

    public boolean isConnected() {
        return connected;
    }

    private void setConnected(boolean connected) {
        this.connected = connected;
    }


    public File getfFlights() {
        return fFlights;
    }

    public ObservableList<String> getDriveList() {
        return driveList;
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

    public compass(osType currOs, int gpsLimit) {
        boolean conn = false;
        fFlights = null;        
        fDrive = null;   
        idxDrive = 0;
        driveList = FXCollections.observableArrayList();
        
        setDateLevel(gpsLimit);        
        conn = testConnection(currOs);                               
    }    
    
    private void setDateLevel(int gpsLimit) { 

        Calendar myCalendar = Calendar.getInstance();
        myCalendar.add(Calendar.MONTH, -(gpsLimit));
        SimpleDateFormat sdf = new SimpleDateFormat("YYYY-MM-dd");
        closingDate = sdf.format(myCalendar.getTime());   
        SimpleDateFormat sdfMsg = new SimpleDateFormat("dd/MM/YY");
        msgClosingDate = sdfMsg.format(myCalendar.getTime());  
        
    }   

    public boolean testConnection(osType currOs) {
        
        boolean res = false;
        boolean cond1 = false;
        boolean cond2 = false;
        boolean cond3 = false;
        
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
                            if (listFile[i].getName().equals("displays") && listFile[i].isDirectory()) {
                                fConfig = listFile[i];
                                cond1 = true;
                            }
                            if (listFile[i].getName().equals("waypoints") && listFile[i].isDirectory()) {
                                fWaypoint = listFile[i];
                                cond2 = true;
                            }                            
                            if (listFile[i].getName().equals("tracks") && listFile[i].isDirectory()) {
                                fFlights = listFile[i];
                                cond3 = true;
                            }
                        }
                        if (cond1 == true && cond2 == true && cond3 == true) {
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
    
    private void exploreFolder(File dir, ArrayList<String> trackPathList) throws Exception {  
        // Recursivité à vérifier pour le skytraax        
        File[] files = dir.listFiles();
        for (int i = 0; i < files.length; i++) {
            String fileName = files[i].getName();
            if (fileName.endsWith(".igc") || fileName.endsWith(".IGC")) {                                    
                // Problem of dot files writed by MacOS 
            //    System.out.println(files[i].getPath());
                if (files[i].isFile() && !fileName.startsWith("._") && files[i].getName().length() > 9) {
                    if (files[i].getName().substring(0,10).compareTo(closingDate) > 0) {
                        trackPathList.add(files[i].getPath());
                        System.out.println("    add : "+files[i].getPath());
                    }
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
    
}
