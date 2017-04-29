package tk.ardentbot.utils.games;

import lombok.Getter;
import lombok.Setter;

import java.security.SecureRandom;
import java.util.ArrayList;

public class Hand {
    @Getter
    private ArrayList<Card> cards = new ArrayList<>();

    public Hand() {
    }

    public String readable() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < cards.size(); i++) {
            Card card = cards.get(i);
            sb.append(card.getValue().getValue() + " " + card.getSuit().getEmoji());
            if (i < (cards.size() - 1)) sb.append(", ");
        }
        sb.append("\nTotal: " + total());
        return sb.toString();
    }

    public int total() {
        final int[] t = {0};
        cards.forEach(c -> {
            t[0] += c.getValue().getValue();
        });
        return t[0];
    }

    public Hand generate() {
        SecureRandom random = new SecureRandom();
        Value val = Value.values()[random.nextInt(Value.values().length)];
        if (val.getValue() == 11 && val.getValue() + total() > 21) val.setValue(1);
        cards.add(new Card(Suit.values()[random.nextInt(Suit.values().length)], val));
        return this;
    }

    public enum Suit {
        SPADES(":spades:"),
        DIAMONDS(":diamonds:"),
        CLUBS(":clubs:"),
        HEARTS(":hearts:"),;

        @Getter
        private String emoji;

        Suit(String s) {
            emoji = s;
        }
    }

    public enum Value {
        ACE("Ace", 11),
        TWO("Two", 2),
        THREE("Three", 3),
        FOUR("Four", 4),
        FIVE("Five", 5),
        SIX("Six", 6),
        SEVEN("Seven", 7),
        EIGHT("Eight", 8),
        NINE("Nine", 9),
        TEN("Ten", 10),
        JACK("Jack", 10),
        QUEEN("Queen", 10),
        KING("King", 10);

        @Getter
        private String name;
        @Getter
        @Setter
        private int value;

        Value(String name, int value) {
            this.name = name;
            this.value = value;
        }
    }

    public static class Card {
        @Getter
        private Suit suit;
        @Getter
        private Value value;

        public Card(Suit s, Value v) {
            suit = s;
            value = v;
        }
    }
}
