package tk.ardentbot.Utils.Discord;

import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.User;
import tk.ardentbot.Main.Ardent;

import static tk.ardentbot.Main.Ardent.*;

public class UserUtils {
    public static boolean isPatron(User user) {
        String id = user.getId();
        return developers.contains(id) || moderators.contains(id) || translators.contains(id) || patrons.contains(id);
    }

    public static boolean hasManageServerOrStaff(Member member) {
        return member.hasPermission(Permission.MANAGE_SERVER) || Ardent.developers.contains(member.getUser().getId())
                || Ardent.moderators.contains(member.getUser().getId());
    }
}
