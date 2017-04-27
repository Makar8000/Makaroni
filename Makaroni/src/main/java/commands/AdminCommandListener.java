package commands;
import java.util.HashMap;
import java.util.Map;

import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

public class AdminCommandListener extends ListenerAdapter {
	private final Map<String, GuildAction> commands;
	private final String adminID = "85924030661533696";

	public AdminCommandListener() {
		commands = new HashMap<String, GuildAction>();
		addCommands();
	}

	@Override
	public void onGuildMessageReceived(GuildMessageReceivedEvent event) {
		if(!event.getAuthor().getId().equals(adminID))
			return;
		String command = event.getMessage().getContent().split(" ", 2)[0].toLowerCase();
		if (commands.containsKey(command))
			commands.get(command).run(event);
	}

	private void addCommands() {
		addShutdownCommand();
		addRemoveCommand();
		addDynamicCommand();
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
	
	private GuildAction addRemoveCommand() {
		GuildAction action = new GuildAction() {
			public String getCommand() {
				return "!rem";
			}

			public void run(GuildMessageReceivedEvent event) {
				String[] command = event.getMessage().getContent().split(" ", 2);
				if(command.length == 2 && commands.containsKey(command[1]))
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
				String[] command = event.getMessage().getContent().split(" ", 3);
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
		GuildAction action = new GuildAction() {
			public String getCommand() {
				return "!" + command;
			}

			public void run(GuildMessageReceivedEvent event) {
				event.getChannel().sendMessage(response).queue();
			}
		};
		return action;
	}
}
