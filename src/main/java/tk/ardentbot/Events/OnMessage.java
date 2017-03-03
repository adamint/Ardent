package tk.ardentbot.Events;

import tk.ardentbot.Bot.BotException;
import tk.ardentbot.Main.Ardent;
import tk.ardentbot.Utils.GuildUtils;
import tk.ardentbot.Utils.MuteDaemon;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.hooks.SubscribeEvent;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.Instant;

import static tk.ardentbot.Main.Ardent.ardent;
import static tk.ardentbot.Main.Ardent.conn;

public class OnMessage {
    @SubscribeEvent
    public void onMessage(MessageReceivedEvent event) {
        User user = event.getAuthor();
        if (!user.isBot()) {
            try {
                Ardent.factory.incrementMessagesReceived();
                MessageChannel channel = event.getChannel();
                Message message = event.getMessage();
                if (channel instanceof PrivateChannel) {
                    Ardent.factory.pass(event);
                }
                else {
                    Guild guild = event.getGuild();
                    if (guild != null) {
                        Member ardentMember = guild.getMember(ardent);
                        Member userMember = guild.getMember(user);
                        if (ardentMember != null && userMember != null) {
                            if (!userMember.hasPermission(Permission.MANAGE_SERVER) && ardentMember.hasPermission(Permission.MESSAGE_MANAGE)) {
                                Statement statement = conn.createStatement();
                                ResultSet mutes = statement.executeQuery("SELECT * FROM Mutes WHERE GuildID='" + guild.getId() + "'");
                                boolean cancel = false;
                                while (mutes.next()) {
                                    if (mutes.getString("UserID").equalsIgnoreCase(user.getId())) {
                                        message.deleteMessage().queue();
                                        String reply = MuteDaemon.getTranslationForNonCommands("mute", GuildUtils.getLanguage(guild), "youremuted").getTranslation()
                                                .replace("{0}", guild.getName()).replace("{1}", Date.from(Instant.ofEpochSecond(mutes.getLong("UnmuteEpochSecond") / 1000)).toLocaleString());
                                        user.openPrivateChannel().queue(privateChannel -> privateChannel.sendMessage(reply).queue());
                                        cancel = true;
                                    }
                                }
                                mutes.close();
                                statement.close();
                                if (!cancel) Ardent.factory.pass(event);
                            }
                            else Ardent.factory.pass(event);
                        }
                        else Ardent.factory.pass(event);
                    }
                }
            }
            catch (Exception e) {
                new BotException(e);
            }
        }
    }
}
