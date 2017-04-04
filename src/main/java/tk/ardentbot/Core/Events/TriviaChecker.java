package tk.ardentbot.Core.Events;

import net.dv8tion.jda.core.entities.ChannelType;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import tk.ardentbot.BotCommands.RPG.Trivia;
import tk.ardentbot.Main.Shard;
import tk.ardentbot.Utils.Discord.GuildUtils;
import tk.ardentbot.Utils.RPGUtils.TriviaGame;

class TriviaChecker {
    static void check(MessageReceivedEvent event) throws Exception {
        if (event.isFromType(ChannelType.TEXT)) {
            User user = event.getAuthor();
            Guild guild = event.getGuild();
            Shard shard = GuildUtils.getShard(guild);
            TextChannel channel = event.getTextChannel();
            for (TriviaGame triviaGame : Trivia.gamesInSession) {
                if (triviaGame.getGuildId().equalsIgnoreCase(guild.getId()) && triviaGame.getTextChannelId().equalsIgnoreCase(channel
                        .getId()))
                {
                    if (!triviaGame.isAnsweredCurrentQuestion()) {
                        if (triviaGame.isSolo() && !triviaGame.getCreator().equalsIgnoreCase(user.getId())) return;
                        String content = event.getMessage().getContent();
                        if (triviaGame.getCurrentTriviaQuestion() != null && content.equalsIgnoreCase(triviaGame
                                .getCurrentTriviaQuestion().getAnswer()))
                        {
                            triviaGame.addPoint(user);
                            shard.help.sendEditedTranslation("trivia", GuildUtils.getLanguage(guild), "gotitright", user, channel,
                                    user.getName());
                            if (triviaGame.getRound() < triviaGame.getTotalRounds()) {
                                Trivia.dispatchRound(guild, channel, guild.getMemberById(triviaGame.getCreator()).getUser(), triviaGame,
                                        triviaGame.getTimer());
                            }
                            else triviaGame.finish(shard, shard.help);
                        }
                    }
                }
            }
        }
    }
}
