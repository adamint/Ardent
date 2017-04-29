package tk.ardentbot.commands.guildinfo;

import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.*;
import tk.ardentbot.core.executor.Command;
import tk.ardentbot.main.Shard;
import tk.ardentbot.utils.discord.GuildUtils;
import tk.ardentbot.utils.discord.MessageUtils;

import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;

public class Whois extends Command {
    public Whois(CommandSettings commandSettings) {
        super(commandSettings);
    }

    @Override
    public void noArgs(Guild guild, MessageChannel channel, User user, Message message, String[] arg) throws Exception {
        Shard shard = GuildUtils.getShard(guild);
        Member member;
        List<User> mentionedUsers = message.getMentionedUsers();
        if (mentionedUsers.size() > 0) {
            member = guild.getMember(mentionedUsers.get(0));
        } else member = guild.getMember(user);

        List<Role> rolesList = member.getRoles();
        String nick;
        if (member.getNickname() == null) nick = "None";
        else nick = member.getNickname();
        long digTemp = Instant.now().atOffset(ZoneOffset.UTC).toEpochSecond() - member.getJoinDate().toInstant().atOffset(ZoneOffset.UTC)
                .toEpochSecond();
        long daysInGuild = Math.round(digTemp / (60 * 60 * 24));
        if (daysInGuild == 0) daysInGuild = 1;

        StringBuilder roles = new StringBuilder();
        for (int i = 0; i < rolesList.size(); i++) {
            if (i == 0) roles.append(rolesList.get(0).getName());
            else roles.append(", " + rolesList.get(i).getName());
        }

        EmbedBuilder builder = MessageUtils.getDefaultEmbed(message.getAuthor());
        builder.setAuthor("Information About {0}".replace("{0}", member.getUser().getName()),
                "https://ardentbot.tk/guild", shard.bot.getAvatarUrl());
        builder.setThumbnail(member.getUser().getAvatarUrl());

        builder.addField("Username", member.getUser().getName(), true);
        builder.addField("Nickname", nick, true);

        builder.addField("None", "#" + member.getUser().getDiscriminator(), true);
        builder.addField("Discriminator", member.getJoinDate().toLocalDate().toString(), true);

        builder.addField("Joindate", String.valueOf(daysInGuild), true);
        builder.addField("roles", roles.toString(), true);

        sendEmbed(builder, channel, user);
    }

    @Override
    public void setupSubcommands() {
    }
}
