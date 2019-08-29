package commands;

import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.json.JSONException;
import org.json.JSONObject;
import utils.Constants;
import utils.TokenManager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class XIVCommandListener extends ListenerAdapter {
    private final OkHttpClient client = new OkHttpClient();
    private final Map<String, GuildAction> commands;
    private final String keyParam;
    private final String baseUrl;
    private final String ariyalaUrl;
    private final Map<String, String> augmentTokens;

    public XIVCommandListener() {
        commands = new HashMap<>();
        keyParam = "?private_key=" + TokenManager.getXIVAPITok();
        baseUrl = "http://xivapi.com";
        ariyalaUrl = "http://ffxiv.ariyala.com/store.app?identifier=";
        augmentTokens = new HashMap<>();
        augmentTokens.put("mainthand", "solvent");
        augmentTokens.put("head", "twine");
        augmentTokens.put("chest", "twine");
        augmentTokens.put("hands", "twine");
        augmentTokens.put("waist", "coating");
        augmentTokens.put("legs", "twine");
        augmentTokens.put("feet", "twine");
        augmentTokens.put("ears", "coating");
        augmentTokens.put("neck", "coating");
        augmentTokens.put("wrist", "coating");
        augmentTokens.put("ringLeft", "coating");
        augmentTokens.put("ringRight", "coating");
        addCommands();
    }

    @Override
    public void onGuildMessageReceived(GuildMessageReceivedEvent event) {
        String command = event.getMessage().getContentRaw().split(" ", 2)[0].toLowerCase();
        if (commands.containsKey(command))
            commands.get(command).run(event);
    }

    private void addCommands() {
        addGetLootCommand();
    }

    private GuildAction addGetLootCommand() {
        GuildAction action = new GuildAction() {
            public String getCommand() {
                return "lootbreakdown";
            }

            public void run(GuildMessageReceivedEvent event) {
                String[] command = event.getMessage().getContentRaw().split(" ", 2);
                if (command.length != 2)
                    return;

                try {
                    StringBuilder ret = new StringBuilder();

                    String ariyalaId = command[1].substring(command[1].lastIndexOf('/') + 1);
                    Request ariyalaRequest = new Request.Builder().url(ariyalaUrl + ariyalaId).build();
                    Response ariyalaResponse = client.newCall(ariyalaRequest).execute();
                    JSONObject json = new JSONObject(ariyalaResponse.body().string());

                    String job = json.getString("content");
                    ret.append("Job: " + job + "\n");
                    JSONObject gearSet = json.getJSONObject("datasets").getJSONObject(job).getJSONObject("normal").getJSONObject("items");
                    Iterator<String> keys = gearSet.keys();
                    int coatings = 0;
                    int twines = 0;
                    boolean solvent = false;
                    ArrayList<String> raidPieces = new ArrayList<>();

                    while (keys.hasNext()) {
                        String slot = keys.next();
                        String augType = augmentTokens.get(slot);
                        if (augType == null)
                            continue;

                        int itemId = gearSet.getInt(slot);
                        String itemUrl = baseUrl + "/Item/" + itemId + keyParam + "&columns=Name";
                        Request itemRequest = new Request.Builder().url(itemUrl).build();
                        Response itemResponse = client.newCall(itemRequest).execute();
                        String itemName = new JSONObject(itemResponse.body().string()).getString("Name");

                        if (itemName.startsWith("Augmented")) {
                            if (augType.equals("coating"))
                                coatings++;
                            if (augType.equals("twine"))
                                twines++;
                            if (augType.equals("solvent"))
                                solvent = true;
                        } else {
                            if (slot.startsWith("ring"))
                                slot = "ring";
                            raidPieces.add(slot);
                        }
                    }

                    if (coatings > 0)
                        ret.append("Coatings: " + coatings + "\n");
                    if (twines > 0)
                        ret.append("Twines: " + twines + "\n");
                    if (solvent)
                        ret.append("Solvent: 1\n");
                    ret.append("Raid Pieces: ");
                    for (String s : raidPieces)
                        ret.append(s + ", ");

                    event.getChannel().sendMessage(ret.toString()).queue();
                } catch (IOException ex) {
                    event.getChannel().sendMessage("IO/Network Error!").queue();
                    ex.printStackTrace();
                } catch (JSONException ex) {
                    event.getChannel().sendMessage("API Error!").queue();
                    ex.printStackTrace();
                }
            }
        };
        commands.put(Constants.PREFIX + action.getCommand(), action);
        return action;
    }
}