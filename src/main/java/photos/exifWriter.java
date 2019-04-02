/*
 * Copyright Gil THOMAS
 * This file forms an integral part of Logfly project
 * See the LICENSE file distributed with source code
 * for details of Logfly licence project
 */
package photos;

import geoutils.position;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import org.apache.commons.imaging.ImageReadException;
import org.apache.commons.imaging.ImageWriteException;
import org.apache.commons.imaging.Imaging;
import org.apache.commons.imaging.common.ImageMetadata;
import org.apache.commons.imaging.formats.jpeg.JpegImageMetadata;
import org.apache.commons.imaging.formats.jpeg.exif.ExifRewriter;
import org.apache.commons.imaging.formats.tiff.TiffImageMetadata;
import org.apache.commons.imaging.formats.tiff.constants.ExifTagConstants;
import org.apache.commons.imaging.formats.tiff.constants.GpsTagConstants;
import org.apache.commons.imaging.formats.tiff.write.TiffOutputDirectory;
import org.apache.commons.imaging.formats.tiff.write.TiffOutputSet;

/**
 *
 * @author gil
 */
public class exifWriter {
    
    // https://www.javatips.net/api/gopro2mapillary-master/src/main/java/it/inserpio/mapillary/gopro/importer/exif/EXIFPropertyWriter.java
    // https://commons.apache.org/proper/commons-imaging/xref-test/org/apache/commons/imaging/examples/WriteExifMetadataExample.html
    public static int setExifGPSTag(File jpegImageFile, File jpegImageOutputFile, position coordinates) {
        
        int res = -1;
        OutputStream os = null;
        boolean canThrow = false;
        
        try {
            TiffOutputSet outputSet = null;
            final ImageMetadata metadata = Imaging.getMetadata(jpegImageFile);
            final JpegImageMetadata jpegMetadata = (JpegImageMetadata) metadata;
            if (jpegMetadata != null) {
                final TiffImageMetadata exif = jpegMetadata.getExif();
                if (exif != null)
                {
                  outputSet = exif.getOutputSet();
                }
                if (outputSet == null) {
                    outputSet = new TiffOutputSet();
                }                
                outputSet.setGPSInDegrees(coordinates.getLongitude(), coordinates.getLatitude());     
                os = new FileOutputStream(jpegImageOutputFile);
                os = new BufferedOutputStream(os);

                new ExifRewriter().updateExifMetadataLossless(jpegImageFile, os, outputSet);   
                
                res = 0;
            }
        } catch (Exception e) {
            res = 1;
        }  
        
        return res;
    }       
    
    public static int removeExifTag(final File jpegImageFile, final File dst) throws IOException, ImageReadException, ImageWriteException {
        
        int res = -1;
        
        try (FileOutputStream fos = new FileOutputStream(dst);
             OutputStream os = new BufferedOutputStream(fos)) 
        {
            TiffOutputSet outputSet = null;
            final ImageMetadata metadata = Imaging.getMetadata(jpegImageFile);
            final JpegImageMetadata jpegMetadata = (JpegImageMetadata) metadata;            
            if (null != jpegMetadata) {
                final TiffImageMetadata exif = jpegMetadata.getExif();
                if (null != exif) {
                    outputSet = exif.getOutputSet();
                }
            }
            if (null == outputSet) {
                // file does not contain any exif metadata
                return 1;
            }
            {
                TiffOutputDirectory gpsDirectory = outputSet.getOrCreateGPSDirectory();
                //gpsDirectory.removeField(GpsTagConstants.GPS_TAG_GPS_VERSION_ID);
                gpsDirectory.removeField(GpsTagConstants.GPS_TAG_GPS_TIME_STAMP);
                gpsDirectory.removeField(GpsTagConstants.GPS_TAG_GPS_DATE_STAMP);
                gpsDirectory.removeField(GpsTagConstants.GPS_TAG_GPS_SPEED_REF);
                gpsDirectory.removeField(GpsTagConstants.GPS_TAG_GPS_SPEED);
                gpsDirectory.removeField(GpsTagConstants.GPS_TAG_GPS_ALTITUDE_REF);
                gpsDirectory.removeField(GpsTagConstants.GPS_TAG_GPS_ALTITUDE);
                gpsDirectory.removeField(GpsTagConstants.GPS_TAG_GPS_IMG_DIRECTION_REF);
                gpsDirectory.removeField(GpsTagConstants.GPS_TAG_GPS_IMG_DIRECTION);
                gpsDirectory.removeField(GpsTagConstants.GPS_TAG_GPS_LATITUDE_REF);
                gpsDirectory.removeField(GpsTagConstants.GPS_TAG_GPS_LATITUDE);
                gpsDirectory.removeField(GpsTagConstants.GPS_TAG_GPS_LONGITUDE_REF);
                gpsDirectory.removeField(GpsTagConstants.GPS_TAG_GPS_LONGITUDE);   
                new ExifRewriter().updateExifMetadataLossless(jpegImageFile, os,outputSet);
                res = 0;
            }
        } catch (Exception e) {
            res = 1;
        }
        
        return res;
    
    }
    
}
