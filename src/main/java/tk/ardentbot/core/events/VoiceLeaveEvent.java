package tk.ardentbot.core.events;

import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.events.guild.voice.GuildVoiceLeaveEvent;
import net.dv8tion.jda.core.hooks.SubscribeEvent;
import tk.ardentbot.commands.music.GuildMusicManager;
import tk.ardentbot.commands.music.Music;

public class VoiceLeaveEvent {
    @SubscribeEvent
    public void onVoiceLeaveEvent(GuildVoiceLeaveEvent event) throws Exception {
        Guild guild = event.getGuild();
        GuildMusicManager musicManager = Music.getGuildAudioPlayer(guild, null);
        assert musicManager != null;
        if (event.getMember().getUser().getId().equalsIgnoreCase(guild.getSelfMember().getUser().getId())) {
            musicManager.scheduler.manager.resetQueue();
        }
    }
}
