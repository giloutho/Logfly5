/*
 * Copyright Gil THOMAS
 * This file forms an integral part of Logfly project
 * See the LICENSE file distributed with source code
 * for details of Logfly licence project
 */
package geoutils;

/**
 *
 * @author gil
 * 
 * Repésente une position sur la terre exprimée en latitude, longitude
 * avec éventuellement une altitude sur un datum donné (pour l'instant limité à WGS84).
 */
public class position {
    
    private int altitude;
    private String hemisphere;
    private int latDegres;
    private double latitude;
    private double latMin_mm;
    private int latMin_ms;
    private double latSec_ms;
    private int longDegres;
    private double longitude;
    private double longMin_mm;
    private int longMin_ms;
    private double longSec_ms;    
    private String meridien;

    public position() {
        altitude = 0;
//        hemisphere = "N";
//        latDegres = 0;
//        latitude =0;
//        latMin_mm = 0;
//        latMin_ms = 0;
//        latSec_ms = 0;
//        longDegres = 0;
//        longitude = 0;
//        longMin_mm = 0;
//        longMin_ms = 0;
//        longSec_ms = 0;    
//        meridien = "E";        
    }
    
    public int getAltitude() {
        return altitude;
    }

    public void setAltitude(int altitude) {
        this.altitude = altitude;
    }

    public String getHemisphere() {
        return hemisphere;
    }

    public int getLatDegres() {
        return latDegres;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLatMin_mm() {
        return latMin_mm;
    }

    public int getLatMin_ms() {
        return latMin_ms;
    }

    public double getLatSec_ms() {
        return latSec_ms;
    }

    public int getLongDegres() {
        return longDegres;
    }

    public double getLongitude() {
        return longitude;
    }

    public double getLongMin_mm() {
        return longMin_mm;
    }

    public int getLongMin_ms() {
        return longMin_ms;
    }

    public double getLongSec_ms() {
        return longSec_ms;
    }

    public String getMeridien() {
        return meridien;
    }
    
    public void setLatDegres(int latDegres) {
        this.latDegres = latDegres;
        setLatitudeDMm(latDegres, getLatMin_mm(), getHemisphere());
       // setLatitudeDMS(latDegres, getLatMin_ms(), getLatSec_ms(), getHemisphere());
    }
        
    public void setLatMin_mm(double latMin_mm) {
        this.latMin_mm = latMin_mm;
        setLatitudeDMm(getLatDegres(), latMin_mm, getHemisphere());
    }    
    
    public void setHemisphere(String hemisphere) {
        this.hemisphere = hemisphere;
        setLatitudeDMm(getLatDegres(), getLatMin_mm(), hemisphere);
    }
    public void setLatMin_ms(int latMin_ms) {
        this.latMin_ms = latMin_ms;
        setLatitudeDMS(getLatDegres(), latMin_ms, getLatSec_ms(), getHemisphere());
    }   
    
    public void setLatSec_ms(double latSec_ms) {
        this.latSec_ms = latSec_ms;
        setLatitudeDMS(getLatDegres(), getLatMin_ms(), latSec_ms, getHemisphere());
    }    

    public void setLongDegres(int longDegres) {
        this.longDegres = longDegres;
        setLongitudeDMm(longDegres, getLongMin_mm(), getMeridien());
     //   setLongitudeDMS(longDegres, getLongMin_mm(), getLongSec_ms(), getMeridien());
    }   
    
    public void setLongMin_mm(double longMin_mm) {
        this.longMin_mm = longMin_mm;
        setLongitudeDMm(getLongDegres(), longMin_mm, getMeridien());
    }
    
    public void setLongMin_ms(int longMin_ms) {
        this.longMin_ms = longMin_ms;
        setLongitudeDMS(getLongDegres(), longMin_ms, getLongSec_ms(), getMeridien()); 
    }    
    
    public void setLongSec_ms(double longSec_ms) {
        this.longSec_ms = longSec_ms;
        setLongitudeDMS(getLongDegres(), getLongMin_ms(), longSec_ms, getMeridien()); 
    }
    
    public void setMeridien(String meridien) {
        this.meridien = meridien;
        setLongitudeDMm(getLongDegres(), getLongMin_mm(), meridien);
    }    
    
    public void setLatitudeDd(double dDecimalDegrees)  {
        latitude = dDecimalDegrees;
        // from http://forums.esri.com/Thread.asp?c=93&f=993&t=116218
        // converts decimal degrees longitude to DDDMMSSH and decimal degrees latitude to DDMMSSH
  
        double dMinutesPart;
        int iSign;
  
        if (dDecimalDegrees < 0) {
            dDecimalDegrees = -dDecimalDegrees;
            iSign = -1;
        } else {
            iSign = 1;
        }
  
        latDegres = ((int)Math.floor(dDecimalDegrees));
        dMinutesPart = (dDecimalDegrees - latDegres) * 60;
        latMin_mm = dMinutesPart;
        latSec_ms = (dMinutesPart - Math.floor(dMinutesPart)) * 60;
        latMin_ms = (int)Math.floor(dMinutesPart);
  
        if (iSign == 1)
            hemisphere = "N";
        else
            hemisphere = "S";        
    }
    
    public void setLatitudeDMm(int pDeg, double pMin, String pHem) {
    
        double calcLatitude;
        calcLatitude = pDeg+((pMin*60)/3600);
        // si hémisphère Sud on passe en négatif
        if (pHem.equals("S")) calcLatitude = calcLatitude * - 1;
        latitude = calcLatitude;        
        latDegres = ((int)pDeg);
        latMin_ms = (int)Math.floor(pMin);
        latMin_mm = pMin;
        latSec_ms = (pMin- this.getLatMin_ms())*60;
        hemisphere = pHem;               
    }
    
    public void setLatitudeDMS(int pDeg, int pMin, double pSec, String pHem) {
        double calcLatitude = pDeg+(((pMin*60)+(pSec))/3600);
        // Si hémisphère Sud on passe en négatif
        if (pHem.equals("S")) calcLatitude = calcLatitude * - 1;
        latitude = calcLatitude;  
        latDegres = ((int)pDeg);
        latMin_ms = (int)pMin;
        latMin_mm = pMin+(pSec/60);
        latSec_ms = pSec;
        hemisphere = pHem;         
    }
    
    public void setLongitudeDd(double dDecimalDegrees)  {
        longitude = dDecimalDegrees;
        // from http://forums.esri.com/Thread.asp?c=93&f=993&t=116218
        double dMinutesPart;
        int iSign;
  
        if (dDecimalDegrees < 0) {
            dDecimalDegrees = -dDecimalDegrees;
            iSign = -1;
        } else {
            iSign = 1;
        }
  
        longDegres = ((int)Math.floor(dDecimalDegrees));
        dMinutesPart = (dDecimalDegrees - longDegres) * 60;
        longMin_mm = dMinutesPart;
        longSec_ms = (dMinutesPart - Math.floor(dMinutesPart)) * 60;
        longMin_ms = (int)Math.floor(dMinutesPart);
  
        if (iSign == 1)
            meridien = "E";
        else
            meridien = "W";                
    }
    
    public void setLongitudeDMm(int pDeg, double pMin, String pMer)  {
        double calcLongitude;
        calcLongitude = pDeg+((pMin*60)/3600);
        // si on est en Ouest on passe en négatif
        if (pMer.equals("W")) calcLongitude = calcLongitude * - 1;
        longitude = calcLongitude;        
        longDegres = ((int)pDeg);
        longMin_ms = (int)Math.floor(pMin);
        longMin_mm = pMin;
        longSec_ms = (pMin- this.getLongMin_ms())*60;
        meridien = pMer;   
    }
    
    public void setLongitudeDMS(int pDeg, double pMin, double pSec, String pMer)  {
        double calcLongitude = pDeg+(((pMin*60)+(pSec))/3600);
        // est en Ouest on passe en négatif
        if (pMer.equals("W")) calcLongitude = calcLongitude * - 1;
        longitude = calcLongitude;  
        longDegres = ((int)pDeg);
        longMin_ms = (int)pMin;
        longMin_mm = pMin+(pSec/60);
        longSec_ms = pSec;
        meridien = pMer;          
    }
    
}
