package tk.ardentbot.Utils.RPGUtils.Profiles;

import com.rethinkdb.net.Cursor;
import lombok.Getter;
import net.dv8tion.jda.core.entities.User;
import tk.ardentbot.Main.Ardent;
import tk.ardentbot.Rethink.Models.OneTimeBadgeModel;
import tk.ardentbot.Utils.Discord.UserUtils;
import tk.ardentbot.Utils.RPGUtils.BadgesList;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static tk.ardentbot.Core.CommandExecution.BaseCommand.asPojo;
import static tk.ardentbot.Main.Ardent.globalGson;
import static tk.ardentbot.Rethink.Database.connection;
import static tk.ardentbot.Rethink.Database.r;

public class Profile {
    private static CopyOnWriteArrayList<Profile> cachedProfiles = new CopyOnWriteArrayList<>();

    private String user_id;
    private double money;
    @Getter
    private double stocksOwned;
    private List<Badge> badges = new ArrayList<>();

    private Profile(User user) {
        this.user_id = user.getId();
        Cursor<HashMap> profiles = r.db("data").table("profiles").filter(row -> row.g("user_id").eq(user_id)).run
                (connection);
        if (profiles.hasNext()) {
            Profile profile = asPojo(profiles.next(), Profile.class);
            Cursor<HashMap> rBadges = r.db("data").table("badges").filter(row -> row.g("user_id").eq(user_id))
                    .run(connection);
            rBadges.forEach(b -> badges.add(asPojo(b, Badge.class)));
            this.money = profile.getMoney();
            this.stocksOwned = profile.getStocksOwned();
        }
        else {
            this.stocksOwned = 0;
            this.money = 25;
            r.db("data").table("profiles").insert(r.json(globalGson.toJson(this))).run(connection);
        }
        cachedProfiles.add(this);
        Ardent.profileUpdateExecutorService.schedule(() -> {
            r.db("data").table("profiles").filter(row -> row.g("user_id").eq(user_id)).update(r.json(globalGson.toJson(Profile.this)))
                    .run(connection);
            cachedProfiles.remove(Profile.this);
        }, 5, TimeUnit.MINUTES);
    }

    public Profile(String user_id, double money, List<Badge> badges) {
        this.user_id = user_id;
        this.money = money;
        this.badges = badges;
    }

    public static Profile get(User user) {
        List<Profile> cached = cachedProfiles.stream().filter(profile -> profile.user_id.equalsIgnoreCase(user.getId())).collect
                (Collectors.toList());
        if (cached.size() > 0) return cached.get(0);
        else return new Profile(user);
    }

    public User getUser() {
        return UserUtils.getUserById(user_id);
    }

    public double getMoney() {
        return money;
    }

    public boolean addBadge(BadgesList badge) {
        boolean succeeded = true;
        if (badge.isOneTime()) {
            if (badges.contains(badge)) {
                succeeded = false;
            }
            else {
                r.db("data").table("one_time").insert(r.json(globalGson.toJson(new OneTimeBadgeModel(user_id, badge.getId())))).run
                        (connection);
            }
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
            Badge badgeToAdd = new Badge(user_id, badge.getId(), badge.getName(), badge.isGuildWide(), badge.isOneTime(), instant
                    .getEpochSecond());
            badges.add(badgeToAdd);
            return true;
        }
        else return false;
    }

    public List<Badge> getBadges() {
        return badges;
    }

    public void addMoney(double amount) {
        money += amount;
    }

    public void removeMoney(double amount) {
        money -= amount;
    }

    public void addStock(double amountToAdd) {
        stocksOwned += amountToAdd;
    }

    public void removeStock(double amountToRemove) {
        stocksOwned -= amountToRemove;
    }
}
