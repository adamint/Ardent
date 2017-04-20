package tk.ardentbot.utils.premium;

import com.rethinkdb.net.Cursor;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Role;
import tk.ardentbot.core.misc.loggingUtils.BotException;
import tk.ardentbot.main.Ardent;

import java.sql.SQLException;
import java.util.HashMap;

import static tk.ardentbot.main.Ardent.botLogsShard;
import static tk.ardentbot.rethink.Database.connection;
import static tk.ardentbot.rethink.Database.r;

public class UpdatePremiumMembers implements Runnable {
    Guild ardentGuild = null;

    @Override
    public void run() {
        if (ardentGuild == null) {
            ardentGuild = botLogsShard.jda.getGuildById("260841592070340609");
        }
        ardentGuild.getMembers().forEach(member -> {
            for (Role role : member.getRoles()) {
                String roleName = role.getName();
                try {
                    if (roleName.equalsIgnoreCase("Tier 1")) {
                        if (checkIfHasPermissions(member, "tier1")) Ardent.tierOnepatrons.add(member.getUser().getId());
                    }
                    else if (roleName.equalsIgnoreCase("Tier 2")) {
                        if (checkIfHasPermissions(member, "tier2")) Ardent.tierTwopatrons.add(member.getUser().getId());
                    }
                    else if (roleName.equalsIgnoreCase("Tier 3")) {
                        if (checkIfHasPermissions(member, "tier3")) Ardent.tierThreepatrons.add(member.getUser().getId());
                    }
                }
                catch (SQLException ex) {
                    new BotException(ex);
                }
            }
        });
    }

    private boolean checkIfHasPermissions(Member member, String tierName) throws SQLException {
        boolean has = false;
        String id = member.getUser().getId();
        Cursor<HashMap> set = r.db("data").table("patrons").filter(row -> row.g("tier").eq(tierName).and(
                row.g("user_id").eq(id))).run(connection);
        if (set.hasNext()) has = true;
        else {
            r.db("data").table("patrons").filter(row -> row.g("user_id").eq(id)).delete().run(connection);
        }
        return has;
    }
}
