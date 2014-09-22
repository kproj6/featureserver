package com.sintef.featureserver.rs.features;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sintef.featureserver.netcdf.Bounds;
import com.sintef.featureserver.netcdf.NetCdfManager;
import com.sintef.featureserver.netcdf.SalinityDataPoint;
import java.io.IOException;
import java.util.List;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import ucar.ma2.InvalidRangeException;

/**
 * JAVADOCX
 *
 * @author arve
 */
@Path("feature")
public class SalinityResource {
    private NetCdfManager netCdfManager;
    private ObjectMapper objectMapper;

    public SalinityResource(@Context NetCdfManager manager) {
        this.netCdfManager = manager;
        this.objectMapper = new ObjectMapper();
    }

    @Path("salinity")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response salinityInRegionAtTime(
            @QueryParam("time") final int time,
            @QueryParam("depth") final int depth,
            @QueryParam("startx") final int startX,
            @QueryParam("endx") final int endX,
            @QueryParam("starty") final int startY,
            @QueryParam("endy") final int endY) throws JsonProcessingException {

        final Bounds bounds = new Bounds(time, depth, startX, endX, startY, endY);
        final List<SalinityDataPoint> data;
        try {
            data = netCdfManager.readData(bounds);
        } catch (IOException e) {
            // @TODO: Return json as body on these errors as well.
            return Response
                    .status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Could not read data file. " + e.getMessage())
                    .build();
        } catch (InvalidRangeException e) {
            return Response
                    .status(Response.Status.BAD_REQUEST)
                    .entity("Invalid ranges provided. " + e.getMessage())
                    .build();
        }
        // @TODO(Arve) We need to turn the data into JSON :P
        String result = objectMapper.writeValueAsString(data);

        return Response.ok(result).build();
    }
}
