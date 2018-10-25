package utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import net.dv8tion.jda.core.entities.User;

public class ReminderManager {
	private final Map<String, Reminder> reminders;

	public ReminderManager() {
		reminders = new HashMap<String, Reminder>();
	}

	public void add(String id, Reminder reminder) {
		reminder.schedule();
		reminders.put(id, reminder);
	}

	public boolean remove(String id, User u) {
		if (reminders.containsKey(id)) {
			Reminder rem = reminders.get(id);
			if (rem.getUser().getId().equals(u.getId())) {
				rem.cancel();
				reminders.remove(id);
				return true;
			}
		}
		return false;
	}

	public ArrayList<Reminder> getAll(User u) {
		ArrayList<Reminder> ret = new ArrayList<Reminder>();
		reminders.entrySet().removeIf(r -> r.getValue().getEpoch() + 120000 < System.currentTimeMillis());
		reminders.forEach((k, r) -> {
			if (r.getUser().getId().equals(u.getId()))
				ret.add(r);
		});
		ret.sort(Reminder.ReminderComparator);
		return ret;
	}
}
