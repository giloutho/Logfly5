/*
 * Copyright Gil THOMAS
 * This file forms an integral part of Logfly project
 * See the LICENSE file distributed with source code
 * for details of Logfly licence project
 */
package org.logfly.igc;

/**
 *
 * @author gil
 */
public class mergingIGC {
    
    private StringBuilder sbTotRecB = new StringBuilder();
    
    /**
     * Extraction of B Records from IGC original file
     * IGC file was checked in logbook addition  
     * @param sTrack 
     */
    public void extractBRec(String sTrack) {
        String DebChar;
        StringBuilder sbRecB = new StringBuilder();
        
        // We received some tracklogs where there is only chr(10)
        String BadCar = Character.toString((char)10);
        String[] sLine = sTrack.split(BadCar);
        int Lg_sLine =  sLine.length;
        // later we received a track file with only chr(13) -:)
        if (Lg_sLine == 1) {
            BadCar = Character.toString((char)13);
            sLine = sTrack.split(BadCar);
            Lg_sLine = sLine.length;
        }
        if (Lg_sLine > 3) {     
            for (int i = 0; i < Lg_sLine; i++) {
                if (!sLine[i].trim().equals("")) {
                    DebChar = sLine[i].substring(0,1);
                    if (DebChar.equals("B") && sLine[i].length() > 23) {
                        sbRecB.append(sLine[i]);
                    }
                }                                     
            }
        }        
        sbTotRecB.append(sbRecB.toString());
    }
    
    public String getTotIGC() {
        return sbTotRecB.toString();
    }
    
}
