/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package airspacelib;

/**
 *
 * @author gil
 */
public class LatLng {
  public LatLng()
    {
        latitude = 0;
        longitude = 0;
    }

    public LatLng(double latitude, double longitude)
    {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public final double latitude;
    public final double longitude;

    public boolean equals(LatLng latLng)
    {
        return ((this.longitude == latLng.longitude) && (this.latitude == latLng.latitude));
    }

    public double bearingTo(LatLng to){
        double longitude1 = this.longitude;
        double longitude2 = to.longitude;
        double latitude1 = Math.toRadians(this.latitude);
        double latitude2 = Math.toRadians(to.latitude);
        double longDiff= Math.toRadians(longitude2-longitude1);
        double y= Math.sin(longDiff)*Math.cos(latitude2);
        double x=Math.cos(latitude1)*Math.sin(latitude2)-Math.sin(latitude1)*Math.cos(latitude2)*Math.cos(longDiff);

        return (Math.toDegrees(Math.atan2(y, x))+360)%360;
    }

    public double distanceTo(LatLng to, String unit) {
        double theta = this.longitude - to.longitude;
        double dist = Math.sin(Math.toRadians(this.latitude)) *
                Math.sin(Math.toRadians(to.latitude)) +
                Math.cos(Math.toRadians(this.latitude)) *
                        Math.cos(Math.toRadians(to.latitude)) *
                        Math.cos(Math.toRadians(theta));

        dist = Math.acos(dist);
        dist = Math.toDegrees(dist);
        dist = dist * 60 * 1.1515;
        if (unit == "K") {
            dist = dist * 1.609344;
        } else if (unit == "N") {
            dist = dist * 0.8684;
        } else if (unit == "M") {
            dist = dist * 1609.344;
        }

        return (dist);
    }    
}
