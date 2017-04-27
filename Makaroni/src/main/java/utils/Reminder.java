package utils;

import static java.util.concurrent.TimeUnit.*;

import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;

import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;

public class Reminder {
	private ScheduledExecutorService scheduler;
	private ScheduledFuture<?> scheduleHandle;
	private long epoch;
	private TextChannel channel;
	private User user;
	private String msg;
	private boolean all;

	public Reminder(long epoch, TextChannel channel, User user, String msg) {
		scheduler = Executors.newScheduledThreadPool(1);
		this.epoch = epoch;
		this.channel = channel;
		this.user = user;
		this.msg = msg;
		this.all = false;
	}

	public Reminder(long epoch, TextChannel channel, User user, String msg, boolean all) {
		scheduler = Executors.newScheduledThreadPool(1);
		this.epoch = epoch;
		this.channel = channel;
		this.user = user;
		this.msg = msg;
		this.all = all;
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
		Runnable remindRunnable = new Runnable() {
			public void run() {
				if (!all)
					channel.sendMessage("Hey " + user.getAsMention() + ", " + msg).queue();
				else
					channel.sendMessage("Hey @everyone, " + msg).queue();
			}
		};
		return remindRunnable;
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

	public long getEpoch() {
		return epoch;
	}

	public String getTime() {
		SimpleDateFormat df = new SimpleDateFormat("MM/dd/yyyy h:mma z");
		return df.format(new Date(epoch));
	}
	
	public String toString() {
		StringBuilder ret = new StringBuilder(getTime());
		ret.append(" : ");
		ret.append(getMsg());
		return ret.toString();
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

	public static Comparator<Reminder> ReminderComparator = new Comparator<Reminder>() {
		@Override
		public int compare(Reminder r1, Reminder r2) {
			return r1.compareTo(r2);
		}

	};
}