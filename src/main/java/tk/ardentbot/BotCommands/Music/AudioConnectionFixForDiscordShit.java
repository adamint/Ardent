package tk.ardentbot.BotCommands.Music;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.TextChannel;
import tk.ardentbot.Core.LoggingUtils.BotException;
import tk.ardentbot.Utils.Discord.GuildUtils;

import java.util.HashMap;

import static tk.ardentbot.Main.Ardent.ardent;

public class AudioConnectionFixForDiscordShit implements Runnable {
    private static HashMap<String, String> voiceChannelsAtZeroDuration = new HashMap<>();

    @Override
    public void run() {
        for (Guild guild : ardent.jda.getGuilds()) {
            GuildMusicManager guildMusicManager = Music.getGuildAudioPlayer(guild, null);
            AudioPlayer player = guildMusicManager.player;
            if (!player.isPaused()) {
                AudioTrack playingTrack = player.getPlayingTrack();
                if (playingTrack != null) {
                    long position = playingTrack.getPosition();
                    if (position == 0) {
                        String textChannelId = voiceChannelsAtZeroDuration.get(guild.getId());
                        if (textChannelId == null) {
                            ArdentMusicManager ardentMusicManager = guildMusicManager.scheduler.manager;
                            TextChannel channel = ardentMusicManager.getChannel();
                            voiceChannelsAtZeroDuration.put(guild.getId(), channel.getId());
                        }
                        else {
                            ardent.musicManagers.remove(Long.parseLong(guild.getId()));
                            TextChannel channel = guild.getTextChannelById(textChannelId);
                            if (channel == null) channel = guild.getPublicChannel();
                            try {
                                ardent.help.sendRetrievedTranslation(channel, "music", GuildUtils.getLanguage(guild),
                                        "autoresetplayer", null);
                            }
                            catch (Exception e) {
                                new BotException(e);
                            }
                        }
                    }
                }
                else voiceChannelsAtZeroDuration.remove(guild.getId());
            }
        }
    }
}
