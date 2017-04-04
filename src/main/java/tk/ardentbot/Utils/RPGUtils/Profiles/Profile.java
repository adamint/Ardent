package tk.ardentbot.Utils.RPGUtils.Profiles;

import lombok.Getter;
import net.dv8tion.jda.core.entities.User;
import tk.ardentbot.Core.Misc.LoggingUtils.BotException;
import tk.ardentbot.Main.Ardent;
import tk.ardentbot.Utils.Discord.UserUtils;
import tk.ardentbot.Utils.RPGUtils.BadgesList;
import tk.ardentbot.Utils.SQL.DatabaseAction;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class Profile {
    private String userId;
    private double moneyAmount;
    @Getter
    private double stocksOwned;
    private List<Badge> badges = new ArrayList<>();

    private Profile(User user) throws SQLException {
        Ardent.profileUpdateExecutorService.execute(() -> {
            try {
                this.userId = user.getId();
                DatabaseAction getProfile = new DatabaseAction("SELECT * FROM Profiles WHERE UserID=?").set(userId);
                ResultSet set = getProfile.request();
                if (set.next()) {
                    moneyAmount = set.getDouble("Money");
                    DatabaseAction retrieveBadges = new DatabaseAction("SELECT * FROM Badges WHERE UserID=?").set(userId);
                    ResultSet badgesWithId = retrieveBadges.request();
                    while (badgesWithId.next()) {
                        badges.add(new Badge(userId, badgesWithId.getString("BadgeID"), badgesWithId.getString("FeatureName"), badgesWithId
                                .getBoolean("GuildWide"), badgesWithId.getBoolean("OneTime"), badgesWithId
                                .getTimestamp("ExpirationTime").toInstant().getEpochSecond()));
                    }
                    retrieveBadges.close();
                }
                else {
                    moneyAmount = 25;
                    new DatabaseAction("INSERT INTO Profiles VALUES (?,?)").set(userId).set(25).update();
                }
                getProfile.close();
            }
            catch (SQLException e) {
                new BotException(e);
            }
        });
    }

    public Profile(String userId, double moneyAmount, List<Badge> badges) {
        this.userId = userId;
        this.moneyAmount = moneyAmount;
        this.badges = badges;
    }

    public static Profile get(User user) {
        Profile toReturn;
        String id = user.getId();
        if (Ardent.userProfiles.containsKey(id)) {
            toReturn = Ardent.userProfiles.get(id);
        }
        else {
            Profile profile = null;
            try {
                profile = new Profile(user);
                Ardent.userProfiles.put(id, profile);
            }
            catch (SQLException e) {
                e.printStackTrace();
            }
            toReturn = profile;
        }
        return toReturn;
    }

    public User getUser() {
        return UserUtils.getUserById(userId);
    }

    public double getMoneyAmount() {
        return moneyAmount;
    }

    public boolean addBadge(BadgesList badge) throws SQLException {
        boolean succeeded = true;
        if (badge.isOneTime()) {
            DatabaseAction containsBadge = new DatabaseAction("SELECT * FROM OneTime WHERE UserID=? AND BadgeID=?").set(userId).set(badge
                    .getId());
            ResultSet set = containsBadge.request();
            if (set.next()) {
                succeeded = false;
            }
            else {
                new DatabaseAction("INSERT INTO OneTime VALUES (?,?)").set(userId).set(badge.getId()).update();
            }
            containsBadge.close();
        }
        if (succeeded) {
            int dayDuration = badge.getDayDuration();
            Instant instant;
            if (dayDuration == 0) {
                instant = Instant.ofEpochSecond(1);
            }
            else {
                instant = Instant.now().plusSeconds((dayDuration * 24 * 60 * 60));
            }
            badges.add(new Badge(userId, badge.getId(), badge.getName(), badge.isGuildWide(), badge.isOneTime(), instant.getEpochSecond()));
            new DatabaseAction("INSERT INTO Badges VALUES (?,?,?,?,?,?)").set(userId).set(badge.getId()).set(badge.getName())
                    .set(badge.isGuildWide()).set(badge.isOneTime()).set(Timestamp.from(instant)).update();
            return true;
        }
        else return false;
    }

    public List<Badge> getBadges() {
        return badges;
    }

    public void addMoney(double amount) {
        moneyAmount += amount;
    }

    public void removeMoney(double amount) {
        moneyAmount -= amount;
    }

    public void addStock(double amountToAdd) {
        stocksOwned += amountToAdd;
    }

    public void removeStock(double amountToRemove) {
        stocksOwned -= amountToRemove;
    }
}
