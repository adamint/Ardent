package Commands.GuildInfo;

import Backend.Commands.BotCommand;
import Backend.Commands.Subcommand;
import Backend.Translation.Language;
import Backend.Translation.Translation;
import Backend.Translation.TranslationResponse;
import Main.Ardent;
import Utils.MessageUtils;
import com.vdurmont.emoji.Emoji;
import com.vdurmont.emoji.EmojiManager;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;

import java.util.ArrayList;
import java.util.HashMap;

public class Emojis extends BotCommand {
    public Emojis(CommandSettings commandSettings) {
        super(commandSettings);
    }

    @Override
    public void noArgs(Guild guild, MessageChannel channel, User user, Message message, String[] args, Language language) throws Exception {
        sendHelp(language, channel);
    }

    @Override
    public void setupSubcommands() throws Exception {
        subcommands.add(new Subcommand(this, "info") {
            @Override
            public void onCall(Guild guild, MessageChannel channel, User user, Message message, String[] args, Language language) throws Exception {
                if (args.length > 2) {
                    ArrayList<Translation> queries = new ArrayList<>();
                    queries.add(new Translation("emojis", "lookuptitle"));
                    queries.add(new Translation("emojis", "thefoundemoji"));

                    HashMap<Integer, TranslationResponse> responses = getTranslations(language, queries);

                    Emoji emoji = EmojiManager.getForAlias(args[2]);
                    if (emoji != null) {
                        EmbedBuilder builder = MessageUtils.getDefaultEmbed(guild, user, Emojis.this);
                        builder.setTitle(responses.get(0).getTranslation(), Ardent.url);

                        StringBuilder description = new StringBuilder();
                        description.append(responses.get(1).getTranslation().replace("{0}", args[2]));
                        description.append("\n\n" + emoji.getDescription());

                        builder.setDescription(description.toString());
                        sendEmbed(builder, channel);
                    }
                    else sendRetrievedTranslation(channel, "emojis", language, "includeemoji");
                }
                else sendRetrievedTranslation(channel, "emojis", language, "includeemoji");
            }
        });
    }
}
