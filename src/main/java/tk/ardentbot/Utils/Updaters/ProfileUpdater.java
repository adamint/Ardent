package tk.ardentbot.Utils.Updaters;

import net.dv8tion.jda.core.entities.User;
import tk.ardentbot.Core.Misc.LoggingUtils.BotException;
import tk.ardentbot.Main.Ardent;
import tk.ardentbot.Utils.Discord.UserUtils;
import tk.ardentbot.Utils.RPGUtils.Profiles.Badge;
import tk.ardentbot.Utils.SQL.DatabaseAction;

import java.sql.SQLException;
import java.time.Instant;
import java.util.Iterator;

public class ProfileUpdater implements Runnable {
    @Override
    public void run() {
        Ardent.userProfiles.forEach((id, profile) -> {
            for (Iterator<Badge> iterator = profile.getBadges().iterator(); iterator.hasNext(); ) {
                Badge badge = iterator.next();
                if (badge.getExpirationEpochSeconds() < Instant.now().getEpochSecond() && badge.getExpirationEpochSeconds() != 1) {
                    User user = UserUtils.getUserById(id);
                    if (user != null) {
                        user.openPrivateChannel().queue(privateChannel -> {
                            Ardent.shard0.help.sendTranslatedMessage("Your badge with the ID of **" + badge.getName() + "** has " +
                                            "expired" +
                                            ".", privateChannel,
                                    user);
                            iterator.remove();
                            try {
                                new DatabaseAction("DELETE FROM Badges WHERE BadgeID=? AND UserID=?").set(badge.getId()).set(id)
                                        .update();
                            }
                            catch (SQLException e) {
                                new BotException(e);
                            }
                        });
                    }
                }
            }
            try {
                new DatabaseAction("UPDATE Profiles SET Money=? WHERE UserID=?").set(profile.getMoneyAmount()).set(id).update();
            }
            catch (SQLException e) {
                new BotException(e);
            }
        });
    }
}
