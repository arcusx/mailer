/**
 * 
 */

package com.arcusx.mailer.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.activation.DataHandler;
import javax.mail.BodyPart;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.Session;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import com.arcusx.mailer.HtmlMessageBody;
import com.arcusx.mailer.Message;
import com.arcusx.mailer.MessageAttachment;
import com.arcusx.mailer.MessageImage;

public class MimeMessageBuilder
{
	private static final String CONTENT_TYPE = "Content-Type";
	private static final String TEXT_PLAIN_CHARSET_UTF8_FORMAT_FLOWED = "text/plain; charset=UTF-8; format=flowed";
	private static final String TEXT_HTML_CHARSET_UTF8_FORMAT_FLOWED = "text/html; charset=UTF-8; format=flowed";

	private static final String MIME_SUBTYPE_RELATED = "related";
	private static final String MIME_SUBTYPE_ALTERNATIVE = "alternative";
	private static final String MIME_SUBTYPE_MIXED = "mixed";

	MimeMessage createMimeMessage(Message message) throws MessagingException, IOException
	{
		Object messageBody = buildMessageContentFor(message);

		if (message.hasAttachments())
			messageBody = wrapAsMixedWithAttachments(messageBody, message.getMessageAttachments());

		return buildMessageWithBodyAndHeaders(messageBody, message);
	}

	public byte[] createMimeMessageAsBytes(Message message) throws MessagingException, IOException
	{
		MimeMessage mimeMessage = createMimeMessage(message);

		return toBytes(mimeMessage);
	}

	private byte[] toBytes(MimeMessage mimeMessage) throws IOException, MessagingException
	{
		ByteArrayOutputStream bytesOut = new ByteArrayOutputStream();
		mimeMessage.writeTo(bytesOut);
		bytesOut.close();
		return bytesOut.toByteArray();
	}

	private MimeMessage buildMessageWithBodyAndHeaders(Object messageBody, Message message) throws AddressException, MessagingException, IOException
	{
		MimeMessage mimeMessage = new MimeMessage((Session) null);
		fillAddressesAndSubject(mimeMessage, message);

		if (messageBody instanceof Multipart)
		{
			mimeMessage.setContent((Multipart) messageBody);
		}
		else if (messageBody instanceof BodyPart)
		{
			BodyPart bodyPart = (BodyPart) messageBody;
			Object content = bodyPart.getContent();
			String contentType = bodyPart.getContentType();
			mimeMessage.setContent(content, contentType);
		}
		else
		{
			throw new IllegalStateException("Message body (" + messageBody + ") is neither body part nor multipart.");
		}

		return mimeMessage;
	}

	private void fillAddressesAndSubject(MimeMessage mimeMessage, Message message) throws MessagingException, AddressException
	{
		fillSender(mimeMessage, message);

		fillRecipients(mimeMessage, message);

		fillCcRecipients(mimeMessage, message);

		fillSubject(mimeMessage, message);

		fillReplyTo(mimeMessage, message);
	}

	private void fillSender(MimeMessage mimeMessage, Message message) throws MessagingException, AddressException
	{
		String sender = message.getSender();
		mimeMessage.setFrom(new InternetAddress(sender));
	}

	private void fillRecipients(MimeMessage mimeMessage, Message message) throws MessagingException, AddressException
	{
		Set<String> recipients = message.getRecipients();
		for (String recipient : recipients)
		{
			mimeMessage.addRecipient(javax.mail.Message.RecipientType.TO, new InternetAddress(recipient));
		}
	}

	private void fillCcRecipients(MimeMessage mimeMessage, Message message) throws MessagingException, AddressException
	{
		Set<String> ccRecipients = message.getCcRecipients();
		for (String ccRecipient : ccRecipients)
		{
			mimeMessage.addRecipient(javax.mail.Message.RecipientType.CC, new InternetAddress(ccRecipient));
		}
	}

	private void fillSubject(MimeMessage mimeMessage, Message message) throws MessagingException
	{
		String subject = message.getSubject();
		mimeMessage.setSubject(subject);
	}

	private void fillReplyTo(MimeMessage mimeMessage, Message message) throws MessagingException, AddressException
	{
		String replyTo = message.getReplyTo();
		if (replyTo != null)
		{
			mimeMessage.setReplyTo(new InternetAddress[] { new InternetAddress(replyTo)});
		}
	}

	private Object buildMessageContentFor(Message message) throws MessagingException
	{
		boolean htmlImages = message.getHtmlBody() != null && message.getHtmlBody().hasImages();

		Object messageBody = buildMessageBodyFor(message);
		if (htmlImages)
		{
			Multipart multipart = wrapAsRelatedWithEmbeddedImages(messageBody, message.getHtmlBody().getImages());
			return multipart;
		}
		else
		{
			return messageBody;
		}
	}

	private Object buildMessageBodyFor(Message message) throws MessagingException
	{
		boolean plain = message.getBody() != null;
		boolean html = message.getHtmlBody() != null;

		if (plain && html)
		{
			MimeMultipart multipart = wrapAsAlternative(createPlainFor(message), createHtmlFor(message.getHtmlBody()));
			return multipart;
		}
		else if (plain)
			return createPlainFor(message);
		else if (html)
			return createHtmlFor(message.getHtmlBody());
		else
			throw new IllegalArgumentException("Neither plain text body nor html body. Can't send empty mail.");
	}

	private Multipart wrapAsMixedWithAttachments(Object content, Iterable<MessageAttachment> messageAttachments) throws MessagingException
	{
		if (content instanceof Multipart)
			return wrapAsMixedWithAttachments((Multipart) content, messageAttachments);
		else if (content instanceof BodyPart)
			return wrapAsMixedWithAttachments((BodyPart) content, messageAttachments);
		else
			throw new IllegalStateException("Content is neither BodyPart nor Multipart (content=" + content + ").");
	}

	private Multipart wrapAsMixedWithAttachments(Multipart content, Iterable<MessageAttachment> messageAttachments) throws MessagingException
	{
		return wrapAsMixedWithAttachments(wrapAsBodyPart(content), messageAttachments);
	}

	private Multipart wrapAsMixedWithAttachments(BodyPart messageBody, Iterable<MessageAttachment> messageAttachments) throws MessagingException
	{
		MimeMultipart mixedMultipart = new MimeMultipart(MIME_SUBTYPE_MIXED);
		mixedMultipart.addBodyPart(messageBody);

		List<MimeBodyPart> attachmentBodyParts = createAttachmentBodyParts(messageAttachments);
		addBodyParts(mixedMultipart, attachmentBodyParts);

		return mixedMultipart;
	}

	private MimeBodyPart wrapAsBodyPart(Multipart multipart) throws MessagingException
	{
		MimeBodyPart multipartBodyPart = new MimeBodyPart();
		multipartBodyPart.setContent(multipart);
		return multipartBodyPart;
	}

	private Multipart wrapAsRelatedWithEmbeddedImages(Object content, List<MessageImage> images) throws MessagingException
	{
		if (content instanceof Multipart)
			return wrapAsRelatedWithEmbeddedImages((Multipart) content, images);
		else if (content instanceof BodyPart)
			return wrapAsRelatedWithEmbeddedImages((BodyPart) content, images);
		else
			throw new IllegalStateException("Content is neither BodyPart nor Multipart (content=" + content + ").");
	}

	private Multipart wrapAsRelatedWithEmbeddedImages(Multipart content, List<MessageImage> images) throws MessagingException
	{
		return wrapAsRelatedWithEmbeddedImages(wrapAsBodyPart(content), images);
	}

	private Multipart wrapAsRelatedWithEmbeddedImages(BodyPart content, List<MessageImage> images) throws MessagingException
	{
		MimeMultipart relatedMultipart = new MimeMultipart(MIME_SUBTYPE_RELATED);
		relatedMultipart.addBodyPart(content);

		List<MimeBodyPart> imageBodyParts = createImageBodyParts(images);
		addBodyParts(relatedMultipart, imageBodyParts);

		return relatedMultipart;
	}

	private void addBodyParts(MimeMultipart multipart, List<MimeBodyPart> bodyParts) throws MessagingException
	{
		for (MimeBodyPart mimeBodyPart : bodyParts)
		{
			multipart.addBodyPart(mimeBodyPart);
		}
	}

	private MimeMultipart wrapAsAlternative(MimeBodyPart plainPart, MimeBodyPart htmlPart) throws MessagingException
	{
		MimeMultipart alternativeMultipart = new MimeMultipart(MIME_SUBTYPE_ALTERNATIVE);
		alternativeMultipart.addBodyPart(plainPart);
		alternativeMultipart.addBodyPart(htmlPart);
		return alternativeMultipart;
	}

	private MimeBodyPart createHtmlFor(HtmlMessageBody body) throws MessagingException
	{
		MimeBodyPart htmlTextBodyPart = new MimeBodyPart();
		HtmlImageIdExpander expander = new HtmlImageIdExpander(body.getHtml(), body.getImages());
		htmlTextBodyPart.setText(expander.getHtmlWithImageIdsExpanded());
		htmlTextBodyPart.addHeader(CONTENT_TYPE, TEXT_HTML_CHARSET_UTF8_FORMAT_FLOWED);
		return htmlTextBodyPart;
	}

	private MimeBodyPart createPlainFor(Message message) throws MessagingException
	{
		MimeBodyPart plainTextBodyPart = new MimeBodyPart();
		plainTextBodyPart.setText(message.getBody());
		plainTextBodyPart.addHeader(CONTENT_TYPE, TEXT_PLAIN_CHARSET_UTF8_FORMAT_FLOWED);
		return plainTextBodyPart;
	}

	private List<MimeBodyPart> createImageBodyParts(List<MessageImage> images) throws MessagingException
	{
		List<MimeBodyPart> imageBodyParts = new ArrayList<MimeBodyPart>();
		for (MessageImage image : images)
		{
			MimeBodyPart imageBodyPart = new MimeBodyPart();
			imageBodyPart.setDataHandler(new DataHandler(image.getData(), image.getContentType()));
			imageBodyPart.setContentID(image.identifier);
			imageBodyPart.setDisposition(Part.INLINE);
			imageBodyPart.setFileName(image.getName());
			imageBodyParts.add(imageBodyPart);
		}
		return imageBodyParts;
	}

	private List<MimeBodyPart> createAttachmentBodyParts(Iterable<MessageAttachment> attachments) throws MessagingException
	{
		List<MimeBodyPart> attachmentBodyParts = new ArrayList<MimeBodyPart>();
		for (MessageAttachment attachment : attachments)
		{
			MimeBodyPart attachmentBodyPart = new MimeBodyPart();
			attachmentBodyPart.setDataHandler(new DataHandler(attachment.getData(), attachment.getContentType()));
			attachmentBodyPart.setDisposition(Part.ATTACHMENT);
			attachmentBodyPart.setFileName(attachment.getName());
			attachmentBodyParts.add(attachmentBodyPart);
		}
		return attachmentBodyParts;
	}
}
