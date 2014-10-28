package com.sintef.featureserver.rs.features;

import com.sintef.featureserver.exception.BadRequestException;
import com.sintef.featureserver.exception.InternalServerException;
import com.sintef.featureserver.netcdf.AreaBounds;
import com.sintef.featureserver.netcdf.Feature;
import com.sintef.featureserver.netcdf.NetCdfManager;
import com.sintef.featureserver.util.ImageRenderer;
import com.sintef.featureserver.util.RsUtil;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import org.joda.time.DateTime;
import ucar.ma2.InvalidRangeException;
import ucar.unidata.geoloc.LatLonPoint;
import ucar.unidata.geoloc.LatLonPointImpl;

/**
 * Serves an image showing sea water velocity.
 */
@Path("feature/current-magnitude")
public class CurrentMagnitude {
    private final NetCdfManager netCdfManager;

    public CurrentMagnitude(@Context final NetCdfManager manager) {
        this.netCdfManager = manager;
    }

    /**
     *
     * @param startLat Latitude of top left corner
     * @param startLon Longitude of top left corner
     * @param endLat Latitude of bottom right corner
     * @param endLon Longitude of bottom right corner
     * @param depth Depth
     * @param time datetime
     * @return Image showing the current magnitude data in the region specified
     * @throws java.io.IOException
     * @throws BadRequestException if any parameters are missing or invalid. This
     * results in a 400 bad request response.
     */
    @GET
    @Path("area")
    public Response currentInRegionAtTime(
            @QueryParam("startLat") final Float startLat,
            @QueryParam("startLon") final Float startLon,
            @QueryParam("endLat") final Float endLat,
            @QueryParam("endLon") final Float endLon,
            @QueryParam("depth") final Float depth,
            @QueryParam("time") final String time)throws IOException {

        RsUtil.validateAreaQueryParams(startLat, startLon, endLat, endLon, depth, time);

        final DateTime dt;
        try {
            dt = DateTime.parse(time);
        } catch (final IllegalArgumentException e) {
            throw new BadRequestException("Time format not recognized", e);
        }

        final LatLonPoint topLeft = new LatLonPointImpl(startLat, startLon);
        final LatLonPoint bottomRight = new LatLonPointImpl(endLat, endLon);
        final AreaBounds bounds = new AreaBounds(topLeft, bottomRight, depth, dt);
        final double[][][] areaData;

        try {
			areaData = netCdfManager.getVectorArea(bounds, Feature.WATER_VELOCITY);
        } catch (final IOException e) {
            throw new InternalServerException("Could not read data file.", e);
        } catch (final InvalidRangeException e) {
            throw new BadRequestException("Invalid ranges provided.", e);
        }

        final double[][] areaMagnitudes = RsUtil.getMagnitudesFromVectors(areaData);

        final BufferedImage image = ImageRenderer.render(areaMagnitudes, Feature.CURRENT_MAGNITUDE, true);
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, "png", baos);
        final byte[] imageData = baos.toByteArray();
        return Response.ok(imageData).type("image/png").build();
    }

}
