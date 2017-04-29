package tk.ardentbot.core.misc.web.models;

import tk.ardentbot.core.executor.Category;

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
