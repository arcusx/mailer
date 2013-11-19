/**
 * 
 */

package com.arcusx.mailer;

import java.io.Serializable;

public class MessageAttachment implements Serializable
{
	private static final long serialVersionUID = 1L;

	private final String name;
	private final String contentType;
	private final byte[] data;

	MessageAttachment(String name, String contentType, byte[] data)
	{
		this.name = name;
		this.contentType = contentType;
		this.data = data;
	}

	public String getContentType()
	{
		return this.contentType;
	}

	public String getName()
	{
		return this.name;
	}

	public byte[] getData()
	{
		return this.data;
	}
}
