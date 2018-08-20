/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package airspacelib;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.util.GeometricShapeFactory;

/**
 * Created by Rob Verhoef on 20-10-2015.
 */
public class GeometricHelpers {
    public static Geometry drawFullArc(LatLng start, LatLng end, LatLng center)
    {
        // Get Location class from LatLng Class

        // Get the distance to begin or end point. (distance in meters)
        // Multiply by 2 for diameter
        Double distance = center.distanceTo(end, "M") * 2;
        // Calculate Lateral and longitudinal degrees from the distance in meters
        Double latTraveledDeg = (1 / 110.54) * (distance / 1000);
        Double longTraveledDeg = (1 / (111.320 * Math.cos(Math.toRadians(center.latitude)))) * (distance/1000);

        // find the first angle to the begin point
        // The 0 degree line for the arc is horizontal and the first point is left
        // So recalculate the angles
        // angles should all be positive

        Double arcBegin = center.bearingTo(start);
        if (arcBegin<0) arcBegin = 360 - (arcBegin + 360); else arcBegin = 360 - arcBegin;
        Double arcEnd = center.bearingTo(end);
        if (arcEnd<0) arcEnd = 360 - (arcEnd + 360); else arcEnd = 360 - arcEnd;
        arcBegin = arcBegin + 90;
        arcEnd = arcEnd + 90;
        Double arcSize = arcBegin - arcEnd;
        // if the size is positive this is an counterclockwise arc (positive)
        // if the size is negative this is an clockwise arc

        GeometricShapeFactory geometricShapeFactory = new GeometricShapeFactory();
        geometricShapeFactory.setCentre(new Coordinate(center.longitude, center.latitude));
        geometricShapeFactory.setHeight(latTraveledDeg);
        geometricShapeFactory.setWidth(longTraveledDeg);
        geometricShapeFactory.setNumPoints(50);

        // because the arc is drawn counter clockwise the arcEnd is actually the startpoint

        if (arcSize>0)
            return geometricShapeFactory.createArcPolygon(Math.toRadians(arcEnd) , Math.toRadians(arcSize));
        else
            return geometricShapeFactory.createArcPolygon(Math.toRadians(arcBegin) , Math.toRadians(arcSize * -1));
    }

    public static ArrayList<Coordinate> drawCircle(LatLng center, Double radius)
    {
        // Get the distance to begin or end point. (distance in meters)
        // Multiply by 2 for diameter
        Double distance = (radius * 1853) * 2;
        // Calculate Lateral and longitudinal degrees from the distance in meters
        Double latTraveledDeg = (1 / 110.54) * (distance / 1000);
        Double longTraveledDeg = (1 / (111.320 * Math.cos(Math.toRadians(center.latitude)))) * (distance/1000);

        GeometricShapeFactory geometricShapeFactory = new GeometricShapeFactory();
        geometricShapeFactory.setCentre(new Coordinate(center.longitude, center.latitude));
        geometricShapeFactory.setHeight(latTraveledDeg);
        geometricShapeFactory.setWidth(longTraveledDeg);
        geometricShapeFactory.setNumPoints(50);

        org.locationtech.jts.geom.Polygon coordinates;
        coordinates = geometricShapeFactory.createEllipse();

        ArrayList<Coordinate> list = new ArrayList<Coordinate>(Arrays.asList(coordinates.getCoordinates()));
        return list;
    }

    public static ArrayList<Coordinate> drawArc(Double startAngle, Double endAngle, Double radiusNM, LatLng center, Boolean cw)
    {
        // Calculate distance (diameter) in meters
        Double distance = (radiusNM * 1851) * 2;
        // Calculate Lateral and longitudinal degrees from the distance in meters
        Double latTraveledDeg = (1 / 110.54) * (distance / 1000);
        Double longTraveledDeg = (1 / (111.320 * Math.cos(Math.toRadians(center.latitude)))) * (distance/1000);

        Double arcBegin = 360 - startAngle + 90;
        Double arcEnd = 360 - endAngle + 90;
        Double arcSize =  arcEnd - arcBegin;

        if ((arcSize>0) && cw) arcSize = 360 - arcSize;
        else
        if ((arcSize<0) && cw) arcSize = -1 * arcSize;
        else
        if ((arcSize<0) && !cw) arcSize = 360 + arcSize;

        GeometricShapeFactory geometricShapeFactory = new GeometricShapeFactory();
        geometricShapeFactory.setCentre(new Coordinate(center.longitude, center.latitude));
        geometricShapeFactory.setHeight(latTraveledDeg);
        geometricShapeFactory.setWidth(longTraveledDeg);
        geometricShapeFactory.setNumPoints(50);

        // because the arc is drawn counter clockwise the arcEnd is actually the startpoint
        Coordinate[] coordinates;
        if (cw)
            coordinates =  geometricShapeFactory.createArc(Math.toRadians(arcEnd) , Math.toRadians(arcSize)).getCoordinates();
        else
            coordinates = geometricShapeFactory.createArc(Math.toRadians(arcBegin), Math.toRadians(arcSize)).getCoordinates();

        ArrayList<Coordinate> list = new ArrayList<Coordinate>(Arrays.asList(coordinates));
        if (cw) Collections.reverse(list);
        return list;
    }

    public static ArrayList<Coordinate> drawArc(LatLng start, LatLng end, LatLng center, Boolean cw)
    {
        // Get Location class from LatLng Class

        // Get the distance to begin or end point. (distance in meters)
        // Multiply by 2 for diameter
        Double distance = center.distanceTo(end, "M") * 2;
        // Calculate Lateral and longitudinal degrees from the distance in meters
        Double latTraveledDeg = (1 / 110.54) * (distance / 1000);
        Double longTraveledDeg = (1 / (111.320 * Math.cos(Math.toRadians(center.latitude)))) * (distance/1000);

        // find the first angle to the begin point
        // The 0 degree line for the arc is horizontal and the first point is left
        // So recalculate the angles
        // angles should all be positive

        //Double b = center.bearingTo(start);
        //Double e = center.bearingTo(end);

        Double arcBegin = 360 - center.bearingTo(start) + 90;
        Double arcEnd = 360 - center.bearingTo(end) + 90;
        Double arcSize =  arcEnd - arcBegin;

        if ((arcSize>0) && cw) arcSize = 360 - arcSize;
        else
        if ((arcSize<0) && cw) arcSize = -1 * arcSize;
        else
        if ((arcSize<0) && !cw) arcSize = 360 + arcSize;

        //if (arcBegin<0) arcBegin = 360 - (arcBegin + 360); else arcBegin = 360 - arcBegin;
        //if (arcEnd<0) arcEnd = 360 - (arcEnd + 360); else arcEnd = 360 - arcEnd;

        // if the size is positive this is an counterclockwise arc (positive)
        // if the size is negative this is an clockwise arc
        //if (cw) arcSize = arcSize * -1;

        GeometricShapeFactory geometricShapeFactory = new GeometricShapeFactory();
        geometricShapeFactory.setCentre(new Coordinate(center.longitude, center.latitude));
        geometricShapeFactory.setHeight(latTraveledDeg);
        geometricShapeFactory.setWidth(longTraveledDeg);
        geometricShapeFactory.setNumPoints(50);

        // because the arc is drawn counter clockwise the arcEnd is actually the startpoint
        Coordinate[] coordinates;
//        if (arcSize>0)
//        //if (!cw)
//            coordinates =  geometricShapeFactory.createArc(Math.toRadians(arcEnd) , Math.toRadians(arcSize)).getCoordinates();
        if (cw)
            coordinates =  geometricShapeFactory.createArc(Math.toRadians(arcEnd) , Math.toRadians(arcSize)).getCoordinates();
        else
            coordinates = geometricShapeFactory.createArc(Math.toRadians(arcBegin), Math.toRadians(arcSize)).getCoordinates();

        ArrayList<Coordinate> list = new ArrayList<Coordinate>(Arrays.asList(coordinates));
        if (cw) Collections.reverse(list);
        return list;
    }
}

