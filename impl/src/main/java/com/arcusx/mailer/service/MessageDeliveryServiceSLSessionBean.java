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

import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;

import javax.annotation.Resource;
import javax.annotation.security.PermitAll;
import javax.ejb.EJBException;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.apache.log4j.Logger;

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

	@PersistenceContext
	private EntityManager entityManager;

	@Resource(mappedName = "java:/Mail")
	private Session session;

	public MessageDeliveryServiceSLSessionBean()
	{
	}

	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	@PermitAll
	public void sendMessage(Long notificationId)
	{
		if (logger.isDebugEnabled())
			logger.debug("Looking for messages to be sent...");

		try
		{
			MessageEntity n = this.entityManager.find(MessageEntity.class, notificationId);
			Collection<String> recipients = Arrays.asList(n.getRecipients().split(","));
			sendEmail(recipients, n.getSender(), n.getSubject(), n.getBody());
			n.setSentDate(new Date());
		}
		catch (Exception ex)
		{
			throw new EJBException(ex);
		}
	}

	private void sendEmail(Collection<String> recipients, String sender, String subject, String body) throws Exception
	{
		MimeMessage message = new MimeMessage(session);
		message.setFrom(new InternetAddress(sender));
		for (Iterator<String> iter = recipients.iterator(); iter.hasNext();)
		{
			message.addRecipient(Message.RecipientType.TO, new InternetAddress((String) iter.next()));
		}
		message.setSubject(subject);
		message.setText(body);
		Transport.send(message);
	}
}
