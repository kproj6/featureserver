package com.sintef.featureserver.rs.wmts;

import com.sintef.featureserver.netcdf.NetCdfManager;
import com.sintef.featureserver.wmts.GoogleMapsCompatibleTileMatrixSet;
import com.sintef.featureserver.wmts.TileMatrix;
import java.io.IOException;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import ucar.unidata.geoloc.LatLonRect;

/**
 * Serves tiles through the WMTS protocol
 *
 * @author arve
 */

@Path("$wmtsBaseUrl/tile/1.0.0/{TileMatrixSet}/{TileMatrix}/{TileRow}/{TileCol}.png")
@Produces("image/png")
public class TileResource {
    private final NetCdfManager netCdfManager;

    public TileResource(@Context final NetCdfManager netCdfManager) throws IOException {
        this.netCdfManager = netCdfManager;
        final GoogleMapsCompatibleTileMatrixSet tileMatrixSet
                = new GoogleMapsCompatibleTileMatrixSet(
                netCdfManager.getResolution(),
                netCdfManager.getBoundingBox());

    }

    public Response getTile(
            @PathParam("TileMatrixSet") final String variable,
            @PathParam("TileMatrix") final int zoomLevel,
            @PathParam("TileRow") final int tileRow,
            @PathParam("TileMatrix") final int TileCol) throws IOException {

        final double resolution = netCdfManager.getResolution();
        final LatLonRect bbox = netCdfManager.getBoundingBox();
        final GoogleMapsCompatibleTileMatrixSet tileMatrixSet
                = new GoogleMapsCompatibleTileMatrixSet(resolution, bbox);
        final TileMatrix tileMatrix = tileMatrixSet.getTileMatrices().get(zoomLevel);


        return Response.ok().build();
    }

}
