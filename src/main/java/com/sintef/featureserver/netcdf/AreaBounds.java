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
    private final Float depth;
    private final DateTime time;

    /**
     * Preferred constructor
     *
     * Creates bounds in 4D.
     *
     * @param upperLeft Upper left corner of the area
     * @param lowerRight Lower left corner of the area
     * @param time
     * @param depth depth in meters.
     */
    public AreaBounds(
            final LatLonPoint upperLeft,
            final LatLonPoint lowerRight,
            final Float depth,
            final DateTime time) {
        this.upperLeft = upperLeft;
        this.lowerRight = lowerRight;
        this.depth = depth;
        this.time = time;
    }

    /**
     * Alternative constructor
     *
     * Creates bounds in 2D.
     *
     * @param upperLeft Upper left corner of the area
     * @param lowerRight Lower left corner of the area
     */
    public AreaBounds(
            final LatLonPoint upperLeft,
            final LatLonPoint lowerRight) {
        this.upperLeft = upperLeft;
        this.lowerRight = lowerRight;
        this.depth = null;
        this.time = null;
    }


    public LatLonRect getRect() { return new LatLonRect(upperLeft, lowerRight); }

    public float getDepth() { return depth; }

    public DateTime getTime() { return time; }

	public String toString(){
		if (depth == null) {
			return "rect: " + getRect().toString();
		} else {
			return "rect: " + getRect().toString()
				+ "depth" + depth
				+ "date" + time.toString();
		}
	}
}
