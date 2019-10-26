package commands;

import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent;

abstract class PrivateAction {
    public abstract String getCommand();

    public abstract void run(PrivateMessageReceivedEvent event);
}
