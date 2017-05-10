package tk.ardentbot.commands.music;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.managers.AudioManager;
import tk.ardentbot.core.executor.Command;

import java.util.Iterator;
import java.util.concurrent.BlockingQueue;

import static tk.ardentbot.commands.music.Music.getGuildAudioPlayer;

public class GetUrl extends Command {
    public GetUrl(CommandSettings commandSettings) {
        super(commandSettings);
    }

    @Override
    public void noArgs(Guild guild, MessageChannel channel, User user, Message message, String[] args) throws Exception {
        AudioManager audioManager = guild.getAudioManager();
        if (audioManager.isConnected()) {
            try {
                GuildMusicManager manager = getGuildAudioPlayer(guild, channel);
                BlockingQueue<ArdentTrack> queue = manager.scheduler.manager.getQueue();
                if (args.length > 1) {
                    int numberToRemove = Integer.parseInt(args[1]) - 1;
                    if (numberToRemove >= queue.size() || numberToRemove < 0)
                        sendTranslatedMessage("Invalid arguments", channel, user);
                    else {
                        Iterator<ArdentTrack> iterator = queue.iterator();
                        int current = 0;
                        while (iterator.hasNext()) {
                            ArdentTrack ardentTrack = iterator.next();
                            AudioTrack track = ardentTrack.getTrack();
                            AudioTrackInfo info = track.getInfo();
                            String name = info.title;
                            if (current == numberToRemove) {
                                sendTranslatedMessage("The streaming link for {0} is {1}".replace("{0}", info.title).replace
                                        ("{1}", info
                                                .uri), channel, user);
                                return;
                            }
                            current++;
                        }
                    }
                }
                else {
                    ArdentMusicManager musicManager = manager.scheduler.manager;
                    ArdentTrack track = musicManager.getCurrentlyPlaying();
                    if (track != null) {
                        AudioTrackInfo info = track.getTrack().getInfo();
                        sendTranslatedMessage("The streaming link for {0} is {1}".replace("{0}", info.title).replace("{1}", info
                                .uri), channel, user);
                    }
                    else {
                        sendTranslatedMessage("I'm not playing anything right now!", channel, user);
                    }
                }
            }
            catch (NumberFormatException ex) {
                sendTranslatedMessage("Invalid arguments", channel, user);
            }
        }
        else sendTranslatedMessage("I'm not in a voice channel!", channel, user);
    }

    @Override
    public void setupSubcommands() throws Exception {

    }
}
