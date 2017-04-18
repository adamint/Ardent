package tk.ardentbot.commands.fun;

import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;
import tk.ardentbot.core.executor.Command;
import tk.ardentbot.core.translation.Language;
import tk.ardentbot.utils.searching.GoogleSearch;
import tk.ardentbot.utils.searching.SearchResult;

import java.util.List;

public class GSearch extends Command {
    public GSearch(CommandSettings commandSettings) {
        super(commandSettings);
    }

    @Override
    public void noArgs(Guild guild, MessageChannel channel, User user, Message message, String[] args, Language language) throws Exception {
        if (args.length > 1) {
            try {
                List<SearchResult> results = GoogleSearch.performSearch(
                        "018291224751151548851%3Ajzifriqvl1o",
                        replace(message.getContent(), 1));
                sendTranslatedMessage(results.get(0).getSuggestedReturn(), channel, user);
            }
            catch (Exception ex) {
                sendRetrievedTranslation(channel, "other", language, "cannotsearch", user);
            }
        }
        else {
            sendRetrievedTranslation(channel, "other", language, "includesearchterm", user);
        }
    }

    @Override
    public void setupSubcommands() throws Exception {
    }
}