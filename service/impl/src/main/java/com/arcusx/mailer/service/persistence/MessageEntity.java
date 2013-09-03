/**
 *
 * This software is written by arcus(x) GmbH and subject 
 * to a contract between arcus(x) and its customer.
 *
 * This software stays property of arcus(x) unless differing
 * arrangements between arcus(x) and its customer apply.
 *
 * arcus(x) GmbH
 * Bergiusstrasse 27
 * D-22765 Hamburg, Germany
 *
 * Tel.: +49 (0)40.333 102 92 
 * Fax.: +49 (0)40.333 102 93 
 * http://www.arcusx.com
 * mailto:info@arcusx.com
 *
 */

package com.arcusx.mailer.service.persistence;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

/**
 *
 * @author conni
 * @version $Id$
 */
@Entity(name = "Message")
@Table(name = "mailer.message")
@SequenceGenerator(name = "message_id_seq", sequenceName = "mailer.message_seq", allocationSize = 1)
public class MessageEntity
{
	public static enum BodyType
	{
		@Deprecated
		PLAIN, //
		@Deprecated
		XML, //
		MIME;
	}

	@Id
	@Column(name = "message_id", unique = true, nullable = false)
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "message_id_seq")
	private Long messageId;

	/**
	 * @deprecated Stored in MIME body.
	 */
	@Deprecated
	@Column(name = "sender", nullable = false)
	private String sender;

	/**
	 * @deprecated Stored in MIME body.
	 */
	@Deprecated
	@Column(name = "subject", nullable = false)
	private String subject;

	@Column(name = "body", nullable = false)
	private String body;

	@Column(name = "body_type", nullable = true)
	@Enumerated(EnumType.STRING)
	private BodyType bodyType;

	@Column(name = "sent_date", nullable = true)
	private Date sentDate;

	@Column(name = "failure_count", nullable = false)
	private Integer failureCount;

	public MessageEntity()
	{
	}

	public Date getSentDate()
	{
		return this.sentDate;
	}

	public void setSentDate(Date sentDate)
	{
		this.sentDate = sentDate;
	}

	/**
	 * @deprecated Stored in MIME body.
	 */
	@Deprecated
	public String getSender()
	{
		return this.sender;
	}

	public Long getMessageId()
	{
		return this.messageId;
	}

	public String getBody()
	{
		return this.body;
	}

	public void setBody(String body)
	{
		this.body = body;
	}

	@Override
	public boolean equals(Object otherObj)
	{
		if (otherObj == this)
			return true;

		if (otherObj == null)
			return false;

		if (!getClass().equals(otherObj.getClass()))
		{
			return false;
		}

		MessageEntity other = (MessageEntity) otherObj;
		return this.messageId != null && this.messageId.equals(other.messageId);
	}

	public int getFailureCount()
	{
		return this.failureCount.intValue();
	}

	public void setFailureCount(int failureCount)
	{
		if (failureCount < 0)
			throw new IllegalArgumentException("Failure count may not be set < 0.");

		this.failureCount = Integer.valueOf(failureCount);
	}

	public BodyType getBodyType()
	{
		return bodyType;
	}

	public void setBodyType(BodyType bodyType)
	{
		this.bodyType = bodyType;
	}
}
