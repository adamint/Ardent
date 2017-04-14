package tk.ardentbot.BotCommands.RPG;

import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;
import tk.ardentbot.Core.CommandExecution.Command;
import tk.ardentbot.Core.Translation.Language;
import tk.ardentbot.Core.Translation.Translation;
import tk.ardentbot.Core.Translation.TranslationResponse;
import tk.ardentbot.Rethink.Models.Marriage;
import tk.ardentbot.Utils.Discord.MessageUtils;
import tk.ardentbot.Utils.Discord.UserUtils;
import tk.ardentbot.Utils.RPGUtils.Profiles.Profile;
import tk.ardentbot.Utils.RPGUtils.RPGUtils;

import java.util.ArrayList;
import java.util.HashMap;

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
        Profile profile = Profile.get(user);

        EmbedBuilder builder = MessageUtils.getDefaultEmbed(guild, user, this);
        builder.setAuthor(response.get(0).getTranslation().replace("{0}", user.getName()), getShard().url, user.getAvatarUrl());

        Marriage marriage = Marry.getMarriage(user);
        if (marriage == null) builder.addField(response.get(1).getTranslation(), response.get(2).getTranslation(), true);
        else {
            builder.addField(response.get(1).getTranslation(), UserUtils.getNameWithDiscriminator(marriage.getUser_one().equals(user
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
