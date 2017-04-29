package tk.ardentbot.commands.fun;

import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;
import tk.ardentbot.core.executor.Command;
import tk.ardentbot.utils.discord.GuildUtils;

import java.util.Random;
import java.util.concurrent.TimeUnit;

public class Coinflip extends Command {
    public Coinflip(CommandSettings commandSettings) {
        super(commandSettings);
    }

    @Override
    public void noArgs(Guild guild, MessageChannel channel, User user, Message message, String[] args) throws Exception {
        channel.sendMessage("Flipping a coin now...").queue(message1 -> {
            GuildUtils.getShard(guild).executorService.schedule(() -> {
                String after = "Flipping a coin now... Result: ";
                boolean heads = new Random().nextBoolean();
                if (heads) {
                    after += "Heads";
                } else {
                    after += "Tails";
                }
                message1.editMessage(after).queue();
            }, 2, TimeUnit.SECONDS);
        });
    }

    @Override
    public void setupSubcommands() {
    }
}
