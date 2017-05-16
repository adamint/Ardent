package tk.ardentbot.core.events

import net.dv8tion.jda.core.events.guild.member.GuildMemberRoleAddEvent
import net.dv8tion.jda.core.events.guild.member.GuildMemberRoleRemoveEvent
import net.dv8tion.jda.core.hooks.SubscribeEvent
import tk.ardentbot.core.executor.BaseCommand
import tk.ardentbot.rethink.Database.connection
import tk.ardentbot.rethink.Database.r
import tk.ardentbot.rethink.models.GuildModel
import tk.ardentbot.rethink.models.Rankable
import java.time.Instant

class RoleAddRemoveEvents {
    @SubscribeEvent
    fun onRoleAdd(event: GuildMemberRoleAddEvent) {
        val guild = event.guild
        val roles = event.roles
        val user = event.member.user
        val guildModel = BaseCommand.asPojo(r.table("guilds").get(guild.id).run(connection), GuildModel::class.java)
        guildModel.role_permissions.forEach {
            rolePermission ->
            val rankable: Rankable? = rolePermission.rankable ?: return
            if (!rankable?.startsOnServerJoin!! && roles.contains(guild.getRoleById(rankable.startsOnAddedThisRole))) {
                rankable.queued.putIfAbsent(user.id, Instant.now().epochSecond)
            }
        }
    }

    @SubscribeEvent
    fun onRoleRemove(event: GuildMemberRoleRemoveEvent) {
        val guild = event.guild
        val roles = event.roles
        val user = event.member.user
        val guildModel = BaseCommand.asPojo(r.table("guilds").get(guild.id).run(connection), GuildModel::class.java)
        guildModel.role_permissions.forEach {
            rolePermission ->
            val rankable: Rankable? = rolePermission.rankable ?: return
            if (!rankable?.startsOnServerJoin!! && roles.contains(guild.getRoleById(rankable.startsOnAddedThisRole))) {
                rankable.queued.remove(user.id)
            }
        }

    }
}