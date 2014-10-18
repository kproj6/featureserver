/**
 * Filename: TimeParam.java
 * Package: com.sintef.featureserver.datatype
 *
 * Created: 18 Oct 2014
 * 
 * Author: Ondřej Hujňák
 * Licence:
 */
package com.sintef.featureserver.datatype;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.joda.time.DateTime;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sintef.featureserver.util.JSONMsg;

/**
 * Wrapper class for user input of time.
 * 
 * This class takes a string given by user and transforms it into {@link DateTime} object.
 * In case of invalid input throws {@link WebApplicationException}.
 * 
 * @author Ondřej Hujňák
 */
public class TimeParam {
	
	private DateTime val;
	
	/**
	 * Class constructor.
	 * 
	 * Takes user input, validates it and if it's invalid, throws exception.
	 * 
	 * @param timeStr Input string received from user.
	 * @throws WebApplicationException
	 */
	public TimeParam(final String timeStr) throws WebApplicationException {
		if (timeStr == null || timeStr == "") {
			final ObjectMapper mapper = new ObjectMapper();
			JSONMsg msg = new JSONMsg(
					JSONMsg.Status.ERROR,
					"Missing required time value.");
			try {
				throw new WebApplicationException(
						Response.status(Status.BAD_REQUEST)
								.type(MediaType.APPLICATION_JSON)
								.entity(mapper.writeValueAsString(msg))
								.build()
				);
			} catch (JsonProcessingException e) {
				throw new WebApplicationException(
						Response.status(Status.INTERNAL_SERVER_ERROR)
								.build()
				);
			}
		}
		
		try {
			this.val = DateTime.parse(timeStr);
		} catch (IllegalArgumentException e1) {
			final ObjectMapper mapper = new ObjectMapper();
			JSONMsg msg = new JSONMsg(
					JSONMsg.Status.ERROR,
					"Invalid time format. Please use the following pattern: YYYY-MM-DD");
			try {
				throw new WebApplicationException(
						Response.status(Status.BAD_REQUEST)
								.type(MediaType.APPLICATION_JSON)
								.entity(mapper.writeValueAsString(msg))
								.build()
				);
			} catch (JsonProcessingException e2) {
				throw new WebApplicationException(
						Response.status(Status.INTERNAL_SERVER_ERROR)
								.build()
				);
			}
		}
	}

	/**
	 * Getter for value of this object.
	 * 
	 * @return Representation of time as DateTime object.
	 */
	public DateTime val() {
		return val;
	}

}