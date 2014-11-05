package com.sintef.featureserver.wmts;

import ucar.unidata.geoloc.LatLonPoint;
import ucar.unidata.geoloc.LatLonPointImpl;

/**
 * Represents a WMTS TileMatrix.
 *
 * A TileMatrix is a set of tiles at a given "zoom levels".
 * @author arve
 */
public class TileMatrix implements Comparable<TileMatrix>{
    // Identifier for this TileMatrix. 0 is topmost one (furthest zoomed out).
    public int identifier;

    // How big is the area a pixel represents?
    // The scale denominator is defined with respect to a "standardized rendering pixel size" of
    // 0.28 mm × 0.28 mm (millimeters).
    public double scaleDenominator;

    // Top Left corner of the TileMatrix. This should be the same for every "Zoom level" of a
    // TileMatrixSet
    public LatLonPoint topLeftCorner;

    // Width in pixels of a single tile
    public int tileWidth;
    public int tileHeight;

    // Number of tiles in this TileMatrix. Should be 1 for the most zoomed out layer.
    public int matrixWidth;
    public int matrixHeight;

    @Override
    public int compareTo(final TileMatrix other) {
        // If this's scale denominator is higher, we have a lower level.
        return (int)other.scaleDenominator - (int)this.scaleDenominator;
    }

    public TileMatrix(
            final int identifier,
            final double scaleDenominator,
            final LatLonPoint topLeftCorner,
            final int tileWidth,
            final int tileHeight,
            final int matrixWidth,
            final int matrixHeight) {
        this.identifier = identifier;
        this.scaleDenominator = scaleDenominator;
        this.topLeftCorner = topLeftCorner;
        this.tileWidth = tileWidth;
        this.tileHeight = tileHeight;
        this.matrixWidth = matrixWidth;
        this.matrixHeight = matrixHeight;
    }

    public int getIdentifier() {
        return identifier;
    }

    public double getScaleDenominator() {
        return scaleDenominator;
    }

    public LatLonPoint getTopLeftCorner() {
        return topLeftCorner;
    }

    public int getTileWidth() {
        return tileWidth;
    }

    public int getTileHeight() {
        return tileHeight;
    }

    public int getMatrixWidth() {
        return matrixWidth;
    }

    public int getMatrixHeight() {
        return matrixHeight;
    }
}
