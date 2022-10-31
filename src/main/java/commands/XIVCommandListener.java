package commands;

import bean.AriyalaSet;
import bean.EtroSet;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import utils.Constants;

import java.util.HashMap;
import java.util.Map;

public class XIVCommandListener extends ListenerAdapter {
    private final Map<String, MessageAction> commands;

    public XIVCommandListener() {
        commands = new HashMap<>();
        addCommands();
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        String command = event.getMessage().getContentRaw().split(" ", 2)[0].toLowerCase();
        if (commands.containsKey(command))
            commands.get(command).run(event);
    }

    private void addCommands() {
        addGetAriyalaLootCommand();
        addGetEtroLootCommand();
    }

    private MessageAction addGetAriyalaLootCommand() {
        MessageAction action = new MessageAction() {
            public String getCommand() {
                return "alootbreakdown";
            }

            public void run(MessageReceivedEvent event) {
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
                    if (ariyalaSet == null) {
                        msg.editMessage("Network error!").queue();
                        return;
                    }

                    msg.delete().queue();
                    try {
                        event.getMessage().delete().queue();
                    } catch (IllegalStateException ex) {
                    }
                    event.getChannel().sendMessageEmbeds(ariyalaSet.getMessage()).queue();
                });
            }
        };
        commands.put(Constants.PREFIX + action.getCommand(), action);
        return action;
    }

    private MessageAction addGetEtroLootCommand() {
        MessageAction action = new MessageAction() {
            public String getCommand() {
                return "elootbreakdown";
            }

            public void run(MessageReceivedEvent event) {
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
                    if (etroSet == null) {
                        msg.editMessage("Network error!").queue();
                        return;
                    }

                    msg.delete().queue();
                    try {
                        event.getMessage().delete().queue();
                    } catch (IllegalStateException ex) {
                    }
                    event.getChannel().sendMessageEmbeds(etroSet.getMessage()).queue();
                });
            }
        };
        commands.put(Constants.PREFIX + action.getCommand(), action);
        return action;
    }
}