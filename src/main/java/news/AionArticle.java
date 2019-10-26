package news;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.json.JSONObject;

import java.awt.*;
import java.io.Serializable;
import java.time.Instant;

public class AionArticle implements Serializable {
    private static final long serialVersionUID = 4348905330729805950L;
    private final String iconUrl = "https://orig00.deviantart.net/bef1/f/2009/272/0/c/aion_high_rez_icon_by_jocpoc.png";
    private final String thumbUrl = "https://pbs.twimg.com/profile_images/1055147593657835521/IHVQA0WD_400x400.jpg";
    private String id;
    private String title;
    private String url;
    private String summary;
    private String category;
    private String thumb;
    private String date;

    public AionArticle(JSONObject json) {
        this.id = json.getString("_id");
        this.title = json.getString("title");
        this.url = "https://www.aiononline.com/news/" + json.getString("basename");
        this.category = json.getString("category");
        this.summary = json.getString("summary");
        this.thumb = "https://www.aiononline.com" + json.getJSONObject("attributes").getString("thumb");
        this.date = json.getString("publish_date");

        this.category = Character.toTitleCase(category.charAt(0)) + category.substring(1);
    }

    public String getId() {
        return this.id;
    }

    public MessageEmbed getMessage() {
        System.out.println("instanciating");
        EmbedBuilder emb = new EmbedBuilder();
        System.out.println("color");
        emb.setColor(new Color(43, 108, 140));
        System.out.println("title/url");
        emb.setTitle(this.title, this.url);
        System.out.println("date");
        emb.setTimestamp(Instant.parse(this.date));
        System.out.println("auth");
        emb.setAuthor(this.category, null, iconUrl);
        System.out.println("desc");
        emb.setDescription(this.summary);
        System.out.println("thumb" + this.thumb);
        emb.setImage(this.thumb);
        System.out.println("icon");
        emb.setThumbnail(thumbUrl);
        System.out.println("build");
        return emb.build();
    }

    public String toString() {
        return this.getId();
    }
}
