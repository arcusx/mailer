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

/**
 *
 * @author conni
 * @version $Id$
 */
public interface MessageEntity
{
	Long getMessageId();

	Date getSentDate();

	void setSentDate(Date sentDate);

	String getSender();

	void setSender(String sender);

	String getRecipients();

	void setRecipients(String recipients);

	String getSubject();

	void setSubject(String subject);

	String getBody();

	void setBody(String body);

	int getFailureCount();

	void setFailureCount(int failureCount);
}
