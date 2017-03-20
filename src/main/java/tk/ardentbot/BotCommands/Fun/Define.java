package tk.ardentbot.BotCommands.Fun;

import com.mashape.unirest.http.Unirest;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;
import tk.ardentbot.Core.CommandExecution.Command;
import tk.ardentbot.Core.Models.Definition;
import tk.ardentbot.Core.Models.Dictionary;
import tk.ardentbot.Core.Translation.Language;
import tk.ardentbot.Main.Ardent;
import tk.ardentbot.Main.Shard;
import tk.ardentbot.Utils.Discord.GuildUtils;
import tk.ardentbot.Utils.Discord.MessageUtils;

import java.util.Calendar;
import java.util.List;

public class Define extends Command {
    private int requests = 0;

    public Define(CommandSettings commandSettings) {
        super(commandSettings);
    }

    @Override
    public void noArgs(Guild guild, MessageChannel channel, User user, Message message, String[] args, Language
            language) throws Exception {
        if (args.length == 1) {
            sendRetrievedTranslation(channel, "define", language, "includeword", user);
        }
        else {
            Calendar myCal = Calendar.getInstance();
            myCal.setTimeInMillis(System.currentTimeMillis());
            boolean isMidnight = myCal.get(Calendar.HOUR_OF_DAY) == 0
                    && myCal.get(Calendar.MINUTE) == 0
                    && myCal.get(Calendar.SECOND) == 0
                    && myCal.get(Calendar.MILLISECOND) == 0;

            if (isMidnight) requests = 0;
            else {
                if (requests < 2500) {
                    Shard shard = getShard();
                    String query = message.getContent().replace(GuildUtils.getPrefix(guild) + args[0] + " ", "");
                    try {
                        Dictionary dictionary = shard.gson.fromJson(Unirest.get("https://wordsapiv1.p.mashape" +
                                ".com/words/" + query + "/definitions")

                                .header("X-Mashape-Key", Ardent.mashapeKey)
                                .header("Accept", "application/json")
                                .asString().getBody(), Dictionary.class);
                        EmbedBuilder builder = MessageUtils.getDefaultEmbed(guild, user, this);
                        builder.setAuthor("English Dictionary", "https://ardentbot.tk", shard.bot.getAvatarUrl());
                        List<Definition> definitions = dictionary.getDefinitions();
                        StringBuilder description = new StringBuilder();
                        description.append("Word: **" + query + "**\n");
                        if (definitions.size() == 0) description.append("There were no definitions for this word");
                        else {
                            Definition first = definitions.get(0);
                            description.append("Part of Speech: " + first.getPartOfSpeech() + "\n");
                            description.append("Definition: " + first.getDefinition());
                        }
                        builder.setDescription(description.toString());
                        sendEmbed(builder, channel, user);
                    }
                    catch (Exception ex) {
                        sendRetrievedTranslation(channel, "define", language, "nodefinitions", user);
                    }
                }
                else sendRetrievedTranslation(channel, "define", language, "hitapilimit", user);
            }
        }
    }

    @Override
    public void setupSubcommands() throws Exception {

    }
}
