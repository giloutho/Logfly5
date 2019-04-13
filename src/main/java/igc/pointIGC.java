/* 
 * Copyright Gil THOMAS
 * This file forms an integral part of Logfly project
 * See the LICENSE file distributed with source code
 * for details of Logfly licence project
 */
package igc;

import java.time.LocalDateTime;

/**
 *
 * @author gil
 * Properties of an IGC point and utilities
 */

public class pointIGC {
    public int AltiBaro;
    public int AltiGPS;
    public double Cap;
    public String Comment;
    public double Cos_Lat_Rad;
    public LocalDateTime dHeure;
    public double DistanceCum;
    public double DistPtPcdt;
    public double GlideRatio;
    public double Latitude;
    public int Lat_Sec;
    public double Longitude;
    public double Long_Rad;
    public int Long_Sec;
    public int Main_Idx;
    public int Periode;
    public int PeriodePtPcdt;
    public double Sin_Lat_Rad;
    public double TAS;
    public double Vario;
    public double Vitesse;
    public double VitesseMoy;
    public boolean violation;
    
    public pointIGC() {
        AltiBaro = 0;
        AltiGPS = 0;
        Cap = 0;
        Comment = "";
        Cos_Lat_Rad = 0;
        DistanceCum = 0;
        DistPtPcdt = 0;
        GlideRatio = 0;
        Latitude = 0;
        Lat_Sec = 0;
        Longitude = 0;
        Long_Rad = 0;
        Long_Sec = 0;
        Main_Idx = 0;
        Periode = 0;
        PeriodePtPcdt = 0;
        Sin_Lat_Rad = 0;
        TAS = 0;
        Vario = 0;
        Vitesse = 0;
        VitesseMoy = 0;    
        violation = false;       
    }
    
    public void setAltiBaro(int pAltiBaro)
    {
        AltiBaro = pAltiBaro;
    }
    
    public void setAltiGPS(int pAltiGPS)
    {
        AltiGPS = pAltiGPS;
    }
    
    public void setComment(String pComment)
    {
        Comment = pComment;
    }
    
    public void setTAS(int pTAS)
    {
        TAS = pTAS;
    }
    
    public void setPeriode(int pPeriode)
    {
        Periode = pPeriode;
    }

    public double getLatitude() {
        return Latitude;
    }

    public void setLatitude(double Latitude) {
        this.Latitude = Latitude;
    }

    public double getLongitude() {
        return Longitude;
    }

    public void setLongitude(double Longitude) {
        this.Longitude = Longitude;
    }
    
    
    
    public void setLatitudeDMS(int pDeg, double pMin, double pSec, String pHem)
    {
        Latitude = pDeg+(((pMin*60)+(pSec))/3600);
        // if south hemisphere -> negative
        if (pHem.equals("S")) Latitude = Latitude * - 1;
    }
    
     public void setLatitudeSec(int pDeg, int pMin, String pHem)
     {
        Lat_Sec = 60000*pDeg+pMin;
        // if south hemisphere -> negative
        if (pHem.equals("S")) Lat_Sec = Lat_Sec * - 1;;
        // At the same time, sinus and cosine are computed
        double _Lat;
        _Lat = Math.PI * Lat_Sec /(180 * 60000);
        Sin_Lat_Rad = Math.sin(_Lat);
        Cos_Lat_Rad = Math.cos(_Lat);
     }
     
      public void setLongitudeDMS(int pDeg, double pMin, double pSec, String pMer)
      {
        Longitude = pDeg+(((pMin*60)+(pSec))/3600);
        // if West -> negative
        if (pMer.equals("W")) Longitude = Longitude * - 1;
      }
      
      public void setLongitudeSec(int pDeg, int pMin, String pMer)
      {
            Long_Sec = 60000*pDeg+pMin;
            // if West -> negative
            if (pMer.equals("W")) Long_Sec = Long_Sec * - 1;
            Long_Rad = Math.PI * Long_Sec /(180 * 60000);
      }
      
      public void setdHeure(LocalDateTime pDateVol, int pHour, int pMin, int pSec)
      {
        pDateVol = pDateVol.plusHours(pHour);
        pDateVol = pDateVol.plusMinutes(pMin);
        pDateVol = pDateVol.plusSeconds(pSec);
        dHeure = pDateVol;
      }
      
      public void setLdtHeure(LocalDateTime pDateVol)
      {        
        dHeure = pDateVol;
      }
      
      public void setDistPtPcdt(double pDistance)
      {
          DistPtPcdt = pDistance;
      }
      
      public void setPeriodePtPcdt(int pPeriodePtPcdt)
      {
          PeriodePtPcdt = pPeriodePtPcdt;
      }
      
      public void setVitesse(double pVitesse)
      {
          Vitesse = pVitesse;
      }
      
      public void setVario(double pVario)
      {
          Vario = pVario;
      }
              
    
}
