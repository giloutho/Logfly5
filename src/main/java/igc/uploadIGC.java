/* 
 * Copyright Gil THOMAS
 * This file forms an integral part of Logfly project
 * See the LICENSE file distributed with source code
 * for details of Logfly licence project
 */
package igc;

import dialogues.ProgressForm;
import dialogues.alertbox;
import java.awt.Desktop;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Random;
import javafx.concurrent.Task;
import trackgps.traceGPS;

/**
 *
 * @author Gil Thomas logfly.org
 * Upload a track in a server and call VisuGPS service
 */
public class uploadIGC {
    
    private boolean uploadOk;
    private String fTempName;
    private final String CrLf = "\r\n";
    private String pathTemp;
    private String fName;
    private Locale currLocale;
       
    public uploadIGC(traceGPS currTrace, Locale myLocale)  {
        currLocale = myLocale;
        uploadOk = false;
        Export_Visu(currTrace);  
    }

    public boolean isUploadOk() {
        return uploadOk;
    }

    public String getfTempName() {
        return fTempName;
    }
               
    
    /**
     * Selected track is exported in an IGC temp file
     * @param currTrace 
     */
    private void Export_Visu(traceGPS currTrace)  {        
        
        // Name of the file is YYYYMMDDHHMMSS+random number
        LocalDateTime ldt = LocalDateTime.now(); 
        DateTimeFormatter dTF = DateTimeFormatter.ofPattern("YYYYMMddHHmm");
        Random rand = new Random();
        // nextInt(n) send back an integer from 0 (included) to n (excluded) 
        int nombre = rand.nextInt(1001);
        // file YYYYMMDDHHMMSS_Random is created in usaul temp folder
        fileIGC tempTrack = new fileIGC(currLocale);
        fName = ldt.format(dTF)+String.valueOf(nombre)+".igc";
        pathTemp = tempTrack.creaTempIGC(currTrace, fName);
        if (pathTemp != null)  {
           uploadTxt();
        } else  {
            alertbox errMsg = new alertbox(currLocale);
            errMsg.alertNumError(9);   // Unable to create temp file
        }       
    }
    
    /**
     * Upload the temp file to the server logfly.org with a special php script
     */
    private void uploadTxt()  {
            ProgressForm pForm = new ProgressForm();
           
            Task<Void> task = new Task<Void>() {
                @Override
                public Void call() throws InterruptedException {                                       
                    
                    URLConnection conn = null;
                    OutputStream os = null;
                    InputStream is = null;
                    int sizeProg = 0;
                    try {
                            URL url = new URL("http://www.logfly.org/Visu/jtransfert.php");
                            conn = url.openConnection();
                            conn.setDoOutput(true);

                            String postData = "";

                            File myFile = new File (pathTemp);
                            sizeProg = (int)myFile.length()+(4*1024);
                            byte [] txtData  = new byte [(int)myFile.length()];
                            
                            FileInputStream fis = new FileInputStream(myFile);
                            BufferedInputStream bis= new BufferedInputStream(fis);
                            bis.read(txtData,0,txtData.length);


                            String message1 = "";
                            message1 += "-----------------------------4664151417711" + CrLf;
                            message1 += "Content-Disposition: form-data; name=\"uploadedfile\"; filename=\""+fName+"\""+ CrLf;
                            message1 += "Content-Type: text/plain" + CrLf;
                            message1 += CrLf;

                            // the file is sent between the messages in the multipart message.

                            String message2 = "";
                            message2 += CrLf + "-----------------------------4664151417711--"
                                    + CrLf;

                            conn.setRequestProperty("Content-Type",
                                    "multipart/form-data; boundary=---------------------------4664151417711");
                            // might not need to specify the content-length when sending chunkeddata.
                            conn.setRequestProperty("Content-Length", String.valueOf((message1
                                    .length() + message2.length() + txtData.length)));

                            os = conn.getOutputStream();

                            System.out.println(message1);
                            os.write(message1.getBytes());

                            // Envoi du fichier
                            int index = 0;
                            int size = 1024;
                            do {
                                System.out.println("write:" + index);
                                if ((index + size) > txtData.length) {
                                    size = txtData.length - index;
                                }
                                os.write(txtData, index, size);
                                // updateProgress(index, sizeProg);
                                // Without UpdateProgress animation is infinite
                                // We prefer this because progressbar display 100% before page display by VisuGPS
                                index += size;
                            } while (index < txtData.length);
                            System.out.println("written:" + index);
                            
                            System.out.println(message2);
                            os.write(message2.getBytes());
                            os.flush();

                            System.out.println("open is");
                            is = conn.getInputStream();

                            char buff = 512;
                            int len;
                            int idxProg = index;
                            byte[] data = new byte[buff];
                            do {
                                System.out.println("READ");                                
                                //  updateProgress(idxProg, sizeProg);
                                // Without UpdateProgress animation is infinite
                                // We prefer this because progressbar display 100% before page display by VisuGPS
                                len = is.read(data);                                
                                if (len > 0) {
                                    System.out.println(new String(data, 0, len));
                                }
                                idxProg += 1024;
                            } while (len > 0);
                            
                            System.out.println("DONE");
                            //res = true;

                        } catch (Exception e) {
                            e.printStackTrace();
                        } finally {
                            System.out.println("Close connection");
                            updateProgress(sizeProg, sizeProg);
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
                    
                    return null ;
                }
            };

            // binds progress of progress bars to progress of task:
            pForm.activateProgressBar(task);

            // End of task, we run the page display of VisuGPS
            task.setOnSucceeded(event -> {
                pForm.getDialogStage().close();
                lanceVisu();
            });

            pForm.getDialogStage().show();

            Thread thread = new Thread(task);
            thread.start();        
    }
    
    /**
     * Run page display of VisuGPS
     */
    private void lanceVisu()  {
        // En principe, initilisés par un fichier de configuration
        String appVisuURL = "http://www.victorb.fr/visugps/visugps.html?track=";
        String appServerURL = "http://www.logfly.org/Visu/";

        String totUrl = appVisuURL+appServerURL+fName;
        System.out.println("url : "+totUrl);
        // A simple code with Desktp class seems to be unsupported on Linux         
        if(Desktop.isDesktopSupported()){
            Desktop desktop = Desktop.getDesktop();
            try {
                desktop.browse(new URI(totUrl));
            } catch (IOException | URISyntaxException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        } else {
            Runtime runtime = Runtime.getRuntime();
            try {
                runtime.exec("xdg-open " + totUrl);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }        
        
    }
    
    
}
