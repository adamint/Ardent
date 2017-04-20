package tk.ardentbot.commands.administration;

import net.dv8tion.jda.core.entities.*;
import tk.ardentbot.Core.executor.Command;
import tk.ardentbot.Core.misc.logging.BotException;
import tk.ardentbot.Core.translate.Language;
import tk.ardentbot.utils.discord.GuildUtils;

import java.util.ArrayList;
import java.util.List;

public class Clear extends Command {
    public Clear(CommandSettings commandSettings) {
        super(commandSettings);
    }

    @Override
    public void noArgs(Guild guild, MessageChannel channel, User user, Message message, String[] args, Language
            language) throws Exception {
        if (args.length == 1) {
            sendRetrievedTranslation(channel, "clear", language, "help", user);
        }
        else {
            if (GuildUtils.hasManageServerPermission(guild.getMember(user))) {
                if (args.length == 2) {
                    try {
                        int num = Integer.parseInt(args[1]);
                        if (num > 1 && num <= 100) {
                            TextChannel textChannel = (TextChannel) channel;
                            textChannel.getHistory().retrievePast(num).queue(messages -> {
                                textChannel.deleteMessages(messages).queue(aVoid -> {
                                    try {
                                        sendTranslatedMessage(getTranslation("clear", language, "deletemessages")
                                                .getTranslation().replace("{0}", num + ""), channel, user);
                                    }
                                    catch (Exception e) {
                                        try {
                                            sendTranslatedMessage(getTranslation("clear", language, "2weeksorexists")
                                                    .getTranslation(), channel, user);
                                            new BotException(e);
                                        }
                                        catch (Exception e1) {
                                            new BotException(e1);
                                        }
                                    }
                                });
                            });

                        }
                        else sendRetrievedTranslation(channel, "clear", language, "incorrectnumber", user);
                    }
                    catch (NumberFormatException ex) {
                        sendRetrievedTranslation(channel, "prune", language, "notanumber", user);
                    }
                    catch (Exception ex) {
                        sendRetrievedTranslation(channel, "other", language, "needproperpermissions", user);
                    }
                }
                else {
                    List<User> mentionedUsers = message.getMentionedUsers();
                    if (mentionedUsers.size() == 1) {
                        User mentioned = mentionedUsers.get(0);
                        try {
                            int num = Integer.parseInt(replace(message.getRawContent(), 2));
                            if (num > 1 && num < 100) {
                                TextChannel textChannel = (TextChannel) channel;
                                ArrayList<Message> messagesToDelete = new ArrayList<>();
                                textChannel.getHistory().retrievePast(100).queue(messages -> {
                                    for (Message m : messages) {
                                        if (m.getAuthor().getId().equals(mentioned.getId()) && messagesToDelete.size
                                                () < num)
                                            messagesToDelete.add(m);
                                    }
                                    textChannel.deleteMessages(messagesToDelete).queue(aVoid -> {
                                        try {
                                            sendTranslatedMessage(getTranslation("clear", language, "deleteduser")
                                                    .getTranslation().replace("{0}", messagesToDelete.size() + "")
                                                    .replace("{1}", 100 + ""), channel, user);
                                        }
                                        catch (Exception e) {
                                            new BotException(e);
                                        }
                                    });
                                });
                            }
                            else sendRetrievedTranslation(channel, "clear", language, "incorrectnumber", user);
                        }
                        catch (NumberFormatException ex) {
                            sendRetrievedTranslation(channel, "prune", language, "notanumber", user);
                        }

                        catch (Exception ex) {
                            sendRetrievedTranslation(channel, "other", language, "needproperpermissions", user);
                        }
                    }
                    else sendRetrievedTranslation(channel, "other", language, "mentionuser", user);
                }
            }
            else sendRetrievedTranslation(channel, "other", language, "needmessagemanage", user);
        }
    }

    @Override
    public void setupSubcommands() {
    }
}
