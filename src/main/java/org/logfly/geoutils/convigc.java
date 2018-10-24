/* 
 * Copyright Gil THOMAS
 * This file forms an integral part of Logfly project
 * See the LICENSE file distributed with source code
 * for details of Logfly licence project
 */
package org.logfly.geoutils;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;

/**
 *
 * @author Gil Thomas logfly.org
 */
public class convigc {
    
    public static String Lat_Dd_IGC(double dLat)  {
              
        String res = null;
        StringBuilder sbRes = new StringBuilder();
        double AbsLat;
        int fDeg;
        double fMin;
        String sMin;
        DecimalFormatSymbols decimalFormatSymbols = new DecimalFormatSymbols();
        // Imperatif -> forcer le point comme séparateur
        decimalFormatSymbols.setDecimalSeparator('.');        
        DecimalFormat decimalFormat = new DecimalFormat("00.000", decimalFormatSymbols);
  
        try {
            AbsLat = Math.abs(dLat);
            // En faisant un cast integer on ne garde que la partie entière
            fDeg = (int) AbsLat;
            fMin = (AbsLat - fDeg)*60;
            sMin = decimalFormat.format(fMin);
            String[] tbMin = sMin.split("\\.");   // regex bien sûr !
            sbRes.append(String.format("%02d", fDeg)).append(tbMin[0]).append(tbMin[1]);
            if (dLat < 0)
                sbRes.append("S");
            else
                sbRes.append("N");
            res = sbRes.toString();
        } catch (Exception e) {
            
        }               
        return res;
    }
    
    public static String Long_Dd_IGC(double dLong)  {
        String res = null;
        StringBuilder sbRes = new StringBuilder();
        double AbsLong;
        int fDeg;
        double fMin;
        String sMin;
        DecimalFormatSymbols decimalFormatSymbols = new DecimalFormatSymbols();
        // Imperatif -> forcer le point comme séparateur
        decimalFormatSymbols.setDecimalSeparator('.');        
        DecimalFormat decimalFormat = new DecimalFormat("00.000", decimalFormatSymbols);
        try {
            AbsLong = Math.abs(dLong);
            // En faisant un cast integer on ne garde que la partie entière
            fDeg = (int) AbsLong;
            fMin = (AbsLong - fDeg)*60;
             sMin = decimalFormat.format(fMin);
            String[] tbMin = sMin.split("\\.");   // regex bien sûr !
            sbRes.append(String.format("%03d", fDeg)).append(tbMin[0]).append(tbMin[1]);
            if (dLong < 0) 
                sbRes.append("W");
            else
                sbRes.append("E");
            res = sbRes.toString();            
        } catch (Exception e) {
            
        }                

        return res;
    }
}
