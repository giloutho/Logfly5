/*
 * Copyright Gil THOMAS
 * This file forms an integral part of Logfly project
 * See the LICENSE file distributed with source code
 * for details of Logfly licence project
 */
package systemio;

import java.io.File;
import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import settings.configProg;

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
        configProg myConfig = new configProg();
        myConfig.readSettings();   
        if (myConfig.isValidConfig()) {
            fileHandler = new FileHandler(myConfig.getPathW()+ File.separator +"logfly.log",true);    
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
    
}
