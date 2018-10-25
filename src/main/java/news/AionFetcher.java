package news;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import net.dv8tion.jda.core.entities.TextChannel;


public class AionFetcher {
	public static void loopNews(TextChannel channel) {
		AionCache<AionArticle> cache = loadAionArtCache();

		while (true) {
			try {				
				String html = "";
				URL url = new URL("http://na.aiononline.com/en/news/");
				HttpURLConnection con = (HttpURLConnection) url.openConnection();
				String line;
				con.connect();
				if (con.getResponseCode() != 200) {
					Thread.sleep(60000);
					continue;
				}
				BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream(), StandardCharsets.UTF_8));
				while ((line = br.readLine()) != null)
					html += line;
				br.close();
				
				LinkedList<AionArticle> newArticles = new LinkedList<AionArticle>();
				try {
					while(html.indexOf("<div class=\"cat-entry\">") != -1) {
						html = html.substring(html.indexOf("<div class=\"cat-entry\">"));
						AionArticle temp = AionArticle.parse(html);
						html = html.substring(html.indexOf("<div class=\"cat-entry\">")+2);
						
						if(!cache.contains(temp.getId())) {
							cache.add(temp.getId(), temp);
							newArticles.add(temp);
							updateAionArtCache(cache);
						}
					}	
				} catch (Exception ex) {
					System.err.println("error parsing");
				}
				while(!newArticles.isEmpty()) 
					channel.sendMessage(newArticles.removeLast().getMessage()).queue();
			} catch (Exception ex) {
				
			}
			
			try {
				Thread.sleep(60000);
			} catch (Exception ex) {
				
			}
		}
	}
	
	public static void loopPosts(TextChannel channel) {
		AionCache<AionPost> cache = loadAionPostCache();

		while (true) {
			try {				
				URL url = new URL("https://forums.aiononline.com/discover/11.xml");
				HttpURLConnection con = (HttpURLConnection) url.openConnection();
				con.connect();
				if (con.getResponseCode() != 200) {
					Thread.sleep(60000);
					continue;
				}
				
				DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
		        DocumentBuilder builder = domFactory.newDocumentBuilder();
		        Document dDoc = builder.parse(con.getInputStream());
		        NodeList results = dDoc.getFirstChild().getFirstChild().getChildNodes();
		        
		        LinkedList<AionPost> newPosts = new LinkedList<AionPost>();
		        for(int i = 4; i < results.getLength(); i++) {
		        	NodeList res = results.item(i).getChildNodes();
		        	String title = res.item(0).getTextContent();
		        	String link = res.item(1).getTextContent();
		        	String content = res.item(2).getTextContent();
		        	String time = res.item(3).getTextContent();
		        	Long id = Long.parseLong(link.substring(link.lastIndexOf('=')+1).trim());
		        	
		        	AionPost post = new AionPost(id, title, link, content, time);
		        	if(!cache.contains(post.getId())) {
						cache.add(post.getId(), post);
						newPosts.add(post);
						updateAionPostCache(cache);
					}
		        }
		       
				while(!newPosts.isEmpty()) 
					channel.sendMessage(newPosts.removeLast().getMessage()).queue();
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

	@SuppressWarnings("unchecked")
	private static AionCache<AionPost> loadAionPostCache() {
		AionCache<AionPost> a;

		try {
			FileInputStream fis = new FileInputStream("aionpostcache.dat");
			ObjectInputStream ois = new ObjectInputStream(fis);
			a = (AionCache<AionPost>) ois.readObject();
			ois.close();
			fis.close();
		} catch (Exception e) {
			e.printStackTrace();
			return new AionCache<AionPost>();
		}
		
		return a;
	}
	
	private static boolean updateAionPostCache(AionCache<AionPost> cache) {
		try {
			new File("aionpostcache.dat").delete();
			FileOutputStream fos = new FileOutputStream("aionpostcache.dat");
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
