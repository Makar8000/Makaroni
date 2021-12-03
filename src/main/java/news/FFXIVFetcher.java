package news;

import net.dv8tion.jda.api.entities.PrivateChannel;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.math.BigInteger;
import java.net.InetAddress;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class FFXIVFetcher {
    public static final long REQUEST_DELAY = 10000;

    public static void checkNewPatch(PrivateChannel channel) {

    }

    public static void checkMaintenance(PrivateChannel channel) {
        OkHttpClient client = new OkHttpClient();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd-HH");

        while (true) {
            try {
                Request req = new Request.Builder()
                        .url("https://frontier.ffxiv.com/worldStatus/gate_status.json?" + System.currentTimeMillis())
                        .addHeader("User-Agent", String.format("SQEXAuthor/2.0.0(Windows 6.2; ja-jp; %s)", getComputerId()))
                        .addHeader("Accept-Encoding", "gzip, deflate")
                        .addHeader("Accept-Language", "en-US,en;q=0.8,ja;q=0.6,de-DE;q=0.4,de;q=0.2")
                        .addHeader("Origin", "https://launcher.finalfantasyxiv.com")
                        .addHeader("Referer", "https://launcher.finalfantasyxiv.com/v600/index.html?rc_lang=en-gb&time=" + sdf.format(new Date()))
                        .build();
                Response resp = client.newCall(req).execute();

                if (!resp.isSuccessful()) {
                    Thread.sleep(REQUEST_DELAY);
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

            try {
                Thread.sleep(REQUEST_DELAY);
            } catch (Exception ex) {

            }
        }
    }

    private static String getComputerId() throws Exception {
        String machineName = InetAddress.getLocalHost().getHostName();
        String userName = System.getProperty("user.name");
        String osVer = System.getProperty("os.name");
        String processorCount = "" + Runtime.getRuntime().availableProcessors();
        String hash = sha1(machineName + userName + osVer + processorCount);
        return hash;
    }

    private static String sha1(String str) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA-1");
        byte[] messageDigest = md.digest(str.getBytes());
        BigInteger no = new BigInteger(1, messageDigest);
        String hashtext = no.toString(16);

        while (hashtext.length() < 32)
            hashtext = "0" + hashtext;

        return hashtext;
    }
}
