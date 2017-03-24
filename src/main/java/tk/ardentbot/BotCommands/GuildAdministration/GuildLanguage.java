package tk.ardentbot.BotCommands.GuildAdministration;

import net.dv8tion.jda.core.entities.*;
import tk.ardentbot.Core.CommandExecution.Command;
import tk.ardentbot.Core.CommandExecution.Subcommand;
import tk.ardentbot.Core.LoggingUtils.BotException;
import tk.ardentbot.Core.Translation.LangFactory;
import tk.ardentbot.Core.Translation.Language;
import tk.ardentbot.Main.Shard;
import tk.ardentbot.Utils.Discord.GuildUtils;
import tk.ardentbot.Utils.SQL.DatabaseAction;

import java.util.concurrent.TimeUnit;

public class GuildLanguage extends Command {
    public Subcommand set;

    public GuildLanguage(CommandSettings commandSettings) {
        super(commandSettings);
    }

    @Override
    public void noArgs(Guild guild, MessageChannel channel, User user, Message message, String[] args, tk.ardentbot
            .Core.Translation.Language language) throws Exception {
        sendHelp(language, channel, guild, user, this);
    }

    @Override
    public void setupSubcommands() {
        subcommands.add(new Subcommand(this, "view") {
            @Override
            public void onCall(Guild guild, MessageChannel channel, User user, Message message, String[] args,
                               Language language) throws Exception {
                sendTranslatedMessage(getTranslation("language", language, "currentlanguage").getTranslation()
                        .replace("{0}", "**" + LangFactory.getName(language) + "**"), channel, user);
            }
        });

        subcommands.add(new Subcommand(this, "list") {
            @Override
            public void onCall(Guild guild, MessageChannel channel, User user, Message message, String[] args,
                               Language language) throws Exception {
                if (args.length == 2) {
                    StringBuilder languages = new StringBuilder();
                    LangFactory.languages.forEach((lang) -> {
                        if (lang.getLanguageStatus() == Language.Status.MATURE || lang.getLanguageStatus() ==
                                Language.Status.MOST)
                        {
                            languages.append("\n    > **" + LangFactory.getName(lang) + "**  ");
                        }
                    });
                    String reply = getTranslation("language", language, "checklanguages").getTranslation().replace
                            ("{0}", languages.toString()).replace("{2}", set.getName(language)).replace("{1}",
                            GuildUtils.getPrefix(guild) + args[0]);
                    sendTranslatedMessage(reply, channel, user);
                }
                else
                    sendTranslatedMessage(getTranslation("other", language, "checksyntax").getTranslation().replace
                            ("{0}", GuildUtils.getPrefix(guild) + args[0]), channel, user);
            }
        });

        set = new Subcommand(this, "set") {
            @Override
            public void onCall(Guild guild, MessageChannel channel, User user, Message message, String[] args,
                               Language language) throws Exception {
                Shard shard = GuildUtils.getShard(guild);
                Member member = guild.getMember(user);
                if (GuildUtils.hasManageServerPermission(member)) {
                    if (args.length == 3) {
                        Language changeTo = LangFactory.getLanguage(args[2]);
                        if (changeTo != null) {
                            shard.botLanguageData.set(guild, changeTo.getIdentifier());
                            new DatabaseAction("UPDATE Guilds SET Language=? WHERE GuildID=?").set(changeTo
                                    .getIdentifier()).set(guild.getId()).update();
                            shard.botLanguageData.set(guild, changeTo.getIdentifier());
                            sendRetrievedTranslation(channel, "language", changeTo, "changedlanguage", user);
                            shard.executorService.schedule(() -> {
                                try {
                                    sendTranslatedMessage(getTranslation("other", changeTo, "mentionedhelp")
                                            .getTranslation().replace("{0}", GuildUtils.getPrefix(guild) +
                                                    shard.help.getName(changeTo)), channel, user);
                                }
                                catch (Exception e) {
                                    new BotException(e);
                                }

                            }, 2, TimeUnit.SECONDS);
                        }
                        else sendRetrievedTranslation(channel, "language", language, "invalidlanguage", user);
                    }
                    else
                        sendTranslatedMessage(getTranslation("other", language, "checksyntax").getTranslation()
                                .replace("{0}", GuildUtils.getPrefix(guild) + args[0]), channel, user);
                }
                else sendRetrievedTranslation(channel, "other", language, "needmanageserver", user);
            }
        };
        subcommands.add(set);

        // TODO: 3/17/2017 fix below
        /*subcommands.add(new Subcommand(this, "statistics") {
            @Override
            public void onCall(Guild guild, MessageChannel channel, User user, Message message, String[] args,
                               Language language) throws Exception {
                Shard shard = GuildUtils.getShard(guild);
                ArrayList<Translation> translations = new ArrayList<>();
                translations.add(new Translation("language", "languageusages"));
                translations.add(new Translation("status", "guilds"));
                HashMap<Integer, TranslationResponse> responses = getTranslations(language, translations);
                Map<String, Integer> usages = GuildUtils.getLanguageUsages();
                int guilds = shard.jda.getGuilds().size();
                DecimalFormat format = new DecimalFormat("##");
                StringBuilder sb = new StringBuilder();
                sb.append("**" + responses.get(0).getTranslation() + "**\n");
                usages.forEach((key, value) -> {
                    sb.append(" > **" + key + "**: " + format.format((value / guilds)) + "%\n");
                });
                sendTranslatedMessage(sb.toString(), channel, user);
            }
        });*/
    }
}
