package tk.ardentbot.commands.music;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.core.entities.*;
import tk.ardentbot.core.executor.Command;
import tk.ardentbot.utils.discord.UserUtils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.BlockingQueue;

import static tk.ardentbot.commands.music.Music.getGuildAudioPlayer;
import static tk.ardentbot.commands.music.Music.sendTo;

public class Loop extends Command {
    public Loop(CommandSettings commandSettings) {
        super(commandSettings);
    }

    @Override
    public void noArgs(Guild guild, MessageChannel channel, User user, Message message, String[] args) throws Exception {
        if (args.length == 3) {
            if (UserUtils.hasManageServerOrStaff(guild.getMember(user))) {
                try {
                    int songsToLoop = Integer.parseInt(args[1]);
                    int amountOfTimes = Integer.parseInt(args[2]);

                    GuildMusicManager guildMusicManager = getGuildAudioPlayer(guild, channel);
                    TrackScheduler trackScheduler = guildMusicManager.scheduler;
                    BlockingQueue<ArdentTrack> queue = trackScheduler.manager.getQueue();
                    int amountOfTracks = queue.size();

                    if (songsToLoop < 0 || songsToLoop > amountOfTracks) {
                        sendTranslatedMessage("Impossible to loop", channel, user);
                    }
                    else {
                        if (amountOfTimes < 0 || amountOfTimes > 3) {
                            sendTranslatedMessage("You can't loop more than 3 times", channel, user);
                        }
                        else {
                            ArrayList<AudioTrack> tracksToLoop = new ArrayList<>();
                            Iterator<ArdentTrack> trackIterator = queue.iterator();
                            for (int i = 0; i < songsToLoop; i++) {
                                ArdentTrack ardentTrack = trackIterator.next();
                                tracksToLoop.add(ardentTrack.getTrack());
                            }
                            for (int i = 0; i < amountOfTimes; i++) {
                                tracksToLoop.forEach(track -> {
                                    trackScheduler.manager.addToQueue(new ArdentTrack(user.getId(),
                                            (TextChannel) channel, track.makeClone()));
                                });
                            }
                            sendTranslatedMessage("Added a loop for {0} songs in the queue{1} times".replace("{0}", String
                                            .valueOf(songsToLoop)).replace("{1}", String.valueOf(amountOfTimes)), sendTo(channel,
                                    guild),
                                    user);
                        }
                    }
                }
                catch (NumberFormatException ex) {
                    sendTranslatedMessage("That's not a number!", channel, user);
                }
            }
            else sendTranslatedMessage("You need the Manage Server permission to do this", channel, user);
        }
        else sendTranslatedMessage("Use /music loop [# in queue] [# of times to loop]", channel, user);
    }

    @Override
    public void setupSubcommands() throws Exception {

    }
}
