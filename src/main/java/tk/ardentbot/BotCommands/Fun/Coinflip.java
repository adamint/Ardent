package tk.ardentbot.BotCommands.Fun;

import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;
import tk.ardentbot.Core.CommandExecution.Cmd;
import tk.ardentbot.Core.Translation.Language;
import tk.ardentbot.Core.Translation.Translation;
import tk.ardentbot.Core.Translation.TranslationResponse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import static tk.ardentbot.Main.Ardent.ardent;

public class Coinflip extends Cmd {
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
            ardent.executorService.schedule(() -> {
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
