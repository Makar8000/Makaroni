package audio;
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.VoiceChannel;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import net.dv8tion.jda.core.managers.AudioManager;

import java.util.HashMap;
import java.util.Map;

public class AudioPlayerListener extends ListenerAdapter {
	private final AudioPlayerManager playerManager;
	private final Map<Long, GuildMusicManager> musicManagers;

	public AudioPlayerListener() {
		this.musicManagers = new HashMap<>();
		this.playerManager = new DefaultAudioPlayerManager();
		AudioSourceManagers.registerRemoteSources(playerManager);
		AudioSourceManagers.registerLocalSource(playerManager);
	}

	private synchronized GuildMusicManager getGuildAudioPlayer(Guild guild) {
		long guildId = Long.parseLong(guild.getId());
		GuildMusicManager musicManager = musicManagers.get(guildId);

		if (musicManager == null) {
			musicManager = new GuildMusicManager(playerManager);
			musicManager.player.setVolume(15);
			musicManagers.put(guildId, musicManager);
		}

		guild.getAudioManager().setSendingHandler(musicManager.getSendHandler());
		return musicManager;
	}

	@Override
	public void onGuildMessageReceived(GuildMessageReceivedEvent event) {
		String[] command = event.getMessage().getContent().split(" ", 2);
		Guild guild = event.getGuild();

		if (guild != null) {
			GuildMusicManager musicManager = getGuildAudioPlayer(guild);
			if ("!play".equals(command[0]) && command.length == 2) {
				loadAndPlay(event.getChannel(), command[1], event.getMember().getUser().getId());
			} else if ("!play".equals(command[0])) {
				if (musicManager.player.isPaused())
					musicManager.player.setPaused(false);
			} else if ("!skip".equals(command[0])) {
				skipTrack(event.getChannel());
			} else if ("!stop".equals(command[0])) {
				musicManager.player.stopTrack();
			} else if ("!pause".equals(command[0])) {
				musicManager.player.setPaused(true);
			} else if ("!volume".equals(command[0]) && command.length == 2) {
				int volume = 0;
				try {
					volume = Integer.parseInt(command[1]);
					musicManager.player.setVolume(volume);
				} catch (NumberFormatException ex) {
				}
			} else if ("!nowplaying".equals(command[0])) {
				AudioTrack track = musicManager.player.getPlayingTrack();
				if(track != null) {
					event.getChannel().sendMessage(track.getInfo().title);
				}
			} else if ("!reset".equals(command[0]) || "!leave".equals(command[0])) {
				musicManager.player.destroy();
				musicManagers.remove(Long.parseLong(guild.getId()));
				guild.getAudioManager().closeAudioConnection();
			}
		}
	}

	private void loadAndPlay(final TextChannel channel, final String trackUrl, String uid) {
		GuildMusicManager musicManager = getGuildAudioPlayer(channel.getGuild());

		playerManager.loadItemOrdered(musicManager, trackUrl, new AudioLoadResultHandler() {
			@Override
			public void trackLoaded(AudioTrack track) {
				channel.sendMessage("Adding to queue:\n" + track.getInfo().title).queue();

				play(channel.getGuild(), musicManager, track, uid);
			}

			@Override
			public void playlistLoaded(AudioPlaylist playlist) {
				AudioTrack firstTrack = playlist.getSelectedTrack();

				if (firstTrack == null) {
					firstTrack = playlist.getTracks().get(0);
				}

				channel.sendMessage("Adding to queue:\n" + firstTrack.getInfo().title + " (first track of playlist "
						+ playlist.getName() + ")").queue();

				play(channel.getGuild(), musicManager, firstTrack, uid);
			}

			@Override
			public void noMatches() {
				channel.sendMessage("Nothing found from `" + trackUrl + "` D:").queue();
			}

			@Override
			public void loadFailed(FriendlyException exception) {
				channel.sendMessage("Could not play: " + exception.getMessage()).queue();
			}
		});
	}

	private void play(Guild guild, GuildMusicManager musicManager, AudioTrack track, String uid) {
		if (connectToVoiceChannel(guild.getAudioManager(), uid))
			musicManager.scheduler.queue(track);
	}

	private void skipTrack(TextChannel channel) {
		GuildMusicManager musicManager = getGuildAudioPlayer(channel.getGuild());
		musicManager.scheduler.nextTrack();
		channel.sendMessage("Skipped to next track.").queue();
	}

	private static boolean connectToVoiceChannel(AudioManager audioManager, String uid) {
		if (!audioManager.isConnected() && !audioManager.isAttemptingToConnect()) {
			for (VoiceChannel voiceChannel : audioManager.getGuild().getVoiceChannels())
				for (Member u : voiceChannel.getMembers())
					if (u.getUser().getId().equals(uid)) {
						audioManager.openAudioConnection(voiceChannel);
						return true;
					}
			return false;
		}
		return true;
	}
}