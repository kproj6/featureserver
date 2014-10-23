package com.sintef.featureserver.rs.features;

import com.sintef.featureserver.exception.BadRequestException;
import com.sintef.featureserver.exception.InternalServerException;
import com.sintef.featureserver.netcdf.AreaBounds;
import com.sintef.featureserver.netcdf.Feature;
import com.sintef.featureserver.netcdf.NetCdfManager;
import com.sintef.featureserver.util.ImageRenderer;
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
import org.json.JSONArray;
import org.json.JSONObject;
import ucar.ma2.InvalidRangeException;
import ucar.unidata.geoloc.LatLonPoint;
import ucar.unidata.geoloc.LatLonPointImpl;

/**
 * Serves an image showing Salinity.
 * test with http://localhost:10100/feature/salinity?startLat=65.24&startLon=7.56&endLat=65
 * .42&endLon=9.542&depth=2&time=2013-08-05
 */
@Path("feature")
public class SalinityResource {
    private final NetCdfManager netCdfManager;

    public SalinityResource(@Context final NetCdfManager manager) {
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
     * @return Image showing the salinity data in the region specified
     * @throws IOException
     * @throws java.lang.IllegalArgumentException if any parameters are missing or invalid. This
     * results in a 400 bad request response.
     */
    @Path("salinity")
    @GET
    public Response salinityInRegionAtTime(
            @QueryParam("startLat") final Float startLat,
            @QueryParam("startLon") final Float startLon,
            @QueryParam("endLat") final Float endLat,
            @QueryParam("endLon") final Float endLon,
            @QueryParam("depth") final Float depth,
            @QueryParam("time") final String time)throws IOException {

        validateQueryParams(startLat, startLon, endLat, endLon, depth, time);

        final DateTime dt;
        try {
            dt = DateTime.parse(time);
        } catch (final IllegalArgumentException e) {
            throw new BadRequestException("Time format not recognized", e);
        }

        final LatLonPoint upperLeft = new LatLonPointImpl(startLat, startLon);
        final LatLonPoint lowerRight = new LatLonPointImpl(endLat, endLon);
        final AreaBounds bounds = new AreaBounds(upperLeft, lowerRight, depth, dt);
        final double[][] areaData;
        try {
            areaData = netCdfManager.readArea(bounds, "salinity");
        } catch (final IOException e) {
            throw new InternalServerException("Could not read data file.", e);
        } catch (final InvalidRangeException e) {
            throw new BadRequestException("Invalid ranges provided.", e);
        }

        final BufferedImage image = ImageRenderer.render(areaData, Feature.SALINITY, true);
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, "png", baos);
        final byte[] imageData = baos.toByteArray();
        return Response.ok(imageData).type("image/png").build();
    }

    /**
     * Validates that required query parameters are set. Shows error listing all missing fields if
     * something is missing.
     * @throws IllegalArgumentException if any parameter is missing.
     */
    private void validateQueryParams(
            final Float startLat,
            final Float startLon,
            final Float endLat,
            final Float endLon,
            final Float depth,
            final String time) {
        final JSONArray missingFields = new JSONArray();
        if(startLat == null) { missingFields.put("startLat"); }
        if(startLon == null) { missingFields.put("startLon"); }
        if(endLat == null) { missingFields.put("endLat"); }
        if(endLon == null) { missingFields.put("endLon"); }
        if(depth == null) { missingFields.put("depth"); }
        if(time == null) { missingFields.put("time"); }

        if( missingFields.length() != 0) {
            final JSONObject errorObject = new JSONObject().put( "missingFields", missingFields);
            throw new BadRequestException("Missing query parameters in url.", errorObject);
        }
    }
}
