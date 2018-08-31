import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import commands.AdminCommandListener;
import commands.GuildCommandListener;
import commands.NicknameChangeListener;
import commands.ReminderCommandListener;
import commands.SecretSantaCommandListener;
import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.entities.Game;
import news.AionNotification;

public class Makaroni {
	public static void main(String[] args) throws Exception {
		JDA jda = new JDABuilder(AccountType.BOT).setToken(getTok()).buildBlocking();
		//jda.addEventListener(new AudioPlayerListener());
		jda.addEventListener(new GuildCommandListener());
		jda.addEventListener(new AdminCommandListener());
		jda.addEventListener(new NicknameChangeListener());
		jda.addEventListener(new ReminderCommandListener());
		jda.addEventListener(new SecretSantaCommandListener());
		jda.getPresence().setGame(Game.of("with kittens"));
		
		AionNotification.start(jda);
	}

	private static String getTok() {
		try {
			return new String(Files.readAllBytes(new File("bot.tok").toPath()));
		} catch (IOException e) {
			e.printStackTrace();
			return "";
		}
	}
}
