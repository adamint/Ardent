package tk.ardentbot.commands.music;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import lombok.Getter;
import lombok.Setter;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import tk.ardentbot.core.executor.BaseCommand;
import tk.ardentbot.rethink.models.MusicSettingsModel;
import tk.ardentbot.utils.discord.GuildUtils;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import static tk.ardentbot.rethink.Database.connection;
import static tk.ardentbot.rethink.Database.r;

public class ArdentMusicManager {
    private AudioPlayer player;
    private Instant lastPlayedAt;
    private BlockingQueue<ArdentTrack> queue = new LinkedBlockingQueue<>();
    private String channel;
    private ArdentTrack currentlyPlaying;
    @Getter
    @Setter
    private String lastAnnouncementId = null;
    @Getter
    private boolean shouldAnnounce;

    public ArdentMusicManager(AudioPlayer player, MessageChannel channel) {
        this.player = player;
        if (channel != null) {
            this.channel = channel.getId();
            MusicSettingsModel guildMusicSettings = BaseCommand.asPojo(r.db("data").table("music_settings")
                    .get(((TextChannel) channel).getGuild().getId()).run(connection), MusicSettingsModel.class);
            shouldAnnounce = !(guildMusicSettings == null || !guildMusicSettings.announce_music);
        }
        else shouldAnnounce = false;
    }

    public TextChannel getChannel() {
        if (channel == null) return null;
        return GuildUtils.getTextChannelById(channel);
    }

    public void setChannel(MessageChannel channel) {
        assert channel != null;
        this.channel = channel.getId();
        MusicSettingsModel guildMusicSettings = BaseCommand.asPojo(r.db("data").table("music_settings")
                .get(((TextChannel) channel).getGuild().getId()).run(connection), MusicSettingsModel.class);
        shouldAnnounce = !(guildMusicSettings == null || !guildMusicSettings.announce_music);
    }

    public boolean isTrackCurrentlyPlaying() {
        return currentlyPlaying != null;
    }

    public ArdentTrack getCurrentlyPlaying() {
        return currentlyPlaying;
    }

    public void setCurrentlyPlaying(ArdentTrack currentlyPlaying) {
        this.currentlyPlaying = currentlyPlaying;
    }

    public void queue(ArdentTrack track) {
        if (!player.startTrack(track.getTrack(), true)) {
            queue.offer(track);
        }
        else currentlyPlaying = track;
    }

    public void nextTrack() {
        ArdentTrack track = queue.poll();
        if (track != null) {
            player.startTrack(track.getTrack(), false);
            currentlyPlaying = track;
        }
        else {
            player.startTrack(null, false);
            currentlyPlaying = null;
        }
    }

    void addToQueue(ArdentTrack track) {
        queue(track);
        lastPlayedAt = Instant.now();
    }

    public Instant getLastPlayedAt() {
        return lastPlayedAt;
    }

    public void resetQueue() {
        this.queue = new LinkedBlockingQueue<>();
    }

    void shuffle() {
        ArrayList<ArdentTrack> tracks = new ArrayList<>();
        tracks.addAll(queue);
        Collections.shuffle(tracks);
        queue = new LinkedBlockingQueue<>(tracks);
    }

    void removeFrom(User user) {
        queue.removeIf(currentTrack -> currentTrack.getAuthor().equalsIgnoreCase(user.getId()));
    }

    public BlockingQueue<ArdentTrack> getQueue() {
        return queue;
    }

    List<ArdentTrack> getQueueAsList() {
        ArrayList<ArdentTrack> tracks = new ArrayList<>();
        tracks.addAll(queue);
        return tracks;
    }
}
