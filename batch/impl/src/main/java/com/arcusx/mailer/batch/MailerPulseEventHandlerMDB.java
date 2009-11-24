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

import java.util.List;

import javax.ejb.ActivationConfigProperty;
import javax.ejb.EJB;
import javax.ejb.EJBException;
import javax.ejb.MessageDriven;

import org.apache.log4j.Logger;

import com.arcusx.mailer.MessageManager;
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

	@EJB(mappedName = MessageManager.JNDI_NAME)
	private MessageManager messageManager;

	@EJB
	private MessageDeliveryService messageDeliveryService;

	public MailerPulseEventHandlerMDB()
	{
	}

	public void handleEvent(PulseEvent ev)
	{
		if (logger.isDebugEnabled())
			logger.debug("Looking for messages to be sent...");

		List<Long> unsentMessageIds = null;
		try
		{
			unsentMessageIds = this.messageManager.fetchUndeliveredMessageIds();
		}
		catch (Exception ex)
		{
			logger.error("Selecting messages failed.", ex);

			throw new EJBException(ex);
		}

		if (logger.isDebugEnabled())
			logger.debug(unsentMessageIds.size() + " message(s) to be sent...");

		for (Long currMessageId : unsentMessageIds)
		{
			try
			{
				this.messageDeliveryService.sendMessage(currMessageId);
			}
			catch (Exception ex)
			{
				logger.error("Sending message " + currMessageId + " failed.", ex);
			}
		}

	}
}
