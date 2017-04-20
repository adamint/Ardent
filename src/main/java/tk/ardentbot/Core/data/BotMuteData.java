package tk.ardentbot.Core.data;

import com.rethinkdb.net.Cursor;
import net.dv8tion.jda.core.entities.Member;
import tk.ardentbot.rethink.models.MuteData;

import java.util.HashMap;

import static tk.ardentbot.Core.executor.BaseCommand.asPojo;
import static tk.ardentbot.rethink.Database.connection;
import static tk.ardentbot.rethink.Database.r;

public class BotMuteData {

    /**
     * HashMap containing the info about mutes.
     */
    private HashMap<String, HashMap<String, Long>> usersGuildMute = new HashMap<>();


    public BotMuteData() {
        Cursor<HashMap> mutes = r.db("data").table("mutes").run(connection);
        mutes.forEach(hashMap -> {
            MuteData muteData = asPojo(hashMap, MuteData.class);
            this.addRaw(muteData.getGuild_id(), muteData.getUser_id(), muteData.getUnmute_epoch_second());
        });
        mutes.close();
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
        r.db("data").table("mutes").insert(r.hashMap("guild_id", m.getGuild().getId()).with("user_id", m.getUser().getId())
                .with("unmute_epoch_second", d).with("muted_by_id", s.getUser().getId())).run(connection);
    }

    private void sql_del(Member m) {
        r.db("data").table("mutes").filter(r.hashMap("user_id", m.getUser().getId())).delete().run(connection);
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
            boolean muted = this.usersGuildMute.get(member.getGuild().getId()).keySet().contains(member.getUser().getId()) && System
                    .currentTimeMillis() < this.usersGuildMute.get(member.getGuild().getId()).get(member.getUser().getId());
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
        return this.usersGuildMute.keySet().contains(member.getGuild().getId()) && this.usersGuildMute.get(member.getGuild().getId())
                .keySet().contains(member.getUser().getId());
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
     * @return Unmute Time
     */
    public long getUnmuteTime(Member member) {
        if (!this.isMuted(member)) return 0;
        return this.usersGuildMute.get(member.getGuild().getId()).get(member.getUser().getId());
    }


    public HashMap<String, HashMap<String, Long>> getMutes() {
        return this.usersGuildMute;
    }

}
