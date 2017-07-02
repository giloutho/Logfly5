/*
 * Copyright Gil THOMAS
 * Ce fichier fait partie intégrante du projet Logfly
 * Pour tous les détails sur la licence du projet Logfly
 * Consulter le fichier LICENSE distribué avec le code source
 */
package systemio;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 *
 * @author gil
 */
public class webio {
    
    /**
     * Test de validité d'une url. 
     * Le code renvoyé est le status HTTP_OK = 200
     * tous les codes -> http://docs.oracle.com/javase/7/docs/api/java/net/HttpURLConnection.html
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
    
    /**
     * Pour la trace a télécharger, on compose un nom de la forme :
     *      YYYYMMDDHHMMSS_Aleatoire  [Aléatoire = nombre entre 1 et 1000]
     * @return 
     */
    public String aleaNomfichier()  {
        String res = null;
        
        LocalDateTime ldt = LocalDateTime.now();
        String sLdt = ldt.format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss_"));    
        // On veut un nombre aleatoire entre 1 et 1000
        // Min + (int)(Math.random() * ((Max - Min) + 1))
        // glané sur http://stackoverflow.com/questions/363681/generating-random-integers-in-a-specific-range
        int aleaNumber = 1 + (int)(Math.random() * ((1000 - 1) + 1));
        StringBuilder suggName = new StringBuilder();
        suggName.append(sLdt).append(String.format("%d",aleaNumber)).append(".igc");
        res = suggName.toString();
        
        return res;
    }
    
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
            // le fichier est envoyé entre les messages dans le multipart
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
            System.out.println("written:" + index);
            
            os.write(message2.getBytes());
            os.flush();

            is = conn.getInputStream();

            char buff = 512;
            int len;
            byte[] data = new byte[buff];
            do {
                len = is.read(data);                
            } while (len > 0);

            res = tempFicName;
            
        } catch (Exception e) {
            //e.printStackTrace();
            System.out.println("C'est la mémé...");
        } finally {
            System.out.println("Close connection");
            try {
                os.close();
            } catch (Exception e) {
            }
            try {
                is.close();
            } catch (Exception e) {
            }
            try {

            } catch (Exception e) {
            }
        }
        
        return res;
    }
    
}
