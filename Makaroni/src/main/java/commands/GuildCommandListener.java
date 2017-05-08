package commands;

import java.util.HashMap;
import java.util.Map;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

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
		addPingCommand();
		addPoopCommand();
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