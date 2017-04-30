package tk.ardentbot.commands.games;

import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;
import tk.ardentbot.core.executor.Command;
import tk.ardentbot.core.misc.logging.BotException;
import tk.ardentbot.utils.discord.MessageUtils;
import tk.ardentbot.utils.games.Hand;
import tk.ardentbot.utils.rpg.RPGUtils;
import tk.ardentbot.utils.rpg.profiles.Profile;

import java.util.concurrent.CopyOnWriteArrayList;

public class Blackjack extends Command {
    private static CopyOnWriteArrayList<String> sessions = new CopyOnWriteArrayList<>();

    public Blackjack(CommandSettings commandSettings) {
        super(commandSettings);
    }

    @Override
    public void noArgs(Guild guild, MessageChannel channel, User user, Message message, String[] args) throws Exception {
        if (sessions.contains(user.getId())) return;
        sendTranslatedMessage("Enter an amount to bet", channel, user);
        interactiveOperation(channel, message, betMessage -> {
            try {
                int amountToBet = Integer.parseInt(betMessage.getContent());
                Profile profile = Profile.get(user);
                if (amountToBet <= 0 || profile.getMoney() < amountToBet) {
                    sendTranslatedMessage("You entered an invalid amount of money", channel, user);
                    return;
                }
                else if (amountToBet > 5000) {
                    sendTranslatedMessage("The max bet is **$5,000**!", channel, user);
                    return;
                }
                Hand yourHand = new Hand().generate().generate();
                Hand dealerHand = new Hand().generate().generate();
                sessions.add(user.getId());
                dispatchRound(amountToBet, yourHand, dealerHand, guild, channel, user, message, args);
            }
            catch (Exception e) {
                sendTranslatedMessage("You need to enter a whole number. Cancelling...", channel, user);
            }
        });
    }

    @Override
    public void setupSubcommands() throws Exception {
    }

    public void dispatchRound(int bet, Hand yourHand, Hand dealerHand, Guild guild,
                              MessageChannel
                                      channel, User user, Message message, String[] args) {
        EmbedBuilder builder = MessageUtils.getDefaultEmbed(user);
        builder.setAuthor("Blackjack | " + user.getName(), user.getEffectiveAvatarUrl(), user
                .getEffectiveAvatarUrl());
        builder.setDescription("Type `hit` to get another card, `stay` to stay with your current cards, or `cancel` to cancel the game " +
                "(you " +
                "will lose 50% of your original bet)");
        builder.addField("Your hand", yourHand.readable(), true);
        builder.addField("My hand", dealerHand.readable(), true);
        sendEmbed(builder, channel, user);
        boolean success = longInteractiveOperation(channel, message, 25, actionMessage -> {
            String content = actionMessage.getContent();
            if (content.equalsIgnoreCase("hit")) {
                yourHand.generate();
                if (yourHand.total() > 21) {
                    showResults(bet, yourHand, dealerHand, guild, channel, user, message, args);
                }
                else dispatchRound(bet, yourHand, dealerHand, guild, channel, user, message, args);
            }
            else if (content.equalsIgnoreCase("stay")) {
                showResults(bet, yourHand, dealerHand, guild, channel, user, message, args);
            }
            else if (content.equalsIgnoreCase("cancel")) {
                sendTranslatedMessage("Cancelled game, but you lost 50% of your original bet", channel, user);
                sessions.remove(user.getId());
                Profile.get(user).removeMoney(bet / 2);
            }
            else {
                sendTranslatedMessage("Invalid input! Restarting your round... type `cancel` if you want to cancel this game", channel,
                        user);
                dispatchRound(bet, yourHand, dealerHand, guild, channel, user, message, args);
            }
        });
        if (!success) sessions.remove(user.getId());
    }

    public void showResults(int bet, Hand yourHand, Hand dealerHand, Guild guild, MessageChannel channel, User user, Message message,
                            String[] args) {
        try {
            EmbedBuilder builder = MessageUtils.getDefaultEmbed(user);
            builder.setAuthor("Blackjack | Game over", user.getEffectiveAvatarUrl(), user.getEffectiveAvatarUrl());
            if (yourHand.total() > 21) {
                builder.setDescription("You busted and lost {0} :frowning:".replace("{0}", RPGUtils.formatMoney(bet)));
                Profile.get(user).removeMoney(bet);
            }
            while (dealerHand.total() < 17) dealerHand.generate();
            if (dealerHand.total() > 21 && yourHand.total() <= 21 || dealerHand.total() < yourHand.total()) {
                if (dealerHand.total() > 21) builder.setDescription("I busted! You won {0}".replace("{0}", RPGUtils.formatMoney(bet)));
                else builder.setDescription("You had a higher numbered hand than me! You win {0}");
                Profile.get(user).addMoney(bet);
            }
            else if (dealerHand.total() == yourHand.total()) builder.setDescription("We tied! You don't win or lose anything >.>");
            else {
                builder.setDescription("I had a higher numbered hand! You lose {0}".replace("{0}", RPGUtils.formatMoney(bet)));
                Profile.get(user).removeMoney(bet);
            }
            builder.addField("Your hand", yourHand.readable(), true);
            builder.addField("My hand", dealerHand.readable(), true);
            sendEmbed(builder, channel, user);
            sessions.remove(user.getId());
        }
        catch (Exception e) {
            new BotException(e);
        }
    }
}
