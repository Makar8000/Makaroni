package bean;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.json.JSONException;
import org.json.JSONObject;
import utils.TokenManager;

import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class AriyalaSet {
    private String ariyalaId;
    private String job;
    private int dustings;
    private int twines;
    private boolean ester;
    private ArrayList<String> raidPieces;

    public AriyalaSet(String ariyalaId) {
        this.ariyalaId = ariyalaId;
        this.job = "";
        dustings = 0;
        twines = 0;
        ester = false;
        this.raidPieces = new ArrayList<>();
    }

    public String getAriyalaId() {
        return ariyalaId;
    }

    public void setJob(String job) {
        this.job = job;
    }

    public String getJob() {
        return job;
    }

    public int getDustings() {
        return dustings;
    }

    public int addCoating() {
        return ++dustings;
    }

    public int getTwines() {
        return twines;
    }

    public int addTwine() {
        return ++twines;
    }

    public boolean hasEster() {
        return ester;
    }

    public void setEster(boolean hasEster) {
        this.ester = hasEster;
    }

    public void addRaidPiece(String raidPiece) {
        raidPieces.add(raidPiece);
    }

    public String getRaidPieces() {
        StringBuilder ret = new StringBuilder();

        for (int i = 0; i < raidPieces.size(); i++) {
            String slot = raidPieces.get(i);
            ret.append(slot.substring(0, 1).toUpperCase());
            ret.append(slot.substring(1));
            if (i != raidPieces.size() - 1)
                ret.append(", ");
        }

        return ret.toString();
    }

    public MessageEmbed getMessage() {
        EmbedBuilder msg = new EmbedBuilder();
        msg.setColor(new Color(162, 132, 224));
        msg.setAuthor("Ariyala Data", ariyalaUrl + this.getAriyalaId(), iconUrl + this.getJob() + "_Solid.png");
        if (this.getDustings() > 0)
            msg.addField("Dusting(s)", "" + this.getDustings(), true);
        if (this.getTwines() > 0)
            msg.addField("Twine(s)", "" + this.getTwines(), true);
        if (this.hasEster())
            msg.addField("Ester", "1", true);
        msg.setTitle("Raid Pieces");
        msg.setThumbnail(thumbnailUrl);
        msg.setDescription(this.getRaidPieces());
        return msg.build();
    }

    public static AriyalaSet getFromId(String ariyalaId) {
        AriyalaSet set = new AriyalaSet(ariyalaId);

        try {
            /* Grab ariyala item list */
            Request ariyalaRequest = new Request.Builder().url(ariyalaUrl + "store.app?identifier=" + ariyalaId).build();
            Response ariyalaResponse = client.newCall(ariyalaRequest).execute();
            JSONObject json = new JSONObject(ariyalaResponse.body().string());
            String job = json.getString("content");
            set.setJob(job);

            /* Iterate through item list */
            JSONObject gearSet = json.getJSONObject("datasets").getJSONObject(job).getJSONObject("normal").getJSONObject("items");
            Iterator<String> keys = gearSet.keys();
            while (keys.hasNext()) {
                /* Determine current gear slot */
                String slot = keys.next();
                String augType = augmentTokens.get(slot);
                if (augType == null)
                    continue;

                /* Grab XIVAPI item data */
                int itemId = gearSet.getInt(slot);
                String xivApiUrl = apiUrl + "/Item/" + itemId + "?private_key=" + TokenManager.getXIVAPITok() + "&columns=Name";
                Request itemRequest = new Request.Builder().url(xivApiUrl).build();
                Response itemResponse = client.newCall(itemRequest).execute();
                String itemName = new JSONObject(itemResponse.body().string()).getString("Name");

                /* Determine upgrade item if the item is augmented */
                if (itemName.startsWith("Augmented")) {
                    if (augType.equals("dusting"))
                        set.addCoating();
                    if (augType.equals("twine"))
                        set.addTwine();
                    if (augType.equals("ester"))
                        set.setEster(true);
                } else {
                    if (slot.startsWith("ring"))
                        slot = "ring";
                    if (slot.equals("mainhand"))
                        slot = "weapon";
                    set.addRaidPiece(slot);
                }
            }
        } catch (IOException | JSONException ex) {
            return null;
        }

        return set;
    }

    private static final OkHttpClient client = new OkHttpClient();
    private static final String ariyalaUrl = "http://ffxiv.ariyala.com/";
    private static final String apiUrl = "http://xivapi.com";
    private static final String thumbnailUrl = "https://i.imgur.com/PXL66Ix.png";
    private static final String iconUrl = "https://raw.githubusercontent.com/anoyetta/ACT.Hojoring/master/source/ACT.SpecialSpellTimer/ACT.SpecialSpellTimer.Core/resources/icon/Job/";
    private static final Map<String, String> augmentTokens = new HashMap<String, String>() {
        {
            put("mainhand", "ester");
            put("head", "twine");
            put("chest", "twine");
            put("hands", "twine");
            put("waist", "dusting");
            put("legs", "twine");
            put("feet", "twine");
            put("ears", "dusting");
            put("neck", "dusting");
            put("wrist", "dusting");
            put("ringLeft", "dusting");
            put("ringRight", "dusting");
        }
    };
}
