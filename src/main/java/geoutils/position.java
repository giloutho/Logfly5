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

    public int getAltitude() {
        return altitude;
    }

    public void setAltitude(int altitude) {
        this.altitude = altitude;
    }

    public String getHemisphere() {
        return hemisphere;
    }

    public void setHemisphere(String hemiphere) {
        this.hemisphere = hemiphere;
    }

    public int getLatDegres() {
        return latDegres;
    }

    public void setLatDegres(int latDegres) {
        this.latDegres = latDegres;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLatMin_mm() {
        return latMin_mm;
    }

    public void setLatMin_mm(double latMin_mm) {
        this.latMin_mm = latMin_mm;
    }

    public int getLatMin_ms() {
        return latMin_ms;
    }

    public void setLatMin_ms(int latMin_ms) {
        this.latMin_ms = latMin_ms;
    }

    public double getLatSec_ms() {
        return latSec_ms;
    }

    public void setLatSec_ms(double latSec_ms) {
        this.latSec_ms = latSec_ms;
    }

    public int getLongDegres() {
        return longDegres;
    }

    public void setLongDegres(int longDegres) {
        this.longDegres = longDegres;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getLongMin_mm() {
        return longMin_mm;
    }

    public void setLongMin_mm(double longMin_mm) {
        this.longMin_mm = longMin_mm;
    }

    public int getLongMin_ms() {
        return longMin_ms;
    }

    public void setLongMin_ms(int longMin_ms) {
        this.longMin_ms = longMin_ms;
    }

    public double getLongSec_ms() {
        return longSec_ms;
    }

    public void setLongSec_ms(double longSec_ms) {
        this.longSec_ms = longSec_ms;
    }

    public String getMeridien() {
        return meridien;
    }

    public void setMeridien(String meridien) {
        this.meridien = meridien;
    }
    
    
    
    public void setLatitudeDd(double dDecimalDegrees)  {
        this.setLatitude(dDecimalDegrees);
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
  
        this.setLatDegres((int)Math.floor(dDecimalDegrees));
        dMinutesPart = (dDecimalDegrees - latDegres) * 60;
        this.setLatMin_mm(dMinutesPart);
        this.setLatSec_ms((dMinutesPart - Math.floor(dMinutesPart)) * 60);
        this.setLatMin_ms((int)Math.floor(dMinutesPart));
  
        if (iSign == 1)
            this.setHemisphere("N");
        else
            this.setHemisphere("S");        
    }
    
    public void setLatitudeDMm(int pDeg, double pMin, String pHem) {
    
        double calcLatitude;
        calcLatitude = pDeg+((pMin*60)/3600);
        // si hémisphère Sud on passe en négatif
        if (pHem.equals("S")) calcLatitude = calcLatitude * - 1;
        this.setLatitude(calcLatitude);        
        this.setLatDegres((int)pDeg);
        this.setLatMin_ms((int)Math.floor(pMin));
        this.setLatMin_mm(pMin);
        this.setLatSec_ms ((pMin- this.getLatMin_ms())*60);
        this.setHemisphere(pHem);        
        System.out.println(pDeg+"-"+pMin+"-"+pHem);
        System.out.println(this.getLatitude());
        System.out.println(this.getLatDegres());
        System.out.println(this.getLatMin_ms());
        System.out.println(this.getLatMin_mm());
        System.out.println(this.getLatSec_ms());
        System.out.println(this.getHemisphere());        
    }
    
    public void setLatitudeDMS(int pDeg, int pMin, double pSec, String pHem) {
        double calcLatitude = pDeg+(((pMin*60)+(pSec))/3600);
        // Si hémisphère Sud on passe en négatif
        if (pHem.equals("S")) calcLatitude = calcLatitude * - 1;
        this.setLatitude(calcLatitude);  
        this.setLatDegres((int)pDeg);
        this.setLatMin_ms((int)pMin);
        this.setLatMin_mm(pMin+(pSec/60));
        this.setLatSec_ms(pSec);
        this.setHemisphere(pHem);         
    }
    
    public void setLongitudedeDd(double dDecimalDegrees)  {
        this.setLongitude(dDecimalDegrees);
        // from http://forums.esri.com/Thread.asp?c=93&f=993&t=116218
        double dMinutesPart;
        int iSign;
  
        if (dDecimalDegrees < 0) {
            dDecimalDegrees = -dDecimalDegrees;
            iSign = -1;
        } else {
            iSign = 1;
        }
  
        this.setLongDegres((int)Math.floor(dDecimalDegrees));
        dMinutesPart = (dDecimalDegrees - longDegres) * 60;
        this.setLongMin_mm(dMinutesPart);
        this.setLongSec_ms((dMinutesPart - Math.floor(dMinutesPart)) * 60);
        this.setLongMin_ms((int)Math.floor(dMinutesPart));
  
        if (iSign == 1)
            this.setMeridien("E");
        else
            this.setMeridien("W");                
    }
    
    public void setLongitudeDMm(int pDeg, double pMin, String pMer)  {
        double calcLongitude;
        calcLongitude = pDeg+((pMin*60)/3600);
        // si on est en Ouest on passe en négatif
        if (pMer.equals("W")) calcLongitude = calcLongitude * - 1;
        this.setLongitude(calcLongitude);        
        this.setLongDegres((int)pDeg);
        this.setLongMin_ms((int)Math.floor(pMin));
        this.setLongMin_mm(pMin);
        this.setLongSec_ms ((pMin- this.getLongMin_ms())*60);
        this.setMeridien(pMer);   
    }
    
    public void setLongitudeDMS(int pDeg, double pMin, double pSec, String pMer)  {
        double calcLongitude = pDeg+(((pMin*60)+(pSec))/3600);
        // est en Ouest on passe en négatif
        if (pMer.equals("W")) calcLongitude = calcLongitude * - 1;
        this.setLongitude(calcLongitude);  
        this.setLongDegres((int)pDeg);
        this.setLongMin_ms((int)pMin);
        this.setLongMin_mm(pMin+(pSec/60));
        this.setLongSec_ms(pSec);
        this.setMeridien(pMer);          
    }
    
}
