package commands;

import bean.Reminder;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import utils.Constants;
import utils.ReminderManager;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class ReminderCommandListener extends ListenerAdapter {
	private final Map<String, GuildAction> commands;
	private final ReminderManager reminders;

	public ReminderCommandListener() {
		commands = new HashMap<>();
		reminders = new ReminderManager();
		addCommands();
	}

	@Override
	public void onGuildMessageReceived(GuildMessageReceivedEvent event) {
		String command = event.getMessage().getContentRaw().split(" ", 2)[0].toLowerCase();
		if (commands.containsKey(command))
			commands.get(command).run(event);
	}

	private void addCommands() {
		addReminderCommand();
		addGetRemindersCommand();
		addRemReminderCommand();
		addReminderAllCommand();
	}

	private GuildAction addReminderCommand() {
		GuildAction action = new GuildAction() {
			public String getCommand() {
				return "remind";
			}

			public void run(GuildMessageReceivedEvent event) {
				String[] command = event.getMessage().getContentRaw().split(" ", 5);
				if (command.length == 5) {
					try {
						DateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy h:mma z");
						String strDate = command[1] + " " + command[2] + " " + command[3];
						Date date = dateFormat.parse(strDate);
						long time = date.getTime();
						Reminder remind = new Reminder(time, event.getChannel(), event.getAuthor(), command[4], event.getMessageId());
						reminders.add(event.getMessageId(), remind);
						event.getChannel().sendMessage(remind.getEmbed()).queue();
					} catch (ParseException e) {
						event.getChannel().sendMessage("Invalid date format.\n Corrent Date format is `MM/dd/yyyy h:mma z`").queue();
					}
				} else if (command.length == 2 && command[1].equalsIgnoreCase("help")) {
					String remsyntax = "Syntax for reminder command is as follows:\n";
					remsyntax += Constants.PREFIX;
					remsyntax += "remind [date] [time] [timezone] [msg]\n";
					remsyntax += "\nExample:\n";
					remsyntax += Constants.PREFIX;
					remsyntax += "remind 4/30/2017 6:00PM CST make dinner!";
					event.getChannel().sendMessage(remsyntax).queue();
				}
			}
		};
		commands.put(Constants.PREFIX + action.getCommand(), action);
		return action;
	}

	private GuildAction addGetRemindersCommand() {
		GuildAction action = new GuildAction() {
			public String getCommand() {
				return "myreminders";
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
		commands.put(Constants.PREFIX + action.getCommand(), action);
		return action;
	}
	
	private GuildAction addRemReminderCommand() {
		GuildAction action = new GuildAction() {
			public String getCommand() {
				return "remreminder";
			}

			public void run(GuildMessageReceivedEvent event) {
				String[] command = event.getMessage().getContentRaw().split(" ", 2);
				if(command.length == 2) {
					if(reminders.remove(command[1], event.getAuthor())) {
						event.getChannel().sendMessage("Reminder removed.").queue();
					} else {
						event.getChannel().sendMessage("Unable to remove reminder.").queue();
					}
				}
			}
		};
		commands.put(Constants.PREFIX + action.getCommand(), action);
		return action;
	}
	
	private GuildAction addReminderAllCommand() {
		GuildAction action = new GuildAction() {
			public String getCommand() {
				return "reminderall";
			}

			public void run(GuildMessageReceivedEvent event) {
				String[] command = event.getMessage().getContentRaw().split(" ", 6);
				if(command.length == 6) {
					try {
						TextChannel chan = event.getGuild().getTextChannelsByName(command[1], true).get(0);
						if(chan == null)
							return;
						DateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy h:mma z");
						String strDate = command[2] + " " + command[3] + " " + command[4];
						Date date = dateFormat.parse(strDate);
						// starting second sunday in march, ending first sunday november
						long time = date.getTime() - (1000 * 60 * 60);
						Reminder remind = new Reminder(time, chan, event.getAuthor(), command[5], event.getMessageId(), true);
						reminders.add(event.getMessageId(), remind);
						event.getChannel().sendMessage(remind.getEmbed()).queue();
					} catch (NumberFormatException | ParseException ex) {
						event.getChannel().sendMessage("Invalid reminder format.").queue();
					}
				}
			}
		};
		commands.put(Constants.PREFIX + action.getCommand(), action);
		return action;
	}
}