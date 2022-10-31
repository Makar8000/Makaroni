package commands;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import utils.Constants;
import utils.DiscordID;
import utils.EmojiUtils;

import java.awt.*;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class GuildCommandListener extends ListenerAdapter {
    private final Map<String, MessageAction> commands;
    private final Random rand;

    public GuildCommandListener() {
        commands = new HashMap<>();
        rand = new Random();
        addCommands();
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        String command = event.getMessage().getContentRaw().split(" ", 2)[0].toLowerCase();
        if (commands.containsKey(command))
            commands.get(command).run(event);
    }

    private void addCommands() {
        addPollCommand();
        addPingCommand();
        addRollCommand();
        addPoopCommand();
    }

    private MessageAction addPollCommand() {
        MessageAction action = new MessageAction() {
            public String getCommand() {
                return "poll";
            }

            public void run(MessageReceivedEvent event) {
                String invalidMessage = "Invalid Poll Format. Please use the format `"
                        + Constants.PREFIX
                        + "poll yourquestion | option1 | option2 | option3 ...`";
                String[] command = event.getMessage().getContentRaw().split(" ", 2);
                if (command.length < 2) {
                    event.getChannel().sendMessage(invalidMessage).queue();
                    return;
                }
                String[] params = command[1].split("\\|");
                if (params.length < 3) {
                    event.getChannel().sendMessage(invalidMessage).queue();
                    return;
                } else if (params.length > 10) {
                    event.getChannel().sendMessage("You can only have up to 9 different answers in a poll.").queue();
                    return;
                }

                try {
                    event.getMessage().delete().queue();
                } catch (IllegalStateException ex) {
                }
                EmbedBuilder msg = new EmbedBuilder();
                msg.setAuthor(event.getAuthor().getName() + " created a poll!", null, event.getAuthor().getEffectiveAvatarUrl());
                msg.setTitle(params[0], null);
                msg.setColor(Color.MAGENTA);

                StringBuilder answers = new StringBuilder();
                for (int i = 1; i < params.length; i++) {
                    answers.append(EmojiUtils.NUMBER[i]);
                    answers.append(' ');
                    answers.append(params[i].trim());
                    answers.append('\n');
                }

                msg.setDescription(answers.toString());
                Message poll = event.getChannel().sendMessageEmbeds(msg.build()).complete();
                for (int i = 1; i < params.length; i++) {
                    poll.addReaction(EmojiUtils.NUMBER[i]).complete();
                }
            }
        };
        commands.put(Constants.PREFIX + action.getCommand(), action);
        return action;
    }

    private MessageAction addPingCommand() {
        MessageAction action = new MessageAction() {
            public String getCommand() {
                return "pingg";
            }

            public void run(MessageReceivedEvent event) {
                event.getChannel().sendMessage("Ping: ...").queue(m -> {
                    long ping = event.getMessage().getTimeCreated().until(m.getTimeCreated(), ChronoUnit.MILLIS);
                    m.editMessage("Ping: " + ping + "ms | Websocket: " + event.getJDA().getGatewayPing() + "ms").queue();
                });
            }
        };
        commands.put(Constants.PREFIX + action.getCommand(), action);
        return action;
    }

    private MessageAction addRollCommand() {
        MessageAction action = new MessageAction() {
            public String getCommand() {
                return "roll";
            }

            public void run(MessageReceivedEvent event) {
                String[] command = event.getMessage().getContentRaw().split(" ", 2);
                int max = 100;
                if (command.length > 1) {
                    try {
                        max = Integer.parseInt(command[1]) + 1;
                    } catch (NumberFormatException ex) {
                        event.getChannel().sendMessage("Invalid format. Use `!roll [max]`. Example: `!roll 1000`").queue();
                        return;
                    }
                }
                event.getChannel().sendMessage("You rolled a " + rand.nextInt(max) + "!").queue();
            }
        };
        commands.put(Constants.PREFIX + action.getCommand(), action);
        return action;
    }

    private MessageAction addPoopCommand() {
        MessageAction action = new MessageAction() {
            public String getCommand() {
                return "poop";
            }

            public void run(MessageReceivedEvent event) {
                if (event.getAuthor().getId().equals(DiscordID.KAGA))
                    event.getChannel().sendMessage("no").queue();
            }
        };
        commands.put(Constants.PREFIX + action.getCommand(), action);
        return action;
    }
}