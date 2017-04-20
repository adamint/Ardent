package tk.ardentbot.commands.rpg;

import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;
import tk.ardentbot.core.executor.Command;
import tk.ardentbot.core.executor.Subcommand;
import tk.ardentbot.core.translate.Language;
import tk.ardentbot.utils.rpg.RPGUtils;
import tk.ardentbot.utils.rpg.profiles.Profile;

import java.security.SecureRandom;
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
                    if (amountToBet <= 0 || profile.getMoney() < amountToBet) {
                        sendRetrievedTranslation(channel, "bet", language, "lulno", user);
                        return;
                    }
                    sendEditedTranslation("bet", language, "areyousure", user, channel, RPGUtils.formatMoney(amountToBet));
                    interactiveOperation(language, channel, message, (returnedMessage) -> {
                        if (returnedMessage.getContent().equalsIgnoreCase("yes")) {
                            sendRetrievedTranslation(channel, "bet", language, "numbetween1and2", user);
                            interactiveOperation(language, channel, message, (numberInput) -> {
                                try {
                                    int num = Integer.parseInt(numberInput.getContent());
                                    if (num > 0 && num <= 2) {
                                        int generated = new SecureRandom().nextInt(2) + 1;
                                        if (num == generated || new Random().nextInt(20) == 5) {
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
                if (profile.getMoney() < 0) {
                    sendRetrievedTranslation(channel, "bet", language, "cannotbetbecauseindebt", user);
                    return;
                }
                sendEditedTranslation("bet", language, "areyousure", user, channel, RPGUtils.formatMoney(profile.getMoney()));
                interactiveOperation(language, channel, message, (returnedMessage) -> {
                    if (returnedMessage.getContent().equalsIgnoreCase("yes")) {
                        sendRetrievedTranslation(channel, "bet", language, "numbetween1and2", user);
                        interactiveOperation(language, channel, message, (numberInput) -> {
                            try {
                                int num = Integer.parseInt(numberInput.getContent());
                                if (num > 0 && num <= 2) {
                                    int generated = new SecureRandom().nextInt(2) + 1;
                                    if (num == generated) {
                                        sendEditedTranslation("bet", language, "youwon", user, channel, RPGUtils.formatMoney(profile
                                                .getMoney()));
                                        profile.addMoney(profile.getMoney());
                                    }
                                    else {
                                        sendEditedTranslation("bet", language, "youlost", user, channel, RPGUtils.formatMoney(profile
                                                .getMoney()), String.valueOf(generated));
                                        profile.removeMoney(profile.getMoney());
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
