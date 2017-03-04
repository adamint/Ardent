package tk.ardentbot.Utils;

import tk.ardentbot.Backend.Commands.Command;
import tk.ardentbot.Commands.BotInfo.Status;
import tk.ardentbot.Main.Ardent;
import net.dv8tion.jda.core.entities.Guild;
import org.eclipse.jetty.util.ConcurrentArrayQueue;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static tk.ardentbot.Main.Ardent.jda;

public class UsageUtils {
    public static ArrayList<Command> orderByUsageDesc() {
        ConcurrentArrayQueue<Command> unsorted = Ardent.factory.getCommands();
        ArrayList<Command> commands = new ArrayList<>();
        for (Command c : unsorted) {
            if (!c.getCommandIdentifier().equalsIgnoreCase("patreon") && !c.getCommandIdentifier().equalsIgnoreCase("translateforardent") && !c.getCommandIdentifier().equalsIgnoreCase("tweet") && !c.getCommandIdentifier().equalsIgnoreCase("eval") && !c.getCommandIdentifier().equalsIgnoreCase("addenglishbase") && !c.getCommandIdentifier().equalsIgnoreCase("help") && !c.getCommandIdentifier().equalsIgnoreCase("admin")) {
                commands.add(c);
            }
        }
        Collections.sort(commands, SORT_BY_USAGE);
        return commands;
    }

    private static Comparator<Command> SORT_BY_USAGE = (o1, o2) -> {
        if (o1.getBotCommand().usages < o2.getBotCommand().usages) return 1;
        else if (Objects.equals(o1.getBotCommand().usages, o2.getBotCommand().usages)) return 0;
        else return -1;
    };

    public static boolean isGuildFirstInCommands(Guild guild) {
        ConcurrentHashMap<String, Integer> commandsByGuild = Status.commandsByGuild;
        final boolean[] first = {true};
        int guildCommands = commandsByGuild.get(guild.getId());
        commandsByGuild.forEach((key, value) -> {
            if (guildCommands < value) first[0] = false;
        });
        return first[0];
    }

    public static <K, V extends Comparable<? super V>> Map<K, V> sortByValue(Map<K, V> map) {
        return map.entrySet()
                .stream()
                .sorted(Map.Entry.comparingByValue(Collections.reverseOrder()))
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1,
                        LinkedHashMap::new
                ));
    }
    public static Map<Guild, Integer> sortedGuildsByCommandUsage(int amount) {
        Map<String, Integer> allValues = sortByValue(Status.commandsByGuild);
        Map<Guild, Integer> finalValues = new LinkedHashMap<>();
        final int[] counter = {0};
        allValues.forEach((key, value) -> {
            if (counter[0] < amount) {
                finalValues.put(jda.getGuildById(key), value);
                counter[0]++;
            }
        });
        return finalValues;
    }

}
