package tk.ardentbot.Utils.Profiles;

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
            moneyAmount = 0;
            new DatabaseAction("INSERT INTO Profiles VALUES (?,?)").set(userId).set(25).update();
        }
        getProfile.close();

        Ardent.globalExecutorService.scheduleWithFixedDelay(() -> {
            try {
                new DatabaseAction("UPDATE Profiles SET Money=? WHERE UserID=?").set(moneyAmount).set(userId).update();
                DatabaseAction getBadges = new DatabaseAction("SELECT * FROM Badges WHERE UserID=?").set(userId);
                ResultSet badgesSet = getBadges.request();
                for (Badge badge : badges) {
                    boolean found = false;
                    while (badgesSet.next()) {
                        String featureName = badgesSet.getString("FeatureName");
                        boolean guildWide = badgesSet.getBoolean("GuildWide");
                        long epochSeconds = badgesSet.getTimestamp("ExpirationTime").toInstant().getEpochSecond();
                        if ((badge.isGuildWide() == guildWide) && (featureName.equalsIgnoreCase(badge.getId())) && (badge
                                .getExpirationEpochSeconds() == epochSeconds))
                        {
                            found = true;
                        }
                    }
                    if (!found) {
                        new DatabaseAction("INSERT INTO Badges VALUES (?,?,?,?)").set(userId).set(badge.getId()).set(badge.isGuildWide())
                                .set(Timestamp.from(Instant.ofEpochSecond(badge.getExpirationEpochSeconds()))).update();
                    }
                }
                getBadges.close();
            }
            catch (SQLException e) {
                new BotException(e);
            }
        }, 15, 60, TimeUnit.SECONDS);
    }

    public Profile(String userId, double moneyAmount, List<Badge> badges) {
        this.userId = userId;
        this.moneyAmount = moneyAmount;
        this.badges = badges;
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

    public void addMoney(double amount) {
        moneyAmount += amount;
    }

    public void removeMoney(double amount) {
        moneyAmount -= amount;
    }
}
