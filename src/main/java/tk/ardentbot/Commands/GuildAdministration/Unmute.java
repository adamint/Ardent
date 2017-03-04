package tk.ardentbot.Commands.GuildAdministration;

import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.*;
import tk.ardentbot.Backend.Commands.BotCommand;
import tk.ardentbot.Backend.Translation.Language;
import tk.ardentbot.Main.Ardent;
import tk.ardentbot.Utils.Discord.GuildUtils;

import java.util.List;

public class Unmute extends BotCommand {
    public Unmute(CommandSettings commandSettings) {
        super(commandSettings);
    }

    @Override
    public void noArgs(Guild guild, MessageChannel channel, User user, Message message, String[] args, Language language) throws Exception {
        if (args.length == 1) {
            sendTranslatedMessage(getTranslation("unmute", language, "help").getTranslation().replace("{0}", GuildUtils.getPrefix(guild) + args[0]), channel);
        }
        else {
            List<User> mentionedUsers = message.getMentionedUsers();
            if (mentionedUsers.size() == 0) {
                sendRetrievedTranslation(channel, "other", language, "mentionuser");
            }
            else {
                if (guild.getMember(user).hasPermission(Permission.MANAGE_SERVER)) {
                    User mentioned = mentionedUsers.get(0);
                    Member m = guild.getMember(mentioned);

                    if(Ardent.botMuteData.isMuted(m)){
                        Ardent.botMuteData.unmute(m);
                        sendRetrievedTranslation(channel, "unmute", language, "unmuteduser");
                    }else{
                        sendRetrievedTranslation(channel, "unmute", language, "notmuted");
                    }
                    
                }
                else sendRetrievedTranslation(channel, "other", language, "needmanageserver");
            }
        }
    }

    @Override
    public void setupSubcommands() {}
}
