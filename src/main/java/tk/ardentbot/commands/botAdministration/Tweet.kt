package tk.ardentbot.commands.botAdministration

import net.dv8tion.jda.core.entities.Guild
import net.dv8tion.jda.core.entities.Message
import net.dv8tion.jda.core.entities.MessageChannel
import net.dv8tion.jda.core.entities.User
import tk.ardentbot.core.executor.Ratelimitable
import tk.ardentbot.main.Ardent
import tk.ardentbot.main.Ardent.botLogsShard
import tk.ardentbot.utils.discord.GuildUtils

class Tweet(commandSettings: CommandSettings) : Ratelimitable(commandSettings) {
    override fun noArgs(guild: Guild, channel: MessageChannel, user: User, message: Message, args: Array<out String>) {
        if (Ardent.developers.contains(user.id)) {
            val content = message.rawContent.replace(GuildUtils.getPrefix(guild) + args[0], "")
            val status = Ardent.twitter.tweets().updateStatus(content)
            val sb = StringBuilder()
            sb.append("**New Tweet** by Ardent\n" + content + "\n\nIf you liked this, follow us on twitter & like the post - " +
                    "https://twitter.com/ardentbot/status/" + status.id + "\n@everyone")
            botLogsShard.jda.getTextChannelById("272411413031419904").sendMessage(sb.toString()).queue()
        } else sendTranslatedMessage("You need to be an **Ardent developer** to be able to send Tweets from our twitter account!", channel, user)
    }

    override fun setupSubcommands() {
    }

}
