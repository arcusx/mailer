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

package com.arcusx.mailer.service;

import java.util.List;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.annotation.security.PermitAll;
import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.jboss.annotation.ejb.RemoteBinding;

import com.arcusx.mailer.MessageManager;
import com.arcusx.mailer.MessageManagerException;
import com.arcusx.mailer.MimeMessageData;

/**
 *
 * @author conni
 * @version $Id$
 */
@Stateless
@Remote(MessageManager.class)
@RemoteBinding(jndiBinding = MessageManager.JNDI_NAME)
// @SecurityDomain
public class MessageManagerSLSessionBean implements MessageManager
{
	private static Logger logger = Logger.getLogger(MessageManagerSLSessionBean.class);

	@Resource(mappedName = "java:/DefaultDS")
	private DataSource dataSource;

	private MessageStore messageStore;

	public MessageManagerSLSessionBean()
	{
	}

	@PostConstruct
	private void init()
	{
		this.messageStore = new MessageStore(this.dataSource);
	}

	@PermitAll
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public List<Long> fetchUndeliveredMessageIds() throws MessageManagerException
	{
		try
		{
			return this.messageStore.selectUndeliveredMessageIds();
		}
		catch (Exception ex)
		{
			logger.warn("Fetching message failed.", ex);

			throw new MessageManagerException(ex.getMessage());
		}
	}

	@PermitAll
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public MimeMessageData fetchMessage(Long messageId) throws MessageManagerException
	{
		try
		{
			return this.messageStore.selectMessage(messageId);
		}
		catch (Exception ex)
		{
			logger.warn("Fetching message failed.", ex);

			throw new MessageManagerException(ex.getMessage());
		}
	}

	@PermitAll
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public void markMessageSent(Long messageId) throws MessageManagerException
	{
		try
		{
			this.messageStore.markMessageSent(messageId);
		}
		catch (Exception ex)
		{
			logger.warn("Marking message as sent failed.", ex);

			throw new MessageManagerException(ex.getMessage());
		}
	}

	@PermitAll
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public void countMessageSendFailure(Long messageId) throws MessageManagerException
	{
		try
		{
			this.messageStore.countMessageSendFailure(messageId);
		}
		catch (Exception ex)
		{
			logger.warn("Increasing message failure counter failed.", ex);

			throw new MessageManagerException(ex.getMessage());
		}
	}
}
