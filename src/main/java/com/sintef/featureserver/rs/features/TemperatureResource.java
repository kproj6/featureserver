/**
 * Filename: TemperatureResource.java
 * Package: com.sintef.featureserver.rs.features
 *
 * Created: 17 Oct 2014
 * 
 * Author: Ondřej Hujňák
 * Licence:
 */
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
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.joda.time.DateTime;
import org.json.JSONArray;
import org.json.JSONStringer;

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
    public TemperatureResource(
    		@Context final NetCdfManager manager) {
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
    @Path("area")
    public Response temperatureInRegionAtTime (
            @QueryParam("startLat") final Float startLat,
            @QueryParam("startLon") final Float startLon,
            @QueryParam("endLat") final Float endLat,
            @QueryParam("endLon") final Float endLon,
            @QueryParam("depth") final Float depth,
            @QueryParam("time") final String time) throws IOException {
    	
    	validateQueryParams(startLat, startLon, endLat, endLon, depth, time);
    	
    	final DateTime dt = DateTime.parse(time);
        final LatLonPoint topLeft =
        		new LatLonPointImpl(startLat, startLon);
        final LatLonPoint bottomRight = 
        		new LatLonPointImpl(endLat, endLon);
        final AreaBounds bounds = 
        		new AreaBounds(topLeft, bottomRight, depth, dt);
        final double[][] areaData;
        
        try {
            areaData = netCdfManager.readArea(bounds, "temperature");
        } catch (final IOException e) {
        	final String errorMessage = new JSONStringer()
        			.object()
        			.key("status").value("Internal Server Error")
        			.key("message").value("Could not read data file. " + e.getMessage())
					.endObject()
		            .toString();
        	return Response
                    .status(Response.Status.INTERNAL_SERVER_ERROR)
                    .type(MediaType.APPLICATION_JSON)
                    .entity(errorMessage)
                    .build();
        } catch (final InvalidRangeException e) {
        	final String errorMessage = new JSONStringer()
			.object()
			.key("status").value("Bad Request")
			.key("message").value("Invalid ranges provided. " + e.getMessage())
			.endObject()
            .toString();
			return Response
		            .status(Response.Status.BAD_REQUEST)
		            .type(MediaType.APPLICATION_JSON)
		            .entity(errorMessage)
		            .build();
        }

        // @TODO(Arve) Image size
        final BufferedImage image = ImageRenderer.render(areaData, Feature.TEMPERATURE, true);
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, "png", baos);
        final byte[] imageData = baos.toByteArray();
        return Response.ok(imageData).type("image/png").build();
    }

	/**
	 * @param startLat
	 * @param startLon
	 * @param endLat
	 * @param endLon
	 * @param depth
	 * @param time
	 */
	private void validateQueryParams(
			final Float startLat, 
			final Float startLon,
			final Float endLat,
			final Float endLon,
			final Float depth,
			final String time) {
		
		boolean dirty = false;
		final JSONArray missingFields = new JSONArray();
		
		if(startLat == null) { dirty = true; missingFields.put("startLat"); }
		if(startLon == null) { dirty = true; missingFields.put("startLon"); }
		if(endLat == null) { dirty = true; missingFields.put("endLat"); }
		if(endLon == null) { dirty = true; missingFields.put("endLon"); }
		if(depth == null) { dirty = true; missingFields.put("depth"); }
		if(time == null) { dirty = true; missingFields.put("time"); }
		
		if (dirty) {
			final String errorMessage = new JSONStringer()
		            .object()
		            .key("missingFields").value(missingFields)
		            .endObject()
		            .toString();
			throw new IllegalArgumentException(errorMessage);
		}
		
	}
}
