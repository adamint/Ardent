package tk.ardentbot.Utils.Updaters;

import tk.ardentbot.Core.Exceptions.BotException;
import tk.ardentbot.Utils.SQL.DatabaseAction;

import java.sql.ResultSet;
import java.sql.SQLException;

import static tk.ardentbot.Main.Ardent.*;

public class PermissionsDaemon implements Runnable {
    @Override
    public void run() {
        try {
            patrons.clear();
            DatabaseAction retrievePatrons = new DatabaseAction("SELECT * FROM Patrons");
            ResultSet set = retrievePatrons.request();
            while (set.next()) {
                patrons.add(set.getString("UserID"));
            }
            retrievePatrons.close();

            developers.clear();
            moderators.clear();
            translators.clear();

            DatabaseAction retrieveStaff = new DatabaseAction("SELECT * FROM Staff");
            ResultSet staff = retrieveStaff.request();
            while (staff.next()) {
                String role = staff.getString("Role");
                String id = staff.getString("UserID");
                if (role.equalsIgnoreCase("Developer")) developers.add(id);
                else if (role.equalsIgnoreCase("Moderator")) moderators.add(id);
                else if (role.equalsIgnoreCase("Translator")) translators.add(id);
            }

            retrieveStaff.close();
        }
        catch (SQLException e) {
            new BotException(e);
        }
    }
}
