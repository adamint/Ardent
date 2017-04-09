package tk.ardentbot.BotCommands.GuildAdministration;

import com.rethinkdb.net.Cursor;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.*;
import tk.ardentbot.Core.CommandExecution.Command;
import tk.ardentbot.Core.CommandExecution.Subcommand;
import tk.ardentbot.Core.Misc.LoggingUtils.BotException;
import tk.ardentbot.Core.Translation.Language;
import tk.ardentbot.Core.Translation.Translation;
import tk.ardentbot.Core.Translation.TranslationResponse;
import tk.ardentbot.Rethink.Models.AutomessageModel;
import tk.ardentbot.Utils.JLAdditions.Triplet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static tk.ardentbot.Rethink.Database.connection;
import static tk.ardentbot.Rethink.Database.r;

public class Automessage extends Command {
    public Automessage(CommandSettings commandSettings) {
        super(commandSettings);
    }

    private static void check(Guild guild) {
        List<HashMap> selectAutomessage = ((Cursor<HashMap>) r.db("data").table("automessages").filter(row -> row.g("guild_id")
                .eq(guild.getId())).run(connection)).toList();
        if (selectAutomessage.size() == 0) {
            r.db("data").table("automessages").insert(new AutomessageModel(guild.getId(), "000", "000", "000")).run(connection);
        }
    }

    public static Triplet<String, String, String> getMessagesAndChannel(Guild guild) {
        Triplet<String, String, String> triplet;
        check(guild);
        List<HashMap> getAutomessages = ((Cursor<HashMap>) r.db("data").table("automessages").filter(row -> row.g("guild_id")
                .eq(guild.getId())).run(connection)).toList();
        if (getAutomessages.size() > 0) {
            AutomessageModel automessageModel = asPojo(getAutomessages.get(0), AutomessageModel.class);
            String channel;
            String welcome;
            String goodbye;
            if (automessageModel.getChannel_id().equalsIgnoreCase("000")) channel = null;
            else channel = automessageModel.getChannel_id();
            if (automessageModel.getWelcome().equalsIgnoreCase("000")) welcome = null;
            else welcome = automessageModel.getWelcome();
            if (automessageModel.getGoodbye().equalsIgnoreCase("000")) goodbye = null;
            else goodbye = automessageModel.getGoodbye();
            triplet = new Triplet<>(channel, welcome, goodbye);
        }
        else {
            triplet = new Triplet<>(null, null, null);
        }
        return triplet;
    }

    public static String getField(int num) {
        String columnName = null;
        if (num == 0) columnName = "channel_id";
        else if (num == 1) columnName = "welcome";
        else if (num == 2) columnName = "goodbye";
        return columnName;
    }

    public static void remove(Guild guild, int num) {
        set(guild, "000", num);
    }

    @SuppressWarnings("Duplicates")
    public static void set(Guild guild, String text, int num) {
        String fieldName = getField(num);
        r.db("data").table("automessages").filter(row -> row.g("guild_id").eq(guild.getId())).update(r.hashMap(fieldName, text)).run
                (connection);
    }

    @Override
    public void noArgs(Guild guild, MessageChannel channel, User user, Message message, String[] args, Language
            language) throws Exception {
        sendHelp(language, channel, guild, user, this);
    }

    @Override
    public void setupSubcommands() throws Exception {
        subcommands.add(new Subcommand(this, "setup") {
            @Override
            public void onCall(Guild guild, MessageChannel channel, User user, Message message, String[] args, Language language) throws
                    Exception {
                sendRetrievedTranslation(channel, "automessage", language, "automessagesetup", user);
                interactiveOperation(language, channel, message, selectType -> {
                    String type;
                    String content = selectType.getContent();
                    if (content.equalsIgnoreCase("join")) {
                        type = "join";
                        sendRetrievedTranslation(channel, "automessage", language, "typecontent", user);
                    }
                    else if (content.equalsIgnoreCase("leave")) {
                        type = "leave";
                        sendRetrievedTranslation(channel, "automessage", language, "typecontent", user);
                    }
                    else if (content.equalsIgnoreCase("channel")) {
                        type = "channel";
                        sendRetrievedTranslation(channel, "automessage", language, "mentionachannel", user);
                    }
                    else {
                        sendRetrievedTranslation(channel, "automessage", language, "invalidcategorytype", user);
                        return;
                    }
                    interactiveOperation(language, channel, message, inputMessage -> {
                        try {
                            if (type.equals("channel")) {
                                List<TextChannel> mentionedChannels = inputMessage.getMentionedChannels();
                                if (mentionedChannels.size() > 0) {
                                    TextChannel mentioned = mentionedChannels.get(0);
                                    set(guild, mentioned.getId(), 0);
                                    sendTranslatedMessage(getTranslation("automessage", language, "successfullyset")
                                            .getTranslation()
                                            .replace("{0}", getTranslation("automessage", language, "channelword")
                                                    .getTranslation()).replace("{1}", mentioned.getName()), channel, user);

                                }
                                else sendRetrievedTranslation(channel, "automessage", language, "mentionchannel", user);
                            }
                            else {
                                String toPut = inputMessage.getRawContent();
                                if (type.equals("join")) {
                                    set(guild, toPut, 1);
                                    sendTranslatedMessage(getTranslation("automessage", language, "successfullyset")
                                            .getTranslation()
                                            .replace("{0}", getTranslation("automessage", language, "joinword")
                                                    .getTranslation()).replace("{1}", toPut), channel, user);
                                }
                                else {
                                    set(guild, toPut, 2);
                                    sendTranslatedMessage(getTranslation("automessage", language, "successfullyset")
                                            .getTranslation()
                                            .replace("{0}", getTranslation("automessage", language, "leaveword")
                                                    .getTranslation()).replace("{1}", toPut), channel, user);
                                }

                                String textChannel = getMessagesAndChannel(guild).getA();
                                if (textChannel == null || guild.getTextChannelById(textChannel) == null) {
                                    sendRetrievedTranslation(channel, "automessage", language, "youneedtosetachannel", user);
                                }
                            }
                        }
                        catch (Exception e) {
                            new BotException(e);
                        }
                    });
                });
            }
        });

        subcommands.add(new Subcommand(this, "arguments") {
            @Override
            public void onCall(Guild guild, MessageChannel channel, User user, Message message, String[] args,
                               Language language) throws Exception {
                StringBuilder sb = new StringBuilder();
                sb.append(getTranslation("automessage", language, "availablearguments").getTranslation());
                sendTranslatedMessage(sb.toString(), channel, user);
            }
        });

        subcommands.add(new Subcommand(this, "remove") {
            @Override
            public void onCall(Guild guild, MessageChannel channel, User user, Message message, String[] args,
                               Language language) throws Exception {
                if (guild.getMember(user).hasPermission(Permission.MANAGE_SERVER)) {
                    if (args.length == 2) {
                        sendRetrievedTranslation(channel, "automessage", language, "specifytype", user);
                    }
                    else {
                        String type = args[2];
                        Triplet<String, String, String> settings = getMessagesAndChannel(guild);
                        if (type.equalsIgnoreCase("channel")) {
                            if (settings.getA() == null) {
                                sendRetrievedTranslation(channel, "automessage", language, "nochannelset", user);
                            }
                            else {
                                remove(guild, 0);
                                sendTranslatedMessage(getTranslation("automessage", language,
                                        "successfullyremovedchannel").getTranslation(), channel, user);
                            }
                        }
                        else if (type.equalsIgnoreCase("join")) {
                            if (settings.getB() == null) {
                                sendRetrievedTranslation(channel, "automessage", language, "nowelcomeset", user);
                            }
                            else {
                                remove(guild, 1);
                                sendTranslatedMessage(getTranslation("automessage", language,
                                        "successfullyremovedwelcome").getTranslation(), channel, user);
                            }
                        }
                        else if (type.equalsIgnoreCase("leave")) {
                            if (settings.getC() == null) {
                                sendRetrievedTranslation(channel, "automessage", language, "nogoodbyeset", user);
                            }
                            else {
                                remove(guild, 2);
                                sendTranslatedMessage(getTranslation("automessage", language,
                                        "successfullyremovedgoodbye").getTranslation(), channel, user);
                            }
                        }
                        else sendRetrievedTranslation(channel, "tag", language, "invalidarguments", user);
                    }
                }
                else sendRetrievedTranslation(channel, "other", language, "needmanageserver", user);
            }
        });

        subcommands.add(new Subcommand(this, "view") {
            @Override
            public void onCall(Guild guild, MessageChannel channel, User user, Message message, String[] args,
                               Language language) throws Exception {
                ArrayList<Translation> translations = new ArrayList<>();
                translations.add(new Translation("automessage", "settings"));
                translations.add(new Translation("automessage", "nochannel"));
                translations.add(new Translation("automessage", "nowelcome"));
                translations.add(new Translation("automessage", "nogoodbye"));
                translations.add(new Translation("automessage", "channel"));
                translations.add(new Translation("automessage", "welcome"));
                translations.add(new Translation("automessage", "goodbye"));

                HashMap<Integer, TranslationResponse> responses = getTranslations(language, translations);
                Triplet<String, String, String> messages = getMessagesAndChannel(guild);
                StringBuilder sb = new StringBuilder();
                sb.append(responses.get(0).getTranslation() + "\n=============\n");
                if (messages.getA() == null) sb.append(responses.get(1).getTranslation() + "\n\n");
                else {
                    TextChannel textChannel = guild.getTextChannelById(messages.getA());
                    if (textChannel != null) {
                        sb.append(responses.get(4).getTranslation().replace("{0}", textChannel.getName() + "\n\n"));
                    }
                    else sb.append(responses.get(1).getTranslation() + "\n\n");
                }

                if (messages.getB() != null)
                    sb.append(responses.get(5).getTranslation().replace("{0}", messages.getB()) + "\n\n");
                else sb.append(responses.get(2).getTranslation() + "\n\n");

                if (messages.getC() != null)
                    sb.append(responses.get(6).getTranslation().replace("{0}", messages.getC()) + "\n");
                else sb.append(responses.get(3).getTranslation());

                sendTranslatedMessage(sb.toString(), channel, user);
            }
        });
    }
}