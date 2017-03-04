package tk.ardentbot.Events;

import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.events.guild.GuildLeaveEvent;
import net.dv8tion.jda.core.events.guild.member.GuildMemberLeaveEvent;
import net.dv8tion.jda.core.exceptions.PermissionException;
import net.dv8tion.jda.core.hooks.SubscribeEvent;
import tk.ardentbot.Bot.BotException;
import tk.ardentbot.Commands.GuildAdministration.Automessage;
import tk.ardentbot.Main.Ardent;
import tk.ardentbot.Utils.SQL.DatabaseAction;
import tk.ardentbot.Utils.Tuples.Triplet;

import java.sql.SQLException;

@SuppressWarnings("Duplicates")
public class Leave {
    @SubscribeEvent
    public void onLeave(GuildLeaveEvent event) {
        Guild guild = event.getGuild();
        String id = guild.getId();
        Ardent.cleverbots.remove(id);
        Ardent.botPrefixData.remove(guild);
        try {
            new DatabaseAction("DELETE FROM JoinEvents WHERE GuildID=?").set(id).update();
        }
        catch (SQLException e) {
            new BotException(e);
        }
    }

    @SubscribeEvent
    public void onUserLeave(GuildMemberLeaveEvent event) throws SQLException {
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
