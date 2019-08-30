package bean;

import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.MessageEmbed;
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
    private String job;
    private int coatings;
    private int twines;
    private boolean solvent;
    private ArrayList<String> raidPieces;

    public AriyalaSet() {
        this.job = "";
        coatings = 0;
        twines = 0;
        solvent = false;
        this.raidPieces = new ArrayList<>();
    }

    public void setJob(String job) {
        this.job = job;
    }

    public String getJob() {
        return job;
    }

    public int getCoatings() {
        return coatings;
    }

    public int addCoating() {
        return ++coatings;
    }

    public int getTwines() {
        return twines;
    }

    public int addTwine() {
        return ++twines;
    }

    public boolean hasSolvent() {
        return solvent;
    }

    public void setSolvent(boolean hasSolvent) {
        this.solvent = hasSolvent;
    }

    public void addRaidPiece(String raidPiece) {
        raidPieces.add(raidPiece);
    }

    public String getRaidPieces() {
        StringBuilder ret = new StringBuilder();

        for (String p : raidPieces) {
            ret.append(p);
            ret.append(", ");
        }

        return ret.toString();
    }

    public MessageEmbed getMessage() {
        EmbedBuilder msg = new EmbedBuilder();
        msg.setColor(new Color(131, 149, 229));
        msg.setAuthor("Ariyala Data", null, iconUrl + job + "_Solid.png");
        msg.addField("Coatings", ""+this.getCoatings(), true);
        msg.addField("Twines", ""+this.getTwines(), true);
        msg.setTitle("Raid Pieces");
        msg.setThumbnail(thumbnailUrl);
        msg.setDescription(this.getRaidPieces());
        return msg.build();
    }

    public static AriyalaSet getFromId(String ariyalaId) {
        AriyalaSet set = new AriyalaSet();

        try {
            /* Grab ariyala item list */
            Request ariyalaRequest = new Request.Builder().url(ariyalaUrl + ariyalaId).build();
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
                String xivApiUrl = baseUrl + "/Item/" + itemId + "?private_key=" + TokenManager.getXIVAPITok() + "&columns=Name";
                Request itemRequest = new Request.Builder().url(xivApiUrl).build();
                Response itemResponse = client.newCall(itemRequest).execute();
                String itemName = new JSONObject(itemResponse.body().string()).getString("Name");

                /* Determine upgrade item if the item is augmented */
                if (itemName.startsWith("Augmented")) {
                    if (augType.equals("coating"))
                        set.addCoating();
                    if (augType.equals("twine"))
                        set.addTwine();
                    if (augType.equals("solvent"))
                        set.setSolvent(true);
                } else {
                    if (slot.startsWith("ring"))
                        slot = "ring";
                    set.addRaidPiece(slot);
                }
            }
        } catch (IOException ex) {
            return null;
        } catch (JSONException ex) {
            return null;
        }

        return set;
    }

    private static final OkHttpClient client = new OkHttpClient();
    private static final String ariyalaUrl = "http://ffxiv.ariyala.com/store.app?identifier=";
    private static final String baseUrl = "http://xivapi.com";
    private static final String thumbnailUrl = "https://i.imgur.com/UhbRFE2.png";
    private static final String iconUrl = "https://raw.githubusercontent.com/anoyetta/ACT.Hojoring/master/source/ACT.SpecialSpellTimer/ACT.SpecialSpellTimer.Core/resources/icon/Job/";
    private static final Map<String, String> augmentTokens = new HashMap<String, String>() {
        {
            put("mainthand", "solvent");
            put("head", "twine");
            put("chest", "twine");
            put("hands", "twine");
            put("waist", "coating");
            put("legs", "twine");
            put("feet", "twine");
            put("ears", "coating");
            put("neck", "coating");
            put("wrist", "coating");
            put("ringLeft", "coating");
            put("ringRight", "coating");
        }
    };
}
