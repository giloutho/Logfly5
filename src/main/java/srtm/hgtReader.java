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
import java.io.FileNotFoundException;
import java.lang.ref.SoftReference;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.ShortBuffer;
import java.nio.channels.FileChannel;
import java.text.NumberFormat;
import java.util.HashMap;
import org.xnap.commons.i18n.I18n;
import settings.configProg;

/**
 *
 * @author gil
 */
public class hgtReader {
    
    private configProg myConfig;
    private I18n i18n; 
    private String srtmDir;
    private static final int SECONDS_PER_MINUTE = 60;

    public static final String HGT_EXT = ".hgt";

    // alter these values for different SRTM resolutions
    public static final int HGT_RES = 3; // resolution in arc seconds
    public static final int HGT_ROW_LENGTH = 1201; // number of elevation values per line
    public static final int HGT_VOID = -32768; // magic number which indicates 'void data' in HGT file

    private final HashMap<String, ShortBuffer> cache = new HashMap<>();  
    
    private boolean readySrtm;
    
    public hgtReader(configProg pConfig) {   
        myConfig = pConfig;  
        this.i18n = myConfig.getI18n();
        // Checking the working directory
        File fSrtm = new File(myConfig.getPathW()+File.separator+"Srtm");
        if (!fSrtm.exists()) fSrtm.mkdirs();        
        if (fSrtm.exists()) {
            this.srtmDir = fSrtm.getAbsolutePath();           
            readySrtm = true;
        } else {
            readySrtm = false;
        }
    }      
    
    public boolean isReadySrtm() {
        return readySrtm;
    }    
    
    public double getElevationFromHgt(double lat, double lon) {    
        
        double res = 9999;
        boolean hgtOk;
        
        try {
            String file = getHgtFileName(lat,lon);
            // given area in cache?
            if (!cache.containsKey(file)) {
                System.out.println("Mise en cache "+file);
                // fill initial cache value. If no file is found, then
                // we use it as a marker to indicate 'file has been searched
                // but is not there'
                cache.put(file, null);              
                
                String fullPath = new File(srtmDir, file).getPath();
                File f = new File(fullPath);
                if (!f.exists()) {
                    hgtDownload hd = new hgtDownload(i18n, f, lat, lon);
                    hgtOk = hd.isSuccess();
                } else {
                    hgtOk = true;
                }
                if (hgtOk) {
                    // found something: read HGT file...
                    ShortBuffer data = readHgtFile(fullPath);
                    // ... and store result in cache
                    cache.put(file, data);
                    // read elevation value
                    res = readElevation(lat, lon);  
                }                     
            } else {                
                res = readElevation(lat, lon);               
            }          
            
            return res;          
        } catch (FileNotFoundException e) {
            System.err.println("Get elevation from HGT " + lat+","+lon + " failed: => " + e.getMessage());
            // no problem... file not there
            return res;
        } catch (Exception ioe) {
            // oops...
            ioe.printStackTrace(System.err);
            // fallback
            return res;
        }
        
    }    
    
    @SuppressWarnings("resource")
    private ShortBuffer readHgtFile(String file) throws Exception {

        FileChannel fc = null;
        ShortBuffer sb = null;
        try {
            // Eclipse complains here about resource leak on 'fc' - even with 'finally' clause???
            fc = new FileInputStream(file).getChannel();
            // choose the right endianness

            ByteBuffer bb = ByteBuffer.allocateDirect((int) fc.size());
            while (bb.remaining() > 0) fc.read(bb);

            bb.flip();
            //sb = bb.order(ByteOrder.LITTLE_ENDIAN).asShortBuffer();
            sb = bb.order(ByteOrder.BIG_ENDIAN).asShortBuffer();
        } finally {
            if (fc != null) fc.close();
        }

        return sb;
    }       
    
    private double readElevation(double lat, double lon) {
        
        double badRes = 9999;
        
        String tag = getHgtFileName(lat,lon);

        ShortBuffer sb = cache.get(tag);

        if (sb == null) {
            return 9999;
        }

        // see http://gis.stackexchange.com/questions/43743/how-to-extract-elevation-from-hgt-file
        double fLat = frac(lat) * SECONDS_PER_MINUTE;
        double fLon = frac(lon) * SECONDS_PER_MINUTE;

        // compute offset within HGT file
        int row = (int) Math.round(fLat * SECONDS_PER_MINUTE / HGT_RES);
        if(fLat < 0){
             row = row * -1;
         }else{
             row = HGT_ROW_LENGTH - row;
         }        
        
        int col = (int) Math.round(fLon * SECONDS_PER_MINUTE / HGT_RES);
        
        int cell = (HGT_ROW_LENGTH * (row - 1)) + col;

        //System.out.println("Read SRTM elevation data from row/col/cell " + row + "," + col + ", " + cell + ", " + sb.limit());
        // valid position in buffer?
        if (cell < sb.limit() && cell >= 0) {
            short ele = sb.get(cell);
            //System.out.println("==> Read SRTM elevation data from row/col/cell " + row + "," + col + ", " + cell + " = " + ele);
            // check for data voids
            if (ele == HGT_VOID) {
                return badRes;
            } else {
                return ele;
            }
        } else {
            return badRes;
        }
    }        

    public static double frac(double d) {
        long iPart;
        double fPart;

        // Get user input
        iPart = (long) d;
        fPart = d - iPart;
        return fPart;
    }    
    
    public String getHgtFileName(double lat, double lon) {
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

            return NS + f_nlat + WE + f_nlon + HGT_EXT;
    }    
    
    
}
