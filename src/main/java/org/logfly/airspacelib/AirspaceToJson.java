/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.logfly.airspacelib;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;

/**
 *
 * @author gil
 * 
 *  Rob Verhoef used javax.json
 *  Existing code is adpated to json-simple
 *  Properties are expanded
 * 
 */
public class AirspaceToJson {
    
    private String filePath;    
    private JSONObject airspaceJsonObject;
    private String json;
    
    public AirspaceToJson (String Path) {
        filePath =Path;
    }

    public String getJson() {
        return json;
    }
        
    
    public void InsertAirspaces(Airspaces airspaces) {
        
        JSONObject airspaceJsonObjectbuilder = new JSONObject();
        airspaceJsonObjectbuilder.put("type", "FeatureCollection");   
        JSONArray airspacesArray = new JSONArray();

        for(Airspace airspace: airspaces)
        {
            JSONObject feature = new JSONObject();
            feature.put("type", "Feature");
            JSONObject properties = new JSONObject();
            properties.put("stroke", "#FF0000");
            properties.put("stroke-width", 2);
            properties.put("stroke-opacity", 1);
            properties.put("fill", "#FF0000");
            properties.put("fill-opacity", 0.4);
            properties.put("Class", (airspace.Category == null)? "UNK" : airspace.Category.toString());            
            properties.put("Name", (airspace.Name == null)? "UNK" : airspace.Name );
            properties.put("Floor",(airspace.AltLimit_Bottom == null)? "UNK" : airspace.AltLimit_Bottom );
            properties.put("Ceiling",(airspace.AltLimit_Top == null)? "UNK" : airspace.AltLimit_Top );
            feature.put("properties", properties);
            
            
            JSONArray polygons = new JSONArray();
            JSONArray coordinates = new JSONArray();
            for (Coordinate c: airspace.coordinates)
            {
                JSONArray coordinate = new JSONArray();
                coordinate.add(0, c.x);
                coordinate.add(1, c.y);
                coordinates.add(coordinate);
            }

            polygons.add(coordinates);

            JSONObject geometry = new JSONObject();
            geometry.put("type", "Polygon");
            geometry.put("coordinates", polygons);

            feature.put("geometry",geometry);
            
            System.out.println(feature.toString());
            
            System.out.println("Max x "+airspace.getLongMaxi());   // Longitude
            System.out.println("Min x "+airspace.getLongMini());
            System.out.println("Max y "+airspace.getLatMaxi());  // Latitude
            System.out.println("Min y "+airspace.getLatMini());         



            airspacesArray.add(feature);

        }
        
        airspaceJsonObjectbuilder.put("features", airspacesArray);
        airspaceJsonObject = airspaceJsonObjectbuilder;
        json = airspaceJsonObject.toString();
    }    
    
    private Envelope enclosingEnvelopFromGeometry(Geometry geometry) {
        
        final Envelope envelope = new Envelope();
        final Geometry enclosingGeometry = geometry.getEnvelope();
        final Coordinate[] enclosingCoordinates = enclosingGeometry.getCoordinates();
        for (Coordinate c : enclosingCoordinates) {
            envelope.expandToInclude(c);
        }
        return envelope;
    }     
    
}
