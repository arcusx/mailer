
package com.arcusx.mailer;

import java.io.Serializable;

public class MimeMessageData implements Serializable
{
	private static final long serialVersionUID = 1L;

	private Long messageId;

	private byte[] data;

	public MimeMessageData(Long messageId, byte[] data)
	{
		this.messageId = messageId;
		this.data = data;
	}

	public Long getMessageId()
	{
		return messageId;
	}

	public byte[] getData()
	{
		return data;
	}
}
