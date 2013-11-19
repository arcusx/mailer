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
		return this.html;
	}

	public void addInlineImage(String identifier, String type, byte[] imageData)
	{
		addInlineImage(identifier, identifier, type, imageData);
	}

	public void addInlineImage(String filename, String identifier, String type, byte[] imageData)
	{
		final MessageImage image = new MessageImage(filename, identifier, type, imageData);
		this.inlineImages.add(image);
	}

	public List<MessageImage> getImages()
	{
		return Collections.unmodifiableList(this.inlineImages);
	}

	public boolean hasImages()
	{
		return !this.inlineImages.isEmpty();
	}
}
