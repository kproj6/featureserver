/**
 * Filename: FloatParam.java
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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sintef.featureserver.util.JSONMsg;

/**
 * Wrapper class for user input of float number.
 * 
 * This class takes a string given by user and transforms it into Float object.
 * In case of invalid input throws {@link WebApplicationException}.
 * 
 * @author Ondřej Hujňák
 */
public class FloatParam {
	
	private Float val;
	
	/**
	 * Class constructor.
	 * 
	 * Takes user input, validates it and if it's invalid, throws exception.
	 * 
	 * @param floatStr Input string from the user containing the number.
	 * @throws WebApplicationException
	 */
	public FloatParam(final String floatStr) throws WebApplicationException {
		
		if (floatStr == null || floatStr == "") {
			final ObjectMapper mapper = new ObjectMapper();
			JSONMsg msg = new JSONMsg(
					JSONMsg.Status.ERROR,
					"Missing required number parameter.");
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
			this.val = Float.parseFloat(floatStr);
		} catch (NumberFormatException e1) {
			final ObjectMapper mapper = new ObjectMapper();
			JSONMsg msg = new JSONMsg(
					JSONMsg.Status.ERROR,
					"Unknown number format. Please use standard format.");
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
	 * @return Representation of the number as Float.
	 */
	public Float val() {
		return val;
	}

}
