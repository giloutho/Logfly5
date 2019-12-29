/*
 * Copyright Gil THOMAS
 * This file forms an integral part of Logfly project
 * See the LICENSE file distributed with source code
 * for details of Logfly licence project
 */
package trackgps;

import igc.pointIGC;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import model.cutting;
import org.xnap.commons.i18n.I18n;

/**
 * 
 * It's a pure adaptation of analyze class in igc2Kmz/track.py, an effective code of Tom Payne
 * This is probably not a very good translation because of our poor knowledge of the python language
 * We kept the names of the variables and followed step by step original code
 * 
 * testing in igc2Kmz/bin with : python igc2kmz.py -i xxxx.igc -o xxxx.kmz
 * testing in igc2Kmz/bin with : python igc2kmz.py -i 858umbh1.igc -o 858umbh1.kmz
 */

public class analyse {
    
    private traceGPS currTrace;
    private I18n i18n; 
    private List<transCoord> coords = new ArrayList<transCoord>();
    private List<Long> t = new ArrayList<Long>(); 
    private List<Double> climb = new ArrayList<Double>(); 
    private ArrayList<cutting> cuttingList = new ArrayList<cutting>();

    private double R = 6371000.0;
    private String[] cardinals = {"N","NNE","NE","ENE","E","ESE","SE","SSE","S","SSW","SW","WSW","W","WNW","NW","NNW"};
    private final int UNKNOWN = 0;
    private final int THERMAL = 1;
    private final int GLIDE = 2;
    private final  int DIVE = 3;
    private int bestGain = 0;
    private int bestGainEnd;
    private double bestGlide = 0.0;
    private int bestGlideEnd;
    private final double progressValue = 0.7;
    
    public List<remarkable> finalThermals = new ArrayList<remarkable>(); 
    public List<remarkable> finalDives = new ArrayList<remarkable>(); 
    public List<remarkable> finalGlides = new ArrayList<remarkable>();     
    
    private int extractTime;    
    double percThermals;
    double percGlides;
    double percDives;
    
    
    public analyse(traceGPS pTrack, I18n pI18n) {        
        this.currTrace = pTrack;  
        i18n = pI18n;
        for (int i = 0; i < currTrace.Tb_Good_Points.size(); i++) {
            transCoord c = new transCoord(currTrace.Tb_Good_Points.get(i).Latitude,currTrace.Tb_Good_Points.get(i).Longitude,currTrace.Tb_Good_Points.get(i).AltiGPS,currTrace.Tb_Good_Points.get(i).dHeure);
            coords.add(c);
        }       
        if (coords.size() > 0) compute();        
    }

    public int getBestGainEnd() {
        return bestGainEnd;
    }

    public double getBestGlideEnd() {
        return bestGlideEnd;
    }

    public ArrayList<cutting> getCuttingList() {
        return cuttingList;
    }
                            
    private void compute()  {
        int dt = 20;   // dt est le péridoe d'exploration fixé à 20 secondes par défaut  (ligne 39)
        int n = coords.size();
        int period = (int) (Duration.between(coords.get(0).dt,coords.get(n-1).dt).getSeconds()/ n);  
        if (dt < 2 * period) dt = 2 * period;     
        for (int i = 0; i < coords.size(); i++) {
            // en fait on est plus en UTC pusiqu'on a converti mais c'est à priori sans importance
            Instant instant = coords.get(i).dt.atZone(ZoneOffset.UTC).toInstant();
            t.add(instant.toEpochMilli()/1000);            
        }
        
        List<Double> s = new ArrayList<Double>();     
        s.add(0.0);                                    
        for (int i = 1; i < n; i++) {                              
            s.add(s.get(i-1)+distanceTo(coords.get(i-1), coords.get(i)));            
        }

        List<Double> ele = new ArrayList<Double>();     
        for (int i = 1; i < n; i++) {
            ele.add((coords.get(i-1).ele + coords.get(i).ele) / 2.0);
        }     
        
        double total_dz_positive = 0;
        double max_dz_positive = 0;
        double min_ele = coords.get(0).ele;      
        double dz;
        List<Double> speed = new ArrayList<Double>(); 
        List<Double> tec = new ArrayList<Double>(); 
        List<Double> progress = new ArrayList<Double>(); 
        double valProgress;        
        
        for (int i = 1; i < n; i++) {            
            dz = coords.get(i).ele - coords.get(i-1).ele;     
            if (dz > 0) total_dz_positive += dz;
            if (coords.get(i).ele < min_ele) 
                min_ele = coords.get(i).ele;
            else if (coords.get(i).ele - min_ele > max_dz_positive)
                max_dz_positive = coords.get(i).ele - min_ele;
        } 
        
        int i0 = 0;
        int i1 = 0;
        transCoord coord0;
        transCoord coord1;
        double s0;
        double s1;
        float delta0;
        float delta1;
        
        for (int i = 1; i < n; i++) {            
            long t0 = (t.get(i - 1) + t.get(i)) / 2 - dt / 2;           
            while (t.get(i0) <= t0) {
                i0 += 1;
            }
            if (i0 == 0) {
                coord0 = coords.get(0);
                s0 = s.get(0);
            } else {                
                delta0 = (float) (t0 - t.get(i0-1)) / (t.get(i0) - t.get(i0 -1));
                coord0 = interpolate(coords.get(i0 - 1),coords.get(i0),delta0);               
                s0 = (1.0 - delta0) * s.get(i0 - 1) + delta0 * s.get(i0);                
            }
            long t1 = t0 + dt;
            while (i1 < n && t.get(i1) < t1) {
                i1 += 1;
            }
            if (i1 == n) {
                coord1 = coords.get(n - 1);
                s1 = s.get(n - 1);
            } else {
                delta1 = (float) (t1 - t.get(i1 - 1)) / (t.get(i1) - t.get(i1 - 1));  
                coord1 = interpolate(coords.get(i1 - 1),coords.get(i1),delta1);               
                s1 = (1.0 - delta1) * s.get(i1 - 1) + delta1 * s.get(i1);
            }
            double ds = s1 - s0;
            double ds2 = s1 * s1 - s0 * s0;
            dz = coord1.ele - coord0.ele;            
            double dp = distanceTo(coord0, coord1);           
             if (ds == 0.0)
                valProgress = 0.0;
             else if (dp > ds)
                valProgress = 1.0;
            else
                valProgress = dp / ds;
            speed.add(3.6 * ds / dt);
            climb.add(dz / dt);
            tec.add(dz / dt + ds2 / (2 * 9.80665));
            progress.add(valProgress);      
        }

        int[] state = new int[n-1];   // automatically be initialized to zero  (UNKNOWN)
        List<specialSegment> lstGlide = new ArrayList<specialSegment>();
        List<specialSegment> lstDive = new ArrayList<specialSegment>();
        List<specialSegment> lstThermal = new ArrayList<specialSegment>();
        // glide detection
        boolean inGlide = false; 
        int startPoint = 0;
        for (int j = 1; j < progress.size(); j++) {
            if (progress.get(j) >= progressValue) {
                if (!inGlide) {                     
                    startPoint = j;     
                    inGlide = true;
                }                
            } else {
                if (inGlide) {
                    specialSegment pGlide = new specialSegment();
                    pGlide.idxStart = startPoint;
                    pGlide.idxEnd = j;
                    pGlide.typePoint = GLIDE;
                    lstGlide.add(pGlide);
                    inGlide = false;
                }
            }
                
        }
        // Group and condense
        boolean mergeZone = false;
        int realStart = 0;
        int realEnd;
        int beginNext;
        long nextPrevDelta;
        
        for (int i = 0; i < lstGlide.size(); i++) {
            if (i < lstGlide.size()-1) {
                beginNext = lstGlide.get(i+1).idxStart;
                int prevEnd = lstGlide.get(i).idxEnd;
                nextPrevDelta = t.get(beginNext)-t.get(prevEnd);
            } else {
                nextPrevDelta = 100;
            }                                     
            if ( nextPrevDelta < 60) {            
                if (!mergeZone) {
                    mergeZone = true;
                    realStart = lstGlide.get(i).idxStart;                  
                    realEnd = lstGlide.get(i).idxEnd;                  
                } 
            } else {
                if (!mergeZone) {
                    realStart = lstGlide.get(i).idxStart;
                    realEnd = lstGlide.get(i).idxEnd;
                } else {
                   realEnd = lstGlide.get(i).idxEnd; 
                   mergeZone = false;
                } 
                for (int k = realStart; k < realEnd; k++) {
                    state[k] = GLIDE;
                }
            }
        }
                
        // dive detection
        boolean inDive = false;         
        for (int j = 1; j < progress.size(); j++) {
            if (progress.get(j) < progressValue && climb.get(j) < 1.0) {
                if (!inDive) {                     
                    startPoint = j;     
                    inDive = true;
                }                
            } else {
                if (inDive) {
                    specialSegment pDive = new specialSegment();
                    pDive.idxStart = startPoint;
                    pDive.idxEnd = j;
                    pDive.typePoint = DIVE;
                    lstDive.add(pDive);
                    inDive = false;
                }            
            }
        }
        // Group and condense
        mergeZone = false;
        realStart = 0;
        for (int i = 0; i < lstDive.size(); i++) {
            if (i < lstDive.size()-1) {
                beginNext = lstDive.get(i+1).idxStart;
                int prevEnd = lstDive.get(i).idxEnd;
                nextPrevDelta = t.get(beginNext)-t.get(prevEnd);
            } else {
                nextPrevDelta = 100;
            }                                     
            if ( nextPrevDelta < 30) {     
                    if (!mergeZone) {
                        mergeZone = true;
                        realStart = lstDive.get(i).idxStart;                  
                        realEnd = lstDive.get(i).idxEnd;                  
                    } 
            } else {
                if (!mergeZone) {
                    realStart = lstDive.get(i).idxStart;
                    realEnd = lstDive.get(i).idxEnd;
                } else {
                   realEnd = lstDive.get(i).idxEnd; 
                   mergeZone = false;
                }
                if (coords.get(realEnd).ele - coords.get(realStart).ele < -100) {
                    for (int k = realStart; k < realEnd; k++) {
                        state[k] = DIVE;
                    }
                }
            }
        }        
        
        // thermal detection
        boolean inThermal = false;         
        for (int j = 1; j < progress.size(); j++) {
            //if ((progress.get(j) < progressValue && climb.get(j) > 0.0) || (speed.get(j) < 10.0 && climb.get(j) > 0.0) || (climb.get(j) > 1.0)) {
            // we remove last condition climb.get(j) > 1.0
            // In a load-bearing air mass, it is possible to glide with a Vz greater than 1.0 m/s
            if ((progress.get(j) < progressValue && climb.get(j) > 0.0) || (speed.get(j) < 10.0 && climb.get(j) > 0.0)) {    
                if (!inThermal) {                     
                    startPoint = j;     
                    inThermal = true;
                }   
            } else {
                if (inThermal) {
                        specialSegment pThermal = new specialSegment();
                        pThermal.idxStart = startPoint;
                        pThermal.idxEnd = j;
                        pThermal.typePoint = THERMAL;
                        lstThermal.add(pThermal);
                    inThermal = false;                    
                }
            }
        }        
        // Group and condense
        mergeZone = false;
        realStart = 0;        
        for (int i = 0; i < lstThermal.size(); i++) {
            if (i < lstThermal.size()-1) {
                beginNext = lstThermal.get(i+1).idxStart;
                int prevEnd = lstThermal.get(i).idxEnd;
                nextPrevDelta = t.get(beginNext)-t.get(prevEnd);
            } else {
                nextPrevDelta = 100;
            }                                     
            if ( nextPrevDelta < 60) {            
                if (!mergeZone) {
                    mergeZone = true;
                    realStart = lstThermal.get(i).idxStart;                  
                    realEnd = lstThermal.get(i).idxEnd;                  
                } 
            } else {
                if (!mergeZone) {
                    realStart = lstThermal.get(i).idxStart;
                    realEnd = lstThermal.get(i).idxEnd;
                } else {
                   realEnd = lstThermal.get(i).idxEnd; 
                   mergeZone = false;
                }   
                for (int k = realStart; k < realEnd; k++) {
                    state[k] = THERMAL;
                }
            }
        }

        int currState;
        int endPoint;
        for (int i = 0; i < state.length-1; i++) {
            startPoint = i;
            currState = state[i];
            while (i < state.length && state[i] == currState) {
                i += 1;
            }
            endPoint = i;
            i = i -1;
            currState = state[i];

            long diffT = t.get(endPoint) - t.get(startPoint);
            double diffEle = coords.get(endPoint).ele - coords.get(startPoint).ele;
            if (state[i] == THERMAL) {
                // Only gains in altitude above 100 m will be taken into consideration
                if (diffT >= 60 && diffEle > 100) {
                    addDetails(startPoint, endPoint, THERMAL);
                }                
            } else {
                if (state[i] == DIVE) {
                    if (diffT >= 30 && diffEle / diffT < -2) {
                        addDetails(startPoint, endPoint, DIVE);                        
                    }
                } else {
                    if (state[i] == GLIDE) {   
                        double dp = distanceTo(coords.get(startPoint), coords.get(endPoint));                                 
                        if (dp >= 2000) {
                            addDetails(startPoint, endPoint, GLIDE);                            
                        }
                    }
                }                
            }

        }
        currTrace.setBestGain(bestGain);
        
        fillCuttingList();
    }
    
    private void fillCuttingList() {

        DecimalFormatSymbols decimalFormatSymbols = new DecimalFormatSymbols();
        decimalFormatSymbols.setDecimalSeparator('.');        
        DecimalFormat decimalFormat = new DecimalFormat("###.00000", decimalFormatSymbols);         
        
        double totPeriod;
        double totThermals;
        double totGlides;
        double totDives;
        
        // Take off
        String h = currTrace.getDT_Deco().format(DateTimeFormatter.ofPattern("HH:mm:ss"));   
        cutting cu = new cutting();
        cu.setCTime(h);
        cu.setCElapsed("");
        cu.setCText(i18n.tr("Take off")); 
        cu.setCLdt(currTrace.getDT_Deco());                      
        cuttingList.add(cu);
        
        cu = new cutting();
        h = currTrace.getDT_Attero().format(DateTimeFormatter.ofPattern("HH:mm:ss"));         
        cu.setCTime(h);
        cu.setCElapsed("");
        cu.setCText(i18n.tr("Landing")); 
        cu.setCLdt(currTrace.getDT_Attero());        
        cuttingList.add(cu);
        
        double dLat1, dLong1, dLat2, dLong2;
        
        totPeriod = Math.abs(Duration.between(currTrace.getDT_Attero(),currTrace.getDT_Deco()).getSeconds());              
        
        totThermals = 0;
        for (int i = 0; i < finalThermals.size(); i++) {
            remarkable currRmk = finalThermals.get(i);
            cu = new cutting();
            h = currRmk.getStart_time().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
            cu.setCTime(h);
            cu.setCElapsed("xx");
            cu.setCText(i18n.tr("Thermal")+" "+String.valueOf(i));  
            cu.setCHTML(currRmk.getHTMLThermal(i18n));
            cu.setCLdt(currRmk.getFinish_time());
            int pThermal = (int) Math.abs(Duration.between(currRmk.getFinish_time(),currRmk.getStart_time()).getSeconds());  
            pointIGC startPoint = currTrace.Tb_Good_Points.get(currRmk.getIdxStart());              
            pointIGC endPoint = currTrace.Tb_Good_Points.get(currRmk.getIdxEnd());
            StringBuilder sbCoord = new StringBuilder();
            sbCoord.append(decimalFormat.format(startPoint.Latitude)).append(",");
            sbCoord.append(decimalFormat.format(startPoint.Longitude)).append(",");       
            sbCoord.append(decimalFormat.format(endPoint.Latitude)).append(",");
            sbCoord.append(decimalFormat.format(endPoint.Longitude));
            cu.setCCoord(sbCoord.toString());
            
            totThermals += pThermal;
            cuttingList.add(cu);
        }
        
        totGlides = 0;
        for (int i = 0; i < finalGlides.size(); i++) {
            remarkable currRmk = finalGlides.get(i);
            cu = new cutting();
            h = currRmk.getStart_time().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
            cu.setCTime(h);
            cu.setCElapsed("xx");
            cu.setCText(i18n.tr("Glide")+" "+String.valueOf(i));  
            cu.setCHTML(currRmk.getHTMLGlides(i18n));
            cu.setCLdt(currRmk.getFinish_time());
            
            int pGlide = (int) Math.abs(Duration.between(currRmk.getFinish_time(),currRmk.getStart_time()).getSeconds());  
            if (i == 0) extractTime = (int) Math.abs(Duration.between(currRmk.getStart_time(),currTrace.getDT_Deco()).getSeconds());  
            pointIGC startPoint = currTrace.Tb_Good_Points.get(currRmk.getIdxStart());              
            pointIGC endPoint = currTrace.Tb_Good_Points.get(currRmk.getIdxEnd());
            StringBuilder sbCoord = new StringBuilder();
            sbCoord.append(decimalFormat.format(startPoint.Latitude)).append(",");
            sbCoord.append(decimalFormat.format(startPoint.Longitude)).append(",");       
            sbCoord.append(decimalFormat.format(endPoint.Latitude)).append(",");
            sbCoord.append(decimalFormat.format(endPoint.Longitude));
            cu.setCCoord(sbCoord.toString());
            
            totGlides += pGlide;            
            cuttingList.add(cu);
        }        
        
        totDives = 0;
        for (int i = 0; i < finalDives.size(); i++) {
            remarkable currRmk = finalDives.get(i);
            int pDive = (int) Math.abs(Duration.between(currRmk.getFinish_time(),currRmk.getStart_time()).getSeconds());  
            
            totDives += pDive;
        }
        
        Collections.sort(cuttingList, cutting.elapsedComparator);
        for (int i = 1; i < cuttingList.size(); i++) {
            int period = (int) Math.abs(Duration.between(cuttingList.get(i).getCLdt(),cuttingList.get(0).getCLdt()).getSeconds()); 
            cuttingList.get(i).setCElapsed(formatPeriod(period));   
        }
        
        percThermals = totThermals/totPeriod;
        percGlides = totGlides/totPeriod;
        percDives = totDives/totPeriod;        
        
    }    
    
    /*
    *  Only for debug purpose
    */
    private void listRemarkables(int categ) {                  
            switch (categ) {
                case THERMAL:
                    for (int i = 0; i < finalThermals.size(); i++) {  
                        finalThermals.get(i).printThermal();
                    }
                    break;
                case DIVE:
                    for (int i = 0; i < finalDives.size(); i++) {  
                        finalDives.get(i).printDive();
                    }
                    break;
                case GLIDE:
                    for (int i = 0; i < finalGlides.size(); i++) {  
                        finalGlides.get(i).printGlide();
                    }
                    break;               
            }
    }
    
    private void addDetails(int idxStart, int idxEnd, int category) {
        remarkable currRmk = new remarkable();
        currRmk.setIdxStart(idxStart);
        currRmk.setIdxEnd(idxEnd);
        transCoord coord0 = coords.get(idxStart);
        transCoord coord1 = coords.get(idxEnd);
        int total_dz_positive = 0;
        int total_dz_negative = 0;
        double peak_climb_max = 0;
        double peak_climb_min = 0;
        double climb_max = 0;
        double climb_min = 0;
        double dz;
        long dt;
        double dp = distanceTo(coord0, coord1); 
        for (int i = idxStart; i < idxEnd; i++) {
            dz = coords.get(i + 1).ele - coords.get(i).ele;
            dt = t.get(i + 1) - t.get(i);
            if (dz > 0)
                total_dz_positive += dz;
            else if (dz < 0)
                total_dz_negative += dz;
            double peak_climb = (double) (dz / dt);
            if (peak_climb > peak_climb_max) peak_climb_max = peak_climb;
            if (peak_climb < peak_climb_min) peak_climb_min = peak_climb;     
            double currClimb = climb.get(i);
            if (currClimb > climb_max) climb_max = currClimb;
            if (currClimb < climb_min) climb_min = currClimb;  
            
        }
        dz = coords.get(idxEnd).ele - coords.get(idxStart).ele;
        dt = t.get(idxEnd) - t.get(idxStart);  
        double theta = initial_bearing(coord0,coord1);
        int deltaAlt = (int) Math.round(dz);
        if (deltaAlt > bestGain) {
            bestGain = deltaAlt;
            bestGainEnd = idxEnd;
        }
        currRmk.setDeltaAlt(deltaAlt);
        currRmk.setClimbAverage(dz / dt);
        currRmk.setClimbMax(climb_max);
        currRmk.setClimbPeakMax(peak_climb_max);
        double divisor = dt * climb_max;
        if (divisor == 0)
            currRmk.setEfficiency(-1);                
        else
            currRmk.setEfficiency((int) (Math.round(100.0 * dz / divisor)));
        currRmk.setBruteDistance(dp);
        if (dp > bestGlide) {
            bestGlide = dp;
            bestGlideEnd = idxEnd;
        }
        double average_ld = (-dp / dz);
        currRmk.setAverage_ld(average_ld);
        currRmk.setAverage_speed(3.6 * dp / dt);
        currRmk.setMaximum_descent(climb_min);
        currRmk.setPeak_descent(peak_climb_min);
        currRmk.setStart_time(coord0.dt);
        currRmk.setFinish_time(coord1.dt);       
        currRmk.setStart_altitude((int) coord0.ele);
        currRmk.setFinish_altitude((int) coord1.ele);        
        currRmk.setAccumulated_altitude_gain(total_dz_positive);
        currRmk.setAccumulated_altitude_loss(total_dz_negative);  
        currRmk.setDrift_direction(rad_to_cardinal(theta + Math.PI));
        currRmk.setDuration(dt);
        currRmk.setCategory(category);
        switch (category) {
            case THERMAL:
                finalThermals.add(currRmk);
                break;
            case GLIDE:
                finalGlides.add(currRmk);
                break;
            case DIVE:
                finalDives.add(currRmk);
                break;                
        }

    }
    
    private transCoord interpolate(transCoord c, transCoord other, float delta) {
        
       // transCoord resPoint = new transCoord();
        
        double d = Math.sin(c.lat) * Math.sin(other.lat) + Math.cos(c.lat) * Math.cos(other.lat) * Math.cos(other.lon - c.lon);
        if (d < 1.0)
            d = delta * Math.acos(d);
        else 
            d = 0.0;    
        double y = Math.sin(other.lon - c.lon) * Math.cos(other.lat);
        double x = Math.cos(c.lat) * Math.sin(other.lat) - Math.sin(c.lat) * Math.cos(other.lat) * Math.cos(other.lon - c.lon);
        double theta = Math.atan2(y, x);          
        double lat = Math.asin(Math.sin(c.lat) * Math.cos(d) + Math.cos(c.lat) * Math.sin(d) * Math.cos(theta));
        double lon = c.lon + Math.atan2(Math.sin(theta) * Math.sin(d) * Math.cos(c.lat),Math.cos(d) - Math.sin(c.lat) * Math.sin(lat));
        double ele = (1.0 - delta) * c.ele + delta * other.ele;        

        transCoord res = new transCoord(lat, lon, ele); 
        
        return res;
    }
    
    private double distanceTo(transCoord c, transCoord other) {

        double d = Math.sin(c.lat) * Math.sin(other.lat) + Math.cos(c.lat) * Math.cos(other.lat) * Math.cos(c.lon - other.lon);
        double res = 0;
        if(d < 1.0) res = R * Math.acos(d);
        
        return res;
    }
    
    private double height_triangle(int sideA, int sideB, int sideC) {
        
        // compute half triangle perimeter
        double s = (sideA+sideB+sideC)/2;
        // the second part of the Heron formula is then used
        double dArea = Math.sqrt(s*(s-sideA)*(s-sideB)*(s-sideC));
        double h = (dArea*2)/sideB;
        
        return h;
        
    }    
    
    private double initial_bearing(transCoord c, transCoord other) {
        // return the initial bearing from self to other
        double y = Math.sin(other.lon - c.lon) * Math.cos(other.lat);
        double x = Math.cos(c.lat) * Math.sin(other.lat) - Math.sin(c.lat) * Math.cos(other.lat) * Math.cos(other.lon - c.lon);
        
        return Math.atan2(y, x);
    }
    
    private String rad_to_cardinal(double rad) {
        
        String res = "";
        while (rad < 0.0) {
            rad += 2 * Math.PI;
        }
        int idx = (int) (8 * rad / Math.PI + 0.5) % 16;
        if (idx >= 0 && idx < cardinals.length) res = cardinals[idx];
        
        return res;
    }
    
    private class transCoord {
        
        public double lat;
        public double lon;
        public double ele;
        public LocalDateTime dt;
        
        public transCoord(double pLat, double pLong, double pAlt) {
            this.lat = pLat;
            this.lon = pLong;
            this.ele = pAlt;
            this.dt = null;
        }
        
        public transCoord(double pLat, double pLong, double pAlt, LocalDateTime pLdt) {
            this.lat = Math.PI * pLat / 180.0;
            this.lon = Math.PI * pLong / 180.0;
            this.ele = pAlt;
            this.dt = pLdt;
        }    
    }
    
    private class specialSegment {        
        public int idxStart;
        public int idxEnd;
        public int typePoint;
    }
    
    private  String formatPeriod(int iDuration) {
        String res = "";
        if (iDuration > 0) {            
            int nbHour = iDuration/3600;
            int nbMn = (iDuration - (nbHour*3600))/60;
            StringBuilder sbDur = new StringBuilder();
            sbDur.append(String.format("%2d", nbHour)).append("h");
            sbDur.append(String.format("%02d", nbMn)).append("mn");
            res = sbDur.toString();   
        }
        
        return res;
    }    
    
}


