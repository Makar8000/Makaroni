package commands;

import bean.AriyalaSet;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import utils.Constants;

import java.util.HashMap;
import java.util.Map;

public class XIVCommandListener extends ListenerAdapter {
    private final Map<String, GuildAction> commands;

    public XIVCommandListener() {
        commands = new HashMap<>();
        addCommands();
    }

    @Override
    public void onGuildMessageReceived(GuildMessageReceivedEvent event) {
        String command = event.getMessage().getContentRaw().split(" ", 2)[0].toLowerCase();
        if (commands.containsKey(command))
            commands.get(command).run(event);
    }

    private void addCommands() {
        addGetLootCommand();
    }

    private GuildAction addGetLootCommand() {
        GuildAction action = new GuildAction() {
            public String getCommand() {
                return "lootbreakdown";
            }

            public void run(GuildMessageReceivedEvent event) {
                String[] command = event.getMessage().getContentRaw().split(" ", 2);
                if (command.length != 2)
                    return;

                int idIndex = command[1].lastIndexOf('/');
                if (idIndex == -1) {
                    event.getChannel().sendMessage("Invalid ariyala URL.").queue();
                    return;
                }

                String ariyalaId;
                try {
                    ariyalaId = command[1].substring(++idIndex);
                } catch (IndexOutOfBoundsException ex) {
                    event.getChannel().sendMessage("Invalid ariyala URL.").queue();
                    return;
                }

                event.getChannel().sendMessage("Grabbing Ariyala data...").queue(msg -> {
                    AriyalaSet ariyalaSet = AriyalaSet.getFromId(ariyalaId);
                    if (ariyalaSet == null)
                        msg.editMessage("Network error!").queue();

                    msg.delete().queue();
                    event.getMessage().delete().queue();
                    event.getChannel().sendMessage(ariyalaSet.getMessage()).queue();
                });
            }
        };
        commands.put(Constants.PREFIX + action.getCommand(), action);
        return action;
    }
}