/*
 * Copyright Gil THOMAS
 * This file forms an integral part of Logfly project
 * See the LICENSE file distributed with source code
 * for details of Logfly licence project
 */
package polyline;

import java.util.ArrayList;

/**
 * Found on https://github.com/scoutant/polyline-decoder
 * 
 * Reimplementation of Mark McClures Javascript PolylineEncoder
 * All the mathematical logic is more or less copied by McClure
 *  
 * @author Mark Rambow
 * @e-mail markrambow[at]gmail[dot]com
 * @version 0.1
 * 
 */

public class Track {
    
    private ArrayList<Trackpoint> trackpoints = new ArrayList<Trackpoint>();

    public ArrayList<Trackpoint> getTrackpoints() {
        return this.trackpoints;
    }

    public void setTrackpoints(ArrayList<Trackpoint> trackpoints) {
        this.trackpoints = trackpoints;
    }

    public void addTrackpoint(Trackpoint trkpt) {
        this.trackpoints.add(trkpt);
    }   
    
    public int getLength() {
        return this.trackpoints.size();
    }
}

