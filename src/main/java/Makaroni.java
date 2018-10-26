import commands.*;
import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.entities.Game;
import news.AionNotification;
import utils.TokenManager;

public class Makaroni {
	public static void main(String[] args) throws Exception {
		JDA jda = new JDABuilder(AccountType.BOT).setToken(TokenManager.getDiscordTok()).build().awaitReady();
		jda.addEventListener(new GuildCommandListener());
		jda.addEventListener(new AdminCommandListener());
		jda.addEventListener(new NicknameChangeListener());
		jda.addEventListener(new ReminderCommandListener());
		jda.addEventListener(new SecretSantaCommandListener());
		jda.getPresence().setGame(Game.playing("with kittens"));
		
		AionNotification.start(jda);
	}
}
