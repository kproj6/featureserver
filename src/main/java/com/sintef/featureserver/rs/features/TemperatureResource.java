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
import javax.ws.rs.DefaultValue;
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
 * Handles all requests for temperature.
 * 
 * This class contains processing of all /feature/temperature requests.
 */
@Path("feature/temperature")
public class TemperatureResource {
	
	/**
	 * Conversion constants
	 */
	public static final double KELVIN_TO_CELSIUS = 273.15;
	
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
     * @param scale Optional parameter that specifies output scale.
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
            @QueryParam("time") final String time,
            @QueryParam("scale") @DefaultValue("Celsius") final String scale)
            throws IOException {
    	
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
        final double[][] areaData;
        
        try {
            areaData = netCdfManager.readArea(bounds, Feature.TEMPERATURE);
        } catch (final IOException e) {
        	throw new InternalServerException("Could not read data file.", e);
        } catch (final InvalidRangeException e) {
        	throw new BadRequestException("Invalid ranges provided.", e);
        }
        
        if (scale.equalsIgnoreCase("C") || scale.equalsIgnoreCase("Celsius")) {
        	final int height = areaData.length;
        	final int width = areaData[0].length;
        	for (int i = 0; i < height ; i++) {
        		for (int j = 0; j < width; j++) {
        			areaData[i][j] = areaData[i][j] - KELVIN_TO_CELSIUS;
        		}
        	}
        } else if (!scale.equalsIgnoreCase("K") && !scale.equalsIgnoreCase("Kelvin")) {
        	throw new BadRequestException("Unknown scale.");
        }

        // @TODO(Arve) Image size
        final BufferedImage image = ImageRenderer.render(areaData, Feature.TEMPERATURE, true);
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, "png", baos);
        final byte[] imageData = baos.toByteArray();
        return Response.ok(imageData).type("image/png").build();
    }

}
