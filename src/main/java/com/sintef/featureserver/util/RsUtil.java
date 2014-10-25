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
        
        checkPresenceOfQP(
        		"startLat", startLat,
    			"startLon", startLon,
    			"endLat", endLat,
    			"endLon", endLon,
    			"depth", depth,
    			"time", time);
    }
    
    public static void checkPresenceOfQP(Object... objects) {
    	final JSONArray missingFields = new JSONArray();
    	for (int i = 1; i < objects.length; i += 2) {
    		if (objects[i] == null) { missingFields.put(objects[i-1]); }
    	}
    	if( missingFields.length() != 0) {
            final JSONObject errorObject = new JSONObject().put( "missingFields", missingFields);
            throw new BadRequestException("Missing query parameters in url.", errorObject);
        }
    }
    
}
