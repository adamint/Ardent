package tk.ardentbot.commands.fun;

import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.mashape.unirest.request.GetRequest;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;
import tk.ardentbot.core.executor.Command;
import tk.ardentbot.core.models.UDList;
import tk.ardentbot.core.models.UrbanDictionary;
import tk.ardentbot.main.Shard;
import tk.ardentbot.utils.discord.GuildUtils;
import tk.ardentbot.utils.discord.MessageUtils;

import java.awt.*;

public class UD extends Command {
    public UD(CommandSettings commandSettings) {
        super(commandSettings);
    }

    @Override
    public void noArgs(Guild guild, MessageChannel channel, User user, Message message, String[] args) throws Exception {
        Shard shard = GuildUtils.getShard(guild);
        if (args.length == 1) {
            sendTranslatedMessage("Search a word's definition from Urban Dictionary. Example: {0}ud test".replace("{0}", GuildUtils
                    .getPrefix(guild) + args[0]), channel, user);
        } else {
            GetRequest getRequest = Unirest.get("http://api.urbandictionary.com/v0/define?term=" + message
                    .getRawContent().replace(GuildUtils.getPrefix(guild) + args[0] + " ", "").replaceAll(" ", "%20"));
            String json = "";
            try {
                json = getRequest.asJson().getBody().toString();
            } catch (UnirestException e) {
                e.printStackTrace();
            }
            UrbanDictionary definition = shard.gson.fromJson(json, UrbanDictionary.class);
            if (definition.getList().size() == 0) {
                sendTranslatedMessage("There aren't any definitions for this word!", channel, user);
            } else {
                EmbedBuilder builder = MessageUtils.getDefaultEmbed(message.getAuthor());
                UDList list = definition.getList().get(0);


                String def = "Definition";
                String author = "Author";
                String example = "Example";
                String link = "Link";
                String thumbsUp = "Thumbs Up";
                String thumbsDown = "Thumbs Down";
                String urbanDictionary = "Urban Dictionary";

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
