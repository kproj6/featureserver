/**
 * Filename: DepthResource.java
 * Package: com.sintef.featureserver.rs.features
 *
 * Created: 25 Oct 2014
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
 * Handles all requests for depth.
 * 
 * This class contains processing of all /feature/depth requests.
 */
@Path("feature/depth")
public class DepthResource {
	
	/**
	 * Back reference to NetCdfManager class for accessing information from NetCDF files.
	 */
    private final NetCdfManager netCdfManager;

    /**
     * Creates an instance of DepthResource class.
     * 
     * Creates a new instance of DepthResource class and sets back reference
     * for NetCdfManager.
     * 
     * @param manager Reference to NetCdfManager class for handling NetCDF files.
     */
    public DepthResource(
    		@Context final NetCdfManager manager) {
        this.netCdfManager = manager;
    }

    /**
     * Handles request for an image representing depths in given area.
     * 
     * @param startLat Latitude of top left point that should be displayed.
     * @param startLon Longitude of top left point that should be displayed.
     * @param endLat Latitude of bottom right point that should be displayed.
     * @param endLon Longitude of bottom right point that should be displayed.
     * @return Returns HTTP response containing either image with requested data,
     * 			or JSON with error description in case of failure.
     * @throws IOException
     */
    @GET
    public Response depthInRegion (
            @QueryParam("startLat") final Float startLat,
            @QueryParam("startLon") final Float startLon,
            @QueryParam("endLat") final Float endLat,
            @QueryParam("endLon") final Float endLon)
            throws IOException {
    	
    	RsUtil.checkPresenceOfQP(
    			"startLat", startLat,
    			"startLon", startLon,
    			"endLat", endLat,
    			"endLon", endLon);
        
        final LatLonPoint topLeft =
        		new LatLonPointImpl(startLat, startLon);
        final LatLonPoint bottomRight = 
        		new LatLonPointImpl(endLat, endLon);
        final AreaBounds bounds = 
        		new AreaBounds(topLeft, bottomRight);
        final double[][] areaData;
        
        try {
            areaData = netCdfManager.readArea(bounds, Feature.DEPTH);
        } catch (final IOException e) {
        	throw new InternalServerException("Could not read data file.", e);
        } catch (final InvalidRangeException e) {
        	throw new BadRequestException("Invalid ranges provided.", e);
        }

        // @TODO(Arve) Image size
        final BufferedImage image = ImageRenderer.render(areaData, Feature.DEPTH, true);
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, "png", baos);
        final byte[] imageData = baos.toByteArray();
        return Response.ok(imageData).type("image/png").build();
    }

}


