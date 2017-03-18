package tk.ardentbot.Utils.Discord;

import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import tk.ardentbot.Core.Translation.LangFactory;
import tk.ardentbot.Core.Translation.Language;
import tk.ardentbot.Main.Ardent;
import tk.ardentbot.Main.Shard;
import tk.ardentbot.Utils.SQL.DatabaseAction;
import tk.ardentbot.Utils.UsageUtils;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static tk.ardentbot.Main.ShardManager.getShards;

public class GuildUtils {
    public static Shard getShard(int id) {
        for (Shard shard : getShards()) {
            if (shard.getId() == id) {
                return shard;
            }
        }
        return null;
    }

    public static Shard getShard(Guild guild) {
        long bitwise = Long.parseLong(guild.getId()) >> 22;
        long modulus = bitwise % Ardent.shardCount;
        int numbered = (int) modulus;
        System.out.println(guild.getId() + " | " + bitwise + " | " + numbered);
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
        if (guild == null) return "/";
        else {
            String prefix;
            DatabaseAction action = new DatabaseAction("SELECT * FROM Guilds WHERE GuildID=?").set(guild.getId());
            ResultSet set = action.request();
            if (set.next()) {
                prefix = set.getString("Prefix");
            }
            else {
                prefix = "/";
                new DatabaseAction("INSERT INTO Guilds VALUES (?,?,?)").set(guild.getId()).set("english").set("/")
                        .update();
            }
            action.close();
            return prefix;
        }
    }

    public static Map<String, Integer> getLanguageUsages() throws Exception {
        HashMap<String, Integer> languageUses = new HashMap<>();

        // TODO: 3/4/2017 fix this 

        return UsageUtils.sortByValue(languageUses);
    }

    public static Language getLanguage(Guild guild) throws Exception {
        if (guild == null) return LangFactory.english;
        else {
            Language language;
            DatabaseAction action = new DatabaseAction("SELECT * FROM Guilds WHERE GuildID=?").set(guild.getId());
            ResultSet set = action.request();
            if (set.next()) {
                language = LangFactory.getLanguage(set.getString("Language"));
            }
            else {
                language = LangFactory.english;
                new DatabaseAction("INSERT INTO Guilds VALUES (?,?,?)").set(guild.getId()).set("english").set("/")
                        .update();
            }
            if (language == null) language = LangFactory.english;
            action.close();
            return language;
        }
    }

    public static boolean hasManageServerPermission(Member member) {
        return member.hasPermission(Permission.MANAGE_SERVER) || Ardent.developers.contains(member.getUser().getId());
    }

}
