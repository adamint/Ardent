package tk.ardentbot.Utils.Premium;

import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.User;
import org.apache.commons.collections.ListUtils;
import tk.ardentbot.Utils.Discord.UserUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.stream.Collectors;

public class GuildPatronStatus {
    private static final Timer removeCaches = new Timer();
    private static ArrayList<GuildPatronStatus> cache = new ArrayList<>();

    private boolean ownerTierThree;
    private List<String> tierThreeMembers;
    private List<String> tierTwoMembers;
    private List<String> tierOneMembers;
    private String id;

    public GuildPatronStatus(boolean ownerTierThree, List<String> tierThreeMembers, List<String> tierTwoMembers, List<String>
            tierOneMembers, Guild guild) {
        this.ownerTierThree = ownerTierThree;
        this.tierThreeMembers = tierThreeMembers;
        this.tierTwoMembers = tierTwoMembers;
        this.tierOneMembers = tierOneMembers;
        this.id = guild.getId();
    }

    public static GuildPatronStatus getGuildPatronStatus(Guild guild) {
        List<GuildPatronStatus> retrieved = cache.stream().filter(guildPatronStatus -> guildPatronStatus.getId().equalsIgnoreCase(guild
                .getId())).collect(Collectors.toList());
        if (retrieved.size() == 1) return retrieved.get(0);

        boolean isOwnerTierThree = UserUtils.hasTierThreePermissions(guild.getOwner().getUser()) || UserUtils.isStaff(guild.getOwner()
                .getUser());
        ArrayList<String> tierThree = new ArrayList<>();
        ArrayList<String> tierTwo = new ArrayList<>();
        ArrayList<String> tierOne = new ArrayList<>();
        guild.getMembers().forEach(member -> {
            User user = member.getUser();
            String id = user.getId();
            if (UserUtils.hasTierThreePermissions(user)) tierThree.add(id);
            else if (UserUtils.hasTierTwoPermissions(user)) tierTwo.add(id);
            else if (UserUtils.hasTierOnePermissions(user)) tierOne.add(id);
        });

        GuildPatronStatus guildPatronStatus = new GuildPatronStatus(isOwnerTierThree, tierThree, tierTwo, tierOne, guild);

        removeCaches.schedule(new TimerTask() {
            @Override
            public void run() {
                cache.remove(guildPatronStatus);
            }
        }, 15000);

        return guildPatronStatus;
    }

    private String getId() {
        return id;
    }

    public boolean isPremium() {
        return ownerTierThree || tierThreeMembers.size() >= 2 || tierTwoMembers.size() >= 3 || tierOneMembers.size() >= 5;
    }

    public List<User> getTierThree() {
        return UserUtils.getUsersById(tierThreeMembers);
    }

    public List<User> getTierTwo() {
        return UserUtils.getUsersById(tierTwoMembers);
    }

    public List<User> getTierOne() {
        return UserUtils.getUsersById(tierOneMembers);
    }

    public List<User> getPatrons() {
        return UserUtils.getUsersById(ListUtils.union(ListUtils.union(tierOneMembers, tierTwoMembers), tierThreeMembers));
    }
}
