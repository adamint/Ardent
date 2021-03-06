package tk.ardentbot.utils.rpg;

import lombok.Getter;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.User;
import org.apache.commons.collections.ListUtils;
import tk.ardentbot.utils.discord.UserUtils;
import tk.ardentbot.utils.models.RestrictedUser;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static tk.ardentbot.main.Ardent.globalExecutorService;

public class EntityGuild {
    private static ArrayList<EntityGuild> cache = new ArrayList<>();
    private boolean ownerTierThree;
    @Getter
    private List<String> tierThreeMembers;
    @Getter
    private List<String> tierTwoMembers;
    @Getter
    private List<String> tierOneMembers;
    @Getter
    private ArrayList<RestrictedUser> restrictedUsers = new ArrayList<>();
    @Getter
    private String id;

    private EntityGuild(boolean ownerTierThree, List<String> tierThreeMembers, List<String> tierTwoMembers, List<String>
            tierOneMembers, Guild guild) {
        this.ownerTierThree = ownerTierThree;
        this.tierThreeMembers = tierThreeMembers;
        this.tierTwoMembers = tierTwoMembers;
        this.tierOneMembers = tierOneMembers;
        this.id = guild.getId();
    }

    public static EntityGuild get(Guild guild) {
        List<EntityGuild> retrieved = cache.stream().filter(entityGuild -> entityGuild.getId().equalsIgnoreCase(guild
                .getId())).collect(Collectors.toList());
        if (retrieved.size() == 1) return retrieved.get(0);
        EntityGuild entityGuild = new EntityGuild(false, new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), guild);
        cache.add(entityGuild);
        globalExecutorService.scheduleAtFixedRate(() -> {
            boolean isOwnerTierThree = UserUtils.hasTierThreePermissions(guild.getOwner().getUser()) || UserUtils.isStaff(guild
                    .getOwner()
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
            entityGuild.setTiers(isOwnerTierThree, tierOne, tierTwo, tierThree);
        }, 0, 1, TimeUnit.MINUTES);
        return entityGuild;
    }

    public void setTiers(boolean isOwnerTierThree, List<String> tierOne, List<String> tierTwo, List<String> tierThree) {
        this.ownerTierThree = isOwnerTierThree;
        this.tierOneMembers = tierOne;
        this.tierTwoMembers = tierTwo;
        this.tierThreeMembers = tierThree;
    }

    public boolean isPremium() {
        return ownerTierThree || tierThreeMembers.size() >= 2 || tierTwoMembers.size() >= 3 || tierOneMembers.size() >= 5;
    }

    public List<User> getPatrons() {
        return UserUtils.getUsersById(ListUtils.union(ListUtils.union(tierOneMembers, tierTwoMembers), tierThreeMembers));
    }

    public boolean isRestricted(User user) {
        for (RestrictedUser restrictedUser : restrictedUsers) {
            if (restrictedUser.getUserId().equalsIgnoreCase(user.getId())) return true;
        }
        return false;
    }

    public void addRestricted(RestrictedUser restrictedUser) {
        restrictedUsers.add(restrictedUser);
    }

    public void removeRestricted(String id) {
        restrictedUsers.removeIf(restrictedUser -> restrictedUser.getUserId().equalsIgnoreCase(id));
    }
}
