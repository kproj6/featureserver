package com.sintef.featureserver.exception;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.json.JSONObject;

/**
 * Exception class for internal server errors.
 * Throwing this exception will return a 500 Internal Server Error response.
 *
 * @author arve
 */
public class InternalServerException extends WebApplicationException {
    private static final Response.Status STATUS_CODE = Response.Status.INTERNAL_SERVER_ERROR;

    /**
     * Shows a standard json error object of the form
     * { "errorMessage": errorMessage}
     * @param errorMessage Human readable error message.
     */
    public InternalServerException(final String errorMessage) {
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
    public InternalServerException(final String errorMessage, final JSONObject errorObject) {
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
    public InternalServerException(final String errorMessage, final Throwable cause) {
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
