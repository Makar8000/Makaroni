package news;

import java.awt.Color;
import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;

import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageEmbed;

public class AionPost implements Serializable {
	private static final long serialVersionUID = 3831608112025113582L;
	private final String iconUrl = "https://orig00.deviantart.net/bef1/f/2009/272/0/c/aion_high_rez_icon_by_jocpoc.png";
	private long id;
	private String title;
	private String url;
	private String content;
	private String time;

	public AionPost(long id, String title, String url, String content, String time) {
		this.id = id;
		this.title = title;
		this.url = url;
		this.content = content;
		this.time = time;
	}
	
	public long getId() {
		return this.id;
	}

	public Message getMessage() {
		MessageBuilder msg = new MessageBuilder();
		EmbedBuilder emb = new EmbedBuilder();
		emb.setColor(new Color(43, 108, 140));
		emb.setTitle(this.title, this.url);
		emb.setAuthor("Dev Tracker", null, iconUrl);
		
		String tempContent = content;
		tempContent = tempContent.replaceAll("\n\n", "\n")	
			.replaceAll("\n\t\n\t\n\t", "\n\t")
			.replaceAll("\n\t\t\n\t\t\n\t\t", "\n\t\t")	
			.replaceAll("\n\t\t\t\n\t\t\t\n\t\t\t", "\n\t\t\t")
			.replaceAll("\t\t", "\t")
			.replaceAll("\n\n\n", "\n")
			.replaceAll("\n \n","\n");
		if(content.length() > MessageEmbed.TEXT_MAX_LENGTH) {
			String readMore = "\n\n(Click the title to read more)";
			tempContent = content.substring(0, MessageEmbed.TEXT_MAX_LENGTH - readMore.length() - 2);
			tempContent += readMore;
		}
		emb.setDescription(tempContent);
		
		try {
			SimpleDateFormat dt = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z"); 
			emb.setTimestamp(Instant.ofEpochMilli(dt.parse(this.time).getTime()));
		} catch (ParseException e) {
			emb.setTimestamp(Instant.now());
		}
		
		return msg.setEmbed(emb.build()).build();
	}
}
