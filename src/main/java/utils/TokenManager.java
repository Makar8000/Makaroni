package utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Base64;

public class TokenManager {
    private static String discordToken;
    private static String xivapiToken;
    private static String xivLoginToken;

    public static String getDiscordTok() {
        if (discordToken != null && discordToken.length() > 0)
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
        if (xivapiToken != null && xivapiToken.length() > 0)
            return xivapiToken;

        try {
            xivapiToken = new String(Files.readAllBytes(new File("xivapi.tok").toPath()));
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }

        return xivapiToken;
    }

    public static String[] getXIVLogin() {
        String tok = getXIVLoginTok();
        if (tok == null || tok.length() == 0) {
            return new String[]{"", ""};
        }
        return new String(Base64.getDecoder().decode(tok)).split(":");
    }

    private static String getXIVLoginTok() {
        if (xivLoginToken != null && xivLoginToken.length() > 0)
            return xivLoginToken;

        try {
            xivLoginToken = new String(Files.readAllBytes(new File("xivlogin.tok").toPath()));
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }

        return xivLoginToken;
    }
}
