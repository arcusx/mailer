/**
 * 
 */

package com.arcusx.mailer;

public class MessageImage extends MessageAttachment
{
	private static final long serialVersionUID = 1L;

	public final String identifier;

	MessageImage(String identifier, String type, byte[] data)
	{
		this(identifier, identifier, type, data);
	}

	MessageImage(String name, String identifier, String type, byte[] data)
	{
		super(name, type, data);
		this.identifier = identifier;
	}
}