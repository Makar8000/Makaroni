package news;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.PrivateChannel;

import java.awt.*;

public class FFXIVNotification {
    public static void start(PrivateChannel channel) {
        Thread t1 = new Thread(() -> FFXIVFetcher.checkNewPatch(channel));
        Thread t2 = new Thread(() -> FFXIVFetcher.checkMaintenance(channel));
        t1.start();
        t2.start();
    }

    public static MessageEmbed getMessage(String title, String message) {
        EmbedBuilder emb = new EmbedBuilder();
        emb.setColor(new Color(43, 108, 140));
        emb.setTitle(title);
        emb.setDescription(message);
        return emb.build();
    }
}
