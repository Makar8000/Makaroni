package commands;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.ReconnectedEvent;
import net.dv8tion.jda.api.events.ResumedEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import news.FFXIVNotification;
import utils.Constants;
import utils.DiscordID;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class AdminCommandListener extends ListenerAdapter {
    private final Map<String, GuildAction> commands;
    private final Map<String, PrivateAction> privateCommands;

    public AdminCommandListener() {
        commands = new HashMap<>();
        privateCommands = new HashMap<>();
        addCommands();
    }

    @Override
    public void onGuildMessageReceived(GuildMessageReceivedEvent event) {
        if (!event.getAuthor().getId().equals(DiscordID.ADMIN))
            return;
        String command = event.getMessage().getContentRaw().split(" ", 2)[0].toLowerCase();
        if (commands.containsKey(command))
            commands.get(command).run(event);
    }

    @Override
    public void onPrivateMessageReceived(PrivateMessageReceivedEvent event) {
        if (!event.getAuthor().getId().equals(DiscordID.ADMIN))
            return;
        String command = event.getMessage().getContentRaw().split(" ", 2)[0].toLowerCase();
        if (privateCommands.containsKey(command))
            privateCommands.get(command).run(event);
    }

    @Override
    public void onResume(ResumedEvent event) {
        setGame(event.getJDA(), Constants.DEFAULT_GAME);
    }

    @Override
    public void onReconnect(ReconnectedEvent event) {
        setGame(event.getJDA(), Constants.DEFAULT_GAME);
    }

    private void setGame(JDA jda, String game) {
        jda.getPresence().setActivity(Activity.playing(game));
    }

    private void addCommands() {
        addSetGameCommand();
        addShutdownCommand();
        addPrivateShutdownCommand();
        addDeleteCommand();
        addEvalCommand();
        addRemoveCommand();
        addDynamicCommand();
        addDeleteAllCommand();
        addFFXIVMaintNotifyCommand();
    }

    private PrivateAction addDeleteAllCommand() {
        PrivateAction action = new PrivateAction() {
            public String getCommand() {
                return "deleteall";
            }

            public void run(PrivateMessageReceivedEvent event) {
                String[] command = event.getMessage().getContentRaw().split(" ", 2);
                if (command.length != 2)
                    return;

                TextChannel chan = event.getJDA().getTextChannelById(command[1]);
                for (Message message : chan.getIterableHistory().stream()
                        .filter(m -> !m.isPinned())
                        .collect(Collectors.toList()))
                    message.delete().queue();
            }
        };
        privateCommands.put(Constants.PREFIX + action.getCommand(), action);
        return action;
    }

    private PrivateAction addSetGameCommand() {
        PrivateAction action = new PrivateAction() {
            public String getCommand() {
                return "setgame";
            }

            public void run(PrivateMessageReceivedEvent event) {
                String[] command = event.getMessage().getContentRaw().split(" ", 2);
                if (command.length != 2)
                    return;

                setGame(event.getJDA(), command[1]);
            }
        };
        privateCommands.put(Constants.PREFIX + action.getCommand(), action);
        return action;
    }

    private PrivateAction addPrivateShutdownCommand() {
        PrivateAction action = new PrivateAction() {
            public String getCommand() {
                return "shutdown";
            }

            public void run(PrivateMessageReceivedEvent event) {
                event.getJDA().shutdown();
                System.exit(0);
            }
        };
        privateCommands.put(Constants.PREFIX + action.getCommand(), action);
        return action;
    }

    private PrivateAction addFFXIVMaintNotifyCommand() {
        PrivateAction action = new PrivateAction() {
            public String getCommand() {
                return "ffxivmaint";
            }

            public void run(PrivateMessageReceivedEvent event) {
                FFXIVNotification.start(event.getChannel());
                event.getAuthor().openPrivateChannel().complete().sendMessage("Added to queue.").queue();
            }
        };
        privateCommands.put(Constants.PREFIX + action.getCommand(), action);
        return action;
    }

    private GuildAction addShutdownCommand() {
        GuildAction action = new GuildAction() {
            public String getCommand() {
                return "shutdown";
            }

            public void run(GuildMessageReceivedEvent event) {
                event.getJDA().shutdown();
                System.exit(0);
            }
        };
        commands.put(Constants.PREFIX + action.getCommand(), action);
        return action;
    }

    private GuildAction addDeleteCommand() {
        GuildAction action = new GuildAction() {
            public String getCommand() {
                return "deletemsg";
            }

            public void run(GuildMessageReceivedEvent event) {
                String[] command = event.getMessage().getContentRaw().split(" ", 4);
                if (command.length == 4) {
                    String strChan = command[1];
                    String strFrom = command[2];
                    String strTo = command[3];
                    TextChannel chan = event.getGuild().getTextChannelById(strChan);
                    Message start = chan.retrieveMessageById(strFrom).complete();
                    Message end = chan.retrieveMessageById(strTo).complete();
                    for (Message message : chan.getIterableHistory()) {
                        if (message.getTimeCreated().isAfter(start.getTimeCreated()) && message.getTimeCreated().isBefore(end.getTimeCreated())) {
                            System.out.println(message.getAuthor().getName() + ": " + message.getContentRaw());
                            message.delete().queue();
                        }
                    }
                }
            }
        };
        commands.put(Constants.PREFIX + action.getCommand(), action);
        return action;
    }

    private GuildAction addEvalCommand() {
        GuildAction action = new GuildAction() {
            public String getCommand() {
                return "eval";
            }

            public void run(GuildMessageReceivedEvent event) {
                String[] command = event.getMessage().getContentRaw().split(" ", 2);
                if (command.length != 2)
                    return;

                String output = "```";
                EmbedBuilder msg = new EmbedBuilder();
                msg.setAuthor(event.getAuthor().getName(), null, event.getAuthor().getEffectiveAvatarUrl());
                msg.setTitle("Status", null);

                ScriptEngine se = new ScriptEngineManager().getEngineByName("Nashorn");
                se.put("event", event);
                se.put("jda", event.getJDA());
                se.put("guild", event.getGuild());
                se.put("channel", event.getChannel());
                try {
                    output += (String) se.eval(command[1]);
                    msg.setColor(Color.GREEN);
                    msg.setDescription("Evaluated successfully.");
                } catch (Exception e) {
                    output += e;
                    msg.setColor(Color.RED);
                    msg.setDescription("An exception was thrown.");
                }

                output += "```";
                msg.addField("Output", output, false);
                event.getChannel().sendMessage(msg.build()).queue();
            }
        };
        commands.put(Constants.PREFIX + action.getCommand(), action);
        return action;
    }

    private GuildAction addRemoveCommand() {
        GuildAction action = new GuildAction() {
            public String getCommand() {
                return "rem";
            }

            public void run(GuildMessageReceivedEvent event) {
                String[] command = event.getMessage().getContentRaw().split(" ", 2);
                if (command.length == 2)
                    commands.remove(command[1]);
            }
        };
        commands.put(Constants.PREFIX + action.getCommand(), action);
        return action;
    }

    private GuildAction addDynamicCommand() {
        GuildAction action = new GuildAction() {
            public String getCommand() {
                return "add";
            }

            public void run(GuildMessageReceivedEvent event) {
                String[] command = event.getMessage().getContentRaw().split(" ", 3);
                if (command.length == 3) {
                    GuildAction newCommand = newDynamicCommand(command[1], command[2]);
                    commands.put(newCommand.getCommand(), newCommand);
                }
            }
        };
        commands.put(Constants.PREFIX + action.getCommand(), action);
        return action;
    }

    private GuildAction newDynamicCommand(String command, String response) {
        return new GuildAction() {
            public String getCommand() {
                return Constants.PREFIX + command;
            }

            public void run(GuildMessageReceivedEvent event) {
                event.getChannel().sendMessage(response).queue();
            }
        };
    }
}
