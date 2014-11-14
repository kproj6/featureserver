package com.sintef.featureserver.netcdf;

import com.sintef.featureserver.exception.InternalServerException;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.sqlite.SQLiteConfig;
import ucar.ma2.InvalidRangeException;
import ucar.ma2.Section;
import ucar.nc2.Attribute;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.sql.*;
import java.util.ArrayList;

/**
 * Uses an sqlite database with the spatialite extension to index the area and time covered by netCDF files.
 * The index can be queried to list all files that intersect a rectangle at a specified time
 *
 * Created by Emil on 10.11.2014.
 */
public class NetCdfIndexer {
    static final String scanningRoot = "C:/kproj6/";        //TODO: make this a parameter using @Path in FeatureServer.java
    static final String databasePath = "jdbc:sqlite:sinmod.sqlite";
    static Connection connection = null;


    public static void initialize(){
        SQLiteConfig config = new SQLiteConfig();
        config.enableLoadExtension(true);

        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            throw new InternalServerException("Failed to load sqlite.JDBC");
        }

        try {
            connection = DriverManager.getConnection(databasePath, config.toProperties());
        } catch (SQLException e) {
            throw new InternalServerException("Failed to connect to/create indexing database");
        }

        try {
            Statement statement = connection.createStatement();
            statement.executeQuery("SELECT load_extension('spatialite');");
            statement.close();
        } catch (SQLException e) {
            throw new InternalServerException("Failed to load spatialite extension");
        }

    }

    /**
     * Returns a list of all the netCDF files in the index that intersect with a specified rectangle at a specified time
     * @param time the specified time
     * @param startLat top edge of the rectangle
     * @param endLat bottom edge of the rectangle
     * @param startLon left edge of the rectangle
     * @param endLon right edge of the rectangle
     * @return ArrayList<String> of filepaths of all netCDF that intersect the requested rectangle and time
     */
    static ArrayList<String> query(DateTime time, double startLat, double endLat, double startLon, double endLon)
            throws SQLException {
        Statement statement = connection.createStatement();
        String statementString;
        ResultSet rs;

        String intersectsInSpace = "intersects(XYt.coverage, " +
                "GeomFromText('POLYGON((" +
                startLon + " " + startLat + ", " +
                endLon   + " " + startLat + ", " +
                endLon   + " " + endLat   + ", " +
                startLon + " " + endLat   + ", " +
                startLon + " " + startLat + "))', 33333))";

        String intersectsInTime = "intersects(XYt.time, GeomFromText('POINT(0 " + time.getMillis()/1000 + ")', 33333))";

        statementString = "SELECT filepath " +
                "FROM dataset_XY_time AS XYt " +
                "WHERE " + intersectsInSpace +
                " AND " + intersectsInTime + ";";

        rs = statement.executeQuery(statementString);
        ArrayList<String> result = new ArrayList<String>();
        while (rs.next()) {
            String filepath = rs.getString("filepath");
            result.add(filepath);
        }
        statement.close();

        return result;
    }

    /**
     * Adds any netCDF file inside scanningRoot to the index
     * Any already indexed will be skipped
     */
    public static void scan(){
        ArrayList<String> netCDFFiles = null;
        try {
            netCDFFiles = listNetCDFFilesIn(scanningRoot);

            for(String filepath : netCDFFiles){
                addFile(filepath);
            }

        } catch (IOException | SQLException | InvalidRangeException e) {
            System.out.println(e);
            throw new InternalServerException("Scan failed, " + e.toString());
        }
    }

    /**
     * Lists all netCDF files (files ending in ".nc" in the directory and any subdirectories (recursively)
     *
     * @param directory Where the search starts
     * @return ArrayList<String> of filepaths of all netCDF files found
     */
    private static ArrayList<String> listNetCDFFilesIn(String directory) throws IOException {
        final ArrayList<String> result = new ArrayList<String>();

        Path root = Paths.get(directory);
        Files.walkFileTree(root, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
                    throws IOException {
                if (file.toString().endsWith(".nc")) {
                    result.add(file.toString());
                }
                return FileVisitResult.CONTINUE;
            }
        });

        return result;
    }

    /**
     * Adds a netCDF file to the index database, unless it is already indexed
     * The file is opened to discover it's coverage in time and space as well as some metadata
     * @param filepath path of the file to be added
     */
    private static void addFile(String filepath) throws SQLException, InvalidRangeException {
        Statement statement = connection.createStatement();

        //checking if file is already in database
        String statementString = "SELECT * FROM dataset_XY_time WHERE filepath = \""  + filepath + "\";";
        ResultSet rs = statement.executeQuery(statementString);
        if(rs.next() == true){
            rs.close();
            statement.close();
            return;
        }

        rs.close();
        statement.close();

        //actually adding the file
        NetcdfFile file = null;
        try{
            file = NetcdfFile.open(filepath);

            Variable variableRaw = file.findVariable("gridLats");

            int yLength = variableRaw.getDimension(0).getLength();
            int xLength = variableRaw.getDimension(1).getLength();
            int[] origin = new int[] {0,0};
            int[] size = new int[] {yLength,xLength};
            int[] stride = new int[] {yLength-1, xLength-1};
            Section section = new Section(origin,size,stride);

            ucar.ma2.Array corners = variableRaw.read(section);
            double upperLeftLat  = corners.getDouble(0);
            double upperRightLat = corners.getDouble(1);
            double lowerLeftLat  = corners.getDouble(2);
            double lowerRightLat = corners.getDouble(3);

            variableRaw = file.findTopVariable("gridLons");
            corners = variableRaw.read(section);
            double upperLeftLon  = corners.getDouble(0);
            double upperRightLon = corners.getDouble(1);
            double lowerLeftLon  = corners.getDouble(2);
            double lowerRightLon = corners.getDouble(3);

            String coverageString = "GeomFromText('POLYGON((" +
                    upperLeftLon  + " " + upperLeftLat  + ", " +
                    upperRightLon + " " + upperRightLat + ", " +
                    lowerRightLon + " " + lowerRightLat + ", " +
                    lowerLeftLon  + " " + lowerLeftLat  + ", " +
                    upperLeftLon  + " " + upperLeftLat  + "))', 33333)";


            DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");
            variableRaw = file.findVariable("time");
            String startString = variableRaw.findAttributeIgnoreCase("units").getStringValue();
            DateTime startDateTime = formatter.parseDateTime(startString.substring(11));    //substring to remove 'days since '
            DateTime endDateTime = startDateTime.plusHours((int)variableRaw.getSize());

            long startTime = startDateTime.getMillis()/1000;
            long endTime = endDateTime.getMillis()/1000;
            String timeString = "GeomFromText('LINESTRING(0 " + startTime + ", 0 " + endTime + ")', 33333)";

            Attribute attribute = file.findVariable("grid_mapping").findAttributeIgnoreCase("horizontal_resolution");
            int gridSize = attribute.getNumericValue().intValue();

            statementString = "INSERT INTO dataset_XY_time (filepath, coverage, time, gridsize) " +
                    "VALUES(\"" + filepath + "\", " + coverageString + ", " + timeString + ", " + gridSize + ");";

            statement.executeUpdate(statementString);

        } catch (IOException e) {
            throw new InternalServerException("Error while opening netCDF to be added to index, " + filepath);
        } finally {
            if (file != null) {
                try {
                    file.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
