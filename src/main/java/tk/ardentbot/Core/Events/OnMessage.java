package tk.ardentbot.Core.Events;

import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.hooks.SubscribeEvent;
import tk.ardentbot.Core.LoggingUtils.BotException;
import tk.ardentbot.Core.Translation.LangFactory;
import tk.ardentbot.Core.Translation.Language;
import tk.ardentbot.Main.Shard;
import tk.ardentbot.Utils.Discord.GuildUtils;

import java.sql.Date;
import java.time.Instant;

import static tk.ardentbot.Main.Ardent.shard0;

public class OnMessage {
    @SubscribeEvent
    public void onMessage(MessageReceivedEvent event) {
        if (event.getAuthor().isBot()) return;
        try {
            switch (event.getChannel().getType()) {
                case TEXT:
                    Guild guild = event.getGuild();
                    Language language = GuildUtils.getLanguage(guild);
                    Shard shard = GuildUtils.getShard(guild);
                    shard.factory.incrementMessagesReceived();
                    if (event.getGuild() == null)
                        return; // This one will never be executed. But just in case to avoid NPE.

                    Member ardentMember = event.getGuild().getMember(event.getJDA().getSelfUser());
                    Member userMember = event.getMember();

                    if (ardentMember == null || userMember == null || userMember.hasPermission(Permission
                            .MANAGE_SERVER) || !ardentMember.hasPermission(Permission.MESSAGE_MANAGE))
                    {
                        shard.factory.pass(event, language, null, GuildUtils.getPrefix(guild));
                        return; // The event will be handled and musn't be resumed here.
                    }

                    if (!shard.botMuteData.isMuted(event.getMember())) {
                        shard.factory.pass(event, language, null, GuildUtils.getPrefix(guild));
                        return; // The event will be handled and musn't be resumed here.
                    }

                    event.getMessage().delete().queue();
                    String reply = shard.help.getTranslation("mute", GuildUtils.getLanguage(event.getGuild()),
                            "youremuted").getTranslation()
                            .replace("{0}", event.getGuild().getName()).replace("{1}", Date.from(Instant
                                    .ofEpochSecond(shard.botMuteData.getUnmuteTime(event.getMember()) / 1000))
                                    .toLocaleString());
                    event.getAuthor().openPrivateChannel().queue(privateChannel -> privateChannel.sendMessage(reply)
                            .queue());

                    break;
                case PRIVATE:
                    shard0.factory.pass(event, LangFactory.english, null, "/");
                    shard0.factory.incrementMessagesReceived();
                    break;
            }
        }
        catch (Exception ex) {
            new BotException(ex);
        }
    }
}
