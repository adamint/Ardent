package tk.ardentbot.commands.`fun`

import net.dv8tion.jda.core.entities.Guild
import net.dv8tion.jda.core.entities.Message
import net.dv8tion.jda.core.entities.MessageChannel
import net.dv8tion.jda.core.entities.User
import tk.ardentbot.core.executor.BaseCommand
import tk.ardentbot.core.executor.Command
import tk.ardentbot.utils.discord.GuildUtils
import java.util.*
import java.util.concurrent.TimeUnit

class Roll(commandSettings: BaseCommand.CommandSettings) : Command(commandSettings) {

    @Throws(Exception::class)
    override fun noArgs(guild: Guild, channel: MessageChannel, user: User, message: Message, args: Array<String>) {
        val before = "Rolling a 6-sided die now..."
        val after = " Rolling a 6-sided die now... Result: {0}"

        channel.sendMessage(before).queue { message1 ->
            GuildUtils.getShard(guild).executorService.schedule({
                val roll = Random().nextInt(6) + 1
                message1.editMessage(after.replace("{0}", roll.toString())).queue()
            }, 2, TimeUnit.SECONDS)
        }
    }

    override fun setupSubcommands() {

    }
}