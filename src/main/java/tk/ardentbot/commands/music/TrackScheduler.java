package tk.ardentbot.commands.music;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.MessageChannel;
import tk.ardentbot.core.misc.logging.BotException;
import tk.ardentbot.utils.discord.GuildUtils;

import static tk.ardentbot.utils.discord.GuildUtils.getShard;

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
            getShard(guild).help.sendRetrievedTranslation(manager.getChannel(), "music", GuildUtils.getLanguage(guild),
                    "notabletoplaytrack", null);
            new BotException("Exception playing " + track.getInfo().uri + " in " + manager.getChannel().getName() + "" +
                    " (" + guild.getId() + ")");
            if (exception instanceof FriendlyException) {
                FriendlyException exception1 = (FriendlyException) exception;
                new BotException("^ " + exception1.getCause());
            }
        }
        catch (Exception ex) {
            new BotException(ex);
        }
    }
}