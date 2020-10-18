package utils;

import bean.Santa;
import com.google.gson.reflect.TypeToken;

import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class SantaManager implements Serializable {
    private static final long serialVersionUID = 6144114037574556203L;
    private static final String fileName = "santas.json";
    private Map<String, Santa> santas;
    private Map<String, String> selectedPairs;
    private Map<String, ArrayList<String>> blacklistedPairs;
    private boolean gameStarted;

    public SantaManager() {
        santas = new HashMap<>();
        selectedPairs = new HashMap<>();
        blacklistedPairs = new HashMap<>();
        gameStarted = false;
    }

    public boolean add(Santa santa) {
        boolean ret = !santas.containsKey(santa.getDiscordID());
        santas.put(santa.getDiscordID(), santa);
        Data.saveAsJson(this, fileName);
        return ret;
    }

    public boolean remove(String discordID) {
        boolean ret = santas.containsKey(discordID);
        santas.remove(discordID);
        Data.saveAsJson(this, fileName);
        return ret;
    }

    public int size() {
        return santas.size();
    }

    public void setReceiver(String santa, String receiver) {
        selectedPairs.put(santa, receiver);
        Data.saveAsJson(this, fileName);
    }

    public String getReceiver(String santa) {
        if (!gameStarted)
            return null;

        return selectedPairs.get(santa);
    }

    public String getSanta(String receiver) {
        if (!gameStarted)
            return null;

        for (Map.Entry<String, String> entry : selectedPairs.entrySet()) {
            String s = entry.getKey();
            String r = entry.getValue();
            if (r.equals(receiver))
                return s;
        }

        return null;
    }

    public boolean started() {
        return this.gameStarted;
    }

    public void start() {
        this.gameStarted = true;
        Data.saveAsJson(this, fileName);
    }

    public void reset() {
        this.santas = new HashMap<>();
        this.selectedPairs = new HashMap<>();
        this.gameStarted = false;
        Data.saveAsJson(this, fileName);
    }

    public ArrayList<Santa> getAll() {
        ArrayList<Santa> ret = new ArrayList<>(santas.values());
        do
            Collections.shuffle(ret);
        while (!checkExclusions(ret));

        return ret;
    }

    public String toString() {
        StringBuilder str = new StringBuilder();

        santas.forEach((k, s) -> {
            str.append(s.toString());
            str.append("\n\n");
        });

        return str.toString();
    }

    private boolean checkExclusions(ArrayList<Santa> s) {
        if (s.size() < 2)
            return true;

        for (int i = 0; i < s.size(); i++) {
            int j = i == s.size() - 1 ? 0 : i + 1;

            String santaId = s.get(i).getDiscordID();
            String receiverId = s.get(j).getDiscordID();

            ArrayList<String> blkListedReceivers = blacklistedPairs.get(santaId);
            if (blkListedReceivers == null || blkListedReceivers.isEmpty())
                continue;

            for (String blkListedReceiver : blkListedReceivers) {
                if (receiverId.equals(blkListedReceiver))
                    return false;
            }
        }
        return true;
    }

    public static SantaManager loadSantas() {
        Type objectType = new TypeToken<SantaManager>() {}.getType();
        SantaManager s = (SantaManager) Data.loadFromJson(objectType, fileName);

        if (s == null)
            s = new SantaManager();

        return s;
    }
}
