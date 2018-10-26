package news;

import net.dv8tion.jda.core.entities.TextChannel;
import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;


public class AionFetcher {
    public static void loopNews(TextChannel channel) {
        AionCache<AionArticle> cache = loadAionArtCache();

        while (true) {
            try {
                URL url = new URL("https://www.aiononline.com/data/aion-news.json");
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                con.connect();

                if (con.getResponseCode() != 200) {
                    Thread.sleep(60000);
                    continue;
                }

                String jsonString = IOUtils.toString(con.getInputStream(), StandardCharsets.UTF_8);
                con.disconnect();

                LinkedList<AionArticle> newArticles = new LinkedList<AionArticle>();
                try {
                    JSONArray json = new JSONArray(jsonString);
                    for(int i = 0; i < json.length(); i++) {
                        JSONObject jsonArt = json.getJSONObject(i);
                        AionArticle temp = new AionArticle(jsonArt);

                        if (!cache.contains(temp.getId())) {
                            cache.add(temp.getId(), temp);
                            newArticles.add(temp);
                            updateAionArtCache(cache);
                        }
                    }
                } catch (Exception ex) {
                    System.err.println("error parsing");
                }

                while (!newArticles.isEmpty()) {
                    channel.sendMessage(newArticles.removeLast().getMessage()).queue();
                }
            } catch (Exception ex) {

            }

            try {
                Thread.sleep(60000);
            } catch (Exception ex) {

            }
        }
    }

    @SuppressWarnings("unchecked")
    private static AionCache<AionArticle> loadAionArtCache() {
        AionCache<AionArticle> a;

        try {
            FileInputStream fis = new FileInputStream("aionartcache.dat");
            ObjectInputStream ois = new ObjectInputStream(fis);
            a = (AionCache<AionArticle>) ois.readObject();
            ois.close();
            fis.close();
        } catch (Exception e) {
            e.printStackTrace();
            return new AionCache<AionArticle>();
        }

        return a;
    }

    private static boolean updateAionArtCache(AionCache<AionArticle> cache) {
        try {
            new File("aionartcache.dat").delete();
            FileOutputStream fos = new FileOutputStream("aionartcache.dat");
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(cache);
            oos.close();
            fos.close();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return false;
    }
}
