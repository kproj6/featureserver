package com.sintef.featureserver.netcdf;

/**
 * Created by Emil on 18.10.2014.
 */
public enum Feature {
	// Scalar NetCDF variables
	DEPTH("depth"),
	EASTWARD_WATER_SPEED("u_east"),
	NORTHWARD_WATER_SPEED("v_north"),
	EASTWARD_WIND_SPEED("w_east"),
	NORTHWARD_WIND_SPEED("w_north"),
	WTF_VELOCITY("w_velocity"), // @TODO Change to whatever it means
	TEMPERATURE("temperature"),
	SALINITY("salinity"),
	// Vector NetCDF variables
	WATER_VELOCITY("u_east", "v_north"),
	WIND_VELOCITY("w_east", "w_north"),
	// Other
	CURRENT_MAGNITUDE,
	CURRENT_DIRECTION;

	private final String xAxis;
	private final String yAxis;
	private final Integer dimension;

	/**
	 * Creates feature not directly related to NetCDF file.
	 */
	private Feature() {
		this.dimension = 0;
		this.xAxis = null;
		this.yAxis = null;
	}

	/**
	 * Creates 1D feature.
	 *
	 * @param variableName Name of the variable in NetCDF file
	 */
	private Feature(final String variableName) {
		this.dimension = 1;
		this.xAxis = variableName;
		this.yAxis = null;
	}

	/**
	 * Creates 2D feature.
	 *
	 * @param xAxisVarName Name of the x axis variable in NetCDF file
	 */
	private Feature(final String xAxisVarName, final String yAxisVarName) {
		this.dimension = 2;
		this.xAxis = xAxisVarName;
		this.yAxis = yAxisVarName;
	}

	@Override
	public String toString() {
		if (dimension == 1) {
			return xAxis;
		} else if (dimension == 2) {
			return xAxis + ", " + yAxis;
		}
		return "";
	}

	public String x() {
		return xAxis;
	}

	public String y() {
		return yAxis;
	}

	public Integer dimension() {
		return dimension;
	}
}
