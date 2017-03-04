package tk.ardentbot.akioTemporary.commands;

import java.util.HashMap;

public class ArdentExecutorFactory {

    private HashMap<String, ArdentCommand> commands = new HashMap<>();

    /**
     *
     * Link a ArdentCommand with a String command
     *
     * @param command
     * @param ardentCommand
     */
    public void register(String command, ArdentCommand ardentCommand){
        this.commands.putIfAbsent(command, ardentCommand);
    }

    /**
     *
     * Return the ArdentCommand object for this command.
     *
     * @param command String command to execute.
     * @return        NULL if no command found.
     */
    public ArdentCommand getCommandExecutor(String command){
        return this.commands.get(command);
    }

}
