package tk.ardentbot.Utils.Discord;

import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.User;
import tk.ardentbot.Main.Ardent;
import tk.ardentbot.Main.Shard;
import tk.ardentbot.Main.ShardManager;
import tk.ardentbot.Utils.Profiles.Profile;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.stream.Collectors;

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
        ArrayList<String> names = ids.stream().map(id ->
                getUserById(id).getName()).collect(Collectors.toCollection(ArrayList::new));
        return names;
    }

    public static User getUserById(String id) {
        for (Shard shard : ShardManager.getShards()) {
            User user = shard.jda.getUserById(id);
            if (user != null) return user;
        }

        return null;
    }

    public static void addMoney(Shard shard, User user, double amount) throws SQLException {
        if (shard.userProfiles.containsKey(user.getId())) {
            Profile profile = shard.userProfiles.get(user.getId());
            profile.addMoney(amount);
        }
        else {
            shard.userProfiles.put(user.getId(), new Profile(user));
        }
    }

}
