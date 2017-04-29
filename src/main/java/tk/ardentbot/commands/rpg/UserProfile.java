package tk.ardentbot.commands.rpg;

import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;
import tk.ardentbot.core.executor.Command;
import tk.ardentbot.rethink.models.Marriage;
import tk.ardentbot.utils.discord.MessageUtils;
import tk.ardentbot.utils.discord.UserUtils;
import tk.ardentbot.utils.rpg.RPGUtils;
import tk.ardentbot.utils.rpg.profiles.Profile;

import java.util.List;

public class UserProfile extends Command {
    public UserProfile(CommandSettings commandSettings) {
        super(commandSettings);
    }

    @Override
    public void noArgs(Guild guild, MessageChannel channel, User user, Message message, String[] args) throws Exception {
        User toGet;
        List<User> mentionedUsers = message.getMentionedUsers();
        if (mentionedUsers.size() == 0) toGet = user;
        else toGet = mentionedUsers.get(0);

        Profile profile = Profile.get(toGet);

        EmbedBuilder builder = MessageUtils.getDefaultEmbed(user);
        builder.setAuthor("{0}'s profile".replace("{0}", toGet.getName()), getShard().url, toGet.getAvatarUrl());


        Marriage marriage = Marry.getMarriage(toGet);
        if (marriage == null) builder.addField("Married to", "No one :(", true);
        else {
            builder.addField("Married to", UserUtils.getNameWithDiscriminator(marriage.getUser_one().equals(toGet
                    .getId()) ? marriage.getUser_two() : marriage.getUser_one()), true);
        }
        builder.addField("Balance", RPGUtils.formatMoney(profile.getMoney()), true);
        builder.addField("Items", "Coming soon!", true);

        sendEmbed(builder, channel, user);
    }


    @Override
    public void setupSubcommands() throws Exception {
    }


}
