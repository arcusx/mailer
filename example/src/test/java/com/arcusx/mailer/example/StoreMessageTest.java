
package com.arcusx.mailer.example;

import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;

import javax.naming.Context;
import javax.naming.InitialContext;

import com.arcusx.mailer.MailerService;
import com.arcusx.mailer.Message;

public class StoreMessageTest
{

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception
	{
		Context ctx = new InitialContext();
		MailerService mailerService = (MailerService) ctx.lookup(MailerService.JNDI_NAME);

		Message message = new Message();
		message.setSubject("test" + new Date());
		message.setBody("test");
		message.setSender("test@localhost");
		message.setRecipients(new HashSet<String>(Arrays.asList(new String[] { "c.buschka@arcusx.com", "conni@arcusx.lan"})));
		message.setReplyTo("c.buschka@gmx.de");

		mailerService.storeMessage(message);
	}
}
