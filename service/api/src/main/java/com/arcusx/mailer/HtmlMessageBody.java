/**
 * 
 */

package com.arcusx.mailer;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author chwa
 *
 */
public class HtmlMessageBody implements Serializable
{
	/**
	 * Keep serialization compatibility.
	 **/
	private static final long serialVersionUID = 1L;

	private final String html;

	private final List<MessageImage> inlineImages = new ArrayList<MessageImage>();

	public HtmlMessageBody(String html)
	{
		this.html = html;
	}

	public String getHtml()
	{
		return html;
	}

	public void addInlineImage(String identifier, String type, byte[] imageData)
	{
		final MessageImage image = new MessageImage(identifier, type, imageData);
		this.inlineImages.add(image);
	}

	public List<MessageImage> getImages()
	{
		return Collections.unmodifiableList(inlineImages);
	}
}
