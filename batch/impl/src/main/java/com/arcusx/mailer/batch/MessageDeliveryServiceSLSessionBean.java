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

import java.util.Iterator;
import java.util.Set;

import javax.annotation.Resource;
import javax.annotation.security.PermitAll;
import javax.ejb.EJB;
import javax.ejb.EJBException;
import javax.ejb.SessionContext;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.apache.log4j.Logger;

import com.arcusx.mailer.Message;
import com.arcusx.mailer.MessageManager;
import com.arcusx.mailer.MessageManagerException;

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
			Message n = this.messageManager.fetchMessage(messageId);
			boolean sent = trySendMessage(n);
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
	private boolean trySendMessage(Message n) throws MessageManagerException, Exception
	{
		try
		{
			String body = n.getBody();
			String htmlBody = n.getHtmlBody();
			boolean hasPlainTextBody = body != null && body.trim().length() > 0;
			boolean hasHtmlBody = htmlBody != null && htmlBody.trim().length() > 0;

			MimeMessage message = null;
			if (hasPlainTextBody && !hasHtmlBody)
			{
				message = createPlainTextEmail(n.getRecipients(), n.getSender(), n.getSubject(), body);
			}
			else if (!hasPlainTextBody && hasHtmlBody)
			{
				message = createHtmlOnlyEmail(n.getRecipients(), n.getSender(), n.getSubject(), htmlBody);
			}
			else if (hasPlainTextBody && hasHtmlBody)
			{
				message = createMultiPartEmail(n.getRecipients(), n.getSender(), n.getSubject(), body, htmlBody);
			}
			else
			{
				throw new IllegalArgumentException("Neither plain text body nor html body. Can't send empty mail.");
			}

			Transport.send(message);
			return true;
		}
		catch (Exception ex)
		{
			logger.warn("Creating/Sending message " + n.getMessageId() + " failed.", ex);
			return false;
		}
	}

	private MimeMessage createPlainTextEmail(Set<String> recipients, String sender, String subject, String body)
			throws MessagingException
	{
		MimeMessage message = new MimeMessage(session);
		message.setFrom(new InternetAddress(sender));
		for (Iterator<String> iter = recipients.iterator(); iter.hasNext();)
		{
			message.addRecipient(javax.mail.Message.RecipientType.TO, new InternetAddress((String) iter.next()));
		}
		message.setSubject(subject);
		message.setText(body);

		return message;
	}

	private MimeMessage createHtmlOnlyEmail(Set<String> recipients, String sender, String subject, String htmlBody)
			throws MessagingException
	{
		MimeMessage message = new MimeMessage(session);
		message.setFrom(new InternetAddress(sender));
		message.addHeader("Content-Type", "text/html; charset=ISO-8859-1");
		message.addHeader("Content-Transfer-Encoding", "7bit");
		for (Iterator<String> iter = recipients.iterator(); iter.hasNext();)
		{
			message.addRecipient(javax.mail.Message.RecipientType.TO, new InternetAddress((String) iter.next()));
		}
		message.setSubject(subject);
		message.setText(htmlBody, "ISO-8859-1", "html");

		return message;
	}

	private MimeMessage createMultiPartEmail(Set<String> recipients, String sender, String subject,
			String plainTextBody, String htmlBody) throws MessagingException
	{
		MimeMessage message = new MimeMessage(session);
		message.setFrom(new InternetAddress(sender));
		for (Iterator<String> iter = recipients.iterator(); iter.hasNext();)
		{
			message.addRecipient(javax.mail.Message.RecipientType.TO, new InternetAddress((String) iter.next()));
		}
		message.setSubject(subject);

		MimeMultipart mp = new MimeMultipart();

		MimeBodyPart plainTextBodyPart = new MimeBodyPart();
		plainTextBodyPart.addHeader("Content-Type", "text/plain; charset=ISO-8859-1; format=flowed");
		plainTextBodyPart.addHeader("Content-Transfer-Encoding", "7bit");
		// FIXME convert to iso8859?
		plainTextBodyPart.setText(plainTextBody, "ISO-8859-1");

		MimeBodyPart htmlBodyPart = new MimeBodyPart();
		htmlBodyPart.addHeader("Content-Type", "text/html; charset=ISO-8859-1");
		htmlBodyPart.addHeader("Content-Transfer-Encoding", "7bit");
		// FIXME convert to iso8859?
		htmlBodyPart.setText(htmlBody, "ISO-8859-1", "html");

		mp.addBodyPart(plainTextBodyPart);
		mp.addBodyPart(htmlBodyPart);

		message.setContent(mp);

		return message;
	}
}
