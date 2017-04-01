package tk.ardentbot.Core.Misc.WebServer.Models;

import tk.ardentbot.Core.CommandExecution.Category;

/**
 * SparkServer server wrapper for a command
 */
public class Command {
    public String name;
    public Category category;
    public String description;

    public Command(String name, Category category, String description) {
        this.name = name;
        this.category = category;
        this.description = description;
    }
}
