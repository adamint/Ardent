package tk.ardentbot.BotCommands.GuildInfo;

import com.vdurmont.emoji.Emoji;
import com.vdurmont.emoji.EmojiManager;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;
import tk.ardentbot.Core.CommandExecution.Command;
import tk.ardentbot.Core.CommandExecution.Subcommand;
import tk.ardentbot.Core.Translation.Language;
import tk.ardentbot.Core.Translation.Translation;
import tk.ardentbot.Core.Translation.TranslationResponse;
import tk.ardentbot.Main.Shard;
import tk.ardentbot.Utils.Discord.GuildUtils;
import tk.ardentbot.Utils.Discord.MessageUtils;

import java.util.ArrayList;
import java.util.HashMap;

public class Emojis extends Command {
    public Emojis(CommandSettings commandSettings) {
        super(commandSettings);
    }

    @Override
    public void noArgs(Guild guild, MessageChannel channel, User user, Message message, String[] args, Language
            language) throws Exception {
        sendHelp(language, channel, guild, user, this);
    }

    @Override
    public void setupSubcommands() throws Exception {
        subcommands.add(new Subcommand(this, "info") {
            @Override
            public void onCall(Guild guild, MessageChannel channel, User user, Message message, String[] args,
                               Language language) throws Exception {
                if (args.length > 2) {
                    Shard shard = GuildUtils.getShard(guild);
                    ArrayList<Translation> queries = new ArrayList<>();
                    queries.add(new Translation("emojis", "lookuptitle"));
                    queries.add(new Translation("emojis", "thefoundemoji"));

                    HashMap<Integer, TranslationResponse> responses = getTranslations(language, queries);

                    Emoji emoji = EmojiManager.getForAlias(args[2]);
                    if (emoji != null) {
                        EmbedBuilder builder = MessageUtils.getDefaultEmbed(guild, user, Emojis.this);
                        builder.setTitle(responses.get(0).getTranslation(), shard.url);

                        StringBuilder description = new StringBuilder();
                        description.append(responses.get(1).getTranslation().replace("{0}", args[2]));
                        description.append("\n\n" + emoji.getDescription());

                        builder.setDescription(description.toString());
                        sendEmbed(builder, channel, user);
                    }
                    else sendRetrievedTranslation(channel, "emojis", language, "includeemoji", user);
                }
                else sendRetrievedTranslation(channel, "emojis", language, "includeemoji", user);
            }
        });
    }
}
