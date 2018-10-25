package commands;

import java.awt.Color;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import bean.Item;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import services.XIVAPIService;
import utils.Emoji;
import utils.TokenManager;

public class XIVCommandListener extends ListenerAdapter {
    private final Map<String, GuildAction> commands;
    private XIVAPIService service;

    public XIVCommandListener() {
        commands = new HashMap<>();
        Gson gson = new GsonBuilder().setLenient().create();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://xivapi.com/")
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();
        service = retrofit.create(XIVAPIService.class);
        addCommands();
    }

    @Override
    public void onGuildMessageReceived(GuildMessageReceivedEvent event) {
        String command = event.getMessage().getContentRaw().split(" ", 2)[0].toLowerCase();
        if (commands.containsKey(command))
            commands.get(command).run(event);
    }

    private void addCommands() {
        addGetItemCommand();
    }

    private GuildAction addGetItemCommand() {
        GuildAction action = new GuildAction() {
            public String getCommand() {
                return "!xivitem";
            }

            public void run(GuildMessageReceivedEvent event) {
                String[] command = event.getMessage().getContentRaw().split(" ", 2);
                if (command.length < 2)
                    return;
                String input = command[1];
                event.getMessage().delete().queue();

                service.getItem(input, TokenManager.getXIVAPITok()).enqueue(new Callback<Item>() {
                    @Override
                    public void onResponse(Call<Item> call, Response<Item> response) {
                        if (response.isSuccessful()) {
                            Item item = response.body();
                            event.getChannel().sendMessage("Item name: " + item.getName()).queue();
                        }
                    }

                    @Override
                    public void onFailure(Call<Item> call, Throwable t) {
                        t.printStackTrace();
                    }
                });
            }
        };
        commands.put(action.getCommand(), action);
        return action;
    }
}