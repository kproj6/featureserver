/**
 * Filename: NetCdfDescriptor.java
 * Package: com.sintef.featureserver.util
 *
 * Created: 9 Nov 2014
 * 
 * Author: Ondřej Hujňák
 * Licence:
 */
package com.sintef.featureserver.util;

import org.joda.time.DateTime;

/**
 * @author Ondřej Hujňák
 *
 */
public class NetCdfDescriptor {
	
	/**
	 * The name of file, that could be used for reading it's contents.
	 */
	final private String filename;

	/**
	 * Dimensions of the file.
	 * 
	 * NetCDF reference:
	 * dimensions:
	 * 		xc = 640 ;
	 * 		yc = 515 ;
	 * 		zc = 42 ;
	 */
	final private Integer[] dimensions;
	
	/**
	 * Date of the first day included in the file.
	 * 
	 * It is used for finding the most recent source for given request.
	 * 
	 * NetCDF reference:
	 * double time(time) ;
	 *		time:units = "days since 2013-08-05 00:00:00" ;
	 */
	final DateTime startDate;
	
	/**
	 * 	Boundaries of the area covered by the file.
	 * 
	 * NetCDF reference:
	 *		grid_mapping:horizontal_resolution = 160. ;
	 *		grid_mapping:latitude_of_projection_origin = 90. ;
	 *		grid_mapping:longitude_of_projection_origin = 58. ;
	 *
	 *		count end points?
	 */
	// final Boundaries;
	
	/**
	 * Resolution of the file.
	 * 
	 * It is used for calculating stride and for finding the best source file
	 * for given request.
	 * 
	 * NetCDF reference:
	 * 	grid_mapping:horizontal_resolution = 160. ;
	 */
	// final Integer resolution;


	/**
	 * Describes one source file and contains relevant information for deciding whether
	 * it contains data relevant for current request.
	 * 
	 * @param filename The name of the file.
	 * @param dimensions Dimensions of the data part.
	 * @param startDate What is the first day included in the file.
	 */
	public NetCdfDescriptor(
			String filename,
			Integer[] dimensions,
			String startDate) {
		
		this.filename = filename;
		this.dimensions = dimensions;
		this.startDate = DateTime.parse(startDate);
	}
	
	/**
	 * @return the filename
	 */
	public String getFilename() {
		return filename;
	}

	/**
	 * @return the dimensions
	 */
	public Integer[] getDimensions() {
		return dimensions;
	}

	/**
	 * @return the startDate
	 */
	public DateTime getStartDate() {
		return startDate;
	}
	
}


