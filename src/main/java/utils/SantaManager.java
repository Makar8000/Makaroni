package utils;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class SantaManager implements Serializable {
	private static final long serialVersionUID = 6144114037574556203L;
	private Map<String, Santa> santas;
	private Map<String, String> selectedPairs;
	private boolean gameStarted;

	public SantaManager() {
		santas = new HashMap<String, Santa>();
		selectedPairs = new HashMap<String, String>();
		gameStarted = false;
	}

	public boolean add(Santa santa) {
		boolean ret = !santas.containsKey(santa.getDiscordID());
		santas.put(santa.getDiscordID(), santa);
		updateSantas();
		return ret;
	}

	public boolean remove(String discordID) {
		boolean ret = santas.containsKey(discordID);
		santas.remove(discordID);
		updateSantas();
		return ret;
	}
	
	public int size() {
		return santas.size();
	}
	
	public void setReceiver(String santa, String receiver) {
		selectedPairs.put(santa, receiver);
		updateSantas();
	}
	
	public String getReceiver(String santa) {
		if(!gameStarted)
			return null;
		
		return selectedPairs.get(santa);
	}
	
	public String getSanta(String receiver) {
		if(!gameStarted)
			return null;

		for (Map.Entry<String, String> entry : selectedPairs.entrySet()) {
		    String s = entry.getKey();
		    String r = entry.getValue();
		    if(r.equals(receiver))
		    	return s;
		}
		
		return null;
	}
	
	public boolean started() {
		return this.gameStarted;
	}
	
	public void start() {
		this.gameStarted = true;
		updateSantas();
	}
	
	public void reset() {
		this.santas = new HashMap<String, Santa>();
		this.selectedPairs = new HashMap<String, String>();
		this.gameStarted = false;
		updateSantas();
	}

	public ArrayList<Santa> getAll() {
		ArrayList<Santa> ret = new ArrayList<Santa>(santas.values());
		do
			Collections.shuffle(ret);
		while(!checkExclusions(ret));

		return ret;
	}
	
	public String toString() {
		StringBuilder str = new StringBuilder();
		
		santas.forEach((k, s) -> {
			str.append(s.toString());
			str.append("\n\n");
		});
		
		return str.toString();
	}

	public static boolean checkExclusions(ArrayList<Santa> s) {
		for (int i = 0; i < s.size(); i++) {
			int j = i == s.size() - 1 ? 0 : i + 1;
			if(DiscordID.EXCLUSIONS.get(s.get(i).getDiscordID()).contains(s.get(j).getDiscordID()))
				return false;
		}
		return true;
	}
	
	public static SantaManager loadSantas() {
		SantaManager s;

		try {
			FileInputStream fis = new FileInputStream("santas.dat");
			ObjectInputStream ois = new ObjectInputStream(fis);
			s = (SantaManager) ois.readObject();
			ois.close();
			fis.close();
		} catch (Exception e) {
			e.printStackTrace();
			return new SantaManager();
		}
		
		return s;
	}
	
	private boolean updateSantas() {
		try {
			new File("santas.dat").delete();
			FileOutputStream fos = new FileOutputStream("santas.dat");
			ObjectOutputStream oos = new ObjectOutputStream(fos);
			oos.writeObject(this);
			oos.close();
			fos.close();
			return true;
		} catch (IOException e) {
			e.printStackTrace();
		}

		return false;
	}
}
