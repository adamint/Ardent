package tk.ardentbot.botCommands.fun;

import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;
import tk.ardentbot.core.commandExecutor.Command;
import tk.ardentbot.core.translation.Language;
import tk.ardentbot.core.translation.Translation;
import tk.ardentbot.core.translation.TranslationResponse;
import tk.ardentbot.utils.discord.GuildUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class Coinflip extends Command {
    public Coinflip(CommandSettings commandSettings) {
        super(commandSettings);
    }

    @Override
    public void noArgs(Guild guild, MessageChannel channel, User user, Message message, String[] args, Language
            language) throws Exception {
        ArrayList<Translation> translations = new ArrayList<>();
        translations.add(new Translation("coinflip", "original"));
        translations.add(new Translation("coinflip", "heads"));
        translations.add(new Translation("coinflip", "tails"));
        translations.add(new Translation("coinflip", "after"));
        HashMap<Integer, TranslationResponse> responses = getTranslations(language, translations);
        channel.sendMessage(responses.get(0).getTranslation()).queue(message1 -> {
            GuildUtils.getShard(guild).executorService.schedule(() -> {
                String after = responses.get(3).getTranslation();
                boolean heads = new Random().nextBoolean();
                if (heads) {
                    after = after.replace("{0}", responses.get(1).getTranslation());
                }
                else {
                    after = after.replace("{0}", responses.get(2).getTranslation());
                }
                message1.editMessage(after).queue();
            }, 2, TimeUnit.SECONDS);
        });
    }

    @Override
    public void setupSubcommands() {
    }
}
