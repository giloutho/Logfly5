/* 
 * Copyright Gil THOMAS
 * This file forms an integral part of Logfly project
 * See the LICENSE file distributed with source code
 * for details of Logfly licence project
 */
package org.logfly.trackgps;

/**
 *
 * @author Gil Thomas logfly.org
 * Thermal values 
 */
public class thermique {
    
    public int DeltaAlt;
    public double d_DeltaDist;
    public double GlideRatioValue;
    public double MeanVarioValue;
    public int NumPoint;
    
    public thermique() {
        DeltaAlt = 0;
        d_DeltaDist = 0;
        GlideRatioValue = 0;
        MeanVarioValue = 0;
        NumPoint = 0;
    }
}
