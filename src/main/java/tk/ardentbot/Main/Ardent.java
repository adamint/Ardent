package tk.ardentbot.Main;

import tk.ardentbot.Backend.Web.WebServer;

public class Ardent {
    /**
     * Single instance for now
     */
    public static Instance ardent;
    public static void main(String[] args) throws Exception {
        ardent = new Instance();
        WebServer.setup();
    }
}
