package tk.ardentbot.core.events;

import com.rethinkdb.net.Cursor;
import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.events.guild.GuildJoinEvent;
import net.dv8tion.jda.core.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.core.exceptions.PermissionException;
import net.dv8tion.jda.core.hooks.SubscribeEvent;
import tk.ardentbot.commands.administration.Automessage;
import tk.ardentbot.commands.administration.DefaultRole;
import tk.ardentbot.commands.botinfo.Status;
import tk.ardentbot.commands.music.GuildMusicManager;
import tk.ardentbot.core.executor.BaseCommand;
import tk.ardentbot.main.Ardent;
import tk.ardentbot.main.Shard;
import tk.ardentbot.rethink.models.GuildModel;
import tk.ardentbot.rethink.models.Rankable;
import tk.ardentbot.utils.discord.GuildUtils;
import tk.ardentbot.utils.javaAdditions.Triplet;

import java.sql.SQLException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import static tk.ardentbot.rethink.Database.connection;
import static tk.ardentbot.rethink.Database.r;

public class Join {
    public static ArrayList<Instant> botJoinEvents = new ArrayList<>();
    public static ArrayList<Instant> userJoinEvents = new ArrayList<>();

    public static String welcomeText = "Thanks for adding Ardent, we mean it! This bot has everything from mutes to " +
            "music and everything " +
            "in between for your enjoyment.\n\n" +
            "**_Frequently Asked Questions._**\n" +
            "===================================================\n" +
            "**Question 1: How do I use this bot?**\n" +
            "Simply type /help and a list of commands should be revealed!\n" +
            "**Question 2: Would you please give me the link to the website, fam?**\n" +
            "No worries, my dude. Have fun at https://www.ardentbot.tk :smile:\n" +
            "**Question 3: Why should I use this bot?**\n" +
            "Well, you can do everything from server administration to seeing how long users have been in your server" +
            ". There " +
            "are so many commands to play with!\n" +
            "**Question 4: How do I see this message again?**\n" +
            "Simple! Just type /joinmessage";

    @SubscribeEvent
    public void onJoin(GuildJoinEvent event) {
        Guild guild = event.getGuild();
        Shard shard = GuildUtils.getShard(guild);
        Cursor<GuildModel> guilds = r.db("data").table("guilds").filter(r.hashMap("guild_id", guild.getId())).run(connection);
        if (!guilds.hasNext()) {
            TextChannel channel = guild.getPublicChannel();
            channel.sendMessage(welcomeText).queue();

            r.db("data").table("guilds").insert(r.hashMap("guild_id", guild.getId()).with("language", "english").with("prefix", "/")).run
                    (connection);
            String prefix = "/";

            shard.botPrefixData.set(guild, prefix);

            botJoinEvents.add(Instant.now());
            Status.commandsByGuild.put(guild.getId(), 0);
            shard.musicManagers.put(Long.parseLong(guild.getId()), new GuildMusicManager(shard.playerManager,
                    null));


            Ardent.cleverbots.put(guild.getId(), shard.cleverBot.createSession());
            Ardent.sentAnnouncement.put(guild.getId(), false);
            Status.commandsByGuild.put(guild.getId(), 0);
            shard.executorService.schedule(() -> {
                guild.getOwner().getUser().openPrivateChannel().queue(privateChannel -> privateChannel
                        .sendMessage
                                ("Thanks for adding Ardent. If you have questions or something isn't working, join over 300 people on our" +
                                        " support server @ https://discordapp.com/invite/rfGSxNA").queue());
            }, 3, TimeUnit.SECONDS);
        }
    }

    @SubscribeEvent
    public void onJoinUser(GuildMemberJoinEvent event) throws SQLException {
        userJoinEvents.add(Instant.now());

        Member joined = event.getMember();
        User joinedUser = joined.getUser();
        Guild guild = event.getGuild();
        Role role = DefaultRole.getDefaultRole(guild);
        if (role != null) {
            try {
                guild.getController().addRolesToMember(event.getMember(), role).queue();
            }
            catch (PermissionException ex) {
            }
        }
        Triplet<String, String, String> automessageSettings = Automessage.getMessagesAndChannel(guild);
        String channelId = automessageSettings.getA();
        String welcome = automessageSettings.getB();
        if (channelId != null && welcome != null) {
            TextChannel channel = guild.getTextChannelById(channelId);
            if (channel != null) {
                try {
                    welcome = welcome.replace("{user}", joinedUser.getAsMention()).replace("{servername}", guild
                            .getName()).replace("{amtusers}", String.valueOf(guild.getMembers().size()));
                    channel.sendMessage(welcome).queue();
                }
                catch (PermissionException ex) {
                }
            }
        }
        GuildModel guildModel = BaseCommand.asPojo(r.table("guilds").get(guild.getId()).run(connection), GuildModel.class);
        guildModel.role_permissions.forEach(rolePermission -> {
            Rankable rankable = rolePermission.getRankable();
            if (rankable != null && rankable.getStartsOnServerJoin()) {
                rankable.getQueued().put(joinedUser.getId(), Instant.now().getEpochSecond());
            }
        });
    }
}
