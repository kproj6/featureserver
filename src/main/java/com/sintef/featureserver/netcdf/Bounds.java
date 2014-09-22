package com.sintef.featureserver.netcdf;

/**
 * Object representing the bounds the user wants data for.
 * Currently the values are only data indices. This should of course be fixed to allow the user
 * to input coordinates and proper dates instead (ISO8601)
 *
 * @author arve
 */
public class Bounds {
    private final int startX;
    private final int startY;
    private final int endX;
    private final int endY;
    private final int depth;
    private final int time;

    public Bounds(
            final int time,
            final int depth,
            final int startX,
            final int endX,
            final int startY,
            final int endY) {
        this.startX = startX;
        this.startY = startY;
        this.endX = endX;
        this.endY = endY;
        this.depth = depth;
        this.time = time;
    }

    public int getStartX() { return startX; }

    public int getStartY() { return startY; }

    public int getEndX() { return endX; }

    public int getEndY() { return endY; }

    public int getDepth() { return depth; }

    public int getTime() { return time; }

    public String toString(){
        return "startX:" + startX
                + "startY:" + startY
                + "endX:" + endX
                + "endY:" + endY
                + "depth:" + depth
                + "time: " + time;
    }
}
