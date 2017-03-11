package tk.ardentbot.Core.Events;

import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.hooks.SubscribeEvent;
import tk.ardentbot.Core.Exceptions.BotException;
import tk.ardentbot.Utils.Discord.GuildUtils;

import java.sql.Date;
import java.time.Instant;

import static tk.ardentbot.Main.Ardent.ardent;

public class OnMessage {
    @SubscribeEvent
    public void onMessage(MessageReceivedEvent event) {
        if (event.getAuthor().isBot()) return;
        try {
            ardent.factory.incrementMessagesReceived();

            switch (event.getChannel().getType()) {
                case TEXT:
                    if (event.getGuild() == null)
                        return; // This one will never be executed. But just in case to avoid NPE.

                    Member ardentMember = event.getGuild().getMember(event.getJDA().getSelfUser());
                    Member userMember = event.getMember();

                    if (ardentMember == null || userMember == null || userMember.hasPermission(Permission
                            .MANAGE_SERVER) || !ardentMember.hasPermission(Permission.MESSAGE_MANAGE))
                    {
                        ardent.factory.pass(event);
                        return; // The event will be handled and musn't be resumed here.
                    }

                    if (!ardent.botMuteData.isMuted(event.getMember())) {
                        ardent.factory.pass(event);
                        return; // The event will be handled and musn't be resumed here.
                    }

                    event.getMessage().delete().queue();
                    String reply = ardent.help.getTranslation("mute", GuildUtils.getLanguage(event.getGuild()),
                            "youremuted").getTranslation()
                            .replace("{0}", event.getGuild().getName()).replace("{1}", Date.from(Instant
                                    .ofEpochSecond(ardent.botMuteData.getUnmuteTime(event.getMember()) / 1000))
                                    .toLocaleString());
                    event.getAuthor().openPrivateChannel().queue(privateChannel -> privateChannel.sendMessage(reply)
                            .queue());

                    break;
                case PRIVATE:
                    ardent.factory.pass(event);
                    break;
            }
        }
        catch (Exception ex) {
            new BotException(ex);
        }
    }
}
