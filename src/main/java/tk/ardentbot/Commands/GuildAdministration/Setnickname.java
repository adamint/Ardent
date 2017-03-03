package tk.ardentbot.Commands.GuildAdministration;

import tk.ardentbot.Backend.Commands.BotCommand;
import tk.ardentbot.Backend.Translation.Language;
import tk.ardentbot.Bot.BotException;
import tk.ardentbot.Utils.GuildUtils;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;

import java.util.List;

public class Setnickname extends BotCommand {
    public Setnickname(CommandSettings commandSettings) {
        super(commandSettings);
    }

    @Override
    public void noArgs(Guild guild, MessageChannel channel, User user, Message message, String[] args, Language language) throws Exception {
        if (args.length == 1) {
            sendTranslatedMessage(getTranslation("setnickname", language, "help").getTranslation().replace("{0}", GuildUtils.getPrefix(guild) + args[0]), channel);
        }
        else if (args.length == 2) {
            sendTranslatedMessage(getTranslation("setnickname", language, "invalidarguments").getTranslation().replace("{0}", GuildUtils.getPrefix(guild) + args[0]), channel);
        }
        else {
            List<User> mentionedUsers = message.getMentionedUsers();
            if (mentionedUsers.size() == 1) {
                if (guild.getMember(user).hasPermission(Permission.MANAGE_SERVER)) {
                    User mentioned = mentionedUsers.get(0);
                    String newNickname = message.getRawContent().replace("<@" + mentioned.getId() + ">", "").replace(GuildUtils.getPrefix(guild) + args[0], "");
                    while (newNickname.startsWith(" ")) newNickname = newNickname.substring(1);
                    if (newNickname.length() > 32 && newNickname.length() < 2) {
                        sendRetrievedTranslation(channel, "setnickname", language, "between2and32");
                    }
                    else {
                        String finalNewNickname = newNickname;
                        guild.getController().setNickname(guild.getMember(mentioned), newNickname).queue(aVoid -> {
                            try {
                                sendTranslatedMessage(getTranslation("setnickname", language, "success")
                                        .getTranslation().replace("{0}", mentioned.getName()).replace("{1}", finalNewNickname), channel);
                            }
                            catch (Exception e) {
                                new BotException(e);
                            }
                        }, throwable -> {
                            try {
                                sendRetrievedTranslation(channel, "setnickname", language, "failure");
                            }
                            catch (Exception e) {
                                new BotException(e);
                            }
                        });
                    }
                }
                else {
                    sendRetrievedTranslation(channel, "other", language, "needmanageserver");
                }
            }
            else sendRetrievedTranslation(channel, "setnickname", language, "wrongmentionedusers");
        }
    }

    @Override
    public void setupSubcommands() {
    }
}
