package commands;

import net.dv8tion.jda.core.events.ShutdownEvent;
import net.dv8tion.jda.core.events.guild.member.GuildMemberNickChangeEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class NicknameChangeListener extends ListenerAdapter {
	private final Map<Long, Long> nickChanges;
	private final String fileName = "nickchanges.dat";

	public NicknameChangeListener() {
		nickChanges = readFile(fileName);
	}


	@Override
	public void onGuildMemberNickChange(GuildMemberNickChangeEvent event) {
		if(event.getGuild().getAuditLogs().complete().get(0).getUser().getIdLong() != event.getUser().getIdLong()) 
			return;
			
		long canadian = event.getGuild().getRolesByName("canadian", true).get(0).getIdLong();
		if(event.getMember().getRoles().stream().anyMatch(r -> r.getIdLong() == canadian)) 
			return;
		
		if(nickChanges.containsKey(event.getUser().getIdLong())) {
			long timePassed = System.currentTimeMillis() - nickChanges.get(event.getUser().getIdLong()).longValue();
			long daysPassed = timePassed / (1000 * 60 * 60 * 24);
			
			if(daysPassed < 7) {
				long canChangeTime = nickChanges.get(event.getUser().getIdLong()).longValue() + (1000 * 60 * 60 * 24 * 7);
				SimpleDateFormat df = new SimpleDateFormat("MM/dd/yyyy h:mma z");
				StringBuilder msg = new StringBuilder("Sorry, but nickname changes in Canadaland are restricted to once a week. Please try again after ");
				msg.append(df.format(new Date(canChangeTime)));
				msg.append(".\n\n If you need an exception, please contact Makar.");

				try {
					event.getUser().openPrivateChannel().queue(chan -> chan.sendMessage(msg.toString()).queue());
				} catch (Exception ex) {
					ex.printStackTrace();
				}

				event.getGuild().getController().setNickname(event.getMember(), event.getPrevNick()).queue();

			} else {
				nickChanges.put(event.getUser().getIdLong(), System.currentTimeMillis());
			}
		} else {
			nickChanges.put(event.getUser().getIdLong(), System.currentTimeMillis());
		}
	}

	private HashMap<Long, Long> readFile(String fn) {
		HashMap<Long, Long> ret = new HashMap<Long, Long>();

		try {
			BufferedReader br = new BufferedReader(new FileReader(fn));
			
			String line;
			while ((line = br.readLine()) != null) {
				String[] data = line.split(",");
				if (data.length != 2)
					continue;

				ret.put(Long.parseLong(data[0]), Long.parseLong(data[1]));
			}
			
			br.close();
		} catch (IOException e) {
			
		}
		
		return ret;
	}
	
	@Override
	public void onShutdown(ShutdownEvent event) {
		try {
			PrintWriter pr = new PrintWriter(new FileWriter(fileName));
			
			nickChanges.forEach((u, t) -> {
				pr.println(u + "," + t);
			});
			
			pr.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}