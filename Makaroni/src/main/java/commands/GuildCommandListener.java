package commands;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import utils.Emoji;

public class GuildCommandListener extends ListenerAdapter {
	private final Map<String, GuildAction> commands;

	public GuildCommandListener() {
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
		addPollCommand();
		//addPingCommand();
		addPoopCommand();
	}
	
	private GuildAction addPollCommand() {
		GuildAction action = new GuildAction() {
			public String getCommand() {
				return "!poll";
			}

			public void run(GuildMessageReceivedEvent event) {
				String invalidMessage = "Invalid Poll Format. Please use the format `!poll yourquestion | option1 | option2 | option3 ...`";
				String[] command = event.getMessage().getContent().split(" ", 2);
				if(command.length < 2) {
					event.getChannel().sendMessage(invalidMessage).queue();
					return;
				}
				String[] params = command[1].split("\\|");
				if(params.length < 3) {
					event.getChannel().sendMessage(invalidMessage).queue();
					return;
				} else if (params.length > 10) {
					event.getChannel().sendMessage("You can only have up to 9 different answers in a poll.").queue();
					return;
				}
				
				event.getMessage().delete().queue();
				EmbedBuilder msg = new EmbedBuilder();
				msg.setAuthor(event.getAuthor().getName(), null, event.getAuthor().getEffectiveAvatarUrl());
	    		msg.setTitle(params[0], null);
	    		msg.setColor(Color.MAGENTA);
	    		
	    		String answers = "";
	    		for(int i = 1; i < params.length; i++) 
	    			answers += Emoji.NUMBER[i] + " " + params[i].trim() + "\n";
	    		
	    		msg.setDescription(answers);
	    		Message poll = event.getChannel().sendMessage(msg.build()).complete();
	    		for(int i = 1; i < params.length; i++) {
	    			poll.addReaction(Emoji.NUMBER[i]).complete();
	    		}
			}
		};
		commands.put(action.getCommand(), action);
		return action;
	}

	private GuildAction addPingCommand() {
		GuildAction action = new GuildAction() {
			public String getCommand() {
				return "!ping";
			}

			public void run(GuildMessageReceivedEvent event) {
				event.getChannel().sendMessage("Pong!").queue();
			}
		};
		commands.put(action.getCommand(), action);
		return action;
	}

	private GuildAction addPoopCommand() {
		GuildAction action = new GuildAction() {
			public String getCommand() {
				return "!poop";
			}

			public void run(GuildMessageReceivedEvent event) {
				if (event.getAuthor().getId().equals("173302124656984064"))
					event.getChannel().sendMessage("no").queue();
			}
		};
		commands.put(action.getCommand(), action);
		return action;
	}
}