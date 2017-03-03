package tk.ardentbot.Commands.GuildAdministration;

import tk.ardentbot.Backend.Commands.BotCommand;
import tk.ardentbot.Backend.Translation.Language;
import tk.ardentbot.Utils.GuildUtils;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;

import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;

import static tk.ardentbot.Main.Ardent.conn;

public class Unmute extends BotCommand {
    public Unmute(CommandSettings commandSettings) {
        super(commandSettings);
    }

    @Override
    public void noArgs(Guild guild, MessageChannel channel, User user, Message message, String[] args, Language language) throws Exception {
        if (args.length == 1) {
            sendTranslatedMessage(getTranslation("unmute", language, "help").getTranslation().replace("{0}", GuildUtils.getPrefix(guild) + args[0]), channel);
        }
        else {
            List<User> mentionedUsers = message.getMentionedUsers();
            if (mentionedUsers.size() == 0) {
                sendRetrievedTranslation(channel, "other", language, "mentionuser");
            }
            else {
                if (guild.getMember(user).hasPermission(Permission.MANAGE_SERVER)) {
                    User mentioned = mentionedUsers.get(0);
                    Statement statement = conn.createStatement();
                    ResultSet set = statement.executeQuery("SELECT * FROM Mutes WHERE UserID='" + mentioned.getId() + "' AND " +
                            "GuildID='" + guild.getId() + "'");
                    if (set.next()) {
                        statement.executeUpdate("DELETE FROM Mutes WHERE GuildID='" + guild.getId() + "' AND UserID='" + mentioned.getId() + "'");
                        sendRetrievedTranslation(channel, "unmute", language, "unmuteduser");
                    }
                    else sendRetrievedTranslation(channel, "unmute", language, "notmuted");
                    set.close();
                    statement.close();
                }
                else sendRetrievedTranslation(channel, "other", language, "needmanageserver");
            }
        }
    }

    @Override
    public void setupSubcommands() {}
}
