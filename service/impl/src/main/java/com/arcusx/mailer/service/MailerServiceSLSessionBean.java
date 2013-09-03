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

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.annotation.security.PermitAll;
import javax.ejb.EJBException;
import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.sql.DataSource;

import org.jboss.annotation.ejb.RemoteBinding;

import com.arcusx.mailer.MailerException;
import com.arcusx.mailer.MailerService;
import com.arcusx.mailer.Message;

/**
 *
 * @author conni
 * @version $Id$
 */
@Stateless
@Remote(MailerService.class)
@RemoteBinding(jndiBinding = MailerService.JNDI_NAME)
// @SecurityDomain
public class MailerServiceSLSessionBean implements MailerService
{
	@Resource(mappedName = "java:/DefaultDS")
	private DataSource dataSource;

	private MessageStore messageStore;

	public MailerServiceSLSessionBean()
	{
	}

	@PostConstruct
	private void init()
	{
		this.messageStore = new MessageStore(this.dataSource);
	}

	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@PermitAll
	public void storeMessage(Message message) throws MailerException
	{
		try
		{
			byte[] mimeMessageBytes = new MimeMessageBuilder(message.getSender(), message.getRecipients(),
					message.getCcRecipients(), message.getReplyTo(), message.getSubject(), message.getBody(),
					message.getHtmlBody()).createMimeMessageAsBytes();
			this.messageStore.storeMessage(mimeMessageBytes);
		}
		catch (Exception ex)
		{
			throw new EJBException(ex);
		}
	}
}
