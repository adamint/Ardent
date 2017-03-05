package tk.ardentbot.Commands.Fun;

import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;
import tk.ardentbot.Backend.Commands.BotCommand;
import tk.ardentbot.Backend.Translation.Language;
import tk.ardentbot.Backend.Translation.Translation;
import tk.ardentbot.Backend.Translation.TranslationResponse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import static tk.ardentbot.Main.Config.executorService;

public class Roll extends BotCommand {
    public Roll(CommandSettings commandSettings) {
        super(commandSettings);
    }

    @Override
    public void noArgs(Guild guild, MessageChannel channel, User user, Message message, String[] args, Language language) throws Exception {
        ArrayList<Translation> translations = new ArrayList<>();
        translations.add(new Translation("roll", "before"));
        translations.add(new Translation("roll", "after"));
        HashMap<Integer, TranslationResponse> responses = getTranslations(language, translations);
        String before = responses.get(0).getTranslation();
        String after = responses.get(1).getTranslation();

        channel.sendMessage(before).queue(message1 -> {
            executorService.schedule(() -> {
                int roll = new Random().nextInt(6) + 1;
                message1.editMessage(after.replace("{0}", String.valueOf(roll))).queue();
            }, 2, TimeUnit.SECONDS);
        });
    }

    @Override
    public void setupSubcommands() {

    }
}
