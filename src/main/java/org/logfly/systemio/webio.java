/* 
 * Copyright Gil THOMAS
 * This file forms an integral part of Logfly project
 * See the LICENSE file distributed with source code
 * for details of Logfly licence project
 */
package org.logfly.systemio;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 *
 * @author gil
 * internet utilities
 * 
 */
public class webio {
    
    private int dlError;

    public int getDlError() {
        return dlError;
    }
    
    
    
    /**
     * url checking 
     * return code is : status HTTP_OK = 200
     * all codes -> http://docs.oracle.com/javase/7/docs/api/java/net/HttpURLConnection.html
     * @param strUrl
     * @return
     * @throws Exception 
     */
    public int testURL(String strUrl) throws Exception {    
        int res = 0;
        
        try {
            URL url = new URL(strUrl);
            HttpURLConnection urlConn = (HttpURLConnection) url.openConnection();
            urlConn.connect();
            res = urlConn.getResponseCode();
        } catch (IOException e) {
            e.printStackTrace();
            throw e;
        }
        
        return res;
    }
    
    public boolean checkConnection() throws MalformedURLException, IOException {
        boolean res = false;
        
        try {
            URL url = new URL("http://www.google.com");
 
            URLConnection connection = url.openConnection();
            connection.connect();   
            
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.connect();
            if (con.getResponseCode() == 200) res = true;					            
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return res;
    }
    
    /**
     * Give a special random name for track upload.
     *      YYYYMMDDHHMMSS_Randomnumber  [between 1 and 1000]
     * @return 
     */
    public String aleaNomfichier()  {
        String res = null;
        
        LocalDateTime ldt = LocalDateTime.now();
        String sLdt = ldt.format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss_"));    
        // we want a number between 1 and 1000
        // xLogfly -> Min + (int)(Math.random() * ((Max - Min) + 1))  
        // found in http://stackoverflow.com/questions/363681/generating-random-integers-in-a-specific-range
        int aleaNumber = 1 + (int)(Math.random() * ((1000 - 1) + 1));
        StringBuilder suggName = new StringBuilder();
        suggName.append(sLdt).append(String.format("%d",aleaNumber)).append(".igc");
        res = suggName.toString();
        
        return res;
    }
    
    /**
     * Upload track (text file) to the specified url
     * @param txtData
     * @param sUrl
     * @return 
     */
    public String httpUploadIgc(byte[] txtData, String sUrl)  {
        
        String res = null;
        String CrLf = "\r\n";
        String tempFicName;
        OutputStream os = null;
        InputStream is = null;
        
        URLConnection conn = null;
        tempFicName = aleaNomfichier();
        
        try {
            URL url = new URL(sUrl);
            conn = url.openConnection();
            conn.setDoOutput(true);
            
            StringBuilder msg1 = new StringBuilder();
            msg1.append("-----------------------------4664151417711").append(CrLf);
            msg1.append("Content-Disposition: form-data; name=\"uploadedfile\"; filename=\"").append(tempFicName).append("\"").append(CrLf);
            msg1.append("Content-Type: text/plain").append(CrLf);
            msg1.append(CrLf);
            String message1 = msg1.toString();
            // file is sent between messages in the multipart
            StringBuilder msg2 = new StringBuilder();
            msg2.append(CrLf).append("-----------------------------4664151417711--").append(CrLf);
            String message2 = msg2.toString();

            conn.setRequestProperty("Content-Type","multipart/form-data; boundary=---------------------------4664151417711");
            // might not need to specify the content-length when sending chunked data.
            conn.setRequestProperty("Content-Length", String.valueOf((message1.length() + message2.length() + txtData.length)));
            os = conn.getOutputStream();
            os.write(message1.getBytes());
            // Envoi du fichier
            int index = 0;
            int size = 1024;
            do {
                if ((index + size) > txtData.length) {
                    size = txtData.length - index;
                }
                os.write(txtData, index, size);
                index += size;
            } while (index < txtData.length);
            
            os.write(message2.getBytes());
            os.flush();

            is = conn.getInputStream();

            char buff = 512;
            int len;
            byte[] data = new byte[buff];
            StringBuilder sbRep = new StringBuilder();
            do {
                len = is.read(data);
                if (len > 0) {
                    sbRep.append(new String(data, 0, len));
                }               
            } while (len > 0);
            
            if (sbRep.toString().contains("Download")) {
                dlError = 0;
                res = tempFicName;
            } else if (sbRep.toString().contains("Error = quota")) { 
                dlError = 1301;                
            } if (sbRep.toString().contains("Error = size")) { 
                dlError = 1305; 
            }
            
            
        } catch (Exception e) {
            dlError = 1310;
        } finally {
            try {
                os.close();
            } catch (Exception e) {
                dlError = 1310;
            }
            try {
                is.close();
            } catch (Exception e) {
                dlError = 1310;
            }
        }
        
        return res;
    }
    
    public int sendPost(String url, String urlParameters) throws Exception {
        String USER_AGENT = "Mozilla/5.0";

        URL obj = new URL(url);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();

        //add request header
        con.setRequestMethod("POST");
        con.setRequestProperty("User-Agent", USER_AGENT);
        con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");        

        // Send post request
        con.setDoOutput(true);
        DataOutputStream wr = new DataOutputStream(con.getOutputStream());
        wr.writeBytes(urlParameters);
        wr.flush();
        wr.close();

        int responseCode = con.getResponseCode();
        
        // Code 200 -> success for http request
        // Code 404 -> Page not found...

        BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();

        while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
        }
        in.close();
  
        
        return responseCode;
    }
    
}
