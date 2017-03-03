package tk.ardentbot.Commands.Music;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import net.dv8tion.jda.core.entities.User;
import tk.ardentbot.Utils.Tuples.Pair;

import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class TrackScheduler extends AudioEventAdapter {
    private final AudioPlayer player;
    public String ownerOfNowPlaying;
    private BlockingQueue<Pair<String, AudioTrack>> queue;

    /**
     * @param player The audio player this scheduler uses
     */
    public TrackScheduler(AudioPlayer player) {
        this.player = player;
        this.queue = new LinkedBlockingQueue<>();
    }

    /**
     * Add the next track to queue or play right away if nothing is in the queue.
     *
     * @param track The track to play or add to queue.
     */
    public void queue(User user, AudioTrack track) {
        // Calling startTrack with the noInterrupt set to true will start the track only if nothing is currently playing. If
        // something is playing, it returns false and does nothing. In that case the player was already playing so this
        // track goes to the queue instead.
        if (!player.startTrack(track, true)) {
            queue.offer(new Pair<>(user.getId(), track));
        }
        else ownerOfNowPlaying = user.getId();
    }

    /**
     * Start the next track, stopping the current one if it is playing.
     */
    public void nextTrack() {
        // Start the next track, regardless of if something is already playing or not. In case queue was empty, we are
        // giving null to startTrack, which is a valid argument and will simply stop the player.
        Pair<String, AudioTrack> pair = queue.poll();
        if (pair != null) {
            player.startTrack(pair.getV(), false);
            ownerOfNowPlaying = pair.getK();
        }
        else {
            player.startTrack(null, false);
            ownerOfNowPlaying = null;
        }
    }

    @Override
    public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
        // Only start the next track if the end reason is suitable for it (FINISHED or LOAD_FAILED)
        if (endReason.mayStartNext) {
            nextTrack();
        }
    }

    public BlockingQueue<Pair<String, AudioTrack>> getQueue() {
        return queue;
    }

    public void resetQueue() {
        this.queue = new LinkedBlockingQueue<>();
    }

    public void shuffle() {
        ArrayList<Pair<String, AudioTrack>> tracks = new ArrayList<>();
        queue.forEach((tracks::add));
        Collections.shuffle(tracks);
        queue = new LinkedBlockingQueue<>(tracks);
    }
}