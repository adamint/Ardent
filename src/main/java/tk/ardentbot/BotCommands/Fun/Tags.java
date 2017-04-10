package tk.ardentbot.BotCommands.Fun;

import com.rethinkdb.net.Cursor;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;
import tk.ardentbot.Core.CommandExecution.Command;
import tk.ardentbot.Core.CommandExecution.Subcommand;
import tk.ardentbot.Core.Translation.Language;
import tk.ardentbot.Rethink.Models.Tag;
import tk.ardentbot.Utils.Discord.GuildUtils;
import tk.ardentbot.Utils.StringUtils;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

import static tk.ardentbot.Main.Ardent.globalGson;
import static tk.ardentbot.Rethink.Database.connection;
import static tk.ardentbot.Rethink.Database.r;

public class Tags extends Command {
    public Tags(CommandSettings commandSettings) {
        super(commandSettings);
    }

    private static ArrayList<String> getTagsForGuild(Guild guild) throws SQLException {
        ArrayList<String> tags = new ArrayList<>();
        Cursor<HashMap> cursor = r.db("data").table("tags").filter(row -> row.g("guild_id").eq(guild
                .getId())).run(connection);
        cursor.forEach(hashMap -> {
            tags.add(asPojo(hashMap, Tag.class).getResponse());
        });
        cursor.close();
        return tags;
    }

    @Override
    public void noArgs(Guild guild, MessageChannel channel, User user, Message message, String[] args, Language
            language) throws Exception {
        sendHelp(language, channel, guild, user, this);
    }

    @Override
    public void setupSubcommands() throws Exception {
        subcommands.add(new Subcommand(this, "list") {
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
                sendTranslatedMessage(sb.toString(), channel, user);
            }
        });
        subcommands.add(new Subcommand(this, "g") {
            @Override
            public void onCall(Guild guild, MessageChannel channel, User user, Message message, String[] args,
                               Language language) throws Exception {
                StringBuilder sb = new StringBuilder();
                String query = message.getRawContent().replace(GuildUtils.getPrefix(guild) + args[0] + " " +
                        "" + args[1] + " ", "");
                Cursor<HashMap> findTag = r.db("data").table("tags").filter(row -> row.g("name").eq(query).and(row.g("guild_id").eq(guild
                        .getId()))).run(connection);
                if (findTag.hasNext()) {
                    sb.append(asPojo(findTag.next(), Tag.class).getResponse());
                }
                else {
                    sb.append(getTranslation("tag", language, "didntfindtag").getTranslation());
                    ArrayList<String> similars = StringUtils.mostSimilar(query, getTagsForGuild(guild));
                    for (String s : similars) sb.append("\n > " + s);
                    if (similars.size() == 0)
                        sb.append("\n > " + getTranslation("tag", language, "notags").getTranslation() + "!");
                }
                findTag.close();
                sendTranslatedMessage(sb.toString(), channel, user);
            }
        });
        subcommands.add(new Subcommand(this, "search") {
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
                    sendTranslatedMessage(sb.toString(), channel, user);
                }
                else sendRetrievedTranslation(channel, "tag", language, "mustincludesearchterms", user);
            }
        });
        subcommands.add(new Subcommand(this, "add") {
            @Override
            public void onCall(Guild guild, MessageChannel channel, User user, Message message, String[] args,
                               Language language) throws Exception {
                if (GuildUtils.hasManageServerPermission(guild.getMember(user))) {
                    if (args.length > 3) {
                        String name = message.getRawContent().replace(GuildUtils.getPrefix(guild) + args[0] + " " +
                                args[1] + " ", "").split(" ")[0];
                        String result = message.getRawContent().replace(GuildUtils.getPrefix(guild) + args[0] + " " +
                                args[1] + " " + args[2] + " ", "");
                        if (!getTagsForGuild(guild).contains(name)) {
                            r.db("data").table("tags").insert((r.json(globalGson.toJson(new Tag(guild.getId(), name, result, user.getId()
                            ))))).run
                                    (connection);
                            String reply = getTranslation("tag", language, "successfullyadded").getTranslation()
                                    .replace("{0}", GuildUtils.getPrefix(guild) + args[0]).replace("{1}", name);
                            sendTranslatedMessage(reply, channel, user);
                        }
                        else sendRetrievedTranslation(channel, "tag", language, "alreadyexists", user);
                    }
                    else sendRetrievedTranslation(channel, "tag", language, "invalidarguments", user);
                }
                else sendRetrievedTranslation(channel, "other", language, "needmessagemanage", user);
            }
        });
        subcommands.add(new Subcommand(this, "remove") {
            @Override
            public void onCall(Guild guild, MessageChannel channel, User user, Message message, String[] args,
                               Language language) throws Exception {
                if (GuildUtils.hasManageServerPermission(guild.getMember(user))) {
                    if (args.length == 3) {
                        String name = args[2];
                        if (getTagsForGuild(guild).contains(name)) {
                            r.db("data").table("tags").filter(row -> row.g("guild_id").eq(guild.getId()).and(row.g("name").eq(name)))
                                    .delete().run(connection);
                            String reply = getTranslation("tag", language, "successfullyremoved").getTranslation()
                                    .replace("{1}", name);
                            sendTranslatedMessage(reply, channel, user);
                        }
                        else sendRetrievedTranslation(channel, "tag", language, "atagwiththatnamenotfound", user);
                    }
                    else sendRetrievedTranslation(channel, "tag", language, "invalidarguments", user);
                }
                else sendRetrievedTranslation(channel, "other", language, "needmessagemanage", user);
            }
        });
    }
}
