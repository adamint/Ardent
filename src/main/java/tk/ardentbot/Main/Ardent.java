package tk.ardentbot.Main;

import tk.ardentbot.Core.WebServer.SparkServer;

import java.util.ArrayList;

public class Ardent {
    /**
     * Single instance for now
     */
    public static Instance ardent;

    public static ArrayList<String> patrons = new ArrayList<>();
    public static ArrayList<String> developers = new ArrayList<>();
    public static ArrayList<String> moderators = new ArrayList<>();
    public static ArrayList<String> translators = new ArrayList<>();

    public static void main(String[] args) throws Exception {
        ardent = new Instance(true);
        SparkServer.setup();
    }
}
