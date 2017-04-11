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
import tk.ardentbot.Utils.Discord.MessageUtils;
import tk.ardentbot.Utils.MapUtils;
import tk.ardentbot.Utils.RPGUtils.Profiles.Profile;
import tk.ardentbot.Utils.RPGUtils.RPGUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static tk.ardentbot.Rethink.Database.connection;
import static tk.ardentbot.Rethink.Database.r;

public class RPGMoney extends Command {
    private ArrayList<String> generatedFirstTimeFor = new ArrayList<>();

    public RPGMoney(CommandSettings commandSettings) {
        super(commandSettings);
    }

    @Override
    public void noArgs(Guild guild, MessageChannel channel, User user, Message message, String[] args, Language language) throws Exception {
        List<User> mentionedUsers = message.getMentionedUsers();
        if (mentionedUsers.size() > 0) {
            User mentioned = mentionedUsers.get(0);
            sendTranslatedMessage(getTranslation("money", language, "theirbalance").getTranslation().replace("{0}", mentioned.getName())
                    .replace("{1}", RPGUtils.formatMoney(Profile.get(mentioned).getMoney())), channel, user);
        }
        else {
            StringBuilder sb = new StringBuilder();
            HashMap<Integer, TranslationResponse> translations = getTranslations(language, new Translation("money", "yourbalance"),
                    new Translation("money", "checktop"));
            sb.append(translations.get(0).getTranslation().replace("{0}", RPGUtils.formatMoney(Profile.get(user).getMoney())));
            sb.append("\n\n" + translations.get(1).getTranslation());
            sendTranslatedMessage(sb.toString(), channel, user);
        }
    }

    @Override
    public void setupSubcommands() throws Exception {
        subcommands.add(new Subcommand(this, "top") {
            @Override
            public void onCall(Guild guild, MessageChannel channel, User user, Message message, String[] args, Language language) throws
                    Exception {
                HashMap<User, Double> moneyAmounts = new HashMap<>();
                List<HashMap> top = r.db("data").table("profiles").orderBy(r.desc("money")).limit(15).run(connection);
                top.forEach(hashMap -> {
                    Profile profile = asPojo(hashMap, Profile.class);
                    moneyAmounts.put(profile.getUser(), profile.getMoney());
                });

                Map<User, Double> sortedAmounts = MapUtils.sortByValue(moneyAmounts);
                HashMap<Integer, TranslationResponse> translations = getTranslations(language, new Translation("money", "topmoney"),
                        new Translation("money", "howtogetmoney"), new Translation("money", "seepeoplemoney"));
                String topMoney = translations.get(0).getTranslation();

                EmbedBuilder builder = MessageUtils.getDefaultEmbed(guild, user, RPGMoney.this);
                builder.setAuthor(topMoney, getShard().url, guild.getIconUrl());
                StringBuilder description = new StringBuilder();
                description.append("**" + topMoney + "**");
                final int[] current = {0};
                sortedAmounts.forEach((u, money) -> {
                    if (u != null) {
                        description.append("\n#" + (current[0] + 1) + ": **" + u.getName() + "** " + RPGUtils.formatMoney(money));
                        current[0]++;
                    }
                });
                description.append("\n\n" + translations.get(1).getTranslation() + "\n\n" + translations.get(2).getTranslation());
                sendEmbed(builder.setDescription(description.toString()), channel, user);
            }
        });

        subcommands.add(new Subcommand(this, "topguild") {
            @Override
            public void onCall(Guild guild, MessageChannel channel, User user, Message message, String[] args, Language language) throws
                    Exception {
                if (!generatedFirstTimeFor.contains(guild.getId())) {
                    sendRetrievedTranslation(channel, "money", language, "retrievingguildstats", user);
                    generatedFirstTimeFor.add(guild.getId());
                }
                HashMap<User, Double> moneyAmounts = new HashMap<>();
                guild.getMembers().forEach(member -> {
                    User u = member.getUser();
                    moneyAmounts.put(u, Profile.get(u).getMoney());
                });
                Map<User, Double> sortedAmounts = MapUtils.sortByValue(moneyAmounts);

                HashMap<Integer, TranslationResponse> translations = getTranslations(language, new Translation("money", "topguildmoney"),
                        new Translation("money", "howtogetmoney"), new Translation("money", "seepeoplemoney"));
                String topGuildMoney = translations.get(0).getTranslation();

                EmbedBuilder builder = MessageUtils.getDefaultEmbed(guild, user, RPGMoney.this);
                builder.setAuthor(topGuildMoney, getShard().url, guild.getIconUrl());
                StringBuilder description = new StringBuilder();
                description.append("**" + topGuildMoney + "**");
                final int[] current = {0};
                sortedAmounts.forEach((u, money) -> {
                    if (current[0] < 10) {
                        description.append("\n#" + (current[0] + 1) + ": **" + u.getName() + "** " + RPGUtils.formatMoney(money));
                        current[0]++;
                    }
                });
                description.append("\n\n" + translations.get(1).getTranslation() + "\n\n" + translations.get(2).getTranslation());
                sendEmbed(builder.setDescription(description.toString()), channel, user);
            }
        });
    }
}
