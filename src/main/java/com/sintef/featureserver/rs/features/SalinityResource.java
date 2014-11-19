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
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.joda.time.DateTime;
import org.json.JSONObject;
import ucar.ma2.InvalidRangeException;
import ucar.unidata.geoloc.LatLonPoint;
import ucar.unidata.geoloc.LatLonPointImpl;

/**
 * Serves an image showing Salinity.
 */
@Path("feature/salinity")
public class SalinityResource {
    private final NetCdfManager netCdfManager;

    public SalinityResource(@Context final NetCdfManager manager) { this.netCdfManager = manager; }

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
    @GET
    @Path("area")
    public Response salinityInRegionAtTime(
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
        final double[][] areaData;
        try {
            areaData = netCdfManager.getScalarArea(bounds, Feature.SALINITY);
        } catch (final IOException e) {
            throw new InternalServerException("Could not read data file.", e);
        } catch (final InvalidRangeException e) {
            throw new BadRequestException("Invalid ranges provided.", e);
        }

        final BufferedImage image = ImageRenderer.render(areaData, Feature.SALINITY, false);
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, "png", baos);
        final byte[] imageData = baos.toByteArray();
        return Response.ok(imageData).type("image/png").build();
    }

    @GET
    @Path("profile")
    @Produces(MediaType.APPLICATION_JSON)
    public Response salinityProfile(
            @QueryParam("lat") final Float latitude,
            @QueryParam("lon") final Float longitude,
            @QueryParam("time") final String time) throws IOException {
        RsUtil.validateProfileQueryParams(latitude, longitude, time);
        final DateTime dt;
        try {
            dt = DateTime.parse(time);
        } catch (final IllegalArgumentException e) {
            throw new BadRequestException("Time format not recognized", e);
        }
        final LatLonPoint location = new LatLonPointImpl(latitude, longitude);
        final double[] depthProfile;
        try {
            depthProfile = netCdfManager.readDepthProfile(location, dt, Feature.SALINITY);
        } catch (final InvalidRangeException e) {
            throw new BadRequestException("No data for location");
        }
        final JSONObject json = new JSONObject()
                .put("feature", "salinity")
                .put("profile", RsUtil.doubleArrayToJson(depthProfile))
                .put("missingValuePlaceholder", "-1")
                .put("location", new JSONObject()
                                .put( "latitude", latitude)
                                .put("longitude", longitude)
                                .put("time", time));
        return Response.ok(json.toString()).build();
    }


}
