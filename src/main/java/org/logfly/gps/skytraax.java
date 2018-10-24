/*
 * Copyright Gil THOMAS
 * This file forms an integral part of Logfly project
 * See the LICENSE file distributed with source code
 * for details of Logfly licence project
 */
package org.logfly.gps;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Optional;
import java.util.stream.Stream;

import org.logfly.settings.osType;
import org.logfly.systemio.textio;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 *
 * @author gil
 * 
 * Skytraxx 2 is a USB GPS with a special tree structure of files/folders
 * in Folder FLIGHTS, there is a year folder with month subfolders and day subfolders
 * FLIGHTS -> 2017 -> January -> 11 -> 71Bxxxx.IGC
 * FLIGHTS -> 2017 -> December -> 25 -> 7CPxxxx.IGC
 * File is named according IGC file specification
 * See gpsutils
 */

public class skytraax {
    
    private boolean connected = false;
    private File fFlights;   
    private File fWayp;      
    private boolean wpExist = false;
    private File fDrive;
    private int idxDrive;    
    private ObservableList <String> driveList;   
    private String closingSky;
    private String closingDate;
    private String msgClosingDate;    
    private String verFirmware;
    private ArrayList<String> wpPathList;
   

    public boolean isConnected() {
        return connected;
    }

    private void setConnected(boolean connected) {
        this.connected = connected;
    }


    public File getfFlights() {
        return fFlights;
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

    public String getVerFirmware() {
        return verFirmware;
    }

    public File getfWayp() {
        return fWayp;
    }

    public boolean isWpExist() {
        return wpExist;
    }

    public ArrayList<String> getWpPathList() {
        return wpPathList;
    }
        
    
            
    public skytraax(osType currOs, int gpsLimit) {
        boolean conn = false;
        fFlights = null;        
        fDrive = null;   
        verFirmware = null;
        // must be initialized here
        idxDrive = 0;
        driveList = FXCollections.observableArrayList();
        
        setDateLevel(gpsLimit);
        
        conn = testConnection(currOs);               
        
    }
    
    /**
     * Il faut fixer la valeur qui sera la skyClosureDate
     * Si Closing date renvoit 01/07/2016 la skyClosureDate sera "67"
     * Si Closing date renvoit 01/11/2015 la skyClosureDate sera "5B"
     * http://bethecoder.com/applications/tutorials/tools-and-libs/google-guava/collections/list-transform.html
     * https://stackoverflow.com/questions/7383624/how-to-transform-listx-to-another-listy
     * 
     * @param gpsLimit 
     */
    private void setDateLevel(int gpsLimit) {       
        int closingYear;
        int closingMonth;
        int closingDay;
        String sYear;
        String sMonth; 
        String sDay;
        
        if (gpsLimit == 0) gpsLimit = 99;
        Calendar myCalendar = Calendar.getInstance();
        myCalendar.add(Calendar.MONTH, -(gpsLimit));
        SimpleDateFormat sdf = new SimpleDateFormat("YYMMdd");
        SimpleDateFormat ydf = new SimpleDateFormat("YYYY");
        SimpleDateFormat mdf = new SimpleDateFormat("MM"); 
        SimpleDateFormat ddf = new SimpleDateFormat("dd");
        
        
        closingYear = Integer.parseInt(ydf.format(myCalendar.getTime()));
        int iYear = (closingYear-2000) %10;    
        sYear = String.valueOf(iYear);
        closingMonth = Integer.parseInt(mdf.format(myCalendar.getTime()));
        sMonth = gpsutils.listMonth.get(closingMonth);
        closingDay = Integer.parseInt(ddf.format(myCalendar.getTime())) - 1;
        sDay = gpsutils.listDay.get(closingDay);
        closingSky = sYear+sMonth+sDay;
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
        wpExist = false;
            
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
            // with Linux, if no USB plugged, drives is null
            for (File aDrive : drives) {       
                long size = aDrive.getTotalSpace();
                float sizeGo = size / 1000000000;    
                StringBuilder sb = new StringBuilder();
                sb.append(aDrive.getName()).append(" ").append(String.format("%4.0f", sizeGo)).append(" Go");
                driveList.add(sb.toString());
                // Capacity > 64 Go jumped
                if (size < 83999999999L) {
                    File listFile[] = aDrive.listFiles();
                    if (listFile != null) {
                        for (int i=0; i<listFile.length; i++) 
                        {
                            if ( !listFile[i].getName().startsWith("."))
                            {
                                if (listFile[i].getName().equals("SYSTEM") && listFile[i].isDirectory()) {
                                    try {
                                        if (exploreFolderSystem(listFile[i])) cond1 = true;
                                    } catch (Exception e) {

                                    }
                                }
                                if (listFile[i].getName().equals("FLIGHTS") && listFile[i].isDirectory()) {
                                    fFlights = listFile[i];
                                    cond2 = true;
                                }
                                if (listFile[i].getName().equals("WAYPOINTS") && listFile[i].isDirectory()) {
                                    fWayp = listFile[i];
                                    wpExist = true;
                                }                                
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
    
    /** 
     * SD card of Skytraax has a folder called SYSTEM with a settings file called system.txt 
     * @param dir
     * @return
     * @throws Exception 
     */
    private boolean exploreFolderSystem(File dir) throws Exception {  
               
        File[] files = dir.listFiles();
        for (int i = 0; i < files.length; i++) {
            String fileName = files[i].getName();
            if (fileName.equals("system.txt")) {   
                readFirmware(files[i]);
                return true;
            }
        } 
        
        return false;
    }   
    
    private void readFirmware(File fSystem) throws IOException {
        
        try {
            Stream<String> lines = Files.lines(Paths.get(fSystem.getAbsolutePath()));
            Optional<String> hasFirmware = lines.filter(s -> s.contains("Firmware:")).findFirst();
            if(hasFirmware.isPresent()){
                String[] sFirmware = hasFirmware.get().split(":");
                if (sFirmware.length > 1) {
                    verFirmware = sFirmware[1];
                } else {
                    verFirmware = "";
                }
            }
            lines.close();           
        } catch (Exception e) {
            e.printStackTrace();
        }        
    }
            
    private void exploreFolder(File dir, ArrayList<String> trackPathList) throws Exception {  
        // Recursivité à vérifier pour le skytraax        
        File[] files = dir.listFiles();
        for (int i = 0; i < files.length; i++) {
            // We had a problem with an empty folder.
            // this folder trigerred a dead loop
            // In this case files.length had a value of 1 instead of 0 !!!
            if (files[i].isDirectory() && !files[i].getName().equals(dir.getName())) {
                exploreFolder(files[i], trackPathList);                
            } else {
                String fileName = files[i].getName();
                if (fileName.endsWith(".igc") || fileName.endsWith(".IGC")) {                                    
                    // Problem of dot files writed by MacOS 
                    if (files[i].isFile() && !fileName.startsWith("._") && files[i].getName().length() > 3) {
                        if (files[i].getName().substring(0,3).compareTo(closingSky) > 0) {                            
                            trackPathList.add(files[i].getPath());
                        }
                    }
                }
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
        // Recursivité à vérifier pour le skytraax        
        wpPathList = new ArrayList<>();
        File[] files = dir.listFiles();
        for (int i = 0; i < files.length; i++) {
            // We had a problem with an empty folder.
            // this folder trigerred a dead loop
            // In this case files.length had a value of 1 instead of 0 !!!
            if (files[i].isDirectory() && !files[i].getName().equals(dir.getName())) {
                exploreFolder(files[i], wpNameList);                
            } else {
                String fileName = files[i].getName();
                if (fileName.endsWith(".wpt") || fileName.endsWith(".WPT")) {                                    
                    // Problem of dot files writed by MacOS 
                    if (files[i].isFile() && !fileName.startsWith("._") && files[i].getName().length() > 3) {    
                            wpNameList.add(files[i].getName()); 
                            wpPathList.add(files[i].getPath());                        
                    }
                }
            }
        }        
    }    
    
    public void listTracksFiles(ArrayList<String> trackPathList) throws Exception {   

        
        if (fFlights != null && fFlights.exists())  {        
           exploreFolder(fFlights, trackPathList);
        }
    }  
    
    public void listWaypFiles(ArrayList<String> waypPathList) throws Exception {   
        
        if (fWayp != null && fWayp.exists())  {        
           exploreFolderWp(fWayp, waypPathList);
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
