package com.sintef.featureserver.netcdf;

import com.sintef.featureserver.FeatureServer;
import com.sintef.featureserver.exception.InternalServerException;

import java.io.IOException;
import java.util.logging.Logger;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;
import ucar.ma2.Array;
import ucar.ma2.Index;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.dataset.CoordinateAxis1D;
import ucar.nc2.dataset.CoordinateAxis1DTime;
import ucar.nc2.dt.GridCoordSystem;
import ucar.nc2.dt.GridDataset;
import ucar.nc2.dt.GridDatatype;
import ucar.unidata.geoloc.LatLonPoint;
import ucar.unidata.geoloc.LatLonRect;

/**
 * Manages netCdfFiles. Selects the appropriate file to read data from based on the bounds the
 * user specified, downsamples the data to a sane density and returns the result.
 * @TODO (Arve) Should probably handle the caching strategy as well.
 * @TODO(Arve) Add logging
 * @author Arve Nygård
 */
public class NetCdfManager {
    private static final Logger LOGGER = Logger.getLogger(NetCdfManager.class.getName());
    
    /**
     * Gets the variable values at the given area and depth.
     * @param boundingBox Area we are interested in.
     * @param variableName Which variable we want data from
     * @return 2D array of values.
     * @throws IOException
     * @throws InvalidRangeException
     */
    public double[][] readArea(final AreaBounds boundingBox, final Feature var)
    		throws IOException, InvalidRangeException {
    	
    	// Select the variable name that will be used for dimension calculations
    	// I am not sure, but maybe it does not matter which one will be used here
    	String variableName;
    	switch (var) {
		case DEPTH:
			variableName = "depth";
			break;
		case SALINITY:
			variableName = "salinity";
			break;
		case TEMPERATURE:
			variableName = "temperature";
			break;
		case WATER_VELOCITY:
			variableName = "u_east";
			break;
		case WIND_VELOCITY:
			variableName = "w_east";
			break;
		case WTF_VELOCITY:
			variableName = "w_velocity";
			break;
		case CURRENT_MAGNITUDE:
			variableName = "u_east";
			break;
		case CURRENT_DIRECTION:
			variableName = "u_east";
			break;
		default:
			throw new InternalServerException("Requested unknown feature.");
        }
    	
    	// Find files
        final String filename = getCorrectFilePath(boundingBox); // Hardcoded to launch flag for now.

        // Pick files (if there are multiple files with same data)
        // and areas that will be read in them
        
        // Get the dimensions of the result
        
        double[][] result; // @TODO: Initialize here with the right dimensions
        
        switch (var) {
		case DEPTH:
			// Go through files, read it's portion of data and store it to the result array
			result = getScalar2DVars(filename, boundingBox, variableName);
			break;
		case SALINITY:
			// Go through files, read it's portion of data and store it to the result array
			result = getScalars(filename, boundingBox, variableName);
			break;
		case TEMPERATURE:
			// Go through files, read it's portion of data and store it to the result array
			result = getScalars(filename, boundingBox, variableName);
			break;
		case WATER_VELOCITY:
			throw new NotImplementedException();
		case WIND_VELOCITY:
			throw new NotImplementedException();
		case WTF_VELOCITY:
			throw new NotImplementedException();
		case CURRENT_MAGNITUDE:
			// Go through files, read it's portion of data and store it to the result array
			result = getMagnitudeOfVector4DVars(filename, boundingBox, "u_east", "v_north");
			break;
		case CURRENT_DIRECTION:
			throw new NotImplementedException();
		default:
			throw new InternalServerException("Requested unknown feature.");
        }
        
        return result;
    }

	/**
     * Reads the values along the z-axis at a given point for the a given variable.
     * For example: Temperature profile at some location.
     *
     * @param location The point where the profile is to be sampled.
     * @param variableName The type variable we are interested in.
     */
    public short[] readDepthProfile(final LatLonPoint location, final String variableName){
        throw new NotImplementedException();
    }

    /**
     * Calculates the stride (i.e. N in `get every N'th data point) used when fetching data,
     * to avoid returning too many datapoints for the requested region
     * @return int[strideX, strideY]
     */
    private int[] calculateStride(final LatLonRect bounds){
        // Hardcoded to a stride of 1; meaning include every single data point in the region.
        // @TODO(Arve) fixme
        return new int[] {1, 1};
    }

    /**
     * @return path to the file containing the relevant data matching the requested bounds.
     * Should consider scale when appropriate (i.e. a huge spatial region -> use a coarse data
     * source.
     * @TODO(Arve) Currently returns a single file passed as launch parameter.
     * This should talk to the index instead.
     */
    private String getCorrectFilePath(final AreaBounds bounds){
        return FeatureServer.netCdfFile;
    }

	/**
	 * Reads array of scalar values which represents 4D variables in given NetCDF file.
	 * 
	 * @param filename Name of the file, that should be read.
	 * @param boundingBox Bounds of the area that should be returned.
	 * @param variable Name of 4D variable that should be returned.
	 * @return Array of scalar values representing given variable.
	 * @throws IOException 
	 * @throws InvalidRangeException 
	 */
	private double[][] getScalars(
			String filename, AreaBounds boundingBox, String variable)
			throws IOException, InvalidRangeException {
        
		// Open the dataset, find the variable and its coordinate system
        final GridDataset gds = ucar.nc2.dt.grid.GridDataset.open(filename);
        final GridDatatype grid = gds.findGridDatatype(variableName);
        final GridCoordSystem gcs = grid.getCoordinateSystem();

        // Crop the X and Y dimensions
        // @TODO(Arve) calculate stride properly!
        final GridDatatype gridSubset = grid.makeSubset(
                null, // time range. Null to keep everything
                null, // Z range. Null to keep everything
                boundingBox.getRect(), // Rectangle we are interested in
                1, // Z stride
                1, // Y stride
                1); // X stride

        // Default values for Depth and time: If the data volume does not have these axes (i.e.
        // only a single depth layer or time slice, then we grab "everything along that axis")
        int timeIndex = -1;
        int depthIndex = -1;
        if(gcs.hasTimeAxis()) {
            final CoordinateAxis1DTime timeAxis = gcs.getTimeAxis1D();
            timeIndex = timeAxis.findTimeIndexFromDate(boundingBox.getTime().toDate());
        }

        final CoordinateAxis1D depthAxis = gcs.getVerticalAxis();
        if (depthAxis != null) {
            depthIndex = depthAxis.findCoordElementBounded(boundingBox.getDepth());
        }
        // Gridsubset is now the volume we are interested in.
        // -1 to get everything along X and Y dimension.
        final Array areaData = gridSubset.readDataSlice(timeIndex, depthIndex, -1, -1);

        // Create array to hold the data
        final int[] shape = areaData.getShape();
        final double[][] result = new double[shape[0]][shape[1]];

        final Index index = areaData.getIndex();
        for (int i=0; i<shape[0]; i++) {
            for (int j=0; j<shape[1]; j++) {
                result[i][j] = areaData.getDouble(index.set(i,j));
            }
		}
        
        return result;
	}
    

    /**
     * Reads arrays of vectors of 4D variables and returns it's magnitudes.
     * 
	 * @param filename Name of the file, that should be read.
	 * @param boundingBox Bounds of the area that should be returned.
	 * @param xVariable Name of 4D variable that represents magnitude in east-west direction.
	 * @param yVariable Name of 4D variable that represents magnitude in north-south direction.
	 * @return Array of magnitudes.
	 * @throws IOException 
	 * @throws InvalidRangeException 
	 */
	private double[][] getMagnitudeOfVector4DVars(
			String filename, AreaBounds boundingBox, String xVariable, String yVariable)
					throws IOException, InvalidRangeException {
		
		// Open the dataset, find the variable and its coordinate system
        final GridDataset gds = ucar.nc2.dt.grid.GridDataset.open(filename);
        final GridDatatype xGrid = gds.findGridDatatype(xVariable);
        final GridDatatype yGrid = gds.findGridDatatype(yVariable);
        final GridCoordSystem gcs = xGrid.getCoordinateSystem();
		
        // Crop the X and Y dimensions
        // @TODO(Arve) calculate stride properly!
        final GridDatatype xGridSubset = xGrid.makeSubset(
                null, // time range. Null to keep everything
                null, // Z range. Null to keep everything
                boundingBox.getRect(), // Rectangle we are interested in
                1, // Z stride
                1, // Y stride
                1); // X stride
        final GridDatatype yGridSubset = yGrid.makeSubset(
                null, // time range. Null to keep everything
                null, // Z range. Null to keep everything
                boundingBox.getRect(), // Rectangle we are interested in
                1, // Z stride
                1, // Y stride
                1); // X stride

        final CoordinateAxis1DTime timeAxis = gcs.getTimeAxis1D();
        final int timeIndex = timeAxis.findTimeIndexFromDate(boundingBox.getTime().toDate());

        final CoordinateAxis1D depthAxis = gcs.getVerticalAxis();
        final int depthIndex =  depthAxis.findCoordElementBounded(boundingBox.getDepth());

        // Gridsubset is now the volume we are interested in.
        // -1 to get everything along X and Y dimension.
        final Array xData = xGridSubset.readDataSlice(timeIndex, depthIndex, -1, -1);
        final Array yData = yGridSubset.readDataSlice(timeIndex, depthIndex, -1, -1);

        // Create array to hold the data
        final int[] shape = xData.getShape();
        final double[][] result = new double[shape[0]][shape[1]];

        final Index xIndex = xData.getIndex();
        final Index yIndex = yData.getIndex();
        for (int i=0; i<shape[0]; i++) {
            for (int j=0; j<shape[1]; j++) {
            	result[i][j] = Math.sqrt(
            			Math.pow(xData.getDouble(xIndex.set(i,j)), 2) + 
            			Math.pow(yData.getDouble(yIndex.set(i,j)), 2)
            	);
            }
        }
        
        return result;
	}
	
	/**
	 * Reads array of scalar values which represents 2D variables in given NetCDF file.
	 * 
	 * @param filename Name of the file, that should be read.
	 * @param boundingBox Bounds of the area that should be returned. (Only upperLeft and lowerRight points are used)
	 * @param variableName Name of the 2D variable that should be returned.
	 * @return Array of scalar values representing given variable.
	 * @throws IOException 
	 * @throws InvalidRangeException 
	 */
	private double[][] getScalar2DVars(
			String filename, AreaBounds boundingBox, String variableName)
			throws IOException, InvalidRangeException {
		
		// Open the dataset, find the variable and its coordinate system
        final GridDataset gds = ucar.nc2.dt.grid.GridDataset.open(filename);
        final GridDatatype grid = gds.findGridDatatype(variableName);
		
        // Crop the X and Y dimensions
        // @TODO(Arve) calculate stride properly!
        final GridDatatype gridSubset = grid.makeSubset(
                null, // time range. Null to keep everything
                null, // Z range. Null to keep everything
                boundingBox.getRect(), // Rectangle we are interested in
                1, // Z stride
                1, // Y stride
                1); // X stride

        // Gridsubset is now the volume we are interested in.
        // -1 to get everything along X and Y dimension.
        final Array areaData = gridSubset.readDataSlice(-1, -1, -1, -1);

        // Create array to hold the data
        final int[] shape = areaData.getShape();
        final double[][] result = new double[shape[0]][shape[1]];

        final Index index = areaData.getIndex();
        for (int i=0; i<shape[0]; i++) {
            for (int j=0; j<shape[1]; j++) {
                result[i][j] = areaData.getDouble(index.set(i,j));
            }
        }
        
        return result;
	}
}
