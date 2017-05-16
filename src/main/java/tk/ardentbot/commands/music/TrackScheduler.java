package tk.ardentbot.commands.music;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;
import tk.ardentbot.core.misc.logging.BotException;
import tk.ardentbot.utils.discord.GuildUtils;
import tk.ardentbot.utils.discord.MessageUtils;

public class TrackScheduler extends AudioEventAdapter {
    private final AudioPlayer player;
    public ArdentMusicManager manager;

    TrackScheduler(AudioPlayer player, MessageChannel channel) {
        this.player = player;
        this.manager = new ArdentMusicManager(player, channel);
    }

    @Override
    public void onTrackStart(AudioPlayer player, AudioTrack track) {
        if (manager.isShouldAnnounce()) {
            if (manager.getLastAnnouncementId() != null) {
                try {
                    manager.getChannel().getMessageById(manager.getLastAnnouncementId()).queue(message -> message.delete().queue());
                }
                catch (Exception ignored) {
                }
            }
            User me = manager.getChannel().getGuild().getSelfMember().getUser();
            AudioTrackInfo info = track.getInfo();
            EmbedBuilder builder = MessageUtils.getDefaultEmbed(me);
            builder.setAuthor("Now playing " + info.title, "https://ardentbot.tk", "https://s-media-cache-ak0.pinimg" +
                    ".com/736x/69/96/5c/69965c2849ec9b7148a5547ce6714735.jpg");
            builder.setThumbnail("https://s-media-cache-ak0.pinimg.com/736x/69/96/5c/69965c2849ec9b7148a5547ce6714735.jpg");
            builder.addField("Title", info.title, true)
                    .addField("Author", info.author, true)
                    .addField("Duration", Music.getDuration(track), true)
                    .addField("URL", info.uri, true)
                    .addField("Is Stream", String.valueOf(info.isStream), true);
            GuildUtils.getShard(manager.jda).help.sendEmbed(builder, manager.getChannel(), me);
        }
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