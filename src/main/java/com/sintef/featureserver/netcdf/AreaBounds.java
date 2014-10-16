package com.sintef.featureserver.netcdf;

import org.joda.time.DateTime;
import ucar.unidata.geoloc.LatLonPoint;
import ucar.unidata.geoloc.LatLonRect;

/**
 * Object representing the bounds the user wants data for.
 * @author arve
 */
public class AreaBounds {
    final LatLonPoint upperLeft;
    final LatLonPoint lowerRight;
    private final float depth;
    private final DateTime time;

    /**
     * Preferred constructor
     * @param upperLeft Upper left corner of the area
     * @param lowerRight Lower left corner of the area
     * @param time
     * @param depth depth in meters.
     */
    public AreaBounds(
            final LatLonPoint upperLeft,
            final LatLonPoint lowerRight,
            final float depth,
            final DateTime time) {
        this.upperLeft = upperLeft;
        this.lowerRight = lowerRight;
        this.depth = depth;
        this.time = time;
    }


    public LatLonRect getRect() { return new LatLonRect(upperLeft, lowerRight); }

    public float getDepth() { return depth; }

    public DateTime getTime() { return time; }

    public String toString(){
        return "rect: " + getRect().toString()
                + "depth" + depth
                + "date" + time.toString();

    }
}
