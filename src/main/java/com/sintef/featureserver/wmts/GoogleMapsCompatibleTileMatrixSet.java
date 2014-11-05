package com.sintef.featureserver.wmts;

import java.util.ArrayList;
import java.util.List;
import ucar.unidata.geoloc.LatLonPoint;
import ucar.unidata.geoloc.LatLonRect;

/**
 * A TileMatrixSet using the standard `GoogleMapsCompatible` well-known set of values.
 * Projection is WGS 84 / Pseudo-Mercator.
 *
 * Contains 18 zoom levels, ranging from the whole world in a single tile, down to ~0.5m pixel size.
 * Each level scale is the previous level divided by two (in x and y).
 *
 * Level  Scale Denom (           Pixel size (meters)
 * 0    559082264.0287178     156543.0339280410
 * 1    279541132.0143589     78271.51696402048
 * 2    139770566.0071794     39135.75848201023
 * 3    69885283.00358972     19567.87924100512
 * 4    34942641.50179486     9783.939620502561
 * 5    17471320.75089743     4891.969810251280
 * 6    8735660.375448715     2445.984905125640
 * 7    4367830.187724357     1222.992452562820
 * 8    2183915.093862179     611.4962262814100
 * 9    1091957.546931089     305.7481131407048
 * 10   545978.7734655447     152.8740565703525
 * 11   272989.3867327723     76.43702828517624
 * 12   136494.6933663862     38.21851414258813
 * 13   68247.34668319309     19.10925707129406
 * 14   34123.67334159654     9.554628535647032
 * 15   17061.83667079827     4.777314267823516
 * 16   8530.918335399136     2.388657133911758
 * 17   4265.459167699568     1.194328566955879
 * 18   2132.729583849784     0.5971642834779395
 *
 *
 *
 * @author arve
 */
public class GoogleMapsCompatibleTileMatrixSet {
    private static final double MAX_SCALE_DENOM = 559082264.0287178;
    public static final int TILE_SIZE_PX= 256;
    public static final String ID = "GoogleMapsCompatible";
    public static final String ABSTRACT = "the wellknown 'GoogleMapsCompatible' " +
            "tile matrix set defined by OGC WMTS specification";
    public static final String CRS = "urn:ogc:def:crs:EPSG:6.18:3:3857";
    public static final String SCALE_SET = "urn:ogc:def:wkss:OGC:1.0:GoogleMapsCompatible";

    private final List<TileMatrix> tileMatrices;
    private double lowestPixelScale;
    private final LatLonRect bbox;

    public GoogleMapsCompatibleTileMatrixSet(final double sourceDataPixelSize,
            final LatLonRect boundingBox) {
        final double lowestScaleDenominator = sourceDataPixelSize / 0.00028;
        this.bbox = boundingBox;
        tileMatrices = generateTileMatrices(lowestScaleDenominator, bbox.getUpperLeftPoint());
    }

    private List<TileMatrix> generateTileMatrices(final double lowestScaleDenominator,
            final LatLonPoint topLeftCorner) {

        final List<TileMatrix> result = new ArrayList<TileMatrix>();
        // Start at level 0, fully zoomed out. Continue to divide by 2 until we have reached our
        // target resolution.
        int scaleMatrixIdentifier = 0;
        int numberOfTiles = 1;
        // @Todo (Arve) I suspect this ends a level too early. Should stop at level 10, not 9.
        for (double scaleDenom = MAX_SCALE_DENOM; scaleDenom > lowestScaleDenominator; scaleDenom /= 2) {
            final TileMatrix tileMatrix = new TileMatrix(
                    scaleMatrixIdentifier,
                    scaleDenom,
                    topLeftCorner,
                    TILE_SIZE_PX,
                    TILE_SIZE_PX,
                    numberOfTiles,
                    numberOfTiles
            );
            scaleMatrixIdentifier += 1;
            numberOfTiles = numberOfTiles * 2;
            result.add(tileMatrix);
        }
        return result;
    }

    public List<TileMatrix> getTileMatrices() { return tileMatrices; }

    public LatLonRect getBoundingBox() { return bbox; }

    public static double getMaxScaleDenom() { return MAX_SCALE_DENOM; }

    public static int getTileSizePx() { return TILE_SIZE_PX; }

    public static String getId() { return ID; }

    public static String getAbstract() { return ABSTRACT; }

    public static String getCrs() { return CRS; }

    public static String getScaleSet() { return SCALE_SET; }
}
