package tk.ardentbot.Commands.GuildInfo;

import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;
import tk.ardentbot.Backend.Commands.BotCommand;
import tk.ardentbot.Backend.Translation.Language;
import tk.ardentbot.Backend.Translation.Translation;
import tk.ardentbot.Backend.Translation.TranslationResponse;
import tk.ardentbot.Utils.Discord.GuildUtils;

import java.util.ArrayList;
import java.util.HashMap;

public class GuildInfo extends BotCommand {
    public GuildInfo(CommandSettings commandSettings) {
        super(commandSettings);
    }

    @Override
    public void noArgs(Guild guild, MessageChannel channel, User user, Message message, String[] args, Language language) throws Exception {
        ArrayList<Translation> translations = new ArrayList<>();
        translations.add(new Translation("guildinfo", "gsi"));
        translations.add(new Translation("guildinfo", "language"));
        translations.add(new Translation("guildinfo", "prefix"));
        translations.add(new Translation("guildinfo", "numberofusers"));
        HashMap<Integer, TranslationResponse> responses = getTranslations(language, translations);
        StringBuilder sb = new StringBuilder();
        sb.append("**" + responses.get(0).getTranslation() + "**\n" +
                "===========\n");
        sb.append(" **>** *" + responses.get(1).getTranslation() + ": " + language.getIdentifier() + "*\n");
        sb.append(" **>** *" + responses.get(2).getTranslation() + ": " + GuildUtils.getPrefix(guild) + "*\n");
        sb.append(" **>** *" + responses.get(3).getTranslation() + ": " + guild.getMembers().size() + "*\n");
        sendTranslatedMessage(sb.toString(), channel);
    }

    @Override
    public void setupSubcommands() {}
}
