package tk.ardentbot.commands.money;

import com.rethinkdb.net.Cursor;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;
import tk.ardentbot.core.executor.Ratelimitable;
import tk.ardentbot.core.executor.Subcommand;
import tk.ardentbot.utils.MapUtils;
import tk.ardentbot.utils.discord.MessageUtils;
import tk.ardentbot.utils.rpg.RPGUtils;
import tk.ardentbot.utils.rpg.profiles.Profile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static tk.ardentbot.rethink.Database.connection;
import static tk.ardentbot.rethink.Database.r;

public class RPGMoney extends Ratelimitable {
    private ArrayList<String> generatedFirstTimeFor = new ArrayList<>();

    public RPGMoney(CommandSettings commandSettings) {
        super(commandSettings);
    }

    @Override
    public void noArgs(Guild guild, MessageChannel channel, User user, Message message, String[] args) throws Exception {
        List<User> mentionedUsers = message.getMentionedUsers();
        if (mentionedUsers.size() > 0) {
            User mentioned = mentionedUsers.get(0);
            sendTranslatedMessage("{0}'s balance: **{1}**".replace("{0}", mentioned.getName())
                    .replace("{1}", RPGUtils.formatMoney(Profile.get(mentioned).getMoney())), channel, user);
        }
        else {
            StringBuilder sb = new StringBuilder();
            sb.append("Your balance: **{0}**".replace("{0}", RPGUtils.formatMoney(Profile.get(user).getMoney())));
            sb.append("\n\nWho's the richest in your guild? In the entire Ardent database? Check with */money server* and */money top*");
            sendTranslatedMessage(sb.toString(), channel, user);
        }
    }

    @Override
    public void setupSubcommands() throws Exception {
        subcommands.add(new Subcommand("See who has the most money - globally!", "top", "top") {
            @Override
            public void onCall(Guild guild, MessageChannel channel, User user, Message message, String[] args) throws
                    Exception {
                if (args.length == 2) {
                    HashMap<User, Double> moneyAmounts = new HashMap<>();
                    Cursor<HashMap> top = r.db("data").table("profiles").orderBy()
                            .optArg("index", r.desc("money")).limit(20).run(connection);
                    top.forEach(hashMap -> {
                        Profile profile = asPojo(hashMap, Profile.class);
                        assert profile.getUser() != null;
                        moneyAmounts.put(profile.getUser(), profile.getMoney());
                    });
                    Map<User, Double> sortedAmounts = MapUtils.sortByValue(moneyAmounts);
                    String topMoney = "Global Richest Users";
                    EmbedBuilder builder = MessageUtils.getDefaultEmbed(user);
                    builder.setAuthor(topMoney, getShard().url, guild.getIconUrl());
                    StringBuilder description = new StringBuilder();
                    final int[] current = {0};
                    sortedAmounts.forEach((u, money) -> {
                        if (u != null) {
                            description.append("\n#" + (current[0] + 1) + ": **" + u.getName() + "** " + RPGUtils.formatMoney(money));
                            current[0]++;
                        }
                    });
                    description.append("\n\nGet money by sending commands or asking questions on our support server ( https://ardentbot" +
                            ".tk/guild )\n\nSee people's money by doing /money @User or see yours by just using /money");
                    sendEmbed(builder.setDescription(description.toString()), channel, user);
                    return;
                }
                try {
                    int page = Integer.parseInt(args[2]);
                    HashMap<User, Double> moneyAmounts = new HashMap<>();
                    Cursor<HashMap> top = r.db("data").table("profiles").orderBy()
                            .optArg("index", r.desc("money")).slice((page * 20), (page * 20) + 11).limit(25).run(connection);
                    top.forEach(hashMap -> {
                        Profile profile = asPojo(hashMap, Profile.class);
                        assert profile.getUser() != null;
                        moneyAmounts.put(profile.getUser(), profile.getMoney());
                    });
                    Map<User, Double> sortedAmounts = MapUtils.sortByValue(moneyAmounts);
                    String topMoney = "Global Richest Users | Page " + page;
                    EmbedBuilder builder = MessageUtils.getDefaultEmbed(user);
                    builder.setAuthor(topMoney, getShard().url, guild.getIconUrl());
                    StringBuilder description = new StringBuilder();
                    final int[] current = {0};
                    sortedAmounts.forEach((u, money) -> {
                        if (u != null) {
                            description.append("\n#" + ((page * 20) + current[0] + 1) + ": **" + u.getName() + "** " + RPGUtils
                                    .formatMoney(money));
                            current[0]++;
                        }
                    });
                    description.append("\n\nGet money by sending commands or asking questions on our support server ( https://ardentbot" +
                            ".tk/guild )\n\nSee people's money by doing /money @User or see yours by just using /money");
                    sendEmbed(builder.setDescription(description.toString()), channel, user);

                }
                catch (NumberFormatException e) {
                    sendTranslatedMessage("You need to specify a valid page number!", channel, user);
                }
            }
        });

        subcommands.add(new Subcommand("See who has the most money in your server", "server", "server", "guild", "topguild") {
            @Override
            public void onCall(Guild guild, MessageChannel channel, User user, Message message, String[] args) throws
                    Exception {
                if (!generatedFirstTimeFor.contains(guild.getId())) {
                    sendTranslatedMessage("Please wait a second, generating and caching your server's statistics", channel, user);
                    generatedFirstTimeFor.add(guild.getId());
                }
                HashMap<User, Double> moneyAmounts = new HashMap<>();
                guild.getMembers().forEach(member -> {
                    User u = member.getUser();
                    moneyAmounts.put(u, Profile.get(u).getMoney());
                });
                Map<User, Double> sortedAmounts = MapUtils.sortByValue(moneyAmounts);
                EmbedBuilder builder = MessageUtils.getDefaultEmbed(user);
                builder.setAuthor("Richest Users | This Server", getShard().url, guild.getIconUrl());
                StringBuilder description = new StringBuilder();
                description.append("**Richest Users in this Server**");
                final int[] current = {0};
                sortedAmounts.forEach((u, money) -> {
                    if (current[0] < 10) {
                        description.append("\n#" + (current[0] + 1) + ": **" + u.getName() + "** " + RPGUtils.formatMoney(money));
                        current[0]++;
                    }
                });
                description.append("\n\nGet money by sending commands or asking questions on our support server ( https://ardentbot" +
                        ".tk/guild )\n\nSee people's money by doing /money @User or see yours by just using /money");
                sendEmbed(builder.setDescription(description.toString()), channel, user);
            }
        });
    }
}
