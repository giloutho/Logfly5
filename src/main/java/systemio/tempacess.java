/*
 * Copyright Gil THOMAS
 * Ce fichier fait partie intégrante du projet Logfly
 * Pour tous les détails sur la licence du projet Logfly
 * Consulter le fichier LICENSE distribué avec le code source
 */
package systemio;

import java.io.File;

/**
 *
 * @author Gil Thomas logfly.org
 */
public class tempacess {
    
    /* Dans les versions précédentes de Logfly, il y a eu des problèmes de chemin 
    *  ou de droit d'accès pour l'écriture de fichiers temporaires.
    *  Dans un post, il était expliqué que les folders utilisés ci dessous ne posaient pas de problèmes
    *  A voir à l'épreuve du feu...
    */
    
    /**
     * avec fPref = tmp et fExt = txt, on obtient un nom de fichier temporaire avec l'extension demandée
     *      f.getAbsolutePath() -> File path: C:\Users\TP\AppData\Local\Temp\tmp2447618135336474361.txt
     * avec f = File.createTempFile(fPref, null), on obtient un nom de fichier temporaire avec l'extension tmp
     *      f.getAbsolutePath() -> File path: C:\Users\TP\AppData\Local\Temp\tmp1783337266599428081.tmp         
     * @param fPref
     * @param fExt
     * @return 
    */ 
    public static File getTempFile (String fPref, String fExt)  {
        
        File f = null;
        try  {
            // creates temporary file
            f = File.createTempFile(fPref, fExt);                                     
        }catch(Exception e){
         // if any error occurs
         e.printStackTrace();
        }
        
        return f;
    }
       
    /**
     *  Destiné à des fichiers temporaires non destinés à l'utilisateur final
     *  Demande un fichier assigné pour le dossier et le nom passés en paramètre
     *  Sur Mac on obtiendra /Users/Gil/Library/Application Support/dName/fName
     *  Sur Windows Utilisateurs\Users\Appdata\Local\dName\fName
     *  Pas de problèmes de droits parait il ...
     *  La finalité est de récupérer un path, le fichier n'est pas créé
     * @param dName
     * @param fName
     * @return 
     */
    public static File getAppFile(String dName, String fName)  { return new File(getAppDir(dName), fName); }


    // Demande un chemin complet pour le fichier en paramètre
    public static File getAppDir(String dName)  { return getAppDataDir(dName, true); }

    // Assigne un chemin complet au fichier
    public static File getAppDataDir(String dName, boolean doCreate)
    {
        // Get user home + AppDataDir (platform specific) + name (if provided)
        String dir = System.getProperty("user.home");
        if(isWindows) dir += File.separator + "AppData" + File.separator + "Local";
        else if(isMac) dir += File.separator + "Library" + File.separator + "Application Support";
        if(dName!=null) dir += File.separator + dName;

        // Create file, actual directory (if requested) and return
        File dfile = new File(dir);
        if(doCreate && dName!=null) dfile.mkdirs();
        return dfile;
    }
    
     // Whether Windows/Mac
    static boolean isWindows = (System.getProperty("os.name").indexOf("Windows") >= 0);
    static boolean isMac = (System.getProperty("os.name").indexOf("Mac OS X") >= 0);
    
}
