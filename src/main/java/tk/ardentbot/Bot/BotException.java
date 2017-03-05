package tk.ardentbot.Bot;

import org.apache.commons.lang3.exception.ExceptionUtils;

import static tk.ardentbot.Main.Ardent.ardent;

/**
 * Sends exceptions to the error channel
 */
public class BotException {
    public BotException(Exception ex) {
        ardent.help.sendTranslatedMessage("```" + ExceptionUtils.getStackTrace(ex) + "```", ardent.jda
                .getTextChannelById("270572632343183361"));
        ex.printStackTrace();
     }

    public BotException(String s) {
        ardent.help.sendTranslatedMessage("```" + s + "```", ardent.jda.getTextChannelById("270572632343183361"));
    }
}
