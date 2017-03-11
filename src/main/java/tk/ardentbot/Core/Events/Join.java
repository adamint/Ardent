package tk.ardentbot.Core.Events;

import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.events.guild.GuildJoinEvent;
import net.dv8tion.jda.core.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.core.exceptions.PermissionException;
import net.dv8tion.jda.core.hooks.SubscribeEvent;
import tk.ardentbot.BotCommands.BotInfo.Status;
import tk.ardentbot.BotCommands.GuildAdministration.Automessage;
import tk.ardentbot.BotCommands.GuildAdministration.DefaultRole;
import tk.ardentbot.BotCommands.Music.GuildMusicManager;
import tk.ardentbot.Core.Exceptions.BotException;
import tk.ardentbot.Utils.SQL.DatabaseAction;
import tk.ardentbot.Utils.Tuples.Triplet;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import static tk.ardentbot.Main.Ardent.ardent;

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
            "Simple! Just type /joinmessage\n" +
            "**Question 5: I'm not english!!! How do I find Ardent in my language??**\n" +
            "Easy! Simply do /language view to see a list of all supported language - then do /language set " +
            "myLanguageNameHere!\n\n" +
            "**Reset Ardent to english at ANY time by mentioning Ardent and typing english - Example: @Ardent " +
            "english**";

    @SubscribeEvent
    public void onJoin(GuildJoinEvent event) {
        botJoinEvents.add(Instant.now());
        Guild guild = event.getGuild();
        Status.commandsByGuild.put(guild.getId(), 0);
        ardent.musicManagers.put(Long.parseLong(guild.getId()), new GuildMusicManager(ardent.playerManager));

        ardent.cleverbots.put(guild.getId(), ardent.cleverBot.createSession());
        ardent.sentAnnouncement.put(guild.getId(), false);
        TextChannel channel = guild.getPublicChannel();
        try {
            String prefix;
            String language;

            DatabaseAction getGuild = new DatabaseAction("SELECT * FROM Guilds WHERE GuildID=?")
                    .set(guild.getId());
            ResultSet isGuildIn = getGuild.request();
            if (!isGuildIn.next()) {
                new DatabaseAction("INSERT INTO Guilds VALUES (?,?,?)").set(guild.getId())
                        .set("english").set("/").update();
                prefix = "/";
                language = "english";
            }
            else {
                prefix = isGuildIn.getString("Prefix");
                language = isGuildIn.getString("Language");
            }

            ardent.botPrefixData.set(guild, prefix);
            ardent.botLanguageData.set(guild, language);

            getGuild.close();

            new DatabaseAction("INSERT INTO JoinEvents VALUES (?,?)").set(guild.getId())
                    .set(Timestamp.from(Instant.now())).update();

            Status.commandsByGuild.put(guild.getId(), 0);
            ardent.executorService.schedule(() -> {
                channel.sendMessage(welcomeText).queue();
                guild.getOwner().getUser().openPrivateChannel().queue(privateChannel -> privateChannel.sendMessage
                        ("Hey! Thanks for adding Ardent. If you have **any** " +
                                "questions, comments, concerns, or bug reports, please join our support guild at " +
                                "https://discordapp.com/invite/rfGSxNA\n" +
                                "Also, please don't hesitate to contact Adam#9261 or join our guild.").queue());
            }, 3, TimeUnit.SECONDS);
        }
        catch (SQLException e) {
            new BotException(e);
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
    }
}
