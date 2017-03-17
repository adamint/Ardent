package tk.ardentbot.Utils.Discord;

import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.User;
import tk.ardentbot.Main.Ardent;

import java.util.ArrayList;

import static tk.ardentbot.Main.Ardent.*;

public class UserUtils {
    public static boolean hasTierOnePermissions(User user) {
        String id = user.getId();
        return developers.contains(id) || moderators.contains(id) || translators.contains(id) || tierOnepatrons
                .contains(id)
                || tierTwopatrons.contains(id) || tierThreepatrons.contains(id);
    }

    public static boolean hasTierTwoPermissions(User user) {
        String id = user.getId();
        return developers.contains(id) || moderators.contains(id) || translators.contains(id)
                || tierTwopatrons.contains(id) || tierThreepatrons.contains(id);
    }

    public static boolean hasTierThreePermissions(User user) {
        String id = user.getId();
        return developers.contains(id) || moderators.contains(id) || translators.contains(id) || tierThreepatrons
                .contains(id);
    }

    public static boolean hasManageServerOrStaff(Member member) {
        return member.hasPermission(Permission.MANAGE_SERVER) || Ardent.developers.contains(member.getUser().getId())
                || Ardent.moderators.contains(member.getUser().getId());
    }

    public static ArrayList<String> getNamesById(ArrayList<String> ids) {
        ArrayList<String> names = new ArrayList<>();
        for (String id : ids) names.add(ardent.jda.getUserById(id).getName());
        return names;
    }

}
