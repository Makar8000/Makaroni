package commands;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import utils.Emoji;

public class XIVCommandListener extends ListenerAdapter {
    private final Map<String, GuildAction> commands;

    public XIVCommandListener() {
        commands = new HashMap<String, GuildAction>();
        addCommands();
    }

    @Override
    public void onGuildMessageReceived(GuildMessageReceivedEvent event) {
        String command = event.getMessage().getContent().split(" ", 2)[0].toLowerCase();
        if (commands.containsKey(command))
            commands.get(command).run(event);
    }

    private void addCommands() {
        addPingCommand();
    }

    private GuildAction addPingCommand() {
        GuildAction action = new GuildAction() {
            public String getCommand() {
                return "!xivping";
            }

            public void run(GuildMessageReceivedEvent event) {
                event.getChannel().sendMessage("XIVPong!").queue();
            }
        };
        commands.put(action.getCommand(), action);
        return action;
    }
}