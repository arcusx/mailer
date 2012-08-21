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

/**
 * 
 */

package com.arcusx.mailer.xml;

import java.io.StringWriter;

import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;

import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

import com.arcusx.mailer.Message;

/**
 * Created on Aug 20, 2012.
 * 
 * @author sven
 */
public class MessageToXmlTransformer
{
	public String transform(Message message) throws Exception
	{
		StringWriter strWr = new StringWriter();

		SAXTransformerFactory saxTrFactory = (SAXTransformerFactory) SAXTransformerFactory.newInstance();
		TransformerHandler trHandler = saxTrFactory.newTransformerHandler();
		trHandler.setResult(new StreamResult(strWr));
		ContentHandler cHandler = trHandler;
		cHandler.startDocument();
		String uri = "";
		cHandler.startElement(uri, "Mail", "Mail", new AttributesImpl());
		insertBody(cHandler, uri, message);
		cHandler.endElement(uri, "Mail", "Mail");
		cHandler.endDocument();

		return strWr.getBuffer().toString();
	}

	private void insertBody(ContentHandler cHandler, String uri, Message message) throws SAXException
	{
		cHandler.startElement(uri, "Body", "Body", new AttributesImpl());
		insertPlainMessage(cHandler, uri, message);
		insertHtmlMessage(cHandler, uri, message);
		cHandler.endElement(uri, "Body", "Body");
	}

	private void insertHtmlMessage(ContentHandler cHandler, String uri, Message message) throws SAXException
	{
		cHandler.startElement(uri, "Html", "Html", new AttributesImpl());
		cHandler.startElement(uri, "Text", "Text", new AttributesImpl());
		char[] ch = message.getHtmlBody().toCharArray();
		cHandler.characters(ch, 0, ch.length);
		cHandler.endElement(uri, "Text", "Text");
		cHandler.endElement(uri, "Html", "Html");
	}

	private void insertPlainMessage(ContentHandler cHandler, String uri, Message message) throws SAXException
	{
		cHandler.startElement(uri, "Plain", "Plain", new AttributesImpl());
		cHandler.startElement(uri, "Text", "Text", new AttributesImpl());
		char[] ch = message.getBody().toCharArray();
		cHandler.characters(ch, 0, ch.length);
		cHandler.endElement(uri, "Text", "Text");
		cHandler.endElement(uri, "Plain", "Plain");
	}
}
