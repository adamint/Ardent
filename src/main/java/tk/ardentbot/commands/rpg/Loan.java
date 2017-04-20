package tk.ardentbot.commands.rpg;

import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;
import tk.ardentbot.Core.executor.Command;
import tk.ardentbot.Core.executor.Subcommand;
import tk.ardentbot.Core.translate.Language;
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
    public void noArgs(Guild guild, MessageChannel channel, User user, Message message, String[] args, Language language) throws Exception {
        sendHelp(language, channel, guild, user, this);
    }

    @Override
    public void setupSubcommands() throws Exception {
        subcommands.add(new Subcommand(this, "cancel") {
            @Override
            public void onCall(Guild guild, MessageChannel channel, User user, Message message, String[] args, Language language) throws
                    Exception {

            }
        });

        subcommands.add(new Subcommand(this, "mylenders") {
            @Override
            public void onCall(Guild guild, MessageChannel channel, User user, Message message, String[] args, Language language) throws
                    Exception {

            }
        });

        subcommands.add(new Subcommand(this, "myloans") {
            @Override
            public void onCall(Guild guild, MessageChannel channel, User user, Message message, String[] args, Language language) throws
                    Exception {

            }
        });

        subcommands.add(new Subcommand(this, "collect") {
            @Override
            public void onCall(Guild guild, MessageChannel channel, User user, Message message, String[] args, Language language) throws
                    Exception {

            }
        });

        subcommands.add(new Subcommand(this, "give") {
            @Override
            public void onCall(Guild guild, MessageChannel channel, User user, Message message, String[] args, Language language) throws
                    Exception {
                List<User> mentionedUsers = message.getMentionedUsers();
                if (mentionedUsers.size() == 0) {
                    sendRetrievedTranslation(channel, "other", language, "mentionuser", user);
                    return;
                }
                User loanTo = mentionedUsers.get(0);
                ArrayList<LoanModel> loansBetween = queryAsArrayList(LoanModel.class, r.table("loans").filter(row -> row.g("loaner_id")
                        .eq(user.getId())
                        .and(row.g("receiver_id").eq(loanTo.getId()))).run(connection));
                if (loansBetween.size() > 0) {
                    sendRetrievedTranslation(channel, "loan", language, "alreadyloaningtothisperson", user);
                    return;
                }
                try {
                    double amount = Double.parseDouble(replace(message.getRawContent(), 3));
                    Profile userProfile = Profile.get(user);
                    if (userProfile.getMoney() < amount) {
                        sendRetrievedTranslation(channel, "loan", language, "notenoughmoney", user);
                        return;
                    }
                    sendRetrievedTranslation(channel, "loan", language, "enteryourinterestrate", user);
                    interactiveOperation(language, channel, message, interestMessage -> {
                        try {
                            double interestPercentage = Double.parseDouble(interestMessage.getContent().replace("%", ""));
                            sendRetrievedTranslation(channel, "loan", language, "enteramountofdaysforrepayment", user);
                            interactiveOperation(language, channel, message, repaymentDaysMessage -> {
                                try {
                                    double days = Double.parseDouble(repaymentDaysMessage.getContent());
                                    sendEditedTranslation("loan", language, "offeredtoloan", user, channel, loanTo.getAsMention(), user
                                            .getAsMention(), RPGUtils.formatMoney(amount), String.valueOf(interestPercentage), String
                                            .valueOf(days));
                                    longInteractiveOperation(language, channel, message, loanTo, 30, yesOrNoResponseMessage -> {
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
                                    sendRetrievedTranslation(channel, "tag", language, "invalidarguments", user);
                                }
                            });
                        }
                        catch (Exception e) {
                            sendRetrievedTranslation(channel, "tag", language, "invalidarguments", user);
                        }
                    });
                }
                catch (Exception e) {
                    sendRetrievedTranslation(channel, "tag", language, "invalidarguments", user);
                }
            }
        });
    }
}
