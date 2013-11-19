
package com.arcusx.mailer.service;

import java.util.List;

import com.arcusx.mailer.MessageImage;

public class HtmlImageIdExpander
{
	private String html;
	private List<MessageImage> inlineImages;

	public HtmlImageIdExpander(String html, List<MessageImage> inlineImages)
	{
		this.html = html;
		this.inlineImages = inlineImages;
	}

	public String getHtmlWithImageIdsExpanded()
	{
		String htmlWithIdsExpanded = this.html;
		for (MessageImage image : this.inlineImages)
		{
			htmlWithIdsExpanded = htmlWithIdsExpanded.replace(image.identifier, "cid:" + image.identifier);
		}
		return htmlWithIdsExpanded;
	}
}
