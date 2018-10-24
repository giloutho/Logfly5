/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.logfly.airspacelib;

import java.util.ArrayList;
import java.util.Arrays;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;

/**
 * Created by Rob Verhoef on 20-10-2015.
 * 
 * We try to keep original name class
 * 
 */

public class Airspace {
    
   public Airspace()
    {
        coordinates = new ArrayList<Coordinate>();
        AltLimit_Top = 0;
        AltLimit_Top_Ref = AltitudeReference.STD;
        AltLimit_Top_Unit = AltitudeUnit.F;
        AltLimit_Bottom = 0;
        AltLimit_Bottom_Ref = AltitudeReference.STD;
        AltLimit_Bottom_Unit = AltitudeUnit.F;
        originalText = new StringBuilder();
        latMini = 0 ;
        latMaxi = 0;
        longMini = 0;
        longMaxi = 0;
        violations = 0;        
    }

    public AirspaceCategory Category;
    public String typeColor;
    public String Version;
    public Integer ID;
    public String Country;
    public String Name;
    public Integer AltLimit_Top;
    public StringBuilder originalText;
    public Integer getAltLimit_Top()
    {
        return (AltLimit_Top == null) ? 0 : AltLimit_Top;
    }
    public AltitudeUnit AltLimit_Top_Unit;

    public AltitudeReference AltLimit_Top_Ref;
    public Integer AltLimit_Bottom;
    public Integer getAltLimit_Bottom()
    {
        return (AltLimit_Top == null) ? 0 : AltLimit_Bottom;
    }
    public AltitudeUnit AltLimit_Bottom_Unit;
    public AltitudeReference AltLimit_Bottom_Ref;
    private org.locationtech.jts.geom.Geometry Geometry;
    private org.locationtech.jts.geom.LineString Line;
    public ArrayList<Coordinate> coordinates;
    private double latMini;
    private double latMaxi;
    private double longMini;
    private double longMaxi;
    private int violations;
    private String dbGeoJson;
    
    public double getLatMini() {
        return latMini;
    }

    public double getLatMaxi() {
        return latMaxi;
    }

    public double getLongMini() {
        return longMini;
    }

    public double getLongMaxi() {
        return longMaxi;
    }

    public int getViolations() {
        return violations;
    }

    public void setViolations(int violations) {
        this.violations = violations;
    }

    public String getDbGeoJson() {
        return dbGeoJson;
    }

    public void setDbGeoJson(String dbGeoJson) {
        this.dbGeoJson = dbGeoJson;
    }
            
    public Boolean checkGeometry()
    {
        if (coordinates == null) return false;
        if (coordinates.size()==0) return false;
        if (coordinates.size()>0) return true;
        return true;
    }

    public Geometry getGeometry()
    {
        if (Geometry == null) {
            if (coordinates.size()==0)
            {
                int i=1;
            }
            if ((coordinates.get(0).x != coordinates.get(coordinates.size()-1).x) ||
                    (coordinates.get(0).y != coordinates.get(coordinates.size()-1).y) )
            {
                coordinates.add(coordinates.get(0));
            }
            Coordinate[] c = coordinates.toArray(new Coordinate[coordinates.size()]);

            try {
                Geometry = new GeometryFactory().createPolygon(c);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return Geometry;
    }

    public LineString getLine()
    {
        if (Line == null)
        {
            Coordinate[] c = coordinates.toArray(new Coordinate[coordinates.size()]);

            try {
                Line = new GeometryFactory().createLineString(c);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return Line;
    }

    public Geometry getEnvelope()
    {
        if (Geometry == null) {
            Coordinate[] c = coordinates.toArray(new Coordinate[coordinates.size()]);
            Geometry = new GeometryFactory().createPolygon(c);
        }
        return Geometry.getEnvelope();
    }

    public void setGeometry(Geometry geometry)
    {
        this.Geometry = geometry;
        coordinates = new ArrayList<>(Arrays.asList(geometry.getCoordinates()));
    }    
        
    /**
     * From https://stackoverflow.com/questions/8520692/minimal-bounding-rectangle-with-jts
     * where enclosingEnvelopFromGeometry(Geometry geometry) is described
     * @param geometry
     * @return 
     */
    public void computeBoundingBox() {
        
        if (coordinates.size()> 3) {
            final Envelope envelope = new Envelope();
            final Geometry enclosingGeometry = getGeometry();
            final Coordinate[] enclosingCoordinates = enclosingGeometry.getCoordinates();
                for (Coordinate c : enclosingCoordinates) {
                    envelope.expandToInclude(c);
                }
            latMini = envelope.getMinY();
            latMaxi = envelope.getMaxY();
            longMini = envelope.getMinX();
            longMaxi = envelope.getMaxX();
        } else {
            // On prend une marge de 11 km soit 0.1 degrés
            latMini = (coordinates.get(0).y)-0.1;
            latMaxi = (coordinates.get(0).y)+0.1;
            longMini = (coordinates.get(0).x)-0.1;
            longMaxi = (coordinates.get(0).x)+0.1;
        }    
    }     
    
}
