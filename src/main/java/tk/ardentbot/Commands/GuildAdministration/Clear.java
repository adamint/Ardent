package tk.ardentbot.Commands.GuildAdministration;

import tk.ardentbot.Backend.Commands.BotCommand;
import tk.ardentbot.Backend.Translation.Language;
import tk.ardentbot.Bot.BotException;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.*;

import java.util.ArrayList;
import java.util.List;

public class Clear extends BotCommand {
    public Clear(CommandSettings commandSettings) {
        super(commandSettings);
    }

    @Override
    public void noArgs(Guild guild, MessageChannel channel, User user, Message message, String[] args, Language language) throws Exception {
        if (args.length == 1) {
            sendRetrievedTranslation(channel, "clear", language, "help");
        }
        else {
            if (guild.getMember(user).hasPermission(Permission.MESSAGE_MANAGE)) {
                if (args.length == 2) {
                    try {
                        int num = Integer.parseInt(args[1]);
                        if (num > 1 && num <= 100) {
                            TextChannel textChannel = (TextChannel) channel;
                            textChannel.getHistory().retrievePast(num).queue(messages -> {
                                textChannel.deleteMessages(messages).queue(aVoid -> {
                                    try {
                                        sendTranslatedMessage(getTranslation("clear", language, "deletemessages").getTranslation().replace("{0}", num + ""), channel);
                                    }
                                    catch (Exception e) {
                                        try {
                                            sendTranslatedMessage(getTranslation("clear", language, "2weeksorexists").getTranslation(), channel);
                                            new BotException(e);
                                        }
                                        catch (Exception e1) {
                                            new BotException(e1);
                                        }
                                    }
                                });
                            });

                        }
                        else sendRetrievedTranslation(channel, "clear", language, "incorrectnumber");
                    }
                    catch (NumberFormatException ex) {
                        sendRetrievedTranslation(channel, "prune", language, "notanumber");
                    }
                }
                else {
                    List<User> mentionedUsers = message.getMentionedUsers();
                    if (mentionedUsers.size() == 1) {
                        User mentioned = mentionedUsers.get(0);
                        try {
                            int num = Integer.parseInt(args[2]);
                            if (num > 1 && num < 100) {
                                TextChannel textChannel = (TextChannel) channel;
                                ArrayList<Message> messagesToDelete = new ArrayList<>();
                                textChannel.getHistory().retrievePast(100).queue(messages -> {
                                    for (Message m : messages) {
                                        if (m.getAuthor().getId().equals(mentioned.getId()) && messagesToDelete.size() < num)
                                            messagesToDelete.add(m);
                                    }
                                    textChannel.deleteMessages(messagesToDelete).queue(aVoid -> {
                                        try {
                                            sendTranslatedMessage(getTranslation("clear", language, "deleteduser").getTranslation().replace("{0}", messagesToDelete.size() + "").replace("{1}", 100 + ""), channel);
                                        }
                                        catch (Exception e) {
                                            new BotException(e);
                                        }
                                    });
                                });
                            }
                            else sendRetrievedTranslation(channel, "clear", language, "incorrectnumber");
                        }
                        catch (NumberFormatException ex) {
                            sendRetrievedTranslation(channel, "prune", language, "notanumber");
                        }
                    }
                    else sendRetrievedTranslation(channel, "other", language, "mentionuser");
                }
            }
            else sendRetrievedTranslation(channel, "other", language, "needmessagemanage");
        }
    }

    @Override
    public void setupSubcommands() {}
}
