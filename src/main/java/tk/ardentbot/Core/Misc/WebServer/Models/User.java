package tk.ardentbot.core.misc.webServer.models;

/**
 * SparkServer representation of JDA Users
 */
public class User {
    public String id;
    public String username;
    public String discriminator;
    public String avatar;
    public String role;

    public User(String id, String username, String discriminator, String avatar, String role) {
        this.id = id;
        this.username = username;
        this.discriminator = discriminator;
        this.avatar = avatar;
        this.role = role;
    }
}