package com.sintef.featureserver.netcdf;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Value object for a Salinity datapoint.
 *
 * @author arve
 */

public class SalinityDataPoint {
    public int x;
    public int y;
    public int salinity;

    @JsonCreator
    public SalinityDataPoint(
            @JsonProperty("x") final int x,
            @JsonProperty("y") final int y,
            @JsonProperty("salinity") final int salinity) {
        this.x = x;
        this.y = y;
        this.salinity = salinity;
    }

    @Override
    public String toString() {
        return "Salinity: "
                + "x: " + x
                + "y: " + y
                + "salinity: " + salinity
                + "\n";
    }
}
