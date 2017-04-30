package tk.ardentbot.commands.rpg;

import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;
import tk.ardentbot.core.executor.Command;
import tk.ardentbot.core.executor.Subcommand;
import tk.ardentbot.utils.rpg.RPGUtils;
import tk.ardentbot.utils.rpg.profiles.Profile;

import java.security.SecureRandom;
import java.util.Random;

public class Bet extends Command {
    public Bet(CommandSettings commandSettings) {
        super(commandSettings);
    }

    @Override
    public void noArgs(Guild guild, MessageChannel channel, User user, Message message, String[] args) throws Exception {
        sendHelp(channel, guild, user, this);
    }

    @Override
    public void setupSubcommands() throws Exception {
        subcommands.add(new Subcommand("Bet a specified amount of money", "start", "start") {
            @Override
            public void onCall(Guild guild, MessageChannel channel, User user, Message message, String[] args) throws
                    Exception {
                if (args.length == 2) {
                    sendTranslatedMessage("You need to specify a bet amount", channel, user);
                    return;
                }
                try {
                    double amountToBet = Double.parseDouble(args[2]);
                    Profile profile = Profile.get(user);
                    if (amountToBet <= 0 || profile.getMoney() < amountToBet) {
                        sendTranslatedMessage("Invalid amount - either it was more than what you have or less than $0", channel, user);
                        return;
                    }
                    sendEditedTranslation("Are you sure you want to bet **{0}**? Type **yes** if so, or **no** to cancel", user, channel,
                            RPGUtils.formatMoney(amountToBet));
                    interactiveOperation(channel, message, (returnedMessage) -> {
                        if (returnedMessage.getContent().equalsIgnoreCase("yes")) {
                            sendTranslatedMessage("Type 1 or 2 below - choose wisely!", channel, user);
                            interactiveOperation(channel, message, (numberInput) -> {
                                try {
                                    int num = Integer.parseInt(numberInput.getContent());
                                    if (num > 0 && num <= 2) {
                                        int generated = new SecureRandom().nextInt(2) + 1;
                                        if (num == generated || new Random().nextInt(20) == 5) {
                                            profile.addMoney(profile.afterCredit(amountToBet));
                                            sendTranslatedMessage("Congrats! You won " + RPGUtils.formatMoney(profile.afterCredit
                                                    (amountToBet)), channel, user);
                                        }
                                        else {
                                            sendTranslatedMessage("Sorry, you lost " + RPGUtils.formatMoney(amountToBet) + " :frowning: " +
                                                    "The correct answer " +
                                                    "was " + generated, channel, user);
                                            profile.removeMoney(amountToBet);
                                        }
                                    }
                                    else sendTranslatedMessage("You specified an invalid number", channel, user);
                                }
                                catch (Exception ex) {
                                    sendTranslatedMessage("You specified an invalid number", channel, user);
                                }
                            });
                        }
                        else {
                            sendTranslatedMessage("Ok, cancelled your bet", channel, user);
                        }
                    });
                }
                catch (NumberFormatException e) {
                    sendTranslatedMessage("That's not a number!", channel, user);
                }
            }
        });

        subcommands.add(new Subcommand("Bet all your money", "all", "all") {
            @Override
            public void onCall(Guild guild, MessageChannel channel, User user, Message message, String[] args) throws
                    Exception {
                Profile profile = Profile.get(user);
                if (profile.getMoney() < 0) {
                    sendTranslatedMessage("You can't bet if you're in debt!", channel, user);
                    return;
                }
                sendEditedTranslation("Are you sure you want to bet all? Type **yes** if so, or **no** to cancel", user, channel,
                        RPGUtils.formatMoney(profile.getMoney()));
                interactiveOperation(channel, message, (returnedMessage) -> {
                    if (returnedMessage.getContent().equalsIgnoreCase("yes")) {
                        sendTranslatedMessage("Type 1 or 2 below - choose wisely!", channel, user);
                        interactiveOperation(channel, message, (numberInput) -> {
                            try {
                                int num = Integer.parseInt(numberInput.getContent());
                                if (num > 0 && num <= 2) {
                                    int generated = new SecureRandom().nextInt(2) + 1;
                                    if (num == generated || new Random().nextInt(20) == 5) {
                                        profile.addMoney(profile.afterCredit(profile.getMoney()));
                                        sendTranslatedMessage("Congrats! You won " + RPGUtils.formatMoney(profile.afterCredit
                                                (profile.getMoney())), channel, user);
                                    }
                                    else {
                                        sendTranslatedMessage("Sorry, you lost " + RPGUtils.formatMoney(profile.getMoney()) + " :frowning: The correct answer " +
                                                "was " + generated, channel, user);
                                        profile.setZero();
                                    }
                                }
                                else sendTranslatedMessage("You specified an invalid number", channel, user);
                            }
                            catch (Exception ex) {
                                sendTranslatedMessage("You specified an invalid number", channel, user);
                            }
                        });
                    }
                    else {
                        sendTranslatedMessage("Ok, cancelled your bet", channel, user);
                    }
                });
            }
        });
    }
}
