package com.sintef.featureserver.providers;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONStringer;

/**
 * Exception mapper for {@link java.lang.IllegalArgumentException}.
 * Catches the exception and returns a 400 Bad request response with a JSON body showing the
 * error message.
 * @author arve
 */
@Provider
public class IllegalArgumentExceptionMapper implements ExceptionMapper<IllegalArgumentException> {
    private static final Logger LOGGER
            = Logger.getLogger(IllegalArgumentExceptionMapper.class.getName());

    @Override
    public Response toResponse(final IllegalArgumentException e) {
        LOGGER.log(Level.WARNING, "BadRequestException: " + e.getMessage(), e);

        JSONObject message;
        try {
            // If the error message already is a JSON object, we parse it
            message = new JSONObject(e.getMessage());
        } catch (final JSONException f){
            // Else wrap the naked string into a JSON object.
            final String mesageJson = new JSONStringer().object()
                    .key("message")
                    .value(e.getMessage())
                    .endObject()
                    .toString();
            message = new JSONObject(mesageJson);
        }

        final String json = new JSONStringer()
                .object()
                    .key("error")
                    .value(message)
                .endObject()
                .toString();

        return Response
                .status(Response.Status.BAD_REQUEST)
                .entity(json)
                .type(MediaType.APPLICATION_JSON)
                .build();
    }
}