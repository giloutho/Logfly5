/* 
 * Copyright Gil THOMAS
 * This file forms an integral part of Logfly project
 * See the LICENSE file distributed with source code
 * for details of Logfly licence project
 */
package gps;

/**
 *
 * @author gil
 */
public class gpsutils {
    
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
