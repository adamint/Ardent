package tk.ardentbot.rethink.models;

import lombok.Getter;
import lombok.Setter;
import net.dv8tion.jda.core.entities.User;
import tk.ardentbot.core.executor.BaseCommand;

import java.util.HashMap;

import static tk.ardentbot.rethink.Database.connection;
import static tk.ardentbot.rethink.Database.r;

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
    @Getter
    private String id;

    public Lottery(long endTime, long luck, int ticketCost) {
        entries = new HashMap<>();
        this.endTime = endTime;
        this.inAction = true;
        this.luck = luck;
        this.ticketCost = ticketCost;
        id = String.valueOf(generateId(r.table("lotteries").count().run(connection)));
        r.table("lotteries").insert(r.json(BaseCommand.getStaticGson().toJson(this))).run(connection);
    }

    private int generateId(int start) {
        if ((int) r.table("lotteries").get(start).count().run(connection) == 0) {
            return start;
        }
        else return generateId(start + 1);
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
