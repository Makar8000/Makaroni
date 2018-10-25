package news;

import net.dv8tion.jda.core.JDA;
import utils.DiscordID;

public class AionNotification {
	public static void start(JDA jda) {
		Thread t1 = new Thread(() -> {
			AionFetcher.loopNews(jda.getTextChannelById(DiscordID.GAMING));
		});
		t1.start();
		
		Thread t2 = new Thread(() -> {
			AionFetcher.loopPosts(jda.getTextChannelById(DiscordID.MAPLE_SYRUP));
		});
		t2.start();
	}
}
