package tk.ardentbot.botCommands.rpg;

import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;
import tk.ardentbot.core.commandExecutor.Command;
import tk.ardentbot.core.commandExecutor.Subcommand;
import tk.ardentbot.core.translation.Language;
import tk.ardentbot.utils.discord.MessageUtils;
import tk.ardentbot.utils.rpgUtils.BadgesList;
import tk.ardentbot.utils.rpgUtils.profiles.Badge;
import tk.ardentbot.utils.rpgUtils.profiles.Profile;

public class Badges extends Command {
    public Badges(CommandSettings commandSettings) {
        super(commandSettings);
    }

    @Override
    public void noArgs(Guild guild, MessageChannel channel, User user, Message message, String[] args, Language language) throws Exception {
        sendHelp(language, channel, guild, user, this);
    }

    @Override
    public void setupSubcommands() throws Exception {
        subcommands.add(new Subcommand(this, "mine") {
            @Override
            public void onCall(Guild guild, MessageChannel channel, User user, Message message, String[] args, Language language) throws
                    Exception {
                String yourBadges = getTranslation("badges", language, "mybadges").getTranslation();
                EmbedBuilder builder = MessageUtils.getDefaultEmbed(guild, user, Badges.this);
                builder.setAuthor(yourBadges, getShard().url, getShard().bot.getAvatarUrl());
                StringBuilder description = new StringBuilder();
                description.append("**" + yourBadges + "**");
                for (Badge badge : Profile.get(user).getBadges()) {
                    BadgesList b = BadgesList.from(badge.getId());
                    if (b != null) {
                        description.append("\n + " + b.getName() + " - " + b.getDescription());
                        description.append("\n   ID: " + b.getId());
                    }
                }
                sendEmbed(builder.setDescription(description.toString()), channel, user);
            }
        });

        subcommands.add(new Subcommand(this, "buy") {
            @Override
            public void onCall(Guild guild, MessageChannel channel, User user, Message message, String[] args, Language language) throws
                    Exception {
                if (args.length == 2) sendRetrievedTranslation(channel, "badges", language, "specifyid", user);
                else {
                    String id = args[2];
                    BadgesList badgeToBuy = BadgesList.from(id);
                    if (badgeToBuy == null) {
                        sendRetrievedTranslation(channel, "badges", language, "specifyid", user);
                        return;
                    }
                    Profile profile = Profile.get(user);
                    if (profile.getMoney() < badgeToBuy.getCost()) {
                        sendRetrievedTranslation(channel, "rpg", language, "notenoughmoney", user);
                    }
                    else {
                        boolean succeeded = profile.addBadge(badgeToBuy);
                        if (succeeded) {
                            sendEditedTranslation("badges", language, "bought", user, channel, badgeToBuy.getName());
                            profile.removeMoney(badgeToBuy.getCost());
                        }
                        else {
                            sendRetrievedTranslation(channel, "badges", language, "failedtoadd", user);
                        }
                    }
                }
            }
        });

        subcommands.add(new Subcommand(this, "view") {
            @Override
            public void onCall(Guild guild, MessageChannel channel, User user, Message message, String[] args, Language language) throws
                    Exception {
                String badgesList = getTranslation("badges", language, "badgeslist").getTranslation();
                EmbedBuilder builder = MessageUtils.getDefaultEmbed(guild, user, Badges.this);
                builder.setAuthor(badgesList, getShard().url, guild.getSelfMember().getUser().getAvatarUrl());
                StringBuilder description = new StringBuilder();
                description.append("**" + badgesList + "**");
                for (BadgesList b : BadgesList.values()) {
                    description.append("\n + " + b.getName() + " - " + b.getDescription());
                    description.append("\n   ID: " + b.getId());
                }
                sendEmbed(builder.setDescription(description.toString()), channel, user);
            }
        });
    }
}
