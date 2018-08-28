/* 
 * Copyright Gil THOMAS
 * This file forms an integral part of Logfly project
 * See the LICENSE file distributed with source code
 * for details of Logfly licence project
 */
package systemio;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.logging.Level;
import Logfly.Main;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 *
 * @author Gil Thomas logfly.org
 * Input/output utilities
 * 
 */
public class textio {
    
    StringBuilder sbError;
    
    /**
     * Fastest method read in http://javarevisited.blogspot.fr/2016/07/10-examples-to-read-text-file-in-java.html
     * @param fichier
     * @return 
     */
    public String readTxt(File fichier){
        String res = null;
        
        try {
            res = new String(Files.readAllBytes(Paths.get(fichier.getAbsolutePath())));     
        } catch (IOException e) {
            sbError = new StringBuilder(this.getClass().getName()+"."+Thread.currentThread().getStackTrace()[1].getMethodName());
            sbError.append("\r\n").append(e.toString());
            mylogging.log(Level.SEVERE, sbError.toString());
        }    
                                
        return res;
    }
    
    public static boolean writeTxtFile(File fichier, String sContent) throws FileNotFoundException, IOException  {
        boolean res = false;
        
        BufferedWriter bw = new BufferedWriter(new FileWriter(fichier));
        try{    	    
    	    bw.write(sContent);
        } finally {
    	    bw.close();    	
            res = true;
    	}
        
        return res;
    }
    
    public static String getFileExtension(File file) {
        String name = file.getName();
        // If fileName do not contain "." or starts with "." then it is not a valid file
        if (name.contains(".") && name.lastIndexOf(".")!= 0) {
            try {
                return name.substring(name.lastIndexOf(".") + 1);
            } catch (Exception e) {
                return "";
            }
        } else {
           return ""; 
        }
    }        
    
    /**
     * Gets the base name, without extension, of given file name.
     * e.g. getBaseName("file.txt") will return "file"
     *
     * @param fileName
     * @return the base name
     */
    public static String getBaseName(String fileName) {
        int index = fileName.lastIndexOf('.');
        if (index == -1) {
            return fileName;
        } else {
            return fileName.substring(0, index);
        }
    }    
    
}
