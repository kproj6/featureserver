package com.sintef.featureserver.netcdf;

import com.sintef.featureserver.FeatureServer;
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
    public short[][] readArea(final AreaBounds boundingBox, final String variableName) throws IOException,
    InvalidRangeException {

        final String filename = getCorrectFilePath(boundingBox); // Hardcoded to launch flag for now.

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

        final CoordinateAxis1DTime timeAxis = gcs.getTimeAxis1D();
        final int timeIndex = timeAxis.findTimeIndexFromDate(boundingBox.getTime().toDate());

        final CoordinateAxis1D depthAxis = gcs.getVerticalAxis();
        final int depthIndex =  depthAxis.findCoordElementBounded(boundingBox.getDepth());

        // Gridsubset is now the volume we are interested in.
        // -1 to get everything along X and Y dimension.
        final Array areaData = gridSubset.readDataSlice(timeIndex, depthIndex, -1, -1);

        // Create array to hold the data
        final int[] shape = areaData.getShape();
        final short[][] result = new short[shape[0]][shape[1]];

        final Index index = areaData.getIndex();
        for (int i=0; i<shape[0]; i++) {
            for (int j=0; j<shape[1]; j++) {
                result[i][j] = areaData.getShort(index.set(i,j));
            }
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
}
