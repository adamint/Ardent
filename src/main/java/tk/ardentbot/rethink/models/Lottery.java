package tk.ardentbot.rethink.models;

import lombok.Getter;
import lombok.Setter;
import net.dv8tion.jda.core.entities.User;

import java.util.HashMap;

public class Lottery {
    @Getter
    @Setter
    private HashMap<String, Integer> entries;
    @Getter
    @Setter
    private int ticketCost;
    @Getter
    @Setter
    private long endTime;
    @Getter
    @Setter
    private boolean inAction;
    @Getter
    @Setter
    private long luck;

    public Lottery(long endTime, long luck, int ticketCost) {
        entries = new HashMap<>();
        this.endTime = endTime;
        this.inAction = true;
        this.luck = luck;
        this.ticketCost = ticketCost;
    }

    public void addTickets(User user, int amount) {
        assert entries != null;
        if (entries.containsKey(user.getId())) {
            int old = entries.get(user.getId());
            entries.replace(user.getId(), old, old + amount);
        }
        else entries.put(user.getId(), amount);
    }

    public int getPot() {
        final int[] pot = {0};
        entries.values().forEach(tickets -> pot[0] += (ticketCost * tickets));
        return pot[0];
    }
}
