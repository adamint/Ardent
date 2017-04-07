package tk.ardentbot.Utils.Premium;

import com.rethinkdb.net.Cursor;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Role;
import tk.ardentbot.Core.Misc.LoggingUtils.BotException;
import tk.ardentbot.Main.Ardent;
import tk.ardentbot.Utils.SQL.DatabaseAction;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;

import static tk.ardentbot.Main.Ardent.botLogsShard;
import static tk.ardentbot.Rethink.Database.r;

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
        List<HashMap> set = ((Cursor<HashMap>) r.db("data").table("patrons").filter(row -> row.g("tier")));
        DatabaseAction action = new DatabaseAction("SELECT * FROM Patron WHERE UserID=? AND TierName=?").set(id).set(tierName);
        ResultSet set = action.request();
        if (set.next()) {
            has = true;
        }
        else {
            new DatabaseAction("DELETE FROM Patron WHERE UserID=?").set(id).update();
            new DatabaseAction("INSERT INTO Patron VALUES (?,?)").set(id).set(tierName).update();
        }
        action.close();
        return has;
    }
}
