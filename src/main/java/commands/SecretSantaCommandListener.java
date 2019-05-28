package commands;

import bean.Santa;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.PrivateChannel;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.message.priv.PrivateMessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import utils.Constants;
import utils.DiscordID;
import utils.SantaManager;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class SecretSantaCommandListener extends ListenerAdapter {
    private final Map<String, PrivateAction> commands;
    private final SantaManager santas;

    public SecretSantaCommandListener() {
        commands = new HashMap<>();
        santas = SantaManager.loadSantas();
        addCommands();
    }

    @Override
    public void onPrivateMessageReceived(PrivateMessageReceivedEvent event) {
        String command = event.getMessage().getContentRaw().split(" ", 2)[0].toLowerCase();
        if (commands.containsKey(command))
            commands.get(command).run(event);
    }

    private void addCommands() {
        addSantaAddCommand();
        addSantaRemoveCommand();
        addSantaParticipantsCommand();
        addSantaGoCommand();
        addSantaStopCommand();
        addSantaSendSCommand();
        addSantaSendRCommand();
        addSantaSendACommand();
    }

    private PrivateAction addSantaAddCommand() {
        PrivateAction action = new PrivateAction() {
            public String getCommand() {
                return "addsanta";
            }

            public void run(PrivateMessageReceivedEvent event) {
                String invalidMessage = "Invalid format. Please use the format `!addsanta Your Name | Address | Message for your secret santa (what _not_ to buy & other notes)`";
                String[] command = event.getMessage().getContentRaw().split(" ", 2);
                if (command.length < 2) {
                    event.getChannel().sendMessage(invalidMessage).queue();
                    return;
                }
                String[] params = command[1].split("\\|");
                if (params.length < 3) {
                    event.getChannel().sendMessage(invalidMessage).queue();
                    return;
                }

                Santa santa = new Santa();
                santa.setDiscordID(event.getAuthor().getId());
                santa.setRealName(params[0].trim());
                santa.setAddress(params[1].trim());
                santa.setNotes(params[2].trim());

                EmbedBuilder msg = new EmbedBuilder();
                if (santas.add(santa)) {
                    msg.setAuthor(event.getAuthor().getName() + " was added as a secret Santa!", null,
                            event.getAuthor().getEffectiveAvatarUrl());
                    msg.setTitle("Details", null);
                    msg.setColor(Color.CYAN);
                    msg.setDescription(santa.toString());
                } else {
                    msg.setAuthor(event.getAuthor().getName() + " is already registered as a secret Santa.", null,
                            event.getAuthor().getEffectiveAvatarUrl());
                    msg.setTitle("Your information has been updated", null);
                    msg.setColor(Color.MAGENTA);
                    msg.setDescription(santa.toString());
                }
                event.getChannel().sendMessage(msg.build()).queue();
            }
        };
        commands.put(Constants.PREFIX + action.getCommand(), action);
        return action;
    }

    private PrivateAction addSantaRemoveCommand() {
        PrivateAction action = new PrivateAction() {
            public String getCommand() {
                return "removesanta";
            }

            public void run(PrivateMessageReceivedEvent event) {
                EmbedBuilder msg = new EmbedBuilder();
                if (santas.remove(event.getAuthor().getId())) {
                    msg.setAuthor(event.getAuthor().getName() + " has been removed as a secret Santa.", null,
                            event.getAuthor().getEffectiveAvatarUrl());
                    msg.setColor(Color.RED);
                } else {
                    msg.setAuthor(event.getAuthor().getName() + " is not registered as a secret Santa!", null,
                            event.getAuthor().getEffectiveAvatarUrl());
                    msg.setColor(Color.RED);
                    msg.setDescription(
                            "If you would like to register, use the format `!addsanta Your Name | Address | Message for your secret santa (what _not_ to buy & other notes)`");
                }
                event.getChannel().sendMessage(msg.build()).queue();
            }
        };
        commands.put(Constants.PREFIX + action.getCommand(), action);
        return action;
    }

    private PrivateAction addSantaParticipantsCommand() {
        PrivateAction action = new PrivateAction() {
            public String getCommand() {
                return "santaparticipants";
            }

            public void run(PrivateMessageReceivedEvent event) {
                StringBuilder str = new StringBuilder();

                str.append("Current secret santa participants are:\n\n");
                santas.getAll().forEach(santa -> {
                    str.append(event.getJDA().getUserById(santa.getDiscordID()).getName());
                    str.append('\n');
                });

                event.getChannel().sendMessage(str.toString()).queue();
            }
        };
        commands.put(Constants.PREFIX + action.getCommand(), action);
        return action;
    }

    private PrivateAction addSantaGoCommand() {
        PrivateAction action = new PrivateAction() {
            public String getCommand() {
                return "gosanta";
            }

            public void run(PrivateMessageReceivedEvent event) {
                if (!event.getAuthor().getId().equals(DiscordID.ADMIN_ID))
                    return;
                if (santas.size() <= 1) {
                    event.getChannel().sendMessage("Only one participant").queue();
                    return;
                }

                ArrayList<Santa> s = santas.getAll();
                for (int i = 0; i < s.size(); i++) {
                    int j = i == s.size() - 1 ? 0 : i + 1;
                    Santa rsanta = s.get(j);
                    PrivateChannel chan = event.getJDA().getUserById(s.get(i).getDiscordID()).openPrivateChannel()
                            .complete();
                    User receiver = event.getJDA().getUserById(rsanta.getDiscordID());
                    santas.setReceiver(s.get(i).getDiscordID(), rsanta.getDiscordID());

                    EmbedBuilder msg = new EmbedBuilder();
                    msg.setAuthor(receiver.getName() + " was selected as your receiver!", null,
                            receiver.getEffectiveAvatarUrl());
                    msg.setColor(Color.ORANGE);
                    msg.setDescription("Send them a gift for Christmas :)");
                    msg.addField("Name", rsanta.getRealName(), false);
                    msg.addField("Address", rsanta.getAddress(), false);
                    msg.addField("Notes", rsanta.getNotes(), false);
                    chan.sendMessage(msg.build()).queue();
                    System.out.println(s.get(i).toString() + "\n\n");
                }
                santas.start();
            }
        };
        commands.put(Constants.PREFIX + action.getCommand(), action);
        return action;
    }

    private PrivateAction addSantaStopCommand() {
        PrivateAction action = new PrivateAction() {
            public String getCommand() {
                return "stopsanta";
            }

            public void run(PrivateMessageReceivedEvent event) {
                if (!event.getAuthor().getId().equals(DiscordID.ADMIN_ID))
                    return;
                if (santas.size() <= 1) {
                    event.getChannel().sendMessage("Only one participant").queue();
                    return;
                }

                santas.reset();
                event.getChannel().sendMessage("Secret Santa manager has been reset").queue();
            }
        };
        commands.put(Constants.PREFIX + action.getCommand(), action);
        return action;
    }

    private PrivateAction addSantaSendSCommand() {
        PrivateAction action = new PrivateAction() {
            public String getCommand() {
                return "heysanta";
            }

            public void run(PrivateMessageReceivedEvent event) {
                if (!santas.started())
                    return;
                String invalidMessage = "Invalid format. Please use the format `"
                        + Constants.PREFIX
                        + "heysanta <your message to your secret santa>`";
                String[] command = event.getMessage().getContentRaw().split(" ", 2);
                if (command.length < 2) {
                    event.getChannel().sendMessage(invalidMessage).queue();
                    addReaction(event.getMessage(), false);
                    return;
                }

                User santa = event.getJDA().getUserById(santas.getSanta(event.getAuthor().getId()));
                PrivateChannel chan = santa.openPrivateChannel().complete();

                EmbedBuilder msg = new EmbedBuilder();
                msg.setColor(Color.MAGENTA);
                msg.setAuthor(event.getAuthor().getName(), null, event.getAuthor().getEffectiveAvatarUrl());
                msg.setDescription(command[1]);
                msg.addField("", "**You can reply using** `" + Constants.PREFIX + "heyreceiver <msg>`", false);
                chan.sendMessage(msg.build()).queue();

                addReaction(event.getMessage(), true);
            }
        };
        commands.put(Constants.PREFIX + action.getCommand(), action);
        return action;
    }

    private PrivateAction addSantaSendRCommand() {
        PrivateAction action = new PrivateAction() {
            public String getCommand() {
                return "heyreceiver";
            }

            public void run(PrivateMessageReceivedEvent event) {
                if (!santas.started())
                    return;
                String invalidMessage = "Invalid format. Please use the format `"
                        + Constants.PREFIX
                        + "heyreceiver <your message to the person you are sending a gift to>`";
                String[] command = event.getMessage().getContentRaw().split(" ", 2);
                if (command.length < 2) {
                    event.getChannel().sendMessage(invalidMessage).queue();
                    addReaction(event.getMessage(), false);
                    return;
                }

                User receiver = event.getJDA().getUserById(santas.getReceiver(event.getAuthor().getId()));
                PrivateChannel chan = receiver.openPrivateChannel().complete();

                EmbedBuilder msg = new EmbedBuilder();
                msg.setColor(new Color(244, 74, 65));
                msg.setAuthor("Santa", null, "http://makar.pw/up/EukeH.png");
                msg.setDescription(command[1]);
                msg.addField("", "**You can reply using** `" + Constants.PREFIX + "heysanta <msg>`", false);
                chan.sendMessage(msg.build()).queue();

                addReaction(event.getMessage(), true);
            }
        };
        commands.put(Constants.PREFIX + action.getCommand(), action);
        return action;
    }

    private PrivateAction addSantaSendACommand() {
        PrivateAction action = new PrivateAction() {
            public String getCommand() {
                return "heychannel";
            }

            public void run(PrivateMessageReceivedEvent event) {
                if (!santas.started())
                    return;
                String invalidMessage = "Invalid format. Please use the format `"
                        + Constants.PREFIX
                        + "heychannel <your message to the #secret-santa channel>`";
                String[] command = event.getMessage().getContentRaw().split(" ", 2);
                if (command.length < 2) {
                    event.getChannel().sendMessage(invalidMessage).queue();
                    addReaction(event.getMessage(), false);
                    return;
                }

                TextChannel chan = event.getJDA().getTextChannelById(DiscordID.SECRET_SANTA);

                EmbedBuilder msg = new EmbedBuilder();
                msg.setColor(new Color(244, 74, 65));
                msg.setAuthor("Santa", null, "http://makar.pw/up/EukeH.png");
                msg.setDescription(command[1]);
                chan.sendMessage(msg.build()).queue();

                addReaction(event.getMessage(), true);
            }
        };
        commands.put(Constants.PREFIX + action.getCommand(), action);
        return action;
    }

    private void addReaction(Message msg, boolean good) {
        String ok = "\u2705";
        String bad = "\u274C";
        msg.addReaction(good ? ok : bad).queue();
    }
}