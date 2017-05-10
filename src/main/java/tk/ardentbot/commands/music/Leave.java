package tk.ardentbot.commands.music;

import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.managers.AudioManager;
import tk.ardentbot.core.executor.Command;
import tk.ardentbot.utils.discord.UserUtils;

public class Leave extends Command {
    public Leave(CommandSettings commandSettings) {
        super(commandSettings);
    }

    @Override
    public void noArgs(Guild guild, MessageChannel channel, User user, Message message, String[] args) throws Exception {
        AudioManager audioManager = guild.getAudioManager();
        Member member = guild.getMember(user);
        if (audioManager.isConnected()) {
            if (UserUtils.hasManageServerOrStaff(member) || UserUtils.isBotCommander(member) || audioManager.getConnectedChannel()
                    .getMembers().size() == 2) {
                String name = audioManager.getConnectedChannel().getName();
                audioManager.closeAudioConnection();
                sendTranslatedMessage("Disconnected from {0}".replace("{0}", name), channel, user);
            }
            else sendTranslatedMessage("You need the Manage Server permission to do this", channel, user);
        }
        else sendTranslatedMessage("I'm not in a voice channel!", channel, user);
    }

    @Override
    public void setupSubcommands() throws Exception {
    }
}
