/*
 * Copyright Gil THOMAS
 * This file forms an integral part of Logfly project
 * See the LICENSE file distributed with source code
 * for details of Logfly licence project
 */
package srtm;

import java.awt.Desktop;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.xnap.commons.i18n.I18n;
import systemio.webdown;

/**
 *
 * @author gil
 */
public class srtmhgt {
    
    private String srtmDir;
    private String fSrtmName;
    private I18n i18n; 
    private double lat;
    private double lon;
    private File hgtFile = null;

    public File getHgtFile() {
        return hgtFile;
    }

    public void setHgtFile(File hgtFile) {
        this.hgtFile = hgtFile;
    }
        
    public srtmhgt(I18n pI18n, String pDir, double pLat, double pLong) {        
        this.i18n = pI18n;
        this.srtmDir = pDir;
        this.lat = pLat;
        this.lon = pLong;
        getSrtmFile();
    }    
    
    private void getSrtmFile() {
        
        File fSrtm = null;
        
        fSrtmName = getSrtmFileName();
        
        if (fSrtmName != null) {
            fSrtm = new File(srtmDir+File.separator+fSrtmName);
            if (fSrtm.exists()) {
                setHgtFile(fSrtm);
            } else {
                String zipName = zoning();
                downloadSrtmZip(zipName);    
                if (fSrtm.exists()) {
                    setHgtFile(fSrtm);
                } else {
                   setHgtFile(null);
                }
            }
        }        
    }
    
    private void downloadSrtmZip(String zipName) {
        
        String sUrl = "http://viewfinderpanoramas.org/dem3/"+zipName+".zip";       
        String msg = i18n.tr("Srtm file import completed");       
        webdown myLoad = new webdown(sUrl,srtmDir, i18n, msg);
        if (myLoad.isDownSuccess()) {
            File downFile = new File(myLoad.getDownPath());
            unzipping(zipName);
        } else {
            
        }                        
    }
    
    private void unzipping(String fName ) {

        int bufferSize = 8192;   
        int hgtNb = 0;
        //String pattern = Pattern.quote(System.getProperty("file.separator"));
        String pattern = Character.toString((char)47);

        String source = srtmDir+File.separator+fName+".zip";
        File fZip = new File(source);
        // it's necessary to create the parent folder - fName folder   

        try {
            //Compress file
            FileInputStream fileInputStream = new FileInputStream(source);
            BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream);
            ZipInputStream zipInputStream = new ZipInputStream(bufferedInputStream);

            ZipEntry zipEntry;

            String unzippedMsg = "";

            while ((zipEntry = zipInputStream.getNextEntry()) != null) {
                try {
                    byte[] buffer = new byte[bufferSize];
                    String fHgt = null;
                    // L32/N44E006.hgt
                    String[] s = zipEntry.getName().split(pattern);
                    for (int i = 0; i < s.length; i++) {
                        if (s[i].contains(".hgt")) {
                            fHgt = s[i];
                        }
                    }
                    if (fHgt != null) {
                        String unzippedFile = srtmDir+File.separator+ fHgt;
                        FileOutputStream fileOutputStream = new FileOutputStream(unzippedFile);
                        int size;
                        while ((size = zipInputStream.read(buffer)) != -1) {
                            fileOutputStream.write(buffer, 0, size);
                        }
                        fileOutputStream.flush();
                        fileOutputStream.close();
                        unzippedMsg += unzippedFile + "\n";
                        hgtNb++;
                    }
                } catch (Exception ex) {
                }
            }
            zipInputStream.close();
            if (hgtNb > 0) {
                fZip.delete();
                
            }
        } catch (IOException ex) {
            System.out.println("Exception : "+ex.toString());
        }        
    }
    
    
    private String zoning() {
        String res = null;
        try {
            // Zonage latitude
            double dLat = Math.abs(lat);
            int d = (int) dLat/4;
            int zone = 65+d;   
            res = Character.toString ((char) zone);   
            if (lon > 0) {
                int e = (int) lon/6;
                zone = 31+e;                
            } else {
                double dLong = Math.abs(lon);
                int f = (int) dLong/6;
                zone = 30-f;                
            }
            res = res + String.valueOf(zone);
            if (lat < 0) {
                res = "S"+res;
            }
        } catch (Exception e) {
            
        }
                        
        return res;
    }        

    /*
     * Return the SRTM file name without the extension
     * 
     * @param lat Latitude
     * @param lon Longitude
     * @return SRTM filename
     */
    public String getSrtmFileName() {
            int nlat = Math.abs((int) Math.floor(lat));
            int nlon = Math.abs((int) Math.floor(lon));

            NumberFormat nf = NumberFormat.getInstance();
            String NS, WE;
            String f_nlat, f_nlon;

            if (lat > 0) {
                    NS = "N";
            } else {
                    NS = "S";
            }
            if (lon > 0) {
                    WE = "E";
            } else {
                    WE = "W";
            }

            nf.setMinimumIntegerDigits(2);
            f_nlat = nf.format(nlat);
            nf.setMinimumIntegerDigits(3);
            f_nlon = nf.format(nlon);
            
            StringBuilder sb = new StringBuilder();
            sb.append(NS).append(f_nlat).append(WE).append(f_nlon);
            sb.append(".hgt");
            return sb.toString();
    }    
    
    
}
