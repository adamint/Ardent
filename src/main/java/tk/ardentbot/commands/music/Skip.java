package tk.ardentbot.commands.music;

import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.managers.AudioManager;
import tk.ardentbot.core.executor.Command;
import tk.ardentbot.utils.discord.UserUtils;

import static tk.ardentbot.commands.music.Music.getGuildAudioPlayer;
import static tk.ardentbot.commands.music.Music.sendTo;

public class Skip extends Command {
    public Skip(CommandSettings commandSettings) {
        super(commandSettings);
    }

    @Override
    public void noArgs(Guild guild, MessageChannel channel, User user, Message message, String[] args) throws Exception {
        AudioManager audioManager = guild.getAudioManager();
        Member member = guild.getMember(user);
        if (audioManager.isConnected()) {
            GuildMusicManager manager = getGuildAudioPlayer(guild, channel);
            ArdentMusicManager ardentMusicManager = manager.scheduler.manager;
            ArdentTrack track = ardentMusicManager.getCurrentlyPlaying();
            if (track != null) {
                String ownerId = track.getAuthor();
                if (ownerId == null) ownerId = "";
                if (UserUtils.hasManageServerOrStaff(member) || UserUtils.isBotCommander(member) || user.getId().equalsIgnoreCase
                        (ownerId))
                {
                    ardentMusicManager.nextTrack();
                    sendTranslatedMessage("Skipped the playing song", sendTo(channel, guild), user);
                }
                else {
                    sendTranslatedMessage("You need to have queued the song or have the Manage Server permission", sendTo
                            (channel, guild), user);
                }
            }
        }
        else sendTranslatedMessage("I'm not in a voice channel!", channel, user);
    }

    @Override
    public void setupSubcommands() {

    }
}
