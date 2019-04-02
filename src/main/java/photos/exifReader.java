/*
 * Copyright Gil THOMAS
 * This file forms an integral part of Logfly project
 * See the LICENSE file distributed with source code
 * for details of Logfly licence project
 */
package photos;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.logging.Level;
import org.apache.commons.imaging.Imaging;
import org.apache.commons.imaging.common.ImageMetadata;
import org.apache.commons.imaging.common.RationalNumber;
import org.apache.commons.imaging.formats.jpeg.JpegImageMetadata;
import org.apache.commons.imaging.formats.tiff.TiffField;
import org.apache.commons.imaging.formats.tiff.constants.ExifTagConstants;
import org.apache.commons.imaging.formats.tiff.constants.GpsTagConstants;
import org.apache.commons.imaging.formats.tiff.constants.TiffTagConstants;
import org.xnap.commons.i18n.I18n;
import systemio.mylogging;

/**
 *
 * @author gil
 */
public class exifReader {
    
    private I18n i18n;   
    private StringBuilder sbError;
    private String tagDateTimeOriginal;
    private LocalDateTime ldtOriginal;
    private boolean infoGPS = false;
    private boolean infoExif = false;
    private double tagLatitude = 0;
    private double tagLongitude = 0;
    private int tagAltitude = 0;
    private StringBuilder sbInfo;
    private final String RC = "\r\n";  
    private DateTimeFormatter dtfExif = DateTimeFormatter.ofPattern("yyyy:MM:dd HH:mm:ss");    

    public exifReader(I18n pI18n) {        
        this.i18n = pI18n;
    } 

    public boolean isInfoGPS() {
        return infoGPS;
    }

    public LocalDateTime getLdtOriginal() {
        return ldtOriginal;
    }

    public boolean isInfoExif() {
        return infoExif;
    }
                    
    public String getTagDateTimeOriginal() {
        return tagDateTimeOriginal;
    }        

    public double getTagLatitude() {
        return tagLatitude;
    }       

    public double getTagLongitude() {
        return tagLongitude;
    }

    public int getTagAltitude() {
        return tagAltitude;
    }

    public String getPhotoInfos() {
        return sbInfo.toString();
    }
    
    
        
    /**
     * Idea to show several informations about the photo was dropped     
     */
    public void decodeExifInfos(File fImg) {
        TiffField field;
        JpegImageMetadata jpegMetadata;
        
        sbInfo = new StringBuilder();
                
        try {           
            ImageMetadata myMetadata = Imaging.getMetadata(fImg);
            jpegMetadata = (JpegImageMetadata) myMetadata;  
            
            sbInfo.append(i18n.tr("File name")).append(" : ").append(fImg.getName()).append(RC);  
            sbInfo.append(i18n.tr("Path")).append(" : ").append(fImg.getParent()).append(RC);  
            double fSize = (double) fImg.length() / (1024 * 1024);
            sbInfo.append(i18n.tr("Size file")).append(" : ").append(String.format("%3.2f", fSize)).append(" Mb").append(RC);            
            
            field = jpegMetadata.findEXIFValueWithExactMatch(TiffTagConstants.TIFF_TAG_MAKE);
            if (field != null) {
                sbInfo.append(i18n.tr("Camera make")).append(" : ").append(field.getValueDescription()).append(RC);
            }

            field = jpegMetadata.findEXIFValueWithExactMatch(TiffTagConstants.TIFF_TAG_MODEL);
            if (field != null) {
                sbInfo.append(i18n.tr("Camera model")).append(" : ").append(field.getValueDescription()).append(RC);
            }

            field = jpegMetadata.findEXIFValueWithExactMatch(ExifTagConstants.EXIF_TAG_DATE_TIME_ORIGINAL);                
            if (field != null) {            
                tagDateTimeOriginal = field.getValueDescription();
                sbInfo.append(i18n.tr("Date/Time Original")).append(" : ").append(field.getValueDescription()).append(RC);
            }                
            field = jpegMetadata.findEXIFValueWithExactMatch( TiffTagConstants.TIFF_TAG_DATE_TIME);
            if (field != null) {
                sbInfo.append(i18n.tr("Modify date")).append(" : ").append(field.getValueDescription()).append(RC);
            }        
            field = jpegMetadata.findEXIFValueWithExactMatch(TiffTagConstants.TIFF_TAG_XRESOLUTION);
            if (field != null) {
                sbInfo.append("Resolution").append(" : ").append(field.getValueDescription());
            }
            field = jpegMetadata.findEXIFValueWithExactMatch(TiffTagConstants.TIFF_TAG_YRESOLUTION);
            if (field != null) {
                sbInfo.append(" x ").append(field.getValueDescription()).append(RC);
            }            

            field = jpegMetadata.findEXIFValueWithExactMatch(ExifTagConstants.EXIF_TAG_ISO);
            if (field != null) {
                sbInfo.append("ISO").append(" : ").append(field.getValueDescription()).append(RC);
            }
            field = jpegMetadata.findEXIFValueWithExactMatch(ExifTagConstants.EXIF_TAG_FNUMBER);
            if (field != null) {
                sbInfo.append(i18n.tr("Aperture")).append(" : ").append(field.getValueDescription()).append(RC);
            }
            field = jpegMetadata.findEXIFValueWithExactMatch(ExifTagConstants.EXIF_TAG_EXPOSURE_TIME );
            if (field != null) {
                sbInfo.append(i18n.tr("Exposure time")).append(" : ").append(field.getValueDescription()).append(RC);
               
            }
            sbInfo.append("==================================================").append(RC);
            field = jpegMetadata.findEXIFValueWithExactMatch(GpsTagConstants.GPS_TAG_GPS_LATITUDE);
            if (field != null) {            
                metadataGPS(jpegMetadata,true);
            } else {
                infoGPS =false;
                sbInfo.append(i18n.tr("No GPS information")).append(RC);
                sbInfo.append("==================================================").append(RC);
            }
            infoExif = true;
        } catch (Exception e) {
            infoExif = false;
            sbError = new StringBuilder(this.getClass().getName()+"."+Thread.currentThread().getStackTrace()[1].getMethodName());
            sbError.append("\r\n").append(e.toString());
            mylogging.log(Level.SEVERE, sbError.toString());              
            throw new NullPointerException(); 
        }
    }  
    
    public void decodeGPS(File fImg) {
        
        JpegImageMetadata jpegMetadata;
        try {
            ImageMetadata myMetadata = Imaging.getMetadata(fImg);
            jpegMetadata = (JpegImageMetadata) myMetadata;   
            metadataGPS(jpegMetadata,false);
        } catch (Exception e) {
            infoGPS = false;
            sbError = new StringBuilder(this.getClass().getName()+"."+Thread.currentThread().getStackTrace()[1].getMethodName());
            sbError.append("\r\n").append(e.toString());
            mylogging.log(Level.SEVERE, sbError.toString());   
            throw new NullPointerException();                       
        }
    }
    
    public void metadataGPS(JpegImageMetadata jpegMetadata, boolean asInfo) {
        TiffField field;
        
        try {                        
            field = jpegMetadata.findEXIFValueWithExactMatch(ExifTagConstants.EXIF_TAG_DATE_TIME_ORIGINAL);                
            if (field != null) {            
                String sRawDate =  field.getValueDescription().replaceAll("'", "");       
                if (sRawDate.length() > 19) 
                    tagDateTimeOriginal = sRawDate.substring(0, 19);
                else
                    tagDateTimeOriginal = sRawDate;
                ldtOriginal = LocalDateTime.parse(tagDateTimeOriginal, dtfExif);
                infoExif = true;
                
                field = jpegMetadata.findEXIFValueWithExactMatch(GpsTagConstants.GPS_TAG_GPS_LATITUDE);
                if (field != null) {                    
                    decodeLatitude(jpegMetadata,field);
                    if (infoGPS && asInfo) {
                        sbInfo.append(i18n.tr("Latitude")).append(" : ").append(String.format("%2.4f", tagLatitude)) .append(RC);
                        sbInfo.append(i18n.tr("Longitude")).append(" : ").append(String.format("%3.4f", tagLongitude)) .append(RC);
                        if (tagAltitude != 9999)
                            sbInfo.append(i18n.tr("Altitude")).append(" : ").append(String.valueOf(tagAltitude)).append(" m").append(RC);
                        sbInfo.append("==================================================").append(RC);        
                    }
                } else {
                    infoGPS =false;
                }                
            } else {
                // DateTimeOriginal is essential
                infoExif = false;
                infoGPS = false;
            }                            
        } catch (Exception e) {
            infoGPS = false;
            throw new NullPointerException(); 
        }            
    }
            
    
    private void decodeLatitude(JpegImageMetadata jpegMetadata, TiffField latitudeField) {
        try {            
            RationalNumber latitude[] = (RationalNumber[]) latitudeField.getValue();  
            if (latitude.length == 3) {
                RationalNumber latitudeDegrees = latitude[0];
                RationalNumber latitudeMinutes = latitude[1];
                RationalNumber latitudeSeconds = latitude[2];     
                int deg = latitudeDegrees.intValue();
                double min = latitudeMinutes.doubleValue();
                double sec = latitudeSeconds.doubleValue();
                tagLatitude = deg+(((min*60)+(sec))/3600); 
                TiffField latitudeRefField =  jpegMetadata.findEXIFValueWithExactMatch(GpsTagConstants.GPS_TAG_GPS_LATITUDE_REF);
                String latitudeRef = latitudeRefField.getStringValue();
                if (latitudeRef.equals("S")) tagLatitude = tagLatitude * - 1;
                decodeLongitude(jpegMetadata);
            } 
        } catch (Exception e) {
            infoGPS = false;
            sbError = new StringBuilder(this.getClass().getName()+"."+Thread.currentThread().getStackTrace()[1].getMethodName());
            sbError.append("\r\n").append(e.toString());
            mylogging.log(Level.SEVERE, sbError.toString());              
        }        
    }
    
    private void decodeLongitude(JpegImageMetadata jpegMetadata) {
        
        try {
            TiffField longitudeField = jpegMetadata.findEXIFValueWithExactMatch(GpsTagConstants.GPS_TAG_GPS_LONGITUDE);
            RationalNumber longitude[] = (RationalNumber[]) longitudeField.getValue();
            if (longitude.length == 3) {
                RationalNumber longitudeDegrees = longitude[0];
                RationalNumber longitudeMinutes = longitude[1];
                RationalNumber longitudeSeconds = longitude[2];
                int deg = longitudeDegrees.intValue();
                double min = longitudeMinutes.doubleValue();
                double sec = longitudeSeconds.doubleValue();
                tagLongitude = deg+(((min*60)+(sec))/3600);
                TiffField longitudeRefField =  jpegMetadata.findEXIFValueWithExactMatch(GpsTagConstants.GPS_TAG_GPS_LONGITUDE_REF);
                String longitudeRef = longitudeRefField.getStringValue();
                if (longitudeRef.equals("W")) tagLongitude = tagLongitude * - 1;    
                infoGPS = true;
                decodeAltitude(jpegMetadata);                
            }
            
        } catch (Exception e) {
            infoGPS = false;
        }
        
    }

        
    private void decodeAltitude(JpegImageMetadata jpegMetadata) {
        
        try {
            TiffField altiField = jpegMetadata.findEXIFValueWithExactMatch(GpsTagConstants.GPS_TAG_GPS_ALTITUDE);
            if (altiField != null) {
                tagAltitude = altiField.getIntValue();
            } else {
                tagAltitude = 9999;
            }       
        } catch (Exception e) {
            tagAltitude = 9999;
        }
    }    
        
}
