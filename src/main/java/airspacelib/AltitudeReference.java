/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package airspacelib;

/**
 * Created by Rob Verhoef on 20-10-2015
 *    was originally in AirspacesImportApp/src/com/mobileaviationtools/AirspacesData/
 */
public enum AltitudeReference {
    GND,        // Ground
    MSL,        // Main sea level
    STD,        // Standard atmosphere
    AGL         // Above Ground level
    ;

    @Override
    public String toString() {
        return super.toString();
    }
}