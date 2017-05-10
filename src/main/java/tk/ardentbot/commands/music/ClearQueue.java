package tk.ardentbot.commands.music;

import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.managers.AudioManager;
import tk.ardentbot.core.executor.Command;
import tk.ardentbot.utils.discord.UserUtils;

import static tk.ardentbot.commands.music.Music.getGuildAudioPlayer;

public class ClearQueue extends Command {
    public ClearQueue(CommandSettings commandSettings) {
        super(commandSettings);
    }

    @Override
    public void noArgs(Guild guild, MessageChannel channel, User user, Message message, String[] args) throws Exception {
        AudioManager audioManager = guild.getAudioManager();
        Member member = guild.getMember(user);
        if (UserUtils.hasManageServerOrStaff(member) || (audioManager.isConnected() && audioManager.getConnectedChannel().getMembers()
                .size() == 2)) {
            if (audioManager.isConnected()) {
                GuildMusicManager manager = getGuildAudioPlayer(guild, channel);
                manager.scheduler.manager.resetQueue();
                sendTranslatedMessage("Cleared all songs from the queue", channel, user);
            }
            else sendTranslatedMessage("I'm not in a voice channel!", channel, user);
        }
        else sendTranslatedMessage("You need the Manage Server permission to do this", channel, user);
    }

    @Override
    public void setupSubcommands() throws Exception {

    }
}
