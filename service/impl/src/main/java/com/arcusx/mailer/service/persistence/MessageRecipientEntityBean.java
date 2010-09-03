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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

@Entity
@Table(name = "mailer.message_recipient")
@SequenceGenerator(name = "message_recipient_id_seq", sequenceName = "mailer.message_recipient_seq", allocationSize = 1)
public class MessageRecipientEntityBean implements MessageRecipientEntity
{
	@Id
	@Column(name = "message_recipient_id", unique = true, nullable = false)
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "message_recipient_id_seq")
	private Long messageRecipientId;

	@Column(name = "email_address", nullable = false)
	private String emailAddress;

	public MessageRecipientEntityBean()
	{
	}

	public Long getMessageRecipientId()
	{
		return messageRecipientId;
	}

	public void setEmailAddress(String emailAddress)
	{
		this.emailAddress = emailAddress;
	}

	public String getEmailAddress()
	{
		return emailAddress;
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

		MessageRecipientEntityBean other = (MessageRecipientEntityBean) otherObj;
		return this.messageRecipientId != null && this.messageRecipientId.equals(other.messageRecipientId);
	}
}
