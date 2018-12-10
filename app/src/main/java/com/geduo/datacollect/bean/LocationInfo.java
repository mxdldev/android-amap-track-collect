package com.geduo.datacollect.bean;

/**
 * Description: <><br>
 * Author:      gxl<br>
 * Date:        2018/12/6<br>
 * Version:     V1.0.0<br>
 * Update:     <br>
 */
public class LocationInfo {
    private double lat;
    private double lon;

    public LocationInfo(double lat, double lon) {
        this.lat = lat;
        this.lon = lon;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLon() {
        return lon;
    }

    public void setLon(double lon) {
        this.lon = lon;
    }
}
