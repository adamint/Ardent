package tk.ardentbot.BotCommands.Music;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class ArdentMusicManager {
    private AudioPlayer player;
    private Instant lastPlayedAt;
    private BlockingQueue<ArdentTrack> queue = new LinkedBlockingQueue<>();
    private MessageChannel channel;
    private ArdentTrack currentlyPlaying;
    public ArdentMusicManager(AudioPlayer player, MessageChannel channel) {
        this.player = player;
        this.channel = channel;
    }

    public TextChannel getChannel() {
        return (TextChannel) channel;
    }

    public void setChannel(MessageChannel channel) {
        this.channel = channel;
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

    void nextTrack() {
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
