package tk.ardentbot.Utils.Discord;

import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import tk.ardentbot.Core.Translation.LangFactory;
import tk.ardentbot.Core.Translation.Language;
import tk.ardentbot.Main.Ardent;
import tk.ardentbot.Main.Shard;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static tk.ardentbot.Main.ShardManager.getShards;

public class GuildUtils {
    public static Guild getGuild(String id) {
        for (Shard shard : getShards()) {
            Guild temp = shard.jda.getGuildById(id);
            if (temp != null) return temp;
        }
        return null;
    }

    public static Shard getShard(int id) {
        for (Shard shard : getShards()) {
            if (shard.getId() == id) {
                return shard;
            }
        }
        return null;
    }

    public static Shard getShard(Guild guild) {
        if (guild == null) return null;
        long bitwise = Long.parseLong(guild.getId()) >> 22;
        long modulus = bitwise % Ardent.shardCount;
        int numbered = (int) modulus;
        return getShard(numbered);
    }

    public static void updatePrefix(String prefix, Guild guild) {
        getShard(guild).botPrefixData.set(guild, prefix);
    }

    private static ArrayList<String> getGuildIds() {
        ArrayList<String> guildIds = new ArrayList<>();
        for (Shard shard : getShards()) {
            shard.jda.getGuilds().forEach(guild -> guildIds.add(guild.getId()));
        }
        return guildIds;
    }

    public static String getPrefix(Guild guild) throws Exception {
        String prefix = getShard(guild).botPrefixData.getPrefix(guild);
        if (prefix != null) return prefix;
        else return "/";
    }

    public static Map<String, Integer> getLanguageUsages() throws Exception {
        HashMap<String, Integer> languageUses = new HashMap<>();

        // TODO: 3/4/2017 fix this 
        // TODO: 3/19/2017 still waiting 

        return UsageUtils.sortByValue(languageUses);
    }

    public static Language getLanguage(Guild guild) throws Exception {
        try {
            Language language = getShard(guild).botLanguageData.getLanguage(guild);
            if (language != null) return language;
            else return LangFactory.english;
        }
        catch (Exception ex) {
            try {
                getShard(guild).botLanguageData.set(guild, "english");
            }
            catch (Exception e) {
                return LangFactory.english;
            }
            return LangFactory.english;
        }
    }

    public static boolean hasManageServerPermission(Member member) {
        return member.hasPermission(Permission.MANAGE_SERVER) || Ardent.developers.contains(member.getUser().getId());
    }

}
