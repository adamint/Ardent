package tk.ardentbot.Core.LoggingUtils;

import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.TextChannel;

import static tk.ardentbot.Main.Ardent.ardent;

public class EventLogger {
    private static TextChannel botLogs = ardent.botLogs;

    public static void join(Guild guild) {
        botLogs.sendMessage(":hihihi: `Joined a new guild: " + guild.getName() + " (" + guild.getMembers().size() +
                ")`").queue();
    }

    public static void leave(Guild guild) {
        botLogs.sendMessage(":frowning: `Left a guild: " + guild.getName() + " (" + guild.getMembers().size() + ")`")
                .queue();
    }
}
