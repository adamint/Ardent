package tk.ardentbot.commands.administration;

import com.rethinkdb.net.Cursor;
import lombok.Getter;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.*;
import tk.ardentbot.core.executor.Command;
import tk.ardentbot.core.executor.Subcommand;
import tk.ardentbot.core.misc.logging.BotException;
import tk.ardentbot.rethink.models.AutomessageModel;
import tk.ardentbot.utils.javaAdditions.Triplet;
import java.util.Scanner;

import java.util.HashMap;
import java.util.List;

import static tk.ardentbot.rethink.Database.connection;
import static tk.ardentbot.rethink.Database.r;


public class Automessage extends Command {
    public Automessage(CommandSettings commandSettings) {
        super(commandSettings);
    }

    private static void check(Guild guild) {
        List<HashMap> selectAutomessage = ((Cursor<HashMap>) r.db("data").table("automessages").filter(row -> row.g("guild_id")
                .eq(guild.getId())).run(connection)).toList();
        if (selectAutomessage.size() == 0) {
            r.db("data").table("automessages").insert(r.json(getStaticGson().toJson(new AutomessageModel(guild.getId(), "000", "000",
                    "000")))
            ).run(connection);
        }
    }

    public static Triplet<String, String, String> getMessagesAndChannel(Guild guild) {
        Triplet<String, String, String> triplet;
        check(guild);
        List<HashMap> getAutomessages = ((Cursor<HashMap>) r.db("data").table("automessages").filter(row -> row.g("guild_id")
                .eq(guild.getId())).run(connection)).toList();
        if (getAutomessages.size() > 0) {
            AutomessageModel automessageModel = asPojo(getAutomessages.get(0), AutomessageModel.class);
            String channel;
            String welcome;
            String goodbye;
            if (automessageModel.getChannel_id().equalsIgnoreCase("000")) channel = null;
            else channel = automessageModel.getChannel_id();
            if (automessageModel.getWelcome().equalsIgnoreCase("000")) welcome = null;
            else welcome = automessageModel.getWelcome();
            if (automessageModel.getGoodbye().equalsIgnoreCase("000")) goodbye = null;
            else goodbye = automessageModel.getGoodbye();
            triplet = new Triplet<>(channel, welcome, goodbye);
        } else {
            triplet = new Triplet<>(null, null, null);
        }
        return triplet;
    }

    public static String getField(int num) {
        String columnName = null;
        if (num == 0) columnName = "channel_id";
        else if (num == 1) columnName = "welcome";
        else if (num == 2) columnName = "goodbye";
        return columnName;
    }

    public static void remove(Guild guild, int num) {
        set(guild, "000", num);
    }

    @SuppressWarnings("Duplicates")
    public static void set(Guild guild, String text, int num) {
        String fieldName = getField(num);
        r.db("data").table("automessages").filter(row -> row.g("guild_id").eq(guild.getId())).update(r.hashMap(fieldName, text)).run
                (connection);
    }

    @Override
    public void noArgs(Guild guild, MessageChannel channel, User user, Message message, String[] args) throws Exception {
        sendHelp(channel, guild, user, this);
    }

    @Override
    public void setupSubcommands() throws Exception {
        subcommands.add(new Subcommand("Prompt the automessage setup", "setup", "setup") {
            @Override
            public void onCall(Guild guild, MessageChannel channel, User user, Message message, String[] args) {
                sendTranslatedMessage("Type **join** to set this server's join message, **leave** to set this server's leave message, or " +
                        "**channel** to set the channel I'll send join & leave messages to", channel, user);
                interactiveOperation(channel, message, selectType -> {
                    String type;
                    String content = selectType.getContent();
                    if (content.equalsIgnoreCase("join")) {
                        type = "join";
                        sendTranslatedMessage("Type the message you want to send below", channel, user);
                    } else if (content.equalsIgnoreCase("leave")) {
                        type = "leave";
                        sendTranslatedMessage("Type the message you want to send below", channel, user);
                    } else if (content.equalsIgnoreCase("channel")) {
                        type = "channel";
                        sendTranslatedMessage("Mention a channel to send the automessages to", channel, user);
                    } else {
                        sendTranslatedMessage("You specified an invalid category type! Please redo setup", channel, user);
                        return;
                    }
                    longInteractiveOperation(channel, message, 60, inputMessage -> {
                        try {
                            if (type.equals("channel")) {
                                List<TextChannel> mentionedChannels = inputMessage.getMentionedChannels();
                                if (mentionedChannels.size() > 0) {
                                    TextChannel mentioned = mentionedChannels.get(0);
                                    set(guild, mentioned.getId(), 0);
                                    sendTranslatedMessage("Successfully set {0} as the automessage sending channel".replace("{0}",
                                            mentioned.getAsMention()), channel, user);

                                } else sendTranslatedMessage("You needed to mention a channel!", channel, user);
                            } else {
                                String toPut = inputMessage.getRawContent();
                                if (type.equals("join")) {
                                    set(guild, toPut, 1);
                                    sendTranslatedMessage("Successfully set {0} as the join message".replace("{0}",
                                            toPut), channel, user);
                                } else {
                                    set(guild, toPut, 2);
                                    sendTranslatedMessage("Successfully set {0} as the leave message".replace("{0}",
                                            toPut), channel, user);
                                }

                                String textChannel = getMessagesAndChannel(guild).getA();
                                if (textChannel == null || guild.getTextChannelById(textChannel) == null) {
                                    sendTranslatedMessage("Warning: you need a set a channel to send messages to, or else nothing " +
                                            "will happen", channel, user);
                                }
                            }
                        } catch (Exception e) {
                            new BotException(e);
                        }
                    });
                });
            }
        });

        subcommands.add(new Subcommand("View special arguments you can add to your messages", "arguments") {
            @Override
            public void onCall(Guild guild, MessageChannel channel, User user, Message message, String[] args) {
                sendTranslatedMessage("The available arguments are listed here:\n" +
                        "{user}: mentions the user involved\n" +
                        "{servername}: replaced with the name of your server\n" +
                        "{amtusers}: replaced with the amount of users in your server", channel, user);
            }
        });

        subcommands.add(new Subcommand("Remove a setting", "remove", "remove") {
            @Override
            public void onCall(Guild guild, MessageChannel channel, User user, Message message, String[] args) {
                if (guild.getMember(user).hasPermission(Permission.MANAGE_SERVER)) {
                    if (args.length == 2) {
                        sendTranslatedMessage("Type **join** to remove this server's join message, **leave** to remove this server's " +
                                "leave message, or " +
                                "**channel** to remove the set channel", channel, user);
                    } else {
                        String type = args[2];
                        Triplet<String, String, String> settings = getMessagesAndChannel(guild);
                        if (type.equalsIgnoreCase("channel")) {
                            if (settings.getA() == null) {
                                sendTranslatedMessage("There isn't a channel set!", channel, user);
                            } else {
                                remove(guild, 0);
                                sendTranslatedMessage("Successfully removed the automessage channel", channel, user);
                            }
                        } else if (type.equalsIgnoreCase("join")) {
                            if (settings.getB() == null) {
                                sendTranslatedMessage("There's no welcome message set!", channel, user);
                            } else {
                                remove(guild, 1);
                                sendTranslatedMessage("Successfully removed the welcome message", channel, user);
                            }
                        } else if (type.equalsIgnoreCase("leave")) {
                            if (settings.getC() == null) {
                                sendTranslatedMessage("There's no goodbye message set!", channel, user);
                            } else {
                                remove(guild, 2);
                                sendTranslatedMessage("The goodbye message has been successfully removed", channel, user);
                            }
                        } else
                            sendTranslatedMessage("Cancelling removal, you specified an invalid category", channel, user);
                    }
                } else
                    sendTranslatedMessage("You need the `Manage Server` permission to use this command", channel, user);
            }
        });

        subcommands.add(new Subcommand("View the current automessage settings", "view", "view", "settings") {
            @Override
            public void onCall(Guild guild, MessageChannel channel, User user, Message message, String[] args) {
                Triplet<String, String, String> messages = getMessagesAndChannel(guild);
                StringBuilder sb = new StringBuilder();
                sb.append("**Automessage Settings**\n=============\n");
                if (messages.getA() == null) sb.append("There isn't a set automessage channel" + "\n\n");
                else {
                    TextChannel textChannel = guild.getTextChannelById(messages.getA());
                    if (textChannel != null) {
                        sb.append("The set channel for messages is {0}".replace("{0}", textChannel.getName() + "\n\n"));
                    } else sb.append("There isn't a set automessage channel" + "\n\n");
                }

                if (messages.getB() != null)
                    sb.append("The set join message is {0}".replace("{0}", messages.getB()) + "\n\n");
                else sb.append("There isn't a set join message" + "\n\n");

                if (messages.getC() != null)
                    sb.append("The set join message is {0}".replace("{0}", messages.getC()) + "\n");
                else sb.append("There isn't a set goodbye message");

                sendTranslatedMessage(sb.toString(), channel, user);
            }
        });
    }
}