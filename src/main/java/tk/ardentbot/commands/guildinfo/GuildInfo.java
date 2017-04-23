package tk.ardentbot.commands.guildinfo;

import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.OnlineStatus;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;
import tk.ardentbot.core.executor.Command;
import tk.ardentbot.core.translate.Language;
import tk.ardentbot.core.translate.Translation;
import tk.ardentbot.core.translate.TranslationResponse;
import tk.ardentbot.utils.discord.GuildUtils;
import tk.ardentbot.utils.discord.MessageUtils;
import tk.ardentbot.utils.discord.UserUtils;
import tk.ardentbot.utils.rpg.EntityGuild;

import java.util.ArrayList;
import java.util.HashMap;

public class GuildInfo extends Command {
    public GuildInfo(CommandSettings commandSettings) {
        super(commandSettings);
    }

    @Override
    public void noArgs(Guild guild, MessageChannel channel, User user, Message message, String[] args, Language language) throws Exception {
        ArrayList<Translation> trs = new ArrayList<>();
        trs.add(new Translation("guildinfo", "guildinfo"));
        trs.add(new Translation("guildinfo", "language"));
        trs.add(new Translation("guildinfo", "prefix"));
        trs.add(new Translation("guildinfo", "numberofusers"));
        trs.add(new Translation("guildinfo", "owner"));
        trs.add(new Translation("guildinfo", "premium"));
        trs.add(new Translation("roleinfo", "creationtime"));
        trs.add(new Translation("guildinfo", "publicchannel"));
        trs.add(new Translation("guildinfo", "voicechannelcount"));
        trs.add(new Translation("guildinfo", "textchannelcount"));
        trs.add(new Translation("guildinfo", "rolecount"));
        trs.add(new Translation("guildinfo", "region"));
        trs.add(new Translation("guildinfo", "online"));

        HashMap<Integer, TranslationResponse> translations = getTranslations(language, trs);
        EmbedBuilder builder = MessageUtils.getDefaultEmbed(guild, user, this);
        builder.setAuthor(translations.get(0).getTranslation(), guild.getIconUrl(), guild.getIconUrl());
        builder.addField(translations.get(3).getTranslation(), String.valueOf(guild.getMembers().size()), true);
        builder.addField(translations.get(12).getTranslation(), String.valueOf(guild.getMembers().stream().filter(m -> m.getOnlineStatus
                () == OnlineStatus.ONLINE).count()), true);
        builder.addField(translations.get(1).getTranslation(), language.getIdentifier(), true);
        builder.addField(translations.get(2).getTranslation(), GuildUtils.getPrefix(guild), true);
        builder.addField(translations.get(5).getTranslation(), String.valueOf(EntityGuild.get(guild).isPremium()), true);
        builder.addField(translations.get(4).getTranslation(), UserUtils.getNameWithDiscriminator(guild.getOwner().getUser().getId()),
                true);
        builder.addField(translations.get(6).getTranslation(), guild.getCreationTime().toLocalDate().toString(), true);
        builder.addField(translations.get(7).getTranslation(), guild.getPublicChannel().getAsMention(), true);
        builder.addField(translations.get(8).getTranslation(), String.valueOf(guild.getVoiceChannels().size()), true);
        builder.addField(translations.get(9).getTranslation(), String.valueOf(guild.getTextChannels().size()), true);
        builder.addField(translations.get(10).getTranslation(), String.valueOf(guild.getRoles().size()), true);
        builder.addField(translations.get(11).getTranslation(), guild.getRegion().getName(), true);

        sendEmbed(builder, channel, user);
    }

    @Override
    public void setupSubcommands() {
    }
}
