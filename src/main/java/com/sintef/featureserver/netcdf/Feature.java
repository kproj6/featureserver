package com.sintef.featureserver.netcdf;

/**
 * Created by Emil on 18.10.2014.
 */
public enum Feature {
	DEPTH("depth"),
	SALINITY("salinity"),
	TEMPERATURE("temperature"),
	WATER_VELOCITY("u_east"),
	WIND_VELOCITY("w_east"),
	WTF_VELOCITY("w_velocity"), // @TODO Change to whatever w_velocity in Sintefs' NetCDFs mean
	CURRENT_MAGNITUDE("u_east"),
    CURRENT_DIRECTION("u_east");
	
	private final String dimensionVarName;
	
	/**
     * @param text
     */
    private Feature(final String dimensionVarName) {
        this.dimensionVarName = dimensionVarName;
    }

    /* (non-Javadoc)
     * @see java.lang.Enum#toString()
     */
    @Override
    public String toString() {
        return dimensionVarName;
    }
}
