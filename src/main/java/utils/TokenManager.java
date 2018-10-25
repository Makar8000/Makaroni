package utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class TokenManager {
    private static String discordToken;
    private static String xivapiToken;

    public static String getDiscordTok() {
        if(discordToken != null && discordToken.length() > 0)
            return discordToken;

        try {
            discordToken = new String(Files.readAllBytes(new File("bot.tok").toPath()));
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }

        return discordToken;
    }

    public static String getXIVAPITok() {
        if(xivapiToken != null && xivapiToken.length() > 0)
            return xivapiToken;

        try {
            xivapiToken = new String(Files.readAllBytes(new File("xivapi.tok").toPath()));
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }

        return xivapiToken;
    }
}
