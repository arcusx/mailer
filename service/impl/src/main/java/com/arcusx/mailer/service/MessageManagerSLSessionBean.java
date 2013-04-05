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

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.annotation.security.PermitAll;
import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.mail.MessagingException;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.apache.log4j.Logger;
import org.jboss.annotation.ejb.RemoteBinding;

import com.arcusx.mailer.Message;
import com.arcusx.mailer.MessageManager;
import com.arcusx.mailer.MessageManagerException;
import com.arcusx.mailer.MimeMessageData;
import com.arcusx.mailer.service.persistence.MessageEntity;
import com.arcusx.mailer.xml.XmlToMessageTransformer;

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
	private static final int MAX_FAILURE_COUNT = 10;

	private static Logger logger = Logger.getLogger(MessageManagerSLSessionBean.class);

	@PersistenceContext
	private EntityManager entityManager;

	public MessageManagerSLSessionBean()
	{
	}

	@PermitAll
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public List<Long> fetchUndeliveredMessageIds() throws MessageManagerException
	{
		// hibernate sucks ass
		Query query = this.entityManager.createNativeQuery("select m.message_id from mailer.message m where sent_date is null and failure_count < ?");
		query.setParameter(1, Integer.valueOf(MAX_FAILURE_COUNT));
		List<BigInteger> messageIds = query.getResultList();
		List<Long> result = new ArrayList<Long>(messageIds.size());
		for (BigInteger curr : messageIds)
		{
			result.add(Long.valueOf(curr.longValue()));
		}

		return result;
	}

	@PermitAll
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public MimeMessageData fetchMessage(Long messageId) throws MessageManagerException
	{
		Query query = this.entityManager.createNativeQuery("select m.* from mailer.message m where m.message_id = ?", MessageEntity.class);
		query.setParameter(1, messageId);
		List<MessageEntity> messages = query.getResultList();
		if (messages == null || messages.isEmpty())
			throw new MessageManagerException("Message " + messageId + " not found.");

		try
		{
			MessageEntity messageEntity = (MessageEntity) messages.get(0);
			switch (messageEntity.getBodyType())
			{
				case MIME:
					return mimeMessageForMime(messageEntity);
				case PLAIN:
					return mimeMessageForPlain(messageEntity);
				case XML:
					return mimeMessageForXml(messageEntity);
				default:
					throw new IllegalArgumentException("Unknown body type " + messageEntity.getBodyType() + ".");
			}
		}
		catch (Exception ex)
		{
			logger.warn("Fetching message failed.", ex);

			throw new MessageManagerException(ex.getMessage());
		}
	}

	private MimeMessageData mimeMessageForMime(MessageEntity messageEntity) throws IOException
	{
		byte[] mimeMessageBytes = messageEntity.getBody().getBytes("ASCII");
		return new MimeMessageData(messageEntity.getMessageId(), mimeMessageBytes);
	}

	private MimeMessageData mimeMessageForPlain(MessageEntity messageEntity) throws IOException, MessagingException
	{
		byte[] mimeMessageBytes = new MimeMessageBuilder(messageEntity.getSender(), messageEntity.getRecipientsAsStrings(), null, messageEntity.getSubject(),
				messageEntity.getBody(), null).createMimeMessageAsBytes();
		return new MimeMessageData(messageEntity.getMessageId(), mimeMessageBytes);
	}

	private MimeMessageData mimeMessageForXml(MessageEntity messageEntity) throws Exception
	{
		Message message = new Message(messageEntity.getMessageId());
		new XmlToMessageTransformer().transform(messageEntity.getBody(), message);

		byte[] mimeMessageBytes = new MimeMessageBuilder(messageEntity.getSender(), messageEntity.getRecipientsAsStrings(), null, messageEntity.getSubject(),
				message.getBody(), message.getHtmlBody()).createMimeMessageAsBytes();
		return new MimeMessageData(messageEntity.getMessageId(), mimeMessageBytes);
	}

	@PermitAll
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public void markMessageSent(Long messageId) throws MessageManagerException
	{
		if (messageId == null)
			throw new IllegalArgumentException("Message id may not be null.");

		MessageEntity n = this.entityManager.find(MessageEntity.class, messageId);
		if (n.getSentDate() != null)
			return; // no problem, already sent
		else
			n.setSentDate(new Date());
	}

	@PermitAll
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public void countMessageSendFailure(Long messageId) throws MessageManagerException
	{
		if (messageId == null)
			throw new IllegalArgumentException("Message id may not be null.");

		MessageEntity n = this.entityManager.find(MessageEntity.class, messageId);
		if (n.getSentDate() != null)
			return; // no problem, already sent
		else
			n.setFailureCount(n.getFailureCount() + 1);
	}
}
