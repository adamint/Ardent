package tk.ardentbot.commands.fun;

import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;
import tk.ardentbot.core.executor.Command;
import tk.ardentbot.utils.discord.GuildUtils;

import java.util.Random;
import java.util.concurrent.TimeUnit;

public class Roll extends Command {
    public Roll(CommandSettings commandSettings) {
        super(commandSettings);
    }

    @Override
    public void noArgs(Guild guild, MessageChannel channel, User user, Message message, String[] args) throws Exception {
        String before = "Rolling a 6-sided die now...";
        String after = " Rolling a 6-sided die now... Result: {0}";

        channel.sendMessage(before).queue(message1 -> {
            GuildUtils.getShard(guild).executorService.schedule(() -> {
                int roll = new Random().nextInt(6) + 1;
                message1.editMessage(after.replace("{0}", String.valueOf(roll))).queue();
            }, 2, TimeUnit.SECONDS);
        });
    }

    @Override
    public void setupSubcommands() {

    }
}
