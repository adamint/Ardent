package tk.ardentbot.Commands.BotAdministration;

import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.requests.RestAction;
import org.apache.commons.lang3.tuple.Triple;
import tk.ardentbot.Backend.Commands.BotCommand;
import tk.ardentbot.Backend.Translation.Language;
import tk.ardentbot.Main.Config;
import tk.ardentbot.Utils.Discord.GuildUtils;
import tk.ardentbot.Utils.Engine;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static tk.ardentbot.Main.Config.conn;

public class Eval extends BotCommand {
    public Eval(CommandSettings commandSettings) {
        super(commandSettings);
    }

    @Override
    public void noArgs(Guild guild, MessageChannel channel, User user, Message message, String[] args, Language
            language) throws Exception {
        if (Config.developers.contains(user.getId())) {
            if (args.length == 1) channel.sendMessage("Use " + args[0] + " (code) to evaluate stuff");
            else {
                String content = message.getContent().replace(GuildUtils.getPrefix(guild) + args[0] + " ", "");
                final MessageBuilder builder = new MessageBuilder();
                final Map<String, Object> shortcuts = new HashMap<>();
                shortcuts.put("api", message.getJDA());
                shortcuts.put("jda", message.getJDA());
                shortcuts.put("channel", channel);
                shortcuts.put("server", guild);
                shortcuts.put("guild", guild);

                shortcuts.put("message", message);
                shortcuts.put("msg", message);
                shortcuts.put("me", message.getAuthor());
                shortcuts.put("bot", message.getJDA().getSelfUser());
                shortcuts.put("conn", conn);

                final int timeout = 10;

                final Triple<Object, String, String> result = Engine.GROOVY.eval(shortcuts, Collections.emptyList(), Engine.DEFAULT_IMPORTS, timeout, content);

                if (result.getLeft() instanceof RestAction<?>) {
                    ((RestAction<?>) result.getLeft()).queue();
                }
                else if (result.getLeft() != null) {
                    builder.appendCodeBlock(result.getLeft().toString(), "");
                }
                if (!result.getMiddle().isEmpty()) {
                    builder.append("\n").appendCodeBlock(result.getMiddle(), "");
                }
                if (!result.getRight().isEmpty()) {
                    builder.append("\n").appendCodeBlock(result.getRight(), "");
                }

                if (builder.isEmpty()) {
                    message.addReaction("✅").queue();
                }
                else {
                    for (final Message m : builder.buildAll(MessageBuilder.SplitPolicy.NEWLINE, MessageBuilder.SplitPolicy.SPACE, MessageBuilder.SplitPolicy.ANYWHERE)) {
                        channel.sendMessage(m).queue();
                    }
                }
            }
        }
    }
    @Override
    public void setupSubcommands() throws Exception {
    }
}
