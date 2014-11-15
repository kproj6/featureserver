package com.sintef.featureserver.netcdf;

import com.sintef.featureserver.FeatureServer;
import com.sintef.featureserver.exception.InternalServerException;
import com.sintef.featureserver.util.NetCdfDescriptor;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.joda.time.DateTime;
import ucar.ma2.Array;
import ucar.ma2.Index;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.Attribute;
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
 * @author Arve Nygård
 */
public class NetCdfManager {

    /**
     * Gets the values of a scalar variables at the given area.
     *
     * @param boundingBox Area we are interested in.
     * @param feature Which feature we are interested in.
     * @return 2D array of values.
     * @throws IOException
     * @throws InvalidRangeException
     */
    public double[][] getScalarArea(final AreaBounds boundingBox, final Feature feature)
            throws IOException, InvalidRangeException {

        if (feature.dimension() != 1) {
            switch (feature.dimension()) {
            case 0:
                throw new InternalServerException("Used Feature is not directly related to NetCDF.");
            case 2:
                throw new InternalServerException("Asked for scalar data from vector variables.");
            default:
                throw new InternalServerException("Unknown Feature.");
            }
        }

        // Find files
        final List<NetCdfDescriptor> files = getCorrectFilePath(boundingBox); // Hardcoded to launch
        // flag for now.

        // Filter files by date (keep only the most current ones)
        
        // Count the resolution of result (Or get)
        
        // Filter files by resolution

        double[][] result;
        final int[] stride = calculateStride(boundingBox.getRect());

        // There will be loop through files that should be read
        final NetCdfDescriptor file = files.get(0);

        // Open the dataset, find the variable and its coordinate system
        final GridDataset gds = ucar.nc2.dt.grid.GridDataset.open(file.getFilename());
        final GridDatatype grid = gds.findGridDatatype(feature.toString());
        final GridCoordSystem gcs = grid.getCoordinateSystem();

        // Crop the X and Y dimensions
        final GridDatatype gridSubset = grid.makeSubset(
                null, // time range. Null to keep everything
                null, // Z range. Null to keep everything
                boundingBox.getRect(), // Rectangle we are interested in
                1, // Z stride
                stride[1], // Y stride
                stride[0]); // X stride

        // Default values for depth and time: If the data volume does not have these axes (i.e.
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
        result = new double[shape[0]][shape[1]];

        final Index index = areaData.getIndex();
        for (int i=0; i<shape[0]; i++) {
            for (int j=0; j<shape[1]; j++) {
                result[i][j] = areaData.getDouble(index.set(i,j));
            }
        }
        gds.close();
        return result;
    }

    /**
     * Gets the values of a vector variables at the given area.
     *
     * @param boundingBox Area we are interested in.
     * @param feature Which feature we are interested in.
     * @return 2D array of 2D values. (Every point in the array has form [x axis, y axis])
     * @throws IOException
     * @throws InvalidRangeException
     */
    public double[][][] getVectorArea(final AreaBounds boundingBox, final Feature feature)
            throws IOException, InvalidRangeException {

        if (feature.dimension() != 2) {
            switch (feature.dimension()) {
            case 0:
                throw new InternalServerException("Used Feature is not directly related to NetCDF.");
            case 1:
                throw new InternalServerException("Asked for vector data from scalar variables.");
            default:
                throw new InternalServerException("Unknown Feature.");
            }
        }

        // Find files
        final List<NetCdfDescriptor> files = getCorrectFilePath(boundingBox); // Hardcoded to launch
        // flag for now.

        // Pick files (if there are multiple files with same data)
        // and areas that will be read in them

        double[][][] result;
        final int[] stride = calculateStride(boundingBox.getRect());

        // There will be loop through all files that should be read
        final NetCdfDescriptor file = files.get(0);

        // Open the dataset, find the variable and its coordinate system
        final GridDataset gds = ucar.nc2.dt.grid.GridDataset.open(file.getFilename());
        final GridDatatype xGrid = gds.findGridDatatype(feature.x());
        final GridDatatype yGrid = gds.findGridDatatype(feature.y());
        final GridCoordSystem gcs = xGrid.getCoordinateSystem();

        // Crop the X and Y dimensions
        final GridDatatype xGridSubset = xGrid.makeSubset(
                null, // time range. Null to keep everything
                null, // Z range. Null to keep everything
                boundingBox.getRect(), // Rectangle we are interested in
                1, // Z stride
                stride[1], // Y stride
                stride[0]); // X stride
        final GridDatatype yGridSubset = yGrid.makeSubset(
                null, // time range. Null to keep everything
                null, // Z range. Null to keep everything
                boundingBox.getRect(), // Rectangle we are interested in
                1, // Z stride
                stride[1], // Y stride
                stride[0]); // X stride

        // Default values for depth and time: If the data volume does not have these axes (i.e.
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
        final Array xData = xGridSubset.readDataSlice(timeIndex, depthIndex, -1, -1);
        final Array yData = yGridSubset.readDataSlice(timeIndex, depthIndex, -1, -1);

        // Create array to hold the data
        final int[] shape = xData.getShape();
        result = new double[shape[0]][shape[1]][2];

        final Index xIndex = xData.getIndex();
        final Index yIndex = yData.getIndex();
        for (int i=0; i<shape[0]; i++) {
            for (int j=0; j<shape[1]; j++) {
                result[i][j][0] = xData.getDouble(xIndex.set(i,j));
                result[i][j][1] = yData.getDouble(yIndex.set(i,j));
            }
        }
        gds.close();
        return result;
    }

    /**
     * Reads the values along the z-axis at a given point for the a given variable.
     * For example: Temperature profile at some location.
     *
     * @param location The point where the profile is to be sampled.
     * @param feature The feature variable we are interested in.
     */
    public double[] readDepthProfile(
            final LatLonPoint location,
            final DateTime dateTime,
            final Feature feature) throws IOException, InvalidRangeException {
        final List<NetCdfDescriptor> filesContainingLocation = getCorrectFilePath(location);

        // Select the newest one.
        final NetCdfDescriptor file
                = filesContainingLocation.stream().max(NetCdfDescriptor.getDateComparator()).get();

        // Open the dataset, find the variable and its coordinate system
        final GridDataset gds = ucar.nc2.dt.grid.GridDataset.open(file.getFilename());

        final GridDatatype grid = gds.findGridDatatype(feature.toString());
        final GridCoordSystem gcs = grid.getCoordinateSystem();

        int timeIndex = -1;
        if(gcs.hasTimeAxis()) {
            final CoordinateAxis1DTime timeAxis = gcs.getTimeAxis1D();
            timeIndex = timeAxis.findTimeIndexFromDate(dateTime.toDate());
        }

        final int[] xy = gcs.findXYindexFromLatLon(location.getLatitude(), location.getLongitude(),
                null);

        if(xy[0] == -1 || xy[1] == -1){
            throw new InvalidRangeException("No data for the given location");
        }
        final Array depthProfile = grid.readDataSlice(
                timeIndex,
                -1, // All Z indices
                xy[0], // X coordinate
                xy[1] // Y coordinate
        );
        return (double[])depthProfile.get1DJavaArray(double.class);
    }

    /**
     * Calculates the stride (i.e. N in `get every N'th data point) used when fetching data,
     * to avoid returning too many datapoints for the requested region
     * @return int[strideX, strideY]
     */
    private int[] calculateStride(final LatLonRect bounds){
        // Hardcoded to a stride of 1; meaning include every single data point in the region.
        // @TODO(Arve) This should be calculated properly.
        return new int[] {1, 1};
    }

    /**
     * @return path to the file containing the relevant data matching the requested bounds.
     * Should consider scale when appropriate (i.e. a huge spatial region -> use a coarse data
     * source.
     * This should talk to the index instead.
     */
    private List<NetCdfDescriptor> getCorrectFilePath(final AreaBounds bounds){
        final List<NetCdfDescriptor> result = new ArrayList<NetCdfDescriptor>();
        result.add(new NetCdfDescriptor(
                FeatureServer.netCdfFile,
                new Integer[] {640, 515, 42},
                "1970-01-01")
        );
        return result;
    }

    /**
     * Get the file that contains the given point. If multiple files match, use the newest one.
     * @param point location we are interested in
     * @return NetCdfDescriptor describing the file that contains this point.
     */
    private List<NetCdfDescriptor> getCorrectFilePath(final LatLonPoint point){
        final List<NetCdfDescriptor> result = new ArrayList<NetCdfDescriptor>();
        // @Todo (Arve) Get this from the indexer.
        result.add(new NetCdfDescriptor(
                FeatureServer.netCdfFile,
                new Integer[] {640, 515, 42},
                "1970-01-01")
        );
        return result;
    }
    public LatLonRect getBoundingBox() throws IOException {
        // @Todo (Arve) This needs to be maintained by the indexer once we are working with
        // multiple files.
        final GridDataset gds = ucar.nc2.dt.grid.GridDataset.open(FeatureServer.netCdfFile);

        final List<Attribute> attrs = gds.getGlobalAttributes();

        return gds.getBoundingBox();

    }

    public double getResolution() throws IOException {
        final GridDataset gds = ucar.nc2.dt.grid.GridDataset.open(FeatureServer.netCdfFile);
        final List<Attribute> globalAttributes = gds.getGlobalAttributes();
        for (final Attribute attr: globalAttributes) {
            if (attr.getName().equals("horizontal_resolution")) {
                return attr.getNumericValue(0).intValue();  // Single value variable.
            }
        }
        throw new InternalServerException("Could not parse resolution of dataset.");
    }
}
