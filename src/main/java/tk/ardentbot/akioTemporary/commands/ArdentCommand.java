package tk.ardentbot.akioTemporary.commands;

public abstract class ArdentCommand {

    public ArdentCmdCategory category;

    public ArdentCommand(ArdentCmdCategory category){
        this.category = category;
    }

    public ArdentCmdCategory getCategory() {
        return category;
    }

    public abstract void execute(CommandMessage cmd);
}