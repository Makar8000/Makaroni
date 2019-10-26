import commands.*;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import utils.TokenManager;

public class Makaroni {
    public static void main(String[] args) throws Exception {
        JDA jda = new JDABuilder().setToken(TokenManager.getDiscordTok()).build().awaitReady();
        jda.addEventListener(new GuildCommandListener());
        jda.addEventListener(new AdminCommandListener());
        jda.addEventListener(new NicknameChangeListener());
        jda.addEventListener(new ReminderCommandListener());
        jda.addEventListener(new MarkStatusCommandListener());
        jda.addEventListener(new XIVCommandListener());
        //jda.addEventListener(new SecretSantaCommandListener())
        jda.getPresence().setActivity(Activity.playing("with kittens"));

        //AionNotification.start(jda);
    }
}
