/*
 * Copyright Gil THOMAS
 * This file forms an integral part of Logfly project
 * See the LICENSE file distributed with source code
 * for details of Logfly licence project
 */
package org.logfly.systemio;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author gil
 */
public class checking {
    

    public static boolean parseDouble(String sDouble) {
        
        boolean res;
        final String Digits     = "(\\p{Digit}+)";
        final String HexDigits  = "(\\p{XDigit}+)";
        // an exponent is 'e' or 'E' followed by an optionally
        // signed decimal integer.
        final String Exp        = "[eE][+-]?"+Digits;
        final String fpRegex    =
            ("[\\x00-\\x20]*"+  // Optional leading "whitespace"
             "[+-]?(" + // Optional sign character
             "NaN|" +           // "NaN" string
             "Infinity|" +      // "Infinity" string

             // A decimal floating-point string representing a finite positive
             // number without a leading sign has at most five basic pieces:
             // Digits . Digits ExponentPart FloatTypeSuffix
             //
             // Since this method allows integer-only strings as input
             // in addition to strings of floating-point literals, the
             // two sub-patterns below are simplifications of the grammar
             // productions from section 3.10.2 of
             // The Java Language Specification.

             // Digits ._opt Digits_opt ExponentPart_opt FloatTypeSuffix_opt
             "((("+Digits+"(\\.)?("+Digits+"?)("+Exp+")?)|"+

             // . Digits ExponentPart_opt FloatTypeSuffix_opt
             "(\\.("+Digits+")("+Exp+")?)|"+

             // Hexadecimal strings
             "((" +
              // 0[xX] HexDigits ._opt BinaryExponent FloatTypeSuffix_opt
              "(0[xX]" + HexDigits + "(\\.)?)|" +

              // 0[xX] HexDigits_opt . HexDigits BinaryExponent FloatTypeSuffix_opt
              "(0[xX]" + HexDigits + "?(\\.)" + HexDigits + ")" +

              ")[pP][+-]?" + Digits + "))" +
             "[fFdD]?))" +
             "[\\x00-\\x20]*");// Optional trailing "whitespace"

        if (Pattern.matches(fpRegex, sDouble))
            res = true;
        else {
            res = false;
        }
        
        return res;
    }
    
    public static boolean checkInt(String sInt) {
        boolean res;
        
        final String fpRegex = "([\\+-]?\\d+)([eE][\\+-]?\\d+)?";
        if (Pattern.matches(fpRegex, sInt))
            res = true;
        else {
            res = false;
        }
        
        return res;
    }
    
    public static boolean checkMail(String strAdress)  {
        
        Pattern validMail = Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$");
        Matcher matcher = validMail.matcher(strAdress);
        return matcher.find();        
    }    
    
}
