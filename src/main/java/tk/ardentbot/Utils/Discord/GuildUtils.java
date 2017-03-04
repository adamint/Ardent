package tk.ardentbot.Utils.Discord;

import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import tk.ardentbot.Backend.Translation.LangFactory;
import tk.ardentbot.Backend.Translation.Language;
import tk.ardentbot.Main.Ardent;
import tk.ardentbot.Utils.SQL.DatabaseAction;
import tk.ardentbot.Utils.UsageUtils;

import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import static tk.ardentbot.Main.Ardent.*;

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
        ArrayList<Language> languagesUnfiltered = LangFactory.languages;
        ArrayList<Language> languages = languagesUnfiltered.stream().filter(l -> l.getLanguageStatus() == Language.Status.MATURE || l.getLanguageStatus() == Language.Status.MOST).collect(Collectors.toCollection(ArrayList::new));
        for (Language l : languages) languageUses.put(l.getIdentifier(), 0);

        ArrayList<String> guildIds = getGuildIds();

        DatabaseAction languageUsages = new DatabaseAction("SELECT * FROM Guilds");
        ResultSet set = languageUsages.request();
        while (set.next()) {
            String id = set.getString("GuildID");
            if (guildIds.contains(id)) {
                String language = set.getString("Language");
                Language temp = LangFactory.getLanguage(language);
                if (temp != null && languageUses.containsKey(temp.getIdentifier())) {
                    int oldValue = languageUses.get(temp.getIdentifier());
                    languageUses.replace(temp.getIdentifier(), oldValue, (oldValue + 1));
                }
            }
        }
        languageUsages.close();
        return UsageUtils.sortByValue(languageUses);
    }

    public static Language getLanguage(Guild guild) throws Exception {
        if (guild == null) return LangFactory.english;
        Statement statement = conn.createStatement();
        ResultSet set = statement.executeQuery("SELECT * FROM Guilds WHERE GuildID='" + guild.getId() + "'");
        if (set.next()) {
            String language = set.getString("Language");
            set.close();
            statement.close();
            if (language.equalsIgnoreCase("english")) return LangFactory.english;
            else if (language.equalsIgnoreCase("french")) return LangFactory.french;
            else if (language.equalsIgnoreCase("turkish")) return LangFactory.turkish;
            else if (language.equalsIgnoreCase("croatian")) return LangFactory.croatian;
            else if (language.equalsIgnoreCase("romanian")) return LangFactory.romanian;
            else if (language.equalsIgnoreCase("portugese")) return LangFactory.portugese;
            else if (language.equalsIgnoreCase("german")) return LangFactory.german;
            else if (language.equalsIgnoreCase("cyrillicserbian")) return LangFactory.cyrillicserbian;
            else if (language.equalsIgnoreCase("spanish")) return LangFactory.spanish;
            else if (language.equalsIgnoreCase("dutch")) return LangFactory.dutch;
            else if (language.equalsIgnoreCase("polish")) return LangFactory.polish;
            else if (language.equalsIgnoreCase("emoji")) return LangFactory.emoji;
            else return LangFactory.english;
        }
        else return LangFactory.english;
    }

    public static boolean hasManageServerPermission(Member member) {
        return member.hasPermission(Permission.MANAGE_SERVER) || Ardent.developers.contains(member.getUser().getId());
    }

}
