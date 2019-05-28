package commands;

import bean.UserStatus;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.guild.member.GenericGuildMemberEvent;
import net.dv8tion.jda.core.events.guild.voice.GenericGuildVoiceEvent;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.events.message.MessageUpdateEvent;
import net.dv8tion.jda.core.events.message.priv.PrivateMessageReceivedEvent;
import net.dv8tion.jda.core.events.message.react.GenericMessageReactionEvent;
import net.dv8tion.jda.core.events.user.GenericUserEvent;
import net.dv8tion.jda.core.events.user.update.GenericUserPresenceEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import utils.Constants;
import utils.DiscordID;

import java.awt.*;
import java.time.Instant;

public class MarkStatusCommandListener extends ListenerAdapter {
    private UserStatus markStatus;
    private boolean enabled;

    public MarkStatusCommandListener() {
        markStatus = new UserStatus("Mark");
        enabled = true;
    }

    private boolean isMark(User user) {
        return user.getId().equals(DiscordID.ADMIN_ID);
    }

    private void parseEvent(User user, Class event) {
        if (isMark(user) && enabled) {
            markStatus.setStatus(System.currentTimeMillis(), event.getSimpleName());
        }
    }

    @Override
    public void onPrivateMessageReceived(PrivateMessageReceivedEvent event) {
        String id = event.getAuthor().getId();
        if (id.equals(DiscordID.CHRIS) || id.equals(DiscordID.ADMIN_ID)) {
            if (event.getMessage().getContentRaw().startsWith(Constants.PREFIX + "isMarkAlive")) {
                EmbedBuilder msg = new EmbedBuilder();
                msg.setTimestamp(Instant.ofEpochMilli(markStatus.getEpoch()));
                msg.setColor(Color.CYAN);
                msg.setTitle("Mark Status");
                msg.setDescription(markStatus.toString());
                event.getChannel().sendMessage(msg.build()).queue();
                event.getJDA().getUserById(DiscordID.ADMIN_ID).openPrivateChannel().queue(chan ->
                        chan.sendMessage(event.getAuthor().getName() + " used the isMarkAlive command").queue()
                );
            } else if (event.getMessage().getContentRaw().startsWith(Constants.PREFIX + "disableTracking")) {
                enabled = false;
            } else if (event.getMessage().getContentRaw().startsWith(Constants.PREFIX + "enableTracking")) {
                enabled = true;
            }
        }
    }

    @Override
    public void onGenericUser(GenericUserEvent event) {
        parseEvent(event.getUser(), event.getClass());
    }

    @Override
    public void onGenericUserPresence(GenericUserPresenceEvent event) {
        parseEvent(event.getUser(), event.getClass());
    }

    @Override
    public void onGenericMessageReaction(GenericMessageReactionEvent event) {
        parseEvent(event.getUser(), event.getClass());
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        parseEvent(event.getAuthor(), event.getClass());
    }

    @Override
    public void onMessageUpdate(MessageUpdateEvent event) {
        parseEvent(event.getAuthor(), event.getClass());
    }

    @Override
    public void onGenericGuildMember(GenericGuildMemberEvent event) {
        parseEvent(event.getUser(), event.getClass());
    }

    @Override
    public void onGenericGuildVoice(GenericGuildVoiceEvent event) {
        parseEvent(event.getMember().getUser(), event.getClass());
    }

}