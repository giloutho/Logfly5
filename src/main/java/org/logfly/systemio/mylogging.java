/*
 * Copyright Gil THOMAS
 * This file forms an integral part of Logfly project
 * See the LICENSE file distributed with source code
 * for details of Logfly licence project
 */
package org.logfly.systemio;

import java.io.File;
import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import org.logfly.settings.configProg;

/**
 *
 * @author gil
 * 
 */
public class mylogging {
    
    static Logger logger;
    public Handler fileHandler;
    SimpleFormatter plainText;

    private mylogging() throws IOException{
        //instance the logger
        logger = Logger.getLogger(mylogging.class.getName());
        //instance the filehandler
        String logPath;
        String OS = System.getProperty("os.name").toLowerCase();
        if (OS.indexOf("win") >= 0) {
            logPath = System.getProperty("user.home")+"\\AppData\\Roaming"; 
        } else if (OS.indexOf("mac")>= 0) {
            logPath = System.getProperty("user.home")+"/Library/Preferences";
        } else if (OS.indexOf("nix") >= 0 || OS.indexOf("nux") >= 0 || OS.indexOf("aix") > 0 ) {
            logPath = System.getProperty("user.home")+"/.logfly";   
        } else {
            logPath = null;
        }                 
        if (logPath != null && !logPath.isEmpty()) {
            fileHandler = new FileHandler(logPath+ File.separator +"logfly.log",true);    
        } else {
            fileHandler = new FileHandler("logfly.log",true);
        }
        //instance formatter, set formatting, and handler
        plainText = new SimpleFormatter();
        fileHandler.setFormatter(plainText);
        logger.addHandler(fileHandler);
    }
    
    private static Logger getLogger(){
        if(logger == null){
            try {
                new mylogging();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return logger;
    }
    
    public static void log(Level level, String msg){
        getLogger().log(level, msg);
        System.out.println(msg);
    }
    
    public static String readLogFile() {
        String logTxt;
        textio currLog = new textio();     
        String logPath;
        String OS = System.getProperty("os.name").toLowerCase();
        if (OS.indexOf("win") >= 0) {
            logPath = System.getProperty("user.home")+"\\AppData\\Roaming\\logfly.log"; 
        } else if (OS.indexOf("mac")>= 0) {
            logPath = System.getProperty("user.home")+"/Library/Preferences/logfly.log";
        } else if (OS.indexOf("nix") >= 0 || OS.indexOf("nux") >= 0 || OS.indexOf("aix") > 0 ) {
            logPath = System.getProperty("user.home")+"/.logfly/logfly.log";   
        } else {
            logPath = null;
        }                                    
        File fileLog = new File(logPath);
        if (fileLog.exists()) 
            logTxt = currLog.readTxt(fileLog); 
        else {
            fileLog = new File("logfly.log");
            if (fileLog.exists()) 
                logTxt = currLog.readTxt(fileLog); 
            else
                logTxt = "Log file not found";
        }
        
        return logTxt;
    }    
    
}
