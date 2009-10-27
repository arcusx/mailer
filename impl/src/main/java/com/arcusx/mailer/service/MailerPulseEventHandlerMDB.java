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
import java.util.List;

import javax.ejb.ActivationConfigProperty;
import javax.ejb.EJB;
import javax.ejb.EJBException;
import javax.ejb.MessageDriven;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.apache.log4j.Logger;

import com.arcusx.pulse.PulseEvent;
import com.arcusx.pulse.PulseEventHandler;

/**
 *
 * @author conni
 * @version $Id$
 */
@MessageDriven(name = "MailerPulseEventHandlerMDB", //
messageListenerInterface = PulseEventHandler.class, //
activationConfig = { //
@ActivationConfigProperty(propertyName = "ScheduleExpr", propertyValue = "0 * * * * *")} //
)
public class MailerPulseEventHandlerMDB implements PulseEventHandler
{
	private static Logger logger = Logger.getLogger(MailerPulseEventHandlerMDB.class);

	@PersistenceContext
	private EntityManager entityManager;

	@EJB
	private MessageDeliveryService messageSendService;

	public MailerPulseEventHandlerMDB()
	{
	}

	public void handleEvent(PulseEvent ev)
	{
		try
		{
			@SuppressWarnings("unchecked")
			List<BigInteger> messageIds = this.entityManager.createNativeQuery(
					"select m.message_id from mailer.message m where sent_date is null").getResultList();

			if (logger.isInfoEnabled())
				logger.info("Sending " + messageIds.size() + " messages(s)...");

			for (BigInteger notificationId : messageIds)
			{
				this.messageSendService.sendMessage(Long.valueOf(notificationId.longValue()));
			}
		}
		catch (Exception ex)
		{
			logger.error("Sending notification failed.", ex);

			throw new EJBException(ex);
		}
	}
}
