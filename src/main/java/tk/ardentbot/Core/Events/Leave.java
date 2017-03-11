package tk.ardentbot.Core.Events;

import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.events.guild.GuildLeaveEvent;
import net.dv8tion.jda.core.events.guild.member.GuildMemberLeaveEvent;
import net.dv8tion.jda.core.exceptions.PermissionException;
import net.dv8tion.jda.core.hooks.SubscribeEvent;
import tk.ardentbot.BotCommands.GuildAdministration.Automessage;
import tk.ardentbot.Core.Exceptions.BotException;
import tk.ardentbot.Utils.SQL.DatabaseAction;
import tk.ardentbot.Utils.Tuples.Triplet;

import java.sql.SQLException;
import java.time.Instant;
import java.util.ArrayList;

import static tk.ardentbot.Main.Ardent.ardent;

@SuppressWarnings("Duplicates")
public class Leave {
    public static ArrayList<Instant> botLeaveEvents = new ArrayList<>();
    public static ArrayList<Instant> userLeaveEvents = new ArrayList<>();

    @SubscribeEvent
    public void onLeave(GuildLeaveEvent event) {
        botLeaveEvents.add(Instant.now());

        Guild guild = event.getGuild();
        String id = guild.getId();
        ardent.cleverbots.remove(id);
        ardent.botPrefixData.remove(guild);
        try {
            new DatabaseAction("DELETE FROM JoinEvents WHERE GuildID=?").set(id).update();
        }
        catch (SQLException e) {
            new BotException(e);
        }
    }

    @SubscribeEvent
    public void onUserLeave(GuildMemberLeaveEvent event) throws SQLException {
        userLeaveEvents.add(Instant.now());

        Guild guild = event.getGuild();
        Member left = event.getMember();
        Triplet<String, String, String> automessageSettings = Automessage.getMessagesAndChannel(guild);
        String channelId = automessageSettings.getA();
        String leave = automessageSettings.getC();
        if (channelId != null && leave != null) {
            TextChannel channel = guild.getTextChannelById(channelId);
            if (channel != null) {
                try {
                    leave = leave.replace("{user}", left.getUser().getName()).replace("{servername}", guild.getName()).replace("{amtusers}", String.valueOf(guild.getMembers().size()));
                    channel.sendMessage(leave).queue();
                }
                catch (PermissionException ex) {
                }
            }
        }
    }
}
