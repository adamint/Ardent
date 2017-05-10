package tk.ardentbot.commands.music;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;
import tk.ardentbot.core.executor.Command;
import tk.ardentbot.utils.discord.UserUtils;

import static tk.ardentbot.commands.music.Music.*;

public class Playing extends Command {
    public Playing(CommandSettings commandSettings) {
        super(commandSettings);
    }

    @Override
    public void noArgs(Guild guild, MessageChannel channel, User user, Message message, String[] args) throws Exception {
        GuildMusicManager manager = getGuildAudioPlayer(guild, channel);
        ArdentMusicManager ardentMusicManager = manager.scheduler.manager;
        ArdentTrack nowPlaying = ardentMusicManager.getCurrentlyPlaying();
        if (nowPlaying != null) {
            AudioTrack track = nowPlaying.getTrack();
            AudioTrackInfo info = track.getInfo();
            StringBuilder sb = new StringBuilder();
            String queuedBy = "queued by";
            sb.append(info.title + ": " + info.author + " " + getCurrentTime
                    (track) +
                    "\n     *" + queuedBy + " " + UserUtils.getUserById(nowPlaying.getAuthor()).getName() +
                    "* - [" + nowPlaying.getVotedToSkip().size() + " / " + Math.round(guild.getAudioManager().getConnectedChannel
                    ().getMembers().size() / 2) + "] votes to skip");
            sendTranslatedMessage(sb.toString(), sendTo(channel, guild), user);
        }
        else sendTranslatedMessage("I'm not playing anything right now!", channel, user);
    }

    @Override
    public void setupSubcommands() throws Exception {

    }
}
