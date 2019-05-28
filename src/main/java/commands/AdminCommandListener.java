package commands;

import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Game;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.core.events.message.priv.PrivateMessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import utils.DiscordID;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

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
		if (!event.getAuthor().getId().equals(DiscordID.ADMIN_ID))
			return;
		String command = event.getMessage().getContentRaw().split(" ", 2)[0].toLowerCase();
		if (commands.containsKey(command))
			commands.get(command).run(event);
	}

	@Override
	public void onPrivateMessageReceived(PrivateMessageReceivedEvent event) {
		if (!event.getAuthor().getId().equals(DiscordID.ADMIN_ID))
			return;
		String command = event.getMessage().getContentRaw().split(" ", 2)[0].toLowerCase();
		if (privateCommands.containsKey(command))
			privateCommands.get(command).run(event);
	}

	private void addCommands() {
		addSetGameCommand();
		addShutdownCommand();
		addDeleteCommand();
		addEvalCommand();
		addRemoveCommand();
		addDynamicCommand();
	}

	private PrivateAction addSetGameCommand() {
		PrivateAction action = new PrivateAction() {
			public String getCommand() {
				return "!setgame";
			}

			public void run(PrivateMessageReceivedEvent event) {
				String[] command = event.getMessage().getContentRaw().split(" ", 2);
				if (command.length != 2)
					return;

				event.getJDA().getPresence().setGame(Game.playing(command[1]));
			}
		};
		privateCommands.put(action.getCommand(), action);
		return action;
	}

	private GuildAction addShutdownCommand() {
		GuildAction action = new GuildAction() {
			public String getCommand() {
				return "!shutdown";
			}

			public void run(GuildMessageReceivedEvent event) {
				event.getJDA().shutdown();
				System.exit(0);
			}
		};
		commands.put(action.getCommand(), action);
		return action;
	}

	private GuildAction addDeleteCommand() {
		GuildAction action = new GuildAction() {
			public String getCommand() {
				return "!deletemsg";
			}

			public void run(GuildMessageReceivedEvent event) {
				String[] command = event.getMessage().getContentRaw().split(" ", 4);
				if (command.length == 4) {
					String strChan = command[1];
					String strFrom = command[2];
					String strTo = command[3];
					TextChannel chan = event.getGuild().getTextChannelById(strChan);
					Message start = chan.getMessageById(strFrom).complete();
					Message end = chan.getMessageById(strTo).complete();
					for (Message message : chan.getIterableHistory()) {
						if (message.getCreationTime().isAfter(start.getCreationTime()) && message.getCreationTime().isBefore(end.getCreationTime())) {
							System.out.println(message.getAuthor().getName() + ": " + message.getContentRaw());
							message.delete().queue();
						}
					}
				}
			}
		};
		commands.put(action.getCommand(), action);
		return action;
	}
	
	private GuildAction addEvalCommand() {
		GuildAction action = new GuildAction() {
			public String getCommand() {
				return "!eval";
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
		        } 
		        catch(Exception e) {
		        	output += e;
		    		msg.setColor(Color.RED);
		    		msg.setDescription("An exception was thrown.");
		        }
		        
		        output += "```";
	    		msg.addField("Output", output, false);
	        	event.getChannel().sendMessage(msg.build()).queue();
			}
		};
		commands.put(action.getCommand(), action);
		return action;
	}

	private GuildAction addRemoveCommand() {
		GuildAction action = new GuildAction() {
			public String getCommand() {
				return "!rem";
			}

			public void run(GuildMessageReceivedEvent event) {
				String[] command = event.getMessage().getContentRaw().split(" ", 2);
				if (command.length == 2)
					commands.remove(command[1]);
			}
		};
		commands.put(action.getCommand(), action);
		return action;
	}

	private GuildAction addDynamicCommand() {
		GuildAction action = new GuildAction() {
			public String getCommand() {
				return "!add";
			}

			public void run(GuildMessageReceivedEvent event) {
				String[] command = event.getMessage().getContentRaw().split(" ", 3);
				if (command.length == 3) {
					GuildAction newCommand = newDynamicCommand(command[1], command[2]);
					commands.put(newCommand.getCommand(), newCommand);
				}
			}
		};
		commands.put(action.getCommand(), action);
		return action;
	}

	private GuildAction newDynamicCommand(String command, String response) {
		return new GuildAction() {
			public String getCommand() {
				return "!" + command;
			}

			public void run(GuildMessageReceivedEvent event) {
				event.getChannel().sendMessage(response).queue();
			}
		};
	}
}
