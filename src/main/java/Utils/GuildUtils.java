package Utils;

import Backend.Translation.LangFactory;
import Backend.Translation.Language;
import Main.Ardent;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;

import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static Main.Ardent.conn;
import static Main.Ardent.jda;

public class GuildUtils {
    public static ConcurrentHashMap<String, String> guildPrefixes = new ConcurrentHashMap<>();

    public static int getVoiceConnections() {
        int amtConnections = 0;
        for (Guild g : jda.getGuilds()) {
            if (g.getAudioManager().isConnected()) amtConnections++;
        }
        return amtConnections;
    }

    public static void updatePrefix(String prefix, Guild guild) {
        String old = guildPrefixes.get(guild.getId());
        guildPrefixes.replace(guild.getId(), old, prefix);
    }

    public static void addAllPrefixes() throws Exception {
        Statement statement = conn.createStatement();
        ResultSet guilds = statement.executeQuery("SELECT * FROM Guilds");
        ArrayList<String> guildIds = getGuildIds();
        while (guilds.next()) {
            String id = guilds.getString("GuildID");
            if (guildIds.contains(id)) {
                guildPrefixes.put(id, guilds.getString("Prefix"));
            }
        }
        guilds.close();
        statement.close();
    }

    public static ArrayList<String> getGuildIds() {
        ArrayList<String> guildIds = new ArrayList<>();
        jda.getGuilds().forEach(guild -> {
            guildIds.add(guild.getId());
        });
        return guildIds;
    }

    public static String getPrefix(Guild guild) throws Exception {
        if (guild == null) return "/";
        else return guildPrefixes.get(guild.getId());
    }

    public static Map<String, Integer> getLanguageUsages() throws Exception {
        HashMap<String, Integer> languageUses = new HashMap<>();
        ArrayList<Language> languagesUnfiltered = LangFactory.languages;
        ArrayList<Language> languages = languagesUnfiltered.stream().filter(l -> l.getLanguageStatus() == Language.Status.MATURE || l.getLanguageStatus() == Language.Status.MOST).collect(Collectors.toCollection(ArrayList::new));
        for (Language l : languages) languageUses.put(l.getIdentifier(), 0);

        ArrayList<String> guildIds = getGuildIds();

        Statement statement = conn.createStatement();
        ResultSet set = statement.executeQuery("SELECT * FROM Guilds");

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

        set.close();
        statement.close();
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
        if (member.hasPermission(Permission.MANAGE_SERVER) || Ardent.developers.contains(member.getUser().getId()))
            return true;
        else return false;
    }

}
