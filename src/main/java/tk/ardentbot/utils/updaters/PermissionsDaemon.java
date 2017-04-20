package tk.ardentbot.utils.updaters;

import com.rethinkdb.net.Cursor;
import tk.ardentbot.rethink.models.Patron;
import tk.ardentbot.rethink.models.Staff;

import java.util.HashMap;

import static tk.ardentbot.Core.executor.BaseCommand.asPojo;
import static tk.ardentbot.main.Ardent.*;
import static tk.ardentbot.rethink.Database.connection;
import static tk.ardentbot.rethink.Database.r;

public class PermissionsDaemon implements Runnable {
    @Override
    public void run() {
        Cursor<HashMap> patrons = r.db("data").table("patrons").run(connection);
        patrons.forEach(hashMap -> {
            Patron patron = asPojo(hashMap, Patron.class);
            if (patron.getTier().equalsIgnoreCase("tier1")) {
                if (!tierOnepatrons.contains(patron.getUser_id())) tierOnepatrons.add(patron.getUser_id());
            }
            else if (patron.getTier().equalsIgnoreCase("tier2")) {

                if (!tierTwopatrons.contains(patron.getUser_id())) tierTwopatrons.add(patron.getUser_id());
            }
            else if (patron.getTier().equalsIgnoreCase("tier3")) {
                if (!tierThreepatrons.contains(patron.getUser_id())) tierThreepatrons.add(patron.getUser_id());
            }
        });
        Cursor<HashMap> staff = r.db("data").table("staff").run(connection);
        staff.forEach(hashMap -> {
            Staff staffMember = asPojo(hashMap, Staff.class);
            if (staffMember.getRole().equalsIgnoreCase("Developer")) {
                if (!developers.contains(staffMember.getId())) developers.add(staffMember.getId());
            }
            else if (staffMember.getRole().equalsIgnoreCase("Moderator")) {
                if (!moderators.contains(staffMember.getId())) moderators.add(staffMember.getId());
            }
            else if (staffMember.getRole().equalsIgnoreCase("Translator")) {
                if (translators.contains(staffMember.getId())) translators.add(staffMember.getId());
            }
        });
        patrons.close();
        staff.close();
    }
}
