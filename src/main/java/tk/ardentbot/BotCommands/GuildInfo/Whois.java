package tk.ardentbot.BotCommands.GuildInfo;

import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.*;
import tk.ardentbot.Core.CommandExecution.Command;
import tk.ardentbot.Core.Translation.Language;
import tk.ardentbot.Core.Translation.Translation;
import tk.ardentbot.Core.Translation.TranslationResponse;
import tk.ardentbot.Main.Shard;
import tk.ardentbot.Utils.Discord.GuildUtils;
import tk.ardentbot.Utils.Discord.MessageUtils;

import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Whois extends Command {
    public Whois(CommandSettings commandSettings) {
        super(commandSettings);
    }

    @Override
    public void noArgs(Guild guild, MessageChannel channel, User user, Message message, String[] args, Language language) throws Exception {
        Shard shard = GuildUtils.getShard(guild);
        Member member;
        List<User> mentionedUsers = message.getMentionedUsers();
        if (mentionedUsers.size() > 0) {
            member = guild.getMember(mentionedUsers.get(0));
        }
        else member = guild.getMember(user);

        ArrayList<Translation> translations = new ArrayList<>();
        translations.add(new Translation("whois", "informationabout"));
        translations.add(new Translation("whois", "username"));
        translations.add(new Translation("whois", "nickname"));
        translations.add(new Translation("other", "none"));
        translations.add(new Translation("whois", "discriminator"));
        translations.add(new Translation("whois", "joindate"));
        translations.add(new Translation("whois", "daysinguild"));
        translations.add(new Translation("whois", "roles"));

        HashMap<Integer, TranslationResponse> responses = getTranslations(language, translations);

        List<Role> rolesList = member.getRoles();
        String nick;
        if (member.getNickname() == null) nick = responses.get(3).getTranslation();
        else nick = member.getNickname();
        long digTemp = Instant.now().atOffset(ZoneOffset.UTC).toEpochSecond() - member.getJoinDate().toInstant().atOffset(ZoneOffset.UTC).toEpochSecond();
        long daysInGuild = Math.round(digTemp / (60 * 60 * 24));
        if (daysInGuild == 0) daysInGuild = 1;

        StringBuilder roles = new StringBuilder();
        for (int i = 0; i < rolesList.size(); i++) {
            if (i == 0) roles.append(rolesList.get(0).getName());
            else roles.append(", " + rolesList.get(i).getName());
        }

        EmbedBuilder builder = MessageUtils.getDefaultEmbed(guild, message.getAuthor(), this);
        builder.setAuthor(responses.get(0).getTranslation().replace("{0}", member.getUser().getName()),
                "https://ardentbot.tk/guild", shard.bot.getAvatarUrl());
        builder.setThumbnail(member.getUser().getAvatarUrl());

        builder.addField(responses.get(1).getTranslation(), member.getUser().getName(), true);
        builder.addField(responses.get(2).getTranslation(), nick, true);

        builder.addField(responses.get(4).getTranslation(), "#" + member.getUser().getDiscriminator(), true);
        builder.addField(responses.get(5).getTranslation(), member.getJoinDate().toLocalDate().toString(), true);

        builder.addField(responses.get(6).getTranslation(), String.valueOf(daysInGuild), true);
        builder.addField(responses.get(7).getTranslation(), roles.toString(), true);

        sendEmbed(builder, channel, user);
    }

    @Override
    public void setupSubcommands() {}
}
