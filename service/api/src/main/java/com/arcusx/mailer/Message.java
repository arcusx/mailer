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

/**
 *
 * @author conni
 * @version $Id$
 */
public final class Message implements Serializable
{
	private static final long serialVersionUID = 1L;

	private String sender;

	private String recipients;

	private String subject;

	private String body;

	public Message()
	{
	}

	public String getSender()
	{
		return this.sender;
	}

	public String getRecipients()
	{
		return this.recipients;
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

	public void setRecipients(String recipients)
	{
		this.recipients = recipients;
	}

	public void setSubject(String subject)
	{
		this.subject = subject;
	}

	public void setBody(String body)
	{
		this.body = body;
	}

}
