package tk.ardentbot.commands.rpg;

import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;
import tk.ardentbot.core.executor.Command;
import tk.ardentbot.core.executor.Subcommand;
import tk.ardentbot.rethink.models.LoanModel;
import tk.ardentbot.utils.rpg.RPGUtils;
import tk.ardentbot.utils.rpg.profiles.Profile;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static tk.ardentbot.rethink.Database.connection;
import static tk.ardentbot.rethink.Database.r;

public class Loan extends Command {
    public Loan(CommandSettings commandSettings) {
        super(commandSettings);
    }

    @Override
    public void noArgs(Guild guild, MessageChannel channel, User user, Message message, String[] args) throws Exception {
        sendHelp(channel, guild, user, this);
    }

    @Override
    public void setupSubcommands() throws Exception {
        subcommands.add(new Subcommand("Cancel a loan", "cancel", "cancel") {
            @Override
            public void onCall(Guild guild, MessageChannel channel, User user, Message message, String[] args) throws
                    Exception {

            }
        });

        subcommands.add(new Subcommand("View your lenders", "mylenders", "mylenders") {
            @Override
            public void onCall(Guild guild, MessageChannel channel, User user, Message message, String[] args) throws
                    Exception {

            }
        });

        subcommands.add(new Subcommand("View your loans", "myloans", "myloans") {
            @Override
            public void onCall(Guild guild, MessageChannel channel, User user, Message message, String[] args) throws
                    Exception {

            }
        });

        subcommands.add(new Subcommand(this, "collect") {
            @Override
            public void onCall(Guild guild, MessageChannel channel, User user, Message message, String[] args) throws
                    Exception {
                List<User> mentioned = message.getMentionedUsers();
                if (mentioned.size() == 0) {
                    sendTranslatedMessage("You need to mention a user!", channel, user);
                    return;
                }
                User collectFrom = mentioned.get(0);
                LoanModel loan = asPojo(r.table("loans").filter(row -> row.g("loaner_id")
                        .eq(user.getId()).and(row.g("receiver_id").eq(collectFrom.getId()))).run(connection), LoanModel.class);
                if (loan == null) {
                    sendTranslatedMessage("You haven't loaned to this user!", channel, user);
                    return;
                }
                if (Instant.now().getEpochSecond() > loan.getPayback_by_epoch_second()) {
                    Profile collecteeProfile = Profile.get(collectFrom);
                    if (collecteeProfile.getMoney() >= loan.getEffectiveAmount()) {
                        collecteeProfile.removeMoney(loan.getEffectiveAmount());
                        Profile.get(user).addMoney(loan.getEffectiveAmount());
                        sendEditedTranslation("loan", language, "collected", user, channel, String.valueOf(loan.getEffectiveAmount()),
                                collectFrom.getAsMention());
                    }
                    else {
                        Profile.get(user).addMoney(loan.getEffectiveAmount() / 2);
                        collecteeProfile.setZero().removeMoney(loan.getEffectiveAmount() / 2);
                        collecteeProfile.updateCredit(-25);
                        sendEditedTranslation("loan", language, "defaulted", user, channel, collectFrom.getAsMention(), String.valueOf
                                (loan.getEffectiveAmount() / 2));
                        sendEditedTranslation("loan", language, "defaulted", user, channel, collectFrom.getAsMention(), String.valueOf
                                (-25));

                    }
                }
                else sendEditedTranslation("loan", language, "notyetpayback", user, channel, getDate());
            }
        });

        subcommands.add(new Subcommand("Give a loan", "give", "loan") {
            @Override
            public void onCall(Guild guild, MessageChannel channel, User user, Message message, String[] args) throws
                    Exception {
                List<User> mentionedUsers = message.getMentionedUsers();
                if (mentionedUsers.size() == 0) {
                    sendTranslatedMessage("You need to mention a user!", channel, user);
                    return;
                }
                User loanTo = mentionedUsers.get(0);
                ArrayList<LoanModel> loansBetween = queryAsArrayList(LoanModel.class, r.table("loans").filter(row -> row.g("loaner_id")
                        .eq(user.getId()).and(row.g("receiver_id").eq(loanTo.getId()))).run(connection));
                if (loansBetween.size() > 0) {
                    sendTranslatedMessage("You are already loaning to this user!", channel, user);
                    return;
                }
                try {
                    double amount = Double.parseDouble(replace(message.getRawContent(), 3));
                    Profile userProfile = Profile.get(user);
                    if (userProfile.getMoney() < amount) {
                        sendTranslatedMessage("You don't have enough money", channel, user);
                        return;
                    }
                    sendTranslatedMessage("Enter your interest rate", channel, user);
                    interactiveOperation(channel, message, interestMessage -> {
                        try {
                            double interestPercentage = Double.parseDouble(interestMessage.getContent().replace("%", ""));
                            sendTranslatedMessage("Enter the amount of days for your re-payment", channel, user);
                            interactiveOperation(channel, message, repaymentDaysMessage -> {
                                try {
                                    double days = Double.parseDouble(repaymentDaysMessage.getContent());
                                    sendEditedTranslation("loan", language, "offeredtoloan", user, channel, loanTo.getAsMention(), user
                                            .getAsMention(), RPGUtils.formatMoney(amount), String.valueOf(interestPercentage), String
                                            .valueOf(days));
                                    longInteractiveOperation(channel, message, loanTo, 30, yesOrNoResponseMessage -> {
                                        String content = yesOrNoResponseMessage.getContent();
                                        if (content.equalsIgnoreCase("yes")) {
                                            userProfile.removeMoney(amount);
                                            Profile loanToProfile = Profile.get(loanTo);
                                            loanToProfile.addMoney(amount);
                                            sendEditedTranslation("loan", language, "loanstartsnow", user, channel, loanTo.getAsMention(),
                                                    String.valueOf(days), RPGUtils.formatMoney((amount * (1 + interestPercentage / 100))));
                                            r.table("loans").insert(r.json(gson.toJson(new LoanModel(user.getId(), loanTo.getId(),
                                                    amount, interestPercentage, Instant.now().plusSeconds((long) (days * 24 * 60 * 60))
                                                    .getEpochSecond())))).run(connection);
                                        }
                                        else {
                                            sendEditedTranslation("loan", language, "turneddownloan", user, channel, loanTo.getAsMention
                                                    (), user
                                                    .getAsMention());
                                        }
                                    });
                                }
                                catch (Exception e) {
                                    sendTranslatedMessage("Invalid arguments!", channel, user);
                                }
                            });
                        }
                        catch (Exception e) {
                            sendTranslatedMessage("Invalid arguments!", channel, user);
                        }
                    });
                }
                catch (Exception e) {
                    sendTranslatedMessage("Invalid arguments!", channel, user);
                }
            }
        });
    }
}