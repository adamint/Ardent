package tk.ardentbot.BotCommands.Fun;

import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;
import tk.ardentbot.Core.CommandExecution.Cmd;
import tk.ardentbot.Core.CommandExecution.SubCmd;
import tk.ardentbot.Core.Translation.Language;
import tk.ardentbot.Utils.Discord.GuildUtils;
import tk.ardentbot.Utils.SQL.DatabaseAction;
import tk.ardentbot.Utils.StringUtils;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import static tk.ardentbot.Utils.SQL.SQLUtils.cleanString;

public class Tags extends Cmd {
    public Tags(CommandSettings commandSettings) {
        super(commandSettings);
    }

    public static ArrayList<String> getTagsForGuild(Guild guild) throws SQLException {
        ArrayList<String> tags = new ArrayList<>();
        DatabaseAction getTags = new DatabaseAction("SELECT * FROM Tags WHERE GuildID=?").set(guild.getId());
        ResultSet set = getTags.request();
        while (set.next()) {
            tags.add(set.getString("Name"));
        }
        getTags.close();
        return tags;
    }

    @Override
    public void noArgs(Guild guild, MessageChannel channel, User user, Message message, String[] args, Language
            language) throws Exception {
        sendHelp(language, channel, guild, user, this);
    }

    @Override
    public void setupSubcommands() throws Exception {
        subCmds.add(new SubCmd(this, "list") {
            @Override
            public void onCall(Guild guild, MessageChannel channel, User user, Message message, String[] args,
                               Language language) throws Exception {
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
        subCmds.add(new SubCmd(this, "g") {
            @Override
            public void onCall(Guild guild, MessageChannel channel, User user, Message message, String[] args,
                               Language language) throws Exception {
                StringBuilder sb = new StringBuilder();
                String query = cleanString(message.getRawContent().replace(GuildUtils.getPrefix(guild) + args[0] + " " +
                        "" + args[1] + " ", ""));
                DatabaseAction findTag = new DatabaseAction("SELECT * FROM Tags WHERE Name=?").set(query);
                ResultSet set = findTag.request();
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
                findTag.close();
                sendTranslatedMessage(sb.toString(), channel);
            }
        });
        subCmds.add(new SubCmd(this, "search") {
            @Override
            public void onCall(Guild guild, MessageChannel channel, User user, Message message, String[] args,
                               Language language) throws Exception {
                if (args.length > 2) {
                    StringBuilder sb = new StringBuilder();
                    sb.append("**" + getTranslation("tag", language, "searchresults").getTranslation() + "**:");
                    String query = message.getRawContent().replace(GuildUtils.getPrefix(guild) + args[0] + " " +
                            args[1] + " ", "");
                    ArrayList<String> similars = StringUtils.mostSimilar(query, getTagsForGuild(guild));
                    for (String s : similars) sb.append("\n > " + s);
                    if (similars.size() == 0)
                        sb.append("\n > " + getTranslation("tag", language, "notags").getTranslation() + "!");
                    sendTranslatedMessage(sb.toString(), channel);
                }
                else sendRetrievedTranslation(channel, "tag", language, "mustincludesearchterms");
            }
        });
        subCmds.add(new SubCmd(this, "add") {
            @Override
            public void onCall(Guild guild, MessageChannel channel, User user, Message message, String[] args,
                               Language language) throws Exception {
                if (guild.getMember(user).hasPermission(Permission.MESSAGE_MANAGE)) {
                    if (args.length > 3) {
                        String name = args[2];
                        String result = message.getRawContent().replace(GuildUtils.getPrefix(guild) + args[0] + " " +
                                args[1] + " " + args[2] + " ", "");
                        if (!getTagsForGuild(guild).contains(name)) {
                            new DatabaseAction("INSERT INTO Tags VALUES (?,?,?,?)").set(guild.getId()).set(name).set
                                    (result).set(user.getId()).update();
                            String reply = getTranslation("tag", language, "successfullyadded").getTranslation()
                                    .replace("{0}", GuildUtils.getPrefix(guild) + args[0]).replace("{1}", name);
                            sendTranslatedMessage(reply, channel);
                        }
                        else sendRetrievedTranslation(channel, "tag", language, "alreadyexists");
                    }
                    else sendRetrievedTranslation(channel, "tag", language, "invalidarguments");
                }
                else sendRetrievedTranslation(channel, "other", language, "needmessagemanage");
            }
        });
        subCmds.add(new SubCmd(this, "remove") {
            @Override
            public void onCall(Guild guild, MessageChannel channel, User user, Message message, String[] args,
                               Language language) throws Exception {
                if (guild.getMember(user).hasPermission(Permission.MESSAGE_MANAGE)) {
                    if (args.length == 3) {
                        String name = args[2];
                        if (getTagsForGuild(guild).contains(name)) {
                            new DatabaseAction("DELETE FROM Tags WHERE GuildID=? AND Name=?").set(guild.getId())
                                    .set(name).update();
                            String reply = getTranslation("tag", language, "successfullyremoved").getTranslation()
                                    .replace("{1}", name);
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
}
