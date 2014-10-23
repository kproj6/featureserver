package com.sintef.featureserver.exception;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.json.JSONObject;

/**
 * Exception class for invalid input.
 * Throwing this exception will return a 400 Bad Request response.
 *
 * @author arve
 */
public class BadRequestException extends WebApplicationException {
    private static final Response.Status STATUS_CODE = Response.Status.BAD_REQUEST;


    /**
     * Shows a standard json error object of the form
     * { "errorMessage": errorMessage}
     * @param errorMessage Human readable error message.
     */
    public BadRequestException(final String errorMessage) {
        super(Response
                .status(STATUS_CODE)
                .entity(new JSONObject().put("errorMessage", errorMessage).toString())
                .type(MediaType.APPLICATION_JSON)
                .build());
    }

    /**
     * Shows an extended json error object of the form
     * { "errorMessage": errorMessage,
     *   "error":  {...}}
     * @param errorMessage Human readable error message.
     * @param errorObject JSONObject containing additional info.
     */
    public BadRequestException(final String errorMessage, final JSONObject errorObject) {
        super(Response
                .status(STATUS_CODE)
                .entity(new JSONObject()
                    .put("errorMessage", errorMessage)
                    .put("error", errorObject)
                    .toString())
                .type(MediaType.APPLICATION_JSON)
                .build());
    }

    /**
     * Shows an extended json error object of the form
     * { "errorMessage": errorMessage,
     *   "cause":  "exception message"}
     * @param errorMessage Human readable error message.
     * @param cause third party exception that caused this to be thrown.
     */
    public BadRequestException(final String errorMessage, final Throwable cause) {
        super(Response
                .status(STATUS_CODE)
                .entity(new JSONObject()
                    .put("errorMessage", errorMessage)
                    .put("cause", cause.getMessage())
                    .toString())
                .type(MediaType.APPLICATION_JSON)
                .build());
    }
}
