package tk.ardentbot.commands.`fun`

import at.mukprojects.giphy4j.Giphy
import at.mukprojects.giphy4j.exception.GiphyException
import net.dv8tion.jda.core.entities.Guild
import net.dv8tion.jda.core.entities.Message
import net.dv8tion.jda.core.entities.MessageChannel
import net.dv8tion.jda.core.entities.User
import tk.ardentbot.core.executor.Command
import tk.ardentbot.main.Ardent.globalExecutorService
import java.util.*
import java.util.concurrent.TimeUnit


class GIF(commandSettings: CommandSettings) : Command(commandSettings) {
    internal var cooldown = ArrayList<User>()

    @Throws(Exception::class)
    override fun noArgs(guild: Guild, channel: MessageChannel, user: User, message: Message, args: Array<String>) {
        setupCategories()
        val author = message.author
        if (cooldown.contains(author))
            sendTranslatedMessage("Please wait a few seconds to send this command again.", channel, user)
        else {
            try {
                channel.sendMessage(giphy.searchRandom(randomMemeCategory()).data.imageUrl).queue()
            } catch (e: GiphyException) {
                e.printStackTrace()
            }

        }
        cooldown.add(author)
        globalExecutorService.schedule<Boolean>({ cooldown.remove(author) }, 4, TimeUnit.SECONDS)
    }

    override fun setupSubcommands() {}

    companion object {
        var giphy = Giphy("dc6zaTOxFJmzC")
        var categories = ArrayList<String>()

        fun setupCategories() {
            categories.clear()
            categories.add("no")
            categories.add("funny")
            categories.add("cat")
            categories.add("puppy")
            categories.add("sherlock")
            categories.add("supernatural")
            categories.add("love")
            categories.add("happy")
        }

        private fun randomMemeCategory(): String {
            val random = Random()
            return categories[random.nextInt(categories.size)]
        }
    }
}
