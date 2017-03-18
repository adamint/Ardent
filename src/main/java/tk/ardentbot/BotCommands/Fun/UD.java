package tk.ardentbot.BotCommands.Fun;

import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.mashape.unirest.request.GetRequest;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;
import tk.ardentbot.Core.CommandExecution.Command;
import tk.ardentbot.Core.Models.UDList;
import tk.ardentbot.Core.Models.UrbanDictionary;
import tk.ardentbot.Core.Translation.Language;
import tk.ardentbot.Core.Translation.Translation;
import tk.ardentbot.Core.Translation.TranslationResponse;
import tk.ardentbot.Main.Shard;
import tk.ardentbot.Utils.Discord.GuildUtils;
import tk.ardentbot.Utils.Discord.MessageUtils;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;

public class UD extends Command {
    public UD(CommandSettings commandSettings) {
        super(commandSettings);
    }

    @Override
    public void noArgs(Guild guild, MessageChannel channel, User user, Message message, String[] args, Language
            language) throws Exception {
        Shard shard = GuildUtils.getShard(guild);
        if (args.length == 1) {
            sendTranslatedMessage(getTranslation("ud", language, "help").getTranslation().replace("{0}", GuildUtils
                    .getPrefix(guild) + args[0]), channel, user);
        }
        else {
            GetRequest getRequest = Unirest.get("http://api.urbandictionary.com/v0/define?term=" + message
                    .getRawContent().replace(GuildUtils.getPrefix(guild) + args[0] + " ", "").replaceAll(" ", "%20"));
            String json = "";
            try {
                json = getRequest.asJson().getBody().toString();
            }
            catch (UnirestException e) {
                e.printStackTrace();
            }
            UrbanDictionary definition = shard.gson.fromJson(json, UrbanDictionary.class);
            if (definition.getList().size() == 0) {
                sendRetrievedTranslation(channel, "ud", language, "notranslations", user);
            }
            else {
                EmbedBuilder builder = MessageUtils.getDefaultEmbed(guild, message.getAuthor(), this);
                UDList list = definition.getList().get(0);

                Translation defTranslation = new Translation("ud", "def");
                Translation authorTranslation = new Translation("ud", "author");
                Translation exampleTranslation = new Translation("ud", "example");
                Translation linkTranslation = new Translation("ud", "link");
                Translation thumbsUpTranslation = new Translation("ud", "thumbsup");
                Translation thumbsDownTranslation = new Translation("ud", "thumbsdown");
                Translation urbanDictionaryTranslation = new Translation("ud", "urbandictionary");
                ArrayList<Translation> translationQueries = new ArrayList<>();
                translationQueries.add(defTranslation);
                translationQueries.add(authorTranslation);
                translationQueries.add(exampleTranslation);
                translationQueries.add(linkTranslation);
                translationQueries.add(thumbsUpTranslation);
                translationQueries.add(thumbsDownTranslation);
                translationQueries.add(urbanDictionaryTranslation);

                HashMap<Integer, TranslationResponse> translations = getTranslations(language, translationQueries);

                String def = translations.get(0).getTranslation();
                String author = translations.get(1).getTranslation();
                String example = translations.get(2).getTranslation();
                String link = translations.get(3).getTranslation();
                String thumbsUp = translations.get(4).getTranslation();
                String thumbsDown = translations.get(5).getTranslation();
                String urbanDictionary = translations.get(6).getTranslation();

                builder.setAuthor(urbanDictionary, shard.bot.getAvatarUrl(), shard.bot.getAvatarUrl());
                builder.setThumbnail("https://i.gyazo.com/6a40e32928743e68e9006396ee7c2a14.jpg");
                builder.setColor(Color.decode("#00B7BE"));

                String definitionText = list.getDefinition();
                String exampleText = list.getExample();
                if (definitionText.length() > 1024) definitionText = definitionText.substring(0, 1023);
                if (exampleText.length() > 1024) exampleText = exampleText.substring(0, 1023);

                builder.addField(def, definitionText, true);
                builder.addField(example, exampleText, true);

                builder.addField(thumbsUp, String.valueOf(list.getThumbsUp() + ":thumbsup:"), true);
                builder.addField(thumbsDown, String.valueOf(list.getThumbsDown() + ":thumbsdown:"), true);

                builder.addField(author, list.getAuthor(), true);
                builder.addField(link, list.getPermalink(), true);


                sendEmbed(builder, channel, user);
            }
        }
    }

    @Override
    public void setupSubcommands() {
    }
}
