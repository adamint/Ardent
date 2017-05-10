package tk.ardentbot.commands.music;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.managers.AudioManager;
import tk.ardentbot.core.executor.Command;
import tk.ardentbot.utils.discord.UserUtils;

import static tk.ardentbot.commands.music.Music.getGuildAudioPlayer;
import static tk.ardentbot.commands.music.Music.sendTo;

public class Restart extends Command {
    public Restart(CommandSettings commandSettings) {
        super(commandSettings);
    }

    @Override
    public void noArgs(Guild guild, MessageChannel channel, User user, Message message, String[] args) throws Exception {
        GuildMusicManager musicManager = getGuildAudioPlayer(guild, channel);
        ArdentMusicManager player = musicManager.scheduler.manager;
        ArdentTrack current = player.getCurrentlyPlaying();
        if (current != null) {
            AudioManager audioManager = guild.getAudioManager();
            if (UserUtils.hasManageServerOrStaff(guild.getMember(user)) || user.getId().equalsIgnoreCase
                    (current.getAuthor()) || UserUtils.isBotCommander(guild.getMember(user)) || (audioManager.isConnected() &&
                    audioManager.getConnectedChannel().getMembers().size() == 2))
            {
                AudioTrack track = current.getTrack();
                track.setPosition(0);
                sendTranslatedMessage("Restarted the current track", channel, user);
            }
            else {
                sendTranslatedMessage("You need to have queued the song or have the Manage Server permission", sendTo
                        (channel, guild), user);
            }
        }
        else sendTranslatedMessage("I'm not playing anything right now!", channel, user);
    }

    @Override
    public void setupSubcommands() throws Exception {

    }
}
