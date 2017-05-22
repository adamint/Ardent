package tk.ardentbot.commands.botinfo;

import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;
import tk.ardentbot.core.executor.Command;
import tk.ardentbot.main.Shard;
import tk.ardentbot.main.ShardManager;

public class ShardInfo extends Command {
    public ShardInfo(CommandSettings commandSettings) {
        super(commandSettings);
    }

    @Override
    public void noArgs(Guild guild, MessageChannel channel, User user, Message message, String[] args) throws Exception {
        StringBuilder builder = new StringBuilder();
        for (Shard shard : ShardManager.getShards()) {
            builder.append(shard.jda.getShardInfo()).append(" | STATUS: ").append(shard.jda.getStatus()).append(" | " +
                    "U: ").append(shard.jda.getUsers().size()).append(" | G: ").append(shard.jda.getGuilds().size()).append
                    (" | L: ").append(" | MC: ").append(shard.jda.getVoiceChannels().stream().filter
                    (voiceChannel -> voiceChannel.getMembers().contains(voiceChannel.getGuild().getSelfMember
                            ())).count()).append(" | LE: " + (System.currentTimeMillis() - shard.getLAST_EVENT()));

            if (shard.jda.getShardInfo() != null && shard.jda.getShardInfo().equals(guild.getJDA().getShardInfo())) {
                builder.append(" <- CURRENT");
            }

            builder.append("\n");
        }
        channel.sendMessage(String.format("```prolog\n%s```", builder.toString())).queue();
    }

    @Override
    public void setupSubcommands() {
    }
}
