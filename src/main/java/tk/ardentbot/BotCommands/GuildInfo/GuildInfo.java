package tk.ardentbot.botCommands.guildInfo;

import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;
import tk.ardentbot.core.commandExecutor.Command;
import tk.ardentbot.core.translation.Language;
import tk.ardentbot.core.translation.Translation;
import tk.ardentbot.core.translation.TranslationResponse;
import tk.ardentbot.utils.discord.GuildUtils;
import tk.ardentbot.utils.rpgUtils.EntityGuild;

import java.util.ArrayList;
import java.util.HashMap;

public class GuildInfo extends Command {
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
        translations.add(new Translation("guildinfo", "premium"));
        HashMap<Integer, TranslationResponse> responses = getTranslations(language, translations);
        StringBuilder sb = new StringBuilder();
        sb.append("**" + responses.get(0).getTranslation() + "**\n" +
                "===========\n");
        sb.append(" **>** *" + responses.get(1).getTranslation() + ": " + language.getIdentifier() + "*\n");
        sb.append(" **>** *" + responses.get(2).getTranslation() + ": " + GuildUtils.getPrefix(guild) + "*\n");
        sb.append(" **>** *" + responses.get(3).getTranslation() + ": " + guild.getMembers().size() + "*\n");
        sb.append(" **>** *" + responses.get(4).getTranslation() + ": " + EntityGuild.get(guild).isPremium() + "*");
        sendTranslatedMessage(sb.toString(), channel, user);
    }

    @Override
    public void setupSubcommands() {}
}
