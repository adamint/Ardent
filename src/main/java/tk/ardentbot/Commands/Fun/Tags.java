package tk.ardentbot.Commands.Fun;

import tk.ardentbot.Backend.Commands.BotCommand;
import tk.ardentbot.Backend.Commands.Subcommand;
import tk.ardentbot.Backend.Translation.Language;
import tk.ardentbot.Utils.GuildUtils;
import tk.ardentbot.Utils.StringUtils;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import static tk.ardentbot.Main.Ardent.conn;
import static tk.ardentbot.Utils.SQLUtils.cleanString;

public class Tags extends BotCommand {
    public Tags(CommandSettings commandSettings) {
        super(commandSettings);
    }

    @Override
    public void noArgs(Guild guild, MessageChannel channel, User user, Message message, String[] args, Language language) throws Exception {
        sendTranslatedMessage(getHelp(language), channel);
    }

    @Override
    public void setupSubcommands() throws Exception {
        subcommands.add(new Subcommand(this, "list") {
            @Override
            public void onCall(Guild guild, MessageChannel channel, User user, Message message, String[] args, Language language) throws Exception {
                StringBuilder sb = new StringBuilder();
                sb.append("**" + getTranslation("tag", language, "listtags").getTranslation() + "**");
                ArrayList<String> tags = getTagsForGuild(guild);
                for (String tag : tags) sb.append("\n  > *" + tag + "*");
                if (tags.size() == 0) {
                    sb.append("\n > " + getTranslation("tag", language, "notags").getTranslation() + "!");
                }
                sendTranslatedMessage(sb.toString(), channel);
            }
        });
        subcommands.add(new Subcommand(this, "g") {
            @Override
            public void onCall(Guild guild, MessageChannel channel, User user, Message message, String[] args, Language language) throws Exception {
                StringBuilder sb = new StringBuilder();
                String query = cleanString(message.getRawContent().replace(GuildUtils.getPrefix(guild) + args[0] + " " + args[1] + " ", ""));
                Statement statement = conn.createStatement();
                ResultSet set = statement.executeQuery("SELECT * FROM Tags WHERE Name='" + query + "'");
                if (set.next()) {
                    sb.append(set.getString("Text"));
                }
                else {
                    sb.append(getTranslation("tag", language, "didntfindtag").getTranslation());
                    ArrayList<String> similars = StringUtils.mostSimilar(query, getTagsForGuild(guild));
                    for (String s : similars) sb.append("\n > " + s);
                    if (similars.size() == 0)
                        sb.append("\n > " + getTranslation("tag", language, "notags").getTranslation() + "!");
                }
                set.close();
                statement.close();
                sendTranslatedMessage(sb.toString(), channel);
            }
        });
        subcommands.add(new Subcommand(this, "search") {
            @Override
            public void onCall(Guild guild, MessageChannel channel, User user, Message message, String[] args, Language language) throws Exception {
                if (args.length > 2) {
                    StringBuilder sb = new StringBuilder();
                    sb.append("**" + getTranslation("tag", language, "searchresults").getTranslation() + "**:");
                    String query = message.getRawContent().replace(GuildUtils.getPrefix(guild) + args[0] + " " + args[1] + " ", "");
                    ArrayList<String> similars = StringUtils.mostSimilar(query, getTagsForGuild(guild));
                    for (String s : similars) sb.append("\n > " + s);
                    if (similars.size() == 0)
                        sb.append("\n > " + getTranslation("tag", language, "notags").getTranslation() + "!");
                    sendTranslatedMessage(sb.toString(), channel);
                }
                else sendRetrievedTranslation(channel, "tag", language, "mustincludesearchterms");
            }
        });
        subcommands.add(new Subcommand(this, "add") {
            @Override
            public void onCall(Guild guild, MessageChannel channel, User user, Message message, String[] args, Language language) throws Exception {
                if (guild.getMember(user).hasPermission(Permission.MESSAGE_MANAGE)) {
                    if (args.length > 3) {
                        String name = args[2];
                        String result = message.getRawContent().replace(GuildUtils.getPrefix(guild) + args[0] + " " + args[1] + " " + args[2] + " ", "");
                        if (!getTagsForGuild(guild).contains(name)) {
                            Statement statement = conn.createStatement();
                            statement.executeUpdate("INSERT INTO Tags VALUES ('" + guild.getId() + "', '" + cleanString(name) + "', '" + cleanString(result) + "', '" + user.getId() + "')");
                            String reply = getTranslation("tag", language, "successfullyadded").getTranslation().replace("{0}", GuildUtils.getPrefix(guild) + args[0]).replace("{1}", name);
                            sendTranslatedMessage(reply, channel);
                        }
                        else sendRetrievedTranslation(channel, "tag", language, "alreadyexists");
                    }
                    else sendRetrievedTranslation(channel, "tag", language, "invalidarguments");
                }
                else sendRetrievedTranslation(channel, "other", language, "needmessagemanage");
            }
        });
        subcommands.add(new Subcommand(this, "remove") {
            @Override
            public void onCall(Guild guild, MessageChannel channel, User user, Message message, String[] args, Language language) throws Exception {
                if (guild.getMember(user).hasPermission(Permission.MESSAGE_MANAGE)) {
                    if (args.length == 3) {
                        String name = args[2];
                        if (getTagsForGuild(guild).contains(name)) {
                            Statement statement = conn.createStatement();
                            statement.executeUpdate("DELETE FROM Tags WHERE GuildID='" + guild.getId() + "' AND Name='" + cleanString(name) + "'");
                            String reply = getTranslation("tag", language, "successfullyremoved").getTranslation().replace("{1}", name);
                            sendTranslatedMessage(reply, channel);
                        }
                        else sendRetrievedTranslation(channel, "tag", language, "atagwiththatnamenotfound");
                    }
                    else sendRetrievedTranslation(channel, "tag", language, "invalidarguments");
                }
                else sendRetrievedTranslation(channel, "other", language, "needmessagemanage");
            }
        });
    }

    public static ArrayList<String> getTagsForGuild(Guild guild) throws SQLException {
        ArrayList<String> tags = new ArrayList<>();
        Statement statement = conn.createStatement();
        ResultSet set = statement.executeQuery("SELECT * FROM Tags WHERE GuildID='" + guild.getId() + "'");
        while (set.next()) {
            tags.add(set.getString("Name"));
        }
        set.close();
        statement.close();
        return tags;
    }
}
