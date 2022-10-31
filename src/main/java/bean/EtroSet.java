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

public class EtroSet {
    private String etroId;
    private String job;
    private int dustings;
    private int twines;
    private boolean ester;
    private ArrayList<String> raidPieces;

    public EtroSet(String etroId) {
        this.etroId = etroId;
        this.job = "";
        dustings = 0;
        twines = 0;
        ester = false;
        this.raidPieces = new ArrayList<>();
    }

    public String getEtroId() {
        return etroId;
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

    public int addDusting() {
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
        msg.setColor(new Color(180, 96, 166));
        msg.setAuthor("Etro Data", etroUrl + this.getEtroId(), iconUrl + this.getJob() + "_Solid.png");
        if (this.getTwines() > 0)
            msg.addField("Leftside Aug(s)", "" + this.getTwines(), true);
        if (this.getDustings() > 0)
            msg.addField("Rightside Aug(s)", "" + this.getDustings(), true);
        if (this.hasEster())
            msg.addField("Weapon Aug", "1", true);
        msg.setTitle("Raid Pieces");
        msg.setThumbnail(thumbnailUrl);
        msg.setDescription(this.getRaidPieces());
        return msg.build();
    }

    public static EtroSet getFromId(String etroId) {
        EtroSet set = new EtroSet(etroId);

        try {
            /* Grab etro item list */
            Request etroRequest = new Request.Builder().url(etroApiUrl + etroId).build();
            Response etroResponse = client.newCall(etroRequest).execute();
            JSONObject gearSet = new JSONObject(etroResponse.body().string());
            String job = gearSet.getString("jobAbbrev");
            set.setJob(job);

            /* Iterate through item list */
            Iterator<String> keys = augmentTokens.keySet().iterator();
            while (keys.hasNext()) {
                /* Determine current gear slot */
                String slot = keys.next();
                String augType = augmentTokens.get(slot);

                /* Grab XIVAPI item data */
                int itemId = gearSet.getInt(slot);
                String xivApiUrl = apiUrl + "/Item/" + itemId + "?private_key=" + TokenManager.getXIVAPITok() + "&columns=Name";
                Request itemRequest = new Request.Builder().url(xivApiUrl).build();
                Response itemResponse = client.newCall(itemRequest).execute();
                String itemName = new JSONObject(itemResponse.body().string()).getString("Name");

                /* Determine upgrade item if the item is augmented */
                if (itemName.startsWith("Augmented")) {
                    if (augType.equals("dusting"))
                        set.addDusting();
                    if (augType.equals("twine"))
                        set.addTwine();
                    if (augType.equals("ester"))
                        set.setEster(true);
                } else {
                    if (slot.startsWith("finger"))
                        slot = "ring";
                    set.addRaidPiece(slot);
                }
            }
        } catch (IOException | JSONException ex) {
            return null;
        }

        return set;
    }

    private static final OkHttpClient client = new OkHttpClient();
    private static final String etroApiUrl = "http://etro.gg/api/gearsets/";
    private static final String etroUrl = "https://etro.gg/gearset/";
    private static final String apiUrl = "http://xivapi.com";
    private static final String thumbnailUrl = "https://i.imgur.com/huoF8gA.png";
    private static final String iconUrl = "https://raw.githubusercontent.com/anoyetta/ACT.Hojoring/master/source/ACT.SpecialSpellTimer/ACT.SpecialSpellTimer.Core/resources/icon/Job/";
    private static final Map<String, String> augmentTokens = new HashMap<String, String>() {
        {
            put("weapon", "ester");
            put("head", "twine");
            put("body", "twine");
            put("hands", "twine");
            put("legs", "twine");
            put("feet", "twine");
            put("ears", "dusting");
            put("neck", "dusting");
            put("wrists", "dusting");
            put("fingerL", "dusting");
            put("fingerR", "dusting");
        }
    };
}
