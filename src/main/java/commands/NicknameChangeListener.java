package commands;

import net.dv8tion.jda.api.events.guild.member.update.GuildMemberUpdateNicknameEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

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
    public void onGuildMemberUpdateNickname(GuildMemberUpdateNicknameEvent event) {
        if (event.getGuild().retrieveAuditLogs().cache(false).complete().get(0).getUser().getIdLong() != event.getUser().getIdLong())
            return;

        long canadian = event.getGuild().getRolesByName("canadian", true).get(0).getIdLong();
        if (event.getMember().getRoles().stream().anyMatch(r -> r.getIdLong() == canadian))
            return;

        if (nickChanges.containsKey(event.getUser().getIdLong())) {
            long timePassed = System.currentTimeMillis() - nickChanges.get(event.getUser().getIdLong()).longValue();
            long daysPassed = timePassed / (1000 * 60 * 60 * 24);

            if (daysPassed < 7) {
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

                event.getMember().modifyNickname(event.getOldNickname()).queue();

            } else {
                nickChanges.put(event.getUser().getIdLong(), System.currentTimeMillis());
                saveFile(fileName);
            }
        } else {
            nickChanges.put(event.getUser().getIdLong(), System.currentTimeMillis());
            saveFile(fileName);
        }
    }

    private HashMap<Long, Long> readFile(String fn) {
        HashMap<Long, Long> ret = new HashMap<>();

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

    private void saveFile(String fn) {
        try {
            PrintWriter pr = new PrintWriter(new FileWriter(fn));

            nickChanges.forEach((u, t) -> pr.println(u + "," + t));

            pr.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}