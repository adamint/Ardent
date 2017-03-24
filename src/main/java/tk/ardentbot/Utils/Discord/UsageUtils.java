package tk.ardentbot.Utils.Discord;

import net.dv8tion.jda.core.entities.Guild;
import org.eclipse.jetty.util.ConcurrentArrayQueue;
import tk.ardentbot.BotCommands.BotInfo.Status;
import tk.ardentbot.Core.CommandExecution.BaseCommand;

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import java.lang.management.ManagementFactory;
import java.util.*;
import java.util.stream.Collectors;

import static tk.ardentbot.Main.Ardent.shard0;

public class UsageUtils {
    private static Comparator<BaseCommand> SORT_BY_USAGE = (o1, o2) -> {
        if (o1.getBotCommand().usages < o2.getBotCommand().usages) return 1;
        else if (Objects.equals(o1.getBotCommand().usages, o2.getBotCommand().usages)) return 0;
        else return -1;
    };

    public static ArrayList<BaseCommand> orderByUsageDesc() {
        ConcurrentArrayQueue<BaseCommand> unsorted = shard0.factory.getBaseCommands();
        ArrayList<BaseCommand> baseCommands = new ArrayList<>();
        for (BaseCommand c : unsorted) {
            if (!c.getCommandIdentifier().equalsIgnoreCase("patreon") && !c.getCommandIdentifier().equalsIgnoreCase
                    ("translateforardent") && !c.getCommandIdentifier().equalsIgnoreCase("tweet") && !c
                    .getCommandIdentifier().equalsIgnoreCase("eval") && !c.getCommandIdentifier().equalsIgnoreCase
                    ("addenglishbase") && !c.getCommandIdentifier().equalsIgnoreCase("help") && !c
                    .getCommandIdentifier().equalsIgnoreCase("manage") && !c.getCommandIdentifier().equalsIgnoreCase
                    ("admin"))
            {
                baseCommands.add(c);
            }
        }
        Collections.sort(baseCommands, SORT_BY_USAGE);
        return baseCommands;
    }

    public static boolean isGuildFirstInCommands(Guild guild) {
        /*ConcurrentHashMap<String, Integer> commandsByGuild = Status.commandsByGuild;
        final boolean[] first = {true};
        int guildCommands = commandsByGuild.get(guild.getId());
        commandsByGuild.forEach((key, value) -> {
            if (guildCommands < value) first[0] = false;
        });
        return first[0];*/

        // @Akio take a look at this pls
        return false;
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
                finalValues.put(shard0.jda.getGuildById(key), value);
                counter[0]++;
            }
        });
        return finalValues;
    }

    // Credit http://stackoverflow.com/questions/18489273/how-to-get-percentage-of-cpu-usage-of-os-from-java
    public static double getProcessCpuLoad() throws Exception {
        MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
        ObjectName name = ObjectName.getInstance("java.lang:type=OperatingSystem");
        AttributeList list = mbs.getAttributes(name, new String[]{"ProcessCpuLoad"});

        if (list.isEmpty()) return Double.NaN;

        Attribute att = (Attribute) list.get(0);
        Double value = (Double) att.getValue();

        // usually takes a couple of seconds before we get real values
        if (value == -1.0) return Double.NaN;
        // returns a percentage value with 1 decimal point precision
        return ((int) (value * 1000) / 10.0);
    }
}
