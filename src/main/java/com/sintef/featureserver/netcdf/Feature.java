package com.sintef.featureserver.netcdf;

/**
 * Created by Emil on 18.10.2014.
 */
public enum Feature {
	DEPTH,
	SALINITY,
	TEMPERATURE,
	WATER_VELOCITY,
	WIND_VELOCITY,
	WTF_VELOCITY, // @TODO Change to whatever w_velocity in Sintefs' NetCDFs mean
	CURRENT_MAGNITUDE,
    CURRENT_DIRECTION
}
