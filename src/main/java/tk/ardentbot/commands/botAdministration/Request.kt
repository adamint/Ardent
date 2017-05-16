package tk.ardentbot.commands.botAdministration

import net.dv8tion.jda.core.entities.Guild
import net.dv8tion.jda.core.entities.Message
import net.dv8tion.jda.core.entities.MessageChannel
import net.dv8tion.jda.core.entities.User
import tk.ardentbot.core.executor.Ratelimitable
import tk.ardentbot.main.Ardent.botLogsShard
import tk.ardentbot.utils.discord.GuildUtils


class Request(commandSettings: CommandSettings) : Ratelimitable(commandSettings) {
    override fun noArgs(guild: Guild, channel: MessageChannel?, user: User, message: Message, args: Array<out String>?) {
        if (args!!.size == 1) {
            sendTranslatedMessage("Request a feature by typing /request and then your request", channel, user)
            return
        }
        val prefix = GuildUtils.getPrefix(guild)
        val request = message.rawContent.replace(prefix + args[0] + " ", "")
        val ideasChannel = botLogsShard.jda.getTextChannelById("262810786186002432")
        ideasChannel.sendMessage("Request: " + user.name + "#" + user.discriminator + " (" +
                guild.name + "): " + request).queue()
        sendTranslatedMessage("Sent your request to Ardent's owner! He will message you if your suggestion will be implemented", channel, user)
    }

    override fun setupSubcommands() {
    }
}