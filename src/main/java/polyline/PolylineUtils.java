/*
 * Copyright Gil THOMAS
 * This file forms an integral part of Logfly project
 * See the LICENSE file distributed with source code
 * for details of Logfly licence project
 */
package polyline;

import java.util.List;

/**
 *
 * Found on https://github.com/scoutant/polyline-decoder
 */
public class PolylineUtils {

    public static String toString(List<Point> polyline) {
        String str = "[ ";
        for( Point p : polyline) {
                str += p;
        }
        return str + " ]";
    }

    public static String toMarkers(List<Point> polyline) {
        String str = "";
        for( Point p : polyline) {
                str += "|" + p.getLat()+","+p.getLng();
        }
        return str.substring(1, str.length());
    }    
}
