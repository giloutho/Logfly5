/* 
 * Copyright Gil THOMAS
 * This file forms an integral part of Logfly project
 * See the LICENSE file distributed with source code
 * for details of Logfly licence project
 */
package systemio;

import java.io.File;

/**
 *
 * @author Gil Thomas logfly.org
 * Temporary files utility
 * 
 */
public class tempacess {
    
    /* in xLogfly there was some problems with writing temp files path or access rights
    *  We found a post where some folders should not be a problem    
    */
    
    /**
     * with fPref = tmp and fExt = txt, we will have a file with required extension
     *      f.getAbsolutePath() -> File path: C:\Users\TP\AppData\Local\Temp\tmp2447618135336474361.txt
     * with f = File.createTempFile(fPref, null), we will have a file with tmp extension
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
     *  for internal temporary files
     *  Ask for a file in folder parameter an name parameter
     *  On Mac we will have /Users/Gil/Library/Application Support/dName/fName
     *  On Windows Windows Utilisateurs\Users\Appdata\Local\dName\fName
     *  Apparently, no right access... 
     *  Goal is a path, file is not created
     * @param dName
     * @param fName
     * @return 
     */
    public static File getAppFile(String dName, String fName)  { return new File(getAppDir(dName), fName); }


    // Ask for an absolute path with file parameter
    public static File getAppDir(String dName)  { return getAppDataDir(dName, true); }

    // Assign an absolute path
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
