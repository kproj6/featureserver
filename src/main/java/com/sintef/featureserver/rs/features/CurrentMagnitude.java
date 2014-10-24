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
     * @param startLon Longditude of top left coner
     * @param endLat Latidtude of bottom right corner
     * @param endLon Longditude of bottom right corner
     * @param depth Depth
     * @param time datetime
     * @return Image showing the current magnitude data in the region specified
     * @throws java.io.IOException
     * @throws BadRequestException if any parameters are missing or invalid. This
     * results in a 400 bad request response.
     */
    @GET
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

        final LatLonPoint upperLeft = new LatLonPointImpl(startLat, startLon);
        final LatLonPoint lowerRight = new LatLonPointImpl(endLat, endLon);
        final AreaBounds bounds = new AreaBounds(upperLeft, lowerRight, depth, dt);
        final double[][] eastVelocity;
        final double[][] northVelocity;

        try {
            eastVelocity = netCdfManager.readArea(bounds, "u_east");
            northVelocity = netCdfManager.readArea(bounds, "v_north");
        } catch (final IOException e) {
            throw new InternalServerException("Could not read data file.", e);
        } catch (final InvalidRangeException e) {
            throw new BadRequestException("Invalid ranges provided.", e);
        }
        final double[][] magnitude = magnitudeArray(eastVelocity, northVelocity);
        final BufferedImage image = ImageRenderer.render(magnitude, Feature.CURRENT_MAGNITUDE, true);
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, "png", baos);
        final byte[] imageData = baos.toByteArray();
        return Response.ok(imageData).type("image/png").build();
    }

    private double[][] magnitudeArray(final double[][] east, final double[][] north){
        final int e_width = east[0].length;
        final int e_height = east.length;
        final int n_width = north[0].length;
        final int n_height = north.length;
        if (e_width != n_width || e_height != n_height) {
            throw new InternalServerException("Malformed source data. Variables u_east and " +
                    "v_north do not contain the same amount of data points for the selected " +
                    "region!");
        }
        final double[][] result = new double[e_height][e_width];
        for (int y = 0; y < e_height; y++)
            for (int x = 0; x < e_width; x++) {
                result[y][x] = Math.sqrt(east[y][x] * east[y][x] + north[y][x] * north[y][x]);
            }
        return result;
    }
}
