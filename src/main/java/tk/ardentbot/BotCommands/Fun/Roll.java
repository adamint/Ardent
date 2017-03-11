package tk.ardentbot.BotCommands.Fun;

import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;
import tk.ardentbot.Core.CommandExecution.Command;
import tk.ardentbot.Core.Translation.Language;
import tk.ardentbot.Core.Translation.Translation;
import tk.ardentbot.Core.Translation.TranslationResponse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import static tk.ardentbot.Main.Ardent.ardent;

public class Roll extends Command {
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
            ardent.executorService.schedule(() -> {
                int roll = new Random().nextInt(6) + 1;
                message1.editMessage(after.replace("{0}", String.valueOf(roll))).queue();
            }, 2, TimeUnit.SECONDS);
        });
    }

    @Override
    public void setupSubcommands() {

    }
}
