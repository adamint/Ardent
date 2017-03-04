package tk.ardentbot.Utils.Discord;

public class Emoji {
    private String packName;
    private String name;
    private String imageURL;

    public Emoji(String packName, String name, String imageURL) {
        this.packName = packName;
        this.name = name;
        this.imageURL = imageURL;
    }

    public String getPackName() {
        return packName;
    }

    public String getName() {
        return name;
    }

    public String getImageURL() {
        return imageURL;
    }
}
