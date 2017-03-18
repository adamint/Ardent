package tk.ardentbot.Core.BotData;

import net.dv8tion.jda.core.entities.Member;
import tk.ardentbot.Core.LoggingUtils.BotException;
import tk.ardentbot.Main.Ardent;
import tk.ardentbot.Utils.SQL.DatabaseAction;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;

public class BotMuteData {

    /**
     * HashMap containing the info about mutes.
     */
    private HashMap<String, HashMap<String, Long>> usersGuildMute = new HashMap<>();


    public BotMuteData() {
        try {
            DatabaseAction selectMutes = new DatabaseAction("SELECT * FROM Mutes");
            ResultSet mutes = selectMutes.request();
            while (mutes.next()) { // Adding all old mute into the HashMap
                this.addRaw(mutes.getString("GuildID"), mutes.getString("UserID"), mutes.getLong("UnmuteEpochSecond"));
            }
            selectMutes.close();
        }
        catch (Exception ex) {
            new BotException(ex);
        }
    }

    private void addRaw(String gID, String uID, long endMute) {
        if (this.usersGuildMute.keySet().contains(gID)) {
            HashMap<String, Long> usersMute = this.usersGuildMute.get(gID);
            if (!usersMute.keySet().contains(uID)) {
                usersMute.put(uID, endMute);
                this.usersGuildMute.replace(gID, usersMute); // Just in case.
            }
        }
        else {
            this.usersGuildMute.put(gID, new HashMap<>());
            this.addRaw(gID, uID, endMute); // Re-run to take the new entry.
        }
    }


    private void sql_add(Member m, long d, Member s) {
        try {
            PreparedStatement statement = Ardent.conn.prepareStatement("INSERT INTO Mutes VALUES (?, ?, ?, ?)");

            statement.setString(1, m.getGuild().getId());
            statement.setString(2, s.getUser().getId());
            statement.setLong(3, d);
            statement.setString(4, m.getUser().getId());

            statement.executeUpdate();

            statement.close();
        }
        catch (Exception ex) {
            new BotException(ex);
        }
    }

    private void sql_del(Member m) {
        try {
            PreparedStatement statement = Ardent.conn.prepareStatement("DELETE FROM Mutes WHERE GuildID = ? AND " +
                    "UserID = ?");

            statement.setString(1, m.getGuild().getId());
            statement.setString(2, m.getUser().getId());

            statement.executeUpdate();

            statement.close();
        }
        catch (Exception ex) {
            new BotException(ex);
        }
    }

    /**
     * Mute the user for a specified amount of time.
     * If the user is already muted, the duration will be added
     * to the current one.
     *
     * @param member   Guild's Member.
     * @param duration Duration (in millisecond) for the mute.
     */
    public boolean mute(Member member, long duration, Member sender) {
        if (this.isMuted(member)) return false;
        this.usersGuildMute.get(member.getGuild().getId()).put(member.getUser().getId(), System.currentTimeMillis() + duration);
        this.sql_add(member, System.currentTimeMillis() + duration, sender);
        return true;
    }

    /**
     * Unmute the uset and delete it from the mute list.
     *
     * @param member Guild's Member
     */
    public void unmute(Member member) {
        this.sql_del(member);
        this.usersGuildMute.get(member.getGuild().getId()).remove(member.getUser().getId());
    }


    /**
     * Check if an user is muted.
     *
     * @param member Guild's Member.
     * @return True if the user is muted. False if not.
     */
    public boolean isMuted(Member member) {
        if (this.usersGuildMute.keySet().contains(member.getGuild().getId())) {
            boolean muted = this.usersGuildMute.get(member.getGuild().getId()).keySet().contains(member.getUser().getId()) && System.currentTimeMillis() < this.usersGuildMute.get(member.getGuild().getId()).get(member.getUser().getId());
            if (!muted) {
                this.sql_del(member);
            }
            return muted;
        }
        else {
            this.usersGuildMute.put(member.getGuild().getId(), new HashMap<>()); // Adding the guild to the mutes data. Avoiding NPE.
        }
        return false;
    }

    /**
     * Check if an user is in the mute list.
     * WARNING !! Even if the user is mute,
     * it will return true !
     *
     * @param member Guild's Member
     * @return True if the user is in the mute list.
     */
    public boolean wasMute(Member member) {
        return this.usersGuildMute.keySet().contains(member.getGuild().getId()) && this.usersGuildMute.get(member.getGuild().getId()).keySet().contains(member.getUser().getId());
    }

    /**
     * Get the mute duration left for an user.
     *
     * @param member Guild's Member
     * @return Duration left until unmute
     */
    public long getMuteDuration(Member member) {
        if (!this.isMuted(member)) return 0;
        return this.usersGuildMute.get(member.getGuild().getId()).get(member.getUser().getId()) - System.currentTimeMillis();
    }

    /**
     * Get the unmute time in millisecond for an user.
     *
     * @param member Guild's Member
     * @return       Unmute Time
     */
    public long getUnmuteTime(Member member){
        if (!this.isMuted(member)) return 0;
        return this.usersGuildMute.get(member.getGuild().getId()).get(member.getUser().getId());
    }


    public HashMap<String, HashMap<String, Long>> getMutes() {
        return this.usersGuildMute;
    }

}
