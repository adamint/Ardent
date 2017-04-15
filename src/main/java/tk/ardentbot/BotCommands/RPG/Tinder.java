package tk.ardentbot.BotCommands.RPG;

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
import tk.ardentbot.Rethink.Models.TinderMatch;
import tk.ardentbot.Utils.Discord.MessageUtils;
import tk.ardentbot.Utils.Discord.UserUtils;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashMap;

import static tk.ardentbot.Rethink.Database.connection;
import static tk.ardentbot.Rethink.Database.r;

public class Tinder extends Command {
    public Tinder(CommandSettings commandSettings) {
        super(commandSettings);
    }

    private static User getPotentialMatch(User user, Guild guild, ArrayList<TinderMatch> userMatches) {
        User u = guild.getMembers().get(new SecureRandom().nextInt(guild.getMembers().size())).getUser();
        if (user.getId().equals(u.getId()) || user.isBot()) return getPotentialMatch(user, guild, userMatches);
        for (TinderMatch t : userMatches) {
            if (t.getPerson_id().equals(u.getId())) return getPotentialMatch(user, guild, userMatches);
        }

        return u;
    }

    @Override
    public void noArgs(Guild guild, MessageChannel channel, User user, Message message, String[] args, Language language) throws Exception {
        sendHelp(language, channel, guild, user, this);
    }

    @Override
    public void setupSubcommands() throws Exception {
        subcommands.add(new Subcommand(this, "matchme") {
            @Override
            public void onCall(Guild guild, MessageChannel channel, User user, Message message, String[] args, Language language) throws
                    Exception {
                ArrayList<TinderMatch> matches = queryToArraylist(TinderMatch.class, r.table("tinder_matches").filter(row -> row.g
                        ("user_id").eq(user.getId())).run(connection));
                User potentialMatch = getPotentialMatch(user, guild, matches);
                HashMap<Integer, TranslationResponse> translations = getTranslations(language,
                        new Translation("tinder", "tindermatchme"), new Translation("tinder", "swipeleftorright"),
                        new Translation("tinder", "usetindermessagetostartmessage"), new Translation("tinder", "theirpicture"),
                        new Translation("tinder", "theirname"));
                EmbedBuilder builder = MessageUtils.getDefaultEmbed(guild, user, Tinder.this);
                String matchMe = translations.get(0).getTranslation();
                builder.setAuthor(matchMe, getShard().url, getShard().bot.getAvatarUrl());
                builder.setThumbnail(potentialMatch.getAvatarUrl());
                StringBuilder description = new StringBuilder();
                description.append("**" + matchMe + "**");
                description.append("\n" + translations.get(4).getTranslation() + ": " + UserUtils.getNameWithDiscriminator(potentialMatch
                        .getId()));
                description.append("\n\n" + translations.get(1).getTranslation() + "\n" + translations.get(2).getTranslation());
                sendEmbed(builder.setDescription(description.toString()), channel, user, ":arrow_left:", ":arrow_right:");
            }
        });
    }

}
