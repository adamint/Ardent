package tk.ardentbot.Utils.Profiles;

import net.dv8tion.jda.core.entities.User;
import tk.ardentbot.Utils.Discord.UserUtils;
import tk.ardentbot.Utils.SQL.DatabaseAction;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

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
