package commands;

import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

abstract class GuildAction {
    public abstract String getCommand();

    public abstract void run(GuildMessageReceivedEvent event);
}
