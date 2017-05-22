package tk.ardentbot.commands.guildinfo;

import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.OnlineStatus;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;
import tk.ardentbot.core.executor.Command;
import tk.ardentbot.utils.discord.GuildUtils;
import tk.ardentbot.utils.discord.MessageUtils;
import tk.ardentbot.utils.discord.UserUtils;
import tk.ardentbot.utils.rpg.EntityGuild;

public class GuildInfo extends Command {
    public GuildInfo(CommandSettings commandSettings) {
        super(commandSettings);
    }

    @Override
    public void noArgs(Guild guild, MessageChannel channel, User user, Message message, String[] args) throws Exception {
        EmbedBuilder builder = MessageUtils.getDefaultEmbed(user);
        builder.setAuthor("Server Info", guild.getIconUrl(), guild.getIconUrl());
        builder.addField("Number of users", String.valueOf(guild.getMembers().size()), true);
        builder.addField("Online users", String.valueOf(guild.getMembers().stream().filter(m -> m.getOnlineStatus
                () == OnlineStatus.ONLINE).count()), true);
        builder.addField("Prefix", GuildUtils.getPrefix(guild), true);
        builder.addField("Premium Server", String.valueOf(EntityGuild.get(guild).isPremium()), true);
        builder.addField("Owner", UserUtils.getNameWithDiscriminator(guild.getOwner().getUser().getId()),
                true);
        builder.addField("Creation Date", guild.getCreationTime().toLocalDate().toString(), true);
        builder.addField("Public channel", guild.getPublicChannel().getAsMention(), true);
        builder.addField("# of Voice Channels", String.valueOf(guild.getVoiceChannels().size()), true);
        builder.addField("# of Text Channels", String.valueOf(guild.getTextChannels().size()), true);
        builder.addField("# of Roles", String.valueOf(guild.getRoles().size()), true);
        builder.addField("Region", guild.getRegion().getName(), true);
        builder.addField("Verification Level", String.valueOf(guild.getVerificationLevel()), true);


        sendEmbed(builder, channel, user);
    }

    @Override
    public void setupSubcommands() {
    }
}
