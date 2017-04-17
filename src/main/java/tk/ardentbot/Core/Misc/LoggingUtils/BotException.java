package tk.ardentbot.core.misc.loggingUtils;

import org.apache.commons.lang3.exception.ExceptionUtils;

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
}
