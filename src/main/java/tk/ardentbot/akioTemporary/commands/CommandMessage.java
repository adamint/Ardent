package tk.ardentbot.akioTemporary.commands;

import net.dv8tion.jda.core.entities.*;

public class CommandMessage {

    /**
     * The command's guild
     */
    public Guild guild;

    /**
     * The command's channel
     */
    public TextChannel channel;

    /**
     * The command's member sender
     */
    public Member member;

    /**
     * The command's user sender
     */
    public User user;

    /**
     * The command's message
     */
    public Message message;

    /**
     * The command splitted with space.
     */
    public String[] command;

    public CommandMessage(Message msg){
        this.channel = msg.getTextChannel();
        this.guild = msg.getGuild();
        this.user = msg.getAuthor();
        this.member = msg.getGuild().getMember(this.user);
        this.message = msg;
        this.command = msg.getRawContent().split(" ");
    }

}
