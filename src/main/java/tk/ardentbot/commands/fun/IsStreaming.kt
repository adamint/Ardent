package tk.ardentbot.commands.`fun`

import com.mb3364.twitch.api.Twitch
import com.mb3364.twitch.api.handlers.ChannelResponseHandler
import com.mb3364.twitch.api.handlers.StreamResponseHandler
import com.mb3364.twitch.api.models.Channel
import com.mb3364.twitch.api.models.Stream
import net.dv8tion.jda.core.entities.Guild
import net.dv8tion.jda.core.entities.Message
import net.dv8tion.jda.core.entities.MessageChannel
import net.dv8tion.jda.core.entities.User
import tk.ardentbot.core.executor.BaseCommand
import tk.ardentbot.core.executor.Command
import tk.ardentbot.utils.discord.MessageUtils
import java.text.NumberFormat
import java.util.*

class IsStreaming(commandSettings: BaseCommand.CommandSettings) : Command(commandSettings) {
    val twitch = Twitch()
    override fun noArgs(guild: Guild?, channel: MessageChannel?, user: User?, message: Message?, args: Array<out String>?) {
        if (args?.size == 1) {
            sendTranslatedMessage("You need to specify a streamer's name!", channel, user)
            return
        }
        val channelName = message?.rawContent?.replace(args!![0] + " ", "")
        twitch.channels().get(channelName, object : ChannelResponseHandler {
            override fun onSuccess(twitchChannel: Channel?) {
                twitch.streams().get(twitchChannel?.name, object : StreamResponseHandler {
                    override fun onSuccess(stream: Stream?) {
                        val builder = MessageUtils.getDefaultEmbed(user)
                        builder.setAuthor("${twitchChannel?.name} on twitch", twitchChannel?.url, twitchChannel?.logo)
                        if (twitchChannel?.videoBanner == null) {
                            builder.setThumbnail(twitchChannel?.logo)
                        } else {
                            builder.setThumbnail(twitchChannel.videoBanner)
                        }
                        builder.addField("Display name", twitchChannel?.displayName, true)
                        builder.addField("Twitch URL", "[click here](${twitchChannel?.url})", true)
                        if (twitchChannel?.status != null) {
                            builder.addField("Status", twitchChannel.status, true)
                        }
                        if (stream?.isOnline != null) {
                            builder.addField("Currently streaming", (stream.isOnline).toString(), true)
                            builder.addField("Game", stream.game, true)
                            builder.addField("Viewers", NumberFormat.getNumberInstance(Locale.US).format(stream.viewers), true)
                            builder.addField("Average FPS", stream.averageFps.toInt().toString() + " frames/second", true)
                            builder.setThumbnail(null)
                            builder.setImage(stream.preview.medium)
                        } else {
                            builder.addField("Currently streaming", "false", true)
                        }
                        builder.addField("Views", NumberFormat.getNumberInstance(Locale.US).format(twitchChannel?.views), true)
                        builder.addField("Followers", NumberFormat.getNumberInstance(Locale.US).format(twitchChannel?.followers), true)
                        builder.addField("Creation Date", twitchChannel?.createdAt?.toLocaleString(), true)
                        builder.addField("Partnered channel", twitchChannel?.isPartner.toString(), true)
                        builder.addField("Mature content", twitchChannel?.isMature.toString(), true)
                        builder.addField("Language", twitchChannel?.broadcasterLanguage, true)
                        sendEmbed(builder, channel, user)
                    }

                    override fun onFailure(error: Throwable?) {
                        sendTranslatedMessage("Failed to get information about that streamer... make sure you're entering it correctly",
                                channel, user)
                    }

                    override fun onFailure(statusCode: Int, statusMessage: String?, errorMessage: String?) {
                        sendTranslatedMessage("Failed to retrieve information. Error:\n```$errorMessage```", channel, user)
                        println("$statusCode + $statusMessage + $errorMessage")
                    }
                })
            }

            override fun onFailure(error: Throwable?) {
                sendTranslatedMessage("Failed to get information about that streamer... make sure you're entering it correctly",
                        channel, user)
            }

            override fun onFailure(statusCode: Int, statusMessage: String?, errorMessage: String?) {
                sendTranslatedMessage("Failed to retrieve information. Error:\n```$errorMessage```", channel, user)
            }
        })
    }

    override fun setupSubcommands() {
    }

    fun setClientId(id: String?): IsStreaming {
        twitch.clientId = id
        return this
    }
}