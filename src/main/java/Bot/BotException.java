package Bot;

import org.apache.commons.lang3.exception.ExceptionUtils;

import static Main.Ardent.help;
import static Main.Ardent.jda;

/**
 * Sends exceptions to the error channel
 */
public class BotException {
    public BotException(Exception ex) {
        help.sendTranslatedMessage("```" + ExceptionUtils.getStackTrace(ex) + "```", jda.getTextChannelById("270572632343183361"));
        ex.printStackTrace();
     }

    public BotException(String s) {
        help.sendTranslatedMessage("```" + s + "```", jda.getTextChannelById("270572632343183361"));
    }
}
