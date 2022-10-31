import commands.*;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;
import utils.TokenManager;

import java.util.ArrayList;

public class Makaroni {
    public static void main(String[] args) throws Exception {
        JDA jda = JDABuilder.createDefault(TokenManager.getDiscordTok())
                .enableIntents(getIntents())
                .build().awaitReady();
        jda.addEventListener(new GuildCommandListener());
        jda.addEventListener(new AdminCommandListener());
        //jda.addEventListener(new NicknameChangeListener());
        jda.addEventListener(new ReminderCommandListener());
        jda.addEventListener(new MarkStatusCommandListener());
        jda.addEventListener(new XIVCommandListener());
        jda.addEventListener(new SecretSantaCommandListener());
        jda.getPresence().setActivity(Activity.playing("with kittens"));
    }

    private static ArrayList<GatewayIntent> getIntents() {
        // Add non-default intents
        ArrayList<GatewayIntent> intents = new ArrayList<>();
        intents.add(GatewayIntent.GUILD_MEMBERS);
        intents.add(GatewayIntent.GUILD_PRESENCES);
        intents.add(GatewayIntent.GUILD_WEBHOOKS);
        intents.add(GatewayIntent.GUILD_MESSAGE_TYPING);
        intents.add(GatewayIntent.DIRECT_MESSAGE_TYPING);
        return intents;
    }
}
