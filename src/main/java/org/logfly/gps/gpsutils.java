/* 
 * Copyright Gil THOMAS
 * This file forms an integral part of Logfly project
 * See the LICENSE file distributed with source code
 * for details of Logfly licence project
 */
package org.logfly.gps;

import java.util.ArrayList;

/**
 *
 * @author gil
 */
public class gpsutils {
    
    
    /**
     * IGC File naming convention [FAI specifications]
     * For a track dated :  2017 september 30th we got an IGC file called : 79Uxxxx.IGC
     * first digit : year value 0 to 9, cycling every 10 years
     * second digit : month  -> 1  to  C (12)
     * third digit : day  -> 1  to  31 (V) 
     * 4th digit manufacturer's IGC code letter
     * 5 to 7th digit = unique FR Serial Number (S/N); 3 alphanumeric characters
     * 8 th digit : flight number of the day; 1 to 9 then, if needed, A=10 through to Z=35
     * 
     * 
     * 
     * Month -> value 1 to 9 then A for 10, B=11, C=12.
     */
    public static ArrayList<String> listMonth = new ArrayList<String>() {{
        add("0");        
        add("1");
        add("2");
        add("3");
        add("4");
        add("5");
        add("6");
        add("7");
        add("8");
        add("9");
        add("A");
        add("B");
        add("C");        
    }};
    
    /**
     * IGC File naming convention
     * Day -> value 1 to 9 then A=10, B=11, C=12, .... U=30, V=31.
     */
    public static ArrayList<String> listDay = new ArrayList<String>() {{
        add("0");    
        add("1");   
        add("2");
        add("3");
        add("4");
        add("5");
        add("6");
        add("7");
        add("8");
        add("9");        
        add("A");
        add("B");
        add("C");
        add("D");
        add("E");
        add("F");
        add("G");
        add("H");
        add("I");
        add("J");     
        add("K");   
        add("L");
        add("M");
        add("N");
        add("O");
        add("P");
        add("Q");
        add("R");
        add("S");
        add("T");
        add("U");     
        add("V");     // 31         
    }};       
    
    /**
     * calculates checksum of the parameter string and adds this to the output 
     * @param s
     * @return 
     */
    public static String ajouteChecksum(String s) {
        String res = null;
        int chksum = 0;
        char[] charArray = s.toCharArray();
        for (int i=0; i<charArray.length; i++) {
            switch (charArray[i]) {
                case '$': break;
                case '*': break;
                default:  chksum = chksum ^ (int)charArray[i];
            }
            
        }
        res = s+Integer.toHexString(chksum).toUpperCase();
               
        return res;     
    }
    
    public static boolean checkCrc(byte[] packetData)  {              
        int checksum = 0xFF & packetData[2];
        for (int i = 3; i < packetData.length - 1; i++) {
            checksum = checksum ^ 0xFF & packetData[i];
        }     
        if (Integer.toHexString(checksum).equals(Integer.toHexString(0xFF & packetData[packetData.length-1])))
            return true;
        else
            return false;            
    }
    
    public static int oneByteToInt(byte value) {
        return (int)value & 0xFF;
    }
    
    public static int twoBytesToInt(byte[] b) {
        // http://www.java2s.com/Code/Android/Date-Type/Integer.htm
        // On a inversé les bytes par rapport au code d'origine
        int temp0 = b[1] & 0xFF;
        int temp1 = b[0] & 0xFF;
        return ((temp0 << 8) + temp1);

    }
    
    public static int fourBytesToInt(byte[] b) {
        // http://www.java2s.com/Code/Android/Date-Type/Integer.htm
        // On a inversé entièrement l'ordre des bytes par rapport au code d'origine
        if (b.length == 4)
          return b[3] << 24 | (b[2] & 0xff) << 16 | (b[1] & 0xff) << 8
              | (b[0] & 0xff);    

        return 0;
    }
    
    /**
     * digital signature generation
     * @param b
     * @return 
     */
    public static String bytesToHex(byte[] b) {
        // Initialy letters were in upperercase
        // for xLogfly compatibility, they are converted in lowercase
        char hexDigit[] = {'0', '1', '2', '3', '4', '5', '6', '7',
                           '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};
        StringBuffer buf = new StringBuffer();
        for (int j=0; j<b.length; j++) {
           buf.append(hexDigit[(b[j] >> 4) & 0x0f]);
           buf.append(hexDigit[b[j] & 0x0f]);
        }
        return buf.toString();
    }
    
}
