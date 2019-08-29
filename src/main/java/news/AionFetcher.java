package news;

import net.dv8tion.jda.core.entities.TextChannel;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.util.LinkedList;


public class AionFetcher {
    public static void loopNews(TextChannel channel) {
        AionCache<AionArticle> cache = loadAionArtCache();
        OkHttpClient client = new OkHttpClient();

        while (true) {
            try {
                Request req = new Request.Builder().url("https://www.aiononline.com/data/aion-news.json").build();
                Response resp = client.newCall(req).execute();

                if (resp.code() != 200) {
                    Thread.sleep(60000);
                    continue;
                }

                LinkedList<AionArticle> newArticles = new LinkedList<>();
                try {
                    JSONArray json = new JSONArray(resp.body().string());
                    for (int i = 0; i < json.length(); i++) {
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
            return new AionCache<>();
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
