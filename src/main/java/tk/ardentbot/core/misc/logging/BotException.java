package tk.ardentbot.core.misc.logging;

import org.apache.commons.lang3.exception.ExceptionUtils;
import tk.ardentbot.utils.discord.UserUtils;
import tk.ardentbot.utils.rpg.RPGUtils;

import static tk.ardentbot.main.Ardent.botLogsShard;

/**
 * Sends exceptions to the error channel
 */
public class BotException {
    public BotException(Exception ex) {
        ex.printStackTrace();
        botLogsShard.help.sendTranslatedMessage("```" + ExceptionUtils.getStackTrace(ex) + "```", botLogsShard.jda
                .getTextChannelById("270572632343183361"), null);
    }

    public BotException(String s) {
        botLogsShard.help.sendTranslatedMessage("```" + s + "```", botLogsShard.jda.getTextChannelById
                ("270572632343183361"), null);
    }

    public BotException(String user_id, double amount, double money) {
        if (amount > 0) {
            botLogsShard.help.sendTranslatedMessage(UserUtils.getNameWithDiscriminator(user_id) + " just got " + RPGUtils.formatMoney
                    (amount) + " | " +
                    "Balance: " + RPGUtils.formatMoney(money), botLogsShard.jda.getTextChannelById("305450680913625089"), null);
        }
        else {
            botLogsShard.help.sendTranslatedMessage(UserUtils.getNameWithDiscriminator(user_id) + " just lost " + RPGUtils.formatMoney
                    (amount) + " | " +
                    "Balance: " + RPGUtils.formatMoney(money), botLogsShard.jda.getTextChannelById("305450680913625089"), null);

        }
    }
}
