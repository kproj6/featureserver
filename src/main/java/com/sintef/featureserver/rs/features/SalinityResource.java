package com.sintef.featureserver.rs.features;

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
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.validation.constraints.NotNull;
import javax.ws.rs.core.Response;
import org.joda.time.DateTime;
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

    @Path("salinity")
    @GET
    @Produces("image/png")
    public Response salinityInRegionAtTime(
            @NotNull @QueryParam("startLat") final float startLat,
            @NotNull @QueryParam("startLon") final float startLon,
            @NotNull @QueryParam("endLat") final float endLat,
            @NotNull @QueryParam("endLon") final float endLon,
            @NotNull @QueryParam("depth") final float depth,
            @NotNull @QueryParam("time") final String time)throws IOException {

        final LatLonPoint upperLeft = new LatLonPointImpl(startLat, startLon);
        final LatLonPoint lowerRight = new LatLonPointImpl(endLat, endLon);
        final DateTime dt = DateTime.parse(time);
        final AreaBounds bounds = new AreaBounds(upperLeft, lowerRight, depth, dt);
        final double[][] areaData;
        try {
            areaData = netCdfManager.readArea(bounds, "salinity");
        } catch (final IOException e) {
            // @TODO(Arve): Return json as body on these errors as well.
            return Response
                    .status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Could not read data file. " + e.getMessage())
                    .build();
        } catch (final InvalidRangeException e) {
            return Response
                    .status(Response.Status.BAD_REQUEST)
                    .entity("Invalid ranges provided. " + e.getMessage())
                    .build();
        }


        final BufferedImage image = ImageRenderer.render(areaData, Feature.SALINITY, true);
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, "png", baos);
        final byte[] imageData = baos.toByteArray();
        return Response.ok(imageData).build();
    }
}
