package tk.ardentbot.commands.nsfw

import net.dv8tion.jda.core.entities.Guild
import net.dv8tion.jda.core.entities.Message
import net.dv8tion.jda.core.entities.MessageChannel
import net.dv8tion.jda.core.entities.User
import org.jsoup.Jsoup
import tk.ardentbot.core.executor.Command
import java.net.URL
import java.security.SecureRandom

class Men(commandSettings: CommandSettings) : Command(commandSettings) {
    val men = mutableListOf<String>()
    override fun noArgs(guild: Guild?, channel: MessageChannel?, user: User?, message: Message?, args: Array<out String>?) {
        if (men.size == 0) {
            addMen("http://imgur.com/a/oVGSO")
        }
        if (NSFW.canSendNSFW(user, channel, guild, this)) {
            sendTranslatedMessage(men[SecureRandom().nextInt(men.size)], channel, user)
        }
    }

    private fun addMen(site: String) {
        val menList = Jsoup.parse(URL(site), 10000)
        val links = menList.getElementsByTag("img")
        links.forEach({ link ->
            val url = link.attr("src")
            if (url.contains("//i.imgur.com")) men.add("https:" + url)
        })
    }

    override fun setupSubcommands() {
    }
}