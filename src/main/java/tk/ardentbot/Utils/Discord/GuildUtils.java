package tk.ardentbot.Utils.Discord;

import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import tk.ardentbot.Backend.Translation.Language;
import tk.ardentbot.Utils.UsageUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static tk.ardentbot.Main.Config.*;

public class GuildUtils {
    public static void updatePrefix(String prefix, Guild guild) {
        botPrefixData.set(guild, prefix);
    }

    private static ArrayList<String> getGuildIds() {
        ArrayList<String> guildIds = new ArrayList<>();
        jda.getGuilds().forEach(guild -> {
            guildIds.add(guild.getId());
        });
        return guildIds;
    }

    public static String getPrefix(Guild guild) throws Exception {
        if (guild == null) return "/";
        else return botPrefixData.getPrefix(guild);
    }

    public static Map<String, Integer> getLanguageUsages() throws Exception {
        HashMap<String, Integer> languageUses = new HashMap<>();

        // TODO: 3/4/2017 fix this 
        
        return UsageUtils.sortByValue(languageUses);
    }

    public static Language getLanguage(Guild guild) throws Exception {
        return botLanguageData.getLanguage(guild);
    }

    public static boolean hasManageServerPermission(Member member) {
        return member.hasPermission(Permission.MANAGE_SERVER) || developers.contains(member.getUser().getId());
    }

}
