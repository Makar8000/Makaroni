package news;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.MessageEmbed;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class FFXIVNotification {
    private static final Map<String, Thread> threadMap = new HashMap<>();
    private static final String KEY_MAINT = "M";
    private static final String KEY_PATCH = "P";

    public static boolean startMaint(MessageChannel channel, String id) {
        String key = id + KEY_MAINT;
        if (threadMap.containsKey(key))
            return false;
        Thread t = new Thread(() -> FFXIVFetcher.checkMaintenance(channel));
        t.start();
        threadMap.put(key, t);
        return true;
    }

    public static boolean startPatch(MessageChannel channel, String id) {
        String key = id + KEY_PATCH;
        if (threadMap.containsKey(key))
            return false;
        Thread t = new Thread(() -> FFXIVFetcher.checkNewPatch(channel));
        t.start();
        threadMap.put(key, t);
        return true;
    }

    public static boolean stopMaint(String id) {
        return stopThread(id + KEY_MAINT);
    }

    public static boolean stopPatch(String id) {
        return stopThread(id + KEY_PATCH);
    }

    public static boolean setDelayMaint(String delayStr) {
        try {
            FFXIVFetcher.REQUEST_DELAY_MAINT = Long.parseLong(delayStr);
            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return false;
    }

    public static boolean setDelayPatch(String delayStr) {
        try {
            FFXIVFetcher.REQUEST_DELAY_PATCH = Long.parseLong(delayStr);
            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return false;
    }

    private static boolean stopThread(String key) {
        if (!threadMap.containsKey(key))
            return false;
        threadMap.get(key).interrupt();
        threadMap.remove(key);
        return true;
    }

    public static MessageEmbed getMessage(String title, String message) {
        EmbedBuilder emb = new EmbedBuilder();
        emb.setColor(new Color(43, 108, 140));
        emb.setTitle(title);
        emb.setDescription(message);
        return emb.build();
    }
}
