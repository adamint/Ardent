package tk.ardentbot.Commands.BotInfo;

import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.entities.Invite;
import net.dv8tion.jda.core.exceptions.PermissionException;
import tk.ardentbot.Backend.Commands.BotCommand;
import tk.ardentbot.Backend.Translation.Language;
import tk.ardentbot.Bot.BotException;
import tk.ardentbot.Main.Ardent;

import java.util.concurrent.TimeUnit;

import static tk.ardentbot.Main.Ardent.jda;

public class Bug extends BotCommand {
    public Bug(CommandSettings commandSettings) {
        super(commandSettings);
    }

    @Override
    public void noArgs(Guild guild, MessageChannel channel, User user, Message message, String[] args, Language
            language) throws Exception {
        TextChannel tch = (TextChannel) channel;
        tch.getInvites().queue(invites -> {
            boolean delete = false;
            for (Invite invite : invites) {
                if (invite.getInviter().equals(Ardent.ardent)) {
                    delete = true;
                    try {
                        invite.delete().queue(successMsg -> {
                            try {
                                sendRetrievedTranslation(channel, "bug", language, "successfullyremoved");
                            }
                            catch (Exception e) {
                                new BotException(e);
                            }
                        });
                    }
                    catch (PermissionException ex) {
                        try {
                            sendRetrievedTranslation(channel, "bug", language, "cantremove");
                        }
                        catch (Exception e) {
                            new BotException(e);
                        }
                    }
                }
            }
            if (!delete) {
                try {
                    tch.createInvite().setUnique(true).setTemporary(true).setMaxAge((long) 1, TimeUnit.DAYS).queue
                            (invite -> {
                        jda.getTextChannelById("267404524484820994").sendMessage("**Request for help by " + invite
                                .getInviter
                                ().getName() + "\n" +
                                invite.getCode()).queue(successMsg -> {
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
        });
    }

    @Override
    public void setupSubcommands() throws Exception {
    }
}
