package tk.ardentbot.Core.Events;

import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.events.guild.voice.GuildVoiceLeaveEvent;
import net.dv8tion.jda.core.hooks.SubscribeEvent;
import tk.ardentbot.BotCommands.Music.ArdentTrack;
import tk.ardentbot.BotCommands.Music.GuildMusicManager;
import tk.ardentbot.BotCommands.Music.Music;
import tk.ardentbot.Utils.Discord.GuildUtils;

public class VoiceLeaveEvent {
    @SubscribeEvent
    public void onVoiceLeaveEvent(GuildVoiceLeaveEvent event) throws Exception {
        Guild guild = event.getGuild();
        GuildMusicManager musicManager = Music.getGuildAudioPlayer(guild, null);
        assert musicManager != null;
        if (event.getMember().getUser().getId().equalsIgnoreCase(guild.getSelfMember().getUser().getId())) {
            musicManager.scheduler.manager.resetQueue();
        }
        else {
            ArdentTrack track = musicManager.scheduler.manager.getCurrentlyPlaying();
            assert track != null;
            if (track.getVotedToSkip().size() >= Math.round(event.getChannelLeft().getMembers().size())) {
                GuildUtils.getShard(guild).help.sendRetrievedTranslation(musicManager.scheduler.manager.getChannel(), "music", GuildUtils
                        .getLanguage(guild), "skippedtrack", guild.getSelfMember().getUser());
                musicManager.scheduler.manager.nextTrack();
            }
        }
    }
}
