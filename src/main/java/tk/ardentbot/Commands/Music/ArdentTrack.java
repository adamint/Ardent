package tk.ardentbot.Commands.Music;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.core.entities.TextChannel;

public class ArdentTrack {
    private String authorId;
    private TextChannel addedFrom;
    private AudioTrack track;

    public ArdentTrack(String authorId, TextChannel addedFrom, AudioTrack track) {
        this.authorId = authorId;
        this.addedFrom = addedFrom;
        this.track = track;
    }

    public String getAuthor() {
        return authorId;
    }

    public TextChannel getAddedFrom() {
        return addedFrom;
    }

    public AudioTrack getTrack() {
        return track;
    }
}
