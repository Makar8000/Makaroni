package bean;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;

import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

public class Reminder {
    private ScheduledExecutorService scheduler;
    private ScheduledFuture<?> scheduleHandle;
    private long epoch;
    private TextChannel channel;
    private User user;
    private String msg;
    private boolean all;
    private String id;

    public Reminder(long epoch, TextChannel channel, User user, String msg, String id) {
        scheduler = Executors.newScheduledThreadPool(1);
        this.epoch = epoch;
        this.channel = channel;
        this.user = user;
        this.msg = msg;
        this.all = false;
        this.id = id;
    }

    public Reminder(long epoch, TextChannel channel, User user, String msg, String id, boolean all) {
        scheduler = Executors.newScheduledThreadPool(1);
        this.epoch = epoch;
        this.channel = channel;
        this.user = user;
        this.msg = msg;
        this.all = all;
        this.id = id;
    }

    public boolean schedule() {
        try {
            scheduleHandle = scheduler.schedule(getRunnable(), getTimeUntil(epoch), MILLISECONDS);
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    public boolean cancel() {
        return scheduleHandle.cancel(true);
    }

    private Runnable getRunnable() {
        return () -> {
            if (!all)
                channel.sendMessage("Hey " + user.getAsMention() + ", " + msg).queue();
            else
                channel.sendMessage("Hey @everyone, " + msg).queue();
        };
    }

    private long getTimeUntil(long epoch) {
        return epoch - System.currentTimeMillis();
    }

    public User getUser() {
        return user;
    }

    public String getMsg() {
        return msg;
    }

    public String getId() {
        return id;
    }

    public long getEpoch() {
        return epoch;
    }

    public String getTime() {
        SimpleDateFormat df = new SimpleDateFormat("MM/dd/yyyy h:mma z");
        return df.format(new Date(epoch));
    }

    public String toString() {
        StringBuilder ret = new StringBuilder(getId());
        ret.append(" : ");
        ret.append(getTime());
        ret.append(" : ");
        ret.append(getMsg());
        return ret.toString();
    }

    public MessageEmbed getEmbed(boolean withStatus) {
        EmbedBuilder msg = new EmbedBuilder();
        msg.setColor(Color.GREEN);
        msg.setAuthor(user.getName(), null, user.getEffectiveAvatarUrl());
        if (withStatus) {
            msg.setTitle("Status", null);
            msg.setDescription("Reminder set successfully.");
        }
        msg.addField("ID", getId(), false);
        msg.addField("Time", getTime(), false);
        msg.addField("Message", getMsg(), false);
        return msg.build();
    }

    public MessageEmbed getEmbed() {
        return getEmbed(true);
    }

    public int compareTo(Reminder r) {
        try {
            int ret = Math.toIntExact(epoch - r.getEpoch());
            return ret;
        } catch (ArithmeticException ex) {
            if (epoch > r.getEpoch())
                return 1;
        }
        return -1;
    }

    public static Comparator<Reminder> ReminderComparator = Reminder::compareTo;
}