/*
 * Copyright Gil THOMAS
 * Ce fichier fait partie intégrante du projet Logfly
 * Pour tous les détails sur la licence du projet Logfly
 * Consulter le fichier LICENSE distribué avec le code source
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
 */
public class textio {
    
    /**
     * Prcedure provenant du projet test Logbook
     * inutilisée pour l'instant
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
