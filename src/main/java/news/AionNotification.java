package news;

import net.dv8tion.jda.api.JDA;
import utils.DiscordID;

public class AionNotification {
    public static void start(JDA jda) {
        Thread t1 = new Thread(() -> AionFetcher.loopNews(jda.getTextChannelById(DiscordID.GENERAL)));
        t1.start();
    }
}
