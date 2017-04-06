package tk.ardentbot.Utils.Updaters;

import com.rethinkdb.net.Cursor;
import tk.ardentbot.Rethink.Models.Patron;
import tk.ardentbot.Rethink.Models.Staff;

import static tk.ardentbot.Main.Ardent.*;
import static tk.ardentbot.Rethink.Database.connection;
import static tk.ardentbot.Rethink.Database.r;

public class PermissionsDaemon implements Runnable {
    @Override
    public void run() {
        tierOnepatrons.clear();
        tierTwopatrons.clear();
        tierThreepatrons.clear();
        Cursor<Patron> patrons = r.db("data").table("patrons").run(connection);
        patrons.forEach(patron -> {
            if (patron.getTier().equalsIgnoreCase("tier1")) {
                tierOnepatrons.add(patron.getUser_id());
            }
            else if (patron.getTier().equalsIgnoreCase("tier2")) {
                tierTwopatrons.add(patron.getUser_id());
            }
            else if (patron.getTier().equalsIgnoreCase("tier3")) {
                tierThreepatrons.add(patron.getUser_id());
            }

        });
        developers.clear();
        moderators.clear();
        translators.clear();
        Cursor<Staff> staff = r.db("data").table("staff").run(connection);
        staff.forEach(staffMember -> {
            if (staffMember.getRole().equalsIgnoreCase("Developer")) developers.add(staffMember.getUser_id());
            else if (staffMember.getRole().equalsIgnoreCase("Moderator")) moderators.add(staffMember.getUser_id());
            else if (staffMember.getRole().equalsIgnoreCase("Translator")) translators.add(staffMember.getUser_id());
        });
        patrons.close();
        staff.close();
    }
}
