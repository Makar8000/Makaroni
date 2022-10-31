package commands;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

abstract class MessageAction {
    public abstract String getCommand();

    public abstract void run(MessageReceivedEvent event);
}
