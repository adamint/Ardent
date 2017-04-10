package tk.ardentbot.Utils.Updaters;

import com.rethinkdb.net.Cursor;
import net.dv8tion.jda.core.entities.User;
import tk.ardentbot.Main.Ardent;
import tk.ardentbot.Utils.RPGUtils.Profiles.Badge;
import tk.ardentbot.Utils.RPGUtils.Profiles.Profile;

import java.time.Instant;
import java.util.HashMap;
import java.util.Iterator;

import static tk.ardentbot.Core.CommandExecution.BaseCommand.asPojo;
import static tk.ardentbot.Rethink.Database.connection;
import static tk.ardentbot.Rethink.Database.r;

public class ProfileUpdater implements Runnable {
    public static void updateProfiles() {
        Cursor<HashMap> profiles = r.db("data").table("profiles").run(connection);
        profiles.forEach(hashMap -> {
            Profile profile = asPojo(hashMap, Profile.class);
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
