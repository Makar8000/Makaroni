import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import audio.AudioPlayerListener;
import commands.AdminCommandListener;
import commands.GuildCommandListener;
import commands.ReminderCommandListener;
import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.entities.Game;

public class Makaroni {
	public static void main(String[] args) throws Exception {
		JDA jda = new JDABuilder(AccountType.BOT).setToken(getTok()).buildBlocking();
		//jda.addEventListener(new AudioPlayerListener());
		jda.addEventListener(new GuildCommandListener());
		jda.addEventListener(new AdminCommandListener());
		jda.addEventListener(new ReminderCommandListener());
		jda.getPresence().setGame(Game.of("with kittens"));
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
