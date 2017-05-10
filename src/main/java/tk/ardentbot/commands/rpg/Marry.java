package tk.ardentbot.commands.rpg;

import com.rethinkdb.net.Cursor;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;
import tk.ardentbot.core.executor.Command;
import tk.ardentbot.core.misc.logging.BotException;
import tk.ardentbot.rethink.models.Marriage;
import tk.ardentbot.utils.discord.UserUtils;

import java.util.HashMap;
import java.util.List;

import static tk.ardentbot.rethink.Database.connection;
import static tk.ardentbot.rethink.Database.r;

public class Marry extends Command {
    public Marry(CommandSettings commandSettings) {
        super(commandSettings);
    }

    static Marriage getMarriage(User user) {
        Cursor<HashMap> marriagesForUser = r.db("data").table("marriages").filter(row -> row.g("user_one").eq(user.getId()).or(row.g
                ("user_two").eq(user.getId()))).run(connection);
        if (marriagesForUser.hasNext()) return asPojo(marriagesForUser.next(), Marriage.class);
        else return null;
    }

    @Override
    public void noArgs(Guild guild, MessageChannel channel, User user, Message message, String[] args) throws Exception {
        if (args.length == 1) {
            Marriage marriage = getMarriage(user);
            if (marriage == null) sendTranslatedMessage("You're not married! Marry someone with /marry @(User)", channel, user);
            else {
                sendEditedTranslation("**{0}** is married to **{1}**", user, channel, UserUtils.getUserById(marriage.getUser_one())
                        .getName(), UserUtils.getUserById(marriage.getUser_two()).getName());
            }
        }
        else {
            List<User> mentionedUsers = message.getMentionedUsers();
            if (mentionedUsers.size() == 0) {
                sendTranslatedMessage("You need to mention someone!", channel, user);
                return;
            }
            User toMarryTo = mentionedUsers.get(0);
            Marriage marriage = getMarriage(user);
            if (marriage != null) {
                sendTranslatedMessage("Polygamy isn't allowed >.>", channel, user);
                return;
            }
            if (toMarryTo.isBot()) {
                sendTranslatedMessage("You can't marry a bot, but nice try! As for me, I'll forever love Adam.", channel, user);
                return;
            }
            if (getMarriage(toMarryTo) != null) {
                sendTranslatedMessage("That person is already married!", channel, user);
                return;
            }
            if (toMarryTo.getId().equals(user.getId())) {
                sendTranslatedMessage("Why are you even trying that??", channel, user);
                return;
            }
            sendEditedTranslation("{0}, {1} is proposing to you! Type `yes` to accept or `no` to brutally reject them", user, channel,
                    toMarryTo.getAsMention(), user.getAsMention());
            longInteractiveOperation(channel, message, toMarryTo, 90, replyMessage -> {
                String reply = replyMessage.getContent();
                if (reply.equalsIgnoreCase("yes")) {
                    Marriage m = getMarriage(toMarryTo);
                    if (m != null) {
                        sendTranslatedMessage("Polygamy isn't allowed >.>", channel, user);
                        return;
                    }
                    sendTranslatedMessage("Congratulations! You're now married!", channel, user);
                    r.db("data").table("marriages").insert(r.json(gson.toJson(new Marriage(user.getId(), toMarryTo.getId())))).run
                            (connection);
                }
                else if (reply.equalsIgnoreCase("no")) {
                    try {
                        sendEditedTranslation("Damn, {1} rejected you, {0} :frowning:", user, channel, user.getAsMention(), toMarryTo
                                .getName());
                    }
                    catch (Exception e) {
                        new BotException(e);
                    }
                }
                else {
                    sendTranslatedMessage("Received an invalid response, cancelling marriage...", channel, user);
                }
            });
        }
    }

    @Override
    public void setupSubcommands() throws Exception {
    }
}
