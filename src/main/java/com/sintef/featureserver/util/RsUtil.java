package com.sintef.featureserver.util;

import com.sintef.featureserver.exception.BadRequestException;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Utilities for common tasks in the resources
 */
public class RsUtil {

    /**
     * Validates input format for "4D" resources where the user is required to provide an
     * (x, y) rectangle, and z and t coordinates.
     * @param startLat  Latitude of upper left corner
     * @param startLon  Longditude of upper left corner
     * @param endLat    Latitude of lower right corner
     * @param endLon    Longditude of lower right corner
     * @param depth     Depth in meters
     * @param time      ISO8601 datetime string.
     * @throws BadRequestException which results in a 400 bad request response with a JSON body
     * describing the error.
     */
    public static void validateAreaQueryParams(
            final Float startLat,
            final Float startLon,
            final Float endLat,
            final Float endLon,
            final Float depth,
            final String time) {
        final JSONArray missingFields = new JSONArray();
        if(startLat == null) { missingFields.put("startLat"); }
        if(startLon == null) { missingFields.put("startLon"); }
        if(endLat == null) { missingFields.put("endLat"); }
        if(endLon == null) { missingFields.put("endLon"); }
        if(depth == null) { missingFields.put("depth"); }
        if(time == null) { missingFields.put("time"); }

        if( missingFields.length() != 0) {
            final JSONObject errorObject = new JSONObject().put( "missingFields", missingFields);
            throw new BadRequestException("Missing query parameters in url.", errorObject);
        }
    }
}
