package tk.ardentbot.commands.music;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;
import tk.ardentbot.core.executor.Command;
import tk.ardentbot.utils.discord.GuildUtils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.BlockingQueue;

import static tk.ardentbot.commands.music.Music.*;

public class Queue extends Command {
    public Queue(CommandSettings commandSettings) {
        super(commandSettings);
    }

    @Override
    public void noArgs(Guild guild, MessageChannel channel, User user, Message message, String[] args) throws Exception {
        StringBuilder sb = new StringBuilder();
        String queuedBy = "queued by";
        sb.append("__Music Queue__\n");
        BlockingQueue<ArdentTrack> queue = getGuildAudioPlayer(guild, channel).scheduler.manager.getQueue();
        Iterator<ArdentTrack> iterator = queue.iterator();
        int current = 1;
        ArrayList<AudioTrack> trackList = new ArrayList<>();
        while (iterator.hasNext()) {
            ArdentTrack ardentTrack = iterator.next();
            AudioTrack track = ardentTrack.getTrack();
            trackList.add(track);
            sb.append("#" + current + ": " + track.getInfo().title + ": " + track.getInfo().author + " " +
                    getDuration(track) + "\n     *" + queuedBy + " " + GuildUtils.getShard(guild).jda
                    .getUserById(ardentTrack.getAuthor()).getName()
                    + "*\n");
            current++;
        }
        if (current == 1) {
            sb.append("There aren't any songs in the queue!");
        }
        sendTranslatedMessage(sb.toString(), sendTo(channel, guild), user);
    }

    @Override
    public void setupSubcommands() throws Exception {

    }
}
