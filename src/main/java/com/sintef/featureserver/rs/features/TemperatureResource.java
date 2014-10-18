package com.sintef.featureserver.rs.features;

import com.sintef.featureserver.netcdf.AreaBounds;
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
 * Handles all requests for temperature.
 * 
 * This class contains processing of all /feature/temperature requests.
 */
@Path("temperature")
public class TemperatureResource {
	
	/**
	 * Back reference to NetCdfManager class for accessing information from NetCDF files.
	 */
    private final NetCdfManager netCdfManager;

    /**
     * Creates an instance of TemperatureResource class.
     * 
     * Creates a new instance of TemperatureResource class and sets back reference
     * for NetCdfManager.
     * 
     * @param manager Reference to NetCdfManager class for handling NetCDF files.
     */
    public TemperatureResource(@Context final NetCdfManager manager) {
        this.netCdfManager = manager;
    }

    /**
     * Handles request for an image representing temperatures in given area.
     * 
     * @param startLat Latitude of top left point that should be displayed.
     * @param startLon Longitude of top left point that should be displayed.
     * @param endLat Latitude of bottom right point that should be displayed.
     * @param endLon Longitude of bottom right point that should be displayed.
     * @param depth	Specifies which depth should be displayed.
     * @param time Specifies time that should be displayed.
     * @return Returns HTTP response containing either image with requested data,
     * 			or JSON with error description in case of failure.
     * @throws IOException
     */
    @GET
    @Path("image")
    @Produces("image/png")
    public Response temperatureInRegionAtTime(
            @NotNull @QueryParam("startLat") final float startLat,
            @NotNull @QueryParam("startLon") final float startLon,
            @NotNull @QueryParam("endLat") final float endLat,
            @NotNull @QueryParam("endLon") final float endLon,
            @NotNull @QueryParam("depth") final float depth,
            @NotNull @QueryParam("time") final String time)throws IOException {

        final LatLonPoint topLeft = new LatLonPointImpl(startLat, startLon);
        final LatLonPoint bottomRight = new LatLonPointImpl(endLat, endLon);
        final DateTime dt = DateTime.parse(time);
        final AreaBounds bounds = new AreaBounds(topLeft, bottomRight, depth, dt);
        final double[][] areaData;
        
        try {
            areaData = netCdfManager.readArea(bounds, "temperature");
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

        // @TODO(Arve) Image size
        final BufferedImage image = ImageRenderer.render(areaData);
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, "png", baos);
        final byte[] imageData = baos.toByteArray();
        return Response.ok(imageData).build();
    }
}
