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
import java.util.logging.Logger;
import Logfly.Main;

/**
 *
 * @author Gil Thomas logfly.org
 * Input/output utilities
 * 
 */
public class textio {
    
    /**
     * Old method unused
     * @param fichier
     * @return 
     */
    public String readTxt(File fichier){
        String res = null;
        
        long topDebut = System.currentTimeMillis();
        BufferedReader br = null;
        StringBuilder sbTexte = new StringBuilder();              
        try {
                InputStream ips=new FileInputStream(fichier); 
                InputStreamReader ipsr=new InputStreamReader(ips);
                br=new BufferedReader(ipsr);
                String ligne;
                while ((ligne=br.readLine())!=null){
                        sbTexte.append(ligne+"\n");
                }       
                res = sbTexte.toString();
                try {
                    br.close();
                } catch (IOException ex) {
                    Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
                }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
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
        try {
            return name.substring(name.lastIndexOf(".") + 1);
        } catch (Exception e) {
            return "";
        }
    }
    
}
