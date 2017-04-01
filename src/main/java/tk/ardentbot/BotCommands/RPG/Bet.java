package tk.ardentbot.BotCommands.RPG;

import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;
import tk.ardentbot.Core.CommandExecution.Command;
import tk.ardentbot.Core.CommandExecution.Subcommand;
import tk.ardentbot.Core.Translation.Language;
import tk.ardentbot.Utils.RPGUtils.Profiles.Profile;
import tk.ardentbot.Utils.RPGUtils.RPGUtils;

import java.util.Random;

public class Bet extends Command {
    public Bet(CommandSettings commandSettings) {
        super(commandSettings);
    }

    @Override
    public void noArgs(Guild guild, MessageChannel channel, User user, Message message, String[] args, Language language) throws Exception {
        sendHelp(language, channel, guild, user, this);
    }

    @Override
    public void setupSubcommands() throws Exception {
        subcommands.add(new Subcommand(this, "start") {
            @Override
            public void onCall(Guild guild, MessageChannel channel, User user, Message message, String[] args, Language language) throws
                    Exception {
                if (args.length == 2) {
                    sendRetrievedTranslation(channel, "prune", language, "notanumber", user);
                    return;
                }
                try {
                    double amountToBet = Double.parseDouble(args[2]);
                    Profile profile = Profile.get(user);
                    if (amountToBet <= 0 || profile.getMoneyAmount() < amountToBet) {
                        sendRetrievedTranslation(channel, "bet", language, "lulno", user);
                        return;
                    }
                    sendEditedTranslation("bet", language, "areyousure", user, channel, RPGUtils.formatMoney(amountToBet));
                    interactivate(language, channel, message, (returnedMessage) -> {
                        if (returnedMessage.getContent().equalsIgnoreCase("yes")) {
                            sendRetrievedTranslation(channel, "bet", language, "numbetween1and2", user);
                            interactivate(language, channel, message, (numberInput) -> {
                                try {
                                    int num = Integer.parseInt(numberInput.getContent());
                                    if (num > 0 && num <= 2) {
                                        int generated = new Random().nextInt(2) + 1;
                                        if (num == generated) {
                                            profile.addMoney(amountToBet);
                                            sendEditedTranslation("bet", language, "youwon", user, channel, RPGUtils.formatMoney
                                                    (amountToBet));
                                        }
                                        else {
                                            sendEditedTranslation("bet", language, "youlost", user, channel, RPGUtils.formatMoney
                                                    (amountToBet), String.valueOf(generated));
                                            profile.removeMoney(amountToBet);
                                        }
                                    }
                                    else sendRetrievedTranslation(channel, "bet", language, "invalidnumberspecified", user);
                                }
                                catch (Exception ex) {
                                    sendRetrievedTranslation(channel, "bet", language, "invalidnumberspecified", user);
                                }
                            });
                        }
                        else {
                            sendRetrievedTranslation(channel, "bet", language, "cancellingbet", user);
                        }
                    });
                }
                catch (NumberFormatException e) {
                    sendRetrievedTranslation(channel, "prune", language, "notanumber", user);
                }
            }
        });

        subcommands.add(new Subcommand(this, "all") {
            @Override
            public void onCall(Guild guild, MessageChannel channel, User user, Message message, String[] args, Language language) throws
                    Exception {
                Profile profile = Profile.get(user);
                sendEditedTranslation("bet", language, "areyousure", user, channel, RPGUtils.formatMoney(profile.getMoneyAmount()));
                interactivate(language, channel, message, (returnedMessage) -> {
                    if (returnedMessage.getContent().equalsIgnoreCase("yes")) {
                        sendRetrievedTranslation(channel, "bet", language, "numbetween1and2", user);
                        interactivate(language, channel, message, (numberInput) -> {
                            try {
                                int num = Integer.parseInt(numberInput.getContent());
                                if (num > 0 && num <= 2) {
                                    int generated = new Random().nextInt(2) + 1;
                                    if (num == generated) {
                                        sendEditedTranslation("bet", language, "youwon", user, channel, RPGUtils.formatMoney(profile
                                                .getMoneyAmount()));
                                        profile.addMoney(profile.getMoneyAmount());
                                    }
                                    else {
                                        sendEditedTranslation("bet", language, "youlost", user, channel, RPGUtils.formatMoney(profile
                                                .getMoneyAmount()), String.valueOf(generated));
                                        profile.removeMoney(profile.getMoneyAmount());
                                    }
                                }
                                else sendRetrievedTranslation(channel, "bet", language, "invalidnumberspecified", user);
                            }
                            catch (Exception ex) {
                                sendRetrievedTranslation(channel, "bet", language, "invalidnumberspecified", user);
                            }
                        });
                    }
                    else {
                        sendRetrievedTranslation(channel, "bet", language, "cancellingbet", user);
                    }
                });
            }
        });
    }
}
