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

import java.io.StringReader;

import javax.xml.namespace.QName;
import javax.xml.stream.EventFilter;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import com.arcusx.mailer.Message;

/**
 * Created on Aug 21, 2012.
 * 
 * @author sven
 */
public class XmlToMessageTransformer
{
	private PlainParsing plainState = new PlainParsing();

	private HtmlParsing htmlState = new HtmlParsing();

	private RootParsing rootState = new RootParsing();

	private ParsingState state = rootState;

	public void transform(String xml, Message message) throws Exception
	{
		// Use  reference implementation
		//System.setProperty("javax.xml.stream.XMLInputFactory", "com.bea.xml.stream.MXParserFactory");
		// Create the XML input factory
		XMLInputFactory factory = XMLInputFactory.newInstance();

		StringReader reader = new StringReader(xml);
		XMLEventReader eventReader = factory.createXMLEventReader(reader);
		// Create a filtered reader
		XMLEventReader filteredEventReader = factory.createFilteredReader(eventReader, new EventFilter()
		{
			public boolean accept(XMLEvent event)
			{
				if (event.isStartElement())
					return true;
				if (event.isEndElement())
					return true;
				if (event.isCharacters())
					return true;
				if (event.isEndDocument())
					return true;
				return false;
			}
		});
		// Main event loop
		while (filteredEventReader.hasNext())
		{
			XMLEvent e = (XMLEvent) filteredEventReader.next();
			if (e.isEndDocument())
			{
				filteredEventReader.close();
				break;
			}
			state.process(e, message);
		}
	}

	public static boolean isElementWithName(StartElement se, String name)
	{
		QName qname = se.getName();
		return name.equals(qname.toString());
	}

	public static boolean isElementWithName(EndElement ee, String name)
	{
		QName qname = ee.getName();
		return name.equals(qname.toString());
	}

	abstract class ParsingState
	{
		abstract public void process(XMLEvent event, Message message);
	}

	class RootParsing extends ParsingState
	{
		@Override
		public void process(XMLEvent event, Message message)
		{
			if (event.isStartElement())
			{
				boolean isPlainMessage = isElementWithName(event.asStartElement(), "Plain");
				boolean isHtmlMessage = isElementWithName(event.asStartElement(), "Html");
				if (isPlainMessage)
				{
					state = plainState;
				}
				else if (isHtmlMessage)
				{
					state = htmlState;
				}
			}
		}

	}

	class PlainParsing extends ParsingState
	{
		private boolean readPlainTextMessage = false;

		private String data = "";

		@Override
		public void process(XMLEvent event, Message message)
		{
			if (event.isStartElement())
			{
				if (isElementWithName(event.asStartElement(), "Text"))
				{
					readPlainTextMessage = true;
				}
			}
			else if (event.isEndElement())
			{
				if (isElementWithName(event.asEndElement(), "Text"))
				{
					readPlainTextMessage = false;
				}
				else if (isElementWithName(event.asEndElement(), "Plain"))
				{
					state = rootState;
					message.setBody(data);
				}
			}
			else if (event.isCharacters() && readPlainTextMessage)
			{
				String characters = event.asCharacters().getData();
				data += characters;
			}
		}
	}

	class HtmlParsing extends ParsingState
	{
		private boolean readHtmlMessage;

		private String data = "";

		@Override
		public void process(XMLEvent event, Message message)
		{
			if (event.isStartElement())
			{
				if (isElementWithName(event.asStartElement(), "Text"))
				{
					readHtmlMessage = true;
				}
			}
			else if (event.isEndElement())
			{
				if (isElementWithName(event.asEndElement(), "Text"))
				{
					readHtmlMessage = false;
				}
				else if (isElementWithName(event.asEndElement(), "Html"))
				{
					state = rootState;
					message.setHtmlBody(data);
				}
			}
			else if (event.isCharacters() && readHtmlMessage)
			{
				String characters = event.asCharacters().getData();
				data += characters;
			}
		}
	}
}