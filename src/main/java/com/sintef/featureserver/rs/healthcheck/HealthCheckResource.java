package com.sintef.featureserver.rs.healthcheck;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;


@Path("health")
public class HealthCheckResource {
    @Path("shallow")
    @GET
    public Response shallowHealthCheck() {
        return Response.ok("Shallow health check ok", MediaType.TEXT_PLAIN_TYPE).build();
    }
}
