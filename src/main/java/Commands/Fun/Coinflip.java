package Commands.Fun;

import Backend.Commands.BotCommand;
import Backend.Translation.Language;
import Backend.Translation.Translation;
import Backend.Translation.TranslationResponse;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.TimerTask;

import static Main.Ardent.timer;

public class Coinflip extends BotCommand {
    public Coinflip(CommandSettings commandSettings) {
        super(commandSettings);
    }

    @Override
    public void noArgs(Guild guild, MessageChannel channel, User user, Message message, String[] args, Language language) throws Exception {
        ArrayList<Translation> translations = new ArrayList<>();
        translations.add(new Translation("coinflip", "original"));
        translations.add(new Translation("coinflip", "heads"));
        translations.add(new Translation("coinflip", "tails"));
        translations.add(new Translation("coinflip", "after"));
        HashMap<Integer, TranslationResponse> responses = getTranslations(language, translations);
        channel.sendMessage(responses.get(0).getTranslation()).queue(message1 -> {
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    String after = responses.get(3).getTranslation();
                    boolean heads = new Random().nextBoolean();
                    if (heads) {
                        after = after.replace("{0}", responses.get(1).getTranslation());
                    }
                    else {
                        after = after.replace("{0}", responses.get(2).getTranslation());
                    }
                    message1.editMessage(after).queue();
                }
            }, 2000);
        });
    }

    @Override
    public void setupSubcommands() {
    }
}
