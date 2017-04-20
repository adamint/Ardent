package tk.ardentbot.utils.rpg;

import java.text.DecimalFormat;

public class RPGUtils {
    public static final DecimalFormat moneyFormat = new DecimalFormat("#,###.00");

    public static String formatMoney(double money) {
        return "$" + moneyFormat.format(money);
    }
}
