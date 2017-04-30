package tk.ardentbot.commands.games;

import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.*;
import tk.ardentbot.core.executor.Command;
import tk.ardentbot.core.executor.Subcommand;
import tk.ardentbot.core.misc.logging.BotException;
import tk.ardentbot.main.Shard;
import tk.ardentbot.utils.discord.GuildUtils;
import tk.ardentbot.utils.models.TriviaQuestion;
import tk.ardentbot.utils.rpg.TriviaGame;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Trivia extends Command {
    public static final CopyOnWriteArrayList<TriviaGame> gamesInSession = new CopyOnWriteArrayList<>();
    public static final CopyOnWriteArrayList<String> gamesSettingUp = new CopyOnWriteArrayList<>();
    public static ArrayList<TriviaQuestion> triviaQuestions = new ArrayList<>();

    public Trivia(CommandSettings commandSettings) {
        super(commandSettings);
    }

    public static void dispatchRound(Guild guild, TextChannel channel, User creator, TriviaGame currentGame, ScheduledExecutorService ex) {
        try {
            Shard shard = GuildUtils.getShard(guild);
            currentGame.incrementRounds();
            if (currentGame.getRound() >= currentGame.getTotalRounds()) {
                currentGame.finish(shard, shard.help);
                return;
            }
            if (!gamesInSession.contains(currentGame)) return;
            channel.sendMessage(currentGame.displayScores(shard, shard.help).build()).queue();
            TriviaQuestion triviaQuestion = triviaQuestions.get(new SecureRandom().nextInt(triviaQuestions.size()));
            currentGame.setCurrentTriviaQuestion(triviaQuestion);
            StringBuilder question = new StringBuilder();
            question.append("**" + triviaQuestion.getCategory() + "**: " + triviaQuestion.getQuestion());
            shard.help.sendTranslatedMessage(question.toString(), channel, creator);

            int currentRound = currentGame.getRound();
            ex.schedule(() -> {
                if (!currentGame.isAnsweredCurrentQuestion() && currentRound == currentGame.getRound() && !(currentRound ==
                        currentGame.getTotalRounds()))
                {
                    try {
                        shard.help.sendEditedTranslation("No one got it right! The correct answer was {0}", creator, channel, triviaQuestion
                                .getAnswers().get(0));
                        if (currentGame.getRound() + 1 < currentGame.getTotalRounds())
                            dispatchRound(guild, channel, creator, currentGame, ex);
                        else currentGame.finish(shard, shard.help);
                    }
                    catch (Exception e) {
                        new BotException(e);
                    }
                }
            }, 17, TimeUnit.SECONDS);
        }
        catch (Exception ignored) {
        }
    }

    @Override
    public void noArgs(Guild guild, MessageChannel channel, User user, Message message, String[] args) throws Exception {
        sendHelp(channel, guild, user, this);
    }

    @Override
    public void setupSubcommands() throws Exception {
        subcommands.add(new Subcommand("Start a new trivia game", "start", "start") {
            @Override
            public void onCall(Guild guild, MessageChannel channel, User user, Message message, String[] args) throws
                    Exception {
                for (TriviaGame triviaGame : gamesInSession) {
                    if (triviaGame.getGuildId().equalsIgnoreCase(guild.getId())) {
                        sendTranslatedMessage("There's already a game in session in this server!", channel, user);
                        return;
                    }
                }
                if (gamesSettingUp.contains(guild.getId())) {
                    sendTranslatedMessage("There's already a game in session in this server!", channel, user);
                    return;
                }
                gamesSettingUp.add(guild.getId());
                sendTranslatedMessage("Do you want to play this solo? Type `yes` if so, or `no` if not", channel, user);
                interactiveOperation(channel, message, (soloMessage) -> {
                    String content = soloMessage.getContent();
                    boolean solo;
                    solo = content.equalsIgnoreCase("yes");
                    TriviaGame currentGame = new TriviaGame(user, solo, (TextChannel) channel, 15);
                    gamesInSession.add(currentGame);
                    sendTranslatedMessage("The game is starting! Type your answers in this channel. You have **15** seconds to answer " +
                            "each question.", channel, user);
                    commenceRounds(guild, (TextChannel) channel, user, currentGame);
                    gamesSettingUp.remove(guild.getId());
                });
            }
        });

        subcommands.add(new Subcommand("Stop a currently active trivia game", "stop", "stop") {
            @Override
            public void onCall(Guild guild, MessageChannel channel, User user, Message message, String[] args) throws
                    Exception {
                if (guild.getMember(user).hasPermission(Permission.MANAGE_SERVER)) {
                    if (gamesInSession.stream().filter(game -> game.getGuildId().equals(guild.getId())).count() > 0 || gamesSettingUp
                            .contains(guild.getId()))
                    {
                        gamesSettingUp.remove(guild.getId());
                        gamesInSession.removeIf(g -> g.getGuildId().equals(guild.getId()));
                        sendTranslatedMessage("Stopped the trivia game in session.", channel, user);
                    }
                    else {
                        sendTranslatedMessage("There isn't a trivia game running!", channel, user);
                    }
                }
                else sendTranslatedMessage("You need the Manage Server permission to use this command", channel, user);
            }
        });
    }

    private void commenceRounds(Guild guild, TextChannel channel, User creator, TriviaGame currentGame) {
        currentGame.setEx(Executors.newSingleThreadScheduledExecutor());
        dispatchRound(guild, channel, creator, currentGame, currentGame.getEx());
    }
}
