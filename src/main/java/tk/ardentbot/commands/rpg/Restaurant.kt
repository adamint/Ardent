package tk.ardentbot.commands.rpg

import net.dv8tion.jda.core.entities.Guild
import net.dv8tion.jda.core.entities.Message
import net.dv8tion.jda.core.entities.MessageChannel
import net.dv8tion.jda.core.entities.User
import tk.ardentbot.core.executor.Command
import tk.ardentbot.core.executor.Subcommand

class Restaurant(commandSettings: CommandSettings) : Command(commandSettings) {
    // Only people who have ordered food (and has been delivered) will be able to play bar games
    // The food will disappear after the player does a certain amount of bar games - depends on the food
    // There will also be /restaurant drunkdial which will randomly connect you with another channel like DiscordTel
    // convert <b>ALL</b> games to bar games as well
    override fun noArgs(guild: Guild?, channel: MessageChannel?, user: User?, message: Message?, args: Array<out String>?) {
        sendHelp(channel, guild, user, this)
    }

    override fun setupSubcommands() {
        subcommands.add(object : Subcommand("", "", "order") {
            override fun onCall(guild: Guild?, channel: MessageChannel?, user: User?, message: Message?, args: Array<out String>?) {

            }
        })
    }

}