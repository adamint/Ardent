package tk.ardentbot.commands.rpg;

import com.vdurmont.emoji.EmojiParser;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;
import tk.ardentbot.core.executor.Command;
import tk.ardentbot.core.executor.Subcommand;
import tk.ardentbot.rethink.models.TinderMatch;
import tk.ardentbot.utils.discord.MessageUtils;
import tk.ardentbot.utils.discord.UserUtils;

import java.security.SecureRandom;
import java.util.ArrayList;

import static tk.ardentbot.rethink.Database.connection;
import static tk.ardentbot.rethink.Database.r;

public class Tinder extends Command {
    private static ArrayList<String> sentTo = new ArrayList<>();

    public Tinder(CommandSettings commandSettings) {
        super(commandSettings);
    }

    /**
     * For future use
     *
     * @param userId user to check for matches from
     * @return list of tinder matches who that person HAS swiped right on
     */
    private static ArrayList<TinderMatch> getMutuallySwipedWith(String userId) {
        ArrayList<TinderMatch> mutuallySwipedWith = new ArrayList<>();
        ArrayList<TinderMatch> matches = queryAsArrayList(TinderMatch.class, r.table("tinder_matches").filter(r.hashMap
                ("user_id", userId).with("swipedRight", true)).run(connection));
        matches.forEach(potentialMatch -> {
            ArrayList<TinderMatch> personMatches = queryAsArrayList(TinderMatch.class, r.table("tinder_matches").filter(r.hashMap
                    ("user_id", potentialMatch.getPerson_id()).with("swipedRight", true)).run(connection));
            personMatches.forEach(personMatch -> {
                if (personMatch.getPerson_id().equals(userId)) mutuallySwipedWith.add(personMatch);
            });
        });
        return mutuallySwipedWith;
    }

    private static boolean swipedRightWith(String userId, String toCheckWithId) {
        final boolean[] mutualSwipedWith = {false};
        ArrayList<TinderMatch> matches = queryAsArrayList(TinderMatch.class, r.table("tinder_matches").filter(r.hashMap("user_id",
                userId).with("swipedRight", true)).run(connection));
        matches.forEach(tinderMatch -> {
            if (tinderMatch.getPerson_id().equals(toCheckWithId)) {
                ArrayList<TinderMatch> personMatches = queryAsArrayList(TinderMatch.class, r.table("tinder_matches").filter(r.hashMap
                        ("user_id",
                                toCheckWithId).with("swipedRight", true)).run(connection));
                personMatches.forEach(personMatch -> {
                    if (personMatch.getPerson_id().equals(userId)) mutualSwipedWith[0] = true;
                });
            }
        });
        return mutualSwipedWith[0];
    }

    private static User getPotentialMatch(User user, Guild guild, ArrayList<TinderMatch> userMatches) {
        User u = guild.getMembers().get(new SecureRandom().nextInt(guild.getMembers().size())).getUser();
        if (user.getId().equals(u.getId()) || u.isBot()) return getPotentialMatch(user, guild, userMatches);
        for (TinderMatch t : userMatches) {
            if (t.getPerson_id().equals(u.getId())) return getPotentialMatch(user, guild, userMatches);
        }
        return u;
    }

    @Override
    public void noArgs(Guild guild, MessageChannel channel, User user, Message message, String[] args) throws Exception {
        sendHelp(channel, guild, user, this);
    }

    @Override
    public void setupSubcommands() throws Exception {
        subcommands.add(new Subcommand("Match yourself with someone in the server!", "matchme", "matchme") {
            @Override
            public void onCall(Guild guild, MessageChannel channel, User user, Message message, String[] args) throws
                    Exception {
                ArrayList<TinderMatch> matches = queryAsArrayList(TinderMatch.class, r.table("tinder_matches").filter(row -> row.g
                        ("user_id").eq(user.getId())).run(connection));
                User potentialMatch = getPotentialMatch(user, guild, matches);
                EmbedBuilder builder = MessageUtils.getDefaultEmbed(user);
                String matchMe = "Match me | Tinder";
                builder.setAuthor(matchMe, getShard().url, getShard().bot.getAvatarUrl());
                builder.setThumbnail(potentialMatch.getAvatarUrl());
                StringBuilder description = new StringBuilder();
                description.append("**" + matchMe + "**");
                description.append("\nTheir name: " + UserUtils.getNameWithDiscriminator(potentialMatch
                        .getId()));
                description.append("Swipe right (rightreaction) to connect with this person, or swipe left (left reaction) to pass them " +
                        "by\nType /tinder connect to connect with the people you've swiped right on");
                Message sent = sendEmbed(builder.setDescription(description.toString()), channel, user, ":arrow_left:",
                        ":arrow_right:");
                interactiveReaction(channel, sent, user, 15, messageReaction -> {
                    String name = messageReaction.getEmote().getName();
                    if (name != null) {
                        if (name.equals("➡")) {
                            r.table("tinder_matches").insert(r.json(gson.toJson(new TinderMatch(user.getId(), potentialMatch.getId
                                    (), true)))
                            ).run(connection);
                            sendEditedTranslation("You swiped right on {0}! Connect with them using /tinder connect", user, channel,
                                    potentialMatch.getName());
                        }
                        else if (name.equals("⬅")) {
                            r.table("tinder_matches").insert(r.json(gson.toJson(new TinderMatch(user.getId(), potentialMatch.getId
                                    (), false)))
                            ).run(connection);
                            sendEditedTranslation("You swiped right on {0} - Don't worry, you can find better!", user, channel,
                                    potentialMatch.getName());
                        }
                        else sendTranslatedMessage("You reacted with an unexpected emoji :thinking:", channel, user);
                    }
                });
            }
        });

        subcommands.add(new Subcommand("Connect with the people you've swiped right on", "connect", "connect") {
            @Override
            public void onCall(Guild guild, MessageChannel channel, User user, Message message, String[] args) throws
                    Exception {
                String yourMatches = "Discord Tinder | Your Matches";
                String swipedRightOnYou = "Swiped right on you";
                EmbedBuilder builder = MessageUtils.getDefaultEmbed(user);
                builder.setAuthor(yourMatches, getShard().url, getShard().bot.getAvatarUrl());
                builder.setThumbnail(user.getAvatarUrl());
                StringBuilder description = new StringBuilder();
                description.append("**" + yourMatches + "**");
                ArrayList<TinderMatch> matches = queryAsArrayList(TinderMatch.class, r.table("tinder_matches").filter(r.hashMap("user_id",
                        user.getId()).with("swipedRight", true)).run(connection));
                if (matches.size() == 0) {
                    description.append("\nYou don't have any connections :frowning:");
                }
                else {
                    for (int i = 0; i < matches.size(); i++) {
                        String swipedRightWithId = matches.get(i).getPerson_id();
                        boolean mutual = swipedRightWith(user.getId(), swipedRightWithId);
                        String yesNo;
                        if (mutual) yesNo = EmojiParser.parseToUnicode(":white_check_mark:");
                        else yesNo = EmojiParser.parseToAliases(":x:");
                        description.append("\n#" + (i + 1) + ": " + UserUtils.getNameWithDiscriminator(swipedRightWithId) + " | " +
                                swipedRightOnYou + ": " + yesNo);
                    }
                }
                description.append("\n\nUse /tinder message [number on connection list] to message that person. However, they must have " +
                        "swiped right on you as well to be able to contact them");
                sendEmbed(builder.setDescription(description), channel, user);
            }
        });

        subcommands.add(new Subcommand("Message one of your mutual connections!", "message [number in connection list]", "message") {
            @Override
            public void onCall(Guild guild, MessageChannel channel, User user, Message message, String[] args) throws
                    Exception {
                if (args.length == 2) {
                    sendTranslatedMessage("Usage: /tinder message [person's number in /tinder connect] [message here]", channel, user);
                    return;
                }
                if (message.getRawContent().split(" ").length == 3) {
                    sendTranslatedMessage("You need to include a message to send that person!", channel, user);
                    return;
                }
                try {
                    int number = Integer.parseInt(args[2]) - 1;
                    if (number < 0)
                        sendTranslatedMessage("Usage: /tinder message [person's number in /tinder connect] [message here]", channel, user);

                    else {
                        ArrayList<TinderMatch> matches = queryAsArrayList(TinderMatch.class, r.table("tinder_matches").filter(r.hashMap
                                ("user_id", user.getId()).with("swipedRight", true)).run(connection));
                        if (number >= matches.size()) {
                            sendTranslatedMessage("Sorry, but you don't have this many matches!", channel, user);
                            return;
                        }
                        TinderMatch selected = matches.get(number);
                        if (!swipedRightWith(user.getId(), selected.getPerson_id())) {
                            sendTranslatedMessage("That person hasn't swiped right on you!", channel, user);
                            return;
                        }
                        User toMessage = UserUtils.getUserById(selected.getPerson_id());
                        try {
                            toMessage.openPrivateChannel().queue(privateChannel -> privateChannel.sendMessage("One of your Tinder " +
                                    "matches, **" + UserUtils.getNameWithDiscriminator(user.getId()) + "**, sent you " +
                                    "a message!\n\n**" + user.getName() + "**: " + replace(message.getRawContent(), 3)).queue());
                            if (!sentTo.contains(toMessage.getId())) {
                                toMessage.openPrivateChannel().queue(privateChannel -> {
                                    privateChannel.sendMessage("To stop receiving messages from this person, remove them from your match " +
                                            "list with /tinder remove [number on the /tinder connect list]\n" +
                                            "\n" +
                                            "To send a message back to this person, type /tinder message [number on your /tinder connect " +
                                            "list] [message here] in a server - meet other tinder savvy people on the Ardent guild (where" +
                                            " we have a dedicated Tinder channel)! - https://discordapp.com/invite/rfGSxNA").queue();
                                    sentTo.add(toMessage.getId());
                                });
                            }
                            sendTranslatedMessage("Ok! I sent that person your message :wink:", channel, user);
                        }
                        catch (Exception e) {
                            sendTranslatedMessage("I was unable to message that person :frowning:", channel, user);
                        }
                    }
                }
                catch (NumberFormatException e) {
                    sendTranslatedMessage("Usage: /tinder message [person's number in /tinder connect] [message here]", channel, user);
                }
            }
        });

        subcommands.add(new Subcommand("Remove one of your connections", "remove [number in connection list]", "remove") {
            @Override
            public void onCall(Guild guild, MessageChannel channel, User user, Message message, String[] args) throws
                    Exception {
                if (args.length == 2) {
                    sendTranslatedMessage("Usage: /tinder remove [person's number in /tinder connect]", channel, user);
                    return;
                }
                try {
                    int number = Integer.parseInt(args[2]) - 1;
                    if (number < 0) sendTranslatedMessage("Usage: /tinder remove [person's number in /tinder connect]", channel, user);
                    else {
                        ArrayList<TinderMatch> matches = queryAsArrayList(TinderMatch.class, r.table("tinder_matches").filter(r.hashMap
                                ("user_id", user.getId()).with("swipedRight", true)).run(connection));
                        if (number >= matches.size()) {
                            sendTranslatedMessage("Sorry, but you don't have this many matches!", channel, user);
                            return;
                        }
                        TinderMatch selected = matches.get(number);
                        r.table("tinder_matches").filter(r.hashMap("user_id", user.getId()).with("person_id", selected.getPerson_id()))
                                .delete().run(connection);
                        sendTranslatedMessage(":ok_hand: Removed that person from your connection list", channel, user);
                    }
                }
                catch (NumberFormatException e) {
                    sendTranslatedMessage("Usage: /tinder remove [person's number in /tinder connect]", channel, user);
                }
            }
        });
    }

}
