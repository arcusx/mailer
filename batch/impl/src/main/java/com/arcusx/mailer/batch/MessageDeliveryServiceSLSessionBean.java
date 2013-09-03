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

import javax.annotation.Resource;
import javax.annotation.security.PermitAll;
import javax.ejb.EJB;
import javax.ejb.EJBException;
import javax.ejb.SessionContext;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.mail.Session;

import org.apache.log4j.Logger;

import com.arcusx.mailer.MessageManager;

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
			return new MessageDeliverySession(session, messageManager).sendMessage(messageId);
		}
		catch (Exception ex)
		{
			this.sessionContext.setRollbackOnly();

			String msg = "Sending message failed. Rolling back tx.";
			logger.error(msg, ex);

			throw new EJBException(msg);
		}
	}
}
