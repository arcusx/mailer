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

import java.util.HashSet;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import com.arcusx.mailer.Message;

import static org.junit.Assert.assertEquals;

public class MessageTransformerTest
{

	@Before
	public void setUp() throws Exception
	{
	}

	@Test
	public void testTransformToXmlString() throws Exception
	{
		// given
		Message message = new Message();
		message.setBody("Test Plain Body");
		message.setHtmlBody("<html><body><h1>Headline</h1> Test HTML Body</body></html>");
		Set<String> recipients = new HashSet<String>();
		recipients.add("recipient@test.de");
		message.setRecipients(recipients);
		message.setSender("sender@test.de");
		message.setSubject("Test Message");
		
		MessageToXmlTransformer messageTransformer = new MessageToXmlTransformer();

		// when
		String transform = messageTransformer.transform(message);
		
		// then
		assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\"?><Mail><Body><Plain><Text>Test Plain Body</Text></Plain><Html><Text>&lt;html&gt;&lt;body&gt;&lt;h1&gt;Headline&lt;/h1&gt; Test HTML Body&lt;/body&gt;&lt;/html&gt;</Text></Html></Body></Mail>", transform);
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

		String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><Mail><Body><Plain><Text>Test Plain Body</Text></Plain><Html><Text>&lt;html&gt;&lt;body&gt;&lt;h1&gt;Headline&lt;/h1&gt; Test HTML Body&lt;/body&gt;&lt;/html&gt;</Text></Html></Body></Mail>";
		XmlToMessageTransformer messageTransformer = new XmlToMessageTransformer();

		// when
		messageTransformer.transform(xml, message);
		
		// then
		assertEquals("Test Plain Body", message.getBody());
		assertEquals("<html><body><h1>Headline</h1> Test HTML Body</body></html>", message.getHtmlBody());
	}
	
}
