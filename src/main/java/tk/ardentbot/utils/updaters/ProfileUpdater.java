package tk.ardentbot.utils.updaters;

import com.rethinkdb.net.Cursor;
import net.dv8tion.jda.core.entities.User;
import tk.ardentbot.main.Ardent;
import tk.ardentbot.utils.rpg.profiles.Badge;
import tk.ardentbot.utils.rpg.profiles.Profile;

import java.time.Instant;
import java.util.HashMap;
import java.util.Iterator;

import static tk.ardentbot.core.executor.BaseCommand.asPojo;
import static tk.ardentbot.rethink.Database.connection;
import static tk.ardentbot.rethink.Database.r;

public class ProfileUpdater implements Runnable {
    public static void updateProfiles() {
        Cursor<HashMap> profiles = r.db("data").table("profiles").run(connection);
        profiles.forEach(hashMap -> {
            Profile profile = asPojo(hashMap, Profile.class);
            if (profile.getUser() == null && !Ardent.testingBot) {
                r.table("profiles").get(profile.user_id).delete().run(connection);
                return;
            }
            for (Iterator<Badge> iterator = profile.getBadges().iterator(); iterator.hasNext(); ) {
                Badge badge = iterator.next();
                if (badge.getExpirationEpochSeconds() < Instant.now().getEpochSecond() && badge.getExpirationEpochSeconds() != 1) {
                    User user = profile.getUser();
                    if (user != null) {
                        user.openPrivateChannel().queue(privateChannel -> {
                            Ardent.shard0.help.sendTranslatedMessage("Your badge with the ID of **" + badge.getName() + "** has " +
                                    "expired" + ".", privateChannel, user);
                            r.db("data").table("badges").filter(row -> row.g("user_id").eq(user.getId()).and(row.g("badge_id").eq(badge
                                    .getId()))).delete().run(connection);
                            iterator.remove();
                        });
                    }
                }
            }
        });
    }

    @Override
    public void run() {
        updateProfiles();
    }
}
