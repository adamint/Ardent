package tk.ardentbot.rethink.models;

import lombok.Getter;

public class LoanModel {
    @Getter
    private String loaner_id;
    @Getter
    private String receiver_id;
    @Getter
    private double amount;
    @Getter
    private double interest_rate;
    @Getter
    private long payback_by_epoch_second;

    public LoanModel(String loaner_id, String receiver_id, double amount, double interest_rate, long payback_by_epoch_second) {
        this.loaner_id = loaner_id;
        this.receiver_id = receiver_id;
        this.amount = amount;
        this.interest_rate = interest_rate;
        this.payback_by_epoch_second = payback_by_epoch_second;
    }

    public double getEffectiveAmount() {
        return amount * (1 + (interest_rate / 100));
    }
}
