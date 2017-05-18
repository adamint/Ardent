package tk.ardentbot.commands.administration

import net.dv8tion.jda.core.entities.Guild
import net.dv8tion.jda.core.entities.Message
import net.dv8tion.jda.core.entities.MessageChannel
import net.dv8tion.jda.core.entities.User
import tk.ardentbot.core.executor.Command
import tk.ardentbot.core.executor.Subcommand
import tk.ardentbot.rethink.Database.connection
import tk.ardentbot.rethink.Database.r
import tk.ardentbot.rethink.models.GuildModel
import tk.ardentbot.rethink.models.RolePermission
import tk.ardentbot.utils.discord.MessageUtils

class Rankables(commandSettings: CommandSettings) : Command(commandSettings) {
    override fun noArgs(guild: Guild?, channel: MessageChannel?, user: User?, message: Message?, args: Array<String>?) {
        sendHelp(channel, guild, user, this)
    }

    override fun setupSubcommands() {
        subcommands.add(object : Subcommand("See what rankables your server administrators have set up", "list", "list") {
            override fun onCall(guild: Guild, channel: MessageChannel?, user: User, message: Message, args: Array<out String>?) {
                val builder = MessageUtils.getDefaultEmbed(user).setAuthor("Server rankables", guild.iconUrl, guild.iconUrl)
                val guildModel = asPojo(r.table("guilds").get(guild.id).run(connection), GuildModel::class.java)
                val rolePermissions: ArrayList<RolePermission>? = guildModel.role_permissions
                if (rolePermissions != null) {

                } else {
                    builder.setDescription("There are no rankables :( Setup some using /rankables setup and it will take you through the setup")
                }
                sendEmbed(builder, channel, user)
            }
        })
    }
}