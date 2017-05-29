package tk.ardentbot.core.events;

import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.events.Event;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.hooks.SubscribeEvent;
import tk.ardentbot.core.executor.BaseCommand;
import tk.ardentbot.core.misc.logging.BotException;
import tk.ardentbot.main.Ardent;
import tk.ardentbot.main.Shard;
import tk.ardentbot.rethink.models.AdvertisingInfraction;
import tk.ardentbot.rethink.models.AntiAdvertisingSettings;
import tk.ardentbot.utils.discord.GuildUtils;
import tk.ardentbot.utils.discord.UserUtils;

import java.sql.Date;
import java.time.Instant;
import java.util.ArrayList;

import static tk.ardentbot.main.Ardent.shard0;
import static tk.ardentbot.rethink.Database.connection;
import static tk.ardentbot.rethink.Database.r;

public class OnMessage {
    @SubscribeEvent
    public void onEvent(Event event) {
        Shard shard = GuildUtils.getShard(event.getJDA());
        shard.setLAST_EVENT(System.currentTimeMillis());
    }

    @SubscribeEvent
    public void onMessage(MessageReceivedEvent event) {
        if (!Ardent.started) return;
        if (event.getAuthor().isBot()) return;
        try {
            switch (event.getChannel().getType()) {
                case TEXT:
                    TriviaChecker.check(event);
                    Guild guild = event.getGuild();
                    Shard shard = GuildUtils.getShard(guild);
                    shard.factory.incrementMessagesReceived();
                    if (event.getGuild() == null)
                        return; // This one will never be executed. But just in case to avoid NPE.
                    AntiAdvertisingSettings antiAdvertisingSettings = BaseCommand.asPojo(r.table("anti_advertising_settings").get(guild
                            .getId()).run(connection), AntiAdvertisingSettings.class);
                    if (antiAdvertisingSettings != null && !antiAdvertisingSettings.isAllow_discord_server_links()) {
                        if (!event.getMember().hasPermission(Permission.MANAGE_SERVER)) {
                            if (event.getMessage().getRawContent().contains("discordapp.com/invite") || event.getMessage().getRawContent
                                    ().contains("discord.gg"))
                            {
                                event.getMessage().delete().queue();
                                ArrayList<AdvertisingInfraction> infractions = BaseCommand.queryAsArrayList(AdvertisingInfraction.class,
                                        r.table
                                                ("advertising_infractions").filter(row -> row.g("guild_id").eq(guild.getId())
                                                .and(row.g("user_id").eq(event.getAuthor().getId()))).run(connection));
                                if (infractions.size() > 2 && antiAdvertisingSettings.isBan_after_two_infractions()) {
                                    guild.getController().ban(event.getAuthor(), 1).queue();
                                    shard.help.sendEditedTranslation("Banned {0} for advertising.", event.getAuthor(), event.getChannel()
                                            , UserUtils.getNameWithDiscriminator(event.getAuthor().getId()));
                                    event.getAuthor().openPrivateChannel().queue(privateChannel -> {
                                        privateChannel.sendMessage("You were banned from " + guild.getName() + " for advertising.").queue();
                                    });
                                    r.table("advertising_infractions").filter(row -> row.g("guild_id").eq(guild.getId())
                                            .and(row.g("user_id").eq(event.getAuthor().getId()))).delete().run(connection);
                                }
                                else {
                                    r.table("advertising_infractions").insert(r.json(BaseCommand.getStaticGson().toJson(new
                                            AdvertisingInfraction(event.getAuthor().getId(), guild.getId())))).run(connection);
                                    shard.help.sendEditedTranslation("{0}, you can't advertise Discord servers here!", event.getAuthor(),
                                            event
                                                    .getChannel(), event.getAuthor().getAsMention());
                                }
                                return;
                            }

                        }
                    }

                    if (guild.getId().equalsIgnoreCase("260841592070340609")) {
                        UserUtils.addMoney(event.getAuthor(), 0.10);
                    }

                    Member ardentMember = event.getGuild().getMember(event.getJDA().getSelfUser());
                    Member userMember = event.getMember();

                    if (ardentMember == null || userMember == null || userMember.hasPermission(Permission
                            .MANAGE_SERVER) || !ardentMember.hasPermission(Permission.MESSAGE_MANAGE))
                    {
                        shard.factory.pass(event, GuildUtils.getPrefix(guild));
                        return; // The event will be handled and musn't be resumed here.
                    }

                    if (!shard.botMuteData.isMuted(event.getMember())) {
                        shard.factory.pass(event, GuildUtils.getPrefix(guild));
                        return; // The event will be handled and musn't be resumed here.
                    }

                    event.getMessage().delete().queue();
                    String reply = "Sorry, but you're muted in {0} until {1}".replace("{0}", event.getGuild().getName()).replace("{1}",
                            Date.from(Instant
                                    .ofEpochSecond(shard.botMuteData.getUnmuteTime(event.getMember()) / 1000))
                                    .toLocaleString());
                    event.getAuthor().openPrivateChannel().queue(privateChannel -> privateChannel.sendMessage(reply)
                            .queue());
                    break;
                case PRIVATE:
                    event.getChannel().sendMessage("Please use Ardent commands inside of a guild!").queue();
                    shard0.factory.incrementMessagesReceived();
                    break;
            }
        }
        catch (Exception ex) {
            new BotException(ex);
        }
    }
}
