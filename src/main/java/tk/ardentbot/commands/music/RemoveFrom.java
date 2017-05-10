package tk.ardentbot.commands.music;

import net.dv8tion.jda.core.entities.*;
import tk.ardentbot.core.executor.Command;
import tk.ardentbot.utils.discord.UserUtils;

import java.util.List;

import static tk.ardentbot.commands.music.Music.getGuildAudioPlayer;

public class RemoveFrom extends Command {
    public RemoveFrom(CommandSettings commandSettings) {
        super(commandSettings);
    }

    @Override
    public void noArgs(Guild guild, MessageChannel channel, User user, Message message, String[] args) throws Exception {
        Member member = guild.getMember(user);
        if (UserUtils.hasManageServerOrStaff(member)) {
            List<User> mentionedUsers = message.getMentionedUsers();
            if (mentionedUsers.size() == 1) {
                User deleteFrom = mentionedUsers.get(0);
                getGuildAudioPlayer(guild, channel).scheduler.manager.removeFrom(deleteFrom);
                sendTranslatedMessage("I removed all tracks from {0}".replace("{0}", deleteFrom.getName()), channel, user);
            }
            else sendTranslatedMessage("You need to mention a user", channel, user);
        }
        else sendTranslatedMessage("You need the Manage Server permission to do this", channel, user);
    }

    @Override
    public void setupSubcommands() throws Exception {

    }
}
