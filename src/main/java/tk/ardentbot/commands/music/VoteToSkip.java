package tk.ardentbot.commands.music;

import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.managers.AudioManager;
import tk.ardentbot.core.executor.Command;

import java.util.stream.Collectors;

import static tk.ardentbot.commands.music.Music.getGuildAudioPlayer;

public class VoteToSkip extends Command {
    public VoteToSkip(CommandSettings commandSettings) {
        super(commandSettings);
    }

    @Override
    public void noArgs(Guild guild, MessageChannel channel, User user, Message message, String[] args) throws Exception {
        AudioManager audioManager = guild.getAudioManager();
        VoiceChannel connected = audioManager.getConnectedChannel();
        if (connected != null && connected.getMembers().stream().filter((member -> member.getUser().getId().equals(user.getId())))
                .collect(Collectors.toList()).size() > 0)
        {
            GuildMusicManager guildMusicManager = getGuildAudioPlayer(guild, channel);
            ArdentTrack track = guildMusicManager.scheduler.manager.getCurrentlyPlaying();
            if (track == null) {
                sendTranslatedMessage("I'm not playing anything right now!", channel, user);
                return;
            }
            if (track.getVotedToSkip().contains(user.getId())) {
                sendTranslatedMessage("You already voted to skip >.>", channel, user);
                return;
            }
            track.addSkipVote(user);
            if (track.getVotedToSkip().size() >= Math.round(connected.getMembers().size() / 2)) {
                sendTranslatedMessage("Half of the people in the channel voted to skip the current song", channel, user);
                guildMusicManager.scheduler.manager.nextTrack();
            }
            else
                sendTranslatedMessage("Your vote to skip has been recorded. You need half of the users in the channel to force a " +
                        "skip", channel, user);

        }
        else sendTranslatedMessage("Either I or you aren't in a voice channel", channel, user);
    }

    @Override
    public void setupSubcommands() {

    }
}
