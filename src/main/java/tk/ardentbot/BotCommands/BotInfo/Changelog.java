package tk.ardentbot.BotCommands.BotInfo;

import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;
import tk.ardentbot.Core.CommandExecution.Command;
import tk.ardentbot.Core.CommandExecution.Subcommand;
import tk.ardentbot.Core.Translation.Language;
import tk.ardentbot.Core.Translation.Translation;
import tk.ardentbot.Core.Translation.TranslationResponse;
import tk.ardentbot.Main.Ardent;
import tk.ardentbot.Utils.Discord.GuildUtils;
import tk.ardentbot.Utils.Log;
import tk.ardentbot.Utils.SQL.DatabaseAction;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;

import static tk.ardentbot.Main.Ardent.ardent;

public class Changelog extends Command {
    public Changelog(CommandSettings commandSettings) {
        super(commandSettings);
    }

    private static ArrayList<Log> getLogs() throws SQLException {
        ArrayList<Log> logs = new ArrayList<>();
        DatabaseAction getChangelogs = new DatabaseAction("SELECT * FROM Changelog ORDER BY Time DESC");
        ResultSet set = getChangelogs.request();
        while (set.next()) {
            ArrayList<String> features = new ArrayList<>();
            String featuresUnformatted = set.getString("Information");
            String[] parsed = featuresUnformatted.split("\\|next\\|");
            for (String s : parsed) features.add(s);
            logs.add(new Log(set.getString("Title"), features, set.getTimestamp("Time")));
        }
        getChangelogs.close();
        return logs;
    }

    @Override
    public void noArgs(Guild guild, MessageChannel channel, User user, Message message, String[] args, Language
            language) throws Exception {
        ArrayList<Translation> translations = new ArrayList<>();
        translations.add(new Translation("changelog", "listtitle"));
        translations.add(new Translation("changelog", "updatesbydate"));
        translations.add(new Translation("changelog", "howtoviewindividual"));
        HashMap<Integer, TranslationResponse> responses = getTranslations(language, translations);

        ArrayList<Log> logs = getLogs();
        EmbedBuilder builder = new EmbedBuilder();
        builder.setTitle(responses.get(0).getTranslation(), ardent.url);

        StringBuilder description = new StringBuilder();
        description.append(responses.get(1).getTranslation() + "\n");
        for (int i = 0; i < logs.size(); i++) {
            Log current = logs.get(i);
            description.append("#" + (i + 1) + ": " + current.getTitle() + "\n");
        }

        description.append("\n" + responses.get(2).getTranslation().replace("{0}", GuildUtils.getPrefix(guild) +
                args[0]));
        builder.setDescription(description.toString());

        sendEmbed(builder, channel, user);
    }

    @Override
    public void setupSubcommands() throws Exception {
        subcommands.add(new Subcommand(this, "add") {
            @Override
            public void onCall(Guild guild, MessageChannel channel, User user, Message message, String[] args,
                               Language language) throws Exception {
                if (Ardent.developers.contains(user.getId())) {
                    if (args.length > 3) {
                        String input = message.getContent().replace(GuildUtils.getPrefix(guild) + args[0] + " " +
                                args[1] + " ", "");
                        String[] parsed = input.split("@next@");
                        if (parsed.length == 2) {
                            String title = parsed[0];
                            String information = parsed[1];
                            new DatabaseAction("INSERT INTO Changelog VALUES (?,?,?)").set(title).set(information)
                                    .set(Timestamp.from(Instant.now())).update();
                        }
                        else
                            sendTranslatedMessage("Idiot, delineate the seperation of title & information by @next@",
                                    channel, user);
                    }
                    else
                        sendTranslatedMessage("Idiot, delineate the seperation of title & information by @next@",
                                channel, user);
                }
                else sendRetrievedTranslation(channel, "other", language, "needdeveloperpermission", user);
            }
        });
        subcommands.add(new Subcommand(this, "view") {
            @Override
            public void onCall(Guild guild, MessageChannel channel, User user, Message message, String[] args,
                               Language language) throws Exception {
                if (args.length > 2) {
                    try {
                        int number = Integer.parseInt(args[2]) - 1;
                        ArrayList<Log> logs = getLogs();
                        if (number >= 0 && number < logs.size()) {
                            Log log = logs.get(number);
                            ArrayList<Translation> translations = new ArrayList<>();
                            translations.add(new Translation("changelog", "listtitle"));
                            translations.add(new Translation("changelog", "howtoviewindividual"));
                            HashMap<Integer, TranslationResponse> responses = getTranslations(language, translations);

                            EmbedBuilder builder = new EmbedBuilder();
                            builder.setTitle(responses.get(0).getTranslation(), ardent.url);

                            StringBuilder description = new StringBuilder();
                            description.append("**" + log.getTitle() + "**\n\n");
                            for (String line : log.getFeatures()) {
                                description.append(" - " + line + "\n\n");
                            }
                            description.append(Date.from(log.getTimestamp().toInstant()));
                            description.append("\n\n" + responses.get(1).getTranslation().replace("{0}", GuildUtils
                                    .getPrefix(guild) + args[0]));
                            builder.setDescription(description.toString());

                            sendEmbed(builder, channel, user);

                        }
                        else sendRetrievedTranslation(channel, "changelog", language, "includenumber", user);
                    }
                    catch (NumberFormatException ex) {
                        sendRetrievedTranslation(channel, "changelog", language, "includenumber", user);
                    }
                }
                else sendRetrievedTranslation(channel, "changelog", language, "includenumber", user);
            }
        });
    }
}
