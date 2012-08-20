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

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.CDATASection;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Text;

import com.arcusx.mailer.Message;


/**
 * Created on Aug 20, 2012.
 * 
 * @author sven
 */
public class MessageTransformer
{	
	public String transform(Message message) throws Exception
	{
		DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
		docBuilderFactory.setNamespaceAware(true);
		docBuilderFactory.setValidating(false);
		DocumentBuilder documentBuilder = docBuilderFactory.newDocumentBuilder();
		Document document = documentBuilder.newDocument();
		document.setNodeValue("Mail");
		
		Element body = document.createElement("Body");
		Element plain = document.createElement("Plain");
		Text plainText = document.createTextNode("text");
		plainText.appendData(message.getBody());
		
		document.appendChild(body);
		body.appendChild(plain);
		plain.appendChild(plainText);
		
		Element html = document.createElement("Html");
		body.appendChild(html);
		
		Element htmlText = document.createElement("text");
		CDATASection htmlCDATA = document.createCDATASection(message.getHtmlBody());
		htmlText.appendChild(htmlCDATA);
		
		return document.toString();
	}
}
	