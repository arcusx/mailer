/**
 * 
 */

package com.arcusx.mailer.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.activation.DataHandler;
import javax.mail.Address;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.internet.MimeMessage;

import org.junit.Test;

import com.arcusx.mailer.HtmlMessageBody;

/**
 * @author chwa
 */
public class MimeMessageBuilderTest
{
	@Test
	public void thatBuilderBuildsThrowsExceptionForEmptyBodies() throws Exception
	{
		// given
		String sender = "sender";
		Set<String> recipients = new HashSet<String>(Arrays.asList("recipient1"));
		String subject = "subject";

		MimeMessageBuilder builder = new MimeMessageBuilder(sender, recipients, null, subject, null, null);

		// when
		try
		{
			MimeMessage message = builder.createMimeMessage();
			// then
			fail("No IllegalArgumentException caught, allthough expected.");
		}
		catch (IllegalArgumentException e)
		{
			// ok expected
		}

	}

	@Test
	public void thatBuilderBuildsPlainTextMailForPlainText() throws Exception
	{
		// given
		String sender = "sender";
		Set<String> recipients = new HashSet<String>(Arrays.asList("recipient1"));
		String subject = "subject";
		String plainTextBody = "plainTextBody";
		String replyTo = "replyTo";

		MimeMessageBuilder builder = new MimeMessageBuilder(sender, recipients, replyTo, subject, plainTextBody, null);

		// when
		MimeMessage message = builder.createMimeMessage();

		// then
		final Address[] fromAddresses = message.getFrom();
		assertNotNull(fromAddresses);
		assertEquals(1, fromAddresses.length);
		assertEquals(sender, fromAddresses[0].toString());
		assertEquals(subject, message.getSubject());
		final Address[] recipientAddresses = message.getRecipients(Message.RecipientType.TO);
		assertNotNull(recipientAddresses);
		assertEquals(recipients.size(), recipientAddresses.length);
		assertEquals(replyTo, message.getReplyTo()[0].toString());
		final DataHandler dataHandler = message.getDataHandler();
		assertEquals("text/plain; charset=UTF-8", dataHandler.getContentType());
		Object content = dataHandler.getContent();
		assertEquals(plainTextBody, content);
	}

	@Test
	public void thatBuilderBuildsHtmlMailForHtml() throws Exception
	{
		// given
		String sender = "sender";
		Set<String> recipients = new HashSet<String>(Arrays.asList("recipient1"));
		String subject = "subject";
		HtmlMessageBody htmlBody = new HtmlMessageBody("<html><body>htmlText</body></html>");
		String replyTo = "replyTo";

		MimeMessageBuilder builder = new MimeMessageBuilder(sender, recipients, replyTo, subject, null, htmlBody);

		// when
		MimeMessage message = builder.createMimeMessage();

		// then
		final Address[] fromAddresses = message.getFrom();
		assertNotNull(fromAddresses);
		assertEquals(1, fromAddresses.length);
		assertEquals(sender, fromAddresses[0].toString());
		assertEquals(subject, message.getSubject());
		final Address[] recipientAddresses = message.getRecipients(Message.RecipientType.TO);
		assertNotNull(recipientAddresses);
		assertEquals(recipients.size(), recipientAddresses.length);
		assertEquals(replyTo, message.getReplyTo()[0].toString());
		final DataHandler dataHandler = message.getDataHandler();
		assertEquals("text/html; charset=UTF-8", dataHandler.getContentType());
		Object content = dataHandler.getContent();
		assertEquals(htmlBody.getHtml(), content);
	}

	@Test
	public void thatBuilderBuildsMultipartMails() throws Exception
	{
		// given
		String sender = "sender";
		Set<String> recipients = new HashSet<String>(Arrays.asList("recipient1"));
		String subject = "subject";
		String plainTextBody = "plainTextBody with äöü";
		HtmlMessageBody htmlBody = new HtmlMessageBody("<html><body>htmlText with äöü</body></html>");
		String replyTo = "replyTo";

		MimeMessageBuilder builder = new MimeMessageBuilder(sender, recipients, replyTo, subject, plainTextBody, htmlBody);

		// when
		MimeMessage message = builder.createMimeMessage();

		// then
		final Address[] fromAddresses = message.getFrom();
		assertNotNull(fromAddresses);
		assertEquals(1, fromAddresses.length);
		assertEquals(sender, fromAddresses[0].toString());
		assertEquals(subject, message.getSubject());
		final Address[] recipientAddresses = message.getRecipients(Message.RecipientType.TO);
		assertNotNull(recipientAddresses);
		assertEquals(recipients.size(), recipientAddresses.length);

		final DataHandler dataHandler = message.getDataHandler();
		assertTrue(dataHandler.getContentType().startsWith("multipart/related"));
		Multipart content = (Multipart) dataHandler.getContent();
		assertNotNull(content);

		assertEquals(replyTo, message.getReplyTo()[0].toString());
	}

	@Test
	public void thatBuilderBuildsMultipartMailsWithInlineImages() throws Exception
	{
		// given
		String sender = "sender";
		Set<String> recipients = new HashSet<String>(Arrays.asList("recipient1"));
		String subject = "subject";
		String plainTextBody = "plainTextBody";
		HtmlMessageBody htmlBody = new HtmlMessageBody("<html><body><img src=\"/de/logo.png\"/>htmlText</body></html>");
		final byte[] imageData = new byte[] { 12, 12, 12, 12, 12};
		htmlBody.addInlineImage("/de/logo.png", "image/png", imageData);
		String replyTo = null;

		MimeMessageBuilder builder = new MimeMessageBuilder(sender, recipients, replyTo, subject, plainTextBody, htmlBody);

		// when
		MimeMessage message = builder.createMimeMessage();

		// then
		final Address[] fromAddresses = message.getFrom();
		assertNotNull(fromAddresses);
		assertEquals(1, fromAddresses.length);
		assertEquals(sender, fromAddresses[0].toString());
		assertEquals(subject, message.getSubject());
		final Address[] recipientAddresses = message.getRecipients(Message.RecipientType.TO);
		assertNotNull(recipientAddresses);
		assertEquals(recipients.size(), recipientAddresses.length);

		DataHandler dataHandler = message.getDataHandler();
		assertTrue(dataHandler.getContentType().startsWith("multipart/related"));
		Multipart content = (Multipart) dataHandler.getContent();
		assertNotNull(content);
		assertEquals(2, content.getCount());

		final BodyPart alternativeMultipartBodyPart = content.getBodyPart(0);
		dataHandler = alternativeMultipartBodyPart.getDataHandler();
		assertTrue(dataHandler.getContentType().startsWith("multipart/alternative"));
		Multipart alternativeMultipart = (Multipart) dataHandler.getContent();
		assertNotNull(alternativeMultipart);
		assertEquals(2, alternativeMultipart.getCount());

		final BodyPart plainTextPart = alternativeMultipart.getBodyPart(0);
		assertNotNull(plainTextPart);
		dataHandler = plainTextPart.getDataHandler();
		assertEquals("text/plain; charset=UTF-8", dataHandler.getContentType());
		assertEquals(plainTextBody, dataHandler.getContent());

		final BodyPart htmlPart = alternativeMultipart.getBodyPart(1);
		dataHandler = htmlPart.getDataHandler();
		assertEquals("text/html; charset=UTF-8", dataHandler.getContentType());
		assertEquals("<html><body><img src=\"cid:/de/logo.png\"/>htmlText</body></html>", dataHandler.getContent());

		final BodyPart imagePart = content.getBodyPart(1);
		dataHandler = imagePart.getDataHandler();
		assertEquals("inline", imagePart.getDisposition());
		final String[] header = imagePart.getHeader("Content-ID");
		assertEquals(1, header.length);
		assertEquals("/de/logo.png", header[0]);
		assertEquals("image/png", dataHandler.getContentType());
		assertEquals(imageData, dataHandler.getContent());

		assertEquals(sender, message.getReplyTo()[0].toString());
	}
}
