/**
 * 
 */

package com.arcusx.mailer.batch;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.activation.DataHandler;
import javax.mail.MessagingException;
import javax.mail.Part;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import com.arcusx.mailer.HtmlMessageBody;
import com.arcusx.mailer.MessageImage;

/**
 * @author chwa
 *
 */
public class MimeMessageBuilder
{
	private final Session session;
	private final String sender;
	private final Set<String> recipients;
	private final String subject;
	private final String plainTextBody;
	private final HtmlMessageBody htmlBody;

	public MimeMessageBuilder(Session session, String sender, Set<String> recipients, String subject,
			String plainTextBody)
	{
		this(session, sender, recipients, subject, plainTextBody, null);
	}

	public MimeMessageBuilder(Session session, String sender, Set<String> recipients, String subject,
			HtmlMessageBody htmlBody)
	{
		this(session, sender, recipients, subject, null, htmlBody);
	}

	public MimeMessageBuilder(Session session, String sender, Set<String> recipients, String subject,
			String plainTextBody, HtmlMessageBody htmlBody)
	{
		super();
		this.session = session;
		this.sender = sender;
		this.recipients = recipients;
		this.subject = subject;
		this.plainTextBody = plainTextBody;
		this.htmlBody = htmlBody;
	}

	public MimeMessage createMimeMessage() throws MessagingException
	{
		MimeMessage message = null;

		boolean hasPlainTextBody = plainTextBody != null && plainTextBody.trim().length() > 0;
		boolean hasHtmlBody = htmlBody != null;
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

	private MimeMessage createPlainTextEmail() throws MessagingException
	{
		MimeMessage message = new MimeMessage(session);
		message.setFrom(new InternetAddress(sender));
		for (Iterator<String> iter = recipients.iterator(); iter.hasNext();)
		{
			message.addRecipient(javax.mail.Message.RecipientType.TO, new InternetAddress(iter.next()));
		}
		message.setSubject(subject);
		message.setText(plainTextBody, "UTF-8");

		return message;
	}

	private MimeMessage createHtmlOnlyEmail() throws MessagingException
	{
		MimeMessage message = new MimeMessage(session);
		message.setFrom(new InternetAddress(sender));
		message.addHeader("Content-Type", "text/html; charset=UTF-8");
		for (Iterator<String> iter = recipients.iterator(); iter.hasNext();)
		{
			message.addRecipient(javax.mail.Message.RecipientType.TO, new InternetAddress(iter.next()));
		}
		message.setSubject(subject);
		message.setText(htmlBody.getHtml(), "UTF-8", "html");

		return message;
	}

	private MimeMessage createMultiPartEmail() throws MessagingException
	{
		MimeMessage message = new MimeMessage(session);
		message.setFrom(new InternetAddress(sender));
		for (Iterator<String> iter = recipients.iterator(); iter.hasNext();)
		{
			message.addRecipient(javax.mail.Message.RecipientType.TO, new InternetAddress(iter.next()));
		}
		message.setSubject(subject);

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
		String html = htmlBody.getHtml();
		final List<MessageImage> images = htmlBody.getImages();
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
		final List<MessageImage> images = htmlBody.getImages();
		for (MessageImage image : images)
		{
			MimeBodyPart imageBodyPart = new MimeBodyPart();
			imageBodyPart.setDataHandler(new DataHandler(image.data, image.type));
			imageBodyPart.setHeader("Content-ID", image.identifier);
			imageBodyPart.setDisposition(Part.INLINE);
			imageBodyParts.add(imageBodyPart);
		}
		return imageBodyParts;
	}

	private MimeBodyPart createPlainTextBodyPart() throws MessagingException
	{
		MimeBodyPart plainTextBodyPart = new MimeBodyPart();
		plainTextBodyPart.addHeader("Content-Type", "text/plain; charset=UTF-8; format=flowed");
		plainTextBodyPart.setText(plainTextBody, "UTF-8");
		return plainTextBodyPart;
	}

}
