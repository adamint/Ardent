package tk.ardentbot.core.events

import net.dv8tion.jda.core.events.guild.member.GuildMemberRoleAddEvent
import net.dv8tion.jda.core.hooks.SubscribeEvent

class RoleAddRemoveEvents {
    @SubscribeEvent
    fun onRoleAdd(event: GuildMemberRoleAddEvent)
}