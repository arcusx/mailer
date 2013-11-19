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

package com.arcusx.mailer;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 *
 * @author conni
 * @version $Id$
 */
public class Message implements Serializable
{
	private static final long serialVersionUID = 1L;

	private Long messageId;

	private String sender;

	private Set<String> recipients = new HashSet<String>();

	private Set<String> ccRecipients = new HashSet<String>();

	private String subject;

	private String body;

	private HtmlMessageBody htmlBody;

	private String replyTo;

	private List<MessageAttachment> messageAttachments = new ArrayList<MessageAttachment>();

	public Message()
	{
	}

	public Message(Long messageId)
	{
		this.messageId = messageId;
	}

	public Long getMessageId()
	{
		return this.messageId;
	}

	public void setReplyTo(String replyTo)
	{
		this.replyTo = replyTo;
	}

	public String getReplyTo()
	{
		return this.replyTo;
	}

	public String getSender()
	{
		return this.sender;
	}

	public Set<String> getRecipients()
	{
		return Collections.unmodifiableSet(this.recipients);
	}

	public String getSubject()
	{
		return this.subject;
	}

	public String getBody()
	{
		return this.body;
	}

	public void setSender(String sender)
	{
		this.sender = sender;
	}

	public void setRecipients(Set<String> recipients)
	{
		this.recipients = new HashSet<String>(recipients);
	}

	public void addRecipient(String recipient)
	{
		this.recipients.add(recipient);
	}

	public void removeRecipient(String recipient)
	{
		this.recipients.remove(recipient);
	}

	public void setSubject(String subject)
	{
		this.subject = subject;
	}

	public void setBody(String body)
	{
		this.body = body;
	}

	public HtmlMessageBody getHtmlBody()
	{
		return this.htmlBody;
	}

	public void setHtmlBody(HtmlMessageBody htmlBody)
	{
		this.htmlBody = htmlBody;
	}

	public Set<String> getCcRecipients()
	{
		return this.ccRecipients;
	}

	public void setCcRecipients(Set<String> ccRecipients)
	{
		this.ccRecipients = ccRecipients;
	}

	public boolean hasAttachments()
	{
		return this.messageAttachments != null && !this.messageAttachments.isEmpty();
	}

	public void addMessageAttachment(String name, String contentType, byte[] data)
	{
		this.messageAttachments.add(new MessageAttachment(name, contentType, data));
	}

	public Iterable<MessageAttachment> getMessageAttachments()
	{
		if (this.messageAttachments == null)
			return Collections.emptyList();

		return Collections.unmodifiableList(this.messageAttachments);
	}
}
