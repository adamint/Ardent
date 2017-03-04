package tk.ardentbot.akioTemporary.commands.fun;

import tk.ardentbot.akioTemporary.commands.ArdentCmdCategory;
import tk.ardentbot.akioTemporary.commands.ArdentCommand;
import tk.ardentbot.akioTemporary.commands.CommandMessage;

public class EchoCommand extends ArdentCommand {

    public EchoCommand(){
        super(ArdentCmdCategory.Fun);
    }

    @Override
    public void execute(CommandMessage cmd) {
        String echoText = cmd.message.getContent().replace("@here", "@ here").replace("@everyone", "@ everyone").replace(cmd.command[0], "");
        cmd.channel.sendMessage(echoText).queue();
    }


    // To make some sub command, just check the cmd[1] in a switch case
    // And make some method under this command to call.

    // Par exemple !info [user/channel/server].
    // if(cmd.lenght < 2) return;
    // switch(cmd[1]){
    //    case "user":
    //        this.userInfo(cmd);
    //        break;
    //    case "channel":
    //  [...]
    //

}
