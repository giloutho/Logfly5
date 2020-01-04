/*
 * Copyright Gil THOMAS
 * This file forms an integral part of Logfly project
 * See the LICENSE file distributed with source code
 * for details of Logfly licence project
 */
package trackgps;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import org.xnap.commons.i18n.I18n;

/**
 *
 * It's a pure adaptation of make_analysis_folder class in igc2Kmz/__init__.py, a beautiful code of Tom Payne
 * 
 */
public class remarkable {
   
    private int category;   // THERMAL = 1 GLIDE = 2 DIVE = 3
    private int idxStart;
    private int idxEnd;
    private double climbAverage;
    private double climbMax;
    private double climbMin;
    private double climbPeakMax;
    private double climbPeakMin;  
    private int DeltaAlt;
    private int efficiency;
    private double brute_distance;
    private double distance;
    private double average_ld;
    private double average_speed;
    private double maximum_descent;
    private double peak_descent;
    private int start_altitude;
    private int finish_altitude;
    private LocalDateTime start_time;
    private LocalDateTime finish_time;
    private DateTimeFormatter dtfHHmmss = DateTimeFormatter.ofPattern("HH:mm:ss");
    private long duration;
    private int accumulated_altitude_gain;
    private int accumulated_altitude_loss;
    private String drift_direction;
    private double meanVario;
    
    public remarkable() {
        category = 0;
        DeltaAlt = 0;
        idxStart = 0;
        idxEnd = 0;
        climbAverage = 0.0;
        climbMax = 0.0;
        climbMin = 0.0;
        climbPeakMax = 0.0;
        climbPeakMin = 0.0; 
        efficiency = 0;   // gérer si c'est -1, il met la string 'n/a'
        average_ld = 0.0;   // gérer si c'est négatif, il le met à inf
        average_speed = 0.0;
        maximum_descent = 0.0;        
    }  

    public void setCategory(int category) {
        this.category = category;
    }
       
    public void setIdxStart(int idxStart) {
        this.idxStart = idxStart;
    }

    public int getIdxStart() {
        return idxStart;
    }        

    public void setIdxEnd(int idxEnd) {
        this.idxEnd = idxEnd;
    }

    public int getIdxEnd() {
        return idxEnd;
    }        

    public void setClimbAverage(double climbAverage) {
        this.climbAverage = climbAverage;
    }

    public double getClimbAverage() {
        return climbAverage;
    }        

    public void setClimbMax(double climbMax) {
        this.climbMax = climbMax;
    }

    public void setClimbMin(double climbMin) {
        this.climbMin = climbMin;
    }

    public void setClimbPeakMax(double climbPeakMax) {
        this.climbPeakMax = climbPeakMax;
    }

    public void setClimbPeakMin(double climbPeakMin) {
        this.climbPeakMin = climbPeakMin;
    }

    public void setDeltaAlt(int DeltaAlt) {
        this.DeltaAlt = DeltaAlt;
    }

    public int getDeltaAlt() {
        return DeltaAlt;
    }
    
    public void setEfficiency(int efficiency) {
        this.efficiency = efficiency;
    }

    public int getEfficiency() {
        return efficiency;
    }
        
    public void setBruteDistance(double pDistance) {
        this.brute_distance = pDistance;
        distance =  pDistance / 1000.0;
    }

    public double getDistance() {
        return distance;
    }
        
    public void setAverage_ld(double average_ld) {
        this.average_ld = average_ld;
    }

    public double getAverage_ld() {
        return average_ld;
    }
        
    public void setAverage_speed(double average_speed) {
        this.average_speed = average_speed;
    }

    public double getAverage_speed() {
        return average_speed;
    }
    
    public void setMaximum_descent(double maximum_descent) {
        this.maximum_descent = maximum_descent;
    }
    
   public void setPeak_descent(double peak_descent) {
        this.peak_descent = peak_descent;
    }

    public void setStart_altitude(int start_altitude) {
        this.start_altitude = start_altitude;
    }

    public void setFinish_altitude(int finish_altitude) {
        this.finish_altitude = finish_altitude;
    }

    public void setAccumulated_altitude_gain(int accumulated_altitude_gain) {
        this.accumulated_altitude_gain = accumulated_altitude_gain;
    }

    public void setAccumulated_altitude_loss(int accumulated_altitude_loss) {
        this.accumulated_altitude_loss = accumulated_altitude_loss;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public void setStart_time(LocalDateTime start_time) {
        this.start_time = start_time;
    }

    public LocalDateTime getStart_time() {
        return start_time;
    }
        

    public void setFinish_time(LocalDateTime finish_time) {
        this.finish_time = finish_time;
    }

    public LocalDateTime getFinish_time() {
        return finish_time;
    }    
    
    public void setDrift_direction(String drift_direction) {
        this.drift_direction = drift_direction;
    }               
    
    public int getCategory() {
        return category;
    }
    
    public String getStringDuration() {
        String res = "";
        
        int h = (int) duration/3600;
        int mn = (int) (duration - (h*3600))/60;              
        int sec = (int) duration - ((h*3600)+(mn*60));
        if (h > 0)
            res = String.format("%2d", h)+"h"+String.format("%2d", mn)+"mn"+String.format("%2d", sec)+"s";
        else if (mn > 0)
            res = String.format("%2d", mn)+"mn"+String.format("%2d", sec)+"s";
        else
            res = String.format("%2d", sec)+"s";
        
        return res;
    }

    public double getMeanVario() {
        double d;
        if (duration > 0)
            d = (double) DeltaAlt/duration; 
        else
            d = 0.0;
        return d;
    }
    
    
        
    public String getLegend() {
        String res = null;
        StringBuilder sb = new StringBuilder();
        
        try {
            switch (category) {
                case 1 :
                    // Thermal
                    sb.append(String.valueOf(DeltaAlt)).append("m").append(" - ");
                    double d = (double) DeltaAlt/duration;                                        
                    sb.append(String.format("%2.1f", d)).append(" ").append("m/s");
                    break;
                case 2 :
                    // Glide
                    sb.append(String.format("%2.1f",distance)).append(" ").append("km").append(" [");
                    sb.append(String.format("%2.1f",average_ld)).append("] ");
                    double speed = Math.round(3.6 * brute_distance / duration);
                    sb.append(String.format("%3.0f",speed)).append("km/h");
                    break;
                case 3 :
                    // Dive
                    int negDelta = DeltaAlt*-1;
                    sb.append(String.valueOf(negDelta)).append(" ").append("m");
                    double dd =  (double) DeltaAlt/duration;
                    sb.append(" ").append(String.format("%2.1f", dd)).append(" ").append("m/s");                    
                    break;
            }
            res = sb.toString();
        } catch (Exception e) {
            
        }
                
        return res;
    }
    
    
    public String getHTMLThermal(I18n i18n) {
        String lDeb = "<tr><td>";
        String endLine = "</td></tr>";         
        StringBuilder sb = new StringBuilder();
        sb.append("<table><caption>").append(getLegend()).append("</caption>");                
        sb.append(lDeb).append(i18n.tr("Altitude gain")).append("</td><td>").append(String.valueOf(DeltaAlt)).append("m").append(endLine);
        sb.append(lDeb).append(i18n.tr("Average climb")).append("</td><td>").append(String.format("%2.1f",climbAverage)).append("m/s").append(endLine);
        sb.append(lDeb).append(i18n.tr("Maximum climb")).append("</td><td>").append(String.format("%2.1f",climbMax)).append("m/s").append(endLine);
        sb.append(lDeb).append(i18n.tr("Peak climb")).append("</td><td>").append(String.format("%2.1f",climbPeakMax)).append("m/s").append(endLine);
        sb.append(lDeb).append(i18n.tr("Efficiency")).append("</td><td>").append(String.valueOf(efficiency)).append("%").append(endLine);
        sb.append(lDeb).append(i18n.tr("Start altitude")).append("</td><td>").append(String.valueOf(start_altitude)).append("m").append(endLine);
        sb.append(lDeb).append(i18n.tr("Finish altitude")).append("</td><td>").append(String.valueOf(finish_altitude)).append("m").append(endLine);
        sb.append(lDeb).append(i18n.tr("Start time")).append("</td><td>").append(start_time.format(dtfHHmmss)).append(endLine);
        sb.append(lDeb).append(i18n.tr("Finish time")).append("</td><td>").append(finish_time.format(dtfHHmmss)).append(endLine);
        sb.append(lDeb).append(i18n.tr("Duration")).append("</td><td>").append(getStringDuration()).append(endLine);
        sb.append(lDeb).append(i18n.tr("Accumulated altitude gain")).append("</td><td>").append(String.valueOf(accumulated_altitude_gain)).append("m").append(endLine);
        sb.append(lDeb).append(i18n.tr("Accumulated altitude loss")).append("</td><td>").append(String.valueOf(accumulated_altitude_loss)).append("m").append(endLine);
        sb.append(lDeb).append(i18n.tr("Drift")).append("</td><td>").append(String.format("%2.1f",average_speed)).append("km/h").append(" ").append(drift_direction).append(endLine);  
        sb.append("</table>");

        return sb.toString(); 
    }    
    
    public void printThermal() {

        String RC = "\n";          
        StringBuilder sb = new StringBuilder();        
        sb.append("-------------------------------------").append(RC);   
        sb.append(getLegend()).append(RC);    
        sb.append("Altitude gain").append(" ").append(String.valueOf(DeltaAlt)).append("m").append(" ").append(RC);
        sb.append("Average climb").append(" ").append(String.format("%2.1f",climbAverage)).append("m/s").append(RC);
        sb.append("Maximum climb").append(" ").append(String.format("%2.1f",climbMax)).append("m/s").append(RC);
        sb.append("Peak climb").append(" ").append(String.format("%2.1f",climbPeakMax)).append("m/s").append(RC);
        sb.append("Efficiency").append(" ").append(String.valueOf(efficiency)).append("%").append(RC);
        sb.append("Start altitude").append(" ").append(String.valueOf(start_altitude)).append("m").append(RC);
        sb.append("Finish altitude").append(" ").append(String.valueOf(finish_altitude)).append("m").append(RC);
        sb.append("Start time").append(" ").append(start_time.format(dtfHHmmss)).append(RC);
        sb.append("Finish time").append(" ").append(finish_time.format(dtfHHmmss)).append(RC);
        sb.append("Duration").append(" ").append(getStringDuration()).append(RC);
        sb.append("Accumulated altitude gain").append(" ").append(String.valueOf(accumulated_altitude_gain)).append("m").append(RC);
        sb.append("Accumulated altitude loss").append(" ").append(String.valueOf(accumulated_altitude_loss)).append("m").append(RC);
        sb.append("Drift").append(" ").append(String.format("%2.1f",average_speed)).append("km/h").append(" ").append(drift_direction).append(RC);  
        System.out.println(sb.toString());
    }
    
    public String getHTMLGlides(I18n i18n) {
        String lDeb = "<tr><td>";
        String endLine = "</td></tr>";         
        StringBuilder sb = new StringBuilder();              
        sb.append("<table><caption>").append(getLegend()).append("</caption>"); 
        sb.append(lDeb).append(i18n.tr("Altitude change")).append("</td><td>").append(String.valueOf(DeltaAlt)).append("m").append(endLine);
        sb.append(lDeb).append(i18n.tr("Average descent")).append("</td><td>").append(String.format("%2.1f",climbAverage)).append("m/s").append(endLine);
        sb.append(lDeb).append(i18n.tr("Distance")).append("</td><td>").append(String.format("%2.1f",distance)).append("km").append(endLine);
        sb.append(lDeb).append(i18n.tr("Average glide ratio")).append("</td><td>").append(String.format("%2.1f",average_ld)).append(":1").append(endLine);
        sb.append(lDeb).append(i18n.tr("Average speed")).append("</td><td>").append(String.format("%2.1f",average_speed)).append("km/h").append(endLine);
        sb.append(lDeb).append(i18n.tr("Start altitude")).append("</td><td>").append(String.valueOf(start_altitude)).append("m").append(endLine);
        sb.append(lDeb).append(i18n.tr("Finish altitude")).append("</td><td>").append(String.valueOf(finish_altitude)).append("m").append(endLine);
        sb.append(lDeb).append(i18n.tr("Start time")).append("</td><td>").append(start_time.format(dtfHHmmss)).append(endLine);
        sb.append(lDeb).append(i18n.tr("Finish time")).append("</td><td>").append(finish_time.format(dtfHHmmss)).append(endLine);
        sb.append(lDeb).append(i18n.tr("Duration")).append("</td><td>").append(getStringDuration()).append(endLine);
        sb.append(lDeb).append(i18n.tr("Accumulated altitude gain")).append("</td><td>").append(String.valueOf(accumulated_altitude_gain)).append("m").append(endLine);
        sb.append(lDeb).append(i18n.tr("Accumulated altitude loss")).append("</td><td>").append(String.valueOf(accumulated_altitude_loss)).append("m").append(endLine);
        sb.append("</table>");
        
        return sb.toString();                
    }
    
    public void printGlide() {
        String RC = "\n";          
        StringBuilder sb = new StringBuilder();
        
        sb.append("-------------------------------------").append(RC);       
        sb.append(getLegend()).append(RC);
        sb.append("Altitude change").append(" ").append(String.valueOf(DeltaAlt)).append("m").append(" ").append(RC);
        sb.append("Average descent").append(" ").append(String.format("%2.1f",climbAverage)).append("m/s").append(RC);
        sb.append("Distance").append(" ").append(String.format("%2.1f",distance)).append("km").append(RC);
        sb.append("Average glide ratio").append(" ").append(String.format("%2.1f",average_ld)).append(":1").append(RC);
        sb.append("Average speed").append(" ").append(String.format("%2.1f",average_speed)).append("km/h").append(RC);
        sb.append("Start altitude").append(" ").append(String.valueOf(start_altitude)).append("m").append(RC);
        sb.append("Finish altitude").append(" ").append(String.valueOf(finish_altitude)).append("m").append(RC);
        sb.append("Start time").append(" ").append(start_time.format(dtfHHmmss)).append(RC);
        sb.append("Finish time").append(" ").append(finish_time.format(dtfHHmmss)).append(RC);
        sb.append("Duration").append(" ").append(getStringDuration()).append(RC);
        sb.append("Accumulated altitude gain").append(" ").append(String.valueOf(accumulated_altitude_gain)).append("m").append(RC);
        sb.append("Accumulated altitude loss").append(" ").append(String.valueOf(accumulated_altitude_loss)).append("m").append(RC);
        
        System.out.println(sb.toString());
    }
    
    public String getHTMLDives(I18n i18n) {
        String lDeb = "<tr><td>";
        String endLine = "</td></tr>";         
        StringBuilder sb = new StringBuilder();              
        sb.append("<table><caption>").append(getLegend()).append("</caption>"); 
        sb.append(lDeb).append(i18n.tr("Average descent")).append("</td><td>").append(String.format("%2.1f",climbAverage)).append("m/s").append(endLine);
        sb.append(lDeb).append(i18n.tr("Maximum descent")).append("</td><td>").append(String.format("%2.1f",maximum_descent)).append("m/s").append(endLine);
        sb.append(lDeb).append(i18n.tr("Peak descent")).append("</td><td>").append(String.format("%2.1f",peak_descent)).append("m/s").append(endLine);     
        sb.append(lDeb).append(i18n.tr("Start altitude")).append("</td><td>").append(String.valueOf(start_altitude)).append("m").append(endLine);
        sb.append(lDeb).append(i18n.tr("Finish altitude")).append("</td><td>").append(String.valueOf(finish_altitude)).append("m").append(endLine);
        sb.append(lDeb).append(i18n.tr("Start time")).append("</td><td>").append(start_time.format(dtfHHmmss)).append(endLine);
        sb.append(lDeb).append(i18n.tr("Finish time")).append("</td><td>").append(finish_time.format(dtfHHmmss)).append(endLine);
        sb.append(lDeb).append(i18n.tr("Duration")).append("</td><td>").append(getStringDuration()).append(endLine);
        sb.append(lDeb).append(i18n.tr("Accumulated altitude gain")).append("</td><td>").append(String.valueOf(accumulated_altitude_gain)).append("m").append(endLine);
        sb.append(lDeb).append(i18n.tr("Accumulated altitude loss")).append("</td><td>").append(String.valueOf(accumulated_altitude_loss)).append("m").append(endLine);
        sb.append("</table>");
        
        return sb.toString();                
    }
    
    public void printDive() {
        String RC = "\n";          
        StringBuilder sb = new StringBuilder();
        
        sb.append("-------------------------------------").append(RC);       
        sb.append(getLegend()).append(RC);
        sb.append("Average descent").append(" ").append(String.format("%2.1f",climbAverage)).append("m/s").append(RC);
        sb.append("Maximum descent").append(" ").append(String.format("%2.1f",maximum_descent)).append("m/s").append(RC);
        sb.append("Peak descent").append(" ").append(String.format("%2.1f",peak_descent)).append("m/s").append(RC);     
        sb.append("Start altitude").append(" ").append(String.valueOf(start_altitude)).append("m").append(RC);
        sb.append("Finish altitude").append(" ").append(String.valueOf(finish_altitude)).append("m").append(RC);
        sb.append("Start time").append(" ").append(start_time.format(dtfHHmmss)).append(RC);
        sb.append("Finish time").append(" ").append(finish_time.format(dtfHHmmss)).append(RC);
        sb.append("Duration").append(" ").append(getStringDuration()).append(RC);
        sb.append("Accumulated altitude gain").append(" ").append(String.valueOf(accumulated_altitude_gain)).append("m").append(RC);
        sb.append("Accumulated altitude loss").append(" ").append(String.valueOf(accumulated_altitude_loss)).append("m").append(RC);
        
         System.out.println(sb.toString());
    }
    
}
