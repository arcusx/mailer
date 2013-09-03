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

package com.arcusx.mailer.batch;

import java.io.ByteArrayInputStream;
import java.util.List;

import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.MimeMessage;

import org.apache.log4j.Logger;

import com.arcusx.mailer.MessageManager;
import com.arcusx.mailer.MessageManagerException;
import com.arcusx.mailer.MimeMessageData;

/**
 *
 * @author conni
 * @version $Id$
 */
public class MessageDeliverySession
{
	private static Logger logger = Logger.getLogger(MessageDeliverySession.class);

	private Session mailSession;

	private MessageManager messageManager;

	public MessageDeliverySession(Session mailSession, MessageManager messageManager)
	{
		this.messageManager = messageManager;
		this.mailSession = mailSession;
	}

	/**
	 * Fetch a list of message ids to be delivered.
	 * 
	 * <b>This should be called in its own transaction.</b>
	 */
	public List<Long> fetchUndeliveredMessageIds()
	{
		try
		{
			return this.messageManager.fetchUndeliveredMessageIds();
		}
		catch (Exception ex)
		{
			throw new RuntimeException(ex);
		}
	}

	/**
	 * Fetch a message, try to send it, mark it as sent or failed respectively.
	 * 
	 * <b>This should be called in its own transaction.</b>
	 */
	public boolean sendMessage(Long messageId)
	{
		try
		{
			MimeMessageData mimeMessage = this.messageManager.fetchMessage(messageId);
			boolean sent = trySendMessage(mimeMessage);
			if (sent)
				this.messageManager.markMessageSent(messageId);
			else
				this.messageManager.countMessageSendFailure(messageId);
			return sent;
		}
		catch (Exception ex)
		{
			throw new RuntimeException(ex);
		}
	}

	private boolean trySendMessage(MimeMessageData mimeMessageData) throws MessageManagerException, Exception
	{
		try
		{
			MimeMessage message = new MimeMessage(this.mailSession, new ByteArrayInputStream(mimeMessageData.getData()));

			Transport.send(message);
			return true;
		}
		catch (Exception ex)
		{
			logger.warn("Creating/Sending message " + mimeMessageData.getMessageId() + " failed.", ex);
			return false;
		}
	}

}
