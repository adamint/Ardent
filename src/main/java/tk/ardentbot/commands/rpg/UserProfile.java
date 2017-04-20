package tk.ardentbot.commands.rpg;

import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;
import tk.ardentbot.core.executor.Command;
import tk.ardentbot.core.translate.Language;
import tk.ardentbot.core.translate.Translation;
import tk.ardentbot.core.translate.TranslationResponse;
import tk.ardentbot.rethink.models.Marriage;
import tk.ardentbot.utils.discord.MessageUtils;
import tk.ardentbot.utils.discord.UserUtils;
import tk.ardentbot.utils.rpg.RPGUtils;
import tk.ardentbot.utils.rpg.profiles.Profile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class UserProfile extends Command {
    public UserProfile(CommandSettings commandSettings) {
        super(commandSettings);
    }

    @Override
    public void noArgs(Guild guild, MessageChannel channel, User user, Message message, String[] args, Language language) throws Exception {
        ArrayList<Translation> translations = new ArrayList<>();
        translations.add(new Translation("profile", "titlename"));
        translations.add(new Translation("profile", "marriedto"));
        translations.add(new Translation("profile", "nobody"));
        translations.add(new Translation("profile", "money"));
        translations.add(new Translation("profile", "items"));
        HashMap<Integer, TranslationResponse> response = getTranslations(language, translations);
        User toGet;
        List<User> mentionedUsers = message.getMentionedUsers();
        if (mentionedUsers.size() == 0) toGet = user;
        else toGet = mentionedUsers.get(0);

        Profile profile = Profile.get(toGet);

        EmbedBuilder builder = MessageUtils.getDefaultEmbed(guild, user, this);
        builder.setAuthor(response.get(0).getTranslation().replace("{0}", toGet.getName()), getShard().url, toGet.getAvatarUrl());

        Marriage marriage = Marry.getMarriage(toGet);
        if (marriage == null) builder.addField(response.get(1).getTranslation(), response.get(2).getTranslation(), true);
        else {
            builder.addField(response.get(1).getTranslation(), UserUtils.getNameWithDiscriminator(marriage.getUser_one().equals(toGet
                    .getId()) ? marriage.getUser_two() : marriage.getUser_one()), true);
        }
        builder.addField(response.get(3).getTranslation(), RPGUtils.formatMoney(profile.getMoney()), true);
        builder.addField(response.get(4).getTranslation(), "Coming soon!", true);

        sendEmbed(builder, channel, user);
    }


    @Override
    public void setupSubcommands() throws Exception {
    }


}
