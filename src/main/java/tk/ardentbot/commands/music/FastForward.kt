package tk.ardentbot.commands.music

import net.dv8tion.jda.core.entities.Guild
import net.dv8tion.jda.core.entities.Message
import net.dv8tion.jda.core.entities.MessageChannel
import net.dv8tion.jda.core.entities.User
import tk.ardentbot.commands.music.Music.getGuildAudioPlayer
import tk.ardentbot.commands.music.Music.sendTo
import tk.ardentbot.core.executor.Command
import tk.ardentbot.utils.discord.UserUtils

class FastForward(commandSettings: CommandSettings) : Command(commandSettings) {
    override fun noArgs(guild: Guild, channel: MessageChannel, user: User, message: Message, args: Array<out String>) {
        val audioManager = guild.audioManager
        val member = guild.getMember(user)
        if (audioManager.isConnected) {
            val manager = getGuildAudioPlayer(guild, channel)
            val ardentMusicManager = manager.scheduler.manager
            val track = ardentMusicManager.currentlyPlaying
            if (track != null) {
                var ownerId: String? = track.author
                if (ownerId == null) ownerId = ""
                if (UserUtils.hasManageServerOrStaff(member) || UserUtils.isBotCommander(member) || user.id.equals
                (ownerId, ignoreCase = true)) {
                    try {
                        val move = args[1].toInt() * 1000
                        val position = track.track.position
                        if (move + position > track.track.duration) {
                            sendTranslatedMessage("You can't skip further ahead than the duration of the song!", channel, user)
                            return
                        } else {
                            track.track.position = position + move
                            sendTranslatedMessage("Skipped ahead ${move / 1000} seconds", channel, user)
                        }
                    } catch(e: Exception) {
                        sendTranslatedMessage("You need to provide a valid integer to skip forward", channel, user)
                    }
                } else {
                    sendTranslatedMessage("You need to have queued the song or have the Manage Server permission", sendTo(channel, guild), user)
                }
            }
        } else
            sendTranslatedMessage("I'm not in a voice channel!", channel, user)
    }

    override fun setupSubcommands() {
    }
}