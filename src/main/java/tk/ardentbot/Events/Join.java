package tk.ardentbot.Events;

import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.events.guild.GuildJoinEvent;
import net.dv8tion.jda.core.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.core.exceptions.PermissionException;
import net.dv8tion.jda.core.hooks.SubscribeEvent;
import tk.ardentbot.Commands.BotInfo.Status;
import tk.ardentbot.Commands.GuildAdministration.Automessage;
import tk.ardentbot.Commands.GuildAdministration.DefaultRole;
import tk.ardentbot.Main.Ardent;
import tk.ardentbot.Utils.GuildUtils;
import tk.ardentbot.Utils.Tuples.Triplet;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Timer;
import java.util.TimerTask;

import static tk.ardentbot.Main.Ardent.*;

public class Join {
    public static String welcomeText = "Thanks for adding Ardent, we mean it! This bot has everything from mutes to music and everything " +
            "in between for your enjoyment.\n\n" +
            "**_Frequently Asked Questions._**\n" +
            "===================================================\n" +
            "**Question 1: How do I use this bot?**\n" +
            "Simply type /help and a list of commands should be revealed!\n" +
            "**Question 2: Would you please give me the link to the website, fam?**\n" +
            "No worries, my dude. Have fun at https://www.ardentbot.tk :smile:\n" +
            "**Question 3: Why should I use this bot?**\n" +
            "Well, you can do everything from server administration to seeing how long users have been in your server. There " +
            "are so many commands to play with!\n" +
            "**Question 4: How do I see this message again?**\n" +
            "Simple! Just type /joinmessage\n" +
            "**Question 5: I'm not english!!! How do I find Ardent in my language??**\n" +
            "Easy! Simply do /language view to see a list of all supported language - then do /language set myLanguageNameHere!\n\n" +
            "**Reset Ardent to english at ANY time by mentioning Ardent and typing english - Example: @Ardent english**";

    @SubscribeEvent
    public void onJoin(GuildJoinEvent event) {
        Guild guild = event.getGuild();
        Status.commandsByGuild.put(guild.getId(), 0);
        Ardent.cleverbots.put(guild.getId(), Ardent.cleverBot.createSession());

        if (announcement != null) {
            sentAnnouncement.put(guild.getId(), false);
        }

        TextChannel channel = guild.getPublicChannel();
        try {
            Statement statement = conn.createStatement();
            ResultSet set = statement.executeQuery("SELECT * FROM Guilds WHERE GuildID='" + guild.getId() + "'");
            if (!set.next()) {
                Status.commandsByGuild.put(guild.getId(), 0);
                statement.executeUpdate("INSERT INTO Guilds VALUES ('" + guild.getId() + "', 'english', '/')");
                new Timer().schedule(new TimerTask() {
                    @Override
                    public void run() {
                        guild.getOwner().getUser().openPrivateChannel().queue(privateChannel -> privateChannel.sendMessage("Hey! Thanks for adding Ardent. If you have **any** " +
                                "questions, comments, concerns, or bug reports, please join our support guild at https://discordapp.com/invite/rfGSxNA\n" +
                                "Also, please don't hesitate to contact Adam#9261 (the developer). Ardent is a small bot, but we want as much feedback " +
                                "and ideas as we can get!").queue());
                        this.cancel();
                    }
                }, (1000 * 20));
                try {
                    statement.executeUpdate("INSERT INTO SlowmodeSettings VALUES ('" + guild.getId() + "', '5', '0')");
                }
                catch (SQLException e) {
                    e.printStackTrace();
                }
            }

            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    channel.sendMessage(welcomeText).queue();
                }
            }, 2500);

            set.close();
            statement.close();
        }
        catch (SQLException e) {
            e.printStackTrace();
        }

        GuildUtils.guildPrefixes.put(guild.getId(), "/");
    }

    @SubscribeEvent
    public void onJoinUser(GuildMemberJoinEvent event) throws SQLException {
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
                    welcome = welcome.replace("{user}", joinedUser.getAsMention()).replace("{servername}", guild.getName()).replace("{amtusers}", String.valueOf(guild.getMembers().size()));
                    channel.sendMessage(welcome).queue();
                }
                catch (PermissionException ex) {
                }
            }
        }
    }
}
