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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sintef.featureserver.datatype.FloatParam;
import com.sintef.featureserver.datatype.TimeParam;
import com.sintef.featureserver.netcdf.AreaBounds;
import com.sintef.featureserver.netcdf.NetCdfManager;
import com.sintef.featureserver.util.ImageRenderer;
import com.sintef.featureserver.util.JSONMsg;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

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
    public Response temperatureInRegionAtTime (
            @QueryParam("startLat") final FloatParam startLat,
            @QueryParam("startLon") final FloatParam startLon,
            @QueryParam("endLat") final FloatParam endLat,
            @QueryParam("endLon") final FloatParam endLon,
            @QueryParam("depth") final FloatParam depth,
            @QueryParam("time") final TimeParam time) throws IOException {
    	
    	if (startLat == null || startLon == null ||
    		endLat == null || endLon == null ||
    		depth == null || time == null) {
    		
    		final ObjectMapper mapper = new ObjectMapper();
			JSONMsg msg = new JSONMsg(
					JSONMsg.Status.ERROR,
					"Missing one or more parameters.");
			try {
				throw new WebApplicationException(
						Response.status(Status.BAD_REQUEST)
								.type(MediaType.APPLICATION_JSON)
								.entity(mapper.writeValueAsString(msg))
								.build()
				);
			} catch (JsonProcessingException e) {
				throw new WebApplicationException(
						Response.status(Status.INTERNAL_SERVER_ERROR)
								.build()
				);
			}
    	}

        final LatLonPoint topLeft =
        		new LatLonPointImpl(startLat.val(), startLon.val());
        final LatLonPoint bottomRight = 
        		new LatLonPointImpl(endLat.val(), endLon.val());
        final AreaBounds bounds = 
        		new AreaBounds(topLeft, bottomRight, depth.val(), time.val());
        final double[][] areaData;
        
        try {
            areaData = netCdfManager.readArea(bounds, "temperature");
        } catch (final IOException e1) {
        	final ObjectMapper mapper = new ObjectMapper();
			JSONMsg msg = new JSONMsg(
					JSONMsg.Status.ERROR,
					"Could not read data file. " + e1.getMessage());
			try {
	            return Response
	                    .status(Response.Status.INTERNAL_SERVER_ERROR)
	                    .type(MediaType.APPLICATION_JSON)
	                    .entity(mapper.writeValueAsString(msg))
	                    .build();
			} catch (JsonProcessingException e2) {
				return Response
						.status(Response.Status.INTERNAL_SERVER_ERROR)
						.build();
			}
        } catch (final InvalidRangeException e1) {
        	final ObjectMapper mapper = new ObjectMapper();
			JSONMsg msg = new JSONMsg(
					JSONMsg.Status.ERROR,
					"Invalid ranges provided. " + e1.getMessage());
			try {
	            return Response
	                    .status(Response.Status.INTERNAL_SERVER_ERROR)
	                    .type(MediaType.APPLICATION_JSON)
	                    .entity(mapper.writeValueAsString(msg))
	                    .build();
			} catch (JsonProcessingException e2) {
				return Response
						.status(Response.Status.INTERNAL_SERVER_ERROR)
						.build();
			}
        }

        // @TODO(Arve) Image size
        final BufferedImage image = ImageRenderer.render(areaData);
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, "png", baos);
        final byte[] imageData = baos.toByteArray();
        return Response.ok(imageData).build();
    }
}
