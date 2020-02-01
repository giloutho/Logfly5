/*
 * Copyright Gil THOMAS
 * This file forms an integral part of Logfly project
 * See the LICENSE file distributed with source code
 * for details of Logfly licence project
 */
package srtm;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.ref.SoftReference;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Map;
import org.xnap.commons.i18n.I18n;
import settings.configProg;

/**
 *
 * @author Simone Rondelli - https://github.com/monejava/unibo-geotools
 * decoration gil
 */

public class srtmcalc {
    
    private String srtmDir;
    private configProg myConfig;
    private I18n i18n; 
    private double THREE_SECONDS_IN_DEGREE = 1200.0;    
    private Map<File, SoftReference<BufferedInputStream>> srtmMap;
    private boolean readySrtm;


    public srtmcalc(configProg pConfig) {   
        myConfig = pConfig;  
        this.i18n = myConfig.getI18n();
        File fSrtm = new File(myConfig.getPathW()+File.separator+"Srtm");
        if (!fSrtm.exists()) fSrtm.mkdirs();        
        if (fSrtm.exists()) {
            this.srtmDir = fSrtm.getAbsolutePath();
            srtmMap = new HashMap<File, SoftReference<BufferedInputStream>>();            
            readySrtm = true;
        } else {
            readySrtm = false;
        }
    }        

    public boolean isReadySrtm() {
        return readySrtm;
    }
           
    public double getHeight(double lat, double lon) throws IOException {    
        double val = 9999;    
        
        srtmhgt selHgt = new srtmhgt(i18n, srtmDir, lat, lon);
        File fHgt = selHgt.getHgtFile();
        if (fHgt != null) {
            val = extractHeight(lat,lon, fHgt);
        } 
        
        return val;        
    }
    
    public double extractHeight(double lat, double lon, File fHgt) throws IOException {

        double left;
        double bottom;
        double calcHeight = 9999;
             
        left = Math.floor(lat);
        bottom = Math.floor(lon);
        int colmin = (int) Math.floor((lat-left)*THREE_SECONDS_IN_DEGREE+0.5);
        int rowmin = (int) Math.floor((lon-bottom)*THREE_SECONDS_IN_DEGREE+0.5);
        calcHeight = getValues(fHgt, rowmin, colmin);
        
        return calcHeight;
    }    
    
    /**
     * Return the SRTM file name without the extension
     * 
     * @param lat Latitude
     * @param lon Longitude
     * @return SRTM filename
     */
    private String getSrtmFileName(double lat, double lon) {
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

        return NS + f_nlat + WE + f_nlon;
    }    
    
    private double getValues(File fHgt, int rowmin, int colmin) throws IOException {

         SoftReference<BufferedInputStream> inRef = srtmMap.get(fHgt);
         BufferedInputStream in = (inRef != null) ? inRef.get() : null;                       
         if (in == null) {
                 int srtmbuffer = 1201 * 1201 * 2;
                 in = new BufferedInputStream(new FileInputStream(fHgt), srtmbuffer);
                 srtmMap.put(fHgt, new SoftReference<BufferedInputStream>(in));                    
                 in.mark(srtmbuffer);
         }
         in.reset();

         long starti = ((1200 - colmin) * 2402) + (rowmin * 2);

         int newIndex = 2*((1201-colmin)*1201+rowmin);
         in.skip(newIndex);

        // in.skip(starti);

         short myShort = readShort(in);            

         return myShort;
    }  
    
    private short readShort(BufferedInputStream in) throws IOException {
            int ch1 = in.read();
            byte byte1 = (byte) ch1;
            int ch2 = in.read();
            byte byte2 = (byte) ch2;
            int newAlt = (256*byte1 + ((byte2>0)?byte2:byte2+256));
            return (short) ((ch1 << 8) + (ch2));

    }     
}
