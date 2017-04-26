package tk.ardentbot.commands.games;

import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;
import tk.ardentbot.core.executor.Command;
import tk.ardentbot.core.misc.logging.BotException;
import tk.ardentbot.core.translate.Language;
import tk.ardentbot.core.translate.Translation;
import tk.ardentbot.core.translate.TranslationResponse;
import tk.ardentbot.utils.discord.MessageUtils;
import tk.ardentbot.utils.games.Hand;
import tk.ardentbot.utils.rpg.RPGUtils;
import tk.ardentbot.utils.rpg.profiles.Profile;

import java.util.HashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class Blackjack extends Command {
    private static CopyOnWriteArrayList<String> sessions = new CopyOnWriteArrayList<>();

    public Blackjack(CommandSettings commandSettings) {
        super(commandSettings);
    }

    @Override
    public void noArgs(Guild guild, MessageChannel channel, User user, Message message, String[] args, Language language) throws Exception {
        if (sessions.contains(user.getId())) return;
        sendRetrievedTranslation(channel, "blackjack", language, "enterbet", user);
        interactiveOperation(language, channel, message, betMessage -> {
            try {
                int amountToBet = Integer.parseInt(betMessage.getContent());
                Profile profile = Profile.get(user);
                if (amountToBet <= 0 || profile.getMoney() < amountToBet) {
                    sendRetrievedTranslation(channel, "bet", language, "lulno", user);
                    return;
                }
                HashMap<Integer, TranslationResponse> translations = getTranslations(language, new Translation("blackjack", "game"),
                        new Translation("blackjack", "actions"), new Translation("blackjack", "yourhand"),
                        new Translation("blackjack", "myhand"));
                Hand yourHand = new Hand().generate().generate();
                Hand dealerHand = new Hand().generate().generate();
                sessions.add(user.getId());
                dispatchRound(amountToBet, yourHand, dealerHand, translations, guild, channel, user, message, args, language);
            }
            catch (Exception nfe) {
                sendRetrievedTranslation(channel, "other", language, "enterwholenumber", user);
            }
        });
    }

    @Override
    public void setupSubcommands() throws Exception {
    }

    public void dispatchRound(int bet, Hand yourHand, Hand dealerHand, HashMap<Integer, TranslationResponse> translations, Guild guild,
                              MessageChannel
                                      channel, User user, Message message, String[] args, Language language) {
        EmbedBuilder builder = MessageUtils.getDefaultEmbed(guild, user, this);
        builder.setAuthor(translations.get(0).getTranslation() + " | " + user.getName(), user.getEffectiveAvatarUrl(), user
                .getEffectiveAvatarUrl());
        builder.setDescription(translations.get(1).getTranslation());
        builder.addField(translations.get(2).getTranslation(), yourHand.readable(), true);
        builder.addField(translations.get(3).getTranslation(), dealerHand.readable(), true);
        sendEmbed(builder, channel, user);
        boolean success = interactiveOperation(language, channel, message, actionMessage -> {
            String content = actionMessage.getContent();
            if (content.equalsIgnoreCase("hit")) {
                yourHand.generate();
                if (yourHand.total() > 21)
                    showResults(bet, yourHand, dealerHand, translations, guild, channel, user, message, args, language);
                else dispatchRound(bet, yourHand, dealerHand, translations, guild, channel, user, message, args, language);
            }
            else if (content.equalsIgnoreCase("stay")) {
                showResults(bet, yourHand, dealerHand, translations, guild, channel, user, message, args, language);
            }
            else if (content.equalsIgnoreCase("cancel")) {
                sendRetrievedTranslation(channel, "blackjack", language, "cancelledgame", user);
                sessions.remove(user.getId());
            }
            else {
                sendRetrievedTranslation(channel, "blackjack", language, "invalidinput", user);
                dispatchRound(bet, yourHand, dealerHand, translations, guild, channel, user, message, args, language);
            }
        });
        if (!success) sessions.remove(user.getId());
    }

    public void showResults(int bet, Hand yourHand, Hand dealerHand, HashMap<Integer, TranslationResponse> translations, Guild guild,
                            MessageChannel
                                    channel, User user, Message message, String[] args, Language language) {
        try {
            HashMap<Integer, TranslationResponse> tx = getTranslations(language, new Translation("blackjack", "gameover"),
                    new Translation("blackjack", "ibusted"), new Translation("blackjack", "youhadhigher"),
                    new Translation("blackjack", "youbusted"), new Translation("blackjack", "youhadlower"),
                    new Translation("blackjack", "tied"));
            EmbedBuilder builder = MessageUtils.getDefaultEmbed(guild, user, this);
            builder.setAuthor(tx.get(0).getTranslation(), user.getEffectiveAvatarUrl(), user.getEffectiveAvatarUrl());
            if (yourHand.total() > 21) {
                builder.setDescription(tx.get(3).getTranslation().replace("{0}", RPGUtils.formatMoney(bet)));
                Profile.get(user).removeMoney(bet);
                return;
            }
            while (dealerHand.total() < 17) dealerHand.generate();
            if (dealerHand.total() > 21 && yourHand.total() <= 21 || dealerHand.total() < yourHand.total()) {
                if (dealerHand.total() > 21) builder.setDescription(tx.get(1).getTranslation().replace("{0}", RPGUtils.formatMoney(bet)));
                else builder.setDescription(tx.get(2).getTranslation().replace("{0}", RPGUtils.formatMoney(bet)));
                Profile.get(user).addMoney(bet);
            }

            else if (dealerHand.total() == yourHand.total()) builder.setDescription(tx.get(5).getTranslation());
            else {
                builder.setDescription(tx.get(4).getTranslation().replace("{0}", RPGUtils.formatMoney(bet)));
                Profile.get(user).removeMoney(bet);
            }
            builder.addField(translations.get(2).getTranslation(), yourHand.readable(), true);
            builder.addField(translations.get(3).getTranslation(), dealerHand.readable(), true);
            sendEmbed(builder, channel, user);
            sessions.remove(user.getId());
        }
        catch (Exception e) {
            new BotException(e);
        }
    }
}
