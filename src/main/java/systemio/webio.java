/* 
 * Copyright Gil THOMAS
 * This file forms an integral part of Logfly project
 * See the LICENSE file distributed with source code
 * for details of Logfly licence project
 */
package systemio;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
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
    public String aleaNomfichier(String sExt)  {
        String res = null;
        
        LocalDateTime ldt = LocalDateTime.now();
        String sLdt = ldt.format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss_"));    
        // we want a number between 1 and 1000
        // xLogfly -> Min + (int)(Math.random() * ((Max - Min) + 1))  
        // found in http://stackoverflow.com/questions/363681/generating-random-integers-in-a-specific-range
        int aleaNumber = 1 + (int)(Math.random() * ((1000 - 1) + 1));
        StringBuilder suggName = new StringBuilder();
        suggName.append(sLdt).append(String.format("%d",aleaNumber)).append(sExt);
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
        tempFicName = aleaNomfichier(".igc");
        
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
    
    public void httpUploadFile(File upFile, String sUrl) {        
         
        String charset = "UTF-8";
        String param = "value";
        String boundary = Long.toHexString(System.currentTimeMillis()); // Just generate some unique random value.
        String CRLF = "\r\n"; // Line separator required by multipart/form-data.
                    
        try {
            URLConnection connection = new URL(sUrl).openConnection();
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
            try (
                OutputStream output = connection.getOutputStream();
                PrintWriter writer = new PrintWriter(new OutputStreamWriter(output, charset), true);
            ) {
                    // Send normal param.
                    writer.append("--" + boundary).append(CRLF);
                    writer.append("Content-Disposition: form-data; name=\"param\"").append(CRLF);
                    writer.append("Content-Type: text/plain; charset=" + charset).append(CRLF);
                    writer.append(CRLF).append(param).append(CRLF).flush();                
                    // Send binary file.
                    writer.append("--" + boundary).append(CRLF);
                    writer.append("Content-Disposition: form-data; name=\"binaryFile\"; filename=\"" + upFile.getName() + "\"").append(CRLF);
                    writer.append("Content-Type: " + URLConnection.guessContentTypeFromName(upFile.getName())).append(CRLF);
                    writer.append("Content-Transfer-Encoding: binary").append(CRLF);
                    writer.append(CRLF).flush();
                    Files.copy(upFile.toPath(), output);
                    output.flush(); // Important before continuing with writer!
                    writer.append(CRLF).flush(); // CRLF is important! It indicates end of boundary.          
                    
                    // End of multipart/form-data.
                    writer.append("--" + boundary + "--").append(CRLF).flush();                    
              }
            // Request is lazily fired whenever you need to obtain information about response.            
            int responseCode = ((HttpURLConnection) connection).getResponseCode();
            // Code values https://docs.oracle.com/javase/7/docs/api/java/net/HttpURLConnection.html
            if (responseCode == 200)
                dlError = 0;
            else
                dlError = 1310;
        } catch (Exception e) {
            dlError = 1310;   // Error while transferring file
        }         
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
