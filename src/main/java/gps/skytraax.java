/*
 * Copyright Gil THOMAS
 * This file forms an integral part of Logfly project
 * See the LICENSE file distributed with source code
 * for details of Logfly licence project
 */
package gps;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Optional;
import java.util.stream.Stream;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import settings.osType;
import systemio.textio;

/**
 *
 * @author gil
 * 
 * Skytraxx 2 is a USB GPS with a special tree structure of files/folders
 * in Folder FLIGHTS, there is a year folder with month subfolders and day subfolders
 * FLIGHTS -> 2017 -> January -> 11 -> 71Bxxxx.IGC
 * FLIGHTS -> 2017 -> December -> 25 -> 7CPxxxx.IGC
 * For a track dated :  2017 september 30th we got an IGC file called : 79Uxxxx.IGC
 * first digit : year   2017 -> 7    (2020 -> will be A ? ) 
 * second digit : month  January -> 1   december -> C
 * third digit : day   First -> 1    31 -> V 
 */

public class skytraax {
    
    private boolean connected = false;
    private File fFlights;   
    private File fDrive;
    private int idxDrive;    
    private ObservableList <String> driveList;   
    private String closingSky;
    private String closingDate;
    private String msgClosingDate;    
    private String verFirmware;
    private ArrayList<String> flightFolderList;
   

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
            
    public skytraax(osType currOs, int gpsLimit) {
        boolean conn = false;
        fFlights = null;        
        fDrive = null;   
        verFirmware = null;
        
        setDateLevel(gpsLimit);
        
        conn = testConnection(currOs);
        
        setConnected(conn);                
        
    }
    
    ArrayList<String> listMonth = new ArrayList<String>() {{
        add("0");        
        add("1");
        add("2");
        add("3");
        add("4");
        add("5");
        add("6");
        add("7");
        add("8");
        add("9");
        add("A");
        add("B");
        add("C");        
    }};    
    
    ArrayList<String> listYear = new ArrayList<String>() {{
        add("0");    // 2010
        add("1");   
        add("2");
        add("3");
        add("4");
        add("5");
        add("6");
        add("7");
        add("8");
        add("9");        
        add("A");
        add("B");
        add("C");
        add("D");
        add("E");
        add("F");
        add("G");
        add("H");
        add("I");
        add("J");     
        add("K");         // 2030  -:)
    }};        
    
    ArrayList<String> listDay = new ArrayList<String>() {{
        add("0");    
        add("1");   
        add("2");
        add("3");
        add("4");
        add("5");
        add("6");
        add("7");
        add("8");
        add("9");        
        add("A");
        add("B");
        add("C");
        add("D");
        add("E");
        add("F");
        add("G");
        add("H");
        add("I");
        add("J");     
        add("K");   
        add("L");
        add("M");
        add("N");
        add("O");
        add("P");
        add("Q");
        add("R");
        add("S");
        add("T");
        add("U");     
        add("V");     // 31         
    }};            
    
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
        
        Calendar myCalendar = Calendar.getInstance();
        myCalendar.add(Calendar.MONTH, -(gpsLimit));
        SimpleDateFormat sdf = new SimpleDateFormat("YYMMdd");
        SimpleDateFormat ydf = new SimpleDateFormat("YYYY");
        SimpleDateFormat mdf = new SimpleDateFormat("MM"); 
        SimpleDateFormat ddf = new SimpleDateFormat("dd");
        
        
        closingYear = Integer.parseInt(ydf.format(myCalendar.getTime()));
        sYear = listYear.get(closingYear-2010);
        closingMonth = Integer.parseInt(mdf.format(myCalendar.getTime()));
        sMonth = listMonth.get(closingMonth);
        closingDay = Integer.parseInt(ddf.format(myCalendar.getTime())) - 1;
        sDay = listDay.get(closingDay);
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
                            if (listFile[i].getName().equals("SYSTEM") && listFile[i].isDirectory()) {
                                try {
                                    if (exploreFolderSystem(listFile[i])) cond1 = true;
                                } catch (Exception e) {
                                    
                                }
                            }
                            if (listFile[i].getName().equals("FLIGHTS") && listFile[i].isDirectory()) {
                                //System.out.println("Fichier : "+listFile[i].getName()+" "+listFile[i].getAbsolutePath());
                                fFlights = listFile[i];
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
                System.out.println(verFirmware);
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
            String fileName = files[i].getName();
            if (fileName.endsWith(".igc") || fileName.endsWith(".IGC")) {                                    
                // Problem of dot files writed by MacOS 
                if (files[i].isFile() && !fileName.startsWith("._") && files[i].getName().length() > 3) {
                    if (files[i].getName().substring(0,3).compareTo(closingSky) > 0) {
                        trackPathList.add(files[i].getPath());
                        System.out.println(files[i].getPath());
                    }
                }
            }
            if (files[i].isDirectory()) {
                exploreFolder(files[i], trackPathList);
            }
        }        
    }
    
    public void listTracksFiles(ArrayList<String> trackPathList) throws Exception {   

        flightFolderList = new ArrayList<>();
        
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
