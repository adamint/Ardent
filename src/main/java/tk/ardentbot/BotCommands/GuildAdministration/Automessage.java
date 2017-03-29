package tk.ardentbot.BotCommands.GuildAdministration;

import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.*;
import tk.ardentbot.Core.CommandExecution.Command;
import tk.ardentbot.Core.CommandExecution.Subcommand;
import tk.ardentbot.Core.Translation.Language;
import tk.ardentbot.Core.Translation.Translation;
import tk.ardentbot.Core.Translation.TranslationResponse;
import tk.ardentbot.Main.Ardent;
import tk.ardentbot.Utils.Discord.GuildUtils;
import tk.ardentbot.Utils.JLAdditions.Triplet;
import tk.ardentbot.Utils.SQL.DatabaseAction;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Automessage extends Command {
    public Automessage(CommandSettings commandSettings) {
        super(commandSettings);
    }

    private static void check(Guild guild) throws SQLException {
        DatabaseAction selectAutomessage = new DatabaseAction("SELECT * FROM Automessages WHERE GuildID=?")
                .set(guild.getId());
        ResultSet set = selectAutomessage.request();
        if (!set.next()) {
            new DatabaseAction("INSERT INTO Automessages VALUES (?,?,?,?)").set(guild.getId()).set("000")
                    .set("000").set("000").update();
        }
        selectAutomessage.close();
    }

    public static Triplet<String, String, String> getMessagesAndChannel(Guild guild) throws SQLException {
        Triplet<String, String, String> triplet;
        check(guild);
        Statement statement = Ardent.conn.createStatement();
        ResultSet set = statement.executeQuery("SELECT * FROM Automessages WHERE GuildID='" + guild.getId() + "'");
        if (set.next()) {
            String channel = set.getString("ChannelID");
            String welcome = set.getString("Welcome");
            String goodbye = set.getString("Goodbye");
            if (channel.equalsIgnoreCase("000")) channel = null;
            if (welcome.equalsIgnoreCase("000")) welcome = null;
            if (goodbye.equalsIgnoreCase("000")) goodbye = null;
            triplet = new Triplet<>(channel, welcome, goodbye);
        }
        else {
            triplet = new Triplet<>(null, null, null);
        }
        set.close();
        statement.close();
        return triplet;
    }

    public static void remove(Guild guild, int num) throws SQLException {
        String columnName;
        if (num == 0) columnName = "ChannelID";
        else if (num == 1) columnName = "Welcome";
        else if (num == 2) columnName = "Goodbye";
        else return;
        new DatabaseAction("UPDATE Automessages SET " + columnName + "=? WHERE GuildID=?").set("000").set(guild.getId
                ()).update();
    }

    @SuppressWarnings("Duplicates")
    public static void set(Guild guild, String text, int num) throws SQLException {
        String columnName;
        if (num == 0) columnName = "ChannelID";
        else if (num == 1) columnName = "Welcome";
        else if (num == 2) columnName = "Goodbye";
        else return;
        new DatabaseAction("UPDATE Automessages SET " + columnName + " =? WHERE GuildID=?").set(text).set(guild.getId
                ()).update();
    }

    @Override
    public void noArgs(Guild guild, MessageChannel channel, User user, Message message, String[] args, Language
            language) throws Exception {
        sendHelp(language, channel, guild, user, this);
    }

    @Override
    public void setupSubcommands() throws Exception {
        subcommands.add(new Subcommand(this, "view") {
            @Override
            public void onCall(Guild guild, MessageChannel channel, User user, Message message, String[] args,
                               Language language) throws Exception {
                ArrayList<Translation> translations = new ArrayList<>();
                translations.add(new Translation("automessage", "settings"));
                translations.add(new Translation("automessage", "nochannel"));
                translations.add(new Translation("automessage", "nowelcome"));
                translations.add(new Translation("automessage", "nogoodbye"));
                translations.add(new Translation("automessage", "channel"));
                translations.add(new Translation("automessage", "welcome"));
                translations.add(new Translation("automessage", "goodbye"));

                HashMap<Integer, TranslationResponse> responses = getTranslations(language, translations);
                Triplet<String, String, String> messages = getMessagesAndChannel(guild);
                StringBuilder sb = new StringBuilder();
                sb.append(responses.get(0).getTranslation() + "\n=============\n");
                if (messages.getA() == null) sb.append(responses.get(1).getTranslation() + "\n\n");
                else {
                    TextChannel textChannel = guild.getTextChannelById(messages.getA());
                    if (textChannel != null) {
                        sb.append(responses.get(4).getTranslation().replace("{0}", textChannel.getName() + "\n\n"));
                    }
                    else sb.append(responses.get(1).getTranslation() + "\n\n");
                }

                if (messages.getB() != null)
                    sb.append(responses.get(5).getTranslation().replace("{0}", messages.getB()) + "\n\n");
                else sb.append(responses.get(2).getTranslation() + "\n\n");

                if (messages.getC() != null)
                    sb.append(responses.get(6).getTranslation().replace("{0}", messages.getC()) + "\n");
                else sb.append(responses.get(3).getTranslation());

                sendTranslatedMessage(sb.toString(), channel, user);
            }
        });

        subcommands.add(new Subcommand(this, "arguments") {
            @Override
            public void onCall(Guild guild, MessageChannel channel, User user, Message message, String[] args,
                               Language language) throws Exception {
                StringBuilder sb = new StringBuilder();
                sb.append(getTranslation("automessage", language, "availablearguments").getTranslation());
                sendTranslatedMessage(sb.toString(), channel, user);
            }
        });

        subcommands.add(new Subcommand(this, "set") {
            @Override
            public void onCall(Guild guild, MessageChannel channel, User user, Message message, String[] args,
                               Language language) throws Exception {
                if (guild.getMember(user).hasPermission(Permission.MANAGE_SERVER)) {
                    if (args.length < 4) {
                        sendRetrievedTranslation(channel, "automessage", language, "specifytype", user);
                    }
                    else {
                        if (args[2].equalsIgnoreCase("channel")) {
                            List<TextChannel> mentionedChannels = message.getMentionedChannels();
                            if (mentionedChannels.size() > 0) {
                                TextChannel mentioned = mentionedChannels.get(0);
                                set(guild, mentioned.getId(), 0);
                                sendTranslatedMessage(getTranslation("automessage", language, "successfullyset")
                                        .getTranslation()
                                        .replace("{0}", getTranslation("automessage", language, "channelword")
                                                .getTranslation()).replace("{1}", mentioned.getName()), channel, user);
                            }
                            else sendRetrievedTranslation(channel, "automessage", language, "mentionchannel", user);
                        }
                        else if (args[2].equalsIgnoreCase("join")) {
                            String msg = message.getRawContent().replace(GuildUtils.getPrefix(guild) + args[0] + " "
                                    + args[1] + " " + args[2] + " ", "");
                            if (msg.length() == message.getRawContent().length()) {
                                msg = message.getRawContent().replace("/" + args[0] + " "
                                        + args[1] + " " + args[2] + " ", "");
                            }
                            set(guild, msg, 1);
                            set(guild, channel.getId(), 0);
                            sendTranslatedMessage(getTranslation("automessage", language, "successfullyset")
                                    .getTranslation()
                                    .replace("{0}", getTranslation("automessage", language, "joinword")
                                            .getTranslation()).replace("{1}", msg), channel, user);
                        }
                        else if (args[2].equalsIgnoreCase("leave")) {
                            String msg = message.getRawContent().replace(GuildUtils.getPrefix(guild) + args[0] + " "
                                    + args[1] + " " + args[2] + " ", "");
                            set(guild, msg, 2);
                            set(guild, channel.getId(), 0);
                            sendTranslatedMessage(getTranslation("automessage", language, "successfullyset")
                                    .getTranslation()
                                    .replace("{0}", getTranslation("automessage", language, "leaveword")
                                            .getTranslation()).replace("{1}", msg), channel, user);

                        }
                        else sendRetrievedTranslation(channel, "tag", language, "invalidarguments", user);
                    }
                }
                else sendRetrievedTranslation(channel, "other", language, "needmanageserver", user);
            }
        });

        subcommands.add(new Subcommand(this, "remove") {
            @Override
            public void onCall(Guild guild, MessageChannel channel, User user, Message message, String[] args,
                               Language language) throws Exception {
                if (guild.getMember(user).hasPermission(Permission.MANAGE_SERVER)) {
                    if (args.length == 2) {
                        sendRetrievedTranslation(channel, "automessage", language, "specifytype", user);
                    }
                    else {
                        String type = args[2];
                        Triplet<String, String, String> settings = getMessagesAndChannel(guild);
                        if (type.equalsIgnoreCase("channel")) {
                            if (settings.getA() == null) {
                                sendRetrievedTranslation(channel, "automessage", language, "nochannelset", user);
                            }
                            else {
                                remove(guild, 0);
                                sendTranslatedMessage(getTranslation("automessage", language,
                                        "successfullyremovedchannel").getTranslation(), channel, user);
                            }
                        }
                        else if (type.equalsIgnoreCase("join")) {
                            if (settings.getB() == null) {
                                sendRetrievedTranslation(channel, "automessage", language, "nowelcomeset", user);
                            }
                            else {
                                remove(guild, 1);
                                sendTranslatedMessage(getTranslation("automessage", language,
                                        "successfullyremovedwelcome").getTranslation(), channel, user);
                            }
                        }
                        else if (type.equalsIgnoreCase("leave")) {
                            if (settings.getC() == null) {
                                sendRetrievedTranslation(channel, "automessage", language, "nogoodbyeset", user);
                            }
                            else {
                                remove(guild, 2);
                                sendTranslatedMessage(getTranslation("automessage", language,
                                        "successfullyremovedgoodbye").getTranslation(), channel, user);
                            }
                        }
                        else sendRetrievedTranslation(channel, "tag", language, "invalidarguments", user);
                    }
                }
                else sendRetrievedTranslation(channel, "other", language, "needmanageserver", user);
            }
        });
    }
}
