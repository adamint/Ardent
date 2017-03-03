package Events;

import Commands.GuildAdministration.Automessage;
import Main.Ardent;
import Utils.Triplet;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.events.guild.GuildLeaveEvent;
import net.dv8tion.jda.core.events.guild.member.GuildMemberLeaveEvent;
import net.dv8tion.jda.core.exceptions.PermissionException;
import net.dv8tion.jda.core.hooks.SubscribeEvent;

import java.sql.SQLException;
import java.sql.Statement;

import static Main.Ardent.conn;

@SuppressWarnings("Duplicates")
public class Leave {
    @SubscribeEvent
    public void onLeave(GuildLeaveEvent event) {
        String id = event.getGuild().getId();
        Ardent.cleverbots.remove(id);
        try {
            Statement statement = conn.createStatement();
            statement.executeUpdate("DELETE FROM SlowmodeSettings WHERE GuildID='" + id + "'");
            statement.close();
        }
        catch (SQLException e) {
            e.printStackTrace();
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
