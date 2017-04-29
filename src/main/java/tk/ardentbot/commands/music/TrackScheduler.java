package tk.ardentbot.commands.music;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.MessageChannel;
import tk.ardentbot.core.misc.logging.BotException;

public class TrackScheduler extends AudioEventAdapter {
    private final AudioPlayer player;
    public ArdentMusicManager manager;

    TrackScheduler(AudioPlayer player, MessageChannel channel) {
        this.player = player;
        this.manager = new ArdentMusicManager(player, channel);
    }

    @Override
    public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
        if (endReason.mayStartNext) {
            manager.nextTrack();
        }
    }

    @Override
    public void onTrackStuck(AudioPlayer player, AudioTrack track, long thresholdMs) {
        manager.nextTrack();
        onException(player, track, thresholdMs);
    }

    @Override
    public void onTrackException(AudioPlayer player, AudioTrack track, FriendlyException exception) {
        onException(player, track, exception);
    }


    private void onException(AudioPlayer player, AudioTrack track, Object exception) {
        manager.setCurrentlyPlaying(null);
        manager.nextTrack();
        try {
            Guild guild = manager.getChannel().getGuild();
            manager.getChannel().sendMessage("I wasn't able to play that track, skipping...").queue();
        }
        catch (Exception ex) {
            new BotException(ex);
        }
    }
}