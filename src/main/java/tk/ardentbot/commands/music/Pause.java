package tk.ardentbot.commands.music;

import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.managers.AudioManager;
import tk.ardentbot.core.executor.Command;
import tk.ardentbot.utils.discord.UserUtils;

import static tk.ardentbot.commands.music.Music.getGuildAudioPlayer;

public class Pause extends Command {
    public Pause(CommandSettings commandSettings) {
        super(commandSettings);
    }

    @Override
    public void noArgs(Guild guild, MessageChannel channel, User user, Message message, String[] args) throws Exception {
        AudioManager audioManager = guild.getAudioManager();
        Member member = guild.getMember(user);
        if (UserUtils.hasManageServerOrStaff(member) || UserUtils.isBotCommander(member) || (audioManager.isConnected() && audioManager
                .getConnectedChannel().getMembers().size() == 2)) {
            if (audioManager.isConnected()) {
                GuildMusicManager manager = getGuildAudioPlayer(guild, channel);
                if (!manager.player.isPaused()) {
                    sendTranslatedMessage("Paused music playback", channel, user);
                    manager.player.setPaused(true);
                }
                else {
                    sendTranslatedMessage("Can't pause an already-paused player!", channel, user);
                }
            }
            else sendTranslatedMessage("I'm not in a voice channel!", channel, user);
        }
        else sendTranslatedMessage("You need the Manage Server permission to do this", channel, user);
    }

    @Override
    public void setupSubcommands() throws Exception {

    }
}
