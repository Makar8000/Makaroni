package bean;

import java.io.Serializable;

public class Santa implements Serializable {
	private static final long serialVersionUID = -951670018401801249L;
	
	private String realName;
	private String discordID;
	private String address;
	private String notes;
	
	public String getRealName() {
		return realName;
	}
	public void setRealName(String realName) {
		this.realName = realName;
	}
	public String getDiscordID() {
		return discordID;
	}
	public void setDiscordID(String discordID) {
		this.discordID = discordID;
	}
	public String getAddress() {
		return address;
	}
	public void setAddress(String address) {
		this.address = address;
	}
	public String getNotes() {
		return notes;
	}
	public void setNotes(String notes) {
		this.notes = notes;
	}
	
	public String toString() {
		StringBuilder str = new StringBuilder();
		
		str.append("Name: ");
		str.append(getRealName());
		str.append("\nDiscord ID: ");
		str.append(getDiscordID());
		str.append("\nAddress: ");
		str.append(getAddress());
		str.append("\nNotes: ");
		str.append(getNotes());
		
		return str.toString();
	}
}
