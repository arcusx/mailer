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
public class MessageManagerException extends Exception implements Serializable
{
	private static final long serialVersionUID = 1L;

	public MessageManagerException(String msg)
	{
		super(msg);
	}
}
