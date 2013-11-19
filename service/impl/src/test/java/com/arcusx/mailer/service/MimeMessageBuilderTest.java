/**
 * 
 */

package com.arcusx.mailer.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.Collection;
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
	MimeMessageBuilder builder = new MimeMessageBuilder();

	@Test
	public void thatBuilderBuildsThrowsExceptionForEmptyBodies() throws Exception
	{
		// given
		com.arcusx.mailer.Message message = createMessage("sender", Arrays.asList("recipient1"), null, "subject", null, null);

		// when
		try
		{
			this.builder.createMimeMessage(message);
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

		com.arcusx.mailer.Message message = createMessage(sender, recipients, replyTo, subject, plainTextBody, null);

		// when
		MimeMessage mimeMessage = this.builder.createMimeMessage(message);

		// then
		final Address[] fromAddresses = mimeMessage.getFrom();
		assertNotNull(fromAddresses);
		assertEquals(1, fromAddresses.length);
		assertEquals(sender, fromAddresses[0].toString());
		assertEquals(subject, mimeMessage.getSubject());
		final Address[] recipientAddresses = mimeMessage.getRecipients(Message.RecipientType.TO);
		assertNotNull(recipientAddresses);
		assertEquals(recipients.size(), recipientAddresses.length);
		assertEquals(replyTo, mimeMessage.getReplyTo()[0].toString());
		final DataHandler dataHandler = mimeMessage.getDataHandler();
		assertEquals("text/plain; charset=UTF-8; format=flowed", dataHandler.getContentType());
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

		com.arcusx.mailer.Message message = createMessage(sender, recipients, replyTo, subject, null, htmlBody);

		// when
		MimeMessage mimeMessage = this.builder.createMimeMessage(message);

		// then
		final Address[] fromAddresses = mimeMessage.getFrom();
		assertNotNull(fromAddresses);
		assertEquals(1, fromAddresses.length);
		assertEquals(sender, fromAddresses[0].toString());
		assertEquals(subject, mimeMessage.getSubject());
		final Address[] recipientAddresses = mimeMessage.getRecipients(Message.RecipientType.TO);
		assertNotNull(recipientAddresses);
		assertEquals(recipients.size(), recipientAddresses.length);
		assertEquals(replyTo, mimeMessage.getReplyTo()[0].toString());
		final DataHandler dataHandler = mimeMessage.getDataHandler();
		assertEquals("text/html; charset=UTF-8; format=flowed", dataHandler.getContentType());
		Object content = dataHandler.getContent();
		assertEquals(htmlBody.getHtml(), content);

	}

	@Test
	public void thatBuilderBuildsMultipartAlternativeMailsForHtmlAndPlainText() throws Exception
	{
		// given
		String sender = "sender";
		Set<String> recipients = new HashSet<String>(Arrays.asList("recipient1"));
		String subject = "subject";
		String plainTextBody = "plainTextBody with äöü";
		HtmlMessageBody htmlBody = new HtmlMessageBody("<html><body>htmlText with äöü</body></html>");
		String replyTo = "replyTo";

		com.arcusx.mailer.Message message = createMessage(sender, recipients, replyTo, subject, plainTextBody, htmlBody);

		// when
		MimeMessage mimeMessage = this.builder.createMimeMessage(message);

		// then
		final Address[] fromAddresses = mimeMessage.getFrom();
		assertNotNull(fromAddresses);
		assertEquals(1, fromAddresses.length);
		assertEquals(sender, fromAddresses[0].toString());
		assertEquals(subject, mimeMessage.getSubject());
		final Address[] recipientAddresses = mimeMessage.getRecipients(Message.RecipientType.TO);
		assertNotNull(recipientAddresses);
		assertEquals(recipients.size(), recipientAddresses.length);

		String expectedContentType = "multipart/alternative";
		String actualContentType = mimeMessage.getDataHandler().getContentType();
		assertContentTypeStartsWith(expectedContentType, actualContentType);
		Multipart content = (Multipart) mimeMessage.getDataHandler().getContent();
		assertNotNull(content);

		assertEquals(replyTo, mimeMessage.getReplyTo()[0].toString());

	}

	@Test
	public void thatBuilderBuildsMultipartMixedMailsPlainTextWithAttachments() throws Exception
	{
		// given
		String sender = "sender";
		Set<String> recipients = new HashSet<String>(Arrays.asList("recipient1"));
		String subject = "subject";
		String plainTextBody = "plainTextBody";
		String replyTo = "replyTo";

		com.arcusx.mailer.Message message = createMessage(sender, recipients, replyTo, subject, plainTextBody, null);
		message.addMessageAttachment("attachment.pdf", "application/pdf", new byte[] {
				12,
				13,
				14,
				15,
				16});

		// when
		MimeMessage mimeMessage = this.builder.createMimeMessage(message);

		// then
		final Address[] fromAddresses = mimeMessage.getFrom();
		assertNotNull(fromAddresses);
		assertEquals(1, fromAddresses.length);
		assertEquals(sender, fromAddresses[0].toString());
		assertEquals(subject, mimeMessage.getSubject());
		final Address[] recipientAddresses = mimeMessage.getRecipients(Message.RecipientType.TO);
		assertNotNull(recipientAddresses);
		assertEquals(recipients.size(), recipientAddresses.length);

		String expectedContentType = "multipart/mixed";
		String actualContentType = mimeMessage.getDataHandler().getContentType();
		assertContentTypeStartsWith(expectedContentType, actualContentType);
		Multipart content = (Multipart) mimeMessage.getContent();
		assertNotNull(content);

		assertEquals(replyTo, mimeMessage.getReplyTo()[0].toString());

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
		final byte[] imageData = new byte[] {
				12,
				12,
				12,
				12,
				12};
		htmlBody.addInlineImage("/de/logo.png", "image/png", imageData);
		String replyTo = null;

		com.arcusx.mailer.Message message = createMessage(sender, recipients, replyTo, subject, plainTextBody, htmlBody);

		// when
		MimeMessage mimeMessage = this.builder.createMimeMessage(message);

		// then
		final Address[] fromAddresses = mimeMessage.getFrom();
		assertNotNull(fromAddresses);
		assertEquals(1, fromAddresses.length);
		assertEquals(sender, fromAddresses[0].toString());
		assertEquals(subject, mimeMessage.getSubject());
		final Address[] recipientAddresses = mimeMessage.getRecipients(Message.RecipientType.TO);
		assertNotNull(recipientAddresses);
		assertEquals(recipients.size(), recipientAddresses.length);

		String expectedContentType = "multipart/related";
		String actualContentType = mimeMessage.getDataHandler().getContentType();
		assertContentTypeStartsWith(expectedContentType, actualContentType);
		Multipart content = (Multipart) mimeMessage.getDataHandler().getContent();
		assertNotNull(content);
		assertEquals(2, content.getCount());

		final BodyPart alternativeMultipartBodyPart = content.getBodyPart(0);
		expectedContentType = "multipart/alternative";
		actualContentType = alternativeMultipartBodyPart.getDataHandler().getContentType();
		assertContentTypeStartsWith(expectedContentType, actualContentType);
		Multipart alternativeMultipart = (Multipart) alternativeMultipartBodyPart.getDataHandler().getContent();
		assertNotNull(alternativeMultipart);
		assertEquals(2, alternativeMultipart.getCount());

		final BodyPart plainTextPart = alternativeMultipart.getBodyPart(0);
		assertNotNull(plainTextPart);
		assertEquals("text/plain; charset=UTF-8; format=flowed", plainTextPart.getContentType());
		assertEquals(plainTextBody, plainTextPart.getContent());

		final BodyPart htmlPart = alternativeMultipart.getBodyPart(1);
		assertEquals("text/html; charset=UTF-8; format=flowed", htmlPart.getContentType());
		assertEquals("<html><body><img src=\"cid:/de/logo.png\"/>htmlText</body></html>", htmlPart.getContent());

		final BodyPart imagePart = content.getBodyPart(1);
		assertEquals("inline", imagePart.getDisposition());
		final String[] header = imagePart.getHeader("Content-ID");
		assertEquals(1, header.length);
		assertEquals("/de/logo.png", header[0]);
		assertEquals("image/png", imagePart.getDataHandler().getContentType());
		assertEquals(imageData, imagePart.getDataHandler().getContent());

		assertEquals(sender, mimeMessage.getReplyTo()[0].toString());

	}

	private static void assertContentTypeStartsWith(String expectedContentType, String actualContentType)
	{
		assertTrue(actualContentType + " should start with " + expectedContentType, actualContentType.startsWith(expectedContentType));
	}

	private com.arcusx.mailer.Message createMessage(String sender, Collection<String> recipients, String replyTo, String subject, String plainTextBody,
			HtmlMessageBody htmlBody)
	{
		com.arcusx.mailer.Message message = new com.arcusx.mailer.Message();
		message.setSender(sender);
		message.setReplyTo(replyTo);
		message.setBody(plainTextBody);
		message.setHtmlBody(htmlBody);

		for (String recipient : recipients)
		{
			message.addRecipient(recipient);
		}
		message.setSubject(subject);
		return message;
	}
}
