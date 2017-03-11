package tk.ardentbot.Utils.Discord;

import net.dv8tion.jda.core.entities.User;

import static tk.ardentbot.Main.Ardent.*;

public class UserUtils {
    public static boolean isPatron(User user) {
        String id = user.getId();
        return developers.contains(id) || moderators.contains(id) || translators.contains(id) || patrons.contains(id);
    }
}
