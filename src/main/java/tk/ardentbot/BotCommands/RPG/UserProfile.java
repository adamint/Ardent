package tk.ardentbot.BotCommands.RPG;

import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.exceptions.PermissionException;
import tk.ardentbot.Core.CommandExecution.Command;
import tk.ardentbot.Core.Translation.Language;
import tk.ardentbot.Core.Translation.Translation;
import tk.ardentbot.Core.Translation.TranslationResponse;
import tk.ardentbot.Utils.RPGUtils.Profiles.Profile;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;

public class UserProfile extends Command {
    public UserProfile(CommandSettings commandSettings) {
        super(commandSettings);
    }

    @Override
    public void noArgs(Guild guild, MessageChannel channel, User user, Message message, String[] args, Language language) throws Exception {
        ArrayList<Translation> translations = new ArrayList<>();
        translations.add(new Translation("profile", "titlename"));

        HashMap<Integer, TranslationResponse> response = getTranslations(language, translations);

        Profile profile = Profile.get(user);
        BufferedImage profileImage = ImageIO.read(new URL(
                "https://s-media-cache-ak0.pinimg.com/736x/4d/10/77/4d1077fb1750b5d16f413a60707fa57a.jpg"));

        Graphics2D graphics = (Graphics2D) profileImage.getGraphics().create();
        int width = graphics.getDeviceConfiguration().getBounds().width;
        graphics.setStroke(new java.awt.BasicStroke(3));
        graphics.setColor(Color.white);

        URLConnection imgConn = new URL(user.getAvatarUrl()).openConnection();
        imgConn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.11 (KHTML, like Gecko) " +
                "Chrome/23.0.1271.95 Safari/537.11");

        String name = user.getName();
        if (name.length() >= 6) graphics.setFont(new Font("Jokerman", Font.PLAIN, 24));
        else graphics.setFont(new Font("Jokerman", Font.PLAIN, 28));

        graphics.drawString(response.get(0).getTranslation().replace("{0}", cutToSeven(user.getName())), 25, 75);

        graphics.drawImage(ImageIO.read(imgConn.getInputStream()), width - 112, 17, 100, 100, null);
        graphics.drawRect(width - 115, 15, 104, 104);

        graphics.dispose();

        File file = new File("img.png");
        ImageIO.write(profileImage, "png", file);
        try {
            channel.sendFile(file, null).queue();
        }
        catch (PermissionException ex) {
            sendRetrievedTranslation(channel, "other", language, "giveattachfiles", user);
        }
    }

    private String cutToSeven(String name) {
        if (name.length() <= 7) return name;
        else return name.substring(0, 7);
    }

    @Override
    public void setupSubcommands() throws Exception {
    }


}
