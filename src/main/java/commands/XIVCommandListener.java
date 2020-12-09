package commands;

import bean.AriyalaSet;
import bean.EtroSet;
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
        addGetAriyalaLootCommand();
        addGetEtroLootCommand();
    }

    private GuildAction addGetAriyalaLootCommand() {
        GuildAction action = new GuildAction() {
            public String getCommand() {
                return "alootbreakdown";
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

                event.getChannel().sendMessage("Grabbing ariyala data...").queue(msg -> {
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

    private GuildAction addGetEtroLootCommand() {
        GuildAction action = new GuildAction() {
            public String getCommand() {
                return "elootbreakdown";
            }

            public void run(GuildMessageReceivedEvent event) {
                String[] command = event.getMessage().getContentRaw().split(" ", 2);
                if (command.length != 2)
                    return;

                int idIndex = command[1].lastIndexOf('/');
                if (idIndex == -1) {
                    event.getChannel().sendMessage("Invalid etro URL.").queue();
                    return;
                }

                String etroId;
                try {
                    etroId = command[1].substring(++idIndex);
                } catch (IndexOutOfBoundsException ex) {
                    event.getChannel().sendMessage("Invalid etro URL.").queue();
                    return;
                }

                event.getChannel().sendMessage("Grabbing etro data...").queue(msg -> {
                    EtroSet etroSet = EtroSet.getFromId(etroId);
                    if (etroSet == null)
                        msg.editMessage("Network error!").queue();

                    msg.delete().queue();
                    event.getMessage().delete().queue();
                    event.getChannel().sendMessage(etroSet.getMessage()).queue();
                });
            }
        };
        commands.put(Constants.PREFIX + action.getCommand(), action);
        return action;
    }
}