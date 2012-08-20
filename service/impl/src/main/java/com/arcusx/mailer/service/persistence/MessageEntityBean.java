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

import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

/**
 *
 * @author conni
 * @version $Id$
 */
@Entity
@Table(name = "mailer.message")
@SequenceGenerator(name = "message_id_seq", sequenceName = "mailer.message_seq", allocationSize = 1)
public class MessageEntityBean implements MessageEntity
{
	@Id
	@Column(name = "message_id", unique = true, nullable = false)
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "message_id_seq")
	private Long messageId;

	@Column(name = "sender", nullable = false)
	private String sender;

	@OneToMany(fetch = FetchType.EAGER, targetEntity = MessageRecipientEntityBean.class, cascade = CascadeType.ALL)
	@JoinColumn(name = "message_id", nullable = false, unique = false)
	private Set<MessageRecipientEntity> recipients = new HashSet<MessageRecipientEntity>();

	@Column(name = "subject", nullable = false)
	private String subject;

	@Column(name = "body", nullable = false)
	private String body;
	
	@Column(name= "type", nullable = true)
	private Integer type;

	@Column(name = "sent_date", nullable = true)
	private Date sentDate;

	@Column(name = "failure_count", nullable = false)
	private Integer failureCount;

	public MessageEntityBean()
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

	public String getSender()
	{
		return this.sender;
	}

	public void setSender(String sender)
	{
		this.sender = sender;
	}

	public Long getMessageId()
	{
		return this.messageId;
	}

	public Set<MessageRecipientEntity> getRecipients()
	{
		return Collections.unmodifiableSet(this.recipients);
	}

	public void setRecipients(Set<MessageRecipientEntity> recipients)
	{
		this.recipients = new HashSet<MessageRecipientEntity>(recipients);
	}

	public void removeRecipient(MessageRecipientEntity recipient)
	{
		this.recipients.remove(recipient);
	}

	public void addRecipient(MessageRecipientEntity recipient)
	{
		this.recipients.add(recipient);
	}

	public String getSubject()
	{
		return this.subject;
	}

	public void setSubject(String subject)
	{
		this.subject = subject;
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

		MessageEntityBean other = (MessageEntityBean) otherObj;
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

	public Integer getType()
	{
		return type;
	}

	public void setType(Integer type)
	{
		this.type = type;
	}

}
