package tk.ardentbot.commands.administration;

import net.dv8tion.jda.core.entities.*;
import tk.ardentbot.core.executor.Command;
import tk.ardentbot.core.misc.logging.BotException;
import tk.ardentbot.utils.discord.GuildUtils;

import java.util.ArrayList;
import java.util.List;

public class Clear extends Command {
    public Clear(CommandSettings commandSettings) {
        super(commandSettings);
    }

    @Override
    public void noArgs(Guild guild, MessageChannel channel, User user, Message message, String[] args) throws Exception {
        if (args.length == 1) {
            sendTranslatedMessage("**Clearing Messages**\n" +
                    "/clear 10: removes the last 10 messages\n" +
                    "/clear [2-100] removes the specified amount\n" +
                    "/clear @User [2-100]: removes the specified messages from the user mentioned", channel, user);
        }
        else {
            if (GuildUtils.hasManageServerPermission(guild.getMember(user))) {
                if (args.length == 2) {
                    try {
                        int num = Integer.parseInt(args[1]);
                        if (num > 1 && num <= 100) {
                            TextChannel textChannel = (TextChannel) channel;
                            textChannel.getHistory().retrievePast(num).queue(messages -> {
                                try {
                                    textChannel.deleteMessages(messages).queue(aVoid -> {
                                        sendTranslatedMessage("Successfully deleted " + num + " messages", channel, user);
                                    });
                                }
                                catch (Exception e) {
                                    sendTranslatedMessage("Make sure the requested messages aren't over 2 weeks old and that I have " +
                                            "permission " +
                                            "to delete messages.", channel, user);
                                }
                            });

                        }
                        else sendTranslatedMessage("You need to specify a number between 1 and 100", channel, user);
                    }
                    catch (NumberFormatException ex) {
                        sendTranslatedMessage("That wasn't a number!", channel, user);
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
                                            sendTranslatedMessage("Deleted " + messagesToDelete.size() + " messages from that user",
                                                    channel, user);
                                        }
                                        catch (Exception e) {
                                            new BotException(e);
                                        }
                                    });
                                });
                            }
                            else sendTranslatedMessage("You need to specify a number between 1 and 100", channel, user);
                        }
                        catch (NumberFormatException ex) {
                            sendTranslatedMessage("That's not a number!", channel, user);
                        }
                    }
                    else sendTranslatedMessage("You need to mention a user", channel, user);
                }
            }
            else sendTranslatedMessage("You need the Manage Server permission to use this command", channel, user);
        }
    }

    @Override
    public void setupSubcommands() {
    }
}
