package tk.ardentbot.commands.nsfw;

import com.mashape.unirest.http.Unirest;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;
import org.json.JSONArray;
import tk.ardentbot.core.executor.Command;
import tk.ardentbot.core.translate.Language;

public class Tits extends Command {
    public Tits(CommandSettings commandSettings) {
        super(commandSettings);
    }

    @Override
    public void noArgs(Guild guild, MessageChannel channel, User user, Message message, String[] args, Language language) throws Exception {
        if (NSFW.canSendNSFW(user, channel, guild, language, this)) {
            sendTranslatedMessage("http://media.oboobs.ru/" + new JSONArray(Unirest.get("http://api.oboobs.ru/boobs/0/1/random").asString
                    ().getBody()).getJSONObject(0).getString("preview"), channel, user);
        }

    }

    @Override
    public void setupSubcommands() throws Exception {
    }
}
