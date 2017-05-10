package tk.ardentbot.commands.music;

import com.rethinkdb.net.Cursor;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;
import tk.ardentbot.core.executor.Command;
import tk.ardentbot.rethink.models.MusicSettingsModel;

import java.util.HashMap;

import static tk.ardentbot.rethink.Database.connection;
import static tk.ardentbot.rethink.Database.r;

public class MusicConfig extends Command {
    public MusicConfig(CommandSettings commandSettings) {
        super(commandSettings);
    }

    @Override
    public void noArgs(Guild guild, MessageChannel channel, User user, Message message, String[] args) throws Exception {
        Cursor<HashMap> settings = r.db("data").table("music_settings").filter(row -> row.g("guild_id").eq(guild.getId())).run
                (connection);
        if (settings.hasNext()) {
            MusicSettingsModel musicSettingsModel = asPojo(settings.next(), MusicSettingsModel.class);
            sendTranslatedMessage("**music Settings**\n" + "Delete music play messages: " + musicSettingsModel
                    .isRemove_addition_messages(), channel, user);

        }
        else
            sendTranslatedMessage("Your guild has no set music settings! Type **/manage** to find your portal" +
                    " link", channel, user);
    }

    @Override
    public void setupSubcommands() throws Exception {

    }
}
