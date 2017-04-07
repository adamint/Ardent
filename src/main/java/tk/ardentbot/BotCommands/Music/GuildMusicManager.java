package tk.ardentbot.BotCommands.Music;

import com.sedmelluq.discord.lavaplayer.player.AudioConfiguration;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.source.soundcloud.SoundCloudAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioSourceManager;
import net.dv8tion.jda.core.entities.MessageChannel;
import tk.ardentbot.Main.Ardent;

public class GuildMusicManager {
    /**
     * Creates a player and a track scheduler.
     *
     * @param manager Audio player manager to use for creating the player.
     */

    static AudioPlayerManager fallbackManager = null;
    /**
     * Track scheduler for the player.
     */
    public final TrackScheduler scheduler;
    /**
     * Audio player for the guild.
     */
    final AudioPlayer player;

    public GuildMusicManager(AudioPlayerManager manager, MessageChannel channel) {
        if (manager == null) {
            fallbackManager = new DefaultAudioPlayerManager();
            fallbackManager.useRemoteNodes(Ardent.node1Url + ":8080");

            fallbackManager.getConfiguration().setResamplingQuality(AudioConfiguration.ResamplingQuality.LOW);
            fallbackManager.registerSourceManager(new YoutubeAudioSourceManager());
            fallbackManager.registerSourceManager(new SoundCloudAudioSourceManager());

            AudioSourceManagers.registerRemoteSources(fallbackManager);
            AudioSourceManagers.registerLocalSource(fallbackManager);
            player = fallbackManager.createPlayer();
        }
        else player = manager.createPlayer();
        scheduler = new TrackScheduler(player, channel);
        player.addListener(scheduler);
    }

    /**
     * @return Wrapper around AudioPlayer to use it as an AudioSendHandler.
     */
    AudioPlayerSendHandler getSendHandler() {
        return new AudioPlayerSendHandler(player);
    }
}
