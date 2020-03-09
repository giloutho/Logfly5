/*
 * Copyright Gil THOMAS
 * This file forms an integral part of Logfly project
 * See the LICENSE file distributed with source code
 * for details of Logfly licence project
 */
package systemio;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.logging.Level;
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
                url = privateData.bazileUrl.toString()+"/LastVersionsCatalog_BPa.txt";
                break;
        }
    }
    
    public String askList() {
    
        StringBuilder sbListe = new StringBuilder();
        
        try {

            URL obj = new URL(url);
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();

            // optional default is GET
            con.setRequestMethod("GET");

            //add request header
            con.setRequestProperty("User-Agent", "Mozilla/5.0");

            int responseCode = con.getResponseCode();
            //System.out.println("\nSending 'GET' request to URL : " + url);
            //System.out.println("Response Code : " + responseCode);

            InputStream in = con.getInputStream();
            try {
              byte[] bytes = new byte[2048];
              int length;

              while ((length = in.read(bytes)) != -1) {
                String s = new String(bytes,0,length);
                sbListe.append(s);           
              }
            } finally {
              in.close();
            }
        } catch (Exception ex) {
            sbError = new StringBuilder(this.getClass().getName()+"."+Thread.currentThread().getStackTrace()[1].getMethodName());
            sbError.append("\r\n").append(ex.toString());
            mylogging.log(Level.SEVERE, sbError.toString());            
        }
        
        return sbListe.toString();

    }         
    
}
