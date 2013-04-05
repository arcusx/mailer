/**
 *
 * This software is written by arcus(x) GmbH and subject 
 * to a contract between arcus(x) and its customer.
 *
 * This software stays property of arcus(x) unless differing
 * arrangements between arcus(x) and its customer apply.
 *
 * arcus(x) GmbH
 * Hamburg, Germany
 *
 * Tel.: +49 (0)40.333 102 92 
 * Fax.: +49 (0)40.333 102 93 
 * http://www.arcusx.com
 * mailto:info@arcusx.com
 *
 */

package com.arcusx.mailer.xml;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.codec.binary.Base64;
import org.junit.Before;
import org.junit.Test;

import com.arcusx.mailer.HtmlMessageBody;
import com.arcusx.mailer.Message;
import com.arcusx.mailer.MessageImage;

public class MessageTransformerTest
{

	@Before
	public void setUp() throws Exception
	{
	}

	@Test
	public void testTransformToMessage() throws Exception
	{
		// given
		Message message = new Message();
		Set<String> recipients = new HashSet<String>();
		recipients.add("recipient@test.de");
		message.setRecipients(recipients);
		message.setSender("sender@test.de");
		message.setSubject("Test Message");

		Base64 base64 = new Base64();
		final String encoded = base64.encodeAsString(new byte[] { 10});
		final String encoded2 = base64.encodeAsString(new byte[] { 20});

		String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><Mail><Body><Plain><Text>Test Plain Body</Text></Plain><Html><Text>&lt;html&gt;&lt;body&gt;&lt;h1&gt;Headline&lt;/h1&gt; Test HTML Body&lt;/body&gt;&lt;/html&gt;</Text><Images><Image name=\"/de/logo.png\" type=\"image/png\">"
				+ encoded + "</Image>" +
						"<Image name=\"/de/acknowledge.png\" type=\"image/png\">"
				+ encoded2 + "</Image></Images></Html></Body></Mail>";
		XmlToMessageTransformer messageTransformer = new XmlToMessageTransformer();

		// when
		messageTransformer.transform(xml, message);

		// then
		assertEquals("Test Plain Body", message.getBody());
		assertEquals("<html><body><h1>Headline</h1> Test HTML Body</body></html>", message.getHtmlBody().getHtml());
		final List<MessageImage> images = message.getHtmlBody().getImages();
		assertEquals(2, images.size());
		final MessageImage image = images.get(0);
		assertEquals("/de/logo.png", image.identifier);
		assertEquals("image/png", image.type);
		assertNotNull(image.data);
		assertEquals(1, image.data.length);
		assertEquals(10, image.data[0]);
		
		final MessageImage image2 = images.get(1);
		assertEquals("/de/acknowledge.png", image2.identifier);
		assertEquals("image/png", image2.type);
		assertNotNull(image2.data);
		assertEquals(1, image2.data.length);
		assertEquals(20, image2.data[0]);
	}

}
