package tk.ardentbot.commands.music

import net.dv8tion.jda.core.entities.*
import tk.ardentbot.commands.music.Music.getOutputChannel
import tk.ardentbot.core.executor.Command
import tk.ardentbot.rethink.Database.connection
import tk.ardentbot.rethink.Database.r
import tk.ardentbot.rethink.models.MusicSettingsModel

class AnnounceMusic(commandSettings: CommandSettings) : Command(commandSettings) {
    override fun noArgs(guild: Guild?, channel: MessageChannel?, user: User?, message: Message?, args: Array<out String>?) {
        if (args?.size!! > 1) {
            var guildMusicSettings: MusicSettingsModel? = asPojo(r.db("data").table("music_settings").get(guild?.id).run(connection),
                    MusicSettingsModel::class.java)
            if (guildMusicSettings == null) {
                guildMusicSettings = MusicSettingsModel(guild?.id, false, "none")
                r.table("music_settings").insert(gson.toJson(guildMusicSettings)).run<Any>(connection)
            }
            val arg = args[1]
            if (arg.equals("true", true)) {
                val outputChannel: TextChannel? = getOutputChannel(guild)
                if (outputChannel == null) {
                    sendTranslatedMessage("You need to set a music output channel first! Use the /musicoutput to set the text channel",
                            channel, user)
                    return
                }
                r.table("music_settings").get(guild?.id).update(r.hashMap("announce_music", true)).default_(true).run<Any>(connection)
                sendTranslatedMessage("I now will send a message when each new song starts", channel, user)
            } else if (arg.equals("false", true)) {
                r.table("music_settings").get(guild?.id).update(r.hashMap("announce_music", false)).run<Any>(connection)
                sendTranslatedMessage("I won't announce new songs", channel, user)
            }
        } else {
            val guildMusicSettings: MusicSettingsModel? = asPojo(r.db("data").table("music_settings").get(guild?.id).run(connection),
                    MusicSettingsModel::class.java)
            if (guildMusicSettings == null) {
                sendTranslatedMessage("I will **not** announce new songs when they start playing. Type /announcemusic [true/false] to set this",
                        channel, user)
                r.db("data").table("music_settings").insert(r.json(gson.toJson(MusicSettingsModel(guild?.id!!, false,
                        "none")))).run<Any>(connection)
            } else {
                if (guildMusicSettings.announce_music) {
                    sendTranslatedMessage("I **will** announce new songs when they start playing. Type /announcemusic [true/false] to set this",
                            channel, user)
                } else sendTranslatedMessage("I will **not** announce new songs when they start playing. Type /announcemusic [true/false] to set this",
                        channel, user)
            }
        }
    }

    override fun setupSubcommands() {
    }

}
