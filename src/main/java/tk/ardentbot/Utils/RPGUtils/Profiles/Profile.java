package tk.ardentbot.Utils.RPGUtils.Profiles;

import net.dv8tion.jda.core.entities.User;
import tk.ardentbot.Core.LoggingUtils.BotException;
import tk.ardentbot.Main.Ardent;
import tk.ardentbot.Utils.Discord.UserUtils;
import tk.ardentbot.Utils.SQL.DatabaseAction;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class Profile {
    private String userId;
    private double moneyAmount;
    private List<Badge> badges = new ArrayList<>();

    public Profile(User user) throws SQLException {
        this.userId = user.getId();
        DatabaseAction getProfile = new DatabaseAction("SELECT * FROM Profiles WHERE UserID=?").set(userId);
        ResultSet set = getProfile.request();
        if (set.next()) {
            moneyAmount = set.getDouble("Money");
            DatabaseAction retrieveBadges = new DatabaseAction("SELECT * FROM Badges WHERE UserID=?").set(userId);
            ResultSet badgesWithId = retrieveBadges.request();
            while (badgesWithId.next()) {
                badges.add(new Badge(userId, badgesWithId.getString("FeatureName"), badgesWithId.getBoolean("GuildWide"), badgesWithId
                        .getLong("ExpirationTime")));
            }
            retrieveBadges.close();
        }
        else {
            moneyAmount = 25;
            new DatabaseAction("INSERT INTO Profiles VALUES (?,?)").set(userId).set(25).update();
        }
        getProfile.close();
    }

    public Profile(String userId, double moneyAmount, List<Badge> badges) {
        this.userId = userId;
        this.moneyAmount = moneyAmount;
        this.badges = badges;
    }

    public static Profile get(User user) {
        String id = user.getId();
        if (Ardent.userProfiles.containsKey(id)) {
            return Ardent.userProfiles.get(id);
        }
        else {
            Profile profile = null;
            try {
                profile = new Profile(user);
            }
            catch (SQLException e) {
                e.printStackTrace();
            }
            Ardent.userProfiles.put(id, profile);
            return profile;
        }
    }

    public static void startProfileChecking() {
        Ardent.globalExecutorService.scheduleWithFixedDelay(() -> {
            Ardent.userProfiles.values().forEach(profile -> {
                try {
                    new DatabaseAction("UPDATE Profiles SET Money=? WHERE UserID=?").set(profile.moneyAmount).set(profile.userId).update();
                    new DatabaseAction("DELETE FROM Badges WHERE UserID=?").set(profile.userId).update();
                    for (Badge badge : profile.getBadges()) {
                        new DatabaseAction("INSERT INTO Badges VALUES (?,?,?,?)").set(profile.userId).set(badge.getId()).set(badge
                                .isGuildWide())
                                .set(Timestamp.from(Instant.ofEpochSecond(badge.getExpirationEpochSeconds()))).update();
                    }
                }
                catch (SQLException e) {
                    new BotException(e);
                }
            });
        }, 15, 60, TimeUnit.SECONDS);
    }

    public User getUser() {
        return UserUtils.getUserById(userId);
    }

    public double getMoneyAmount() {
        return moneyAmount;
    }

    public List<Badge> getBadges() {
        return badges;
    }

    public Profile addMoney(double amount) {
        moneyAmount += amount;
        return this;
    }

    public void removeMoney(double amount) {
        moneyAmount -= amount;
    }
}
