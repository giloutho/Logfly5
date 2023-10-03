/*
 * Copyright Gil THOMAS
 * This file forms an integral part of Logfly project
 * See the LICENSE file distributed with source code
 * for details of Logfly licence project
 */
package systemio;

import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.logging.Level;
import org.apache.commons.io.IOUtils;
import settings.privateData;

/**
 *
 * @author gil
 * 
 * For uploading text files to various sites
 * 
 */
public class textdownload {
    
    private StringBuilder sbError;  
    private String url;
    
    public textdownload(int idxUrl) {
        switch (idxUrl) {
            case 1 :
                url = privateData.sitesUrl.toString()+"/Liste_Sites.txt";
                break;
            case 2 :
                //url = privateData.bazileUrl.toString()+"/LastVersionsCatalog_BPa.txt";
                url = privateData.bazileUrl.toString();
                break;
        }
    }
    
    /**
     * With a copy of sitelistdown code, we have an encoding problem 
     * We change for a new code from https://stackoverflow.com/questions/5769717/how-can-i-get-an-http-response-body-as-a-string-in-java/5769756#5769756
     * great article about encoding in java : https://www.codeflow.site/fr/article/java-char-encoding
     * 
     * @return 
     */
    public String askList() {
            
        String sList = null;
        
        try {            
            URL urlBaz = new URL(url);
            URLConnection con = urlBaz.openConnection();
            InputStream in = con.getInputStream();
            String encoding = con.getContentEncoding();
            encoding = encoding == null ? "ISO-8859-1" : encoding;
            String body = IOUtils.toString(in, encoding); 
            sList = body;
        } catch (Exception ex) {
            sbError = new StringBuilder(this.getClass().getName()+"."+Thread.currentThread().getStackTrace()[1].getMethodName());
            sbError.append("\r\n").append(ex.toString());
            mylogging.log(Level.SEVERE, sbError.toString());            
        }


        return sList;

    }         
    
}
