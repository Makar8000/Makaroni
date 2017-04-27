package commands;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import utils.Reminder;
import utils.ReminderManager;

public class GuildCommandListener extends ListenerAdapter {
	private final Map<String, GuildAction> commands;
	private final ReminderManager reminders;

	public GuildCommandListener() {
		commands = new HashMap<String, GuildAction>();
		reminders = new ReminderManager();
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
		addReminderCommand();
		addGetRemindersCommand();
		addRemReminderCommand();
		addReminderAllCommand();
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

	private GuildAction addReminderCommand() {
		GuildAction action = new GuildAction() {
			public String getCommand() {
				return "!remind";
			}

			public void run(GuildMessageReceivedEvent event) {
				String[] command = event.getMessage().getContent().split(" ", 3);
				if (command.length == 3) {
					try {
						DateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy-h:mma-z");
						Date date = dateFormat.parse(command[1]);
						// starting second sunday in march, ending first sunday november
						long time = date.getTime() - (1000 * 60 * 60);
						Reminder remind = new Reminder(time, event.getChannel(), event.getAuthor(), command[2]);
						reminders.add(event.getMessageId(), remind);
						event.getChannel().sendMessage("Added a reminder. Your reminder ID is " + event.getMessageId()).queue();
					} catch (ParseException e) {
						event.getChannel().sendMessage("Invalid date format.").queue();
						return;
					}
				}
			}
		};
		commands.put(action.getCommand(), action);
		return action;
	}

	private GuildAction addGetRemindersCommand() {
		GuildAction action = new GuildAction() {
			public String getCommand() {
				return "!myreminders";
			}

			public void run(GuildMessageReceivedEvent event) {
				ArrayList<Reminder> remind = reminders.getAll(event.getAuthor());
				if (!remind.isEmpty()) {
					StringBuilder msg = new StringBuilder("Here are all reminders for ");
					msg.append(event.getAuthor().getName());
					msg.append(":\n");
					for (Reminder r : remind) {
						msg.append(r.toString());
						msg.append('\n');
					}
					event.getChannel().sendMessage(msg.toString()).queue();
				} else {
					event.getChannel().sendMessage("You currently have no reminders.").queue();
				}
			}
		};
		commands.put(action.getCommand(), action);
		return action;
	}
	
	private GuildAction addRemReminderCommand() {
		GuildAction action = new GuildAction() {
			public String getCommand() {
				return "!remreminder";
			}

			public void run(GuildMessageReceivedEvent event) {
				String[] command = event.getMessage().getContent().split(" ", 2);
				if(command.length == 2) {
					if(reminders.remove(command[1], event.getAuthor())) {
						event.getChannel().sendMessage("Reminder removed.").queue();
					} else {
						event.getChannel().sendMessage("Unable to remove reminder.").queue();
					}
				}
			}
		};
		commands.put(action.getCommand(), action);
		return action;
	}
	
	private GuildAction addReminderAllCommand() {
		GuildAction action = new GuildAction() {
			public String getCommand() {
				return "!reminderall";
			}

			public void run(GuildMessageReceivedEvent event) {
				String[] command = event.getMessage().getContent().split(" ", 4);
				if(command.length == 4) {
					try {
						TextChannel chan = event.getGuild().getTextChannelById(command[1]);
						if(chan == null)
							return;
						DateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy-h:mma-z");
						Date date = dateFormat.parse(command[2]);
						// starting second sunday in march, ending first sunday november
						long time = date.getTime() - (1000 * 60 * 60);
						Reminder remind = new Reminder(time, chan, event.getAuthor(), command[3], true);
						reminders.add(event.getMessageId(), remind);
						event.getChannel().sendMessage("Added a reminder. Your reminder ID is " + event.getMessageId()).queue();
					} catch (NumberFormatException | ParseException ex) {
						event.getChannel().sendMessage("Invalid reminder format.").queue();
						return;
					}
				}
			}
		};
		commands.put(action.getCommand(), action);
		return action;
	}
}