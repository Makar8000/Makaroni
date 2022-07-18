package news;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import okhttp3.*;
import utils.TokenManager;

import java.net.InetAddress;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FFXIVFetcher {
    private static final Random rand = new Random();
    private static final int REQUEST_DELAY_VARIANCE = 2000;
    public static long REQUEST_DELAY_PATCH = 8000;
    public static long REQUEST_DELAY_MAINT = 8000;

    // https://github.com/goatcorp/FFXIVQuickLauncher/blob/6.2.43/src/XIVLauncher.Common/Constants.cs#L21
    private static final String PATCHER_USER_AGENT = "FFXIV PATCH CLIENT";
    // https://github.com/goatcorp/FFXIVQuickLauncher/blob/6.2.43/src/XIVLauncher.Common/Game/Launcher.cs#L673
    private static final String REFERER_LAUNCHER = "https://launcher.finalfantasyxiv.com/v610/index.html?rc_lang=en-us&time=";
    // https://github.com/goatcorp/FFXIVQuickLauncher/blob/6.2.43/src/XIVLauncher.Common/Game/Launcher.cs#L510
    private static final String OAUTH_TOP_URL = "https://ffxiv-login.square-enix.com/oauth/ffxivarr/login/top?lng=en&rgn=3&isft=0&cssmode=1&isnew=1&launchver=3";
    // When checking for future patches, this needs to be updated to be current values
    private static final String[] VER_INFO = {
            "2022.07.08.0000.0000", // Base ffxivgame.ver
            "2022.05.26.0000.0000", // HW   ex1.ver
            "2022.05.26.0000.0000", // SB   ex2.ver
            "2022.05.26.0000.0000", // ShB  ex3.ver
            "2022.05.27.0000.0000"  // EW   ex4.ver
    };
    // Until a proper way is implemented, get this from https://github.com/goatcorp/FFXIVQuickLauncher/blob/6.2.43/src/XIVLauncher.Common/Game/Launcher.cs#L271
    // It is a hash of "ffxivboot.exe", "ffxivboot64.exe", "ffxivlauncher.exe", "ffxivlauncher64.exe", "ffxivupdater.exe", "ffxivupdater64.exe"
    // This means it shouldn't need an update unless there is an update to these files
    private static final String BOOT_VER_HASH = "2022.03.25.0000.0001=ffxivboot.exe/1030040/82ea9c341751c6cef6454f342c42211c32edbeb5,ffxivboot64.exe/1250200/18fdc2e05715c66f12bd00030c9ecf8def3582cb,ffxivlauncher.exe/10042776/6643c7d565bbe54255c17bf243eab36aea17f2d3,ffxivlauncher64.exe/10128280/439c921b4d1eee911e1cd0150b6a2ae5bf21853d,ffxivupdater.exe/1061272/b093ef36524fd273c956986213d14a0af0dcc432,ffxivupdater64.exe/1294744/966754f32e429e3010f4916ba42a86f76cf0d6ee";
    private static final String VER_REPORT = String.format("%s\nex1\t%s\nex2\t%s\nex3\t%s\nex4\t%s", BOOT_VER_HASH, VER_INFO[1], VER_INFO[2], VER_INFO[3], VER_INFO[4]);
    private static String userAgent = "";

    public static void checkNewPatch(MessageChannel channel) {
        String[] xivLogin = TokenManager.getXIVLogin();
        if (xivLogin[0].isEmpty() || xivLogin[1].isEmpty()) {
            channel.sendMessage(FFXIVNotification.getMessage("FFXIV Notifications", "Invalid token in file.")).queue();
            return;
        }

        final String msgTitle = "FFXIV Patch Status";
        Message retryMessage = null;
        int retryCount = 0;

        while (!Thread.currentThread().isInterrupted()) {
            try {
                try {
                    userAgent = String.format("SQEXAuthor/2.0.0(Windows 6.2; ja-jp; %s)", getComputerId());
                } catch (Exception e) {
                    e.printStackTrace();
                    return;
                }


                String stored = getOAuthTop();
                String sessionId = null;
                String patchList = null;

                if (stored != null)
                    sessionId = getSessionId(xivLogin[0], xivLogin[1], stored);

                if (sessionId != null)
                    patchList = registerSession(sessionId);

                if (patchList != null && !patchList.isEmpty()) {
                    System.out.println(patchList);
                    channel.sendMessage(FFXIVNotification.getMessage(msgTitle, String.format("New patches available! %s", patchList))).queue();
                    return;
                }

                if (patchList == null) {
                    String retryStr = "An issue occured checking for patch updates. Retrying... %s";
                    if (retryMessage == null)
                        retryMessage = channel.sendMessage(FFXIVNotification.getMessage(msgTitle, String.format(retryStr, " "))).complete();
                    if (retryCount > 0)
                        retryMessage.editMessage(FFXIVNotification.getMessage(msgTitle, String.format(retryStr, retryCount))).queue();
                    retryCount++;
                } else if (retryMessage != null) {
                    retryMessage.delete().queue();
                    retryMessage = null;
                    retryCount = 0;
                }

                Thread.sleep(REQUEST_DELAY_PATCH + rand.nextInt(REQUEST_DELAY_VARIANCE));
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                return;
            } catch (Exception ex) {
                channel.sendMessage(FFXIVNotification.getMessage(msgTitle, "An issue occured checking for patch updates. Maybe there is an update?")).queue();
            }
        }
    }

    public static void checkMaintenance(MessageChannel channel) {
        OkHttpClient client = new OkHttpClient();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd-HH-mm");

        while (!Thread.currentThread().isInterrupted()) {
            try {
                try {
                    userAgent = String.format("SQEXAuthor/2.0.0(Windows 6.2; ja-jp; %s)", getComputerId());
                } catch (Exception e) {
                    e.printStackTrace();
                    return;
                }

                try {
                    Request req = new Request.Builder()
                            .url("https://frontier.ffxiv.com/worldStatus/gate_status.json?" + System.currentTimeMillis())
                            .addHeader("User-Agent", userAgent)
                            .addHeader("Accept-Encoding", "gzip, deflate")
                            .addHeader("Accept-Language", "en-US,en;q=0.8,ja;q=0.6,de-DE;q=0.4,de;q=0.2")
                            .addHeader("Origin", "https://launcher.finalfantasyxiv.com")
                            .addHeader("Referer", REFERER_LAUNCHER + sdf.format(new Date()))
                            .build();
                    Response resp = client.newCall(req).execute();

                    if (!resp.isSuccessful()) {
                        Thread.sleep(REQUEST_DELAY_MAINT + rand.nextInt(REQUEST_DELAY_VARIANCE));
                        continue;
                    }

                    boolean gateStatus = resp.body().string().charAt(10) == '1';
                    if (gateStatus) {
                        channel.sendMessage(FFXIVNotification.getMessage("FFXIV Server Status", "FFXIV is now online!")).queue();
                        return;
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }

                Thread.sleep(REQUEST_DELAY_MAINT + rand.nextInt(REQUEST_DELAY_VARIANCE));
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                return;
            }
        }
    }

    private static String registerSession(String sessionId) {
        OkHttpClient client = new OkHttpClient();
        String curVer = VER_INFO[0];

        try {
            RequestBody formBody = RequestBody.create(VER_REPORT, MediaType.get("text/plain; charset=utf-8"));
            Request req = new Request.Builder()
                    .url(String.format("https://patch-gamever.ffxiv.com/http/win32/ffxivneo_release_game/%1$s/%2$s", curVer, sessionId))
                    .addHeader("X-Hash-Check", "enabled")
                    .addHeader("User-Agent", PATCHER_USER_AGENT)
                    .post(formBody)
                    .build();
            Response resp = client.newCall(req).execute();
            if (!resp.isSuccessful())
                return null;

            return parsePatchList(resp.body().string());
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return null;
    }

    private static String getSessionId(String u, String p, String stored) {
        OkHttpClient client = new OkHttpClient();

        try {
            RequestBody formBody = new FormBody.Builder()
                    .add("_STORED_", stored)
                    .add("sqexid", u)
                    .add("password", p)
                    .add("otppw", "")
                    .build();
            Request req = new Request.Builder()
                    .url("https://ffxiv-login.square-enix.com/oauth/ffxivarr/login/login.send")
                    .addHeader("Accept", "image/gif, image/jpeg, image/pjpeg, application/x-ms-application, application/xaml+xml, application/x-ms-xbap, */*")
                    .addHeader("Referer", OAUTH_TOP_URL)
                    .addHeader("Accept-Encoding", "gzip, deflate")
                    .addHeader("Accept-Language", "en-US,en;q=0.8,ja;q=0.6,de-DE;q=0.4,de;q=0.2")
                    .addHeader("User-Agent", userAgent)
                    .addHeader("Host", "ffxiv-login.square-enix.com")
                    .addHeader("Connection", "Keep-Alive")
                    .addHeader("Cache-Control", "no-cache")
                    .addHeader("Cookie", "_rsid=\"\"")
                    .post(formBody)
                    .build();
            Response resp = client.newCall(req).execute();
            if (!resp.isSuccessful())
                return null;

            String body = resp.body().string();
            String regexStr = "window.external.user\\(\"login=auth,ok,(?<launchParams>.*)\\);";
            Pattern rgx = Pattern.compile(regexStr);
            Matcher matches = rgx.matcher(body);
            if (matches.find()) {
                String[] results = matches.group("launchParams").split(",");
                if (results[0].equalsIgnoreCase("sid"))
                    return results[1];
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return null;
    }

    private static String getOAuthTop() {
        OkHttpClient client = new OkHttpClient();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd-HH-mm");

        try {
            Request req = new Request.Builder()
                    .url(OAUTH_TOP_URL)
                    .addHeader("Accept", "image/gif, image/jpeg, image/pjpeg, application/x-ms-application, application/xaml+xml, application/x-ms-xbap, */*")
                    .addHeader("Referer", REFERER_LAUNCHER + sdf.format(new Date()))
                    .addHeader("Accept-Encoding", "gzip, deflate")
                    .addHeader("Accept-Language", "en-US,en;q=0.8,ja;q=0.6,de-DE;q=0.4,de;q=0.2")
                    .addHeader("User-Agent", userAgent)
                    .addHeader("Connection", "Keep-Alive")
                    .addHeader("Cookie", "_rsid=\"\"")
                    .build();
            Response resp = client.newCall(req).execute();
            if (!resp.isSuccessful())
                return null;

            String body = resp.body().string();
            String regexStr = "\\t<\\s*input .* name=\"_STORED_\" value=\"(?<stored>.*)\">";
            Pattern rgx = Pattern.compile(regexStr);
            Matcher matches = rgx.matcher(body);
            if (matches.find())
                return matches.group("stored");
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return null;
    }

    private static String getComputerId() throws Exception {
        String machineName = InetAddress.getLocalHost().getHostName();
        String userName = System.getProperty("user.name");
        String osVer = System.getProperty("os.name");
        String processorCount = "" + Runtime.getRuntime().availableProcessors();
        String hashText = machineName + userName + osVer + processorCount;

        MessageDigest md = MessageDigest.getInstance("SHA-1");
        byte[] bytes = md.digest(hashText.getBytes());
        bytes[0] = (byte) -(bytes[1] + bytes[2] + bytes[3] + bytes[4]);

        StringBuilder hex = new StringBuilder();
        for (int i = 0; i < 5; i++)
            hex.append(String.format("%02X", bytes[i]));
        return hex.toString().toLowerCase();
    }

    private static String parsePatchList(String body) {
        try {
            Pattern r = Pattern.compile("[\\.\\w]*?\\.patch");
            Matcher m = r.matcher(body);
            StringBuilder ret = new StringBuilder("\n");
            while (m.find()) {
                ret.append("\n");
                ret.append(m.group());
            }

            return ret.toString();
        } catch (Exception ex) {
            ex.printStackTrace();
            return body;
        }
    }
}
