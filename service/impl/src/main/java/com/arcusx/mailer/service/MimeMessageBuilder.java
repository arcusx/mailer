/**
 * 
 */

package com.arcusx.mailer.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.activation.DataHandler;
import javax.mail.MessagingException;
import javax.mail.Part;
import javax.mail.Session;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import com.arcusx.mailer.HtmlMessageBody;
import com.arcusx.mailer.MessageImage;

public class MimeMessageBuilder
{
	private final String sender;
	private final Set<String> recipients;
	private final String replyTo;
	private final String subject;
	private final String plainTextBody;
	private final HtmlMessageBody htmlBody;
	private final Set<String> ccRecipients;

	public MimeMessageBuilder(String sender, Set<String> recipients, String replyTo, String subject, String plainTextBody, HtmlMessageBody htmlBody)
	{
		this(sender, recipients, Collections.<String> emptySet(), replyTo, subject, plainTextBody, htmlBody);
	}

	public MimeMessageBuilder(String sender, Set<String> recipients, Set<String> ccRecipients, String replyTo, String subject, String plainTextBody,
			HtmlMessageBody htmlBody)
	{
		this.sender = sender;
		this.recipients = recipients;
		this.ccRecipients = ccRecipients;
		this.replyTo = replyTo;
		this.subject = subject;
		this.plainTextBody = plainTextBody;
		this.htmlBody = htmlBody;
	}

	public byte[] createMimeMessageAsBytes() throws MessagingException, IOException
	{
		MimeMessage mimeMessage = createMimeMessage();
		ByteArrayOutputStream bytesOut = new ByteArrayOutputStream();
		mimeMessage.writeTo(bytesOut);
		bytesOut.close();
		return bytesOut.toByteArray();
	}

	public MimeMessage createMimeMessage() throws MessagingException
	{
		if (this.sender == null)
			throw new IllegalArgumentException("Sender is null.");
		if (this.subject == null)
			throw new IllegalArgumentException("Subject is null.");

		MimeMessage message = null;

		boolean hasPlainTextBody = notNullAndNotEmpty(this.plainTextBody);
		boolean hasHtmlBody = this.htmlBody != null;
		if (hasPlainTextBody && !hasHtmlBody)
		{
			message = createPlainTextEmail();
		}
		else if (!hasPlainTextBody && hasHtmlBody)
		{
			message = createHtmlOnlyEmail();
		}
		else if (hasPlainTextBody && hasHtmlBody)
		{
			message = createMultiPartEmail();
		}
		else
		{
			throw new IllegalArgumentException("Neither plain text body nor html body. Can't send empty mail.");
		}
		return message;
	}

	public boolean notNullAndNotEmpty(final String text)
	{
		return text != null && !text.trim().isEmpty();
	}

	private MimeMessage createPlainTextEmail() throws MessagingException
	{
		MimeMessage message = new MimeMessage((Session) null);
		fillAddressesAndSubject(message);
		message.setText(this.plainTextBody, "UTF-8");

		return message;
	}

	private MimeMessage createHtmlOnlyEmail() throws MessagingException
	{
		MimeMessage message = new MimeMessage((Session) null);
		fillAddressesAndSubject(message);
		message.addHeader("Content-Type", "text/html; charset=UTF-8");
		message.setText(this.htmlBody.getHtml(), "UTF-8", "html");

		return message;
	}

	private MimeMessage createMultiPartEmail() throws MessagingException
	{
		MimeMessage message = new MimeMessage((Session) null);
		fillAddressesAndSubject(message);

		MimeBodyPart plainTextBodyPart = createPlainTextBodyPart();
		MimeBodyPart htmlBodyPart = createHtmlRelatedMultipartBodyPart();

		MimeMultipart alternativeMultipart = new MimeMultipart();
		alternativeMultipart.setSubType("alternative");

		alternativeMultipart.addBodyPart(plainTextBodyPart);
		alternativeMultipart.addBodyPart(htmlBodyPart);

		MimeBodyPart alternativeMultipartBodyPart = new MimeBodyPart();
		alternativeMultipartBodyPart.setContent(alternativeMultipart);

		MimeMultipart relatedMultipart = new MimeMultipart();
		relatedMultipart.setSubType("related");
		relatedMultipart.addBodyPart(alternativeMultipartBodyPart);

		List<MimeBodyPart> createImageBodyParts = createImageBodyParts();
		for (MimeBodyPart mimeBodyPart : createImageBodyParts)
		{
			relatedMultipart.addBodyPart(mimeBodyPart);
		}

		message.setContent(relatedMultipart);

		return message;
	}

	private MimeBodyPart createHtmlRelatedMultipartBodyPart() throws MessagingException
	{
		MimeBodyPart htmlBodyPart = new MimeBodyPart();
		htmlBodyPart.addHeader("Content-Type", "text/html; charset=UTF-8");
		String html = this.htmlBody.getHtml();
		final List<MessageImage> images = this.htmlBody.getImages();
		for (MessageImage image : images)
		{
			html = html.replace(image.identifier, "cid:" + image.identifier);
		}
		htmlBodyPart.setText(html, "UTF-8", "html");
		return htmlBodyPart;
	}

	private List<MimeBodyPart> createImageBodyParts() throws MessagingException
	{
		List<MimeBodyPart> imageBodyParts = new ArrayList<MimeBodyPart>();
		final List<MessageImage> images = this.htmlBody.getImages();
		for (MessageImage image : images)
		{
			MimeBodyPart imageBodyPart = new MimeBodyPart();
			imageBodyPart.setDataHandler(new DataHandler(image.data, image.type));
			imageBodyPart.setContentID(image.identifier);
			imageBodyPart.setDisposition(Part.INLINE);
			imageBodyPart.setFileName(image.name);
			imageBodyParts.add(imageBodyPart);
		}
		return imageBodyParts;
	}

	private MimeBodyPart createPlainTextBodyPart() throws MessagingException
	{
		MimeBodyPart plainTextBodyPart = new MimeBodyPart();
		plainTextBodyPart.addHeader("Content-Type", "text/plain; charset=UTF-8; format=flowed");
		plainTextBodyPart.setText(this.plainTextBody, "UTF-8");
		return plainTextBodyPart;
	}

	private void fillAddressesAndSubject(MimeMessage message) throws MessagingException, AddressException
	{
		message.setFrom(new InternetAddress(this.sender));

		for (Iterator<String> iter = this.recipients.iterator(); iter.hasNext();)
		{
			message.addRecipient(javax.mail.Message.RecipientType.TO, new InternetAddress(iter.next()));
		}

		for (Iterator<String> iter = this.ccRecipients.iterator(); iter.hasNext();)
		{
			message.addRecipient(javax.mail.Message.RecipientType.CC, new InternetAddress(iter.next()));
		}

		message.setSubject(this.subject);

		if (this.replyTo != null)
		{
			message.setReplyTo(new InternetAddress[] { new InternetAddress(this.replyTo)});
		}
	}
}
