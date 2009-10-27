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

/**
 *
 * @author conni
 * @version $Id$
 */
public interface MailerService
{
	String JNDI_NAME = "ejb/com/arcusx/mailer/MailerService";

	/**
	 * Store a message for delivery.
	 * 
	 * @param message
	 * @throws MailerException
	 */
	void storeMessage(Message message) throws MailerException;
}
