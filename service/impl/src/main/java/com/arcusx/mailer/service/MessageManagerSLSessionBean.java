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

import java.math.BigInteger;
import java.util.Date;
import java.util.List;

import javax.annotation.security.PermitAll;
import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.jboss.annotation.ejb.RemoteBinding;

import com.arcusx.mailer.Message;
import com.arcusx.mailer.MessageManager;
import com.arcusx.mailer.MessageManagerException;
import com.arcusx.mailer.service.persistence.MessageEntity;
import com.arcusx.mailer.service.persistence.MessageEntityBean;

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
	@PersistenceContext
	private EntityManager entityManager;

	public MessageManagerSLSessionBean()
	{
	}

	@PermitAll
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public List<Long> fetchUndeliveredMessageIds() throws MessageManagerException
	{
		List<Long> messageIds = this.entityManager.createNativeQuery(
				"select m.message_id from mailer.message m where sent_date is null", Long.class).getResultList();
		return messageIds;
	}

	@PermitAll
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public Message fetchMessage(Long messageId) throws MessageManagerException
	{
		List<MessageEntity> messages = this.entityManager.createNativeQuery(
				"select m.* from mailer.message m where m.message_id = ?", MessageEntityBean.class).getResultList();
		if (messages == null || messages.isEmpty())
			throw new MessageManagerException("Message " + messageId + " not found.");

		MessageEntity messageEntity = (MessageEntity) messages.get(0);

		Message messageData = new Message(messageEntity.getMessageId());
		messageData.setBody(messageEntity.getBody());
		messageData.setRecipients(messageEntity.getRecipients());
		messageData.setSender(messageEntity.getSender());
		messageData.setSubject(messageEntity.getSubject());
		return messageData;
	}

	@PermitAll
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public void markMessageSent(Long messageId) throws MessageManagerException
	{
		if (messageId == null)
			throw new IllegalArgumentException("Message id may not be null.");

		MessageEntity n = this.entityManager.find(MessageEntityBean.class, messageId);
		if (n.getSentDate() != null)
			return; // no problem, already sent
		else
			n.setSentDate(new Date());
	}

}
