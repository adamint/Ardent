package tk.ardentbot.commands.music;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import lombok.Getter;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;

import java.util.ArrayList;

public class ArdentTrack {
    @Getter
    private ArrayList<String> votedToSkip = new ArrayList<>();
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

    TextChannel getAddedFrom() {
        return addedFrom;
    }

    AudioTrack getTrack() {
        return track;
    }

    public boolean addSkipVote(User user) {
        if (votedToSkip.contains(user.getId())) return false;
        votedToSkip.add(user.getId());
        return true;
    }
}
