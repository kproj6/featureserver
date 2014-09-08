package com.sintef.featureserver.netcdf;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import ucar.ma2.ArrayShort;
import ucar.ma2.InvalidRangeException;
import ucar.ma2.Range;
import ucar.ma2.Array;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;

/**
 * Manages netCdfFiles. Selects the appropriate file to read data from based on the bounds the
 * user specified, downsamples the data to a sane density and returns the result.
 * @TODO (Arve) Should probably handle the caching strategy as well.
 *
 * @author Arve Nygård
 */
public class NetCdfManager {
    private static final int MAX_POINTS_X = 400;
    private static final int MAX_POINTS_Y = 300;
    private static final Logger LOGGER = Logger.getLogger(NetCdfManager.class.getName());
    private final String filePath;

    public NetCdfManager(String filePath){ this.filePath = filePath; }

    public List<SalinityDataPoint> readData(final Bounds bounds) throws IOException, InvalidRangeException {
        final String filename = getCorrectFilePath(bounds); // Hardcoded to launch flag for now.
        NetcdfFile ncfile = null;
        try {
            ncfile = NetcdfFile.open(filename);
            return getSalinityData(ncfile, bounds);

        } catch (IOException ioe) {
            LOGGER.log(Level.SEVERE, "Could not open file: " + filename, ioe);
            throw ioe;
        } catch (InvalidRangeException e) {
            final String message = "invalid bounds provided: " + bounds;
            LOGGER.log(Level.SEVERE, message, e);
            throw e;

        } finally {
            if (null != ncfile) {
                try {
                    ncfile.close();
                } catch (IOException ioe) {
                    LOGGER.log(Level.SEVERE, "trying to close " + filename, ioe);
                    return null;
                }
            }
        }
    }

    private List<SalinityDataPoint> getSalinityData(final NetcdfFile ncfile, final Bounds bounds)
    throws InvalidRangeException, IOException {
        // @TODO(Arve) The amount of dimensions and their orders are hardcoded for salinity,
        // but this method should be generalized to handle variable and programmatically find
        // dimension order from the file, in order to avoid copying all this logic for each
        // variable type (salinity, wind, temp. etc...)

        final Variable salinity = ncfile.findVariable("salinity");
        // Different variables have different dimension orders.
        final int[] shape = salinity.getShape();

        // Define the "origin" in the variable space (first element to fetch in each dimension)

        // Salinity has dimensions {time, depth, y, x}
        final int[] origin = new int[] {
                0, // time. Hardcoded right now. Should use user value obviously.
                bounds.getDepth(),
                bounds.getStartY(),
                bounds.getStartX()
        };

        final List<Range> ranges = getRanges(bounds, shape);
        ArrayShort.D2 data = (ArrayShort.D2) salinity.read(ranges).reduce();
        final int[] reducedShape = data.getShape();
        final List<SalinityDataPoint> result = new ArrayList<>();
        for (int i=0; i<reducedShape[0]; i++) {
            for (int j=0; j<reducedShape[1]; j++) {
                result.add(new SalinityDataPoint(i,j, data.get(i,j)));
            }
        }
        return result;
    }

    /**
     * Gets the range based on a Bounds object (which was created based on the user's query
     * parameters)
     * Again, currently specialized for salinity, needs to be general.
     * @param bounds
     * @return list of ranges used to slice the data.
     * @throws InvalidRangeException
     */
    private List<Range> getRanges(final Bounds bounds, final int[] shape) throws
            InvalidRangeException {
        LOGGER.log(Level.INFO, "getting ranges for bounds" + bounds);
        final ArrayList<Range> ranges = new ArrayList<>();

        // Ranges in the form of Range(int startIndex, int endIndex)
        ranges.add(new Range(bounds.getTime(), bounds.getTime())); // Time. Hardcoded to 0. Needs to
        // use user value.
        ranges.add(new Range(bounds.getDepth(), bounds.getDepth())); // Depth. Single slice
        ranges.add(new Range(bounds.getStartX(), bounds.getEndX())); // y
        ranges.add(new Range(bounds.getStartY(), bounds.getEndY())); // x
        return ranges;
    }



    /**
     * Calculates the stride (i.e. N in `get every N'th data point) used when fetching data,
     * to avoid returning too many datapoints for the requested region
     * @return int[strideX, strideY]
     */
    private int[] calculateStride(final int[] varShape, final Bounds bounds){
        // Hardcoded to a stride of 1; meaning include every single data point in the region.
        // Obviously this needs to be fixed :P
        return new int[] {1, 1};
    }


    /**
     * @return path to the file containing the relevant data matching the requested bounds.
     * Should consider scale when appropriate (i.e. a huge spatial region -> use a coarse data
     * source.
     */
    private String getCorrectFilePath(final Bounds bounds){
        return this.filePath;
    }
}
