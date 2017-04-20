package tk.ardentbot.commands.music;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.TextChannel;
import tk.ardentbot.core.misc.logging.BotException;
import tk.ardentbot.main.Shard;
import tk.ardentbot.utils.discord.GuildUtils;

import java.util.HashMap;
import java.util.List;

import static tk.ardentbot.main.ShardManager.getShards;

public class StuckVoiceConnection implements Runnable {
    private static HashMap<String, String> voiceChannelsAtZeroDuration = new HashMap<>();

    @Override
    public void run() {
        for (Shard shard : getShards()) {
            for (Guild guild : shard.jda.getGuilds()) {
                GuildMusicManager guildMusicManager = Music.getGuildAudioPlayer(guild, null, shard);
                AudioPlayer player = guildMusicManager.player;
                if (!player.isPaused() && guild.getAudioManager().getConnectedChannel() != null && guild.getAudioManager()
                        .getConnectedChannel().getMembers().size() > 1)
                {
                    AudioTrack playingTrack = player.getPlayingTrack();
                    if (playingTrack != null) {
                        long position = playingTrack.getPosition();
                        if (position == 0 || (position > 0 && !guild.getAudioManager().isConnected())) {
                            String textChannelId = voiceChannelsAtZeroDuration.get(guild.getId());
                            if (textChannelId == null) {
                                ArdentMusicManager ardentMusicManager = guildMusicManager.scheduler.manager;
                                TextChannel channel = ardentMusicManager.getChannel();
                                voiceChannelsAtZeroDuration.put(guild.getId(), channel.getId());
                            }
                            else {
                                TextChannel channel = guild.getTextChannelById(textChannelId);
                                ArdentMusicManager ardentMusicManager = guildMusicManager.scheduler.manager;
                                if (ardentMusicManager != null) {
                                    List<ArdentTrack> queue = ardentMusicManager.getQueueAsList();
                                    shard.musicManagers.remove(Long.parseLong(guild.getId()));
                                    GuildMusicManager manager = Music.getGuildAudioPlayer(guild, channel, shard);
                                    for (ArdentTrack track : queue) {
                                        manager.scheduler.manager.addToQueue(new ArdentTrack(track.getAuthor(), track
                                                .getAddedFrom(), track.getTrack().makeClone()));
                                    }
                                }
                                else shard.musicManagers.remove(Long.parseLong(guild.getId()));

                                if (channel == null) channel = guild.getPublicChannel();
                                try {
                                    shard.help.sendRetrievedTranslation(channel, "music", GuildUtils.getLanguage(guild),
                                            "autoresetplayer", null);
                                }
                                catch (Exception e) {
                                    new BotException(e);
                                }
                            }
                        }
                    }
                    else {
                        voiceChannelsAtZeroDuration.remove(guild.getId());
                    }
                }
            }
        }
    }
}
