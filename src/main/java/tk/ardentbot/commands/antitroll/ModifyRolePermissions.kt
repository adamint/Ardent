package tk.ardentbot.commands.antitroll

import net.dv8tion.jda.core.entities.Guild
import net.dv8tion.jda.core.entities.Message
import net.dv8tion.jda.core.entities.MessageChannel
import net.dv8tion.jda.core.entities.User
import tk.ardentbot.core.executor.BaseCommand
import tk.ardentbot.core.executor.Command
import tk.ardentbot.core.executor.Subcommand
import tk.ardentbot.rethink.Database.connection
import tk.ardentbot.rethink.Database.r
import tk.ardentbot.rethink.models.GuildModel
import tk.ardentbot.rethink.models.RolePermission
import tk.ardentbot.utils.discord.MessageUtils

class ModifyRolePermissions(commandSettings: CommandSettings) : Command(commandSettings) {
    override fun noArgs(guild: Guild?, channel: MessageChannel?, user: User?, message: Message?, args: Array<out String>?) {
        sendHelp(channel, guild, user, this)
    }

    override fun setupSubcommands() {
        subcommands.add(object : Subcommand("View available settings you can change for both roles and rankables", "help", "help") {
            override fun onCall(guild: Guild, channel: MessageChannel, user: User, message: Message, args: Array<String>) {
                val builder = MessageUtils.getDefaultEmbed(user).setAuthor("Role permission help", "https://ardentbot.tk", user.effectiveAvatarUrl)
                builder.setDescription("""== **Role Permissions** ==

**canusecommands** *<true/false>*: Block from or allow people with a specific role to use Ardent commands
**cansendlinks** *<true/false>*: Don't trust new members? Automatically remove links they send if this setting is false
**cansendinvites** *<true/false>*: Having this set to false means that users with this role cannot send invites to other Discord servers

== **Rankable Settings** ==

**setrankable** *<true/false>*: Make a role into a `Rankable` role, used before the following subcommand
**startonserverjoin** *<true/false>*: Start or stop the rankable timer for users right after they join the server
**afteraddingthisrole** *<role name you want>*: Read carefully. 1) the above setting must be **false** for this to work.
When the role you provide is added to a user, the rankable timer will start **then**
**setdaystowait** *<amount of days - decimals ALLOWED>*: Set the amount of days before this role is added to the user (after they've
achieved the rankable requirement)""")
                sendEmbed(builder, channel, user)
            }
        })
        subcommands.add(object : Subcommand("Change role permissions using this!", "set @MentionRole <setting> <true/false>", "set") {
            override fun onCall(guild: Guild, channel: MessageChannel, user: User, message: Message, args: Array<out String>) {
                if (args.size == 4) {
                    val mentionedRoles = message.mentionedRoles
                    if (mentionedRoles.size == 1) {
                        val role = mentionedRoles[0]
                        val setTo = when (args[3]) {
                            "false" -> false
                            "true" -> true
                            else -> {
                                sendTranslatedMessage("You need to include either true or false!", channel, user)
                                return
                            }
                        }
                        var gm: GuildModel? = BaseCommand.asPojo(r.table("guilds").get(guild.id).run(connection), GuildModel::class.java)
                        if (gm == null) {
                            gm = GuildModel(guild.id, "english", "/")
                            r.table("guilds").insert(r.json(gson.toJson(gm))).runNoReply(connection)
                        }
                        if (gm.role_permissions == null) gm.role_permissions = arrayListOf<RolePermission>()
                        var rolePermission: RolePermission? = null
                        gm.role_permissions.forEach {
                            r ->
                            if (r.id == role.id) rolePermission = r
                        }
                        if (rolePermission == null) rolePermission = RolePermission(role.id, rankable = null)
                        when (args[2]) {
                            "canusecommands" -> {

                            }
                            else -> sendTranslatedMessage("Type /permissions help to see a list of available settings", channel, user)
                        }
                        return
                    }
                    sendTranslatedMessage("You need to mention a role >.>", channel, user)
                } else sendTranslatedMessage("Please type /permissions to see how to use this subcommand", channel, user)
            }
        })
        subcommands.add(object : Subcommand("Set certain roles as `Rankables`, meaning that people can rank up to obtain specific roles after " +
                "a set period of time - either after joining the server or after being added to another role!", "rankable setup", "rankable") {
            override fun onCall(guild: Guild?, channel: MessageChannel?, user: User?, message: Message?, args: Array<out String>?) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }
        })
    }
}