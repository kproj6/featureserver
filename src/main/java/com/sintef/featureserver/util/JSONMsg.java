/**
 * Filename: JSONMsg.java
 * Package: com.sintef.featureserver.util
 *
 * Created: 18 Oct 2014
 * 
 * Author: Ondřej Hujňák
 * Licence:
 */
package com.sintef.featureserver.util;

/**
 * Simple dictionary class specifying template for JSON messages.
 * 
 * @author Ondřej Hujňák
 */
public class JSONMsg {

	/**
	 * Enumeration of possible statuses.
	 */
	public enum Status {
		OK,
		ERROR
	};

	public Status status;
	public String msg;

	/**
	 * Constructor for creation of JSON message representation.
	 * 
	 * @param status Status of the request.
	 * @param msg Message with description.
	 */
	public JSONMsg(final Status status, final String msg) {
		this.status = status;
		this.msg = msg;
	}
}
