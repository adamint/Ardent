package tk.ardentbot.commands.botinfo

import net.dv8tion.jda.core.entities.Guild
import net.dv8tion.jda.core.entities.Message
import net.dv8tion.jda.core.entities.MessageChannel
import net.dv8tion.jda.core.entities.User
import tk.ardentbot.core.executor.BaseCommand
import tk.ardentbot.core.executor.Command

class Donate(commandSettings: BaseCommand.CommandSettings) : Command(commandSettings) {
    override fun noArgs(guild: Guild?, channel: MessageChannel?, user: User?, message: Message?, args: Array<out String>?) {
        sendTranslatedMessage("Want to support Ardent? We need your help! Pledge at https://patreon.com/ardent and receive perks and the " +
                "satisfaction of helping us maintain our bot, used by over 100,000 people !\n" +
                " You can also donate directly at https://www.paypal.me/ardentbot", channel, user)
    }


    override fun setupSubcommands() {
    }
}