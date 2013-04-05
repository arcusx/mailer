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

import javax.annotation.Resource;
import javax.annotation.security.PermitAll;
import javax.ejb.EJB;
import javax.ejb.EJBException;
import javax.ejb.SessionContext;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
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
@Stateless
// @SecurityDomain
public class MessageDeliveryServiceSLSessionBean implements MessageDeliveryService
{
	private static Logger logger = Logger.getLogger(MessageDeliveryServiceSLSessionBean.class);

	@Resource(mappedName = "java:/Mail")
	private Session session;

	@EJB(mappedName = MessageManager.JNDI_NAME)
	private MessageManager messageManager;

	@Resource
	private SessionContext sessionContext;

	public MessageDeliveryServiceSLSessionBean()
	{
	}

	@PermitAll
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
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
			this.sessionContext.setRollbackOnly();

			String msg = "Sending message failed. Rolling back tx.";
			logger.error(msg, ex);

			throw new EJBException(msg);
		}
	}

	/**
	 * @param messageId
	 * @throws MessageManagerException
	 * @throws Exception
	 */
	private boolean trySendMessage(MimeMessageData mimeMessageData) throws MessageManagerException, Exception
	{
		try
		{
			MimeMessage message = new MimeMessage(session, new ByteArrayInputStream(mimeMessageData.getData()));

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
