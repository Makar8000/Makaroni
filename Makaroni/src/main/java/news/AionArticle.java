package news;

import java.awt.Color;
import java.io.Serializable;
import java.time.Instant;

import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.entities.Message;

public class AionArticle implements Serializable {
	private static final long serialVersionUID = 4348905330729805950L;
	private final String iconUrl = "https://orig00.deviantart.net/bef1/f/2009/272/0/c/aion_high_rez_icon_by_jocpoc.png";
	private long id;
	private String title;
	private String url;
	private String type;

	public AionArticle(long id, String title, String url, String type) {
		this.id = id;
		this.title = title;
		this.url = url;
		this.type = type;
	}
	
	public long getId() {
		return this.id;
	}

	public Message getMessage() {
		MessageBuilder msg = new MessageBuilder();
		EmbedBuilder emb = new EmbedBuilder();
		emb.setColor(new Color(43, 108, 140));
		emb.setTitle(this.title, this.url);
		emb.setTimestamp(Instant.now());
		emb.setAuthor(this.type, null, iconUrl);
		return msg.setEmbed(emb.build()).build();
	}

	public static AionArticle parse(String html) {
		try {
			String catEntry = "<div class=\"cat-entry\">";

			int endIndex = html.indexOf(catEntry, 2);
			html = html.substring(html.indexOf(catEntry), endIndex > -1 ? endIndex : html.length() - 1);
			String url = html.substring(html.indexOf("<h2><a href=\"") + "<h2><a href=\"".length(), html.indexOf("</a></h2>"));
			String title = url.split("\">")[1];
			url = "http://na.aiononline.com" + url.split("\">")[0];
			Long id = Long.parseLong(url.substring(url.lastIndexOf('/') + 1, url.indexOf('-')));
			
			String type = html.substring(html.indexOf("<li>Category: <a href=\"") + "<li>Category: <a href=\"".length());
			type = type.substring(type.indexOf('>')+1,type.indexOf('<'));
			if(type.endsWith("s"))
				type = type.substring(0, type.length()-1);

			return new AionArticle(id, title, url, type);
		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}
	}
}
