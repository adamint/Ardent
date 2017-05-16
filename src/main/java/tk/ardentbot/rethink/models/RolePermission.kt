package tk.ardentbot.rethink.models

data class RolePermission(var id: String, var canUseArdentCommands: Boolean = true, val rankable: Rankable)

data class Rankable(var roleId: String, var startsOnServerJoin: Boolean = false, var startsOnAddedThisRole: String, var secondsToWait: Long, var queued: MutableList<Pair<String, Long>>)
