package com.sintef.featureserver.rs.wmts;

import com.sintef.featureserver.netcdf.NetCdfManager;
import com.sintef.featureserver.util.VelocityUtil;
import com.sintef.featureserver.wmts.GoogleMapsCompatibleTileMatrixSet;
import com.sintef.featureserver.wmts.TileMatrix;
import java.io.IOException;
import java.net.URI;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import ucar.unidata.geoloc.LatLonPoint;
import ucar.unidata.geoloc.LatLonRect;

/**
 * Serves the Capabilities document as prescribed by the WMTS spec.
 * The spec only defines an XML encoding for this document.
 * The path to this document is also defined by the spec.
 * @author arve
 */

@Path("WMTS/1.0.0/WMTSCapabilities.xml")
public class CapabilityResource {
    private final NetCdfManager netCdfManager;
    private final VelocityEngine velocityEngine;
    private final UriInfo uriInfo;

    public CapabilityResource(
            @Context final NetCdfManager netCdfManager,
            @Context final UriInfo uriInfo,
            @Context final VelocityEngine velocityEngine) {
        this.netCdfManager = netCdfManager;
        this.velocityEngine = velocityEngine;
        this.uriInfo = uriInfo;

    }

    @GET
    @Produces("text/xml")
    public Response getCapabilities() throws IOException {


        final Template xmlTemplate
                = VelocityUtil.loadTemplate(velocityEngine, "capabilities.xml");
        final VelocityContext context = new VelocityContext();

        final URI wmtsBaseUrl = UriBuilder.
                fromUri(uriInfo.getBaseUri())
                .path("WMTS")
                .build();
        final LatLonRect boundingBox = netCdfManager.getBoundingBox();
        final LatLonPoint upperLeft = boundingBox.getUpperLeftPoint();
        final LatLonPoint lowerRight = boundingBox.getLowerRightPoint();
        final GoogleMapsCompatibleTileMatrixSet tileMatrixSet
                = new GoogleMapsCompatibleTileMatrixSet(
                    netCdfManager.getResolution(),
                    netCdfManager.getBoundingBox());

        context.put("WmtsBaseUrl", wmtsBaseUrl);
        context.put("upperLeftPoint", upperLeft);
        context.put("lowerRightPoint", lowerRight);
        context.put("tileMatrixSet", tileMatrixSet);
        final String xmlString = VelocityUtil.renderTemplate(xmlTemplate, context);
        return Response.ok(xmlString).build();
    }
}
