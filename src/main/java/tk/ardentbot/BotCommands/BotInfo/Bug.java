package tk.ardentbot.BotCommands.BotInfo;

import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.entities.Invite;
import net.dv8tion.jda.core.exceptions.PermissionException;
import tk.ardentbot.Core.CommandExecution.Command;
import tk.ardentbot.Core.Exceptions.BotException;
import tk.ardentbot.Core.Translation.Language;

import java.util.concurrent.TimeUnit;

import static tk.ardentbot.Main.Ardent.ardent;

public class Bug extends Command {
    public Bug(CommandSettings commandSettings) {
        super(commandSettings);
    }

    @Override
    public void noArgs(Guild guild, MessageChannel channel, User user, Message message, String[] args, Language
            language) throws Exception {
        TextChannel tch = (TextChannel) channel;
        final boolean[] delete = {false};
        tch.getInvites().queue(invites -> {
            for (Invite invite : invites) {
                if (invite.getInviter().equals(ardent.bot)) {
                    try {
                        sendRetrievedTranslation(channel, "bug", language, "disabled");
                    }
                    catch (Exception e) {
                        new BotException(e);
                    }
                }
            }
        });
        if (!delete[0]) {
            try {
                tch.createInvite().setUnique(true).setTemporary(true).setMaxAge((long) 1, TimeUnit.DAYS).queue
                        (invite -> {
                            ardent.jda.getTextChannelById("267404524484820994").sendMessage("**Request for help by "
                                    + user.getName() + "**\n" + invite.getCode()).queue(successMsg -> {
                                try {
                                    sendRetrievedTranslation(channel, "bug", language, "success");
                                }
                                catch (Exception e) {
                                    new BotException(e);
                                }
                            });
                        });
            }
            catch (PermissionException ex) {
                try {
                    sendRetrievedTranslation(channel, "bug", language, "nopermission");
                }
                catch (Exception e) {
                    new BotException(e);
                }
            }
        }
    }

    @Override
    public void setupSubcommands() throws Exception {
    }
}
