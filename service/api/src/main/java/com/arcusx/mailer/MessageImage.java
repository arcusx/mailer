/**
 * 
 */

package com.arcusx.mailer;

import java.io.Serializable;

public class MessageImage implements Serializable
{
	/**
	 * Keep serialization compatibility.
	 **/
	private static final long serialVersionUID = 1L;

	public final String identifier;
	public final String name;
	public final String type;
	public final byte[] data;

	MessageImage(String identifier, String type, byte[] data)
	{
		this(identifier, identifier, type, data);
	}

	MessageImage(String name, String identifier, String type, byte[] data)
	{
		super();
		this.name = name;
		this.identifier = identifier;
		this.type = type;
		this.data = data;
	}
}