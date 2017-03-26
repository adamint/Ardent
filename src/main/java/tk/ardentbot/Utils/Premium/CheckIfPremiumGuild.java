package tk.ardentbot.Utils.Premium;

import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import tk.ardentbot.Main.Shard;
import tk.ardentbot.Main.ShardManager;
import tk.ardentbot.Utils.Discord.UserUtils;

public class CheckIfPremiumGuild implements Runnable {
    @Override
    public void run() {
        for (Shard shard : ShardManager.getShards()) {
            for (Guild guild : shard.jda.getGuilds()) {
                int premiumCounter = 0;
                for (Member member : guild.getMembers()) {
                    if (UserUtils.hasTierThreePermissions(member.getUser())) premiumCounter++;
                }
                if (premiumCounter == 0) {
                    guild.getPublicChannel().sendMessage("Someone in your guild needs Tier 3 permissions to be able to access me (the " +
                            "premium bot). " +
                            "Pledge $5 a month @ https://patreon.com/ardent to get access!").queue();
                    guild.leave().queue();
                }
            }
        }
    }
}
