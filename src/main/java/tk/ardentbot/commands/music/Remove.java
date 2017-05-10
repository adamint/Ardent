package tk.ardentbot.commands.music;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.managers.AudioManager;
import tk.ardentbot.core.executor.Command;
import tk.ardentbot.utils.discord.UserUtils;

import java.util.Iterator;
import java.util.concurrent.BlockingQueue;

import static tk.ardentbot.commands.music.Music.getGuildAudioPlayer;
import static tk.ardentbot.commands.music.Music.sendTo;

public class Remove extends Command {
    public Remove(CommandSettings commandSettings) {
        super(commandSettings);
    }

    @Override
    public void noArgs(Guild guild, MessageChannel channel, User user, Message message, String[] args) throws Exception {
        if (args.length > 1) {
            AudioManager audioManager = guild.getAudioManager();
            Member member = guild.getMember(user);
            if (audioManager.isConnected()) {
                try {
                    GuildMusicManager manager = getGuildAudioPlayer(guild, channel);
                    BlockingQueue<ArdentTrack> queue = manager.scheduler.manager.getQueue();
                    int numberToRemove = Integer.parseInt(args[1]) - 1;
                    if (numberToRemove >= queue.size() || numberToRemove < 0)
                        sendTranslatedMessage("Invalid arguments", channel, user);
                    else {
                        Iterator<ArdentTrack> iterator = queue.iterator();
                        int current = 0;
                        while (iterator.hasNext()) {
                            ArdentTrack ardentTrack = iterator.next();
                            AudioTrack track = ardentTrack.getTrack();
                            String name = track.getInfo().title;
                            if (current == numberToRemove) {
                                if (UserUtils.hasManageServerOrStaff(member) || ardentTrack.getAuthor()
                                        .equalsIgnoreCase(user.getId()) || UserUtils.isBotCommander(member))
                                {
                                    queue.remove(ardentTrack);
                                    sendTranslatedMessage("Removed {0} from the queue".replace("{0}", name), sendTo(channel,
                                            guild), user);
                                }
                                else {
                                    sendTranslatedMessage("You need to have queued the song or have the Manage Server " +
                                            "permission", sendTo(channel, guild), user);
                                }
                            }
                            current++;
                        }
                    }
                }
                catch (NumberFormatException ex) {
                    sendTranslatedMessage("Invalid arguments", channel, user);
                }
            }
            else sendTranslatedMessage("I'm not in a voice channel!", channel, user);
        }
        else sendTranslatedMessage("That's not a number!", channel, user);
    }

    @Override
    public void setupSubcommands() throws Exception {

    }
}
