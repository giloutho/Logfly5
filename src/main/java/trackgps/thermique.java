/*
 * Copyright Gil THOMAS
 * Ce fichier fait partie intégrante du projet Logfly
 * Pour tous les détails sur la licence du projet Logfly
 * Consulter le fichier LICENSE distribué avec le code source
 */
package trackgps;

/**
 *
 * @author Gil Thomas logfly.org
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
